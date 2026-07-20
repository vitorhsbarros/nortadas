---
name: gh-pr
description: Drafts and opens GitHub pull requests for vitorhsbarros/nortadas using a fixed body template (Summary / Related issues / Test plan, with an optional Key design decisions section). Use this whenever the user asks to open, create, or file a pull request for this repo — "open a PR", "push and PR this branch", "let's get this merged", "/gh-pr" — even if they don't spell out the base/head branches or exact wording; infer those from the current branch and its commit history. Trigger any time work on a branch is ready to be proposed for merge, not just when the user says "pull request" literally.
---

# gh-pr

Opens PRs on `vitorhsbarros/nortadas` in one consistent shape, grounded in what the branch's
commits actually contain rather than a restatement of the conversation that produced them — a PR
description that drifts from the real diff is worse than no description, since it actively
misleads reviewers.

## Workflow

### 1. Determine base and head

- **head** — the current branch (`git branch --show-current`). It must be pushed to `origin`
  already; if it isn't, push it first, but confirm with the user before doing so — pushing is a
  shared-state action, not something to do silently as a side effect of opening a PR.
- **base** — the repo's default branch unless the user names a different one:
  `gh repo view vitorhsbarros/nortadas --json defaultBranchRef -q .defaultBranchRef.name`.

### 2. Read the real diff

Get the actual commits that will go into the PR: `git log --oneline <base>..<head>`. If a commit's
one-line message doesn't say enough, check `git show --stat <sha>`. This is the source of truth
for the "Summary" section — write it from what's actually there, not from memory of the
conversation. If there are no commits ahead of base, say so and stop rather than opening an empty
PR.

### 3. Collect related issues from the commits

Commits in this repo follow the `commit-message` skill's format
(`<type>: <message>, <ref-keyword> #<issue_number>`). Scan the commit messages for
`closes|fixes|refs #<N>` and carry those references into the PR body verbatim: `closes`/`fixes`
stay as closing keywords so GitHub auto-closes the issue on merge, `refs` stays a plain reference.
Don't invent an issue number if none of the commits mention one — ask the user instead of guessing.

### 4. Format the body

Always use this section structure. Omit "Key design decisions" entirely if there's nothing a
reviewer would need explained — don't leave it as an empty header just to fill out the template:

```
## Summary
- <bullet per meaningful change, grounded in the actual commits from step 2>

## Related issues
Closes #<N>
Refs #<N>

## Key design decisions
- <anything a reviewer would otherwise ask "why did you do it this way?" about>

## Test plan
- [ ] <concrete, verifiable step a reviewer could actually run or check>
```

Test plan items should be as concrete as the acceptance criteria the `gh-issue` skill writes —
"run `./gradlew test`" or "confirm `GET /api/beaches` returns 200 with the new field" beats
"make sure it works."

### 5. Preview and get explicit confirmation

Opening a PR publishes content on the user's behalf. Show the fully rendered title and body
before creating anything, and wait for an explicit yes — moving on to another topic is not
confirmation, same rule as `gh-issue`'s issue-creation step.

### 6. Create

```bash
gh pr create --repo vitorhsbarros/nortadas --base <base> --head <head> --title "<title>" --body "<body>"
```

Relay the resulting PR URL back to the user.

### 7. Updating an existing open PR

When more commits land on a branch that already has an open PR, the PR's diff and commit list
update on their own — but the **body does not**, so a Summary/Related-issues/Test-plan written for
the earlier state goes stale and starts misleading reviewers. When asked to refresh a PR (or when
you've just pushed commits to a branch with an open PR), redo steps 2–4 against the *current*
`git log <base>..<head>` and re-render the body, then:

```bash
gh pr edit <number> --repo vitorhsbarros/nortadas --title "<title>" --body "<body>"
```

Find the PR number for the current branch with `gh pr view --json number -q .number` if you don't
already have it. Same confirmation rule as creation — show the new body first.

## Notes

- Target repo is always `vitorhsbarros/nortadas`.
- Title: short, imperative, describes the change itself — not just an issue number.
- If the user only gives you loose intent ("push and open a PR for this"), still do steps 1-4
  yourself from the real branch/commit state rather than asking them to dictate the body.
