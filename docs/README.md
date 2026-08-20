# docs/

Two kinds of content live here, and they are unrelated to each other.

## Images (`docs/*.png`, `docs/*.gif`)

Screenshots and GIFs referenced from [`README.md`](../README.md) — the crafting recipes,
the fire bowl, the processing steps. Player-facing documentation. If you change a recipe
or a texture, the matching image is now wrong; either update it or say so in the PR.

## Operations (`docs/ops/`)

**Answers the question: _how is the work itself run?_**

Runbooks for the process around the code: how work is dispatched to agents, how the board
is kept in sync, how tickets are classified. Written for the orchestrator/supervisor, not
for workers — a worker reads [`AGENTS.md`](../AGENTS.md), which is deliberately short.

| Document | Contents |
|----------|----------|
| [orchestration.md](ops/orchestration.md) | Dispatch policy, Orca CLI protocol, gates, commit verification, the dispatch failure modes |
| [orca-progress-loop.md](ops/orca-progress-loop.md) | Scheduled progress supervisor: locks, collecting workers, harvesting, invariants |
| [issue-labels.md](ops/issue-labels.md) | Issue label taxonomy — a closed set; new labels need human approval |
| [version-migration.md](ops/version-migration.md) | The staged 1.20.4 → 1.21.11 → 26.2 migration and its gates |

Design documents (`docs/design/`) do not exist yet and are only created for work that
changes player-visible mechanics — see the conditional design phase in
[orchestration.md](ops/orchestration.md).

## Rule of thumb

> Writing code? Read [`AGENTS.md`](../AGENTS.md).
> Dispatching work to agents? Read `docs/ops/orchestration.md`.
> Bumping a Minecraft version? Read `docs/ops/version-migration.md` first — it is staged
> for a reason.
