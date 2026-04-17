#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"

ENV_NAME="pro"
declare -a EXTRA_ARGS=()

while [ "$#" -gt 0 ]; do
  case "$1" in
    --env)
      ENV_NAME="$2"
      shift 2
      ;;
    --)
      shift
      EXTRA_ARGS=("$@")
      break
      ;;
    -h|--help)
      echo "Usage: $(basename "$0") [--env dev|test|pro] [-- <extra-args>]"
      exit 0
      ;;
    *)
      echo "[err] unknown arg: $1" >&2
      exit 2
      ;;
  esac
done

"$SCRIPT_DIR/backend-stop.sh" || true
"$SCRIPT_DIR/backend-start.sh" --env "$ENV_NAME" -- "${EXTRA_ARGS[@]}"
