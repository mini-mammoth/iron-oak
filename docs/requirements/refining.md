---
domain: Refining
domain_code: REF
status: active
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - washing.md
  - ../concept/balance.md
---

# Refining Requirements

> Part of the [Iron Oak requirements](README.md) · concept:
> [Concept](../concept/README.md) · numbers: [Balance](../concept/balance.md)

**Scope:** the last step — turning shreds into something vanilla recognises. Shreds come from
[`washing.md`](washing.md).

Code: `data/iron_oak/recipes/crafting_<metal>_ore_from_shreds.json`,
`data/iron_oak/recipes/smelting_{iron,gold}_nugget.json`.

---

### REF-01: Nine shreds make one raw ore

**Status:** done · **Issue:** —

WHEN the player fills a 3×3 crafting grid with shreds of one metal THEN the system SHALL
produce one `minecraft:raw_<metal>`.

**Why 9:** it mirrors the vanilla nugget-to-ingot ratio, so the value of a shred is legible
without a wiki, and it closes the loop at the same item the infused bone meal is seeded from
(concept principle 2).

**Acceptance criteria** (verify: `inspect`, `runClient`)
- [ ] Three recipes exist, one per metal, each a full 3×3 of that metal's shred
- [ ] Output is exactly 1 raw ore of the matching metal
- [ ] Shreds of different metals do not combine

---

### REF-02: A shred smelts into a nugget

**Status:** partial · **Issue:** — · **Open question:** CON-Q2

WHEN a shred is smelted in a furnace THEN the system SHALL produce one nugget of that metal
in 50 ticks, granting 0.1 XP.

**Partial by metal:** iron and gold have this recipe; copper does not, because vanilla has no
copper nugget. The copper arm of this requirement is empty — see
[REF-03](#ref-03-copper-has-no-nugget-path).

**Why a second path:** 9 shreds for one raw ore is a poor return on a handful; the furnace
gives the player something useful out of a small pile without waiting for nine.

**Acceptance criteria** (verify: `inspect`, `runClient`)
- [ ] `smelting_iron_nugget.json` and `smelting_gold_nugget.json` exist, 50 ticks, 0.1 XP
- [ ] Both use the vanilla `minecraft:smelting` type, so a blast furnace is *not* an
      alternative (no blasting recipe is shipped)
- [ ] The recipes carry a `group` so the recipe book collapses them with vanilla nuggets

---

### REF-03: Copper has no nugget path

**Status:** wontfix (provisional) · **Issue:** — · **Open question:** CON-Q2

IF a metal has no vanilla nugget item THEN Iron Oak SHALL NOT invent one, and that metal's
shreds SHALL be refined only through [REF-01](#ref-01-nine-shreds-make-one-raw-ore).

This documents the status quo: copper shreds have exactly one use, nine at a time. Marked
**provisional** because nobody decided it — the recipe is absent by omission, not by a
recorded choice. Resolving [CON-Q2](../concept/README.md#open-questions) either confirms this
requirement or replaces it with "Iron Oak adds a copper nugget", which is a new item and
therefore a full matrix arm plus a design discussion.

**Acceptance criteria** (verify: `inspect`)
- [ ] No `smelting_copper_nugget.json` exists, and no copper nugget item is registered
- [ ] Copper shreds are usable via REF-01 only
- [ ] The concept's metal table explains why (no vanilla `*_nugget` to target)

---

### REF-04: The loop closes at raw ore

**Status:** done · **Issue:** —

WHEN the player holds a raw ore produced by REF-01 THEN it SHALL be indistinguishable from a
mined one, and therefore usable to craft more infused bone meal.

**Why:** the chain must be a loop, not a dead end — the output of refining is the input of
[INF-01](infusion.md), which is what makes a self-sustaining farm possible.

**Acceptance criteria** (verify: `runClient`)
- [ ] The result is vanilla `minecraft:raw_<metal>`, not a mod item
- [ ] It smelts into an ingot as usual, and blast-furnaces as usual
- [ ] It can be crafted straight back into infused bone meal

---

## Open questions

- **CON-Q2** Copper's missing nugget — see [REF-03](#ref-03-copper-has-no-nugget-path).
- **REF-Q1** Should shreds have a blasting recipe? Vanilla treats ore-adjacent items as
  blast-furnace material; shreds are furnace-only today, which is inconsistent with the
  raw ore they represent.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial. REF-03 records copper's missing nugget as a provisional `wontfix` rather than an accidental gap. |

*Last updated: 2026-08-20*
