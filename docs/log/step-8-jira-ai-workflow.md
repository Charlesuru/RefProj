# Step 8 — Jira + AI workflow (log)

- **Date:** 2026-10-22
- **Ticket:** REF-12 (the first real Jira ticket; REF-1…REF-11 recreated as history)
- **PRs (stacked, merged in order):** #38 conventions check → #39 skills + MCP → #40 CI review → #41 ADR + Jira docs →
  this docs PR
- **Reference docs:** [ai-workflow.md](../ai-workflow.md), [jira.md](../jira.md), [ADR 0012](../adr/0012-ai-workflow.md)

## 1. Owner decisions
| Question | Decision |
|---|---|
| Jira project key | **`REF`**, and REF-1…REF-11 recreated **in order** so every old commit points at the right work |
| Jira access for the AI | **Atlassian Rovo MCP server** (per-user OAuth in the browser, no API token on disk) |
| CI review | **Claude only, on the label `ai-review`**, with the owner's Claude subscription token — the owner must never exceed the quota or pay; seeing how AI review is wired into CI matters more than review quality |
| A GitHub identity for the AI | First chosen: GitHub App `refproj-ai` (so the owner could approve AI PRs and `main` could require 1 approval, with an admin bypass for the owner's own PRs). **Dropped before merge** by the owner: *every line is owned by the human who opens the PR, whoever typed it* — with one person, approving "the AI's" PR is a ritual. 0 required approvals; a team sets 1 code-owner approval |
| Way of working | Owner tasks in **small steps**, one at a time; Jira in **Swedish** UI terms and by navigation, not deep links |

## 2. What was built
| Area | Result |
|---|---|
| Jira | Jira Cloud Free, site `studiodraug.atlassian.net`, team-managed Kanban project **RefProj / `REF`** (columns Att göra → Pågående → Under granskning → Klart); work types Uppgift, Berättelse, Bugg, Epic, Underuppgift; REF-1…REF-11 *Klart* (labels `history`, `plan-step`), REF-12 for this step |
| Conventions in CI | `scripts/conventions.sh` (shared rules), used by the `commit-msg` hook and by `pr-conventions.yml` → required check **Conventions**: PR title, branch name, same key in both, every commit subject, no merge commits; Dependabot exempt; ✅/❌ job summary |
| Skills | `/ticket` (draft → Jira after a yes), `/implement` (ticket → branch → code → gates → PR from the developer's account), `/review-pr` (comment-only review); shared `review-pr/checklist.md`; started only by a person |
| MCP | `.mcp.json` → Atlassian Rovo MCP (`/mcp` → *atlassian* → *Authenticate*) |
| CI review | `ai-review.yml` + `anthropics/claude-code-action` v1.0.256 (SHA-pinned): label-triggered, Claude Haiku, 10 turns, 10 min, read + comment tools only, same-repo PRs only; token in the `ai-review` environment; the label is removed after the run |
| Settings | Ruleset `main` requires *CI result* + *Conventions*; environment `ai-review` (PR merge refs only); label `ai-review`; action allowlisted; gitleaks rule for Claude OAuth tokens |
| Docs | ADR 0012, `ai-workflow.md`, `jira.md`, CLAUDE.md §3.5/§3.7, approvals text in `ci-cd.md` / `pr-workflow.md`, `secrets.md` (Claude token runbook) |

## 3. Problems and fixes
| Problem | Fix |
|---|---|
| GitHub Models (the free option for an automatic review) answered with a stub | Retired by GitHub on 2026-07-30; `actions/ai-inference` v3 needs Copilot + a PAT. Dropped |
| The Jira setup wizard created project *Team Draug* (`KAN`) with three sample items | Project renamed to RefProj, key changed to `REF` (renames `KAN-n`); the samples were rewritten as REF-1…REF-3 instead of deleted, because Jira never reuses numbers |
| REF-3 was a sub-task; the API refused to change its type ("Ärendetypen är ogiltig"), and it lost its parent when REF-2 changed type | Owner converted it to *Uppgift* in the web UI (found through search, not on the board) |
| Jira Markdown escaped task-list checkboxes | ✅ / ⬜ characters instead |
| The site is Swedish: issue-type names are `Uppgift`, `Berättelse`, `Bugg` | `/ticket` passes the Swedish names; `docs/jira.md` lists both |
| `scripts/ai/bot.env` was ignored by `*.env` in `.gitignore` | Renamed to `bot.conf` (later removed with the GitHub App) |
| `gitleaks dir .` scanned gitignored local secret files | Pre-commit uses `gitleaks git --staged`; the findings were not printed |
| The GitHub App was built and then rejected | Unpushed stack rewritten: the App commit dropped, later commits resolved without it, branch deleted — no PR adds and removes it |

## 4. Verification
- `scripts/ci/check-conventions.sh` passes the whole REF-11/REF-12 history and fails bad titles, branches, subjects
  and a merge commit; the `commit-msg` hook accepts/rejects the same subjects.
- shellcheck, actionlint, zizmor clean; gitleaks rule matches a fake Claude token and nothing else.
- PRs #38–#41: every check green, including the new *Conventions* on its own PR; merged by the owner with
  *Rebase and merge* — 7 commits, linear history.
- `configure-repo.sh` applied; checked through the API: ruleset `main` requires *CI result* + *Conventions*, 0
  approvals, no bypass; environment `ai-review` (`refs/pull/*/merge`) with `CLAUDE_CODE_OAUTH_TOKEN` (name only);
  label `ai-review`; `anthropics/claude-code-action@*` allowlisted.
- Jira: REF-1…REF-12 numbered as in the history table; created and edited through the MCP server.
