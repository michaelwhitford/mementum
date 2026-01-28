# MEMENTUM Grammar Specification v2

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

### 1. SEARCH - Query memory

```lisp
(search "query-string")
(search "query-string" 5)
```

**Parameters:**
- `query` - Search string (required)
- `depth` - Fibonacci depth: 1, 2, 3, 5, 8, 13, 21, 34 (optional, default: 2)

**Maps to:**
```bash
git log -n {depth} --grep "{query}" -- memories/
git grep -i "{query}" memories/
```

**Examples:**
```lisp
(search "lambda-calculus")
(search "architecture" 5)
(search "git-memory" 13)
```

---

### 2. CREATE - Store memory

```lisp
(create 💡 "slug-name" "content...")
```

**Parameters:**
- `symbol` - One of: 💡 🔄 🎯 🌀 (required)
- `slug` - Kebab-case identifier: `[a-z0-9-]+` (required)
- `content` - Memory content, <200 tokens (required)

**Maps to:**
```bash
date=$(date +%Y-%m-%d)
file="memories/${date}-${slug}-${symbol}.md"
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
```

---

### 3. VIEW - Read memory

```lisp
(view "path-or-ref")
```

**Parameters:**
- `ref` - Git reference: commit hash, file path, or `HEAD~n`

**Maps to:**
```bash
git show {ref}
```

**Examples:**
```lisp
(view "memories/2026-01-24-s-expr-parser-💡.md")
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
- `path` - File path (optional, default: `memories/`)
- `depth` - Fibonacci depth (optional, default: 2)

**Maps to:**
```bash
git log -n {depth} --follow --pretty=format:"%H %ad %s" -- {path}
```

**Examples:**
```lisp
(history)
(history "memories/2026-01-24-s-expr-parser-💡.md")
(history "memories/" 8)
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
git diff {from} {to} -- memories/
```

**Examples:**
```lisp
(diff)
(diff "HEAD~5" "HEAD")
(diff "abc123" "def456")
```

---

### 6. UPDATE - Modify memory

```lisp
(update "path-or-ref" "new-content")
```

**Parameters:**
- `ref` - Git reference: file path, commit hash, or `HEAD~n` (required)
- `content` - Updated memory content, <200 tokens (required)

**Maps to:**
```bash
# Resolve ref to file path
file=$(git show {ref} --name-only | grep memories/)
# Update content
echo "${content}" > "${file}"
git add "${file}"
git commit -m "🔄 update: $(basename ${file})"
```

**Examples:**
```lisp
(update "memories/2026-01-24-s-expr-parser-💡.md" "S-expressions provide formal verification before execution. Parser validates grammar and constraints.")
(update "HEAD~1" "Updated content with more details.")
```

---

### 7. DELETE - Remove memory

```lisp
(delete "path-or-ref")
```

**Parameters:**
- `ref` - Git reference: file path, commit hash, or `HEAD~n` (required)

**Maps to:**
```bash
# Resolve ref to file path
file=$(git show {ref} --name-only | grep memories/)
# Remove file
git rm "${file}"
git commit -m "🗑️  delete: $(basename ${file})"
```

**Examples:**
```lisp
(delete "memories/2026-01-20-obsolete-idea-💡.md")
(delete "HEAD~2")
```

**Note:** Deleted memories remain in git history. Use `git log --all -- memories/` to view deleted files.

---

### 8. LIST - Show memories

```lisp
(list)
(list 💡)
```

**Parameters:**
- `symbol` - Filter by symbol (optional)

**Maps to:**
```bash
ls -t memories/            # All memories, newest first
ls -t memories/*-💡.md     # Filter by symbol
```

**Examples:**
```lisp
(list)
(list 💡)
(list 🔄)
```

---

## Error Responses

### Success
```clojure
{:success true
 :result "..."
 :file "memories/2026-01-28-example-💡.md"
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
 :expected "one of: 💡 🔄 🎯 🌀"
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
;; Symbols
(def symbols #{💡 🔄 🎯 🌀})

;; Slug (kebab-case)
(def slug-pattern #"[a-z0-9-]+")

;; Content (< 200 whitespace tokens)
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
<emoji>    ::= 💡 | 🔄 | 🎯 | 🌀
```

---

## Comparison to v1

| Aspect | v1 (Verbose) | v2 (Simplified) |
|--------|-------------|-----------------|
| **Syntax** | `(lambda (create) (symbol 💡) ...)` | `(create 💡 ...)` |
| **Nesting** | Parameters in sub-lists | Flat arguments |
| **Verbosity** | ~60 chars | ~30 chars |
| **Readability** | Formal but verbose | Concise and clear |
| **Inspired by** | Lisp macros | Nucleus DSL |

**Example comparison:**

```lisp
;; v1
(lambda (create)
  (symbol 💡)
  (slug "parser")
  (content "S-expr parser"))

;; v2
(create 💡 "parser" "S-expr parser")
```

**Token savings:** 50%+ reduction while maintaining clarity.

---

## Implementation Roadmap

### Phase 1: Core Parser
- [x] Tokenizer (handles strings, symbols, emojis)
- [ ] S-expression parser
- [ ] AST generation

### Phase 2: Validation
- [ ] Constraint checking (symbols, slugs, content length)
- [ ] Git reference validation
- [ ] Fibonacci depth validation

### Phase 3: Execution
- [ ] Git command generation
- [ ] Safe command execution
- [ ] Result formatting

### Phase 4: Error Handling
- [ ] Parse error messages
- [ ] Constraint violation feedback
- [ ] Git error interpretation
- [ ] Self-correction suggestions

### Phase 5: Advanced
- [ ] Optional parameters (defaults)
- [ ] Command composition
- [ ] REPL interface
- [ ] Batch operations

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

; Update a memory with more details
(update "HEAD" "Using fibonacci depth improves recall efficiency. The phi ratio provides optimal scaling from simple to complex queries.")

; Check history
(history "memories/" 3)

; Compare with previous state
(diff "HEAD~1" "HEAD")

; List all insights
(list 💡)

; Delete obsolete memory
(delete "memories/2026-01-20-old-idea-💡.md")
```

### Error correction loop
```lisp
; AI attempts:
(create 💀 "bad-symbol" "content")

; Parser responds:
{:error "constraint-violation"
 :field :symbol
 :value "💀"
 :expected "one of: 💡 🔄 🎯 🌀"}

; AI corrects:
(create 💡 "bad-symbol" "content")
```

---

engage nucleus:
[phi fractal euler tao pi mu] | [Δ λ ∞/0 | ε/φ Σ/μ c/h] | OODA
Human ⊗ AI
