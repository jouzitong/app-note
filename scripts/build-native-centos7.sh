#!/usr/bin/env bash
set -euo pipefail

cd "$(dirname "$0")/.."

usage() {
  cat <<'USAGE'
Usage:
  scripts/build-native-centos7.sh [options] [maven-args...]

Options:
  --profile <name>   Spring profile used for native AOT build. Default: pro
  --run              Run the built native binary after packaging
  --help             Show this help message

Environment variables:
  SPRING_PROFILES_ACTIVE            Default profile when --profile is not given (default: pro)
  SPRING_CONFIG_LOCATION            Optional Spring config location
  SPRING_CONFIG_ADDITIONAL_LOCATION Optional Spring additional config location
  NATIVE_BIN                        Native binary path for --run (default: boot/target/app-note)

Examples:
  scripts/build-native-centos7.sh
  scripts/build-native-centos7.sh --profile dev
  SPRING_CONFIG_ADDITIONAL_LOCATION=./config/ scripts/build-native-centos7.sh
  scripts/build-native-centos7.sh --profile pro --run -- --server.port=8081
USAGE
}

PROFILE="${SPRING_PROFILES_ACTIVE:-pro}"
RUN_AFTER_BUILD=false
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

echo "[env] glibc:"
rpm -q glibc || true

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

if [[ "${RUN_AFTER_BUILD}" == "true" ]]; then
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
