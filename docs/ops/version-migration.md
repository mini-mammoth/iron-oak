---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - orchestration.md
  - orca-progress-loop.md
---

# Version Migration — 1.20.4 → 1.21.11 + 26.2

`main` now targets **Minecraft 1.21.11** (`mod_version=1.3.0+1.21.11`), migrated up from
1.20.4.

**The endpoint is two supported lines, not one.** 1.21.11 is released and then kept on its
own branch; `main` moves on to 26.2. Both are maintained. See
[Two supported lines](#two-supported-lines) for what that costs and how a fix reaches both.

This document is the plan and the gate list. It is process policy, not a task briefing —
workers get scoped tickets, not this file.

---

## Why this is three stages and not one bump

Two ecosystem facts decide the shape of the work:

1. **Minecraft 26.1 and later are unobfuscated.** Yarn and Intermediary stopped being
   updated after **1.21.11** — the last yarn artifact on `maven.fabricmc.net` is
   `1.21.11+build.6` (published 2026-05-27), and there is no `26.x` yarn at all. This mod
   used yarn until this migration.
2. **Fabric API renamed its own surface to official names in 26.1.** Not backwards
   compatible.

Fabric's own guidance is explicit: **migrate the mappings before bumping the Minecraft
version.** See "The ordering, corrected" below for how that actually played out — the
mapping migration went first, on 1.20.4, not on 1.21.11 as originally planned here.

That fixes the order, and it is stricter than it first looks: `migrateMappings` rewrites
**source**, so it needs a tree that resolves. Run it after a four-release version bump and
it has nothing to work with. So the mapping migration went **first, on 1.20.4** — where
the tree still compiled and a green build proved the mapping change alone worked. Only
then did the Minecraft version move.

It also happens to be free reach: 1.21.11 is a version worth shipping (see below), and we
pass through it anyway.

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

## Status

| Stage | State |
|---|---|
| Yarn → Mojang official mappings, still on 1.20.4 | **done**, build green |
| 1.20.4 → 1.21.11 | **done**, build green, `runDatagen` green |
| 1.21.11 in-game verification | **done** — walked 2026-08-21, passed (#19) |
| 1.21.11 release tag | **done** — `v1.3.0+1.21.11`, published to Modrinth and CurseForge |
| `v1.21.11` branch cut and wired into CI | **done** — #52; CI green on Linux and Windows, both layers |
| 1.21.11 → 26.2 | **in progress** — #20. Toolchain proven, ~52 source errors remain |
| 1.20.4 line | **dropped** — decided 2026-08-21 |
| 1.18.2 and 1.19 lines | **dropped** — decided 2026-08-21; archived, not built |

---

## The ordering, corrected

The original plan here said: bump to 1.21.11 on Yarn first, then migrate mappings. That
was wrong, and the reason is worth keeping.

`migrateMappings` rewrites **source**, so it needs a tree that resolves. Doing the version
bump first means running the mapping migration on a codebase that does not compile.
Migrating the mappings **first**, while still on 1.20.4, means only one variable changes
and a green build proves it worked. That is the order actually used:

1. **Mappings, on 1.20.4.** `./gradlew migrateMappings --mappings "net.minecraft:mappings:1.20.4"`
   on Loom 1.5 — the task has existed far longer than the 1.13 floor the porting guide
   mentions, and it worked. Output went to `remappedSrc/`, non-destructively.
2. **Version bump, on Mojang mappings.** Toolchain lifted wholesale from Fabric's own
   1.21.11 example mod branch rather than assembled by hand.
3. **26.2**, on the same mappings.

### What `migrateMappings` cannot do

Three classes of leftover, all of which need a human pass:

- **Wildcard imports.** `import net.minecraft.item.*;` is left untouched — the task cannot
  resolve a wildcard. It had already added every explicit import needed, so the wildcards
  were simply dead lines, but they fail the build with a confusing "package does not
  exist".
- **Types it cannot resolve** get emitted as the obfuscated name: one import came out as
  `import I;`. The correct type was already imported alongside it.
- **Anything that is not Java.** The access widener names classes as strings. Ours turned
  out to be inert (its only entry is a comment), but a real one must be translated by
  hand via https://mappings.dev

---

## Target versions

| Stage | Minecraft | Mappings | Loader | Fabric API | Java | Loom plugin |
|-------|-----------|----------|--------|-----------|------|-------------|
| was | 1.20.4 | yarn 1.20.4+build.3 | 0.15.7 | 0.96.4+1.20.4 | 17 | `fabric-loom` 1.5 |
| **now** | **1.21.11** | **Mojang official** | 0.19.3 | 0.141.6+1.21.11 | **21** | `net.fabricmc.fabric-loom-remap` 1.17-SNAPSHOT |
| next | 26.2 | Mojang official | 0.19.3 | 0.158.0+26.2 | **25** | `net.fabricmc.fabric-loom` 1.17-SNAPSHOT |

Gradle 9.5.1 for both. Note the plugin id: the `-remap` variant is for obfuscated
Minecraft (≤ 1.21.11); 26.1+ ships unobfuscated and uses the plain `fabric-loom`, with
mod dependencies on `implementation` instead of `modImplementation` and no `mappings`
line at all.

**Java jumps twice**: 17 → 21 for 1.21.11, then 21 → 25 for 26.2. Three places each time:
`build.gradle` (`options.release`, `sourceCompatibility`/`targetCompatibility`),
`fabric.mod.json` (`"java"`), and `iron-oak.mixins.json` (`compatibilityLevel`).

---

## The 1.21.11 gate — cleared 2026-08-21

**Passed.** The in-game loop was walked on 2026-08-21 and reported working, `v1.3.0+1.21.11`
is tagged at `5df3263`, and both platforms have the jar (#19). The list below is kept because
it is the reason the gate existed, and because **26.2 inherits every item on it** — see
Stage 3.

The mechanical evidence at the tag, for the record: `./gradlew build` green with 326 unit
tests, `./gradlew runGametest` green with seven gametests, and the jar checked as an artefact
at 396 files rather than trusted from its exit code.

Keep the shape of this in mind when 26.2 comes up: `./gradlew build`, `runGametest` and
`runDatagen` being green is narrower evidence than it sounds. The two test layers cover ids,
committed resources, the 6×3 matrix and the fire bowl's ticking, and **nothing in the list
below**. For everything here, a green build says only that the mod compiles and remaps.

Several changes in the 1.21.11 migration were behavioural and could only be checked by
playing:

- **`ofLegacyCopy` vs `ofFullCopy`** on block settings. `ofFullCopy` also copies the source
  block's loot table, which would make an infused log drop a plain vanilla log.
  `ofLegacyCopy` was chosen deliberately. **Verify the logs drop themselves.**
- **`playerWillDestroy` replacing `onRemove`** for dropping stored items. The new
  `affectNeighborsAfterRemoval` runs after the block entity is gone, so the drop moved to
  a pre-removal hook. This covers a player breaking the bowl; it may **not** cover
  destruction by explosion or piston, which the old hook did. **Verify breaking a
  non-burning bowl returns its contents.**
- **`useWithoutItem` / `useItemOn` split.** The old single `use` handled both empty hand
  and held item. **Verify both: empty hand takes items out, a log goes in.**
- **Frost walker.** `EnchantmentHelper.hasFrostWalker` was removed; the enchantment is now
  resolved from the registry. **Verify frost walker boots still prevent the burn.**
- **The block entity's save format** moved to `ValueInput`/`ValueOutput`. **Verify a bowl
  with items in it survives a world reload.**
- **Recipe JSON** was rewritten for all 15 recipes. **Verify each of the three metals
  end-to-end.**
- **`FURNACE_MISC`** was picked for `recipeBookCategory()`, which is new and required.
  Check it does not put the mod's recipes somewhere silly in the recipe book.
- **The renderer** was rewritten for the two-phase render-state pipeline. **Verify the
  input item shows in the bowl and the output spins once the fire is out.**

Acceptance for 1.21.11, in `runClient`:

- [ ] Craft infused bone meal (all three metals)
- [ ] Infuse a sapling, grow the tree, cut it, confirm the log drops itself
- [ ] Burn a log in a fire bowl → ash; output renders and spins when unlit
- [ ] Wash ash in water → shreds
- [ ] 9 shreds → raw ore; smelt a shred → nugget
- [ ] All 36 blocks and 9 items present and correctly named in the mod's creative tab.
      **The tab is on the second creative page** — click the `>` at the top right of the
      creative screen, or press `PAGE_DOWN`. Fabric puts every non-vanilla tab on page 2
      and up; page 1 holds only the 14 vanilla tabs. A tab that is "missing" is on page 2
      until proven otherwise (see #26).
- [ ] Place items in a bowl, save and reload, items still there
- [ ] Hopper automation still feeds the bowl
- [ ] `./gradlew runServer` starts (environment is `*`, so a client-only regression is real)

---

## Two supported lines

**Decided 2026-08-21: 1.21.11 and 26.2 are both supported, on separate branches.**

One jar cannot serve both. The divergence is the whole build regime, not a handful of
renames:

| | 1.21.11 | 26.2 |
|---|---|---|
| Mappings | obfuscated, remapped | unobfuscated |
| Loom plugin | `net.fabricmc.fabric-loom-remap` | `net.fabricmc.fabric-loom` |
| Mod dependencies | `modImplementation` + a `mappings` line | `implementation`, no `mappings` line |
| Java | 21 | 25 |
| Fabric API | pre-26.1 package names | official names |

Java settles it on its own: a class file compiled at release 25 does not load on a JDK 21
runtime, and `fabric.mod.json` has to declare a different `minecraft` range and `java` floor
per artefact. Parallel support therefore means **two artefacts**, and that is a branch
question, not a build-variant question.

The shape is the one [`../../AGENTS.md`](../../AGENTS.md) already mandates, and which
`v1.18.x` already runs: newest version on `main`, older lines on their own branches, a fix
committed on `main` first and then cherry-picked — never developed twice in parallel.

| Branch | Minecraft | Role |
|---|---|---|
| `main` | 26.2 | development; the newest supported version |
| `v1.21.x` | 1.21.11 | maintained; cut from the 1.21.11 release tag |

Wiring, once the tag exists:

- add `v1.21.x` to **both** branch lists in `.github/workflows/main.yml` — the file states in
  a comment that the two lists are kept in sync by hand, because Actions does not reliably
  support YAML anchors.
- `.github/workflows/release.yml` needs **no** change: it triggers on a *published release*
  regardless of branch, and a release cut from `v1.21.x` publishes with that branch's
  `gradle.properties`. Check `publish_game_versions` there as well — it is a judgement call
  per line, not derived from `minecraft_version`.

### What a second line actually costs

Per gameplay change: one cherry-pick, and **two** in-game verification passes, because
`runClient` is the gate for rendering and feel on both lines and a green build proves neither.
Per release: two cuts, two changelogs. That is the price of the download share, and it is
why the 1.20.4 line was dropped rather than kept as a third.

The requirements catalogue is already built for this: status is stated against `main`, and a
requirement that only fails on the other line keeps its `main` status and names the port
issue in a **port note** — as [BRN-10](../requirements/burning.md#brn-10-show-what-is-inside)
did for #27 while 1.21.11 was still a branch. Do not add a per-branch status column.

### Why not a preprocessor

[Stonecutter](https://github.com/kikugie/stonecutter) builds N version jars from one source
tree, and was considered. Rejected here: the divergence is concentrated in `FireBowlBlock`,
`FireBowlEntity` and the fire bowl renderer — exactly the classes the migration rewrote — so
it would mean conditional blocks threaded through the most interesting code in the mod, plus
a per-version Loom plugin id, to save duplicating twenty-odd Java files. An
Architectury-style common/platform split solves a different problem: multiple **loaders**
(#21), not multiple Minecraft versions.

Revisit if a third line ever becomes necessary.

---

## Stage 3 — 26.2, and the traps it inherits

Stages 1 and 2 are done — see **Status** above. Their step-by-step plans have been retired
from this document rather than left to rot: what is worth keeping from them is recorded in
"The ordering, corrected" and "What `migrateMappings` cannot do".

**`migration/26.2` is not a usable base.** It reads as "almost done" and is not:

- it changes **exactly three files** — `build.gradle`, `gradle.properties`,
  `fabric.mod.json`. There are **no Java changes on it at all**; the ~52 source errors are
  entirely unaddressed.
- it is **57 commits behind `main`** (merge-base `330f4ae`, from before the test harness),
  which is why diffing it against `main` looks like it deletes the whole test suite.

So the source port has not started, and it is now bigger than that branch ever saw: both test
layers from #40 and the `RequirementCatalogue` from #43 have to be ported too, and
`RequirementTracingTest` fails the build if a gate token loses its citation. Re-apply the
12-line toolchain diff on current `main` and treat the rest as new work; do not rebase the
branch.

Two warnings from those stages are **not** historical, because 26.1+ renames things again
and every one of them will bite a second time:

- **A missed resource-directory rename does not fail the build.** 1.21 renamed
  `loot_tables` → `loot_table`, `tags/blocks` → `tags/block`, `tags/items` → `tags/item`,
  `recipes` → `recipe`. Everything compiles, the jar builds, CI is green — and in-game the
  recipes, loot tables or tags are simply absent. `RegistryAssetsTest` catches an item whose
  asset files are missing, but nothing catches a whole directory that vanilla stopped reading,
  which is why the acceptance below is in-game and not a green build.
- **A missing *asset* layer is quieter still.** 1.21.4 put `assets/<ns>/items/<id>.json`
  between an item and its model, and this mod shipped none of them until #26 — every stack
  rendered as the missing-texture cube. Nothing was logged, and nothing could have been:
  `ClientItemInfoLoader` and `ItemModelResolver` have no diagnostic for an absent
  definition, they fall back to `MissingItemModel` in silence. So on the assets side a
  clean `latest.log` is not evidence of anything. Check that the files exist and that what
  they point at exists.
- **The access widener is not source code.** `migrateMappings` will not translate it. Ours
  is inert (its only entry is a comment), but a real one must be done by hand via
  https://mappings.dev

When pinning a Loom version, verify it against
`https://maven.fabricmc.net/fabric-loom/fabric-loom.gradle.plugin/maven-metadata.xml`
before writing it into `gradle.properties` — these move.

### The work itself

- Bump `minecraft_version` to `26.2`, `fabric_version` to `0.158.0+26.2`.
- Fabric API's own classes were renamed to official names in 26.1. Every
  `net.fabricmc.fabric.api.*` import is a candidate.
- Vanilla changes between 1.21.11 and 26.2 on top of that.
- Fabric's docs now target 26.2 and show Mojmap throughout, so from here the docs and the
  code finally speak the same language: https://docs.fabricmc.net/

Acceptance: the same in-game checklist as 1.21.11 above, plus a check that the mod loads
on a **dedicated server** (`./gradlew runServer`) — `environment` is `*` in
`fabric.mod.json`, so a client-only regression is a real risk and the fire bowl renderer is
the obvious place for one.

---

## What happens to the old versions

`main` moves to the newest supported version. **The 1.20.4 line is dropped — decided
2026-08-21.** It sits at ~2 % of Fabric API downloads and is falling, and a third maintained
line would cost a third in-game pass per fix (see "What a second line actually costs"). It is
not branched and not published against; the last release on it, `1.2.1+1.20.4`, stays up.

**The 1.18.2 and 1.19 lines are dropped too — decided 2026-08-21.** `v1.18.x` and `1.19`
stay on the remote as **archives**: no CI, no releases, no cherry-picks. Their published jars
stay up. An archive is not a supported line, and the CI branch lists must not imply it is —
that was the state before this decision and it read as support.

Existing branches: `v1.21.11` (supported), `v1.18.x` and `1.19` (archived), plus unmerged
`restone_leaves`.

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
| Plan approved | before the first `area:build` ticket | human — **done** |
| Mapping migration build green | before starting the version bump | orchestrator — a mapping-only change must not alter behaviour — **done** |
| 1.21.11 in-game loop verified | before merging the 1.21.11 work | human, on `runClient` evidence — **done** 2026-08-21 |
| 1.21.11 release tagged | after stage 2 | human — **done**, `v1.3.0+1.21.11` |
| `v1.21.11` cut from the tag and added to CI | before `main` moves to 26.2 | orchestrator — **done**, #52 |
| Stage 3 started | after 1.21.11 is released | human — **in progress**, #20 |
| Old-version branch policy | before `main` moves | human — **done**: 1.20.4, 1.19 and 1.18.2 dropped; 1.21.11 kept on `v1.21.11` |

---

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-21 | 1.3 | 1.21.11 is verified in-game and released as `v1.3.0+1.21.11` (#19). The blocking-gate section becomes the record of a cleared gate, kept because 26.2 inherits every item on its list. Stage 3 is unblocked; cutting `v1.21.x` (#52) is next. |
| 2026-08-21 | 1.2 | Two supported lines, not one endpoint (#50): 1.21.11 is kept on `v1.21.x` and `main` goes to 26.2, with the cost of the second line and the rejected preprocessor alternative recorded. 1.20.4 is dropped, closing that gate. Corrects the 26.2 status — `migration/26.2` carries no Java changes and is 57 commits behind, so the source port has not started and now includes porting both test layers. |
| 2026-08-21 | 1.1 | The blocking gate accounts for the test harness (#47). Both layers are named in the evidence, and the point is sharpened rather than dropped: what they cover is listed, and none of it is on the in-game list below. The resource-rename warning credits `RegistryAssetsTest` for the half it now catches. |
| 2026-08-20 | 1.0 | Initial plan. Target versions and the yarn-discontinuation constraint verified against `maven.fabricmc.net` and the Fabric porting docs; reach figures measured via the Modrinth API. |

*Last updated: 2026-08-21*
