---
domain: Trees
domain_code: TRE
status: active
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - infusion.md
  - burning.md
  - matrix.md
  - ../concept/balance.md
---

# Tree Requirements

> Part of the [Iron Oak requirements](README.md) · concept:
> [Concept](../concept/README.md) · numbers: [Balance](../concept/balance.md)

**Scope:** infused saplings, their growth, the trees they become, and the logs those trees
drop. Creating an infused sapling is [`infusion.md`](infusion.md); consuming the logs is
[`burning.md`](burning.md).

Code: `OreInfusedSaplingBlock`, `init/ModBlocks.java`, `init/ModSaplingGenerators.java`,
`init/ModConfiguredFeatures.java`, `src/main/generated/data/iron_oak/worldgen/`,
`data/iron_oak/loot_tables/blocks/`, `data/minecraft/tags/`.

---

### TRE-01: An infused sapling behaves like its vanilla counterpart

**Status:** done · **Issue:** —

WHEN the player interacts with an infused sapling THEN it SHALL behave exactly as the vanilla
sapling it was made from — same placement rules, same block settings, same instant break,
same reaction to vanilla bone meal.

`OreInfusedSaplingBlock` extends `SaplingBlock` and overrides nothing; the block settings are
`FabricBlockSettings.copyOf(Blocks.<WOOD>_SAPLING)`.

**Acceptance criteria** (verify: `runClient`)
- [ ] Placeable on the same soil blocks as the vanilla sapling, and only there
- [ ] Broken by hand instantly, drops itself
- [ ] Vanilla bone meal advances its growth stage as on a vanilla sapling
- [ ] Can be placed in a flower pot only if the vanilla one can (it cannot — no potted
      variant is registered)

---

### TRE-02: Growth follows vanilla rules

**Status:** done · **Issue:** — · **Open question:** CON-Q5

WHEN an infused sapling receives a random tick under vanilla growth conditions THEN it SHALL
grow at the vanilla rate, with no additional delay or requirement.

