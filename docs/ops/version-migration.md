---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-08-20
related:
  - ../../AGENTS.md
  - orchestration.md
  - orca-progress-loop.md
---

# Version Migration — 1.20.4 → 26.2

The mod currently targets **Minecraft 1.20.4** (`mod_version=1.2.1+1.20.4`). The goal is
**26.2**, with a **1.21.11 release tagged on the way**.

This document is the plan and the gate list. It is process policy, not a task briefing —
workers get scoped tickets, not this file.

---

## Why this is three stages and not one bump

Two ecosystem facts decide the shape of the work:

1. **Minecraft 26.1 and later are unobfuscated.** Yarn and Intermediary stopped being
   updated after **1.21.11** — the last yarn artifact on `maven.fabricmc.net` is
   `1.21.11+build.6` (published 2026-05-27), and there is no `26.x` yarn at all. This mod
   uses yarn today.
2. **Fabric API renamed its own surface to official names in 26.1.** Not backwards
   compatible.

Fabric's own guidance is explicit: **migrate the mappings before bumping the Minecraft
version.**

That fixes the order, and it is stricter than it first looks: `migrateMappings` rewrites
**source**, so it needs a tree that resolves. Run it after a four-release version bump and
it has nothing to work with. So the mapping migration goes **first, on 1.20.4** — where
the tree still compiles and a green build proves the mapping change alone worked. Only
then does the Minecraft version move.

The stage numbering below reflects that: mappings, then version, then 26.2. It also happens
to be free reach — 1.21.11 is a version worth shipping (see below), and we pass through it
anyway.

### Why 26.2 and not stopping at 1.21.11

Measured on 2026-08-20 via the Modrinth API:

- Among the **1000 most recently updated Fabric mods**, 26.2 is the single
  most-targeted version (661 of 1000), ahead of 1.21.1 (627) and 1.21.11 (593). 1.20.4 —
  where we are — is at 274 and falling.
- By Fabric API downloads per version family: 1.21.2–8 19.0 %, **1.21.9–11 18.9 %**,
  **26.1.x 18.5 %**, 1.20.1 14.6 %, 1.21.1 13.7 %, 26.2 5.3 %. The 26.x family is ~24 %
  combined and only months old; 1.20.4 is ~2 %.

So 1.21.11 is worth a release (18.9 % family share) and 26.x is where the ecosystem is
going. Stopping at 1.21.11 would mean doing the mapping migration again later anyway.

Numbers are cumulative downloads and therefore favour older versions — re-measure before
the next planning round rather than trusting these.

---

## Target versions

| Stage | Minecraft | Mappings | Loader | Fabric API | Java |
|-------|-----------|----------|--------|-----------|------|
| now | 1.20.4 | yarn 1.20.4+build.3 | 0.15.7 | 0.96.4+1.20.4 | 17 |
| 1 | **1.21.11** | yarn 1.21.11+build.6 | 0.19.3 | 0.141.6+1.21.11 | 21 |
| 2 | 1.21.11 | **Mojang official** | 0.19.3 | 0.141.6+1.21.11 | 21 |
| 3 | **26.2** | Mojang official | 0.19.3 | 0.158.0+26.2 | 21 |

Loom must be **1.13 or newer** for `migrateMappings`; the newest stable line is
`1.17.19` (`1.18.0` is alpha only). Verify against
`https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml`
before pinning — these move.

**Java goes 17 → 21.** Update all three places: `build.gradle` (`targetJavaVersion`,
`options.release`, `sourceCompatibility`/`targetCompatibility`), `fabric.mod.json`
(`"java": ">=21"`), and `iron-oak.mixins.json` (`compatibilityLevel: "JAVA_21"`).

---

## Stage 1 — yarn → Mojang official mappings, still on 1.20.4

Mechanical, and mostly done by Loom. Goes first, while the tree still compiles on 1.20.4 — that way only the mapping set changes and a green build is proof.

