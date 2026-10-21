# Debug menu (`feature:debug`)

Tools for developers and testers inside the app. Available in **every non-PROD environment and in every debug
build; never in a PROD release build** (`AppConfig.isDebugMenuEnabled`). Decision and alternatives: [ADR 0010](adr/0010-debug-menu.md).

## Opening it
Tap the tab with the environment name (`DEV`, `SIT`, `STAGE`) on the left edge of any screen, including onboarding
and login. It is hidden while the debug menu itself is open. A PROD debug build shows `PROD`.

## What it contains
| Section | Item | What it does |
|---|---|---|
| Environment | Environment, version, build type, API base URL, device | Public build facts, for bug reports |
| Feature flags | One switch per flag, with its source (default / remote / override) | A switch stores an override on this device; *Remove override* returns to the remote or default value. See [feature-flags.md](feature-flags.md) |
| App state | Show onboarding | Marks onboarding as not done; the session is kept |
| | Sign in as test user / Sign out | Signs in with the environment's test account and skips onboarding — from any screen straight to the tab shell |
| | Clear all data | After a confirmation: tasks, session, onboarding, analytics consent and flag overrides — the state of a fresh install |
| Tools | Screen catalog | Every feature's screens in sample states, plus the design system's tokens and components |
| | Logs | The last 500 log lines of this session (memory only), newest first, filtered by level. Includes every analytics call, since debug builds never send analytics |
| | Send a non-fatal error | Reports `DebugNonFatalException` to the crash reporter |
| | Crash the app | After a confirmation: throws `DebugTestCrashException`. Release builds (SIT, STAGE) report it to Crashlytics on the next start |

The login form is **pre-filled** with the test account (`test@example.com` / `Password1`, public demo values of the
fake backend — [features.md](features.md#session-and-authentication)).

## How it is wired
| Piece | Where |
|---|---|
| `debugModule` (ViewModels, routes, catalog), added only while the menu is enabled | `feature/debug`, `app:shared/di/AppGraph.kt` |
| `DebugMenuBadge`, drawn over the root `NavDisplay` | `feature/debug`, `app:shared/App.kt` |
| Routes `DebugRoute`, `DebugLogsRoute`, `DebugCatalogRoute`, `DebugPreviewRoute` (all `DebugDestination`: no badge, no analytics screen views) | `core:navigation` |
| `SignInWithTestAccountUseCase`, `ClearLocalDataUseCase`, `ResetOnboardingUseCase`, `GetTestAccountUseCase` | `domain` |
| `TestAccountProvider`: the fake backend's account, `null` when the menu is disabled | `data/DataModule.kt` |
| `LogBuffer` (Kermit writer, memory only) and `LoggingAnalyticsTracker` | `core:observability`, created in `startApp` |
| `ScreenPreview` / `ScreenPreviewGroup` | `core:ui` |

State shortcuts never navigate. They change the app's state, `AppViewModel` picks the new top-level destination, and
the root back stack starts over — which also closes the menu.

## Adding a sample screen to the catalog
In the feature, next to the screen:
```kotlin
internal val tasksPreviews = ScreenPreviewGroup(
    title = "Tasks",
    previews = listOf(ScreenPreview("Empty") { TasksScreen(state = TasksUiState(isLoading = false), onAction = {}) }),
)
```
and bind it in the feature's Koin module: `single<ScreenPreviewGroup>(named("tasks")) { tasksPreviews }`.
Screens must be stateless (state in, actions out) to be previewable. Preview names are developer-facing labels in
English, like flag descriptions; they are not translated.

## Rules
- A new debug tool goes into `debugModule` or checks `AppConfig.isDebugMenuEnabled`; extend `AppGraphTest` for it.
- Never put a real secret into debug code: it ships (unreachable) in the PROD binary. Real test accounts must come from
  a non-PROD source per environment.
- New data stored on the device must be added to `ClearLocalDataUseCase`.
