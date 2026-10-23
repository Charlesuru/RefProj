#!/usr/bin/env bash
# PreToolUse hook for the repo-guide agent (.claude/agents/repo-guide.md): the agent may only call Atlassian MCP tools
# that read. The agent's `tools:` list already names read tools only; this hook is the second guard, in case a write
# tool is ever added to that list by mistake. The MCP acts with the developer's own Jira permissions (OAuth), so Jira
# itself would allow writes. Everything that does not read is blocked (exit 2).
#   stdin: the hook's JSON payload; the tool name is read from .tool_name
set -euo pipefail

tool="$(jq -r '.tool_name // ""')"

# Read tools are named get*, search* or lookup* (e.g. getJiraIssue, searchJiraIssuesUsingJql, lookupJiraAccountId).
pattern='^mcp__atlassian__(get|search|lookup)[A-Z]'
if [[ ! "$tool" =~ $pattern ]]; then
  echo "repo-guide is read-only: $tool is not a read tool. Allowed: Atlassian tools named get*, search* or lookup*" >&2
  exit 2
fi

exit 0
