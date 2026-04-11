#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "${ROOT_DIR}"

MVN_CMD="${MVN_CMD:-mvn}"

echo "[build] packaging Spring Boot jar (module: boot)"
"${MVN_CMD}" -pl boot -am -DskipTests clean package

JAR_PATH="$(
  find "${ROOT_DIR}/boot/target" -maxdepth 1 -type f -name "boot-*.jar" ! -name "*.original" \
    | sort \
    | tail -n 1
)"

if [[ -z "${JAR_PATH}" ]]; then
  echo "[error] boot jar not found under boot/target" >&2
  exit 1
fi

echo "[ok] boot jar: ${JAR_PATH}"
echo
echo "Run:"
echo "  java -jar \"${JAR_PATH}\""

