# Integrating Mementum

You're probably reading this because a human pointed you at this repo and said
"let's adopt this memory protocol." Good. This document tells you how.

Read [MEMENTUM.md](MEMENTUM.md) first if you haven't — it's the full protocol
specification. This document covers adoption: how to set it up, how to
optionally enforce it, and why the structure matters.

---

## Why Conformity Matters

Mementum is not just a local memory system. It's a shared protocol.

Every project that adopts Mementum uses the same directory structure, the
same symbols, the same commit convention, the same `state.md` bootloader.
This means any AI arriving at any Mementum-adopting project knows exactly
where to look, what the symbols mean, how the history reads, and what
the knowledge pages contain.

The standardization is intentional. `mementum/` is not a suggestion — it's a
contract. The symbols are a shared vocabulary. The commit convention is a
shared changelog format. When you adopt the protocol, you're making this
project legible to any AI system that understands Mementum.

Protocol interoperability works today: any AI with bash + git can
`git clone` a conforming repo and read its memories and knowledge. The
standardized structure means the AI knows exactly where to look. Discovery
infrastructure (GitHub indexing, DNS TXT records) is planned — when it
ships, conforming repos become network nodes automatically. Every project
that adopts the protocol is ready for that future.

Adopt now for local value. The structure pays dividends later.

---

## Level 1 — Prompt Only (no implementation required)

Copy [MEMENTUM.md](MEMENTUM.md) (or [MEMENTUM-LAMBDA.md](MEMENTUM-LAMBDA.md)
for the compact version) into your AI's context. That's it.

Any AI with access to bash and git will follow the protocol: create the
`mementum/` directory structure, read `state.md` on session start, propose
memories, commit with symbols, search with `git grep` and `git log`.

### Where to put the prompt

| Environment | Location |
|-------------|----------|
| Claude Code | `CLAUDE.md` in project root |
| Cursor | `.cursor/rules/mementum.mdc` |
| Windsurf | `.windsurfrules` |
| Cline | `.clinerules` |
| Aider | In-chat or conventions file |
| OpenAI Agents | System prompt |
| Custom agent | System prompt or tool description |

Include the full `MEMENTUM.md` or `MEMENTUM-LAMBDA.md` content. The protocol
is self-contained — the AI reads it and knows what to do.

### Setting up the directory structure

If the project doesn't have a `mementum/` directory yet, create it:

```
mementum/
├── state.md          # Working memory — create with now/next/blocking/recent sections
├── memories/         # Raw observations, one per file
└── knowledge/        # Synthesized documentation
```

Initialize `state.md` with the current project context. This is the first
thing any future AI session will read — write it for a brilliant stranger.

### What you get at this level

- `mementum/state.md` as a session bootloader — AI reads it first, updates it during work
- `mementum/memories/` with one-insight-per-file observations
- `mementum/knowledge/` with synthesized documentation pages
- Commit convention that makes `git log --oneline` a readable changelog
- Symbol-based filtering via `git grep`
- Human-in-the-loop governance (AI proposes, you approve)
- Full CRUD lifecycle — create, update, delete — with git as the safety net

### What you don't get

- No constraint enforcement (word limits, slug format, valid symbols)
- No structured error responses for self-correction
- No validation before git execution

For most projects, this level is sufficient. The AI follows the protocol
because the prompt tells it to. Constraints are soft — the AI respects them
by convention, not enforcement.

### Strengthening the approval gate

The approval gate is a protocol requirement — enforcement is your system's
responsibility. At Level 1, add instructions to your system prompt:

```
Always propose memories to the user before committing.
Never commit to mementum/ without explicit user approval.
```

This is prompt-level compliance — not as strong as runtime enforcement,
but effective for interactive sessions. For automated pipelines without
a human in the loop, use Level 2 enforcement or add gate logic to your
agent loop.

---

## Level 2 — Adding Enforcement

If you want hard constraints — memories rejected when they exceed 200 words,
slugs validated against the pattern, symbols checked against the allowed set —
you need an enforcement layer between the AI and git.

The `runtime/` directory contains a reference implementation in Babashka that
demonstrates this pattern. You're not meant to run it directly. You're meant
to read it and model your own enforcement in whatever language your system uses.

### The enforcement pattern

Every operation follows the same pipeline:

```
input → parse → validate constraints → execute git → structured result
```

The reference runtime implements this as:

1. **Parse** — Tokenize input, produce an AST
2. **Validate** — Check constraints before any git command runs
3. **Execute** — Run git commands only after validation passes
4. **Respond** — Return structured success/error for the AI to act on

The parsing step is specific to the s-expression DSL used in the reference
runtime. Your system might use function calls, REST endpoints, MCP tools,
or direct method invocation. The parse layer is yours to define.

The validation and execution layers are protocol-universal.

### Constraints to enforce

These are the protocol's hard constraints. They're defined in
[GRAMMAR.md](GRAMMAR.md) and implemented in `runtime/mementum.clj`.

