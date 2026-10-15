# Step 4 — Core (log)

- **Date:** 2026-10-15
- **Ticket:** REF-4 (placeholder key until Jira exists)
- **PRs (stacked, merge in order):** [#8](https://github.com/Charlesuru/RefProj/pull/8) design system ·
  [#9](https://github.com/Charlesuru/RefProj/pull/9) core infrastructure + Koin ·
  [#10](https://github.com/Charlesuru/RefProj/pull/10) navigation + localisation ·
  [#11](https://github.com/Charlesuru/RefProj/pull/11) feature flags ·
  [#12](https://github.com/Charlesuru/RefProj/pull/12) Firebase ·
  [#15](https://github.com/Charlesuru/RefProj/pull/15) iOS signing fix ·
  [#13](https://github.com/Charlesuru/RefProj/pull/13) `deploy:dev` + SIT nightly · this docs PR
- **Reference docs:** [ADR 0007](../adr/0007-core-infrastructure.md), [design-system.md](../design-system.md),
  [feature-flags.md](../feature-flags.md), [localisation.md](../localisation.md), [observability.md](../observability.md),
  [ci-cd.md](../ci-cd.md), [secrets.md](../secrets.md)

## 1. Owner decisions (session start)
| Question | Decision |
|---|---|
| Firebase console work | In parallel with the code; the code runs without Firebase config (no-op) |
| PR label `deploy:dev` + SIT nightly (step-3 proposal) | **Yes, both** |
| Delivery | Several small stacked PRs |
| App icons | Keep the placeholders |
| Firebase billing | Project is on Blaze because Google Cloud `refproj` has billing; the products used are free. Advice: budget alert (or unlink billing → Spark) |

## 2. What was built
| Area | Result |
|---|---|
| Design system | Full M3 colour scheme (light/dark), extended colours, type scale, spacing/sizes/shapes/elevation tokens, 11 `Ref*` components, Material icons as vector drawables, strings EN+SV |
| Modules | `core:storage`, `core:network`, `core:observability`, `core:testing`, `core:navigation`, `core:ui`, `core:featureflags`, `domain`, `data` |
| DI | Koin; composition root `app:shared/di/AppGraph.kt` (`startApp`), called from `RefProjApplication` and `RefProjApp.init` |
| Navigation | Navigation 3 `NavDisplay`; routes + saved-state serializers in `core:navigation`; features contribute screens via `NavEntryInstaller` |
| Storage | `KeyValueStore`; `SecureStore` (Android Keystore AES-256-GCM, iOS Keychain this-device-only) |
| Network | Ktor client per environment, `safeApiCall`, credential redaction in debug logs |
| Observability | Kermit; Crashlytics + Analytics native on both platforms; Kotlin/Native crash hook; R8 mapping + dSYM upload |
| Feature flags | Override > remote > environment default; never on in PROD by default |
| Localisation | Per-app language on Android 13+, `CFBundleLocalizations` on iOS, `TranslationsTest` |
| CI/CD | CI on stacked PRs; Firebase config from secrets; `deploy:dev` PR previews; SIT nightly; SPM cache |

Tests added (all labelled): 13 test classes — UNIT (contrast, storage, analytics events, log writer, use cases,
error mapping, navigator, routes, MVVM base, view models, flags, config, translations, crash bridge) and INTEGRATION
(HTTP client with MockEngine, repository, Koin graph).

## 3. Problems and fixes
| Problem | Fix |
|---|---|
| Spotless did not pass the root `.editorconfig` to ktlint (root-level file trees), so Compose rule settings were silently ignored | `setEditorConfigPath(...)` in `QualityConventionPlugin` |
| detekt line length (120) differed from ktlint (140); `MatchingDeclarationName` fights Compose file naming; `FunctionNaming` flagged test names in KMP host-test source sets | Aligned / disabled / excluded in `detekt.yml`, each with a reason |
| `CFBridgingRetain` is in `platform.Foundation`, not CoreFoundation; multiplatform-settings Keychain needs two opt-ins | Fixed imports and documented opt-ins |
| Stacked PRs got no CI (`pull_request` filtered on base `main`), and retargeting does not trigger a run | `ci.yml` runs on every PR; stack rebased |
| Firebase Analytics ships Guava 31.1-android (GHSA-7g45-4rm6-3mm3) — found by dependency review | Constraint to Guava 33.7.2-android ("app runtime security pins" in the catalog) |
| `startIosApp` had too many parameters (detekt) | Swift builds `AppConfig` and passes it |
| Firebase iOS 13.0.x was one week old (new major) | Pinned 12.19.2 (policy: latest release ≥ two weeks old) and committed `Package.resolved`; `FirebaseAnalyticsCore` instead of `FirebaseAnalytics` (no ad-ID / ad-conversion code) |
| `deploy:dev` must not run PR code on the self-hosted Mac | `_distribute.yml` input `trusted`; PR previews always use GitHub-hosted macOS |
| First iOS deploy with Firebase failed: the provisioning profile passed as xcargs applied to Swift package resource bundles (`… does not support provisioning profiles`). Only simulator builds had been tried locally | #15: `update_code_signing_settings` on the app target only; verified with a local signed `ios archive` before merging. Lesson: archive a signed build locally whenever the iOS dependency graph changes |
| Rebase-merging stacked PRs gives new SHAs on `main`, so the next PR shows conflicts; retargeting does not start CI | Restack the rest of the stack onto `main` after every merge (`git rebase --onto`), which also triggers CI |

## 4. Verification
- `./gradlew spotlessCheck detekt allTests :app:android:assembleDevDebug` green on every PR branch; CI green on #8–#9 at the time of writing.
- Android DEV on the emulator: English and Swedish, ViewModel via Koin, flag-driven build info, Firebase initialised.
  Without `google-services.json`: debug and R8 release still build.
- iOS DEV in the simulator (`fastlane ios build`): Koin start from Swift, Swedish, Firebase Crashlytics + Analytics 12.19.2
  configured; without the plist the build warns and runs with no-op observability.
- Firebase config files match all bundle IDs / package names. gitleaks clean on the branch and all non-ignored files.
- Deploy with Firebase on `main` (run 38711322510): Android DEV + SIT → App Distribution (installed by the owner via
  App Tester), iOS DEV + SIT → TestFlight.

## 5. Owner actions
See the step-4 checklist in [PROGRESS.md](../PROGRESS.md#step-4--owner-checklist).
