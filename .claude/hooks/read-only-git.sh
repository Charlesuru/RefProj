#!/usr/bin/env bash
# PreToolUse hook for the repo-guide agent (.claude/agents/repo-guide.md): the agent may only run a single read-only
# git command through Bash. Everything else is blocked (exit 2), so the agent cannot change files even in auto mode.
#   stdin: the hook's JSON payload; the command is read from .tool_input.command
set -euo pipefail

command="$(jq -r '.tool_input.command // ""')"

deny() {
  echo "repo-guide is read-only: $1. Allowed: git log|show|blame|diff|status|ls-files|grep|shortlog|rev-parse|describe" >&2
  exit 2
}

# One plain command: no chaining, pipes, redirection, substitution or line breaks.
case "$command" in
  *[\;\&\|\<\>\$\`]* | *$'\n'*) deny "shell operators are not allowed" ;;
esac

# The subcommand must follow `git` directly, so global options such as `git -c core.pager=…` are rejected.
pattern='^git (log|show|blame|diff|status|ls-files|grep|shortlog|rev-parse|describe)( |$)'
if [[ ! "$command" =~ $pattern ]]; then
  deny "only read-only git commands"
fi

# Options of the allowed subcommands that write files or run programs.
case "$command" in
  *--output* | *" -O"* | *--open-files-in-pager* | *--ext-diff*) deny "options that write files or run programs" ;;
esac

exit 0
