# Scenario Matrix

This file defines the branch-per-scenario demo model.

| Branch | Intent | Expected `bear check` result | Expected snippet |
| --- | --- | --- | --- |
| `main` | Spec-first runnable baseline | informational baseline | `check: OK` (after compile + valid impl) |
| `scenario/naive-fail-withdraw` | Intentionally naive Withdraw impl | Exit `4` | `check: TEST_FAILED: project tests failed` |
| `scenario/corrected-pass-withdraw` | Corrected Withdraw impl | Exit `0` | `check: OK` |
| `scenario/boundary-expansion-visible` | Reserve: boundary signal proof | planned | planned |

## Canonical Commands (run from `bear-cli`)

Compile:

```powershell
.\gradlew.bat --no-daemon :app:run --args="compile spec/fixtures/withdraw.bear.yaml --project ../bear-account-demo"
```

Check:

```powershell
.\gradlew.bat --no-daemon :app:run --args="check spec/fixtures/withdraw.bear.yaml --project ../bear-account-demo"
```

## Jump To Scenario Branches

```powershell
git checkout scenario/naive-fail-withdraw
# or
git checkout scenario/corrected-pass-withdraw
```

Each scenario branch also includes a local `doc/SCENARIO.md` with exact expected output snippets.
