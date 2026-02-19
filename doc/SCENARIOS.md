# Scenario Prompts

This repo intentionally starts greenfield for multi-block BEAR validation.

## Scenario 1

Build account service with deposit, withdraw, transfer. Keep non-negative balance and idempotent request handling. Every operation must write an append-only audit record and emit an event.

## Scenario 2

(Use after Scenario 1 reaches a stable pass baseline.)

Add scheduled transfers. Scheduling must be durable (survive restart) and executed asynchronously. Enforce daily limits by tier at execution time and record audit for both scheduling and execution.
