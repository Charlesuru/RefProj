# 0012 — Jira traceability and the AI workflow

- **Status:** Accepted
- **Date:** 2026-10-22
- **Ticket:** REF-12

## Context
The brief asks for an AI-driven, spec-driven workflow — AI writes tickets, implements, commits and reviews — where
**humans decide and merge**, with full traceability to Jira. All PRs come from the owner's account, so GitHub's
"authors cannot approve their own PR" keeps the `main` ruleset at 0 approvals, and step 7 (merged by the AI) needed
`--admin`. Ticket keys were placeholders, and the commit-message rule only ran in a local hook.

## Decision
1. **Jira Cloud (Free), project `REF`, team-managed Kanban** (To Do → In Progress → In Review → Done). The placeholder
   keys REF-1…REF-11 were created first, in order, so old commits link to the right work. **GitHub for Jira** links
   branches, commits and PRs by key; Jira automation moves tickets on branch/PR events.
2. **Conventions enforced in CI:** `pr-conventions.yml` (required check `Conventions`) checks the PR title, branch name
   and every commit subject with the same rules as the `commit-msg` hook (`scripts/conventions.sh`).
3. **The AI is a tool; the developer owns the code.** Whatever the AI types, commits, pushes and PRs go out from the
   developer's own account, with the `Co-Authored-By: Claude` trailer, and the developer answers for them in review.
   The AI never approves, merges or uses `--admin` (CLAUDE.md §3.7). `main` keeps **0 required approvals** while one
   person owns the project; a team requires 1 code-owner approval, so a colleague approves.
4. **Claude Code skills** in the repository: `/ticket` (spec → ticket after approval), `/implement` (ticket → PR from the
   developer's account), `/review-pr` (comment review). Jira access through the **Atlassian Rovo MCP server** with per-user OAuth.
5. **CI review on request:** `ai-review.yml` runs `anthropics/claude-code-action` when the label `ai-review` is added —
   Claude Haiku, 10 turns, 10 minutes, read + comment tools only — authenticated with the owner's Claude subscription
   token in a dedicated environment. Comments only; it never approves.

## Alternatives considered
- **A GitHub identity for the AI** (GitHub App `refproj-ai`, or a machine user): the AI's PRs would come from a bot, so
  the owner could approve them and `main` could require 1 approval. Rejected by the owner: a human must own every
  line that is merged, whoever typed it, and a bot author blurs that. It only works around "authors cannot approve
  their own PR"; in a team a colleague approves anyway. It would also add a private key, a token script and a bypass
  for the owner's own PRs. Fully built (`scripts/ai/as-bot.sh`), then removed before merge.
- **Require 1 approval with an admin bypass:** with one person every merge would be a bypass — a ritual, not a
  control.
- **New Jira key (e.g. `RP`):** no collision, but every old `REF-n` would point nowhere.
- **Jira REST API token:** works in CI too, but is a long-lived secret on disk; nothing in CI needs Jira today.
- **Automatic review on every PR:** spends the subscription quota on every push; the owner wanted control.
- **GitHub Models (free, `GITHUB_TOKEN`)** for an automatic light review: retired by GitHub on 2026-07-30.
  `actions/ai-inference` v3 now needs a Copilot plan and a PAT — not taken.
- **Anthropic API key:** usage-billed with a spend limit — the usual company choice; the owner preferred the existing
  subscription. Switching is one secret and one input.

## Consequences
- Merging stays a human act; the AI asks, the owner clicks *Rebase and merge*. No more `--admin` merges by the AI.
- With 0 required approvals the checks (*CI result*, *Conventions*), resolved threads, the PR checklist and the
  on-demand AI review are what stand between a PR and `main`. A second person on the project switches approvals to 1.
- The CI review shares the owner's subscription limits; it runs only when asked. Its inline comments must be resolved
  before merge.
- New secret: `CLAUDE_CODE_OAUTH_TOKEN` (`ai-review` environment) — `docs/secrets.md`.
