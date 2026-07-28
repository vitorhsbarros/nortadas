# Nortada — Architecture

This document defines the target architecture for the Spring Boot backend. It complements
`docs/OOA/` (analysis) and `docs/OOD/` (design) with the concrete rules the codebase must follow
as it grows: **Clean Architecture** layering, **SOLID**, **GRASP**, and **GoF** patterns where
they fit naturally.

## 1. Layers (Clean Architecture)

Dependencies only point inward. Outer layers depend on inner layers through interfaces
("ports"); inner layers never depend on outer ones or on frameworks.

| Layer | Contains | Depends on |
|---|---|---|
| **Domain** | Entities, value objects, domain services (`Beach`, `Municipality`, `Region`, `WeatherReading`, `NortadaStatus`, `NortadaDetectionService`) | nothing — pure Java, no framework of any kind (see §3.1) |
| **Application** | Use cases that orchestrate domain objects; outbound **ports** (interfaces) the use cases need, e.g. `BeachRepositoryPort`, `WeatherClientPort` | Domain only |
| **Interface Adapters** | Inbound: Spring `@RestController`s, DTOs, mappers. Outbound: JPA repository adapters, Open-Meteo HTTP client adapter, data model mappers | Application (implements its ports) |
| **Frameworks & Drivers** | Spring Boot, Spring Data JPA/Hibernate, PostgreSQL, the scheduler trigger, `application.yml` | Everything (wires it together) |

## 2. Package structure

The `domain` layer is split **one package per DDD aggregate root** (each holding the root plus its
`*Factory`), rather than by technical role — see §2.1 below for the per-package rationale.

```
com.nortadas
├── domain
│   ├── beach/               (Beach + BeachFactory)
│   ├── municipality/        (Municipality + MunicipalityFactory)
│   ├── region/              (Region + RegionFactory)
│   ├── weatherreading/      (WeatherReading + WeatherReadingFactory)
│   ├── favourite/           (FavouriteBeaches)
│   ├── service/             (domain services: NortadaDetectionService,
│   │                         NortadaDetectionStrategy, SectorSpeedDetectionStrategy)
│   └── valueobject/         (BeachId, MunicipalityId, RegionId, WeatherReadingId, Name,
│                             Latitude, Longitude, WindSpeed, WindDirection, WeatherCode,
│                             WeatherCondition, NortadaStatus)
├── application
│   ├── usecase/             (GetBeachListUseCase, GetBeachDetailUseCase, FetchWeatherUseCase,
│   │                         PurgeOldWeatherReadingsUseCase; plus the layer's own result types
│   │                         BeachStatusView / PageResult and BeachNotFoundException)
│   └── port/                (BeachRepositoryPort, WeatherClientPort,
│                             WeatherReadingRepositoryPort — interfaces only)
├── infrastructure
│   ├── persistence
│   │   ├── datamodel/       (JPA @Entity data models: BeachDataModel, RegionDataModel,
│   │   │                     MunicipalityDataModel, WeatherReadingDataModel)
│   │   ├── mapper/          (domain <-> data model mappers)
│   │   └── repository/      (Spring Data JPA repos + adapter implementing *Port)
│   └── weather
│       └── OpenMeteoClientAdapter   (implements WeatherClientPort)
├── web
│   ├── controller/          (BeachController)
│   ├── dto/                 (BeachResponse, WeatherReadingResponse — HAL+JSON)
│   ├── mapper/              (BeachDtoMapper: application view -> DTO)
│   └── error/               (ApiExceptionHandler + web exceptions -> RFC-7807 ProblemDetail)
├── scheduler
│   ├── WeatherDataScheduler       (hourly fetch trigger)
│   └── WeatherRetentionScheduler  (daily retention purge trigger)
└── config
    └── ClockConfig, DetectionConfig, OpenMeteoHttpClientConfig,
        SchedulingConfig, SecurityConfig   (wiring only — one concern per class,
        e.g. DetectionConfig picks the NortadaDetectionStrategy implementation)
```

This refines the flat layout in `docs/OOA/package-structure.md` — `controller`/`dto` become
`web`, `repository` splits into `infrastructure/persistence` (JPA) + `application/port`
(interface), and `application`/`infrastructure` are new to make the dependency direction explicit.

