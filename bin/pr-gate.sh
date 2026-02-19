#!/usr/bin/env bash
set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "usage: pr-gate.sh <base-ref> (expected remote-tracking ref, e.g. origin/main)" >&2
  exit 64
fi
BASE_REF="$1"
if [[ "${BASE_REF}" != origin/* ]]; then
  echo "usage: pr-gate.sh <base-ref> (expected remote-tracking ref, e.g. origin/main)" >&2
  exit 64
fi

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
BLOCKS_FILE="${REPO_ROOT}/bear.blocks.yaml"
SPEC_DIR="${REPO_ROOT}/spec"

if [[ -f "${BLOCKS_FILE}" ]]; then
  "${SCRIPT_DIR}/bear.sh" pr-check --all --project "${REPO_ROOT}" --base "${BASE_REF}"
  exit $?
fi

mapfile -t IR_FILES < <(find "${SPEC_DIR}" -maxdepth 1 -type f -name '*.bear.yaml' 2>/dev/null | sort)

if [[ ${#IR_FILES[@]} -eq 0 ]]; then
  echo "pr-gate: No BEAR block index or IR files found" >&2
  echo "pr-gate: Create initial IR file(s), create bear.blocks.yaml, compile, then rerun pr-gate." >&2
  exit 64
fi

for ir in "${IR_FILES[@]}"; do
  rel="spec/$(basename "${ir}")"
  echo "pr-gate: checking ${rel} against ${BASE_REF}"
  "${SCRIPT_DIR}/bear.sh" pr-check "${rel}" --project "${REPO_ROOT}" --base "${BASE_REF}"
done
