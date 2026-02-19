# bear-account-demo

Standalone project used to validate BEAR agent workflow in realistic conditions.

## Included BEAR Assets

- BEAR CLI wrapper scripts (`bin/*`)
- BEAR agent bootstrap (`AGENTS.md`, `BEAR_AGENT.md`)
- BEAR primer (`doc/BEAR_PRIMER.md`)

## Canonical Commands

Windows:

```powershell
.\bin\bear-all.ps1
.\bin\pr-gate.ps1 <base-ref>
```

Bash:

```sh
./bin/bear-all.sh
./bin/pr-gate.sh <base-ref>
```

Gate behavior:
- if `bear.blocks.yaml` exists -> use repo-level `--all`
- otherwise -> fallback to deterministic `spec/*.bear.yaml` loop
