# Triage: ZF-1 Add a short Getting Started section to the README

## Summary
Add a short "Getting Started" section to `README.md` so a new reader can run the Employee Management app (React + Spring Boot, MySQL + MongoDB) quickly. Issue #4 has only the title; the answers confirm issue #1 adds nothing, so the issue text alone is the requirement.

## Evidence
- `README.md` (1,040+ lines, 48 KB) has no Getting Started section. Setup is spread across "Backend Setup" (l.425), "Frontend Setup" (l.613) and "Containerization" (l.705). The Table of Contents (l.13-56) must also be updated.
- Run commands exist: `Makefile`, `package.json` scripts (`npm run dev` runs backend and frontend together), `docker-compose.yml` (backend 8080, frontend 3000, mysql, mongodb).
- Versions: Java 11 (`backend/pom.xml:23`), Node 18 in `frontend/Dockerfile:2`.
- Not found: `backend/config.properties`. Only `backend/example_config.properties` exists, yet README l.467 tells the reader to use `backend/config.properties`.

## Complexity and risk
- Complexity: S. One markdown file, roughly 20-30 lines plus one TOC line.
- Risk: low. Documentation only, no code, auth or data touched.

## Gaps (non-blocking; I propose defaults)
1. Which path to feature: Docker Compose (fewest prerequisites) or manual run. Default: Docker Compose first, manual as the second option, each linking to the existing detailed sections.
2. Unverified commands. Guesses from reading only: the compose file does not set `JWT_SECRET` (README l.478 marks it required), the mongodb healthcheck uses the `mongo` shell (guess: absent from `mongo:6.0`), and the `Makefile` names `docker-compose.yaml` while the file is `docker-compose.yml`, so `make docker-up` probably fails. I did not run Docker, so the section should only show commands verified at implementation, or avoid `make docker-up`.
3. Placement: default is right after the intro, before Overview, with a TOC entry.
4. `config.properties` vs `example_config.properties` naming: worth a one-line fix in the section (copy the example file).

## Recommendation
skipSpec: false. Gap 2 means commands need checking before they are written down. Proceed to a short spec/implementation with the defaults above; changing the `Makefile` or compose file is out of scope unless the owner asks.
