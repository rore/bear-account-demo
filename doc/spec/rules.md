# rules.md

Source-of-truth:
- `bear-account-demo/doc/spec/rules.md`

Rules:
- no overdraft: resulting balances must remain non-negative
- idempotency: same `txId` must not double-apply effects
- every successful money operation must persist an append-only audit record
- every operation must emit an event for downstream consumers
