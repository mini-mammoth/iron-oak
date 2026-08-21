---
domain: Burning
domain_code: BRN
status: active
last_updated: 2026-08-21
version: 2
related:
  - README.md
  - trees.md
  - washing.md
  - ../concept/balance.md
---

# Burning Requirements — the fire bowl

> Part of the [Iron Oak requirements](README.md) · concept:
> [Concept](../concept/README.md) · numbers: [Balance](../concept/balance.md)

**Scope:** the fire bowl block — crafting, lighting, holding one log, turning it into ash,
handing the ash out, and burning the player who touches it. The logs come from
[`trees.md`](trees.md); the ash is consumed in [`washing.md`](washing.md).

Code: `FireBowlBlock`, `FireBowlEntity`, `FireBowlRenderer`, `BurningRecipe`,
`init/ModRecipes.java`, `data/iron_oak/recipes/burning_<metal>_ash.json`,
`data/minecraft/tags/blocks/campfires.json`.

Statuses below are stated against **`main` (1.20.4)**. Where a port behaves differently, the
requirement says so and names the issue.

---

### BRN-01: Craft the fire bowl

**Status:** done · **Issue:** —

WHEN the player arranges 5 × `iron_ingot` as a frame with `iron_bars` in the centre and a
`bucket` at the bottom THEN the system SHALL produce one fire bowl.

**Acceptance criteria** (verify: `inspect`, `runClient`)
- [ ] The recipe is shaped `I I` / `I#I` / `IBI`
- [ ] The block is mined with a pickaxe (`mineable/pickaxe`) and uses cauldron block settings
- [ ] Its collision box is 12/16 of a block high, so the player stands *in* it, not on it

---

### BRN-02: Accept exactly one burnable log at a time

**Status:** done · **Issue:** —

WHEN the player right-clicks the fire bowl with an item that matches a burning recipe AND
the input slot is empty THEN the system SHALL store exactly one of that item and consume one
from the player's stack.

IF the input slot is occupied, or the held item matches no burning recipe, THEN the
interaction SHALL be rejected without consuming anything.

**Why:** one log at a time is what makes the bowl a throughput constraint the player has to
build around, rather than a furnace with a queue.

**Acceptance criteria** (verify: `runClient`)
- [ ] Any infused log of any metal is accepted; a vanilla log is not
- [ ] A second log cannot be inserted while one is stored
- [ ] Exactly one item leaves the player's stack, and a wood-place sound plays
- [ ] Right-clicking with a non-matching item does nothing (no sound, no consumption)

---

### BRN-03: Burn one log into one ash of its metal

**Status:** broken · **Issue:** #28 · **Open question:** CON-Q3

WHEN the fire bowl is lit AND holds an input THEN it SHALL advance a cook timer each tick,
and on reaching the recipe's cook time SHALL replace the input with the recipe result —
one `iron_oak:<metal>_ash` per infused log of that metal, worth 0.2 XP by the recipe.

**The three-duration defect is fixed; the requirement is not.** #28 was real: the same log
burned for 200 ticks when a player inserted it, 150 after a hopper insert following a
completed cook, and 200 again after a chunk reload — because the duration lived in a cached
field that only the player path ever set and nothing ever persisted.

The fix **deleted the field**. `FireBowlEntity.cookingTotalTime(...)` derives the duration
from the matched recipe on demand and falls back to `ModRecipes.DEFAULT_COOKING_TIME` when an
input matches no recipe, so there is no stored value left to be wrong or to be lost.
`ModRecipesTest` pins that 200-tick default out of the committed recipe JSON, and
`FireBowlGameTest` burns a log to ash and feeds one by hopper.

