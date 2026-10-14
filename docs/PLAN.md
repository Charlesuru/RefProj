# Build-out plan

The master plan for building this reference project. Written so that a fresh AI session
(after `/clear`) has full context. Status per step lives in `docs/PROGRESS.md`.

## Purpose
Show what a **complete enterprise mobile setup** looks like end to end: source code, CI/CD, GitHub, Jira,
and an AI-driven (but human-decided) development workflow. What the app *does* is secondary — the setup is the point.

## Owner's requirements (original brief, condensed)
- Compose Multiplatform, Android + iOS. MVVM + Clean Architecture. Multi-module (`app/core/domain/data/feature`, more as needed; one module per feature).
- 4 environments: DEV, SIT, STAGE, PROD.
- Secrets/keys handled securely and robustly. Correct `.gitignore`.
- Fastlane so both the owner and AI can build locally.
- Full CI/CD with GitHub Actions. Phase 1: GitHub-hosted runners. Phase 2: owner's Mac as self-hosted runner (to save GitHub minutes).
- Dependency injection (Koin). Navigation 3.
- Feature flags: code can be merged, but features ship in release only when their flag is on.
- Debug menu: manage feature flags, jump straight into states (e.g. logged-in), default test user pre-filled on login, preview screens in-app, plus other standard enterprise debug tools.
- App flow: onboarding, login, logged-in mode = tab bar with a few tabs.
- Tests: unit, integration, UI (+ screenshot, architecture). Every test clearly marked by type.
- Observability (Firebase Crashlytics/Analytics + structured logging). Localisation (English + Swedish). Design system.
- Enterprise linting.
- Jira connected to the repo: tickets linked to commits/branches/PRs for full traceability.
- AI-driven, spec-driven workflow: AI writes tickets → AI implements → AI commits → AI reviews PRs → **humans decide and merge**.
- Trunk-based development.
- Final summary describing everything, including the CI/CD pipeline, test types and observability.

## Owner decisions (already answered — do not re-ask)
| Topic | Decision |
|---|---|
| GitHub | Private repo under `https://github.com/Charlesuru` → `Charlesuru/RefProj` |
| Accounts | Owner HAS Apple Developer Program and Google Play Console accounts |
| Observability | Firebase (Crashlytics + Analytics) |
| IDs | Bundle ID / applicationId `com.charlesuru.refproj` (+ `.dev`, `.sit`, `.stage`). Code package `com.charlesuru.refproj` |
| Approach | Step by step; owner reviews after each step |
| Jira key | Placeholder `REF` until Jira exists (step 8) |

## How we work (session protocol)
1. Each step runs in its **own session**. Owner runs `/clear` between steps and types "kör steg N".
2. At session start: read `CLAUDE.md` (auto-loaded), `docs/PLAN.md`, `docs/PROGRESS.md`.
3. At step end: verify (build/test/lint), update `docs/PROGRESS.md`, commit, then tell the owner:
   a summary in Swedish, which **model** to use for the next step, and anything they must prepare.
4. **Tell the owner when to switch model** (`/model`) — they cannot be expected to remember. Also say when context is getting large and a `/clear` is wise.
5. Blocked on a secret, account or console action → stop and give exact, numbered instructions in Swedish.

## Steps

### Step 1 — Foundation ✅ (model: Opus)
Repo, modules, version catalog, convention plugins, 4 environments (Android flavors, iOS xcconfig + XcodeGen), lint, CLAUDE.md, docs.

### Step 2 — Fastlane, secrets, signing (model: Opus)
- `Gemfile` + `fastlane/` with lanes for Android and iOS: `build`, `test`, `beta` (Firebase App Distribution / TestFlight / Play internal), `release`, parametrised by environment.
- Android release signing from env vars / `~/.gradle/gradle.properties` (never committed). Upload key + Play App Signing.
- iOS signing with **fastlane match** (separate private repo, e.g. `Charlesuru/refproj-certificates`, encrypted). App Store Connect API key (.p8) for CI.
- `docs/secrets.md`: inventory of every secret, where it lives (local / GitHub Environment), how to rotate.
- Secret scanning: gitleaks (pre-commit + CI). Pre-commit hooks (format, lint, commit-msg Jira check).
- Version numbering strategy (versionName from tag, versionCode/build number from CI run).
- Owner prepares: `gh auth login`, Apple Team ID, cert repo, App Store Connect API key, Play service account, apps created in App Store Connect / Play Console.

### Step 3 — CI/CD (model: Opus)
- GitHub Actions: PR checks (lint, detekt, tests, build both platforms), `main` → DEV/SIT builds, tag → STAGE/PROD with manual approval via GitHub Environments.
- Caching, concurrency cancellation, SARIF upload (detekt), dependency review, Dependabot/Renovate, CODEOWNERS, branch protection, PR template.
- Phase 2 in same step or follow-up: self-hosted Mac runner (setup instructions + `runs-on` switch).

### Step 4 — Core (model: Sonnet)
- Modules: `domain`, `data`, `core:navigation`, `core:network` (Ktor), `core:featureflags`, `core:observability` (Kermit → Crashlytics), `core:storage`, `core:testing`, `core:ui`.
- Full design system (tokens: color, type, spacing, shape; components), app icons per environment.
- Koin DI graph, Nav3 back stack + serializers for iOS, localisation (EN + SV), Firebase wiring (native SDK on each platform, bridged via interface).
- Feature flags: defaults per environment, PROD default OFF, overridable in debug menu, ready for Remote Config.

### Step 5 — Features (model: Sonnet)
App concept: a simple personal task/habit tracker. Onboarding → login (fake auth backend, session persisted) → tab shell (e.g. Home, Tasks, Profile). One module per feature.

### Step 6 — Debug menu (model: Sonnet)
`feature:debug`, only in non-PROD/debug builds: feature flag toggles, environment info, jump to state (logged in / onboarding), pre-filled test user, in-app screen catalog/preview, log viewer, crash test button, clear data.

### Step 7 — Tests (model: Sonnet)
Unit (commonTest), integration (repositories + Ktor MockEngine, Koin graph check), UI (Compose UI tests), screenshot (Roborazzi), architecture (Konsist), coverage (Kover) + thresholds in CI. Each test class labelled with its type.

### Step 8 — Jira + AI workflow (model: Opus)
Jira Cloud project (owner creates), GitHub for Jira app, smart commits/branch naming, commit-msg hook enforcing ticket key, PR template linking tickets. AI workflow: Claude slash commands / skills for "spec → ticket", "implement ticket", "review PR"; Claude GitHub Action for automated PR review; human approval required to merge.

### Step 9 — Summary (model: Opus)
Complete write-up: architecture, modules, environments, secrets, CI/CD pipeline diagram, test pyramid, observability, workflow. Final review of the whole repo.
