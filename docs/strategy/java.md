---
domain: Strategy
domain_code: JAVA
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - README.md
  - testing.md
  - ../ops/version-migration.md
  - ../../.claude/skills/java/SKILL.md
---

# Java Guidelines

`AGENTS.md` tells you the rules of the repository. This document records the rules of the
**code** — the conventions that are in force but only implied by the source, and the two
that have already cost real bugs.

Read it before your first non-trivial change to `src/`. It is short because the codebase is
small: twenty-odd files, a couple of thousand lines, the largest a few hundred.

**Two things to know before you read a name here.**

**Mappings are Mojang official, not Yarn.** Yarn was discontinued after 1.21.11 and this
mod migrated off it, so every name below is the Mojang one — `Level`, `Identifier`,
`BlockBehaviour.Properties`, `BuiltInRegistries`. A Yarn-era snippet found online (`World`,
`AbstractBlock.Settings`, `world.isClient`, `new Identifier(...)`) needs translating first:
https://mappings.dev. Watch `Registries` in particular — under Yarn it held the registries
themselves, under Mojang mappings it holds their `ResourceKey`s and `BuiltInRegistries`
holds the registries, so the same name means two different things depending on the vintage
of the snippet. Resolve API questions with the `minecraft-fabric-lookup` skill rather than
from memory.

The language level is **Java 21** (`options.release = 21`, `sourceCompatibility 21`,
`"java": ">=21"` in `fabric.mod.json`), and Gradle itself must run on JDK 21 too, because
Loom does not run on 22+.

---

## Registration: plain `Registry.register`, in `init/`

Everything the game needs to know about lives in `com.minimammoth.ironoak.init` as a
`Mod*` class: `ModBlocks`, `ModItems`, `ModEntityTypes`, `ModRecipes`,
`ModSaplingGenerators`, `ModConfiguredFeatures`. Each is a utility class with a private
constructor, `public static final` fields, and a private `register(...)` helper that ends
in `Registry.register(BuiltInRegistries.X, key, value)`.

Since 1.21.2 a block's or item's settings must **carry their own registry key**, so
construction and registration are one step and cannot be split:

```java
private static Block register(String name, Function<BlockBehaviour.Properties, Block> factory, Block copyFrom) {
    ResourceKey<Block> key = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(MOD_ID, name));
    BlockBehaviour.Properties settings = BlockBehaviour.Properties.ofLegacyCopy(copyFrom).setId(key);
    return Registry.register(BuiltInRegistries.BLOCK, key, factory.apply(settings));
}
```

A block can no longer be built as a static constant and registered later, which is why
`ModBlocks.FIRE_BOWL` is now `register("fire_bowl", FireBowlBlock::new, Blocks.CAULDRON)`
rather than a `new FireBowlBlock(...)` with a matching line further down the file.

**There is no `DeferredRegister` here, and adding one is not an improvement.**
`DeferredRegister` is a Forge/NeoForge construct: those loaders open their registries only
during a registration event, so mods must queue their entries and let the loader flush them
at the right moment. Fabric has no such event. Registries are simply open during mod
initialization, so `Registry.register` at init time *is* the idiom. A tutorial that shows
you `DeferredRegister` is a tutorial for a different loader.

### Class-load order is load-bearing

The pattern has one non-obvious consequence. Since construction and registration are the
same step, a field like `ModBlocks.FIRE_BOWL` **registers itself at class load** — before
`onInitialize()` runs, and only if something causes the class to load at all. So these
classes are initialized by being *touched*, and the order in `IronOak.onInitialize()` is
deliberate:

```java
ModRecipes.onInitialize();      // empty body: forces the static block that registers the recipe types
ModBlocks.onInitialize();       // empty body: loading the class is what registers every block
ModEntityTypes.onInitialize();  // real body, and it dereferences ModBlocks.FIRE_BOWL
ModItems.onInitialize();        // registers the creative tab; the items registered themselves on load
```

**Two of those four have empty bodies, and both say so in a comment.** Their whole purpose
is to make the JVM run the class — for `ModRecipes` the `static` block that registers the
recipe types and serializers, for `ModBlocks` the field initialisers that register all 37
blocks. Deleting a "pointless" call breaks registration at load. Reordering the rest
produces a `null` block reference: `ModEntityTypes` builds its block entity type against
`ModBlocks.FIRE_BOWL`.

