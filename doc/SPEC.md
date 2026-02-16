# Account Demo Spec (v0 / M1)

This demo centers on one behavior: `Withdraw`.

For detailed domain pack, see:
- `doc/spec/app.md`
- `doc/spec/apis.md`
- `doc/spec/rules.md`
- `doc/spec/blocks.md`

## Withdraw Contract

Inputs:
- `accountId: string`
- `amount: decimal`
- `currency: string`
- `txId: string`

Output:
- `balance: decimal`

Allowed capabilities:
- `ledger.getBalance`
- `ledger.setBalance`
- `idempotency.get`
- `idempotency.put`

Idempotency:
- key: `txId`

Invariant:
- `non_negative(balance)`

Local IR source of truth in this repo:
- `spec/*.bear.yaml` (created during greenfield bootstrap)