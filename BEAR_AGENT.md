# BEAR_AGENT.md (Demo Contract, M1)

Source-of-truth:
- `bear-cli/doc/m1-canonical/BEAR_AGENT.md`

M1 sync model:
- This file is committed directly in demo for isolated sessions.
- Sync from source-of-truth is manual in M1.

Session profile:
- BEAR_AGENT (declared in AGENTS.md for auto-loaded sessions).

## Read In This Order

1. `doc/BEAR_PRIMER.md`
2. the feature request

## Session Baseline Check

Before planning or editing:
1. Run `git status --short`.
2. If pre-existing changes exist, explicitly report them and confirm whether to treat them as baseline before proceeding.

## Mandatory BEAR Loop

1. Read the feature request in domain terms.
2. Discover existing BEAR structure:
- inspect `spec/*.bear.yaml`
- inspect `bear.blocks.yaml` if present
- inspect generated package namespaces and existing `*Impl.java` files
3. Decide if boundary/contract/effect changes are required.
4. If required, update IR before implementation edits.
5. Decide create-vs-update block.
6. If no IR exists yet, create the first `spec/*.bear.yaml` before expecting gate success.
7. Run canonical gate command.
8. Fix failures by category (schema/validation, drift, boundary signal, tests).
9. Report exactly what changed:
- IR and boundary deltas
- implementation files
- tests and gate result

## Edit Boundaries

Do not edit generated files:
- `build/generated/bear/**`

Editable locations:
- implementation: `src/main/java/**/<BlockName>Impl.java`
- tests: `src/test/java/**`
- IR/docs/scripts in repo-owned paths

## Canonical Command

Use one command as the done gate:
- PowerShell: `./bin/bear-all.ps1`
- Bash: `./bin/bear-all.sh`
