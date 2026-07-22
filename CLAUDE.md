# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Nortada is an app to check "Nortada" wind conditions on Portuguese beaches. The backend is a **Spring Boot**
service (Web, JPA, PostgreSQL, Flyway, Security, HATEOAS) scaffolded under `backend/` (Phase 1a per
`docs/user-stories/`; the old plain-Gradle `app/` skeleton has been removed). Package layout follows the
Clean Architecture layering in `docs/architecture.md`, not the flat OOA layout in
`docs/OOA/package-structure.md` (the latter is superseded — see Architecture notes below).

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

## Architecture notes

- Domain classes enforce their own invariants in constructors rather than via a validation framework, e.g.
  `Name` rejects blank/too-short/too-long/special-character/letterless values, `Region`/`Beach` reject null
  constructor args (`backend/src/main/java/com/nortadas/domain/`).
- **Domain is organized by DDD aggregate boundary, one package per aggregate root** (each holding the root
  entity plus its `*Factory`), plus a shared value-object package — not the flat
  `controller`/`service`/`repository`/`dto` layout in `docs/OOA/package-structure.md`, which
  `docs/architecture.md` explicitly supersedes:
  - `domain.beach` — `Beach` (aggregate root) + `BeachFactory`
  - `domain.municipality` — `Municipality` (aggregate root) + `MunicipalityFactory`
  - `domain.region` — `Region` (aggregate root) + `RegionFactory`
  - `domain.favourite` — `FavouriteBeaches` (its own aggregate: a beach-reference collection pending `User`
    linkage, not nested under `beach`)
  - `domain.valueobject` — every value object, including ones that read as "beach concepts" but have no
    identity and value-based equality: `BeachId`, `MunicipalityId`, `RegionId`, `Latitude`, `Longitude`,
    `WindSpeed`, `WindDirection`, `Name`, `WeatherReading`, `NortadaStatus`
  - `domain.service` — the domain's non-aggregate behavioral package: `NortadaDetectionService` (the entry
    point), `NortadaDetectionStrategy` (GoF Strategy seam), `SectorSpeedDetectionStrategy` (default rule,
    US010). Named to mirror `application.usecase` on the other side of the layer boundary — package name
    alone should tell you which "service" (domain vs application) you're looking at, since
    `NortadaDetectionService` is plain Java with no Spring annotation, while `application.usecase` classes
    are `@Service`-annotated Spring beans.
  - Dependency direction is one-way: `valueobject` must never import from an aggregate package
    (`beach`/`municipality`/`region`). If you need a `{@link Beach}`-style Javadoc cross-reference from
    inside `valueobject`, use the fully-qualified name in the tag instead of an import — a real import there
    previously created a package cycle even when only used for Javadoc.
- **Aggregate roots are constructed only through their `*Factory`** (GoF Factory / GRASP Creator;
  `docs/architecture.md` §7). The entity's own constructors are **package-private**, so
  `new Beach(...)`/`new Region(...)`/`new Municipality(...)` will not compile outside the aggregate's own
  package — go through `BeachFactory.create(...)`/`.rehydrate(...)`, `RegionFactory.create(...)`/`.rehydrate(...)`,
  or `MunicipalityFactory.create(...)` (mappers and cross-package tests already do). Same-package unit tests
  still call the constructors directly, on purpose — they're testing the invariant checks the constructor
  enforces.
- **`Beach` belongs to a `Municipality`, which belongs to a `Region`** (`Beach → Municipality → Region`).
  `Beach` no longer references a `Region` directly: `Beach.getRegion()` is derived via
  `municipality.getRegion()`, so region is never stored redundantly on the beach. Municipality is the search
  granularity between beach and the seven broad NUTS-II regions.
- **Domain entities use DDD-correct equality**: every entity's `equals`/`hashCode` (`Beach`, `Municipality`,
  `Region`) is **identity-based** (compares only its id — `BeachId`/`MunicipalityId`/`RegionId`). Each also
  exposes a null-safe, type-safe `sameAs(...)` method that is **attribute-based** (compares every field) —
  two entities can be `equals` (same identity) but not `sameAs` (different state) if only their descriptive
  attributes differ. Value objects keep plain value-based `equals` and get no `sameAs` (they already *are*
  their attributes).
