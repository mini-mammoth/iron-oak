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

**This does not hold.** Three cook durations are observable for the same log
(all three verified on `main`, all three pre-existing — see #28):

| Insertion path | Duration | Cause |
|---|---|---|
| Player right-click | 200 ticks | `setInput` passes the recipe's cook time |
| Hopper, after one completed cook | 150 ticks | hoppers go through `setStack`, which never sets a cook time; `litServerTick` left the field at `DEFAULT_COOKING_TOTAL_TIME` |
| Hopper, after a chunk reload | 200 ticks | `cooking_total_time` is not written to NBT, so the field falls back to its initialiser |

The recipe JSON declares no `cookingtime`, so the serializer default of 200 is the intended
number for all paths. Whether 150 was ever meant to exist is
[CON-Q3](../concept/README.md#open-questions).

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

**Partial:** `unlitTime` is not persisted, so the countdown restarts on every chunk load
(#28). An idle bowl in a loaded-and-unloaded chunk can stay lit indefinitely.

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

**Partial:** the burning-arrow path can never fire. `FireBowlBlock.onProjectileHit` requires
`LIT` to already be `true` before setting `LIT` to `true` — the branch is unreachable in any
state where it would change something (#28). Flint and steel works.

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

`FireBowlEntity` syncs through `setStack` → `updateListeners`, plus
`toUpdatePacket`/`toInitialChunkDataNbt` so a freshly loaded chunk renders correctly.

**Port note:** on the 1.21.11 branch the input is stored but not rendered, and a loaded bowl
cannot be lit — #27. Both trace to `setInput` bypassing `setStack`. On `main` the behaviour
is correct; do not close #27 against this requirement.

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

*Last updated: 2026-08-21*
