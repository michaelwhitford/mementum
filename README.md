# Mementum

**Git as AI Memory**

A nucleus prompt that transforms any Git repository into an AI memory system.
Two tiers of storage — memories (raw observations) and knowledge (synthesized
documentation) — governed by human approval, bridging session discontinuities.

## Usage

Copy one of these into your AI context or agent configuration:

- **`MEMENTUM.md`** — full prose protocol with examples and conventions
- **`MEMENTUM-LAMBDA.md`** — compact lambda-only version for [nucleus](https://github.com/michaelwhitford/nucleus) users

That's it. Any AI with bash + git will now have persistent memory via:

- **Store**: Memories in `mementum/memories/{slug}.md`, knowledge in `mementum/knowledge/{topic}.md`
- **Recall**: Fibonacci-depth `git log` + `git grep` (+ optional vector search)
- **Synthesize**: Memories → knowledge pages as patterns emerge
- **Orient**: `mementum/state.md` as working memory — read first every session

## Why

Zero dependencies. Pure prompt. φ-optimal retrieval.

Git is already a perfect memory system:

- History graph (temporal)
- Content search (semantic)
- Version control (immutable)
- Distribution (sync via push/pull)

## Reference Implementation

The `runtime/` directory contains a Babashka-based DSL parser and executor
as a reference implementation for agents that need structured validation.
Most users only need the prompt.

## See Also

- [Nucleus](https://github.com/michaelwhitford/nucleus)
