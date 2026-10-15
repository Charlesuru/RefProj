#!/usr/bin/env bash
# OWNER ONLY. Copies the release secrets from this machine into the GitHub Environments, and
# (re)creates the read-only deploy key CI uses to read the match certificates repo.
#   scripts/github/set-ci-secrets.sh
#
# Reads, never prints:
#   fastlane/.env                 APPLE_TEAM_ID, ASC_KEY_ID, ASC_ISSUER_ID, ASC_KEY_PATH, MATCH_PASSWORD,
#                                 PLAY_SERVICE_ACCOUNT_FILE, REFPROJ_SIGNING_* (if set there),
#                                 FIREBASE_SERVICE_ACCOUNT_FILE, FIREBASE_APP_ID_ANDROID_<ENV>, FIREBASE_TESTER_GROUPS
#   ~/.gradle/gradle.properties   refproj.signing.* (upload keystore path, alias, passwords)
#   Firebase client config        app/android/google-services.json, iosApp/Firebase/<Env>/GoogleService-Info.plist
# Writes (docs/secrets.md, docs/ci-cd.md):
#   repository variables          APPLE_TEAM_ID, ASC_KEY_ID, ASC_ISSUER_ID, MATCH_GIT_URL (SSH), REFPROJ_SIGNING_KEY_ALIAS,
#                                 FIREBASE_TESTER_GROUPS
#   environment variables         FIREBASE_APP_ID_ANDROID_<ENV> in dev/sit/stage
#   secrets in dev/sit/stage/prod REFPROJ_SIGNING_KEYSTORE_BASE64, REFPROJ_SIGNING_STORE_PASSWORD, REFPROJ_SIGNING_KEY_PASSWORD,
#                                 ASC_KEY_CONTENT_BASE64, MATCH_PASSWORD, MATCH_GIT_PRIVATE_KEY,
#                                 GOOGLE_SERVICES_JSON_BASE64, GOOGLE_SERVICE_INFO_PLIST_BASE64 (that env's plist)
#   secrets in dev/sit/stage      FIREBASE_SERVICE_ACCOUNT_JSON
#   secrets in prod               PLAY_SERVICE_ACCOUNT_JSON
# Re-running rotates the deploy key and overwrites the secrets with the current local values.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

repo="$(gh repo view --json nameWithOwner --jq .nameWithOwner)"
cert_repo="${MATCH_CERT_REPO:-Charlesuru/refproj-certificates}"
deploy_key_title="RefProj CI (read-only)"
environments=(dev sit stage prod)

