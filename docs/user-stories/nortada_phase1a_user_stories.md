# Nortada App — Phase 1a User Stories (Backend + Tests)

## US005 — Object-Oriented Analysis (OOA)
*As a developer, I want to perform an object-oriented analysis of the system, so that I can identify the real-world entities, responsibilities and relationships before writing any code.*

**Acceptance Criteria:**
- Use case diagram covering Phase 1 actors and interactions
- Identification of candidate classes from the domain (Beach, WindReading, NortadaStatus, etc.)
- Class responsibilities defined (what each class knows and does)
- Relationships between classes identified (association, aggregation, composition)
- Domain model diagram produced

---

## US006 — Object-Oriented Design (OOD)
*As a developer, I want to produce an object-oriented design from the analysis, so that I have a concrete blueprint to implement against.*

**Acceptance Criteria:**
- Class diagram with attributes, methods and visibility
- Sequence diagrams for key interactions (e.g. fetch wind data → detect Nortada → return status)
- Package structure defined for Spring Boot backend (controller, service, repository, domain)
- API contract defined (endpoints, request/response structure) following REST Level 3 HATEOAS (HAL+JSON)
- Each resource response includes a `_links` section with relevant hypermedia links
- Design decisions documented (e.g. why scheduled job vs on-demand fetch)

---

## US007 — Backend Project Scaffolding
*As a developer, I want to scaffold the Spring Boot project, so that the backend codebase has a clean, consistent structure to build on.*

**Acceptance Criteria:**
- Spring Boot project initialised with correct dependencies (Web, JPA, PostgreSQL, Flyway, Security, HATEOAS)
- Package structure matches the design defined in US006
- Project committed to the repository under `/backend` directory
- Application starts without errors locally

---

## US008 — Beach Data Seeding
*As a developer, I want a curated list of Portuguese beaches seeded into the database, so that the app has real data to work with from day one.*

**Acceptance Criteria:**
- At least 20 Portuguese coastal beaches included
- Each beach has a name, latitude, longitude and region
- Data is loaded via a Flyway migration script
- Beaches are distributed across the main coastal regions (Norte, Centro, Lisboa, Alentejo, Algarve)

---

## US009 — Wind Data Integration (Open-Meteo)
*As a developer, I want the backend to fetch wind data from Open-Meteo for each beach, so that we have up-to-date wind speed and direction available.*

**Acceptance Criteria:**
- Scheduled job fetches wind data for all beaches every hour
- Wind speed and direction are stored per beach
- Failed fetches are logged and do not crash the application
- Open-Meteo base URL is configurable via `application.yml`

---

## US010 — Nortada Detection Logic
*As a developer, I want a service that grades how strong the Nortada is for a given beach, so that the app can display an accurate intensity indicator.*

**Acceptance Criteria:**
- Wind direction between 315° and 45° (N to NNW) is the gate for a Nortada; wind outside this sector is always `NONE`
- Within the sector, sustained wind speed grades the reading into one of the five `NortadaStatus` levels:
  - `NONE` — off-sector, or sustained speed below 15 km/h
  - `LIGHT` — 15 to below 25 km/h
  - `MODERATE` — 25 to below 40 km/h
  - `STRONG` — 40 to below 55 km/h
  - `SEVERE` — 55 km/h or above
- Detection logic is encapsulated in a dedicated service class
- Logic is covered by unit tests (JUnit 5), including the boundary values between levels
- Detection runs year-round; no calendar-based exclusion of any months

---

## US011 — Beach List API Endpoint
*As a mobile developer, I want a REST endpoint that returns all beaches with their current Nortada status, so that the app can display the main beach list screen.*

**Acceptance Criteria:**
- `GET /api/beaches` returns a list of beaches with name, region and Nortada status
- Response follows HAL+JSON format with `_links` per beach (self, collection)
- Response is paginated with HATEOAS pagination links (first, prev, next, last)
- Endpoint is covered by integration tests
- Returns correct HTTP status codes (200, 500)

---

## US012 — Beach Detail API Endpoint
*As a mobile developer, I want a REST endpoint that returns the current Nortada status for a specific beach, so that the app can display the beach detail screen.*

**Acceptance Criteria:**
- `GET /api/beaches/{id}` returns beach details and current Nortada status
- Response follows HAL+JSON format with `_links` (self, collection)
- Returns 404 if beach does not exist
- Endpoint is covered by integration tests
- Returns correct HTTP status codes (200, 404, 500)

---

## US013 — SAST Integration (Semgrep)
*As a developer, I want Semgrep running in the CI pipeline on every PR, so that common security vulnerabilities in the codebase are caught early.*

**Acceptance Criteria:**
- Semgrep runs as a job in `.github/workflows/ci.yml`
- Java ruleset is applied to the Spring Boot backend
- Pipeline fails if high severity findings are detected
- Runs on every pull request