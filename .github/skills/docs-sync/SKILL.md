---
name: docs-sync
description: >-
  Keeps project documentation in sync with staged (indexed) git changes right
  before a commit. Use this skill ALWAYS when the user asks to summarize
  changes, prepare a commit message, write a commit message, or explicitly asks
  to update/refresh documentation before committing - even if the user does not
  say the word "documentation" directly. Trigger phrases include "commit it",
  "commit message", "summarize changes", "what did I change", "I'm preparing a
  commit", "update docs", "refresh README/CHANGELOG/ARCHITECTURE". It works on
  the staged diff (git diff --cached), not unstaged changes and not the full
  history.
---

# Docs Sync

This skill synchronizes project documentation with what is actually in the git
index (staged) at the moment when the user is already asking to summarize
changes or prepare a commit message. The commit message is still prepared as
usual - this skill is added to that step rather than replacing it.

## When to run

Run it when the user:
- asks to prepare a commit message or summarize changes before a commit;
- explicitly asks to update documentation for the current changes;
- asks "what did I change" in the context of a git repository.

Do not run it for reading history (`git log`), analyzing someone else's PRs, or
when the user explicitly asks for a description of unstaged/working-tree
changes without a commit - that is out of scope for this skill (see the scope
below).

## Change scope

Only **staged** changes - what will go into the next commit. Never include
unstaged or untracked files in the analysis; if the staging area is empty, say
so and stop (suggest `git add`), and do not analyze the working directory as a
substitute.

When documentation is updated in English, update the Russian counterpart in the
same sync pass **if the RU counterpart exists**. If no RU counterpart exists,
continue without blocking and note that skip in the final summary.

## Workflow

### 1. Get the staged diff

```bash
bash scripts/get_staged_diff.sh /path/to/repo
```

If `STAGED DIFF (full)` is empty, the staging area is empty. Tell the user and
stop without editing anything.

### 2. Prepare the commit message (as usual)

Nothing changes in this part of the user's existing workflow - summarize the
staged diff into a commit message exactly as you would without this skill.

### 2b. Check for new GitHub issues not yet in the project phases file

Run the following script to detect GitHub issues that have been created (e.g.
during the current working session) but not yet recorded in
`docs/PROJECT_PHASES_EN.md`:

```bash
bash scripts/get_new_phase_issues.sh /path/to/repo
```

- If the output is empty, skip this step entirely — all issues are already
  recorded.
- If the script outputs `SKIP:`, the gh CLI is unavailable or the phases file
  does not exist; skip this step and note it in the final summary.
- If new issues are listed, proceed to step 2c before continuing with step 3.

### 2c. Record new issues in PROJECT_PHASES_EN.md and PROJECT_PHASES_RU.md

For each new issue reported in step 2b:

1. Open `docs/PROJECT_PHASES_EN.md` and find the **Current backlog** table of
   the matching phase (identified by the `[Phase N]` prefix in the issue title).
   The table uses this format:
   ```
   | ID   | Task                   | Status | Issue |
   |-|-|-|-|
   | 4.13 | **Last existing task** | ⏳     | #104  |
   ```
2. Determine the next available ID by finding the highest existing ID in that
   table and incrementing it (e.g. `4.13` → `4.14`).
3. Append a new row for each issue, using the issue title (stripped of the
   `[Phase N] Refactoring: ` prefix) as the task name:
   ```
   | 4.14 | **Remove sensitive files from git tracking** | ⏳ | #107 |
   ```
4. Apply the same edit to `docs/PROJECT_PHASES_RU.md`: find the equivalent
   backlog table (same phase, same ID column) and add the same rows, translating
   only the task name to Russian. Keep the issue number and status identical.

**Rules:**
- Never modify rows that already exist in the table.
- Use `⏳` for status of all newly added rows.
- Add all new rows as a contiguous block at the bottom of the existing backlog
  table, in ascending issue number order.
- If `PROJECT_PHASES_RU.md` does not have an equivalent table, add a note in
  the final summary: "RU file missing equivalent table for Phase N backlog".

### 3. Classify the changes

Group files from `STAGED FILES (name-status)` into categories using
`references/mapping.md` (progress/changelog, architecture,
configuration/settings, API/interface). One file may belong to multiple
categories. Explicitly filter out anything that mapping.md says is not a reason
to update docs (tests only, formatting only, lockfiles only).

If no categories with meaningful changes remain after filtering, mention that
in the summary and stop there (do not edit documentation "just in case").

### 4. Find documentation in the repository

```bash
bash scripts/find_docs.sh /path/to/repo
```

The list contains only *candidates*. Do not assume ahead of time that
documentation lives in a specific place: each project has its own structure
(it may have only a root README.md, a /docs directory with many files, or
docs/adr/NNN-*.md).

### 5. Map the diff to specific documents

For each non-empty category from step 3:
- search the discovered docs (step 4) for mentions of changed modules or
  directories, function/class names, configuration keys, and route paths by
  actually reading candidate files, not just by file name;
- if you find a match, read the relevant section before editing so you do not
  break neighboring content;
- if no document matches, do not create a new file on your own; record it in
  the final summary as "no place found to record this - category X, changes in
  Y".

### 6. Apply the edits

Edit documents directly (the user chose this mode - no intermediate
confirmation). For each edit:
- use targeted edits (replace a specific fragment) rather than rewriting the
  entire file;
- in Changelog/Progress, add a new entry instead of rewriting history;
- in Architecture, add a new paragraph or section if the structure truly
  changed; never rewrite already accepted ADRs after the fact;
- in Settings/API, update the specific value, signature, or example where it is
  outdated while preserving the rest of the document's style and formatting.
- for docs with EN/RU pairs, apply equivalent updates to both files in the same
  commit scope. Detect counterparts using common patterns such as
  `*_EN.*` ↔ `*_RU.*`, `*_EN` ↔ `*_RU`, and known paired docs like
  `PROJECT_PHASES_EN.md` ↔ `PROJECT_PHASES_RU.md`.

Never invent facts that are not in the diff. If the nature of the change is not
clear from the diff (for example, only "+1 -1" with no context), leave the
marker `<!-- TODO(docs-sync): verify -->` nearby instead of guessing.

### 7. Final summary

At the end, show the user a table:

| Changed files (diff) | Category | Document | What was recorded |
|---|---|---|---|

Also include a separate list of what was skipped and why (no changelog,
no matching document found, filtered out as trivial).

If an EN document was updated but no RU counterpart exists, explicitly list that
in the skipped section.

Remind the user that updated docs are not staged - they need `git add` before
the commit if they want to include them in the same commit.

## Skill files

- `scripts/get_staged_diff.sh` - staged name-status + stat + full diff.
- `scripts/find_docs.sh` - a heuristic list of doc candidates (tracked git
  files only, so `.gitignore` is already respected).
- `scripts/get_new_phase_issues.sh` - queries GitHub for `[Phase N]` issues
  not yet referenced in `docs/PROJECT_PHASES_EN.md`.
- `references/mapping.md` - a table of "change -> documentation category"
  signals and a list of what to skip.
