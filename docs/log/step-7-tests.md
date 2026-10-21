# Step 7 — Tests (log)

- **Date:** 2026-10-21
- **Ticket:** REF-11 (placeholder key until Jira exists)
- **PRs (stacked, merge in order):** #31 Kover → #32 architecture → #33 integration → #34 device tests → #35 UI →
  #36 screenshots → this docs PR
- **Reference docs:** [testing.md](../testing.md), [ADR 0011](../adr/0011-test-strategy.md)

## 1. Owner decisions (session start)
| Question | Decision |
|---|---|
| Spec REF-11 (unit, integration, UI, screenshot, architecture, coverage, docs) as stacked PRs | Approved |
| Where the Konsist tests live | New JVM module **`:tests:architecture`** |
| Tests that need a real device (Keystore, Room on Android) | **Nightly CI** on an emulator, not on every PR |
| Coverage thresholds | **Floors at the measured level per module**, ratcheted up |
| Model | Opus (plan said Sonnet; owner's choice) |

## 2. What was built
| Area | Result |
|---|---|
| Coverage | `refproj.coverage` + Kover in `refproj.kmp.library`; floors in `config/kover/coverage-floors.properties`; CI job summary table (`scripts/ci/coverage-summary.sh`) and HTML artifact; `koverVerify` in the gates |
| Architecture | `:tests:architecture` (Konsist, in `allTests`): layers, pure domain, ViewModel shape, no service locators, naming, test labels, design-system use and string resources in features |
| Platform tests | `PlatformTest` (`core:testing`): commonTest classes run under Robolectric on the Android host, as-is on iOS |
| Integration | Room repository test on both platforms; `DatabaseMigrationTest` over every committed schema with `ALL_MIGRATIONS`; real Ktor client → `DataError` chain; timeout mapping; Keychain store error contract |
| Device tests | `refproj.kmp.devicetest` (Gradle Managed Device, API 34 ATD): Android Keystore store, production Room setup; workflow **Device tests** (Mon–Fri 01:30 UTC, manual); `fastlane android device_test` |
| UI | `runComposeUiTest` screen tests for login, onboarding, tasks, profile, home; `AppFlowTest` end to end (DEV and PROD release) on both platforms; `<Feature>TestTags` |
| Screenshots | `ScreenCatalogScreenshotTest` (Roborazzi): every catalog sample × light/dark/Swedish = 75 reference images, verified on every run |

Totals after the step: 53 labelled test classes (32 unit, 10 integration, 6 UI, 1 screenshot, 4 architecture);
a local run executes 229 tests on the Android host, 157 on the iOS simulator and 18 architecture tests, plus 5 on a
device. Total line coverage 89 % (was 80 %).

## 3. Problems and fixes
| Problem | Fix |
|---|---|
| `:data=0` in the floors file was read as an empty key | A colon ends a key in `.properties`; keys use dots (`core.ui`) |
| Konsist's `assertArchitecture` missed a feature importing `data` and another feature | Own import/qualified-name check per layer; every rule mutation-tested against a deliberate violation |
| Robolectric failed on JDK 25 (`jdk.internal.access` not exported) | `--add-exports` and `--enable-native-access` for host tests in the convention plugin |
| Bundled SQLite cannot load under Robolectric | Host tests use the framework driver; the production driver runs on iOS and in the device tests |
| `MigrationTestHelper` (native) found no schema | It appends the database class name itself; pass the parent `schemas` directory |
| A mutation check left a generated `2.json` behind, and `git checkout` reverted wanted changes | Cleaned up and reapplied; lesson: revert mutations file by file |
| iOS Keychain unavailable in the simulator test process (`-25291`) | Contract tested with a failing/working keychain; real Keychain listed as a known gap |
| Host tests could only enable `withHostTest` once | `isIncludeAndroidResources = true` set in `refproj.kmp.library` for every module |
| Controlled text fields reset when the test's state did not follow the input | Tests hold the state in `mutableStateOf` |
| The second `AppFlowTest` found no screens (global Koin restarted between tests) | Each test gets its own `koinApplication` through `KoinIsolatedContext` |
| End-to-end test could not read other modules' (internal) strings; local simulator runs in Swedish | Stable test tags on the controls the flow uses |
| Screenshot recording hung for 10 minutes | Endless animations kept Robolectric producing frames; the Compose clock is paused and advanced by a fixed time |
| Images landed in the module root; the Swedish variant was English | Explicit reference path; set the JVM default locale as well as the Robolectric qualifier |
| `TaskRepositoryImplTest` failed once on CI (#34): two quick inserts arrived as one Room emission | The test waits for each expected state instead of counting emissions (fix on #33) |
| Dependency review: FreeMarker 2.3.32 (GHSA-27j2-h3m2-8237, critical) via the Kover Gradle plugin | Added to the `build-tool-security-pins` bundle (2.3.35). The PR comment from the first run stays on #31 after the fix; the later review run is green |

## 4. Verification
- `./gradlew spotlessCheck detekt allTests koverVerify :app:android:assembleDevDebug` green on every branch.
- Device tests 5/5 on the local emulator and on the managed device.
- Mutation checks: every architecture rule, a too-high coverage floor, a schema change without migration, a
  swapped reference image — each fails as expected.
- Android DEV (emulator): onboarding → pre-filled login → Home; a task survives an app restart.
- iOS DEV (simulator): launches with the badge, no crash; the flow itself runs as `AppFlowTest` on the simulator.
- CI: every PR green; the screenshot references recorded on macOS match Linux CI; UI tests and `AppFlowTest` pass on
  CI's iOS simulator.

## 5. Owner actions
See the step-7 checklist in [PROGRESS.md](../PROGRESS.md#step-7--owner-checklist).
