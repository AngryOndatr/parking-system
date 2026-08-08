#!/bin/bash
# Lists tracked documentation-like files anywhere in the repo (respects
# .gitignore because it only lists files git already tracks).
# Usage: find_docs.sh [repo_path]
set -euo pipefail

REPO="${1:-.}"
cd "$REPO"

git ls-files | grep -Ei \
  '(^|/)(README|CHANGELOG|CHANGES|HISTORY|CONTRIBUTING|ARCHITECTURE|PROGRESS|ROADMAP)(\.[a-zA-Z]+)?$|(^|/)docs?/.*\.(md|mdx|rst|adoc|txt)$|(^|/)adr/.*\.md$|(^|/)decisions?/.*\.md$' \
  || true
