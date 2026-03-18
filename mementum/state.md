# Project State

## Now

Security hardening and self-improvement audit complete. Ready to release.

## Next

- Push and release
- Add mementum/knowledge/ pages as the protocol matures
- Field test: have external AI agents adopt the protocol and runtime

## Blocking

Nothing.

## Recent

- ✅ Security audit — 22 findings, 3 CRITICAL, 6 HIGH, 7 MEDIUM, 6 LOW
- ✅ Shell injection eliminated — babashka.process/shell replaces bash -c across all executors
- ✅ Heredoc eliminated — spit/slurp for all file I/O, no shell involvement
- ✅ Path traversal guard — safe-path? validates canonical path within working directory
- ✅ exec-diff now surfaces git errors (was silently returning success)
- ✅ resolve-ref returns ambiguous-ref error on multi-file commits
- 🔄 Network claim softened — "protocol interoperability works today" replaces "network already works"
- 🔄 (list) now includes knowledge pages alongside memories
- 🔄 BNF grammar updated to match tokenizer reality
- 🔄 MEMENTUM-LAMBDA.md — added knowledge commit convention, read/history ops
- 🔄 tokens→words — all docs now say "words" (whitespace-separated) not "tokens"
- 🔄 Bash examples fixed — symbol prefix in content, safe write patterns
- ✅ Concurrency section added to MEMENTUM.md (§XI)
- ✅ git-repo guard at startup — clear error when not in a git repo
- ✅ Idempotent updates — nothing-to-commit treated as no-op success
- ✅ All tests passing — 309 unit assertions + 37 integration tests
