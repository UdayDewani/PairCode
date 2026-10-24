# Agent Context & Decisions

## Context
- **Workspace:** `D:\Projects\PairCode`
- **Project Structure:**
  - `backend/` (Spring Boot 3.4 / Java 21 API)
  - `docs/` (Project documentation)
  - `frontend/` (Next.js 16 + React 19 UI; renamed from `pair-code-ui-design`)
  - `docker-compose.yml`, `.env.example`, `README.md`

## Major Decisions Log

### [2026-10-24] Run Knowledge Graph Extraction via /graphify
- **Decision:** Executed full graphify pipeline on workspace (`.`), extracted AST from 38 code files and semantic concepts/hyperedges from 9 documents and 9 assets via parallel subagents.
- **Output:** Built graph with 615 nodes, 1,297 edges across 33 detected communities with interactive visualization at `graphify-out/graph.html`.
- **Key Central Bridges Identified:** `RoomService`, `RealtimeService`, `AuthPrincipal`, `SubmissionService`.

### [2026-10-24] Run Project Stack (Docker + Frontend)
- **Decision:** Starting Docker Desktop to bring up the multi-container stack (`postgres`, `redis`, `api`) via `docker compose up --build`, and preparing the Next.js frontend (`frontend/.env.local`, `pnpm dev`).
- **Rationale:** User requested to run the project.

### [2026-10-24] Fix Flyway Migration V6 Foreign Key Constraint
- **Decision:** Updated `V6__real_test_cases_and_starter_code.sql` to delete existing `test_results` referencing the placeholder test cases before deleting the placeholder rows from `test_cases`.
- **Rationale:** Prevented `PSQLException: foreign key constraint "test_results_test_case_id_fkey" violation` when migrating existing databases with submission test runs.



### [2026-10-24] Rename UI directory to frontend and push defect fixes to GitHub
- **Decision:**
  1. Renamed `pair-code-ui-design` to `frontend` across the repository to standardize project folder naming.
  2. Updated `README.md` commands and documentation to refer to `frontend`.
  3. Added root `.gitignore` ignoring `.env*`, `.vscode/`, `backend/target/`, `node_modules/`, and `.next/`.
  4. Untracked compiled Maven target classes/reports from git index.
  5. Committed all new implementations (Phase 1-5 defect fixes, new test migrations, unit tests) and pushed to GitHub `origin/main`.
- **Rationale:** User explicit request to rename `pair-code-ui-design` to `frontend` and push all new files to GitHub.

### [2026-10-24] Exclude all graphify artifacts from git tracking
- **Decision:**
  1. Updated root `.gitignore` with wildcards (`graphify*`, `*graphify*`, `**/graphify*`, `graphify-out/`) to strictly prevent any graphify outputs or working files from being staged or pushed.
  2. Verified `graphify-out/` is marked as ignored in git.
- **Rationale:** User explicit rule not to push graphify artifacts to GitHub.

### [2026-10-24] Restore Landing Page as Default Route for Unauthenticated Visitors
- **Decision:**
  1. Updated `frontend/app/page.tsx` so unauthenticated visitors landing on `/` see the full product landing page (`<Landing />`) instead of immediately displaying the sign in / sign up card.
  2. Routed user action buttons ("Log in", "Get started", "Start interviewing", "View demo") to open `<Auth />` with the appropriate mode (login vs register).
  3. Added "Back to home" navigation in `<Auth />` to easily return to the landing page.
- **Rationale:** User reported navigating to `http://localhost:3000/` was bypassing the landing page and immediately showing the sign up form.
