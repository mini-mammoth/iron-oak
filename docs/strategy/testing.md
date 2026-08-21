---
domain: Strategy
domain_code: TEST
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - README.md
  - test-driven-development.md
  - java.md
  - ../ops/version-migration.md
---

# Testing Strategy

> **Layers 1 and 2 exist and run. Layer 3 does not.**
>
> #40 landed the harness this document was written to specify:
> `fabric-loader-junit` in `src/test`, run by `./gradlew build`; a gametest run
> configuration in `src/gametest`, run by `./gradlew runGametest`; both reporting JUnit
> XML on Linux and Windows in CI.
>
> **Layer 3 — client gametests — is still not wired up.** `fabric-client-gametest-api-v1`
> is on the classpath, but there is no run configuration and no client test. Nothing below
> about layer 3 is runnable, and the interaction-semantics question at the end of that
> section is still open.

This document is the **how**: which layer a test belongs at, what runs it, and what a test
in this repo actually looks like. For the **when** — does the test come before the code —
see [test-driven-development.md](test-driven-development.md). For **which requirement a test
proves**, and why that citation is checked in both directions, see *Citing the requirement a
test proves* below.

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
| **3 — Client gametests** *(not wired up)* | a real client attached to a real server | `fabric-client-gametest-api-v1` | tens of seconds | what the client *has* — resolved models, synced block-entity state |

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
It is a JUnit `LauncherSessionListener`, so adding the dependency is the whole
installation — it starts Knot before the first test class and needs no code.

What it does *not* do is bootstrap Minecraft or invoke the mod's entrypoint, because no
game was started. `BootstrappedGame` does both, once per JVM, and every test class carries
`@ExtendWith(BootstrappedGame.class)`. **Do not touch a Minecraft class from a static
initialiser in a test** — static initialisers run when JUnit loads the class, before any
extension has run, and touching `BuiltInRegistries` that early poisons its class
initialiser for every test that follows. Resolve registries in `@BeforeAll` or on demand.

Tags are the other thing no world hands you: nothing loads a data pack at layer 1, so every
`TagKey` is unbound and touching one throws. `TagBinding` binds the mod's three
`*_infused_logs` tags out of the committed JSON, which is also how the recipe tests prove
those files name items that exist.

What belongs here, by name:

