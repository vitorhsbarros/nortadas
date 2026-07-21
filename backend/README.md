# Nortadas backend

Spring Boot service reporting Nortada wind conditions on Portuguese beaches.
Architecture rules live in [`docs/architecture.md`](../docs/architecture.md) — read it before
adding classes or packages.

## Prerequisites

- JDK 21 is provisioned automatically by the Gradle toolchain (foojay resolver).
- Docker (only for running the app against a real PostgreSQL).

## Run locally

Start PostgreSQL (credentials match the `application.yml` defaults):

```bash
docker compose -f backend/docker-compose.yml up -d
```

Then start the app from the repo root:

```bash
./gradlew :backend:bootRun
```

Flyway applies the migrations in `src/main/resources/db/migration` on startup.
Datasource settings are overridable via `POSTGRES_HOST`, `POSTGRES_PORT`, `POSTGRES_DB`,
`POSTGRES_USER`, `POSTGRES_PASSWORD`; the Open-Meteo base URL via `OPEN_METEO_BASE_URL`.

## Build & test (no Docker/PostgreSQL needed)

```bash
./gradlew build
```

Tests run under the `test` profile (`src/test/resources/application-test.yml`), which swaps the
datasource for in-memory H2 in PostgreSQL compatibility mode — the context-load test still
exercises the Flyway migration, so the build is self-contained.

## Security posture

`SecurityConfig` currently permits all requests (public read-only API, no auth user stories yet)
with CSRF disabled and stateless sessions. Revisit when authenticated features land.
