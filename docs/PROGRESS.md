# Progress log

Read at the start of every AI session (after `docs/PLAN.md`). Update at the end of every step.

**Next action:** iOS and Android owner setup is done (Firebase deferred to step 4). Run step 3 (model: Opus).
At the start of step 3, verify
(`bundle exec fastlane ios certificates`, `bundle exec fastlane android build env:prod type:release format:aab` → signed),
then push `main` + the step-2 branch to GitHub (`Charlesuru/RefProj`, empty, private) and open the PR for step 2.

| Step | Scope | Status |
|---|---|---|
| 1 | Foundation: repo, modules, version catalog, convention plugins, 4 environments, lint, CLAUDE.md | ✅ Done 2026-10-12 |
| 2 | Fastlane + secrets + signing (match, Play upload key) | ✅ Code done 2026-10-14 · ⏳ owner setup |
| 3 | CI/CD (GitHub Actions → later self-hosted Mac runner) | ⏳ Next |
| 4 | Core: design system, Koin, Nav3, feature flags, observability (Firebase), localisation, domain/data | |
| 5 | Features: onboarding, login, tabbed logged-in shell | |
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

## Open items / decisions pending
- Play Console moved *App integrity* to the *Protected with Play* page; the signing-certificate view was not found there.
  Upload-key registration is instead proven by the successful API upload above.
- A draft internal release (from the API upload test) may remain in Play Console; owner can discard it.
- Jira project key: placeholder `REF` until Jira is set up (step 8).
- App icons: added with the design system (step 4).
- detekt 1.23.x with Kotlin-compiler pinning; move to detekt 2.x when stable.
