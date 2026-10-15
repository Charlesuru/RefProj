# 0007 — Core infrastructure: DI, navigation, storage, observability, feature flags

- **Status:** Accepted
- **Date:** 2026-10-15
- **Ticket:** REF-4

## Context
Plan step 4 builds the infrastructure every feature relies on. It must work identically on Android and iOS, keep
`domain` pure, keep features independent of each other, and handle secrets and personal data safely.

## Decision
| Concern | Decision |
|---|---|
| DI | **Koin**, one composition root (`app:shared/di/AppGraph.kt`, `startApp`). Each module exposes a Koin `Module`; `domain` has no DI dependency (its use cases are wired in `app:shared`). `AppGraphTest` resolves the real graph on both platforms |
| Navigation | **Navigation 3**. Route keys live in `core:navigation`; features contribute screens through a `NavEntryInstaller` bound in Koin, so they never reference each other. Every route is registered in `NavSavedStateConfiguration` (no reflection on iOS); `RoutesTest` enforces it |
| MVVM | `MviViewModel<State, Action, Event>` in `core:ui`: one `StateFlow`, actions in, one-off events through a `Channel` |
| Storage | `KeyValueStore` (multiplatform-settings: SharedPreferences / NSUserDefaults) for non-sensitive data. `SecureStore` for tokens: Android Keystore AES-256-GCM, iOS Keychain `AfterFirstUnlockThisDeviceOnly` |
| Network | One Ktor client: base URL per environment, `expectSuccess`, timeouts, debug-only header logging with credentials redacted. `safeApiCall` maps expected failures; `data` maps them to domain `DataError` |
| Observability | Kermit for logging; `CrashReporter`/`AnalyticsTracker` interfaces implemented **natively** per platform (Firebase Android SDK in `app:android`, Firebase iOS SDK via SPM in `iosApp`). Collection off by default, on in release builds. See `docs/observability.md` |
| Firebase project | One Firebase project (Google Cloud `refproj`) with an Android and an iOS app per environment |
| Firebase config | `google-services.json` / `GoogleService-Info.plist` are not committed; CI writes them from environment secrets. Builds without them run with no-op observability |
| Feature flags | Enum catalogue with per-environment defaults (never on in PROD), remote source interface (Remote Config later), debug overrides only when `AppConfig.isDebugMenuEnabled`. See `docs/feature-flags.md` |
| Localisation | Compose resources per module, English + Swedish, `TranslationsTest` |

## Alternatives considered
- **Kotlin-side Firebase wrappers (GitLive firebase-kotlin-sdk, CrashKiOS):** one API in common code, but an extra
  third-party layer between us and Google's SDKs, Kotlin/Native linking work for Crashlytics, and slower SDK updates.
  The native-SDK-behind-an-interface approach keeps each platform on the official SDK and keeps shared code vendor-free.
- **One Firebase project per environment:** stronger isolation of PROD data and quotas, at the cost of four consoles,
  four sets of config and service accounts. Revisit if PROD needs separate access control (e.g. a real backend).
- **EncryptedSharedPreferences (Jetpack Security):** deprecated; a small Keystore AES-GCM implementation is clearer.
- **Hilt / kotlin-inject / Metro:** Hilt is Android-only; compile-time DI for KMP is promising but less established.

## Consequences
- New screens: add a route key in `core:navigation` (and register it), a `NavEntryInstaller` in the feature's Koin module.
- New flags only through `FeatureFlag`; a test blocks PROD-on defaults.
- Firebase SDK updates on iOS are manual (Dependabot does not read XcodeGen specs): see `docs/observability.md`.
- Analytics consent for PROD must be decided with the onboarding feature (step 5).
