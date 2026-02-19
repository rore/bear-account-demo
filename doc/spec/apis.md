# apis.md

Source-of-truth:
- `bear-account-demo/doc/spec/apis.md`

Primary API intents:
- deposit funds
- withdraw funds
- transfer funds between accounts

Common request fields (by operation):
- `accountId: string`
- `amount: decimal`
- `currency: string`
- `txId: string` (idempotency key)

Transfer-specific fields:
- `fromAccountId: string`
- `toAccountId: string`

Common response intent:
- resulting balance and operation status
