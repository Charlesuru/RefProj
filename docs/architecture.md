# Architecture

## Layers & module graph

```
                 ┌──────────────┐        ┌──────────────┐
                 │ app:android  │        │ iosApp (Swift)│
                 └──────┬───────┘        └──────┬───────┘
                        └──────────┬────────────┘
                            ┌──────▼──────┐
                            │ app:shared  │  root UI, DI graph, NavDisplay
                            └──────┬──────┘
             ┌─────────────────────┼─────────────────────┐
     ┌───────▼───────┐     ┌───────▼───────┐     ┌───────▼───────┐
     │ feature:login │     │ feature:home  │ ... │ feature:debug │   UI + ViewModels
     └───────┬───────┘     └───────┬───────┘     └───────┬───────┘
             └─────────────┬───────┴─────────────────────┘
                     ┌─────▼─────┐          ┌──────────┐
                     │  domain   │◄─────────│   data   │   repositories implement domain interfaces
                     └───────────┘          └──────────┘
          core:* (config, designsystem, navigation, network, featureflags, observability, …)
                         may be used by every layer above
```

| Rule | Why |
|---|---|
| `feature` never depends on another `feature` | Features build, test and ship independently |
| `feature` never depends on `data` | UI only knows use cases/interfaces → swappable data sources, fast tests |
| `domain` is pure Kotlin | Business rules testable without any platform |
| Only `app:shared` sees everything | Single composition root for DI and navigation |

## Is every feature its own module?
Yes. A feature = one user-facing capability (e.g. `login`, `onboarding`, `home`, `profile`).
Benefits: parallel builds, enforced boundaries, clear code ownership, and a feature flag can
remove a whole feature from the graph. We start with a single module per feature; split into
`feature:x:api` + `feature:x:impl` only if a feature must expose types to others (see ADR-0003).

## Environments
| Env | Android applicationId | iOS bundle ID | Purpose |
|---|---|---|---|
| DEV | `com.studiodraug.refproj.dev` | `com.studiodraug.refproj.dev` | Daily development, debug tools on |
| SIT | `…refproj.sit` | `…refproj.sit` | System integration testing against SIT backend |
| STAGE | `…refproj.stage` | `…refproj.stage` | Production-like pre-release / UAT |
| PROD | `com.studiodraug.refproj` | `com.studiodraug.refproj` | App Store / Google Play |

Android: product flavors in `app/android/build.gradle.kts`.
iOS: build configurations `<Debug|Release>-<Env>` + `iosApp/Configs/<Env>.xcconfig`, one scheme per env.
Both feed an `AppConfig` into shared code at startup. Shared code never chooses the environment itself.

## Key technology choices
| Concern | Choice | ADR |
|---|---|---|
| UI | Compose Multiplatform (shared UI on Android + iOS) | 0001 |
| Navigation | Navigation 3 (JetBrains multiplatform artifacts, stable) | 0002 |
| Modularization | Multi-module, convention plugins | 0003 |
| iOS project | XcodeGen (generated, not committed) | 0004 |
| DI | Koin | 0005 (step 4) |
| Lint | ktlint (via Spotless) + Compose rules + detekt | — |
