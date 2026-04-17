#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
NOTE_UI_DIR="$ROOT_DIR/note-ui"
DIST_DIR="$NOTE_UI_DIR/dist"

if ! command -v npm >/dev/null 2>&1; then
  echo "[error] npm not found, please install Node.js first."
  exit 1
fi

if ! command -v zip >/dev/null 2>&1; then
  echo "[error] zip command not found, please install zip first."
  exit 1
fi

if [[ ! -d "$NOTE_UI_DIR" ]]; then
  echo "[error] note-ui directory not found: $NOTE_UI_DIR"
  exit 1
fi

cd "$NOTE_UI_DIR"

if [[ ! -d "node_modules" ]]; then
  echo "[build] node_modules not found, running npm ci..."
  npm ci
else
  echo "[build] running npm ci to ensure lockfile-consistent deps..."
  npm ci
fi

echo "[build] npm run build"
npm run build

if [[ ! -d "$DIST_DIR" ]]; then
  echo "[error] dist directory not found after build: $DIST_DIR"
  exit 1
fi

ZIP_OUTPUT="${1:-note-ui-dist-$(date +%Y%m%d-%H%M%S).zip}"
case "$ZIP_OUTPUT" in
  /*) ZIP_PATH="$ZIP_OUTPUT" ;;
  *) ZIP_PATH="$ROOT_DIR/$ZIP_OUTPUT" ;;
esac

mkdir -p "$(dirname "$ZIP_PATH")"
rm -f "$ZIP_PATH"

echo "[zip] creating: $ZIP_PATH"
(
  cd "$NOTE_UI_DIR"
  zip -r "$ZIP_PATH" dist
)

echo "[ok] build and zip completed"
echo "[ok] zip file: $ZIP_PATH"
