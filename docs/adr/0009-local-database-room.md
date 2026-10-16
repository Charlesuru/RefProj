# 0009 — Local database: Room (KMP)

- **Status:** Accepted
- **Date:** 2026-10-16
- **Ticket:** REF-5

## Context
Step 5 adds the first user data that grows over time (tasks). `KeyValueStore` is meant for small, flat values and
cannot query, order or migrate a list. The database must work on Android and iOS from `commonMain`, support schema
migrations, expose changes as `Flow`, and must not ship code with a license outside the allowlist (ADR 0008).

## Decision
**Room 2.8 (Kotlin Multiplatform)** with the **bundled SQLite driver** (`androidx.sqlite:sqlite-bundled`), owner
decision at the start of step 5.
- Lives in `data` (the project map puts persistence there); entities and DAOs are `internal`, the domain sees only
  `TaskRepository`.
- Build logic in the convention plugin `refproj.kmp.room`: KSP compiler per target, schema export to
  `data/schemas/` (committed — it records every version and feeds migration tests).
- Bundled SQLite: the same SQLite version on every OS release and platform, no system-library link flags on iOS.
- Queries run on `Dispatchers.IO` (`buildWithDefaults`). Destructive migration is never enabled.
- File location: Android `databases/` (already excluded from backup), iOS `Application Support/databases/` with
  `NSURLIsExcludedFromBackupKey` — same backup policy on both platforms.

| Criterion | Room |
|---|---|
| License | Apache-2.0 (allowlisted) |
| Maintenance | Google / AndroidX, stable KMP support since 2.7 (2025), regular releases |
| KMP support | Android, iOS, JVM, (Web alpha); KSP processor per target |
| Size | Runtime + bundled SQLite: roughly 1–2 MB per ABI on Android; part of the static framework on iOS |
| Versions | room 2.8.5, sqlite-bundled 2.7.1, KSP 2.3.12 — each the latest release that is at least two weeks old |

## Alternatives considered
- **SQLDelight** — mature, SQL-first, also good. Room was chosen as the official AndroidX library most Android
  teams already know, with annotation-based DAOs and auto-migrations.
- **`KeyValueStore` + JSON** — no new dependency, but no queries, no migrations and the whole list is rewritten on
  every change. Not production-grade for growing data.

## Consequences
- Every entity change bumps the database version and adds a migration; the exported schema shows the diff in review.
- Room needs a `Context` on Android, so database tests run on the iOS simulator in `allTests`
  (`TaskRepositoryImplTest`); Android on-device tests follow in step 7. The Koin graph test replaces `TaskRepository`.
- KSP becomes part of the build (one more tool to upgrade with Kotlin).
