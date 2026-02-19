# app.md

Source-of-truth:
- `bear-account-demo/doc/spec/app.md`

Domain:
- account service
- operations: deposit, withdraw, transfer
- extension target: scheduled transfers

Quality constraints:
- non-negative balances
- idempotent request handling
- append-only audit logging
- event emission per operation
