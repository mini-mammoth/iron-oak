---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-08-20
related:
  - ../../AGENTS.md
  - orchestration.md
  - ./orca-progress-loop.md
---

# Issue Labels — Taxonomy and Rules

Labels on this board are **not decoration, they are the machine interface**. The progress
loop ([`orca-progress-loop.md`](orca-progress-loop.md)) decides purely from labels and
issue body what it dispatches, what it locks, and what it puts in front of a human. A
mislabelled ticket is handled wrongly: without `area:*` the dispatcher has no collision
check, and without `status:needs-human` a worker will grab a ticket that needs a human
decision.

So the label set is **closed** and grows only through human approval.

---

## The five ground rules

1. **Only labels from the tables below.** If none fits, that is a signal — either the
   information belongs in the title/body, or a new label is needed (rule 2).
2. **A new label is always approved by a human.** No agent runs
   `gh label create|edit|delete`. Details: [New labels](#new-labels--approval-required).
3. **Mandatory per ticket: exactly one type label and one or two `area:*`.** Everything
   else is optional.
4. **`status:*` belongs to the loop**, not to the human — with one exception
   (`status:changes-requested`, below).
5. **A label is not a substitute for prose.** What a ticket is *about* (the fire bowl,
   a specific wood type, a crash report) goes in the title and body. Topic labels are not
   part of the taxonomy — they are not queryable enough to steer work, and they breed one
   new label per feature.

---

## The axes

### 1. Type — what kind of work is this? (mandatory, exactly 1)

| Label | Meaning |
|-------|---------|
| `bug` | Behaviour deviates from what the mod documents or intends. The loop pulls **all open `bug` tickets regardless of milestone**. |
| `enhancement` | New or extended behaviour. |
| `documentation` | Only `docs/`, `AGENTS.md`, `README.md` — no production code. |

`design` from the source taxonomy is deliberately **absent**: this is a mod, not a
product with a brand system. Visual work here is textures and models, which is
`area:assets` with type `enhancement`.

### 2. Area — which workstream? (mandatory, 1–2)

Areas are cut along **file surfaces that do not overlap**, because that is what the
dispatcher uses to decide whether two tickets may run in parallel.

| Label | Workstream | Owns |
|-------|-----------|------|
| `area:blocks` | Blocks and block entities | `FireBowlBlock`, `FireBowlEntity`, `OreInfusedSaplingBlock`, `init/ModBlocks`, `init/ModEntityTypes` |
| `area:items` | Items and their behaviour | `OreInfusedAsh`, `OreInfusedBoneMeal`, `init/ModItems` |
| `area:recipes` | Recipe types, serializers and recipe JSON | `BurningRecipe`, `WashingRecipe`, `init/ModRecipes`, `resources/data/iron_oak/recipes/` |
| `area:worldgen` | Tree growth and world generation | `init/ModConfiguredFeatures`, `init/ModSaplingGenerators`, `init/ModWorldGenerator` |
| `area:datagen` | The data generator and its output | `init/ModDataGenerator`, `src/main/generated/`, `scripts/generate.go` |
| `area:client` | Rendering and client-only code | `client/IronOakClient`, `FireBowlRenderer` |
| `area:assets` | Hand-written resources | `resources/assets/iron_oak/` (models, blockstates, textures, lang), `resources/data/iron_oak/{loot_tables,tags}/` |
| `area:build` | Toolchain and CI | `build.gradle`, `gradle.properties`, `settings.gradle`, `gradle/`, `.github/`, `fabric.mod.json`, `iron_oak.accesswidener` |

**More than two `area:*` means the ticket is too big.** Split it — do not add a third
label. This is not a formality: the loop only dispatches same-area tickets after
comparing their file surface, and a ticket spanning three areas reliably blows a worker's
context window.

`area:build` is special: it is the only area that may change what "green" means for every
other area at once. A version bump, a Loom bump or a CI change is **never** dispatched
in parallel with anything else. See the migration note in
[`orchestration.md`](orchestration.md#version-migrations-are-serial).

### 3. Status — where is it in the loop? (0–1, set by the loop)

| Label | Meaning | Who sets it |
|-------|---------|-------------|
| `status:in-progress` | Claimed — the **dispatch lock**, max 1 worker per ticket | Loop, **before** creating the worktree |
| `status:needs-human` | Waiting on a human decision/fix, **never** auto-dispatched | Loop or human |
| `status:changes-requested` | Unaddressed change requests on an open PR | **Human only** — the loop consumes it |

Never two `status:*` at once. On transition, remove the old one rather than adding to it.

`status:changes-requested` is the one label **you** set rather than the loop: GitHub
forbids "request changes" on your own PR, and all worker PRs are created under your
account — without this label you would have no way to feed review feedback into the loop.

### 4. Human ownership (optional, 0–2)

| Label | Meaning |
|-------|---------|
| `blocker` | Blocks the next release. A prioritisation signal, **not** a dispatch ban. |
| `decision` | Needs a human decision, no agent autopilot. |

`decision` without `status:needs-human` is a mistake: only `status:*` blocks dispatch.
And the rule from [`orchestration.md`](orchestration.md) still holds — **human-owned by
body, not just by label**. A ticket whose goal lies outside the repo (publishing to
Modrinth or CurseForge, answering a bug reporter, deciding which Minecraft versions to
support) is human-owned even without a label.

### 5. Priority (optional, 0–1)

`high-priority` · `medium-priority` · `low-priority`

Only set these when the order does not already follow from milestone + `blocker` +
`Blocked by #n`. In practice that is almost only for the post-release backlog
(`low-priority`): tickets without a milestone, which the loop does not touch anyway.

---

## New labels — approval required

> **No agent creates, edits or deletes labels.** `gh label create`, `gh label edit` and
> `gh label delete` are human commands. `gh issue edit --add-label <existing label>` is
> allowed.

An agent that needs a label which does not exist:

1. **labels the ticket with the closest existing label** — the work stays dispatchable,
   nothing waits on approval;
2. **proposes the new label as an issue comment** (template below), and
3. **carries on.** The proposal is not a blocker.

Proposal template:

```markdown
**Label proposal** (needs approval)
- Name: `area:networking`
- Description: packets and client/server sync
- Colour: #1D76DB
- Axis: area
- Why no existing label fits: <one sentence>
- Set instead: `area:blocks`
```

A human approves by creating the label, or by explicitly approving it in the ticket —
then an agent may use it. A comment without a reply is **not** approval.

### Why this rule exists

It is imported, not learned here: on the board this taxonomy comes from, labels were
created ad hoc while filing tickets until 26 labels were in use, 15 of them outside the
documented taxonomy and nine used exactly once. Worse were the near-duplicates —
`frontend` next to `area:frontend`, `feature` next to `enhancement` — which make every
board query silently incomplete: `gh issue list --label area:frontend` missed tickets
that were obviously frontend work. A set that grows without review stops being a filter.

This repo starts clean (only the GitHub defaults exist today), which is exactly when the
rule is cheap to adopt.

### Colour convention for new labels

| Axis | Colour logic |
|------|--------------|
| Type | keep the GitHub defaults |
| `area:*` | one fixed, strong colour per area — never two areas in the same colour |
| `status:*` | pastel (`#fef2c0`, `#e99695`) — instantly readable as a process state |
| Human ownership | red/orange (`#b60205`, `#d93f0b`) |
| Priority | red → yellow → green |

Every label needs a **description**. Labels without one are where drift starts — nobody
can tell them from a neighbouring label.

---

## Labelling in practice

When filing a ticket, decide in this order:

1. **Type**: does behaviour deviate from what the mod intends → `bug`. Otherwise new
   behaviour → `enhancement`. Docs only → `documentation`.
2. **Area**: which directories does the work touch? Map them with the table above. A
   crash on load is usually `area:build`; a wrong drop is `area:assets` (loot table) or
   `area:blocks`. More than two → split.
3. **Human?** Goal lies outside the repo, or needs a decision → `decision` and/or
   `blocker` **plus** `status:needs-human`.
4. **Priority** only if not derivable from milestone / `blocker` / `Blocked by`.
5. **Nothing else.** The topic goes in the title, the reference as `Ref:` in the body.

Worked examples from the current backlog:

| Ticket | Correct | Why |
|--------|---------|-----|
| #15 washing ash doesn't work | `bug`, `area:recipes` | `WashingRecipe` + the recipe JSON; one area, cleanly dispatchable |
| #11 Mod refuses to load on Quilt 0.17.4 | `bug`, `area:build`, `status:needs-human` | loader-compat decision (do we support Quilt at all?) belongs to a human before any code |
| #1 Ideas | `enhancement`, `low-priority`, `status:needs-human` | an idea collection, not a unit of work — must be split before anything is dispatched |
| #7 Dependency Dashboard | — | Renovate's own bookkeeping issue; do not label, do not dispatch |

---

## Inventory and migration

The repo currently carries only GitHub's default labels. These are **not** part of the
taxonomy and should not be set:

| Not in the taxonomy | Use instead |
|---------------------|-------------|
| `duplicate`, `invalid`, `question`, `wontfix`, `good first issue`, `help wanted` | unused GitHub defaults — close the ticket or state it in prose |

`good first issue` and `help wanted` are a defensible exception for a public mod: they
address outside contributors, not the loop. Keep them if you want drive-by contributions,
but they carry **no** meaning for dispatch, and a ticket labelled only with those is
still unlabelled as far as the loop is concerned.

**Deleting a label is a human decision** (it removes it from every historical ticket) and
is deliberately not done in this document.

### Labels to create for this taxonomy

None of these exist yet. Creating them is a human action:

```bash
gh label create "area:blocks"    --description "Blocks and block entities"            --color 0E8A16
gh label create "area:items"     --description "Items and their behaviour"            --color 5319E7
gh label create "area:recipes"   --description "Recipe types, serializers, recipe JSON" --color D93F0B
gh label create "area:worldgen"  --description "Tree growth and world generation"     --color 006B75
gh label create "area:datagen"   --description "Data generator and its output"        --color 1D76DB
gh label create "area:client"    --description "Rendering and client-only code"       --color BFD4F2
gh label create "area:assets"    --description "Models, blockstates, textures, lang, loot tables, tags" --color C2E0C6
gh label create "area:build"     --description "Toolchain, Gradle, CI, mod metadata"  --color FBCA04

gh label create "status:in-progress"       --description "Claimed by a worker — dispatch lock" --color FEF2C0
gh label create "status:needs-human"       --description "Waiting on a human decision or fix"  --color E99695
gh label create "status:changes-requested" --description "Unaddressed review feedback on an open PR" --color E99695

gh label create "blocker"  --description "Blocks the next release"      --color B60205
gh label create "decision" --description "Needs a human decision"       --color D93F0B

gh label create "high-priority"   --description "Do first"  --color B60205
gh label create "medium-priority" --description "Normal"    --color FBCA04
gh label create "low-priority"    --description "Backlog"   --color 0E8A16
```

---

## Commands

```bash
# Read the board
gh label list                                                   # current set
gh issue list --milestone "1.21.11 Port" --json number,title,labels
gh issue list --label bug --state open                          # loop scope: bugs

# Label a ticket (allowed: existing labels only)
gh issue create --milestone <ms> --label bug --label area:recipes --title <t> --body <b>
gh issue edit <n> --add-label blocker --remove-label question

# Human only:
gh label create "area:networking" --description "..." --color 1D76DB
gh label delete question
```

Check a ticket for completeness before dispatch: does it have exactly one type label and
at least one `area:*`? If not, label it first.
