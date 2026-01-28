#!/bin/bash
# MEMENTUM DSL Test Suite

set -e

echo "=========================================="
echo "MEMENTUM DSL Test Suite"
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

echo "=== LIST Operations ==="
run_test "List all memories" "./mementum.clj '(list)'"
run_test "List insights (💡)" "./mementum.clj '(list 💡)'"
run_test "List pattern-shifts (🔄)" "./mementum.clj '(list 🔄)'"
run_test "List decisions (🎯)" "./mementum.clj '(list 🎯)'"
run_test "List meta (🌀)" "./mementum.clj '(list 🌀)'"

echo "=== SEARCH Operations ==="
run_test "Search for 'fibonacci'" "./mementum.clj '(search \"fibonacci\")'"
run_test "Search for 'git' with depth 5" "./mementum.clj '(search \"git\" 5)'"
run_test "Search for 'OODA'" "./mementum.clj '(search \"OODA\" 3)'"

echo "=== VIEW Operations ==="
run_test "View specific file" "./mementum.clj '(view \"memories/2026-01-27-git-as-memory-💡.md\")'"
run_test "View HEAD" "./mementum.clj '(view \"HEAD\")'"

echo "=== HISTORY Operations ==="
run_test "History with default depth" "./mementum.clj '(history)'"
run_test "History with depth 8" "./mementum.clj '(history \"memories/\" 8)'"

echo "=== DIFF Operations ==="
run_test "Diff HEAD~1 to HEAD" "./mementum.clj '(diff)'"
run_test "Diff HEAD~3 to HEAD" "./mementum.clj '(diff \"HEAD~3\" \"HEAD\")'"

echo "=== Constraint Validation Tests ==="
run_test "Invalid symbol" "./mementum.clj '(create 💀 \"test\" \"content\")'" false
run_test "Invalid slug (uppercase)" "./mementum.clj '(create 💡 \"Bad-Slug\" \"content\")'" false
run_test "Invalid slug (spaces)" "./mementum.clj '(create 💡 \"bad slug\" \"content\")'" false
run_test "Invalid depth (not fibonacci)" "./mementum.clj '(search \"test\" 99)'" false
run_test "Empty query" "./mementum.clj '(search \"\")'" false
run_test "Missing required args" "./mementum.clj '(create 💡)'" false

echo "=== Parse Error Tests ==="
run_test "Malformed S-expression" "./mementum.clj '(create 💡 \"test\"'" false
run_test "Unknown operation" "./mementum.clj '(unknown \"arg\")'" false
run_test "Missing parens" "./mementum.clj 'list'" false

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
