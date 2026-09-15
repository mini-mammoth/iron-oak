---
name: java
description: The rules for writing Java in this mod — modern-Java style (var by default, arrow switches, pattern-matching instanceof), plain Registry.register in init/, the class-load order that registration depends on, the block-entity sync rule, where null is tolerated, and what needs a ticket instead of a decision. Use before writing or changing anything under src/, when adding a block, item, recipe or block entity, when touching an interaction hook or anything the renderer reads, when deciding between var and an explicit type, or when a registration produces a null or a texture goes missing. Triggers on "add a block", "add an item", "register", "DeferredRegister", "block entity", "sync", "markUpdated", "ActionResult"/"InteractionResult", "@Nullable", "should I use var", "is this idiomatic here", and any new arm of the 6x3 matrix.
---

# Writing Java in Iron Oak

[`docs/strategy/java.md`](../../../docs/strategy/java.md) is the authority and explains
**why** each of these holds. This is the same content as a checklist, for use while you are
typing rather than before you start. Where the two disagree, `java.md` wins — and fix this
file in the same PR.

Read `java.md` in full before your first non-trivial change to `src/`. The mod is twenty-odd
files and a couple of thousand lines; the document is shorter than the codebase.

---

## Stop — these need a ticket, not a decision

Do not do any of these as part of another change, however small it looks:

| Do not | Instead |
|---|---|
| Add a mixin | `iron-oak.mixins.json` is empty and there is no `mixin` package. The first one is a design decision. Report it. |
| Add an access-widener entry | `iron_oak.accesswidener` is declared but inert — its only entry is commented out, so it widens nothing. Same reasoning. |
| Touch `gradle.properties`, `build.gradle`, the Gradle wrapper or `.github/` | A version bump is never a side effect. It belongs to an `area:build` ticket. Do not re-pin the wrapper to 8.11.1 — Loom 1.17 needs Gradle 9.x. |
| Hand-edit `fabric/src/main/generated/` | Output. Change `ModDataGenerator` (or a provider), run `./gradlew runDatagen`, commit both together. |
| "Unify" `iron_oak` and `iron-oak` | Mod id and artifact id. Both load-bearing. |
| Answer a Minecraft API question from memory or from a tutorial | Use the `minecraft-fabric-lookup` skill. Mappings are **Mojang official, not Yarn** — a Yarn-era snippet (`World`, `AbstractBlock.Settings`, `world.isClient`, `new Identifier(...)`) needs translating first. |
| Ship half an arm of the 6×3 matrix | All-or-nothing. Complete it or report that you cannot. |

---

## Modern Java: what is in force

