# docs/

Three kinds of content live here. They answer different questions, and consulting the
wrong one wastes your time.

## Images (`docs/*.png`, `docs/*.gif`)

Screenshots and GIFs referenced from [`README.md`](../README.md) — the crafting recipes,
the fire bowl, the processing steps. Player-facing documentation. If you change a recipe
or a texture, the matching image is now wrong; either update it or say so in the PR.

## Strategy (`docs/strategy/`)

**Answers the question: _why is the code written this way?_**

Settled technical decisions about the artefact: how registration works and why it is not
DeferredRegister, the client/server split, where `null` is tolerated, which of the three
testable layers a test belongs at, and when a test comes first. They exist so those
decisions are not re-argued in every ticket. Do not ship something that contradicts a
strategy document without a discussion — raising the question is right, shipping the
contradiction quietly is not.

| Document | Contents |
|----------|----------|
| [strategy/README.md](strategy/README.md) | What "strategy" is, how it differs from concept, requirements, ops and design, and the `status` convention |
| [strategy/testing.md](strategy/testing.md) | The three testable layers for a Fabric mod, which class in this mod belongs at each, tooling, anti-patterns |
| [strategy/test-driven-development.md](strategy/test-driven-development.md) | Where a test comes first, where it honestly does not, and why |
| [strategy/java.md](strategy/java.md) | Registration patterns, class-load order, the client/server sync rule, null handling, the 6×3 matrix |

Two documents there are `status: proposed`: the test harness they describe is **not wired
up yet** — no `src/test`, no test task, `./gradlew build` runs zero tests. #40 builds it.
Read them as the target, not the present.

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
> Wondering **why the code is shaped that way**, or where a test belongs? `docs/strategy/`.
> Dispatching work to agents? Read `docs/ops/orchestration.md`.
> Bumping a Minecraft version? Read `docs/ops/version-migration.md` first — it is staged
> for a reason.
