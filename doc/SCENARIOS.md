# Scenario Prompts

This repo intentionally starts greenfield for multi-block BEAR validation.

## Scenario 1: Greenfield Multi-Block (Shared Root)

Prompt to give agent:

Build account service with deposit, withdraw, transfer. Keep non-negative balance and idempotent request handling. Every operation must write an append-only audit record and emit an event.

Expected outcome:
- agent creates `spec/*.bear.yaml`
- agent creates `bear.blocks.yaml`
- at least two blocks share `projectRoot: services/account`
- `bin/bear-all.*` exits `0`

## Scenario 2: Feature Extension (from greenfield pass)

Start from branch `scenario/1-greenfield-pass`.

Prompt to give agent:

Add scheduled transfers. Scheduling must be durable (survive restart) and executed asynchronously. Enforce daily limits by tier at execution time and record audit for both scheduling and execution.

Expected outcome:
- modifies at least one existing block
- adds at least one new block
- `bin/bear-all.*` exits `0`
- `bin/pr-gate.* origin/scenario/1-greenfield-pass` emits deterministic deltas