Language level is **Java 21** (`options.release = 21`, `"java": ">=21"` in
`fabric.mod.json`). Gradle itself must run on **JDK 21** — Loom does not run on 22+:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
```

Read off the tree, not aspirational. The **Status** column says what the code does today; if
you want a count, grep for it rather than trusting a number written down here:

| Feature | Status here | Rule |
|---|---|---|
| `var` for locals | pervasive, in `src/main` and the tests alike | **Default to `var`** wherever the right-hand side makes the type obvious. Reach for an explicit type only when it genuinely is not — a bare `Map.of()`, a builder chain that returns something surprising. |
| Explicit types on fields and signatures | always | `var` is for locals only. Fields, parameters and return types are spelled out. |
| Arrow switch | the only form present — **no** `case X:` anywhere | Always `case A ->`. Prefer a switch *expression* that returns, as `FireBowlEntity.getSlotsForFace` does. Never write a colon switch. |
| Pattern-matching `instanceof` | standard throughout | `if (x instanceof ServerLevel server)`. Never cast after a bare `instanceof`. |
| `final` on locals | **never used** | Do not add it. It is not this codebase's style and adding it to one method makes every other method look deliberate. |
| Records | in the tests (`Matrix.Arm`, the requirement catalogue), not in `src/main` | Fine for a value type — a tuple with a name. There is nothing in `src/main` that wants one yet; do not convert an existing class to make a point. |
| `Optional` | the house style for absence | For "might not be there" in your **own** API. See the null section. |
| `List.of` / `Map.of` | used for fixed data | Immutable factories. `ModItems` builds the three infusion maps this way. |
| Streams | sparingly, in `src/main` | Used where they read better, not on principle. A `for` loop over 18 arms is not a defect. |
| Text blocks | unused | No multi-line string in `src/main` needs one. Fine in a test message if it earns its place. |
| Sealed types | unused | No hierarchy here is closed enough to want one. Do not introduce one without a reason in the ticket. |

The distinction that matters: `final` locals and colon switches are **not wanted**. Records,
text blocks and sealed types are merely **unused** — reach for them if the code genuinely
calls for one.

---

## Registration

Everything the game must know about lives in `com.minimammoth.ironoak.init` as a `Mod*`
class: a private constructor, `public static final` fields, a private `register(...)` helper
ending in `Registry.register(BuiltInRegistries.X, key, value)`.

- **No `DeferredRegister`.** That is a Forge/NeoForge construct for loaders whose registries
  open only during an event. Fabric's registries are open during mod initialization, so
  `Registry.register` at init time *is* the idiom.
- **Settings carry their own registry key** since 1.21.2, so construction and registration
  are one step and cannot be split. Build the `ResourceKey` first, `setId(key)` on the
  settings, register the result.

### Class-load order is load-bearing

A field like `ModBlocks.FIRE_BOWL` **registers itself at class load**, before
`onInitialize()` runs, and only if something touches the class. Two of the four
`onInitialize()` calls in `IronOak.onInitialize()` have **empty bodies whose only purpose is
to make the JVM load the class** — and both say so in a comment.

- Deleting a "pointless" `onInitialize()` call breaks registration at load.
- Reordering them produces a `null` block reference: `ModEntityTypes` dereferences
  `ModBlocks.FIRE_BOWL`.
- **When you add a `Mod*` class, give it an `onInitialize()` and call it from
  `IronOak.onInitialize()` in dependency order, even if the body is empty.** Never rely on a
  class being loaded by luck.

Configured features are the exception: they are a **data** registry, reached through
`ModDataGenerator.buildRegistry()`. The commented-out `ModConfiguredFeatures.bootstrap()`
line in `IronOak` is correct as it stands — do not "restore" it.

### The id is the contract; the constant name is not

The most expensive lesson in the repo (#30, shipped for years):

```java
IRON_SPRUCE_TREE = registerKey("iron_dark_oak_tree");   // compiled, datagen clean, game wrong
```

- The registered **id** is the contract with saved worlds, data packs and resource packs. A
  Java field name is a comment the compiler does not check.
- **Rule: a constant's name equals the id it registers, character for character.**
  `IRON_SPRUCE_TREE` holds `iron_spruce_tree` or it is misnamed.
- When they disagree, **fix the name, not the key.** Renaming identifiers corrects the wiring
  without touching `registerKey(...)` strings, generated output or existing worlds.
- `TreeMatrixTest` checks this now, for all 18 arms. It will catch you.

---

## The 6×3 matrix

Six woods × three metals = 18 arms. Each arm is a log block, a sapling block, two block
items, a sapling generator, a configured feature, a loot table, a tag, a model, a blockstate,
a texture, a lang entry and a recipe.

`ModBlocks` and `ModItems` are long, flat and repetitive **on purpose**: every arm is one
line carrying its own id literal, which is what made #26 findable in seconds, and a missing
entry is a `null` at registration rather than a compile error.

- **Copy the majority shape, not the nearest line.** `ModSaplingGenerators` is the one
  all-static `init/` class still missing a private constructor — noise, not precedent.
- **Do not refactor the matrix into a loop** unless the same PR moves the ids into one
  declarative table that derives every id. A loop that still spells ids out eighteen times
  has removed the greppability and kept the duplication.
- Where something must cover the whole matrix, **derive it**: `ModItems.onInitialize()` and
  `ModModelGenerator.generateItemModels` both walk `BuiltInRegistries.ITEM` filtered to the
  `iron_oak` namespace, so a new arm cannot be forgotten.

---

## Block entities: the sync rule

#27 defect 1: a log was stored correctly in the fire bowl and rendered as nothing. The write
did not push an update, so the client's copy stayed empty and the renderer correctly drew
nothing.

`FireBowlEntity.markUpdated()` is the single funnel, and every container write goes through
it. Five rules, and they apply to every block entity added to this mod:

1. **`setChanged()` is not a sync.** It marks the chunk for saving. `sendBlockUpdated(…)` is
   what reaches a client. Vanilla's `removeItemNoUpdate` is named for save-dirtying, not for
   the client — it still has to push.
2. **Any field the renderer reads must be mutated only through a path that syncs.** A direct
   `items.set(...)` or a new mutable field is invisible on the client until you make it not be.
3. **Any synced field must also survive a reload** — `saveAdditional`, `loadAdditional` *and*
   `getUpdateTag`. Forgetting one is #28 defect 2, where the idle countdown restarted on
   every world load.
4. **Persist real state; derive everything else.** #28 deleted a cached `cookingTotalTime`
   field in favour of a function of the matched recipe. Derived state is neither saved nor
   synced.
5. **Fix the sync; do not reinstate client-side mutation.** Server-authoritative is the
   correct shape.

---

## The client/server split

Two entrypoints: `main` (`IronOak`) runs on client and dedicated server; `client`
(`IronOakClient`) is client-only. `IronOakClient` and `FireBowlRenderer` are
`@Environment(EnvType.CLIENT)`; everything else is common.

- **Never reference a client-only class from common code.** `FireBowlBlock` and
  `FireBowlEntity` must not import `net.minecraft.client.*` — on a dedicated server those
  classes do not exist and the JVM fails at class load, not at the call.
- **The renderer reaches into the block entity, never the other way round.** That direction
  is the rule.
- Inside common code branch on `level.isClientSide()`, or better on
  `level instanceof ServerLevel` where the server path needs a `ServerLevel` anyway — recipe
  lookups do.

### Interaction hooks return semantics, not booleans

#27 defect 2: a bowl containing a log swallowed the flint and steel, because the port
replaced `PASS` ("I have no use for this, let the held item act") with `TRY_WITH_EMPTY_HAND`,
which routes into the *take the items out* branch.

**When you change or port an interaction hook: resolve the return-value semantics against
the jar, state which value means _fall through_, and write the reasoning down where the next
reader will find it.** Do not pattern-match on the name. `FireBowlBlock.useItemOn` already
carries that javadoc — read it before touching the method.

---

## `null`

Vanilla is full of nullable returns and null-as-a-flag parameters. Accept that at the
boundary; be defensive inside.

- **Annotate what vanilla annotates.** `@Nullable` on `newBlockEntity`, `getTicker`,
  `getStateForPlacement`. The mod uses `org.jetbrains.annotations.Nullable` everywhere except
  `FireBowlRenderer`, which takes `org.jspecify.annotations.Nullable` from the signature it
  overrides — **follow the file you are in.**
- `world.playSound(null, …)` and `build(null)` are flags, not missing values. Leave them.
- Use `Boolean.TRUE.equals(state.getValue(LIT))`, not the bare getter — the property getter is
  nullable-annotated and the direct form warns. (`canPlaceLiquid` still has the bare form;
  that is the outlier, not the model.)
- **Inside a `BlockEntity`, treat `level` as nullable everywhere or nowhere.** A reader must
  be able to tell which methods are safe without checking all of them. Prefer taking the
  level as a parameter where the caller already has one.
- **Do not return `null` from your own methods.** `Optional` is the house style.

---

## Style

- **All-static classes get a private constructor.** Datagen providers are instantiated by
  Fabric and correctly do not.
- **Constants are `static final` UPPER_SNAKE, and magic numbers get names.** One default, one
  home: `ModRecipes.DEFAULT_COOKING_TIME` is declared once, in the class that registers the
  serializer that consumes it, and its javadoc names the other reader. Before #28 there were
  two disagreeing defaults.
- **Javadoc records *why*, and credits what was copied.** When you model a behaviour on a
  vanilla class, **name the class** — the next migration needs to know what to re-check, and
  #28 defect 1 was a guard modelled on `CampfireBlock.onProjectileHit` and transcribed wrong.
- **One logger:** `IronOak.LOGGER`. No `System.out`, and no debug logging left in a commit.
- **English everywhere** in code, identifiers, comments and commit messages. Player-facing
  strings live in `assets/iron_oak/lang/` — never inline.
- `FireBowlBlock.FIRE_DAME` is a typo for `FIRE_DAMAGE`. It is public; rename it in a ticket
  that already touches the file rather than spreading it further.

---

## Tests come with the change

- Which layer: **name, id, map, file or number → layer 1** (`fabric/src/test`); **tick,
  entity or block changing → layer 2** (`fabric/src/gametest`). See
  [`docs/strategy/testing.md`](../../../docs/strategy/testing.md).
- **A bug fix ships with the test that catches it, and the test comes first.** Observe the
  red locally, report it in the PR, never commit red.
- **Cite the requirement the test proves** — `@Requirement("MAT-02")`. If the requirement's
  acceptance criteria name `test` or `gametest`, the citation is mandatory and
  `RequirementTracingTest` enforces it in both directions. If no requirement describes what
  you are testing, cite nothing: a plausible-looking id reports coverage that does not exist.
- **If you wrote a script to verify a fix, you wrote a test. Check it in.**

## Before you commit

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew build          # compiles, remaps, builds the jar, runs the unit tests
./gradlew runGametest    # headless server, real world
./gradlew runDatagen     # only if you changed ModDataGenerator or anything it emits
```

**Verify the artefact, not the exit code**, whenever you touch the wrapper, Loom or Gradle:

```bash
# Not iron-oak-*.jar — that glob also matches the sources jar and the pre-shadow
# "-dev-shadow" jar, and unzip -l over multiple archives prints "0 files", which reads
# like the empty-jar bug you are checking for. common/ has its own (empty) build/libs/
# too, so this is scoped to fabric/ specifically.
# find, not `ls | grep`: ls is aliased on some machines here and the alias breaks the glob.
unzip -l "$(find fabric/build/libs -name '*.jar' ! -name '*-sources.jar' ! -name '*-dev-shadow.jar')" | tail -1
```

Hundreds of files, not 2. It is the order of magnitude that carries the signal — the count
grows with every arm of the matrix.

`:fabric:runClient` is still the only gate for rendering, particles, sound and feel — not
unqualified `runClient`, which Gradle will happily satisfy with `common`'s own empty,
mod-free client instead. **If you did not launch the game, say so in the PR** instead of
implying you did.
