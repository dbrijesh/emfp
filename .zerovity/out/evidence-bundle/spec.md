# ZF-2 Employee status toggle
Status: Draft

## 1. Problem and outcome
Users of the employee directory cannot see or change whether an employee is active. This change adds a boolean active/inactive status to each employee, a `PATCH /api/employees/{id}/status` endpoint, and a Status column with a toggle switch in the employee list. Success: a user flips the switch, the change persists across reload, and the API returns the new status. Source: https://github.com/dbrijesh/emfp/issues/6

## 2. Scope
In scope
- `active` boolean on the Employee entity and the employees table. Existing and new employees default to active.
- `PATCH /api/employees/{id}/status`, callable by any authenticated user.
- `active` exposed in employee responses and accepted in POST/PUT request bodies.
- Status column with toggle switch in `EmployeeList`, and a status control in `EmployeeForm`.
- Confirmation dialog before deactivating (see section 6, O1).
- Mirror all backend changes into `packages/java/` (decision from CONTEXT.md).
- SQL bootstrap scripts updated for the new column.

Out of scope
- Filtering, hiding or counting by status anywhere (list, dashboard, department views, CSV export). The flag is display-only.
- Role-based permissions beyond "authenticated".
- Status values other than active/inactive; status history or audit.

## 3. Requirements
- R1. The system must store an `active` boolean per employee, and every existing employee must be active after the change.
- R2. A newly created employee without an explicit `active` value must be active.
- R3. Employee responses (GET list, GET by id, POST, PUT, PATCH) must include `active`.
- R4. `PATCH /api/employees/{id}/status` with body `{"active": <boolean>}` must set the employee's status and return 200 with the updated employee.
- R5. The PATCH endpoint must change only the status; all other employee fields must remain unchanged.
- R6. The PATCH endpoint must return 404 for an unknown id, and 400 when `active` is missing or not a boolean.
- R7. The PATCH endpoint must return 401 for an unauthenticated request.
- R8. POST and PUT must accept an optional `active` field. On PUT, an omitted `active` must leave the stored status unchanged.
- R9. The employee list must show a Status column with a toggle switch reflecting `active` for each row.
- R10. Flipping the switch to active must call the PATCH endpoint and, on success, show the new state without a page reload.
- R11. Flipping the switch to inactive must first ask the user to confirm; cancelling must leave the state unchanged and send no request.
- R12. If the PATCH call fails, the switch must keep its previous state and an error toast must be shown. The switch must be disabled while its request is in flight.
- R13. The employee form (add and edit) must let the user set status, defaulting to active for new employees and showing the stored value when editing.
- R14. Status must not affect list contents, filtering, counts or CSV output.
- R15. The same backend changes must exist in `packages/java/`.

## 4. Acceptance criteria
- AC1 (R1). Given a database with employees created before this change, when the backend starts, then every employee row has `active = true`.
- AC2 (R2). Given a POST body without `active`, when the employee is created, then the response has `active: true`.
- AC3 (R3). Given any employee, when GET /api/employees and GET /api/employees/{id} are called, then each employee JSON contains boolean `active`.
- AC4 (R4). Given an active employee and a valid JWT, when PATCH `/status` with `{"active": false}` is sent, then the response is 200 with `active: false` and a following GET returns `active: false`.
- AC5 (R5). Given an employee, when PATCH `/status` is sent, then firstName, lastName, email, age and department are identical before and after.
- AC6 (R6). Given a valid JWT, when PATCH is sent for a nonexistent id, then 404; when the body is `{}` or `{"active":"yes"}`, then 400 and the stored status is unchanged.
- AC7 (R7). Given no Authorization header, when PATCH is sent, then 401 and the status is unchanged.
- AC8 (R8). Given POST with `active:false`, then the response has `active:false`. Given PUT without `active` on an inactive employee, then the employee stays inactive. Given PUT with `active:true`, then it becomes active.
- AC9 (R9). Given the list page with a mix of statuses, then the header has "Status" and each row's switch is checked iff `active` is true.
- AC10 (R10). Given an inactive row, when the switch is clicked, then PATCH `{"active":true}` is sent and the switch shows checked after the response, with no refetch of the list.
- AC11 (R11). Given an active row, when the switch is clicked, then a confirm dialog appears and no request is sent; on Confirm PATCH `{"active":false}` is sent; on Cancel the switch stays checked and no request is sent.
- AC12 (R12). Given the PATCH rejects, then the switch shows its previous state and an error toast appears; while the request is pending the switch is disabled.
- AC13 (R13). Given /add-employee, then the status control is on by default and the submitted body carries `active`. Given /edit-employee/{id} for an inactive employee, then the control is off.
- AC14 (R14). Given inactive employees exist, then the row count, "Total employees" and "Visible now" tiles, search and filters, and CSV output are the same as if all were active.
- AC15 (R15). Given the change, then each backend file changed under `backend/src/main/java` has the same change in `packages/java/employeemanagement`, verified by diff.

