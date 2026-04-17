#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

has_glibc_flag=false
for arg in "$@"; do
  case "$arg" in
    --target-glibc|--no-glibc-check)
      has_glibc_flag=true
      break
      ;;
  esac
done

if [[ "${has_glibc_flag}" == "true" ]]; then
  exec "$SCRIPT_DIR/build-native-linux.sh" "$@"
fi

echo "[warn] scripts/build-native-centos7.sh is deprecated, use scripts/build-native-linux.sh"
echo "[warn] applying default compatibility check: --target-glibc 2.17"
exec "$SCRIPT_DIR/build-native-linux.sh" --target-glibc 2.17 "$@"
