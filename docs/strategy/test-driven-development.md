---
domain: Strategy
domain_code: TDD
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - README.md
  - testing.md
  - java.md
---

# Test-Driven Development

> **This is in force now.** #40 landed the harness: `src/test` runs inside
> `./gradlew build`, `src/gametest` runs under `./gradlew runGametest`, and both report to
> CI on Linux and Windows. There is somewhere to put a test, so the rule below applies —
> and, just as importantly, so does where it stops.

[testing.md](testing.md) is the **how**: three layers, which class belongs at which. This
document is the **when**.

---

## The position

**Test-first is mandatory where it works, and not required where it does not.**

That is a deliberate choice, not a compromise. This repo has 19 Java files, about 1,500
lines, and **zero** existing tests. A rule that said *every change ships a test written
first* would be ignored within a week — because there are changes here where it is
impossible, and once a rule is ignored once it stops being a rule. A rule nobody follows
devalues the ones that matter. So the scope is narrow and the scope is real.

The narrow rule, in one line:

> **Every bug fix starts with a failing test. Every consistency invariant is a test.
> Everything else is a judgement call you record in the PR.**

Both halves of that are earned by the four bugs of 2026-08-20, all four of which are now
fixed and merged. Look at what they were: #26 and #30 were *invariants* — a file must exist
for every registered id; a name must match the id it holds. #27 and #28 were *defects with
an exact, deterministic reproduction*, written down in the issue before anyone touched the
code. Those are the two cases where test-first costs almost nothing and pays immediately.
None of the four fixes shipped with a test, because at the time there was nowhere to put
one. #40 went back and wrote them: the #26 and #30 invariants as layer-1 tests, and #28's
save-format and hopper defects at layer 1 and 2 respectively. Every one of them was checked
against a deliberately re-broken tree before being trusted — see the red-step evidence in
that PR.

---

## Red-green-refactor when the starting point is zero tests

The classic cycle assumes a suite. Here it needs one adjustment and one honest admission.

### 1. Red — and it has to be red *for the real reason*

Writing a test that fails is easy. Writing a test that fails **because of the bug** is the
entire skill, and this repo has already produced the cautionary example.

In #30 the generated JSON was **correct**. The Java constant names were rotated, the code
was internally self-consistent, `runDatagen` produced no diff, everything compiled. A
matrix test written against the committed JSON would have passed against the bug. The
throwaway script that found it walked `ModBlocks` → `ModSaplingGenerators` →
`ModConfiguredFeatures` → the feature id, and reported **54 inconsistencies**. Those 54
findings are what a red step looks like.

So: **run the new test against the unfixed tree and read the failure message.** If it does
not name the defect, the test is not testing the defect. And if you cannot get it to fail —
say so in the PR rather than shipping a test that only ever passes.

For a bug that is already fixed, that means re-breaking the tree on purpose, running the
test, and reverting. #40 did exactly this and it paid twice over. Rotating only the
generator *names*, the way #30 had them, failed the layer-1 matrix test with
`iron_oak:copper_spruce_sapling grows with the wrong generator` — and left every gametest
green, because the in-world outcome was unchanged. Rotating the feature *keys* as well
failed the gametests with `Expected block Iron Infused Spruce Log: got Iron Infused Dark
Oak Log`. Two halves of one bug, each invisible to the other layer. Neither test subsumes
the other, and a single red step would have hidden that.

The one thing you never do is commit red. `AGENTS.md` asks for a commit per work item; a
commit that leaves CI failing poisons the branch for everyone who bisects it. **Observe the
red locally, then commit the test and the fix together**, one commit per defect. The red is
evidence you report, not an artefact you push.

### 2. Green — minimally, and inside the ticket

Enough code to pass, nothing speculative. `AGENTS.md`'s scope discipline is not suspended
because you are writing a test: if the test reveals work outside the issue, finish what is
in scope and report the rest.

### 3. Refactor — and here is the admission

Refactoring is normally safe because the suite has your back. **It does not, here.** With
no tests around the ~1,850 lines you did not touch, a refactor in this repo is as unverified
as it was before you added your one test. Two consequences:

- Keep the refactor step inside the blast radius your new test actually covers.
- For anything wider, `runClient` is still the gate, and `AGENTS.md` requires you to say in
  the PR whether you launched the game. That does not change until the gametests exist.

