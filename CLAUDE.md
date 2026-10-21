# CLAUDE.md — Rules for AI agents working in this repository

This is an **enterprise reference project**: a Compose Multiplatform (Android + iOS) app.
Every change must meet production standards. When a shortcut and the correct approach differ,
take the correct approach — or stop and ask.

Communicate with the user in **Swedish**. Write code, comments, commit messages, PRs and docs in **English**.

## 0. Session start (always — the user runs `/clear` between steps)
1. Read `docs/PLAN.md` (requirements, owner decisions, step-by-step plan, session protocol).
2. Read `docs/PROGRESS.md` (what is done, what is next, open items).
3. Continue with the next step. Do not re-ask questions already answered in `docs/PLAN.md`.
4. At the end of a step: verify, update `docs/PROGRESS.md`, commit, and tell the user which
   **model** to use next (`/model`) and what they need to prepare.

## 1. Non-negotiable rules

### Security & secrets
- **Never** commit secrets: keys, tokens, passwords, keystores, certificates, provisioning profiles,
  `google-services.json`, `GoogleService-Info.plist`, service-account JSON. See `.gitignore`.
- Secrets live in exactly three places:
  1. **CI:** GitHub Environments secrets (`dev`, `sit`, `stage`, `prod`) — PROD requires approval.
  2. **Local:** `~/.gradle/gradle.properties`, `local.properties`, `iosApp/Configs/Local.xcconfig`, `fastlane/.env` (all gitignored).
  3. **Signing (iOS):** fastlane match, encrypted git repo.
- Never print, log, echo or paste a secret value — not in code, logs, commit messages, PRs or chat.
- Never embed real secrets in the app binary. `AppConfig`, `BuildConfig` and xcconfig values are **public**.
- If a task needs a secret you don't have: **stop** and ask the user, with exact instructions.
- Secrets inventory and rotation: `docs/secrets.md`. A new secret updates that file, `.gitignore` and `.gitleaks.toml`.
- Never disable security features (TLS validation, R8, backup exclusion, secret scanning) to make something work.

### Architecture (see `docs/architecture.md`)
- **Clean Architecture + MVVM + unidirectional data flow.**
- Dependency direction: `app → feature → domain ← data`, and everything may depend on `core`.
- `feature:*` must **never** depend on another `feature:*` or on `data`. Navigate via route keys in `core:navigation`.
- `domain` is pure Kotlin: no Compose, no Android/iOS APIs, no Ktor.
- One ViewModel per screen. It exposes a single `StateFlow<UiState>` and accepts `UiAction`s; one-off events via a `Channel`/`Flow`.
- All dependencies via **Koin** constructor injection. No service locators inside business logic.
- Put shared code in `commonMain`. Use `expect/actual` or interfaces + DI for platform specifics.

### Feature flags & trunk-based development
- We use **trunk-based development**: short-lived branches (< 2 days), small PRs, merge to `main` often.
- Unfinished work is merged **behind a feature flag** that defaults to **OFF in PROD**.
- Remove a flag (and the dead branch) once the feature has been fully rolled out.

### Build & dependencies
- All versions in `gradle/libs.versions.toml`. Never hardcode a version in a build script.
- Module build logic goes in `build-logic/` convention plugins, not copy-pasted between modules.
- New libraries need a justification in the PR (license, maintenance, size, KMP support).
- Never commit the generated Xcode project. Edit `iosApp/project.yml`, then run `xcodegen generate`.

## 2. Quality gates (must pass before every commit)

```bash
./gradlew spotlessApply          # auto-format
./gradlew spotlessCheck detekt   # lint — zero findings allowed
./gradlew allTests               # unit + integration tests (Android host + iOS simulator)
./gradlew koverVerify             # coverage floors per module (config/kover/coverage-floors.properties)
./gradlew :app:android:assembleDevDebug
```

- No new warnings. No `@Suppress` without a comment explaining why.
- `TODO`/`FIXME` must reference a Jira ticket: `// TODO(REF-123): ...`.
- New logic requires tests. Mark the test type at the top of every test class:
  `// UNIT TEST`, `// INTEGRATION TEST`, `// UI TEST`, `// SCREENSHOT TEST`, `// ARCHITECTURE TEST`.
- User-facing strings go in `composeResources/values*/strings.xml` (English default + Swedish). Never hardcode UI text.
- UI uses design-system components and tokens from `core:designsystem` — no raw colors or dp magic numbers in features.

## 3. Workflow: spec-driven, AI-assisted, human-decided

1. **Ticket first.** Every change starts from a Jira ticket (`REF-123`) with a spec and acceptance criteria.
   If none exists, draft one and ask the user to approve it before coding.
2. **Branch:** `<type>/REF-123-short-description`, e.g. `feat/REF-42-login-screen`.
3. **Commits:** Conventional Commits with the ticket key:
   `feat(login): add biometric unlock [REF-42]`
   Types: `feat, fix, refactor, perf, test, docs, build, ci, chore`.
4. **PR:** title `REF-123: <summary>`, use the PR template, link the ticket, list how it was tested.
5. **AI review** runs on every PR; a **human approves and merges**. Never merge your own PR.
6. Never push directly to `main`. Never force-push to shared branches. Never skip hooks (`--no-verify`).

## 4. When to stop and ask

Stop and ask the user (in Swedish, with step-by-step instructions) when:
- A secret, credential, certificate or account access is needed.
- A manual step is required in an external console (Apple, Google Play, Firebase, GitHub, Jira).
- A decision changes architecture, adds a module, or adds a dependency.
- Requirements are ambiguous or a ticket's acceptance criteria are missing.

## 5. Project map

| Path | What |
|---|---|
| `app/android` | Android shell: flavors `dev/sit/stage/prod`, `Application`, `MainActivity` |
| `app/shared` | KMP umbrella: root UI, DI graph, navigation host. Exported to iOS as `Shared.framework` |
| `iosApp/` | iOS shell. `project.yml` (XcodeGen), `Configs/*.xcconfig` per environment |
| `core/*` | Cross-cutting infrastructure (config, designsystem, navigation, network, …) |
| `domain` | Use cases + business models (pure Kotlin) |
| `data` | Repositories, API clients, persistence |
| `feature/*` | One module per user-facing feature |
| `build-logic/` | Gradle convention plugins (`refproj.*`) |
| `fastlane/` | Build/test/upload lanes for both platforms — `bundle exec fastlane lanes` |
| `.github/` | CI/CD workflows, Dependabot, CODEOWNERS, PR template, repo settings as code — see `docs/ci-cd.md` |
| `.githooks/`, `scripts/` | Git hooks (gitleaks, format, commit-msg), `setup.sh`, CI helpers (`scripts/ci`), GitHub setup (`scripts/github`) |
| `.claude/` | Claude Code agents: `repo-guide` answers questions about the repo, read-only (`claude --agent repo-guide`) |
| `config/detekt` | Static analysis rules |
| `docs/` | Architecture, ADRs, runbooks, progress log |

Progress and next steps: `docs/PROGRESS.md`. Read it at the start of every session.
