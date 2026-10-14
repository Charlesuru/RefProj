# Secrets inventory

Every secret this project uses: what it is, where it lives, who needs it and how to rotate it.
**If you add a secret, add it here, to `fastlane/.env.example`, and (if it is a file type) to
`.gitignore` and `.gitleaks.toml` — in the same PR.**

## Ground rules
- Secrets live in exactly three places (see `CLAUDE.md`):
  1. **CI:** GitHub Environment secrets — `dev`, `sit`, `stage`, `prod`. `prod` (and `stage`) require reviewer approval.
  2. **Local:** `fastlane/.env`, `~/.gradle/gradle.properties`, `iosApp/Configs/Local.xcconfig` (all gitignored),
     and key files under `~/.secrets/refproj/` (`chmod 700`), **outside** the repository.
  3. **iOS signing:** fastlane match, in the private encrypted repo `Charlesuru/refproj-certificates`.
- Nothing in `AppConfig`, `BuildConfig`, `*.xcconfig` or `Info.plist` is secret — it ships inside the app.
- Never print, log or paste a secret. fastlane and Gradle only read them from the environment.
- Leaked or suspected leaked? **Rotate first, investigate second.** Removing a commit does not un-leak a secret.

## Protection layers
| Layer | What it does |
|---|---|
| `.gitignore` | Key, certificate, profile, `.env` and Firebase config files are never staged by accident |
| `.githooks/pre-commit` | `gitleaks git --staged` blocks commits containing secrets (installed by `scripts/setup.sh`) |
| CI | `gitleaks` on every PR and push to `main` + GitHub secret scanning and push protection ([ci-cd.md](ci-cd.md#security-model)) |
| Play App Signing | We only hold the **upload** key; Google holds the app signing key. A lost/leaked upload key is resettable |
| fastlane match | Certificates/profiles are AES-encrypted at rest; CI gets read-only access |
| GitHub Environments | Secrets are scoped per environment; `dev`/`sit` accept only `main`, `stage`/`prod` only `v*` tags and need a human approval |

## Inventory

**Secret** = must be protected (GitHub *environment secret*). **Config** = not secret (GitHub *variable*: repository-level
unless marked per environment). CI values are copied from the owner's machine by `scripts/github/set-ci-secrets.sh`.

| Variable | Kind | What | Local | CI (GitHub Environment) | Rotate |
|---|---|---|---|---|---|
| `REFPROJ_SIGNING_STORE_FILE` | Secret (file) | Android **upload** keystore (`.jks`) | `~/.secrets/refproj/upload.jks` | `REFPROJ_SIGNING_KEYSTORE_BASE64` (decoded to a temp file by the workflow) — all envs | [Upload key](#android-upload-key) |
| `REFPROJ_SIGNING_STORE_PASSWORD` | Secret | Keystore password | `~/.gradle/gradle.properties` as `refproj.signing.storePassword`, or `fastlane/.env` | all envs | with the key |
| `REFPROJ_SIGNING_KEY_ALIAS` | Config | Key alias (`upload`) | as above (`refproj.signing.keyAlias`) | repository variable | — |
| `REFPROJ_SIGNING_KEY_PASSWORD` | Secret | Key password | as above (`refproj.signing.keyPassword`) | all envs | with the key |
| `PLAY_SERVICE_ACCOUNT_FILE` | Secret (file) | Google Play service account JSON (release manager on this app only) | `~/.secrets/refproj/play-service-account.json` | `PLAY_SERVICE_ACCOUNT_JSON` — `prod` only | [Service accounts](#google-service-accounts) |
| `FIREBASE_SERVICE_ACCOUNT_FILE` | Secret (file) | Firebase service account JSON (*Firebase App Distribution Admin* role) | `~/.secrets/refproj/firebase-service-account.json` | `FIREBASE_SERVICE_ACCOUNT_JSON` — `dev`, `sit`, `stage` | [Service accounts](#google-service-accounts) |
| `FIREBASE_APP_ID_ANDROID_<ENV>` | Config | Firebase Android app ID per environment (`1:…:android:…`). Unset → CI skips that Android upload | `fastlane/.env` | environment variable — `dev`, `sit`, `stage` | — |
| `FIREBASE_TESTER_GROUPS` | Config | App Distribution tester group aliases | `fastlane/.env` | repository variable | — |
| `APPLE_TEAM_ID` | Config | Apple Developer Team ID (10 chars) | `fastlane/.env` + `Local.xcconfig` (`DEVELOPMENT_TEAM`) | repository variable | — |
| `ASC_KEY_ID` | Config | App Store Connect API key ID | `fastlane/.env` | repository variable | with the key |
| `ASC_ISSUER_ID` | Config | App Store Connect issuer ID | `fastlane/.env` | repository variable | — |
| `ASC_KEY_PATH` | Secret (file) | App Store Connect API key `.p8` (role: *App Manager*) | `~/.secrets/refproj/AuthKey_<ID>.p8` | — | [ASC key](#app-store-connect-api-key) |
| `ASC_KEY_CONTENT_BASE64` | Secret | Same `.p8`, base64 | — | all envs | [ASC key](#app-store-connect-api-key) |
| `MATCH_GIT_URL` | Config | Certificates repo: HTTPS locally, SSH (`git@github.com:Charlesuru/refproj-certificates.git`) on CI | `fastlane/.env` | repository variable | — |
| `MATCH_PASSWORD` | Secret | Passphrase that encrypts the match repo | `fastlane/.env` + password manager | all envs | [match](#ios-certificates-fastlane-match) |
| `MATCH_GIT_PRIVATE_KEY` | Secret | Private half of the **read-only deploy key** on the certificates repo (ed25519) | — | all envs | [match](#ios-certificates-fastlane-match) |
| `MACOS_RUNNER` | Config | Label of the optional self-hosted Mac for iOS deploys ([ci-cd.md](ci-cd.md#self-hosted-mac-runner-optional)); unset = GitHub-hosted | — | repository variable | — |
| `google-services.json` / `GoogleService-Info.plist` | Config (treated as secret) | Firebase client config per env (step 4) | `app/android/src/<env>/`, `iosApp/Firebase/<Env>/` | base64 secrets per env | Regenerate in Firebase console |

Firebase client config files contain API keys that are public by design (restricted by app ID / bundle ID),
but we keep them out of git so a public fork never points at our Firebase project.

## Rotation runbooks

### Android upload key
1. Generate a new key (`keytool -genkeypair -v -keystore ~/.secrets/refproj/upload-new.jks -storetype PKCS12 -alias upload -keyalg RSA -keysize 4096 -validity 9125`).
2. Export the certificate: `keytool -export -rfc -keystore ~/.secrets/refproj/upload-new.jks -alias upload -file upload_certificate.pem`.
3. Play Console → *Test and release → App integrity → Upload key → Request upload key reset*, attach the `.pem`.
4. When Google confirms, update the local file/passwords, run `scripts/github/set-ci-secrets.sh` (all environments). Delete the old key and the `.pem`.

### Google service accounts
1. Google Cloud console → *IAM → Service accounts* → the account → *Keys → Add key → JSON*.
2. Replace the local file and the CI secret (`PLAY_SERVICE_ACCOUNT_JSON` via `scripts/github/set-ci-secrets.sh` /
   `FIREBASE_SERVICE_ACCOUNT_JSON`).
3. Run one upload (`bundle exec fastlane android beta env:dev`) to verify, then **delete the old key** in the console.

### App Store Connect API key
1. App Store Connect → *Users and Access → Integrations → App Store Connect API → Team Keys → +* (role *App Manager*).
2. Download the `.p8` (only possible once) to `~/.secrets/refproj/`, update `ASC_KEY_ID` and `ASC_KEY_PATH` in `fastlane/.env`,
   then run `scripts/github/set-ci-secrets.sh`.
3. Verify (`bundle exec fastlane ios certificates`), then **revoke** the old key.

### iOS certificates (fastlane match)
- **Certificate expiry (yearly):** `bundle exec fastlane match nuke distribution` (owner only, revokes the cert), then
  `bundle exec fastlane ios bootstrap_signing`. CI picks up the new cert automatically.
- **New passphrase:** `bundle exec fastlane match change_password`, update `MATCH_PASSWORD` in `fastlane/.env`, run
  `scripts/github/set-ci-secrets.sh`.
- **CI deploy key:** run `scripts/github/set-ci-secrets.sh`. It generates a new key, replaces the deploy key
  *RefProj CI (read-only)* on the certificates repo and updates `MATCH_GIT_PRIVATE_KEY` in all environments.

## If a secret is committed
1. **Rotate it immediately** using the runbook above. Treat it as public from the moment it was pushed.
2. Remove it from the working tree and add a test/rule to `.gitleaks.toml` if it was not caught.
3. History rewriting is a separate, coordinated decision (force-push to `main` is otherwise forbidden). Rotation is what protects us.
4. Write a short incident note in the PR.
