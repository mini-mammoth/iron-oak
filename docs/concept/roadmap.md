---
domain: Roadmap
domain_code: RDM
status: active
last_updated: 2026-08-20
version: 2
related:
  - README.md
  - ../design/README.md
  - ../requirements/README.md
  - ../ops/version-migration.md
---

# Roadmap — where the mod is meant to grow

> **Nothing here is committed.** A line becomes real when its design document passes the human
> gate in [`../ops/orchestration.md`](../ops/orchestration.md); until then it is an intention
> with an argument attached, and the requirements it would mint do not exist yet.
>
> Promote a line by getting its design approved, minting its requirements as `planned` in
> [`../requirements/README.md`](../requirements/README.md), and then writing code. In that
> order.

---

## The three lines on the table

In this order, and the order is the argument:

### 1. Automated washing — [design](../design/automated-washing.design.md) · draft

The log chain automates from tree to ash and then stops: ash → shreds is one right-click per
item. Everything downstream of the fire bowl is therefore hand work, which makes the mod's
stated endgame — automating the chain — unreachable. This is also the oldest debt in the mod:
the README has advertised it as *coming soon* since 1.1.x, and
[`WSH-04`](../requirements/washing.md#wsh-04-automated-washing) has been `planned` with nothing
behind it.

**Why first:** it completes something that already exists rather than adding a new axis; it
introduces no new resource, no new tree and no matrix cost; and the station it proposes is the
same shape the leaves line needs twice more, so building it well pays three times.

### 2. Tree purity and breeding — [design](../design/tree-purity.design.md) · draft

One infused sapling is currently the whole game: every tree is identical, every log yields one
ash, and the only variable left is plot size. Purity makes a tree a crop under care — weak at
first, bred upward by the player's work, drifting back toward baseline per generation if
neglected, so an iron tree is not an infinite iron faucet.

**Why second:** it is the mod's missing mid-game and it changes no player-facing chain, only the
numbers inside it. **Blocked on a measurement:** `CON-Q6` — nobody has counted yield per tree, and
a multiplier on an unmeasured baseline cannot be balanced.

### 3. Leaf resources — [design](../design/leaf-resources.design.md) · draft

A second production line for the resources that have no raw ore: infused leaves → dried on a
rack (sun dries, rain sets it back) → sieved in a separator → redstone, lapis. Metals travel
through the trunk, dusts through the canopy. Substantial prior art exists on the
`restone_leaves` branch and is inventoried in the design.

**Why third:** it is the largest of the three — a new axis on the matrix, two new stations, a new
recipe type — and its most interesting long-term goal, **crossing the two lines into "super
trees"** that carry metal logs *and* resource leaves, depends on decisions that belong to purity.
Doing it before purity means making those decisions twice.

## What each line still needs before it can start

| Line | Blocking decisions |
|---|---|
| Automated washing | How water gets into the basin; whether one basin per farm is enough; whether the basin also serves dried leaves (which would make it a dependency of line 3) |
| Tree purity | How many grades; what the baseline is; which lever purity moves; **and `CON-Q6` measured** |
| Leaf resources | Whether the line covers all six woods or needs a written `MAT-02` carve-out; which resources are in scope; sieve or wash for the last step; how the super tree relates to purity |

Every one of these is an open question inside the matching design document. None is answered
here.

## Explicitly deferred within these lines

- **Super trees** (metal logs *and* resource leaves on one tree) — the stated long-term goal of
  the leaves line. Deferred, but it already constrains the near-term implementation: leaf content
  and purity are to be carried as *data on one block*, not as new block types, or the crossing
  becomes 36 tree families. That constraint is the reason it is mentioned here at all.
- **More grades, more resources, more woods** — each is a balance knob once the mechanics exist,
  not a feature of its own.

## Parked ideas

Unrated, not committed, and not attached to any of the three lines.

| Idea | Note |
|---|---|
| Chance-based infusion | `CON-Q1` — make the javadoc true, or delete the javadoc |
| Iron Oak copper nugget | `CON-Q2` — add a mod item, or keep copper's nugget arm closed as `wontfix` |
| Slower growth for infused saplings | `CON-Q5` — overlaps line 2; purity may make it unnecessary |
| A stated ore-in/ore-out target | `CON-Q6` — a prerequisite for line 2, not an idea any more |
| Wood-type differentiation | `TRE-Q1` — woods differ only in shape today |
| Water cauldron as a washing target | `WSH-Q1` — intuitive, but half-answers line 1 by accident |
| Blasting recipe for shreds | `REF-Q1` — inconsistent that shreds are furnace-only |
| Mechanical matrix checks | `MAT-Q1` — after #30 this is no longer a style question: a check comparing registry ids against constant names would have caught a live gameplay bug |

Already shipped, harvested from issue #1: copper trees, gold trees, and hopper automation of the
fire bowl.

## Bigger swings, unevaluated

Each needs its own design document and a gate before any code:

| Idea | First obstacle |
|---|---|
| More wood types (mangrove, cherry, bamboo) | A full matrix arm each — and worse once purity or leaves add an axis |
| More metals | Principle 2 requires a vanilla `raw_*` seed item; only iron, copper and gold have one |
| Nether variants (stems, quartz, ancient debris) | Contradicts principle 2, probably principle 5 |
| Infused planks or beams | Contradicts principle 4 — ore content is supposed to die in the crafting table |

## Not roadmap — tracked elsewhere

| Work | Where it lives |
|---|---|
| 1.21.11 → 26.2 migration | [`../ops/version-migration.md`](../ops/version-migration.md), issue #20 |
| NeoForge alongside Fabric | Issue #21 |
| Known broken behaviour | The status matrix in [`../requirements/README.md`](../requirements/README.md) |

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 2 | Placeholder replaced (#36) with three ordered lines, each backed by a design draft: automated washing, tree purity, leaf resources. Records why the super-tree goal already constrains how purity and leaf content must be stored. Still nothing committed. |
| 2026-08-20 | 1 | Placeholder. Harvested issue #1, the README's "coming soon", and the concept's open questions. |

*Last updated: 2026-08-20*
