# Progress log

Read at the start of every AI session (after `docs/PLAN.md`). Update at the end of every step.

**Next action:** owner merges the step-5 stack #18 → #19 → #20 → #21 → #22 (rebase, one by one; AI restacks), then
step 6 — debug menu (model: Sonnet).
Detailed step logs: `docs/log/`.

| Step | Scope | Status |
|---|---|---|
| 1 | Foundation: repo, modules, version catalog, convention plugins, 4 environments, lint, CLAUDE.md | ✅ Done 2026-10-12 |
| 2 | Fastlane + secrets + signing (match, Play upload key) | ✅ Done 2026-10-14 (PR #1; Firebase → step 4) |
| 3 | CI/CD (GitHub Actions → later self-hosted Mac runner) | ✅ Done 2026-10-14 (PRs #2–#5) |
| 4 | Core: design system, Koin, Nav3, feature flags, observability (Firebase), localisation, domain/data | ✅ Done 2026-10-15 (PRs #8–#15) |
| 5 | Features: onboarding, login, tabbed logged-in shell | ✅ Done 2026-10-16 (PRs #18–#22, awaiting merge) |
| 6 | Debug menu | |
| 7 | Tests: unit, integration, UI, screenshot, architecture + coverage | |
| 8 | Jira integration + AI workflow (spec → ticket → code → PR → AI review) | |
| 9 | Final summary & docs | |

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
- [ ] Merge #18 → #19 → #20 → #21 → #22 (**Rebase and merge**), one at a time; ask the AI to restack after each merge
- [ ] Watch the CI iOS job on #18: first CI run of the ad-hoc signed simulator build
- [ ] Try the flow on a device (DEV build from App Distribution / TestFlight after merge): test account
      `test@example.com` / `Password1` (public demo values of the fake backend, `docs/features.md`)
- [ ] Optional: Firebase DebugView with a release build to see `tutorial_complete`, `login`, `task_added` after opting in

## Open items / decisions pending
- Play Console moved *App integrity* to the *Protected with Play* page; the signing-certificate view was not found there.
  Upload-key registration is instead proven by the successful API upload above.
- A draft internal release (from the API upload test) may remain in Play Console; owner can discard it.
- Jira project key: placeholder `REF` until Jira is set up (step 8).
- Required PR approvals are 0 until the AI has its own GitHub identity (step 8) → then 1 in `ruleset-main.json`.
- Build-tool security pins and the accepted GHSA: re-check on every AGP/Kotlin/Gradle upgrade.
- Switch `uses: ./…` to `uses: $/…` once actionlint supports it (then drop the zizmor exception).
- App icons are **placeholders** (white "R", colour + label per environment), generated by
  `scripts/assets/generate-placeholder-icons.swift`. Owner decision (step 4): keep them until a real logo exists;
  Play also needs a 512 px store icon before the first production release.
- Analytics consent: resolved in step 5 (opt-in). A privacy policy text/link is still needed before the first PROD release.
- Auth backend is `FakeAuthApi`: replace with a Ktor `AuthApi` when a backend exists; the token is not yet attached to
  HTTP requests (no API needs it). Step 6 debug menu: pre-fill the test account (credentials live in `data`; expose them
  via a debug-only binding, not to features directly).
- Room: Android on-device database tests and a first migration test (step 7).
- Nav3: a tab's ViewModels are recreated when returning to it (state rebuilt from repositories); revisit if a screen
  needs to keep transient state across tab switches.
- Firebase iOS SDK is updated manually (Dependabot cannot read XcodeGen specs) — `docs/observability.md#versions`.
- App runtime security pin Guava 33.7.2-android: drop when Firebase Analytics requires ≥ 32.
- Secure storage (`AndroidKeystoreSecureStore`, `KeychainSecureStore`) needs on-device tests (step 7).
- One Firebase project for all environments (ADR 0007); revisit if PROD needs separate access control.
- detekt 1.23.x with Kotlin-compiler pinning; move to detekt 2.x when stable.
- Confirm the Dependabot bundler job is green after the Gemfile fix (next weekly run).
- License policy (ADR 0008): owner to decide on a build-time license plugin (e.g. `app.cash.licensee`) that also
  covers dependencies without GitHub license data; an open-source notices screen is still to be built (step 6 or later).
