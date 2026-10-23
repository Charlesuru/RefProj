# Jira

Every change starts from a Jira ticket, and every branch, commit and pull request names it — so Jira shows the code
for a ticket, and `git log` shows the ticket for the code. Workflow: [ai-workflow.md](ai-workflow.md); rules:
[CLAUDE.md](../CLAUDE.md) §3.

| | |
|---|---|
| Site | Jira Cloud (Free plan) — [`https://studiodraug.atlassian.net`](https://studiodraug.atlassian.net) (UI in English; names in the UI and the API follow each user's language, e.g. *Task* / *Uppgift*) |
| Project | **RefProj**, key **`REF`**, team-managed **Kanban** (continuous flow fits trunk-based development; no sprints) |
| Issue types | **Story** (user-facing behaviour) · **Bug** (added under *Space settings → Work types*) · **Task** (build, CI, docs, refactoring). Tools match on `untranslatedName`, which does not depend on the user's language |
| Board columns | **To Do → In Progress → In Review → Done** |

## How a ticket links to the code
The **GitHub for Atlassian** app (formerly *GitHub for Jira*; by Atlassian, free) reads the repository and attaches everything that names a ticket key to
the ticket's *Development* panel — no smart-commit syntax is needed:

| Where the key appears | Format (checked locally by `commit-msg`, in CI by *Conventions*) | Shows in Jira as |
|---|---|---|
| Branch | `feat/REF-42-due-dates` | branch |
| Commit subject | `feat(tasks): add due dates [REF-42]` | commit |
| PR title | `REF-42: Add due dates` | pull request + its status (open, merged) and reviews |
| Deployments | the workflows that deploy a commit (`deploy-main.yml`, `release.yml`) | builds/deployments, once enabled in the app |

**Smart commits** (`REF-42 #comment …`, `#time`, `#<transition>`) are allowed in the commit **body**, never the
subject. We do not rely on them: status changes come from the automation flow below and by hand, so they work the
same for humans and the AI.

### One story, several pull requests
A story is a unit of **value** that the PO accepts; a pull request is a unit of **review**. With trunk-based
development a story is often delivered in several small PRs behind a feature flag (each branch < 2 days), and every
branch, commit and PR names the story's key, so Jira shows all of them on the story. Sub-tasks are not created just to
map PRs; use them only when work is split between people.

| Status | Set when | By |
|---|---|---|
| **In Progress** | the first branch for the ticket is created | the *Start work* flow (and `/implement`) |
| **In Review** | the ticket's **last** PR is opened — not before | the developer (or `/implement`, when told it is the last PR) |
| **Done** | the acceptance criteria are met — a merge alone does not mean done | the PO, by hand |

### Automation (Space settings → Automation)
Jira calls the rules *flows*: *Create flow → Create from scratch* (not *with Rovo*, which uses Rovo credits). The
action is **Transition work item**. The Free plan limits flow runs per month — plenty for one person.

| Flow | Trigger | Action |
|---|---|---|
| Start work | *Branch created* | ticket → **In Progress** |
| ~~Review~~ (off) | *Pull request created* | ticket → **In Review** |
| ~~Done~~ (off) | *Pull request merged* | ticket → **Done** |

*Review* and *Done* are turned off (REF-20): they assumed one PR per ticket, so a story delivered in several PRs went
to In Review on its first PR and to Done on its first merge. Both statuses are set by hand instead (see above).
The AI also moves its tickets to In Progress (`/implement`), so the board stays right even if the flow is off.

## Access
| Who | How |
|---|---|
| People | Jira web / mobile app |
| The AI (Claude Code, locally) | **Atlassian Rovo MCP server** in [`.mcp.json`](../.mcp.json) (`https://mcp.atlassian.com/v1/mcp/authv2`). Each developer signs in with OAuth in the browser (`/mcp` → *atlassian* → *Authenticate*); the AI acts with **that person's** Jira permissions. No API token on disk, nothing in CI |
| The `repo-guide` agent (`claude --agent repo-guide`) | The same MCP server, **read-only**: its tool list has only the read tools (search, get ticket, remote links), and the hook `.claude/hooks/read-only-atlassian.sh` blocks every Atlassian tool not named `get*`, `search*` or `lookup*`. It never creates, edits, comments on or transitions tickets (REF-21) |
| GitHub | GitHub for Atlassian app (read-only on code; installed on `Charlesuru/RefProj` only) |

## History before Jira
Steps 2–7 used `REF-<n>` as placeholder keys before the project existed. To keep every old commit pointing at the
right work, the first tickets were created **in this order**, then closed (label `history`; plan steps also
`plan-step`). Jira never reuses a number, so the three sample work items of the setup wizard were not deleted: the
project key was changed from `KAN` to `REF` (which renames `KAN-1`…`KAN-3` to `REF-1`…`REF-3`) and they were rewritten
as the first three entries (the sample sub-task converted to a task):

| Key | Work |
|---|---|
| REF-1 | Step 1 — Foundation |
| REF-2 | Step 2 — Fastlane, secrets and signing |
| REF-3 | Step 3 — CI/CD with GitHub Actions |
| REF-4 | Step 4 — Core: design system, Koin, navigation, feature flags, Firebase |
| REF-5 | Step 5 — Features: onboarding, login, tabbed shell |
| REF-6 | Skip build and test jobs for docs-only PRs |
| REF-7 | Read-only repo-guide agent |
| REF-8 | Render Compose previews in library modules |
| REF-9 | Test of the PR workflow (PR #26, closed, nothing merged) |
| REF-10 | Step 6 — Debug menu |
| REF-11 | Step 7 — Tests |
| REF-12 | Step 8 — Jira + AI workflow |
