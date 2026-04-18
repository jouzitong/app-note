#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/.." && pwd)"
NOTE_PROJECT_DIR="$ROOT_DIR/note-project"
BOOT_MODULE_PATH="note-project/boot"
BOOT_TARGET_DIR="$NOTE_PROJECT_DIR/boot/target"
cd "$ROOT_DIR"

usage() {
  cat <<'USAGE'
Usage:
  scripts/start-boot-jar.sh [<jvm-opts>] [-- <app-args>]

Options:
  <jvm-opts>    JVM 参数，例如 -Dspring.profiles.active=test、-Denv=local、-Xmx1g
  -- <app-args> 透传给 Spring Boot 应用参数，例如 --server.port=19813
  -h, --help    Show this help message

Examples:
  scripts/start-boot-jar.sh
  scripts/start-boot-jar.sh -Denv=local
  scripts/start-boot-jar.sh -Dspring.profiles.active=test
  scripts/start-boot-jar.sh -Dspring.profiles.active=dev -- --server.port=19813
USAGE
}

ENV_NAME="dev"
declare -a JVM_OPTS=()
declare -a APP_ARGS=()
HAS_PROFILE_OPT=0

while (($# > 0)); do
  case "$1" in
    --)
      shift
      APP_ARGS=("$@")
      break
      ;;
    -Dspring.profiles.active=*)
      ENV_NAME="${1#*=}"
      JVM_OPTS+=("$1")
      HAS_PROFILE_OPT=1
      shift
      ;;
    -Denv=*)
      ENV_NAME="${1#*=}"
      JVM_OPTS+=("$1")
      JVM_OPTS+=("-Dspring.profiles.active=$ENV_NAME")
      HAS_PROFILE_OPT=1
      shift
      ;;
    -D*)
      JVM_OPTS+=("$1")
      shift
      ;;
    -X*)
      JVM_OPTS+=("$1")
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "[err] unknown argument: $1" >&2
      usage
      exit 2
      ;;
  esac
done

case "$ENV_NAME" in
  dev|test|local)
    ;;
  *)
    echo "[err] unsupported env: $ENV_NAME" >&2
    echo "[err] allowed values: dev, test, local" >&2
    echo "[err] pro is not allowed in this debug start script" >&2
    exit 2
    ;;
esac

if [[ "$HAS_PROFILE_OPT" -eq 0 ]]; then
  JVM_OPTS+=("-Dspring.profiles.active=$ENV_NAME")
fi

if ! command -v mvn >/dev/null 2>&1; then
  echo "[err] mvn not found in PATH" >&2
  exit 1
fi
if ! command -v java >/dev/null 2>&1; then
  echo "[err] java not found in PATH" >&2
  exit 1
fi

echo "[build] mvn -pl $BOOT_MODULE_PATH -am package -DskipTests"
mvn -pl "$BOOT_MODULE_PATH" -am package -DskipTests

CONFIG_SRC_DIR="note-project/config"
CONFIG_DEST_DIR="note-project/boot/target/config"
if [[ ! -d "$CONFIG_SRC_DIR" ]]; then
  echo "[err] config directory not found: $CONFIG_SRC_DIR" >&2
  exit 1
fi

rm -rf "$CONFIG_DEST_DIR"
mkdir -p "$CONFIG_DEST_DIR"
cp -a "$CONFIG_SRC_DIR/." "$CONFIG_DEST_DIR/"
echo "[ok] synced config: $CONFIG_SRC_DIR -> $CONFIG_DEST_DIR"

shopt -s nullglob
jar_candidates=("$BOOT_TARGET_DIR"/boot-*.jar)
shopt -u nullglob
jar_path=""
for candidate in "${jar_candidates[@]}"; do
  if [[ "$candidate" == *.jar.original ]]; then
    continue
  fi
  if [[ -z "$jar_path" || "$candidate" -nt "$jar_path" ]]; then
    jar_path="$candidate"
  fi
done

if [[ -z "$jar_path" ]]; then
  echo "[err] build succeeded but boot jar not found under $BOOT_TARGET_DIR" >&2
  exit 1
fi

echo "[run] jar: $jar_path"
echo "[run] env: $ENV_NAME"
echo "[run] config dir: $(pwd)/$CONFIG_DEST_DIR"
echo "[run] spring.config.location: file:$(pwd)/$CONFIG_DEST_DIR/"

cmd=(
  java
  "${JVM_OPTS[@]}"
  "-Dspring.config.name=application"
  "-Dspring.config.location=optional:file:$(pwd)/$CONFIG_DEST_DIR/"
  -jar
  "$jar_path"
)

if [[ "${APP_ARGS+x}" == "x" ]]; then
  cmd+=("${APP_ARGS[@]}")
fi

exec "${cmd[@]}"
