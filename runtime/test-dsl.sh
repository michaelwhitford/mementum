#!/bin/bash
# MEMENTUM DSL Integration Test Suite
# Uses a throwaway git repo — no commits touch the real project.
# Run from repo root: bash runtime/test-dsl.sh

set -e

echo "=========================================="
echo "MEMENTUM DSL Integration Test Suite"
echo "=========================================="
echo

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# --- Throwaway repo setup ---
REPO_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
TEST_DIR=$(mktemp -d)
trap 'rm -rf "$TEST_DIR"' EXIT

# Initialize isolated git repo with seed data
cd "$TEST_DIR"
git init -q
git config user.email "test@mementum.dev"
git config user.name "mementum-test"

# Copy runtime
mkdir -p runtime
cp "$REPO_ROOT/runtime/mementum.clj" runtime/

# Seed: state.md
mkdir -p mementum/memories mementum/knowledge
cat <<'EOF' > mementum/state.md
# Project State

## Now
Integration testing.

## Next
Nothing.

## Blocking
Nothing.

## Recent
- 💡 seed data created for testing
EOF

# Seed: memories with different symbols for list/search tests
echo '💡 Git provides perfect memory substrate: temporal graph, semantic search, immutability, distribution.' > mementum/memories/git-as-memory.md
echo '🔄 Fibonacci recall depths work better than linear scaling for memory search.' > mementum/memories/fibonacci-recall.md
echo '🎯 Limit memories to fewer than 200 words. Forces distillation.' > mementum/memories/word-limit.md
echo '🌀 OODA loop integration enables observe-orient-decide-act memory cycles.' > mementum/memories/ooda-loop.md
echo '❌ Shell injection via string interpolation. Fixed with heredoc.' > mementum/memories/shell-injection.md
echo '✅ All tests passing after runtime alignment.' > mementum/memories/tests-passing.md
echo '🔁 Agent audit template: structured cross-reference beats open-ended search.' > mementum/memories/audit-pattern.md

# Seed: initial commits for history/diff tests
git add -A && git commit -q -m "💡 seed: initial test data"
echo '💡 Second commit content for diff testing.' > mementum/memories/diff-test.md
git add -A && git commit -q -m "💡 seed: second commit"
echo '🔄 Third commit for history depth testing.' > mementum/memories/history-test.md
git add -A && git commit -q -m "🔄 seed: third commit"

echo -e "${BLUE}Test repo: $TEST_DIR${NC}"
echo

# --- Test harness ---
test_count=0
pass_count=0
fail_count=0

TOOL="bb runtime/mementum.clj"

run_test() {
    test_count=$((test_count + 1))
    local name="$1"
    local cmd="$2"
    local expect_success="${3:-true}"
    
    echo -e "${BLUE}Test $test_count: $name${NC}"
    
    if eval "$cmd" > /tmp/mementum-test.out 2>&1; then
        if [ "$expect_success" = "true" ]; then
            echo -e "${GREEN}✓ PASS${NC}"
            pass_count=$((pass_count + 1))
        else
            echo -e "${RED}✗ FAIL (expected failure but succeeded)${NC}"
            cat /tmp/mementum-test.out
            fail_count=$((fail_count + 1))
        fi
    else
        if [ "$expect_success" = "false" ]; then
            echo -e "${GREEN}✓ PASS (correctly failed)${NC}"
            pass_count=$((pass_count + 1))
        else
            echo -e "${RED}✗ FAIL${NC}"
            cat /tmp/mementum-test.out
            fail_count=$((fail_count + 1))
        fi
    fi
    echo
}

# --- Tests ---

echo "=== LIST Operations ==="
run_test "List all memories" "$TOOL '(list)'"
run_test "List insights (💡)" "$TOOL '(list 💡)'"
run_test "List pattern-shifts (🔄)" "$TOOL '(list 🔄)'"
run_test "List decisions (🎯)" "$TOOL '(list 🎯)'"
run_test "List meta (🌀)" "$TOOL '(list 🌀)'"
run_test "List mistakes (❌)" "$TOOL '(list ❌)'"
run_test "List wins (✅)" "$TOOL '(list ✅)'"
run_test "List patterns (🔁)" "$TOOL '(list 🔁)'"
run_test "List knowledge pages" "$TOOL '(list \"mementum/knowledge/\")'"