- `RegionId` is a **name-derived natural key** — a short code (1-3 uppercase letters, e.g. `NOR` for "Norte"):
  the first three Unicode letters of the region's name at creation time, accent-stripped and uppercased
  (`RegionId.fromName(Name)`, GRASP Creator). Unlike `BeachId`, this is deterministic (the same name always
  yields the same code) rather than randomly generated — appropriate because regions are a small, fixed,
  curated vocabulary (Portugal's coastal regions), so uniqueness comes from the closed set of names, not from
  the id. It's still a **snapshot** — renaming a region later does not change its id. Rehydrate from storage
  via the validating `RegionId.of(String)`.
- `MunicipalityId` is also a natural key, but of a different kind: unlike `RegionId` (derived from the name)
  or `BeachId` (randomly generated), it is **externally assigned** — Portugal's official INE/DICOFRE
  municipality code (exactly 4 digits, e.g. `0107` for Espinho). It is stored as a `String`, not an int,
  because leading zeros are significant (`0107` ≠ `107`). There is deliberately **no generator**:
  `MunicipalityId.of(String)` validates and rehydrates, and there is *no* `fromName`/`newId` equivalent —
  municipalities are a curated reference set whose codes come from outside this system. Consequently
  `MunicipalityFactory` exposes only `create` (no `rehydrate`), since there is no id-generation case to
  distinguish. Seeded in `V3__add_municipality.sql`.
- **JPA data models are named `*DataModel`, not `*Entity`** (`infrastructure/persistence/datamodel/`, e.g.
  `BeachDataModel`, `RegionDataModel`, `MunicipalityDataModel`) — deliberately not "Entity", to avoid
  clashing with the DDD sense of *entity* used for domain roots like `Beach`. They may use Lombok (`@Getter`)
  and are translated to/from domain objects by the `*Mapper` classes; ORM annotations
  (`@Entity`/`@Table`/`@Column`) never appear on a domain class.
- Double-backed value objects (`Latitude`, `Longitude`, `WindSpeed`, `WindDirection`) normalize `-0.0` to
  `0.0` in their constructors so `equals`/`hashCode` stay consistent for zero (`Double.compare` would
  otherwise treat them as different).
- `backend/build.gradle` wires a JaCoCo coverage gate into `check`: every `com.nortadas.domain*` package must
  hit **≥95% line and branch coverage** (currently 100% across all five domain packages — `beach`,
  `municipality`, `region`, `favourite`, `valueobject`; add a new aggregate package to the gate's `includes`
  list when you create one). Bootstrap/config classes (`NortadasApplication`, `config/**`) are excluded from
  the gate.
- `docs/OOA/nortada-OOA.puml` (analysis) and `docs/OOD/nortada-OOD.puml` (design, with operations/visibility
  and the full application/infrastructure/web class set) hold the PlantUML diagrams behind the architecture;
  `docs/OOD/sequences/` has the key interaction flows; `docs/OOD/api-contract.md` defines the REST Level 3
  HATEOAS/HAL+JSON contract for the beach endpoints; `docs/OOD/design-decisions.md` records the ADRs (e.g.
  scheduled vs on-demand fetch). Consult these before introducing new domain types, endpoints, or
  relationships.
- `docs/architecture.md` defines the target Clean Architecture layering (domain / application / infrastructure /
  web), the SOLID/GRASP/GoF conventions, and the rule that domain objects, ORM data models (`*DataModel`), and
  DTOs stay separate types mapped at layer boundaries — consult it before adding new classes or packages.

## CI (`.github/workflows/ci-pipeline.yml`)

Runs on pull requests with four jobs; only secret scanning is implemented so far, the rest are placeholders:
- `secret-scan` — Gitleaks, configured via `.gitleaks.toml` (allowlists test/spec files and common placeholder
  patterns like `EXAMPLE_*`, `YOUR_*_HERE`)
- `sast-semgrep` — placeholder ("coming in Phase 1")
- `build-and-test-with-coverage` — placeholder ("coming in Phase 1")
- `sca` — placeholder ("coming in Phase 2")

All files require review from `@vitorhsbarros` per `.github/CODEOWNERS`.
