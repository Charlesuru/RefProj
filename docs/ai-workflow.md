# AI workflow — spec → ticket → code → PR → review → human merge

The AI does the typing; **humans decide**. Every change still follows [CLAUDE.md](../CLAUDE.md) §3 and the
[pull-request workflow](pr-workflow.md). This page covers what is specific to the AI. Decision: [ADR 0012](adr/0012-ai-workflow.md).

## Who owns the code
The AI is a tool, like the IDE or a code generator. The developer who runs the session is the **author**: commits,
pushes and pull requests go out from **their own** GitHub account, and they answer for the code in review, whoever
or whatever typed it. Commits the AI wrote carry the trailer `Co-Authored-By: Claude …`, so `git log` still shows
where it helped.

| Rule | |
|---|---|
| Read before you ship | the developer reads the diff before the PR is opened — "the AI wrote it" is never an answer in review |
| Another human approves | in a team, `main` requires 1 approval from a code owner who did not write the PR. RefProj has one person, so the ruleset requires **0 approvals** ([ci-cd.md → Approvals](ci-cd.md#settings-as-code)); the checks, the PR checklist and the AI review are the safety net |
| The AI never decides | it never approves, merges, enables auto-merge or uses `--admin`, and never pushes to `main` (CLAUDE.md §3.7) |

A separate GitHub identity for the AI (a GitHub App) was considered and rejected: it only works around "authors
cannot approve their own PR", and in a team a colleague approves anyway (ADR 0012).

## The loop
```mermaid
flowchart LR
    I[idea / bug] -->|/ticket| T[Jira ticket<br/>acceptance criteria]
    T -->|owner: yes| T2[REF-n in Jira]
    T2 -->|/implement REF-n| C[branch · code · tests · gates]
    C -->|developer's account| PR[pull request]
    PR --> CI[CI result · Conventions]
    PR -. "label ai-review" .-> AR[Claude review<br/>comments only]
    PR -. "/review-pr n" .-> LR[local deep review]
    CI & AR & LR --> H{owner reviews}
    H -->|Rebase and merge| M[(main)]
    H -->|comments| C
```

| Step | Who | How |
|---|---|---|
| Spec → ticket | AI drafts, **owner approves** | `/ticket <idea>` — duplicates check, acceptance criteria, flag, test plan; created in Jira only after a yes |
| Ticket → PR | AI | `/implement REF-n` — branch, code, tests, gates, both apps run, commits + PR from the developer's account, ticket → *In Review* |
| Review | AI (advisory) | label **`ai-review`** on the PR (CI, Claude Haiku) or `/review-pr n` locally (deeper, current model) — comments, never approvals |
| Decision | **owner** | reads the diff and the AI's comments, resolves every thread, *Rebase and merge* (in a team: a colleague approves first) |

The skills live in [`.claude/skills/`](../.claude/skills/) and are only started by a person (`disable-model-invocation`):
[`ticket`](../.claude/skills/ticket/SKILL.md), [`implement`](../.claude/skills/implement/SKILL.md),
[`review-pr`](../.claude/skills/review-pr/SKILL.md) with its shared [checklist](../.claude/skills/review-pr/checklist.md).
Jira access: [jira.md → Access](jira.md#access).

## CI review
[`ai-review.yml`](../.github/workflows/ai-review.yml) runs [`anthropics/claude-code-action`](https://github.com/anthropics/claude-code-action)
when someone with write access adds the label **`ai-review`** to a pull request. Claude reads `CLAUDE.md` and the
[review checklist](../.claude/skills/review-pr/checklist.md), posts inline comments and one summary comment as
`github-actions[bot]`, and the workflow removes the label again — add it again for a fresh review after new pushes.

| | |
|---|---|
| When | only on the label — never automatically, so nothing uses the quota unless someone asks |
| Model and limits | Claude Haiku, `--max-turns 10`, job timeout 10 minutes |
| Authentication | `CLAUDE_CODE_OAUTH_TOKEN` (the owner's Claude subscription, `claude setup-token`) in the environment `ai-review` — [secrets.md](secrets.md#claude-token-ci-review). A company would use an Anthropic API key with a spend limit, or workload identity federation instead of a stored key |
| Quota | uses the subscription's normal usage limits; a review of a small PR is a small fraction of one interactive session. Reaching the limit pauses usage until it resets — it only costs money if *extra usage* is switched on in the Claude settings |
| Tools | `Read`, `Grep`, `Glob`, `gh pr view/diff/comment`, inline comments — no edits, no pushes, no other commands |
| Not for | forks and Dependabot (no access to the secret) |

Its comments are advice: inline comments open threads that must be resolved (ruleset), but only the owner's approval
counts. GitHub Models — the free option considered first — was retired on 2026-07-30 (ADR 0012).
