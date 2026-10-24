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

### [2026-10-24] Rename UI directory to frontend and push defect fixes to GitHub
- **Decision:**
  1. Renamed `pair-code-ui-design` to `frontend` across the repository to standardize project folder naming.
  2. Updated `README.md` commands and documentation to refer to `frontend`.
  3. Added root `.gitignore` ignoring `.env*`, `.vscode/`, `backend/target/`, `node_modules/`, and `.next/`.
  4. Untracked compiled Maven target classes/reports from git index.
  5. Committed all new implementations (Phase 1-5 defect fixes, new test migrations, unit tests) and pushed to GitHub `origin/main`.
- **Rationale:** User explicit request to rename `pair-code-ui-design` to `frontend` and push all new files to GitHub.
