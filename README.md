# Wallet Service Demo

This repository demonstrates agent-autopilot implementation from a normal product spec.

Developer instruction to agent:
- Implement the specs.

Verification command:
```powershell
bear check --all --project .
```

CI demo:
- This repo uses the packaged BEAR CI wrapper under `.bear/ci/`.
- GitHub Actions runs BEAR governance in `observe` mode for PR visibility rather than blocking on ordinary governance signals.
- CI writes reviewer-facing artifacts to `build/bear/ci/bear-ci-report.json` and `build/bear/ci/bear-ci-summary.md`.
- In CI, the PR target branch defines the governance comparison base.
- Demo PR examples:
  - `baseline/greenfield-output -> main`
  - `scenario/02-feature-extension -> baseline/greenfield-output`
  - `scenario/03-boundary-expansion -> baseline/greenfield-output`

Product specifications:
- `spec/SPEC.md`
- `spec/SPEC.balance-alerts.md`

Branch context:
- Branch role: balance-alerts scenario
- Cut from: `baseline/greenfield-output`
- Use this governance base for `pr-check`: `origin/baseline/greenfield-output`
