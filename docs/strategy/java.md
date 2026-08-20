---
domain: Strategy
domain_code: JAVA
status: active
last_updated: 2026-08-20
related:
  - ../../AGENTS.md
  - README.md
  - testing.md
  - ../ops/version-migration.md
---

# Java Guidelines

`AGENTS.md` tells you the rules of the repository. This document records the rules of the
**code** — the conventions that are in force but only implied by the source, and the two
that have already cost real bugs.

Read it before your first non-trivial change to `src/`. It is short because the codebase is
small: 19 files, roughly 1,500 lines, the largest around 260.

**Two caveats up front.**

Method names in this document are the ones on the branch you are reading. The
1.20.4 → 1.21.11 migration renames several of the load-bearing ones — `onUse` splits into
`useItemOn` and `useWithoutItem`, and the inventory and NBT hooks change spelling too (see
[#27](https://github.com/mini-mammoth/iron-oak/issues/27)). The *rules* below survive the
rename; the identifiers do not. Grep before you trust a name, and resolve API questions
with the `minecraft-fabric-lookup` skill rather than from memory.

The language level is **Java 17** (`options.release = 17`, `sourceCompatibility 17`,
`"java": ">=17"` in `fabric.mod.json`) even though Gradle itself must run on JDK 21. Do not
use a Java 21-only language feature; it will compile on your machine and fail the release
configuration.

---

## Registration: plain `Registry.register`, in `init/`

Everything the game needs to know about lives in `com.minimammoth.ironoak.init` as a
`Mod*` class: `ModBlocks`, `ModItems`, `ModEntityTypes`, `ModRecipes`,
`ModSaplingGenerators`, `ModConfiguredFeatures`. Each is a utility class with a private
constructor, `public static final` fields, and a `static void onInitialize()` that calls
`Registry.register(Registries.X, new Identifier(MOD_ID, "id"), value)`.

**There is no `DeferredRegister` here, and adding one is not an improvement.**
`DeferredRegister` is a Forge/NeoForge construct: those loaders open their registries only
during a registration event, so mods must queue their entries and let the loader flush them
at the right moment. Fabric has no such event. Registries are simply open during mod
initialization, so `Registry.register` at init time *is* the idiom. A tutorial that shows
you `DeferredRegister` is a tutorial for a different loader.

### Class-load order is load-bearing

The pattern has one non-obvious consequence. Fields like `ModBlocks.FIRE_BOWL` are
`static final` and constructed at **class load**, while registration happens later in
`onInitialize()`. So some classes are initialized by being *touched*, and the order in
`IronOak.onInitialize()` is deliberate:

```java
ModRecipes.onInitialize();      // no-op body; exists only to force this class to load
ModBlocks.onInitialize();
ModEntityTypes.onInitialize();  // dereferences ModBlocks.FIRE_BOWL
ModItems.onInitialize();        // dereferences ModBlocks.* for every BlockItem
```

`ModRecipes.onInitialize()` has an empty body and a comment saying so — its whole purpose
is to make the JVM run the class's `static` block, which is where the recipe types and
serializers register. Deleting the "pointless" call breaks recipes at load. Reordering the
rest produces a `null` block reference at registration.

**Rule:** when you add a `Mod*` class, give it an `onInitialize()` and call it from
`IronOak.onInitialize()` in dependency order, even if the body is empty. Do not rely on a
class being loaded by luck.

### Configured features are a *data* registry, not a code one

`ModConfiguredFeatures.bootstrap()` is **commented out** in `IronOak.onInitialize()` and
reached instead through `ModDataGenerator.buildRegistry()`:

```java
registryBuilder.addRegistry(RegistryKeys.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
```

That is correct and intentional: since 1.19.3 configured features are a dynamic
(data-driven) registry. The Java is a *generator* for JSON, and the JSON in
`src/main/generated/` is what ships and what runs. The commented-out line is a leftover
from before that change — leave it or remove it in a ticket that touches the file, but do
not "restore" it.

### The id is the contract. The constant name is not.

This is the most expensive lesson in the repository. From
[#30](https://github.com/mini-mammoth/iron-oak/issues/30), on `main`, unchanged for years:

```java
IRON_SPRUCE_TREE = registerKey("iron_dark_oak_tree");                              // ModConfiguredFeatures
IRON_SPRUCE      = generator("iron_dark_oak", ModConfiguredFeatures.IRON_SPRUCE_TREE); // ModSaplingGenerators
IRON_SPRUCE_SAPLING = sapling("iron_spruce_sapling", ModSaplingGenerators.IRON_SPRUCE, …); // ModBlocks
```

Every line compiles. The generated JSON is correct. `runDatagen` produces no diff. And in
game, for all three metals: spruce saplings grow dark oak, dark oak grows jungle, jungle
grows spruce. Only the **names** lied, and `ModBlocks` consumes them by name.

- The registered id is the contract with saved worlds, data packs and resource packs.
  Changing it breaks existing worlds. Changing a Java field name breaks nothing — and the
  compiler will never tell you the two disagree.
- **Rule: a constant's name must equal the id it registers, character for character.**
  `IRON_SPRUCE_TREE` holds `iron_spruce_tree` or it is misnamed.
- When they do disagree, **fix the name, not the key** — renaming identifiers corrects the
  wiring without touching `registerKey(...)` strings, generated output, or existing worlds.
- The invariant is mechanically checkable. See the worked example in
  [testing.md](testing.md).

---

## The 6×3 matrix decides the shape of `ModBlocks` and `ModItems`

Six wood types (oak, acacia, birch, jungle, spruce, dark oak) × three metals (iron, copper,
gold) = 18 arms. Each arm is a log block, a sapling block, two block items, a sapling
generator, a configured feature, a loot table, a tag, a model, a blockstate, a texture, a
lang entry, and a recipe. That is why `ModBlocks` and `ModItems` are long, flat and
repetitive.

**They are meant to be.** Two reasons, and both are load-bearing:

1. **Greppable.** In #26, a creative-menu screenshot showed exactly 15 magenta cubes and
   the mod had exactly 15 `iron_*` registrations. That match was findable in seconds
   because every registration is one literal line with a literal string in it. A clever
   loop would have cost an hour.
2. **A half-filled arm crashes.** `ModEntityTypes` and `ModItems` dereference `ModBlocks`
   fields directly; a missing entry is a `null` at registration, not a compile error.
   `AGENTS.md` is blunt about this: the matrix is all-or-nothing. Complete the arm or do
   not start it.

**Rule:** do not refactor the matrix into a loop unless the same PR moves the ids into a
single declarative table — one place that lists `{metal, wood}` and derives every id from
it. A loop that still spells the ids out in eighteen places has removed the greppability
and kept the duplication.

**When you add an arm, copy the majority shape, not the nearest line.** The file has warts
from half-finished edits: `ModItems.IRON_OAK_SAPLING` is `private static Item` while its
copper and gold siblings are `private static final`, and `ModSaplingGenerators` is the one
`init/` class without a private constructor. Those are noise, not precedent.

---

## The client/server split

The mod is `"environment": "*"` with two entrypoints:

| Entrypoint | Class | Runs |
|---|---|---|
| `main` | `com.minimammoth.ironoak.IronOak` | client **and** dedicated server |
| `client` | `com.minimammoth.ironoak.client.IronOakClient` | client only |

`IronOakClient` and `FireBowlRenderer` are annotated `@Environment(EnvType.CLIENT)`.
Everything else is common code.

**Never reference a client-only class from common code.** `FireBowlBlock` and
`FireBowlEntity` must not import `net.minecraft.client.*`; on a dedicated server those
classes do not exist and the JVM fails at class load, not at the call. The renderer reaches
*into* the block entity (`entity.getInput()`), never the other way round. That direction is
the rule.

Inside common code, branch on `world.isClient` — and understand which side you are on
before you mutate anything.

### The #27 rule: server-authoritative mutation must push its own sync

This is the second expensive lesson.
[#27](https://github.com/mini-mammoth/iron-oak/issues/27) defect 1: a log placed in the
fire bowl was stored correctly and rendered as nothing.

`FireBowlEntity` has exactly one method that pushes state to clients:

```java
@Override
public void setStack(int slot, ItemStack stack) {
    ImplementedInventory.super.setStack(slot, stack);
    if (world != null) {
        this.markDirty();
        world.updateListeners(pos, getCachedState(), getCachedState(), 3);  // <- the sync
    }
}
```

`setInput` writes the same list directly and skips it:

```java
public void setInput(ItemStack input, int cookingTotalTime) {
    items.set(INPUT_SLOT, input);   // no updateListeners: the client never hears about it
    …
}
```

The renderer draws `getInput()`, so the client's copy of the block entity stayed empty and
drew nothing. Retrieval still worked, because that path is server-side.

On 1.20.4 the old `onUse` ran on **both** sides, so the client happened to mutate its own
block entity and had the item locally. The 1.21.11 rewrite correctly bails out client-side —
which exposed a sync gap that double execution had been hiding for years.

Three rules follow, and they apply to every block entity added to this mod:

- **`markDirty()` is not a sync.** It marks the chunk for saving. `world.updateListeners(…)`
  (via `toUpdatePacket`) is what reaches a client.
- **Any field the renderer reads must be mutated only through a path that syncs.** If you
  add a direct `items.set(...)` or a new mutable field, it is invisible on the client until
  you make it not be. `spawnContainingItems` is worth re-reading with that in mind.
- **Any synced field must also survive a reload.** `writeNbt`/`readNbt` and
  `toInitialChunkDataNbt` are three places, and forgetting one of them is
  [#28](https://github.com/mini-mammoth/iron-oak/issues/28) defect 2: `cookingTotalTime` and
  `unlitTime` are never written, so a log mid-burn silently reverts to a hardcoded default
  on world load and the idle countdown restarts.
- **Fix the sync, do not reinstate client-side mutation.** Server-authoritative is the
  correct shape; the client predicting block-entity contents is not.

### Return values from interaction hooks are semantics, not booleans

#27 defect 2: a fire bowl containing a log swallowed the flint and steel. The 1.20.4 code
returned `ActionResult.PASS` — "I have no use for this, let the held item act" — and the
migration replaced it with a value that instead routed into the empty-hand path, which is
the *take the items out* branch. An empty bowl still lit, a loaded one did not.

**Rule:** when you change or port an interaction hook, resolve the return-value semantics
against the jar and state which value means *fall through*. Do not pattern-match on the
name. "An item this block has no use for must fall through to that item's own behaviour" is
a behavioural requirement, and it is not testable by compiling.

---

## Where `null` is tolerated, and where it is a bug

Vanilla Minecraft is full of nullable returns and null-as-a-flag parameters. This codebase
accepts that at the boundary and is defensive about it internally. Both halves matter.

**Deliberate, keep it:**

| Site | Why |
|---|---|
| `FireBowlBlock.getCodec()` returns `null` | Carries a comment and a link: the codec is not used yet in this version. Returning `null` is the documented state, not an oversight. |
| `@Nullable` on `createBlockEntity`, `getTicker`, `getPlacementState`, `extinguish`'s entity | These match the vanilla signatures. Annotate what vanilla annotates, with `org.jetbrains.annotations.Nullable`. |
| `world.playSound(null, pos, …)` | `null` means "no player is excluded from hearing this". A flag, not a missing value. |
| `craft(inventory, null)` | The registry lookup is unused on this path in this version. Re-check it at the next version bump — it is exactly the kind of parameter that becomes mandatory. |
| `FabricBlockEntityTypeBuilder…build(null)` | The type argument vanilla ignores. |
| `Boolean.TRUE.equals(state.get(LIT))` instead of `state.get(LIT)` | The property getter is nullable-annotated, so the direct form warns. The defensive form is this codebase's convention — use it consistently. `canFillWithFluid` still has the bare form; that is the outlier, not the model. |

**A real hazard, and the shape to avoid:** `FireBowlEntity` guards `world != null` in
`setStack` and then dereferences `this.world` unguarded in `getRecipeFor` a few lines
later. A `BlockEntity` genuinely can exist before its world is set, so the guard is not
paranoia — which means the unguarded dereference is a latent NPE.

> **Rule: inside a `BlockEntity`, treat `world` as nullable everywhere or nowhere.** Today
> the file does both, and a reader cannot tell which methods are safe. If you touch one of
> them, make it consistent within that class.

Beyond the vanilla boundary: do not return `null` from your own methods. `Optional` is
already the house style for "might not be there" — `getRecipeFor` returns
`Optional<BurningRecipe>`, `SaplingGenerator` takes `Optional`s, `getPlacementState` wraps
a nullable super call in `Optional.ofNullable(...).orElse(...)`.

---

## Conventions actually in force

- **`var` for locals.** Used pervasively for anything whose type the right-hand side makes
  obvious. Explicit types for fields and signatures.
- **Utility classes get a private constructor.** Every `init/Mod*` class except
  `ModSaplingGenerators`.
- **Constants are `static final` UPPER_SNAKE, and magic numbers get names.**
  `UNLIT_TOTAL_TIME`, `DEFAULT_COOKING_TOTAL_TIME`, `INPUT_SLOT`, `OUTPUT_SLOT`, `SHAPE`.
  One caveat worth knowing: `FireBowlEntity.cookingTotalTime` is initialized to `200`
  inline while `DEFAULT_COOKING_TOTAL_TIME` is `150`, so "the default" depends on whether
  the bowl has burned anything yet. If you touch that field, collapse it to one named
  default. (`FireBowlBlock.FIRE_DAME` is a typo for `FIRE_DAMAGE`; it is public, so rename
  it in a ticket that already touches the file rather than spreading it further.)
- **Javadoc records *why*, and credits what was copied.** `ImplementedInventory` names its
  original author and the Fabric wiki page; `unlitServerTick` says "Borrowed from
  `CampfireBlockEntity`". **Keep doing that** — when you model a behaviour on a vanilla
  class, name the class. The next migration needs to know which vanilla code to re-check,
  and #28 defect 1 is a guard copied from `CampfireBlock.onProjectileHit` that was already
  wrong.
- **One logger.** `IronOak.LOGGER`, from `LoggerFactory.getLogger(MOD_ID)`. No second
  logger, no `System.out`, and no debug logging left in a commit.
- **English everywhere** in code, identifiers, comments and commit messages. Player-facing
  strings belong in `assets/iron_oak/lang/`, never inline — and assert the key, not the
  text, if you test it.

---

## Things not to do without a ticket

| Do not | Why |
|---|---|
| Add a mixin | `iron-oak.mixins.json` exists but is **empty**, and there is no `mixin` package. Mixins are a maintenance liability across version bumps; adding the first one is a design decision, not an implementation detail. |
| Add an access-widener entry | There is exactly one, for the cooking-recipe serializer factory. Widening more vanilla surface changes what the remapper produces and what future versions can break. |
| Hand-edit `src/main/generated/` | It is output. The next `runDatagen` reverts you silently. Change `ModDataGenerator` (or its providers) and commit the regenerated files with the Java change. |
| Change `gradle.properties`, `build.gradle`, the Gradle wrapper or `.github/` in a feature PR | `AGENTS.md`: a version bump is never a side effect. It makes the PR unreviewable and unrevertable. Report it and let it be an `area:build` ticket. |
| "Unify" the mod id and the artifact id | `iron_oak` (underscore) and `iron-oak` (hyphen) are both load-bearing. |
| Answer a Minecraft API question from memory | Use the `minecraft-fabric-lookup` skill. #26, #27 and #28 all have a version-drift component; guessing produced code that compiled and did the wrong thing. |

---

## 📅 Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial version (#39). Writes down what `AGENTS.md` only implies: the `Registry.register` pattern and why Fabric has no `DeferredRegister`, class-load order as a load-bearing property of `init/`, "the id is the contract and the constant name is not" (#30), the server-authoritative-plus-explicit-sync rule (#27), the persisted-field rule (#28), the tolerated nulls with reasons, and how the 6×3 matrix constrains the shape of `ModBlocks`/`ModItems`. |

*Last updated: 2026-08-20*
