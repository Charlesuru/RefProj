# Testing

How the app is tested, where each kind of test lives, and how to run, write and maintain them.
Decision record: [ADR 0011](adr/0011-test-strategy.md). Quality gates: `CLAUDE.md` §2.

## Test pyramid

| Type | Label | What it checks | Where | Runs on | When |
|---|---|---|---|---|---|
| Unit | `// UNIT TEST` | One class: use cases, ViewModels, mappers, stores with fakes | `commonTest` of each module | Android host (JVM) + iOS simulator | every PR |
| Integration | `// INTEGRATION TEST` | Real collaborators together: Room, Ktor client + error mapping, the Koin graph, migrations | `commonTest`, `iosTest` | Android host (Robolectric) + iOS simulator | every PR |
| Integration (device) | `// INTEGRATION TEST` | What only a real Android runtime can check: Keystore, bundled SQLite, database files | `androidDeviceTest` | Android emulator (Gradle Managed Device) | nightly |
| UI | `// UI TEST` | Screens as a user sees them; the whole app flow end to end | `commonTest` | Android host (Robolectric) + iOS simulator | every PR |
| Screenshot | `// SCREENSHOT TEST` | Every catalog screen state pixel for pixel: light, dark, Swedish | `app/shared/src/androidHostTest` | Android host (Robolectric, Roborazzi) | every PR |
| Architecture | `// ARCHITECTURE TEST` | Layer rules, conventions, test labels, design-system use | `tests/architecture` | JVM (Konsist) | every PR |

