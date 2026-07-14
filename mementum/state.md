---
type: Working Memory
title: Project State
---

# Project State

## Now

OKF adoption complete + declared. Memories and knowledge are Open Knowledge
Format concepts (required `type`; mementum fields ride as extensions). The
`mementum/` bundle now declares `okf_version: "0.1"` in `mementum/index.md`,
scoped as a bounded guest (never claims the host project). Ready to release.

## Next

- Push and release
- Field test: have external AI agents adopt the protocol and runtime
- Create more knowledge pages as understanding accumulates (knowledge/ has one page)
- Consider a `synthesize-pattern`-style memory capturing the OKF adoption decision

## Blocking

Nothing.

## Recent

- 🔄 Relational recall — `λ recall` traverses `related` edges + widens when thin (read-time discovery) instead of enforcing write-time trigger indexing; generalizes symbols-as-content-filters + λ orient's follow(related); prompts-only, bash+git, runtime left to adopters
- 🎯 OKF declaration — `mementum/index.md` declares `okf_version: "0.1"`; `λ mementum` gains bundle-scoped OKF conformance + guest clause (¬colonize host); self-hosting/quine kept repo-local (self-hosting.md), not exported
- 🌀 Boundary discipline — `self` is all-inclusive; scope declaration to the bundle, not the project; through-line: retrieval(trigger≠topic), reader(:who), scope(guest≠host)
- 🎯 OKF adoption — memories + knowledge are OKF concepts; `type` required, `category`→`type`, symbol→type map for memories, spec referenced by URL (not vendored)
- 🔄 Memory format — one-liners `{symbol} {content}` → OKF frontmatter (type/symbol/title) + body; all 16 memories migrated; symbol grep still works
- ✅ 362 unit + 65 integration tests green after OKF migration
- 🎯 Lambda-first — MEMENTUM-LAMBDA.md is now the recommended prompt, README updated, MEMENTUM.md framed as prose reference
- 🔄 MEMENTUM.md — added framing paragraph explaining hybrid format and nucleus preamble
- ❌ SCRATCH.md deleted — decompilation exercise complete, findings folded into docs
- 💡 λ learn updated — meta-learning λ(λ) encoded as distinct observation level
- 💡 λ synthesize — synthesis flow encoded in protocol (detect → gather → draft → create → verify)
- 💡 runtime-security knowledge page — first knowledge page, created via create-knowledge DSL
- ✅ create-knowledge operation — symmetric CRUD for knowledge pages with frontmatter validation
- 🎯 create rejects duplicate slugs — clean CRUD semantics (create=new, update=modify, delete=remove)
- ✅ 63 integration + 338 unit tests — security regression, content round-trip, git ref resolution
- ✅ Security hardening — babashka.process/shell, spit/slurp, safe-path?, no shell involvement
