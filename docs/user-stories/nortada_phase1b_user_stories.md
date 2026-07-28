# Nortada App — Phase 1b User Stories (Mobile Frontend)

Phase 1b delivers the mobile client for the Phase 1a backend, plus the small set of backend
changes that client needs. Four product decisions shape every story below:

- **Audience: beachgoers and families.** Nortada is framed as a condition to avoid, not an
  opportunity. The status colour ramp runs green (calm, good beach day) → red (very windy),
  and copy is written for someone deciding whether to pack the car.
- **Local-first, no accounts.** The app has a name and a set of favourite beaches, and both
  live on the device. There is no `User` concept in Phase 1 — `FavouriteBeaches` exists in the
  domain precisely so it can gain that linkage later (`docs/OOA/class-responsibilities.md`).
  Device-local storage is the honest Phase 1 expression of that design, not a workaround: it
  removes signup friction, keeps personal data off the server entirely, and adds no auth
  surface. The trade-off — state does not survive uninstall or a device change — is acceptable
  for a name and three beaches, and US027 requires the stored shape to be versioned and keyed
  by server beach UUID so a future account migration is an upload, not a re-mapping.
- **Full hypermedia client.** The backend is REST Level 3 (HAL+JSON); the app knows exactly
  one URL — the API entry point — and discovers every other URL from `_links`. This is why
  US017 exists.
- **Five Nortada levels, year-round.** `NONE`, `LIGHT`, `MODERATE`, `STRONG`, `SEVERE`, per
  US010 as reconciled in issue #28. There is no "active/inactive" and no "out of season"
  concept anywhere in the UI.

**Voice: a mascot addresses the user directly.** Copy is written in the mascot's first person
and uses the user's name, which is why the name is a required field in onboarding (US028) and
non-nullable in the profile store (US027). Mascot artwork and a written voice/tone guide are
**not covered by the stories below** and need their own story before the copy in US028 and
US030 can be finalised.

Language: the app ships in English for now, but all user-facing strings are centralised
(US020) so a Portuguese translation can be added later without touching components.

**Navigation shape.** First launch runs onboarding (US028), after which the app opens on the
Home dashboard of the user's favourite beaches (US030). Home, Explore (the searchable beach
list) and Map are the three top-level destinations; beach detail and the info legend are pushed
or presented on top of them (US029).

---

## Backend enablers

These three stories exist only to unblock the mobile client. They are backend work and belong
in the `backend/` module, following `docs/architecture.md`.

### US017 — API Entry-Point Resource
*As a mobile developer, I want a single HAL entry point for the API, so that the app can discover every other endpoint from hypermedia instead of hard-coding URL paths.*

**Acceptance Criteria:**
- `GET /api` returns `200 OK` with media type `application/hal+json`
- Response carries a `_links` section with at least `self` and `beaches`
- The `beaches` link is a URI Template (RFC 6570) exposing the `page` and `size` parameters,
  extended with `q` and `region` once US019 lands
- A client that knows only `/api` can reach the beach list and, from there, any beach detail
  without constructing a URL by hand
- Covered by a `MockMvc` integration test asserting the link relations and templated href
- `docs/OOD/api-contract.md` gains a section documenting the entry point, and the link-relation
  table is updated

---

### US018 — Expose Beach Coordinates in the API
*As a mobile developer, I want each beach's latitude and longitude in the API response, so that the app can plot beaches on a map.*

**Acceptance Criteria:**
- `latitude` and `longitude` appear on `BeachResponse`, on both the list and detail endpoints
- Values are decimal degrees (WGS84), matching the domain `Latitude`/`Longitude` value objects
- No domain or persistence change is required — the coordinates already exist on the `Beach`
  aggregate; this is a DTO and mapper change only
- `docs/OOD/api-contract.md` example payloads updated for both endpoints
- Covered by integration tests asserting the fields on list items and on the detail resource

---

### US019 — Beach Search and Region Filtering
*As a mobile developer, I want to search beaches by name and filter them by region, so that the app can help users find their beach in a growing catalogue.*

**Acceptance Criteria:**
- `GET /api/beaches?q=` matches beach names by case-insensitive **and accent-insensitive**
  substring (searching `povoa` finds `Póvoa de Varzim`)
- `GET /api/beaches?region=` filters to an exact region name; an unknown region returns an
  empty page with `200 OK`, not `404`
- `q` and `region` combine, and combine with `page`/`size`; `page.totalElements` reflects the
  **filtered** result set
- Pagination `_links` (`first`/`prev`/`next`/`last`) preserve the active `q` and `region`
- `GET /api/regions` returns the region catalogue as a HAL collection, so the filter UI does
  not have to infer regions from a paged beach list