**Wiring lives in `config`, one class per concern** — there is no single `AppConfig`. Because the
domain is framework-free (§3.1), anything domain-side that must become a Spring bean is constructed
there: `DetectionConfig` is what chooses `SectorSpeedDetectionStrategy` as the default detection rule,
so swapping the rule never touches a caller (OCP).

### 2.1 Per-package rationale

- `domain.beach` — `Beach` (aggregate root) + `BeachFactory`.
- `domain.municipality` — `Municipality` (aggregate root) + `MunicipalityFactory`.
- `domain.region` — `Region` (aggregate root) + `RegionFactory`.
- `domain.favourite` — `FavouriteBeaches` (its own aggregate: a beach-reference collection pending
  `User` linkage, not nested under `beach`).
- `domain.weatherreading` — `WeatherReading` (aggregate root) + `WeatherReadingFactory`. It is an
  aggregate, **not** a value object: it has its own `WeatherReadingId` identity and identity-based
  equality, and is a per-beach time series that grows without bound, so it references its `BeachId`
  rather than being embedded in the `Beach` aggregate.
- `domain.valueobject` — every value object, including ones that read as "beach concepts" but have no
  identity and value-based equality: `BeachId`, `MunicipalityId`, `RegionId`, `WeatherReadingId`,
  `Latitude`, `Longitude`, `WindSpeed`, `WindDirection`, `Name`, `WeatherCode`, `WeatherCondition`,
  `NortadaStatus`. Double-backed value objects (`Latitude`, `Longitude`, `WindSpeed`, `WindDirection`)
  normalize `-0.0` to `0.0` in their constructors so `equals`/`hashCode` stay consistent for zero
  (`Double.compare` would otherwise treat them as different).
- `domain.service` — the domain's non-aggregate behavioral package: `NortadaDetectionService` (the entry
  point), `NortadaDetectionStrategy` (GoF Strategy seam), `SectorSpeedDetectionStrategy` (default rule,
  US010). Named to mirror `application.usecase` on the other side of the layer boundary — package name
  alone should tell you which "service" (domain vs application) you're looking at, since
  `NortadaDetectionService` is plain Java with no Spring annotation, while `application.usecase` classes
  are `@Service`-annotated Spring beans.

**Dependency direction is one-way**: `valueobject` must never import from an aggregate package
(`beach`/`municipality`/`region`). If you need a `{@link Beach}`-style Javadoc cross-reference from
inside `valueobject`, use the fully-qualified name in the tag instead of an import — a real import there
previously created a package cycle even when only used for Javadoc.

**Identity strategy differs by aggregate.** `RegionId` is a **name-derived natural key** — a short code
(1-3 uppercase letters, e.g. `NOR` for "Norte"): the first three Unicode letters of the region's name at
creation time, accent-stripped and uppercased (`RegionId.fromName(Name)`, GRASP Creator). Unlike
`BeachId`, this is deterministic (the same name always yields the same code) rather than randomly
generated — appropriate because regions are a small, fixed, curated vocabulary (Portugal's coastal
regions), so uniqueness comes from the closed set of names, not from the id. It's still a **snapshot**
— renaming a region later does not change its id. Rehydrate from storage via the validating
`RegionId.of(String)`.

`MunicipalityId` is also a natural key, but of a different kind: unlike `RegionId` (derived from the
name) or `BeachId` (randomly generated), it is **externally assigned** — Portugal's official INE/DICOFRE
municipality code (exactly 4 digits, e.g. `0107` for Espinho). It is stored as a `String`, not an int,
because leading zeros are significant (`0107` ≠ `107`). There is deliberately **no generator**:
`MunicipalityId.of(String)` validates and rehydrates, and there is *no* `fromName`/`newId` equivalent —
municipalities are a curated reference set whose codes come from outside this system. Consequently
`MunicipalityFactory` exposes only `create` (no `rehydrate`), since there is no id-generation case to
distinguish. Seeded in `V3__add_municipality.sql`.

