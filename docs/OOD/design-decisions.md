# Nortada — Design Decisions (OOD)

Lightweight Architecture Decision Records for the Phase 1 object-oriented design. Each
record states the decision, the context, and the rationale. These complement
`docs/architecture.md` (the standing rules) by capturing the *choices* made during OOD and
why. Referenced from US006.

---

## ADR-001 — Scheduled job vs on-demand weather fetch

**Decision.** Weather data is fetched by a **scheduled background job**
(`WeatherDataScheduler`, hourly per US009), not on-demand when a client requests a beach.
Read endpoints (`GET /api/beaches`, `GET /api/beaches/{id}`) serve the most recently
persisted `WeatherReading` and its derived `NortadaStatus`.

**Context.** Open-Meteo is an external dependency with rate limits and latency. Nortada
conditions change on the order of hours, not seconds, and many clients read the same small,
fixed set of Portuguese beaches.

**Rationale.**
- **Decoupled latency & availability** — API responses never block on a third-party call;
  an Open-Meteo outage degrades freshness, not availability (a failed fetch is logged and
  does not crash the app, US009).
- **Bounded external load** — one hourly sweep for all beaches instead of N calls per
  request; naturally cacheable and cheap.
- **Read scalability** — request handling is a pure DB read + in-memory detection, trivially
  scalable and easy to test with `MockMvc`.
- **Fit to the data** — hourly granularity matches how fast Nortada actually changes.

**Trade-off / rejected alternative.** On-demand fetch gives per-request freshness but
couples every read to Open-Meteo's latency and quota, and multiplies external calls. The
staleness window (up to one hour) is acceptable for this domain, so the scheduled approach
wins. The detection rule stays independent of *when* data arrives, so this can be revisited
without touching the domain.

---

## ADR-002 — REST Level 3 (HATEOAS) with HAL+JSON

**Decision.** The API is hypermedia-driven (Richardson Maturity Level 3) using the
`application/hal+json` representation. Every resource includes a `_links` section; the
collection includes pagination links (`first`/`prev`/`next`/`last`) and each item includes
`self`/`collection`.

**Rationale.**
- **Discoverability & decoupling** — the mobile client navigates by following links rather
  than hard-coding URI templates, so URIs can evolve server-side.
- **Standard pagination** — HAL's page metadata + navigation links are a well-understood
  convention that Spring HATEOAS supports directly.
- **Explicit contract** — see `docs/OOD/api-contract.md` for the concrete payloads.

**Trade-off.** HAL adds envelope verbosity versus a flat JSON body; accepted for the
navigation and evolvability benefits, and it is the format the Phase 1 stories (US011/US012)
already require.

---

## ADR-003 — Three-model separation (domain / data model / DTO)

**Decision.** Three distinct types per concept, mapped explicitly at boundaries: the pure
`domain` object (e.g. `Beach`), the JPA `@Entity` data model
(`infrastructure/persistence/datamodel/BeachDataModel`), and the HAL+JSON `web/dto`
(`BeachResponse`). Dedicated mapper classes translate between them.

**Rationale.**
- **Protected Variations / Low Coupling** — ORM shape and wire shape can change without
  touching business rules; the domain has zero framework knowledge (`architecture.md`
  section 3.1).
- **Single Responsibility** — no class serves two layers; mappers (Pure Fabrication) own
  the translation and are independently testable.

**Trade-off.** More classes and explicit mapping code versus reusing one annotated class
everywhere. Accepted — sharing a class across layers is the recurring Clean Architecture
defect this project explicitly avoids.

---

## ADR-004 — Ports & Adapters (Dependency Inversion)

**Decision.** The `application` layer depends only on outbound **ports**
(`BeachRepositoryPort`, `WeatherReadingRepositoryPort`, `WeatherClientPort` — interfaces in
`application/port`). Concrete adapters in `infrastructure` implement them
(`BeachRepositoryAdapter` over Spring Data JPA, `OpenMeteoClientAdapter` over HTTP). Wiring
happens once in `config/AppConfig`.

