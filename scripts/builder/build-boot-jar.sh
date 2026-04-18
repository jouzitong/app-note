#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
ROOT_DIR="$(cd "$SCRIPT_DIR/../.." && pwd)"
cd "$ROOT_DIR"

usage() {
  cat <<'USAGE'
Usage:
  scripts/builder/build-boot-jar.sh [options] [maven-args...] [-- <app-args>]

Options:
  --profile <name>  Spring profile for optional --run step. Default: pro
  --run             Run built boot jar after packaging
  -h, --help        Show this help message

Environment:
  BOOT_JAR  Boot jar path for --run (default: latest boot/target/boot-*.jar)

Examples:
  scripts/builder/build-boot-jar.sh
  scripts/builder/build-boot-jar.sh --profile dev
  scripts/builder/build-boot-jar.sh --run -- --server.port=8081
USAGE
}

PROFILE="pro"
RUN_AFTER_BUILD=0
declare -a MAVEN_ARGS=()
declare -a APP_ARGS=()

while [ "$#" -gt 0 ]; do
  case "$1" in
    --profile)
      if [ "$#" -lt 2 ]; then
        echo "[err] --profile requires a value" >&2
        exit 2
      fi
      PROFILE="$2"
      shift 2
      ;;
    --run)
      RUN_AFTER_BUILD=1
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    --)
      shift
      APP_ARGS=("$@")
      break
      ;;
    *)
      MAVEN_ARGS+=("$1")
      shift
      ;;
  esac
done

case "$PROFILE" in
  dev|test|pro|local) ;;
  *)
    echo "[err] unsupported profile: $PROFILE (allowed: dev|test|pro|local)" >&2
    exit 2
    ;;
esac

if ! command -v mvn >/dev/null 2>&1; then
  echo "[err] mvn not found in PATH" >&2
  exit 1
fi

build_cmd=(mvn -pl boot -am -DskipTests package)
if [ "${#MAVEN_ARGS[@]}" -gt 0 ]; then
  build_cmd+=("${MAVEN_ARGS[@]}")
fi
echo "[build] ${build_cmd[*]}"
"${build_cmd[@]}"

echo "[check] boot/target"
ls -la boot/target | sed -n '1,120p'

find_latest_boot_jar() {
  shopt -s nullglob
  local jars=(boot/target/boot-*.jar)
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

JAR_PATH="${BOOT_JAR:-}"
if [ -z "$JAR_PATH" ]; then
  JAR_PATH="$(find_latest_boot_jar)"
fi

if [ -z "$JAR_PATH" ] || [ ! -f "$JAR_PATH" ]; then
  echo "[err] boot jar not found: ${JAR_PATH:-<empty>}" >&2
  exit 1
fi

echo "[ok] boot jar: $JAR_PATH"

if [ "$RUN_AFTER_BUILD" -eq 1 ]; then
  if ! command -v java >/dev/null 2>&1; then
    echo "[err] java not found in PATH" >&2
    exit 1
  fi
  if [ ! -d "$ROOT_DIR/config" ]; then
    echo "[err] config directory not found: $ROOT_DIR/config" >&2
    exit 1
  fi
  cmd=(
    java
    "-Dspring.profiles.active=$PROFILE"
    "-Dspring.config.additional-location=optional:file:$(pwd)/config/"
    -jar
    "$JAR_PATH"
  )
  if [ "${#APP_ARGS[@]}" -gt 0 ]; then
    cmd+=("${APP_ARGS[@]}")
  fi
  echo "[run] ${cmd[*]}"
  exec "${cmd[@]}"
fi