**Stored raw values, derived categories** — a `WeatherReading` stores only raw observations; every
interpretation of them is computed on demand and **never persisted**, so the rules can change without a
data migration and no stored row can go stale against them:
- `NortadaStatus` is derived from a whole reading by `NortadaDetectionService` (US010, a domain service,
  since the rule spans wind speed *and* direction).
- `WeatherCondition` is derived from the single stored `WeatherCode` by `WeatherCondition.fromWmoCode(...)`
  (Information Expert — no service needed for a one-field mapping).

`WeatherCode` is the raw WMO `ww` code Open-Meteo returns. It validates the **full spec range 0–99**,
while `fromWmoCode` only recognises the ~27 codes Open-Meteo actually emits (0–3, 45/48, 51–57, 61–67,
71–77, 80–86, 95–99) and maps everything else — the 4–44 gap included — to `UNKNOWN`. That gap is
Open-Meteo's emitted-code table, not an oversight: a forecast model can't produce observer-only codes
(haze, smoke, duststorm). `UNKNOWN` is the deliberate safety net so a newly-added provider code degrades
gracefully instead of throwing mid-fetch.

## 3. Three models, never mixed

A recurring Clean Architecture mistake is letting one class serve two layers. This project keeps
three distinct model types, mapped explicitly at the boundaries:

- **Domain object** (`domain/`) — pure business object, enforces its own invariants (as `Name`,
  `Region` already do). Pure Java only — see §3.1. Construction is factorized behind a dedicated
  `*Factory` per aggregate root (`BeachFactory`, `MunicipalityFactory`, `RegionFactory`) rather than
  exposed constructors — see §7. `Beach` belongs to a `Municipality`, which in turn belongs to a
  `Region` (`Beach -> Municipality -> Region`), so beaches can be filtered at a finer granularity
  than the seven NUTS-II regions as the catalogue grows.
- **Data model** (`infrastructure/persistence/datamodel/`) — `@Entity` classes shaped for
  Hibernate/JPA, named `*DataModel` (e.g. `BeachDataModel`) rather than `*Entity` to avoid clashing
  with the DDD sense of "entity" used for domain objects like `Beach`/`Region`. Can be denormalized
  or structured differently from the domain object.
- **DTO** (`web/dto/`) — wire format returned to clients, shaped for the API contract (HAL+JSON,
  `_links`, pagination), independent of both the domain and the data model.

### 3.1 The domain layer is pure Java — no frameworks, including Lombok

The `domain/` layer (business rules) must have **zero framework dependencies of any kind**. No
Spring, no JPA/Hibernate, no Jackson, no HTTP client — and **no Lombok**. Domain classes hand-write
their constructors (where they already enforce invariants), getters, `equals`/`hashCode`, and
`toString` in plain Java rather than generating them with `@Getter`/`@Value`/`@Data` etc.

The point is that the business rules stay independent of every tool the project happens to use
today: a domain class should read the same, and compile, if Spring and Lombok were removed from the
build tomorrow. Lombok is a compile-time annotation processor — convenient, but still an external
dependency the domain would be coupled to, so it's excluded here on the same principle as the rest.

**This ban is scoped to `domain/` only.** Every other layer may use frameworks freely — Lombok in
`infrastructure/persistence/datamodel` JPA data models, `web/dto` DTOs, adapters, and config is fine
and encouraged where it cuts boilerplate. Purity is a property we buy for the business rules
specifically, not a project-wide style rule.

Concretely, outside `domain/` **let Lombok generate the constructors** rather than hand-writing them:

- `web/dto` DTOs — `@Getter` + `@AllArgsConstructor`.
- `infrastructure/persistence/datamodel` data models — `@Getter` + `@AllArgsConstructor` +
  `@NoArgsConstructor(access = AccessLevel.PROTECTED)`, the no-arg one being what Hibernate requires;
  `protected` keeps it out of application use, as the previous hand-written version did.
- Hand-write a constructor there only when Lombok *can't* express it — a **subset-of-fields overload**,
  such as `BeachResponse`'s reading-less constructor, which delegates to the generated one.

Because a Lombok constructor is positional in **field-declaration order**, reordering fields silently
reorders the constructor's parameters; keep field order stable when editing these classes.

