# Account Demo Spec (v0)

This demo currently covers one application behavior: `Withdraw`.

## Withdraw

Inputs:
- `accountId: string`
- `amount: decimal`
- `currency: string`
- `txId: string`

Output:
- `balance: decimal`

Allowed capabilities:
- `ledger`
  - `getBalance`
  - `setBalance`
- `idempotency`
  - `get`
  - `put`

Idempotency:
- key: `txId`
- store capability: `idempotency` (`get`/`put`)

Invariant:
- `non_negative(balance)`

IR source of truth in `bear-cli`:
- `spec/fixtures/withdraw.bear.yaml`
