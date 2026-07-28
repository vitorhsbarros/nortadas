---
name: commit-message
description: Enforces this project's fixed commit message format — "<type>: <message>, <ref-keyword> #<issue_number>" — whenever a commit is about to be created in the nortadas repo. Use this any time the user asks to commit changes, create a git commit, or write/check a commit message here, or whenever you (Claude) are about to run `git commit` as part of finishing some other task in this repo. Every commit in this project must reference a GitHub issue — this skill also covers finding or confirming that issue number before committing.
model: haiku
effort: high
---

# commit-message

Every commit on this repo follows one fixed shape, so `git log` reads consistently and every
change is traceable to the issue that motivated it:

```
<type>: <message>, <ref-keyword> #<issue_number>
```

- **type** — `fix` | `feature` | `documentation` | `refactor`
- **message** — short, imperative, lowercase after the colon, no trailing period
- **ref-keyword** — `closes` | `fixes` | `refs`
  - `closes` / `fixes` — this commit is the piece of work that satisfies the issue (GitHub
    auto-closes it when merged to the default branch). Use these interchangeably.
  - `refs` — related to the issue but doesn't resolve it (partial progress, or one of several
    commits contributing to the same issue).

The issue reference is **mandatory on every commit** — never write a commit without one.

A single commit may reference more than one issue — most commonly when it finishes a sub-issue and
thereby also completes its parent. Repeat the keyword per issue, comma-separated:
`… , closes #22, closes #19`. Keywords can be mixed too (`… , closes #22, refs #19`) when the
commit resolves one issue but only advances another.

## Workflow

1. **Pick the type from the actual diff, not the user's phrasing.** Look at what changed:
   docs-only changes under `docs/` → `documentation`; a behavior change that fixes broken output
   → `fix`; new capability → `feature`; restructuring with no behavior change → `refactor`. If a
   change spans categories, pick the one that best describes its primary intent.

2. **Find the issue number.**
   - If the user names one, use it.
   - Otherwise, look for a matching open issue: `gh issue list --repo vitorhsbarros/nortadas`.
     If more than one plausibly matches, ask the user which one rather than guessing.
   - If genuinely no issue exists for this work, stop and ask the user whether to file one first
     (the `gh-issue` skill covers that) — do not commit without a reference.

3. **Pick `closes`/`fixes` vs. `refs`.** If this commit satisfies the issue's acceptance
   criteria in full, close it. If more work is still needed on that issue, use `refs`.

4. **Compose and commit.** First line must match the format exactly. Only add a commit body if
   it carries real additional context — not a restatement of the first line.

## Examples

- `fix: correct wind direction range in Nortada detection, closes #15`
- `feature: add beach list API endpoint, refs #16`
- `documentation: add Clean Architecture rules to docs/architecture.md, closes #11`
- `refactor: split BeachRepository into port and JPA adapter, refs #12`
- `feature: add commit-message skill enforcing commit format, closes #22, closes #19` (finishes a sub-issue and its parent)

## Notes

- This governs message *format* only — whether to commit at all still follows the normal rule of
  only committing when the user explicitly asks.
- Don't fabricate an issue number or silently drop the reference if one can't be found — that
  breaks the traceability this format exists for. Ask instead.
