#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/.." && pwd)"
SPEC_DIR="${REPO_ROOT}/spec"

mapfile -t IR_FILES < <(find "${SPEC_DIR}" -maxdepth 1 -type f -name '*.bear.yaml' 2>/dev/null | sort)

if [[ ${#IR_FILES[@]} -eq 0 ]]; then
  echo "bear-all: No IR files found under spec/*.bear.yaml" >&2
  echo "bear-all: Create initial block IR, run compile, then rerun bear-all." >&2
  exit 64
fi

for ir in "${IR_FILES[@]}"; do
  rel="spec/$(basename "${ir}")"
  echo "bear-all: checking ${rel}"
  "${SCRIPT_DIR}/bear.sh" check "${ir}" --project "${REPO_ROOT}"
done