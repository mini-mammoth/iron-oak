---
name: minecraft-fabric-lookup
description: Resolve any Minecraft or Fabric API question for this mod against the actual jar instead of from memory. Use when a build error says a class or method cannot be found, when porting to a new Minecraft version, when you need the exact signature of a vanilla class, when deciding how to use a vanilla API, when picking toolchain versions (Loom, Gradle, Java, Fabric API), or when a data pack / resource change might be silently ignored. Triggers on "cannot find symbol" against net.minecraft, "does not override a method from a supertype", any Minecraft version bump, and any question of the form "what is X called now" or "how do I do X in Fabric".
---

# Minecraft / Fabric: look it up, don't remember it

**Minecraft has no API stability guarantee.** Classes are renamed, methods change
signature, and constructors gain required arguments between point releases. Anything you
"know" about the Minecraft API — from training, from a tutorial, from a cheatsheet, from
another mod — is a guess about a specific version, and this mod does not stay on one
version.

So: **never answer a Minecraft API question from memory, and never copy a snippet from a
tutorial without checking it.** Resolve it against the jar Loom actually put on the
classpath. It takes seconds and it is never wrong.

Two things make this concrete:

- The mod's own history. `ResourceLocation` became `Identifier`; `Item.Settings` gained a
  required registry key; `AbstractCookingRecipe` dropped its `RecipeType` constructor
  argument; `EnchantmentHelper.hasFrostWalker` was deleted. Every one of those would have
  been an invented workaround if it had been answered from memory.
- A widely-installed third-party Minecraft skill was evaluated for this repo and rejected
  for exactly this reason: its version table said 1.21.x, but its registration examples
  were 1.20.x code that throws on the version it claimed to target. It encoded facts
  instead of a method, so it rotted.

Everything below is method, not facts. It stays correct as the mod moves.

---

## The tool

`scripts/mcfind.sh`, run from the repo root. It reads `minecraft_version` from
`gradle.properties` and finds the matching mapped jar itself, so it follows whatever
branch you are on with no arguments and no configuration.

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms   # whatever this version needs
S=.claude/skills/minecraft-fabric-lookup/scripts/mcfind.sh

