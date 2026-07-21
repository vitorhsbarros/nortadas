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
| **Domain** | Entities, value objects, domain services (`Beach`, `Region`, `WeatherReading`, `NortadaStatus`, `NortadaDetectionService`) | nothing — pure Java, no framework of any kind (see §3.1) |
| **Application** | Use cases that orchestrate domain objects; outbound **ports** (interfaces) the use cases need, e.g. `BeachRepositoryPort`, `WeatherClientPort` | Domain only |
| **Interface Adapters** | Inbound: Spring `@RestController`s, DTOs, mappers. Outbound: JPA repository adapters, Open-Meteo HTTP client adapter, data model mappers | Application (implements its ports) |
| **Frameworks & Drivers** | Spring Boot, Spring Data JPA/Hibernate, PostgreSQL, the scheduler trigger, `application.yml` | Everything (wires it together) |

## 2. Package structure

```
com.nortadas
├── domain
│   ├── Beach, Region, WeatherReading, NortadaStatus, User, FavouriteBeaches
│   └── valueobject/        (BeachId, Name, Latitude, Longitude, Email, ...)
├── application
│   ├── usecase/            (GetBeachListUseCase, GetBeachDetailUseCase, DetectNortadaUseCase)
│   └── port/                (BeachRepositoryPort, WeatherClientPort — interfaces only)
├── infrastructure
│   ├── persistence
│   │   ├── datamodel/       (JPA @Entity data models: BeachDataModel, RegionDataModel, WeatherReadingDataModel)
│   │   ├── mapper/          (domain <-> data model mappers)
│   │   └── repository/      (Spring Data JPA repos + adapter implementing *Port)
│   └── weather
│       └── OpenMeteoClientAdapter   (implements WeatherClientPort)
├── web
│   ├── controller/          (BeachController, RegionController)
│   ├── dto/                 (BeachResponse, BeachListResponse — HAL+JSON)
│   └── mapper/              (domain -> DTO)
├── scheduler
│   └── WeatherDataScheduler  (driving adapter, invokes application use cases)
└── config
    └── AppConfig             (wires adapters to ports)
```

This refines the flat layout in `docs/OOA/package-structure.md` — `controller`/`dto` become
`web`, `repository` splits into `infrastructure/persistence` (JPA) + `application/port`
(interface), and `application`/`infrastructure` are new to make the dependency direction explicit.

## 3. Three models, never mixed

A recurring Clean Architecture mistake is letting one class serve two layers. This project keeps
three distinct model types, mapped explicitly at the boundaries:

- **Domain object** (`domain/`) — pure business object, enforces its own invariants (as `Name`,
  `Region` already do). Pure Java only — see §3.1. Construction is factorized behind a dedicated
  `*Factory` per aggregate root (`BeachFactory`, `RegionFactory`) rather than exposed constructors —
  see §7.
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

Mapping between them is an explicit, testable step (`mapper` classes) — never shared inheritance,
never a "smart" object doing double duty.

## 4. Example flow — beach list request

```
BeachController
  → web/dto request
  → application/usecase.GetBeachListUseCase
      → application/port.BeachRepositoryPort (interface)
          → infrastructure/persistence adapter → Spring Data JPA → BeachDataModel
          → persistence mapper → domain.Beach
      → domain.NortadaDetectionService (per beach)
  → web/mapper → web/dto.BeachResponse (HAL+JSON)
→ client
```

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
  its own identity per `CLAUDE.md`; `Beach` owns its `WeatherReading` history).
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
  package-private so callers outside the aggregate's package cannot bypass the factory.
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

- **Domain + application**: plain JUnit 5 unit tests, no Spring context.
- **Infrastructure adapters**: integration tests (`@DataJpaTest` for persistence, a mock HTTP
  server for the Open-Meteo adapter).
- **Controllers**: `MockMvc`/`WebTestClient` tests asserting DTO shape and HATEOAS `_links`.
