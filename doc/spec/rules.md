# rules.md

Source-of-truth:
- `bear-account-demo/doc/spec/rules.md`

Rules:
- no overdraft: resulting `balance` must be non-negative
- idempotency: same `txId` must not double-apply withdraw
- ledger write should represent exactly one resulting balance on success
