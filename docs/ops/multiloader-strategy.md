---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-09-15
related:
  - multiloader.md
  - ../../AGENTS.md
  - orchestration.md
---

# How Iron Oak reaches more than one loader

The decision behind #74. `multiloader.md` says *which* versions and loaders we ship; this says
*how*, and why the obvious answer was wrong.

---

## The decision

**Shared source compiled into per-loader subprojects. No Architectury.**

```
common/     the mod. Vanilla API only. Not a library — its SOURCE is compiled into each
            loader project, so each copy sees that loader's API.
fabric/     entrypoints, registration, client bindings, datagen, gametests
neoforge/   entrypoints, registration, client bindings
forge/      later, if it earns its place — same shape
```

Each subproject produces a jar. Whether those are shipped separately or merged into one file
carrying all three manifests is a **packaging** question, decided after the split works.

---

## Why not Architectury

Two reasons, either sufficient.

**It cannot reach Forge.** Every live Architectury branch declares `platforms=fabric,neoforge`;
the Forge subprojects are commented out on `1.21`/`1.21.11` and absent on `26.x`. Its
`feature/lex_forge` branch was last touched **2023-09-22**. Forge itself is alive — LexForge
publishes through 26.2 — so this is Architectury's limit, not the ecosystem's.

**Nobody in our neighbourhood uses it.** Sample of 150 recently-updated Modrinth mods
supporting both Fabric and NeoForge, 2026-09-15:

| Approach | Count |
|---|---|
| no Architectury, one jar serving all loaders | 111 |
| no Architectury, separate jars per loader | 39 |
| Architectury | **0** |

Read that with its caveats: Architectury API can be bundled jar-in-jar and would then not show
as a dependency, and the sample is recency-weighted. It is not proof that Architectury is bad.
It is enough to stop calling it "the standard tooling", which is what #21 and `multiloader.md`
assumed.

The cost it would have added: Architectury API becomes a **required companion mod** for every
player.

---

## The variable that actually decides this

Not mod size. **How much of the mod touches loader API.**

| Mod | Loader-specific | Common | Technique |
|---|---|---|---|
| `sdlink` (MIT) | **3 classes**, two named `dummy/` | 115 | one shared source set, one jar, trivially |
| Seaworthy Boats | 3 entrypoints | 28, **compiled 3× into relocated packages** | real triplication |
| **Iron Oak** | **8 of ~20** — the whole `init/` registration spine, both client rendering classes, all datagen | the rest | — |

`sdlink` is a Discord bridge: it hooks server lifecycle and nothing else, so its loader surface
is almost zero and a single compile serves every loader. **That does not generalise to us.**
Iron Oak registers blocks, items and block entities, binds a renderer, and builds a creative
tab — all loader-specific.

Seaworthy Boats is the honest comparison, and it pays for the single jar by shipping its common
code three times in relocated packages (`…_common_fabric`, `…_common_forge`,
`…_common_neoforge`). That works, but the generator producing those variants is theirs, and
their licence is **All Rights Reserved** — readable for technique, not copyable. Same licence
we use.

---

## What is hard, and is hard under every strategy

**Registration — decided 2026-09-15: declare once, perform per loader.**

Fabric is `Registry.register`; Forge and NeoForge open their registries only during an event.
Two shapes were on the table:

| | Matrix written | Use sites | Cost |
|---|---|---|---|
| each loader in its own idiom | **twice**, two dialects | untouched | a new matrix arm touches both |
| **declare once, suppliers everywhere** ← chosen | **once**, in `common/` | **~58 gain `.get()`** | Fabric carries a pattern it does not need alone |

Chosen the second, because **Forge is a goal**: at three platforms, writing the 6×3 matrix once
is worth more than sparing the use sites, and the all-or-nothing matrix rule makes a duplicated
matrix the more dangerous shape.

Consequences to plan for:

- An entry is a supplier, read with `.get()` — about 58 places.
- `ModRecipes`' `static {}` block goes. It registers at class load, which is exactly the timing
  the other loaders do not allow.
- `BlockBehaviour.Properties.ofLegacyCopy(copyFrom)` needs `copyFrom` as a real block. Fine for
  vanilla sources; a trap wherever a mod block copies another mod block.
- The rule this overturns lived in **four** places — `AGENTS.md`, `docs/strategy/java.md`, and
  twice in the `java` skill. All four changed together; a worker reading one of them stale gets
  a contradiction it cannot resolve.

**What is not decided here** is the mechanism `common/` uses to reach each loader's registrar.
That is implementation, and the worker with the code in front of it will choose better than this
document can.

**Datagen and gametests stay Fabric-only.** Architectury abstracts neither, and neither does
this. `runDatagen` output is committed and shared with every platform; `runGametest` covers the
Fabric jar. A NeoForge jar ships with layer-1 unit tests plus a human `runClient` pass — a
genuinely weaker gate, and any PR shipping one says so.

---

## What this means for #71

