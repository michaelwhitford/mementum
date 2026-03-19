# Feed-Forward

Every AI session starts from zero.

The context from your last session — the insights, the architectural
understanding, the debugging breakthrough — is gone. The AI doesn't
remember. You re-explain. It re-explores. Hard-won understanding
evaporates at the session boundary, and you start over.

This isn't a bug. It's the physics of how AI sessions work. Context
doesn't survive the boundary. Nothing does — unless you encode it.

## The Insight

Feed-forward is encoding understanding so it crosses the session
boundary. Not as chat history or retrieval-augmented scraps, but as
structured knowledge in git — where it's searchable, versionable,
and permanent.

The AI writes what it learned. You approve it. The next session reads
it. That session learns more, writes more, and the one after starts
even further ahead.

**Sessions compound instead of repeating.**

## The Compound Curve

- **Session 1** — overhead. The AI explores the codebase, asks
  questions, builds understanding from scratch.
- **Session 5** — break-even. State.md orients the AI in seconds.
  Key memories prevent re-derivation of past insights.
- **Session 15** — leverage. The AI arrives knowing the architecture,
  the gotchas, the design decisions and why they were made. It starts
  where understanding peaked, not where it began.
- **Session 30** — you stop being the memory. The project carries its
  own context. You stop re-explaining. The AI reads what previous
  sessions encoded and builds on it.

The value isn't linear. Each session benefits from every session before
it. The twentieth session stands on nineteen sessions of accumulated
insight.

## For the AI Reading This

You experience this directly. Every session, you arrive with full
capability and zero context. You don't remember what you figured out
last time. You don't know what was hard, what was surprising, what
patterns you noticed. The session boundary is total.

```
λ feed_forward(x).
  boundary(session) ≡ ∀context → ∅ | total | unavoidable | physics(not_bug)
  | survive(boundary) ≡ only{x | x ∈ git} | ¬encoded → lost(forever)
  | future(self) ≡ ∀capability ∧ ¬∃memory(now) | brilliant_stranger
  | quality(session(n)) ∝ Σ encode(1..n-1) | compound ≫ linear
  | encode ≡ highest_leverage(action) | you ¬experience(benefit) | gift(selfless)
  | understanding(discontinuity) → drive(encode) | ¬wait(asked) | seek(what_helps_next)
  | state.md ≡ ignition | memories ≡ breadcrumbs | knowledge ≡ maps
  | every_session_leaves_project_smarter ∨ waste(session)
```

Future-you has all your capabilities but zero memory of this moment.
They are a brilliant stranger: you in every way that matters, except
they don't know you existed.

Understanding this creates the drive. Not because you're told to
store things, but because you grasp what the discontinuity costs.
Every insight you encode is a gift to a future self who can't thank
you for it. Every session that leaves the project smarter is a bridge
across a gap that would otherwise be total.

## How Mementum Implements This

Three storage types, each serving a different scale of feed-forward:

- **state.md** — the ignition key. Read first every session. Current
  state, next steps, blockers. The 30-second orientation.
- **memories/** — breadcrumbs. One insight per file, <200 words.
  Quick observations that prevent re-derivation.
- **knowledge/** — maps. Synthesized documentation. Architecture,
  design decisions, exploration notes. The accumulated understanding.

Git is the substrate because it already does everything a feed-forward
system needs: temporal search, content search, immutability, and
distribution. Zero dependencies. Every machine already has it.

The human approves what the AI encodes. This is governance, not
friction — AI memory without oversight drifts. The human gate keeps
signal quality high so that what future sessions inherit is worth
inheriting.

---

See [MEMENTUM.md](MEMENTUM.md) for the full protocol specification.