**Note:** no time cost is charged for farming ore. Whether it *should* be is
[CON-Q5](../concept/README.md#open-questions).

**Acceptance criteria** (verify: `runClient`)
- [ ] A sapling in light level ≥ 9 with room above grows within the vanilla time envelope
- [ ] The sapling does **not** grow in the dark, and does not grow when blocked above
- [ ] No metal-specific difference: iron, copper and gold saplings of the same wood behave
      identically

---

### TRE-03: A grown tree is made of infused logs of its own metal and wood

**Status:** done · **Issue:** —

WHEN an infused sapling grows THEN the resulting tree SHALL consist of infused logs of the
**same metal and the same wood type** as the sapling, with **vanilla** leaves of that wood
type and the vanilla trunk/foliage shape.

Jungle trees keep their vanilla decorators (cocoa beans 0.2, trunk and leaf vines 0.25).
There is no infused-leaves block; leaves are `minecraft:<wood>_leaves`.

**Acceptance criteria** (verify: `runClient`, `inspect`)
- [ ] All 18 combinations grow, and each yields logs of its own metal and wood type
- [ ] `src/main/generated/data/iron_oak/worldgen/configured_feature/<metal>_<wood>_tree.json`
      names `iron_oak:<metal>_<wood>_log` and `minecraft:<wood>_leaves`
- [ ] Tree silhouettes match their vanilla counterparts (dark oak thick trunk, spruce
      conifer, acacia fork)

---

### TRE-04: Datagen reproduces the shipped tree features

**Status:** broken · **Issue:** #30

WHEN `./gradlew runDatagen` runs THEN the emitted configured features SHALL be identical to
the committed ones — datagen output is generated, so re-running it must be a no-op.

**This is currently false.** The committed data in `src/main/generated/` is correct
(`<metal>_jungle_tree.json` → jungle log + jungle leaves), but
`ModConfiguredFeatures.bootstrap()` pairs three wood types with each other's shapes:

| Registered feature | Built by | Log used |
|---|---|---|
| `*_JUNGLE_TREE` | `oreSpruce(...)` | `*_SPRUCE_LOG` |
| `*_DARK_OAK_TREE` | `oreJungle(...)` | `*_JUNGLE_LOG` |
| `*_SPRUCE_TREE` | `oreDarkOak(...)` | `*_DARK_OAK_LOG` |

`ModSaplingGenerators` carries the mirror image of the same rotation in its id strings
(`COPPER_JUNGLE = generator("copper_spruce", …)`). Consequence: the next `runDatagen` will
silently rewrite 9 of the 18 tree features and jungle/spruce/dark oak saplings will start
growing each other's trees. The shipped 1.20.4 jar is unaffected, because it ships the
committed JSON.

**Out of scope for the documentation baseline** — fixing it is a code change in
`area:worldgen`, tracked as #30.

**Acceptance criteria** (verify: `runDatagen`)
- [ ] `./gradlew runDatagen` leaves `git status` clean
- [ ] Each `bootstrap()` line pairs `<metal>_<wood>_TREE` with `ore<Wood>(<METAL>_<WOOD>_LOG)`
- [ ] Each `ModSaplingGenerators` entry's id string matches its own wood type

---

### TRE-05: Infused logs are vanilla logs for every other purpose

**Status:** done · **Issue:** —

WHEN a vanilla recipe, tag or tool refers to `minecraft:<wood>_logs` THEN infused logs of
that wood type SHALL be included.

They are added to `minecraft:<wood>_logs` as both block and item tags. This is what makes
axes, stripping-adjacent recipes and — deliberately — the planks recipe work.

**Why it matters:** crafting infused logs into planks destroys the ore content. That is
intended (concept principle 4) and is a *consequence* of tag membership, not a separate rule.

**Acceptance criteria** (verify: `inspect`, `runClient`)
- [ ] Each of the six `data/minecraft/tags/blocks/<wood>_logs.json` lists all three metals
- [ ] The matching item tags do the same
- [ ] Crafting an infused log into planks yields **vanilla** planks and no shred, ash or ore
- [ ] An axe is the effective tool, at the vanilla log speed

---

### TRE-06: Breaking infused blocks drops the block itself

**Status:** done · **Issue:** —

WHEN an infused log or sapling is broken THEN it SHALL drop exactly one of itself, subject
only to `survives_explosion`.

There is no ore loss on breaking, no fortune interaction and no shred drop — the metal only
leaves the wood in the fire bowl.

**Acceptance criteria** (verify: `runClient`, `inspect`)
- [ ] All 36 loot tables exist (18 logs, 18 saplings) and each drops its own block
- [ ] Fortune does not change the drop
- [ ] Blowing up a log may destroy it (vanilla explosion behaviour)

---

### TRE-07: Infused trees never generate naturally

**Status:** done · **Issue:** —

WHEN a world generates THEN no infused tree SHALL appear — infused trees exist only where a
player planted and grew an infused sapling.

The mod emits configured features only; there are no placed features and no biome
modifications (`ModWorldGenerator` writes `configured_feature/` only).

**Acceptance criteria** (verify: `inspect`, `runClient`)
- [ ] `src/main/generated/data/iron_oak/worldgen/` contains no `placed_feature/` directory
- [ ] No biome modification API is used anywhere in `src/main/java`
- [ ] A freshly generated world contains no infused logs

---

## Open questions

- **CON-Q5** Should infused saplings grow more slowly than vanilla ones? See
  [TRE-02](#tre-02-growth-follows-vanilla-rules).
- **TRE-Q1** Should any wood type differ in yield or behaviour, or is the wood type purely
  cosmetic? Today it only changes shape and appearance (see
  [`../concept/balance.md`](../concept/balance.md)).

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial. Records the datagen/source divergence for jungle, spruce and dark oak as TRE-04 (`broken`, #30), verified against the committed generated JSON. |

*Last updated: 2026-08-20*
