---
domain: Strategy
domain_code: STR
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - ../README.md
  - testing.md
  - test-driven-development.md
  - java.md
---

# docs/strategy/

**Answers the question: _why is the code written this way?_**

Strategy documents record **settled technical decisions** — the ones that should not be
re-argued in every ticket. Where the code has a shape that surprises a newcomer, this
directory says why it has that shape, so the next worker extends it instead of
"fixing" it.

They are not tutorials, and they are not a wish list. Everything in here is either in
force today or explicitly marked as intended-but-not-wired-up, with the ticket that wires
it up.

- Do not ship something that contradicts a strategy document without a discussion.
  Raising the question is right; shipping the contradiction quietly is not.
- If a document is wrong, fix the document in the PR that proves it wrong. A stale
  strategy document is worse than none, because it is read as authority.

---

## The documents

| Document | Answers |
|----------|---------|
| [testing.md](testing.md) | Where does a test for *this* go, which of the three layers runs it, which requirement does it cite, and what does `./gradlew build` actually prove? |
| [test-driven-development.md](test-driven-development.md) | Does the test come first — and where does it honestly not, in a repo whose entire existing surface is untested? |
| [java.md](java.md) | Registration, the client/server split, where `null` is tolerated, and how the 6×3 matrix constrains `ModBlocks`/`ModItems`. |

`java.md` answers *why*. When what you need is the rule rather than the reason, the
`java` skill is the same conventions as a checklist — it is generated from this document and
defers to it on every point of substance.

**Read [`java.md`](java.md) before your first non-trivial change to `src/`.** It records
the things a reader of this codebase cannot infer in one pass — including the two that
have already cost real bugs: a registered id is the contract and a Java constant name is
not (#30), and a server-side mutation the renderer reads must push a sync explicitly
(#27).

---

## How this differs from the neighbouring directories

The split is by **question**, not by topic. Consulting the wrong one wastes your time.

| Where | Question | Example |
|---|---|---|
| [`AGENTS.md`](../../AGENTS.md) | What do I do, right now, on this ticket? | Run Gradle with JDK 21 |
| `docs/concept/` | Why does the **mod** work this way, for the player? | Why planks destroy the ore content |
| `docs/requirements/` | What must the mod do, and does it do it today? | `TRE-04`, with a status |
| `docs/strategy/` | Why is the **code** written this way? | Why `Registry.register`, not DeferredRegister |
| `docs/ops/` | How is the work itself run? | Dispatch, gates, labels, releases |
| `docs/design/` | How will this one change be built? | Does not exist yet — created per ticket |

`docs/concept/` and `docs/requirements/` land with #32; until it merges, the mod's
player-facing behaviour is described in [`README.md`](../../README.md) and the gameplay
loop summary in [`AGENTS.md`](../../AGENTS.md).

The line between concept and strategy: **concept decides what the fire bowl does, strategy
decides why its block entity syncs the way it does.** If a decision would still hold after
a total rewrite of the gameplay, it is concept. If it would change, it is strategy.

The line between strategy and ops: strategy is about the artefact, ops is about the
process. "Tests live at the lowest layer that can fail for the real reason" is strategy.
"A worker may not merge without a green `gh pr checks`" is ops.

---

## Status honesty

Each document carries a `status` in its frontmatter, and it means what it says:

| Status | Meaning |
|---|---|
| `active` | In force. Follow it. |
| `proposed` | The decision is settled, the infrastructure is not. The document names the ticket that builds it and says plainly what does not exist yet. |

`testing.md` and `test-driven-development.md` were `proposed` when they were written and
are `active` now: #40 wired up layers 1 and 2, so both are in force. **One thing in
`testing.md` is still ahead of the tree** — layer 3, client gametests, has no run
configuration and no test, and that section says so where it appears. Everything else in
both documents describes something you can run.

The one deferral those documents carried is also gone. `test-driven-development.md` used to
say that no test would cite a requirement id until `docs/requirements/` existed; it does
(#32), so tests cite it (#43), and the citation is checked in both directions rather than
being a comment.

---

## 📅 Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial version (#39). Taxonomy adapted from the openkegelbillard `docs/strategy/` split; contents written for a Fabric mod from scratch. Records the concept/strategy and strategy/ops boundaries, and the `proposed` status convention for the not-yet-wired test harness. |
| 2026-08-21 | 1.2 | Tests cite requirements (#43). `testing.md` gains the `@Requirement` section and `test-driven-development.md` loses its requirement-ID deferral, since `docs/requirements/` landed with #32. Notes that the `java` skill is the checklist form of `java.md` and defers to it. |
| 2026-08-21 | 1.1 | `testing.md` and `test-driven-development.md` moved from `proposed` to `active` (#40). The status-honesty section now names the one part still ahead of the tree — layer 3 — rather than the whole harness. |

*Last updated: 2026-08-21*
