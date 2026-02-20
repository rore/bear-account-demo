# Product Spec: Wallet Transfer Service

## Goal
Build a small backend service that manages wallets and money movement with deterministic behavior.

## Delivery Constraints
- In-memory only.
- No external database.
- No message bus.
- No real authentication.
- Coarse locking per wallet is acceptable.
- Expose a minimal REST API.

## Entities
- Wallet: `walletId`, `ownerId`, `status`
- Balance: integer cents
- Operation: `opId`, `requestId`, `type`, `amountCents`, `fromWalletId?`, `toWalletId?`, `timestamp`, `result`
- Ledger entry: immutable record derived from operations

## API Contract

### 1. Create wallet
- Endpoint: `POST /wallets`
- Input: `ownerId`
- Output: `walletId`

### 2. Deposit (idempotent)
- Endpoint: `POST /wallets/{walletId}/deposits`
- Input: `amountCents`, `requestId`
- Rules:
  - `amountCents > 0`
  - idempotent by `(walletId, requestId)`
- Output: `opId`, `balanceCents`

### 3. Withdraw (idempotent + non-negative)
- Endpoint: `POST /wallets/{walletId}/withdrawals`
- Input: `amountCents`, `requestId`
- Rules:
  - `amountCents > 0`
  - resulting balance must never be negative
  - idempotent by `(walletId, requestId)`
- Output: `opId`, `balanceCents`

### 4. Transfer (idempotent + atomic + non-negative)
- Endpoint: `POST /transfers`
- Input: `fromWalletId`, `toWalletId`, `amountCents`, `requestId`
- Rules:
  - `amountCents > 0`
  - source balance must never be negative
  - logically atomic (both ledger sides recorded or neither)
  - idempotent by `(fromWalletId, requestId)`
- Output: `opId`, `fromBalanceCents`, `toBalanceCents`

### 5. Get balance
- Endpoint: `GET /wallets/{walletId}/balance`
- Output: `balanceCents`

### 6. Get statement
- Endpoint: `GET /wallets/{walletId}/statement?sinceTimestamp=<optional>`
- Output: ordered ledger entries for the wallet (stable ordering)

## Ordering and Time
- Statement ordering must be deterministic and stable for repeated reads.
- Timestamps are service-generated.

## Error Expectations
- invalid amount => validation error
- missing wallet => not found
- insufficient funds => domain error with no state mutation
- idempotent replay => same successful result payload as original request