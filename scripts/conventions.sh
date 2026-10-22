# shellcheck shell=bash
# Naming conventions for commits, branches and pull requests (CLAUDE.md §3), shared by the commit-msg hook
# (.githooks/commit-msg) and CI (scripts/ci/check-conventions.sh). Source it; every check prints why it failed
# to stderr and returns non-zero.
#
#   commit:  <type>(<scope>): <summary> [REF-123]    ≤ 100 characters
#   branch:  <type>/REF-123-short-description
#   PR:      REF-123: <summary>

CONVENTION_TYPES='feat|fix|refactor|perf|test|docs|build|ci|chore|revert'
CONVENTION_KEY='[A-Z][A-Z0-9]+-[0-9]+'
CONVENTION_SUBJECT_MAX=100

# Subjects git writes itself. `fixup!`/`squash!`/`amend!` are allowed locally (git rebase --autosquash folds them),
# but never on main — check_commit_subject rejects them unless the second argument is "local".
convention_is_git_subject() {
  [[ "$1" =~ ^(Merge\ |Revert\ \") ]]
}

convention_is_autosquash_subject() {
  [[ "$1" =~ ^(fixup!|squash!|amend!)\  ]]
}

# $1 = subject line, $2 = "local" (commit-msg hook) or "ci" (default)
check_commit_subject() {
  local subject="$1" mode="${2:-ci}"
  if convention_is_git_subject "$subject"; then
    return 0
  fi
  if convention_is_autosquash_subject "$subject"; then
    [[ "$mode" == local ]] && return 0
    echo "  '$subject': fold it in before merge — git rebase -i --autosquash origin/main" >&2
    return 1
  fi
  local pattern="^(${CONVENTION_TYPES})(\([a-z0-9._/-]+\))?!?: [^ ].* \[${CONVENTION_KEY}\]$"
  if [[ ! "$subject" =~ $pattern ]]; then
    cat >&2 <<MSG
  subject must be a Conventional Commit ending with a Jira key:
      <type>(<scope>): <summary> [REF-123]
    types: ${CONVENTION_TYPES//|/ }
    got:   $subject
MSG
    return 1
  fi
  if ((${#subject} > CONVENTION_SUBJECT_MAX)); then
    echo "  subject is ${#subject} characters; keep it at ${CONVENTION_SUBJECT_MAX} or fewer: $subject" >&2
    return 1
  fi
}

check_branch_name() {
  local branch="$1"
  local pattern="^(${CONVENTION_TYPES})/${CONVENTION_KEY}-[a-z0-9]+(-[a-z0-9]+)*$"
  if [[ ! "$branch" =~ $pattern ]]; then
    cat >&2 <<MSG
  branch must be <type>/<JIRA-KEY>-short-description (lower-case words joined by '-'):
      feat/REF-123-biometric-unlock
    got: $branch
MSG
    return 1
  fi
}

check_pr_title() {
  local title="$1"
  local pattern="^${CONVENTION_KEY}: [^ ].*$"
  if [[ ! "$title" =~ $pattern ]]; then
    cat >&2 <<MSG
  pull request title must start with the Jira key:
      REF-123: Add biometric unlock
    got: $title
MSG
    return 1
  fi
}
