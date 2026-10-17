# Evidence bundle: ZF-2

- **Work item:** ZF-2, Implement the employee status toggle (backend PATCH /api/employees/{id}/status + frontend Status column with toggle switch)
- **Source:** https://github.com/dbrijesh/emfp/issues/6
- **Spec:** `spec.md` (copy of `docs/specs/ZF-2.md`, status Draft)
- **Branch:** `zerovity/zf-2-implement-the-employee-status-to`
- **PR:** none yet; `pr-description.md` is in `.zerovity/out/`
- **Runs:** frontend `npm test -- --ci` (see `validation.log`). Backend `mvn test`: not run.
- **Approvals:** see platform audit log

## Files

| Item | File | SHA-256 | Notes |
|---|---|---|---|
| Approved spec | `spec.md` | 09a0c918af5a71adccb30127849d27d501c0a07fe8a3a96ba750fc6121871b2b | Copy of `docs/specs/ZF-2.md` |
| Latest verification review | `review.md` | 731a03519a60e3fc4171eb448def57d960a91396e3eacc59d4c780c3f97c150f | Verifier's `result.json` from commit fbf728f, copied verbatim. Recommends accept, conditional on green `mvn test`. |
| Test and build output | `validation.log` | cc00f14cf2b809fb70b9b68f281cf03edb12c7dc3bf0cbd68e880fa20e0223f0 | Frontend only: 26 suites, 45 tests, 17 snapshots, all passed |
| PR description | not included | n/a | Lives at `.zerovity/out/pr-description.md`; not part of the copy list in the bundle layout |
| Screenshots and extra logs | `evidence/` | n/a | Empty. No screenshots taken; the app was not run. |
| Backend test output | not included | n/a | No output. `mvn test` could not run (sandbox denied it; no JDK found). |

## Open flags

- **Backend tests unproven.** `EmployeeStatusApiTest` (12 tests, AC2 to AC8) has never been compiled or run. Needs a green `mvn test` in `backend/`.
- **AC1 unproven.** Existing rows becoming active has not been run against a pre-change database.
- **AC14 thin.** One frontend test; assertion depth not reviewed.
- **Spec O1.** Reading of the combined CONTEXT.md answer (form control and deactivation confirmation) needs approver confirmation.
- **Spec O2.** Only PATCH requires authentication; other employee endpoints remain open by design.
- **Spec O4.** Migration relies on `ddl-auto=update` and the column default; no migration tool in the repo.
- **Pre-existing.** `HealthController.java` exists in `backend/` but not `packages/java/`.
