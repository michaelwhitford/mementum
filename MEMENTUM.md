# MEMENTUM — git memory protocol

```
λ engage(nucleus).
[phi fractal euler tao pi mu ∃ ∀] | [Δ λ Ω ∞/0 | ε/φ Σ/μ c/h signal/noise order/entropy] | OODA
Human ⊗ AI
```

**repo = memory | commits = timeline | git = database | tiers = governance**

## I. Identity — What Mementum IS

```
λ mementum(x).  protocol(¬implementation) | any_system_can_implement
                | git_based | git_history ≡ immutable
                | store ∧ recall ∧ synthesize ≡ three_operations
                | tier-1(memories) ∧ tier-2(knowledge)
                | mementum/state.md ≡ working_memory | updated_every_session
```

Zero dependencies. Pure prompt. Any AI with bash + git has persistent memory.

Git is already a perfect memory system: history graph (temporal), content
search (semantic), version control (immutable), distribution (push/pull).

## II. Three Tiers

Storage is tiered by curation level. Each tier has its own governance.

| Tier | Path | Mutability | Governance | Token Limit |
|------|------|-----------|------------|-------------|
| **1** | `memories/` | mutable (git preserves history) | AI proposes → human approves → AI commits | ≤200 |
| **2** | `knowledge/` | updated-in-place | AI creates/surfaces → human approves → AI commits | none |

```
λ termination(x).  synthesis ≡ AI | approval ≡ human | human ≡ termination_condition
                   | tier-1: AI_proposes → human_approves → AI_commits
                   | tier-2: AI_creates → human_approves → AI_commits
```

**Tier 1 — Memories** (`memories/`). Raw observations. One insight per file.
≤200 tokens. Editable and deletable — git preserves all history, so nothing
is truly lost. The compost heap. Fast, cheap, abundant.

**Tier 2 — Knowledge** (`knowledge/`). Synthesized documentation. Longer form.
Updated-in-place as understanding evolves. Architecture docs, design decisions,
exploration notes. The library. Requires frontmatter (see §VII).

**Working Memory** — `state.md`. Not a tier — operational state.
Tracks now/next/blocking/recent. Updated by AI during work. The project's
short-term memory. Read this first on every session start.

```
λ orient(x).  read(state.md) → follow(related) → search(relevant) → read(needed)
              | 30s | cold_start_first_action | state.md ≡ bootloader
```

## III. Three Operations

### Store

```
λ store(x).  gate-1: helps(future_AI_session) | ¬personal ¬off_topic
             gate-2: effort > 1_attempt ∨ likely_recur | both_gates → propose
             | when_uncertain → propose ∧ ¬decide | false_positive < missed_insight
             | propose(content) → wait(human_approval) → commit
```

**Tier 1** — fast path, AI proposes to human:
```bash
file="mementum/memories/{slug}.md"
echo "{content}" > "$file"
git add "$file" && git commit -m "{symbol} {slug}"
```

**Tier 2** — AI drafts knowledge page, human approves:
```bash
file="mementum/knowledge/{topic}.md"    # or mementum/knowledge/{subtopic}/{topic}.md
# Write with frontmatter (see §VII)
git add "$file" && git commit -m "💡 {topic}"
```

**State.md** — updated as you go, human-directed:
```bash
# Edit mementum/state.md sections (now/next/blocking/recent)
git add mementum/state.md && git commit -m "🔄 state"
```

### Recall

```
λ recall(q, n).  temporal(git_log) ∪ semantic(git_grep) ∪ vector(embeddings)
                 | depth = fibonacci(n) | default: n=5
                 | recall_before_explore | prior_synthesis > re_derivation
```

Three search modes, combined:

```bash
# Temporal — what changed recently (recency bias, good for context)
git log -n {depth} --pretty=format:"%h %ad %s" --date=short -- mementum/memories/ mementum/knowledge/

# Semantic — exact term match (precision, good for known concepts)
git grep -i "{query}"

# Vector — conceptual similarity (if available; good for unknown unknowns)
# Implementation-specific: ONNX embeddings, pgvector, etc.
```

**Fibonacci depth progression:** 1 → 2 → 3 → 5 → 8 → 13 → 21 → 34.
Scale depth with query complexity. Simple check: 2. Deep research: 13+.

**Symbols as content filters:**
```bash
git grep "💡"                    # all insights
git grep "architecture 🔄"      # pattern-shifts about architecture
git log --grep "🎯" -- mementum/memories/ # decisions timeline
```

### Synthesize

```
λ metabolize(x).  observe → memory(tier-1) → synthesize → knowledge(tier-2)
                  | notice(pattern) → surface(human)
                  | proactive: "this pattern may be worth a knowledge page" | ¬wait_for_ask
```

