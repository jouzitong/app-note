#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
APP_CMD="$ROOT_DIR/bin/app"
ENV_NAME="pro"
SKIP_FRONTEND=0
SKIP_BACKEND=0
BACKEND_CHECK_URL="${BACKEND_CHECK_URL:-}"
REQUIRED_BRANCH="master"

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--env dev|test|pro] [--skip-backend] [--skip-frontend]

Pipeline:
  1) build backend native
  2) deploy backend
  3) stop/start backend
  4) build frontend
  5) deploy frontend to nginx
  6) optional backend URL health check
USAGE
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --env)
      ENV_NAME="$2"
      shift 2
      ;;
    --skip-backend) SKIP_BACKEND=1; shift ;;
    --skip-frontend) SKIP_FRONTEND=1; shift ;;
    -h|--help) usage; exit 0 ;;
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

if ! git -C "$ROOT_DIR" rev-parse --is-inside-work-tree >/dev/null 2>&1; then
  echo "[err] not a git repository: $ROOT_DIR" >&2
  exit 1
fi

CURRENT_BRANCH="$(git -C "$ROOT_DIR" rev-parse --abbrev-ref HEAD 2>/dev/null || true)"
if [ -z "$CURRENT_BRANCH" ] || [ "$CURRENT_BRANCH" = "HEAD" ]; then
  echo "[err] detached HEAD or branch not detected, abort release" >&2
  exit 1
fi
if [ "$CURRENT_BRANCH" != "$REQUIRED_BRANCH" ]; then
  echo "[err] release is only allowed on branch: $REQUIRED_BRANCH" >&2
  echo "[err] current branch: $CURRENT_BRANCH" >&2
  exit 1
fi
echo "[ok] branch check passed: $CURRENT_BRANCH"

if [ "$SKIP_BACKEND" -eq 0 ]; then
  "$SCRIPT_DIR/builder/build-native-linux.sh" --profile "$ENV_NAME"
  "$SCRIPT_DIR/deploy/deploy-app-note.sh" --env "$ENV_NAME"
  "$APP_CMD" stop || true
  "$APP_CMD" start --no-tail
  "$APP_CMD" status
fi

if [ "$SKIP_FRONTEND" -eq 0 ]; then
  "$SCRIPT_DIR/builder/build-note-ui-prod.sh"
  "$SCRIPT_DIR/deploy/deploy-note-ui-nginx.sh"
fi

if [ -n "$BACKEND_CHECK_URL" ]; then
  echo "[check] $BACKEND_CHECK_URL"
  curl -fsS --max-time 8 "$BACKEND_CHECK_URL" >/dev/null
  echo "[ok] backend check passed"
else
  echo "[check] BACKEND_CHECK_URL is empty, skip http check"
fi

echo "[done] release pipeline completed"