| Target | Why it is layer 1 |
|---|---|
| `FireBowlEntity.getSlotsForFace(Direction)` | A pure `switch` over `Direction`: `DOWN` → output only, `UP` → input only, the four sides → both. No level touched. This is the whole contract with hopper automation, which `README.md` documents as a supported way to play. |
| `FireBowlEntity.canTakeItemThroughFace` | Pure — `slot == OUTPUT_SLOT`. Note that its neighbour `canPlaceItemThroughFace` is **not**: it needs a `ServerLevel` to resolve a recipe, and tests for one with an `instanceof`. Same interface, different layer. |
| `FireBowlEntity.cookingTotalTime(Optional<RecipeHolder<BurningRecipe>>)` | A private static function from a matched recipe to a duration, falling back to `ModRecipes.DEFAULT_COOKING_TIME` when there is none. It became a function in #28, having been a cached field before — and as a function it is the easiest thing in the mod to test. |
| `ImplementedInventory` defaults | `isEmpty()` over a `NonNullList`; `removeItem(slot, count)` calling `setChanged()` only when the result is non-empty; `setItem` clamping to `getMaxStackSize()` **after** it has already stored the stack, which means it mutates the caller's instance. Construct it with `ImplementedInventory.ofSize(2)` — no block entity, no level. Worth pairing with the observation that `FireBowlEntity` overrides four of these defaults purely to add the client sync. |
| `OreInfusedBoneMeal`'s infusion map | The constructor takes a `Map<Block, Block>`; `ModItems` hand-builds three of them with `Map.of(...)`. Assert each map has exactly six entries, keyed by the six vanilla saplings, valued by the **same metal's** sapling for the **same wood**. This is the #30 failure class on the item side of the matrix, and it is six lines of test. |
| `BurningRecipe` / `WashingRecipe` cook time | Both are thin subclasses of `AbstractCookingRecipe`; all the behaviour is in the serializer and the JSON. `ModRecipes` registers `new AbstractCookingRecipe.Serializer<>(BurningRecipe::new, DEFAULT_COOKING_TIME)`, all three burning recipes omit `cookingtime` in their JSON and therefore take that default, and `FireBowlEntity` reads the duration back out with `holder.value().cookingTime()`. Run the serializer's codec over the committed `data/iron_oak/recipe/*.json` and assert the input item and the cook time. No level, no fire. |
| `FireBowlEntity` save/load round-trip | `saveAdditional` into a fresh `ValueOutput`, `loadAdditional` on a second entity, compare. This is the regression guard for [#28](https://github.com/mini-mammoth/iron-oak/issues/28) defect 2, where `unlitTime` was never written and the idle countdown restarted on every world load; it is persisted as `unlit_time` now. A block entity is constructible without a level (`new FireBowlEntity(pos, state)`), so the round-trip needs no server. A `ValueInput`/`ValueOutput` pair is not a bare `CompoundTag` any more: build them with `TagValueOutput.createWithContext(reporter, registries)` and `TagValueInput.create(reporter, registries, tag)`, and pass a `ProblemReporter.Collector` rather than `ProblemReporter.DISCARDING` so a serialisation problem fails the test instead of vanishing. |
| The 6×3 matrix | The worked example below. |
| Registry ↔ assets consistency | For every id in `BuiltInRegistries.ITEM`/`BLOCK` with namespace `iron_oak`, assert the asset files that id requires exist — the `items/` definition, the model it points at, the blockstate. Nothing but the registry list and the filesystem. This is the test that would have caught #26, where 15 magenta cubes in a creative search were precisely the mod's 15 `iron_*` entries. Since the fix, `ModModelGenerator` emits the `items/` layer by walking the registry, so this test also guards the thing that walk assumes: that every mod item has a matching `iron_oak:item/<id>` model. |

### Layer 2 — Server gametests

`fabric-gametest-api-v1` (4.0.21) is **already on the classpath** via `fabric-api`. Its
report file is JUnit XML, so gametests are CI-reportable, and #40 gave them a run
configuration: `./gradlew runGametest`.

Three facts about its shape, resolved against the jar rather than remembered, because none
of them is guessable:

- **The run configuration is a plain `server()` with no main class of its own.** The API
  mixes into `net.minecraft.server.Main` and diverts into its own headless runner when
  `-Dfabric-api.gametest` is set, then exits with the result. A failing gametest is a
  non-zero exit, which is what lets it gate CI.
- **Tests are found through a `fabric-gametest` entrypoint**, and a test is a `public void`
  method taking a single `GameTestHelper`. Not static, not returning anything.
- **`@GameTest` defaults `structure` to `fabric-gametest-api-v1:empty`**, an 8×8×8 empty
  box the API ships. None of the gametests in this repo needs structure NBT of its own;
  they build what they need with `helper.setBlock`. That 8-block ceiling is worth
  remembering when a test grows a tree.

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
| `FireBowlBlock.playerWillDestroy` | **Both halves of this are now wrong, and no test was shipped for it — see the comment in `FireBowlGameTest`.** Since 1.21.11, `LevelChunk.setBlockState` calls `BlockEntity.preRemoveSideEffects`, whose default body drops the contents of any `Container` block entity. `FireBowlEntity` is one, so vanilla drops the contents on every removal path: `playerWillDestroy`'s own `spawnContainingItems()` is redundant, and a lit bowl no longer destroys what is inside it. Whether the lit-bowl exception should be restored — by overriding `preRemoveSideEffects` — is a gameplay decision with a requirement behind it, not a test-harness one. |
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
the flint and steel) sit on the boundary. `GameTestHelper` offers `useBlock(pos, player)`
and `makeMockPlayer(GameType)` (give the mock player the item via
`setItemInHand` before calling `useBlock`), so a server gametest can drive it — confirmed on
`v1.21.1` (#54), where the same defect shape reappeared through that version's
`ItemInteractionResult` and `FireBowlGameTest.flintAndSteelLightsALoadedBowl` (alongside the
empty-bowl half) both proves the fix and fails without it.

### Not a layer: the `runClient` checklist

**A human launching `./gradlew runClient` is still the only gate that covers rendering,
sound and feel**, and `AGENTS.md` is explicit that you must say so in the PR rather than
imply you did it. The harness did not remove that gate; it shrank it. Seven gametests have
retired seven of its lines. The rest of the list is still walked by hand.

---

## Citing the requirement a test proves

A test names the **issue** it came from in its javadoc, and that is still asked for below.
An issue says why the test was written; it is a moment, and prose. It cannot answer the one
question worth asking of [`docs/requirements/`](../requirements/README.md): *which
requirement has nothing proving it?*

So a test that proves a requirement also cites it:

```java
@Requirement("MAT-02")
@Test
void everyFeatureKeyHasCommittedJson() { … }
```

`@Requirement` is repeatable — a test that proves two requirements says so twice rather than
picking the closer one — and it works identically at both layers. Three facts about how it is
built, because none of them is guessable:

- It lives in a third source set, **`src/testsupport`**, which depends on nothing. Layer 1 is
  `src/test` and layer 2 is `src/gametest`; those two share no classpath, so an annotation
  both of them use can live in neither.
- It is **`RetentionPolicy.SOURCE`**. Nothing reads it reflectively, so it is erased at
  compile time and reaches no runtime classpath and no artefact. Check it the way
  `AGENTS.md` says to check the jar — by looking:
  `unzip -l build/libs/iron-oak-*.jar | grep -ic requirement` must be 0.
- The citations are therefore read out of the **source text**, by
  `RequirementTracingTest`. A reflective reader would need the layer-2 classes on the
  layer-1 classpath while layer 2 already needs the annotation, and no arrangement of source
  sets satisfies both. Reading text is also the only way to see both layers without running
  either.

### The gate token is a claim, and the claim is checked

`docs/requirements/` gained two `verify:` gates for this — `test` and `gametest` — and unlike
the other four they are traced. A requirement naming one of them asserts that a test at that
layer proves part of it, and `RequirementTracingTest` fails the build on either half of a
broken link:

| Failure | What it means |
|---|---|
| A citation names an id that is not in `docs/requirements/` | A typo, or a renumbering the index forbids. |
| A requirement claims `test`/`gametest` and nothing at that layer cites it | The requirement claims coverage it does not have — usually because the last test citing it was deleted or renamed. |

Adding `test` to a requirement without writing one fails `./gradlew build`. So does deleting
the last test for a requirement that still claims it. That is the whole point: without the
second rule the gate token would rot exactly like every other hand-maintained cross-reference.

`RequirementTracingTest` also holds the catalogue to its own stated rule — that the index's
status matrix and the domain files "are the same fact written twice; a mismatch is a bug in
the docs" — and checks the headline tally against the table it counts.

**Two things it deliberately does not do.** It does not require a test to cite a requirement:
`ImplementedInventoryTest` pins the defaults of an internal helper that no requirement
describes, and a false citation there would be worse than none. And it enforces no coverage
number — a requirement with no gate token is simply not claiming automated coverage, which is
the honest state for most of the thirty-six. See *What this strategy deliberately does not do*
below.

A gate token means a test proves **part** of the requirement, not all of it. Most of these
requirements have acceptance criteria no automated gate can reach; the criteria stay the
specification, and `runClient` stays the gate for anything in-world.

---

## The worked example: the #30 matrix test

This is what a test in this repo actually looks like, and it is not hypothetical. The
worker who fixed #30 **wrote it as a throwaway script**, ran it, and deleted it. The fix
landed in #37 and #40 rebuilt the script as `TreeMatrixTest`, which walks all 18 arms on
every build. So what follows is not a proposal: it is why that test is shaped the way it
is, and the three consequences at the end are the rules it was written to.

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
   is not evidence — and with #37 landed, the only way left to see it was to re-break the
   tree deliberately, which is what #40 did. Rotating the generator *names* failed
   `TreeMatrixTest` and left every gametest green; rotating the feature *keys* as well
   failed the gametests instead. Two halves of one bug, each invisible to the other layer,
   and neither test subsumes the other.

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
| `net.fabricmc:fabric-loader-junit` | 0.19.3 | A `testImplementation` dependency, pinned to `loader_version` — it boots the very loader it ships against, and a mismatch fails at class-load time with a stack trace that blames Knot rather than the version. It pulls in the Jupiter engine but **not** the parameterised runner, so `org.junit.jupiter:junit-jupiter` is declared alongside it. |
| `fabric-gametest-api-v1` | 4.0.21 | Already on the classpath via `fabric-api`. Driven by the `runGametest` run configuration. |
| `fabric-client-gametest-api-v1` | 6.0.0 | Present, and **unused** — layer 3 is not wired up. |

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

## Where tests live

```
src/test/java/com/minimammoth/ironoak/…               layer 1, loader-junit
src/gametest/java/com/minimammoth/ironoak/gametest/…  layer 2
src/gametest/resources/fabric.mod.json                its fabric-gametest entrypoints
src/testsupport/java/…/requirements/Requirement.java  the @Requirement annotation, and only that
```

Two directories rather than one, because the two kinds have nothing in common: one is
milliseconds and runs on every build, the other needs a game and a run configuration. Not
co-located with `src/main` — Loom's source sets and the remapper make co-location a fight
with no prize.

`testsupport` is a third, and it holds one annotation. It exists because the other two share
no classpath and both need it — see *Citing the requirement a test proves* above. It depends
on nothing at all: no Minecraft, no JUnit, no Loom mod registration.

`gametest` is a source set of its own, registered with Loom as a second mod
(`iron_oak_gametest`) so its classes are remapped against the mod they test, and carrying
its own `fabric.mod.json` because the entrypoint has to be declared somewhere. It also
means the `jar` task — which only ever packages `main` — cannot ship any of it. That is
worth checking rather than assuming:

```bash
unzip -l build/libs/iron-oak-*.jar | grep -ic gametest   # must be 0
```

`src/test` needs no such care: Gradle never packages a test source set.

Naming: mirror the class under test (`FireBowlEntityTest`, `ModRecipesTest`), or the
invariant when a test spans several (`TreeMatrixTest`, `RegistryAssetsTest`).
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
| Citing the nearest plausible requirement because a test "should" have one | `RequirementTracingTest` then reports that requirement as proven while nothing proves it — strictly worse than no citation, because it is read as coverage. | Cite the requirement the test actually proves, or none. `ImplementedInventoryTest` correctly has none. |
| Adding `test` to a requirement's `verify:` list before the test exists | It fails `./gradlew build` — the gate list is a claim, and the claim is checked. | Write the test, cite the requirement, then add the gate token. |
| Adding a test that needs a newer Loom, Gradle or Minecraft | `AGENTS.md`: a version bump is never a side effect. | Report it. It belongs in an `area:build` ticket. |

---

## What this strategy deliberately does not do

- **No coverage threshold, and no coverage number in CI.** The repo starts at zero, the
  matrix inflates line counts for free, and a percentage nobody enforces is worse than
  silence. The gate stays what `AGENTS.md` already says it is: `gh pr checks` green, plus
  `runClient` for anything in-world.
- **No mocking framework.** Nothing in this mod is worth a mock. Everything is either a
  value (layer 1) or a world (layer 2).
- **No requirement-coverage number, and no rule that every test cites a requirement.** The
  tracing test checks that the claims which *are* made are true; it does not count them.
  Twenty-three of the thirty-six requirements name no automated gate, and most of those
  honestly cannot — `runClient` is still the only thing that can see a particle.
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
| 2026-08-21 | 2.1 | Tests cite requirements (#43). New section on `@Requirement`: why the annotation lives in a third `testsupport` source set, why it is `RetentionPolicy.SOURCE`, and why the citations are read out of source text rather than reflectively — the reflective version needs a classpath cycle between the two test source sets. Records that `test` and `gametest` are traced `verify:` gates and that a gate token is a checked claim in both directions. |
| 2026-08-21 | 2.0 | Layers 1 and 2 exist (#40). Status changed from proposed to adopted. Records what #40 had to resolve against the jar rather than guess: loader-junit is a `LauncherSessionListener` and needs no code, static initialisers must not touch Minecraft, tags have to be bound by hand, the gametest run configuration is a plain `server()`, `@GameTest` defaults to an 8×8×8 empty structure. `playerWillDestroy` rewritten — `BlockEntity.preRemoveSideEffects` now drops any container's contents, so both halves of the old entry were wrong. Layer 3 marked as still not wired up, and the flint-and-steel interaction question left open rather than answered. |
| 2026-08-21 | 2.2 | The worked example describes a test that exists (#47). It said "nothing checks it today" while `TreeMatrixTest` had been checking all 18 arms since #40. The red-step consequence now records how #40 saw red after the fix had landed — by re-breaking the tree in two ways, one per layer. |

*Last updated: 2026-08-21*
