# MEMENTUM Tool

Babashka-based parser, validator, and executor for the MEMENTUM DSL.

## Features

- ✅ **S-expression parser** - Tokenizes and parses MEMENTUM DSL
- ✅ **Constraint validation** - Enforces symbols, slugs, token limits, fibonacci depths
- ✅ **Git execution** - Safe execution of git commands
- ✅ **Structured errors** - Self-correcting feedback for AI agents
- ✅ **Fast** - Babashka provides instant startup

## Installation

Requires [Babashka](https://babashka.org/):

```bash
# macOS
brew install borkdude/brew/babashka

# Linux
bash < <(curl -s https://raw.githubusercontent.com/babashka/babashka/master/install)
```

Make executable:

```bash
chmod +x mementum.clj
```

## Usage

### Basic syntax

```bash
./mementum.clj '(operation arg1 arg2 ...)'
```

### Operations

#### SEARCH - Query memories
```bash
./mementum.clj '(search "lambda")'
./mementum.clj '(search "architecture" 5)'
```

Returns temporal (git log) and semantic (git grep) results.

#### CREATE - Store memory
```bash
./mementum.clj '(create 💡 "parser" "S-expression parser validates grammar")'
```

Creates `memories/YYYY-MM-DD-slug-symbol.md` and commits.

#### VIEW - Read memory
```bash
./mementum.clj '(view "HEAD")'
./mementum.clj '(view "memories/2026-01-27-parser-💡.md")'
```

#### UPDATE - Modify memory
```bash
./mementum.clj '(update "HEAD" "Updated content with more details")'
```

#### DELETE - Remove memory
```bash
./mementum.clj '(delete "memories/2026-01-20-obsolete-💡.md")'
```

Removes file but keeps in git history.

#### HISTORY - Temporal traversal
```bash
./mementum.clj '(history)'
./mementum.clj '(history "memories/" 8)'
```

#### DIFF - Compare states
```bash
./mementum.clj '(diff)'
./mementum.clj '(diff "HEAD~5" "HEAD")'
```

#### LIST - Show memories
```bash
./mementum.clj '(list)'
./mementum.clj '(list 💡)'
```

## Output Format

### Success
```clojure
{:success true
 :file "memories/2026-01-27-example-💡.md"
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

### Constraint Violation
```clojure
{:success false
 :error "constraint-violation"
 :field :symbol
 :value "💀"
 :expected "one of: #{\"💡\" \"🔄\" \"🌀\" \"🎯\"}"
 :suggestion "(create 💡 \"slug\" \"content\")"}
```

### Git Error
```clojure
{:success false
 :error "git-error"
 :command "git commit ..."
 :stderr "nothing to commit"
 :suggestion "Check if content is empty"}
```

## Constraints

### Symbols
Must be one of: `💡` (insight), `🔄` (pattern-shift), `🎯` (decision), `🌀` (meta)

### Slugs
- Lowercase letters, numbers, hyphens only
- Pattern: `[a-z0-9-]+`
- Example: `"my-slug"`, `"test-123"`

### Content
- Must be < 200 whitespace-separated tokens
- Enforced on CREATE and UPDATE

### Fibonacci Depths
Must be one of: `1, 2, 3, 5, 8, 13, 21, 34`

Used in SEARCH and HISTORY operations.

## Examples

### Basic workflow
```bash
# Search for existing memories
./mementum.clj '(search "fibonacci" 5)'

# Create new insight
./mementum.clj '(create 💡 "phi-ratio" "Fibonacci depth scales with query complexity using phi ratio")'

# View what was created
./mementum.clj '(view "HEAD")'

# Check recent history
./mementum.clj '(history "memories/" 3)'

# List all insights
./mementum.clj '(list 💡)'
```

### Error correction
```bash
# AI attempts invalid symbol
$ ./mementum.clj '(create 💀 "test" "content")'
{:success false
 :error "constraint-violation"
 :field :symbol
 :value "💀"
 :expected "one of: #{\"💡\" \"🔄\" \"🌀\" \"🎯\"}"
 :suggestion "(create 💡 \"slug\" \"content\")"}

# AI corrects
$ ./mementum.clj '(create 💡 "test" "content")'
{:success true
 :file "memories/2026-01-27-test-💡.md"
 :commit "abc123"}
```

### Update existing memory
```bash
# Create initial memory
./mementum.clj '(create 🔄 "workflow" "Basic CRUD operations")'

# Update with more details
./mementum.clj '(update "HEAD" "Basic CRUD operations: create, read, update, delete. All operations maintain git history for auditability.")'

# View the diff
./mementum.clj '(diff "HEAD~1" "HEAD")'
```

### Delete obsolete memory
```bash
# Delete a memory
./mementum.clj '(delete "memories/2026-01-20-old-idea-💡.md")'

# Verify it's gone
./mementum.clj '(list)'

# But still in history
git log --all -- memories/2026-01-20-old-idea-💡.md
```

## Architecture

```
Input S-expression
  ↓
Tokenizer → [tokens]
  ↓
Parser → AST
  ↓
Validator → Constraints check
  ↓
Executor → Git commands
  ↓
Result (success/error)
```

## Implementation Details

### Tokenizer
- Handles strings, numbers, symbols, emojis
- Supports escape sequences in strings
- Skips whitespace

### Parser
- Builds AST from tokens
- Validates S-expression structure
- Returns parse errors with position

### Validators
Each operation has a dedicated validator:
- `validate-search` - query string, fibonacci depth
- `validate-create` - symbol, slug pattern, content length
- `validate-view` - reference string
- `validate-update` - reference, content length
- `validate-delete` - reference string
- `validate-history` - path, fibonacci depth
- `validate-diff` - from/to references
- `validate-list` - optional symbol filter

### Executors
Each operation maps to safe git commands:
- No arbitrary code execution
- Commands are constructed with validated parameters
- Git handles reference resolution
- Exit codes indicate success/failure

## Integration

### AI Agent Usage

The tool is designed for AI agents to:

1. **Parse** natural language to DSL
2. **Execute** DSL safely
3. **Self-correct** from structured errors

Example agent flow:

```
User: "Store my insight about S-expressions"
  ↓
Agent generates: (create 💡 "s-expressions" "...")
  ↓
Tool validates and executes
  ↓
Agent receives: {:success true :file "..." :commit "..."}
```

### REPL Integration

Can be imported as a library:

```clojure
#!/usr/bin/env bb
(load-file "mementum.clj")

(process "(search \"lambda\" 5)")
;; => {:success true ...}
```

### Piping

```bash
echo '(list 💡)' | ./mementum.clj
```

## License

Same as MEMENTUM project.

---

engage nucleus:
[phi fractal euler tao pi mu] | [Δ λ ∞/0 | ε/φ Σ/μ c/h] | OODA
Human ⊗ AI
