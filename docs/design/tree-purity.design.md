---
domain: Tree Purity
domain_code: DES
status: draft
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - leaf-resources.design.md
  - ../requirements/trees.md
  - ../concept/README.md
  - ../concept/balance.md
---

# Design: Tree Purity and Breeding

> Status: **draft** — not approved, nothing may be implemented from it.
> Requirements: none yet (would mint a `PUR` domain) · Open questions:
> [`CON-Q5`](../concept/README.md#open-questions),
> [`CON-Q6`](../concept/README.md#open-questions),
> [`TRE-Q1`](../requirements/trees.md#open-questions)

## Problem

One infused sapling is the whole game. Once the player has an iron tree, the farm is
finished: every tree is identical to every other, every log yields exactly one ash, and the
only variable left is how many trees fit on the plot. There is no reason to care for a
tree, no reason to keep a good line alive, and no way for the farm to get *better* — so the
mod's mid-game is "build the same thing wider".

The intent is that trees have a **purity** (how much metal is actually in the wood), that
they start weak and are **bred** upward by the player's work, and that a line **drifts back
toward baseline** if left alone — so an iron tree is a crop under care, not an ore faucet.

## Constraints

- **The matrix is all-or-nothing** ([`MAT-01`](../requirements/matrix.md)). 6 woods × 3
  metals is already 18 arms; anything that multiplies that number is a design problem, not
  an implementation detail.
- **No GUI, no power** ([principle 5](../concept/README.md#design-principles)). Breeding has
  to be expressible with blocks, right-clicks and hoppers.
- **Legible without a wiki** ([principle 1](../concept/README.md#design-principles)). If the
  player cannot see that this log is better than that one, the mechanic does not exist for
  them.
- **Existing worlds must not break.** Every infused log and sapling already in a world has no
  purity; whatever the design is, those have to resolve to something sane without a data
  fixer.
- **Items must still stack.** A farm that produces five partial stacks of the same log
  because each carries a slightly different number is a worse game than one without purity at
  all. This constraint kills the most obvious implementation — see Option A.
- **No test suite.** Anything that only shows up statistically over many generations is
  effectively unverifiable here. The mechanic has to be observable in a single `runClient`
  session.

## Options — how purity is carried

### Option A — A continuous data component (0–100)

A `iron_oak:purity` data component on the item, mirrored in the sapling's block entity.

**For:** no new blocks, matrix untouched, idiomatic for the target version, and the value
flows naturally along the chain (sapling → log → ash → shred).
**Against:** **items with different components do not stack.** A continuous value means a
harvest of 40 logs at purities 37, 38, 41, … is 40 unstackable items. This alone disqualifies
the continuous form.
**Matrix cost:** none.

### Option B — Discrete grades as separate blocks and items

`iron_oak_log_rough` / `_refined` / `_pure`, per wood, per metal.

**For:** visible in the inventory, stacks correctly, no hidden data, trivially legible.
**Against:** 3 grades × 6 woods × 3 metals = 54 logs and 54 saplings, 54 configured features,
108 loot tables, 108 models, and every future wood type or metal multiplies by three. It
turns the matrix rule from a discipline into a wall.
**Matrix cost:** ×3 (or ×N grades) on every arm. Prohibitive.

### Option C — A quantised grade as a data component

The same component as Option A, but the value is one of a small fixed set of grades (say 4).
Purity is a grade, not a percentage.

**For:** keeps Option A's zero matrix cost *and* stacks properly — all grade-2 iron oak logs
are one stack. Legible via item name or tooltip ("Iron Infused Oak Log — Refined"). Existing
items with no component read as the baseline grade, so old worlds keep working with no fixer.
**Against:** still invisible in the world until the player looks; needs a tooltip and probably
a model or colour tint per grade to be properly readable.
**Matrix cost:** none. Blocks stay at 18 + 18.

**Recommendation: Option C.** It is Option A with the stacking problem removed, and the
grade count becomes a balance knob rather than a code change. Whether the grade also shows on
the *placed* sapling as a blockstate (so a plantation is readable at a glance) is a separate,
cheaper decision — the sapling is one block either way.

## Options — how purity goes up

The user's intent is that trees are *bred*: purity is earned by work, not by luck.

### Breeding option 1 — Combine saplings
Two saplings in a crafting grid yield one of the better grade (or one grade above the average,
capped). Simple, vanilla-shaped, automatable — arguably *too* automatable, since an autocrafter
loop would climb the ladder unattended.

### Breeding option 2 — Feed the sapling
Applying more infused bone meal (or ash of the same metal) to a planted infused sapling raises
its grade, with diminishing returns. Reuses an existing item and an existing gesture
([`INF-02`](../requirements/infusion.md)), and ties purity back to the loop's own output —
the player spends metal to make the farm better, which is exactly principle 2's logic applied
one level up.

### Breeding option 3 — Growing conditions
The grade of the *grown tree* is derived from how the sapling was treated: spacing, light,
soil block, whether it was bone-mealed, whether it grew undisturbed. Most "farming" of the
three and the most interesting to optimise, but the hardest to make legible and the hardest to
verify in one session.

### Breeding option 4 — A grafting station
Another station block (the mod's fourth): two saplings in, one better sapling out, over time.
Consistent with the fire bowl / dry rack / separator pattern and rate-limited by construction,
which fixes breeding option 1's runaway autocrafter.

**Leaning:** option 2 as the primary (cheap, thematic, no new block), with option 4 as the
bulk path later if breeding needs a throughput limit. Options 1 and 3 are recorded, not
recommended — 1 because it automates away the care, 3 because it hides the rules.

## Degradation — decided

**Per generation, drift toward baseline.** A sapling taken from a tree's own drops comes out
one grade *below* its parent unless the player actively breeds it back up. A good line
therefore costs upkeep every generation, and neglect returns the farm to baseline rather than
destroying it.

Explicitly rejected: exhausting the standing tree per harvest (it takes away something the
player already built — it reads as punishment), and random drift in both directions (unreadable
and unbalanceable without tests).

The consequence to design around: a player who wants a stable high-grade farm needs a
*reserve* — either a few kept saplings at the top grade, or continuous breeding input. That
tension is the point.

## What purity actually changes

One lever, chosen for visibility:

| Candidate lever | Effect | Assessment |
|---|---|---|
| **Ash per log** | A higher grade log burns to 2 ash instead of 1 | Most visible; one fire bowl cycle shows it. Preferred |
| Shreds per ash | A washing recipe per grade | Hides the effect one step further down the chain |
| Raw ore per 9 shreds | Grade changes the final ratio | Invisible until the very end; worst readability |
| Growth speed | Higher grade grows faster | Compounds with `CON-Q5`; changes farm layout, not yield |

Whichever is chosen, [`../concept/balance.md`](../concept/balance.md) gains a grade table, and
`CON-Q6` (nobody has measured yield per tree) has to be answered *first* — a multiplier on an
unmeasured baseline cannot be balanced.

## Open questions

1. **How many grades?** Four is a guess. Three is more legible, five gives breeding a longer
   arc.
2. **What is the baseline?** Does a freshly infused vanilla sapling start at the bottom grade
   (so every farm begins weak) or in the middle (so breeding can go both ways)?
3. **Is purity per tree or per log?** One grade for the whole tree is simpler; per-log
   variance would be more organic and much harder to read.
4. **Does the grade survive the chain?** Log → ash → shred carrying the grade means three
   more components and three more stacking surfaces; collapsing it at the ash step (grade
   decides *how many* ash, then the ash is plain) is simpler and probably enough.
5. **Does it show on the placed block?** A blockstate or tint on the sapling, or a different
   log texture per grade, or nothing but a tooltip.
6. **How does this interact with the leaves line?** See
   [`leaf-resources.design.md`](leaf-resources.design.md) — the "super tree" idea would have
   to decide whether one tree has one purity or one per line.
7. **What happens to existing worlds?** Baseline grade for anything without the component is
   the obvious answer; it should still be written down before it is relied on.

## Requirements this would mint

A new domain, `PUR`, in `docs/requirements/purity.md` — all `planned` on approval:

- `PUR-01` Every infused sapling and log carries a purity grade
- `PUR-02` A freshly infused vanilla sapling starts at the baseline grade
- `PUR-03` Breeding raises a sapling's grade, with diminishing returns
- `PUR-04` A sapling harvested from a tree drifts one grade toward baseline
- `PUR-05` Purity changes yield (lever per the decision above)
- `PUR-06` Items of the same grade stack; items without the component read as baseline
- `PUR-07` The player can tell a log's grade without leaving the game

## Verification

`runClient`, with a written checklist: breed a line up through every grade in one session,
confirm stacking at each grade, harvest and confirm the drift, then reload the world and
confirm nothing reset. Plus one deliberate old-world test: a world saved before the change must
open with its logs readable as baseline. Both are exactly the class of failure that
`./gradlew build` cannot see.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial draft (#36). Carrier options weighed; continuous components rejected on stacking, per-grade blocks rejected on matrix cost. Degradation decided as per-generation drift. Seven open questions. |

*Last updated: 2026-08-20*
