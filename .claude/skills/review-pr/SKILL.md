---
name: review-pr
description: Review a pull request against this repository's rules and its Jira ticket, then post the findings as a comment review — never an approval. Use when the user asks to review PR #N or "/review-pr N".
argument-hint: "<PR number>"
disable-model-invocation: true
---

# /review-pr — AI review, human decision

Review pull request `#$ARGUMENTS`. The review **informs** the owner; it never approves, requests changes as a
blocker, or merges. The lighter automatic variant runs in CI on the label `ai-review` (`docs/ai-workflow.md`).

## 1. Gather
```bash
gh pr view $ARGUMENTS --json title,body,headRefName,baseRefName,author,files,commits,statusCheckRollup
gh pr diff $ARGUMENTS
```
- The ticket: the key in the title → fetch it with the Atlassian MCP (acceptance criteria). No ticket → that is
  finding number one.
- Read the changed files in full where the diff alone is not enough (callers, tests, the module's README/docs).
- CI state: failing checks are context, not your findings — mention them in one line.

## 2. Review
Work through [checklist.md](checklist.md). For each acceptance criterion, say whether the diff implements **and
tests** it (✅ / ⚠️ / ❌). Verify a suspicion by reading the code before reporting it; drop what you cannot verify.

## 3. Show the user first
Show the draft in Swedish chat: a verdict line, the acceptance-criteria table, the findings (blocker / should fix /
nit) with `path:line`. Ask: **"Ska jag posta granskningen på PR:en?"**

## 4. Post — as a comment
The review itself is in **English** and posted from the developer's account, so start the summary with
"Automated review by Claude (requested by @<login>)". Inline comments for findings tied to a line; one summary body.
```bash
gh pr review $ARGUMENTS --comment --body-file <summary.md>
```
For inline comments use the REST API (`gh api repos/{owner}/{repo}/pulls/$ARGUMENTS/reviews
-f event=COMMENT …` with `comments[]`). Every inline comment opens a thread that must be resolved before merge.

## Never
- `--approve` or `--request-changes`, merge, or push to the PR branch during a review.
- Review your own PR as if a human had: say plainly when the PR was written by the AI, so the owner reads it as a
  second opinion, not as an approval.
