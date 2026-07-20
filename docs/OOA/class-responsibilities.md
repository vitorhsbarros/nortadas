# Class Responsibilities (OOA)

What each candidate class from `nortada-OOA.puml` is expected to know and do. This is analysis,
not design — no attributes, method signatures, or package placement here; see `docs/OOD/` and
`docs/architecture.md` for that.

- **User** — knows its own identity, name and email. Owns exactly one `FavouriteBeaches`
  collection. Is the author of the `Comment`s and `Vote`s it has made.

- **Beach** — knows its own identity, name, and geographic location (latitude/longitude), and
  which `Region` it belongs to. Holds the history of `WeatherReading`s taken for it, and derives
  its current `NortadaStatus` from the most recent one. Holds the `Comment`s written about it.

- **Region** — knows its own identity and name, and groups the `Beach`es located within it. Does
  not know about weather or Nortada status — that's a `Beach`/`WeatherReading` concern.

- **WeatherReading** — knows the wind speed, wind direction, temperature, which `Beach` it was
  taken for, and when it was fetched. Is the sole source of truth a `NortadaStatus` is derived
  from — it does not decide the status itself.

- **NortadaStatus** — knows the graded classification derived from a `WeatherReading` — one of
  `NONE`, `LIGHT`, `MODERATE`, `STRONG`, or `SEVERE`, reflecting how strong the Nortada is —
  evaluated year-round per US010's rules. Represents the *result* of the detection rule, not the
  rule itself — the rule lives in a dedicated detection service (see `docs/architecture.md` §1,
  domain services).

- **FavouriteBeaches** — knows which `Beach`es a single `User` has favourited. Aggregates
  references to `Beach`, it does not own or duplicate `Beach` data.

- **Comment** — knows its text content, which `Beach` it was written about, and which `User`
  wrote it. Holds the `Vote`s cast on it.

- **Vote** — knows which `User` cast it, on which `Comment`, and its direction. Has no
  responsibilities beyond recording that single cast vote.

## Note on scope

`Comment`, `Vote`, and `FavouriteBeaches` are part of the full domain model captured during
analysis, but no Phase 1 user story (`docs/user-stories/nortada_phase1a_user_stories.md`,
`nortada_phase1b_user_stories.md`) requires them yet — they exist today as stubs
(see `CLAUDE.md`). The Phase 1 use case diagram (`nortada-use-case.puml`) reflects that: it only
covers viewing beaches and fetching/detecting Nortada status.
