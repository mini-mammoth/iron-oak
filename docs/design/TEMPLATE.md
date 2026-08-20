---
domain: [Feature name]
domain_code: DES
status: draft
last_updated: YYYY-MM-DD
version: 1
related:
  - README.md
  - ../concept/README.md
---

# Design: [Feature name]

> Status: **draft** — not approved, nothing may be implemented from it.
> Requirements: `XXX-nn` in `../requirements/<domain>.md` · Concept:
> [`../concept/README.md`](../concept/README.md)

## Problem

What the player cannot do today, and why that is worth changing. One paragraph. If this
section needs three, the design is probably two designs.

## Constraints

The rules this design has to survive. Usually some of:

- The concept's design principles — quote the ones that bite
  ([`../concept/README.md`](../concept/README.md#design-principles))
- The 6×3 matrix: does this add an axis? What does that cost?
- No test suite: how will this be verified at all?
- The target Minecraft version and what it actually offers (check with the
  `minecraft-fabric-lookup` skill, never from memory)

## Options

At least two, each with what it costs. The rejected ones are the point of the document.

### Option A — [name]
How it works, in three or four sentences.
**For:** … **Against:** … **Matrix cost:** …

### Option B — [name]
…

## Proposal

The recommended option and why. Then the mechanics in enough detail to argue with:

- Blocks, items, recipe types that need to exist
- Data that has to be stored, and where (block entity NBT, data component, blockstate)
- Numbers, marked as first guesses; the approved ones move to
  [`../concept/balance.md`](../concept/balance.md)
- What the player does, step by step

## Open questions

Numbered, so the gate can answer them one by one. A design with none is suspicious.

## Requirements this would mint

The IDs to create with status `planned` once approved — title only, not the full text.

## Verification

How this gets proven without a test suite. Anything in-world means `runClient` and a written
checklist; say so.

## Version History

| Date | Version | Changes |
|------|---------|---------|
| YYYY-MM-DD | 1 | Initial draft. |

*Last updated: YYYY-MM-DD*
