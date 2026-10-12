# 0001–0004 — Foundation decisions

- **Status:** Accepted
- **Date:** 2026-10-12

## 0001 — Compose Multiplatform for shared UI
**Decision:** Share UI, ViewModels and business logic in Kotlin; Android and iOS are thin shells.
**Alternatives:** KMP with native UI (SwiftUI + Compose) gives the most native feel but doubles UI work.
**Consequences:** One UI codebase. iOS-specific UI (e.g. system share sheets) goes through `expect/actual` or Swift interop.

## 0002 — Navigation 3
**Decision:** Use Navigation 3 (`org.jetbrains.androidx.navigation3`, stable 1.1.x on all platforms).
The back stack is a plain observable list we own; routes are `@Serializable` `NavKey`s.
**Alternatives:** Navigation 2 (Compose) — graph-based, less flexible. Decompose — powerful but non-standard.
Voyager — weaker maintenance.
**Consequences:** Back stack logic is ordinary, testable Kotlin. iOS needs polymorphic serializers registered
for saved state (handled in `core:navigation`).

## 0003 — Multi-module with convention plugins
**Decision:** `app / core / domain / data / feature` modules. Shared build config in `build-logic` plugins
(`refproj.kmp.library`, `refproj.kmp.compose`, `refproj.kmp.feature`, `refproj.android.application`, `refproj.quality`).
One module per feature, single module (no api/impl split) until a feature must expose types to another.
**Consequences:** Module boundaries are enforced by Gradle and architecture tests. Adding a module = a 3-line build file.

## 0004 — XcodeGen for the iOS project
**Decision:** The Xcode project is generated from `iosApp/project.yml` and is not committed.
**Alternatives:** Committed `.pbxproj` (constant merge conflicts, unreviewable diffs). Tuist (more powerful, heavier).
**Consequences:** Run `xcodegen generate` after pulling changes to `project.yml`. Fastlane and CI do this automatically.
