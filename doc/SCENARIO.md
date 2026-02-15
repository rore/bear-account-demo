# scenario/corrected-pass-withdraw

Intent: demonstrate deterministic pass with corrected Withdraw implementation.

## Command (from `bear-cli`)

```powershell
.\gradlew.bat --no-daemon :app:run --args="check spec/fixtures/withdraw.bear.yaml --project ../bear-account-demo"
```

## Expected Result

- Exit code: `0`
- Exact snippet:

```text
check: OK
```
