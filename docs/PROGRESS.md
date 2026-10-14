# Progress log

Read at the start of every AI session (after `docs/PLAN.md`). Update at the end of every step.

**Next action:** Owner completes the *Step 2 — owner setup* checklist below (console work, no AI needed).
Then step 3 (model: Opus). At the start of step 3, first verify the checklist items that are marked done
(`bundle exec fastlane ios certificates`, `bundle exec fastlane android build env:prod type:release format:aab`),
then push `main` + the step-2 branch to GitHub and open the PR for step 2.

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
- Not verifiable without owner accounts: match, TestFlight, Play and Firebase uploads (see checklist).

## Step 2 — owner setup (checklist)
Owner decision: certificates repo `Charlesuru/refproj-certificates` (private) — **yes**.
- [ ] `gh auth login`; create private `Charlesuru/RefProj` (empty) and private `Charlesuru/refproj-certificates` (empty)
- [ ] `./scripts/setup.sh` (installs hooks, gems, creates `fastlane/.env` + `Local.xcconfig`)
- [ ] Apple: Team ID → `fastlane/.env` + `Local.xcconfig`; 4 App IDs registered (`com.charlesuru.refproj[.dev|.sit|.stage]`)
- [ ] App Store Connect: 4 apps (RefProj DEV/SIT/STAGE/RefProj); API key (App Manager) → `~/.secrets/refproj/`, IDs in `fastlane/.env`
- [ ] `MATCH_PASSWORD` chosen (password manager) → `bundle exec fastlane ios bootstrap_signing`
- [ ] Android upload key in `~/.secrets/refproj/upload.jks`, passwords in `~/.gradle/gradle.properties`
- [ ] Play Console: app `com.charlesuru.refproj`, Play App Signing on, first AAB uploaded manually (internal track), service account JSON
- [ ] Firebase project + Android apps for DEV/SIT/STAGE (can wait until step 4) + App Distribution service account

## Open items / decisions pending
- Jira project key: placeholder `REF` until Jira is set up (step 8).
- App icons: added with the design system (step 4).
- detekt 1.23.x with Kotlin-compiler pinning; move to detekt 2.x when stable.
