# Bear Account Demo

This repository is the companion demo for [BEAR CLI](https://github.com/rore/bear-cli).

It is meant to show how BEAR looks in a real PR review flow, not only as local commands.

Start with the BEAR-side explanation here:
- [BEAR demo guide](https://github.com/rore/bear-cli/blob/main/docs/public/DEMO.md)

## What This Demo Contains

The demo is organized around showcase branches and PRs:

- `main`
  - spec-only starting point
  - target for the greenfield baseline review showcase
- `baseline/greenfield-output`
  - committed greenfield BEAR output baseline
- `scenario/02-feature-extension`
  - ordinary feature evolution on top of the governed baseline
- `scenario/03-boundary-expansion`
  - intentional governed-surface expansion on top of the baseline

Those branches are used to demonstrate three BEAR PR outcomes:

- greenfield baseline review -> `REVIEW REQUIRED`
- ordinary feature extension -> `PASS`
- intentional expansion on existing code -> `REVIEW REQUIRED`

## CI Demo

This repo uses the packaged BEAR CI wrapper under `.bear/ci/`.

GitHub Actions runs BEAR governance in `observe` mode so PRs stay visible for review without blocking on governance-review-only outcomes.

The workflow publishes:
- a GitHub Actions check
- a sticky PR comment with the BEAR decision
- uploaded artifacts:
  - `build/bear/ci/bear-ci-report.json`
  - `build/bear/ci/bear-ci-summary.md`

In CI, the PR target branch defines the governance comparison base.

## Developer Prompt

Developer instruction to agent:
- Implement the specs.

Primary product spec on `main`:
- `spec/SPEC.md`

## Branch Context

- Branch role: demo main/spec-only base
- Use this branch as the PR target for the greenfield baseline review showcase.
