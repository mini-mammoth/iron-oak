---
domain: Concept
domain_code: CON
status: active
last_updated: 2026-08-20
version: 2
related:
  - balance.md
  - roadmap.md
  - ../design/README.md
  - ../requirements/README.md
  - ../../README.md
---

# Iron Oak — Concept

**Answers the question: _what is this mod, and why does it work this way?_**

This is the settled description of the mod as it exists. It is not a wish list — the
roadmap in [`roadmap.md`](roadmap.md) holds ideas, and the testable behaviour lives in
[`docs/requirements/`](../requirements/README.md). Every number quoted here is tabulated
with its source location in [`balance.md`](balance.md).

If a change contradicts this document, that is a product discussion, not a code decision.
Raising the question is right; quietly shipping the contradiction is not.

---

## Pitch

> Farm your ingots instead of mining them in the dark.

Iron Oak turns ore acquisition into agriculture. You infuse a sapling with ore-bearing
bone meal, grow it, cut it, and process the wood back into metal. The mine becomes a
tree farm.

## The player fantasy

Mining is the part of Minecraft that pulls the player *away* from the base: dark, vertical,
inventory-limited, hostile. Iron Oak keeps that same reward loop above ground, in daylight,
next to the things the player already built. The mod is for the player who has a base they
enjoy and does not want to leave it to keep it supplied.

It is deliberately **not** a shortcut. The chain is longer than swinging a pickaxe and it
costs raw ore to enter (see *Seeding cost* below). It trades **time and infrastructure**
for **not going underground**.

## The gameplay loop

```
raw ore (from mining)  ──┐
bone meal ───────────────┴──▶  ore-infused bone meal        (crafting table)
                                       │  applied to a vanilla sapling
                                       ▼
                               infused sapling              (block)
                                       │  grows, vanilla growth rules
                                       ▼
                                infused tree  ──▶ infused logs
                                       │              │
                                       │              └──▶ planks: ore content LOST (intended)
                                       ▼
                              fire bowl (burning)  ──▶  infused ash
                                       │
                                       ▼
                         wash in water (washing)  ──▶  shreds
                                       │
                        ┌──────────────┴──────────────┐
                        ▼                             ▼
              9 shreds → 1 raw ore            1 shred → 1 nugget (smelting)
```

Six wood types (oak, birch, acacia, jungle, spruce, dark oak) × three metals (iron, copper,
gold) = the **6×3 matrix** that shapes the whole codebase. See
[`requirements/matrix.md`](../requirements/matrix.md).

## Design principles

These are the rules the existing content follows. They are the reason to say no to an
otherwise attractive feature.

1. **Vanilla-shaped.** Infused trees are vanilla tree shapes with a recoloured trunk and
   vanilla leaves. Infused logs sit in the vanilla `minecraft:*_logs` tags, so every vanilla
   recipe and tool interaction keeps working. Nothing in the mod requires a new UI screen or
   a custom container — the fire bowl is operated by right-click and hoppers, like a
   campfire.
2. **The loop costs raw ore to start.** Infused bone meal needs raw ore as the seed
   ingredient. The mod is never a way to get a metal you have never held; it is a way to
   stop going back down for more of it.
3. **The player pays with steps, not with clicks.** Each stage is cheap to perform but the
   chain is long, and every stage is a place where a hopper can be attached. Automating the
   chain is the intended endgame, not a shortcut around the design.
4. **Ore content is destroyable.** Crafting infused logs into planks throws the metal away.
   This is intended: infused wood is a *carrier*, not a building material, and the player is
   expected to lose a batch to the wrong crafting recipe once and remember it.
5. **No new dimensions, mobs, or tech tiers.** Iron Oak stays a self-contained processing
   chain. Feature ideas that need a power system, a machine GUI, or a new dimension are out
   of concept — see [`roadmap.md`](roadmap.md).
6. **The matrix is all-or-nothing.** A wood type or a metal exists in *every* arm (block,
   item, sapling generator, feature, loot table, tag, model, texture, lang key, recipe) or
   it does not exist. A half-filled matrix ships a crash.

## Progression

