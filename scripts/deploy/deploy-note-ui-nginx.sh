#!/usr/bin/env bash
set -euo pipefail

SRC_ROOT="${SRC_ROOT:-/home/workroom/items/app-note}"
DEST_ROOT="${DEST_ROOT:-/home/app/app-note}"
SRC_DIST="${SRC_DIST:-$SRC_ROOT/note-ui/dist}"
NGINX_WEB_ROOT="${NGINX_WEB_ROOT:-/usr/share/nginx/html/app-note}"
ENABLE_NGINX_RELOAD="${ENABLE_NGINX_RELOAD:-1}"
NO_BACKUP=0
DRY_RUN=0

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--src-dist <path>] [--dry-run] [--no-backup]

Environment:
  NGINX_WEB_ROOT       Nginx static root for app-note (default: $NGINX_WEB_ROOT)
  ENABLE_NGINX_RELOAD  1=reload nginx after deploy, 0=skip reload
USAGE
}

run() {
  if [ "$DRY_RUN" -eq 1 ]; then
    echo "[dry-run] $*"
  else
    eval "$@"
  fi
}
ts() { date +%Y%m%d-%H%M%S; }

while [ "$#" -gt 0 ]; do
  case "$1" in
    --src-dist)
      SRC_DIST="$2"
      shift 2
      ;;
    --dry-run) DRY_RUN=1; shift ;;
    --no-backup) NO_BACKUP=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *)
      echo "[err] unknown arg: $1" >&2
      usage
      exit 2
      ;;
  esac
done

if [ ! -d "$SRC_DIST" ]; then
  echo "[err] dist directory not found: $SRC_DIST" >&2
  echo "[hint] run first: scripts/builder/build-note-ui-prod.sh" >&2
  exit 1
fi

BACKUP_ROOT="$DEST_ROOT/bak"
TMP_DIR="${NGINX_WEB_ROOT}.tmp-$(ts)"
OLD_DIR="${NGINX_WEB_ROOT}.old-$(ts)"

run "install -d -m 755 \"$BACKUP_ROOT\""
run "rm -rf \"$TMP_DIR\""
run "install -d -m 755 \"$TMP_DIR\""
run "cp -a \"$SRC_DIST/.\" \"$TMP_DIR/\""

if [ -d "$NGINX_WEB_ROOT" ]; then
  if [ "$NO_BACKUP" -eq 0 ]; then
    run "mv \"$NGINX_WEB_ROOT\" \"$OLD_DIR\""
    run "mv \"$OLD_DIR\" \"$BACKUP_ROOT/frontend-dist-$(ts)\""
  else
    run "rm -rf \"$NGINX_WEB_ROOT\""
  fi
fi

run "mv \"$TMP_DIR\" \"$NGINX_WEB_ROOT\""
echo "[ok] deployed dist to: $NGINX_WEB_ROOT"

if [ "$ENABLE_NGINX_RELOAD" = "1" ]; then
  if command -v nginx >/dev/null 2>&1; then
    run "nginx -t"
  fi
  if command -v systemctl >/dev/null 2>&1; then
    run "systemctl reload nginx"
  elif command -v nginx >/dev/null 2>&1; then
    run "nginx -s reload"
  else
    echo "[warn] nginx reload skipped: nginx/systemctl command not found"
  fi
fi

echo "[done] frontend deploy finished"
