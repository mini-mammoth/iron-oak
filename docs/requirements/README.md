---
domain: Requirements Index
domain_code: REQ
status: active
last_updated: 2026-08-21
version: 2
related:
  - ../concept/README.md
  - ../concept/balance.md
  - ../ops/orchestration.md
  - ../../AGENTS.md
---

# Iron Oak — Requirements

**Answers the question: _what must the mod do, and does it do it today?_**

For *why* the mod works this way, read [`../concept/README.md`](../concept/README.md). For
the numbers, read [`../concept/balance.md`](../concept/balance.md). This index is the
**single source of truth for implementation status**.

---

## Domains

| Domain | File | Code | Covers |
|---|---|---|---|
| Infusion | [infusion.md](infusion.md) | `INF` | Ore-infused bone meal and turning a vanilla sapling into an infused one |
| Trees | [trees.md](trees.md) | `TRE` | Infused saplings, growth, tree features, logs and their drops |
| Burning | [burning.md](burning.md) | `BRN` | The fire bowl: lighting, cooking, afterburn, hoppers, hazards |
| Washing | [washing.md](washing.md) | `WSH` | Ash → shreds in water, and the automation that does not exist yet |
| Refining | [refining.md](refining.md) | `REF` | Shreds → raw ore and nuggets; closing the loop |
| Matrix | [matrix.md](matrix.md) | `MAT` | The 6 woods × 3 metals completeness rules |

## How to read a requirement

```
### BRN-04: Keep burning briefly after the last log     <- stable ID, never renumbered
**Status:** partial · **Issue:** #28                    <- authoritative status
WHEN … THEN the system SHALL …                          <- the behaviour
**Why:** …                                              <- the design intent
**Acceptance criteria** (verify: runClient)             <- how it is proven
- [ ] …
```

`verify:` names the gates that actually prove the requirement. **Two of them are automated
and traced back to this file; the rest are still walked by a human.**

| `verify:` | Means | Traced |
|---|---|---|
| `test` | A layer-1 test in `src/test` proves part of it — `./gradlew build` runs it | yes |
| `gametest` | A layer-2 server gametest in `src/gametest` proves part of it — `./gradlew runGametest` | yes |
| `build` | `./gradlew build` covers it (registration, compilation, the jar) | no |
| `runDatagen` | Proven by regenerating and diffing `src/main/generated/` | no |
| `runClient` | Must be checked in game — CI cannot see it | no |
| `inspect` | Read the data or asset file and compare | no |

Which layer a test belongs at, and what a test here looks like, is
[`../strategy/testing.md`](../strategy/testing.md). Whether the test comes first is
[`../strategy/test-driven-development.md`](../strategy/test-driven-development.md).

### `test` and `gametest` are a two-way contract

A test names the requirement it proves with `@Requirement`:

```java
@Requirement("MAT-02")
@Test
void everyFeatureKeyHasCommittedJson() { … }
```

`RequirementTracingTest` reads those citations back out of the source of both test source
sets and fails the build on either half of a broken link:

- a citation naming an id that does not exist here — a typo, or a requirement that was
  renumbered in defiance of the rule below;
- a requirement whose gate list says `test` or `gametest` while nothing at that layer cites
  it — the requirement claims automated coverage it does not have.

So a gate token in this file is a **claim**, and the claim is checked. Adding `test` to a
requirement without writing one fails `./gradlew build`, and so does deleting the last test
for a requirement that still claims it.

Two things it deliberately does not do. It does not require every test to cite a requirement
— `ImplementedInventoryTest` pins the defaults of an internal helper that no requirement
describes, and a false citation there would be worse than none. And it enforces **no
coverage percentage**: a requirement with no gate token is simply not claiming automated
coverage, which is the honest state for most of the 36 and is not a build failure. See
[`../strategy/testing.md`](../strategy/testing.md) on why there is no coverage number here.

A gate token says a test proves *part* of the requirement, not all of it. Most of these
requirements have criteria no automated gate can reach — the acceptance criteria stay the
specification, and `runClient` stays the gate for anything in-world.

## Status vocabulary

| Status | Meaning |
|---|---|
| `done` | Implemented and believed to work on `main` |
| `partial` | Implemented, but a stated part of it does not hold |
| `broken` | Specified and implemented, and it does not work — a bug, not a gap |
| `planned` | Intended, nothing built |
| `wontfix` | Deliberately not done; the requirement records the decision |

