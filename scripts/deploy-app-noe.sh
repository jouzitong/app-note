#!/usr/bin/env bash
set -euo pipefail

SRC_ROOT="${SRC_ROOT:-/home/workroom/items/app-note}"
DEST_ROOT="${DEST_ROOT:-/home/app/app-note}"

BOOT_DIR="$SRC_ROOT/boot"
BIN_SRC="$BOOT_DIR/target/app-note"
BIN_DEST="$DEST_ROOT/app-note"

DRY_RUN=0
NO_BACKUP=0
BACKUP_DIR=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--dry-run] [--no-backup]

Environment:
  SRC_ROOT   Source repo root (default: $SRC_ROOT)
  DEST_ROOT  Target dir (default: $DEST_ROOT)

Copies:
  - $BIN_SRC -> $BIN_DEST
  - boot application configs -> $DEST_ROOT/config/

Backup:
  - Old files are copied into: $DEST_ROOT/bak/<timestamp>/
USAGE
}

log() { printf '%s\n' "$*"; }
run() {
  if [ "$DRY_RUN" -eq 1 ]; then
    log "[dry-run] $*"
  else
    eval "$@"
  fi
}

ts() { date +%Y%m%d-%H%M%S; }

ensure_backup_dir() {
  if [ "$NO_BACKUP" -eq 1 ]; then
    return 0
  fi
  if [ -z "$BACKUP_DIR" ]; then
    BACKUP_DIR="$DEST_ROOT/bak/$(ts)"
    run "install -d -m 755 \"$BACKUP_DIR\""
  fi
}

backup_rel() {
  # backup a DEST_ROOT-relative path into BACKUP_DIR
  local rel="$1"
  local src="$DEST_ROOT/$rel"
  if [ "$NO_BACKUP" -eq 1 ]; then
    return 0
  fi
  if [ -e "$src" ] || [ -L "$src" ]; then
    ensure_backup_dir
    local dst="$BACKUP_DIR/$rel"
    run "install -d -m 755 \"$(dirname "$dst")\""
    run "cp -a \"$src\" \"$dst\""
    log "Backed up: $src -> $dst"
  fi
}

first_existing() {
  # prints first existing path among args
  local p
  for p in "$@"; do
    if [ -f "$p" ]; then
      printf '%s' "$p"
      return 0
    fi
  done
  return 1
}

while [ "$#" -gt 0 ]; do
  case "$1" in
    --dry-run) DRY_RUN=1; shift ;;
    --no-backup) NO_BACKUP=1; shift ;;
    -h|--help) usage; exit 0 ;;
    *) log "Unknown arg: $1"; usage; exit 2 ;;
  esac
done

if [ ! -x "$BIN_SRC" ]; then
  log "ERROR: native binary not found/executable: $BIN_SRC"
  exit 1
fi

run "install -d -m 755 \"$DEST_ROOT\" \"$DEST_ROOT/bin\" \"$DEST_ROOT/logs\" \"$DEST_ROOT/config\" \"$DEST_ROOT/bak\""

# Copy binary atomically
backup_rel "app-note"
TMP_BIN="$DEST_ROOT/.app-note.tmp-$(ts)"
run "cp -f \"$BIN_SRC\" \"$TMP_BIN\""
run "chmod 755 \"$TMP_BIN\""
run "mv -f \"$TMP_BIN\" \"$BIN_DEST\""
log "Installed binary: $BIN_DEST"

# Config sources
APP_YML_SRC=""
if APP_YML_SRC=$(first_existing \
  "$BOOT_DIR/application.yml" \
  "$BOOT_DIR/src/main/resources/application.yml" \
  "$BOOT_DIR/target/classes/application.yml" \
  "$BOOT_DIR/src/main/resources/application.yaml" \
  "$BOOT_DIR/target/classes/application.yaml" \
); then
  :
else
  log "ERROR: application.yml (or application.yaml) not found under $BOOT_DIR"
  exit 1
fi

ATHENA_SRC=$(first_existing \
  "$BOOT_DIR/application-athena.yaml" \
  "$BOOT_DIR/src/main/resources/application-athena.yaml" \
  "$BOOT_DIR/target/classes/application-athena.yaml" \
) || {
  log "ERROR: application-athena.yaml not found under $BOOT_DIR"
  exit 1
}

PRO_SRC=$(first_existing \
  "$BOOT_DIR/application-pro.yaml" \
  "$BOOT_DIR/src/main/resources/application-pro.yaml" \
  "$BOOT_DIR/target/classes/application-pro.yaml" \
) || {
  log "ERROR: application-pro.yaml not found under $BOOT_DIR"
  exit 1
}

# Always place as requested names under config/
backup_rel "config/application.yml"
run "cp -f \"$APP_YML_SRC\" \"$DEST_ROOT/config/application.yml\""
run "chmod 644 \"$DEST_ROOT/config/application.yml\""
if [[ "$APP_YML_SRC" == *.yaml ]]; then
  log "Note: source was $(basename "$APP_YML_SRC"), copied to config/application.yml"
fi

backup_rel "config/application-athena.yaml"
run "cp -f \"$ATHENA_SRC\" \"$DEST_ROOT/config/application-athena.yaml\""
run "chmod 644 \"$DEST_ROOT/config/application-athena.yaml\""

backup_rel "config/application-pro.yaml"
run "cp -f \"$PRO_SRC\" \"$DEST_ROOT/config/application-pro.yaml\""
run "chmod 644 \"$DEST_ROOT/config/application-pro.yaml\""

log "Installed configs into: $DEST_ROOT/config"
if [ -n "$BACKUP_DIR" ]; then
  log "Backup dir: $BACKUP_DIR"
fi
log "Done."