$S class ResourceLocation Identifier      # which package is it in now? does it still exist?
$S sig 'net.minecraft.world.item.Item$Properties'   # exact signatures
$S pkg net.minecraft.world.item.crafting  # what lives in this package?
$S data data/minecraft/tags               # what data paths does vanilla itself use?
$S jar                                    # which jar am I looking at
```

If it says there is no mapped jar, run `./gradlew build` once so Loom sets Minecraft up.

---

## The five questions, and how to answer each

### 1. "Cannot find symbol" — was it renamed, or removed?

```bash
$S class TheClassName
```

`NOT FOUND` means renamed or deleted, not that you mistyped. Guess the new name from what
it does and check that too — Mojang's names are descriptive, and they sometimes converge
on the community name (`ResourceLocation` → `Identifier`). Search the package it ought to
live in:

```bash
$S pkg net.minecraft.world.item.crafting | grep -i cook
```

### 2. "Does not override a method from a supertype" — what is the signature now?

```bash
$S sig net.minecraft.world.level.block.state.BlockBehaviour
```

Read the real parameter list. Hooks gain parameters (`entityInside` gained an
`InsideBlockEffectApplier`), narrow their types (`Level` → `ServerLevel`), or are replaced
by a differently-named hook (`onRemove` → `affectNeighborsAfterRemoval`).

**A replacement hook may not run at the same moment as the one it replaced.** That is a
behaviour change hiding inside a compile fix — `affectNeighborsAfterRemoval` runs *after*
the block entity is gone, so anything that needed the block entity had to move to a
pre-removal hook. When you swap hooks, ask when the new one fires.

### 3. "How am I supposed to use this?" — read vanilla's own answer

Signatures tell you what compiles, not what is correct. For that, read how vanilla does
the nearest equivalent thing:

```bash
./gradlew genSources          # once per version; takes a few minutes
```

Then read the vanilla class that solves your problem, out of the sources jar:

```bash
SRC=$(find .gradle/loom-cache ~/.gradle/caches/fabric-loom -name "*sources*.jar" -path "*minecraft*" | head -1)
unzip -p "$SRC" net/minecraft/world/level/block/CampfireBlock.java
```

Pick the closest vanilla analogue and copy its shape. For this mod that is usually:

| Our thing | Vanilla analogue |
|---|---|
| Fire bowl (block that cooks items) | `CampfireBlock` |
| `FireBowlEntity` | `CampfireBlockEntity` |
| `FireBowlRenderer` | `CampfireRenderer` |
| `BurningRecipe` / `WashingRecipe` | `SmeltingRecipe`, `CampfireCookingRecipe` |
| Infused bone meal | `BoneMealItem` |
| Block/item registration | whatever `Blocks` / `Items` do |

**Do not read decompiled sources to browse.** They are enormous and will eat your context
window. Extract the one class you need and read that.

### 4. "Which toolchain versions?" — copy Fabric's own tested combination

Do not assemble Loom + Gradle + Java + Fabric API by hand. `FabricMC/fabric-example-mod`
has **a branch per Minecraft version**, and it is the combination upstream actually tests:

```bash
git clone --depth 1 -b <mc-version> https://github.com/FabricMC/fabric-example-mod
cat fabric-example-mod/gradle.properties
grep -E "id '|release|VERSION_|mappings|implementation" fabric-example-mod/build.gradle
grep distributionUrl fabric-example-mod/gradle/wrapper/gradle-wrapper.properties
```

That branch tells you the Java version, the Gradle version, the Loom plugin **id** (it is
not the same across the obfuscated/unobfuscated split), whether mod dependencies use
`modImplementation` or `implementation`, and whether there is a `mappings` line at all.

For what actually exists, query the registries rather than trusting a blog post:

```bash
curl -s https://meta.fabricmc.net/v2/versions/loader | head -c 300
curl -s https://maven.fabricmc.net/net/fabricmc/fabric-api/fabric-api/maven-metadata.xml | grep -o "<version>[^<]*</version>" | tail
curl -s https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml | grep -o "<version>[^<]*</version>" | tail
```

Java version changes must land in **three** places or the build lies to you:
`build.gradle` (`options.release`, `sourceCompatibility`/`targetCompatibility`),
`fabric.mod.json` (`"java"`), and `iron-oak.mixins.json` (`compatibilityLevel`).

### 5. "Will this resource change be ignored?" — diff against vanilla's own data

**This is the dangerous class of change, because it does not fail the build.** Data pack
directory names and JSON shapes change between versions. Get one wrong and everything
compiles, the jar builds, CI is green, and in-game the recipe simply does not exist. The
tests help only where they name the path: `ModRecipesTest` reads
`data/iron_oak/recipe/*.json` off the classpath and `RegistryAssetsTest` walks every
registered id's asset files, so a rename away from either fails the build. A directory
neither test names goes silent, and nothing catches that but playing.

Vanilla ships its own data pack inside the jar, so it is the authority on both:

```bash
# directory names — ours must match vanilla's
$S data data/minecraft | cut -d/ -f3 | sort -u
$S data data/minecraft/tags | cut -d/ -f4 | sort -u

# JSON shape — read a vanilla file of the same kind you are writing
JAR=$($S jar); unzip -p "$JAR" data/minecraft/recipe/charcoal.json
```

Then compare against `src/main/resources/data/`. Recipes, loot tables, tags and the
worldgen output are all worth checking after any version bump.

---

## Rules

- **Answer from the jar, not from memory.** If you cannot show where you got a signature,
  you do not know it.
- **State which version you checked.** "In 1.21.11, `Item.Properties` requires `setId`" is
  useful. "`Item.Properties` requires `setId`" will be wrong eventually.
- **A missing API is a real finding, not a failure.** If the method you need does not
  exist in the target version, report that. Do not invent a workaround around a removed
  method — it will compile and misbehave.
- **Never edit `src/main/generated/`.** It is datagen output. Change the provider and run
  `./gradlew runDatagen`.
- **Compiling is not working.** Two test layers exist — `./gradlew build` and `./gradlew
  runGametest` — and between them they cover ids, committed resources, the 6×3 matrix and
  the fire bowl's ticking. They see nothing a player looks at. Rendering, particles, sound
  and feel still need `./gradlew runClient` and an actual observation before you claim
  they work. See `AGENTS.md` and `docs/strategy/testing.md`.

## Where else to look

- Fabric docs — https://docs.fabricmc.net/ (targets the current version; check which)
- Mapping lookup across versions — https://mappings.dev
- Version/dependency picker — https://fabricmc.net/develop
- This mod's migration history and its gates — `docs/ops/version-migration.md`
