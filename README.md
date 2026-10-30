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

The API image includes the Docker CLI and talks to the host daemon through `/var/run/docker.sock`. The runner work directory defaults to `/tmp/paircode-run` and must be the same absolute path on the host and in the API container; override it with `RUNNER_WORK_DIR` in both places if needed. Docker Desktop's `/tmp` bind is `noexec`, so local runs there should use an executable path such as `RUNNER_WORK_DIR=/var/lib/paircode-run docker compose up --build`. See [docs/code-runner.md](docs/code-runner.md) for the runner health check, cleanup, limits, and troubleshooting.

Copy `.env.example` and set a strong, unique `JWT_SECRET` outside development. Never commit `.env.local` or production secrets.

## Manual checklist

1. Register two accounts and log in in two browser profiles.
2. Create a room, copy its `PC-XXXXX` code, and join it from the second account.
3. Open a WebSocket from each profile; relay a Yjs binary update and awareness event.
4. Send chat, run a complete Python/Java/C++ program with its stdin, and submit. Untouched starter code must finish as `WRONG_ANSWER` with per-test details; a correct solution must score 100; a Java compile error must show the `javac` message.
5. Change the room language as the interviewer; confirm the filename extension and editor mode change, and that an empty/starter document is replaced exactly once. Verify a locked candidate sees the language selector disabled with its reason.
6. Lock candidate editing, verify both REST snapshot and WebSocket document update are forbidden, then unlock.
7. End the room and inspect `GET /api/v1/sessions/{id}` as the interviewer.
8. Check `GET /actuator/health` shows `runner` as `UP`; remove a runner image tag and restart to confirm it reports that image as missing.
9. Run `docker compose -f docker-compose.prod.yml exec api docker version` on the deployed host.

## Security notes and open decisions

The execution runner is deliberately isolated behind `CodeExecutor`. Docker limits network, CPU, memory, process count, filesystem writes, execution time and captured output. This reduces risk but is not a complete multi-tenant sandbox: production should use a dedicated runner host/VM or gVisor/Kata, seccomp/AppArmor, cgroup monitoring, image signing, stricter mount isolation, and an asynchronous Redis-backed queue. The current queue is a bounded in-process worker pool; Redis is available in the stack for the next production queue/presence implementation.

The prototype did not contain an editor binding, OAuth configuration, or a session-review visual. The API and protocol support these features; the UI keeps the original layout and adds real authentication/room creation without introducing a large editor UI dependency.
