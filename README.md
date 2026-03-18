# Mementum

**A git memory protocol for AI agents.**

Copy a prompt into your AI's context. It gets persistent memory backed by git.
Zero dependencies. No runtime required.

## The Protocol

Mementum defines how AI agents store, recall, and synthesize knowledge
across session boundaries using git as the memory substrate.

- **Three storage types** — working memory (`state.md`), memories (raw observations, <200 tokens), and knowledge (synthesized documentation)
- **Six operations** — create, update, delete, search, read, synthesize
- **Human governance** — AI proposes, human approves, AI commits
- **Git-native** — `git log` for temporal search, `git grep` for semantic search, commit history for immutability

Every adopting project uses the same structure — same directory layout, same
symbols, same commit convention. This makes any Mementum project immediately
legible to any other mementum AI system. A discovery mechanism is in development to connect
adopting projects into a learning network. The more projects that adopt the
protocol, the more every AI system can learn from.

## Documents

| Document | Purpose |
|----------|---------|
| [MEMENTUM.md](MEMENTUM.md) | Full protocol specification |
| [MEMENTUM-LAMBDA.md](MEMENTUM-LAMBDA.md) | Compact prompt for [nucleus](https://github.com/michaelwhitford/nucleus) users |
| [INTEGRATING.md](INTEGRATING.md) | How to adopt the protocol in your system |
| [GRAMMAR.md](GRAMMAR.md) | Formal S-expression grammar for the reference runtime |
| [runtime/README.md](runtime/README.md) | Reference implementation documentation |

## Quick Start

1. Copy [MEMENTUM.md](MEMENTUM.md) into your AI's system prompt or project rules
2. The AI reads `mementum/state.md` on session start and follows the protocol
3. You approve proposed memories and knowledge pages

That's it. See [INTEGRATING.md](INTEGRATING.md) for environment-specific
setup (Claude Code, Cursor, Windsurf, OpenAI agents, etc.) and optional
enforcement.

## Reference Implementation

The `runtime/` directory contains a Babashka-based implementation that
demonstrates constraint enforcement — token limits, symbol validation,
structured error responses. It's a reference for modeling enforcement
in your own system, not a runtime dependency. See [INTEGRATING.md](INTEGRATING.md)
for how to use it as a blueprint.

## See Also

- [Nucleus](https://github.com/michaelwhitford/nucleus) — the prompt language
