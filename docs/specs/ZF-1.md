# ZF-1 Add a short Getting Started section to the README
Status: Draft

## 1. Problem and outcome
A new reader of `README.md` has to piece setup together from "Backend Setup" (l.425), "Frontend Setup" (l.613) and "Containerization" (l.705) in a 1,050-line file. We add one short "Getting Started" section near the top that gets them from clone to a running app. Success: a reviewer can follow only that section and reach the frontend at http://localhost:3000 talking to the backend at http://localhost:8080.

## 2. Scope
In scope:
- One new `## Getting Started` section in `README.md`, about 20-30 lines.
- One Table of Contents entry for it.
- Links from the section to the existing detailed sections.

Out of scope:
- Any change to code, `Makefile`, `docker-compose.yml`, `backend/example_config.properties` or other docs.
- Rewriting or removing existing README sections.
- Deployment (Kubernetes, AWS, Jenkins) instructions.

## 3. Requirements
- R1: The README must contain a `## Getting Started` section placed immediately before `## Overview`.
- R2: The Table of Contents must list "Getting Started" with a working anchor, before the "Overview" entry.
- R3: The section must state the prerequisites: Java 11 (`backend/pom.xml`), Maven, Node.js and npm, MySQL, MongoDB. A Node version is stated only if verified (`frontend/Dockerfile` uses Node 18).
- R4: The section must give the steps to configure the backend: copy `backend/example_config.properties` to `backend/config.properties`, fill in the listed keys (`MYSQL_*`, `MONGO_URI`) plus `JWT_SECRET`, and make sure the MySQL database named in `MYSQL_DB` exists.
- R5: The section must give the commands to start the backend (`mvn spring-boot:run` in `backend`) and the frontend (`npm install`, then `npm start` in `frontend`), and the URL where each is reachable.
- R6: The section must give one way to check the app works (Swagger UI at http://localhost:8080/swagger-ui.html, and the frontend at http://localhost:3000).
- R7: The section must link to the detailed sections ("Backend Setup", "Frontend Setup", "Containerization") instead of repeating them.
- R8: Every command shown must be either already stated in the README or run successfully at implementation; unverified commands must not be shown (see open point 1 on Docker Compose).
- R9: Nothing outside the new section and its TOC line may change in the repository.

## 4. Acceptance criteria
- AC1 (R1): Given `README.md`, when headings are listed, then `## Getting Started` appears once, directly before `## Overview`.
- AC2 (R2): Given the TOC, when the "Getting Started" link is clicked in GitHub's rendering, then it jumps to the section, and the entry sits before "Overview".
- AC3 (R3): Given the section, when read, then each prerequisite in R3 is named and the Java version matches `backend/pom.xml`.
- AC4 (R4): Given a fresh clone with no `backend/config.properties`, when the configure steps are followed, then `backend/config.properties` exists with a value for every key in `example_config.properties` plus `JWT_SECRET`.
- AC5 (R5): Given the prerequisites are running and config is done, when the backend and frontend commands are run, then http://localhost:8080 and http://localhost:3000 respond.
- AC6 (R6): Given both are running, when the Swagger URL is opened, then Swagger UI loads, and the section names this as the check.
- AC7 (R7): Given the section, when each link is followed, then it resolves to an existing heading in the README.
- AC8 (R8): Given the PR description, then it lists each command shown and how it was verified (run, or already in the README), and no command depends on `make docker-up`.
- AC9 (R9): Given `git diff --stat main`, then only `README.md` is listed, and `git diff main -- README.md` shows only the added section and one TOC line.
- AC10 (length): Given the section, then it is at most about 30 non-blank lines.

## 5. Technical design
- File touched: `README.md` only.
- TOC (l.13-56): insert `- [Getting Started](#getting-started)` before `- [Overview](#overview)`.
- Section: inserted immediately before `## Overview` (l.58), so the TOC stays first.
- Content, in order: prerequisites bullets; numbered steps (clone, copy config, create DB, start backend, start frontend); a verify line; a "More detail" line with links.
- Commands come from existing README text and repo files: `cp backend/example_config.properties backend/config.properties` (that file lists key names only, no values, so the step must say to fill them in; `backend/src/main/resources/application.properties` imports `file:config.properties` relative to the working directory, i.e. `backend/`), `mvn spring-boot:run`, `npm install`, `npm start`, and `openssl rand -base64 32` for `JWT_SECRET` (README l.478).
- Failure handling: none at runtime. Markdown only; check that anchors render.

## 6. Risks and open points
1. Docker Compose path (approver decision). Triage suggested leading with Docker Compose, but it is unverified: `docker-compose.yml` does not set `JWT_SECRET`, the mongodb healthcheck uses the `mongo` shell (may be absent in `mongo:6.0`), and the `Makefile` names `docker-compose.yaml`, so `make docker-up` likely fails. Proposed default: lead with the manual path (every step is already in the README) and give Docker Compose only a link to "Containerization", with no command, unless the implementer runs `docker compose up --build` successfully. Fixing compose or the Makefile is out of scope.
2. `example_config.properties` lacks `JWT_SECRET` and has no values, and the README tells readers to use `config.properties`. The section covers this in one line; the file itself is not changed here.
3. Placement before `## Overview` is a default; the approver may prefer elsewhere.
4. Issue #4 has only a title; audience and length are inferred ("short", new developer).

## 7. Traceability
| R | AC | Planned check |
|---|----|---------------|
| R1 | AC1 | Manual: list headings in `README.md` |
| R2 | AC2 | Manual: click TOC link in a GitHub markdown preview |
| R3 | AC3 | Manual: compare with `backend/pom.xml` |
| R4 | AC4 | Manual: follow steps on a clean clone |
| R5 | AC5 | Manual: run both commands, request both URLs |
| R6 | AC6 | Manual: open Swagger UI |
| R7 | AC7 | Manual: follow each link |
| R8 | AC8 | Review of PR description |
| R9 | AC9, AC10 | `git diff --stat main`, `git diff main -- README.md` |
