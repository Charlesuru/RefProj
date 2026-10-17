#!/usr/bin/env bash
# Decides whether a pull request touches anything that can affect the apps or the build.
#   scripts/ci/changes.sh <base-sha> <head-sha>
#
# Writes code=true|false to $GITHUB_OUTPUT (or stdout when run locally). code=false only when every changed
# file matches the docs allowlist below; anything else, an empty diff or an unknown path means code=true.
# When in doubt, the full pipeline runs. Keep the allowlist in sync with deploy-main.yml's paths-ignore.
set -euo pipefail

base="${1:?usage: changes.sh <base-sha> <head-sha>}"
head="${2:?usage: changes.sh <base-sha> <head-sha>}"

is_docs() {
  case "$1" in
    *.md | docs/* | .github/ISSUE_TEMPLATE/*) return 0 ;;
    *) return 1 ;;
  esac
}

# Three dots: only the PR's own changes (since the merge base), also for stacked PRs.
# --no-renames lists both sides of a rename, so moving code into docs/ still counts as a code change.
files="$(git diff --name-only --no-renames "$base...$head")"

code=false
if [[ -z "$files" ]]; then
  code=true
else
  while IFS= read -r file; do
    if ! is_docs "$file"; then
      echo "changes: code change in $file"
      code=true
      break
    fi
  done <<< "$files"
fi

echo "changes: code=$code"
echo "code=$code" >> "${GITHUB_OUTPUT:-/dev/stdout}"
