# Product Spec: Wallet Service (BEAR Demo)

## Goal
Build a small backend service that manages wallets and simple money movement.

## Delivery constraints

In-memory only.

No external database.

No message bus.

No authentication.

Expose a minimal REST API.

## Entities

Wallet: walletId, ownerId, status

Balance: integer cents

Operation: seq, opId, requestId, type, amountCents, walletId, balanceCents

## API contract

### 1. Create wallet

POST /wallets

Input: ownerId

Output: walletId

### 2. Deposit (idempotent)

POST /wallets/{walletId}/deposits

Input: amountCents, requestId

Rules:

amountCents > 0

idempotent by (walletId, requestId)

replay returns the exact same {opId, balanceCents} as the first successful call

Output: opId, balanceCents

### 3. Withdraw (idempotent + non-negative)

POST /wallets/{walletId}/withdrawals

Input: amountCents, requestId

Rules:

amountCents > 0

resulting balance must not be negative

idempotent by (walletId, requestId)

replay returns the exact same {opId, balanceCents} as the first successful call

Output: opId, balanceCents

### 4. Get balance

GET /wallets/{walletId}/balance

Output: balanceCents

### 5. Get statement

GET /wallets/{walletId}/statement?sinceSeq=<optional>

Output: entries ordered by seq ascending

Each entry contains: seq, opId, requestId, type, amountCents, balanceCents

## Ordering and identifiers

The service assigns each operation a monotonically increasing seq.

Statement ordering is defined by seq.

opId is service-generated and unique within a single run.

## Error expectations

invalid amount => 400 validation error

missing wallet => 404 not found

insufficient funds => 409 domain error

idempotent replay => 200 with the same successful payload as the original request

