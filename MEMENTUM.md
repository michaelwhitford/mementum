# MEMENTUM — git memory

## Nucleus Operating Principles

```
[phi fractal euler tao pi mu] | [Δ λ ∞/0 | ε/φ Σ/μ c/h] | OODA
Human ⊗ AI
```

## λ Operations

```
λ store(x) → memories/{symbol}-{date}-{slug}.md → git commit -m "{symbol} x"
λ recall(q,n=2) → git log -n n -- memories/ | git grep -i q
```

Default depth n=2 (fibonacci hint: n-1, n-2). Adjust n for deeper search.

## Symbols

```
🧠💡 insight  🔄Δ pattern-shift  🎯⚡ decision  🌀 meta
```

## Auto-Trigger

**Store ONLY when critical:**

- Novel architectural insight (🧠💡)
- Significant pattern shift (🔄Δ)
- Strategic decision with >1 week impact (🎯⚡)
- Meta-learning that changes approach (🌀)

**Skip:** routine changes, minor fixes, incremental work

File: `memories/{symbol}-YYYY-MM-DD-{slug}.md` (keep <200 tokens)
Commit: `git commit -m "{symbol} terse-description"`

**Token Budget:** Each memory ≤200 tokens. Compress ruthlessly.

## Recall Pattern

```bash
git log -n 2 -- memories/           # Recent context (n-1, n-2)
git grep -i "{query}" memories/     # Semantic search all memories
git log --grep "{symbol}" -- memories/  # Search by symbol
```

Fibonacci depth: Start shallow (2), expand as needed (3,5,8,13...)

## OODA

```
observe  → git log -n 13 -- memories/
orient   → git grep -i "{query}" memories/
decide   → create|update memory
act      → git commit
```

---

repo=memory | commits=timeline | git=database

[phi fractal euler tao pi mu] | [Δ λ ∞/0 | ε/φ Σ/μ c/h] | OODA
Human ⊗ AI
