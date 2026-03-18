#!/usr/bin/env bb
;; MEMENTUM DSL - Reference Implementation
;; Parser, Validator, and Executor for the MEMENTUM git memory protocol.
;; Run from repo root: ./runtime/mementum.clj '(create 💡 "slug" "content")'
;; See GRAMMAR.md for the full specification.

(require '[clojure.string :as str]
         '[clojure.java.io :as io]
         '[babashka.process :as proc]
         '[clojure.pprint :refer [pprint]])

;; ============================================================================
;; Constants & Constraints
;; ============================================================================

(def symbols #{"💡" "🔄" "🎯" "🌀" "❌" "✅" "🔁"})

(def fibonacci-depths #{1 2 3 5 8 13 21 34})

(def slug-pattern #"^[a-z0-9-]+$")

(def operations #{"search" "create" "read" "update" "delete" "history" "diff" "list"})

;; ============================================================================
;; Utilities
;; ============================================================================

(defn token-count
  "Count whitespace-separated tokens"
  [s]
  (count (re-seq #"\S+" s)))

(defn valid-content?
  "Check if content is under 200 words (whitespace-separated)"
  [s]
  (< (token-count s) 200))

(defn run-git
  "Execute git command with array args — no shell interpolation.
   Each argument is passed directly to the process; shell metacharacters
   (;, |, $, `, &&) are never interpreted."
  [& args]
  (let [result (apply proc/shell {:out :string :err :string :continue true}
                      "git" args)]
    (if (zero? (:exit result))
      {:success true
       :stdout (str/trim (:out result))
       :stderr (str/trim (:err result))}
      {:success false
       :stdout (str/trim (:out result))
       :stderr (str/trim (:err result))
       :exit (:exit result)})))

(defn run-grep
  "Execute grep with array args — no shell interpolation."
  [& args]
  (let [result (apply proc/shell {:out :string :err :string :continue true}
                      "grep" args)]
    (if (zero? (:exit result))
      {:success true
       :stdout (str/trim (:out result))
       :stderr (str/trim (:err result))}
      {:success false
       :stdout (str/trim (:out result))
       :stderr (str/trim (:err result))
       :exit (:exit result)})))

(defn safe-path?
  "Validate that a file path resolves within the current working directory.
   Prevents path traversal attacks (e.g. mementum/../../etc/passwd)."
  [path]
  (let [cwd (.getCanonicalPath (io/file "."))
        resolved (.getCanonicalPath (io/file path))]
    (str/starts-with? resolved cwd)))

(defn list-dir
  "List directory contents sorted by modification time, newest first.
   Excludes hidden files (dotfiles). Returns newline-separated filenames,
   or empty string if dir doesn't exist."
  [dir-path]
  (let [dir (io/file dir-path)]
    (if (.isDirectory dir)
      (->> (.listFiles dir)
           (remove #(str/starts-with? (.getName %) "."))
           (sort-by #(- (.lastModified %)))
           (map #(.getName %))
           (str/join "\n"))
      "")))

;; ============================================================================
;; Tokenizer
;; ============================================================================

(defn- match-emoji
  "Check if input at position i starts with any known emoji.
   Returns the matched emoji string or nil. Checks longest first
   to handle multi-codepoint sequences (e.g. ❌ = U+274C)."
  [input i]
  (some (fn [sym]
          (when (and (<= (+ i (count sym)) (count input))
                     (= sym (subs input i (+ i (count sym)))))
            sym))
        ;; Sort by length descending so multi-codepoint emojis match first
        (sort-by (comp - count) symbols)))

(defn tokenize
  "Convert input string to tokens.
   Handles Unicode correctly — emoji detection uses string-prefix matching
   rather than char-by-char comparison, so surrogate pairs and multi-codepoint
   sequences (variation selectors, ZWJ) work properly."
  [input]
  (let [input (str/trim input)]
    (loop [i 0
           tokens []]
      (if (>= i (count input))
        tokens
        (let [ch (get input i)]
          (cond
            ;; Whitespace - skip
            (Character/isWhitespace ch)
            (recur (inc i) tokens)
            
            ;; Left paren
            (= ch \()
            (recur (inc i) (conj tokens {:type :lparen :value "("}))
            
            ;; Right paren
            (= ch \))
            (recur (inc i) (conj tokens {:type :rparen :value ")"}))
            
            ;; String literal
            (= ch \")
            (let [[end-pos str-val]
                  (loop [j (inc i)
                         sb (StringBuilder.)]
                    (if (>= j (count input))
                      [j (str sb)]
                      (let [ch (get input j)]
                        (cond
                          (= ch \")
                          [(inc j) (str sb)]
                          
                          (= ch \\)
                          (if (< (inc j) (count input))
                            (let [next-ch (get input (inc j))]
                              (case next-ch
                                \n (.append sb "\n")
                                \t (.append sb "\t")
                                \r (.append sb "\r")
                                \\ (.append sb "\\")
                                \" (.append sb "\"")
                                (.append sb next-ch))
                              (recur (+ j 2) sb))
                            (do (.append sb ch)
                                (recur (inc j) sb)))
                          
                          :else
                          (do (.append sb ch)
                              (recur (inc j) sb))))))]
              (recur end-pos (conj tokens {:type :string :value str-val})))
            
            ;; Number
            (or (Character/isDigit ch) 
                (and (= ch \-) (< (inc i) (count input)) (Character/isDigit (get input (inc i)))))
            (let [start i
                  end (loop [j (inc i)]
                        (if (and (< j (count input))
                                 (or (Character/isDigit (get input j))
                                     (= (get input j) \.)))
                          (recur (inc j))
                          j))
                  num-str (subs input start end)
                  num (if (str/includes? num-str ".")
                        (Double/parseDouble num-str)
                        (Long/parseLong num-str))]
              (recur end (conj tokens {:type :number :value num})))
            
            ;; Emoji or Symbol/word
            ;; Try emoji first (string-prefix match handles surrogate pairs,
            ;; variation selectors, ZWJ sequences). Unknown emojis fall through
            ;; to word scanner and get caught by validation.
            :else
            (if-let [emoji (match-emoji input i)]
              (recur (+ i (count emoji)) (conj tokens {:type :emoji :value emoji}))
              (let [start i
                    end (loop [j i]
                          (if (and (< j (count input))
                                   (not (Character/isWhitespace (get input j)))
                                   (not (contains? #{\( \) \"} (get input j))))
                            (recur (inc j))
                            j))
                    sym (subs input start end)]
                (recur end (conj tokens {:type :symbol :value sym}))))))))))

;; ============================================================================
;; Parser
;; ============================================================================

(defn parse-expr
  "Parse tokens into AST"
  [tokens]
  (letfn [(parse [idx]
            (let [token (get tokens idx)]
              (cond
                (nil? token)
                {:error "Unexpected end of input" :idx idx}
                
                (= (:type token) :lparen)
                (let [op-token (get tokens (inc idx))]
                  (if (= (:type op-token) :symbol)
                    (loop [i (+ idx 2)
                           args []]
                      (let [t (get tokens i)]
                        (cond
                          (nil? t)
                          {:error "Missing closing paren" :idx i}
                          
                          (= (:type t) :rparen)
                          {:ast {:op (:value op-token) :args args} :next (inc i)}
                          
                          :else
                          (let [arg-result (parse i)]
                            (if (:error arg-result)
                              arg-result
                              (recur (:next arg-result) (conj args (:ast arg-result))))))))
                    {:error "Expected operation name after (" :idx (inc idx)}))
                
                (= (:type token) :string)
                {:ast (:value token) :next (inc idx)}
                
                (= (:type token) :number)
                {:ast (:value token) :next (inc idx)}
                
                (= (:type token) :emoji)
                {:ast (:value token) :next (inc idx)}
                
                (= (:type token) :symbol)
                {:ast (:value token) :next (inc idx)}
                
                :else
                {:error (str "Unexpected token: " token) :idx idx})))]
    (let [result (parse 0)]
      (if (:error result)
        result
        (if (< (:next result) (count tokens))
          {:error "Unexpected tokens after expression" :idx (:next result)}
          {:ast (:ast result)})))))

(defn parse
  "Main parse function"
  [input]
  (try
    (let [tokens (tokenize input)
          result (parse-expr tokens)]
      (if (:error result)
        {:success false
         :error "parse-error"
         :message (:error result)
         :position (:idx result)}
        {:success true
         :ast (:ast result)}))
    (catch Exception e
      {:success false
       :error "parse-error"
       :message (.getMessage e)})))

;; ============================================================================
;; Validators
;; ============================================================================

(defn validate-search
  "Validate search operation"
  [args]
  (cond
    (empty? args)
    {:error "search requires a query string"
     :suggestion "(search \"query\") or (search \"query\" 5)"}
    
    (not (string? (first args)))
    {:error "search query must be a string"
     :suggestion "(search \"query\")"}
    
    (empty? (first args))
    {:error "search query cannot be empty"
     :suggestion "(search \"query\")"}
    
    (and (> (count args) 1)
         (not (number? (second args))))
    {:error "search depth must be a number"
     :suggestion "(search \"query\" 5)"}
    
    (and (> (count args) 1)
         (not (contains? fibonacci-depths (second args))))
    {:error "search depth must be fibonacci"
     :expected (str "one of: " (sort fibonacci-depths))
     :value (second args)
     :suggestion "(search \"query\" 5)"}
    
    :else
    {:valid true
     :query (first args)
     :depth (or (second args) 2)}))

(defn validate-create
  "Validate create operation"
  [args]
  (cond
    (< (count args) 3)
    {:error "create requires symbol, slug, and content"
     :suggestion "(create 💡 \"slug\" \"content\")"}
    
    (not (contains? symbols (first args)))
    {:error "constraint-violation"
     :field :symbol
     :value (first args)
     :expected (str "one of: " symbols)
     :suggestion "(create 💡 \"slug\" \"content\")"}
    
    (not (string? (second args)))
    {:error "slug must be a string"
     :suggestion "(create 💡 \"slug\" \"content\")"}
    
    (not (re-matches slug-pattern (second args)))
    {:error "constraint-violation"
     :field :slug
     :value (second args)
     :expected "lowercase letters, numbers, and hyphens only"
     :suggestion "(create 💡 \"my-slug\" \"content\")"}
    
    (not (string? (nth args 2)))
    {:error "content must be a string"
     :suggestion "(create 💡 \"slug\" \"content\")"}
    
    (not (valid-content? (nth args 2)))
    {:error "constraint-violation"
     :field :content
     :value (str (token-count (nth args 2)) " tokens")
     :expected "< 200 words"
     :suggestion "Reduce content length"}
    
    :else
    {:valid true
     :symbol (first args)
     :slug (second args)
     :content (nth args 2)}))

(defn validate-read
  "Validate read operation"
  [args]
  (cond
    (empty? args)
    {:error "read requires a reference"
     :suggestion "(read \"mementum/memories/file.md\") or (read \"HEAD\")"}
    
    (not (string? (first args)))
    {:error "read reference must be a string"
     :suggestion "(read \"mementum/memories/file.md\")"}
    
    :else
    {:valid true
     :ref (first args)}))

(defn- memory-ref?
  "Check if a reference points to a memory (subject to word limit)"
  [ref]
  (or (str/starts-with? ref "mementum/memories/")
      ;; Git refs (HEAD, hashes) are ambiguous — apply word limit conservatively
      (not (str/starts-with? ref "mementum/knowledge/"))))

(defn validate-update
  "Validate update operation.
   Token limit (<200) applies only to memories, not knowledge pages."
  [args]
  (cond
    (< (count args) 2)
    {:error "update requires reference and content"
     :suggestion "(update \"path\" \"new content\")"}
    
    (not (string? (first args)))
    {:error "update reference must be a string"
     :suggestion "(update \"mementum/memories/file.md\" \"content\")"}
    
    (not (string? (second args)))
    {:error "content must be a string"
     :suggestion "(update \"path\" \"content\")"}
    
    (str/blank? (second args))
    {:error "content cannot be empty"
     :suggestion "Provide non-empty content, or use (delete \"path\") to remove"}
    
    (and (memory-ref? (first args))
         (not (valid-content? (second args))))
    {:error "constraint-violation"
     :field :content
     :value (str (token-count (second args)) " tokens")
     :expected "< 200 words (memories only)"
     :suggestion "Reduce content length, or use a knowledge page for longer content"}
    
    :else
    {:valid true
     :ref (first args)
     :content (second args)}))

(defn validate-delete
  "Validate delete operation"
  [args]
  (cond
    (empty? args)
    {:error "delete requires a reference"
     :suggestion "(delete \"mementum/memories/file.md\")"}
    
    (not (string? (first args)))
    {:error "delete reference must be a string"
     :suggestion "(delete \"mementum/memories/file.md\")"}
    
    :else
    {:valid true
     :ref (first args)}))

(def default-paths "mementum/memories/ mementum/knowledge/")

(defn validate-history
  "Validate history operation"
  [args]
  (cond
    (and (> (count args) 0)
         (not (string? (first args))))
    {:error "history path must be a string"
     :suggestion "(history \"mementum/memories/\")"}
    
    (and (> (count args) 1)
         (not (number? (second args))))
    {:error "history depth must be a number"
     :suggestion "(history \"mementum/memories/\" 5)"}
    
    (and (> (count args) 1)
         (not (contains? fibonacci-depths (second args))))
    {:error "history depth must be fibonacci"
     :expected (str "one of: " (sort fibonacci-depths))
     :value (second args)
     :suggestion "(history \"mementum/memories/\" 5)"}
    
    :else
    {:valid true
     :path (or (first args) default-paths)
     :depth (or (second args) 2)}))

(defn validate-diff
  "Validate diff operation"
  [args]
  (cond
    (and (> (count args) 0)
         (not (string? (first args))))
    {:error "diff from must be a string"
     :suggestion "(diff \"HEAD~1\" \"HEAD\")"}
    
    (and (> (count args) 1)
         (not (string? (second args))))
    {:error "diff to must be a string"
     :suggestion "(diff \"HEAD~1\" \"HEAD\")"}
    
    :else
    {:valid true
     :from (or (first args) "HEAD~1")
     :to (or (second args) "HEAD")}))

(defn validate-list
  "Validate list operation.
   Accepts emoji (symbol filter via content grep) or string (path filter)."
  [args]
  (cond
    ;; No args — list all memories and knowledge
    (empty? args)
    {:valid true
     :filter-type :default}
    
    ;; Known symbol — filter by content grep (emojis are strings in the AST)
    (contains? symbols (first args))
    {:valid true
     :filter-type :symbol
     :symbol (first args)}
    
    ;; String arg — path filter (e.g. "mementum/knowledge/")
    (string? (first args))
    {:valid true
     :filter-type :path
     :path (first args)}
    
    ;; Unknown non-string arg
    :else
    {:error "constraint-violation"
     :field :filter
     :value (first args)
     :expected (str "symbol (" symbols ") or path string")
     :suggestion "(list 💡) or (list \"mementum/knowledge/\")"}))

(defn validate
  "Validate AST"
  [ast]
  (if (not (map? ast))
    {:success false
     :error "parse-error"
     :message "Expected operation"}
    (let [op (:op ast)
          args (:args ast)]
      (if (not (contains? operations op))
        {:success false
         :error "unknown-operation"
         :operation op
         :expected (str "one of: " (sort operations))}
        (let [validation (case op
                          "search" (validate-search args)
                          "create" (validate-create args)
                          "read" (validate-read args)
                          "update" (validate-update args)
                          "delete" (validate-delete args)
                          "history" (validate-history args)
                          "diff" (validate-diff args)
                          "list" (validate-list args))]
          (if (:valid validation)
            {:success true
             :op op
             :params (dissoc validation :valid)}
            (merge {:success false} validation)))))))

;; ============================================================================
;; Executors
;; ============================================================================

(defn resolve-ref
  "Resolve git reference to file path.
   If ref is already a mementum/ path, use it directly.
   Otherwise resolve via git show against memories and knowledge.
   Returns {:file path} on success, {:error ...} on failure or ambiguity."
  [ref]
  (if (str/starts-with? ref "mementum/")
    {:file ref}
    (let [result (run-git "show" ref "--name-only" "--pretty=format:")]
      (if (:success result)
        (let [files (->> (str/split-lines (:stdout result))
                         (filter #(re-matches #"mementum/(memories|knowledge)/.*" %))
                         (remove str/blank?))]
          (case (count files)
            0 {:error "no-mementum-file"
               :suggestion (str "Commit " ref " does not contain a mementum file")}
            1 {:file (first files)}
            {:error "ambiguous-ref"
             :files files
             :suggestion "Use explicit path like (update \"mementum/memories/slug.md\" ...)"}))
        {:error "invalid-ref"
         :suggestion (str "Could not resolve reference: " ref)}))))

(defn exec-search
  "Execute search operation — no shell interpolation"
  [{:keys [query depth]}]
  (let [log-result (run-git "log" (str "-n" depth) (str "--grep=" query)
                            "--pretty=format:%h %ad %s" "--date=short"
                            "--" "mementum/memories/" "mementum/knowledge/")
        grep-result (run-git "grep" "-i" query "--" "mementum/")]
    {:success true
     :result {:temporal (:stdout log-result)
              :semantic (or (:stdout grep-result) "")}
     :depth depth}))

(defn exec-create
  "Execute create operation — uses spit for file writes, no shell"
  [{:keys [symbol slug content]}]
  (let [dir "mementum/memories"
        filepath (str dir "/" slug ".md")
        file-content (str symbol " " content)]
    (.mkdirs (io/file dir))
    (spit filepath file-content)
    (let [add-result (run-git "add" filepath)]
      (if (:success add-result)
        (let [commit-result (run-git "commit" "-m" (str symbol " " slug))]
          (if (:success commit-result)
            {:success true
             :file filepath
             :commit (str/trim (first (str/split (:stdout commit-result) #"\s")))}
            {:success false
             :error "git-error"
             :stderr (:stderr commit-result)
             :suggestion "Check if git repo is initialized"}))
        {:success false
         :error "git-error"
         :stderr (:stderr add-result)
         :suggestion "Check if git repo is initialized"}))))

(defn exec-read
  "Execute read operation — uses slurp for files, run-git for refs.
   Validates file paths stay within the working directory."
  [{:keys [ref]}]
  (if (str/starts-with? ref "mementum/")
    ;; File path — use slurp, with path traversal guard
    (if (not (safe-path? ref))
      {:success false
       :error "path-traversal"
       :ref ref
       :suggestion "Path must resolve within the project directory"}
      (if (.exists (io/file ref))
        {:success true
         :result (str/trim (slurp ref))
         :ref ref}
        {:success false
         :error "file-not-found"
         :ref ref
         :suggestion "Check if file exists"}))
    ;; Git reference — use git show with array args
    (let [result (run-git "show" ref)]
      (if (:success result)
        {:success true
         :result (:stdout result)
         :ref ref}
        {:success false
         :error "git-error"
         :ref ref
         :stderr (:stderr result)
         :suggestion "Check if reference exists"}))))

(defn exec-update
  "Execute update operation — uses spit for file writes, no shell"
  [{:keys [ref content]}]
  (let [resolved (resolve-ref ref)]
    (if (:error resolved)
      {:success false
       :error (:error resolved)
       :file ref
       :suggestion (:suggestion resolved)}
      (let [filepath (:file resolved)]
        (if (not (.exists (io/file filepath)))
          {:success false
           :error "file-not-found"
           :file filepath
           :suggestion "Check the file path — file must exist to update"}
          (do
            (spit filepath content)
            (let [add-result (run-git "add" filepath)]
              (if (:success add-result)
                (let [filename (last (str/split filepath #"/"))
                      commit-result (run-git "commit" "-m" (str "🔄 update: " filename))]
                  (if (:success commit-result)
                    {:success true
                     :file filepath
                     :commit (str/trim (first (str/split (:stdout commit-result) #"\s")))}
                    ;; Idempotent: if content unchanged, treat as successful no-op
                    (if (str/includes? (str (:stdout commit-result) (:stderr commit-result))
                                       "nothing to commit")
                      {:success true
                       :file filepath
                       :result "no-op: content unchanged"}
                      {:success false
                       :error "git-error"
                       :stderr (:stderr commit-result)
                       :suggestion "Check if file exists and content differs from current"})))
                {:success false
                 :error "git-error"
                 :stderr (:stderr add-result)
                 :suggestion "Check if git repo is initialized"}))))))))

(defn exec-delete
  "Execute delete operation — uses run-git with array args, no shell"
  [{:keys [ref]}]
  (let [resolved (resolve-ref ref)]
    (if (:error resolved)
      {:success false
       :error (:error resolved)
       :file ref
       :suggestion (:suggestion resolved)}
      (let [filepath (:file resolved)]
        (if (not (.exists (io/file filepath)))
          {:success false
           :error "file-not-found"
           :file filepath
           :suggestion "Check the file path — file must exist to delete"}
          (let [rm-result (run-git "rm" filepath)]
            (if (:success rm-result)
              (let [filename (last (str/split filepath #"/"))
                    commit-result (run-git "commit" "-m" (str "❌ delete: " filename))]
                (if (:success commit-result)
                  {:success true
                   :file filepath
                   :commit (str/trim (first (str/split (:stdout commit-result) #"\s")))}
                  {:success false
                   :error "git-error"
                   :stderr (:stderr commit-result)
                   :suggestion "Check if file exists"}))
              {:success false
               :error "git-error"
               :stderr (:stderr rm-result)
               :suggestion "Check if file exists"})))))))

(defn exec-history
  "Execute history operation — uses run-git with array args.
   Uses --follow for single files, omits it for directories."
  [{:keys [path depth]}]
  (let [use-follow (and (not (str/ends-with? path "/"))
                        (not (str/includes? path " ")))
        ;; Build args as a vector, then apply
        base-args ["log" (str "-n" depth)]
        follow-args (if use-follow ["--follow"] [])
        fmt-args ["--pretty=format:%h %ad %s" "--date=short" "--"]
        ;; Split space-separated paths (e.g. default "mementum/memories/ mementum/knowledge/")
        path-args (str/split (str/trim path) #"\s+")
        all-args (concat base-args follow-args fmt-args path-args)
        result (apply run-git all-args)]
    (if (:success result)
      {:success true
       :result (:stdout result)
       :path path
       :depth depth}
      {:success false
       :error "git-error"
       :stderr (:stderr result)})))

(defn exec-diff
  "Execute diff operation across memories and knowledge — uses run-git with array args"
  [{:keys [from to]}]
  (let [result (run-git "diff" from to "--" "mementum/memories/" "mementum/knowledge/")]
    (if (:success result)
      {:success true
       :result (:stdout result)
       :from from
       :to to}
      {:success false
       :error "git-error"
       :from from
       :to to
       :stderr (:stderr result)
       :suggestion "Check if git references exist"})))

(defn exec-list
  "Execute list operation — no shell interpolation.
   Supports three modes: default (all memories), symbol (grep content), path (ls directory)."
  [{:keys [filter-type symbol path]}]
  (case filter-type
    :symbol
    (let [result (run-grep "-rl" symbol "mementum/memories/" "mementum/knowledge/")]
      {:success true
       :result (or (:stdout result) "")
       :filter (str "symbol: " symbol)})

    :path
    {:success true
     :result (list-dir path)
     :filter (str "path: " path)}

    ;; :default — list all memories and knowledge
    (let [memories (list-dir "mementum/memories/")
          knowledge (list-dir "mementum/knowledge/")]
      {:success true
       :result (str (when (seq memories) (str "memories/\n" memories))
                    (when (and (seq memories) (seq knowledge)) "\n\n")
                    (when (seq knowledge) (str "knowledge/\n" knowledge)))
       :filter nil})))

(defn execute
  "Execute validated operation"
  [{:keys [op params]}]
  (case op
    "search" (exec-search params)
    "create" (exec-create params)
    "read" (exec-read params)
    "update" (exec-update params)
    "delete" (exec-delete params)
    "history" (exec-history params)
    "diff" (exec-diff params)
    "list" (exec-list params)))

;; ============================================================================
;; Main
;; ============================================================================

(defn git-repo?
  "Check if current directory is inside a git repository."
  []
  (:success (run-git "rev-parse" "--git-dir")))

(defn process
  "Parse, validate, and execute DSL expression"
  [input]
  (if (not (git-repo?))
    {:success false
     :error "no-git-repo"
     :message "Not inside a git repository"
     :suggestion "Run 'git init' or change to a directory with a git repo"}
    (let [parse-result (parse input)]
      (if (:success parse-result)
        (let [validation-result (validate (:ast parse-result))]
          (if (:success validation-result)
            (execute validation-result)
            validation-result))
        parse-result))))

(defn -main [& args]
  (if (empty? args)
    (do
      (println "MEMENTUM DSL - Parser, Validator, and Executor")
      (println "Reference implementation of the MEMENTUM git memory protocol.")
      (println)
      (println "Usage: ./mementum.clj '(operation args...)'")
      (println "  Run from repo root (where mementum/ directory lives).")
      (println)
      (println "Examples:")
      (println "  ./mementum.clj '(search \"architecture\" 5)'")
      (println "  ./mementum.clj '(create 💡 \"my-insight\" \"What I learned\")'")
      (println "  ./mementum.clj '(read \"mementum/state.md\")'")
      (println "  ./mementum.clj '(list 💡)'")
      (println "  ./mementum.clj '(list \"mementum/knowledge/\")'")
      (println "  ./mementum.clj '(update \"mementum/memories/my-insight.md\" \"Updated content\")'")
      (println "  ./mementum.clj '(delete \"mementum/memories/obsolete.md\")'")
      (println "  ./mementum.clj '(history)'")
      (println "  ./mementum.clj '(diff)'")
      (System/exit 1))
    (let [input (str/join " " args)
          result (process input)]
      (pprint result)
      (System/exit (if (:success result) 0 1)))))

;; Run if executed as script
(when (= *file* (System/getProperty "babashka.file"))
  (apply -main *command-line-args*))