**Rule:** when you add a `Mod*` class, give it an `onInitialize()` and call it from
`IronOak.onInitialize()` in dependency order, even if the body is empty. Do not rely on a
class being loaded by luck.

### Configured features are a *data* registry, not a code one

`ModConfiguredFeatures.bootstrap()` is **commented out** in `IronOak.onInitialize()` and
reached instead through `ModDataGenerator.buildRegistry()`:

```java
registryBuilder.add(Registries.CONFIGURED_FEATURE, ModConfiguredFeatures::bootstrap);
```

That is correct and intentional: since 1.19.3 configured features are a dynamic
(data-driven) registry. The Java is a *generator* for JSON, and the JSON in
`src/main/generated/` is what ships and what runs. The commented-out line is a leftover
from before that change — leave it or remove it in a ticket that touches the file, but do
not "restore" it.

### The id is the contract. The constant name is not.

This is the most expensive lesson in the repository.
[#30](https://github.com/mini-mammoth/iron-oak/issues/30) shipped for years and was fixed
in #37. What it looked like:

```java
IRON_SPRUCE_TREE = registerKey("iron_dark_oak_tree");                              // ModConfiguredFeatures
IRON_SPRUCE      = generator("iron_dark_oak", ModConfiguredFeatures.IRON_SPRUCE_TREE); // ModSaplingGenerators
IRON_SPRUCE_SAPLING = sapling("iron_spruce_sapling", ModSaplingGenerators.IRON_SPRUCE, …); // ModBlocks
```

Every line compiled. The generated JSON was correct. `runDatagen` produced no diff. And in
game, for all three metals: spruce saplings grew dark oak, dark oak grew jungle, jungle
grew spruce. Only the **names** lied, and `ModBlocks` consumes them by name.

All three files now agree with themselves — `IRON_SPRUCE_TREE = registerKey("iron_spruce_tree")` —
and the current registration shape helps, because the id literal and the constant name sit
on the same line. That makes the next instance of this mistake visible in review instead of
invisible. It does not make it impossible.

- The registered id is the contract with saved worlds, data packs and resource packs.
  Changing it breaks existing worlds. Changing a Java field name breaks nothing — and the
  compiler will never tell you the two disagree. That asymmetry is the whole bug.
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
   because every arm is one line carrying its own id literal. A clever loop would have cost
   an hour.
2. **A half-filled arm crashes.** `ModEntityTypes` and `ModItems` dereference `ModBlocks`
   fields directly; a missing entry is a `null` at registration, not a compile error.
   `AGENTS.md` is blunt about this: the matrix is all-or-nothing. Complete the arm or do
   not start it.

**Rule:** do not refactor the matrix into a loop unless the same PR moves the ids into a
single declarative table — one place that lists `{metal, wood}` and derives every id from
it. A loop that still spells the ids out in eighteen places has removed the greppability
and kept the duplication.

There is one place the codebase already does derive rather than list, and it is the right
model for anything that must cover the whole matrix: `ModItems.onInitialize()` fills the
creative tab, and `ModModelGenerator.generateItemModels` emits the item definitions, both
by walking `BuiltInRegistries.ITEM` filtered to the `iron_oak` namespace. Complete by
construction — a new arm cannot be forgotten by either.

**When you add an arm, copy the majority shape, not the nearest line.** The `private
static final` / `register(...)` pairing in `ModBlocks` and `ModItems` is the shape; every
field in both files follows it today. `ModSaplingGenerators` is the one all-static `init/`
class still missing a private constructor — noise, not precedent.

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

Inside common code, branch on `level.isClientSide()`, or better on
`level instanceof ServerLevel` where the server path needs a `ServerLevel` anyway — recipe
lookups do. Understand which side you are on before you mutate anything.

### The #27 rule: server-authoritative mutation must push its own sync

This is the second expensive lesson.
[#27](https://github.com/mini-mammoth/iron-oak/issues/27) defect 1: a log placed in the
fire bowl was stored correctly and rendered as nothing. `setInput` wrote the item list
directly, and the one method that pushed state to clients was somewhere else, so the
client's copy of the block entity stayed empty and the renderer correctly drew nothing.
Retrieval still worked, because that path is server-side.

On 1.20.4 the old `onUse` ran on **both** sides, so the client happened to mutate its own
block entity and had the item locally. The 1.21.11 rewrite correctly bails out client-side —
which exposed a sync gap that double execution had been hiding for years.

The fix is the shape to copy. `FireBowlEntity.markUpdated()` is now the single funnel, and
every container write goes through it — `setItem`, `removeItem`, `removeItemNoUpdate`,
`clearContent`, and `setInput`, which is now just `setItem(INPUT_SLOT, input)`:

```java
private void markUpdated() {
    if (level == null) {
        return;
    }

    setChanged();
    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);  // <- the sync
}
```

Five rules follow, and they apply to every block entity added to this mod:

- **`setChanged()` is not a sync.** It marks the chunk for saving. `sendBlockUpdated(…)`
  (via `getUpdatePacket`) is what reaches a client. Vanilla's `removeItemNoUpdate` is named
  for save-dirtying, not for the client — it still has to push.
- **Any field the renderer reads must be mutated only through a path that syncs.** A direct
  `items.set(...)` or a new mutable field is invisible on the client until you make it not
  be. `FireBowlEntity`'s own comment states the invariant: every write to `items` outside
  `loadAdditional` has to end up in `markUpdated`.
- **Any synced field must also survive a reload** — `saveAdditional`, `loadAdditional` and
  `getUpdateTag`. Forgetting one is
  [#28](https://github.com/mini-mammoth/iron-oak/issues/28) defect 2: `unlitTime` was never
  written, so the idle countdown restarted on every world load. It is persisted as
  `unlit_time` now, with a default that is deliberately correct for a bowl saved before the
  fix.
- **Persist real state; derive everything else.** The same #28 also killed a field:
  `cookingTotalTime` used to be cached on the entity, written on exactly one insertion path
  and never saved. It is now a private static function of the matched recipe, and the class
  comment says why — a value that belongs to the recipe must not be cached on the entity.
  Derived state is neither saved nor synced.
- **Fix the sync, do not reinstate client-side mutation.** Server-authoritative is the
  correct shape; the client predicting block-entity contents is not.

### Return values from interaction hooks are semantics, not booleans

#27 defect 2: a fire bowl containing a log swallowed the flint and steel. The 1.20.4 code
returned `ActionResult.PASS` — "I have no use for this, let the held item act" — and the
migration replaced it with `TRY_WITH_EMPTY_HAND`, which instead routes into `useWithoutItem`,
the *take the items out* branch. An empty bowl still lit, a loaded one did not.
`PASS` is the successor of `ActionResult.PASS`; `TRY_WITH_EMPTY_HAND` is not.

`FireBowlBlock.useItemOn` now carries a javadoc that spells out all four values it can
return and why each one is needed — including why the client returns `CONSUME` (it cannot
resolve a recipe without a `ServerLevel`) and why the server must therefore answer
`SUCCESS_SERVER` to get the arm swing. **Read it before you touch that method**; it is the
reasoning the compiler cannot hold.

**Rule:** when you change or port an interaction hook, resolve the return-value semantics
against the jar, state which value means *fall through*, and write the reasoning down where
the next reader will find it. Do not pattern-match on the name. "An item this block has no
use for must fall through to that item's own behaviour" is a behavioural requirement, and
it is not testable by compiling.

---

## Where `null` is tolerated, and where it is a bug

Vanilla Minecraft is full of nullable returns and null-as-a-flag parameters. This codebase
accepts that at the boundary and is defensive about it internally. Both halves matter.

**Deliberate, keep it:**

| Site | Why |
|---|---|
| `FireBowlBlock.codec()` returns `null` | Carries a comment and a link: the codec is not used yet. Returning `null` is the documented state, not an oversight. |
| `@Nullable` on `newBlockEntity`, `getTicker`, `getStateForPlacement`, `extinguish`'s entity | These match the vanilla signatures. Annotate what vanilla annotates. Note the mod uses `org.jetbrains.annotations.Nullable` everywhere except `FireBowlRenderer`, which takes `org.jspecify.annotations.Nullable` from the signature it overrides — follow the file you are in. |
| `world.playSound(null, pos, …)` | `null` means "no player is excluded from hearing this". A flag, not a missing value. |
| `FabricBlockEntityTypeBuilder…build(null)` | The type argument vanilla ignores. |
| `Boolean.TRUE.equals(state.getValue(LIT))` instead of `state.getValue(LIT)` | The property getter is nullable-annotated, so the direct form warns. The defensive form is this codebase's convention — use it consistently. `canPlaceLiquid` still has the bare form; that is the outlier, not the model. |

**The shape to avoid, and it is worth knowing because the codebase used to have it.**
`FireBowlEntity` once guarded `world != null` in one method and dereferenced the same field
unguarded in the next. A `BlockEntity` genuinely can exist before its level is set, so the
guard was not paranoia — which made the unguarded dereference a latent NPE.

That is now consistent, and consistency is the rule rather than the specific fix:
`markUpdated` and `spawnContainingItems` both return early on `level == null`,
`getRecipeFor` takes the `ServerLevel` as a parameter instead of reaching for the field,
`canPlaceItemThroughFace` tests `this.level instanceof ServerLevel`, and
`FireBowlRenderer.extractRenderState` guards `entity.getLevel() == null` before reading the
game time.

> **Rule: inside a `BlockEntity`, treat `level` as nullable everywhere or nowhere.** A
> reader must be able to tell which methods are safe without checking all of them. Prefer
> taking the level as a parameter where the caller already has one — that removes the
> question instead of answering it.

Beyond the vanilla boundary: do not return `null` from your own methods. `Optional` is
already the house style for "might not be there" — `getRecipeFor` returns
`Optional<BurningRecipe>`, `TreeGrower` takes `Optional`s, `getStateForPlacement` wraps a
nullable super call in `Optional.ofNullable(...).orElse(...)`.

---

## Conventions actually in force

- **`var` for locals.** Used pervasively for anything whose type the right-hand side makes
  obvious. Explicit types for fields and signatures.
- **All-static classes get a private constructor.** Every one in `init/` has it except
  `ModSaplingGenerators`. The datagen providers (`ModDataGenerator`, `ModModelGenerator`,
  `ModWorldGenerator`) are instantiated by Fabric and correctly do not.
- **Constants are `static final` UPPER_SNAKE, and magic numbers get names.**
  `UNLIT_TOTAL_TIME`, `INPUT_SLOT`, `OUTPUT_SLOT`, `SHAPE`,
  `ModRecipes.DEFAULT_COOKING_TIME`. That last one is the model for a shared number: it is
  declared once, in the class that registers the serializer that consumes it, and its
  javadoc names the other reader (`FireBowlEntity`, which falls back to it when an input
  has no recipe) and says the two must not drift. Before #28 there were two disagreeing
  defaults — a `200` inline field initialiser and a `150` named constant. One default, one
  home. (`FireBowlBlock.FIRE_DAME` is a typo for `FIRE_DAMAGE`; it is public, so rename it
  in a ticket that already touches the file rather than spreading it further.)
- **Javadoc records *why*, and credits what was copied.** `ImplementedInventory` names its
  original author and the Fabric wiki page; `unlitServerTick` says "Borrowed from
  `CampfireBlockEntity`"; the post-#28 code names `AbstractFurnaceBlockEntity`,
  `CampfireBlockEntity.markUpdated`, `CampfireBlockEntity.placeFood` and
  `JukeboxBlockEntity.popOutTheItem` at the four places it copies their shape. **Keep doing
  that** — when you model a behaviour on a vanilla class, name the class. The next migration
  needs to know which vanilla code to re-check, and #28 defect 1 was a guard modelled on
  `CampfireBlock.onProjectileHit` that had been transcribed wrong.
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
| Add an access-widener entry | `iron_oak.accesswidener` is declared in `fabric.mod.json` but **inert**: its only line is commented out, so it widens nothing. Adding the first live entry is a decision about how much vanilla surface this mod couples to, not an implementation detail. |
| Hand-edit `src/main/generated/` | It is output. The next `runDatagen` reverts you silently. Change `ModDataGenerator` (or its providers) and commit the regenerated files with the Java change. |
| Change `gradle.properties`, `build.gradle`, the Gradle wrapper or `.github/` in a feature PR | `AGENTS.md`: a version bump is never a side effect. It makes the PR unreviewable and unrevertable. Report it and let it be an `area:build` ticket. In particular do not re-pin the wrapper to 8.11.1 — that pin belonged to the 1.20.4 line and Loom 1.17 needs Gradle 9.x. |
| "Unify" the mod id and the artifact id | `iron_oak` (underscore) and `iron-oak` (hyphen) are both load-bearing. |
| Answer a Minecraft API question from memory | Use the `minecraft-fabric-lookup` skill. #26, #27 and #28 all have a version-drift component; guessing produced code that compiled and did the wrong thing. |
