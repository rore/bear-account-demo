#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
PACKAGED_BEAR="${REPO_ROOT}/.bear/tools/bear-cli/bin/bear"

if [[ -f "${PACKAGED_BEAR}" ]]; then
  bash "${PACKAGED_BEAR}" "$@"
  exit $?
fi

if command -v bear >/dev/null 2>&1; then
  bear "$@"
  exit $?
fi

echo "bear wrapper: missing BEAR CLI. Expected .bear/tools/bear-cli/bin/bear or bear on PATH." >&2
exit 127