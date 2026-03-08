# Feature Extension Spec: Transaction Notes

This spec extends the baseline account and transaction log service defined in spec/SPEC.md.

The original spec remains in force except where this file adds or clarifies behavior.

## Goal

Add support for an optional human-readable note on deposit and withdraw operations.

The note is stored with the transaction entry and returned when transactions are queried.

## Data Model Extension

Transaction adds:

- note: optional string

## External API Changes

### 1. Deposit

- POST /accounts/{accountId}/deposit
- Request body adds an optional note field
- Response stays: balanceCents and txSeq
- Errors remain the same as the baseline spec

### 2. Withdraw

- POST /accounts/{accountId}/withdraw
- Request body adds an optional note field
- Response stays: balanceCents and txSeq
- Errors remain the same as the baseline spec

### 3. Get transactions

- GET /accounts/{accountId}/transactions?sinceSeq=<int>
- Each returned transaction includes note when a note was supplied on the original operation

Example transaction item:

- seq: 2
- type: WITHDRAW
- requestId: req-2
- amountCents: 200
- balanceAfterCents: 1300
- note: ATM withdrawal

## Core Rules

- note is optional for both Deposit and Withdraw
- if omitted, the operation remains valid
- if provided, the note must be stored exactly as supplied
- transaction query results must preserve the original note value
- idempotent replay of a successful Deposit or Withdraw must return the same result as the first successful execution
- if the original successful request included a note, the stored transaction for that operation must retain that same note

## Architecture Constraints

- the existing external API shape is extended only by the optional note field
- the existing account and transaction log responsibilities remain unchanged
- transaction notes belong to transaction log data, not account balance state

## Non-goals

- no full-text search
- no update or delete of notes after the transaction is created
- no separate notes endpoint
