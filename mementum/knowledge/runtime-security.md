---
title: Runtime Security Architecture
status: active
category: architecture
tags: [security, runtime, babashka]
related:
  - dsl-design
depends-on: []
---

# Runtime Security Architecture

## Core Principle

Structural elimination over input validation. Do not sanitize dangerous
inputs — remove the dangerous path entirely.

## Three Layers

### 1. No Shell Involvement

The runtime never passes user input through a shell interpreter.

- **Git commands**: `babashka.process/shell` with array args. Each argument
  is a separate OS-level string — semicolons, pipes, backticks, and dollar
  signs are literal characters, never interpreted. `(run-git "grep" "-i" query "--" "mementum/")` passes `query` as one atomic argument regardless of content.

- **File writes**: `spit` (Clojure I/O). Content goes directly to the
  filesystem with no shell parsing, no heredoc delimiters, no expansion.

- **File reads**: `slurp` for mementum paths, `run-git show` for git refs.
  No `cat`, no shell involvement.

This eliminates shell injection as a class, not as individual cases.

### 2. Path Traversal Prevention

`safe-path?` resolves the canonical path and validates it stays within the
working directory. `mementum/../../etc/passwd` resolves to `/etc/passwd`,
which does not start with the cwd — rejected before any I/O occurs.

### 3. CRUD Enforcement

- `create` rejects if file exists (prevents silent overwrite)
- `create-knowledge` rejects if file exists (same)
- `update` rejects if file does not exist
- `delete` rejects if file does not exist
- `resolve-ref` rejects ambiguous multi-file commits

Every operation has exactly one valid state transition. No operation
silently does the wrong thing.

## Error Recovery

Every failure returns structured data: `{:error type :field name :value what-was-sent :expected what-is-valid :suggestion how-to-fix}`. The AI reads the suggestion and retries. No human needed for mechanical errors.

## Design Decisions

**Why babashka.process over clojure.java.shell?** Both avoid shell
interpolation when used correctly. `babashka.process/shell` is built
into Babashka, provides `:continue` for non-zero exit handling, and
its API is cleaner for the array-args pattern.

**Why spit/slurp over shell writes?** Heredoc (`cat <<'EOF'`) prevents
shell expansion but the delimiter itself can be injected if content
contains the delimiter string on its own line. `spit` has no such
vector — there is no delimiter, no parsing, no interpretation.

**Why reject ambiguous refs?** `resolve-ref` on a multi-file commit
could silently pick the wrong file. Explicit error with file list is
better than non-deterministic success.