# RefProj

Enterprise reference project — Compose Multiplatform app for Android and iOS.

## Requirements
| Tool | Version |
|---|---|
| JDK | 21+ (Temurin recommended) |
| Android Studio | latest stable, with the Kotlin Multiplatform plugin |
| Xcode | 26+ (Apple silicon Mac) |
| XcodeGen | `brew install xcodegen` |
| Ruby / Bundler | 3.3+ (for fastlane, step 2) |

## Getting started
```bash
# Android — install DEV debug on a connected device/emulator
./gradlew :app:android:installDevDebug

# iOS — generate the Xcode project, then open it
cp iosApp/Configs/Local.xcconfig.example iosApp/Configs/Local.xcconfig   # set your DEVELOPMENT_TEAM
(cd iosApp && xcodegen generate)
open iosApp/RefProj.xcodeproj    # pick scheme RefProj-Dev
```

## Environments
`DEV`, `SIT`, `STAGE`, `PROD` — see [docs/architecture.md](docs/architecture.md#environments).

| | Android | iOS |
|---|---|---|
| Select env | Build variant `devDebug`, `sitRelease`, … | Scheme `RefProj-Dev`, `RefProj-Sit`, … |

## Quality checks
```bash
./gradlew spotlessApply                 # format
./gradlew spotlessCheck detekt allTests # what CI runs
```

## Docs
- [CLAUDE.md](CLAUDE.md) — rules for AI agents (and humans)
- [docs/architecture.md](docs/architecture.md) — modules, layers, environments
- [docs/adr/](docs/adr/) — architecture decisions
- [docs/PROGRESS.md](docs/PROGRESS.md) — build-out progress