**Rationale.** Keeps dependencies pointing inward (DIP); lets a second weather provider or a
different persistence technology be added as a new adapter (OCP) without editing use cases;
enables mocking ports in unit tests.

---

## ADR-005 — Nortada detection as a Strategy behind a domain service

**Decision.** Detection logic lives in the `domain` layer as a `NortadaDetectionService`
delegating to a `NortadaDetectionStrategy` (default `SectorSpeedDetectionStrategy`
implementing the US010 sector + graded-speed rule). It takes a `WeatherReading` and returns
a `NortadaStatus`.

**Rationale.**
- **Information Expert / SRP** — the rule is one cohesive responsibility, separate from
  fetching and from HTTP concerns.
- **Open/Closed via Strategy (GoF)** — alternate rule sets swap in without editing callers.
- **Testability** — a pure function `(reading) -> status` is directly unit-testable at
  boundary values (US010) with no Spring context.

---

## ADR-006 — Use cases as Facades

**Decision.** Client-facing operations are single-method application services
(`GetBeachListUseCase`, `GetBeachDetailUseCase`, `FetchWeatherUseCase`) that coordinate
ports and the detection service behind one call.

**Rationale.** Controllers stay thin GRASP Controllers (pure HTTP↔use-case translation); the
Facade hides the multi-collaborator orchestration; each use case has one reason to change.

---

## GoF patterns used (summary)

| Pattern | Where | Purpose |
|---|---|---|
| Strategy | `NortadaDetectionStrategy` | Pluggable detection rules (ADR-005). |
| Adapter | `OpenMeteoClientAdapter`, repository adapters, mappers | Bridge external API / ORM to ports (ADR-004, ADR-003). |
| Repository | `*RepositoryPort` + JPA adapter | Persistence hidden behind a port. |
| Facade | `*UseCase` classes | One coordinating method per operation (ADR-006). |
| Factory | value-object constructors / `BeachId.newId()` | Enforce invariants and generate identity at construction. |

---

## Implementation status

The scaffolding this section once listed as pending (US007) is **done** — every value object,
domain service, use case, port, data model, mapper, adapter, controller and DTO in
`nortada-OOD.puml` now exists, and `domain/` is fully pure Java (no Lombok, no Spring). The
diagram is kept in step with the code; where the two disagree, the code wins and the diagram is
a bug.

Two design-time types were **never built** and are intentionally absent from the diagram:

- `User` and the `Email` value object — no authenticated user story has been implemented yet.
  `FavouriteBeaches` exists as its own aggregate awaiting that linkage.

Deviations from the original design worth knowing, each deliberate:

- **`Municipality` was added** between `Beach` and `Region` (`Beach -> Municipality -> Region`),
  so beaches can be filtered more finely than the seven NUTS-II regions. `Beach.getRegion()` is
  derived through the municipality rather than stored.
- **`BeachRepositoryPort` is unpaged** (`findAll() : List<Beach>`). Pagination is expressed with
  the application layer's own `PageResult<T>` and applied in memory, so no Spring Data
  `Page`/`Pageable` leaks inward (`architecture.md` §1). Revisit with a paged port method if the
  catalogue outgrows it.
- **There is no `BeachListResponse` DTO** — the list endpoint returns Spring HATEOAS's
  `PagedModel<BeachResponse>`.
- **Detection does not run during the fetch.** `FetchWeatherUseCase` stores raw readings only;
  `NortadaStatus` and `WeatherCondition` are derived at read time and never persisted (ADR-005),
  so the rules can change without a data migration.
- **Persistence classes are named for their role**: `*Mapper` (not `*PersistenceMapper`) and
  `Jpa*RepositoryAdapter` (not `*RepositoryAdapter`).
- **Wiring is split across purpose-specific `config` classes** (`DetectionConfig`, `ClockConfig`,
  `OpenMeteoHttpClientConfig`, `SchedulingConfig`, `SecurityConfig`) rather than one `AppConfig`.
