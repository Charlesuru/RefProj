#!/usr/bin/env bash
# Materialises file-type secrets from environment variables for fastlane/Gradle, and removes them again.
#   scripts/ci/secret-files.sh write   # before the build
#   scripts/ci/secret-files.sh clean   # always, after the build (matters on self-hosted runners)
#
# Input (GitHub Environment secrets, passed as env vars; each is optional):
#   REFPROJ_SIGNING_KEYSTORE_BASE64  -> REFPROJ_SIGNING_STORE_FILE
#   PLAY_SERVICE_ACCOUNT_JSON        -> PLAY_SERVICE_ACCOUNT_FILE
#   FIREBASE_SERVICE_ACCOUNT_JSON    -> FIREBASE_SERVICE_ACCOUNT_FILE
# The resulting *_FILE paths are exported to later steps via $GITHUB_ENV. Values are never printed.
#
# Firebase client config goes where the build expects it, inside the checkout (both paths are gitignored):
#   GOOGLE_SERVICES_JSON_BASE64      -> app/android/google-services.json
#   GOOGLE_SERVICE_INFO_PLIST_BASE64 -> iosApp/Firebase/<Env>/GoogleService-Info.plist   (needs TARGET_ENV)
set -euo pipefail
umask 077

dir="${RUNNER_TEMP:?RUNNER_TEMP is not set}/refproj-secrets"

write_file() {
  local source_var="$1" target_var="$2" file="$3" decode="$4"
  [[ -n "${!source_var:-}" ]] || return 0
  if [[ "$decode" == base64 ]]; then
    printf '%s' "${!source_var}" | base64 --decode > "$dir/$file"
  else
    printf '%s' "${!source_var}" > "$dir/$file"
  fi
  echo "$target_var=$dir/$file" >> "$GITHUB_ENV"
  echo "secret-files: wrote $target_var"
}

firebase_android="app/android/google-services.json"
firebase_ios_dir() { # iosApp/Firebase/<Env>, from TARGET_ENV (dev -> Dev)
  local env="${TARGET_ENV:?TARGET_ENV is not set}"
  printf 'iosApp/Firebase/%s' "$(tr '[:lower:]' '[:upper:]' <<< "${env:0:1}")${env:1}"
}

write_firebase_config() {
  if [[ -n "${GOOGLE_SERVICES_JSON_BASE64:-}" ]]; then
    printf '%s' "$GOOGLE_SERVICES_JSON_BASE64" | base64 --decode > "$firebase_android"
    echo "secret-files: wrote $firebase_android"
  fi
  if [[ -n "${GOOGLE_SERVICE_INFO_PLIST_BASE64:-}" ]]; then
    local ios_dir
    ios_dir="$(firebase_ios_dir)"
    mkdir -p "$ios_dir"
    printf '%s' "$GOOGLE_SERVICE_INFO_PLIST_BASE64" | base64 --decode > "$ios_dir/GoogleService-Info.plist"
    echo "secret-files: wrote $ios_dir/GoogleService-Info.plist"
  fi
}

case "${1:-}" in
  write)
    mkdir -p "$dir"
    write_file REFPROJ_SIGNING_KEYSTORE_BASE64 REFPROJ_SIGNING_STORE_FILE upload.jks base64
    write_file PLAY_SERVICE_ACCOUNT_JSON PLAY_SERVICE_ACCOUNT_FILE play-service-account.json raw
    write_file FIREBASE_SERVICE_ACCOUNT_JSON FIREBASE_SERVICE_ACCOUNT_FILE firebase-service-account.json raw
    write_firebase_config
    ;;
  clean)
    rm -rf "$dir"
    # Workspaces persist on a self-hosted runner: never leave config behind.
    rm -f "$firebase_android"
    rm -rf iosApp/Firebase
    ;;
  *)
    echo "usage: secret-files.sh <write|clean>" >&2
    exit 1
    ;;
esac
