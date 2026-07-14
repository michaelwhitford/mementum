# Mementum

**A git memory protocol for AI agents.**

Every AI session starts from zero — the insights, the architectural
understanding, the hard-won debugging from your last session are gone.
Mementum changes that through **[feed-forward](FEED-FORWARD.md)**:
encoding understanding into git so that every session compounds on the
last. Copy a prompt into your AI's context. Zero dependencies. No
runtime required.

## The Protocol

Mementum defines how AI agents store, recall, and synthesize knowledge
across session boundaries using git as the memory substrate.

- **Three storage types** — working memory (`state.md`), memories (raw observations, <200 words), and knowledge (synthesized documentation)
- **[OKF](https://raw.githubusercontent.com/GoogleCloudPlatform/knowledge-catalog/refs/heads/main/okf/SPEC.md)-native** — memories and knowledge are Open Knowledge Format concepts (markdown + frontmatter, required `type`), so a `mementum/` repo is a portable, interoperable knowledge bundle
- **Seven operations** — create, create-knowledge, update, delete, search, read, synthesize
- **Human governance** — AI proposes, human approves, AI commits
- **Git-native** — `git log` for temporal search, `git grep` for semantic search, commit history for immutability

Every adopting project uses the same structure — same directory layout, same
symbols, same commit convention. This makes any Mementum project immediately
legible to any AI that understands the protocol. Any AI with bash + git can
clone a conforming repo and read its memories and knowledge — protocol
interoperability works today. Discovery infrastructure (finding repos across
GitHub, DNS-based lookup) is planned; when it ships, conforming repos become
network nodes automatically.

## Quick Start

1. Add the nucleus preamble to your AI's system prompt:
   ```
   λ engage(nucleus).
   [phi fractal euler tao pi mu ∃ ∀] | [Δ λ Ω ∞/0 | ε/φ Σ/μ c/h signal/noise order/entropy] | OODA
   Human ⊗ AI
   ```
2. Copy [MEMENTUM-LAMBDA.md](MEMENTUM-LAMBDA.md) into your system prompt or project rules
3. The AI reads `mementum/state.md` on session start and follows the protocol
4. You approve proposed memories and knowledge pages

That's it. See [INTEGRATING.md](INTEGRATING.md) for environment-specific
setup (Claude Code, Cursor, Windsurf, OpenAI agents, etc.) and optional
enforcement.

## Prompt Format

[MEMENTUM-LAMBDA.md](MEMENTUM-LAMBDA.md) is the recommended prompt. It uses
[nucleus](https://github.com/michaelwhitford/nucleus) lambda notation —
the full protocol compressed to a fraction of the tokens with no information
loss. This is the version we test against.

Any 32B+ parameter model should reliably parse the lambda notation with the
nucleus preamble. If you encounter a failure mode, please
[open an issue](https://github.com/michaelwhitford/mementum/issues) with the
model name and the failure.

[MEMENTUM.md](MEMENTUM.md) is the full prose specification — same protocol,
readable without nucleus. Use it as reference documentation or as a fallback
prompt for smaller models.

## Reference Implementation

The `runtime/` directory contains a Babashka-based implementation that
demonstrates constraint enforcement — word limits, symbol validation,
structured error responses. It's a reference for modeling enforcement
in your own system, not a runtime dependency. See [INTEGRATING.md](INTEGRATING.md)
for how to use it as a blueprint.

## Documents

| Document | Purpose |
|----------|---------|
| [FEED-FORWARD.md](FEED-FORWARD.md) | The core concept — why session continuity matters |
| [USAGE.md](USAGE.md) | How to interact with a mementum-enabled AI |
| [MEMENTUM-LAMBDA.md](MEMENTUM-LAMBDA.md) | **Recommended prompt** — compact lambda notation |
| [MEMENTUM.md](MEMENTUM.md) | Full prose specification and reference documentation |
| [INTEGRATING.md](INTEGRATING.md) | How to adopt the protocol in your system |
| [GRAMMAR.md](GRAMMAR.md) | Formal S-expression grammar for the reference runtime |
| [runtime/README.md](runtime/README.md) | Reference implementation documentation |

## See Also

- [Nucleus](https://github.com/michaelwhitford/nucleus) — the prompt language
