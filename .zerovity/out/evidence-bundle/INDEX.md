# Evidence bundle: ZF-1 Add a short Getting Started section to the README

- Work item: ZF-1
- Title: Add a short Getting Started section to the README
- Source: https://github.com/dbrijesh/emfp/issues/4
- Branch: zerovity/zf-1-add-a-short-getting-started-sect (based on `main`)
- PR: not opened. The PR description is at `.zerovity/out/pr-description.md`.
- Runs: none recorded in CONTEXT.md. No build or test run was executed in this environment (Markdown-only change; MySQL and MongoDB are not available here).
- Approvals: see platform audit log. Approval of the spec and of the Docker Compose open point is not recorded here.

## Contents

| Item | File | SHA-256 | Notes |
|---|---|---|---|
| Approved spec | `spec.md` (copy of `docs/specs/ZF-1.md`) | abf0c11516a803cd594adfb3e57a30d50499048b4e65c98437fff3184b6af34c | Copied without edits. Status line still says Draft. |
| Latest review | `review.md` (copy of `.zerovity/out/result.json` from commit 92f3e11, verifier VERIFY phase, recommendation accept) | 73183c342bbc09fa9530a917679757683ec23282d824d1e61513f9d4fedfc10f | Copied from git history. The working-tree copy was deleted before SHIP started. Contains 3 low findings and 1 low scope finding. |
| Validation log | `validation.log` | not produced | Missing. No build or test command was run. The change is Markdown only. Anchors and commands were checked by reading and grep; see the PR description. |
| Extra evidence | `evidence/` | not produced | Empty. No screenshots or logs. The end-to-end run (AC4, AC5, AC6) was not performed. |
| PR description | `.zerovity/out/pr-description.md` (outside this bundle) | not included | Written in SHIP. Lists the checks and flags. |

## Open flags

- Unproven: AC4 (config steps on a clean clone), AC5 (backend and frontend start and respond), AC6 (Swagger UI loads). Not run.
- Unproven: AC2 (TOC anchor clicked in GitHub rendering). Not checked.
- Not met: R8. The `cp backend/example_config.properties backend/config.properties` command is not stated in the README and was not run.
- Scope: AC9 / R9 says only `README.md` should differ from `main`. The branch also contains `docs/specs/ZF-1.md` and `.zerovity/out/*` pipeline files. A person needs to decide whether to keep them.
- Open review findings (low, not fixed): no clone command in step 1, no config values in step 2, no database creation command in step 3.
- No escalation. No revision loops.
- No pre-existing failures recorded, because no tests were run.

## Redaction

No secrets or personal data were found in the copied files. Files were hashed as copied.
