# WORKFLOW.md (Demo Runbook, M1)

Source-of-truth:
- `bear-cli/doc/m1-canonical/WORKFLOW.md`

M1 sync model:
- Committed directly for isolated sessions.
- Manual sync from source-of-truth.

## Read In This Order

1. `doc/BEAR_PRIMER.md`
2. `doc/spec/*`
3. the feature request

## Standard Flow

1. Read request.
2. Discover current BEAR structure from repo state:
- inspect `spec/*.bear.yaml` if present
- inspect generated package namespaces and existing `*Impl.java` files
3. Apply IR-first rule if boundary/contract/effect changes are needed.
4. Decide create-vs-update for blocks:
- update existing block when feature fits current contract/capability boundary
- create a new block when feature introduces a distinct contract/responsibility boundary
5. If no IR exists, create initial `spec/*.bear.yaml` first.
6. Implement in `*Impl.java` and tests only.
7. Run canonical gate:
- `./bin/bear-all.ps1` or `./bin/bear-all.sh`
8. Resolve failures by category until gate exits `0`.

## Failure Triage

1. `exit 2` (validation/schema/semantic):
- fix IR shape/references
- rerun gate

2. `exit 3` (drift):
- run compile for the IR file that triggered drift:
  - `./bin/bear.* compile <ir-file> --project .`
- ensure generated tree matches current IR
- rerun gate

3. boundary expansion lines present:
- confirm this is intended
- ensure IR change is explicit and reviewed
- continue with compile + implementation + gate

4. `exit 4` (tests/verification):
- fix impl/tests/verification issue
- rerun gate

## M1 Constraints

- No generated-file edits.
- No silent boundary expansion.
- One command determines done/not-done.

## M1 Manual Sync Checklist

When source texts change in `bear-cli/doc/m1-canonical/`:
1. Update demo copies (`doc/BEAR_PRIMER.md`, `BEAR_AGENT.md`, `WORKFLOW.md`).
2. Keep `Source-of-truth` lines accurate.
3. Keep domain docs (`doc/spec/*`) owned in demo and synced with current behavior.
4. Confirm canonical gate still matches docs.