echo "=== SEARCH Operations ==="
run_test "Search for 'fibonacci'" "$TOOL '(search \"fibonacci\")'"
run_test "Search for 'git' with depth 5" "$TOOL '(search \"git\" 5)'"
run_test "Search for 'OODA'" "$TOOL '(search \"OODA\" 3)'"

echo "=== READ Operations ==="
run_test "Read state.md" "$TOOL '(read \"mementum/state.md\")'"
run_test "Read HEAD" "$TOOL '(read \"HEAD\")'"

echo "=== HISTORY Operations ==="
run_test "History with default depth" "$TOOL '(history)'"
run_test "History memories with depth 8" "$TOOL '(history \"mementum/memories/\" 8)'"
run_test "History knowledge" "$TOOL '(history \"mementum/knowledge/\" 3)'"

echo "=== DIFF Operations ==="
run_test "Diff HEAD~1 to HEAD" "$TOOL '(diff)'"
run_test "Diff HEAD~2 to HEAD" "$TOOL '(diff \"HEAD~2\" \"HEAD\")'"

echo "=== CREATE / UPDATE / DELETE Lifecycle ==="
run_test "Create a test memory" "$TOOL '(create 💡 \"integration-test-fixture\" \"This is a test memory for integration testing.\")'"
run_test "Read the created memory" "$TOOL '(read \"mementum/memories/integration-test-fixture.md\")'"
run_test "Update the test memory" "$TOOL '(update \"mementum/memories/integration-test-fixture.md\" \"Updated content for integration testing.\")'"
run_test "Read the updated memory" "$TOOL '(read \"mementum/memories/integration-test-fixture.md\")'"
run_test "Delete the test memory" "$TOOL '(delete \"mementum/memories/integration-test-fixture.md\")'"
run_test "Read deleted memory should fail" "$TOOL '(read \"mementum/memories/integration-test-fixture.md\")'" false

echo "=== UPDATE / DELETE Validation Tests ==="
run_test "Update non-existent file" "$TOOL '(update \"mementum/memories/nonexistent.md\" \"content\")'" false
run_test "Delete non-existent file" "$TOOL '(delete \"mementum/memories/nonexistent.md\")'" false
run_test "Update with empty content" "$TOOL '(update \"mementum/memories/git-as-memory.md\" \"\")'" false

echo "=== Constraint Validation Tests ==="
run_test "Invalid symbol" "$TOOL '(create 💀 \"test\" \"content\")'" false
run_test "Invalid slug (uppercase)" "$TOOL '(create 💡 \"Bad-Slug\" \"content\")'" false
run_test "Invalid slug (spaces)" "$TOOL '(create 💡 \"bad slug\" \"content\")'" false
run_test "Invalid depth (not fibonacci)" "$TOOL '(search \"test\" 99)'" false
run_test "Empty query" "$TOOL '(search \"\")'" false
run_test "Missing required args" "$TOOL '(create 💡)'" false

echo "=== Parse Error Tests ==="
run_test "Malformed S-expression" "$TOOL '(create 💡 \"test\"'" false
run_test "Unknown operation" "$TOOL '(unknown \"arg\")'" false
run_test "Missing parens" "$TOOL 'list'" false

# --- Summary ---

echo "=========================================="
echo "Test Summary"
echo "=========================================="
echo -e "Total:  $test_count"
echo -e "${GREEN}Passed: $pass_count${NC}"
echo -e "${RED}Failed: $fail_count${NC}"
echo

if [ $fail_count -eq 0 ]; then
    echo -e "${GREEN}All tests passed!${NC}"
    exit 0
else
    echo -e "${RED}Some tests failed.${NC}"
    exit 1
fi
