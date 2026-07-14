---
type: Architecture
title: OKF — Knowledge & Memory Format
status: active
description: Mementum stores memories and knowledge as Open Knowledge Format (OKF) concepts, with a required `type` field and mementum-specific extensions.
tags: [okf, format, knowledge, memories, frontmatter, interoperability]
related:
  - runtime-security
depends-on: []
---

# OKF — Knowledge & Memory Format

Mementum's `memories/` and `knowledge/` are stored as
[Open Knowledge Format (OKF)](https://raw.githubusercontent.com/GoogleCloudPlatform/knowledge-catalog/refs/heads/main/okf/SPEC.md)
concepts. A `mementum/` repo is therefore a conformant **OKF knowledge bundle**:
every non-reserved `.md` file is a markdown document with a YAML frontmatter
block whose only required field is `type`. The spec is **referenced by URL, not
vendored** — no copy drifts in-repo.

## Core Principle

Interoperability in both directions. Any OKF-aware tool can read a mementum
repo; any OKF bundle can be consumed here. Achieved by conforming to OKF's
single hard requirement (`type`) while carrying mementum's own semantics as
**producer extensions** — which OKF explicitly permits and consumers must
preserve.

## Concept Format

### Memory

The event **symbol maps to the OKF `type`** (symbols *are* event-types); the
emoji is preserved as the `symbol` extension for the `git log` changelog and
`git grep` filtering.

```yaml
---
type: Insight        # REQUIRED — mapped from symbol
symbol: 💡           # extension — emoji for changelog + grep
title: my-slug       # recommended (slug by default)
---

Body — one insight, <200 words (frontmatter does not count).
```

Symbol → type: `💡` Insight · `🔄` Shift · `🎯` Decision · `🌀` Meta ·
`❌` Mistake · `✅` Win · `🔁` Pattern.

### Knowledge

`type` **replaces the old `category` field**. The mementum `status` lifecycle
and relationship fields ride along as extensions.

```yaml
---
type: Architecture   # REQUIRED (was `category`)
title: Page Title    # recommended
description: One-line summary   # recommended
status: active       # extension — open | designing | active | done
tags: [relevant, tags]
related: [other-page]      # extension
depends-on: [prereq-page]  # extension
---
```

Type values are producer-chosen and not registered centrally — e.g.
`Architecture`, `Design`, `Reference`, `Playbook`, `Explore`, `Experiment`.

## Conformance

A `mementum/` bundle is OKF-conformant when:

1. Every non-reserved `.md` has a parseable frontmatter block.
2. Every frontmatter block has a **non-empty `type`**.
3. `index.md` / `log.md` (reserved) follow the OKF structure *if present* —
   mementum treats them as optional, since `git log`, `(list)`, and `state.md`
   already cover history, listing, and working memory.

Everything else is soft: consumers tolerate unknown types, missing optional
fields, unknown keys, and broken cross-links.

## Cross-linking

Relationships use standard markdown links. Bundle-relative links (leading `/`,
resolved from the repo root) are preferred because they survive moves —
e.g. `[runtime security](/mementum/knowledge/runtime-security.md)`. The
`related` / `depends-on` frontmatter fields remain as typed-edge extensions.

## Enforcement (runtime)

The reference runtime (`runtime/mementum.clj`) enforces the format:

- `create` writes the OKF frontmatter, deriving `type` from the symbol via the
  `symbol->type` map. The `<200` word limit measures the **body** (frontmatter
  excluded via `content-body`).
- `create-knowledge` requires a non-empty `type`; `status` is validated **only
  when present** (`title`/`status` are no longer required). Missing `type`
  returns a `constraint-violation` with `:field :type`.

## Fears / Failure Modes

- **Adding a memory without frontmatter** → non-conformant bundle. The runtime
  prevents this on `create`; hand-written memories must include the block.
- **Reintroducing `category`** → dead field. It was folded into `type`; a page
  with `category` but no `type` fails conformance.
- **Requiring `title`/`status`** → over-strict. OKF makes only `type` required;
  keep the others optional so partial/agent-generated concepts remain valid.
- **Vendoring the spec** → drift. Reference the canonical URL; do not copy
  `SPEC.md` into the repo.

## Design Decisions

**Why `type` from the symbol (not a flat `type: Memory`)?** Symbols already
encode event-type (see `symbols-are-event-types`). Mapping them to OKF `type`
gives consumers meaningful concept kinds for free while the emoji stays usable
for the changelog and grep.

**Why keep `status`, `related`, `depends-on`?** They carry mementum's lifecycle
and graph semantics. OKF permits producer extensions, so interoperability costs
nothing to retain them.

**Why reference, not mirror, the spec?** A vendored copy drifts from upstream
and implies ownership. A URL tracks the source of truth.
