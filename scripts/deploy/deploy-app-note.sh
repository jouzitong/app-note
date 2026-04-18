#!/usr/bin/env bash
set -euo pipefail

SRC_ROOT="${SRC_ROOT:-/home/workroom/items/app-note}"
DEST_ROOT="${DEST_ROOT:-/home/app/app-note}"
DEFAULT_ENV="${DEFAULT_ENV:-pro}"

BOOT_DIR="$SRC_ROOT/note-project/boot"
CONFIG_SRC_ROOT="${CONFIG_SRC_ROOT:-$SRC_ROOT/note-project/config}"
APP_CTL_SRC_DEFAULT="$SRC_ROOT/note-project/bin/app"
APP_CTL_DEST="$DEST_ROOT/bin/app"
JAR_DEST="$DEST_ROOT/app-note.jar"

DRY_RUN=0
NO_BACKUP=0
ENV_NAME="$DEFAULT_ENV"
BACKUP_DIR=""

usage() {
  cat <<USAGE
Usage: $(basename "$0") [--env dev|test|pro] [--dry-run] [--no-backup]

Environment:
  SRC_ROOT         Source repo root (default: $SRC_ROOT)
  CONFIG_SRC_ROOT  Config source dir (default: $CONFIG_SRC_ROOT)
  DEST_ROOT        Deploy root (default: $DEST_ROOT)
  BOOT_JAR         Source boot jar (default: latest note-project/boot/target/boot-*.jar)

Deploy layout:
  $DEST_ROOT/bin
  $DEST_ROOT/config
  $DEST_ROOT/logs
  $DEST_ROOT/app-note.jar
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

sha256_file() {
  local path="$1"
  if command -v sha256sum >/dev/null 2>&1; then
    sha256sum "$path" | awk '{print $1}'
  else
    shasum -a 256 "$path" | awk '{print $1}'
  fi
}

find_latest_boot_jar() {
  shopt -s nullglob
  local jars=("$BOOT_DIR"/target/boot-*.jar)
  shopt -u nullglob
  local newest=""
  local jar
  for jar in "${jars[@]}"; do
    case "$jar" in
      *.jar.original) continue ;;
    esac
    if [ -z "$newest" ] || [ "$jar" -nt "$newest" ]; then
      newest="$jar"
    fi
  done
  printf '%s' "$newest"
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

JAR_SRC="${BOOT_JAR:-}"
if [ -z "$JAR_SRC" ]; then
  JAR_SRC="$(find_latest_boot_jar)"
fi
if [ -z "$JAR_SRC" ] || [ ! -f "$JAR_SRC" ]; then
  echo "[err] boot jar not found: ${JAR_SRC:-<empty>}" >&2
  echo "[hint] build first: scripts/builder/build-boot-jar.sh --profile $ENV_NAME" >&2
  exit 1
fi

APP_CTL_SRC="${APP_CTL_SRC:-$APP_CTL_SRC_DEFAULT}"
if [ ! -f "$APP_CTL_SRC" ]; then
  echo "[err] app control script not found: $APP_CTL_SRC" >&2
  exit 1
fi

APP_YAML_SRC=""
if APP_YAML_SRC=$(first_existing \
  "$CONFIG_SRC_ROOT/application.yaml" \
  "$CONFIG_SRC_ROOT/application.yml" \
); then
  :
else
  echo "[err] application.yaml/application.yml not found under $CONFIG_SRC_ROOT" >&2
  exit 1
fi

ENV_YAML_SRC=""
if ENV_YAML_SRC=$(first_existing \
  "$CONFIG_SRC_ROOT/application-${ENV_NAME}.yaml" \
); then
  :
else
  echo "[err] application-${ENV_NAME}.yaml not found under $CONFIG_SRC_ROOT" >&2
  exit 1
fi

ATHENA_YAML_SRC="$(first_existing \
  "$CONFIG_SRC_ROOT/application-athena.yaml" \
  || true
)"

run "install -d -m 755 \"$DEST_ROOT\" \"$DEST_ROOT/bin\" \"$DEST_ROOT/config\" \"$DEST_ROOT/logs\" \"$DEST_ROOT/meta\" \"$DEST_ROOT/bak\""

backup_if_exists "$JAR_DEST"
TMP_JAR="$DEST_ROOT/.app-note.jar.tmp-$(ts)"
run "cp -f \"$JAR_SRC\" \"$TMP_JAR\""
run "chmod 644 \"$TMP_JAR\""
run "mv -f \"$TMP_JAR\" \"$JAR_DEST\""
log "[ok] installed jar: $JAR_DEST"

backup_if_exists "$APP_CTL_DEST"
run "cp -f \"$APP_CTL_SRC\" \"$APP_CTL_DEST\""
run "chmod 755 \"$APP_CTL_DEST\""
log "[ok] installed app ctl: $APP_CTL_DEST"

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
JAR_SHA256="$(sha256_file "$JAR_SRC")"

run "cat > \"$DEST_ROOT/meta/current-version.txt\" <<EOF_VERSION
build_time=$BUILD_TS
git_commit=$GIT_COMMIT
env=$ENV_NAME
jar_sha256=$JAR_SHA256
jar_path=$JAR_DEST
EOF_VERSION"

run "printf '%s | commit=%s | env=%s | sha256=%s\n' \"$(date '+%Y-%m-%d %H:%M:%S')\" \"$GIT_COMMIT\" \"$ENV_NAME\" \"$JAR_SHA256\" >> \"$DEST_ROOT/meta/version-history.log\""

if [ -n "$BACKUP_DIR" ]; then
  log "[ok] backup dir: $BACKUP_DIR"
fi
log "[ok] config dir: $DEST_ROOT/config"
log "[ok] version file: $DEST_ROOT/meta/current-version.txt"
log "[done] deploy finished"
