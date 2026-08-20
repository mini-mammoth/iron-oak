---
domain: Strategy
domain_code: TEST
status: proposed
last_updated: 2026-08-20
related:
  - ../../AGENTS.md
  - README.md
  - test-driven-development.md
  - java.md
  - ../ops/version-migration.md
---

# Testing Strategy

> **The harness described here does not exist yet.**
>
> Today there is **no `src/test`**, no test configuration in `build.gradle`, and
> `./gradlew build` runs **zero tests**. Wiring it up — the `fabric-loader-junit`
> dependency, a gametest run configuration, CI reporting — is **#40**, deliberately a
> separate `area:build` ticket.
>
> This document is the target shape, written down first so that #40 builds the right
> thing and so that the first tests land in the right place. Everything below marked
> *intended* is not runnable yet. Nothing here licenses you to claim a test passed.

This document is the **how**: which layer a test belongs at, what runs it, and what a test
in this repo actually looks like. For the **when** — does the test come before the code —
see [test-driven-development.md](test-driven-development.md).

---

## What `./gradlew build` proves, and what it does not

It proves the mod **compiles and remaps**. That is all. `AGENTS.md` already says so; this
is the evidence.

Four bugs were found in a single day of verification. All four are fixed and merged now;
every one of them was invisible to a green build on both Linux and Windows, and a human
launching the game is what found each one:

