#!/usr/bin/env bb
;; MEMENTUM DSL Tests
;; Run with: bb mementum_test.clj

(require '[clojure.test :refer [deftest is testing run-tests]]
         '[clojure.string :as str])

;; Load the mementum code
(load-file "mementum.clj")

;; ============================================================================
;; Tokenizer Tests
;; ============================================================================

(deftest tokenize-basic-types
  (testing "Tokenize string literals"
    (let [tokens (tokenize "\"hello world\"")]
      (is (= 1 (count tokens)))
      (is (= :string (:type (first tokens))))
      (is (= "hello world" (:value (first tokens))))))
  
  (testing "Tokenize numbers"
    (let [tokens (tokenize "42")]
      (is (= :number (:type (first tokens))))
      (is (= 42 (:value (first tokens)))))
    (let [tokens (tokenize "3.14")]
      (is (= :number (:type (first tokens))))
      (is (= 3.14 (:value (first tokens)))))
    (let [tokens (tokenize "-5")]
      (is (= :number (:type (first tokens))))
      (is (= -5 (:value (first tokens))))))
  
  (testing "Tokenize symbols"
    (let [tokens (tokenize "search")]
      (is (= :symbol (:type (first tokens))))
      (is (= "search" (:value (first tokens))))))
  
  (testing "Tokenize emojis"
    (let [tokens (tokenize "💡")]
      ;; Emojis are tokenized as symbols but validated later
      (is (or (= :emoji (:type (first tokens)))
              (= :symbol (:type (first tokens)))))
      (is (= "💡" (:value (first tokens)))))))

(deftest tokenize-s-expressions
  (testing "Tokenize simple S-expression"
    (let [tokens (tokenize "(search \"test\")")]
      (is (= 4 (count tokens)))
      (is (= :lparen (:type (nth tokens 0))))
      (is (= :symbol (:type (nth tokens 1))))
      (is (= :string (:type (nth tokens 2))))
      (is (= :rparen (:type (nth tokens 3))))))
  
  (testing "Tokenize nested S-expression"
    (let [tokens (tokenize "(create 💡 \"slug\" \"content\")")]
      (is (= 6 (count tokens)))
      (is (= "create" (:value (nth tokens 1))))
      (is (= "💡" (:value (nth tokens 2))))
      (is (= "slug" (:value (nth tokens 3))))
      (is (= "content" (:value (nth tokens 4))))))
  
  (testing "Tokenize with whitespace"
    (let [tokens (tokenize "  ( search   \"test\"  )  ")]
      (is (= 4 (count tokens))))))

(deftest tokenize-escape-sequences
  (testing "Tokenize escaped quotes"
    (let [tokens (tokenize "\"hello \\\"world\\\"\"")]
      (is (= 1 (count tokens)))
      (is (= "hello \"world\"" (:value (first tokens))))))
  
  (testing "Tokenize escaped newlines"
    (let [tokens (tokenize "\"line1\\nline2\"")]
      (is (= "line1\nline2" (:value (first tokens)))))))

;; ============================================================================
;; Parser Tests
;; ============================================================================

(deftest parse-literals
  (testing "Parse string literal"
    (let [result (parse "\"hello\"")]
      (is (:success result))
      (is (= "hello" (:ast result)))))
  
  (testing "Parse number literal"
    (let [result (parse "42")]
      (is (:success result))
      (is (= 42 (:ast result)))))
  
  (testing "Parse emoji"
    (let [result (parse "💡")]
      (is (:success result))
      (is (= "💡" (:ast result))))))

(deftest parse-operations
  (testing "Parse search operation"
    (let [result (parse "(search \"test\")")]
      (is (:success result))
      (is (= "search" (get-in result [:ast :op])))
      (is (= ["test"] (get-in result [:ast :args])))))
  
  (testing "Parse search with depth"
    (let [result (parse "(search \"test\" 5)")]
      (is (:success result))
      (is (= "search" (get-in result [:ast :op])))
      (is (= ["test" 5] (get-in result [:ast :args])))))
  
  (testing "Parse create operation"
    (let [result (parse "(create 💡 \"slug\" \"content\")")]
      (is (:success result))
      (is (= "create" (get-in result [:ast :op])))
      (is (= ["💡" "slug" "content"] (get-in result [:ast :args])))))
  
  (testing "Parse list operation"
    (let [result (parse "(list)")]
      (is (:success result))
      (is (= "list" (get-in result [:ast :op])))
      (is (= [] (get-in result [:ast :args])))))
  
  (testing "Parse list with filter"
    (let [result (parse "(list 💡)")]
      (is (:success result))
      (is (= "list" (get-in result [:ast :op])))
      (is (= ["💡"] (get-in result [:ast :args]))))))

(deftest parse-errors
  (testing "Parse malformed S-expression"
    (let [result (parse "(search \"test\"")]
      (is (not (:success result)))
      (is (= "parse-error" (:error result)))))
  
  (testing "Parse empty input"
    (let [result (parse "")]
      (is (not (:success result)))
      (is (= "parse-error" (:error result)))))
  
  (testing "Parse unexpected tokens"
    (let [result (parse "(search \"test\") extra")]
      (is (not (:success result)))
      (is (= "parse-error" (:error result))))))

;; ============================================================================
;; Validator Tests
;; ============================================================================

(deftest validate-search-operation
  (testing "Valid search with query only"
    (let [result (validate-search ["query"])]
      (is (:valid result))
      (is (= "query" (:query result)))
      (is (= 2 (:depth result)))))
  
  (testing "Valid search with depth"
    (let [result (validate-search ["query" 5])]
      (is (:valid result))
      (is (= "query" (:query result)))
      (is (= 5 (:depth result)))))
  
  (testing "Invalid: empty query"
    (let [result (validate-search [""])]
      (is (not (:valid result)))
      (is (:error result))))
  
  (testing "Invalid: non-fibonacci depth"
    (let [result (validate-search ["query" 99])]
      (is (not (:valid result)))
      (is (= 99 (:value result)))))
  
  (testing "Invalid: missing query"
    (let [result (validate-search [])]
      (is (not (:valid result)))
      (is (:error result))))
  
  (testing "Invalid: non-string query"
    (let [result (validate-search [42])]
      (is (not (:valid result)))
      (is (:error result)))))

(deftest validate-create-operation
  (testing "Valid create"
    (let [result (validate-create ["💡" "test-slug" "content"])]
      (is (:valid result))
      (is (= "💡" (:symbol result)))
      (is (= "test-slug" (:slug result)))
      (is (= "content" (:content result)))))
  
  (testing "Invalid: wrong symbol"
    (let [result (validate-create ["💀" "slug" "content"])]
      (is (not (:valid result)))
      (is (= "constraint-violation" (:error result)))
      (is (= :symbol (:field result)))
      (is (= "💀" (:value result)))))
  
  (testing "Invalid: uppercase in slug"
    (let [result (validate-create ["💡" "Bad-Slug" "content"])]
      (is (not (:valid result)))
      (is (= "constraint-violation" (:error result)))
      (is (= :slug (:field result)))))
  
  (testing "Invalid: spaces in slug"
    (let [result (validate-create ["💡" "bad slug" "content"])]
      (is (not (:valid result)))
      (is (= "constraint-violation" (:error result)))
      (is (= :slug (:field result)))))
  
  (testing "Invalid: content too long"
    (let [long-content (str/join " " (repeat 300 "word"))
          result (validate-create ["💡" "slug" long-content])]
      (is (not (:valid result)))
      (is (= "constraint-violation" (:error result)))
      (is (= :content (:field result)))))
  
  (testing "Invalid: missing arguments"
    (let [result (validate-create ["💡"])]
      (is (not (:valid result)))
      (is (:error result)))))

(deftest validate-view-operation
  (testing "Valid view with path"
    (let [result (validate-view ["memories/file.md"])]
      (is (:valid result))
      (is (= "memories/file.md" (:ref result)))))
  
  (testing "Valid view with git ref"
    (let [result (validate-view ["HEAD"])]
      (is (:valid result))
      (is (= "HEAD" (:ref result)))))
  
  (testing "Invalid: missing ref"
    (let [result (validate-view [])]
      (is (not (:valid result)))
      (is (:error result))))
  
  (testing "Invalid: non-string ref"
    (let [result (validate-view [42])]
      (is (not (:valid result)))
      (is (:error result)))))

(deftest validate-update-operation
  (testing "Valid update"
    (let [result (validate-update ["memories/file.md" "new content"])]
      (is (:valid result))
      (is (= "memories/file.md" (:ref result)))
      (is (= "new content" (:content result)))))
  
  (testing "Invalid: content too long"
    (let [long-content (str/join " " (repeat 300 "word"))
          result (validate-update ["ref" long-content])]
      (is (not (:valid result)))
      (is (= "constraint-violation" (:error result)))
      (is (= :content (:field result)))))
  
  (testing "Invalid: missing arguments"
    (let [result (validate-update ["ref"])]
      (is (not (:valid result)))
      (is (:error result)))))

(deftest validate-delete-operation
  (testing "Valid delete"
    (let [result (validate-delete ["memories/file.md"])]
      (is (:valid result))
      (is (= "memories/file.md" (:ref result)))))
  
  (testing "Invalid: missing ref"
    (let [result (validate-delete [])]
      (is (not (:valid result)))
      (is (:error result)))))

(deftest validate-history-operation
  (testing "Valid history with defaults"
    (let [result (validate-history [])]
      (is (:valid result))
      (is (= "memories/" (:path result)))
      (is (= 2 (:depth result)))))
  
  (testing "Valid history with path"
    (let [result (validate-history ["memories/"])]
      (is (:valid result))
      (is (= "memories/" (:path result)))
      (is (= 2 (:depth result)))))
  
  (testing "Valid history with depth"
    (let [result (validate-history ["memories/" 8])]
      (is (:valid result))
      (is (= "memories/" (:path result)))
      (is (= 8 (:depth result)))))
  
  (testing "Invalid: non-fibonacci depth"
    (let [result (validate-history ["memories/" 99])]
      (is (not (:valid result)))
      (is (= 99 (:value result))))))

(deftest validate-diff-operation
  (testing "Valid diff with defaults"
    (let [result (validate-diff [])]
      (is (:valid result))
      (is (= "HEAD~1" (:from result)))
      (is (= "HEAD" (:to result)))))
  
  (testing "Valid diff with custom refs"
    (let [result (validate-diff ["HEAD~5" "HEAD~2"])]
      (is (:valid result))
      (is (= "HEAD~5" (:from result)))
      (is (= "HEAD~2" (:to result)))))
  
  (testing "Invalid: non-string ref"
    (let [result (validate-diff [42 "HEAD"])]
      (is (not (:valid result)))
      (is (:error result)))))

(deftest validate-list-operation
  (testing "Valid list with no filter"
    (let [result (validate-list [])]
      (is (:valid result))
      (is (nil? (:symbol result)))))
  
  (testing "Valid list with symbol filter"
    (let [result (validate-list ["💡"])]
      (is (:valid result))
      (is (= "💡" (:symbol result)))))
  
  (testing "Invalid: wrong symbol"
    (let [result (validate-list ["💀"])]
      (is (not (:valid result)))
      (is (= "constraint-violation" (:error result)))
      (is (= :symbol (:field result)))
      (is (= "💀" (:value result))))))

;; ============================================================================
;; Integration Validation Tests
;; ============================================================================

(deftest validate-full-operations
  (testing "Validate parsed search"
    (let [parse-result (parse "(search \"test\" 5)")
          validation (validate (:ast parse-result))]
      (is (:success validation))
      (is (= "search" (:op validation)))
      (is (= "test" (get-in validation [:params :query])))
      (is (= 5 (get-in validation [:params :depth])))))
  
  (testing "Validate parsed create"
    (let [parse-result (parse "(create 💡 \"test\" \"content\")")
          validation (validate (:ast parse-result))]
      (is (:success validation))
      (is (= "create" (:op validation)))
      (is (= "💡" (get-in validation [:params :symbol])))
      (is (= "test" (get-in validation [:params :slug])))
      (is (= "content" (get-in validation [:params :content])))))
  
  (testing "Validate unknown operation"
    (let [parse-result (parse "(unknown \"arg\")")
          validation (validate (:ast parse-result))]
      (is (not (:success validation)))
      (is (= "unknown-operation" (:error validation)))))
  
  (testing "Validate invalid symbol"
    (let [parse-result (parse "(create 💀 \"test\" \"content\")")
          validation (validate (:ast parse-result))]
      (is (not (:success validation)))
      (is (= "constraint-violation" (:error validation))))))

;; ============================================================================
;; Utility Tests
;; ============================================================================

(deftest token-count-test
  (testing "Count simple tokens"
    (is (= 3 (token-count "one two three")))
    (is (= 1 (token-count "single")))
    (is (= 0 (token-count "")))
    (is (= 0 (token-count "   "))))
  
  (testing "Count with multiple spaces"
    (is (= 3 (token-count "one    two     three"))))
  
  (testing "Count with newlines"
    (is (= 4 (token-count "one\ntwo\nthree\nfour")))))

(deftest valid-content-test
  (testing "Valid content under 200 tokens"
    (is (valid-content? "Short content"))
    (is (valid-content? (str/join " " (repeat 199 "word")))))
  
  (testing "Invalid content over 200 tokens"
    (is (not (valid-content? (str/join " " (repeat 200 "word")))))
    (is (not (valid-content? (str/join " " (repeat 300 "word")))))))

(deftest current-date-test
  (testing "Date format is YYYY-MM-DD"
    (let [date (current-date)]
      (is (string? date))
      (is (re-matches #"\d{4}-\d{2}-\d{2}" date)))))

;; ============================================================================
;; Constraint Constants Tests
;; ============================================================================

(deftest constraint-constants
  (testing "Valid symbols"
    (is (contains? symbols "💡"))
    (is (contains? symbols "🔄"))
    (is (contains? symbols "🎯"))
    (is (contains? symbols "🌀"))
    (is (= 4 (count symbols))))
  
  (testing "Valid fibonacci depths"
    (is (contains? fibonacci-depths 1))
    (is (contains? fibonacci-depths 2))
    (is (contains? fibonacci-depths 3))
    (is (contains? fibonacci-depths 5))
    (is (contains? fibonacci-depths 8))
    (is (contains? fibonacci-depths 13))
    (is (contains? fibonacci-depths 21))
    (is (contains? fibonacci-depths 34))
    (is (= 8 (count fibonacci-depths))))
  
  (testing "Valid slug pattern"
    (is (re-matches slug-pattern "test"))
    (is (re-matches slug-pattern "test-123"))
    (is (re-matches slug-pattern "a-b-c"))
    (is (not (re-matches slug-pattern "Test")))
    (is (not (re-matches slug-pattern "test_123")))
    (is (not (re-matches slug-pattern "test 123"))))
  
  (testing "Valid operations"
    (is (contains? operations "search"))
    (is (contains? operations "create"))
    (is (contains? operations "view"))
    (is (contains? operations "update"))
    (is (contains? operations "delete"))
    (is (contains? operations "history"))
    (is (contains? operations "diff"))
    (is (contains? operations "list"))
    (is (= 8 (count operations)))))

;; ============================================================================
;; Edge Cases
;; ============================================================================

(deftest edge-cases
  (testing "Empty S-expression"
    (let [result (parse "()")]
      (is (not (:success result)))))
  
  (testing "Multiple emojis"
    (let [result (parse "(create 💡🔄 \"test\" \"content\")")]
      (is (:success result))))
  
  (testing "Very long valid slug"
    (let [long-slug (str/join "-" (repeat 50 "a"))
          result (validate-create ["💡" long-slug "content"])]
      (is (:valid result))))
  
  (testing "Content exactly at limit"
    (let [content (str/join " " (repeat 199 "word"))
          result (validate-create ["💡" "test" content])]
      (is (:valid result))))
  
  (testing "Unicode in content"
    (let [result (validate-create ["💡" "test" "Content with 中文 and émojis 🎉"])]
      (is (:valid result))))
  
  (testing "Nested quotes in string"
    (let [result (parse "(create 💡 \"test\" \"She said \\\"hello\\\"\")")]
      (is (:success result))
      (is (= "She said \"hello\"" (last (get-in result [:ast :args]))))))
  
  (testing "Numbers in various positions"
    (let [result (parse "(search \"test\" 5)")]
      (is (:success result))
      (is (= 5 (last (get-in result [:ast :args]))))))
  
  (testing "All fibonacci depths are valid"
    (doseq [depth [1 2 3 5 8 13 21 34]]
      (let [result (validate-search ["test" depth])]
        (is (:valid result))
        (is (= depth (:depth result)))))))

;; ============================================================================
;; Regression Tests
;; ============================================================================

(deftest regression-tests
  (testing "Empty query should fail (bug fix)"
    (let [result (validate-search [""])]
      (is (not (:valid result)))
      (is (str/includes? (:error result) "empty"))))
  
  (testing "Symbol validation is case-sensitive"
    (let [result (validate-create ["💡" "test" "content"])]
      (is (:valid result)))
    (let [result (validate-create ["💀" "test" "content"])]
      (is (not (:valid result)))))
  
  (testing "Slug pattern rejects uppercase"
    (let [result (validate-create ["💡" "Test" "content"])]
      (is (not (:valid result))))
    (let [result (validate-create ["💡" "test" "content"])]
      (is (:valid result))))
  
  (testing "Missing closing paren detected"
    (let [result (parse "(search \"test\"")]
      (is (not (:success result)))
      (is (= "parse-error" (:error result))))))

;; ============================================================================
;; Run Tests
;; ============================================================================

(defn -main []
  (let [results (run-tests 'user)]
    (println)
    (println "==========================================")
    (println "Test Results Summary")
    (println "==========================================")
    (println (format "Tests run: %d" (:test results)))
    (println (format "Assertions: %d" (+ (:pass results) (:fail results) (:error results))))
    (println (format "Passed: %d" (:pass results)))
    (println (format "Failed: %d" (:fail results)))
    (println (format "Errors: %d" (:error results)))
    (println "==========================================")
    (System/exit (if (zero? (+ (:fail results) (:error results))) 0 1))))

(when (= *file* (System/getProperty "babashka.file"))
  (-main))
