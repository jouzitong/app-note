#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

"${ROOT_DIR}/scripts/build-boot-jar.sh" >/dev/null

JAR_PATH="$(
  find "${ROOT_DIR}/boot/target" -maxdepth 1 -type f -name "boot-*.jar" ! -name "*.original" \
    | sort \
    | tail -n 1
)"

if [[ -z "${JAR_PATH}" ]]; then
  echo "[error] boot jar not found under boot/target" >&2
  exit 1
fi

exec java -jar "${JAR_PATH}" "$@"

