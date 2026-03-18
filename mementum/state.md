# Project State

## Now

Protocol documents aligned and ready for release. MEMENTUM.md,
MEMENTUM-LAMBDA.md, and GRAMMAR.md all reflect the current protocol.
Runtime (runtime/mementum.clj) still needs updating to match.

## Next

- Update runtime to match GRAMMAR.md (filenames, paths, symbols, list filtering)
- Update tests (mementum_test.clj, test-dsl.sh) for new protocol
- Review existing memories for accuracy
- Add mementum/knowledge/ pages as the protocol matures

## Blocking

Nothing.

## Recent

- 🔄 GRAMMAR.md rewritten — aligned with current protocol (paths, filenames, symbols, both tiers)
- 🔄 Store operation expanded — create + update + delete as full lifecycle (all three docs)
- 🔄 Append-only removed — files are mutable, git preserves history (all three docs)
- 🔄 Protocol update — MEMENTUM.md rewritten (two tiers, three ops, knowledge pages)
- 🌱 MEMENTUM-LAMBDA.md created — compact lambda-only prompt
- 🔄 Memory filenames migrated — dropped date prefix and symbol suffix
- 🌱 state.md created — working memory for the repo
- 🌱 knowledge/ directory created
