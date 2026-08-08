#!/usr/bin/env bash
# Reports GitHub issues that contain "[Phase" in the title and are NOT yet
# referenced (by issue number) in docs/PROJECT_PHASES_EN.md.
#
# Usage: bash get_new_phase_issues.sh /path/to/repo [--label <label>]
#
# Output format:
#   === NEW ISSUES NOT YET IN PROJECT_PHASES_EN.md ===
#   #107  [Phase 4] Refactoring: Remove sensitive files from git tracking
#   #108  [Phase 4] Refactoring: Disable JWT_SECRET fallback value
#   ...
#   (empty if all issues are already recorded)
set -euo pipefail

REPO="${1:-.}"
LABEL="${2:-phase-4}"
PHASES_EN="$REPO/docs/PROJECT_PHASES_EN.md"

if ! command -v gh &>/dev/null; then
  echo "SKIP: gh CLI not available — cannot check for new issues" >&2
  exit 0
fi

if [ ! -f "$PHASES_EN" ]; then
  echo "SKIP: $PHASES_EN not found" >&2
  exit 0
fi

# Fetch issues with the given label whose title starts with "[Phase"
ISSUES=$(gh issue list \
  --label "$LABEL" \
  --state open \
  --limit 100 \
  --json number,title \
  --jq '.[] | select(.title | startswith("[Phase")) | "#\(.number)\t\(.title)"' \
  2>/dev/null || true)

if [ -z "$ISSUES" ]; then
  exit 0
fi

NEW_ISSUES=""
while IFS=$'\t' read -r ref title; do
  num="${ref#\#}"
  # Check whether this issue number appears anywhere in the phases file
  if ! grep -qE "(#|issues/)${num}([^0-9]|$)" "$PHASES_EN" 2>/dev/null; then
    NEW_ISSUES="${NEW_ISSUES}${ref}  ${title}"$'\n'
  fi
done <<< "$ISSUES"

if [ -n "$NEW_ISSUES" ]; then
  echo "=== NEW ISSUES NOT YET IN PROJECT_PHASES_EN.md ==="
  printf '%s' "$NEW_ISSUES"
fi
