#!/usr/bin/env bash
set -euo pipefail

SRC_ROOT="${SRC_ROOT:-/home/workroom/items/app-note}"
DEST_ROOT="${DEST_ROOT:-/home/app/app-note}"
DEFAULT_ENV="${DEFAULT_ENV:-pro}"

BOOT_DIR="$SRC_ROOT/boot"
BIN_SRC_DEFAULT="$BOOT_DIR/target/app-note"
BIN_DEST="$DEST_ROOT/app-note"

DRY_RUN=0
NO_BACKUP=0
ENV_NAME="$DEFAULT_ENV"
BACKUP_DIR=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--env dev|test|pro] [--dry-run] [--no-backup]

Environment:
  SRC_ROOT      Source repo root (default: $SRC_ROOT)
  DEST_ROOT     Deploy root (default: $DEST_ROOT)
  NATIVE_BIN    Source native binary (default: $BIN_SRC_DEFAULT)

Deploy layout:
  $DEST_ROOT/bin
  $DEST_ROOT/config
  $DEST_ROOT/logs
  $DEST_ROOT/app-note
  $DEST_ROOT/meta
  $DEST_ROOT/bak
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

backup_if_exists() {
  local path="$1"
  local rel
  rel="${path#"$DEST_ROOT"/}"
  if [ "$NO_BACKUP" -eq 1 ]; then
    return 0
  fi
  if [ -e "$path" ] || [ -L "$path" ]; then
    ensure_backup_dir
    run "install -d -m 755 \"$BACKUP_DIR/$(dirname "$rel")\""
    run "cp -a \"$path\" \"$BACKUP_DIR/$rel\""
    log "[backup] $path -> $BACKUP_DIR/$rel"
  fi
}

first_existing() {
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
    --env)
      if [ "$#" -lt 2 ]; then
        echo "[err] --env requires a value" >&2
        exit 2
      fi
      ENV_NAME="$2"
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

case "$ENV_NAME" in
  dev|test|pro) ;;
  *)
    echo "[err] unsupported env: $ENV_NAME (allowed: dev|test|pro)" >&2
    exit 2
    ;;
esac

BIN_SRC="${NATIVE_BIN:-$BIN_SRC_DEFAULT}"
if [ ! -x "$BIN_SRC" ]; then
  echo "[err] native binary not found or not executable: $BIN_SRC" >&2
  echo "[hint] build first: scripts/build-native-centos7.sh --profile $ENV_NAME" >&2
  exit 1
fi

APP_YAML_SRC=""
if APP_YAML_SRC=$(first_existing \
  "$BOOT_DIR/src/main/resources/application.yaml" \
  "$BOOT_DIR/src/main/resources/application.yml" \
  "$BOOT_DIR/target/classes/application.yaml" \
  "$BOOT_DIR/target/classes/application.yml" \
); then
  :
else
  echo "[err] application.yaml/application.yml not found under $BOOT_DIR" >&2
  exit 1
fi

ENV_YAML_SRC=""
if ENV_YAML_SRC=$(first_existing \
  "$BOOT_DIR/src/main/resources/application-${ENV_NAME}.yaml" \
  "$BOOT_DIR/target/classes/application-${ENV_NAME}.yaml" \
); then
  :
else
  echo "[err] application-${ENV_NAME}.yaml not found under $BOOT_DIR" >&2
  exit 1
fi

ATHENA_YAML_SRC="$(first_existing \
  "$BOOT_DIR/src/main/resources/application-athena.yaml" \
  "$BOOT_DIR/target/classes/application-athena.yaml" \
  || true
)"

run "install -d -m 755 \"$DEST_ROOT\" \"$DEST_ROOT/bin\" \"$DEST_ROOT/config\" \"$DEST_ROOT/logs\" \"$DEST_ROOT/meta\" \"$DEST_ROOT/bak\""

backup_if_exists "$BIN_DEST"
TMP_BIN="$DEST_ROOT/.app-note.tmp-$(ts)"
run "cp -f \"$BIN_SRC\" \"$TMP_BIN\""
run "chmod 755 \"$TMP_BIN\""
run "mv -f \"$TMP_BIN\" \"$BIN_DEST\""
log "[ok] installed binary: $BIN_DEST"

backup_if_exists "$DEST_ROOT/config/application.yaml"
run "cp -f \"$APP_YAML_SRC\" \"$DEST_ROOT/config/application.yaml\""
run "chmod 644 \"$DEST_ROOT/config/application.yaml\""

backup_if_exists "$DEST_ROOT/config/application-${ENV_NAME}.yaml"
run "cp -f \"$ENV_YAML_SRC\" \"$DEST_ROOT/config/application-${ENV_NAME}.yaml\""
run "chmod 644 \"$DEST_ROOT/config/application-${ENV_NAME}.yaml\""

if [ -n "$ATHENA_YAML_SRC" ]; then
  backup_if_exists "$DEST_ROOT/config/application-athena.yaml"
  run "cp -f \"$ATHENA_YAML_SRC\" \"$DEST_ROOT/config/application-athena.yaml\""
  run "chmod 644 \"$DEST_ROOT/config/application-athena.yaml\""
fi

BUILD_TS="$(date '+%Y-%m-%d %H:%M:%S %z')"
GIT_COMMIT="$(git -C "$SRC_ROOT" rev-parse --short HEAD 2>/dev/null || echo unknown)"
BIN_SHA256="$(sha256sum "$BIN_SRC" | awk '{print $1}')"

run "cat > \"$DEST_ROOT/meta/current-version.txt\" <<EOF
build_time=$BUILD_TS
git_commit=$GIT_COMMIT
env=$ENV_NAME
binary_sha256=$BIN_SHA256
binary_path=$BIN_DEST
EOF"

run "printf '%s | commit=%s | env=%s | sha256=%s\n' \"$(date '+%Y-%m-%d %H:%M:%S')\" \"$GIT_COMMIT\" \"$ENV_NAME\" \"$BIN_SHA256\" >> \"$DEST_ROOT/meta/version-history.log\""

if [ -n "$BACKUP_DIR" ]; then
  log "[ok] backup dir: $BACKUP_DIR"
fi
log "[ok] config dir: $DEST_ROOT/config"
log "[ok] version file: $DEST_ROOT/meta/current-version.txt"
log "[done] deploy finished"
