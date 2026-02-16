# bear-account-demo

Standalone M1 demo repository for BEAR agent workflow proof.

## M1 Goal

Given only this repo and a feature request, an agent can:
- complete one non-boundary feature
- complete one boundary-expanding feature with IR-first workflow
- use one canonical gate command as done/not-done signal

## Tooling Assumption

Expected local BEAR CLI location:
- `.bear/tools/bear-cli/bin/bear` (or `.bat` on Windows)

`bin/bear.*` also supports `bear` on PATH as a development fallback.

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