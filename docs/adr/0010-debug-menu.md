# 0010 — Debug menu: runtime-gated feature module

- **Status:** Accepted
- **Date:** 2026-10-21
- **Ticket:** REF-10

## Context
Developers and testers need tools inside the app: feature flag overrides, jumping into a state (signed in,
onboarding), a pre-filled test account, a log viewer, crash tests, clearing data, and a catalog of every screen in
sample states. None of it may be reachable by users of the store build (PROD release). The app is built as one shared
Kotlin framework for all iOS configurations and one KMP library for all Android flavors.

## Decision
- A normal feature module, **`feature:debug`**, depending only on `core:*` and `domain` like every feature.
- **Gated at runtime by `AppConfig.isDebugMenuEnabled`** (`isDebugBuild || !environment.isProduction`, step 4), owner
  decision at the start of step 6. In a PROD release build:
  - `app:shared` does not add `debugModule` to Koin, so the debug routes, ViewModels and screens are never bound;
  - the environment badge (the only entry point) is not drawn;
  - `TestAccountProvider` returns `null` (no pre-filled login), no `LogBuffer` and no analytics logging are created;
  - feature flag overrides are already ignored (`DefaultFeatureFlags`).
  `AppGraphTest` asserts all of this for PROD release and the opposite for every other environment.
- **Entry point:** a tab on the start edge of every screen showing the environment name (owner decision), so testers
  always see which environment they are running and reach the menu from onboarding and login as well.
- The debug menu never writes storage directly: state shortcuts are domain use cases built from existing business
  operations (`SignInWithTestAccountUseCase`, `ClearLocalDataUseCase`, …), so the app reaches the same states a user would.
- Features contribute sample screens through `ScreenPreviewGroup` (`core:ui`) bound in their own Koin module, the
  same pattern as `NavEntryInstaller`; the debug module only collects them.

## Alternatives considered
- **Exclude the module from the PROD binary.** Android could add it per flavor/build type from the shell, but iOS
  links a single `Shared` framework for every configuration, so it would need a second framework (and Xcode build
  configuration logic) only for PROD. Much more build complexity for a small gain: the debug code holds no secrets
  (the fake backend's test account is public demo data, `docs/features.md`) and is unreachable without its Koin bindings.
- **Debug row in Profile only.** Simpler, but not reachable before login, where "sign in as test user" and
  "show onboarding" matter most.
- **Shake gesture.** Needs sensor code per platform, awkward in simulators, invisible to new testers.

## Consequences
- The debug code ships inside the PROD binary (dead code there). It must therefore never contain real secrets: a
  real backend's test accounts must come from a per-environment, non-PROD source (e.g. a SIT config), never be hardcoded.
- Every new debug entry point must check `isDebugMenuEnabled` or live inside `debugModule`; `AppGraphTest` covers the
  existing ones.
- Adding local data means extending `ClearLocalDataUseCase`, or "Clear all data" stops returning to a fresh install.
