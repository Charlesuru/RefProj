#!/usr/bin/env bash
# Prints line coverage per module from the Kover XML reports as a Markdown table.
#   scripts/ci/coverage-summary.sh            # after ./gradlew koverXmlReport
#
# Appends to $GITHUB_STEP_SUMMARY in CI, prints to stdout locally. Floors: config/kover/coverage-floors.properties.
set -euo pipefail

root="$(cd "$(dirname "$0")/../.." && pwd)"
floors="$root/config/kover/coverage-floors.properties"

table="$(
  python3 -I - "$root" "$floors" <<'PY'
import pathlib
import sys
import xml.etree.ElementTree as ET

root = pathlib.Path(sys.argv[1])
floors = {}
for line in pathlib.Path(sys.argv[2]).read_text().splitlines():
    if "=" in line and not line.lstrip().startswith("#"):
        key, value = line.split("=", 1)
        floors[key.strip()] = value.strip()

def coverage(report):
    counter = next((c for c in ET.parse(report).getroot().findall("counter") if c.get("type") == "LINE"), None)
    covered, missed = (0, 0) if counter is None else (int(counter.get("covered")), int(counter.get("missed")))
    lines = covered + missed
    return (covered * 100 / lines if lines else None), lines

rows = []
for report in sorted(root.glob("**/build/reports/kover/report.xml")):
    module_dir = report.parents[3].relative_to(root)
    if module_dir.parts[:1] == ("build-logic",):
        continue
    key = "total" if str(module_dir) == "." else ".".join(module_dir.parts)
    percent, lines = coverage(report)
    shown = "—" if percent is None else f"{percent:.1f} %"
    rows.append((key != "total", key, shown, lines, floors.get(key, "?")))

print("| Module | Line coverage | Lines | Floor |")
print("|---|---:|---:|---:|")
for _, key, shown, lines, floor in sorted(rows):
    name = "**total**" if key == "total" else f"`{key}`"
    print(f"| {name} | {shown} | {lines} | {floor} % |")
PY
)"

{
  echo "### Code coverage (Kover, Android host tests)"
  echo
  echo "$table"
} >> "${GITHUB_STEP_SUMMARY:-/dev/stdout}"
