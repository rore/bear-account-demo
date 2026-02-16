# WORKFLOW.md (Demo Runbook, M1)

Source-of-truth:
- `bear-cli/doc/m1-canonical/WORKFLOW.md`

M1 sync model:
- Committed directly for isolated sessions.
- Manual sync from source-of-truth.

## Standard Flow

1. Read feature request.
2. Discover BEAR structure from repo state:
- inspect `spec/*.bear.yaml` if present
- inspect generated package namespaces and existing `*Impl.java` files
3. Apply IR-first decision rules from `AGENT.md`.
4. Decide create-vs-update block:
- update existing block when feature fits existing contract/capability boundary
- create new block when feature introduces a distinct contract/responsibility boundary
5. Implement in user-owned files only.
6. Run canonical gate.
7. Iterate until gate exits `0`.

## Canonical Command

- PowerShell: `./bin/bear-all.ps1`
- Bash: `./bin/bear-all.sh`

## Failure Modes

1. Validation/schema (`exit 2`)
- Fix IR fields/references and rerun.

2. Drift (`exit 3`)
- Regenerate baseline:
  - `./bin/bear.* compile spec/withdraw.bear.yaml --project .`
- Rerun gate.

3. Boundary expansion signal lines
- Confirm expansion is intentional.
- Keep IR update explicit and reviewable.
- Continue with compile + implementation.

4. Tests/verification (`exit 4`)
- Fix behavior or undeclared-reach violations.
- Rerun gate.

## M1 Manual Sync Checklist

When source texts change in `bear-cli/doc/m1-canonical/`:
1. Update demo copies (`AGENT.md`, `WORKFLOW.md`) only.
2. Keep `Source-of-truth` lines accurate.
3. Keep domain docs (`doc/spec/*`) owned in demo and synced with current behavior.
4. Confirm canonical gate still matches docs.