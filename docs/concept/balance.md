---
domain: Balance
domain_code: BAL
status: active
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - ../requirements/README.md
---

# Balance — every tunable number and where it lives

**Answers the question: _what is the number, and which file decides it?_**

Every row was read out of the repository at version `1.2.1+1.20.4`, not from memory. The
**Where** column is the authority: if you change the number, change it there and update this
table in the same commit. Nothing here is generated — this table is maintained by hand
because the numbers are scattered across Java literals and recipe JSON, and that is exactly
why they were invisible before.

Ticks: 20 ticks = 1 second.

---

## Entry into the loop

| What | Value | Where |
|---|---|---|
| Infused bone meal recipe | 8 × `minecraft:bone_meal` + 1 × `minecraft:raw_<metal>` (centre) → **4** infused bone meal | `data/iron_oak/recipes/crafting_<metal>_bone_meal.json` |
| Infusion success chance | **deterministic** — every use on a matching sapling converts it | `OreInfusedBoneMeal.useOnBlock` / `useOnOakSapling` |
| Bone meal consumed per infusion | 1 | `OreInfusedBoneMeal:47` |
| Infusion particles | 15 (vanilla bone-meal particles) | `OreInfusedBoneMeal:43` |
| Infusion sound | `BLOCK_ENCHANTMENT_TABLE_USE`, volume 0.3, pitch 1.0 | `OreInfusedBoneMeal:44` |
| Accepted saplings | the 6 vanilla saplings, per metal, as an explicit map | `ModItems` — `Map.of(...)` per `*_BONE_MEAL` |

**Open question (CON-Q1):** the javadoc says "has a chance to convert the sapling", the code
always converts. One of the two is wrong; the number above documents the code.

## Trees and logs

| What | Value | Where |
|---|---|---|
| Sapling block settings | copied from the matching vanilla sapling | `ModBlocks` — `FabricBlockSettings.copyOf(Blocks.<WOOD>_SAPLING)` |
| Growth behaviour | plain vanilla `SaplingBlock` — vanilla random tick, light level and bone-meal growth all apply unchanged | `OreInfusedSaplingBlock` (no overrides) |
| Log block settings | copied from the matching vanilla log (hardness, tool, sounds) | `ModBlocks` — `FabricBlockSettings.copyOf(Blocks.<WOOD>_LOG)` |
| Log drop | 1 × itself, `survives_explosion` only — no ore loss when breaking | `data/iron_oak/loot_tables/blocks/<metal>_<wood>_log.json` |
| Sapling drop | 1 × itself | `data/iron_oak/loot_tables/blocks/<metal>_<wood>_sapling.json` |
| Leaves | **vanilla** leaves, no infused variant | `ModConfiguredFeatures.ore<Wood>` |
| Trunk placer / foliage / size | vanilla shape per wood type, e.g. oak `StraightTrunkPlacer(4, 2, 0)` + `BlobFoliagePlacer(2, 0, 3)`; jungle additionally keeps cocoa beans (0.2) and vines (0.25) | `ModConfiguredFeatures:63-90` |
| Logs in vanilla tags | yes — infused logs are added to `minecraft:<wood>_logs` (blocks and items) | `data/minecraft/tags/{blocks,items}/<wood>_logs.json` |

The vanilla tag membership is load-bearing: it is what makes infused logs craftable into
planks (destroying the ore, by design) and usable by every vanilla log recipe and tool.

**Open question (CON-Q5):** growth speed is exactly vanilla, so a farmed ore has no time
cost beyond the tree's own growth.

## Fire bowl (burning)

