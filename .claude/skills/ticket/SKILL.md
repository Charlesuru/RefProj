---
name: ticket
description: Turn a feature idea, bug report or spec into a Jira ticket in project REF with acceptance criteria, after the user approves the draft. Use when the user asks to write, draft or create a ticket/story/bug, or when a change has no ticket yet (CLAUDE.md §3).
argument-hint: "<idea, bug or spec — free text, or a path to a spec file>"
disable-model-invocation: true
---

# /ticket — spec → Jira ticket

Turn `$ARGUMENTS` into **one** Jira ticket (or a short list, if it is clearly several changes) that another session
can implement with `/implement REF-<n>` without asking questions. Jira details: `docs/jira.md`.

## 1. Understand before writing
- Read what the idea touches: `docs/architecture.md`, the relevant `feature/*` / `core/*` modules, `docs/features.md`,
  `docs/feature-flags.md`. Ground the ticket in the code that exists.
- Search Jira for duplicates first (Atlassian MCP, JQL `project = REF AND text ~ "<keywords>"`). Found one? Show it and stop.
- Ambiguous scope, missing behaviour, unclear platform? **Ask** (in Swedish) — at most a few concrete questions, each
  with a recommended answer. Do not invent requirements.

## 2. Draft — show it to the user before creating anything
Write the ticket **in English** (the user reads it in Swedish chat, but tickets are English). Shape:

```markdown
**Type:** Story | Bug | Task        **Labels:** <area>, ai-drafted

## Why
<who needs it and the problem it solves — 1–3 sentences>

## Scope
- <what changes, per module: feature:tasks, data, core:…>
- Feature flag: `<flagKey>` (default OFF in PROD) | none — <reason>

## Acceptance criteria
- [ ] Given <state>, when <action>, then <observable result>
- [ ] … (each one testable; include error and empty states, both platforms, EN + SV strings)

## Out of scope
- <what this ticket deliberately does not do>

## Test plan
- <test types per CLAUDE.md §2: UNIT / INTEGRATION / UI / SCREENSHOT / ARCHITECTURE, and what each proves>

## Notes
- <risks, open questions, links to code: `path/File.kt`>
```

Rules for the draft:
- One ticket = one PR-sized change (< 2 days, trunk-based). Larger → split it and say how.
- User-facing, unfinished across PRs → behind a feature flag, OFF in PROD.
- New module, new dependency or an architecture change → say so under **Notes**; it needs the owner's decision
  (CLAUDE.md §4) before implementation.
- A Bug ticket has *Steps to reproduce*, *Expected*, *Actual* instead of *Why*.

Then ask: **"Ska jag skapa ticketen i Jira så här?"** Only a clear yes creates it.

## 3. Create
- Atlassian MCP: find the site (accessible resources), then create the issue in project **REF** with the chosen
  issue type, summary (imperative, ≤ 80 chars, no key), the description above (Markdown) and the labels.
  Type names come back in the signed-in user's language (`Story` or `Berättelse`). Look the types up (project
  issue-type metadata) and pass the `name` of the one whose `untranslatedName` is `Story`, `Bug` or `Task`.
- Report the key and URL. The next step is `/implement REF-<n>` — say which model the user should use for it.

## Never
- Create, edit or transition tickets without the user's yes. Close, delete or reassign tickets.
- Put secrets, personal data or customer data in a ticket.
