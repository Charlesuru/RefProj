# Step 4 explained — the core infrastructure

> **Audience:** someone learning how an enterprise mobile project is put together.
> **Scope:** plan step 4 (ticket REF-4, PRs #8–#15, finished 2026-10-15).
> **Facts and history:** [step log](../log/step-4-core.md) · **Decisions:** [ADR 0007](../adr/0007-core-infrastructure.md)

---

## 1. What step 4 is, in one paragraph

Steps 1–3 built the *factory*: the repository, the module skeleton, signing, fastlane and CI/CD. Step 4 builds the
*foundation of the house*: everything every feature will need but no single feature owns. Dependency injection,
navigation, a design system, storage, networking, logging and crash reporting, analytics, feature flags and
translations. After step 4 the app still does almost nothing for a user — but a developer adding the login screen in
step 5 never has to decide *how* to store a token, *how* to navigate, *which* button to use or *how* to log. Those
decisions are already made, documented and tested.

**Why build the foundation before any feature?** In a team of 5–50 developers, every infrastructure question left open
gets answered differently by each person. Three ways to navigate, four button styles, tokens in plain preferences.
Fixing that later means touching every feature. Deciding once, early, and enforcing it with code, tests and lint is
much cheaper.

---

## 2. The big picture

```
 app:android ─┐                ┌─ iosApp (Swift): Firebase iOS SDK, Keychain
              └── app:shared ──┘   ← composition root: Koin graph, NavDisplay, root UI
                     │
     feature:home, feature:login, …  ← screens + ViewModels (step 5)
                     │
                  domain  ◄──── data          ← use cases / interfaces   ← repositories
                     │
   core:config  core:designsystem  core:ui  core:navigation  core:featureflags
   core:network core:storage  core:observability  core:testing
```

New modules in step 4: `domain`, `data`, `core:navigation`, `core:network`, `core:featureflags`,
`core:observability`, `core:storage`, `core:testing`, `core:ui` (and a full `core:designsystem`).

**Why so many small modules instead of one `core`?**
- **Enforced boundaries.** If `core:storage` does not depend on `core:network`, nobody *can* accidentally mix them.
  The build system becomes your architecture police.
- **Faster builds.** Gradle only rebuilds the modules that changed and their dependants.
- **Clear ownership.** In a large organisation a platform team owns `core:*`, feature teams own `feature:*`
  (see `.github/CODEOWNERS`).

The golden rule (`docs/architecture.md`): `app → feature → domain ← data`, everyone may use `core`.
Features never know about each other or about `data`.

---

## 3. Area by area

Each section: **what** was built · **why** enterprises do it · **where** to look · **the takeaway**.

### 3.1 Dependency injection — Koin

**What.** Every class gets its dependencies through the constructor. One file,
`app/shared/src/commonMain/kotlin/com/studiodraug/refproj/app/di/AppGraph.kt`, wires everything together
(`startApp(config, crashReporter, analyticsTracker)`). Android calls it from `RefProjApplication`, iOS from Swift.

**Why.**
- *Testability:* a ViewModel that receives a `LoginUseCase` in its constructor can be tested with a fake one.
  A ViewModel that creates its own dependencies cannot.
- *Swappability:* `FakeAuthApi` today, a real Ktor API tomorrow — one line changes in the graph, no feature changes.
- *One composition root:* only `app:shared` sees every module. That single place is the only one allowed to know
  the concrete implementations; everything else talks to interfaces.

**Why Koin and not Hilt?** Hilt is Android-only. In Kotlin Multiplatform the DI must also run on iOS. Koin is the
established KMP choice; compile-time alternatives (kotlin-inject, Metro) were judged less mature (ADR 0007).

**Safety net.** `AppGraphTest` builds the *real* graph on both platforms and resolves every definition. A missing
binding fails a unit test instead of crashing the app at runtime — Koin's main weakness, neutralised.

**Takeaway:** decide the DI strategy before the first feature, have exactly one composition root, and test the graph.

### 3.2 Navigation — Navigation 3

**What.** Every destination is a small serializable *route key* in `core:navigation/Routes.kt`
(`LoginRoute`, `HomeRoute`, …). Features *contribute* their screens through a `NavEntryInstaller` bound in Koin;
`app:shared` collects all installers and renders them with `NavDisplay`. Screens receive a `Navigator`
(`navigate`, `back`, `replaceAll`).

**Why.**
- Feature A can open feature B's screen by its key without depending on feature B's module. That is what keeps
  features independently buildable and testable.
- The back stack is a plain list of keys — easy to reason about, easy to test (`NavigatorTest`), and easy to jump into
  any state (the debug menu in step 6 uses this).
- Keys are `@Serializable`, so the back stack survives process death on Android and state restoration on iOS.
  iOS has no reflection, so every key must be registered in `NavSavedStateConfiguration`; `RoutesTest` fails if you
  forget. *A rule that is easy to forget gets a test.*

**Takeaway:** navigation by keys in a shared module is the standard way to keep feature modules decoupled.

### 3.3 MVVM base — `MviViewModel`

**What.** `core/ui/.../MviViewModel.kt`: a ViewModel exposes **one** `StateFlow<State>`, accepts `Action`s via
`onAction`, and sends one-off `Event`s (navigate, show snackbar) through a `Channel`.

**Why.** *Unidirectional data flow:* the UI renders state and reports intent; it never mutates anything itself.
One state object per screen means a screen is a pure function of its state — trivial to preview, screenshot-test
and debug ("what was the state when it broke?"). Events through a `Channel` are delivered exactly once, so a
navigation doesn't replay after a rotation.

**Takeaway:** a tiny shared base class makes 30 screens written by 10 people look the same.

### 3.4 Design system — `core:designsystem`

**What.** Tokens (colours light/dark, typography, a 4 dp spacing grid, sizes, shapes, elevation) and 11 `Ref*`
components (`RefButton`, `RefTextField`, `RefCard`, loading/error/empty states, …). Details:
[design-system.md](../design-system.md).

**Why.**
- *Consistency:* designers change the brand colour in one place; every screen follows.
- *Rules you can enforce:* `CLAUDE.md` forbids raw colours and dp numbers in features. Review (human or AI) can
  check that mechanically.
- *Accessibility built in:* `ColorContrastTest` fails the build if any text colour is below WCAG AA (4.5:1) on its
  background. Accessibility is a legal requirement in many markets (EU Accessibility Act) — enforcing it by test is
  far cheaper than auditing later.
- *Behaviour built in:* `RefButton(loading = true)` blocks double submits. Every team gets that for free.

**Takeaway:** a design system is not just colours — it is shared behaviour, accessibility and a contract with design.

### 3.5 Storage — two stores, on purpose

**What.**
- `KeyValueStore` (SharedPreferences / NSUserDefaults) for harmless data like "onboarding completed".
- `SecureStore` for secrets the app receives at runtime (session tokens): Android Keystore AES-256-GCM, iOS Keychain
  with `AfterFirstUnlockThisDeviceOnly` (never synced to iCloud, never restored to another device).

**Why two?** Plain preferences are readable on a rooted/jailbroken device and end up in backups. Tokens must not.
Making the *secure* option a separate type forces the developer to choose consciously. `SecureStore` never throws:
a broken keychain must degrade gracefully, not crash the app (a lesson learned the hard way in step 5).

**Note the difference** between *build-time secrets* (signing keys, API keys for CI — they never enter the app,
see [secrets.md](../secrets.md)) and *runtime secrets* (a user's token — stored encrypted on the device).

### 3.6 Network — one Ktor client

**What.** `core:network`: one Ktor `HttpClient` per app with the base URL from the environment (DEV/SIT/STAGE/PROD),
timeouts, `expectSuccess`, and debug-only header logging with `Authorization`/cookies **redacted**. `safeApiCall`
turns exceptions into typed results; `data` maps them to a domain `DataError`.

**Why.** Shared policies (timeouts, logging, later auth headers and retries) live in one place. Errors become
*types* the UI can handle ("no connection" vs "server error") instead of crashes. Redaction is tested
(`HttpClientTest`) because leaking a token into logs that are uploaded to a crash tool is a classic security incident.

### 3.7 Observability — logging, crashes, analytics

**What.** Kermit for structured logging; Firebase Crashlytics and Analytics, implemented **natively** on each
platform (Kotlin in `app:android`, Swift in `iosApp`) behind the interfaces `CrashReporter` and `AnalyticsTracker`.
Warnings become crash breadcrumbs, errors with exceptions become non-fatal reports. Symbols (R8 mapping, dSYM) are
uploaded so stack traces are readable. Details: [observability.md](../observability.md).

**Why.**
- *You can't fix what you can't see.* Without crash reporting you learn about crashes from one-star reviews.
- *Interfaces, not vendors, in shared code.* If the company switches from Firebase to Sentry or Datadog, only the two
  platform implementations change. Tests use `NoOpCrashReporter`.
- *Native SDKs instead of a Kotlin wrapper:* fewer third-party layers between us and Google, faster SDK updates.
- *Privacy by default:* collection is **off** in the manifest/plist; Crashlytics turns on only in release builds;
  Analytics only with the user's opt-in (GDPR — the consent UI came in step 5). No advertising ID, no personal data
  in events. The app builds and runs without any Firebase config (no-op), so forks and PR checks need no secrets.

**Takeaway:** observability is designed in, not bolted on — and privacy is part of the design, not a later fix.

### 3.8 Feature flags — the key to trunk-based development

**What.** `core:featureflags`: an enum `FeatureFlag` with a stable `key` and per-environment defaults. A value is
resolved as **debug override → remote value → environment default**. Details: [feature-flags.md](../feature-flags.md).

**Why.** In trunk-based development everybody merges to `main` daily, so half-finished features *are* in the code
that ships. Flags decide whether users see them. The rules are hard-coded:
- A default can never be ON in PROD — `FeatureFlagTest` fails otherwise. PROD is switched on **remotely** (staged
  rollout, instant kill switch without an app release — important because app store reviews take days).
- Debug overrides work only when `AppConfig.isDebugMenuEnabled`, never in a PROD release build.
- Every flag has a lifecycle ending in **removal**; old flags are technical debt.

**Takeaway:** feature flags separate *deploying* code from *releasing* a feature. That is what makes small PRs and
daily merges safe.

### 3.9 Localisation — English + Swedish

**What.** Each module owns its strings in `composeResources/values*/strings.xml`, keys prefixed with the module name.
Android 13+ per-app language and iOS `CFBundleLocalizations` let the user choose. Details:
[localisation.md](../localisation.md).

**Why.** Retro-fitting translations into hard-coded text is painful. `TranslationsTest` fails when a key is missing
in one language or placeholders differ (`%1$s`) — a missing placeholder is a runtime crash in some languages.

### 3.10 CI/CD additions

| Addition | Why |
|---|---|
| CI runs on *every* PR, not only PRs into `main` | Stacked PRs (below) target each other; they need checks too |
| Firebase config written from GitHub Environment secrets | The config files are not committed; each environment gets its own |
| Label `deploy:dev` on a PR → test build to DEV | Testers can try a change *before* merge. Always on GitHub-hosted runners: PR code must never run on the owner's own Mac |
| SIT nightly build | Integration testers get a fresh build every morning without anyone pressing a button |

### 3.11 Supply-chain security (dependencies)

- **Dependency review** in CI found that Firebase Analytics pulls in a vulnerable Guava (31.1). Fixed with a version
  constraint ("app runtime security pins" in the version catalog), with a note on when to remove it.
- **Version policy for iOS Firebase:** "latest release that is at least two weeks old". Brand-new majors often have
  bugs, and a short delay protects against a compromised release. `Package.resolved` is committed so CI builds exactly
  what was tested.
- **License policy** (ADR 0008): CI enforces a license allowlist for dependencies that ship in the app. In a company,
  an unnoticed GPL dependency can be a legal problem.

**Takeaway:** every dependency is code you ship but didn't write — scan it, pin it, check its license.

---

## 4. How it was delivered — stacked PRs

Step 4 was split into seven small PRs that build on each other ("a stack"):

```
main ← #8 design system ← #9 core + Koin ← #10 navigation + l10n ← #11 flags ← #12 Firebase ← #15 iOS fix ← #13 deploys
```

**Why not one big PR?** A 5 000-line PR cannot be reviewed properly; reviewers skim and approve. Small PRs get real
review, fail CI in a smaller area, and can be reverted individually. **The cost:** after each *rebase-merge* the
commits on `main` get new SHAs, so the rest of the stack must be *restacked* (`git rebase --onto`) — which the AI
does after every merge. This is the everyday trade-off of stacked PRs; tools like Graphite automate it.

---

## 5. What went wrong — and what it teaches

| Problem | Lesson |
|---|---|
| Spotless silently ignored the `.editorconfig`, so lint rules weren't applied | Verify that a rule *actually fires* (break it on purpose once). Silent misconfiguration is worse than a red build |
| detekt and ktlint disagreed on line length | Several linters must share one config of truth; document every disabled rule with a reason |
| Stacked PRs got no CI | Check the CI trigger conditions whenever you change the way you work |
| Vulnerable Guava via Firebase | Transitive dependencies are your responsibility — automated dependency review pays for itself |
| First signed iOS deploy failed (provisioning profile applied to Swift package bundles); only simulator builds had been tried | **Simulator builds don't prove signing works.** Archive a signed build locally when the iOS dependency graph changes |
| `deploy:dev` could have run untrusted PR code on a self-hosted Mac | Self-hosted runners + public repo = treat every PR as hostile; separate trusted and untrusted paths |

---

## 6. Enterprise principles you can reuse anywhere

1. **Decide infrastructure once, early, and write it down** (ADR). The ADR also lists rejected alternatives, so the
   discussion is not re-run every six months.
2. **Turn rules into tests.** Contrast, translations, routes, DI graph, PROD flag defaults, log redaction — each rule
   that a human could forget has a test that fails the build.
3. **Program against interfaces; keep vendors at the edges.** Firebase, Keychain, Ktor sit behind interfaces.
4. **Secure and private by default.** Collection off until allowed, tokens encrypted, secrets never in the binary.
5. **Works without secrets.** A fresh clone, a fork or a PR check builds and runs with no-op implementations.
6. **Small, reviewable changes.** Stacked PRs, each green on its own.
7. **Separate deploy from release.** Feature flags with remote control and a removal plan.
8. **Every dependency is a risk.** Review vulnerabilities, pin versions, check licenses.

---

## 7. Glossary

| Term | Meaning |
|---|---|
| ADR | Architecture Decision Record — a short document per important decision: context, decision, alternatives, consequences |
| Composition root | The single place where the object graph is assembled (`AppGraph.kt`) |
| DI | Dependency injection — objects receive their dependencies instead of creating them |
| KMP | Kotlin Multiplatform — shared Kotlin code for Android and iOS |
| MVVM / UDF / MVI | ViewModel exposes state; UI sends actions; data flows one way |
| Design token | A named design value (`spacing.lg = 16 dp`) used instead of raw numbers |
| Breadcrumb | A log line attached to the next crash report, showing what happened just before |
| Non-fatal | A caught error reported to Crashlytics without crashing |
| R8 mapping / dSYM | Files that turn obfuscated/compiled stack traces back into readable code lines |
| Feature flag | A runtime switch that hides or shows a feature without a new release |
| Trunk-based development | Everybody merges small changes to `main` often, instead of long-lived branches |
| Stacked PRs | A chain of PRs where each targets the previous one's branch |
| Restack | Rebasing the rest of a stack onto `main` after one PR is merged |
| Transitive dependency | A library your library depends on — you ship it too |

---

## 8. Read next

- In the code: `AppGraph.kt` (DI), `core/navigation/Routes.kt` + `Navigator.kt`, `core/ui/MviViewModel.kt`,
  `core/featureflags/FeatureFlag.kt`, `core/observability/CrashReporter.kt`, `core/storage/SecureStore.kt`.
- Reference docs: [architecture](../architecture.md) · [design system](../design-system.md) ·
  [feature flags](../feature-flags.md) · [observability](../observability.md) · [localisation](../localisation.md) ·
  [CI/CD](../ci-cd.md) · [secrets](../secrets.md) · [PR workflow](../pr-workflow.md)
- Then step 5 ([log](../log/step-5-features.md)) shows the foundation in use: onboarding, login and tabs built on
  exactly these pieces.
