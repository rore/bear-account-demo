# Minimal Spec: Account + Transaction Log

## Goal

Implement a tiny bank-account service with two domains:

1. Account domain: owns balance and validates business rules.
2. Transaction Log domain: immutable append-only log of account operations.

Transaction Log is not directly accessible for writes by any external API. Only the Account domain may append to it.

## Delivery constraints

- In-memory only.
- No external database.
- No message bus.
- No authentication/authorization.
- Expose a minimal REST API.

## Data Model

Account

- accountId: string
- balanceCents: int (must be >= 0)

Transaction

- accountId: string
- seq: int (monotonic increasing per account, starting at 1)
- type: string ("DEPOSIT" | "WITHDRAW")
- requestId: string
- amountCents: int
- balanceAfterCents: int

## External API

### 1. Create account

- POST /accounts
- Body: { "ownerId": "string" }
- Response 200: { "accountId": "string" }

### 2. Deposit

- POST /accounts/{accountId}/deposit
- Body: { "amountCents": int, "requestId": "string" }
- Response 200: { "balanceCents": int, "txSeq": int }
- Errors:
  - 400 if amountCents <= 0 or missing requestId
  - 404 if accountId not found

### 3. Withdraw

- POST /accounts/{accountId}/withdraw
- Body: { "amountCents": int, "requestId": "string" }
- Response 200: { "balanceCents": int, "txSeq": int }
- Errors:
  - 400 if amountCents <= 0 or missing requestId
  - 404 if accountId not found
  - 409 if insufficient funds (would make balance negative)

### 4. Get balance

- GET /accounts/{accountId}/balance
- Response 200: { "balanceCents": int }
- Errors:
  - 404 if accountId not found

### 5. Get transactions

- GET /accounts/{accountId}/transactions?sinceSeq=<int>
- sinceSeq defaults to 0 if omitted
- Response 200: { "transactions": [ { "seq": int, "type": string, "requestId": string, "amountCents": int, "balanceAfterCents": int } ... ] }
- Semantics: return only transactions with seq > sinceSeq, in ascending seq order
- Errors:
  - 400 if sinceSeq < 0
  - 404 if accountId not found

## Core Rules

Idempotency

- Deposit and Withdraw are idempotent by (accountId, requestId).
- Replaying the same requestId for the same accountId must return exactly the same {balanceCents, txSeq} as the first successful execution.
- Idempotency applies only to successful operations. If the first attempt failed, a retry is treated as a normal new attempt.

Transaction Log rules

- Append-only. No updates/deletes.
- seq is assigned at append time and is strictly increasing per account.
- The Transaction Log append operation is internal-only: it can only be invoked from Account domain logic. No external endpoint may append directly.

## Architecture Constraints (Domain-Level)

- Account and Transaction Log are separate domain components with independent in-memory state.
- Account component owns balance state and business validation.
- Transaction Log component owns transaction storage and seq assignment.
- Account component must not write transaction storage directly; it may only call Transaction Log through an internal append operation.
- Transaction Log component must not read or modify Account balance state.
- No external API may append transactions directly.

## Non-goals

- No persistence requirements beyond "works in-memory for tests".
- No concurrency guarantees beyond deterministic behavior within a single process run.

