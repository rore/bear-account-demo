#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
LOCAL_BEAR="${REPO_ROOT}/.bear/tools/bear-cli/bin/bear"
LOCAL_BEAR_BAT="${REPO_ROOT}/.bear/tools/bear-cli/bin/bear.bat"

if [[ -x "${LOCAL_BEAR}" ]]; then
  "${LOCAL_BEAR}" "$@"
  exit $?
fi

if [[ -f "${LOCAL_BEAR_BAT}" ]]; then
  "${LOCAL_BEAR_BAT}" "$@"
  exit $?
fi

if command -v bear >/dev/null 2>&1; then
  bear "$@"
  exit $?
fi

echo "bear wrapper: missing BEAR CLI. Expected .bear/tools/bear-cli/bin/bear(.bat) or bear on PATH." >&2
exit 127