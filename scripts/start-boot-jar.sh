#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

usage() {
  cat <<'USAGE'
Usage:
  scripts/start-boot-jar.sh -env dev|test|pro

Options:
  -env <name>   Spring profile. Required, one of: dev, test, pro
  -h, --help    Show this help message

Examples:
  scripts/start-boot-jar.sh -env dev
  scripts/start-boot-jar.sh -env test
  scripts/start-boot-jar.sh -env pro
USAGE
}

ENV_NAME=""

while (($# > 0)); do
  case "$1" in
    -env)
      if (($# < 2)); then
        echo "[err] -env requires a value" >&2
        exit 1
      fi
      ENV_NAME="$2"
      shift 2
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
  dev|test|pro)
    ;;
  "")
    echo "[err] -env is required" >&2
    usage
    exit 2
    ;;
  *)
    echo "[err] unsupported env: $ENV_NAME" >&2
    echo "[err] allowed values: dev, test, pro" >&2
    exit 2
    ;;
esac

jar_path="$(find boot/target -maxdepth 1 -type f -name 'boot-*.jar' ! -name '*.jar.original' | sort | head -n 1 || true)"
if [[ -z "$jar_path" ]]; then
  echo "[err] boot jar not found under boot/target" >&2
  echo "[err] build it first: mvn -pl boot -am package -DskipTests" >&2
  exit 1
fi

echo "[run] jar: $jar_path"
echo "[run] env: $ENV_NAME"

exec java -jar "$jar_path" --spring.profiles.active="$ENV_NAME"
