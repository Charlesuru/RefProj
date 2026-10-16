# 0008 — License policy for dependencies

- **Status:** Accepted
- **Date:** 2026-10-16
- **Ticket:** REF-4

## Context
The apps are distributed as binaries through the App Store and Google Play. A dependency's license decides what we
owe when we distribute it: attribution only (permissive), source of modified files (weak copyleft: MPL, EPL, LGPL), or
the source of the whole app (strong copyleft: GPL, AGPL). GPL-family terms are also incompatible with App Store
distribution in practice, and LGPL's relinking requirement cannot be met by a statically linked iOS framework.
A license violation is a legal risk that cannot be fixed after release, so it must be caught before merge.

Most dependencies are build and test tooling (Kotlin compiler, AGP, Spotless, JUnit, fastlane) that is never
distributed. Its licenses do not create distribution obligations, and some of it is weak copyleft (EPL-2.0, LGPL-2.1,
MPL). Until now GitHub reported every Gradle dependency as runtime scope, so tooling and app could not be told apart.

Baseline on 2026-10-16: 496 Maven packages ship (Android variants and iOS klibraries). Switching on the scopes made
every one of them a "change", so the first run of the check reviewed all of them. Result: no copyleft; everything is
permissive except **12 Google closed-source SDKs** pulled in by Firebase Analytics (`play-services-*`,
`firebase-analytics`, `firebase-measurement-connector`, under the Android SDK License / Google APIs Terms), and about
110 packages without license data in GitHub (mostly AndroidX/JetBrains lifecycle, Compose, Koin, Kermit — all Apache-2.0
upstream). The iOS Firebase SDK (Swift Package Manager) is not in GitHub's dependency graph.

## Decision
- **Allowlist, not denylist** — the common enterprise model ("default deny"): permissive licenses are allowed,
  everything else fails the PR until the owner decides. A denylist silently accepts every license nobody thought of
  (and `deny-licenses` is deprecated in dependency-review-action).
- **Only what ships is checked.** The Gradle dependency graph marks `:app:android`'s variant runtime classpaths and
  `:app:shared`'s iOS klibraries as runtime scope; everything else is development scope. The license check runs on
  runtime scope; the vulnerability check keeps running on every scope.
- **Policy as code:** [`.github/license-policy.yml`](../../.github/license-policy.yml), owned by the code owner.

| Tier | Licenses | Policy |
|---|---|---|
| Allowed | Apache-2.0, Apache-1.1, MIT, MIT-0, BSD-2-Clause, BSD-3-Clause, ISC, 0BSD, Zlib, BSL-1.0, Unicode-3.0, Unicode-DFS-2016, CC0-1.0, Unlicense | in the allowlist |
| Needs approval | Weak copyleft: MPL-2.0, EPL-1.0/2.0, LGPL-2.1/3.0, CDDL; anything non-standard (`LicenseRef-*`, `OTHER`) | fails; the owner may add a per-package exception with a reason (e.g. MPL-2.0 used unmodified) |
| Not allowed | Strong and network copyleft: GPL-2.0/3.0, AGPL-3.0, SSPL; non-commercial or source-available: CC-BY-NC-*, BUSL-1.1, Commons Clause | fails; find another library |
| Vendor (proprietary) | Closed-source SDKs licensed for redistribution in apps (Google Play services, Firebase) | per-package exception, tied to the decision that adopted the vendor |

Exceptions go in `allow-dependencies-licenses` (package URL without version), each with a comment. Current ones: the
12 Google SDKs above (accepted with Firebase, ADR 0007), and tooling that GitHub reports as runtime (all Ruby gems,
because `Gemfile.lock` has no scopes), exempted per gem.

## Alternatives considered
- **Denylist (`deny-licenses`):** fewer failures, but new or unusual licenses pass unnoticed; deprecated upstream.
- **One policy for all scopes:** would either block normal tooling (EPL JUnit, LGPL trove4j in the Kotlin compiler) or
  force copyleft into the allowlist, weakening the app policy.
- **A Gradle license plugin (e.g. Cash App's `licensee`)** that reads POM licenses at build time: covers all shipped
  dependencies, not only changed ones, and the packages GitHub has no license data for. Adds a dependency; proposed as a
  follow-up for the owner to decide.

## Consequences
- A PR that adds or upgrades a shipped dependency with a non-allowed license fails `CI result`.
- Only **changed** dependencies are checked (dependency review compares the PR with `main`); the baseline above
  covers the existing ones.
- Dependencies without license data in GitHub are listed in the job log but **do not fail** (an action limitation).
  Reviewers check the license of every new library anyway (PR template: license, maintenance, size, KMP support).
- Swift packages are not in the graph; a new Swift package needs a manual license check in review.
- New modules that end up in the apps are covered automatically (they resolve through `:app:android` / `:app:shared`).
  A new app module or iOS target must be added to `DEPENDENCY_GRAPH_RUNTIME_*` in `ci.yml`.
- An open-source notices screen (attribution obligation of the permissive licenses) is still to be built; it belongs
  with the debug menu / settings (plan step 6 or later).
