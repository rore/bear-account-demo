# bear-account-demo

Standalone multi-block BEAR demo for realistic agent workflow validation.

## Goal

Validate the BEAR claim end-to-end:
- developer gives a normal product request
- agent creates BEAR decomposition (`spec/*.bear.yaml` + `bear.blocks.yaml`)
- BEAR deterministically enforces structure with one canonical gate

## Read First

1. `doc/BEAR_PRIMER.md`
2. `doc/spec/*`
3. `WORKFLOW.md`
4. `doc/SCENARIOS.md`

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

After you complete greenfield and reach pass, create:
- `scenario/1-greenfield-pass`

Then run extension scenario from:
- `scenario/1-greenfield-pass`
