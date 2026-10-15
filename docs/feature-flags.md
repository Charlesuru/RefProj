# Feature flags (`core:featureflags`)

Trunk-based development: unfinished work is merged to `main` behind a flag that is **off in PROD** (`CLAUDE.md` §1).

## Resolution order
| # | Source | When |
|---|---|---|
| 1 | Debug override (`FeatureFlagOverrides`, stored locally) | Only if `AppConfig.isDebugMenuEnabled`: every non-PROD environment and any debug build. **Never in a PROD release build** |
| 2 | Remote value (`RemoteFlagSource`) | When the remote service has a value for the flag's `key`. Today `NoRemoteFlagSource`; Firebase Remote Config plugs in here |
| 3 | Default (`FeatureFlag.enabledByDefaultIn`) | Per environment. PROD is never in the set (`FeatureFlagTest`) |

## Adding a flag
```kotlin
NEW_CHECKOUT(
    key = "new_checkout",                       // stable: Remote Config parameter + override key
    description = "Redesigned checkout flow",   // shown in the debug menu
    enabledByDefaultIn = setOf(DEV, SIT),       // never PROD
),
```
Read it in a ViewModel with `featureFlags.observe(FeatureFlag.NEW_CHECKOUT)` (reacts to overrides and remote
changes) or `isEnabled(...)` for a one-off check. Gate the entry point (tab, button, route), not every line.

## Lifecycle of a flag
1. **Create** with the first PR of the feature, on in DEV (and SIT when testable).
2. **Test** in STAGE by adding STAGE, or per device with a debug override.
3. **Release** by turning it on in PROD **remotely** (staged rollout), never by changing the default.
4. **Remove** the flag and the old code path once the feature is fully rolled out (a ticket per flag).

## Current flags
| Flag | Default on in | Purpose |
|---|---|---|
| `home_build_info` | DEV, SIT, STAGE | Version and build number on the home screen |
