---
name: architecture-review
description: Reviews a GitHub pull request or the current working diff against this project's architecture rules in docs/architecture.md — Clean Architecture layering, the domain/entity/DTO three-model separation, SOLID, GRASP, expected GoF patterns, ORM/persistence boundaries, and package placement. Use this whenever the user asks to review a PR, review a diff, or check whether a change follows the architecture, Clean Architecture, SOLID, or GRASP conventions for the nortadas backend — e.g. "review PR #12", "check this diff against our architecture", "does this class belong in this package", "/architecture-review". Trigger even when the user doesn't name docs/architecture.md directly or say "architecture" explicitly — any request to validate a change's structure, layering, or design against this project's conventions qualifies. This is a conformance/design review, not a correctness or security audit — for those use /code-review or /security-review instead (or alongside this one).
---

# architecture-review

Checks a diff against the architecture this project has committed to in `docs/architecture.md`,
not just whether the code works. A PR can pass every test and still put a JPA entity in the
domain layer or let a controller decide business logic — this skill exists to catch that class of
problem, which correctness review naturally misses because the code "works fine."

## Workflow

### 1. Get the diff

- PR review: `gh pr diff <number>`
- Working diff (no PR number given): `git diff` against the base branch, or `git diff --staged`
  if the user has already staged their changes — ask if it's ambiguous which one they mean.

### 2. Read the current rules fresh

Read `docs/architecture.md` in full before judging anything. Don't rely on a remembered version
of it from earlier in the conversation — the document is expected to evolve as the project
matures, and a stale mental copy will produce reviews against rules that no longer apply. If the
file doesn't exist, say so and stop rather than inventing architecture rules from general
knowledge.

### 3. Walk the diff against each category below

For each category, look for concrete violations in the *changed* code — don't re-review unrelated
existing code just because it's visible in context. Skip a category entirely if nothing in the
diff touches it (e.g. no persistence changes → say nothing about persistence) rather than forcing
a comment to fill out the list.

- **Layering (Clean Architecture)** — does anything in `domain/` import Spring, JPA, Jackson, or
  an HTTP client? Does `application/` reach into `infrastructure/` or `web/` directly instead of
  through an `application/port` interface? Does a controller contain business logic instead of
  just translating HTTP ↔ a use case call?
- **Three models, never mixed** — is a JPA `@Entity` type leaking into `domain/` or getting
  returned straight from a controller? Is a domain object being serialized directly as an API
  response instead of going through a `web/dto` type? Is there a new type that duplicates an
  existing domain/entity/DTO for the same concept instead of reusing it?
- **SOLID** — SRP (a class taking on more than one reason to change), OCP (a new case handled by
  editing an existing `if`/`switch` instead of adding a `Strategy`/adapter implementation), LSP (a
  port implementation adding stricter preconditions or throwing exceptions the interface doesn't
  promise), ISP (a class forced to implement port methods it doesn't need), DIP (`application`/
  `domain` code doing `new SomeConcreteInfrastructureClass()` instead of depending on its port).
- **GRASP** — Information Expert (logic implemented far from the data it needs), Controller (a
  controller doing more than delegating), Low Coupling/High Cohesion (a class reaching across
  layers instead of through a port), Protected Variations (a volatile detail — external API
  shape, ORM specifics — leaking outside its adapter).
- **Expected GoF patterns** — where `docs/architecture.md` names a pattern for a given piece
  (Strategy for detection rules, Adapter for the external API/persistence boundary, Repository,
  Facade for use cases), does the diff follow it, or reinvent the same shape ad hoc?
- **Persistence** — are ORM annotations (`@Entity`, `@Table`, `@Column`, etc.) confined to
  `infrastructure/persistence/entity`? Is there an explicit mapper translating entity ↔ domain, or
  is the mapping implicit/missing?
- **Package placement** — is every new class in the package `docs/architecture.md` says it
  belongs in?

### 4. Report

For each real finding: **file/class → rule violated → concrete suggested fix**, ranked most
important first (a domain-layer framework leak matters more than a package-placement nit). If
nothing in the diff violates the architecture, say so plainly instead of manufacturing minor
nitpicks to have something to report.

## Notes

- This skill is about conformance to `docs/architecture.md`, not general code quality or security
  — don't drift into those unless asked; point to `/code-review` or `/security-review` instead.
- If `docs/architecture.md` changes (new layer, revised package structure, dropped pattern), this
  skill's behavior changes with it automatically since the rules are read fresh each run — no need
  to update this file when only the architecture doc changes.
