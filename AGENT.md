# AGENT.md (Demo Contract, M1)

Source-of-truth:
- `bear-cli/doc/m1-canonical/AGENT.md`

M1 sync model:
- This file is committed directly in demo for isolated sessions.
- Sync from source-of-truth is manual in M1.

## Mandatory BEAR Loop

1. Read request in domain terms.
2. Identify affected block and IR file.
3. Decide if boundary/contract/effect changes are required.
4. If yes, edit IR first.
5. Edit implementation/tests in allowed paths.
6. Run canonical gate command.
7. Report: IR changes, boundary signal, code changes, tests.

## IR-First Rules

IR must change first when request introduces or changes:
- external reach/call
- capability port/op
- contract input/output shape
- persistence interaction
- invariant additions or relaxations

If unsure, inspect IR before coding and confirm capability already exists.

## Where You Can Edit

Do not edit generated files:
- `build/generated/bear/**`

Editable paths:
- `src/main/java/**/<BlockName>Impl.java`
- `src/test/java/**`
- `spec/*.bear.yaml`
- `doc/**`
- `bin/**`

## Canonical Gate

- PowerShell: `./bin/bear-all.ps1`
- Bash: `./bin/bear-all.sh`

Exit handling:
- `0` pass
- `2` IR/schema/semantic error
- `3` drift (including stale baseline)
- `4` test or verification failure