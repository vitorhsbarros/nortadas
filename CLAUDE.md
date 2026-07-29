# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Nortada is an app to check "Nortada" wind conditions on Portuguese beaches. The backend is a **Spring Boot**
service (Web, JPA, PostgreSQL, Flyway, Security, HATEOAS) scaffolded under `backend/` (Phase 1a per
`docs/user-stories/`; the old plain-Gradle `app/` skeleton has been removed). Package layout follows the
Clean Architecture layering in `docs/architecture.md`, not the flat OOA layout in
`docs/OOA/package-structure.md` (the latter is superseded — see Architecture below).

## Commands

All commands run from the `nortadas/` project root (the Gradle root project) using the wrapper:

- Build: `./gradlew build`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.nortadas.domain.beach.BeachTest"`
- Run a single test method: `./gradlew test --tests "com.nortadas.domain.beach.BeachTest.createConstructorGeneratesIdentityAndKeepsAttributes"`
- Run the app: `./gradlew :backend:bootRun` — needs a running PostgreSQL matching `backend/docker-compose.yml`'s
  defaults (`docker compose -f backend/docker-compose.yml up -d` first). The app defaults to port `8081`
  (not Spring's usual `8080`, which is commonly already taken locally) — override via `SERVER_PORT`.
- The build itself does **not** need Docker/Postgres: `NortadasApplicationTests` boots the full Spring context
  (including the Flyway migration) against H2 in PostgreSQL-compatibility mode via the `test` profile
  (`backend/src/test/resources/application-test.yml`).

Dependency versions for Spring-managed libraries (starters, PostgreSQL driver, Flyway, H2) come from the
Spring Boot BOM, not `gradle/libs.versions.toml` — that file only pins the Boot/dependency-management plugin
versions. The Gradle wrapper is pinned to **9.2.1** (not the latest) for IntelliJ Gradle-sync compatibility;
don't bump it without checking IDE support first.

The **domain (business rules) layer must be pure Java — no frameworks, and no Lombok** (see
`docs/architecture.md` §3.1); hand-write constructors, getters, and `equals`/`hashCode` there. Lombok is fine
in other layers (JPA data models, DTOs, adapters). The domain layer (`backend/src/main/java/com/nortadas/domain/`)
is fully migrated to plain Java — verify with
`grep -rn "lombok\|springframework" backend/src/main/java/com/nortadas/domain` (should return nothing).

**Do not re-add a blanket `application.yml` / `application-*.yml` rule to `.gitignore`.** An old, uncommented
rule like that once silently excluded `backend/src/main/resources/application.yml` and the entire
`backend/src/test/resources/` directory from every commit despite the files existing on disk — every local
build passed while the actual pushed branch was missing its config entirely. Neither file holds real secrets
(datasource credentials are env-var driven with a documented local-dev default, already Gitleaks-allowlisted);
they must stay tracked. If a build looks green locally but you're unsure the branch is self-contained, verify
with a detached `git worktree` at the tip commit rather than trusting the working directory.

## Architecture

`docs/architecture.md` is the source of truth for Clean Architecture layering, package structure and
per-package rationale, the domain/data-model/DTO three-model separation, SOLID/GRASP/GoF conventions, web
layer conventions, and testing/coverage expectations — **read it before adding new classes, packages, or
relationships.** `docs/OOA/nortada-OOA.puml` and `docs/OOD/nortada-OOD.puml` hold the PlantUML diagrams behind
it, `docs/OOD/sequences/` the interaction flows, `docs/OOD/api-contract.md` the REST contract, and
`docs/OOD/design-decisions.md` the ADRs — all cross-referenced from `docs/architecture.md` §10. These stay in
step with the code; where they disagree, the code wins and the diagram is a bug worth fixing in the same PR.

**Aggregate roots are constructed only through their `*Factory`** (GoF Factory / GRASP Creator;
`docs/architecture.md` §7). Entity constructors are **package-private** — `new Beach(...)` will not compile
outside `domain.beach`; go through `BeachFactory.create(...)`/`.rehydrate(...)` (same pattern for `Region`,
`Municipality`).

## CI (`.github/workflows/ci-pipeline.yml`)

Runs on pull requests with four jobs; three are implemented and blocking, `sca` is still a placeholder:
- `secret-scan` — the Gitleaks **CLI**, pinned to 8.30.1 and run directly, scanning the **full commit
  history** (`gitleaks git .`) rather than just the PR's commit range: a secret is leaked the moment it is
  committed, even if a later commit removes it. `--redact` keeps found values out of the logs and the SARIF,
  which uploads to code scanning under the `gitleaks` category. Needs `fetch-depth: 0`.
  Deliberately **not** `gitleaks/gitleaks-action@v2` — that action has no `args` input, so the previous
  `args: detect --source=. --no-git` was silently discarded on every run while the job still reported green;
  it also requires a paid `GITLEAKS_LICENSE` for org-owned repos. (`detect` is no longer a gitleaks command
  either — 8.x splits it into `git` and `dir`.)
  `.gitleaks.toml` allowlist paths are **anchored** (`(^|/)src/test/`, `\.(test|spec)\.[jt]sx?$`) on purpose:
  they used to be bare substrings (`.*test.*`), which exempted any path merely containing those letters —
  `LatestConfig.java`, `contest-data.properties`, `inspector.ts` — from secret scanning entirely. Don't
  loosen them back to substrings.
- `sast-semgrep` — Semgrep OSS CLI (no `SEMGREP_APP_TOKEN`), blocking on `ERROR`-severity findings only,
  with SARIF uploaded to code scanning for inline PR annotations. Two scans: the backend against
  `p/java` + `p/security-audit` + `p/owasp-top-ten`, and the mobile app against `p/typescript` +
  `p/react` + `p/security-audit`. The mobile scan is gated on `mobile/package.json` existing, so it
  no-ops until US014 lands and then enforces itself with no workflow edit. Each scan uploads under its
  own SARIF `category`; without that the second upload would replace the first in code scanning.
  Scans use `continue-on-error` plus a trailing enforce step **on purpose** — `--error` exits non-zero
  on findings, which would otherwise skip the SARIF upload on exactly the runs where the annotations
  matter. Don't "simplify" that away.
- `build-and-test-with-coverage` — validates the Gradle wrapper checksum, then runs `./gradlew build` on
  JDK 21. That single command *is* the coverage gate: `build` → `check` → `jacocoTestCoverageVerification`
  (≥95% line and branch on `com.nortadas.domain*`), so no separate coverage step is needed. Needs no
  Docker/PostgreSQL — the `test` profile runs against H2. The JaCoCo HTML report uploads as an artifact
  even on failure, which is when it's most useful.
- `sca` — placeholder ("coming in Phase 2")

All files require review from `@vitorhsbarros` per `.github/CODEOWNERS`.
