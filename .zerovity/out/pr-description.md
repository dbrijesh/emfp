## What and why
Adds an active/inactive status to each employee. Admins can flip it from a Status toggle in the employee list, and the change persists. The flag is display-only: it does not hide or filter employees anywhere.

Work item: ZF-2 · Source: https://github.com/dbrijesh/emfp/issues/6 · Spec: `docs/specs/ZF-2.md` · Branch: `zerovity/zf-2-implement-the-employee-status-to`

## What changed
- **Backend, entity and schema**: `Employee.active` boolean, column default true (R1, R2). SQL bootstrap scripts (`backend/sql/02_create_tables.sql`, `databases/sql/02_create_tables.sql`) gain `active BOOLEAN NOT NULL DEFAULT TRUE` (R1).
- **Backend, API**: `PATCH /api/employees/{id}/status` with body `{"active": <boolean>}`, returns the updated employee (R4, R5, R6). New `EmployeeStatusRequestDto`. `active` is exposed in all employee responses (R3) and accepted as optional on POST and PUT. On PUT, an omitted `active` leaves the stored value unchanged (R8).
- **Backend, security**: `SecurityConfig` requires authentication for `PATCH /api/employees/*/status` only (R7). All other employee endpoints are unchanged (see Flags).
- **Backend, mirror**: the same changes are applied to `packages/java/employeemanagement` and `packages/java/employeemanagementtest` (R15).
- **Frontend, list**: Status column with an MUI `Switch` per row (R9). Turning a row on calls the PATCH and updates that row without a refetch. Turning a row off opens a confirmation dialog first (R10, R11). A failed PATCH keeps the previous state and shows an error toast. The switch is disabled while its request is pending (R12). The empty-state `colSpan` changes from 6 to 7.
- **Frontend, form**: "Active" switch in `EmployeeForm`, on by default for new employees and loaded from the stored value when editing. It is sent in the payload (R13).
- **Frontend, service**: `updateEmployeeStatus(id, active)` in `employeeService.js` sends the Bearer token from `getToken`, because the app sets no global auth header (spec O3).
- **Tests**: new `backend/src/test/.../EmployeeStatusApiTest.java` (12 tests). New `frontend/__tests__/EmployeeForm.test.js`. Extended `EmployeeList.test.js`. Regenerated `EmployeeList` snapshot.

## How it was checked
| Criterion | Check | Result |
|---|---|---|
| AC1 (R1) existing rows become active | Schema default in SQL; no run against an existing database | **Not proven** |
| AC2 (R2) POST without `active` gives active | `EmployeeStatusApiTest` | **Not run** (mvn denied to verifier; no JDK found by executor) |
| AC3 (R3) `active` in GET responses | `EmployeeStatusApiTest` | **Not run** |
| AC4 (R4) PATCH sets status, returns 200 | `EmployeeStatusApiTest` | **Not run** |
| AC5 (R5) PATCH changes only status | `EmployeeStatusApiTest` | **Not run** |
| AC6 (R6) 404 unknown id, 400 bad body | `EmployeeStatusApiTest` | **Not run**; the 400 path for `{"active":"yes"}` is unobserved |
| AC7 (R7) 401 without token | `EmployeeStatusApiTest` | **Not run** |
| AC8 (R8) POST/PUT `active` handling | `EmployeeStatusApiTest` | **Not run** |
| AC9 (R9) Status column, switch matches `active` | `EmployeeList.test.js` | Passed (frontend suite) |
| AC10 (R10) on-switch PATCH, no refetch | `EmployeeList.test.js` | Passed (frontend suite) |
| AC11 (R11) off-switch confirms first | `EmployeeList.test.js` | Passed (frontend suite) |
| AC12 (R12) revert and toast on failure, disabled while pending | `EmployeeList.test.js` | Passed (frontend suite) |
| AC13 (R13) form default and edit value | `EmployeeForm.test.js` | Passed (frontend suite) |
| AC14 (R14) status does not change lists, counts, CSV | `EmployeeList.test.js` ("does not change counts or CSV columns based on status") | Passed (frontend suite); assertion depth not reviewed |
| AC15 (R15) mirror matches | `diff -rq` of backend vs `packages/java` | Only difference is `HealthController.java`, which was already absent from `packages/java` before this change |

Frontend: `npm test -- --ci`, 26 suites, 45 tests, 17 snapshots, all passed. Log: `.zerovity/out/evidence-bundle/validation.log`.

Backend: **not compiled and not run.** The verifier's `mvn -o test` call was denied by the sandbox. The executor found no JDK or Maven installed. `java` is not on PATH in this shell either.

## Review notes
Read these first:
1. `backend/.../security/SecurityConfig.java`: the only auth change. Confirm the PATCH matcher is ahead of `anyRequest().permitAll()`.
2. `backend/.../controller/EmployeeController.java`: the PATCH handler and the null-guarded `active` handling in `updateEmployee`.
3. `backend/src/test/.../EmployeeStatusApiTest.java`: never executed. Treat as unverified until `mvn test` runs green.
4. `frontend/src/components/EmployeeList.js`: the per-row update and the error revert (R12).

Decisions made from the spec and CONTEXT.md:
- Confirmation before deactivating, and the Active control in the form. CONTEXT.md answered "Yes" to a two-part question. The spec reads it as yes to both. The approver should confirm this (spec O1).
- `packages/java/` is updated, as CONTEXT.md requires.

## Flags
- **Backend tests unverified.** `mvn test` must be run in `backend/` before merge. This covers AC2 to AC8, including the 400 case for a non-boolean `active` and the 401 case without a token. The verifier's recommendation was "accept, conditional on a green `mvn test`".
- **AC1 not proven.** The schema default covers existing rows in principle, but nothing ran against a database that predates the change.
- **AC14 covered by one test only.** The `EmployeeList.test.js` case passes, but I did not review how strong its assertions are. Nothing else checks that status leaves lists, counts or CSV unchanged.
- **Inconsistent authorisation (spec O2).** Only PATCH requires authentication. Other employee endpoints remain open, as the spec scopes it. Securing them is out of scope.
- **Migration relies on `ddl-auto=update` and the column default.** The repository has no migration tool. Existing deployments need the column default to be correct, and this is not proven (spec O4).
- **Pre-existing:** `HealthController.java` is in `backend/` but not in `packages/java/`. Not introduced by this change.
- **Low-severity review notes (not blocking):** the PATCH's Authorization header is built inline and duplicates the pattern in `passkeyService.js`. `applyStatus` in `EmployeeList.js` falls back to the requested value when the response lacks `active`, which can hide a malformed response.
- **Revision loops:** the executor recommended "revise" because the backend was unverified. The verifier recommended "accept, conditional". No code was changed after verification.

🤖 Generated with [Claude Code](https://claude.com/claude-code)
