#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
IR_FILE="${REPO_ROOT}/spec/withdraw.bear.yaml"

if [[ ! -f "${IR_FILE}" ]]; then
  echo "bear-all: missing IR file: ${IR_FILE}" >&2
  exit 64
fi

"${SCRIPT_DIR}/bear.sh" check "${IR_FILE}" --project "${REPO_ROOT}"