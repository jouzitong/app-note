#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
echo "[warn] scripts/deploy-app-noe.sh is deprecated, use scripts/deploy-app-note.sh"
exec "$SCRIPT_DIR/deploy-app-note.sh" "$@"
