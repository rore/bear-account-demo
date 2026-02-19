# WORKFLOW.md (Demo Runbook)

Source-of-truth:
- `bear-cli/doc/m1-canonical/WORKFLOW.md`

## Read In This Order

1. `doc/BEAR_PRIMER.md`
2. request prompt in `doc/SCENARIOS.md`

## Standard Flow

1. Read request.
2. Discover BEAR structure in repo (IR/index may be missing in greenfield).
3. Apply IR-first for boundary/contract/effect changes.
4. Implement in user-owned sources only.
5. Run canonical gate (`bin/bear-all.*`) until exit `0`.

## Canonical Gate

`bin/bear-all.*` behavior:
- if `bear.blocks.yaml` exists: `bear check --all --project <repoRoot>`
- else: loop `bear check <ir-file> --project <repoRoot>` over `spec/*.bear.yaml`

## PR Governance

`bin/pr-gate.* <base-ref>` behavior:
- if `bear.blocks.yaml` exists: `bear pr-check --all --project <repoRoot> --base <base-ref>`
- else: loop `bear pr-check <ir-file> --project <repoRoot> --base <base-ref>` over `spec/*.bear.yaml`

## Constraints

- No generated-file edits.
- No silent boundary expansion.
- One command determines done/not-done.
