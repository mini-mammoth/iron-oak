# docs/

Several kinds of content live here. They answer different questions, and consulting the
wrong one wastes your time.

## Concept (`docs/concept/`)

**Answers the question: _why does the mod work this way?_**

Settled product decisions: the player fantasy, the gameplay loop, the design principles that
justify saying no to a feature, and every tunable number with the file that decides it.
Do not implement something that contradicts the concept without a product discussion —
raising the question is right, shipping the contradiction quietly is not.

| Document | Contents |
|----------|----------|
| [concept/README.md](concept/README.md) | Pitch, player fantasy, the loop, six design principles, progression, the metal/wood scope, open questions |
| [concept/balance.md](concept/balance.md) | Every tunable number — recipe outputs, cook times, yields, damage — with its source location |
| [concept/roadmap.md](concept/roadmap.md) | A holding pen for parked ideas. Unrated and not committed |

## Requirements (`docs/requirements/`)

**Answers the question: _what must the mod do, and does it do it today?_**

Per-domain, ID-addressed behaviour (`INF`, `TRE`, `BRN`, `WSH`, `REF`, `MAT`) with acceptance
criteria that name the gate proving them. Start at
[requirements/README.md](requirements/README.md) — it holds the **status matrix**, which is
the single source of truth for what is implemented, partial, broken, planned or wontfix.

The status matrix carries weight a test report cannot. `./gradlew build` and `./gradlew
runGametest` prove the invariants they cover and nothing past them, and most acceptance
criteria here name `runClient` — rendering, sound and feel have no automated gate. Those
criteria are the written regression net, and a human at the gate is what ticks them.

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

Both testing documents are `status: active`: #40 wired up layers 1 and 2, so `./gradlew build`
runs the loader-JUnit layer and `./gradlew runGametest` runs the server layer. Layer 3 —
client gametests — is the one part still ahead of the tree, and `testing.md` says so where it
appears.

## Operations (`docs/ops/`)

**Answers the question: _how is the work itself run?_**

Runbooks for the process around the code: how work is dispatched to agents, how the board is
kept in sync, how tickets are classified. Written for the orchestrator/supervisor, not for
workers — a worker reads [`AGENTS.md`](../AGENTS.md), which is deliberately short.

| Document | Contents |
|----------|----------|
| [orchestration.md](ops/orchestration.md) | Dispatch policy, Orca CLI protocol, gates, commit verification, the dispatch failure modes |
| [orca-progress-loop.md](ops/orca-progress-loop.md) | Scheduled progress supervisor: locks, collecting workers, harvesting, invariants |
| [issue-labels.md](ops/issue-labels.md) | Issue label taxonomy — a closed set; new labels need human approval |
| [version-migration.md](ops/version-migration.md) | The staged 1.20.4 → 1.21.11 → 26.2 migration and its gates |
| [release.md](ops/release.md) | Cutting a release and publishing to Modrinth and CurseForge |

Design documents (`docs/design/`) do not exist yet and are only created for work that changes
player-visible mechanics or adds a subsystem — see the conditional design phase in
[orchestration.md](ops/orchestration.md). A design document sits *below* a requirement: the
requirement says what must happen, the design says how it will be built.

## Images (`docs/*.png`, `docs/*.gif`)

Screenshots and GIFs referenced from [`README.md`](../README.md) — the crafting recipes, the
fire bowl, the processing steps. Player-facing documentation. If you change a recipe or a
texture, the matching image is now wrong; either update it or say so in the PR.

## Rule of thumb

> Writing code? Read [`AGENTS.md`](../AGENTS.md).
> Deciding **what** the behaviour should be? `docs/requirements/`.
> Wondering **why** it is that way, or what a number is? `docs/concept/`.
> Wondering **why the code is shaped that way**, or where a test belongs? `docs/strategy/`.
> Dispatching work to agents? `docs/ops/orchestration.md`.
> Bumping a Minecraft version? Read `docs/ops/version-migration.md` first — it is staged
> for a reason.
