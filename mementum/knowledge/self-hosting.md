---
type: Meta
title: Mementum is Self-Hosting
status: active
description: This repo describes its own development using mementum — a property of the reference repository, not of the exported protocol.
tags: [self-hosting, reflexive, quine, boundary, meta]
related:
  - okf-format
depends-on: []
---

# Mementum is Self-Hosting

**Scope: this repo only.** This page documents a property of the mementum
*reference repository*, not of the mementum *protocol*. It is deliberately kept
out of the exported prompts (MEMENTUM.md / MEMENTUM-LAMBDA.md) — see
[Boundary](#boundary--why-this-stays-repo-local).

## The property

The mementum repo is itself a mementum bundle. It uses mementum to store its own
development memory:

- `state.md` — working memory tracking the development *of mementum*.
- `memories/` — the insights that produced the protocol, stored *by* the protocol.
- `knowledge/okf-format.md` — an OKF concept describing the OKF concept format
  that governs it. A concept describing the concept format.

So `describes(mementum) ∈ mementum`. The map lives inside the territory. The
repo is the protocol's own first user (dogfood), which is why the best reference
for what mementum is, is the live `mementum/` bundle here — not only the prose
(`assert: runtime(mementum/) > docs`).

## The fixed point

Because the repo is self-hosting, its OKF conformance declaration is itself an
OKF artifact: `mementum/index.md` (carrying `okf_version: "0.1"`) sits *inside*
the very bundle it declares. The declaration conforms to the format it declares
— a quine (`Y f = f (Y f)`).

This is not vicious circularity: it grounds out at **git** (the immutable
substrate the protocol rests on) and the **runtime conformance sweep**
(verifiable truth), neither of which is "self." Reflexive ≠ circular *because*
it terminates at runtime truth.

## Boundary — why this stays repo-local

`self` is all-inclusive; the protocol is a bounded **guest**. In an adopting
project, `mementum/` is a subtree, not the project's identity —
`mementum/ ⊊ host`. The self-hosting / quine property is true *here* only
because this repo is the one place where bundle and project coincide.

Encoding "the protocol describes itself in itself" into the exported prompt
would make every adopting project's memory layer claim to be the project —
mementum colonizing its host. The exported prompt therefore scopes every claim
to `mementum/` and declares only that the **bundle** is OKF-conformant. This
page is the reader-correct counterpart: its audience is *people working on
mementum*, not adopters.

## Same-pattern-as

Boundary discipline — keeping one layer from absorbing another — recurs
throughout mementum's design:

- **retrieval**: index a memory by its trigger (use-context), not its topic
- **reader**: encode each insight for the `:who` it is true for
- **scope**: the protocol is a guest in `mementum/`, never the host

The self-hosting property is safe precisely because that last boundary holds.
