# bear-account-demo

Standalone multi-block BEAR demo for realistic agent workflow validation.

## Goal

Validate the BEAR claim end-to-end:
- developer gives a normal product request
- agent creates required BEAR governance artifacts
- BEAR enforces deterministically with one canonical gate

## Read First

1. `doc/BEAR_PRIMER.md`
2. `WORKFLOW.md`
3. `doc/SCENARIOS.md`

## Canonical Commands

Windows:

```powershell
.\bin\bear-all.ps1
.\bin\pr-gate.ps1 <base-ref>
```

Bash:

```sh
./bin/bear-all.sh
./bin/pr-gate.sh <base-ref>
```

Gate behavior:
- if `bear.blocks.yaml` exists -> use repo-level `--all`
- otherwise -> fallback to deterministic `spec/*.bear.yaml` loop

## Scenario Branch

Start here:
- `scenario/1-greenfield-multiblock-start`

After greenfield passes, create:
- `scenario/1-greenfield-pass`