- Filtering is applied in the application layer over `BeachRepositoryPort.findAll()`,
  consistent with the existing in-memory paging rationale in `GetBeachListUseCase` — the port
  stays free of Spring Data types
- Covered by integration tests: accent-insensitive match, combined filters, filter-preserving
  pagination links, unknown region, empty results
- `docs/OOD/api-contract.md` query-parameter table updated for both new parameters

---

## Mobile foundation

### US014 — Mobile Project Scaffolding
*As a developer, I want to scaffold the React Native project, so that the mobile codebase has a clean, consistent structure to build on.*

**Acceptance Criteria:**
- React Native project initialised with **Expo** (managed workflow — no prebuild/eject) and
  **TypeScript in strict mode**
- Committed under a `/mobile` directory at the repository root
- File-based navigation via `expo-router`
- Layered folder structure, documented in `mobile/README.md`: `app/` (routes only), `src/api`
  (HAL client and wire types), `src/features/<feature>` (screens, hooks, components),
  `src/ui` (design system), `src/lib` (utilities)
- ESLint and Prettier configured, with a passing `lint` script
- Jest and React Native Testing Library configured, with a passing smoke test
- API base URL read from `EXPO_PUBLIC_API_URL` with a documented local default; the README
  records that the Android emulator reaches the host at `10.0.2.2:8081` while iOS uses
  `localhost:8081`
- Application starts without errors on both an iOS simulator and an Android emulator

---

### US020 — Mobile Design System and Theme
*As a user, I want a consistent, readable visual language across the app, so that I can tell at a glance how windy a beach is.*

**Acceptance Criteria:**
- A single status-presentation module maps each of the five `NortadaStatus` values to a label,
  a colour and an icon, using the beachgoer ramp: `NONE` green (calm) → `LIGHT` → `MODERATE`
  amber → `STRONG` → `SEVERE` red (very windy)
- Colour is **never the only signal** — every status indicator also carries its text label, so
  the app is usable with colour-vision deficiency
- Status colours meet WCAG AA contrast against their background in both themes
- Light and dark themes are both supported and follow the OS setting
- Shared typography scale and spacing tokens; screens consume tokens, not raw values
- All user-facing strings live in one module, keyed, so a Portuguese translation can be added
  later without changing components
- A shared weather-condition module maps the `weatherCondition` values (`CLEAR`, `CLOUDY`,
  `FOG`, `DRIZZLE`, `RAIN`, `SNOW`, `THUNDERSTORM`, `UNKNOWN`) to icon and label
- The status and weather mapping functions are unit tested, including every enum value

---

### US021 — HAL Hypermedia API Client
*As a developer, I want a typed hypermedia client for the backend, so that screens fetch data by following links rather than by building URLs.*

**Acceptance Criteria:**
- The client is configured with exactly one URL — the API entry point (US017) — and reaches
  every other resource by following `_links`
- URI Templates in link hrefs are expanded per RFC 6570
- HAL wire types are declared separately from the view models the screens consume, mirroring
  the backend's three-model separation (`docs/architecture.md` §3)
- Server state is cached, deduplicated and refetched through TanStack Query
- RFC 9457 problem-detail error bodies are parsed into typed errors, so a `404` on beach detail
  surfaces as "beach not found" rather than a generic failure
- Request timeouts and offline/network failures produce a typed error, never an unhandled
  rejection
- Unit tested against a mocked transport, covering: link following, template expansion, a
  problem-detail `404`, a `500`, and a network timeout

---

### US022 — Mobile CI Job
*As a developer, I want the mobile app linted, type-checked and tested in CI, so that broken frontend code cannot be merged.*

**Acceptance Criteria:**
- A job in `.github/workflows/ci-pipeline.yml` runs lint, TypeScript type-check and Jest tests
  for `/mobile`
- The job runs on pull requests and fails the pipeline on any lint, type or test failure
- npm dependencies are cached between runs
- The job does not run the backend Gradle build, and the backend jobs do not run the mobile
  checks

---

## Screens

### US015 — Beach List Screen (Explore)
*As a user, I want to browse all Portuguese beaches with a clear Nortada indicator, so that I can find somewhere calm beyond my usual spots.*

This is the **Explore** tab, not the app's landing screen — Home (US030) is what opens after
onboarding.

**Acceptance Criteria:**
- Each row shows beach name, region, the Nortada status as a colour-coded chip with its text
  label, and the current weather-condition icon
- Each row carries a favourite toggle (US024)
- Status uses the five-level ramp from US020 — there is no "active/inactive" or "out of season"
  state
- The list pages by following the `next` link from the response, never by constructing
  `?page=N` by hand