**#71's `common/` + `fabric/` split is the right shape and stays usable.** What goes is the
Architectury *plugin and API*, not the structure. Adopting it means keeping the subprojects and
reverting the dependency — cheaper than starting over.

Its other findings survive the change intact and are worth carrying forward:

- `./gradlew runClient` becomes ambiguous once `common` exists as a Loom-enabled subproject —
  it launches `common`'s empty, mod-free client. Use `:fabric:runClient`.
- `RequirementCatalogue` hard-coded `src/<layer>/java`; that path moves.
- The `.gitignore` datagen-cache entry is anchored to the old path.
- The jar-verification command needs a path qualifier once several subprojects produce jars.

---

## Quilt is free, and we are probably already leaving it on the table

**Quilt reads `fabric.mod.json`.** It needs no manifest of its own, no subproject and no code.
Verified on a shipped jar: Seaworthy Boats is listed for Quilt on Modrinth and contains **no
`quilt.mod.json` and zero Quilt classes** — the Fabric jar simply loads.

It is also current: Quilt's meta knows game versions up to `26.3-pre-2` and loader
`0.31.0-beta.4`, so it is ahead of everything we ship. And it is standard practice — **81 of
100** sampled Fabric+NeoForge mods tag Quilt.

We do not. `modLoaders.add("fabric")` is the whole declaration in `build.gradle`.

**The failure mode is metadata, not the loader.** #11 — our only Quilt report — is a player on
Quilt 0.17.4 whose log said the mod wanted 1.19 while the Modrinth file was marked
1.18-compatible. That is a version-declaration mismatch, the same class of bug as the unbounded
`"minecraft": ">=26.1"` corrected before the 26.2 release. Quilt surfaces it earlier than Fabric
because it validates harder.

So: adding Quilt is one line, and it is worth **verifying rather than asserting** — the
ride-along recipe in [`multiloader.md`](multiloader.md#how-to-verify-a-ride-along-line) applies
unchanged. Run the shipped jar on a Quilt instance once; if it loads, tick the box.

---

## One jar for every loader: yes, and the cost depends on the line

Three manifests coexist in one file because each loader reads its own and ignores the others —
`fabric.mod.json`, `META-INF/mods.toml`, `META-INF/neoforge.mods.toml`. 111 of 150 sampled mods
ship exactly that, and it is plainly nicer for players than three downloads.

What it costs is **not** a style question. It is set by whether the Minecraft version is
obfuscated, and we ship both kinds. Verified by disassembling Seaworthy Boats' shipped jars:

**Obfuscated lines (`v1.21.11`, `v1.21.1`) — the common code must exist twice.** Fabric runs on
**intermediary** names, Forge and NeoForge on **official** ones. The same class file cannot
serve both:

```
1.21.1 jar, same class in three packages:
  _common_fabric     3297 B   class_1268, method_11657        <- intermediary
  _common_neoforge   4312 B   net/minecraft/core/BlockPos     <- official
  _common_forge      4294 B   net/minecraft/core/BlockPos     <- official
```

So on these lines the duplication is **mandatory**, not stylistic — it is remapping, and it is
why the relocated `…_common_<loader>` packages exist at all.

**Unobfuscated lines (`main`, 26.x) — it is nearly free.** Every loader uses official names, so
the copies are the same class:

```
26.2 jar, same class in two packages:
  _common_fabric     4288 B   net/minecraft/core/BlockPos
  _common_neoforge   4300 B   net/minecraft/core/BlockPos     <- 12 B apart: the package name
```

One compile of `common/` can serve all loaders there; only the manifests and the thin
per-loader layers differ.

**Consequence for us:** a single jar is cheap on `main` and needs the duplication machinery on
the 1.21.x lines. That is an argument for doing the split first and the merge per line
afterwards — and for not treating "one jar" as one decision. It is two, and `main` is the easy
one.

---

## Open, deliberately

**Packaging** — separate jars per loader, or one jar carrying `fabric.mod.json`,
`META-INF/mods.toml` and `META-INF/neoforge.mods.toml`. 111 of 150 sampled mods ship one file,
and it is plainly nicer for players, but it is a build concern that can be decided once the
split compiles on two loaders. Do not let it block the split.

**Forge** — the stated goal, and reachable under this strategy where it was not under
Architectury. Still worth deciding on evidence *after* NeoForge works: among recently-updated
Forge mods, 1.20.1 is 83 % while 26.2 is 46 %, so Forge's weight sits on versions we do not
ship. `common/` serves a third platform as easily as a second, so the decision costs nothing to
defer.

**Toolchain** — `sdlink` uses Unimined rather than Loom. Not adopted: this repo's `build.gradle`
carries four load-bearing pieces of reasoning (`runDatagen` inheriting `client`, `runGametest`
with no main class, the `testsupport` source set, the gametest `mods` registration), and
swapping the toolchain re-opens all of them. Revisit only if per-loader Loom setups prove
unworkable.
