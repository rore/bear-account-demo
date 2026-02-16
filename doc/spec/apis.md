# apis.md

Source-of-truth:
- `bear-account-demo/doc/spec/apis.md`

Primary API shape:
- `POST /accounts/{accountId}/withdraw`

Request:
- `accountId: string`
- `amount: decimal`
- `currency: string`
- `txId: string`

Response:
- `balance: decimal`