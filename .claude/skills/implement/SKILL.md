---
name: implement
description: Implement an approved Jira ticket end to end — branch, code, tests, quality gates, commits and a pull request from the developer's account. Use when the user says "implement REF-123", "/implement REF-123" or asks to build a ticket.
argument-hint: "REF-<n>"
disable-model-invocation: true
---

# /implement — ticket → branch → code → PR

Implement Jira ticket `$ARGUMENTS`. You write the code; **the developer owns it** — the PR goes out from their account — and a human merges (CLAUDE.md §3).

## 1. Read the ticket
- Atlassian MCP: fetch `$ARGUMENTS` (summary, description, acceptance criteria, labels, status, comments).
- Stop and ask (in Swedish) when: the ticket does not exist; it has no acceptance criteria; it needs a new module,
  dependency or architecture change not yet decided; it needs a secret or console access (CLAUDE.md §4).
- Restate the plan in a few lines: modules touched, flag, tests per acceptance criterion. Ask only if something is
  genuinely open — the ticket was approved already.
- Move the ticket to **In Progress** (Atlassian MCP transition) once the plan stands.

## 2. Branch
```bash
git switch main && git pull --ff-only
git switch -c <type>/$ARGUMENTS-<short-description>      # lower-case words joined by '-'
```
Type from the ticket: Story → `feat`, Bug → `fix`, Task → `chore`/`build`/`ci`/`docs`/`refactor`/`test` as fits.

## 3. Build it
- Follow CLAUDE.md §1–2 and `docs/architecture.md`: layers, one ViewModel per screen, Koin, strings in
  `composeResources` (EN + SV), design-system tokens, feature flag OFF in PROD for unfinished user-facing work.
- Every acceptance criterion gets a test; label each test class (`// UNIT TEST`, …). Raise coverage floors you beat
  (`config/kover/coverage-floors.properties`), never lower them.
- `TODO`/`FIXME` reference a ticket: `// TODO(REF-123): …`.

## 4. Gates — all green before any commit
Run Gradle outside Claude Code's sandbox: a sandboxed daemon quarantines the iOS test binary
(`docs/log/step-6-debug-menu.md`).
```bash
./gradlew spotlessApply
./gradlew spotlessCheck detekt allTests koverVerify :app:android:assembleDevDebug
bundle exec fastlane ios build env:dev
```
User-facing change? Launch the app on **both** platforms and walk through the acceptance criteria — tests with
fakes have missed launch crashes before.

## 5. Commit and push
Small, self-contained Conventional Commits (each must stand on its own — `main` uses rebase-merge):
```bash
git commit -m "<type>(<scope>): <summary> [$ARGUMENTS]"   # + Co-Authored-By: Claude trailer
git push -u origin <branch>
```
Never `--no-verify`; a failing hook is fixed, not skipped.

## 6. Open the pull request
- Title `$ARGUMENTS: <summary>`; body from `.github/pull_request_template.md` with every section filled in: the
  ticket link, what and why, **how it was tested** (commands, devices), the checklist ticked honestly, notes for the
  reviewer. End with the Claude Code attribution line.
- Before opening it, show the developer the commits and the diff summary; they are the author and read it first.
- `gh pr create --base main --title "…" --body-file <file>`
- Optional deeper review: tell the owner they can add the label **`ai-review`** (it uses their Claude quota).
- Move the ticket to **In Review** and comment the PR link on it (Atlassian MCP).

## 7. Hand over
Watch CI (`gh pr checks <n> --watch` in the background). Green → tell the owner (in Swedish) the PR number, what to
look at, and what to test manually. Red → fix, push again.

## Never
- Approve, merge, enable auto-merge, or use `gh pr merge --admin`.
- Push to `main`, force-push someone else's branch, or commit without the `Co-Authored-By: Claude` trailer.
- Mark acceptance criteria as done that were not verified.
