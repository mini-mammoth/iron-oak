---
domain: Design Documents
domain_code: DES
status: active
last_updated: 2026-08-20
version: 1
related:
  - ../requirements/README.md
  - ../concept/README.md
  - ../ops/orchestration.md
---

# Design Documents

**Answers the question: _how will this be built, and what did we decide along the way?_**

A design document sits between the concept and the code:

| Layer | Question | Where |
|---|---|---|
| Concept | *Why* does the mod work this way? | [`../concept/README.md`](../concept/README.md) |
| Requirements | *What* must it do, and does it? | [`../requirements/README.md`](../requirements/README.md) |
| **Design** | *How* will we build it, and which options did we reject? | here |
| Code | The thing itself | `src/` |

Design comes **before** requirement IDs for new work: the design settles the mechanic, the
approved design is then broken into requirements with `planned` status, and only then does
code get written. For existing behaviour it is the other way round — the requirement
describes what is already there and no design document is owed.

## When a design document is required

Required for work that **changes player-visible mechanics or adds a subsystem**: a new
processing step, a new machine block, a new resource line, a new metal or wood type, or
anything that touches the 6×3 matrix. This is the conditional design phase in
[`../ops/orchestration.md`](../ops/orchestration.md), and it ends in a **human gate**:
approve / revise / reject.

**Not** required for bug fixes, texture and model work, recipe corrections, CI and toolchain
work. Do not manufacture a design document for a one-line fix.

## How to write one

Copy [`TEMPLATE.md`](TEMPLATE.md). Two rules matter more than the structure:

1. **Record the options you rejected, with why.** A design document whose only content is
   the chosen approach is a description, not a decision — and the next person re-litigates
   it from scratch.
2. **Leave open questions open.** A draft that fakes certainty is worse than one that says
   "undecided, here are the three candidates". Mark them, and let the gate resolve them.

A design document is `draft` until the human gate approves it, then `approved`, then
`implemented` once its requirements are `done`. It is never deleted — a rejected design stays
as `rejected` so the same idea does not come back unexamined.

## Index

| Design | Status | Covers | Requirements |
|---|---|---|---|
| [automated-washing.design.md](automated-washing.design.md) | draft | Washing ash without a player right-click | [`WSH-04`](../requirements/washing.md#wsh-04-automated-washing) |
| [tree-purity.design.md](tree-purity.design.md) | draft | Purity/density per tree, breeding it up, drifting back down | to be minted (`PUR`) |
| [leaf-resources.design.md](leaf-resources.design.md) | draft | Infused leaves → drying → sieving → redstone, lapis, … | to be minted (`LEF`) |

All three are **drafts with open questions** and none is approved. Nothing in them may be
implemented on the strength of the document alone — see
[`../concept/roadmap.md`](../concept/roadmap.md) for what is actually committed (nothing yet).

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1 | Folder created (#36). The conditional design phase in `ops/orchestration.md` had pointed here since the ops setup; this is the first content. |

*Last updated: 2026-08-20*
