# 0011 — Test strategy: shared tests on both platforms, gates in CI

- **Status:** Accepted
- **Date:** 2026-10-21
- **Ticket:** REF-11

## Context
The plan asks for unit, integration, UI, screenshot and architecture tests plus coverage thresholds, each test
labelled with its type. The app shares almost all code between Android and iOS, and most bugs are in that shared
code, but some failures only show up on a platform (step 5: a Keychain error crashed the iOS app while every test
with fakes was green). CI time matters too: GitHub-hosted macOS minutes are limited and emulators are slow and flaky.

## Decision
- **Write tests once in `commonTest` and run them on both platforms**: the Android host (JVM) and the iOS simulator.
  Tests that need the platform runtime extend `PlatformTest`, which runs them under **Robolectric** on the Android
  host. This includes Room and the Compose UI tests (`runComposeUiTest`), so UI tests also run on iOS.
- **On-device Android tests only where the JVM cannot stand in** (Android Keystore, the bundled SQLite library,
  database files), on a Gradle Managed Device in a **nightly workflow** — not on every PR (owner decision).
- **Screenshot tests generated from the screen catalog** (Roborazzi, Android host): every sample state each feature
  already binds for the debug menu, light/dark/Swedish. No second list of previews to maintain.
- **Architecture tests with Konsist in their own JVM module `:tests:architecture`** (owner decision), part of
  `allTests`. Rules read the sources, so they also cover the Gradle files and test labels.
- **Coverage with Kover, per-module floors in one file, ratcheted up** (owner decision: floors at the measured level
  rather than one fixed percentage). UI code is excluded from the percentage; it has UI and screenshot tests.
- **Migration tests from committed schemas**: every exported Room schema is migrated to the current version.
- End-to-end UI flows find controls by **test tags**, so they do not depend on the device language.

## Alternatives considered
- **Instrumented Android tests on every PR.** Strongest signal, but +10 minutes and flaky emulators on every PR;
  the JVM covers the same shared code. Nightly is the compromise.
- **Paparazzi for screenshots.** Android-only like Roborazzi, but tied to Android library modules with layoutlib;
  Roborazzi works with Robolectric, which the UI tests already use.
- **Konsist inside `app:shared` tests.** No new module, but architecture tests would run inside the app's test
  classpath and build; a separate JVM module is faster and has no app dependencies.
- **One global coverage threshold (e.g. 80 %).** Simple, but punishes small modules and invites meaningless tests;
  per-module floors show exactly where coverage dropped.
- **XCUITest for iOS.** Real device-level UI tests, but a second test stack (Swift, XcodeGen target, simulators in
  CI) for UI code that is already tested on the iOS simulator through Compose.

## Consequences
- A feature's screens are tested on iOS without Swift tests; native iOS integrations (Keychain, Firebase bridge)
  still need the app itself or a future XCUITest target (gap list in `docs/testing.md`).
- Robolectric and JDK 25 need JVM flags for host tests (`KmpLibraryConventionPlugin`); Robolectric emulates up to
  API 36, newer than the minimum but older than `compileSdk`.
- Reference images (≈ 1.7 MB) live in the repository; an intended UI change re-records them in the same PR.
- Production code gains a few automation IDs (`<Feature>TestTags`) and `ALL_MIGRATIONS`.
- Coverage floors must be raised by hand when coverage rises (the ratchet is a review rule, not automatic).
