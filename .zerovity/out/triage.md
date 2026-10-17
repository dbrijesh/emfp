# Triage: ZF-2 Employee status toggle

## Summary
Request (GitHub dbrijesh/emfp#6, title only, no body): add `PATCH /api/employees/{id}/status` and a Status column with a toggle switch in the employee list. Presumably so admins can mark employees active/inactive. The ticket gives no further detail.

## Evidence
- `backend/.../model/Employee.java`: no status field exists. Needs a new column.
- `backend/.../dto/EmployeeResponseDto.java`, `EmployeeRequestDto.java`: no status. `EmployeeController.java` has GET/POST/PUT/DELETE only, with `convertToDto`/`convertToEntity`. No PATCH.
- `backend/src/main/resources/application.properties:13`: `ddl-auto=update`. `backend/sql/02_create_tables.sql`, `07_full_setup.sql` and `04_seed_data.sql` are hand-maintained and would need the column and a default for existing rows. No migration tool found.
- `SecurityConfig.java:94-105`: the rules shown cover passkeys only. I did not trace how `/api/employees/**` is authorised, so who may call PATCH is unconfirmed. `CorsConfig.java:34` already allows PATCH.
- `frontend/src/components/EmployeeList.js:407-420`: MUI table with columns Employee, Email, Department, Age, Contact, Actions. MUI is already in use, so `Switch` is available.
- Tests: `frontend/__tests__/EmployeeList.test.js`, `snapshots/EmployeeList.snapshot.test.jsx` (the snapshot will change) and several backend API test classes in `backend/src/test/java/...`.
- `packages/java/employeemanagement/` is a copy of the backend sources. I did not check whether it is used or built, and `Makefile` and `ci.yml` do not obviously reference it. It is probably stale, but that is a guess.
- Not found: any existing "status", "active" or "enabled" concept for employees. No spec or linked docs.
- Not a defect, so no reproduction was needed.

## Complexity and risk
- Complexity **M**: touches entity, DTOs, controller, service, SQL scripts, frontend list and tests, and introduces a new concept (employee status).
- Risk **medium**: it adds a column to a shared table and a new write endpoint on personal data. It also changes the shared response shape and the schema for existing rows.

## Decisions received (admin, 2026-10-17)
- Status is a boolean active/inactive. Default ACTIVE for existing and new employees.
- Any authenticated user may call the PATCH.
- Flag only: no hiding or filtering of inactive employees anywhere.
- Q4 was answered with a bare "Yes" to a compound question. Read as: status is settable in the create/edit form (POST/PUT), and the UI confirms before deactivating. This is my reading, not stated.

- `packages/java/employeemanagement/` is in scope: update it as well as `backend/` (admin, 2026-10-17). This roughly doubles the Java file edits (same changes mirrored), so complexity stays M.

## Remaining gaps
1. None blocking.
2. Minor, can be settled in the spec: PATCH body shape (suggest a JSON body with a boolean `active`), response returns the updated employee DTO, and whether the create/edit form defaults to active.
3. Schema rollout: `ddl-auto=update` plus updating `backend/sql/02_create_tables.sql` and `07_full_setup.sql` with a `NOT NULL DEFAULT TRUE` column. Spec detail.

## Recommendation
`skipSpec`: false. Complexity M, risk medium. No blocking questions remain; ready for the spec phase.
