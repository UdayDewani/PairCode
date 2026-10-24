# PairCode

PairCode is a collaborative coding-interview application. The existing Next.js UI lives in `frontend`; the Spring Boot 3 / Java 21 API is in `backend`.

## Run locally

Prerequisites: Docker Desktop and Docker Compose. From this directory:

```sh
docker compose up --build
cd frontend
copy .env.example .env.local
pnpm install
pnpm dev
```

The API is at `http://localhost:8080`, API documentation is at `http://localhost:8080/swagger-ui.html`, and the frontend is at `http://localhost:3000`. Before enabling execution, build the runner images once:

```sh
docker build -t paircode-runner-python backend/runner-images/python
docker build -t paircode-runner-java backend/runner-images/java
docker build -t paircode-runner-cpp backend/runner-images/cpp
```

Copy `.env.example` and set a strong, unique `JWT_SECRET` outside development. Never commit `.env.local` or production secrets.

## Manual checklist

1. Register two accounts and log in in two browser profiles.
2. Create a room, copy its `PC-XXXXX` code, and join it from the second account.
3. Open a WebSocket from each profile; relay a Yjs binary update and awareness event.
4. Send chat, run a complete Python/Java/C++ program, and submit against the selected problem.
5. Lock candidate editing, verify both REST snapshot and WebSocket document update are forbidden, then unlock.
6. End the room and inspect `GET /api/v1/sessions/{id}` as the interviewer.

## Security notes and open decisions

The execution runner is deliberately isolated behind `CodeExecutor`. Docker limits network, CPU, memory, process count, filesystem writes, execution time and captured output. This reduces risk but is not a complete multi-tenant sandbox: production should use a dedicated runner host/VM or gVisor/Kata, seccomp/AppArmor, cgroup monitoring, image signing, stricter mount isolation, and an asynchronous Redis-backed queue. The current queue is a bounded in-process worker pool; Redis is available in the stack for the next production queue/presence implementation.

The prototype did not contain an editor binding, OAuth configuration, or a session-review visual. The API and protocol support these features; the UI keeps the original layout and adds real authentication/room creation without introducing a large editor UI dependency.
