# RefProj

[![CI](https://github.com/Charlesuru/RefProj/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/Charlesuru/RefProj/actions/workflows/ci.yml)

Enterprise reference project — Compose Multiplatform app for Android and iOS.

## Requirements
| Tool | Version |
|---|---|
| JDK | 21+ (Temurin recommended) |
| Android Studio | latest stable, with the Kotlin Multiplatform plugin |
| Xcode | 26+ (Apple silicon Mac) |
| XcodeGen | `brew install xcodegen` |
| Ruby / Bundler | Version in `.ruby-version` (rbenv recommended) |
| gitleaks | `brew install gitleaks` (secret scanning in the pre-commit hook) |

## Getting started
```bash
# One-time: git hooks, Ruby gems, local config templates
./scripts/setup.sh

# Android — install DEV debug on a connected device/emulator
./gradlew :app:android:installDevDebug

# iOS — generate the Xcode project, then open it
# set DEVELOPMENT_TEAM in iosApp/Configs/Local.xcconfig to run on a device
(cd iosApp && xcodegen generate)
open iosApp/RefProj.xcodeproj    # pick scheme RefProj-Dev
```

## Environments
`DEV`, `SIT`, `STAGE`, `PROD` — see [docs/architecture.md](docs/architecture.md#environments).

| | Android | iOS |
|---|---|---|
| Select env | Build variant `devDebug`, `sitRelease`, … | Scheme `RefProj-Dev`, `RefProj-Sit`, … |

## Build & ship (fastlane)
Everyone — developers, AI agents and CI — uses the same lanes. Always via Bundler:
```bash
bundle exec fastlane lanes                               # list all lanes
bundle exec fastlane test                                # quality gate
bundle exec fastlane android build env:sit type:release  # signed if the upload key is configured
bundle exec fastlane ios build env:dev                   # simulator, unsigned
bundle exec fastlane android beta env:dev                # Firebase App Distribution
bundle exec fastlane ios beta env:dev                    # TestFlight
```
STAGE/PROD uploads require a `vX.Y.Z` tag. Versioning, signing and distribution:
[ADR 0005](docs/adr/0005-release-tooling.md). Secrets: [docs/secrets.md](docs/secrets.md).

## Quality checks
```bash
./gradlew spotlessApply                 # format
./gradlew spotlessCheck detekt allTests # what CI runs
```

## CI/CD
GitHub Actions runs the same fastlane lanes: every PR must pass **`CI result`** (lint, tests and builds on both platforms,
secret and dependency scanning). `main` ships to DEV + SIT; a `vX.Y.Z` tag ships to STAGE → PROD after approval.
Details, release how-to and the security model: [docs/ci-cd.md](docs/ci-cd.md).

## Docs
- [CLAUDE.md](CLAUDE.md) — rules for AI agents (and humans)
- [docs/architecture.md](docs/architecture.md) — modules, layers, environments
- [docs/adr/](docs/adr/) — architecture decisions
- [docs/pr-workflow.md](docs/pr-workflow.md) — one change from clone to merge: hooks, CI, review, rebase-merge
- [docs/ci-cd.md](docs/ci-cd.md) — pipelines, releasing, runners, repository settings
- [docs/secrets.md](docs/secrets.md) — every secret, where it lives, how to rotate it
- [SECURITY.md](SECURITY.md) — reporting a vulnerability
- [docs/PROGRESS.md](docs/PROGRESS.md) — build-out progress
