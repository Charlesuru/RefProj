# Review checklist

Shared by the local `/review-pr` skill and the CI review (`.github/workflows/ai-review.yml`). Report only real
problems with a concrete consequence; skip style that Spotless/detekt already enforce. Rank: **blocker** (wrong
behaviour, security, data loss, broken rule in CLAUDE.md §1) → **should fix** → **nit** (at most three).

## Correctness
- Does the change do what the PR (and its ticket's acceptance criteria) says? Edge cases: empty, error, offline,
  process death, both platforms (Android + iOS), English + Swedish.
- Coroutines/Flow: cancellation, dispatcher use, state updated from one place, no leaked scopes.
- Persistence: Room migrations for schema changes (with a migration test), no blocking I/O on the main thread.

## Architecture (`docs/architecture.md`, enforced in part by `:tests:architecture`)
- `app → feature → domain ← data`; no `feature` → `feature`/`data`; `domain` is pure Kotlin.
- One ViewModel per screen, a single `StateFlow<UiState>`, `UiAction`s in, one-off events via `Channel`/`Flow`.
- Koin constructor injection; no service locator in business logic. Shared code in `commonMain`.

## Security (CLAUDE.md §1)
- No secrets, tokens, keys or personal data in code, logs, tests, fixtures or the PR. Nothing secret in
  `AppConfig`/`BuildConfig`/xcconfig.
- No disabled TLS validation, R8, backup exclusion or secret scanning. Workflows: least `permissions:`, SHA-pinned
  actions, untrusted input via `env:` never `${{ }}` inside `run:`.

## Product rules
- Unfinished user-facing work behind a feature flag that is OFF in PROD.
- UI text in `composeResources/values*/strings.xml` (EN + SV); design-system components and tokens, no raw colours
  or dp numbers in features.

## Tests and build
- New logic has tests; each test class starts with its type label (`// UNIT TEST`, …). Tests prove the acceptance
  criteria, not just lines.
- Versions only in `gradle/libs.versions.toml`; build logic in `build-logic/`. A new library is justified in the PR
  (license, maintenance, size, KMP support).
- `TODO`/`FIXME` reference a ticket. No `@Suppress` without a reason.

## Traceability
- PR title `REF-123: …`, branch `<type>/REF-123-…`, commits `<type>(<scope>): … [REF-123]` (CI checks the format;
  you check that the content matches the ticket). Docs/ADR updated when architecture, CI or the workflow changed.
