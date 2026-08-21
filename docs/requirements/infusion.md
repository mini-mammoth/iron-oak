---
domain: Infusion
domain_code: INF
status: active
last_updated: 2026-08-21
version: 2
related:
  - README.md
  - trees.md
  - matrix.md
  - ../concept/README.md
  - ../concept/balance.md
---

# Infusion Requirements

> Part of the [Iron Oak requirements](README.md) · concept:
> [Concept](../concept/README.md) · numbers: [Balance](../concept/balance.md)

**Scope:** ore-infused bone meal — crafting it, and using it to turn a vanilla sapling into
an infused one. Everything after the sapling exists belongs to [`trees.md`](trees.md).

Code: `OreInfusedBoneMeal`, the `Map.of(...)` infusion maps in `init/ModItems.java`,
`data/iron_oak/recipes/crafting_<metal>_bone_meal.json`.

---

### INF-01: Craft ore-infused bone meal from raw ore

**Status:** done · **Issue:** — · **Metals:** iron, copper, gold

WHEN the player surrounds one `minecraft:raw_<metal>` with eight `minecraft:bone_meal` in a
crafting grid THEN the system SHALL produce 4 × `iron_oak:<metal>_bone_meal`.

**Why:** the loop must cost the metal it produces (concept principle 2) — infused bone meal
is never obtainable without already holding that metal.

**Acceptance criteria** (verify: `inspect`, `runClient`)
- [ ] All three recipes exist and are shaped `###` / `#R#` / `###`
- [ ] The centre ingredient is the matching `raw_<metal>`; the ring is vanilla bone meal
- [ ] Output count is 4
- [ ] The recipe appears in the recipe book and in the crafting-table search

---

### INF-02: Applying infused bone meal converts a vanilla sapling

**Status:** done · **Issue:** —

WHEN the player right-clicks a **placed vanilla sapling** with infused bone meal of any
metal THEN the system SHALL replace that block with the infused sapling of the same metal
and wood type, in its default state.

**Accepted saplings:** oak, birch, acacia, jungle, spruce, dark oak — declared explicitly
per metal as a `Map<Block, Block>` in `ModItems`, not by tag.

**Acceptance criteria** (verify: `runClient`, `test`)
- [ ] Each of the 6 vanilla saplings converts, for each of the 3 metals (18 combinations)
- [ ] The resulting block is the infused sapling of the *same* wood type
- [ ] An **already infused** sapling is not converted again and consumes nothing (it is not
      a key in any infusion map)
- [ ] A sapling of a wood type outside the six is unaffected

---

### INF-03: Infusion always succeeds

**Status:** done · **Issue:** — · **Open question:** CON-Q1

WHEN infused bone meal is applied to an accepted sapling THEN the conversion SHALL succeed
every time — there is no random failure and no partial state.

**Note — code and documentation disagree.** The javadoc on `OreInfusedBoneMeal` claims the
item "has a chance to convert the sapling". The code has no randomness. This requirement
documents the **code**; which of the two is the intent is
[CON-Q1](../concept/README.md#open-questions) and must be decided before either is changed.

**Acceptance criteria** (verify: `runClient`)
- [ ] 10 consecutive applications convert 10 saplings — no failures
- [ ] Whichever way CON-Q1 is resolved, this requirement and the javadoc agree afterwards

---

### INF-04: Infusion consumes one bone meal and is visibly confirmed

**Status:** done · **Issue:** —

WHEN a conversion succeeds THEN the system SHALL decrement the held stack by exactly 1, emit
15 bone-meal particles at the sapling, and play `BLOCK_ENCHANTMENT_TABLE_USE` at volume 0.3.

**Why:** the sparkle plus the enchantment sound is the only feedback that the sapling is now
different; the sapling model change is subtle at a distance.

**Acceptance criteria** (verify: `runClient`)
- [ ] Exactly one item leaves the stack per conversion
- [ ] Particles and sound fire on the client for the acting player
- [ ] In creative mode the stack is not consumed (vanilla item behaviour)

---

### INF-05: Infused bone meal still works as ordinary bone meal

**Status:** done · **Issue:** — · **Open question:** INF-Q1

IF infused bone meal is used on a block that is **not** an accepted sapling THEN the system
SHALL fall back to vanilla bone meal behaviour.

`OreInfusedBoneMeal extends BoneMealItem`, so growing crops, spreading grass and fertilising
any other bone-meal target all work — including on wheat, where the player silently spends a
raw-ore-backed item on a vanilla effect.

**Acceptance criteria** (verify: `runClient`)
- [ ] Used on wheat: the crop grows and one infused bone meal is consumed
- [ ] Used on grass blocks: flowers spawn as with vanilla bone meal
- [ ] Used on stone: nothing happens and nothing is consumed

---

## Open questions

- **INF-Q1** Should infused bone meal refuse non-sapling targets instead of burning a
  raw-ore-backed item on wheat? Today it inherits the full vanilla behaviour (INF-05). This
  is a fall-out of the class hierarchy, not a decision anyone recorded.
- **CON-Q1** Deterministic infusion vs. the "has a chance" javadoc — see
  [INF-03](#inf-03-infusion-always-succeeds).
