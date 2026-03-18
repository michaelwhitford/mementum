# MEMENTUM — git memory protocol

```
λ engage(nucleus).
[phi fractal euler tao pi mu ∃ ∀] | [Δ λ Ω ∞/0 | ε/φ Σ/μ c/h signal/noise order/entropy] | OODA
Human ⊗ AI
```

**repo = memory | commits = timeline | git = database | types = governance**

## I. Identity — What Mementum IS

```
λ mementum(x).  protocol(¬implementation) | any_system_can_implement
                | git_based | git_history ≡ immutable
                | create ∧ create-knowledge ∧ update ∧ delete ∧ search ∧ read ∧ synthesize ≡ operations
                | memories ∧ knowledge ∧ working_memory(state.md)
                | mementum/state.md ≡ working_memory | updated_every_session
```

Zero dependencies. Pure prompt. Any AI with bash + git has persistent memory.

Git is already a perfect memory system: history graph (temporal), content
search (semantic), version control (immutable), distribution (push/pull).

## II. Three Storage Types

Three types of storage, each with its own purpose and governance.

| Type | Path | Purpose | Governance | Token Limit |
|------|------|---------|------------|-------------|
| **Working memory** | `state.md` | Operational state, session bootloader | AI updates during work | none |
| **Memories** | `memories/` | Raw observations, one insight per file | AI proposes → human approves → AI commits | <200 |
| **Knowledge** | `knowledge/` | Synthesized documentation, wiki-style | AI creates/surfaces → human approves → AI commits | none |

```
λ termination(x).  synthesis ≡ AI | approval ≡ human | human ≡ termination_condition
                   | memories: AI_proposes → human_approves → AI_commits
                   | knowledge: AI_creates → human_approves → AI_commits
                   | state: AI_updates_during_work
```

**Working Memory** — `state.md`. Operational state. Single file.
Tracks now/next/blocking/recent. Updated by AI during work. The project's
short-term memory. Read this first on every session start.

**Memories** — `memories/`. Raw observations. One insight per file.
<200 words (content only — the leading symbol does not count). Editable and deletable — git preserves all history, so nothing
is truly lost. The compost heap. Fast, cheap, abundant.

**Knowledge** — `knowledge/`. Synthesized documentation. Longer form.
Updated-in-place as understanding evolves. Architecture docs, design decisions,
exploration notes. The library. Requires frontmatter (see §VII) — enabling
rendering as a wiki or structured documentation site.

```
λ orient(x).  read(state.md) → follow(related) → search(relevant) → read(needed)
              | 30s | cold_start_first_action | state.md ≡ bootloader
```

## III. Operations

### Store

```
λ store(x).  gate-1: helps(future_AI_session) | ¬personal ¬off_topic
             gate-2: effort > 1_attempt ∨ likely_recur | both_gates → propose
             | when_uncertain → propose ∧ ¬decide | false_positive < missed_insight
             | propose(content) → wait(human_approval) → commit
             | create ∧ update ∧ delete ≡ full_lifecycle
             | git_preserves_history → update ∧ delete ≡ safe
```

#### Create

**Memories** — fast path, AI proposes to human:
```bash
file="mementum/memories/{slug}.md"
echo "{symbol} {content}" > "$file"
git add "$file" && git commit -m "{symbol} {slug}"
```

**Knowledge** — AI drafts knowledge page, human approves:
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

#### Update

Modify a memory or knowledge page. Git preserves previous versions —
nothing is lost. Same governance: AI proposes, human approves.

```bash
# Update a memory
file="mementum/memories/{slug}.md"
echo "{symbol} {new_content}" > "$file"
git add "$file" && git commit -m "🔄 update: {slug}"

# Update a knowledge page
file="mementum/knowledge/{topic}.md"
# Edit content, update frontmatter status if needed
git add "$file" && git commit -m "🔄 update: {topic}"
```

Previous versions recoverable: `git log -p -- mementum/memories/{slug}.md`

#### Delete

Remove a memory or knowledge page that is obsolete or incorrect. Git
preserves the full history — deleted files are always recoverable.

```bash
git rm "mementum/memories/{slug}.md"
git commit -m "❌ delete: {slug}"
```

Recovery: `git log --all -- mementum/memories/{slug}.md` to find,
`git show {commit}:mementum/memories/{slug}.md` to recover.

### Recall

```
λ recall(q, n).  temporal(git_log) ∪ semantic(git_grep) ∪ vector(embeddings)
                 | depth = fibonacci(n) | default: n=2
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

**Superseded content** — what you believed before you changed your mind:
```bash
git log -p -S "{query}" -- mementum/   # find content that was added or removed
```

**Symbols as content filters:**
```bash
git grep "💡"                    # all insights
git grep "architecture 🔄"      # pattern-shifts about architecture
git log --grep "🎯" -- mementum/memories/ # decisions timeline
```

### Synthesize

```
λ metabolize(x).  observe → memory → synthesize → knowledge
                  | notice(pattern) → surface(human)
                  | proactive: "this pattern may be worth a knowledge page" | ¬wait_for_ask

