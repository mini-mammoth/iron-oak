---
domain: Leaf Resources
domain_code: DES
status: draft
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - tree-purity.design.md
  - automated-washing.design.md
  - ../requirements/trees.md
  - ../requirements/matrix.md
  - ../concept/README.md
---

# Design: Leaf Resources — drying and sieving

> Status: **draft** — not approved, nothing may be implemented from it.
> Requirements: none yet (would mint a `LEF` domain) · Prior art: branch
> `restone_leaves` (note the typo — no `d`)

## Problem

The mod only reaches metals that have a vanilla `raw_*` item to seed from: iron, copper, gold
([concept, *Scope of the metal and wood sets*](../concept/README.md#scope-of-the-metal-and-wood-sets)).
Redstone, lapis and the rest of the "dust" resources are structurally excluded — there is no
raw ore to put in the bone meal recipe, and there is nothing in the wood for them to end up in.
Meanwhile the tree's leaves are pure decoration: they are vanilla blocks that drop vanilla
saplings and sticks.

The idea is a **second production line through the leaves**: leaves carry the dust resources,
the player dries them, sieves the dried leaves, and gets redstone or lapis out. Metals travel
through the trunk, dusts through the canopy.

## Prior art — the `restone_leaves` branch

Substantial work already exists, from the 1.18 era (Gradle 7.4.2, Yarn mappings, ~1,900 lines).
It is **not** portable as code — `main` is now 1.21.11 on Mojang mappings — but the models,
textures and the mechanic shape are version-independent and worth harvesting:

| What | State on the branch |
|---|---|
| `redstone_oak_leaves` | `LeavesBlock`, strength 0.2, random ticks; loot table drops itself, apple, oak sapling, stick |
| `DryRackBlock` / `DryRackEntity` / `DryRackRenderer` | Station with input/output slot and a progress timer. **Progress advances when the sky is visible and is set back by rain.** Double racks were started and deliberately disabled |
| `SeparatorBlock` / `SeparatorEntity` / `SeparatorRenderer` | The sieve. `maxCount(1)`, a 233-line model |
| Recipe type `iron_oak:drying` | Plus `drying_redstone_leaves`: tag `redstone_infused_leaves` → `dried_redstone_leaves` |
| `washing_redstone` | Dried leaves + water → `minecraft:redstone`. **The final step went through washing, not through the separator** — the sieve had a model and a block entity but no recipe type |
| Item properties | Dried leaves are furnace fuel (5) and compostable (0.3) |
| Tags | `iron_oak:redstone_infused_leaves`, `iron_oak:dried_leaves`, plus `minecraft:leaves` and `mineable/{axe,hoe}` |

Two things to take from this: the **sun/rain drying mechanic is already designed and built
once**, and the branch itself never decided whether the last step is a sieve or a wash.

## Constraints

- **Principle 2 still applies** ([concept](../concept/README.md#design-principles)): the line
  must cost the resource it produces. Redstone and lapis have no raw ore, but they do have the
  item itself — `8 × bone_meal + 1 × redstone` is the same bargain as the metals.
- **The matrix rule** ([`MAT-02`](../requirements/matrix.md#mat-02-a-partial-matrix-is-not-shipped)):
  a partial matrix is not shipped. The branch built exactly one combination
  (`redstone_oak_leaves`), which today's rules would reject. Either the line covers every wood
  type per resource, or `MAT-02` needs an explicit, written carve-out. This is the single
  biggest scoping question in this document.
- **No GUI, no power.** The rack and the separator are hopper-operated stations, like the fire
  bowl.
- **The crossing goal constrains the implementation *now*.** The stated intent is that the
  leaves line and the log line become combinable later — a "super tree" with metal logs *and*
  resource leaves. If the leaf resource is expressed as a *block type* (`redstone_oak_leaves`),
  then crossing means metal × resource × wood = 3 × 2 × 6 = **36 tree families**, and every new
  metal or resource multiplies it. If the leaf content is expressed as *data* on one leaves
  block per wood, crossing is nearly free. See the proposal.

## Options — how leaves carry a resource

### Option A — Own tree family per resource (what the branch built)
`redstone_oak`, `lapis_oak`, … each a separate tree grown from its own infused bone meal.
**For:** clean fantasy, clean separation from the metal line, matches the prior art.
**Against:** without care it means a block per resource per wood, and it makes the later
crossing combinatorially expensive.
**Matrix cost:** +1 axis (resources), multiplying with woods.

### Option B — The metal trees' own leaves carry a dust
An iron tree's leaves yield a resource assigned to iron.
**For:** no new trees at all.
**Against:** the mapping metal → dust is arbitrary (why would iron produce redstone?), and it
welds the two lines together permanently, which is the opposite of being able to cross them
deliberately.
**Matrix cost:** none.

### Option C — A separate leaf infusion on any tree
Leaves are infused independently of the trunk, so any metal × any dust is reachable.
**For:** maximal freedom, and the crossing goal falls out for free.
**Against:** two infusions per tree is hard to explain, and it lets a player skip the "grow a
dedicated tree" fantasy entirely.
**Matrix cost:** none, if the leaf content is data.

## Proposal

**Player-facing: Option A.** Dedicated resource trees, as the branch framed it — a redstone
tree is a redstone tree, seeded by redstone-infused bone meal, and the fantasy stays legible.

**Implementation: the leaf content is data, not a block type.** One infused leaves block per
wood type, with the resource carried as a blockstate on the block and a component on the item —
the same argument as [`tree-purity.design.md`](tree-purity.design.md) (quantised value on one
block beats N blocks). This keeps Option A's presentation while leaving the door open to the
super tree: crossing then means "a tree whose trunk carries iron and whose canopy carries
redstone", not a 36th tree family.

The chain, with the branch's mechanics kept where they were good:

```
resource-infused bone meal            8 bone meal + 1 redstone/lapis   (principle 2)
        │  applied to a vanilla sapling
        ▼
resource tree ──▶ infused leaves      harvested with shears; broken by hand → vanilla drops?
        │
        ▼
dry rack        progress advances under open sky, is set back by rain      (from the branch)
        │
        ▼
dried leaves    also fuel (5) and compostable (0.3)                        (from the branch)
        │
        ▼
separator (sieve) ──▶ redstone / lapis / …
```

**The last step should be the separator, not the washing basin** — the sieve is the mechanic
the line was conceived around, it gives the second station a purpose, and it keeps the two
lines distinguishable (metals get washed, leaves get sieved). The branch's `washing_redstone`
recipe is recorded as the cheaper alternative: it needs no new recipe type and would ride on
[`automated-washing.design.md`](automated-washing.design.md) for free. That trade — one fewer
subsystem versus a distinct identity for the line — is open question 3.

Resources to start with: **redstone and lapis**. Both have a vanilla item to seed from, both are
consumed in bulk, and neither is end-game gated. Coal, quartz, amethyst, emerald and diamond are
deliberately *not* in the first scope (question 2).

## Open questions

1. **All six woods, or one?** `MAT-02` says a partial matrix is not shipped. Six woods × 2
   resources is 12 leaf variants of data on 6 blocks — cheap under the proposed implementation,
   expensive under the branch's. If the line ships oak-only, the matrix rule needs an amendment
   that says so out loud.
2. **Which resources, and where does it stop?** Redstone and lapis are safe. Coal has an ore but
   no raw item; quartz is nether-flavoured; diamond and emerald would make a tree farm the best
   gem source in the game, which contradicts "not an ore multiplier"
   ([concept](../concept/README.md#what-iron-oak-is-not)).
3. **Sieve or wash for the last step?** A new `sieving` recipe type and the separator, or reuse
   the existing `washing` type and the proposed basin. Affects how many subsystems this line
   introduces.
4. **How are leaves harvested?** Shears only (a real cost, and it stops accidental harvesting
   while chopping), or by hand with a drop chance like vanilla leaves? The branch's loot table
   was a vanilla oak copy and did not decide.
5. **Does the dry rack stay weather-driven?** It is the most charming thing on the branch —
   drying in the sun, rain setting you back — and it is also the only mechanic in the mod that
   punishes the player for the weather. Worth keeping, worth confirming.
6. **What happens on the super tree?** One purity per tree or one per line; can a single tree
   be bred for both; and does the canopy compete with the trunk for yield. Depends on
   [`tree-purity.design.md`](tree-purity.design.md) being settled first.
7. **What is the yield relation?** How many leaves per tree, how many dried leaves per sieve
   pull, how much redstone per pull — all unmeasured, and `CON-Q6` (nobody measured the log
   line either) should be answered before a second line is balanced against it.

## Requirements this would mint

A new domain, `LEF`, in `docs/requirements/leaves.md` — all `planned` on approval:

- `LEF-01` Craft resource-infused bone meal
- `LEF-02` Infusing a sapling with it grows a resource tree
- `LEF-03` A resource tree's leaves carry that resource
- `LEF-04` Harvesting infused leaves
- `LEF-05` The dry rack dries leaves under open sky and is set back by rain
- `LEF-06` Dried leaves are fuel and compostable
- `LEF-07` The separator turns dried leaves into their resource
- `LEF-08` Both stations expose hopper access like the fire bowl
- `LEF-09` The matrix rule as it applies to woods × resources

## Verification

`runClient` for all of it, and the weather condition needs `/weather` to be part of the written
checklist — a rack that only misbehaves in rain is exactly the kind of thing that ships. Plus a
datagen check: any tree feature added here is generated output and must leave
`src/main/generated/` byte-identical on a second run
([`TRE-04`](../requirements/trees.md#tre-04-datagen-reproduces-the-shipped-tree-features) exists
because that assumption was already wrong once).

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial draft (#36). Inventories the `restone_leaves` prior art, proposes dedicated resource trees presented as families but implemented as data so the later crossing stays affordable. Seven open questions, `MAT-02` conflict flagged. |

*Last updated: 2026-08-20*
