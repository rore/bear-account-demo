# blocks.md

Source-of-truth:
- `bear-account-demo/doc/spec/blocks.md`

## Withdraw

Responsibilities:
- process withdrawal request
- use declared ledger and idempotency capabilities
- enforce non-negative resulting balance

Current declared capabilities:
- `ledger.getBalance`
- `ledger.setBalance`
- `idempotency.get`
- `idempotency.put`
