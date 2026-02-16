# bear-account-demo

Standalone M1 demo repository for BEAR agent workflow proof.

## Agent Bootstrap

Codex sessions auto-load AGENTS.md at repo root. In a real project this file should be a thin bootstrap that points to BEAR_AGENT.md, where BEAR operating rules live.

## M1 Goal

Given only this repo and a feature request, an agent can:
- complete one non-boundary feature
- complete one boundary-expanding feature with IR-first workflow
- use one canonical gate command as done/not-done signal

## Tooling Assumption

Expected local BEAR CLI location:
- `.bear/tools/bear-cli/bin/bear` (or `.bat` on Windows)

`bin/bear.*` also supports `bear` on PATH as a development fallback.

## First-Time Bootstrap

1. Read:
- `doc/BEAR_PRIMER.md`
- `doc/spec/*`
2. If no IR exists in `spec/*.bear.yaml`, create the first block IR.
3. Compile that IR:

```powershell
.\bin\bear.ps1 compile <your-ir-file> --project .
```

4. Run the canonical gate:

```powershell
.\bin\bear-all.ps1
```

## Canonical Commands

Compile baseline generated artifacts:

```powershell
.\bin\bear.ps1 compile spec/withdraw.bear.yaml --project .
```

Run the canonical gate:

```powershell
.\bin\bear-all.ps1
```

Bash:

```sh
./bin/bear.sh compile spec/withdraw.bear.yaml --project .
./bin/bear-all.sh
```

## Canonical M1 Scenario Branches

- `scenario/greenfield-build`
- `scenario/feature-extension`

Legacy/non-canonical branches:
- `scenario/naive-fail-withdraw`
- `scenario/corrected-pass-withdraw`

Evaluator runbooks and expected outcomes are intentionally not stored in this repo.
They live in `bear-cli/doc/m1-eval/`.