Mapping between them is an explicit, testable step (`mapper` classes) — never shared inheritance,
never a "smart" object doing double duty.

### 3.2 Entity equality: identity vs `sameAs`

Domain entities use **DDD-correct equality**: every entity's `equals`/`hashCode` (`Beach`,
`Municipality`, `Region`) is **identity-based** (compares only its id —
`BeachId`/`MunicipalityId`/`RegionId`). Each also exposes a null-safe, type-safe `sameAs(...)` method
that is **attribute-based** (compares every field) — two entities can be `equals` (same identity) but
not `sameAs` (different state) if only their descriptive attributes differ. Value objects keep plain
value-based `equals` and get no `sameAs` (they already *are* their attributes).

## 4. Example flow — beach list request

```
BeachController  (@RequestParam page/size — no request DTO; GET carries no body)
  → application/usecase.GetBeachListUseCase
      → application/port.BeachRepositoryPort (interface)
          → infrastructure/persistence adapter → Spring Data JPA → BeachDataModel
          → persistence mapper → domain.Beach
      → application/port.WeatherReadingRepositoryPort (latest reading per beach)
      → domain.service.NortadaDetectionService (per beach with a reading)
    ← PageResult<BeachStatusView>   (framework-free; no Spring Data Page/Pageable)
  → web/mapper.BeachDtoMapper → PagedModel<BeachResponse> (HAL+JSON)
→ client
```

### 4.1 Web layer conventions

- **Requests**: `GET` endpoints bind inputs directly to `@RequestParam`/`@PathVariable`; there are no
  request DTOs yet because no endpoint takes a body. Introduce them (with validation annotations) when
  write endpoints arrive.
- **Links**: build every HAL link from a controller method reference —
  `linkTo(methodOn(BeachController.class).detail(id))` — never by concatenating a path string. This keeps
  emitted URIs tied to the real `@GetMapping`, so a route change cannot leave a stale link behind. A
  hand-built list-item `self` link already drifted out of this once and was corrected.
- **Errors**: exceptions become RFC-7807 `ProblemDetail`s in `web/error/ApiExceptionHandler`
  (`@RestControllerAdvice`), each with a stable `type` URI. Use cases throw meaningful exceptions
  (`BeachNotFoundException`); the controller does no status-code branching.
- **Pagination**: the application layer returns its own `PageResult<T>`; Spring Data's `Page`/`Pageable`
  must not cross into it (§1). The web layer converts to `PagedModel` with `first`/`prev`/`next`/`last`
  links so clients navigate by link rather than constructing query strings.

## 5. SOLID

- **SRP** — `NortadaDetectionService` only decides Nortada status; `OpenMeteoClientAdapter` only
  talks to Open-Meteo; controllers only translate HTTP ↔ use case calls.
- **OCP** — new detection rules or a second weather provider are added via new `Strategy`/adapter
  implementations, not by editing existing classes.
- **LSP** — any implementation of a port (`BeachRepositoryPort`, `WeatherClientPort`) must be a
  drop-in substitute; no implementation should require callers to know which one is in use.
- **ISP** — prefer small, role-specific ports over one large repository interface; split
  read/write ports if their usage patterns diverge.
- **DIP** — application and domain depend on port interfaces, never on `infrastructure` directly;
  wiring happens once, in `config`.

## 6. GRASP

- **Information Expert** — objects that hold the data compute their own derived state (e.g. a
  domain object exposing behavior over its own fields) instead of controllers/services reaching
  in and computing it externally.
- **Creator** — an object creates or owns the objects it aggregates (`Region` already generates
  its own identity, §2.1; `Beach` owns its `WeatherReading` history).
- **Controller** — Spring `@RestController`s are GRASP controllers only: no business logic,
  pure delegation to a use case.
- **Low Coupling / High Cohesion** — the port/adapter boundary keeps domain code with zero
  knowledge of Spring, JPA, or HTTP.
- **Polymorphism** — vary behavior by type instead of branching (e.g. detection rules, weather
  provider adapters).
- **Pure Fabrication** — mapper classes are not real-world domain concepts but exist purely to
  keep the three models (§3) separate.
