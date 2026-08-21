---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-08-21
version: 1
related:
  - ../../AGENTS.md
  - version-migration.md
  - orchestration.md
  - release.md
---

# Loaders and Version Lines — what Iron Oak ships, and why

This file answers two questions that keep being asked separately and have to be answered
together:

1. **Which Minecraft versions do we support?**
2. **Which mod loaders do we support?**

They are one question because the loader abstraction we would use — Architectury — is
versioned per Minecraft release, and because the cost of a loader is paid once per version
line. Deciding them apart is how you end up with a matrix nobody can build.

This is a **strategy and decision record**. It is not a task briefing; workers get scoped
tickets. The migration mechanics for a single line live in
[`version-migration.md`](version-migration.md).

---

## The decision — 2026-08-21

**Four game lines, three source trees, two loaders.**

| # | Line | Game versions announced | Source tree | Loaders |
|---|---|---|---|---|
| 1 | **1.21.11** | 1.21.11 | `v1.21.x` branch | Fabric |
| 2 | **26.x** | 26.1, 26.1.1, 26.1.2, **26.2** | `main` | Fabric, then NeoForge |
| 3 | **1.21.1** | 1.21, 1.21.1 | `v1.21.1.x` branch | Fabric **and** NeoForge |

Three trees, four lines — because **26.1 and 26.2 share one tree** (evidence
[below](#261-rides-along-with-262-for-free)) and are two entries in
`publish_game_versions`, not two ports.

**1.20.1 is deliberately excluded** despite being worth 10–15 % — see
[Why not 1.20.1](#why-not-1201).

**Forge is not a target and cannot be** — see [Forge is already gone](#forge-is-already-gone).

Expected reach: **~60 % (conservative) to ~72 % (recent-window)** of Fabric demand, plus a
NeoForge audience that barely overlaps it. The range is the honest answer; a single number
here would be false precision.

---

## How reach was measured

Measured **2026-08-21** against the Modrinth API. The numbers in
[`version-migration.md`](version-migration.md) are from 2026-08-20 and use *cumulative*
downloads; that file already warns they favour older versions. These supersede them for
planning.

### Why cumulative downloads mislead

Cumulative Fabric API downloads by family, for the record:

| Family | Cumulative | Share |
|---|---|---|
| 1.21 | 139.8 M | 59.0 % |
| 1.20 | 52.0 M | 22.0 % |
| 26.1 | 18.4 M | 7.8 % |
| 26.2 | 13.7 M | 5.8 % |

That table says "ship 1.20" — and it is wrong, because 1.20.1 has been accumulating since
2023 and 26.2 for months. **Cumulative downloads measure a version's history, not its
present.** Do not plan from them.

### Measure A — current demand (download rate)

For each line, take the newest `fabric-api` build published for it and divide its downloads
by its age in days. Each line is then measured over its own settled window. Two age floors
are shown because a build published days ago is still absorbing the update spike, and the
floor changes the ordering.

| Line | ≥30 d floor | ≥14 d floor |
|---|---|---|
| **26.2** | **19.4 %** | 24.2 % |
| **26.1** | **18.3 %** | 12.8 % |
| 1.20.1 | 15.2 % | 10.6 % |
| **1.21.11** | **12.9 %** | 25.4 % |
| **1.21.1** | **9.8 %** | 9.9 % |
| 1.21.10 | 5.0 % | 3.5 % |
| 1.21.4 | 4.4 % | 3.1 % |
| 1.21.8 | 3.1 % | 2.2 % |
| 1.21 | 3.1 % | 2.2 % |
| 1.21.5 | 2.1 % | 1.5 % |

The two floors disagree on *ordering* — 1.21.11 and 26.2 both received builds 24 days
before the measurement, so the 14-day floor flatters both — but they agree on the *set*.
Everything below 1.21.1 is rounding error.

Cumulative totals for the chosen lines:

| Selection | ≥30 d | ≥14 d |
|---|---|---|
| 1.21.11 + 26.1 + 26.2 | 50.6 % | 62.4 % |
| **+ 1.21.1 (chosen)** | **60.4 %** | **72.3 %** |
| + 1.20.1 as well | 75.6 % | 82.9 % |

**No three-line selection reaches 70 %.** That is the whole reason the fourth line exists.

### Measure B — ecosystem gravity (where mod authors are)

Of the 1000 most recently updated mods per loader, the share targeting each version. This
measures where the ecosystem is, which is a leading indicator of where players go — and it
is the only measure that separates the loaders.

| Version | Fabric | **NeoForge** |
|---|---|---|
| 1.21.1 | 62.3 % | **83.3 %** |
| 26.2 | 64.7 % | 43.8 % |
| 26.1.2 | 60.7 % | 47.4 % |
| 1.21.11 | 59.0 % | 38.4 % |
| 1.20.1 | 47.9 % | 55.7 % |
| 1.21.10 | 44.5 % | 32.6 % |
| 1.21.8 | 43.8 % | 36.7 % |

**The NeoForge column is the finding.** On Fabric, 1.21.1 is a mediocre ~10 % of demand. On
NeoForge it is where essentially everyone is — 83.3 %, twice 26.2's share. So 1.21.1 does
double duty: it is the line that closes the gap to 70 %, *and* it is the only NeoForge
target worth building.

### Caveats — read these before quoting the numbers

- Fabric API downloads are a **proxy for players, not a count of them**. A server operator
  and a modpack each pull it once.
- The ≥30 d and ≥14 d floors differ enough that "70 %" should be read as "somewhere in the
  60s to low 70s", not as a threshold that was cleared.
- **Modrinth only.** Iron Oak also publishes to CurseForge, whose audience skews older and
  more Forge-ward. That population is unmeasured here. If a firm reach target matters,
  measuring CurseForge is the next thing to do, not adding another line.
- Measure B counts *mods*, not players, and a mod supporting a version says nothing about
  how many people play it.
- Re-measure before the next planning round. These numbers have a shelf life of weeks.

---

## Forge is already gone

The original ask was "Forge / NeoForge". **Forge is not available as a target**, and this is
not a decision we get to make — Architectury dropped it.

Every live Architectury branch declares `platforms=fabric,neoforge`:

| Arch branch | `platforms` | Forge subproject |
|---|---|---|
| `1.21` | `fabric,neoforge` | `//include("forge")` — commented out |
| `1.21.11` | `fabric,neoforge` | `//include("forge")` — commented out |
| `26.1` | `fabric,neoforge` | absent |
| `26.2` | `fabric,neoforge` | absent |

`forge_version=51.0.0` still sits in the older branches' `gradle.properties`, which is
misleading: the subproject it configures is not included in the build. On `26.2` the
directory is gone entirely.

This resolves open question 1 of #21 in the direction that issue already recommended, but
for a firmer reason than "Forge is legacy": there is no supported path.

Quilt remains a non-target — Quilt loads Fabric mods, so those players are already reached.
#11 is the place to settle Quilt load failures.

---

## 26.1 rides along with 26.2 for free

This is the cheapest reach on the board: **+12.8–18.3 % for approximately the cost of
testing it.**

Architectury's `26.1` and `26.2` branches are the same build regime:

| | `26.1` | `26.2` |
|---|---|---|
| `platforms` | `fabric,neoforge` | `fabric,neoforge` |
| Loom plugin | `dev.architectury.loom-no-remap` 1.17-SNAPSHOT | same |
| `options.release` | 25 | 25 |
| `CreativeTabRegistry` Minecraft imports | identical | identical |
| `RenderTypeRegistry` | absent | absent |

The 26.1 → 26.2 delta is 27 commits concentrated in the **event system** —
`EventPriority` and `EventResultHolder` added, `Event`/`EventFactory` reworked — which is
why Architectury's own major went 20.0 → 21.0. **Iron Oak uses none of it.**

So 26.1 is not a fourth port. It is extra entries in `publish_game_versions` on the same
branch, plus a `runClient` pass on 26.1 to prove it. Treat it as an acceptance item of the
26.2 work (#20), not a separate migration.

> The same is **not** true of 1.21.10 → 1.21.11 or of any other adjacent pair here. 26.1/26.2
> is a specific, verified exception, not a general rule about adjacent versions.

---

## 1.21.1 needs its own source tree

1.21.1 is not a "backport" in the cheap sense. It is a **different vanilla API surface**, and
Architectury's own repository proves it — same file, same `officialMojangMappings()`, two
branches:

```
CreativeTabRegistry.java @ 1.21     → import net.minecraft.resources.ResourceLocation
CreativeTabRegistry.java @ 1.21.11  → import net.minecraft.resources.Identifier

RenderTypeRegistry.java  @ 1.21     → import net.minecraft.client.renderer.RenderType
RenderTypeRegistry.java  @ 26.1/2   → file does not exist
```

Iron Oak's source uses `net.minecraft.resources.Identifier` in **every** `init/` class and
`ChunkSectionLayer` in `IronOakClient`. Neither name exists at 1.21.1. `ModItems`'
`settings.setId(key)` is a third likely case — **unverified**, resolve it against the jar
with the `minecraft-fabric-lookup` skill rather than from memory.

Architectury's own version ladder says the same thing out loud: one major per Minecraft
release — 13.0 (1.21) → 14.0 (1.21.2) → 15.0 (1.21.4) → 16.1 (1.21.5) → 17.0 (1.21.6/7) →
18.0 (1.21.9/10) → 19.0 (1.21.11) → 20.0 (26.1) → 21.0 (26.2). They do not treat these as
source-compatible either.

**Consequence for maintenance:** the rule in `AGENTS.md` — fix on `main`, then cherry-pick —
still holds, but a cherry-pick from a 26.x tree to the 1.21.1 tree will **not** apply
cleanly. It crosses the `Identifier`/`ResourceLocation` and `ChunkSectionLayer`/`RenderType`
renames. Budget a hand-fix per backport rather than discovering it under time pressure.

The upside: 1.21.1 is the **friendliest Architectury target of the three** — Java 21, the
mature `dev.architectury.loom` 1.6, and `RenderTypeRegistry` still present, so
`IronOakClient` ports rather than disappearing. That, plus its 83 % NeoForge share, is why
it is the right **pilot** for the loader conversion.

---

## What Architectury does and does not give us

Iron Oak's Fabric coupling is thin — 15 `net.fabricmc` imports across five concerns — which
makes it a good Architectury candidate.

| Ours | Architectury equivalent | Status |
|---|---|---|
| `ModInitializer` / `ClientModInitializer` | platform entrypoints calling a common static init | mechanical |
| `FabricItemGroup` + `ItemGroupEvents` | `dev.architectury.registry.CreativeTabRegistry` | exists on all four branches |
| `BlockEntityRendererRegistry` | same simple name, `dev.architectury.registry.client.rendering` | exists on all four branches |
| `BlockRenderLayerMap` | `RenderTypeRegistry` | 1.21/1.21.11 only; **gone on 26.x** |
| `FabricBlockEntityTypeBuilder` | vanilla `BlockEntityType.Builder` | verify against the jar |
| `Registry.register` in `init/` | `registry.registries.DeferredRegister` | **the real work — see below** |

### The registration rewrite is the cost

Architectury's NeoForge side registers via events, so common code must go through
`DeferredRegister`. That is **exactly** the pattern `AGENTS.md` and the `java` skill
currently forbid ("plain `Registry.register` — no DeferredRegister (that is
Forge/NeoForge)"). Both documents have to change in the same PR as the conversion.

It also breaks the documented class-load-order contract:

- `ModRecipes` registers inside a `static {}` block.
- `ModBlocks`/`ModItems` return **live objects** from their `register` helpers, which other
  `init/` classes read immediately.
- Every arm of the 6×3 matrix becomes a `RegistrySupplier<Block>` rather than a `Block`.

This is a rewrite of the mod's init spine, not a port. Per the all-or-nothing rule in
`AGENTS.md`, it is all 18 arms or none.

### Two things Architectury does not abstract at all

Both matter more here than usual, because both are CI gates.

1. **Datagen — no abstraction exists.** Verified by grepping the whole `26.2` tree: zero
   hits for datagen or data providers. `ModDataGenerator`, `ModModelGenerator`,
   `ModConfiguredFeatures` and `FabricDynamicRegistryProvider` stay Fabric-only. The
   workable pattern is: run `runDatagen` in the Fabric subproject and share the committed
   `src/main/generated/` with every platform. This resolves open question 3 of #21.
2. **Gametests — no abstraction exists.** `src/gametest/` runs on
   `fabric-gametest-api-v1`; NeoForge has its own framework. **`./gradlew runGametest` —
   CI gate #2 — would cover the Fabric platform only.** The NeoForge jar ships with layer-1
   unit tests plus a human `runClient` pass and nothing else.

That second point is a genuine reduction in what "green" means, and it must be stated in
any PR that ships a NeoForge jar. See the honesty rule in `AGENTS.md`: if you did not
launch the game, say so.

**Lucky breaks:** the access widener is inert (its only line is a comment), so there is
nothing to translate into a NeoForge Access Transformer — and #20 deletes it anyway. The
mixin config is empty, same story.

**New user-facing dependency:** Architectury API is a required companion mod (LGPL-3, a
separate jar — no friction with Iron Oak's all-rights-reserved licence). Its Fabric build
hard-depends on `fabric-api`, so nothing is lost there.

---

## Toolchain per line

| Line | Arch branch | Arch version | Loom plugin | Java | Notes |
|---|---|---|---|---|---|
| 1.21.1 | `1.21` | 13.0.11 | `dev.architectury.loom` 1.6 | 21 | last touched 2026-07-23 — the best-maintained older branch |
| 1.21.11 | `1.21.11` | 19.0.1 | `dev.architectury.loom` 1.13 | 21 | last touched 2025-12-16 |
| 26.1 | `26.1` | 20.0.12 | `dev.architectury.loom-no-remap` 1.17 | **25** | |
| 26.2 | `26.2` | 21.0.7 | `dev.architectury.loom-no-remap` 1.17 | **25** | last touched 2026-08-02 |

Two things to note:

- **The remap/no-remap split mirrors ours.** Iron Oak already distinguishes
  `net.fabricmc.fabric-loom-remap` (obfuscated, ≤ 1.21.11) from plain `fabric-loom`
  (unobfuscated, 26.1+). Architectury has the identical split as `loom` vs `loom-no-remap`.
  Architectury Loom **replaces** Fabric Loom; it is not added alongside it.
- **Java 25 on the 26.x line is not an Architectury cost.** Architectury's 26.x branches
  compile at `options.release = 25`, and a release-21 compiler cannot read those class
  files — but #20 already moves this mod to JDK 25 for 26.2 independently. The requirements
  coincide. On the 1.21.1 and 1.21.11 lines the JDK-21 rule in `AGENTS.md` stands
  unchanged.

---

## Why not 1.20.1

1.20.1 measures 10.6–15.2 % of current Fabric demand and 55.7 % of recently-updated
NeoForge mods. It is real volume, and it is still excluded:

- It is **three majors behind** with no forward path — supporting it is a permanent branch,
  not a step toward anything.
- It needs a **fourth source tree**, with names older than 1.21.1's.
- This project has just spent an entire migration escaping the 1.20.4 line, which was
  dropped on 2026-08-21 by explicit decision.

If reach later has to grow, **measure CurseForge first**. That is unmeasured population,
not a fifth port.

---

## Ordering, and what depends on what

The order is forced by two rules already in force, not by preference:

- **`AGENTS.md`:** a version bump is never a side effect; toolchain changes are `area:build`
  tickets of their own.
- **`orchestration.md` → "Version migrations are serial":** an `area:build` ticket that
  changes `minecraft_version` or the Loom version is never dispatched in parallel with
  anything, and nothing else is dispatched while it is open. Concurrent builds on two
  Minecraft versions thrash the shared Loom cache.

So this is a queue, not a fan-out:

1. **#52 — cut `v1.21.x` and build it in CI.** The hard prerequisite. The branch is cut
   *from the release tag*, and it must land **before** `main` moves to 26.2 — otherwise the
   1.21.11 tree exists only as a tag and every backport starts with archaeology.
2. **#20 — port `main` to 26.2.** The largest single line. A WIP branch (`migration/26.2`)
   exists and all 52 compile errors are already enumerated in the issue.
3. **26.1 announced alongside 26.2.** An acceptance item of #20, not a migration. Cheapest
   reach on the board.
4. **The 1.21.1 line** — new `v1.21.1.x` branch off the 1.21.11 release tag, ported *down*.
5. **The Architectury/NeoForge conversion**, piloted on 1.21.1, then applied to 26.x.

Steps 4 and 5 are separate tickets on purpose. Doing the `DeferredRegister` rewrite while
also chasing the down-port's renames means two moving targets in one PR — the failure mode
#21 already warns about for 26.2.

**Do the conversion once, then template it.** Converting three trees in parallel is how
this becomes unbounded.

---

## The ongoing cost, stated honestly

After all of this, every future Minecraft release means:

- **3 source trees** to consider, of which 1 (26.x) moves and 2 are maintenance.
- **Up to 6 published artefacts** (3 lines × 2 loaders, where both loaders apply).
- **Cherry-picks that do not apply cleanly** between the 26.x and 1.21.1 trees, because the
  rename boundary sits between them.
- **A CI gate that covers half the artefacts.** `runGametest` is Fabric-only.

#21 asks whether that cost is acceptable and calls it a human decision. As of 2026-08-21
the answer is **yes, for these four lines and these two loaders** — and the honest reading
of that yes is that the number of lines is now capped. A fifth line needs a new decision
and a fresh measurement, not an extrapolation from this one.

---

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-21 | 1 | Initial version. Records the four-line / three-tree / two-loader decision, the 2026-08-21 Modrinth measurement and its method, the evidence that Forge is unavailable in Architectury, that 26.1 shares 26.2's tree, that 1.21.1 needs its own, and that Architectury abstracts neither datagen nor gametests. Supersedes the cumulative-download numbers in `version-migration.md` for planning. |

*Last updated: 2026-08-21*
