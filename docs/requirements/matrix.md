---
domain: Matrix
domain_code: MAT
status: active
last_updated: 2026-09-18
version: 3
related:
  - README.md
  - trees.md
  - infusion.md
  - ../concept/README.md
---

# Matrix Requirements — 8 woods × 3 metals on `v1.21.11`

> Part of the [Iron Oak requirements](README.md) · concept principle 6:
> [Concept](../concept/README.md#design-principles)

**Scope:** the completeness rules that hold across the whole content matrix. These are the
requirements that a *feature* PR breaks by accident, which is why they are stated separately
instead of being repeated in every domain.

Woods: oak, birch, acacia, jungle, spruce, dark oak, cherry, pale oak.
Metals: iron, copper, gold. → **24 combinations** on this line, and per combination two
blocks (log, sapling) plus their block items.

**The wood set is no longer uniform across version lines.** Pale oak does not exist on
`v1.21.1` (see [#88](https://github.com/mini-mammoth/iron-oak/issues/88)), so that line's
matrix is 7 woods × 3 metals = 21 combinations. `Matrix.java` is the single place a line
declares its own wood set — see "The matrix may differ per line" in
[`AGENTS.md`](../../AGENTS.md). The counts below are this line's.

Counts as registered on this line (`1.3.0+1.21.11`): 49 blocks (24 logs + 24 saplings + fire
bowl), 58 items (49 block items + 3 ash + 3 bone meal + 3 shreds), 59 lang keys (49 block + 9
item + 1 item group).

---

### MAT-01: Every combination exists in every arm

**Status:** done · **Issue:** —

WHEN a metal/wood combination exists THEN it SHALL be present in **all** of the following,
with no exception:

| Arm | Location |
|---|---|
| Log block + sapling block | `init/ModBlocks.java` |
| Block items | `init/ModItems.java` |
| Sapling generator | `init/ModSaplingGenerators.java` |
| Configured feature | `init/ModConfiguredFeatures.java` → `src/main/generated/` |
| Loot tables (log, sapling) | `data/iron_oak/loot_table/blocks/` |
| Metal-scoped log tags | `data/iron_oak/tags/{block,item}/<metal>_infused_logs.json` |
| Vanilla wood tags | `data/minecraft/tags/{block,item}/<wood>_logs.json` |
| Blockstate + models | `assets/iron_oak/blockstates/`, `assets/iron_oak/models/` |
| Textures | `assets/iron_oak/textures/` |
| Lang keys | `assets/iron_oak/lang/en_us.json` |
| Item group entry | `init/ModItems.java` |

**Why:** a missing arm is not a cosmetic gap — a sapling whose configured feature is absent
crashes on growth, and a missing lang key ships as a raw translation string.

**Acceptance criteria** (verify: `build`, `inspect`, `test`)
- [ ] 24 log blocks and 24 sapling blocks are registered
- [ ] 48 loot tables exist under `data/iron_oak/loot_table/blocks/`
- [ ] 24 configured features exist under `src/main/generated/.../configured_feature/`
- [ ] Every registered block and item has a lang key; no key is orphaned
- [ ] The jar contains hundreds of files (see [`../ops/release.md`](../ops/release.md)) — a
      collapsed count of 2 means an empty-jar build, not a matrix gap

---

### MAT-02: A partial matrix is not shipped

**Status:** done · **Issue:** —

IF a new wood type or metal cannot be completed across every arm in MAT-01 THEN it SHALL NOT
be merged — the incomplete work is reported instead.

**Why:** this is the rule that already exists in [`AGENTS.md`](../../AGENTS.md) ("The matrix
is all-or-nothing"); it is restated here as a requirement so a matrix gap is a failed
requirement and not just a style note.

**Acceptance criteria** (verify: `inspect`, `test`)
- [ ] No registered block or item lacks a model, texture, loot table or lang key
- [ ] No sapling references a configured feature that does not exist
- [ ] `./gradlew runClient` reaches the creative menu with every mod item rendering

---

### MAT-03: Metal-scoped log tags list every wood on the line

**Status:** done · **Issue:** —

WHEN a recipe needs "any log of this metal" THEN it SHALL use
`iron_oak:<metal>_infused_logs`, and that tag SHALL contain all eight wood types of that
metal (on this line; a line with a smaller wood set, per `Matrix.java`, lists that line's
set).

This tag is what makes one burning recipe per metal sufficient instead of twenty-four.

**Acceptance criteria** (verify: `inspect`, `test`)
- [ ] Each of the three block tags lists exactly eight logs (on this line)
- [ ] The three item tags mirror them
- [ ] `data/iron_oak/recipe/burning_<metal>_ash.json` matches on the tag, not on items

---

### MAT-04: Vanilla wood tags list all three metals

**Status:** done · **Issue:** —

WHEN vanilla refers to `minecraft:<wood>_logs` THEN all three infused variants of that wood
SHALL be members, as blocks and as items.

**Acceptance criteria** (verify: `inspect`)
- [ ] Eight block tag files (on this line), each listing three infused logs, `"replace": false`
- [ ] Eight item tag files doing the same
- [ ] No tag file sets `"replace": true` (that would delete vanilla's own entries)

---

### MAT-05: Every registered object has an English name

**Status:** done · **Issue:** —

WHEN a block or item is registered THEN `assets/iron_oak/lang/en_us.json` SHALL contain its
translation key.

`en_us.json` is the only language file; there is no translation obligation beyond English.

**Acceptance criteria** (verify: `runClient`, `inspect`, `test`)
- [ ] No `block.iron_oak.*` / `item.iron_oak.*` key appears untranslated in-game
- [ ] Names follow the shipped pattern `"<Metal> Infused <Wood> Log"` / `"… Sapling"`
- [ ] The item group has a name

---

## Open questions

- **MAT-Q1** ~~Should MAT-01's counts be enforced mechanically?~~ **Answered: yes (#40).**
  There is a test suite now, and the matrix rule is a gate rather than a hand check:
  `TreeMatrixTest` walks all arms from the sapling id to the committed feature JSON and
  asserts there are exactly as many generators as `Matrix.arms()` (24 on this line),
  `RegistryAssetsTest` derives its file list from the registries so a new arm cannot be
  missed, and `OreInfusedBoneMealTest` pins the bone-meal pairings for every wood in
  `Matrix.WOODS`. All three run on `./gradlew build`. The counts above are still worth
  reading as the specification — the point of MAT-Q1 was that nothing enforced them, and now
  something does.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial. Counts verified against the shipped `1.2.1+1.20.4` tree. |
| 2026-08-21 | 2 | MAT-01, MAT-02, MAT-03 and MAT-05 name `test` as a gate (#43) — the harness from #40 checks them on every build. MAT-Q1 answered: the counts are enforced mechanically now. |
| 2026-09-18 | 3 | Cherry and pale oak land on this line (#88): 6x3 -> 8x3, 18 -> 24 combinations. Notes that `v1.21.1` stays at 7x3/21 because pale oak doesn't exist in that version's vanilla jar, and fixes stale `loot_tables`/`tags/{blocks,items}`/`recipes` path names to the singular directories this line's resources actually use. |

*Last updated: 2026-09-18*
