---
domain: Operations
domain_code: OPS
status: active
related:
  - ../../AGENTS.md
  - version-migration.md
  - orchestration.md
  - release.md
---

# Loaders and Version Lines

Which Minecraft versions Iron Oak ships, which loaders, and what that costs. Versions and
loaders are one decision, not two: Architectury is versioned per Minecraft release, and a
loader is paid for once per line.

Migration mechanics for a single line: [`version-migration.md`](version-migration.md).

---

## The decision — 2026-08-21

**Four game lines, three source trees, two loaders.**

| Line | Announced as | Branch | Loaders |
|---|---|---|---|
| 1.21.11 | 1.21.11 | `v1.21.11` | Fabric |
| 26.x | 26.1, 26.1.1, 26.1.2, **26.2** | `main` | Fabric, then NeoForge |
| 1.21.1 | 1.21, 1.21.1 | `v1.21.1` (#54) | Fabric **and** NeoForge |

Three trees for four lines, because **26.1 and 26.2 share one**. Reach: **~60 %
(conservative) to ~72 % (recent-window)** of Fabric demand, plus a NeoForge audience that
barely overlaps it. The range is the honest answer; a single number would be false precision.

**1.20.1 is excluded** — [below](#why-not-1201). **Forge is not available at all** —
[below](#forge-is-not-a-target-and-cannot-be).

---

## Branches and how work flows

`main` is the frontier. Every frozen line is a branch named `v<exact version>`, cut from that
version's release tag when `main` moves past it.

| Track | Branch | CI | Releases |
|---|---|---|---|
| 26.x | `main` | yes | yes |
| 1.21.11 | `v1.21.11` | yes | yes |
| 1.21.1 | `v1.21.1` (#54) | wired, waiting for the branch | once cut |
| 1.20.4 | — | — | archived |
| 1.19.x | `1.19` | — | archived |
| 1.18.2 | `v1.18.x` | — | archived |

- **Not `v1.21.x`.** The wildcard stopped working when this decision put two supported lines
  inside the 1.21 family; 1.21.1 and 1.21.11 have different vanilla API surfaces.
- **CI builds `main` plus every supported line and nothing else.** Both branch lists in
  `.github/workflows/main.yml` are that set. `v1.21.1` is declared before its branch exists —
  a `push` filter for a missing branch never fires, so the line is wired the moment #54 cuts
  it, and nobody has to remember two hand-synced lists.
- **A frozen line is never byte-identical to its release tag.** It carries at least the commit
  adding itself to its own CI lists, because a `push` trigger evaluates the workflow on the
  pushed ref.
- **Archived lines keep their published jars and history and get nothing else** — no CI, no
  releases, no backports. Dropped 2026-08-21.

### Implement forward, port backward

New work lands on `main`, then moves down the tracks. Never develop the same change twice in
parallel.

**A backport here is a hand-port, not a `git cherry-pick`**, because it crosses two boundaries:

1. **Renames** — `Identifier` on 26.x and 1.21.11 is `ResourceLocation` on 1.21.1;
   `ChunkSectionLayer` is `RenderType`.
2. **Build regime** — `main` is unobfuscated on JDK 25 with the plain Loom plugin; the 1.21.x
   lines are obfuscated on JDK 21 with `-remap`. Anything touching the build never
   cherry-picks at all.

So a change is not done everywhere because it merged to `main`. Budget a hand-port per line,
expect that a change written against 26.x occasionally has to be reworked or rejected on an
older line, and say in the PR which lines you reached.

---

## Reach

Measured against the Modrinth API on **2026-08-21**. Re-measure before the next planning
round; these have a shelf life of weeks.

**Do not plan from cumulative downloads.** By that measure the 1.21 family is 59 % and 26.2 is
5.8 %, which says "ship 1.20" — 1.20.1 has been accumulating since 2023 and 26.2 for months.
Cumulative downloads measure a version's history, not its present.

**Current demand** — newest settled `fabric-api` build per line, downloads ÷ age. Two age
floors, because a build published days ago is still absorbing its update spike:

| Line | ≥30 d | ≥14 d |
|---|---|---|
| **26.2** | 19.4 % | 24.2 % |
| **26.1** | 18.3 % | 12.8 % |
| 1.20.1 | 15.2 % | 10.6 % |
| **1.21.11** | 12.9 % | 25.4 % |
| **1.21.1** | 9.8 % | 9.9 % |
| 1.21.10 | 5.0 % | 3.5 % |
| 1.21.4 | 4.4 % | 3.1 % |

The floors disagree on ordering — 1.21.11 and 26.2 both got builds 24 days before measurement,
which the 14-day floor flatters — but agree on the set. Below 1.21.1 is rounding error.

| Selection | ≥30 d | ≥14 d |
|---|---|---|
| 1.21.11 + 26.1 + 26.2 | 50.6 % | 62.4 % |
| **+ 1.21.1 (chosen)** | **60.4 %** | **72.3 %** |
| + 1.20.1 as well | 75.6 % | 82.9 % |

**No three-line selection reaches 70 %.** That is why the fourth line exists.

**Ecosystem gravity** — share of the 1000 most recently updated mods per loader. This is the
only measure that separates the loaders:

| Version | Fabric | **NeoForge** |
|---|---|---|
| 1.21.1 | 62.3 % | **83.3 %** |
| 26.2 | 64.7 % | 43.8 % |
| 26.1.2 | 60.7 % | 47.4 % |
| 1.21.11 | 59.0 % | 38.4 % |
| 1.20.1 | 47.9 % | 55.7 % |

**The NeoForge column is the finding.** On Fabric 1.21.1 is a mediocre ~10 % of demand; on
NeoForge it is where nearly everyone is, at twice 26.2's share. So 1.21.1 both closes the gap
to 70 % and is the only NeoForge target worth building.

**Caveats.** Fabric API downloads are a proxy for players, not a count. The two floors differ
enough that "70 %" means "60s to low 70s". Measure B counts mods, not players. And this is
**Modrinth only** — CurseForge, where Iron Oak also publishes, skews older and more
Forge-ward and is unmeasured. If a firm reach target matters, measuring CurseForge beats
adding a line.

---

## Forge is not a target and cannot be

Architectury dropped it. Every live branch declares `platforms=fabric,neoforge`:

| Arch branch | Forge subproject |
|---|---|
| `1.21`, `1.21.11` | `//include("forge")` — commented out |
| `26.1`, `26.2` | absent |

`forge_version=51.0.0` still sits in the older branches' `gradle.properties` and configures a
subproject that is not in the build. Ignore it.

Quilt is also a non-target: Quilt loads Fabric mods, so those players are already reached.
#11 is the place for Quilt load failures.

---

## 26.1 rides along with 26.2 for free

**+12.8–18.3 % for roughly the cost of testing it.** Architectury's `26.1` and `26.2`
branches are one build regime — same `loom-no-remap` 1.17, same `options.release = 25`, same
platforms, identical Minecraft imports in the common registry classes, `RenderTypeRegistry`
absent from both.

The 26.1 → 26.2 delta is 27 commits concentrated in the **event system** (`EventPriority` and
`EventResultHolder` added, `Event`/`EventFactory` reworked) — which is why Architectury's major
went 20.0 → 21.0, and which **this mod does not use**.

So 26.1 is extra entries in `publish_game_versions` plus one `runClient` pass, not a port. It
belongs to #20 as an acceptance item.

> This is a verified exception for 26.1/26.2, **not** a general rule about adjacent versions.
> It does not hold for 1.21.10 → 1.21.11.

---

## 1.21.1 needs its own source tree

Not a cheap backport — a different vanilla API surface. Architectury's own repo proves it:
same file, same `officialMojangMappings()`, two branches.

```
CreativeTabRegistry.java @ 1.21     → net.minecraft.resources.ResourceLocation
CreativeTabRegistry.java @ 1.21.11  → net.minecraft.resources.Identifier
RenderTypeRegistry.java  @ 1.21     → net.minecraft.client.renderer.RenderType
RenderTypeRegistry.java  @ 26.1/2   → file does not exist
```

Iron Oak uses `Identifier` in **every** `init/` class and `ChunkSectionLayer` in
`IronOakClient`. Neither exists at 1.21.1. `ModItems`' `settings.setId(key)` is a third likely
case — **unverified**; resolve it against the jar with the `minecraft-fabric-lookup` skill.

Architectury's version ladder says the same: one major per Minecraft release — 13.0 (1.21),
14.0, 15.0, 16.1, 17.0, 18.0, 19.0 (1.21.11), 20.0 (26.1), 21.0 (26.2).

The upside: 1.21.1 is the **friendliest Architectury target** — Java 21, the mature
`dev.architectury.loom` 1.6, and `RenderTypeRegistry` still present, so `IronOakClient` ports
instead of disappearing. That plus its 83 % NeoForge share makes it the right **pilot** for
the loader conversion.

---

## What Architectury gives us, and what it does not

Iron Oak's Fabric coupling is thin — 15 `net.fabricmc` imports across five concerns — which
makes it a good candidate.

| Ours | Architectury equivalent | Status |
|---|---|---|
| `ModInitializer` / `ClientModInitializer` | platform entrypoints calling a common static init | mechanical |
| `FabricItemGroup` + `ItemGroupEvents` | `registry.CreativeTabRegistry` | on all four branches |
| `BlockEntityRendererRegistry` | `registry.client.rendering.BlockEntityRendererRegistry` | on all four branches |
| `BlockRenderLayerMap` | `RenderTypeRegistry` | 1.21/1.21.11 only; **gone on 26.x** |
| `FabricBlockEntityTypeBuilder` | vanilla `BlockEntityType.Builder` | verify against the jar |
| `Registry.register` in `init/` | `registry.registries.DeferredRegister` | **the real work** |

**The registration rewrite is the cost.** Architectury's NeoForge side registers via events, so
common code must use `DeferredRegister` — the pattern `AGENTS.md` and the `java` skill
currently forbid. Both change in the same PR as the conversion. It also breaks the documented
class-load order: `ModRecipes` registers in a `static {}` block, `ModBlocks`/`ModItems` return
live objects other `init/` classes read immediately, and every arm of the 6×3 matrix becomes a
`RegistrySupplier<Block>`. This is a rewrite of the init spine — all 18 arms or none.

**Two things it does not abstract at all**, both CI gates:

- **Datagen.** Zero hits across the whole `26.2` tree. `ModDataGenerator`, `ModModelGenerator`,
  `ModConfiguredFeatures` and `FabricDynamicRegistryProvider` stay Fabric-only. Run
  `runDatagen` in the Fabric subproject and share the committed `src/main/generated/`.
- **Gametests.** Also zero. `src/gametest/` is `fabric-gametest-api-v1`; NeoForge has its own
  framework. **`runGametest` would cover Fabric only**, so a NeoForge jar ships with layer-1
  unit tests plus a human `runClient` pass — a genuinely weaker gate, and any PR shipping one
  must say so.

**Lucky breaks:** the access widener is inert (one comment line), so there is nothing to
translate into a NeoForge Access Transformer — and #20 deletes it anyway. The mixin config is
empty.

**New user-facing dependency:** Architectury API, a required companion mod, LGPL-3 as a
separate jar (no friction with Iron Oak's all-rights-reserved licence). Its Fabric build
hard-depends on `fabric-api`, so nothing is lost there.

---

## Toolchain per line

| Line | Arch branch | Arch version | Loom plugin | Java |
|---|---|---|---|---|
| 1.21.1 | `1.21` | 13.0.11 | `dev.architectury.loom` 1.6 | 21 |
| 1.21.11 | `1.21.11` | 19.0.1 | `dev.architectury.loom` 1.13 | 21 |
| 26.1 | `26.1` | 20.0.12 | `dev.architectury.loom-no-remap` 1.17 | **25** |
| 26.2 | `26.2` | 21.0.7 | `dev.architectury.loom-no-remap` 1.17 | **25** |

`1.21` is the best-maintained older branch (last touched 2026-07-23, ahead of `1.21.4`,
`1.21.7`, `1.21.9`, `1.21.10` and `1.21.11`).

- **The remap/no-remap split mirrors ours.** Architectury Loom **replaces** Fabric Loom; it is
  not added alongside.
- **Java 25 on the 26.x line is not an Architectury cost.** Its 26.x branches compile at
  `options.release = 25` and a release-21 compiler cannot read those class files — but #20
  already moves this mod to JDK 25 for its own reasons. The requirements coincide.

---

## Why not 1.20.1

10.6–15.2 % of current Fabric demand and 55.7 % of recent NeoForge mods — real volume, still
excluded. It is three majors behind with no forward path, so supporting it is a permanent
branch rather than a step toward anything; it needs a fourth source tree with names older than
1.21.1's; and this project just spent a migration escaping the 1.20.4 line, dropped
2026-08-21.

If reach has to grow later, **measure CurseForge first** — that is unmeasured population, not
a fifth port.

---

## Ordering

Forced by two rules, not preference: a version bump is never a side effect (`AGENTS.md`), and
version migrations are serial (`orchestration.md`) because concurrent builds on two Minecraft
versions thrash the shared Loom cache. So this is a queue, not a fan-out.

1. **#52 — cut `v1.21.11` and build it in CI.** ✅ done. Had to land before `main` moved to
   26.2, or the 1.21.11 tree would exist only as a tag and every backport would start with
   archaeology.
2. **#20 — port `main` to 26.2.** The largest single line; all 52 compile errors are
   enumerated in the issue.
3. **26.1 announced alongside 26.2** — an acceptance item of #20.
4. **#54 — the 1.21.1 line**, cut from the 1.21.11 release tag and ported *down*.
5. **#21 — the Architectury/NeoForge conversion**, piloted on 1.21.1, then applied to 26.x.

4 and 5 are separate tickets on purpose: doing the `DeferredRegister` rewrite while also
chasing the down-port's renames means two moving targets in one PR. **Do the conversion once,
then template it** — converting three trees in parallel is how this becomes unbounded.

---

## The ongoing cost

- **3 source trees** — one moving, two maintenance.
- **Up to 6 published artefacts** (3 lines × 2 loaders, where both apply).
- **Cherry-picks that do not apply** between the 26.x and 1.21.1 trees.
- **A CI gate covering half the artefacts** — `runGametest` is Fabric-only.

#21 asks whether that is acceptable. As of 2026-08-21: **yes, for these four lines and these
two loaders** — and the number of lines is now capped. A fifth needs a new decision and a
fresh measurement, not an extrapolation from this one.
