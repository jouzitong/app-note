#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/home/app/app-note}"
LOG_FILE="${LOG_FILE:-$APP_ROOT/logs/app-note.log}"
LINES="${LINES:-200}"
FOLLOW=1

while [ "$#" -gt 0 ]; do
  case "$1" in
    --lines)
      LINES="$2"
      shift 2
      ;;
    --no-follow)
      FOLLOW=0
      shift
      ;;
    -h|--help)
      echo "Usage: $(basename "$0") [--lines N] [--no-follow]"
      exit 0
      ;;
    *)
      echo "[err] unknown arg: $1" >&2
      exit 2
      ;;
  esac
done

if [ ! -f "$LOG_FILE" ]; then
  echo "[err] log file not found: $LOG_FILE" >&2
  exit 1
fi

if [ "$FOLLOW" -eq 1 ]; then
  exec tail -n "$LINES" -f "$LOG_FILE"
else
  exec tail -n "$LINES" "$LOG_FILE"
fi