## 5. Technical design
Backend (`backend/src/main/java/com/example/employeemanagement/`)
- `model/Employee.java`: add `@Column(nullable = false, columnDefinition = "boolean default true") private boolean active = true;`. The column default matters: `ddl-auto=update` adds the column to existing tables and, without a default, MySQL fills existing rows with 0 (inactive), which would break R1. `@AllArgsConstructor` signature changes; check test usages of the all-args constructor.
- `dto/EmployeeRequestDto.java`: add `Boolean active` (optional). `dto/EmployeeResponseDto.java`: add `boolean active`.
- New `dto/EmployeeStatusRequestDto.java`: `@NotNull Boolean active`. A non-boolean JSON value fails deserialisation; confirm `GlobalExceptionHandler` maps this to 400 and add a handler if not.
- `controller/EmployeeController.java`: add `@PatchMapping("/{id}/status")` taking `@Valid @RequestBody EmployeeStatusRequestDto`; load via `employeeService.getEmployeeById` (404 via `ResourceNotFoundException`), set `active`, save via `saveEmployee`, return `convertToDto`. Update `convertToDto` to set `active`; `convertToEntity` and `updateEmployee` set it only when the request value is non-null. Add `@Operation`/`@ApiResponses` like sibling methods.
- `security/SecurityConfig.java`: currently `anyRequest().permitAll()`, so all employee endpoints are unauthenticated. Add `.antMatchers(HttpMethod.PATCH, "/api/employees/*/status").authenticated()` before `anyRequest()`. The existing entry point already returns 401 JSON. Other employee endpoints stay as they are. `CorsConfig` already allows PATCH.
- `config/DataInitializer.java`: no change needed (field default is true).
- SQL: add `active BOOLEAN NOT NULL DEFAULT TRUE` to `employees` in `backend/sql/02_create_tables.sql`, `databases/sql/02_create_tables.sql`, and the combined `07_full_setup.sql` in both folders; add an `ALTER TABLE` note for existing databases. Seed inserts name their columns, so the default applies; check `05_views.sql` still works.
- `packages/java/employeemanagement/**` and `employeemanagementtest/**`: apply the identical edits.

Frontend (`frontend/src/`)
- `services/employeeService.js`: add `updateEmployeeStatus(id, active)` calling `axios.patch` on `<API_URL>/<id>/status` with `{ active }`.
- `components/EmployeeList.js`: add a Status header cell and a body cell with MUI `Switch` (already imported) checked from `employee.active`; per-row pending id state like `deletingEmployeeId`; MUI `Dialog` for deactivation confirmation; on success update that row in `employees` state; on error `notifyApiError` and no state change. Update the empty-state `colSpan` from 6 to 7.
- `components/EmployeeForm.js`: add `active: true` to initial state, load it when editing, render a `FormControlLabel` + `Switch` labelled "Active", and send it in the payload.
- Snapshots in `frontend/__tests__/snapshots/` for EmployeeList and EmployeeForm will change and must be regenerated.

Failure handling: PATCH failure leaves the UI state unchanged (R12); 400/404/401 as in R6/R7.

## 6. Risks and open points
- O1. CONTEXT.md answered "Yes" to a combined question (status settable in form, and confirmation on deactivate). This spec reads it as yes to both. Approver: confirm.
- O2. R7 adds the first authentication requirement on an employee endpoint; all other employee endpoints stay open. This follows the answer "any authenticated user" but leaves the API inconsistent. Securing the rest is out of scope.
- O3. `frontend/src/services/employeeService.js` hardcodes `API_URL` to the Render deployment and sets no auth header itself. The implementer must check how the JWT reaches requests (e.g. an axios default set in `useAuth`/`authService`); if it does not, the toggle would get 401 in the UI. Resolve during implementation.
- O4. The migration relies on `ddl-auto=update` plus the column default; the repo has no migration tool.
- O5. `DataInitializer` skips seeding when data exists, so existing deployments get the default via the column default only.

## 7. Traceability
| R | AC | Planned check |
|---|----|---------------|
| R1 | AC1 | Backend test on repository default + manual check on an existing DB |
| R2 | AC2 | `backend/src/test/java/com/example/employeemanagement/EmployeeStatusApiTest.java` (new) |
| R3 | AC3 | same file |
| R4 | AC4 | same file |
| R5 | AC5 | same file |
| R6 | AC6 | same file |
| R7 | AC7 | same file (no token) |
| R8 | AC8 | same file |
| R9 | AC9 | `frontend/__tests__/EmployeeList.test.js` |
| R10 | AC10 | `EmployeeList.test.js` |
| R11 | AC11 | `EmployeeList.test.js` |
| R12 | AC12 | `EmployeeList.test.js` |
| R13 | AC13 | new `frontend/__tests__/EmployeeForm.test.js` + updated snapshot |
| R14 | AC14 | `EmployeeList.test.js` |
| R15 | AC15 | Manual: `diff -r` of backend vs packages/java trees |
