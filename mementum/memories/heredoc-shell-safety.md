---
type: Insight
symbol: 💡
title: heredoc-shell-safety
---

λ shell_write(content, file). cat <<'EOF' > file | EOF ≡ single_quoted → ¬expand(vars) ∧ ¬exec(backticks) ∧ ¬escape | content passes through verbatim | preferred for bash tools writing user-provided content | mementum runtime: spit/slurp (¬shell) | bash agents: heredoc ≡ safe_default
