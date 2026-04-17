#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/home/app/app-note}"
PID_FILE="${PID_FILE:-$APP_ROOT/bin/app-note.pid}"
WAIT_SECONDS="${WAIT_SECONDS:-20}"

if [ ! -f "$PID_FILE" ]; then
  echo "[ok] pid file not found, app likely stopped: $PID_FILE"
  exit 0
fi

pid="$(cat "$PID_FILE" 2>/dev/null || true)"
if [ -z "${pid:-}" ]; then
  echo "[warn] pid file is empty, removing: $PID_FILE"
  rm -f "$PID_FILE"
  exit 0
fi

if ! kill -0 "$pid" >/dev/null 2>&1; then
  echo "[ok] process not running, cleaning stale pid file: $PID_FILE"
  rm -f "$PID_FILE"
  exit 0
fi

echo "[stop] stopping pid=$pid"
kill "$pid"

for _ in $(seq 1 "$WAIT_SECONDS"); do
  if ! kill -0 "$pid" >/dev/null 2>&1; then
    rm -f "$PID_FILE"
    echo "[ok] stopped"
    exit 0
  fi
  sleep 1
done

echo "[warn] graceful stop timeout, force killing pid=$pid"
kill -9 "$pid" >/dev/null 2>&1 || true
rm -f "$PID_FILE"
echo "[ok] stopped (force)"
