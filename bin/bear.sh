#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
VENDORED_BEAR="${REPO_ROOT}/tools/bear-cli/bin/bear"
LOCAL_BEAR="${REPO_ROOT}/.bear/tools/bear-cli/bin/bear"

# On Unix runners we only execute the shell launcher, never .bat files.
if [[ -f "${VENDORED_BEAR}" ]]; then
  bash "${VENDORED_BEAR}" "$@"
  exit $?
fi

if [[ -f "${LOCAL_BEAR}" ]]; then
  bash "${LOCAL_BEAR}" "$@"
  exit $?
fi

if command -v bear >/dev/null 2>&1; then
  bear "$@"
  exit $?
fi

echo "bear wrapper: missing BEAR CLI. Expected tools/bear-cli/bin/bear, .bear/tools/bear-cli/bin/bear, or bear on PATH." >&2
exit 127
