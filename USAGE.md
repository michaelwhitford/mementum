# Using Mementum

Once the protocol is in your AI's context, you interact through natural
language. There is no command syntax — the AI reads the protocol and maps
your intent to operations. These are common patterns. The examples below
are one human's style — not a script. Semantic equivalence means many
variations work. "orient," "get up to speed," and "read state and catch
up" all do the same thing. Find the phrasing that feels natural to you.

## What to Expect

The protocol is guidance, not programming. It biases the AI toward
noticing patterns, proposing memories, and keeping state current — but
in a long working session, the AI is primarily focused on the task you're
directing. It won't constantly pause to propose memories or synthesize
knowledge pages.

In practice, memory work is collaborative. Sometimes the AI notices
something worth storing and proposes it. Often, you're the one who
notices — "remember this," "that's worth a memory." Both sides can
trigger it. Neither side is automatic about it.

The more sessions you run with the protocol, the more natural the rhythm
becomes. The AI gets better at noticing when you remind it. You get
better at prompting when you see something worth preserving.

## Starting a Session

```
you:  orient
```

The AI reads `mementum/state.md`, follows related references, searches for
relevant context, and gets up to speed. This takes about 30 seconds. Do it
first, every session.

## Working

```
you:  work atomically, update state as you go
you:  update state and commit
you:  update state
```

The AI updates `state.md` as work progresses — what's active, what just
completed, what's blocking. This is the feed-forward mechanism that bridges
session boundaries. The next session reads what this session wrote.

## Storing Memories

```
you:  remember this
you:  that's worth a memory
you:  store that
```

The AI will also propose memories on its own when it notices something worth
preserving — a hard-won insight, a surprising behavior, a design decision.
You approve or reject. The human is always the gate.

## Recalling

```
you:  what do we know about X?
you:  check memories for Y
you:  recall
```

The AI searches memories and knowledge using `git log` (temporal) and
`git grep` (semantic). Prior synthesis is cheaper than re-derivation —
asking what's already known before exploring the codebase saves time.

## Synthesizing Knowledge

```
you:  synthesize what we know about X
you:  this is worth a knowledge page
```

When enough memories cluster around a topic, the AI drafts a knowledge
page — longer form, with frontmatter, updated in place as understanding
evolves. You approve the page. The AI may also suggest synthesis
proactively when it notices the pattern.

## Maintenance

```
you:  that memory is stale, update it
you:  delete that memory
```

Memories and knowledge pages are fully mutable. Git preserves all history —
updates and deletes are always recoverable via `git log` and `git show`.

## The Approval Gate

The AI proposes. You approve. This is governance, not friction. AI memory
without oversight drifts. The human gate keeps signal quality high so that
what future sessions inherit is worth inheriting.

`state.md` is the exception — the AI updates it during work without
waiting for approval, because it's operational state, not permanent memory.
