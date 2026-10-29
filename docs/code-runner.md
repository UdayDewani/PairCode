# Code runner

The API executes candidate code in throwaway Docker containers through `DockerCodeExecutor`. It shells out to the Docker CLI, so the API container needs both the Docker CLI binary and the host Docker socket:

- `backend/Dockerfile` copies the static CLI from `docker:27-cli` into the runtime image.
- `docker-compose.yml` and `docker-compose.prod.yml` mount `/var/run/docker.sock` and bind `RUNNER_WORK_DIR` at the same absolute path on the host and inside the API. The default is `/tmp/paircode-run`.
- The runner images are `paircode-runner-python`, `paircode-runner-java`, and `paircode-runner-cpp`.

Build the runner images once after cloning or changing their Dockerfiles:

```sh
docker build -t paircode-runner-python backend/runner-images/python
docker build -t paircode-runner-java backend/runner-images/java
docker build -t paircode-runner-cpp backend/runner-images/cpp
```

## Work directory

The Docker daemon resolves bind-mount sources on the host, not inside the API container. `RUNNER_WORK_DIR` must therefore be an absolute path that is identical on the host and in the API container. The compose bind mount uses `${RUNNER_WORK_DIR:-/tmp/paircode-run}` on both sides.

The host filesystem for this directory must not be mounted `noexec`, because `javac`/`g++` files are executed from the bind mount. Check it on the VPS with:

```sh
findmnt -no TARGET,OPTIONS /tmp
```

If it is `noexec`, use another absolute directory and set `RUNNER_WORK_DIR` for the compose deployment and the deploy workflow's `mkdir` step.

Run directories are created under the work directory with mode `0777`, and source/input files are `0644`, because sandbox processes run as UID/GID `65534:65534`. Directories and containers are removed after each run. On startup and every five minutes, the API also removes `paircode-run-*` containers and `run-*` directories older than 60 seconds.

## Health check

`GET /actuator/health` includes a `runner` component. When `RUNNER_ENABLED=true`, startup runs `docker version` and `docker image inspect` for each runner image. A missing CLI, socket, or image logs an explicit `ERROR` and makes the component `DOWN`; details name the missing images.

```sh
curl -s http://localhost:8080/actuator/health | jq .components.runner
```

In the deployed API container, verify the CLI can reach the daemon:

```sh
docker compose -f docker-compose.prod.yml exec api docker version
```

## Execution limits

Containers run with no network, a read-only root filesystem, a writable `/tmp` tmpfs, 1 CPU, a 256 MB memory and memory-swap limit, `nofile=256`, `nproc=64`, all capabilities dropped, and `no-new-privileges`. Compilation gets 30 seconds. Execution gets 5 seconds inside the container and 8 seconds overall. Java runs with `-Xmx128m -XX:+UseSerialGC`. Captured stdout and stderr are capped at 64 KB each, but both streams are drained continuously so large output does not block the process.

## Tests

`backend/src/test/java/dev/paircode/api/DockerCodeExecutorTest.java` runs Python, Java, and C++ programs plus compile-error, timeout, and large-output cases. The tests skip when the Docker CLI/daemon is unavailable or the runner images are not built. Build the images first, then run:

```sh
cd backend
mvn -B test
```
