# Jira

Every change starts from a Jira ticket, and every branch, commit and pull request names it — so Jira shows the code
for a ticket, and `git log` shows the ticket for the code. Workflow: [ai-workflow.md](ai-workflow.md); rules:
[CLAUDE.md](../CLAUDE.md) §3.

| | |
|---|---|
| Site | Jira Cloud (Free plan) — [`https://studiodraug.atlassian.net`](https://studiodraug.atlassian.net) (UI in Swedish: projects are *Utrymmen*) |
| Project | **RefProj**, key **`REF`**, team-managed **Kanban** (continuous flow fits trunk-based development; no sprints) |
| Issue types | **Story** (user-facing behaviour) · **Bug** · **Task** (build, CI, docs, refactoring) |
| Board columns | **To Do → In Progress → In Review → Done** |

## How a ticket links to the code
The **GitHub for Jira** app (Atlassian, free) reads the repository and attaches everything that names a ticket key to
the ticket's *Development* panel — no smart-commit syntax is needed:

| Where the key appears | Format (checked locally by `commit-msg`, in CI by *Conventions*) | Shows in Jira as |
|---|---|---|
| Branch | `feat/REF-42-due-dates` | branch |
| Commit subject | `feat(tasks): add due dates [REF-42]` | commit |
| PR title | `REF-42: Add due dates` | pull request + its status (open, merged) and reviews |
| Deployments | the workflows that deploy a commit (`deploy-main.yml`, `release.yml`) | builds/deployments, once enabled in the app |

**Smart commits** (`REF-42 #comment …`, `#time`, `#<transition>`) are allowed in the commit **body**, never the
subject. We do not rely on them: status changes come from the automation rules below, so they work the same for
humans and the AI.

### Automation (Jira → Project settings → Automation)
| Rule | Trigger | Action |
|---|---|---|
| Start work | *Branch created* | ticket → **In Progress** |
| Review | *Pull request created* | ticket → **In Review** |
| Done | *Pull request merged* | ticket → **Done** |

The AI also moves its tickets (`/implement`), so the board stays right even if a rule is off.

## Access
| Who | How |
|---|---|
| People | Jira web / mobile app |
| The AI (Claude Code, locally) | **Atlassian Rovo MCP server** in [`.mcp.json`](../.mcp.json) (`https://mcp.atlassian.com/v1/mcp/authv2`). Each developer signs in with OAuth in the browser (`/mcp` → *atlassian* → *Authenticate*); the AI acts with **that person's** Jira permissions. No API token on disk, nothing in CI |
| GitHub | GitHub for Jira app (read-only on code; installed on `Charlesuru/RefProj` only) |

## History before Jira
Steps 2–7 used `REF-<n>` as placeholder keys before the project existed. To keep every old commit pointing at the
right work, the first tickets were created **in this order**, then closed. Jira never reuses a number, so the two
sample work items of the setup wizard were not deleted: the project key was changed from `KAN` to `REF` (which renames
`KAN-1`/`KAN-2` to `REF-1`/`REF-2`) and both were rewritten as the first two entries:

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
