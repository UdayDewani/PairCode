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

### [2026-10-24] Fix Runtime TypeError room.problem.examples.map is not a function
- **Decision:**
  1. Updated `frontend/lib/api.ts` to export `parseRoom` and apply it to `createRoom`, `room`, `joinRoom`, and `permissions` API endpoints so `problem.examples` and `problem.starterCode` are consistently parsed from backend JSON strings into objects/arrays.
  2. Updated `frontend/components/live-room.tsx` to safely normalize `examples` (handling arrays, JSON strings, or undefined) and wrap room state setters with `parseRoom`.
- **Rationale:** The backend serializes JSONB fields `examples` and `starterCode` as raw JSON strings in the Room DTO. When creating/joining rooms, calling `.map()` on an unparsed string resulted in a runtime TypeError.




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

### [2026-10-29] Upgrade Next.js security release
- **Decision:** Upgraded the frontend from Next.js 16.4.0 to 16.4.3 using `next upgrade --agent=security`, and configured `experimental.agentUpgrade: 'security'` for future security upgrade reminders.
- **Rationale:** The installed Next.js version was covered by a published security advisory. The same-major upgrade does not require a migration codemod.

### [2026-10-29] Add live room language changes
- **Decision:** Added an authorized room-language endpoint with a `language.changed` realtime event. The room UI now lets interviewers and editable candidates choose Java, Python, or C++, keeps custom code intact, and replaces only empty or unchanged starter code with the selected language's starter code.
- **Rationale:** The language selector was permanently disabled despite the runner enforcing that executions match the room language, leaving users unable to switch languages during an interview.

### [2026-10-29] Make the Docker code runner work in the deployed API
- **Decision:**
  1. Copied the static Docker CLI from `docker:27-cli` into the API runtime image so `DockerCodeExecutor` can reach the mounted host socket.
  2. Added `app.runner-work-dir` (`RUNNER_WORK_DIR`, default `/tmp/paircode-run`) and use it for run directories. Compose bind-mounts that identical absolute path into the API. Run directories are `0777` and source/input files `0644` for UID/GID `65534`.
  3. Drains stdout and stderr concurrently with a bounded 64 KB capture so large output cannot deadlock the process. A 1 MB output test passes without hanging.
  4. Removed `--rm` and inspects `State.OOMKilled` before removing the container, so exit 137 is reported as an out-of-memory error rather than a timeout.
  5. Added a startup and periodic self-check exposed as the Actuator `runner` health component. It runs `docker version` and `docker image inspect` for all three images, and logs explicit errors naming missing images or an unavailable daemon.
  6. Added sandbox labels, stale-container/stale-directory cleanup, `--memory-swap` equal to memory, `nofile`/`nproc` limits, and Java `-Xmx128m -XX:+UseSerialGC`.
  7. Used `sh -c` rather than `sh -lc` because the login shell reset `PATH` in the Java runner image and hid `javac`.
- **Rationale:** Deployed Run failed because the API image had no Docker CLI, the daemon could not see the API container's `/tmp`, and the sandbox user could not write to a root-owned `0700` directory. The other changes remove runner deadlocks, make failures diagnosable, and keep leftover sandboxes from accumulating.
- **Verification:** `mvn -B test` passes (24 tests, 1 Testcontainers test skipped because Testcontainers could not negotiate the local Docker Desktop pipe); the executor integration tests ran all three languages plus compile-error, timeout, and large-output cases against locally built runner images. See `docs/code-runner.md`.
