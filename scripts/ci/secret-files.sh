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

case "${1:-}" in
  write)
    mkdir -p "$dir"
    write_file REFPROJ_SIGNING_KEYSTORE_BASE64 REFPROJ_SIGNING_STORE_FILE upload.jks base64
    write_file PLAY_SERVICE_ACCOUNT_JSON PLAY_SERVICE_ACCOUNT_FILE play-service-account.json raw
    write_file FIREBASE_SERVICE_ACCOUNT_JSON FIREBASE_SERVICE_ACCOUNT_FILE firebase-service-account.json raw
    ;;
  clean)
    rm -rf "$dir"
    ;;
  *)
    echo "usage: secret-files.sh <write|clean>" >&2
    exit 1
    ;;
esac
