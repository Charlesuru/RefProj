#!/usr/bin/env bash
# Applies the repository's GitHub settings — "settings as code". Idempotent; safe to re-run.
#   scripts/github/configure-repo.sh [--skip-rulesets]
#
# Requires: gh, logged in as a repository admin. Configures:
#   - merge options (rebase only, linear history), auto-delete branches
#   - Actions: read-only default token, SHA-pinned + allow-listed actions, approval for outside contributors
#   - security: secret scanning + push protection, Dependabot alerts/security updates,
#     private vulnerability reporting, CodeQL default setup for workflow files
#   - GitHub Environments dev/sit (main only), stage/prod (release tags + required reviewer) and
#     ai-review (PR branches <type>/<name>; the Claude token for ai-review.yml)
#   - rulesets from .github/settings/ruleset-*.json (protected main, protected release tags)
# Secrets are NOT set here — see scripts/github/set-ci-secrets.sh (run by the owner).
# Everything is documented in docs/ci-cd.md.
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

repo="${GITHUB_REPOSITORY:-$(gh repo view --json nameWithOwner --jq .nameWithOwner)}"
reviewer="${RELEASE_REVIEWER:-$(gh api user --jq .login)}"
skip_rulesets=false
[[ "${1:-}" == "--skip-rulesets" ]] && skip_rulesets=true

api() { gh api --silent "$@"; }

echo "→ $repo: merge options"
api -X PATCH "repos/$repo" --input - <<'JSON'
{
  "allow_merge_commit": false,
  "allow_squash_merge": false,
  "allow_rebase_merge": true,
  "allow_auto_merge": true,
  "allow_update_branch": true,
  "delete_branch_on_merge": true,
  "has_wiki": false,
  "has_projects": false
}
JSON

echo "→ Actions: permissions"
api -X PUT "repos/$repo/actions/permissions" -F enabled=true -f allowed_actions=selected -F sha_pinning_required=true
# claude-code-action runs oven-sh/setup-bun itself; only that exact commit is allowed (update it with the action).
api -X PUT "repos/$repo/actions/permissions/selected-actions" --input - <<'JSON'
{
  "github_owned_allowed": true,
  "verified_allowed": false,
  "patterns_allowed": [
    "gradle/actions/*", "ruby/setup-ruby@*", "anthropics/claude-code-action@*",
    "oven-sh/setup-bun@0c5077e51419868618aeaa5fe8019c62421857d6"
  ]
}
JSON
api -X PUT "repos/$repo/actions/permissions/workflow" \
  -f default_workflow_permissions=read -F can_approve_pull_request_reviews=false
api -X PUT "repos/$repo/actions/permissions/fork-pr-contributor-approval" \
  -f approval_policy=all_external_contributors

echo "→ Security features"
api -X PATCH "repos/$repo" --input - <<'JSON'
{
  "security_and_analysis": {
    "secret_scanning": { "status": "enabled" },
    "secret_scanning_push_protection": { "status": "enabled" }
  }
}
JSON
# Dependabot alerts also switch on the dependency graph, which ci.yml needs to submit the Gradle graph.
api -X PUT "repos/$repo/vulnerability-alerts"
api -X PUT "repos/$repo/automated-security-fixes"
api -X PUT "repos/$repo/private-vulnerability-reporting"
# CodeQL only accepts "actions" once workflow files exist on the default branch.
if ! api -X PATCH "repos/$repo/code-scanning/default-setup" --input - <<'JSON' 2>/dev/null; then
{ "state": "configured", "languages": ["actions"], "query_suite": "default" }
JSON
  echo "  ! CodeQL default setup skipped: no workflows on the default branch yet. Re-run this script after merging them."
fi

# $1 = environment, $2 = ref type (branch|tag), $3 = ref pattern, $4 = "reviewed" to require approval
configure_environment() {
  local env="$1" ref_type="$2" pattern="$3" reviewed="${4:-}"
  local reviewers='[]'
  if [[ "$reviewed" == reviewed ]]; then
    reviewers="[{\"type\":\"User\",\"id\":$(gh api "users/$reviewer" --jq .id)}]"
  fi
  echo "→ Environment $env ($ref_type $pattern${reviewed:+, reviewer $reviewer})"
  api -X PUT "repos/$repo/environments/$env" --input - <<JSON
{
  "reviewers": $reviewers,
  "prevent_self_review": false,
  "can_admins_bypass": false,
  "deployment_branch_policy": { "protected_branches": false, "custom_branch_policies": true }
}
JSON
  # Replace the allowed refs with exactly the one we want.
  local id
  for id in $(gh api "repos/$repo/environments/$env/deployment-branch-policies" --jq '.branch_policies[].id'); do
    api -X DELETE "repos/$repo/environments/$env/deployment-branch-policies/$id"
  done
  api -X POST "repos/$repo/environments/$env/deployment-branch-policies" -f name="$pattern" -f type="$ref_type"
}

configure_environment dev branch main
# Pull-request previews (label deploy:dev, deploy-pr.yml) run on refs/pull/<n>/merge. dev is the only
# environment that accepts them.
echo "→ Environment dev (+ branch refs/pull/*/merge)"
api -X POST "repos/$repo/environments/dev/deployment-branch-policies" -f name='refs/pull/*/merge' -f type=branch
configure_environment sit branch main
# AI review (ai-review.yml, label "ai-review"): GitHub checks a pull_request run's environment rule against the PR's
# head branch, not refs/pull/<n>/merge (a non-matching run waits forever). '*/*' matches every branch named by the
# conventions (<type>/REF-n-…) and never main. Its environment holds only the Claude token.
configure_environment ai-review branch '*/*'
configure_environment stage tag 'v*' reviewed
configure_environment prod tag 'v*' reviewed

echo "→ Label deploy:dev"
gh label create "deploy:dev" --repo "$repo" --force --color 1F5EFF \
  --description "Deploy this pull request to DEV testers (deploy-pr.yml)" >/dev/null

echo "→ Label ai-review"
gh label create "ai-review" --repo "$repo" --force --color 7B61FF \
  --description "Ask Claude for an automated review of this pull request (ai-review.yml)" >/dev/null

if [[ "$skip_rulesets" == false ]]; then
  for file in .github/settings/ruleset-*.json; do
    name="$(jq -r .name "$file")"
    id="$(gh api "repos/$repo/rulesets" --jq ".[] | select(.name == \"$name\") | .id")"
    if [[ -n "$id" ]]; then
      echo "→ Ruleset $name (update)"
      api -X PUT "repos/$repo/rulesets/$id" --input "$file"
    else
      echo "→ Ruleset $name (create)"
      api -X POST "repos/$repo/rulesets" --input "$file"
    fi
  done
fi

echo "✓ $repo configured"
