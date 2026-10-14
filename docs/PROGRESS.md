# Progress log

Read at the start of every AI session (after `docs/PLAN.md`). Update at the end of every step.

**Next action:** Step 2 (model: Opus). Before starting, ask the owner to confirm the step-2 preparations listed in `docs/PLAN.md`.

| Step | Scope | Status |
|---|---|---|
| 1 | Foundation: repo, modules, version catalog, convention plugins, 4 environments, lint, CLAUDE.md | ✅ Done 2026-10-12 |
| 2 | Fastlane + secrets + signing (match, Play upload key) | ⏳ Next |
| 3 | CI/CD (GitHub Actions → later self-hosted Mac runner) | |
| 4 | Core: design system, Koin, Nav3, feature flags, observability (Firebase), localisation, domain/data | |
| 5 | Features: onboarding, login, tabbed logged-in shell | |
| 6 | Debug menu | |
| 7 | Tests: unit, integration, UI, screenshot, architecture + coverage | |
| 8 | Jira integration + AI workflow (spec → ticket → code → PR → AI review) | |
| 9 | Final summary & docs | |

## Step 1 — verified
- `./gradlew :app:android:assembleDevDebug allTests spotlessCheck detekt` → green
- iOS `RefProj-Sit` built for simulator and launched, showing "Environment: SIT"

## Open items / decisions pending
- Jira project key: placeholder `REF` until Jira is set up (step 8).
- App icons: added with the design system (step 4).
- detekt 1.23.x with Kotlin-compiler pinning; move to detekt 2.x when stable.
