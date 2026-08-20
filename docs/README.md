# docs/

Four kinds of content live here. They answer different questions, and consulting the wrong
one wastes your time.

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

This matters more here than in a repo with tests: there is no test suite, so `./gradlew
build` proves compilation and nothing else. The acceptance criteria are the written
regression net, and `runClient` is the gate for anything that happens in the world.

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

## Design (`docs/design/`)

**Answers the question: _how will this be built, and what did we decide along the way?_**

Technical blueprints for work that changes player-visible mechanics or adds a subsystem, each
recording the options that were rejected and why. Required by the conditional design phase in
[orchestration.md](ops/orchestration.md), which ends in a human gate. For *new* work the design
comes first and the requirements are minted from the approved design; for behaviour that already
exists, the requirement stands alone and no design is owed.

Start at [design/README.md](design/README.md); copy [design/TEMPLATE.md](design/TEMPLATE.md).
All current documents are drafts with open questions — nothing in them is approved.

## Images (`docs/*.png`, `docs/*.gif`)

Screenshots and GIFs referenced from [`README.md`](../README.md) — the crafting recipes, the
fire bowl, the processing steps. Player-facing documentation. If you change a recipe or a
texture, the matching image is now wrong; either update it or say so in the PR.

## Rule of thumb

> Writing code? Read [`AGENTS.md`](../AGENTS.md).
> Deciding **what** the behaviour should be? `docs/requirements/`.
> Deciding **how** to build something new? `docs/design/` — and get the gate first.
> Wondering **why** it is that way, or what a number is? `docs/concept/`.
> Dispatching work to agents? `docs/ops/orchestration.md`.
> Bumping a Minecraft version? Read `docs/ops/version-migration.md` first — it is staged
> for a reason.