```bash
# 1. Back up. The task rewrites your sources in place.
git switch -c migration/mojmap

# 2. Run the migration BEFORE editing build.gradle or gradle.properties.
#    Older Loom writes to --output instead of overwriting in place; check
#    `gradlew help --task migrateMappings` for which flags your Loom has.
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew migrateMappings --mappings "net.minecraft:mappings:1.20.4" \
  --output remappedSrc

# 3. Now switch the mappings dependency in build.gradle:
#      -  mappings "net.fabricmc:yarn:${project.yarn_mappings}:v2"
#      +  mappings loom.officialMojangMappings()
#    and drop yarn_mappings from gradle.properties.

# 4. Rebuild and clean up what the task could not translate.
./gradlew --refresh-dependencies build
```

Caveats, from Fabric's own porting docs:

- Loom **1.13+** is what the porting guide says; the task itself is much older and has
  worked on Loom 1.5. Check what your Loom offers before assuming you must bump it first.
- Do **not** touch `gradle.properties` or `build.gradle` until after the task has run.
- The task does not translate Kotlin — irrelevant here, this mod is pure Java.
- It leaves behind what it cannot resolve: string-literal class references, and the
  access widener, which is **not** source code and must be translated by hand.
  `iron_oak.accesswidener` names `net/minecraft/recipe/CookingRecipeSerializer` in yarn
  form; under Mojmap that path changes. Use https://mappings.dev to translate it.
- Comments and identifiers naming yarn types (`Identifier`, `AbstractBlock`) still read as
  yarn afterwards. Renaming them is cosmetic; do it in a separate commit so the mechanical
  diff stays reviewable.

Acceptance: build green, **and** the same in-game loop as stage 1. A mapping migration
that compiles can still have swapped two same-signature methods.

Nothing is released at the end of this stage: it is a refactor with no behaviour change,
and the version is still 1.20.4. The 1.21.11 release tag comes after stage 2.

---

## Stage 2 — 1.20.4 → 1.21.11, on Mojang mappings

The largest stage by far. Four Minecraft releases' worth of breaking change, and the mod
touches most of the areas that changed.

**Known breaking areas, in the order they will bite.** Treat this as a map of where to
look, not as a verified changelog — confirm each against the compiler and the Fabric docs
for the target version.

| Area | What changes | Files |
|---|---|---|
| Identifiers | `new Identifier(a, b)` → `Identifier.of(a, b)`. ~200 call sites. | `init/ModItems`, `init/ModBlocks`, `init/ModRecipes` |
| Settings objects | `FabricItemSettings` / `FabricBlockSettings` are gone — use vanilla `Item.Settings` / `AbstractBlock.Settings` (`.copyOf(...)`, `.requiresTool()` etc. move accordingly). | `init/ModItems`, `init/ModBlocks` |
| Registry keys in settings | From 1.21.2 an item's and block's settings must carry their own registry key. The idiomatic shape becomes a `register(name, factory, settings)` helper rather than static construction followed by `Registry.register`. **This restructures both registration classes**, it is not a find-and-replace. | `init/ModItems`, `init/ModBlocks` |
| Data components | 1.20.5 replaced NBT-based `ItemStack` data with components. Relevant only where the mod reads/writes stack data. | `FireBowlEntity`, `helper/ImplementedInventory` |
| Recipes | `AbstractCookingRecipe`'s constructor and the serializer shape both changed; `createIcon` is gone; serializers moved to codec pairs. `BurningRecipe`/`WashingRecipe` subclass `AbstractCookingRecipe` and register via `CookingRecipeSerializer`, so both are affected. **Expect this to be the hardest file in the migration.** | `BurningRecipe`, `WashingRecipe`, `init/ModRecipes` |
| Access widener | Widens `CookingRecipeSerializer$RecipeFactory`. If that class or nested type no longer exists, the widener fails the build — and it may simply no longer be needed. | `iron_oak.accesswidener` |
| Resource directories | `loot_tables` → `loot_table`, `tags/blocks` → `tags/block`, `tags/items` → `tags/item`, `recipes` → `recipe`. Directory renames, silently ignored if missed — the mod loads and the recipes do not exist. | `resources/data/**` |
| Pack format | `pack.mcmeta`/`fabric.mod.json` pack version bumps. | `resources/` |
| Datagen | Provider constructor signatures and the `RegistryWrapper` plumbing changed more than once in this range. | `init/ModDataGenerator`, `init/ModConfiguredFeatures` |

