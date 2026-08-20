---
domain: Automated Washing
domain_code: DES
status: draft
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - ../requirements/washing.md
  - ../requirements/burning.md
  - ../concept/README.md
---

# Design: Automated Washing

> Status: **draft** — not approved, nothing may be implemented from it.
> Requirements: [`WSH-04`](../requirements/washing.md#wsh-04-automated-washing) ·
> Open question: [`CON-Q4`](../concept/README.md#open-questions)

## Problem

The chain from log to ash automates cleanly — hopper in, fire bowl, hopper out. Then it
stops dead: the only way to turn ash into shreds is a player right-clicking a water source
block, one item per click ([`WSH-01`](../requirements/washing.md)). A tree farm that produces
ash in bulk has no way to consume it, so the mod's stated endgame — "automating the chain is
the intended design, not a shortcut around it"
([concept principle 3](../concept/README.md#design-principles)) — is unreachable. The README
has advertised this step as *coming soon* since 1.1.x.

## Constraints

- **No machine GUI, no power** ([principle 5](../concept/README.md#design-principles)).
  Vanilla hoppers are the automation, as with the fire bowl.
- **Vanilla-shaped** ([principle 1](../concept/README.md#design-principles)): operated by
  right-click and hoppers, no custom screen.
- **The manual path must survive.** Washing by hand in a lake is the mod's most legible
  moment; automation may not obsolete it, and it is what a new player meets first.
- **Throughput must not beat the manual path per unit of ash.** Automation buys the player
  *unattendedness*, not a better yield — the same reasoning as the fire bowl's one-log slot.
- **The mod already has a station pattern**, three times over: `FireBowlEntity` (2 slots,
  progress timer, side-dependent hopper access, block-entity renderer showing its contents),
  and on the `restone_leaves` branch `DryRackEntity` and `SeparatorEntity` repeat it verbatim.
  A fourth instance should not invent a fourth shape.
- **`WashingRecipe` already carries a cook time** — the serializer defaults it to 200 ticks
  and `OreInfusedAsh` ignores it entirely
  ([`WSH-02`](../requirements/washing.md#wsh-02-washing-is-instant-and-hands-the-shred-over-as-an-item-entity)).
  Any station design gets its duration for free from data that already ships.

## Options

### Option A — A washing basin block

A new block in the fire bowl's image: two slots (ash in, shreds out), a progress timer driven
by the washing recipe's cook time, hopper access top/bottom/sides, a renderer showing what is
inside. Needs water: either it must be placed adjacent to a water source, or it is filled with
a bucket, or it is waterloggable.

**For:** symmetric with the fire bowl, so the player already knows how to use it; gives the
ignored recipe cook time a purpose; the throughput limit falls out of the one-item slot for
free; reuses `ImplementedInventory` and the existing renderer pattern; the same code shape is
needed twice more for the leaves line, so building it well pays three times.
**Against:** a new block, model, texture, recipe and loot table — the largest of these options.
**Matrix cost:** none. One block, metal-agnostic, like the fire bowl.

### Option B — Water cauldron interaction

Hopper feeds ash into a vanilla water cauldron; shreds drop out or go to a hopper below.

**For:** no new block; a cauldron is the intuitive first guess for most players
([`WSH-Q1`](../requirements/washing.md#open-questions)).
**Against:** vanilla cauldrons have no inventory, so this needs a block entity attached to a
vanilla block or a mixin — the mod has no mixins today and the mixin config is deliberately
empty. Cauldron levels also model three states, not a progress bar, so the duration has
nowhere to live.
**Matrix cost:** none.

### Option C — Dispenser dispensing ash into water

Register a dispenser behaviour: a dispenser pointed at water converts one ash per pulse.

**For:** cheapest by far; no new block; redstone-native and very vanilla.
**Against:** output lands as loose item entities that need a water stream or hopper minecart to
collect — awkward and lossy. No visible progress, so the mechanic is invisible. Dispenser
behaviours are also easy to miss in the recipe book: nothing tells the player it exists.
**Matrix cost:** none.

### Option D — Ash converts as a dropped item in water

Any ash *item entity* that lands in water converts itself after a delay. Automated with a
dropper and a water stream; collected wherever the stream ends.

**For:** no new block, no GUI, works with pure vanilla contraptions; reads beautifully — you
throw ash in the river and metal separates out.
**Against:** no throughput limit at all, so it beats the manual path (violates the constraint
above) and invites lag with stacks of hundreds; and it is indistinguishable from the *bug*
reported in [#15](https://github.com/mini-mammoth/iron-oak/issues/15) ("it just starts
floating instead of turning into shreds"), which would make that report permanently
ambiguous.
**Matrix cost:** none.

## Proposal

**Option A, a washing basin.** It is the only option that keeps the throughput limit, gives
the player a visible progress state, and matches the station pattern the mod already uses
three times. Options C and D are cheap but invisible; B needs a mixin for a block the mod does
not own.

Mechanics, all numbers first guesses:

| Aspect | Proposal | Note |
|---|---|---|
| Slots | 2 — ash in, shreds out | Mirrors `FireBowlEntity` exactly |
| Capacity | 1 ash at a time | The throughput limit; same reasoning as the fire bowl's one log |
| Duration | The washing recipe's cook time, 200 ticks | Already in the shipped data, currently ignored |
| Hopper access | top → input, bottom → output, sides → both | Identical to the fire bowl, so contraptions transfer |
| Water requirement | Must be waterlogged, or adjacent to a water source | Undecided — see open questions |
| Rendering | Block entity renderer showing ash/shreds, like the fire bowl | Reuse `FireBowlRenderer`'s approach |
| Recipe | Something cheaper than the fire bowl; the fire bowl costs 5 iron + bars + bucket | Undecided |
| XP | Whatever the manual path does — today neither grants the recipe's 0.2 | Resolve together with `WSH-02` |

The manual right-click path stays exactly as it is. The basin is the *bulk* path, not a
replacement.

## Open questions

1. **How does water get in?** Waterlogged (a bucket fills it, it stays filled), adjacent to a
   water source (free, and encourages riverside builds), or consumed per wash (a real cost,
   and a second input to automate)? Each produces a different build.
2. **Does the basin need to be under the sky, or heated, or neither?** The leaves line's dry
   rack already uses "sky visible" as a condition; reusing that idea here would tie the two
   lines together thematically, but there is no reason washing should care about weather.
3. **Does the water get dirty?** A basin that needs its water replaced after N washes adds a
   consumable and an automation problem. Tempting; possibly one mechanic too many.
4. **Does the basin also accept dried leaves** once the leaves line exists? The existing
   `washing_redstone` recipe on the `restone_leaves` branch washes dried leaves into redstone,
   which means the basin would serve both lines for free — a strong argument for building it
   before the leaves line rather than after.
5. **Is one basin per farm enough?** With 200 ticks per ash, a bulk farm needs many. Either
   the duration drops, the capacity rises, or players build banks of basins — the third is
   probably the intended answer, but it should be a decision.

## Requirements this would mint

On approval, in [`../requirements/washing.md`](../requirements/washing.md), all `planned`:

- `WSH-06` Craft the washing basin
- `WSH-07` The basin washes one ash at a time over the recipe's duration
- `WSH-08` The basin exposes hopper access like the fire bowl
- `WSH-09` The basin only operates with water present
- `WSH-10` The basin shows its contents and progress
- `WSH-04` becomes the umbrella that these five close

## Verification

No test suite, so all of it is a `runClient` checklist: feed a basin by hopper, confirm one
ash at a time, confirm the duration, confirm hoppers cannot pull unwashed ash, leave and
re-enter the chunk and confirm the progress and the rendered contents survive — the fire bowl
lost exactly that battle three times (#27, #28), and the basin will inherit the same
block-entity sync trap if it copies the fire bowl's code without copying its fixes.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial draft (#36). Four options, basin proposed. Five open questions, none resolved. |

*Last updated: 2026-08-20*
