# Heuristics: change -> documentation category

For each file from `git diff --cached --name-status`, determine its category
using the patterns below. One file may belong to multiple categories.

## 1. Progress / Changelog
**Signals:** a new endpoint, a new CLI command, a new feature, a feature-toggle
flag, a fix to user-visible behavior, a version change in a manifest
(`package.json`/`pyproject.toml`/`Cargo.toml`/`*.csproj`).
**Where to write:** `CHANGELOG.md` / `HISTORY.md` / `PROGRESS.md` - if it
exists, add an entry under the `Unreleased` section (or create such a section
as the first line after the heading if it does not exist). Entry format:
`- <short summary in past tense> (<area>)`.
If no such file exists in the repository, do not create it yourself; note it in
the summary as "no changelog available - entry not recorded".

## 2. Architecture
**Signals:** a new top-level directory, module, or service; a new
`docker-compose*.yml`, `Dockerfile`, or Kubernetes manifest; a new dependency
that implies a new subsystem (queue, cache, database, message broker); a data
schema or migration change; changes affecting more than one module at once.
**Where to write:** `ARCHITECTURE.md`, `docs/architecture/*`, `adr/*`,
`docs/decisions/*`.
If you are editing an ADR-like structure, do not edit past ADRs; add a new file
using the same numbering pattern only when the diff clearly shows an
architectural decision (not for minor changes).

## 3. Settings / configuration
**Signals:** changes in `.env.example`, `config/*.yaml|json|toml`,
`settings.py`, `appsettings*.json`, adding or removing a CLI flag, changing a
default parameter value, or adding a new environment variable.
**Where to write:** the configuration section in `README.md` or a dedicated
`docs/configuration.md` - search for headings such as "Configuration",
"Environment Variables", "Settings", or "Parameters".

## 4. API / public interface
**Signals:** changes in route or controller files, a signature change in an
exported function or public class, edits to an OpenAPI/Swagger file, a GraphQL
schema, or protobuf/`.proto` files.
**Where to write:** `docs/api/*`, the "API" section in `README`, or the
OpenAPI spec itself - if it is the source of truth (for example `openapi.yaml`),
update it directly.

## 5. GitHub Issues → Project Phases
**Signals:** `scripts/get_new_phase_issues.sh` returns one or more issue lines
(i.e. issues whose title starts with `[Phase N]` and whose number does not yet
appear in `docs/PROJECT_PHASES_EN.md`).
**Where to write:** `docs/PROJECT_PHASES_EN.md` and `docs/PROJECT_PHASES_RU.md` —
append new rows to the backlog table of the matching phase section
(the table headed `| ID | Task | Status | Issue |`).
**Never** edit this file based on the staged diff alone; only use the output of
`get_new_phase_issues.sh` as the source of truth for this category.

## 6. Multilingual document sync (EN ↔ RU)
**Signals:** staged documentation edits to EN files where a RU counterpart
exists (for example `*_EN.md` with `*_RU.md` in the same docs area).
**Where to write:** apply equivalent updates to both EN and RU files in the
same docs-sync run.
If the RU counterpart does not exist, do not block; report it in the skipped
items as "RU counterpart missing".

## What to skip (not a reason to update docs)
- Changes only in tests (`*test*`, `*spec*`, `__tests__/*`) without changes in
  the code under test.
- Pure formatting or lint changes (whitespace, import order) without logic
  changes.
- Changes only in lockfiles (`package-lock.json`, `poetry.lock`, `Cargo.lock`)
  without a corresponding manifest change.
- Code comments or docstrings that do not affect public behavior.

## Rule when uncertain
If it is unclear whether public behavior changed, do not invent a fact. Either
skip it with a summary note saying "manual verification needed", or leave a
short marker `<!-- TODO(docs-sync): verify -->` near the place where an update
might be needed instead of adding an unverified statement.
