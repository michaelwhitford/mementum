# MEMENTUM Grammar Specification

## Architecture

```
Human (natural language)
  ↓
AI generates S-expression
  ↓
Parser (validates syntax)
  ↓
Executor (runs git commands)
  ↓
Result (success/error feedback)
```

## Philosophy

Inspired by [Matryoshka](https://github.com/yogthos/Matryoshka)'s Nucleus DSL:
- **Reduced entropy** - Simple grammar, fewer ways to express operations
- **Fail-fast** - Parser rejects malformed commands before execution
- **Safe execution** - Only predefined git operations allowed
- **Self-correcting** - Structured errors enable AI to retry

## S-Expression Grammar

All operations use flat S-expression syntax: `(operation arg1 arg2 ...)`

Operations work across both tiers (`mementum/memories/` and `mementum/knowledge/`)
unless otherwise noted.

### 1. SEARCH - Query memories and knowledge

```lisp
(search "query-string")
(search "query-string" 5)
```

**Parameters:**
- `query` - Search string (required)
- `depth` - Fibonacci depth: 1, 2, 3, 5, 8, 13, 21, 34 (optional, default: 2)

**Maps to:**
```bash
git log -n {depth} --grep "{query}" --pretty=format:"%h %ad %s" --date=short -- mementum/memories/ mementum/knowledge/
git grep -i "{query}"
```

**Examples:**
```lisp
(search "lambda-calculus")
(search "architecture" 5)
(search "git-memory" 13)
```

**Search Strategies:**

Symbols act as **content-based filters** rather than path restrictions:

- **Broad search** - `(search "query")` → entire repository
- **By type** - `(search "💡")` → insights only
- **Combined** - `(search "architecture 🔄")` → pattern-shifts in architecture
- **Decisions** - `(search "decisions 🎯")` → decisions about topic

The symbol system creates a **natural query language** built into content itself.

---

### 2. CREATE - Store memory (Tier 1)

```lisp
(create 💡 "slug-name" "content...")
```

**Parameters:**
- `symbol` - One of: 💡 🔄 🎯 🌀 ❌ ✅ 🔁 (required, extensible per domain)
- `slug` - Kebab-case identifier: `[a-z0-9-]+` (required)
- `content` - Memory content, <200 tokens (required)

**Maps to:**
```bash
file="mementum/memories/${slug}.md"
echo "${content}" > "${file}"
git add "${file}"
git commit -m "${symbol} ${slug}"
```

**Examples:**
```lisp
(create 💡 "s-expr-parser" "S-expressions provide formal verification before execution.")
(create 🔄 "fibonacci-depth" "Search depth scales with complexity using phi ratio.")
(create 🎯 "token-budget" "Limit memories to 200 tokens for efficient recall.")
(create 🌀 "meta-learning" "AI learns to store what it struggles to solve repeatedly.")
(create ❌ "shell-injection" "Unescaped content in shell commands caused commit failures.")
(create ✅ "two-tier-design" "Separating memories from knowledge reduced noise in recall.")
```

**Note:** For knowledge pages (Tier 2), use the AI's normal file-writing
capabilities — knowledge pages require frontmatter and are typically longer
than what fits in a single S-expression argument. See MEMENTUM.md §VII.

---

### 3. VIEW - Read file or reference

```lisp
(view "path-or-ref")
```

**Parameters:**
- `ref` - File path, commit hash, or `HEAD~n` (required)

**Maps to:**
```bash
# File path — read directly
cat {ref}
# Git reference — show from history
git show {ref}
```

**Examples:**
```lisp
(view "mementum/memories/s-expr-parser.md")
(view "mementum/knowledge/architecture.md")
(view "mementum/state.md")
(view "HEAD~1")
(view "abc123")
```

---

### 4. HISTORY - Temporal traversal

```lisp
(history)
(history "path")
(history "path" 5)
```

**Parameters:**
- `path` - File or directory path (optional, default: `mementum/memories/ mementum/knowledge/`)
- `depth` - Fibonacci depth (optional, default: 2)

**Maps to:**
```bash
git log -n {depth} --follow --pretty=format:"%H %ad %s" --date=short -- {path}
```

**Examples:**
```lisp
(history)
(history "mementum/memories/s-expr-parser.md")
(history "mementum/memories/" 8)
(history "mementum/knowledge/" 5)
```

---

### 5. DIFF - Compare states

```lisp
(diff)
(diff "HEAD~2" "HEAD")
```

**Parameters:**
- `from` - Git reference (optional, default: `HEAD~1`)
- `to` - Git reference (optional, default: `HEAD`)

**Maps to:**
```bash
git diff {from} {to} -- mementum/memories/ mementum/knowledge/
```

**Examples:**
```lisp
(diff)
(diff "HEAD~5" "HEAD")
(diff "abc123" "def456")
```

---

### 6. UPDATE - Modify file

```lisp
(update "path-or-ref" "new-content")
```

**Parameters:**
- `ref` - File path, commit hash, or `HEAD~n` (required)
- `content` - Updated content (required). Token limit applies only to tier-1 memories (<200 tokens).

**Maps to:**
```bash
# Resolve ref to file path
file=$(git show {ref} --name-only | grep -E 'mementum/(memories|knowledge)/' | head -1)
# Update content
echo "${content}" > "${file}"
git add "${file}"
git commit -m "🔄 update: $(basename ${file})"
```

**Examples:**
```lisp
(update "mementum/memories/s-expr-parser.md" "S-expressions provide formal verification before execution. Parser validates grammar and constraints.")
(update "HEAD~1" "Updated content with more details.")
```

**Note:** Git preserves all history. Previous versions are always recoverable.

---

### 7. DELETE - Remove file

```lisp
(delete "path-or-ref")
```

**Parameters:**
- `ref` - File path, commit hash, or `HEAD~n` (required)

**Maps to:**
```bash
# Resolve ref to file path
file=$(git show {ref} --name-only | grep -E 'mementum/(memories|knowledge)/' | head -1)
# Remove file
git rm "${file}"
git commit -m "🗑️  delete: $(basename ${file})"
```

**Examples:**
```lisp
(delete "mementum/memories/obsolete-idea.md")
(delete "HEAD~2")
```

**Note:** Deleted files remain in git history. Use `git log --all -- mementum/memories/deleted-file.md` to recover.

---

### 8. LIST - Show files

```lisp
(list)
(list 💡)
(list "mementum/knowledge/")
```

**Parameters:**
- `filter` - Symbol (searches content) or path (lists directory). Optional.

**Maps to:**
```bash
# No filter — list all memories
ls -t mementum/memories/

# Symbol filter — grep content across both tiers
grep -rl "💡" mementum/memories/ mementum/knowledge/ 2>/dev/null

# Path filter — list specific directory
ls -t mementum/knowledge/
```

**Examples:**
```lisp
(list)
(list 💡)
(list 🎯)
(list "mementum/knowledge/")
```

---

## Error Responses

### Success
```clojure
{:success true
 :result "..."
 :file "mementum/memories/example.md"
 :commit "abc123"}
```

### Parse Error
```clojure
{:success false
 :error "parse-error"
 :message "Expected string after operation name"
 :position 15
 :suggestion "(search \"query\") or (search \"query\" 5)"}
```

### Constraint Error
```clojure
{:success false
 :error "constraint-violation"
 :field :symbol
 :value "💀"
 :expected "one of: 💡 🔄 🎯 🌀 ❌ ✅ 🔁"
 :suggestion "(create 💡 \"slug\" \"content\")"}
```

### Git Error
```clojure
{:success false
 :error "git-error"
 :command "git commit -m ..."
 :stderr "nothing to commit"
 :suggestion "Check if content is empty"}
```

---

## Constraint Specification

```clojure
;; Symbols (core set — extensible per domain)
(def symbols #{"💡" "🔄" "🎯" "🌀" "❌" "✅" "🔁"})

;; Slug (kebab-case)
(def slug-pattern #"[a-z0-9-]+")

;; Content (< 200 whitespace tokens, tier-1 only)
(defn valid-content? [s]
  (< (count (re-seq #"\S+" s)) 200))

;; Fibonacci depths
(def depths #{1 2 3 5 8 13 21 34})

;; Non-empty string
(defn non-empty? [s]
  (and (string? s) (not (empty? s))))
```

---

## Parser Implementation

### Token Types
```clojure
{:lparen    "("
 :rparen    ")"
 :string    "\"...\""
 :number    123
 :symbol    word
 :emoji     💡}
```

### Parsing Rules
```clojure
(defn parse [input]
  (-> input
      tokenize
      parse-term
      validate-constraints))

(defn parse-term [tokens]
  (match tokens
    [:lparen op & args :rparen] {:op op :args args}
    _ {:error "malformed s-expression"}))
```

### Validation
```clojure
(defn validate-create [args]
  (let [[symbol slug content] args]
    (and (contains? symbols symbol)
         (re-matches slug-pattern slug)
         (valid-content? content))))
```

---

## Grammar BNF

```ebnf
<expr>     ::= "(" <op> <args> ")"
<op>       ::= "search" | "create" | "view" | "history" | "diff" | "update" | "delete" | "list"
<args>     ::= <arg>*
<arg>      ::= <string> | <number> | <symbol> | <emoji>
<string>   ::= '"' [^"]* '"'
<number>   ::= [0-9]+
<symbol>   ::= [a-zA-Z0-9-]+
<emoji>    ::= 💡 | 🔄 | 🎯 | 🌀 | ❌ | ✅ | 🔁
```

---

## Usage Examples

### Basic workflow
```lisp
; Search for relevant memories
(search "git-memory" 5)

; Create new memory
(create 💡 "search-optimization" "Using fibonacci depth improves recall efficiency")

; View recently created
(view "HEAD")

; View working memory
(view "mementum/state.md")

; Update a memory with more details
(update "HEAD" "Using fibonacci depth improves recall efficiency. The phi ratio provides optimal scaling from simple to complex queries.")

; Check history across both tiers
(history "mementum/" 3)

; Compare with previous state
(diff "HEAD~1" "HEAD")

; List all insights
(list 💡)

; List knowledge pages
(list "mementum/knowledge/")

; Delete obsolete memory
(delete "mementum/memories/old-idea.md")
```

### Error correction loop
```lisp
; AI attempts:
(create 💀 "bad-symbol" "content")

; Parser responds:
{:error "constraint-violation"
 :field :symbol
 :value "💀"
 :expected "one of: 💡 🔄 🎯 🌀 ❌ ✅ 🔁"}

; AI corrects:
(create 💡 "bad-symbol" "content")
```

---

engage nucleus:
[phi fractal euler tao pi mu] | [Δ λ ∞/0 | ε/φ Σ/μ c/h] | OODA
Human ⊗ AI
