# Mementum

**Git as AI Memory**

A nucleus prompt that transforms any Git repository into an AI memory system.

## Usage

Copy `MEMENTUM.md` into your AI context or agent configuration.

That's it. Any AI with bash + git will now have persistent memory via:

- **Store**: Markdown files in `memories/{symbol}-{date}-{slug}.md`
- **Recall**: Fibonacci-weighted `git log` + `git grep`
- **Index**: Symbolic tags (🧠💡🔄⚡🌀)

## Why

Zero dependencies. Pure prompt. φ-optimal retrieval.

Git is already a perfect memory system:

- History graph (temporal)
- Content search (semantic)
- Version control (immutable)
- Distribution (sync via push/pull)

## Reference Implementation

The `runtime/` directory contains a Babashka-based DSL parser and executor as a reference implementation for agents that need structured validation. Most users only need the pure prompt.

## See Also

- [Nucleus](https://github.com/michaelwhitford/nucleus)
