## What and why
Adds a short "Getting Started" section near the top of `README.md`, so a new reader can go from a clone to the frontend at http://localhost:3000 talking to the backend at http://localhost:8080 without reading the 1,000+ line file. Work item: ZF-1. Spec: `docs/specs/ZF-1.md`. Source: https://github.com/dbrijesh/emfp/issues/4.

## What changed
- `README.md`: new `## Getting Started` section, 11 non-blank lines, placed directly before `## Overview`. R1, R3, R4, R5, R6, R7.
  - Prerequisites: Java 11, Maven, Node.js and npm, MySQL, MongoDB. Java 11 matches `backend/pom.xml`. The Node 18 note matches `frontend/Dockerfile`. R3.
  - Steps: copy `backend/example_config.properties` to `backend/config.properties`, fill in `MYSQL_*` and `MONGO_URI`, add `JWT_SECRET`, create the MySQL database. R4.
  - Start commands: `cd backend && mvn spring-boot:run`, then `cd frontend && npm install && npm start`. R5.
  - Check step: Swagger UI at http://localhost:8080/swagger-ui.html and the app at http://localhost:3000. R6.
  - "More detail" line linking to `#backend-setup`, `#frontend-setup` and `#containerization`. R7.
- `README.md`: one Table of Contents entry, `- [Getting Started](#getting-started)`, before the Overview entry. R2.
- Docker Compose is not shown as a command. The section links to Containerization only. R8, open point 1 in the spec.
- No other file in the README changed. The diff against `main` for `README.md` is one TOC line plus the section, with no deletions.

## How it was checked
| Criterion | Check | Result |
|---|---|---|
| AC1 (R1) | `grep -n "^## " README.md` | Pass. `## Getting Started` at l.59, `## Overview` at l.72, one occurrence. |
| AC2 (R2) | Read the TOC diff. Anchor `#getting-started` matches the heading. | Partly proven. The anchor matches the heading text by GitHub's slug rules, but the link was not clicked in a GitHub preview. |
| AC3 (R3) | Compared with `backend/pom.xml` (`java.version` 11) and `frontend/Dockerfile` (`node:18`). | Pass by reading. |
| AC4 (R4) | Steps read against `backend/example_config.properties` and `application.properties` (`file:config.properties`). | Not run. No clean clone was configured. |
| AC5 (R5) | Commands are the ones already in the README (l.532, l.642, l.658). | Not run. No MySQL or MongoDB in the environment, so the backend and frontend were not started and the URLs were not requested. |
| AC6 (R6) | Swagger URL matches the README (l.591). | Not run. Swagger UI was not opened. |
| AC7 (R7) | Anchors `#backend-setup`, `#frontend-setup`, `#containerization` compared with the existing headings. | Pass by reading. |
| AC8 (R8) | Each command in the section, with its source. | Partly. `openssl rand -base64 32` (README l.492), `cd backend && mvn spring-boot:run` (l.532) and `cd frontend && npm install && npm start` (l.642, l.658) are already in the README. `cp backend/example_config.properties backend/config.properties` is not in the README; only the file names are mentioned (l.391, l.481). It was not run. Its only source is the repo layout and the spec. None depends on `make docker-up`. |
| AC9 (R9) | `git diff --stat main` | Not met. See Flags. |
| AC10 (length) | 11 non-blank lines, limit about 30. | Pass. |

Automated tests: none run. The change is Markdown only.

## Review notes
- Look first at the section itself, lines 59-70 of `README.md`.
- Step 2 says to fill in `MYSQL_*` and `MONGO_URI` but gives no values. The file `example_config.properties` has none. The verifier suggested linking to the sample block in Backend Setup or naming the default database.
- Step 1 says "Clone the repository and open it" with no command. The verifier suggested adding the `git clone` command used in Backend Setup (l.458). This is not in the current diff.
- Step 3 says to create the MySQL database but gives no SQL. The verifier suggested `CREATE DATABASE employee_management;` or a link to `backend/sql/01_create_database.mysql.sql`. This is not in the current diff.
- Placement before Overview is the spec default. The approver may prefer another place.
- The Docker Compose path is deliberately not a command. `make docker-up` was judged likely broken (the Makefile names `docker-compose.yaml`; the file is `docker-compose.yml`). Fixing it is out of scope.

## Flags
- **Not run end to end.** AC4, AC5 and AC6 are verified by reading only. The backend and frontend were not started. The reviewer should run the section once with MySQL and MongoDB available.
- **Anchor rendering not checked in GitHub.** AC2 is verified by matching heading text, not by clicking the link in GitHub.
- **Scope differs from R9/AC9.** The branch diff against `main` also contains `docs/specs/ZF-1.md`, `.zerovity/out/spec.md` and `.zerovity/out/triage.md`. These are pipeline artifacts from earlier phases. Only `README.md` is product change. The reviewer should decide whether to keep these files on the branch or remove them before merge. The working tree also shows `.zerovity/out/result.json` as deleted, and this phase rewrites it.
- **R8 is not fully met.** The `cp` command in step 2 is not stated in the README and was not run. The executor's evidence said every command was already in the README; that is not correct. The copy is a plain file copy, but the reviewer should decide whether to keep it or reword the step to say "copy the example file".
- **Three low review findings are open.** The verifier raised the clone command, config values and database creation as low findings (above). They were not fixed, and the verifier accepted the change with them. Fixing them would change the README, so they are left for the reviewer.
- **Revision loops:** none. Verifier recommendation was `accept`.

Work item: ZF-1 · Source: https://github.com/dbrijesh/emfp/issues/4 · Branch: zerovity/zf-1-add-a-short-getting-started-sect

🤖 Generated with [Claude Code](https://claude.com/claude-code)
