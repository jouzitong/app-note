#!/usr/bin/env bash
set -euo pipefail

APP_ROOT="${APP_ROOT:-/home/app/app-note}"
PID_FILE="${PID_FILE:-$APP_ROOT/bin/app-note.pid}"

if [ ! -f "$PID_FILE" ]; then
  echo "stopped (pid file missing)"
  exit 1
fi

pid="$(cat "$PID_FILE" 2>/dev/null || true)"
if [ -z "${pid:-}" ]; then
  echo "stopped (pid file empty)"
  exit 1
fi

if kill -0 "$pid" >/dev/null 2>&1; then
  echo "running (pid=$pid)"
  exit 0
fi

echo "stopped (stale pid=$pid)"
exit 1
