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
if [[ ! -d "${REPO_ROOT}" ]]; then
  if ! REPO_ROOT="$(git -C "${SCRIPT_DIR}" rev-parse --show-toplevel 2>/dev/null)"; then
    echo "pr-gate: unable to resolve repo root" >&2
    exit 74
  fi
fi
SPEC_DIR="${REPO_ROOT}/spec"
VENDORED_BEAR="${REPO_ROOT}/tools/bear-cli/bin/bear"
LOCAL_BEAR="${REPO_ROOT}/.bear/tools/bear-cli/bin/bear"

run_bear() {
  if [[ -f "${VENDORED_BEAR}" ]]; then
    bash "${VENDORED_BEAR}" "$@"
    return $?
  fi
  if [[ -f "${LOCAL_BEAR}" ]]; then
    bash "${LOCAL_BEAR}" "$@"
    return $?
  fi
  if command -v bear >/dev/null 2>&1; then
    bear "$@"
    return $?
  fi
  echo "pr-gate: missing BEAR CLI. Expected tools/bear-cli/bin/bear, .bear/tools/bear-cli/bin/bear, or bear on PATH." >&2
  return 127
}

mapfile -t IR_FILES < <(find "${SPEC_DIR}" -maxdepth 1 -type f -name '*.bear.yaml' 2>/dev/null | sort)

if [[ ${#IR_FILES[@]} -eq 0 ]]; then
  echo "pr-gate: No IR files found under spec/*.bear.yaml" >&2
  echo "pr-gate: Create initial block IR, run compile, then rerun pr-gate." >&2
  exit 64
fi

for ir in "${IR_FILES[@]}"; do
  rel="spec/$(basename "${ir}")"
  echo "pr-gate: checking ${rel} against ${BASE_REF}"
  run_bear pr-check "${rel}" --project "${REPO_ROOT}" --base "${BASE_REF}"
done
