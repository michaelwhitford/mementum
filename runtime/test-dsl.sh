#!/bin/bash
# MEMENTUM DSL Integration Test Suite
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

test_count=0
pass_count=0
fail_count=0

run_test() {
    test_count=$((test_count + 1))
    local name="$1"
    local cmd="$2"
    local expect_success="${3:-true}"
    
    echo -e "${BLUE}Test $test_count: $name${NC}"
    echo "Command: $cmd"
    
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

TOOL="bb runtime/mementum.clj"

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
run_test "Diff HEAD~3 to HEAD" "$TOOL '(diff \"HEAD~3\" \"HEAD\")'"

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
