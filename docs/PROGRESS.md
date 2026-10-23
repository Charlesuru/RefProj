# Progress log

Read at the start of every AI session (after `docs/PLAN.md`). Update at the end of every step.

**Next action:** all nine plan steps are done. New work starts from a Jira ticket (`/ticket`); the owner's open
checklist items and *Open items* below are the backlog. Overview of the whole setup: [overview.md](overview.md).
Detailed step logs: `docs/log/`.

| Step | Scope | Status |
|---|---|---|
| 1 | Foundation: repo, modules, version catalog, convention plugins, 4 environments, lint, CLAUDE.md | ✅ Done 2026-10-12 |
| 2 | Fastlane + secrets + signing (match, Play upload key) | ✅ Done 2026-10-14 (PR #1; Firebase → step 4) |
| 3 | CI/CD (GitHub Actions → later self-hosted Mac runner) | ✅ Done 2026-10-14 (PRs #2–#5) |
| 4 | Core: design system, Koin, Nav3, feature flags, observability (Firebase), localisation, domain/data | ✅ Done 2026-10-15 (PRs #8–#15) |
| 5 | Features: onboarding, login, tabbed logged-in shell | ✅ Done 2026-10-16 (PRs #18–#22) |
| 6 | Debug menu | ✅ Done 2026-10-21 (REF-10, PRs #27–#30) |
| 7 | Tests: unit, integration, UI, screenshot, architecture + coverage | ✅ Done 2026-10-21 (REF-11, PRs #31–#37) |
| 8 | Jira integration + AI workflow (spec → ticket → code → PR → AI review) | ✅ Done 2026-10-22 (REF-12, PRs #38–#42) |
| 9 | Final summary & docs | ✅ Done 2026-10-23 (REF-13) |

## Step 1 — verified
- `./gradlew :app:android:assembleDevDebug allTests spotlessCheck detekt` → green
- iOS `RefProj-Sit` built for simulator and launched, showing "Environment: SIT"

## Step 2 — verified
- Branch `build/REF-2-fastlane-secrets-signing` (local; no GitHub remote yet). `REF-2` = placeholder ticket key for plan step 2.
- `bundle exec fastlane android build env:dev` → debug APK; `env:sit type:release format:aab` with a throwaway upload key
  → AAB `jar verified`; without key → `*-unsigned.apk` and `android beta` refuses to upload.
- `bundle exec fastlane ios build env:dev` → `BUILD SUCCEEDED` (simulator, unsigned).
- Versioning: no tag → `0.1.0`; tag on HEAD `v1.4.2` → `1.4.2`; tag on HEAD~1 → `1.5.0`. STAGE/PROD lanes refuse untagged HEAD.
- gitleaks: repo history + working tree clean; custom rules catch passwords, base64 key material, `.jks` files; no false
  positives on empty template values or `${{ secrets.X }}`.
- commit-msg hook accepts/rejects the expected subjects. Gates: spotlessCheck, detekt, allTests, assembleDevDebug green.
- Domain corrected to `com.studiodraug` (bundle IDs, applicationIds, Kotlin packages); GitHub stays `Charlesuru`.
  Both platforms rebuilt and verified (`com.studiodraug.refproj.dev`).
- Fixed: Spotless walked the whole tree (incl. `vendor/bundle` and dangling Xcode symlinks in `build/`);
  lint targets are now pre-filtered file trees with LF line endings.
- Not verifiable without owner accounts: match, TestFlight, Play and Firebase uploads (see checklist).

## Step 2 — owner setup (checklist)
Owner decision: certificates repo `Charlesuru/refproj-certificates` (private) — **yes**.
- [x] `gh auth login` / repos / `./scripts/setup.sh` — owner reports done 2026-10-14 (verify at step 3 start)
- [x] Apple: Team ID → `fastlane/.env` + `Local.xcconfig`; 4 App IDs registered (`com.studiodraug.refproj[.dev|.sit|.stage]`)
- [x] App Store Connect API key (App Manager) → `~/.secrets/refproj/`, IDs in `fastlane/.env`
- [x] App Store Connect: 4 apps (RefProj DEV/SIT/STAGE/RefProj) created by owner 2026-10-14
- [x] `MATCH_PASSWORD` chosen → `ios bootstrap_signing` done 2026-10-14: 1 Apple Distribution cert + 4 App Store
      profiles in `Charlesuru/refproj-certificates`; read-only `ios certificates` lane verified
- [x] Owner ran `bundle exec fastlane ios archive env:dev` — signed archive OK 2026-10-14
- [x] Android upload key in `~/.secrets/refproj/upload.jks` (PKCS12, alias `upload`, RSA 4096, valid to 2051,
      cert SHA-256 `0B:87:58:0F:…:24:E6`), passwords in `~/.gradle/gradle.properties` — 2026-10-14
- [x] Play Console: app `com.studiodraug.refproj` created, first AAB (versionCode 412532) uploaded manually and released
      to the internal track; Play App Signing is implicit for new AAB apps (Google holds the app signing key) — 2026-10-14
- [x] Google Cloud project `refproj` (Play Android Developer API enabled), service account
      `play-publisher@refproj.iam.gserviceaccount.com` (no project roles; Play Console permissions on RefProj only:
      release to production + testing tracks). JSON key → `~/.secrets/refproj/play-service-account.json`, path in
      `fastlane/.env`. Verified 2026-10-14: `validate_play_store_json_key` OK, and an API upload of a newly built AAB to
      the internal track as **draft** succeeded (confirms upload key + service account + permissions end to end)
- [ ] Firebase project + Android apps for DEV/SIT/STAGE + App Distribution service account — deferred to step 4
      (reuse Google Cloud project `refproj`)

## Step 3 — verified
- Repository is **public** (owner decision; GitHub Free gives no protections to private repos — ADR 0006). History uses
  the owner's noreply e-mail; the repository was recreated so no old commit object is reachable.
- PR #2 `CI result` green: gitleaks, actionlint + zizmor, spotless + detekt (SARIF), Android tests, Android builds
  (DEV APK, PROD AAB/R8), iOS simulator tests + build (≈6 min warm), Gradle dependency graph + dependency review.
- Dependency review found vulnerable libraries in the build toolchain (AGP 9.4.1, Lint, Kotlin swift-export); pinned in
  the `build-tool-security-pins` bundle. Accepted: GHSA-r937-wjx7-w2jp (Kotlin embedded in Gradle 9.8.1). See
  `docs/ci-cd.md#vulnerable-build-tooling`.
- Applied by the owner: `configure-repo.sh --skip-rulesets` (merge options, Actions policy with enforced SHA pinning,
  security features, environments dev/sit → `main`, stage/prod → `v*` tags + reviewer).
- Rulesets `main` + `release-tags` active; CodeQL default setup (actions) configured; CI secrets in all four
  environments (names checked, values never read); read-only deploy key on the certificates repo.
- **Deploy DEV + SIT** (run 38608250712, `60e8fee`): all jobs green. iOS DEV + SIT uploaded to TestFlight (match via
  deploy key, signing, upload with the ASC API key); Android skipped as planned until Firebase (step 4).
- The first deploy runs found three issues, fixed in PRs #3–#5: environment secrets need `secrets: inherit` for the
  reusable job, `set-ci-secrets.sh` now trims `.env` values like dotenv, and the app needed icons (placeholders).
- Not exercised yet: `release.yml` (STAGE → PROD) and `store-release.yml`. First real run with the first `v*` tag.

## Step 3 — owner checklist
- [x] Public repository, e-mail clean-up, PR #1 merged (rebase)
- [x] `scripts/github/configure-repo.sh --skip-rulesets`
- [x] Merge PR #2 (**Rebase and merge**)
- [x] `scripts/github/configure-repo.sh` (rulesets `main` + `release-tags`, CodeQL for workflows)
- [x] `scripts/github/set-ci-secrets.sh` (CI secrets + read-only deploy key on the certificates repo)
- [x] First *Deploy DEV + SIT* run: iOS DEV + SIT in TestFlight; Android skipped with a warning (Firebase in step 4)
- [x] Dependabot alert #1 dismissed as *tolerable risk* (matches `allow-ghsas`)
- [ ] TestFlight: internal testing group per app (DEV, SIT) with *automatic distribution*, so new builds reach testers
- [ ] GitHub → Settings → Emails: *Keep my email addresses private* + *Block command line pushes that expose my email*
- [ ] Optional: delete the local branch `build/REF-2-fastlane-secrets-signing` (merged as PR #1)

## Step 4 — verified
- Stacked PRs #8 (design system) → #9 (core + Koin) → #10 (navigation + l10n) → #11 (feature flags) → #12 (Firebase)
  → #15 (iOS archive signing fix) → #13 (`deploy:dev` + SIT nightly) → #14 (docs), each restacked onto `main` after the
  previous rebase-merge. Log: [step-4-core.md](log/step-4-core.md), decisions: ADR 0007.
- Gates green on every branch; Android (emulator) and iOS (simulator) DEV run in English and Swedish; Firebase initialises
  on both platforms with the owner's config, and both build/run without it.
- Dependency review caught Guava 31.1-android shipped via Firebase Analytics → pinned to 33.7.2-android.
- **Deploys with Firebase verified** (run 38711322510): Android DEV + SIT in Firebase App Distribution (owner installed
  both via App Tester), iOS DEV + SIT in TestFlight; Firebase config written from environment secrets; Crashlytics dSYM
  upload validated in a local signed archive.
- The first Firebase deploy failed on iOS: signing passed as xcargs hit the Swift package resource bundles → fixed in #15
  (manual signing on the app target only).
- Not exercised yet: a `deploy:dev` PR preview and a *Deploy SIT* run (nightly/manual).

## Step 4 — owner checklist
- [x] Firebase project on Google Cloud `refproj` (Analytics on), 4 Android + 4 iOS apps; `app/android/google-services.json`
      and `iosApp/Firebase/<Env>/GoogleService-Info.plist` downloaded (checked against bundle IDs 2026-10-15)
- [ ] Budget alert on the billing account (or unlink billing → Spark); Crashlytics/Analytics/App Distribution are free
- [x] App Distribution onboarded for DEV, SIT, STAGE; group `internal` with testers; App Tester on the owner's device
- [x] Service account `firebase-distribution` (*Firebase App Distribution Admin*) → `~/.secrets/refproj/`; `fastlane/.env`
      filled; read-only API check OK for DEV/SIT/STAGE
- [x] Merged #8 → #12, #15, #13, #14 (**Rebase and merge**), restacked after each merge
- [x] `scripts/github/configure-repo.sh` (dev accepts `refs/pull/*/merge`, label `deploy:dev`) — verified via the API
- [x] `scripts/github/set-ci-secrets.sh` — Firebase secrets/variables present in the right environments (names checked)
- [x] First deploys with Firebase: Android → App Distribution, iOS → TestFlight (DEV + SIT)
- [ ] Optional: try `deploy:dev` on a PR; run *Deploy SIT* manually once

## Step 5 — verified
- Stacked PRs #18 (auth + consent, SecureStore hardening) → #19 (Room tasks data) → #20 (onboarding, login, shell,
  profile) → #21 (tasks tab, home) → #22 (docs). Log: [step-5-features.md](log/step-5-features.md), decision: ADR 0009.
- Gates green on every branch. Android DEV (emulator): full flow incl. restart, back handling, logout deleting tasks.
  Android PROD release (R8): flow + Room work. iOS DEV (simulator, ad-hoc signed): launches in Swedish, Keychain OK.
- Running the iOS app found a launch crash (Keychain `-34018` in the unsigned simulator build, thrown while restoring
  the session) → `SecureStore` never throws; simulator builds are signed ad hoc.
- Not exercised: the iOS flow clicked through by hand (no UI automation here), Analytics events in Firebase DebugView.

## Step 5 — owner checklist
- [x] Merge #18 → #19 → #20 → #21 → #22 (**Rebase and merge**), one at a time — merged 2026-10-16/17
- [x] Watch the CI iOS job on #18: first CI run of the ad-hoc signed simulator build
- [ ] Try the flow on a device (DEV build from App Distribution / TestFlight after merge): test account
      `test@example.com` / `Password1` (public demo values of the fake backend, `docs/features.md`)
- [ ] Optional: Firebase DebugView with a release build to see `tutorial_complete`, `login`, `task_added` after opting in

## Between steps — REF-6 (CI skips build/test for docs-only PRs)
- `ci.yml` job *Detect changes* (`scripts/ci/changes.sh`) skips Android/iOS test + build and dependency review when every
  changed file is docs; secret scan, workflow lint and lint still run. `docs/ci-cd.md#docs-only-pull-requests`.
- Verified locally: the script reports docs-only for `130a958` (docs only), code for `12d9b76`, code for an empty diff.
- [ ] Owner: after merge, check that the next docs-only PR shows the four jobs as *skipped* and `CI result` green

## Between steps — REF-7 (read-only repo-guide agent)
- `.claude/agents/repo-guide.md`: a Q&A agent run as its own session (`claude --agent repo-guide`), so it can be
  `/clear`ed independently of the working session. It overrides the session protocol in CLAUDE.md §0 and never
  changes anything: tools are `Read`, `Grep`, `Glob`, `Bash`, and a `PreToolUse` hook
  (`.claude/hooks/read-only-git.sh`) only lets single read-only git commands through.
- Verified locally: the hook allows `git log/blame/status`, blocks writes, chaining, pipes, substitution,
  `git -c …`, `--output` and `grep -O`; the agent answered a history question with sources and refused to create files.

## Step 6 — verified
- Stacked PRs (REF-10): foundations → `feature:debug` → screen catalog → docs. Log:
  [step-6-debug-menu.md](log/step-6-debug-menu.md), decision: ADR 0010, guide: [debug-menu.md](debug-menu.md).
- Gates green on every branch. `AppGraphTest` proves a PROD release graph has no debug tools.
- Android DEV (emulator): every menu item exercised, incl. the test crash. Android PROD release (R8): no badge, empty
  login. iOS DEV (simulator): launches with the badge; not clicked through (no UI automation here).

## Step 6 — owner checklist
- [x] Merge the four REF-10 PRs in order (**Rebase and merge**) — #27–#30 merged 2026-10-21
- [ ] On a device (DEV from App Distribution / TestFlight after merge): open the menu via the `DEV` tab, try
      *Sign in as test user* and the screen catalog on iOS (not clicked through by the AI)
- [ ] Optional: SIT release build → *Crash the app*, reopen the app, check that the crash shows up in Crashlytics

## Step 7 — verified
- Stacked PRs (REF-11): #31 Kover → #32 architecture → #33 integration → #34 device tests → #35 UI → #36 screenshots →
  docs. Log: [step-7-tests.md](log/step-7-tests.md), decision: ADR 0011, guide: [testing.md](testing.md).
- Gates green on every branch. 53 labelled test classes; 229 Android host + 157 iOS simulator + 18 architecture tests,
  5 device tests. Coverage 89 % total, floors per module in `config/kover/coverage-floors.properties`.
- UI tests and the end-to-end `AppFlowTest` run on the iOS simulator too: closes the step-6 gap "iOS not clicked through".
- CI green on every PR; screenshot references recorded on macOS match Linux CI byte for byte (no tolerance needed).
- Device tests green on the local emulator and on a Gradle Managed Device; the nightly workflow runs first after merge.
- Android DEV and iOS DEV launched and run after the changes (Android: login, task survives restart).

## Step 7 — owner checklist
- [x] Merge #31 → #37 — #31 by the owner; #32–#37 restacked and merged by the AI with `--admin` after green CI (owner's
      one-off decision, see the step-7 log)
- [ ] Check the **Test (Android host)** job summary on a PR: the coverage table
- [ ] After merge: Actions → **Device tests** → *Run workflow* once on `main`, check it is green (then nightly Mon–Fri)
- [ ] Optional: open a screenshot test failure artifact once (`test-reports-android`) to see actual/diff images

## Step 8 — verified
- Stacked PRs (REF-12): #38 conventions → #39 skills + MCP → #40 CI review → #41 ADR + Jira docs → docs PR. Log:
  [step-8-jira-ai-workflow.md](log/step-8-jira-ai-workflow.md), decision: ADR 0012, guides: [ai-workflow.md](ai-workflow.md),
  [jira.md](jira.md).
- Jira project `REF` live; REF-1…REF-11 recreated in order (*Klart*), REF-12 for this step; AI access through the
  Atlassian MCP server.
- Required check *Conventions* green on #38–#41 and enforced by the ruleset; script tested against bad input.
- Owner decision: **no GitHub identity for the AI** — the developer who opens a PR owns its code; 0 required approvals;
  the AI never approves, merges or uses `--admin` (CLAUDE.md §3.7). Settles the step-7 open item.
- `configure-repo.sh` applied and checked; `CLAUDE_CODE_OAUTH_TOKEN` in the `ai-review` environment.
- AI review on #42 (label `ai-review`): Claude Haiku posted a verdict, one nit and what it could not verify; label removed.
  It first needed `oven-sh/setup-bun` (run inside `claude-code-action`) allow-listed by exact commit.

## Step 8 — owner checklist
- [x] Jira Cloud site + project RefProj (`REF`), work type *Bug*, REF-3 converted to a task
- [x] Atlassian MCP signed in (`/mcp`)
- [x] Merge #38 → #41 (rebase), `scripts/github/configure-repo.sh`, Claude token (`claude setup-token` → `ai-review`)
- [x] Add the label **`ai-review`** to a PR once and read Claude's comment — #42: summary comment posted, label removed
- [x] Install **GitHub for Atlassian** (formerly GitHub for Jira) on `Charlesuru/RefProj` only — REF-12 shows its
      branches, commits and 5 PRs
- [x] Jira flows *Start work* (branch created → In Progress), *Review* (PR created → In Review), *Done* (PR merged →
      Done) ([jira.md](jira.md#automation-space-settings--automation))
- The GitHub App `refproj-ai` was never created — nothing to delete

## Step 9 — verified
- `docs/overview.md`: the final summary of the whole setup, one section per point of the owner's brief, each linking to
  its detailed doc/ADR; Mermaid module graph, CI/CD pipeline and AI loop, test pyramid. README links to it first.
  Log: [step-9-summary.md](log/step-9-summary.md).
- Whole-repo review against the code: every relative link and anchor in the 44 tracked Markdown files resolves;
  module list, workflows, lanes, test counts (53 labelled classes, 75 screenshot references), coverage floors and
  versions match; gitleaks over the full history is clean; no secret or generated file is tracked.
- Doc drift fixed: CLAUDE.md project map (`tests/architecture`, `config/kover`), README doc list and JDK note, stale
  "awaiting merge" in steps 5 and 6.

## Step 9 — owner checklist
- [x] Read `docs/overview.md` on GitHub (the Mermaid diagrams render there) and merge the REF-13 PR (**Rebase and merge**)
- [x] Pick the next work from the open checklist items above and *Open items* below; each becomes a ticket via `/ticket`
      — REF-14 (epic, due dates)

## After step 9 — REF-20 (a story may span several PRs)
- First epic of the manual walkthrough: REF-14 *Task due dates and reminders*, first story REF-15.
- A story is often delivered in several small PRs, so the owner turned off the Jira flows *Review* and *Done*
  (2026-10-23); only *Start work* is active. In Review (last PR opened) and Done (acceptance criteria met, by the PO)
  are set by hand; no sub-tasks just to map PRs. `/ticket`, `/implement` and the docs follow:
  [jira.md](jira.md#one-story-several-pull-requests).

## Open items / decisions pending
- Play Console moved *App integrity* to the *Protected with Play* page; the signing-certificate view was not found there.
  Upload-key registration is instead proven by the successful API upload above.
- A draft internal release (from the API upload test) may remain in Play Console; owner can discard it.
- Merging Claude co-authored commits: in step 7 the AI's `--admin` merge hit an "extra approval for unattributed
  changes". In step 8 the owner merged #38–#41 in the web UI without it. If it ever blocks again, look at the
  ruleset's Copilot/AI settings rather than adding a bypass.
- Build-tool security pins and the accepted GHSA: re-check on every AGP/Kotlin/Gradle upgrade.
- Switch `uses: ./…` to `uses: $/…` once actionlint supports it (then drop the zizmor exception).
- App icons are **placeholders** (white "R", colour + label per environment), generated by
  `scripts/assets/generate-placeholder-icons.swift`. Owner decision (step 4): keep them until a real logo exists;
  Play also needs a 512 px store icon before the first production release.
- Analytics consent: resolved in step 5 (opt-in). A privacy policy text/link is still needed before the first PROD release.
- Auth backend is `FakeAuthApi`: replace with a Ktor `AuthApi` when a backend exists; the token is not yet attached to
  HTTP requests (no API needs it). The test account reaches the debug tools through
  `TestAccountProvider` (step 6); a real backend's test accounts must come from a non-PROD source (ADR 0010).
- Room: on-device tests and the migration test exist (step 7). The Android migration helper is not used: the KMP
  Android library plugin does not package schemas as test assets; migrations are tested on iOS with the same schemas.
- Debug menu: debug screens pushed on the root back stack are not restored after process death (by design). The iOS
  flow is covered by `AppFlowTest` on the simulator (step 7).
- Local tooling: run Gradle outside Claude Code's sandbox — a sandboxed daemon quarantines Kotlin/Native test binaries
  and Gatekeeper blocks them (step-6 log).
- Nav3: a tab's ViewModels are recreated when returning to it (state rebuilt from repositories); revisit if a screen
  needs to keep transient state across tab switches.
- Firebase iOS SDK is updated manually (Dependabot cannot read XcodeGen specs) — `docs/observability.md#versions`.
- App runtime security pin Guava 33.7.2-android: drop when Firebase Analytics requires ≥ 32.
- Secure storage: Android Keystore tested on a device (nightly). The real iOS Keychain cannot run in the simulator
  test process; needs an app-hosted test (XCUITest target) if it should be automated (`docs/testing.md#known-gaps`).
- Coverage floors are raised by hand (ratchet as a review rule); consider automating with a bot comment later.
- Robolectric emulates up to API 36; bump `ROBOLECTRIC_SDK` (core:testing) with Robolectric upgrades.
- One Firebase project for all environments (ADR 0007); revisit if PROD needs separate access control.
- detekt 1.23.x with Kotlin-compiler pinning; move to detekt 2.x when stable.
- Confirm the Dependabot bundler job is green after the Gemfile fix (next weekly run).
- License policy (ADR 0008): owner to decide on a build-time license plugin (e.g. `app.cash.licensee`) that also
  covers dependencies without GitHub license data; an open-source notices screen is still to be built (not part of the debug menu; later step).
