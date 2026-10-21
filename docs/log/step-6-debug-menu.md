# Step 6 — Debug menu (log)

- **Date:** 2026-10-21
- **Ticket:** REF-10 (placeholder key until Jira exists; REF-6–9 were taken by smaller tickets between steps)
- **PRs (stacked, merge in order):** foundations (domain, data, logging) → `feature:debug` → screen catalog → this docs PR
- **Reference docs:** [debug-menu.md](../debug-menu.md), [ADR 0010](../adr/0010-debug-menu.md),
  [feature-flags.md](../feature-flags.md), [observability.md](../observability.md#logging)

## 1. Owner decisions (session start)
| Question | Decision |
|---|---|
| Spec REF-10 (flags, environment, state jumps, pre-filled test user, log viewer, crash test, clear data, screen catalog) | Approved as proposed, four stacked PRs |
| Entry point | **Tab on the screen edge with the environment name**, on every screen incl. onboarding and login |
| Keeping it out of PROD | **Runtime gate** (`isDebugMenuEnabled`), not a separate PROD binary — ADR 0010 |

## 2. What was built
| Area | Result |
|---|---|
| Domain | `Credentials` (password hidden in `toString`), `TestAccountProvider`, `GetTestAccountUseCase`, `ResetOnboardingUseCase`, `SignInWithTestAccountUseCase` (login first, then onboarding, so the app goes straight to the tabs), `ClearLocalDataUseCase` |
| Data | The fake backend's test account, bound as `TestAccountProvider` only while the debug menu is enabled |
| Observability | `LogBuffer` (Kermit writer, last 500 lines, memory only) and `LoggingAnalyticsTracker`, both created only with the debug menu enabled |
| Login | Form pre-filled with the test account when there is one |
| `feature:debug` | Debug menu (environment, flags, app state, tools), log viewer with level filter, screen catalog + preview container, environment badge |
| Catalog | `ScreenPreview` / `ScreenPreviewGroup` in `core:ui`; every feature binds sample states; design system gallery (colours, type, buttons, inputs, lists, states) |
| Navigation | `DebugRoute`, `DebugLogsRoute`, `DebugCatalogRoute`, `DebugPreviewRoute`, all `DebugDestination` (no badge, no analytics screen views) |
| Design system | `RefFilterChip` |

Tests added (all labelled): UNIT — debug use cases, test account, `LogBuffer`, `LoggingAnalyticsTracker`, `DebugViewModel`,
`DebugLogsViewModel`, `ScreenCatalog`, login pre-fill; INTEGRATION — `AppGraphTest`: debug tools present in DEV/SIT/STAGE and
in a PROD debug build, **absent in a PROD release build** (no test account, no log buffer, no debug routes), every
feature in the catalog; the fake backend accepts the test account.

## 3. Problems and fixes
| Problem | Fix |
|---|---|
| macOS showed *"test.kexe" Not Opened* and the iOS simulator tests hung | The Gradle daemon had been started inside Claude Code's sandbox, which marks every file it writes with `com.apple.quarantine`; Gatekeeper then blocks the Kotlin/Native test binary. Stopped the daemon, removed the attribute, rebuilt the binary and ran Gradle outside the sandbox. Not a project problem — local tooling only |
| The device description (`android.os.Build`) cannot run in JVM host tests | `EnvironmentInfo` is built in the Koin module and injected; the ViewModel never calls platform code |
| detekt `MagicNumber` in the log time format; ktlint `multiple-emitters-check` in the design system gallery | kotlinx-datetime's `LocalTime.Format` DSL; every gallery section wrapped in one `Column` |
| `core:observability` exposed `StateFlow` without declaring coroutines | `api(libs.kotlinx.coroutines.core)` |

## 4. Verification
- `./gradlew spotlessCheck detekt allTests :app:android:assembleDevDebug` green on every branch of the stack.
- Android DEV (emulator): badge on onboarding, login and the tabs; flag override hides the home build info;
  *Sign in as test user* from onboarding lands on Home; log viewer shows auth and analytics lines; *Clear all data*
  returns to onboarding; login is pre-filled; *Crash the app* ends the process with `DebugTestCrashException`;
  screen catalog lists every feature and renders a sample without a doubled status bar.
- Android PROD release (R8): no badge, login fields empty.
- iOS DEV (simulator, `fastlane ios build`): launches with the badge, no crash. The iOS flow was not clicked through
  (no UI automation on this machine); the shared code and every ViewModel test also run on the iOS simulator.

## 5. Owner actions
See the step-6 checklist in [PROGRESS.md](../PROGRESS.md#step-6--owner-checklist).
