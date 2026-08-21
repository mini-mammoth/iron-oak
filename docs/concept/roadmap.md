---
domain: Roadmap
domain_code: RDM
status: placeholder
last_updated: 2026-08-20
version: 1
related:
  - README.md
  - ../requirements/README.md
  - ../ops/version-migration.md
---

# Roadmap — parked ideas

> **This document is a holding pen, not a plan.** Everything below is **unrated and not
> committed**: no priority, no version, no promise. It exists so that ideas which were only
> living in an issue comment or a README aside are not lost while the baseline
> documentation is written.
>
> Filling this in properly — deciding what the mod should become, in what order — is the
> **second** step, deliberately after [`README.md`](README.md) and
> [`../requirements/README.md`](../requirements/README.md) describe what the mod *is*.

Nothing here may be implemented on the strength of this document alone. Promote an item by
writing a requirement for it (with a status of `planned`) and opening an issue; then delete
the line here.

---

## Harvested from issue #1 ("Ideas") — already shipped

| Idea | Where it ended up |
|---|---|
| Copper infused trees | Shipped; part of the 6×3 matrix |
| Gold infused trees | Shipped; part of the 6×3 matrix |
| Automate the fire bowl | Shipped as hopper access — see [BRN-06](../requirements/burning.md) |

## Promised in the README, not built

| Idea | Note |
|---|---|
| **Ore washing automation** | The README says "*Coming soon*". No design exists. Open question **CON-Q4**, requirement [WSH-04](../requirements/washing.md) — status `planned`. Whatever this becomes must not need a machine GUI (concept principle 5). |

## Raised by the baseline, waiting on a decision

Each of these is an open question in [`README.md`](README.md#open-questions) and is *not* a
committed feature:

| Idea | Question |
|---|---|
| Chance-based infusion | **CON-Q1** — make the javadoc true, or delete the javadoc |
| Iron Oak copper nugget | **CON-Q2** — add a mod item, or close copper's nugget arm as `wontfix` |
| Slower growth for infused saplings | **CON-Q5** — a real time cost for farmed ore |
| A stated ore-in/ore-out target | **CON-Q6** — measure the chain, then decide whether it is balanced |
| Wood-type differentiation | Woods differ only in shape today; yield per wood is identical |

## Bigger swings, unevaluated

These would each need a design document and a human gate before any code (see the
conditional design phase in [`../ops/orchestration.md`](../ops/orchestration.md)):

| Idea | First obstacle |
|---|---|
| More wood types (mangrove, cherry, bamboo) | A full matrix arm each — blocks, items, features, loot, tags, models, textures, lang, recipes |
| More metals | Concept principle 2 requires a vanilla `raw_*` seed item; only iron, copper and gold have one |
| Nether variants (stems, quartz, ancient debris) | Contradicts principle 2 and probably principle 5 |
| Infused leaves or infused planks | Contradicts principle 4 — ore content is supposed to die in the crafting table |
| Bulk washing / cauldron chain | Overlaps CON-Q4; decide that first |

## Not roadmap — tracked elsewhere

| Work | Where it lives |
|---|---|
| 1.20.4 → 1.21.11 → 26.2 migration | [`../ops/version-migration.md`](../ops/version-migration.md), issues #19, #20 |
| NeoForge alongside Fabric | Issue #21 |
| Known broken behaviour | The status matrix in [`../requirements/README.md`](../requirements/README.md), issues #15, #27, #28 |

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Placeholder. Harvests issue #1, the README's "coming soon", and the concept's open questions so nothing is lost before the roadmap is actually written. |

*Last updated: 2026-08-20*