### The trap in this stage

**A missed resource-directory rename does not fail the build.** Everything compiles, the
jar builds, CI is green — and in-game the recipes, loot tables or tags are simply absent.
There is no test suite to catch it. So stage 1's acceptance is **not** a green build:

- [ ] `./gradlew build` green (Linux + Windows via CI)
- [ ] `./gradlew runDatagen` produces no unexpected diff
- [ ] `./gradlew runClient`, and in-game: craft infused bone meal, infuse a sapling, grow
      a tree, burn a log in a fire bowl, wash the ash, craft a raw ore. **The whole loop
      from `README.md`, for at least one metal.** Anything less does not verify the
      migration.
- [ ] All 18 log/sapling pairs present in the creative tab (the 6×3 matrix)

### Suggested ticket split

`area:build` first and alone (it is a barrier — see
[`orca-progress-loop.md`](orca-progress-loop.md)), then the code areas. Do **not** try to
land stage 1 as one PR: it will not be reviewable, and a compaction mid-way loses
everything.

1. `area:build` — version bump in `gradle.properties`, Loom + Gradle wrapper, Java 21 in
   all three places, CI modernised. **Will not compile at the end of this ticket** — that
   is expected and must be stated in the ticket, or a worker will try to fix the whole mod
   inside it.
2. `area:items` + `area:blocks` — settings objects, identifiers, the registry-key
   restructure.
3. `area:recipes` — the cooking-recipe rewrite and the access widener.
4. `area:assets` — resource directory renames and pack formats.
5. `area:datagen` — provider signatures, then regenerate and commit.
6. `area:client` — renderer API drift.

Tickets 2–6 each depend on 1. Because the tree does not compile until the last of them
lands, they are **not** independently mergeable — this is the one case in this repo where
a shared integration branch beats PR-per-ticket. Cut them as commits on one
`migration/1.21.11` branch, in order, and open a single PR for stage 1.

That is a deliberate exception to "1 PR = 1 issue", and the reason is worth stating: with
no compiling intermediate state there is nothing for CI to verify per-ticket, so the usual
benefit of separate PRs is absent while the rebase cost is not.

---

## Stage 3 — 1.21.11 → 26.2

- Bump `minecraft_version` to `26.2`, `fabric_version` to `0.158.0+26.2`.
- Fabric API's own classes were renamed to official names in 26.1. Every
  `net.fabricmc.fabric.api.*` import is a candidate.
- Vanilla changes between 1.21.11 and 26.2 on top of that.
- Fabric's docs now target 26.2 and show Mojmap throughout, so from here the docs and the
  code finally speak the same language: https://docs.fabricmc.net/

Acceptance: as stage 1, plus a check that the mod loads on a **dedicated server**
(`./gradlew runServer`) — `environment` is `*` in `fabric.mod.json`, so a client-only
regression is a real risk and the fire bowl renderer is the obvious place for one.

---

## What happens to the old versions

`main` moves to the newest supported version. The 1.20.4 line becomes a branch if it is
worth keeping — that is a **human decision** and is not made here. Existing branches:
`v1.18.x`, `1.19`, plus unmerged `restone_leaves`.

The open issues against old versions (#11 Quilt on 1.18.2, #15 washing on an unstated
version) need a version decision before they are workable. Both are
`status:needs-human` until then.

A **1.21.1 backport** is the obvious candidate for more reach afterwards (13.7 % of
downloads, the biggest single modpack anchor) — but it is a second maintained line, not a
free release, and it is out of scope here.

---

## Gates

| Gate | When | Who |
|---|---|---|
| Plan approved | before the first `area:build` ticket | human |
| Stage 1 build green | before starting stage 2 | orchestrator — a mapping-only change must not alter behaviour |
| Stage 2 in-game loop verified | before merging stage 2 | human, on the worker's `runClient` evidence |
| 1.21.11 release tagged | after stage 2 | human |
| Stage 3 started | after 1.21.11 is released | human |
| Old-version branch policy | before `main` moves | human |

---

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial plan. Target versions and the yarn-discontinuation constraint verified against `maven.fabricmc.net` and the Fabric porting docs; reach figures measured via the Modrinth API. |

*Last updated: 2026-08-20*
