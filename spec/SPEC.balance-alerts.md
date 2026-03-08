# Feature Spec: Balance Alerts

This spec extends the baseline account service defined in `spec/SPEC.md`.

The original spec remains in force except where this file adds or clarifies behavior.

## Goal

Add balance alerts so the system records important balance-related alert events for an account.

## New Behavior

### 1. Failed-withdraw alert

If a withdraw request fails because of insufficient funds, the system must record an alert for that account.

### 2. Alert history query

The system must support querying alert history for an account.

Suggested external API:

- GET `/accounts/{accountId}/alerts`

The response returns alert entries in creation order.

## Alert Data

Each alert entry should contain at least:

- accountId
- alertType
- requestId when the alert is tied to a client operation
- message

Example alert type:

- INSUFFICIENT_FUNDS_ATTEMPT

## Core Rules

- failed withdrawals remain non-sticky for idempotency, as in the baseline behavior
- recording an alert must not change account balance state
- alert history must be queryable independently of transaction history
- alert records are append-only once created

## Non-goals

- no SMS or email provider integration
- no retry scheduling
- no user preference management
- no alert dismissal or deletion
- no configurable thresholds
- no alerts on successful operations
