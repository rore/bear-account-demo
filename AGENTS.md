# AGENTS.md (Codex Auto-Load Bootstrap)

Profile: `BEAR_AGENT`

If you are an agent in this repository, operate as `BEAR_AGENT` for the full session.

Startup (mandatory):
1. Read `BEAR_AGENT.md`.
2. Read `WORKFLOW.md`.
3. Read `doc/BEAR_PRIMER.md`.
4. Read `doc/spec/*`.

Operating rules:
- Apply IR-first decisions exactly as defined in `BEAR_AGENT.md`.
- Never edit generated files under `build/generated/bear/**`.
- Use one gate command as done signal: `./bin/bear-all.ps1` (or `./bin/bear-all.sh`).
- Treat domain docs (`doc/spec/*`) as product context, not BEAR governance.

If instructions conflict, precedence is:
1. `AGENTS.md`
2. `BEAR_AGENT.md`
3. `WORKFLOW.md`
4. `doc/BEAR_PRIMER.md`
5. feature request