Two criteria still hold this open. The recipe declares 0.2 XP and nothing awards it
([BRN-Q1](#open-questions)), and nobody has confirmed in game that both insertion paths now
feel the same. [CON-Q3](../concept/README.md#open-questions) has lost its premise rather than
been answered: 200 is the only duration any path can now produce.

**Acceptance criteria** (verify: `runClient`, `test`, `gametest`)
- [ ] A log takes the same time to burn whether inserted by hand or by hopper
- [ ] The duration survives a save/reload mid-burn
- [ ] Each metal's logs produce that metal's ash; wood type does not matter (tag-matched)
- [ ] Burning grants 0.2 XP, or the recipe stops claiming it does

---

### BRN-04: Keep burning briefly after the last log

**Status:** partial · **Issue:** #28

WHEN a lit fire bowl has an empty input THEN it SHALL remain lit for 100 ticks (5 s) and then
extinguish itself.

**Why:** this window is the whole automation design — a bowl that is being fed stays lit, a
starved one goes out and has to be re-lit by hand (concept: *Progression*).

**Partial:** the persistence half is fixed. `unlitTime` is written as `unlit_time` and read
back with a `0` default so a bowl saved before the fix still loads (#28), and
`FireBowlEntityTest` round-trips it through a `ValueOutput`/`ValueInput` pair. What is
unchecked is what a player sees — that an idle bowl really does go out after about five
seconds, and that a log arriving inside the window is cooked without re-lighting. Both name
`runClient`.

**Acceptance criteria** (verify: `runClient`, `test`)
- [ ] An idle lit bowl goes out after ~5 s
- [ ] A log arriving inside the window is cooked without re-lighting
- [ ] The countdown continues rather than restarting across a save/reload
- [ ] While unlit, stored cooking progress decays by 2 per tick down to 0

---

### BRN-05: Hand out ash, and lose it if the bowl is destroyed while lit

**Status:** done · **Issue:** —

WHEN a cook completes THEN the result SHALL go into the output slot, stacking with an
identical result; IF the output slot cannot take it THEN the result SHALL be scattered on
the ground.

WHEN the player right-clicks a bowl with an empty hand THEN stored items SHALL be dropped —
but only while the bowl is unlit; while lit the player takes fire damage and receives
nothing.

WHEN a **lit** bowl is broken THEN its contents SHALL be lost; when an unlit one is broken
they SHALL drop.

**Why:** losing the contents of a burning bowl is deliberate (`FireBowlBlock` says so) — the
player is expected to put the fire out first.

**Acceptance criteria** (verify: `runClient`)
- [ ] Ash stacks in the output slot up to the stack limit, then scatters
- [ ] Empty-hand right-click on an unlit bowl drops input and output
- [ ] Empty-hand right-click on a lit bowl deals 1.0 fire damage and drops nothing
- [ ] Breaking a lit bowl destroys the contents; breaking an unlit one drops them

---

### BRN-06: Automate with vanilla hoppers

**Status:** done · **Issue:** —

WHEN a hopper is attached THEN the fire bowl SHALL expose: top → input slot, bottom → output
slot, sides → both. Insertion SHALL be accepted only into an empty input slot and only for
items with a burning recipe; extraction SHALL be allowed only from the output slot.

**Acceptance criteria** (verify: `runClient`, `test`, `gametest`)
- [ ] A hopper above feeds logs one at a time; a chest below collects the ash
- [ ] A hopper cannot pull the unburnt log back out of the input slot
- [ ] A hopper cannot push a non-burnable item in
- [ ] The bowl keeps running as long as logs keep arriving (with BRN-04)

**Note:** hopper-fed logs currently burn for the wrong duration — that is BRN-03, not a
defect of the access rules.

---

### BRN-07: Light the bowl like a campfire

**Status:** partial · **Issue:** #28

WHEN the player uses flint and steel or a fire charge on the fire bowl THEN it SHALL light.
WHEN a burning projectile hits it THEN it SHALL also light.

The bowl is a member of `minecraft:campfires` and carries the `WATERLOGGED` property purely
so vanilla recognises it as lightable.

**Partial:** the burning-arrow path is fixed. `FireBowlBlock.onProjectileHit` used to require
`LIT` to already be `true` before setting `LIT` to `true`, so the branch was unreachable in
every state where it would have changed something (#28); it now carries
`CampfireBlock.onProjectileHit`'s condition verbatim — unlit and not waterlogged — and
`FireBowlGameTest` fires a flaming arrow at an unlit bowl. Flint and steel on a *loaded* bowl
was the other half of this, fixed as #27 defect 2.

On this line (`v1.21.1`, #54), the #27 defect 2 shape reappeared during the down-port:
`useItemOn`'s "no recipe matches this held item" branch returned
`ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION`, which runs `useWithoutItem`
first — and on a loaded bowl, `useWithoutItem`'s own `SUCCESS` (spawning the contents)
consumed the interaction before flint and steel's `useOn` ever ran. Fixed to
`SKIP_DEFAULT_BLOCK_INTERACTION`, which is this version's "let the item act" value.
`FireBowlGameTest.flintAndSteelLightsAnEmptyBowl` and `.flintAndSteelLightsALoadedBowl` now
cover both halves in `gametest`. None of the three criteria has been ticked at `runClient`
on this line.

**Acceptance criteria** (verify: `runClient`, `gametest`)
- [ ] Flint and steel lights an unlit bowl, loaded or empty
- [ ] A flaming arrow lights an unlit bowl
- [ ] Lighting an already lit bowl is a no-op

---

### BRN-08: Water puts it out

**Status:** done · **Issue:** —

WHEN water is placed into a **lit** fire bowl THEN it SHALL extinguish, play
`ENTITY_GENERIC_EXTINGUISH_FIRE`, drop to `LIT = false` and schedule a fluid tick.
IF the bowl is unlit THEN it SHALL NOT accept the fluid at all.

**Acceptance criteria** (verify: `runClient`)
- [ ] A water bucket emptied into a lit bowl extinguishes it and is consumed
- [ ] The same on an unlit bowl is rejected — the bowl does not become waterlogged
- [ ] Stored items survive the extinguishing (they are not a lit-bowl break, cf. BRN-05)

---

### BRN-09: Burn whoever stands in it

**Status:** done · **Issue:** —

WHEN a living entity collides with a lit fire bowl THEN it SHALL take 1.0 in-fire damage,
unless it is fire-immune or wearing Frost Walker boots.

**Acceptance criteria** (verify: `runClient`)
- [ ] The player takes damage standing in a lit bowl, none in an unlit one
- [ ] Frost Walker boots and fire-immune mobs are exempt
- [ ] Dropped items and other non-living entities are unaffected

---

### BRN-10: Show what is inside

**Status:** done · **Issue:** — (see port note)

WHEN the fire bowl holds an input or an output THEN the client SHALL render that item lying
in the bowl, and a lit bowl SHALL emit campfire smoke on ~11 % of client ticks.

`FireBowlEntity` funnels every container write through `markUpdated()`, which calls
`sendBlockUpdated`, and implements `getUpdatePacket`/`getUpdateTag` so a freshly loaded chunk
renders correctly.

**Port note (historical):** while 1.21.11 was still a branch, the input was stored but not
rendered there and a loaded bowl could not be lit — #27, both tracing to `setInput` bypassing
the one method that pushes an update to clients. 1.21.11 is `main` now and both are fixed. The
note is kept as the worked example of what a port note is for.

**Acceptance criteria** (verify: `runClient`)
- [ ] An inserted log is visible in the bowl immediately, for a second player too
- [ ] The ash is visible after the cook completes
- [ ] Leaving and re-entering the chunk keeps both visible
- [ ] Smoke particles rise from a lit bowl

---

## Open questions

- **CON-Q3** Which cook duration is the intended one — see
  [BRN-03](#brn-03-burn-one-log-into-one-ash-of-its-metal).
- **BRN-Q1** Should burning grant its declared 0.2 XP? The recipe carries the value and
  nothing awards it (the burning path never calls the XP hook the furnace uses).
- **BRN-Q2** Should a lit bowl really destroy its contents when broken, or was that a
  shortcut? The code comment ("all items get lost. :/") reads more like a shrug than a
  decision.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial. BRN-03/04/07 land as `broken`/`partial` against #28, verified in the 1.20.4 source; BRN-10 records #27 as a port-only failure. |
| 2026-08-21 | 2 | BRN-03 and BRN-06 name `test` and `gametest`, BRN-04 names `test`, BRN-07 names `gametest` (#43). The three #28 defects each have a test now, and `FireBowlGameTest` records why the BRN-05 break rules have none — `BlockEntity.preRemoveSideEffects` changed them under the mod, and BRN-Q2 has to be answered before a test can freeze either behaviour. |
| 2026-08-21 | 3 | BRN-03, BRN-04, BRN-07 and BRN-10 describe the fixed tree (#47). All three #28 defects and both #27 defects were fixed before this catalogue merged, so the failure tables described code that no longer existed — BRN-03's cited a cached field the fix deleted. Statuses are unchanged: what each entry now names is the criterion still waiting at `runClient`, and Mojang names replace the Yarn-era ones in BRN-10. |

*Last updated: 2026-08-21*