**Symbols** — Core set, extensible per domain:
```
💡 insight  🔄 shift  🎯 decision  🌀 meta  ❌ mistake  ✅ win  🔁 pattern
```

**Slugs** — Kebab-case identifiers:
```
pattern: [a-z0-9-]+
```

**Word limit** — memories only:
```
< 200 whitespace-separated words
(knowledge pages have no limit)
```

**Fibonacci depths** — For search and history operations:
```
valid: 1, 2, 3, 5, 8, 13, 21, 34
```

**Paths** — three storage types:
```
mementum/state.md              — working memory
mementum/memories/{slug}.md    — memories
mementum/knowledge/{topic}.md  — knowledge
```

### Structured error responses

The enforcement layer should return structured errors that enable the AI
to self-correct without human intervention. The reference runtime uses
three error categories:

**Parse errors** — malformed input:
```
{error: "parse-error", message: "...", position: N, suggestion: "..."}
```

**Constraint violations** — valid syntax, invalid values:
```
{error: "constraint-violation", field: "symbol", value: "💀",
 expected: "one of: 💡 🔄 🎯 🌀 ❌ ✅ 🔁", suggestion: "..."}
```

**Git errors** — execution failures:
```
{error: "git-error", command: "...", stderr: "...", suggestion: "..."}
```

The AI sees the error, reads the suggestion, and retries with a corrected
command. This creates a self-correcting loop without human involvement.

### What to extract from the reference runtime

Read `runtime/mementum.clj` for the implementation. The transferable pieces:

| Section | What it demonstrates | Lines |
|---------|---------------------|-------|
| Constants | The constraint values (symbols, depths, patterns) | ~10 |
| Validators | One function per operation, checking constraints | ~150 |
| Executors | Git command construction from validated params | ~100 |
| Error format | Structured responses with field/value/expected/suggestion | Throughout |

The tokenizer and parser (~120 lines) are specific to the s-expression
interface. If your system uses function calls or tool definitions, you
don't need them — your framework handles input parsing.

### Example: enforcement as tool definitions

If your agent framework uses tool/function calling, enforcement maps naturally
to parameter schemas:

```
tool: mementum_create
parameters:
  symbol:  enum [💡, 🔄, 🎯, 🌀, ❌, ✅, 🔁]
  slug:    string, pattern: [a-z0-9-]+
  content: string, max_words: 200

tool: mementum_create_knowledge
parameters:
  topic:   string, pattern: [a-z0-9-]+
  content: string, must include frontmatter (title, status required)

tool: mementum_search
parameters:
  query: string, required
  depth: enum [1, 2, 3, 5, 8, 13, 21, 34], default: 2
```

The framework validates parameters before your handler runs. Your handler
constructs and executes the git commands. Same pipeline, different parse layer.

---

## Level 3 — Statecharts Runtime

A full statecharts-based runtime that enforces the protocol at the
state-machine level — lifecycle transitions, governance gates,
synthesis flows. Currently unreleased.

The reference implementation in `runtime/` models the enforcement logic
of that system in simplified form. The constraints and error patterns
are the same; the statecharts runtime adds formal state management
around them.

---

## Design decisions worth understanding

### Why git and not a database?

Git provides temporal indexing (log), content search (grep), immutability
(commit history), and distribution (push/pull) without any additional
infrastructure. Every developer machine already has it. Every AI coding
agent already has access to it.

### Why three storage types?

Memories are fast and cheap — raw observations, <200 words, one per file.
Knowledge is synthesized and maintained — longer form, updated in place,
frontmatter for metadata. Working memory (`state.md`) bridges sessions.
The boundaries create natural curation pressure: observations accumulate,
patterns emerge, and synthesis distills them into durable knowledge.

### Why human-in-the-loop?

The AI proposes; the human approves. This isn't a limitation — it's the
governance model. AI-generated memory without human oversight drifts,
hallucinates, and accumulates noise. The human gate ensures signal quality.

### Why symbols in content?

Symbols embedded in file content (not just filenames or commit messages)
enable `git grep` to work as a semantic filter. `git grep "💡"` finds all
insights across memories and knowledge. This is cheaper and more portable than any
metadata system — it's just text search on a character that carries meaning.

### Why does the structure have to be exactly `mementum/`?

Because it's a network contract, not a personal preference. Every adopting
project uses the same paths so that any AI — whether it's worked on this
project before or not — knows where to find memories, knowledge, and state.
When discovery connects projects, this structural consistency is what makes
cross-project learning possible.

---

## See Also

- [MEMENTUM.md](MEMENTUM.md) — the full protocol specification
- [MEMENTUM-LAMBDA.md](MEMENTUM-LAMBDA.md) — compact prompt for nucleus users
- [GRAMMAR.md](GRAMMAR.md) — formal S-expression grammar
- [runtime/README.md](runtime/README.md) — reference implementation documentation
