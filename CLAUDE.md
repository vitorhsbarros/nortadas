# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Nortada is an app to check "Nortada" wind conditions on Portuguese beaches. The repo is currently in early
scaffolding (Phase 0/1a per `docs/user-stories/`): a plain Gradle Java skeleton exists under `app/`, but the
intended backend is a **Spring Boot** service (Web, JPA, PostgreSQL, Flyway, Security, HATEOAS) that has not
been scaffolded yet — see `docs/OOA/package-structure.md` for the target package layout
(`controller` / `service` / `repository` / `domain` / `dto` / `scheduler` / `config`). Only `domain` exists today.

## Commands

All commands run from the `nortadas/` project root (the Gradle root project) using the wrapper:

- Build: `./gradlew build`
- Run all tests: `./gradlew test`
- Run a single test class: `./gradlew test --tests "com.nortadas.AppTest"`
- Run a single test method: `./gradlew test --tests "com.nortadas.AppTest.appHasAGreeting"`
- Run the app: `./gradlew run` — **note:** `app/build.gradle` currently sets `mainClass = 'org.example.App'`
  (a leftover from `gradle init`), but the real class is `com.nortadas.App`. This must be fixed before `run` works.

Dependency versions (Guava, JUnit Jupiter) are managed centrally in `gradle/libs.versions.toml`, not in
`app/build.gradle` directly. Lombok is used for domain models (`@Getter`, `@EqualsAndHashCode`, `@Value`).

## Architecture notes

- Domain classes enforce their own invariants in constructors rather than via a validation framework, e.g.
  `Name` rejects blank/too-short/too-long/special-character values, `Region` rejects a null `Name`
  (`app/src/main/java/com/nortadas/domain/`).
- `Region` generates its own `UUID` identity in the constructor rather than relying on a persistence layer.
- Several domain classes (`FavouriteBeaches`, `NortadaStatus`, `WeatherReading`) are stubs with no members yet —
  check `docs/user-stories/` (phase 1a/1b) for what they're expected to become before extending them.
- `docs/OOA/nortada-OOA.puml` and `docs/OOD/nortada-OOD.puml` hold the PlantUML analysis/design diagrams behind
  the intended architecture; consult these before introducing new domain types or relationships.
- `docs/architecture.md` defines the target Clean Architecture layering (domain / application / infrastructure /
  web), the SOLID/GRASP/GoF conventions, and the rule that domain objects, ORM entities, and DTOs stay separate
  types mapped at layer boundaries — consult it before adding new classes or packages.

## CI (`.github/workflows/ci-pipeline.yml`)

Runs on pull requests with four jobs; only secret scanning is implemented so far, the rest are placeholders:
- `secret-scan` — Gitleaks, configured via `.gitleaks.toml` (allowlists test/spec files and common placeholder
  patterns like `EXAMPLE_*`, `YOUR_*_HERE`)
- `sast-semgrep` — placeholder ("coming in Phase 1")
- `build-and-test-with-coverage` — placeholder ("coming in Phase 1")
- `sca` — placeholder ("coming in Phase 2")

All files require review from `@vitorhsbarros` per `.github/CODEOWNERS`.
