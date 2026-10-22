#!/usr/bin/env bash
# Checks a pull request against the naming conventions in scripts/conventions.sh: the title, the branch name and
# every commit subject in the PR. The commit-msg hook only runs locally; this is the check nobody can skip.
#   PR_TITLE=… HEAD_REF=… PR_AUTHOR=… scripts/ci/check-conventions.sh <base-sha> <head-sha>
#
# Dependabot pull requests are exempt (their subjects and branches are generated, see docs/pr-workflow.md).
# Also checks that the title and the branch name the same Jira ticket, so the PR, the branch and Jira line up.
set -euo pipefail

base="${1:?usage: check-conventions.sh <base-sha> <head-sha>}"
head="${2:?usage: check-conventions.sh <base-sha> <head-sha>}"
: "${PR_TITLE:?PR_TITLE is not set}" "${HEAD_REF:?HEAD_REF is not set}" "${PR_AUTHOR:?PR_AUTHOR is not set}"

# shellcheck source=scripts/conventions.sh
source "$(dirname "$0")/../conventions.sh"

summary="${GITHUB_STEP_SUMMARY:-/dev/stdout}"

if [[ "$PR_AUTHOR" == "dependabot[bot]" ]]; then
  echo "conventions: Dependabot pull request — exempt."
  echo "Dependabot pull request: naming conventions do not apply." >> "$summary"
  exit 0
fi

failures=0
# $1 = what was checked, $2… = the check
record() {
  local what="$1"
  shift
  if "$@" 2> >(sed 's/^/    /' >&2); then
    echo "| ✅ | $what |" >> "$summary"
  else
    echo "::error title=Naming convention::Failed: $what (details in the job log)"
    echo "| ❌ | $what |" >> "$summary"
    failures=$((failures + 1))
  fi
}

{
  echo "### Naming conventions"
  echo "Rules: \`scripts/conventions.sh\`, explained in \`docs/pr-workflow.md\`."
  echo
  echo "| | Checked |"
  echo "|---|---|"
} >> "$summary"

record "PR title \`$PR_TITLE\`" check_pr_title "$PR_TITLE"
record "Branch \`$HEAD_REF\`" check_branch_name "$HEAD_REF"

title_key="$(grep -oE "^${CONVENTION_KEY}" <<< "$PR_TITLE" || true)"
branch_key="$(sed -nE "s#^[a-z]+/(${CONVENTION_KEY})-.*#\1#p" <<< "$HEAD_REF")"
same_ticket() {
  [[ -z "$title_key" || -z "$branch_key" || "$title_key" == "$branch_key" ]] || {
    echo "  title names $title_key but the branch names $branch_key" >&2
    return 1
  }
}
record "Title and branch name the same ticket" same_ticket

# Every commit the PR adds (also for stacked PRs: base is the branch below, not main). No merge commits exist —
# main requires a linear history — so the first line of each commit is its subject.
while IFS=' ' read -r sha subject; do
  record "Commit \`${sha:0:7}\` $subject" check_commit_subject "$subject" ci
done < <(git log --format='%H %s' --no-merges "$base..$head")

merges="$(git rev-list --merges --count "$base..$head")"
no_merges() {
  ((merges == 0)) || {
    echo "  $merges merge commit(s): main has a linear history — rebase onto the base branch instead" >&2
    return 1
  }
}
record "No merge commits" no_merges

if ((failures > 0)); then
  echo "conventions: $failures check(s) failed."
  exit 1
fi
echo "conventions: all checks passed."
