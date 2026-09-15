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

**Registration.** Fabric is `Registry.register`; Forge and NeoForge are `DeferredRegister`.
Every arm of the 6×3 matrix becomes a supplier rather than a live object, and the class-load
order that `ModRecipes`' `static {}` block and `ModBlocks`/`ModItems` rely on has to be rebuilt.

No strategy avoids this. Architectury would have imposed `DeferredRegister` everywhere;
per-loader subprojects let Fabric keep `Registry.register` and NeoForge use `DeferredRegister`,
at the cost of two registration layers instead of one.

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