# --- Read local configuration without exporting or printing it -----------------------------------
dotenv_value() { # $1 = name; value from fastlane/.env, parsed like dotenv (fastlane): trimmed, quotes removed
  [[ -f fastlane/.env ]] || return 0
  local line
  line="$(grep -E "^$1=" fastlane/.env | tail -n 1 || true)"
  line="${line#*=}"
  line="${line#"${line%%[![:space:]]*}"}"; line="${line%"${line##*[![:space:]]}"}"
  line="${line%\"}"; line="${line#\"}"; line="${line%\'}"; line="${line#\'}"
  printf '%s' "$line"
}
gradle_value() { # $1 = property; value from ~/.gradle/gradle.properties
  local file="$HOME/.gradle/gradle.properties"
  [[ -f "$file" ]] || return 0
  local line
  line="$(grep -E "^$1[[:space:]]*=" "$file" | tail -n 1 || true)"
  line="${line#*=}"
  printf '%s' "${line#"${line%%[![:space:]]*}"}"
}
expand_path() { local p="$1"; printf '%s' "${p/#\~/$HOME}"; }
require() { # $1 = description, $2 = value
  [[ -n "$2" ]] || { echo "✗ Missing: $1 (see docs/secrets.md)" >&2; exit 1; }
}

apple_team_id="$(dotenv_value APPLE_TEAM_ID)"
asc_key_id="$(dotenv_value ASC_KEY_ID)"
asc_issuer_id="$(dotenv_value ASC_ISSUER_ID)"
asc_key_path="$(expand_path "$(dotenv_value ASC_KEY_PATH)")"
match_password="$(dotenv_value MATCH_PASSWORD)"
play_json_path="$(expand_path "$(dotenv_value PLAY_SERVICE_ACCOUNT_FILE)")"

store_file="$(expand_path "$(dotenv_value REFPROJ_SIGNING_STORE_FILE)")"
[[ -n "$store_file" ]] || store_file="$(expand_path "$(gradle_value refproj.signing.storeFile)")"
store_password="$(dotenv_value REFPROJ_SIGNING_STORE_PASSWORD)"
[[ -n "$store_password" ]] || store_password="$(gradle_value refproj.signing.storePassword)"
key_alias="$(dotenv_value REFPROJ_SIGNING_KEY_ALIAS)"
[[ -n "$key_alias" ]] || key_alias="$(gradle_value refproj.signing.keyAlias)"
key_password="$(dotenv_value REFPROJ_SIGNING_KEY_PASSWORD)"
[[ -n "$key_password" ]] || key_password="$(gradle_value refproj.signing.keyPassword)"

firebase_json_path="$(expand_path "$(dotenv_value FIREBASE_SERVICE_ACCOUNT_FILE)")"
firebase_tester_groups="$(dotenv_value FIREBASE_TESTER_GROUPS)"
google_services_json="app/android/google-services.json"
ios_plist() { # $1 = env (dev) -> iosApp/Firebase/Dev/GoogleService-Info.plist
  printf 'iosApp/Firebase/%s/GoogleService-Info.plist' "$(tr '[:lower:]' '[:upper:]' <<< "${1:0:1}")${1:1}"
}

require APPLE_TEAM_ID "$apple_team_id"
require ASC_KEY_ID "$asc_key_id"
require ASC_ISSUER_ID "$asc_issuer_id"
require "ASC_KEY_PATH (file)" "$([[ -f "$asc_key_path" ]] && echo ok)"
require MATCH_PASSWORD "$match_password"
require "PLAY_SERVICE_ACCOUNT_FILE (file)" "$([[ -f "$play_json_path" ]] && echo ok)"
require "upload keystore (refproj.signing.storeFile)" "$([[ -f "$store_file" ]] && echo ok)"
require "refproj.signing.storePassword" "$store_password"
require "refproj.signing.keyAlias" "$key_alias"
require "refproj.signing.keyPassword" "$key_password"
require "FIREBASE_SERVICE_ACCOUNT_FILE (file)" "$([[ -f "$firebase_json_path" ]] && echo ok)"
require FIREBASE_TESTER_GROUPS "$firebase_tester_groups"
require "$google_services_json" "$([[ -f "$google_services_json" ]] && echo ok)"
for env in "${environments[@]}"; do
  require "$(ios_plist "$env")" "$([[ -f "$(ios_plist "$env")" ]] && echo ok)"
done
for env in dev sit stage; do
  name="FIREBASE_APP_ID_ANDROID_$(tr '[:lower:]' '[:upper:]' <<< "$env")"
  require "$name" "$(dotenv_value "$name")"
done

echo "This writes release secrets for $repo (environments: ${environments[*]})"
echo "and rotates the read-only deploy key on $cert_repo."
read -r -p "Continue? [y/N] " answer
[[ "$answer" == [yY] ]] || exit 1

# --- Deploy key for the match repo (read-only, this repository only) ------------------------------
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
ssh-keygen -q -t ed25519 -N "" -C "$deploy_key_title" -f "$work/match_deploy_key"
for id in $(gh api "repos/$cert_repo/keys" --jq ".[] | select(.title == \"$deploy_key_title\") | .id"); do
  gh api --silent -X DELETE "repos/$cert_repo/keys/$id"
done
gh api --silent -X POST "repos/$cert_repo/keys" -f title="$deploy_key_title" \
  -f key="$(cat "$work/match_deploy_key.pub")" -F read_only=true
echo "→ Deploy key on $cert_repo (read-only)"

# --- Repository variables (not secret) -------------------------------------------------------------
set_var() { gh variable set "$1" --repo "$repo" --body "$2" >/dev/null; echo "→ variable $1"; }
set_var APPLE_TEAM_ID "$apple_team_id"
set_var ASC_KEY_ID "$asc_key_id"
set_var ASC_ISSUER_ID "$asc_issuer_id"
set_var MATCH_GIT_URL "git@github.com:$cert_repo.git"
set_var REFPROJ_SIGNING_KEY_ALIAS "$key_alias"
set_var FIREBASE_TESTER_GROUPS "$firebase_tester_groups"
for env in dev sit stage; do
  name="FIREBASE_APP_ID_ANDROID_$(tr '[:lower:]' '[:upper:]' <<< "$env")"
  gh variable set "$name" --repo "$repo" --env "$env" --body "$(dotenv_value "$name")" >/dev/null
  echo "→ $env: variable $name"
done

# --- Environment secrets (value via stdin, never as an argument) ----------------------------------
set_secret() { # $1 = env, $2 = name; value on stdin
  gh secret set "$2" --repo "$repo" --env "$1" >/dev/null
  echo "→ $1: secret $2"
}
for env in "${environments[@]}"; do
  base64 < "$store_file" | tr -d '\n' | set_secret "$env" REFPROJ_SIGNING_KEYSTORE_BASE64
  printf '%s' "$store_password" | set_secret "$env" REFPROJ_SIGNING_STORE_PASSWORD
  printf '%s' "$key_password" | set_secret "$env" REFPROJ_SIGNING_KEY_PASSWORD
  base64 < "$asc_key_path" | tr -d '\n' | set_secret "$env" ASC_KEY_CONTENT_BASE64
  printf '%s' "$match_password" | set_secret "$env" MATCH_PASSWORD
  set_secret "$env" MATCH_GIT_PRIVATE_KEY < "$work/match_deploy_key"
  base64 < "$google_services_json" | tr -d '\n' | set_secret "$env" GOOGLE_SERVICES_JSON_BASE64
  base64 < "$(ios_plist "$env")" | tr -d '\n' | set_secret "$env" GOOGLE_SERVICE_INFO_PLIST_BASE64
done
for env in dev sit stage; do
  set_secret "$env" FIREBASE_SERVICE_ACCOUNT_JSON < "$firebase_json_path"
done
set_secret prod PLAY_SERVICE_ACCOUNT_JSON < "$play_json_path"

echo "✓ Done."