| Bug | What was broken | CI | What would have caught it |
|---|---|---|---|
| [#26](https://github.com/mini-mammoth/iron-oak/issues/26) | Every item and block rendered as the missing-texture cube; the creative tab was gone | green | a registry↔assets consistency test — **layer 1**, and the cheapest test in this repo |
| [#30](https://github.com/mini-mammoth/iron-oak/issues/30) | Spruce saplings grew dark oak, dark oak grew jungle, jungle grew spruce — all three metals, shipped for years | green | a matrix consistency test — **layer 1** |
| [#27](https://github.com/mini-mammoth/iron-oak/issues/27) | A log in the fire bowl was not rendered; a loaded bowl could not be lit | green | a gametest — **layer 2/3** |
| [#28](https://github.com/mini-mammoth/iron-oak/issues/28) | Burning arrow could never light the bowl; cook duration not persisted; a hopper-fed log burned for the wrong time | green | a gametest — **layer 2** |

Read the distribution, not the count: **two of the four were name-and-file bookkeeping
errors that a test with no world at all would have caught.** They cost a human launching
the game. That is where the first tests go.

---

## The three layers

| Layer | Runs | Tool | Cost | Sees |
|---|---|---|---|---|
| **1 — Loader unit tests** | plain JUnit in the JVM, loader booted, no world | `net.fabricmc:fabric-loader-junit` | milliseconds | classes, registries, ids, maps, committed resource files |
| **2 — Server gametests** | a real headless server, a real world, a structure per test | `fabric-gametest-api-v1` | seconds | ticking, block entities, entities, hoppers, growth, save round-trips |
| **3 — Client gametests** | a real client attached to a real server | `fabric-client-gametest-api-v1` | tens of seconds | what the client *has* — resolved models, synced block-entity state |

**Write the test at the lowest layer that can fail for the real reason.** Pushing a test
up a layer buys nothing and costs wall-clock; pushing it *down* past the reason it can
fail produces a test that passes while the mod is broken.

The rule of thumb, in this repo's own terms:

> Is the assertion about a **name, an id, a map, a file or a number**? Layer 1.
> Is it about a **tick, an entity, or a block changing**? Layer 2.
> Is it about **what the client has**? Layer 3.

### Layer 1 — Loader unit tests

There is **no pure-Java layer in a Fabric mod**, and pretending otherwise is the first
trap. `ItemStack.EMPTY`, `Blocks.OAK_SAPLING` and `BuiltInRegistries.ITEM` all need a game that
has been bootstrapped. Layer 1 is not "no Minecraft" — it is **"no world"**.
`fabric-loader-junit` exists exactly for that: it boots the loader inside an ordinary
JUnit run, so Minecraft classes and registries are reachable without starting the game.
The precise bootstrap incantation is #40's to settle against the jar.

What belongs here, by name:

| Target | Why it is layer 1 |
|---|---|
| `FireBowlEntity.getSlotsForFace(Direction)` | A pure `switch` over `Direction`: `DOWN` → output only, `UP` → input only, the four sides → both. No level touched. This is the whole contract with hopper automation, which `README.md` documents as a supported way to play. |
| `FireBowlEntity.canTakeItemThroughFace` | Pure — `slot == OUTPUT_SLOT`. Note that its neighbour `canPlaceItemThroughFace` is **not**: it needs a `ServerLevel` to resolve a recipe, and tests for one with an `instanceof`. Same interface, different layer. |
| `FireBowlEntity.cookingTotalTime(Optional<RecipeHolder<BurningRecipe>>)` | A private static function from a matched recipe to a duration, falling back to `ModRecipes.DEFAULT_COOKING_TIME` when there is none. It became a function in #28, having been a cached field before — and as a function it is the easiest thing in the mod to test. |
| `ImplementedInventory` defaults | `isEmpty()` over a `NonNullList`; `removeItem(slot, count)` calling `setChanged()` only when the result is non-empty; `setItem` clamping to `getMaxStackSize()` **after** it has already stored the stack, which means it mutates the caller's instance. Construct it with `ImplementedInventory.ofSize(2)` — no block entity, no level. Worth pairing with the observation that `FireBowlEntity` overrides four of these defaults purely to add the client sync. |
| `OreInfusedBoneMeal`'s infusion map | The constructor takes a `Map<Block, Block>`; `ModItems` hand-builds three of them with `Map.of(...)`. Assert each map has exactly six entries, keyed by the six vanilla saplings, valued by the **same metal's** sapling for the **same wood**. This is the #30 failure class on the item side of the matrix, and it is six lines of test. |
| `BurningRecipe` / `WashingRecipe` cook time | Both are thin subclasses of `AbstractCookingRecipe`; all the behaviour is in the serializer and the JSON. `ModRecipes` registers `new AbstractCookingRecipe.Serializer<>(BurningRecipe::new, DEFAULT_COOKING_TIME)`, all three burning recipes omit `cookingtime` in their JSON and therefore take that default, and `FireBowlEntity` reads the duration back out with `holder.value().cookingTime()`. Run the serializer's codec over the committed `data/iron_oak/recipe/*.json` and assert the input item and the cook time. No level, no fire. |
| `FireBowlEntity` save/load round-trip | `saveAdditional` into a fresh `ValueOutput`, `loadAdditional` on a second entity, compare. This is the regression guard for [#28](https://github.com/mini-mammoth/iron-oak/issues/28) defect 2, where `unlitTime` was never written and the idle countdown restarted on every world load; it is persisted as `unlit_time` now. A block entity is constructible without a level (`new FireBowlEntity(pos, state)`), so the round-trip needs no server — but note that a `ValueInput`/`ValueOutput` pair is not a bare `CompoundTag` any more, so #40 has to settle how one is built in a test. |
| The 6×3 matrix | The worked example below. |
| Registry ↔ assets consistency | For every id in `BuiltInRegistries.ITEM`/`BLOCK` with namespace `iron_oak`, assert the asset files that id requires exist — the `items/` definition, the model it points at, the blockstate. Nothing but the registry list and the filesystem. This is the test that would have caught #26, where 15 magenta cubes in a creative search were precisely the mod's 15 `iron_*` entries. Since the fix, `ModModelGenerator` emits the `items/` layer by walking the registry, so this test also guards the thing that walk assumes: that every mod item has a matching `iron_oak:item/<id>` model. |

### Layer 2 — Server gametests

`fabric-gametest-api-v1` (4.0.21) is **already on the classpath** via `fabric-api` — it
shows up in the `runClient` mod list. Its report file is JUnit XML, so gametests are
CI-reportable once #40 gives them a run configuration.

A gametest places a structure, ticks the world, and asserts on real block and entity
state. That is the only honest way to test the things `AGENTS.md` already warns CI does
not cover:

| Target | Why it cannot be layer 1 |
|---|---|
| `FireBowlEntity.litServerTick` | The cook completes on a tick counter, moves the result into the output slot, stacks or scatters it, then hands over to the idle timer. There is no way to observe that without ticking a world. |
| `FireBowlEntity.unlitServerTick` | The cook-time decay (`clamp(cookingTime - 2, …)`) and the 100-tick idle extinguish. |
| Hopper insertion | #28 defect 3: the player path used to be handed the duration while the hopper path went through `setItem` and was never given one, so the same log burned for different lengths of time depending on who inserted it. Both paths now funnel through `setItem` and the duration is derived, but the bug only ever existed at the seam between two real inserters — so the regression test needs both. |
| Burning arrow lights the bowl | #28 defect 1: the guard in `onProjectileHit` used to require `LIT` to already be `true` before setting `LIT` to `true`, so the body was unreachable in every state where it would have done anything. It now matches `CampfireBlock.onProjectileHit` — unlit and not waterlogged. Needs a projectile in a level. |
| Infused sapling growth | The in-world proof for #30 — plant `iron_spruce_sapling`, force growth, assert the logs placed are `iron_oak:iron_spruce_log`. The layer-1 matrix test asserts the wiring; this asserts the outcome. |
| `OreInfusedAsh.use` | Raycast to water, match a `WashingRecipe`, spawn the shred as an `ItemEntity` with a pickup delay. Three world interactions in one method. |
| `FireBowlBlock.playerWillDestroy` | Breaking an unlit bowl drops its contents; breaking a lit one destroys them. That second one is *intended* — write it down as a test so nobody "fixes" it by accident. The drop has to happen here rather than in `affectNeighborsAfterRemoval`, which runs after the block entity is already gone; a test is the only thing that would notice if those two were swapped back. |
| `canPlaceItemThroughFace` | Needs a recipe lookup, which needs a `ServerLevel`. |

### Layer 3 — Client gametests

`fabric-client-gametest-api-v1` (6.0.0) is likewise already present. It is the most
expensive layer and the smallest: use it only for things that are true on the client and
false on the server.

**A client gametest does not check pixels.** What it checks is that the client *has* what
the renderer reads. That distinction is the whole of #27 defect 1: the server's `setInput`
wrote the slot directly instead of going through the one method that pushes an update to
clients, so the client's copy of the block entity kept an empty input slot and the renderer
correctly drew nothing. The assertion is *"after the server-side insert, the client's block
entity has the item"*, and only a real client can make it.

Since 1.21.9 that assertion has a name to aim at. Rendering is split in two:
`FireBowlRenderer.extractRenderState` copies what it needs off the block entity into a
`FireBowlRenderState`, and `submit` draws purely from that snapshot. So the testable claim
is `state.hasInput` — an explicit boolean on a plain object — rather than anything about
geometry. Everything after `extractRenderState` is matrix maths and stays untested on
purpose.

The other resident of this layer is the #26 class of failure: whether an item's model
**resolved**, as opposed to whether its file exists. File existence is layer 1 and much
cheaper — do that first, and reach for layer 3 only for the resolution itself.

Interaction semantics (#27 defect 2: flint and steel on a loaded bowl must fall through to
the flint and steel) sit on the boundary. Whether the server gametest harness can drive a
player use-interaction, or whether it needs a client, is a question for #40 to resolve
against the actual API — not to guess.

### Not a layer: the `runClient` checklist

Until #40 lands, **a human launching `./gradlew runClient` is the only gate that covers
in-world behaviour**, and `AGENTS.md` is explicit that you must say so in the PR rather
than imply you did it. That does not change when the harness arrives. It only shrinks:
every gametest that lands is one line the human no longer has to walk.

---

## The worked example: the #30 matrix test

This is what a test in this repo actually looks like, and it is not hypothetical. The
worker who fixed #30 **wrote it as a throwaway script**, ran it, and deleted it. The fix
landed in #37, so what follows is history — but the invariant it checked is permanent, and
nothing checks it today.

The chain it walks, for each of the 18 sapling arms:

```
ModBlocks.<METAL>_<WOOD>_SAPLING            registered as "<metal>_<wood>_sapling"
  → ModSaplingGenerators.<METAL>_<WOOD>     built from a feature ResourceKey
    → ModConfiguredFeatures.<METAL>_<WOOD>_TREE   whose key string is registerKey("…")
      → src/main/generated/…/<that key>.json      the feature that actually ships
```

The assertion at every hop: **the wood and the metal agree**. Against the broken tree the
script reported **54 inconsistencies**. Against the fix, **18 of 18 clean**.

What made the bug survive for years is the thing the test has to be designed around:
**the code was self-consistent, and only the names lied.**

```java
// ModConfiguredFeatures — the constant name does not match the key it holds
IRON_SPRUCE_TREE = registerKey("iron_dark_oak_tree");

// ModSaplingGenerators — takes the constant, so it inherits the lie
IRON_SPRUCE = generator("iron_dark_oak", ModConfiguredFeatures.IRON_SPRUCE_TREE);

// ModBlocks — trusts the constant name
IRON_SPRUCE_SAPLING = sapling("iron_spruce_sapling", ModSaplingGenerators.IRON_SPRUCE, …);
```

The generated JSON was correct. `runDatagen` produced no diff. Every arm compiled. The
three-cycle — spruce grows dark oak, dark oak grows jungle, jungle grows spruce — existed
only in the identifiers, and `ModBlocks` consumes them by name.

Three consequences for how the test must be written, and they generalise to every
consistency test in this repo:

1. **Derive the expected set from the strings, never from the constants.** Cross-product
   `{iron, copper, gold} × {oak, acacia, birch, jungle, spruce, dark_oak}` in the test, and
   look each arm up. A test that builds its expectations by iterating
   `ModConfiguredFeatures.*` is a tautology: it re-states the bug and passes.
2. **Assert on the registered id, not the field.** The id is the contract with saved
   worlds and data packs. The field name is a comment that the compiler does not check —
   see [java.md](java.md).
3. **The test must fail against the broken tree.** Those 54 findings *are* the red step.
   Because the generated JSON was already right, a test written against the JSON alone
   would have passed against the bug. A test you have never seen fail for the real reason
   is not evidence — and now that #37 has landed, a test written today can only be checked
   against the fixed tree, which is exactly the weaker position this document is trying to
   avoid being in next time.

And the process lesson, which is why this document exists at all:

> **If you wrote a script to verify a fix, you wrote a test. Check it in.**

---

## Tooling — the verified facts

Checked against the actual artefacts on 2026-08-20. Those versions — Minecraft 1.21.11,
loader 0.19.3, Fabric API 0.141.6 — are what `gradle.properties` declares on this line, so
the facts below apply to the tree as it stands. Do not re-derive them; do re-check them at
the next version bump, and treat that re-check as part of the migration gate in
[`docs/ops/version-migration.md`](../ops/version-migration.md).

| Artefact | Version | State |
|---|---|---|
| `net.fabricmc:fabric-loader-junit` | 0.19.3 | Exists on `maven.fabricmc.net`, matches this mod's loader version exactly. Boots the loader inside an ordinary JUnit run. **Not yet a dependency — #40.** |
| `fabric-gametest-api-v1` | 4.0.21 | Already on the classpath via `fabric-api`; appears in the `runClient` mod list. Ships `net/fabricmc/fabric/api/gametest/v1/GameTest.class`. |
| `fabric-client-gametest-api-v1` | 6.0.0 | Likewise present. |

The gametest API reads these system properties, which is how #40 will drive it from Gradle
and CI:

| Property | Purpose |
|---|---|
| `fabric-api.gametest` | run gametests |
| `fabric-api.gametest.filter` | run a subset |
| `fabric-api.gametest.report-file` | write the report — **JUnit XML**, so CI can report it |
| `fabric-api.gametest.verify` | verification mode |

Two constraints inherited from `AGENTS.md` that apply to the harness as much as to the mod:

- **JDK 21 for every Gradle invocation.** A test task is not exempt.
- **Do not move the Gradle wrapper to add a test task.** It is on 9.5.1 because Loom 1.17
  requires Gradle 9.x; the old 8.11.1 pin belonged to the 1.20.4 line. If a harness seems
  to need a different Gradle, that is a finding for #40, not a side effect.
- **`runDatagen` inherits `client`**, because Fabric routes model providers through a
  client-only mixin. A test task that needs the datagen classpath inherits the same
  constraint.

---

## Where tests will live (intended, #40 decides)

```
src/test/java/com/minimammoth/ironoak/…      layer 1, loader-junit
src/gametest/java/com/minimammoth/ironoak/…  layers 2 and 3, plus their structure NBT
```

Two directories rather than one, because the two kinds have nothing in common: one is
milliseconds and runs on every build, the other needs a game and a run configuration. Not
co-located with `src/main` — Loom's source sets and the remapper make co-location a fight
with no prize. The exact source-set wiring, the entrypoint registration and the CI job are
#40's call; if it finds a better shape, it changes this section in the same PR.

Naming: mirror the class under test (`FireBowlEntityTest`, `ModConfiguredFeaturesTest`).
When a test exists because of a bug, name the issue in the test's own comment — a test
whose reason is `// #28: cookingTotalTime is not persisted` survives a refactor that would
otherwise delete it as pointless.

---

## Anti-patterns

These are the mistakes **this** codebase is set up to make. They are not a textbook list.

| Anti-pattern | Why it fails here | Instead |
|---|---|---|
| Building a matrix test's expectations by iterating `ModConfiguredFeatures.*` or `ModBlocks.*` | The field names were the bug (#30). The test restates it and passes. | Cross-product the metal and wood **strings**, then look each arm up. |
| Asserting that `src/main/generated/*.json` is correct | It is output, not authority. `AGENTS.md`: regenerate, never hand-edit. | Assert (a) the source Java agrees with the id it registers, and (b) `runDatagen` produces no diff. |
| Mocking `Level` or `ServerLevel` to unit-test ticking | Half of `FireBowlEntity`'s behaviour is the interaction with a real level, and `litServerTick` casts to `ServerLevel` on the first line. A mock asserts your idea of the level. | A layer-2 gametest. |
| Reaching into `FireBowlEntity`'s private state by reflection | The field is not the behaviour, and #28 deleted the most tempting one — `cookingTotalTime` is a function now, not stored state. | Assert the observable: how long the burn takes, and that progress survives a reload. |
| Copying a test from the Fabric wiki or a tutorial | Same failure mode `AGENTS.md` already warns about for API calls, and #26/#27 both hit it. The gametest API's shape changed across this version range. | Resolve it against the jar — the `minecraft-fabric-lookup` skill. |
| Hand-listing the asset paths in an assets test | The 6×3 matrix grows. A hand-written list silently stops covering the newest arm — which is the exact shape of a half-filled matrix. | Derive the list from `BuiltInRegistries.ITEM` / `BLOCK`, filtered to the `iron_oak` namespace. `ModItems.onInitialize` and `ModModelGenerator.generateItemModels` both already do exactly this; copy that, not a list. |
| A test that only exercises `iron_*` | #30 was wrong for **all three metals** and three of six woods. One metal proves one arm. | Loop the full 6×3. The matrix is all-or-nothing (`AGENTS.md`), and so is its test. |
| Asserting on a display string from `lang/en_us.json` | It is localisable, and `de_de` exists. | Assert the translation **key** resolves; leave the text to the translator. |
| A gametest that sleeps, or asserts after a fixed tick count | Flaky by construction, and a flaky gametest gets disabled and then deleted. | Drive ticks explicitly and assert on state, not on elapsed time. |
| A throwaway verification script | #30, verbatim. | Check it in. |
| Adding a test that needs a newer Loom, Gradle or Minecraft | `AGENTS.md`: a version bump is never a side effect. | Report it. It belongs in an `area:build` ticket. |

---

## What this strategy deliberately does not do

- **No coverage threshold, and no coverage number in CI.** The repo starts at zero, the
  matrix inflates line counts for free, and a percentage nobody enforces is worse than
  silence. The gate stays what `AGENTS.md` already says it is: `gh pr checks` green, plus
  `runClient` for anything in-world.
- **No mocking framework.** Nothing in this mod is worth a mock. Everything is either a
  value (layer 1) or a world (layer 2).
- **No snapshot tests of generated JSON.** Idempotence of `runDatagen` is the real
  assertion, and `git status --short` already makes it.
- **No blanket "tests before implementation" rule.** See
  [test-driven-development.md](test-driven-development.md) — there are places in this repo
  where that rule cannot honestly be followed, and a rule nobody follows devalues the ones
  that matter.

---

## 📅 Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial version (#39). Three layers defined for a Fabric mod and mapped onto named classes in this repo; the four bugs of 2026-08-20 (#26, #27, #28, #30) used as the evidence; the #30 matrix check written up as the worked example. Records that the harness is not wired up (#40) and the verified artefact facts as of loader 0.19.3. |
| 2026-08-20 | 1.1 | Rebased onto 1.21.11 (#39). Every named class and method re-checked against the merged tree: Mojang mappings throughout, `getSlotsForFace`/`canTakeItemThroughFace`/`saveAdditional`, `cookingTotalTime` now a derived function and a better layer-1 target, the renderer's `extractRenderState`/`submit` split giving layer 3 an explicit `hasInput` to assert. All four bugs restated as fixed. Gradle wrapper fact corrected to 9.5.1 / Loom 1.17. |

*Last updated: 2026-08-20*
