# Bear Account Demo

This repository is the companion demo for [BEAR CLI](https://github.com/rore/bear-cli).

It is meant to show how BEAR looks in a real PR review flow, not only as local commands.

Start with the BEAR-side explanation here:
- [BEAR demo guide](https://github.com/rore/bear-cli/blob/main/docs/public/DEMO.md)

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

Showcase PR examples:
- `codex/showcase-greenfield-review -> main`
- `scenario/02-feature-extension -> baseline/greenfield-output`
- `scenario/03-boundary-expansion -> baseline/greenfield-output`

## Developer Prompt

Developer instruction to agent:
- Implement the specs.

## Product Specifications

- `spec/SPEC.md`

## Branch Context

- Branch role: greenfield showcase review branch
- Cut from: `main`
- Use this governance base for `pr-check`: `origin/main`