- **Indirection** — ports sit between application and infrastructure so neither depends on the
  other directly.
- **Protected Variations** — volatile things (external API shape, persistence technology) are
  hidden behind an interface so they can change without rippling inward.

## 7. GoF patterns expected in this codebase

- **Strategy** — Nortada detection rule, pluggable so alternate rule sets can be swapped in later.
- **Adapter** — `OpenMeteoClientAdapter` (external API → `WeatherClientPort`); data model/domain
  mappers.
- **Builder/Factory** — a dedicated `*Factory` per aggregate root (`BeachFactory`, `RegionFactory`)
  is the sole public entry point for constructing that aggregate: it exposes named `create`/
  `rehydrate` methods instead of overloaded constructors, and the aggregate's own constructors are
  package-private so callers outside the aggregate's package cannot bypass the factory. Mappers and
  cross-package tests go through the factory; same-package unit tests call the constructors directly,
  on purpose, since they're testing the invariant checks the constructor enforces.
  `MunicipalityFactory` follows the same shape but exposes only `create`, since a municipality's id
  is always an externally-known code (never generated or derived), leaving no separate
  rehydrate-vs-create distinction to make.
- **Repository** — persistence hidden behind a port, implemented by Spring Data JPA.
- **Facade** — use case classes present one coordinating method per client-facing operation, even
  when multiple domain services/ports are involved underneath.

## 8. Persistence (ORM)

- Spring Data JPA + Hibernate is the ORM (per `US007`).
- ORM data models live only in `infrastructure/persistence/datamodel` — the domain layer never
  carries `@Entity`/`@Table`/`@Column` annotations.
- Persistence adapters implement the `application/port` interfaces; nothing outside
  `infrastructure` references Spring Data types directly.

## 9. Testing implications

- **Domain + application**: plain JUnit 5 unit tests, no Spring context (ports and the detection
  service are mocked with Mockito).
- **Infrastructure adapters**: integration tests — `MockRestServiceServer` for the Open-Meteo adapter,
  a Spring context against H2 for persistence.
- **Controllers**: `@SpringBootTest` + `@AutoConfigureMockMvc` + `@ActiveProfiles("test")`, asserting
  DTO shape, HATEOAS `_links` and problem-detail bodies. These run the *full* context against H2 in
  PostgreSQL mode with the real Flyway migrations applied, so the seeded catalogue is the fixture; add
  `@Transactional` to the test class when a method inserts rows, so they roll back and don't leak into
  sibling methods sharing the cached context.
- **Coverage**: the JaCoCo gate (≥95% line *and* branch) only enforces `com.nortadas.domain*` — currently
  100% across all seven domain packages (`beach`, `municipality`, `region`, `favourite`, `weatherreading`,
  `service`, `valueobject`) — and its `includes` list is explicit: a new domain package must be added
  there or it is silently ungated. Bootstrap/config classes (`NortadasApplication`, `config/**`) are
  excluded from the gate. `application`/`web`/`infrastructure` are deliberately outside it entirely:
  cover them by behaviour (use-case unit tests, `MockMvc` integration tests), not by chasing a percentage.

## 10. Related documents

`docs/OOA/nortada-OOA.puml` (analysis) and `docs/OOD/nortada-OOD.puml` (design, with operations/visibility
and the full application/infrastructure/web class set) hold the PlantUML diagrams behind this document;
`docs/OOD/sequences/` has the three interaction flows — `beach-request-flows.puml` (both endpoints,
including the 400/404 problem-detail paths), `scheduler-fetch-weather.puml` (hourly fetch; note detection
deliberately does *not* run there) and `scheduler-retention-purge.puml` (daily purge); `docs/OOD/api-contract.md`
defines the REST Level 3 HATEOAS/HAL+JSON contract for the beach endpoints; `docs/OOD/design-decisions.md`
records the ADRs (e.g. scheduled vs on-demand fetch) plus an *Implementation status* section listing the
deliberate deviations from the original design. Consult these before introducing new domain types,
endpoints, or relationships.

**These diagrams are kept in step with the code — where they disagree, the code wins and the diagram is a
bug worth fixing in the same PR.**