- Infinite scroll appends the next page as the user reaches the end of the list
- Pull-to-refresh re-fetches the first page
- Loading (skeleton rows), error (message plus retry) and empty-catalogue states are all handled
- Tapping a row navigates to that beach's detail screen using the item's `self` link
- Covered by component tests for the loading, error, empty and populated states

---

### US016 — Beach Detail Screen
*As a user, I want to tap a beach and see its conditions in detail, so that I can decide whether to go before I get in the car.*

**Acceptance Criteria:**
- Shows beach name, region, and the Nortada status as a prominent hero with label and colour
- Shows wind speed in km/h and wind direction, rendered as a compass arrow rotated to the
  reported bearing plus a cardinal label (e.g. "NNW · 340°")
- Shows air temperature and **water temperature** in Celsius
- Shows the current weather condition with icon and label
- Shows reading freshness as relative time from `fetchedAt` ("updated 14 minutes ago"), with a
  visible staleness warning when the reading is more than 2 hours old — the fetch scheduler
  runs hourly, so a larger gap means something is wrong upstream
- Handles a beach with **no reading yet**, where `reading` and `weatherCondition` are absent
  from the response, with an explicit "no data yet" state rather than blank fields
- Loading and error states are handled, including a `404` problem detail rendered as
  "beach not found"
- Back navigation returns to the list with its scroll position and loaded pages intact
- Covered by component tests for the populated, no-reading, stale-reading, loading, error and
  404 states

---

### US023 — Beach Search and Region Filter
*As a user, I want to search for a beach by name and filter by region, so that I can find my beach without scrolling the whole list.*

**Acceptance Criteria:**
- A search field on the list screen filters beaches by name, debounced, sending `q` to the API
- Region filter options are populated from `GET /api/regions`, not hard-coded in the app
- Search text and region filter combine, and both survive paging through results
- A clear affordance resets all active filters
- "No beaches match your search" is a distinct state from an empty catalogue
- Filter and search state is driven through the hypermedia client's templated `beaches` link
- Covered by component tests for filtering, combined filters, clearing, and the no-match state

---

### US024 — Favourite Beaches
*As a user, I want to mark the beaches I care about as favourites, so that the app can show me my usual spots first.*

Favourites are foundational here, not an add-on: onboarding (US028) writes them and Home
(US030) reads them, so this story lands before either.

**Acceptance Criteria:**
- A favourite toggle is available on Explore rows (US015) and on the beach detail screen (US016)
- Favourites persist on the device across app restarts (AsyncStorage) — **device-local only**;
  server-side favourites need the `User` concept that `FavouriteBeaches` is explicitly waiting
  on (`docs/OOA/class-responsibilities.md` scope note) and stay out of Phase 1
- Favourites are stored as **server beach UUIDs**, never as list indices or names, so the set
  stays valid across catalogue changes and can be uploaded as-is when accounts arrive
- Adding a favourite beyond the third is allowed but warns that Home shows at most three,
  and lets the user choose which to drop
- A favourite whose beach no longer exists (`404`) is dropped from storage without breaking the
  screen that discovered it
- Toggling is optimistic and reflected immediately across every screen showing that beach
- An empty favourites state explains how to add one
- The store is unit tested: add, remove, restart persistence, the over-three case, and the
  stale-favourite case

---

### US025 — Beach Map Screen
*As a user, I want to see the beaches on a map coloured by conditions, so that I can find a calmer beach near the one I had in mind.*

**Acceptance Criteria:**
- A map screen renders one marker per beach, coloured by its Nortada status using the US020 ramp
- Markers use the coordinates added in US018
- The whole catalogue is loaded for the map rather than paged, which is acceptable at the
  current catalogue size (tens of beaches); the story is revisited if the catalogue grows
- Tapping a marker shows the beach name and status, and offers navigation to its detail screen
- The screen does **not** request location permission — there is no user-location feature at
  this stage and the app should not trigger a permission prompt it does not need
- Map provider setup is documented in `mobile/README.md`, including the Android API key
  requirement
- Loading and error states are handled

---

### US026 — Nortada Info and Level Legend
*As a user, I want to understand what Nortada means and what the levels are, so that the status on the list actually tells me something.*

**Acceptance Criteria:**
- Explains in plain language what the Nortada is and why it matters for a day at the beach
- Lists all five levels with their colour, label and wind-speed thresholds (`NONE` below
  15 km/h, `LIGHT` 15–25, `MODERATE` 25–40, `STRONG` 40–55, `SEVERE` 55+ km/h)
- States the direction gate: wind must come from between 315° and 45° to count as Nortada at all
- Reachable from both the beach list and the beach detail screen
- Presented as a modal or bottom sheet rather than a full navigation destination
- Thresholds are sourced from the same constants as the rest of the app, so the legend cannot
  drift from US010's detection logic