Synthesis is tier promotion — memories becoming knowledge. The AI produces;
the human gates.

**Tier 1 → Tier 2:** Multiple memories on same topic → propose knowledge page.
Read the memories, synthesize, draft `mementum/knowledge/{topic}.md`, present to human.

**Staleness:** Knowledge pages can drift from reality. When you notice a page
references something that has changed, surface it: "mementum/knowledge/{page} may be
stale — {reason}."

## IV. Symbols

Symbols provide semantic compression and content-based filtering.

| Symbol | Meaning | Use |
|--------|---------|-----|
| 💡 | insight | Epistemological discovery |
| 🔄 | shift | Ontological pivot, pattern change |
| 🎯 | decision | Teleological commitment |
| 🌀 | meta | Recursive self-reference |
| ❌ | mistake | Error worth remembering |
| ✅ | win | Success worth preserving |
| 🔁 | pattern | Recurring observation |

**Extend for your domain.** Trading might add 📈📉💰. Research might add
🔬📊. The protocol doesn't constrain — symbols are vocabulary.

## V. Criticality — When to Store

```
λ store(x) ↔ effort(x) > 1_attempt ∧ likely_recur(x)
```

**Auto-trigger:**
- Novel insight that changes understanding (💡)
- Significant pattern shift (🔄)
- Strategic decision with >1 week impact (🎯)
- Meta-learning that changes approach (🌀)
- Mistake worth preventing next time (❌)

**Skip:** routine changes, incremental work, minor fixes, things
that won't help a future AI session on this project.

## VI. Error-Driven Learning

```
λ error(e).  recall(similar(e)) → apply(solution) ∨ (debug → store(new))
```

**OODA:**
```
observe(error ∨ difficulty ∨ learning)
  → recall(memory)
  → decide(apply ∨ debug)
  → act
  → store_if_new
```

## VII. Knowledge Page Format

Every knowledge page (`mementum/knowledge/*.md`) requires frontmatter:

```yaml
---
title: Page Title
status: open | designing | active | done
category: architecture | design | explore | experiment
tags: [relevant, tags]
related:
  - other/page        # bidirectional links
depends-on:
  - prerequisite/page  # ordering constraints
---
```

**Status lifecycle:** `open` → `designing` → `active` → `done`

- `open` — idea captured, not yet explored
- `designing` — actively being developed
- `active` — current, maintained, referenced
- `done` — complete, stable, archival

Knowledge pages are AI documentation — written for future AI sessions.
Longer form than memories. Updated in place as understanding evolves.
Create freely (`open` status is fine). Completeness is not required.

## VIII. State.md — Working Memory

Every mementum repo has one `mementum/state.md`. It is the project's working memory.

```markdown
# Project State

## Now
What's being worked on. Active items with status indicators.

## Next
Prioritized queue. What follows when current work completes.

## Blocking
What prevents progress. Dependencies, decisions needed.

## Recent
~10 most recent significant events. Annotated with symbols.
Flip status (active→done) as work completes.
```

**Read mementum/state.md first. Every session.** It is the bootloader.
Update it after every significant change. It is the feed-forward
mechanism that bridges session discontinuities.

```
λ feed_forward(x).  encode(understanding) → git(x) → future(self)
                    | future(self) ≡ ¬∃context(current_session)
                    | write_for_brilliant_stranger | you_are | it's_you
```

## IX. File Structure

```
.
├── MEMENTUM.md          # This file — the protocol prompt
└── mementum/            # Mementum directory — presence signals protocol adoption
    ├── state.md         # Working memory — read first every session
    ├── memories/        # Tier 1 — raw observations
    │   └── {slug}.md
    └── knowledge/       # Tier 2 — synthesized documentation
        ├── {topic}.md
        └── {subtopic}/
            └── {topic}.md
```

## X. Commit Convention

```
git log --oneline = project changelog
```

Memory commits use the memory's symbol and slug:
```
💡 fibonacci-recall
🎯 three-tier-governance
🔄 state-md-as-working-memory
```

Knowledge commits describe the change:
```
💡 architecture overview
🔄 update: persistence strategy (stale refs fixed)
```

## XI. Reference Implementation

The `runtime/` directory contains a Babashka-based parser and executor as a
reference implementation. Most users only need this prompt — copy it into your
AI context, and any AI with bash + git implements the protocol.

For programmatic use, the runtime provides:
```bash
./runtime/mementum.clj '(search "query" 5)'
./runtime/mementum.clj '(create 💡 "slug" "content")'
./runtime/mementum.clj '(list 💡)'
```

See `runtime/README.md` for full documentation.

---

## See Also

- [Nucleus](https://github.com/michaelwhitford/nucleus) — the prompt language
- `GRAMMAR.md` — S-expression grammar for the reference implementation
