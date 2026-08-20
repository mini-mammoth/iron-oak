---
domain: Washing
domain_code: WSH
status: active
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - burning.md
  - refining.md
  - ../concept/balance.md
---

# Washing Requirements

> Part of the [Iron Oak requirements](README.md) · concept:
> [Concept](../concept/README.md) · numbers: [Balance](../concept/balance.md)

**Scope:** turning infused ash into shreds by washing it in water. The ash comes from
[`burning.md`](burning.md); the shreds are spent in [`refining.md`](refining.md).

Code: `OreInfusedAsh`, `WashingRecipe`, `init/ModRecipes.java`,
`data/iron_oak/recipes/washing_<metal>_shred.json`.

---

### WSH-01: Wash ash in water to get a shred

**Status:** done · **Issue:** —

WHEN the player right-clicks a water **source** block while holding infused ash in the main
hand THEN the system SHALL consume one ash and produce one `iron_oak:<metal>_shred` of the
matching metal.

The recipe is a `iron_oak:washing` recipe, matched by item (not by tag) — one recipe per
metal.

**Acceptance criteria** (verify: `runClient`, `inspect`)
- [ ] All three ash items wash into their own metal's shred
- [ ] Exactly one ash is consumed per use
- [ ] Three `washing_<metal>_shred.json` recipes exist and match on the ash item

---

### WSH-02: Washing is instant and hands the shred over as an item entity

**Status:** done · **Issue:** —

WHEN a wash succeeds THEN the shred SHALL be spawned as an item entity at the water block
with a 40-tick (2 s) pickup delay, attributed to the acting player, accompanied by
`ENTITY_GENERIC_SPLASH` at volume 0.5.

**Note:** the washing recipe declares a 200-tick cook time (serializer default) and 0.2 XP.
Neither is used — `OreInfusedAsh` reads only the recipe's result. Washing is instantaneous
and grants no XP.

**Why the pickup delay:** it keeps the shred visibly *in the water*, which is the whole
readability of the mechanic — you see the metal separate out.

**Acceptance criteria** (verify: `runClient`)
- [ ] The shred appears in the water and cannot be picked up for ~2 s
- [ ] The splash sound plays for nearby players
- [ ] The shred is not lost when it lands in flowing water (it drifts, vanilla behaviour)
- [ ] Either the recipe's XP is awarded, or the recipe stops declaring it

---

### WSH-03: Reject everything that is not a water source in the main hand

**Status:** done · **Issue:** —

IF the raycast does not hit a block, or the target is not a water source block, or the ash is
held in the off hand THEN the interaction SHALL fail with nothing consumed.

The raycast uses `FluidHandling.SOURCE_ONLY`, so a flowing-water stream is not a valid
target unless a source block is hit behind it.

**Acceptance criteria** (verify: `runClient`)
- [ ] Right-clicking air, land or lava with ash consumes nothing
- [ ] Ash in the off hand does nothing
- [ ] A cauldron filled with water is **not** a valid target (it is not a water block)
- [ ] Waterlogged blocks are not valid targets

---

### WSH-04: Automated washing

**Status:** planned · **Issue:** — · **Open question:** CON-Q4

The mod SHALL provide a way to wash ash without a player right-click, so that the chain from
log to shred can be automated end to end.

**Nothing is built.** The README has advertised "Ore Washing — *Coming soon*" since 1.1.x.
There is no block, no cauldron interaction and no dispenser behaviour, and no design exists.
Whatever it becomes must not require a machine GUI (concept principle 5) and must not make
the manual path obsolete.

**Acceptance criteria** (verify: `runClient`) — *to be written with the design*
- [ ] Ash can be fed in and shreds collected without player interaction
- [ ] Throughput is not better than the manual path per unit of ash
- [ ] The README's "coming soon" is replaced by the real mechanic

---

### WSH-05: Washing works alongside other mods' water interactions

**Status:** partial · **Issue:** #15

WHEN another mod also handles right-click-on-water with an item THEN Iron Oak's washing
SHALL either apply or cleanly pass, never leave the ash in a stuck state.

**Unreproduced.** #15 reports ash that "just starts floating instead of turning into shreds",
with Lychee installed, on a 1.18.2/1.19-era pack; the same report carries an unrelated
`Block entity iron_oak:fire_bowl … state Block{minecraft:air} invalid for ticking` warning.
Washing was explicitly re-verified as working on 1.21.11 (#27), and no reproduction exists on
`main`. Kept as `partial` rather than `done` because a player-visible report is open and
unexplained, and rather than `broken` because nobody has reproduced it.

**Acceptance criteria** (verify: `runClient`)
- [ ] Washing works in a plain instance on `main`
- [ ] A reproduction attempt with Lychee installed is documented on #15, either way
- [ ] `use` returns `pass` rather than `fail` where another handler should get the click

---

## Open questions

- **CON-Q4** What shape should automated washing take — see [WSH-04](#wsh-04-automated-washing).
- **WSH-Q1** Should a water cauldron be a valid washing target? It is the intuitive guess for
  most players and would cost one condition (WSH-03), but it also half-answers CON-Q4 and
  should not be added by accident.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Initial. WSH-04 records the README's long-standing "coming soon" as `planned`; WSH-05 keeps #15 visible as unreproduced rather than silently `done`. |

*Last updated: 2026-08-20*
