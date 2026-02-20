# Safety Rules

These rules are mandatory for human and agent sessions in this repository.

## Destructive Command Guardrails

1. Never run recursive deletes from repository root or parent paths.
2. Never delete `.git`, `.bear`, `src`, `doc`, `bin`, `gradle`, or project root files.
3. Never run blanket cleanup commands like `Remove-Item -Recurse -Force *`.
4. Before any cleanup/delete command, run `git status --short` and confirm the target path is in the allowed temp list.

## Allowed Temp Cleanup

Use `scripts/safe-clean-temp.ps1` only.

Allowed temp targets:
- `.gradle-user`
- `.bear-test-results`
- `build/generated/bear/.staging`
- `build/tmp/bear`

If a path is not in this list, do not delete it without explicit user approval.