The mod has no tech tiers and no unlocks. Progression is the player's own infrastructure:

| Stage | What the player has | What it costs them |
|---|---|---|
| Manual | One fire bowl, right-clicking logs in, washing ash by hand | Attention per item |
| Semi-automatic | Hoppers feeding the fire bowl, chest below collecting ash | Keeping the log supply full, re-igniting when it runs dry |
| Farm | A tree farm feeding the bowls, bulk washing | Build effort — washing is still manual today (see [WSH-04](../requirements/washing.md)) |

The intended pressure point is the **fire bowl afterburn window**: the bowl stays lit for a
short while after its last log, so a fed bowl keeps running and a starved one goes out. That
single rule is what makes automation a build problem rather than a toggle.

## Seeding cost and yield

The chain is intentionally close to break-even and is not balanced as a multiplier:

- Entry: 1 raw ore + 8 bone meal → 4 infused bone meal → 4 infused saplings.
- Return: one grown tree yields several infused logs; each log burns to one ash, each ash
  washes to one shred, 9 shreds make one raw ore.

Whether the resulting yield per tree is *right* has never been measured — the numbers were
chosen by feel. No requirement asserts a target ratio, and that is a known gap recorded in
[`balance.md`](balance.md).

## What Iron Oak is not

- Not an ore multiplier or a doubling mod. It moves ore acquisition, it does not inflate it.
- Not a magic mod. The only "magic" is the infusion sparkle; there is no mana, ritual, or
  research.
- Not a tech mod. No power, no pipes, no machine GUIs. Vanilla hoppers are the automation.
- Not open source — see [`LICENSE`](../../LICENSE). Modpacks are welcome, redistribution as
  a standalone download is not.

## Scope of the metal and wood sets

| Axis | Members | Notes |
|---|---|---|
| Metals | iron, copper, gold | Chosen because each has a vanilla `raw_*` item to seed from |
| Woods | oak, birch, acacia, jungle, spruce, dark oak | The six overworld types with a vanilla sapling |
| Deliberately absent | mangrove, cherry, bamboo, azalea, nether stems | Never evaluated; would each need a full matrix arm |
| Deliberately absent | netherite, diamond, emerald, lapis, redstone, coal | No `raw_*` seed item, and gating end-game materials behind a farm contradicts principle 2 |

## Open questions

Recorded, not resolved. Each is referenced from the requirement it affects.

- **CON-Q1** Is the infusion supposed to be deterministic? The code always converts the
  sapling; `OreInfusedBoneMeal`'s javadoc says it "has a chance to convert".
  → [INF-03](../requirements/infusion.md)
- **CON-Q2** Should copper have a nugget? Vanilla has none, so `smelting_*_nugget` exists
  for iron and gold only and the copper arm of that recipe is empty by omission.
  → [REF-03](../requirements/refining.md)
- **CON-Q3** Is the split between a 200-tick player insertion and a 150-tick hopper
  insertion intended, or the bug behind #28? → [BRN-03](../requirements/burning.md)
- **CON-Q4** Should washing be automatable at all, and if so as a block, a cauldron
  interaction, or a dispenser behaviour? The README promises "*coming soon*".
  → [WSH-04](../requirements/washing.md),
  [design draft](../design/automated-washing.design.md)
- **CON-Q5** Should infused saplings grow more slowly than vanilla ones as the cost of
  farmed ore? Today they use vanilla growth exactly. → [TRE-02](../requirements/trees.md),
  [tree purity draft](../design/tree-purity.design.md)
- **CON-Q6** Is there a target yield per tree, or is "roughly break-even by feel" the
  intended balance statement? → [`balance.md`](balance.md). **This one now blocks work:**
  the [tree purity draft](../design/tree-purity.design.md) cannot be balanced against an
  unmeasured baseline.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 2 | Open questions CON-Q4/Q5/Q6 point at the design drafts that now discuss them (#36); CON-Q6 marked as blocking. |
| 2026-08-20 | 1 | Initial concept, written from the shipped 1.20.4 code. Records the loop, the six design principles, the metal/wood scope, and six open questions. No behaviour was changed. |

*Last updated: 2026-08-20*