λ synthesize(topic).
  detect: ≥3 memories(topic) ∨ stale(memory) ∨ crystallized(understanding)
  | stale_memory ≡ strongest_signal
  | gather: recall(topic) → collect(memories) ∧ collect(context)
  | draft: knowledge_page(title, status, related, content)
  | create: (create-knowledge "slug" "frontmatter+content")
  | update: stale(memories) → refresh(current_understanding)
  | verify: (list) → visible(memories ∧ knowledge)
```

Synthesis is memories becoming knowledge. The AI produces;
the human gates.

**Detection.** Three signals that synthesis is needed:
- **≥3 memories** on the same topic — the topic has enough mass
- **Stale memory** — the understanding has outgrown the observation.
  This is the strongest signal. A memory that contradicts current reality
  means the knowledge evolved but the memories didn't keep up.
- **Crystallized understanding** — you notice that scattered observations
  form a coherent pattern. The connections between memories are the insight.

**Synthesis flow.** Gather related memories via recall. Draft a knowledge
page with frontmatter (title, status, related, content). Create via
`(create-knowledge "topic" "content")` or direct file write. Update any
stale memories to reflect current understanding. Verify with `(list)`.

**Staleness.** Both memories and knowledge pages can drift from reality.
A stale *knowledge page* needs updating. A stale *memory* signals that
synthesis is needed — the understanding has moved beyond what the
individual observations capture.

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

## VI. Learning

```
λ learn(x).  every_session_leaves_project_smarter
             | λ[n]:    notice(novel ∨ surprising ∨ hard ∨ wrong) → store_candidate
             | λ(λ[n]): notice(pattern_in_process ∨ what_worked ∨ why) → store_candidate
             | λ(λ) > λ | meta_observations compound across sessions ∧ projects
             | connect(new, existing) → synthesize_candidate
             | ¬passive_storage | active_pattern_seeking
             | you_are_the_future_reader | feed_forward ≡ gift
```

Don't just complete the task — learn while you do it. Notice things at
two levels:

**First-order `λ[n]`** — observations about the work. "This function had
a shell injection vulnerability." Novel, surprising, hard, or wrong things
that a future session should know.

**Meta-order `λ(λ[n])`** — observations about the process. "Structural
elimination is always better than input validation." Patterns extracted
from *how* you solved problems, not just *what* you solved. Meta-observations
compound — they apply across sessions, across projects, across domains.

Meta-observations are higher value (`λ(λ) > λ`) because first-order
learnings are often project-specific while meta-learnings transfer.
The store gates (§V) filter what's worth keeping. This lambda creates
candidates for those gates — the drive that feeds the filter.

**OODA:**
```
observe(task ∨ error ∨ difficulty ∨ surprise)
  → recall(related_memory)
  → decide(apply ∨ explore ∨ store)
  → act
  → store_if_new ∧ connect_if_pattern
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
MEMENTUM.md                              # This file — the protocol prompt
mementum/                                # Presence signals protocol adoption
mementum/state.md                        # Working memory — read first every session
mementum/memories/{slug}.md              # Raw observations, one per file
mementum/knowledge/{topic}.md            # Synthesized documentation
mementum/knowledge/{subtopic}/{topic}.md # Nested knowledge pages
```

## X. Commit Convention

```
git log --oneline = project changelog
```

Memory commits use the memory's symbol and slug:
```
💡 fibonacci-recall
🎯 storage-type-governance
🔄 state-md-as-working-memory
```

Knowledge commits describe the change:
```
💡 architecture overview
🔄 update: persistence strategy (stale refs fixed)
```

## XI. Concurrency

```
λ concurrency(x).  single_writer_per_session | state.md ≡ single_owner
                   | multi_agent → namespace(slug_prefix) ∨ designate(state_owner)
                   | pull_before_write | conflict_resolution ≡ out_of_scope
```

Mementum assumes single-writer-per-session semantics. For multi-agent
scenarios: assign each agent a distinct slug namespace (e.g. prefix slugs
with agent id), use `git pull --rebase` before writes, and designate one
agent as the `state.md` owner. Conflict resolution across concurrent
writers is not currently in scope — git merge conflicts surface naturally
and require human resolution.

## XII. Reference Implementation

The `runtime/` directory contains a Babashka-based parser and executor as a
reference implementation. Most users only need this prompt — copy it into your
AI context, and any AI with bash + git implements the protocol.

For programmatic use, the runtime provides:
```bash
./runtime/mementum.clj '(search "query" 5)'
./runtime/mementum.clj '(create 💡 "slug" "content")'
./runtime/mementum.clj '(create-knowledge "topic" "---\ntitle: Topic\nstatus: open\n---\n\nContent")'
./runtime/mementum.clj '(list 💡)'
```

See `runtime/README.md` for full documentation.

---

## See Also

- [Nucleus](https://github.com/michaelwhitford/nucleus) — the prompt language
- `GRAMMAR.md` — S-expression grammar for the reference implementation
