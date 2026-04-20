#!/usr/bin/env bash

set -u

BASE_DIR="$(cd "$(dirname "$0")" && pwd)"
DATA_DIR="${DATA_DIR:-${BASE_DIR}/data-json}"
HOST="${HOST:-http://localhost:19812}"
ENDPOINT="${ENDPOINT:-/api/v1/imports/note-word/json-file}"
MAX_RETRIES="${MAX_RETRIES:-1}"
SLEEP_SECONDS="${SLEEP_SECONDS:-1}"
AUTH_HEADER="${AUTH_HEADER:-}"

usage() {
  cat <<'EOF'
Batch import note-word JSON files.

Usage:
  ./import_note_word_json_batch.sh [options]

Options:
  -h, --host <url>         API host, default: http://localhost:19812
  -e, --endpoint <path>    API path, default: /api/v1/imports/note-word/json-file
  -d, --dir <path>         JSON directory, default: ./data-json
  -r, --retries <n>        Retry times for each file, default: 1
  -s, --sleep <seconds>    Sleep between retries, default: 1
  --auth "<header>"        Extra auth header, e.g. "Authorization: Bearer xxx"
  --dry-run                Only print files, do not upload
  --help                   Show this help

Environment variables (optional):
  HOST, ENDPOINT, DATA_DIR, MAX_RETRIES, SLEEP_SECONDS, AUTH_HEADER
EOF
}

DRY_RUN=0

while [[ $# -gt 0 ]]; do
  case "$1" in
    -h|--host)
      HOST="$2"
      shift 2
      ;;
    -e|--endpoint)
      ENDPOINT="$2"
      shift 2
      ;;
    -d|--dir)
      DATA_DIR="$2"
      shift 2
      ;;
    -r|--retries)
      MAX_RETRIES="$2"
      shift 2
      ;;
    -s|--sleep)
      SLEEP_SECONDS="$2"
      shift 2
      ;;
    --auth)
      AUTH_HEADER="$2"
      shift 2
      ;;
    --dry-run)
      DRY_RUN=1
      shift
      ;;
    --help)
      usage
      exit 0
      ;;
    *)
      echo "Unknown argument: $1" >&2
      usage
      exit 1
      ;;
  esac
done

if [[ ! -d "$DATA_DIR" ]]; then
  echo "Data directory not found: $DATA_DIR" >&2
  exit 1
fi

FILES=()
while IFS= read -r -d '' file; do
  FILES+=("$file")
done < <(find "$DATA_DIR" -maxdepth 1 -type f -name '*.json' -print0 | sort -z)

TOTAL="${#FILES[@]}"
if [[ "$TOTAL" -eq 0 ]]; then
  echo "No JSON files found in: $DATA_DIR"
  exit 0
fi

URL="${HOST%/}${ENDPOINT}"
SUCCESS=0
FAILED=0
FAILED_FILES=()

echo "Target URL: $URL"
echo "Data dir  : $DATA_DIR"
echo "Files     : $TOTAL"
echo

for i in "${!FILES[@]}"; do
  FILE="${FILES[$i]}"
  BASENAME="$(basename "$FILE")"
  INDEX=$((i + 1))
  ATTEMPT=0
  FILE_OK=0

  echo "[$INDEX/$TOTAL] $BASENAME"

  if [[ "$DRY_RUN" -eq 1 ]]; then
    echo "  dry-run: skip upload"
    SUCCESS=$((SUCCESS + 1))
    continue
  fi

  while [[ "$ATTEMPT" -le "$MAX_RETRIES" ]]; do
    ATTEMPT=$((ATTEMPT + 1))

    TMP_BODY="$(mktemp)"
    HTTP_CODE=""
    if [[ -n "$AUTH_HEADER" ]]; then
      HTTP_CODE="$(curl -sS -o "$TMP_BODY" -w "%{http_code}" \
        -H "Accept: application/json" \
        -H "$AUTH_HEADER" \
        -F "file=@${FILE};type=application/json" \
        "$URL" || echo "000")"
    else
      HTTP_CODE="$(curl -sS -o "$TMP_BODY" -w "%{http_code}" \
        -H "Accept: application/json" \
        -F "file=@${FILE};type=application/json" \
        "$URL" || echo "000")"
    fi

    RESP_CODE="$(tr -d '\r\n' < "$TMP_BODY" | sed -n 's/.*"code"[[:space:]]*:[[:space:]]*\(-\{0,1\}[0-9]\{1,\}\).*/\1/p' | head -n 1)"

    if [[ "$HTTP_CODE" =~ ^2[0-9]{2}$ && "$RESP_CODE" == "0" ]]; then
      echo "  success (HTTP $HTTP_CODE, code=$RESP_CODE)"
      FILE_OK=1
      rm -f "$TMP_BODY"
      break
    fi

    if [[ -n "$RESP_CODE" ]]; then
      echo "  failed  (HTTP $HTTP_CODE, code=$RESP_CODE) attempt $ATTEMPT/$((MAX_RETRIES + 1))"
    else
      echo "  failed  (HTTP $HTTP_CODE, code=unknown) attempt $ATTEMPT/$((MAX_RETRIES + 1))"
    fi
    sed -n '1,8p' "$TMP_BODY" | sed 's/^/  body: /'
    rm -f "$TMP_BODY"

    if [[ "$ATTEMPT" -le "$MAX_RETRIES" ]]; then
      sleep "$SLEEP_SECONDS"
    fi
  done

  if [[ "$FILE_OK" -eq 1 ]]; then
    SUCCESS=$((SUCCESS + 1))
  else
    FAILED=$((FAILED + 1))
    FAILED_FILES+=("$BASENAME")
  fi
done

echo
echo "Done."
echo "Success: $SUCCESS"
echo "Failed : $FAILED"

if [[ "$FAILED" -gt 0 ]]; then
  echo "Failed files:"
  for f in "${FAILED_FILES[@]}"; do
    echo "  - $f"
  done
  exit 2
fi

exit 0
