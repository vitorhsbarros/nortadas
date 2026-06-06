# Nortada App — Phase 0 User Stories

## US001 — Initialize Git Repository
*As a developer, I want to initialize a Git repository with a proper `.gitignore`, so that secrets, environment files and build artifacts are never accidentally committed.*

**Acceptance Criteria:**
- `.gitignore` excludes `application.yml`, `application-*.yml`, keystores, `.env` files, and build directories
- Repository is created on GitHub
- Initial commit contains only project skeleton

---

## US002 — Branch Protection Rules
*As a developer, I want branch protection rules on `main`, so that no code is merged without going through the CI pipeline.*

**Acceptance Criteria:**
- Direct push to `main` is blocked
- PRs require at least 1 approval before merging
- PRs require all CI checks to pass before merging
- `CODEOWNERS` file is defined

---

## US003 — CI Pipeline Skeleton
*As a developer, I want a GitHub Actions pipeline skeleton in place, so that all future security and build stages have a consistent structure to plug into.*

**Acceptance Criteria:**
- Pipeline triggers on every push and pull request
- Pipeline has clearly named jobs (e.g. `secret-scan`, `sast`, `build`)
- Placeholder jobs pass successfully on first run
- Pipeline file lives at `.github/workflows/ci.yml`

---

## US004 — Secret Scanning with Gitleaks
*As a developer, I want Gitleaks running on every push, so that secrets and credentials are never committed to the repository.*

**Acceptance Criteria:**
- Gitleaks runs as a blocking step in the CI pipeline
- Pipeline fails if any secret is detected
- A `.gitleaks.toml` config file is present for any project-specific rules or allowlisted false positives
- Runs on both pushes and pull requests
