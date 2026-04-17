#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/home/app/app-note}"
ENV_NAME="${ENV_NAME:-pro}"
PID_FILE="${PID_FILE:-$APP_ROOT/bin/app-note.pid}"
LOG_FILE="${LOG_FILE:-$APP_ROOT/logs/app-note.log}"
APP_BIN="${APP_BIN:-$APP_ROOT/app-note}"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--env dev|test|pro] [-- <extra-args>]
USAGE
}

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
      usage
      exit 0
      ;;
    *)
      echo "[err] unknown arg: $1" >&2
      usage
      exit 2
      ;;
  esac
done

case "$ENV_NAME" in
  dev|test|pro) ;;
  *) echo "[err] unsupported env: $ENV_NAME" >&2; exit 2 ;;
esac

if [ ! -x "$APP_BIN" ]; then
  echo "[err] binary not executable: $APP_BIN" >&2
  exit 1
fi

mkdir -p "$APP_ROOT/bin" "$APP_ROOT/logs"

if [ -f "$PID_FILE" ]; then
  pid="$(cat "$PID_FILE" 2>/dev/null || true)"
  if [ -n "${pid:-}" ] && kill -0 "$pid" >/dev/null 2>&1; then
    echo "[err] app-note already running (pid=$pid)"
    exit 1
  fi
fi

cmd=(
  "$APP_BIN"
  "--spring.profiles.active=$ENV_NAME"
  "--spring.config.additional-location=file:$APP_ROOT/config/"
)
if ((${#EXTRA_ARGS[@]} > 0)); then
  cmd+=("${EXTRA_ARGS[@]}")
fi

echo "[run] ${cmd[*]}"
nohup "${cmd[@]}" >> "$LOG_FILE" 2>&1 &
echo $! > "$PID_FILE"
echo "[ok] started pid=$(cat "$PID_FILE")"
echo "[ok] log file: $LOG_FILE"