| What | Value | Where |
|---|---|---|
| Crafting recipe | 5 × `iron_ingot` (frame) + 1 × `iron_bars` (centre) + 1 × `bucket` (bottom) → 1 fire bowl | `data/iron_oak/recipes/crafting_fire_bowl.json` |
| Block settings | copied from `minecraft:cauldron` | `ModBlocks:19` |
| Collision / outline shape | 16 × **12** × 16 — a 12/16 block high bowl | `FireBowlBlock:43` |
| Fire damage while lit | 1.0 per collision tick; skipped for fire-immune entities, non-living entities and Frost Walker wearers | `FireBowlBlock:44`, `doFireDamage` |
| Slots | 2 — input (0), output (1) | `FireBowlEntity:36-39` |
| Hopper access | top → input, bottom → output, sides → both | `FireBowlEntity.getAvailableSlots` |
| Input capacity | exactly 1 item, and only while the input slot is empty | `FireBowlEntity.canInsert`, `FireBowlBlock.onUse` |
| Cook time, player insertion | **200 ticks** (10 s) — the recipe's cook time, defaulted by the serializer | `ModRecipes:32,41` (`CookingRecipeSerializer<>(..., 200)`), `FireBowlBlock.onUse` |
| Cook time, hopper insertion | **150 ticks** (7.5 s) after the first completed cook — hoppers write the slot directly and never set a cook time, so the field keeps its last value, which `litServerTick` reset to `DEFAULT_COOKING_TOTAL_TIME` | `FireBowlEntity:43,155`, `FireBowlEntity.setStack` |
| Cook time, first hopper insertion after loading the chunk | 200 ticks — `cooking_total_time` is never written to NBT, so the field falls back to its initialiser | `FireBowlEntity:42`, `readNbt` |
| Afterburn window | **100 ticks** (5 s) with an empty input before the bowl goes out | `FireBowlEntity:46`, `litServerTick` |
| Cooldown while unlit | cooking progress decays by 2 per tick, floored at 0 | `FireBowlEntity:194` |
| Burning XP | 0.2 per log | `data/iron_oak/recipes/burning_<metal>_ash.json` |
| Ash yield | 1 ash per log, any wood type of that metal (tag-matched) | `data/iron_oak/recipes/burning_<metal>_ash.json` |
| Output stacking | stacks in the output slot; overflow is scattered on the ground | `FireBowlEntity.litServerTick` |
| Smoke particles | 2–3 particles on 11 % of client ticks | `FireBowlEntity:141-143` |
| Retrieving items by hand | right-click with an empty hand — **only while unlit**; while lit it burns you and returns nothing | `FireBowlBlock.onUse` |
| Breaking a lit bowl | contents are lost on purpose | `FireBowlBlock.onStateReplaced` |
| Ignitable like a campfire | yes — the bowl is in `minecraft:campfires`, and carries `WATERLOGGED` purely to be recognised as lightable | `data/minecraft/tags/blocks/campfires.json`, `FireBowlBlock:67` |
| Mining tool | pickaxe | `data/minecraft/tags/blocks/mineable/pickaxe.json` |

**Open question (CON-Q3):** the three different cook times above are not a design — they are
what the code does. #28 tracks it.

## Washing

| What | Value | Where |
|---|---|---|
| Trigger | right-click a water **source** block while holding ash, main hand only | `OreInfusedAsh.use` |
| Duration | instantaneous — the washing recipe's 200-tick cook time is parsed and then ignored | `OreInfusedAsh.use`, `ModRecipes:41` |
| Ash consumed | 1 per use | `OreInfusedAsh:55` |
| Shred yield | 1 shred per ash | `data/iron_oak/recipes/washing_<metal>_shred.json` |
| Washing XP | 0.2 declared in the recipe — **never granted**, nothing reads it | `data/iron_oak/recipes/washing_<metal>_shred.json` |
| Output delivery | dropped as an item entity at the water block, pickup delay 40 ticks (2 s) | `OreInfusedAsh:57-59` |
| Sound | `ENTITY_GENERIC_SPLASH`, volume 0.5 | `OreInfusedAsh:63` |
| Automation | none — no block, cauldron or dispenser path exists | — |

**Open question (CON-Q4):** the README advertises "Ore Washing — *coming soon*". Nothing is
built.

## Refining

| What | Value | Where |
|---|---|---|
| Shreds → raw ore | 9 shreds (3×3) → 1 × `minecraft:raw_<metal>` | `data/iron_oak/recipes/crafting_<metal>_ore_from_shreds.json` |
| Shred → nugget (iron) | smelting, 50 ticks, 0.1 XP → 1 × `minecraft:iron_nugget` | `data/iron_oak/recipes/smelting_iron_nugget.json` |
| Shred → nugget (gold) | smelting, 50 ticks, 0.1 XP → 1 × `minecraft:gold_nugget` | `data/iron_oak/recipes/smelting_gold_nugget.json` |
| Shred → nugget (copper) | **does not exist** — vanilla has no copper nugget | — |
| Blasting variant | none — the shreds are furnace-only | — |

**Open question (CON-Q2):** copper's missing nugget arm. Either it is `wontfix` because
vanilla has no such item, or Iron Oak adds its own copper nugget.

## What is deliberately not balanced

- **Yield per tree is unmeasured.** Nobody has counted logs per grown tree per wood type, so
  the ore-in / ore-out ratio of the whole chain is unknown. The chain is "roughly
  break-even by feel". Recorded as **CON-Q6**; no requirement asserts a ratio.
- **No difficulty or config knobs.** There is no config file; every number above is
  compiled in or shipped as data. A server owner can override the JSON with a data pack, but
  not the Java literals.
- **No wood-type differentiation.** All six woods yield the same ash per log; the wood type
  only changes the tree shape and the log's appearance. Whether that is intended flatness or
  a missed opportunity has never been decided.
