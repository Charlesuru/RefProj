#!/usr/bin/env bash
# Installs a pinned, checksum-verified CLI tool on a CI runner and puts it on PATH.
#   scripts/ci/install-tool.sh <gitleaks|actionlint|zizmor|xcodegen>
#
# Single source of truth for CI tool versions (Gradle dependencies live in gradle/libs.versions.toml,
# Ruby gems in Gemfile.lock, GitHub Actions are SHA-pinned in the workflows). Dependabot does not
# update this file: bump a version together with its SHA-256 from the release's checksums.
set -euo pipefail

tool="${1:?usage: install-tool.sh <tool>}"
os="$(uname -s)"
arch="$(uname -m)"

case "$tool/$os/$arch" in
  gitleaks/Linux/x86_64)
    version=8.30.1
    url="https://github.com/gitleaks/gitleaks/releases/download/v${version}/gitleaks_${version}_linux_x64.tar.gz"
    sha256=551f6fc83ea457d62a0d98237cbad105af8d557003051f41f3e7ca7b3f2470eb
    ;;
  actionlint/Linux/x86_64)
    version=1.7.12
    url="https://github.com/rhysd/actionlint/releases/download/v${version}/actionlint_${version}_linux_amd64.tar.gz"
    sha256=8aca8db96f1b94770f1b0d72b6dddcb1ebb8123cb3712530b08cc387b349a3d8
    ;;
  zizmor/Linux/x86_64)
    version=1.30.1
    url="https://github.com/zizmorcore/zizmor/releases/download/v${version}/zizmor-x86_64-unknown-linux-gnu.tar.gz"
    sha256=e65324f4430c2717591937edcec90ccbefaf14c174f8ec9415e03ca875b46e1a
    ;;
  xcodegen/Darwin/*)
    version=2.46.0
    url="https://github.com/yonaskolb/XcodeGen/releases/download/${version}/xcodegen.zip"
    sha256=4d9e34b62172d645eed6457cac13fc222569974098ef4ee9c3368bedf0196806
    ;;
  *)
    echo "install-tool: no pinned build of '$tool' for $os/$arch" >&2
    exit 1
    ;;
esac

dest="${RUNNER_TEMP:?RUNNER_TEMP is not set}/tools/$tool"
mkdir -p "$dest"
archive="$dest/$(basename "$url")"

curl --fail --silent --show-error --location --retry 3 --output "$archive" "$url"
echo "$sha256  $archive" | shasum -a 256 --check --status || {
  echo "install-tool: checksum mismatch for $tool $version — refusing to install" >&2
  exit 1
}

case "$archive" in
  *.tar.gz) tar -xzf "$archive" -C "$dest" ;;
  *.zip) unzip -q "$archive" -d "$dest" ;;
esac
rm "$archive"

bin_dir="$(dirname "$(find "$dest" -type f -name "$tool" -perm -100 | head -n 1)")"
echo "$bin_dir" >> "$GITHUB_PATH"
echo "Installed $tool $version"
