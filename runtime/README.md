# MEMENTUM Runtime — Reference Implementation

Babashka-based parser, validator, and executor for the MEMENTUM DSL.
This is a reference implementation — adapt it to your system. Point your AI
at this code and tell it to integrate with your project.

## Features

- ✅ **S-expression parser** — tokenizes and parses MEMENTUM DSL with full Unicode support
- ✅ **Constraint validation** — enforces symbols, slugs, word limits, fibonacci depths
- ✅ **All storage types** — operates across `mementum/memories/`, `mementum/knowledge/`, and `mementum/state.md`
- ✅ **Git execution** — safe execution of git commands
- ✅ **Structured errors** — self-correcting feedback for AI agents
- ✅ **Fast** — Babashka provides instant startup

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
chmod +x runtime/mementum.clj
```

## Usage

Run from the repo root (where `mementum/` directory lives):

```bash
./runtime/mementum.clj '(operation arg1 arg2 ...)'
```

### Operations

#### SEARCH — Query memories and knowledge
```bash
./runtime/mementum.clj '(search "lambda")'
./runtime/mementum.clj '(search "architecture" 5)'
```

Searches across memories and knowledge. Returns temporal (git log) and semantic (git grep) results.

Symbols act as content-based filters:
- `(search "query")` — entire mementum directory
- `(search "💡")` — insights only
- `(search "architecture 🔄")` — pattern-shifts about architecture

#### CREATE — Store memory
```bash
./runtime/mementum.clj '(create 💡 "parser" "S-expression parser validates grammar")'
./runtime/mementum.clj '(create ❌ "shell-bug" "Unescaped content caused commit failures")'
```

Creates `mementum/memories/{slug}.md` and commits. Rejects if file already exists.

#### CREATE-KNOWLEDGE — Store knowledge page
```bash
./runtime/mementum.clj '(create-knowledge "architecture" "---\ntitle: Architecture\nstatus: open\n---\n\nContent...")'
```

Creates `mementum/knowledge/{topic}.md` and commits. Validates frontmatter (title and status required, status must be `open | designing | active | done`). No word limit. Rejects if file already exists.

#### READ — Read file or reference
```bash
./runtime/mementum.clj '(read "mementum/state.md")'
./runtime/mementum.clj '(read "mementum/memories/parser.md")'
./runtime/mementum.clj '(read "mementum/knowledge/architecture.md")'
./runtime/mementum.clj '(read "HEAD")'
```

#### UPDATE — Modify file
```bash
./runtime/mementum.clj '(update "mementum/memories/parser.md" "Updated content")'
```

Token limit (<200) applies to memories only. Knowledge pages have no limit.
Git preserves all history — previous versions always recoverable.

#### DELETE — Remove file
```bash
./runtime/mementum.clj '(delete "mementum/memories/obsolete.md")'
```

Removes file but preserves in git history. Recovery via `git log --all`.

#### HISTORY — Temporal traversal
```bash
./runtime/mementum.clj '(history)'
./runtime/mementum.clj '(history "mementum/memories/" 8)'
./runtime/mementum.clj '(history "mementum/knowledge/" 5)'
```

#### DIFF — Compare states
```bash
./runtime/mementum.clj '(diff)'
./runtime/mementum.clj '(diff "HEAD~5" "HEAD")'
```

#### LIST — Show files
```bash
./runtime/mementum.clj '(list)'
./runtime/mementum.clj '(list 💡)'
./runtime/mementum.clj '(list "mementum/knowledge/")'
```

Three modes: no args (all memories), symbol (grep content), path (ls directory).

## Output Format

### Success
```clojure
{:success true
 :file "mementum/memories/example.md"
 :commit "abc123"}
```

### Constraint Violation
```clojure
{:success false
 :error "constraint-violation"
 :field :symbol
 :value "💀"
 :expected "one of: #{\"💡\" \"🔄\" \"🎯\" \"🌀\" \"❌\" \"✅\" \"🔁\"}"
 :suggestion "(create 💡 \"slug\" \"content\")"}
```

### Parse Error
```clojure
{:success false
 :error "parse-error"
 :message "Expected string after operation name"
 :position 15
 :suggestion "(search \"query\") or (search \"query\" 5)"}
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

### Symbols (core set — extensible per domain)
`💡` insight · `🔄` shift · `🎯` decision · `🌀` meta · `❌` mistake · `✅` win · `🔁` pattern

### Slugs
Lowercase letters, numbers, hyphens: `[a-z0-9-]+`

### Content
Memories: < 200 whitespace-separated words. Knowledge pages: no limit.

### Fibonacci Depths
`1, 2, 3, 5, 8, 13, 21, 34` — used in SEARCH and HISTORY.

## Integration

### As a library
```clojure
#!/usr/bin/env bb
(load-file "runtime/mementum.clj")

(process "(search \"lambda\" 5)")
;; => {:success true :result {:temporal "..." :semantic "..."} :depth 5}
```

### Piping
```bash
echo '(list 💡)' | ./runtime/mementum.clj
```

### AI Agent Usage

The tool is designed for AI agents to:
1. Generate DSL from natural language
2. Execute safely with validated constraints
3. Self-correct from structured error responses

```
User: "Store my insight about parsers"
  ↓
Agent: (create 💡 "parser-insight" "S-expressions provide...")
  ↓
Tool:  {:success true :file "mementum/memories/parser-insight.md" :commit "abc123"}
```

## Testing

```bash
# Unit tests (run from runtime/ directory)
bb mementum_test.clj

# Integration tests (run from repo root)
bash runtime/test-dsl.sh
```

## License

Same as MEMENTUM project.
