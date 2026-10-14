# 0005 — Release tooling: fastlane, signing, versioning, secret scanning

- **Status:** Accepted
- **Date:** 2026-10-14

## Context
Developers, AI agents and CI must build and ship the app the same way, for 4 environments and 2 platforms,
without secrets ever entering the repository.

## Decision

### fastlane as the single entry point
All build/test/upload commands are lanes in `fastlane/Fastfile`, run through Bundler (`bundle exec fastlane …`)
with versions locked in `Gemfile.lock`. CI calls the same lanes — there is no CI-only build logic.
Gradle stays the build system; fastlane only orchestrates it.

### Distribution per environment
| | Android | iOS |
|---|---|---|
| DEV / SIT / STAGE `beta` | Firebase App Distribution (signed APK) | TestFlight, internal testers |
| PROD `beta` | Play **internal** track (AAB) | TestFlight |
| PROD `release` | Play **production** track as **draft** | App Store Connect upload, **not submitted** |

Promotion to users (Play rollout, App Review submission) is always a human action in the store console.
STAGE and PROD uploads require a `vX.Y.Z` tag on `HEAD`.

### Signing
- **Android:** Play App Signing. We hold only the *upload key*; it is read from Gradle properties or env vars by the
  `refproj.android.application` convention plugin. Without it, release builds are unsigned and fastlane refuses to upload.
- **iOS:** fastlane match with a separate private, encrypted repo (`Charlesuru/refproj-certificates`), `appstore` profiles
  for all four bundle IDs. CI is read-only; only the owner runs `ios bootstrap_signing`. Local development keeps Xcode's
  automatic signing; release archives switch to manual signing via `xcargs`, so the Xcode project stays simple.
- **App Store Connect API key** (not Apple ID + 2FA) for all Apple API access.

### Versioning
- **Version name** (`versionName` / `CFBundleShortVersionString`): `X.Y.Z` from a `vX.Y.Z` tag on `HEAD`;
  otherwise the next minor after the latest tag (so DEV/SIT builds never target a closed App Store version train);
  `0.1.0` before the first tag. Override: `VERSION_NAME`.
- **Build number** (`versionCode` / `CFBundleVersion`): minutes since 2026-01-01 UTC. Monotonic for local and CI builds
  alike, no state to store, fits Android's limit for centuries. CI passes one `BUILD_NUMBER` per workflow run so both
  platforms of a release share it.

### Secret scanning and hooks
- `gitleaks` with project rules (`.gitleaks.toml`) in the pre-commit hook and CI.
- Hooks are plain scripts in `.githooks/` enabled with `git config core.hooksPath .githooks` (`scripts/setup.sh`):
  pre-commit (gitleaks + spotless), commit-msg (Conventional Commits + Jira key), pre-push (spotless + detekt).

## Alternatives considered
- **Gradle Play Publisher / xcodebuild scripts:** platform-specific, two toolchains to learn. fastlane covers both.
- **Xcode Cloud / automatic signing on CI:** ties CI to Apple's runner or to an Apple ID session.
- **versionCode from `github.run_number`:** breaks for local uploads and when workflows are renamed or recreated.
- **lefthook / pre-commit framework:** nicer UX, but an extra tool for every developer; plain hooks suffice for three checks.

## Consequences
- New machines run `./scripts/setup.sh` once. Hooks can be bypassed locally, so CI repeats every check.
- The match repo, App Store Connect apps (one per environment), Play app (PROD) and Firebase apps (DEV/SIT/STAGE)
  must exist before the corresponding upload lane works. See `docs/secrets.md`.
- Play requires the very first AAB of a new app to be uploaded manually in Play Console.