**Status is stated against `main`**, which tracks the newest supported Minecraft version —
read which one from `gradle.properties`, never from here. Version branches do not get their
own column: a requirement that only fails on a port keeps its `main` status and names the
port issue in a **port note**, as [BRN-10](burning.md#brn-10-show-what-is-inside) did for
#27 while 1.21.11 was still a branch.

Rules for keeping this honest:

1. The `**Status:**` line in the domain file and the row below are changed **in the same
   commit**. They are the same fact written twice; a mismatch is a bug in the docs.
2. A status only becomes `done` when its acceptance criteria have actually been checked at
   the gate they name. If you did not launch the game, do not write `done`.
3. Issues reference requirement IDs. IDs are permanent — a dropped requirement becomes
   `wontfix`, it is not deleted and its number is never reused.

**Provenance of this baseline.** The statuses below were derived by reading the
`1.2.1+1.20.4` source, its shipped data and the open issues — **not** from a fresh in-game
pass. That baseline is older than the tree: `main` moved to 1.21.11 the day before this
catalogue merged, and #47 corrected the passages that still described 1.20.4 code. Statuses
did not move with it, because a status only moves at the gate it names. That is also why
every acceptance criterion is still unticked: `done` here means "implemented
and believed to work", and the boxes get ticked by whoever actually checks them at the gate
they name. Treat an unticked criterion on a `done` requirement as unverified, not as proven.

---

## Status matrix

36 requirements: **28 done · 4 partial · 2 broken · 1 planned · 1 wontfix (provisional)**

| ID | Requirement | Status | Issue |
|---|---|---|---|
| [INF-01](infusion.md#inf-01-craft-ore-infused-bone-meal-from-raw-ore) | Craft ore-infused bone meal from raw ore | done | — |
| [INF-02](infusion.md#inf-02-applying-infused-bone-meal-converts-a-vanilla-sapling) | Applying infused bone meal converts a vanilla sapling | done | — |
| [INF-03](infusion.md#inf-03-infusion-always-succeeds) | Infusion always succeeds | done | CON-Q1 |
| [INF-04](infusion.md#inf-04-infusion-consumes-one-bone-meal-and-is-visibly-confirmed) | Infusion consumes one bone meal and is visibly confirmed | done | — |
| [INF-05](infusion.md#inf-05-infused-bone-meal-still-works-as-ordinary-bone-meal) | Infused bone meal still works as ordinary bone meal | done | INF-Q1 |
| [TRE-01](trees.md#tre-01-an-infused-sapling-behaves-like-its-vanilla-counterpart) | An infused sapling behaves like its vanilla counterpart | done | — |
| [TRE-02](trees.md#tre-02-growth-follows-vanilla-rules) | Growth follows vanilla rules | done | CON-Q5 |
| [TRE-03](trees.md#tre-03-a-grown-tree-is-made-of-infused-logs-of-its-own-metal-and-wood) | A grown tree is made of infused logs of its own metal and wood | done | — |
| [TRE-04](trees.md#tre-04-datagen-reproduces-the-shipped-tree-features) | Datagen reproduces the shipped tree features | **broken** | #30 |
| [TRE-05](trees.md#tre-05-infused-logs-are-vanilla-logs-for-every-other-purpose) | Infused logs are vanilla logs for every other purpose | done | — |
| [TRE-06](trees.md#tre-06-breaking-infused-blocks-drops-the-block-itself) | Breaking infused blocks drops the block itself | done | — |
| [TRE-07](trees.md#tre-07-infused-trees-never-generate-naturally) | Infused trees never generate naturally | done | — |
| [BRN-01](burning.md#brn-01-craft-the-fire-bowl) | Craft the fire bowl | done | — |
| [BRN-02](burning.md#brn-02-accept-exactly-one-burnable-log-at-a-time) | Accept exactly one burnable log at a time | done | — |
| [BRN-03](burning.md#brn-03-burn-one-log-into-one-ash-of-its-metal) | Burn one log into one ash of its metal | **broken** | #28 |
| [BRN-04](burning.md#brn-04-keep-burning-briefly-after-the-last-log) | Keep burning briefly after the last log | partial | #28 |
| [BRN-05](burning.md#brn-05-hand-out-ash-and-lose-it-if-the-bowl-is-destroyed-while-lit) | Hand out ash, and lose it if the bowl is destroyed while lit | done | — |
| [BRN-06](burning.md#brn-06-automate-with-vanilla-hoppers) | Automate with vanilla hoppers | done | — |
| [BRN-07](burning.md#brn-07-light-the-bowl-like-a-campfire) | Light the bowl like a campfire | partial | #28 |
| [BRN-08](burning.md#brn-08-water-puts-it-out) | Water puts it out | done | — |
| [BRN-09](burning.md#brn-09-burn-whoever-stands-in-it) | Burn whoever stands in it | done | — |
| [BRN-10](burning.md#brn-10-show-what-is-inside) | Show what is inside | done | #27 (port note) |
| [WSH-01](washing.md#wsh-01-wash-ash-in-water-to-get-a-shred) | Wash ash in water to get a shred | done | — |
| [WSH-02](washing.md#wsh-02-washing-is-instant-and-hands-the-shred-over-as-an-item-entity) | Washing is instant and hands the shred over as an item entity | done | — |
| [WSH-03](washing.md#wsh-03-reject-everything-that-is-not-a-water-source-in-the-main-hand) | Reject everything that is not a water source in the main hand | done | — |
| [WSH-04](washing.md#wsh-04-automated-washing) | Automated washing | planned | CON-Q4 |
| [WSH-05](washing.md#wsh-05-washing-works-alongside-other-mods-water-interactions) | Washing works alongside other mods' water interactions | partial | #15 |
| [REF-01](refining.md#ref-01-nine-shreds-make-one-raw-ore) | Nine shreds make one raw ore | done | — |
| [REF-02](refining.md#ref-02-a-shred-smelts-into-a-nugget) | A shred smelts into a nugget | partial | CON-Q2 |
| [REF-03](refining.md#ref-03-copper-has-no-nugget-path) | Copper has no nugget path | wontfix* | CON-Q2 |
| [REF-04](refining.md#ref-04-the-loop-closes-at-raw-ore) | The loop closes at raw ore | done | — |
| [MAT-01](matrix.md#mat-01-every-combination-exists-in-every-arm) | Every combination exists in every arm | done | — |
| [MAT-02](matrix.md#mat-02-a-partial-matrix-is-not-shipped) | A partial matrix is not shipped | done | — |
| [MAT-03](matrix.md#mat-03-metal-scoped-log-tags-list-all-six-woods) | Metal-scoped log tags list all six woods | done | — |
| [MAT-04](matrix.md#mat-04-vanilla-wood-tags-list-all-three-metals) | Vanilla wood tags list all three metals | done | — |
| [MAT-05](matrix.md#mat-05-every-registered-object-has-an-english-name) | Every registered object has an English name | done | — |

`wontfix*` = provisional; recorded to make an omission explicit, not because anyone decided
it. See [REF-03](refining.md#ref-03-copper-has-no-nugget-path).

### What is not working, in one place

| Issue | Requirement | Symptom |
|---|---|---|
| #28 | [BRN-03](burning.md#brn-03-burn-one-log-into-one-ash-of-its-metal), [BRN-04](burning.md#brn-04-keep-burning-briefly-after-the-last-log), [BRN-07](burning.md#brn-07-light-the-bowl-like-a-campfire) | Three different cook durations for the same log; afterburn timer restarts on chunk load; a flaming arrow can never light the bowl |
| #30 | [TRE-04](trees.md#tre-04-datagen-reproduces-the-shipped-tree-features) | The next `runDatagen` would rotate the jungle, spruce and dark oak tree features |
| #15 | [WSH-05](washing.md#wsh-05-washing-works-alongside-other-mods-water-interactions) | Player report of ash not washing; unreproduced on `main` |
| #27 | [BRN-10](burning.md#brn-10-show-what-is-inside) | 1.21.11 only: input not rendered, a loaded bowl cannot be lit |

## Open questions

Concept-level questions live in
[`../concept/README.md`](../concept/README.md#open-questions) as `CON-Q*`; domain-level ones
sit in their own file. Neither is a work item until someone answers it.

| ID | Question | Affects |
|---|---|---|
| CON-Q1 | Is deterministic infusion intended, or is the "chance" javadoc? | [INF-03](infusion.md#inf-03-infusion-always-succeeds) |
| CON-Q2 | Should copper get a nugget path? | [REF-02](refining.md#ref-02-a-shred-smelts-into-a-nugget), [REF-03](refining.md#ref-03-copper-has-no-nugget-path) |
| CON-Q3 | Which fire-bowl cook duration is the intended one? | [BRN-03](burning.md#brn-03-burn-one-log-into-one-ash-of-its-metal) |
| CON-Q4 | What shape should automated washing take? | [WSH-04](washing.md#wsh-04-automated-washing) |
| CON-Q5 | Should infused saplings grow more slowly than vanilla ones? | [TRE-02](trees.md#tre-02-growth-follows-vanilla-rules) |
| CON-Q6 | Is there a target ore-in/ore-out ratio for the chain? | [`../concept/balance.md`](../concept/balance.md) |
| INF-Q1 | Should infused bone meal refuse non-sapling targets? | [INF-05](infusion.md#inf-05-infused-bone-meal-still-works-as-ordinary-bone-meal) |
| TRE-Q1 | Should wood types differ in anything but appearance? | [trees.md](trees.md#open-questions) |
| BRN-Q1 | Should burning award the 0.2 XP its recipe declares? | [burning.md](burning.md#open-questions) |
| BRN-Q2 | Should a lit bowl really destroy its contents when broken? | [burning.md](burning.md#open-questions) |
| WSH-Q1 | Should a water cauldron be a valid washing target? | [washing.md](washing.md#open-questions) |
| REF-Q1 | Should shreds have a blasting recipe? | [refining.md](refining.md#open-questions) |
| ~~MAT-Q1~~ | ~~Should the matrix counts be enforced mechanically?~~ **Answered: yes (#40)** | [matrix.md](matrix.md#open-questions) |

## Working on a requirement

- Changing behaviour? Update the requirement and its status in the same PR as the code.
  A behaviour change with no requirement change means one of the two is wrong.
- Adding behaviour? Add an ID to the right domain file and a row here, `planned` before
  the code exists, then move it when the acceptance criteria pass.
- Player-visible mechanics or a new subsystem? A design document comes first — see the
  conditional design phase in [`../ops/orchestration.md`](../ops/orchestration.md).
- Writing code? [`AGENTS.md`](../../AGENTS.md) is still the file that tells you how.