Coverage (Kover) is measured on the Android host run and enforced per module ([Coverage](#coverage)).

Every test class carries its label on the line above the class (KDoc and annotations may sit between). The
architecture tests fail a class without one.

## Running tests

```bash
./gradlew allTests                       # everything that runs on every PR: Android host, iOS simulator, architecture
./gradlew koverVerify                    # coverage floors (runs the Android host tests first)
./gradlew :feature:login:allTests        # one module
./gradlew :data:testAndroidHostTest --tests '*TaskRepositoryImplTest*'
bundle exec fastlane test                # the full local gate: format, lint, all tests, coverage
bundle exec fastlane android device_test # on-device tests on a managed emulator (downloads ~1 GB the first time)
ANDROID_SERIAL=emulator-5554 ./gradlew :data:connectedAndroidDeviceTest  # on a running emulator
```

Reports: `<module>/build/reports/tests/`, coverage `build/reports/kover/html/`, screenshot diffs
`app/shared/build/outputs/roborazzi/`.

> Run Gradle outside any sandbox that quarantines files (macOS): a quarantined Kotlin/Native test binary
> (`test.kexe`) is blocked by Gatekeeper and the iOS tests hang.

## Writing tests

### Shared tests and `PlatformTest`
Tests live in `commonTest` and run on both platforms. A test that needs the platform runtime (an Android `Context`,
Room, Compose UI, Android resources) extends **`PlatformTest`** (`core:testing`): on the Android host it runs under
Robolectric, on iOS it runs as-is. Plain logic tests do not extend it (Robolectric adds start-up time).

Shared test doubles live in `core:testing` (`FakeSecureStore`, `FakeAnalyticsTracker`, `testAppConfig()`,
`runViewModelTest`, …). ViewModel tests use `runViewModelTest`, which replaces `Dispatchers.Main`.

### UI tests
Compose Multiplatform's `runComposeUiTest` in `commonTest`; one test class per screen (`<Screen>Test`), driving the
stateless screen composable with a `UiState` and collecting the `UiAction`s it sends.

- Find nodes by what the user sees: the text from string resources (`getString(Res.string.…)` inside the test, so
  the test works in every language), content descriptions, roles.
- Controlled inputs (text fields) need a state that follows the input — hold it in `mutableStateOf` in the test.
- **Test tags** only where a test crosses module boundaries and cannot read the other module's resources:
  `AppFlowTest` uses `<Feature>TestTags` (`LoginTestTags.SUBMIT`, …). They are stable automation IDs, never shown
  to users, and independent of the device language.
- `AppFlowTest` (`app:shared`) runs the real Koin graph, navigation and fake backend end to end, each test in an
  isolated Koin context. Only device storage and the database are replaced.

### Screenshot tests
`ScreenCatalogScreenshotTest` renders **every sample state of the screen catalog** — the previews each feature
binds as its `ScreenPreviewGroup`, plus the design system gallery — in three variants (light, dark, Swedish light)
on a 360×780 dp phone. A new sample state in a `*Previews.kt` file is screenshot-tested automatically.

- Every test run compares against `app/shared/src/androidHostTest/screenshots/*.png`
  (`roborazzi.test.verify=true` in `gradle.properties`).
- After an **intended** UI change, record and commit the new images, and look at them in the PR diff:
  ```bash
  ./gradlew :app:shared:recordRoborazziAndroidHostTest
  ```
- A failed comparison writes `*_actual.png` and `*_compare.png` to `app/shared/build/outputs/roborazzi/`; CI uploads
  them as the `test-reports-android` artifact.
- The Compose clock is paused (`mainClock.autoAdvance = false`) and advanced by a fixed time, so loading
  indicators and other endless animations are frozen in a deterministic frame.
- Robolectric renders with its own bundled graphics stack, so images recorded on macOS match Linux CI. If a
  platform difference ever appears, record from CI's artifact instead of locally.

### Integration tests
- **Room:** `inMemoryTestDatabase()` (`data` commonTest). On iOS it is the production configuration (bundled
  SQLite); on the Android host Robolectric cannot load the bundled native library, so it uses the framework driver.
  The production driver on Android is covered by the device tests.
- **Migrations:** `DatabaseMigrationTest` (iOS simulator) creates a database from **every committed schema** in
  `data/schemas` and migrates it to `RefProjDatabase.VERSION` with `ALL_MIGRATIONS`. Changing an entity:
  1. bump `RefProjDatabase.VERSION` — the build exports `data/schemas/…/<version>.json` (commit it);
  2. add a `Migration(old, new)` to `ALL_MIGRATIONS` (or an `@AutoMigration`);
  3. the test now covers the new path; it fails if the migration is missing or produces the wrong schema.
- **Network:** `HttpClientTest` (core:network) checks the real client configuration against Ktor's `MockEngine`;
  `RemoteErrorChainTest` (data) follows a failure from the engine to the `DataError` a use case sees.
- **Koin graph:** `AppGraphTest` builds the real graph per environment, resolves every entry point and ViewModel and
  proves that a PROD release build has no debug tools.

### On-device tests
Modules that apply `refproj.kmp.devicetest` (`core:storage`, `data`) have `src/androidDeviceTest`, run with
AndroidJUnit4 on a **Gradle Managed Device** (Pixel 6, API 34, ATD image): Gradle downloads the image, starts and
stops the emulator. The **Device tests** workflow runs them Monday–Friday at 01:30 UTC and on demand; a red run
e-mails the workflow owner and is treated like a broken `main`. They are not part of pull request CI: emulators add
10+ minutes and are the main source of flaky CI.

### Architecture tests
`tests/architecture` (Konsist, JVM) parses every module's sources. Rules:

| Test | Rule |
|---|---|
| `LayerDependencyTest` | `app → feature → domain ← data`, every layer may use `core`; checked on imports, fully qualified names and the Gradle dependencies of features and `domain`; `domain` is pure Kotlin; features use no data-source libraries; no `KoinComponent` |
| `ConventionsTest` | ViewModels extend a ViewModel base, live in a feature or `app`, expose no mutable state; use cases live in `domain` and are `operator fun invoke`; repository interfaces in `domain`, `internal` implementations in `data` |
| `TestLabelTest` | every test class is labelled with its type |
| `FeatureUiTest` | features use no raw colours, no dp/sp literals and no hard-coded UI text (`*Previews.kt` may use sample text) |

Each rule was checked against a deliberate violation when it was written. A new rule must be too.

## Coverage
Kover instruments every KMP module and measures the Android host tests (Kotlin/Native has no coverage agent, so
iOS-only code is not measured). `./gradlew koverVerify` checks one floor per module and one for the total, all in
`config/kover/coverage-floors.properties` — a module without a floor fails the build.

- Floors sit about 2 points under the measured value. **Ratchet:** when a PR raises coverage, raise the floor in the
  same PR; never lower a floor without a reason in the PR.
- Excluded: `@Composable` and `@Preview` functions (covered by UI and screenshot tests, not by a percentage),
  generated code (Compose resources, Room implementations) and `core:testing`.
- CI writes a per-module table to the job summary of **Test (Android host)** and uploads the HTML report
  (`coverage-report` artifact).

## Known gaps
| Gap | Why | Covered instead by |
|---|---|---|
| iOS Keychain against the real Keychain | The simulator test process has no app host or keychain (`-25291`) | `KeychainSecureStoreTest` with a failing/working keychain; running the app |
| iOS on a device / XCUITest | No iOS UI automation in CI yet | Compose UI tests on the iOS simulator (same UI code) |
| Coverage of iOS-only code | No Kotlin/Native coverage tool | Small `iosMain` surface; tests run on the simulator |
| Android migration test on a device | Room's Android helper reads schemas from test APK assets, which the KMP Android library plugin does not package | The same schemas and SQL are tested on iOS; Android device test opens the production database |
