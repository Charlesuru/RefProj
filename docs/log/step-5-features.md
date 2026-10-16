# Step 5 — Features (log)

- **Date:** 2026-10-16
- **Ticket:** REF-5 (placeholder key until Jira exists)
- **PRs (stacked, merge in order):** auth + consent → Room tasks data → app flow (onboarding, login, shell,
  profile) → tasks tab + home → this docs PR
- **Reference docs:** [features.md](../features.md), [ADR 0009](../adr/0009-local-database-room.md),
  [observability.md](../observability.md#when-data-is-collected), [design-system.md](../design-system.md)

## 1. Owner decisions (session start)
| Question | Decision |
|---|---|
| Spec (onboarding → login with fake backend → tabs Home / Tasks / Profile; one module per feature; shell in `app:shared`) | Approved as proposed |
| Local storage for tasks | **Room KMP** (new dependency, ADR 0009) |
| Analytics consent (open item from step 4) | **Opt-in on the last onboarding page**, changeable in Profile; Crashlytics stays on (release builds) |

## 2. What was built
| Area | Result |
|---|---|
| Domain | `auth` (User, AuthState, login with local validation, logout that deletes local tasks), `consent`, `tasks` (add/complete/delete, summary) |
| Data | `AuthRepositoryImpl` (session in `SecureStore`), `FakeAuthApi`, `ConsentRepositoryImpl`, Room `RefProjDatabase` + `TaskRepositoryImpl`, schema v1 committed |
| Build | Convention plugin `refproj.kmp.room` (KSP per target, schema export); Room 2.8.5, sqlite-bundled 2.7.1, KSP 2.3.12 |
| Features | `feature:onboarding`, `feature:login`, `feature:tasks`, `feature:profile`; `feature:home` rebuilt (greeting, counts) |
| App | `AppViewModel` derives the top-level destination from state; `MainShell` with one back stack per tab; screen-view analytics |
| Design system | 7 icons, `RefPagerIndicator`, `RefCheckboxListItem`, tokens `illustration`, `pagerIndicatorDot` |
| Observability | Analytics opt-in: `RestrictedAnalyticsTracker` (never in debug builds) + consent applied at startup; pseudonymous user ID while signed in |

Tests added (all labelled): UNIT — auth/task use cases, every ViewModel (onboarding, login, home, tasks, profile,
app), tab back stacks, restricted tracker; INTEGRATION — auth repository + fake backend + secure store, consent
repository, Room repository (iOS simulator), Koin graph incl. every ViewModel and the startup consent.

## 3. Problems and fixes
| Problem | Fix |
|---|---|
| A Room builder extension named `build()` was shadowed by Room's own member, so the bundled driver would silently not have been set (compiler warning) | Renamed to `buildWithDefaults()`; zero-warning policy caught it |
| Room needs a `Context` on Android, so database tests cannot run as Android host tests | Room integration test in `iosTest` (in `allTests`); the Koin graph test replaces `TaskRepository`; Android device tests in step 7 |
| The platform shells switched Analytics on in every release build, which would bypass consent | Shells no longer touch Analytics; shared code applies consent through `RestrictedAnalyticsTracker`. Re-applying at each start also overrides a value the Firebase SDK persisted from older builds |
| iOS crashed on launch in the unsigned simulator build: restoring the session read the Keychain, which failed with `-34018` and threw | `SecureStore` never throws any more (reads → `null`, writes → non-fatal error) on both platforms; `fastlane ios build` now signs ad hoc so the simulator has a keychain. Found only by running the app — unit tests use a fake store |
| detekt `ComplexCondition`/`ReturnCount` on session restore | Small `readStoredUser()` helper |
| Compose lint: `App()` emitted content without a `modifier` parameter | Added `modifier` and passed it down |

## 4. Verification
- `./gradlew spotlessCheck detekt allTests :app:android:assembleDevDebug` green on every branch of the stack.
- Android DEV on the emulator: onboarding (skip → consent page, opt in), wrong password → error, login, session and
  consent survive an app restart, back from Profile → Home, Tasks: add / complete, Home counts follow, logout with
  confirmation → login, tasks deleted after re-login.
- Android PROD release (R8): onboarding → login → add a task, no crash (Room keeps working after shrinking).
- iOS DEV in the simulator (`fastlane ios build`, ad-hoc signed): launches in Swedish without errors; Keychain usable.
  The iOS flow was not clicked through (no UI automation tool on this machine) — the shared code is the same as on
  Android and every ViewModel test runs on the iOS simulator.

## 5. Owner actions
See the step-5 checklist in [PROGRESS.md](../PROGRESS.md#step-5--owner-checklist).
