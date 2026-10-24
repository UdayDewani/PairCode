# Frontend audit

## Implementation plan

1. Create the Spring Boot service, database migrations, local Docker stack, and runner images.
2. Add authentication, profiles, rooms, participants, problem library, and seeded data.
3. Add STOMP/WebSocket collaboration, chat, presence, editing permissions, and room events.
4. Add queued sandbox execution, submissions, snapshots, reviews, documentation, and tests.
5. Replace the prototype's local handlers and sample collections with one REST client and a reconnecting WebSocket client.

## Frontend structure

The frontend is a Next.js 16 / React 19 application. It has no route-based screens, global state manager, API client, authentication storage, WebSocket client, or fake server. `app/page.tsx` is a client component that switches a local `View` union to render every screen. Tailwind CSS is used for styling; `components/ui/button.tsx` supplies the only shared UI primitive.

## Screens

| Screen | Existing behavior | Required API |
| --- | --- | --- |
| Landing | Navigates locally to auth/dashboard | none |
| Login / sign-up | Inputs are not state-bound; button always opens dashboard | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |
| Dashboard | Hard-coded stats and recent sessions; create opens a local modal | `GET /api/v1/sessions`, `GET /api/v1/dashboard`, `POST /api/v1/rooms` |
| Create room modal | Form is mostly uncontrolled and discards choices | `POST /api/v1/rooms`, `GET /api/v1/problems` |
| Room | Code, run state, lock state, chat, participants and output are all local state/sample text | room REST endpoints plus `/ws` room topics |
| Problem library | Four-item static array; filter/search controls do nothing | `GET /api/v1/problems` |
| Session history | Static rows and non-functional search | `GET /api/v1/sessions` |
| Profile | Static read-only values and inactive save controls | `GET/PATCH /api/v1/profile`, `POST /api/v1/profile/password` |

## Expected data shapes

The visual prototype currently exposes only display fields. The backend preserves these and adds identifiers needed for real requests.

- **Room:** `id`, `roomId`, `name`, `status`, `problem`, `language`, `candidateCanEdit`, `editorLocked`, `startedAt`, `endedAt`, `participants`.
- **Problem list item:** `id`, `number`, `title`, `difficulty`, `topic`, `acceptanceRate`; detail adds `description`, `examples`, `constraints`, `starterCode`.
- **User / participant:** `id`, `name`, `email`, `role`, `online`, `avatarColor`.
- **Execution / submission:** `id`, `status`, `stdout`, `stderr`, `compileError`, `exitCode`, `executionTimeMs`, `memoryBytes`, `score`, `testResults`.
- **Test result:** `testCaseId`, `passed`, `runtimeMs`, `actualOutput`, `expectedOutput` (visible cases only).
- **Chat message:** `id`, `roomId`, `author`, `content`, `type`, `createdAt`.
- **Session:** `id`, `room`, `participants`, `problem`, `language`, `score`, `durationSeconds`, `status`, `snapshots`, `submissions`, `chatMessages`, `notes`.

## Required real-time events

The prototype has visual placeholders for a connection state, typed/cursor labels, participant list, chat, editor lock, run state and session end. The client needs `room.join`, `room.leave`, `doc.update`, `awareness`, `chat.send`, `run.request`, `permission.change` client messages and the corresponding broadcast events documented in `docs/realtime-protocol.md`.

## Mismatches and decisions

- The current code editor is a static `<pre>`, not a Yjs-aware editor. Integration introduces a text editor with an update event relay; adding a full Monaco/Yjs binding would change the dependency/UI surface and is explicitly deferred.
- UI roles are contradictory in the room sample (top bar says interviewer, while the participant labels name Uday candidate). Server roles are authoritative and the current user’s role is displayed from the participant record.
- There is no dedicated session-review screen, only a history table. A review action loads the persisted session payload; visual expansion is kept minimal to preserve the existing design.
- The Google button has no OAuth configuration in the project and remains visibly disabled rather than pretending to authenticate.
