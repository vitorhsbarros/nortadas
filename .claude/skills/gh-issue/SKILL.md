---
name: gh-issue
description: Drafts and creates GitHub issues on vitorhsbarros/nortadas using the project's fixed template (description / sub-issues / acceptance criteria), including real linked GitHub sub-issues via the gh CLI. Use this whenever the user asks to create, file, write, open, or draft a GitHub issue for the nortadas project — e.g. "create an issue for X", "file a bug about Y", "open an issue to refactor the Beach class", "/gh-issue" — or wants a piece of work broken into sub-issues/tasks tracked on GitHub, or references a User Story from docs/user-stories/ that should become an issue. Trigger even if the user doesn't mention gh, GitHub, or the template by name — any request to turn a piece of work into a tracked issue for this repo qualifies.
model: haiku
effort: high
---

# gh-issue

Creates GitHub issues for `vitorhsbarros/nortadas` in a single consistent shape, so the tracker
reads the same way regardless of who (or what) filed the issue. The distinctive part of this skill
is that "sub-issues" are not just a bullet list — they become real GitHub issues linked to the
parent via GitHub's native sub-issue relationship, so they show up as a progress checklist on the
parent issue and can be tracked/closed independently.

## Workflow

### 1. Gather the content

Figure out, from the conversation, what the issue is about. If the user references a User Story
(e.g. "implements US012" or "the beach favouriting story"), read the matching file under
`docs/user-stories/*.md` (including `docs/user-stories/completed/`) for the actual wording instead
of guessing — quote or paraphrase it in the description rather than inventing new scope.

Ask the user directly (don't guess) if any of these are unclear:
- **Title** — short imperative phrase, e.g. "Refactor Beach into an Aggregate Root"
- **Description** — a paragraph explaining the issue, referencing the User Story if there is one
- **Sub-issues** — a list of titles for the discrete pieces of follow-up work, if the issue is big
  enough to warrant breaking up. Not every issue needs sub-issues — small, single-step issues can
  have an empty list.
- **Acceptance criteria** — a checklist of concrete, verifiable conditions for the issue to be done

### 2. Format the body

The parent issue body always uses this exact section structure (omit the `sub-issues` section
entirely if there are none — don't leave it empty):

```
### description
<paragraph describing the issue, referencing a User Story where relevant, e.g. "Implements US012">

### sub-issues:
<title of sub-issue 1>
<title of sub-issue 2>

### Acceptance Criteria
- [ ] <concrete, verifiable condition>
- [ ] <concrete, verifiable condition>
```

Acceptance criteria should be things you could literally check a box on after doing the work —
prefer "`Beach` uses a dedicated `BeachId` instead of a primitive" over "improve Beach's identity
handling". Look at existing acceptance criteria in `docs/user-stories/*.md` for the level of
specificity this project expects.

Each sub-issue gets its own short body explaining what that piece of work covers — a sentence or
two is enough, it doesn't need the full template.

### 3. Preview and get explicit confirmation

Show the user the fully rendered parent issue (title + body) and the list of sub-issue titles,
exactly as they'll be created. Creating GitHub issues is publishing public content on the user's
behalf, so **do not run the creation script until the user explicitly confirms** — treat this the
same as posting a comment or publishing anything else. Silence or moving on to another topic is not
confirmation.

### 4. Create

Once confirmed, build a JSON spec and hand it to the bundled script — it handles issue creation
*and* linking sub-issues via GitHub's sub-issues API (which needs each issue's numeric id, not just
its number, and can't be done through `gh issue create` flags alone).

Write the spec to a temp file:

```json
{
  "repo": "vitorhsbarros/nortadas",
  "title": "Refactor Beach into an Aggregate Root",
  "body": "### description\n...\n\n### sub-issues:\nDefine `Beach` identity\n...\n\n### Acceptance Criteria\n- [ ] ...\n",
  "labels": [],
  "sub_issues": [
    { "title": "Define `Beach` identity", "body": "Introduce a dedicated `BeachId` value object." },
    { "title": "Refactor `Beach` into an Aggregate Root", "body": "Enforce invariants inside Beach itself." }
  ]
}
```

Then run:

```bash
scripts/create_issue.sh /path/to/spec.json
```

The script checks `gh auth status` itself and fails with a clear message if the user isn't logged
in — if that happens, tell the user to run `gh auth login` themselves (don't attempt to authenticate
on their behalf). On success it prints a JSON summary with the parent URL and each sub-issue URL;
relay those links back to the user.

`labels` defaults to none — only pass labels the user actually asked for, this project doesn't have
a fixed label set to apply automatically.

A label must already exist on the repo before it can be applied — `gh issue create --label X`
(and therefore the whole script, under `set -euo pipefail`) fails outright if `X` isn't defined,
creating nothing. When the user asks for a label, first check it exists with
`gh label list --repo vitorhsbarros/nortadas`; if it's missing, create it first
(`gh label create "X" --repo vitorhsbarros/nortadas --description "..." --color "RRGGBB"`) or ask
the user, rather than letting the creation fail midway.

## Notes

- Target repo is always `vitorhsbarros/nortadas` — no need to ask.
- If `gh auth status` fails before you've even drafted anything, it's still worth drafting the issue
  content and showing the preview — the user can fix auth and create it later. Only block on auth at
  the actual creation step.