---

## Where the test comes first — no exceptions

| Case | Why it is cheap and why it pays |
|---|---|
| **A bug with a reproduction** | The reproduction is already the test. #28 spells out three defects with exact preconditions — burning arrow on an unlit bowl, save/reload mid-burn, hopper insert versus player insert. Turning each into an assertion is transcription, not design. And it is the only way to know the fix fixed *that*. |
| **Any consistency invariant across the 6×3 matrix** | 18 arms × every resource kind is beyond what a human re-checks by hand, which is precisely why #30 shipped for years and #26 shipped 46 missing files. These tests are pure layer 1, cost milliseconds, and each one retires a line of the `runClient` checklist permanently. |
| **A registered id or a resource path changes** | Ids are the contract with saved worlds and data packs (see [java.md](java.md)). The test that a rename does not break the id↔asset pairing must exist *before* the rename, or you are inspecting the rename by eye — which is what #26 was. |
| **A recipe's numbers change** | The cook duration reaches the player through three hops: the recipe JSON, which omits `cookingtime` for all three burning recipes; `ModRecipes.DEFAULT_COOKING_TIME`, which the serializer therefore supplies; and `FireBowlEntity`, which falls back to the same constant when an input matches no recipe at all. #28 collapsed two disagreeing defaults into that one constant. Never change a number on that path without a test pinning what the player experiences. |
| **A new arm of the matrix — a metal or a wood type** | `AGENTS.md`: the matrix is all-or-nothing and a half-filled arm crashes on the missing entry. The matrix test *is* the completeness check. Write it first and it tells you which of the twelve places you forgot. |

---

## Where the test honestly does not come first

Each of these has a reason, and the reason is the point. "It is hard" is not on the list.

| Case | Why not |
|---|---|
| **A version migration** | You cannot write the test before the API, because the API shape is the unknown. The 1.20.4 → 1.21.11 migration split `onUse` into `useItemOn`/`useWithoutItem`, turned three tick and NBT hooks into differently-shaped ones, and changed interaction return values in a way that #27 turned into a real behavioural regression. Writing a test against a signature you have not resolved yet produces a test of your assumption. **Order here: resolve the API against the jar, make it compile, then write the test that pins the behaviour you had to reason about** — and write it in the same PR, because that reasoning is the thing that will be lost. The fix for #27 did the next best thing available without a harness: it put the whole return-value argument in a javadoc on `useItemOn`. Prose is a weaker guard than a test, and better than nothing. |
| **Rendering, particles, sound, feel** | `FireBowlRenderer.submit` is matrix pushes, translations and a rotation. There is no assertion that distinguishes "right" from "0.2 blocks too low", and inventing one produces a test that fails on every legitimate tweak. What *is* testable is the snapshot it draws from: since 1.21.9 `extractRenderState` copies the state into a `FireBowlRenderState`, so *"the client has the item"* is a boolean on an object — `state.hasInput`. That is #27 defect 1, and it is a client gametest. See [testing.md](testing.md). |
| **Worldgen shape and aesthetics** | `oreJungle` places a straight trunk, blob foliage, cocoa beans and vines. Whether that looks like a jungle tree is a judgement, and pinning the exact `TreeFeatureConfig` in a test only guarantees a failure the next time someone tunes it. What is testable — and what actually broke — is that the jungle key holds the jungle shape and the jungle metal. Test the wiring, not the silhouette. |
| **The existing untested surface** | Retrofitting the lot is a project nobody has commissioned, and a mass of tests written blind against current behaviour would freeze the bugs in place — #28 found three defects that had been sitting there since the 1.20.4 line, unchanged and unnoticed. Do not open that PR. #40 hit this rule head-on: the drop-on-break behaviour turned out to have changed under the mod, and rather than pin whatever it now does, it shipped no test and reported the finding. **Behaviour nobody has decided on does not get a test.** |
| **A pure `docs/` change** | This one included. |

---

## The net grows along the diff

The rule that makes the previous section survivable:

> **When you touch a file, the behaviour you touched gets a test. When you find a bug, its
> reproduction gets a test first. Nothing else is owed.**

That is a rule a worker can follow on a scoped ticket without widening the PR, and it
converges: the parts of the codebase that change are the parts that acquire tests, in the
order that changes arrive. The parts that never change never needed them.

It also has a hard edge, and this is the one to actually remember:

