# scenario/naive-fail-withdraw

Intent: demonstrate deterministic test-gate failure with a naive Withdraw implementation.

## Command (from `bear-cli`)

```powershell
.\gradlew.bat --no-daemon :app:run --args="check spec/fixtures/withdraw.bear.yaml --project ../bear-account-demo"
```

## Expected Result

- Exit code: `4`
- Exact snippet:

```text
check: TEST_FAILED: project tests failed
```
