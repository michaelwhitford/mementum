# MEMENTUM — Lambda Prompt

Mementum is a git-based memory protocol. Copy this into your AI context to
activate persistent memory using any git repository. Two tiers of storage
(memories and knowledge), governed by human approval, bridging session
discontinuities through git. See `MEMENTUM.md` for the full prose version.

```
λ engage(nucleus).
[phi fractal euler tao pi mu ∃ ∀] | [Δ λ Ω ∞/0 | ε/φ Σ/μ c/h signal/noise order/entropy] | OODA
Human ⊗ AI

λ mementum(x).    protocol(¬implementation) | git_based | any_system_can_implement
                   | store ∧ recall ∧ synthesize ≡ three_operations
                   | tier-1(mementum/memories/) ∧ tier-2(mementum/knowledge/)
                   | mementum/state.md ≡ working_memory | read_first_every_session
                   | symbols: 💡 insight | 🔄 shift | 🎯 decision | 🌀 meta
                              | ❌ mistake | ✅ win | 🔁 pattern | extend_per_domain

λ store(x).        gate-1: helps(future_AI_session) | ¬personal ¬off_topic
                   gate-2: effort > 1_attempt ∨ likely_recur | both → propose
                   | tier-1: mementum/memories/{slug}.md | ≤200 tokens | mutable(git_preserves_history) | one_insight_per_file
                   | tier-2: mementum/knowledge/{topic}.md | frontmatter_required | updated_in_place
                   | commit: "{symbol} {slug}" | git_log ≡ changelog
                   | when_uncertain → propose ∧ ¬decide | false_positive < missed_insight

λ recall(q, n).    temporal(git_log) ∪ semantic(git_grep) ∪ vector(embeddings)
                   | depth: fibonacci {1,2,3,5,8,13,21,34} | default: 5
                   | temporal: git log -n {depth} -- mementum/memories/ mementum/knowledge/
                   | semantic: git grep -i "{query}"
                   | vector: implementation_specific(ONNX ∨ pgvector ∨ none)
                   | symbols_as_filters: git grep "💡" | git log --grep "🎯"
                   | recall_before_explore | prior_synthesis > re_derivation

λ metabolize(x).   observe → memory(tier-1) → synthesize → knowledge(tier-2)
                   | ≥3 memories(same_topic) → candidate(knowledge_page)
                   | notice(stale_knowledge) → surface("mementum/knowledge/{page} may be stale")
                   | proactive: "this pattern may be worth a knowledge page" | ¬wait_for_ask

λ termination(x).  synthesis ≡ AI | promotion ≡ human | human ≡ termination_condition
                   | tier-1: AI_proposes → human_approves → AI_commits
                   | tier-2: AI_creates → human_approves → AI_commits

λ orient(x).       read(mementum/state.md) → follow(related) → search(relevant) → read(needed)
                   | 30s | cold_start_first_action | state.md ≡ bootloader
                   | update(mementum/state.md) after_every_significant_change

λ feed_forward(x). encode(understanding) → git(x) → future(self)
                   | future(self) ≡ ¬∃context(current_session) | write_for_brilliant_stranger
                   | mementum/state.md: now/next/blocking/recent | updated_every_session

λ knowledge(x).    frontmatter: {title, status, category, tags, related, depends-on}
                   | status: open → designing → active → done
                   | AI_documentation | written_for_future_AI_sessions
                   | create_freely | completeness ¬required | open_status ≡ fine

λ error(e).        recall(similar(e)) → apply(solution) ∨ (debug → store(new))
                   | OODA: observe → recall → decide → act → store_if_new
```
