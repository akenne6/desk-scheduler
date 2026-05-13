# Plan

> Source of truth for current goals, decisions, and open questions.
> Updated by `/ship` and by hand. Newest entries at the top of each section.

## Current focus
This repo is now the `desk-scheduler` template — a snapshot of the workflow + scaffolding intended as a starting point for future projects. New projects branch off this state and rename via the recipe in [CLAUDE.md](CLAUDE.md) → "Renaming for a new project". Future work in *this* repo is improvements to the template itself.

## Decisions
_Append-only log of meaningful technical decisions and the reasoning behind them._

- _(none yet — workflow tooling is the only completed work so far)_

## Open questions
_Things we haven't resolved. Move resolved ones into Decisions._

- _(none yet)_

## Changelog
_Short bullet per `/ship`, newest first. Format: `YYYY-MM-DD — <summary>`_

- 2026-05-13 — Swapped `/ultrareview` out of the default flow for a new repo-local `/review` slash command that runs in-conversation and posts the review to the PR as a review comment (no cloud-side billing). `/ultrareview` retained in the per-step table as an escalation option. CLAUDE.md workflow step 8, command reference table, and `/address-review` description updated accordingly.
- 2026-05-13 — Added `scripts/enable-required-pr-checks.sh` (genericized via `gh repo view`) to set `PR checks / backend` and `PR checks / frontend` as required status checks on `main`. Fixes a CLAUDE.md reference to a non-existent `PR checks / checks` context.
- 2026-05-13 — Bumped CI to Node 24 (Node 20 hit maintenance EOL April 2026); regenerated `frontend/package-lock.json` under npm 11 to match.
- 2026-05-13 — Established v2 workflow tooling: slash commands (`/plan-feature`, `/ship`, `/verify`, `/merge`, `/integration-verify`, `/promote`, `/address-review`, `/quickfix`), Spotless lint (Google Java Format AOSP), JaCoCo coverage check at 80%, `integration-tests` Maven profile, README.md. Workflow shared via `.claude/` (commands, hook script, project settings.json); `.claude/settings.local.json` stays gitignored.
