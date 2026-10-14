# Step 3 — CI/CD (log)

- **Date:** 2026-10-14
- **Ticket:** REF-3 (placeholder key until Jira exists)
- **Branch / PR:** `ci/REF-3-github-actions` → [#2](https://github.com/Charlesuru/RefProj/pull/2)
- **Reference docs:** [ci-cd.md](../ci-cd.md), [ADR 0006](../adr/0006-ci-cd.md), [secrets.md](../secrets.md)

## 1. Closing step 2 and publishing the repository
| What | Result |
|---|---|
| Verified match: `bundle exec fastlane ios certificates` | All certificates and profiles installed (read-only) |
| Verified Android signing: `android build env:prod type:release format:aab` | `jar verified` |
| gitleaks over the full history | Clean |
| Pushed `main` + step-2 branch, opened PR #1 | Owner approved the one-off direct push to the empty repo's `main` |

**GitHub plan finding.** The owner's account is GitHub Free. Probing the API showed that a *private* repository gets no
branch protection/rulesets (HTTP 403), no required reviewers on environments (HTTP 422), no environment secrets, no code
scanning and no dependency review, and macOS minutes count ×10.

**Decision (owner): make the repository public.** It contains no secrets and public repositories get all of the above
plus unlimited hosted runners (macOS included). The match certificates repo stays private.

**E-mail clean-up before publishing.** All commits carried the owner's personal e-mail. History was rewritten to the
GitHub noreply address (owner ran the script; the agent's guard blocks history rewrites). Because GitHub keeps
force-pushed commits reachable from the PR timeline, the repository was then **deleted and recreated as public** (owner
granted the `delete_repo` scope for this), both branches re-pushed and PR #1 reopened. Local `user.email` is now the noreply
address.

## 2. What was built
### Workflows (`.github/workflows/`)
| File | Trigger | Purpose |
|---|---|---|
| `ci.yml` | PR to `main`, push to `main` | 7 parallel jobs → one required check **`CI result`** |
| `deploy-main.yml` | push to `main` (code only) | DEV + SIT, Android + iOS |
| `release.yml` | tag `vX.Y.Z` on `main` | STAGE → PROD (approval each) → GitHub release |
| `store-release.yml` | manual, from a tag | Play production draft, App Store Connect upload (not submitted) |
| `_distribute.yml` | reusable | `fastlane <platform> <beta\|release> env:<env>` inside the GitHub Environment |

CI jobs: gitleaks · actionlint + zizmor · spotless + detekt (SARIF to code scanning) · Android host tests · Android builds
(DEV debug APK, PROD release AAB with R8) · iOS simulator tests + build · Gradle dependency graph + dependency review.

### Supporting files
- `.github/actions/setup-build` — JDK 25, Gradle cache, Ruby/Bundler, Xcode 26.6, XcodeGen, Kotlin/Native cache.
- `scripts/ci/install-tool.sh` — pinned, SHA-256-verified gitleaks, actionlint, zizmor, XcodeGen.
- `scripts/ci/secret-files.sh` — writes file secrets to `$RUNNER_TEMP` (mode 600), removes them after the job.
- `.github/dependabot.yml` — Gradle, Actions, Bundler; weekly, grouped, 7-day cooldown.
- `.github/CODEOWNERS`, `.github/pull_request_template.md`, `SECURITY.md`, `.github/zizmor.yml`.
- **Settings as code:** `.github/settings/ruleset-main.json`, `ruleset-release-tags.json`,
  `scripts/github/configure-repo.sh` (merge options, Actions policy, security features, environments, rulesets).
- **CI secrets:** `scripts/github/set-ci-secrets.sh` (owner only) — copies local secrets into the GitHub Environments
  without printing them and creates a read-only deploy key on the certificates repo.
- fastlane: new `lint` lane; empty variables in the local `.env` now also count as unset.

### Design choices (details in ADR 0006)
- CI calls the same fastlane lanes as developers — no CI-only build logic.
- Actions pinned to full commit SHAs, enforced by the repository setting; only GitHub-owned actions plus
  `gradle/actions` and `ruby/setup-ruby` are allowed.
- **Rebase-merge only** and linear history, so each Conventional Commit with its Jira key lands on `main`.
- Environments: `dev`/`sit` deploy only from `main`; `stage`/`prod` only from `v*` tags and need the owner's approval;
  only admins can create `v*` tags.
- match on CI uses a **read-only deploy key** instead of a personal access token.
- Required approvals on PRs = **0** for now (GitHub forbids approving your own PR); becomes 1 in step 8.
- Self-hosted Mac (phase 2) is an optional switch (`MACOS_RUNNER`), limited to deploy jobs; PR code never runs on it.
- Android DEV/SIT/STAGE uploads are skipped with a warning until Firebase exists (step 4).
- Tried GitHub's new `uses: $/…` self-repository syntax; reverted because actionlint 1.7.12 cannot parse it yet
  (zizmor rule disabled with a note).

## 3. Verification so far
- Locally: actionlint (with shellcheck) and zizmor — no findings; shellcheck on all scripts; CI helper scripts exercised
  with temporary paths; `spotlessCheck detekt allTests :app:android:assembleDevDebug` green.
- On GitHub (PR #2): gitleaks, workflow lint, lint, Android tests, Android build and iOS (≈8–14 min) green on the first run;
  after the build-tool pins the whole check, including dependency review, is green (`CI result` ✅).

## 4. Problems found and fixed
| Problem | Cause | Fix |
|---|---|---|
| Dependency submission: "Dependency graph is disabled" | Repository settings not applied yet | `configure-repo.sh` enables Dependabot alerts, which switches on the dependency graph |
| `configure-repo.sh` stopped with HTTP 422 at CodeQL | CodeQL "actions" needs workflows on the default branch | Step made non-fatal with a notice; re-run after PR #2 is merged |
| Dependency review: Bouncy Castle 1.80.2 — 1 critical, 1 high, 1 moderate advisory | Pulled in by AGP 9.4.1 (latest stable) on the build classpath **and** on Android Lint's tool classpath (`androidLintTool`) in every Android module | Pinned to 1.86 (version in the catalog): build classpath constraint in the root `build.gradle.kts`, lint classpath via `ToolClasspathPins.kt` in build-logic. Signed AAB/APK, Android Lint and all gates re-verified; no 1.80.2 left in any configuration |
| Dependency review: jose4j 0.9.5 (high) after the Bouncy Castle fix | AGP's bundletool on the build classpath | Reviewed the **whole** submitted graph against the GitHub Advisory Database instead of fixing one alert per CI run: also jdom2, commons-lang3, httpclient, opentelemetry-api. All pinned in one bundle `build-tool-security-pins` (build classpath + Lint + swift-export classpaths) |
| Dependency review: kotlin-gradle-plugin 2.4.10 (moderate) | Embedded in Gradle 9.8.1 (latest) for `kotlin-dsl` in build-logic | Cannot be pinned; accepted in `allow-ghsas` with reason and exit condition, listed in `docs/ci-cd.md` |
| PR #2 stopped running CI | Merge conflict after PR #1 was rebase-merged | Rebased the REF-3 commits onto the new `main` (content unchanged), force-pushed the feature branch with lease |
| Local checkout jumped to an old local `main` | Switched outside the agent (IDE or manually) right after the rebase | Switched back to `ci/REF-3-github-actions`, moved local `main` to `origin/main` |
| First Deploy DEV + SIT: iOS failed with `Missing environment variable(s): MATCH_PASSWORD` (Android skipped as planned) | A reusable workflow only receives the secrets its caller passes — environment secrets included, even though the job declares `environment:`. Not visible before real secrets existed | Callers pass `secrets: inherit` (zizmor `secrets-inherit` suppressed inline with the reason); rule "no repository-level secrets" added to `docs/ci-cd.md` |

## 5. Owner actions
- [x] Approved direct push of `main` to the empty repository
- [x] Decided: public repository
- [x] Ran the e-mail rewrite script; granted `delete_repo`; repository recreated
- [x] `scripts/github/configure-repo.sh --skip-rulesets` (environments, Actions policy, security features)
- [x] Reviewed and merged PR #1 (rebase)
- [ ] Review and merge PR #2 once `CI result` is green
- [ ] `scripts/github/configure-repo.sh` (without the flag) — rulesets for `main` and release tags, CodeQL
- [ ] `scripts/github/set-ci-secrets.sh` — CI secrets + deploy key
- [ ] Watch the first *Deploy DEV + SIT* run (iOS to TestFlight; Android skipped until step 4)
- [ ] Optional: GitHub → Settings → Emails → *Keep my email addresses private* + *Block command line pushes that expose my email*