---

## Onboarding, profile and home

### US027 — Local Profile Store
*As a developer, I want a versioned device-local profile store, so that the user's name and favourites survive restarts and can be migrated to a real account later.*

**Acceptance Criteria:**
- A single persisted profile document holds the user's display name and their favourite beach
  UUIDs, with an explicit `schemaVersion` field
- A read of a document written by an older `schemaVersion` is migrated forward, not discarded;
  an unreadable or corrupt document degrades to an empty profile rather than crashing the app
- Persisted with AsyncStorage. This is the correct choice because the profile holds preferences,
  not secrets — the rule for the codebase is that credentials or tokens, if any ever exist, go
  to `expo-secure-store` instead, and this distinction is written down in `mobile/README.md`
- The display name is **required** — the mascot addresses the user directly by name throughout
  the app, so downstream screens can rely on it always being present and never have to render a
  nameless fallback
- Name validation is centralised in the store, not duplicated per screen: trimmed, 1–30
  characters, rejecting whitespace-only input
- The display name is **never sent to the backend and never logged**; it exists only for
  on-device greetings
- The store is exposed to screens through a single typed hook/provider, so no screen touches
  AsyncStorage directly
- The profile type makes the name non-nullable, so "no name" is unrepresentable once onboarding
  has completed
- Unit tested: write/read round-trip, forward migration from a previous version, corrupt-document
  recovery, name validation boundaries, and the name-never-leaves-device guarantee

---

### US028 — First-Launch Onboarding
*As a new user, I want a short welcome that learns my name and my usual beaches, so that the app is immediately useful without making me create an account.*

**Acceptance Criteria:**
- Onboarding runs only on first launch and never again once completed
- Step 1 asks for a display name and is **required** — the mascot addresses the user by name, so
  the app has no nameless mode. Continue stays disabled until a valid name is entered
- The name field explains *why* it is being asked ("so I know what to call you") rather than
  demanding it blankly, and makes clear the name stays on the device
- Name validation is the store's (US027): trimmed, 1–30 characters, whitespace-only rejected,
  with an inline error message rather than a silent disabled button
- Step 2 lets the user pick favourite beaches from the searchable catalogue, suggesting three
  but accepting **one to three**; this step is skippable and never blocks entry
- Skipping step 2 leads to a Home screen that explains how to add favourites rather than an
  empty void
- No account, login, email or password appears anywhere in the flow
- Name and favourites are both editable afterwards from a settings screen, which also offers a
  "reset app data" action that clears the local profile and returns the user to onboarding
- Covered by tests for: full completion, invalid and whitespace-only name rejected, favourites
  skipped, and the flow not reappearing on the second launch

---

### US029 — App Navigation Shell
*As a user, I want clear top-level navigation, so that I can move between my beaches, the full catalogue and the map without getting lost.*

**Acceptance Criteria:**
- Three top-level destinations in a tab bar: **Home** (US030), **Explore** (US015) and
  **Map** (US025)
- Beach detail (US016) is pushed on top of whichever tab the user came from, and back returns
  there with scroll position and loaded pages intact
- The info legend (US026) is presented modally from Home, Explore and detail
- A first-launch gate routes into onboarding (US028) before the tabs, and straight to Home on
  every later launch, with no visible flash of the wrong screen
- Settings is reachable from Home
- Tab bar labels and icons follow the design system (US020) and are legible in light and dark
  themes

---

### US030 — Home Dashboard
*As a user, I want my favourite beaches summarised on one screen, so that I can decide where to go without tapping through the app.*

**Acceptance Criteria:**
- The mascot greets the user by name; a name is always present once onboarding has completed
  (US027), so there is no nameless fallback path to design
- Shows a card per favourite beach (up to three) with: beach name, Nortada status as a
  colour-coded hero with label, wind speed and cardinal direction, water temperature, weather
  condition, and reading freshness
- Surfaces a headline recommendation naming the calmest of the user's beaches — the audience is
  beachgoers, so calm is the good outcome
- Cards are fetched from the beach **detail** endpoint, because list items carry no `reading`
  block; the three requests are issued in parallel and share one pull-to-refresh. Three
  concurrent requests is acceptable at this scale — revisit with a batch endpoint only if the
  favourites limit ever rises substantially
- A stale reading (older than 2 hours) is flagged on the card, consistent with US016
- A beach with no reading yet renders an explicit "no data yet" card, not blank fields
- One card failing to load does not blank the whole dashboard — that card shows an inline retry
- The empty state (no favourites yet, or onboarding skipped) explains how to add favourites and
  links to Explore
- Tapping a card opens that beach's detail screen
- Covered by tests for: populated dashboard, empty-favourites state, partial failure, and a
  stale reading
