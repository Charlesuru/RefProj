#!/usr/bin/env bash
# One-time developer machine setup. Safe to re-run.
#   ./scripts/setup.sh
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"

missing=()
for tool in java ruby bundle xcodegen gitleaks; do
  command -v "$tool" >/dev/null 2>&1 || missing+=("$tool")
done
if (( ${#missing[@]} > 0 )); then
  echo "Missing tools: ${missing[*]} — see README.md#requirements" >&2
  exit 1
fi

echo "→ Git hooks (.githooks)"
git config core.hooksPath .githooks

echo "→ Ruby gems (vendor/bundle)"
bundle config set --local path vendor/bundle
bundle install --quiet

for template in iosApp/Configs/Local.xcconfig fastlane/.env; do
  if [[ ! -f "$template" ]]; then
    cp "$template.example" "$template"
    echo "→ Created $template from the example — fill in your values (docs/secrets.md)"
  fi
done

echo "✓ Done. Try: bundle exec fastlane android build env:dev"
