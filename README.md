# bear-account-demo

Spec-first demo repository for BEAR usage.

This repo is structured to show BEAR in an agentic workflow using a stable baseline and isolated scenario branches.

## Main Branch Policy

`main` is the canonical spec-driven baseline:
- contains demo spec and minimal runnable scaffold
- contains project documentation
- does not contain intentionally naive/failing scenario variants
- does not contain intentional drift artifacts

## Quickstart (from `bear-cli` repo)

1. Compile generated artifacts into demo:

```powershell
.\gradlew.bat --no-daemon :app:run --args="compile spec/fixtures/withdraw.bear.yaml --project ../bear-account-demo"
```

2. Run BEAR gate against demo:

```powershell
.\gradlew.bat --no-daemon :app:run --args="check spec/fixtures/withdraw.bear.yaml --project ../bear-account-demo"
```

## Scenario Branches

See project docs for branch details.

Current scenario branches:
- `scenario/naive-fail-withdraw`
- `scenario/corrected-pass-withdraw`

Reserved next branch:
- `scenario/boundary-expansion-visible`

