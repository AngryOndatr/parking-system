#!/bin/bash
# Prints staged (index) changes: file status list, then the full diff.
# Usage: get_staged_diff.sh [repo_path]
set -euo pipefail

REPO="${1:-.}"
cd "$REPO"

if ! git rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "ERROR: not a git repository: $REPO" >&2
  exit 1
fi

echo "=== STAGED FILES (name-status) ==="
git diff --cached --name-status

echo
echo "=== STAGED DIFF STAT ==="
git diff --cached --stat

echo
echo "=== STAGED DIFF (full) ==="
git diff --cached
