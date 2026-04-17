#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

usage() {
  cat <<'USAGE'
Usage:
  scripts/build-native-linux.sh [options] [maven-args...]

Options:
  --profile <name>       Spring profile used for native AOT build. Default: pro
  --target-glibc <ver>   Fail when output binary requires a higher GLIBC version
  --no-glibc-check       Disable output binary GLIBC compatibility check
  --run                  Run the built native binary after packaging
  --help                 Show this help message

Environment variables:
  SPRING_PROFILES_ACTIVE            Default profile when --profile is not given (default: pro)
  SPRING_CONFIG_LOCATION            Optional Spring config location
  SPRING_CONFIG_ADDITIONAL_LOCATION Optional Spring additional config location
  NATIVE_BIN                        Native binary path for --run (default: boot/target/app-note)
  TARGET_GLIBC                      Same as --target-glibc

Examples:
  scripts/build-native-linux.sh
  scripts/build-native-linux.sh --profile dev
  scripts/build-native-linux.sh --target-glibc 2.17
  SPRING_CONFIG_ADDITIONAL_LOCATION=./config/ scripts/build-native-linux.sh
  scripts/build-native-linux.sh --profile pro --run -- --server.port=8081
USAGE
}

version_gt() {
  local a="$1"
  local b="$2"
  [[ "$(printf '%s\n' "$a" "$b" | sort -V | tail -n 1)" == "$a" && "$a" != "$b" ]]
}

detect_glibc_version() {
  local glibc_ver=""
  if command -v getconf >/dev/null 2>&1; then
    glibc_ver="$(getconf GNU_LIBC_VERSION 2>/dev/null | awk '{print $2}' || true)"
  fi
  if [[ -z "${glibc_ver}" ]] && command -v ldd >/dev/null 2>&1; then
    glibc_ver="$(ldd --version 2>/dev/null | head -n 1 | sed -n 's/.* \([0-9][0-9.]*\)$/\1/p' || true)"
  fi
  echo "${glibc_ver}"
}

extract_required_glibc_max() {
  local bin_path="$1"
  strings -a "$bin_path" \
    | rg -o 'GLIBC_[0-9]+\.[0-9]+' \
    | sed 's/^GLIBC_//' \
    | sort -Vu \
    | tail -n 1
}

PROFILE="${SPRING_PROFILES_ACTIVE:-pro}"
RUN_AFTER_BUILD=false
TARGET_GLIBC="${TARGET_GLIBC:-}"
ENABLE_GLIBC_CHECK=true
declare -a EXTRA_MVN_ARGS=()
declare -a RUN_ARGS=()

while (($# > 0)); do
  case "$1" in
    --profile)
      if (($# < 2)); then
        echo "[err] --profile requires a value" >&2
        exit 1
      fi
      PROFILE="$2"
      shift 2
      ;;
    --target-glibc)
      if (($# < 2)); then
        echo "[err] --target-glibc requires a value" >&2
        exit 1
      fi
      TARGET_GLIBC="$2"
      shift 2
      ;;
    --no-glibc-check)
      ENABLE_GLIBC_CHECK=false
      shift
      ;;
    --run)
      RUN_AFTER_BUILD=true
      shift
      ;;
    --help|-h)
      usage
      exit 0
      ;;
    --)
      shift
      RUN_ARGS=("$@")
      break
      ;;
    *)
      EXTRA_MVN_ARGS+=("$1")
      shift
      ;;
  esac
done

echo "[env] glibc package:"
rpm -q glibc || true

runtime_glibc="$(detect_glibc_version)"
if [[ -n "${runtime_glibc}" ]]; then
  echo "[env] glibc runtime: ${runtime_glibc}"
else
  echo "[warn] unable to detect runtime glibc version"
fi

echo "[env] java:"
java -version

echo "[env] native-image:"
native-image --version

echo "[build] spring profile: ${PROFILE}"

mvn_cmd=(
  mvn -pl boot -am -Pnative -DskipTests package
  "-Dspring.profiles.active=${PROFILE}"
)

if [[ -n "${SPRING_CONFIG_LOCATION:-}" ]]; then
  mvn_cmd+=("-Dspring.config.location=${SPRING_CONFIG_LOCATION}")
fi
if [[ -n "${SPRING_CONFIG_ADDITIONAL_LOCATION:-}" ]]; then
  mvn_cmd+=("-Dspring.config.additional-location=${SPRING_CONFIG_ADDITIONAL_LOCATION}")
fi
if ((${#EXTRA_MVN_ARGS[@]} > 0)); then
  mvn_cmd+=("${EXTRA_MVN_ARGS[@]}")
fi

echo "[build] command: ${mvn_cmd[*]}"
"${mvn_cmd[@]}"

echo "[ok] output:"
ls -la boot/target | sed -n '1,120p'

bin_path="${NATIVE_BIN:-boot/target/app-note}"
if [[ ! -x "${bin_path}" ]]; then
  fallback_bin="$(find boot/target -maxdepth 1 -type f -name 'app-note*' -perm -111 | head -n 1 || true)"
  if [[ -n "${fallback_bin}" ]]; then
    bin_path="${fallback_bin}"
  fi
fi

if [[ ! -x "${bin_path}" ]]; then
  echo "[err] native binary not found or not executable: ${bin_path}" >&2
  exit 1
fi
echo "[ok] native binary: ${bin_path}"

if [[ "${ENABLE_GLIBC_CHECK}" == "true" && -n "${TARGET_GLIBC}" ]]; then
  required_glibc="$(extract_required_glibc_max "${bin_path}" || true)"
  if [[ -z "${required_glibc}" ]]; then
    echo "[warn] unable to inspect required GLIBC version from binary"
  else
    echo "[check] binary max required GLIBC: ${required_glibc}"
    echo "[check] target GLIBC: ${TARGET_GLIBC}"
    if version_gt "${required_glibc}" "${TARGET_GLIBC}"; then
      echo "[err] incompatible binary: requires GLIBC ${required_glibc}, target is ${TARGET_GLIBC}" >&2
      echo "[hint] build on a host/container with glibc <= ${TARGET_GLIBC}, or rerun with --no-glibc-check if you know the risk" >&2
      exit 1
    fi
  fi
fi

if [[ "${RUN_AFTER_BUILD}" == "true" ]]; then
  run_cmd=("${bin_path}" "--spring.profiles.active=${PROFILE}")
  if [[ -n "${SPRING_CONFIG_LOCATION:-}" ]]; then
    run_cmd+=("--spring.config.location=${SPRING_CONFIG_LOCATION}")
  fi
  if [[ -n "${SPRING_CONFIG_ADDITIONAL_LOCATION:-}" ]]; then
    run_cmd+=("--spring.config.additional-location=${SPRING_CONFIG_ADDITIONAL_LOCATION}")
  fi
  if ((${#RUN_ARGS[@]} > 0)); then
    run_cmd+=("${RUN_ARGS[@]}")
  fi

  echo "[run] command: ${run_cmd[*]}"
  exec "${run_cmd[@]}"
fi