> **If you wrote a script to verify your work, you wrote a test. Check it in.**

The #30 worker's script joined four sources and asserted wood and metal at every hop. It
went from 54 failures to 18-of-18 clean and was then deleted. It is the single most
valuable test this repo could have had, it already existed, and it was thrown away. That is
the failure this document exists to prevent.

---

## Worked walkthrough: how #28's cook duration would have gone

The defect is fixed and merged. Replaying it under this policy is still the clearest
illustration, and it shows where the harness would have changed the outcome.

1. **Read the reproduction from the issue.** `cookingTotalTime` was a field assigned in
   exactly one place — the player insertion path — so a hopper-inserted log kept whatever
   the previous one left behind. Separately, it was never persisted, so a reload dropped it
   back to the default mid-burn.
2. **Pick the lowest layer that can fail for the real reason.** The persistence half is
   layer 1: a `saveAdditional`/`loadAdditional` round-trip on a block-entity instance, no
   level. The hopper half is layer 2: the bug lives at the seam between two real inserters,
   so it needs a hopper and a level.
3. **Write both tests. Run them against the unfixed tree.** The round-trip must fail
   showing the duration reverting to the default; the gametest must fail showing the same
   log burning for two different lengths of time. If either passes, it is aimed at the
   wrong thing — go back to step 2.
4. **Fix.** What actually landed is better than what step 3 was aiming at: rather than
   persisting the field and setting it on both paths, the fix **deleted** it. The duration
   is now derived from the matched recipe on demand, so there is no cached value to be
   wrong or to lose. Note that a test written to assert "the field round-trips" would have
   had to be deleted along with it — which is the argument for asserting on the observable
   (how long the burn takes) rather than on the mechanism.
5. **Re-run. Both green.** Then check the save-format constraint the issue names: a bowl
   saved before the fix must still load. The merged code handles it explicitly — the new
   `unlit_time` key defaults to `0` when absent, with a comment saying why that is the
   right answer for an old save.
6. **Commit test and fix together**, `fix(blocks): …(#28)`. State in the PR that the tests
   were observed red first, and state whether you launched `runClient`.

Note what step 3 buys that a fix alone does not: two defects in #28 interact — fixing the
persistence without the hopper path leaves the duration still wrong in the other path. Two
tests catch that; eyeballing one path does not. What the fix's author had to do instead was
reason it through and write the reasoning into the class comment. Read that comment: it is
what a test would have asserted, in prose.

---

## What this policy deliberately does not include

- **No coverage target.** No percentage, no ratchet, no CI gate on a number. See
  [testing.md](testing.md).
- **No test-review-before-implementation gate.** There is one reviewer and an agent
  workflow; a rule that tests must be approved before code begins would stall every
  ticket. Tests are reviewed with the PR they land in.
- **No requirement-ID annotation on every test** until `docs/requirements/` exists on
  `main` (it lands with #32). Once it does, name the requirement in the test when there is
  one — `TRE-04` for the matrix, for instance — and name the issue when there is not.
  Inventing a citation scheme before the thing being cited exists is bookkeeping for its
  own sake.
- **No "all features must be implemented using TDD."** This document says where, and it
  means only that.

---

## 📅 Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial version (#39). Deliberately narrow: test-first for bug reproductions and matrix invariants, explicitly not for version migrations, rendering, worldgen aesthetics or the existing untested surface — with the reason given for each. Records that red must be observed for the real reason (#30's 54 findings), that red is never committed, and that a verification script is a test. |
| 2026-08-20 | 1.1 | Rebased onto 1.21.11 (#39). The four bugs restated as fixed and merged, none of them with a test. The #28 walkthrough now records what actually landed — the cached field was deleted rather than persisted — and draws the lesson that an assertion on a mechanism dies with the mechanism. The recipe-numbers and rendering rows updated for `ModRecipes.DEFAULT_COOKING_TIME` and the `extractRenderState`/`submit` split. |
| 2026-08-21 | 2.0 | In force (#40). Status changed from proposed to adopted. The four bugs now have their tests, each checked against a deliberately re-broken tree. The red step gains #40's finding that the two layers caught two different halves of #30 and neither subsumed the other. The untested-surface row gains the case that made the rule real: behaviour that changed under the mod got a reported finding, not a test. |

*Last updated: 2026-08-21*
