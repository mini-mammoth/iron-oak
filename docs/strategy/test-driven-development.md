---
domain: Strategy
domain_code: TDD
status: proposed
last_updated: 2026-08-20
related:
  - ../../AGENTS.md
  - README.md
  - testing.md
  - java.md
---

# Test-Driven Development

> **Nothing here is runnable yet.** There is no `src/test`, no test task, and
> `./gradlew build` runs zero tests. The harness is **#40**. This document says what to do
> once there is somewhere to put a test — and, just as importantly, where the rule stops.

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

Both halves of that are earned by the four bugs of 2026-08-20. Look at what they were:
#26 and #30 were *invariants* — a file must exist for every registered id; a name must
match the id it holds. #27 and #28 were *defects with an exact, deterministic
reproduction*, written down in the issue before anyone touched the code. Those are the two
cases where test-first costs almost nothing and pays immediately.

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
`ModConfiguredFeatures` → the feature id, and reported **54 inconsistencies** on `main`.
Those 54 findings are what a red step looks like.

So: **run the new test against the unfixed tree and read the failure message.** If it does
not name the defect, the test is not testing the defect. And if you cannot get it to fail —
say so in the PR rather than shipping a test that only ever passes.

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
no tests around the 1,500 lines you did not touch, a refactor in this repo is as unverified
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
| **A recipe's numbers change** | `FireBowlBlock.onUse` derives the cook duration from the matched recipe (`recipe.get().getCookingTime()`); the serializer carries a 200-tick default; `FireBowlEntity` also holds a `DEFAULT_COOKING_TOTAL_TIME` of 150. Three numbers, two of which disagree. Never change one of them without a test pinning what the player experiences. |
| **A new arm of the matrix — a metal or a wood type** | `AGENTS.md`: the matrix is all-or-nothing and a half-filled arm crashes on the missing entry. The matrix test *is* the completeness check. Write it first and it tells you which of the twelve places you forgot. |

---

## Where the test honestly does not come first

Each of these has a reason, and the reason is the point. "It is hard" is not on the list.

| Case | Why not |
|---|---|
| **A version migration** | You cannot write the test before the API, because the API shape is the unknown. `onUse` splits into `useItemOn`/`useWithoutItem`; `ActionResult.PASS` has no direct successor; the return-value semantics changed in a way that #27 turned into a real behavioural regression. Writing a test against a signature you have not resolved yet produces a test of your assumption. **Order here: resolve the API against the jar, make it compile, then write the test that pins the behaviour you had to reason about** — and write it in the same PR, because that reasoning is the thing that will be lost. |
| **Rendering, particles, sound, feel** | `FireBowlRenderer` is matrix pushes, translations and a rotation per tick. There is no assertion that distinguishes "right" from "0.2 blocks too low", and inventing one produces a test that fails on every legitimate tweak. What *is* testable is the layer below it: does the client **have** the item the renderer draws. That is #27 defect 1, and it is a client gametest — see [testing.md](testing.md). |
| **Worldgen shape and aesthetics** | `oreJungle` places a straight trunk, blob foliage, cocoa beans and vines. Whether that looks like a jungle tree is a judgement, and pinning the exact `TreeFeatureConfig` in a test only guarantees a failure the next time someone tunes it. What is testable — and what actually broke — is that the jungle key holds the jungle shape and the jungle metal. Test the wiring, not the silhouette. |
| **The existing untested surface** | 1,500 lines, no tests. Retrofitting the lot is a project nobody has commissioned, and a mass of tests written blind against current behaviour would freeze the bugs in place — #28 proves there are still bugs in there, all three of them pre-existing and byte-identical on 1.20.4. Do not open that PR. |
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

## Worked walkthrough: fixing #28 test-first

The cook duration defect, end to end, as it would be done under this policy.

1. **Read the reproduction from the issue.** `cookingTotalTime` is assigned only in
   `setInput(ItemStack, int)`, the player path. A hopper goes through `setStack` and leaves
   the field at whatever the last recipe left, or at the initial `200`. Separately, it is
   never written to NBT, so a reload drops it back to the default.
2. **Pick the lowest layer that can fail for the real reason.** The NBT half is layer 1: a
   `writeNbt`/`readNbt` round-trip on a block-entity instance, no world. The hopper half is
   layer 2: the bug lives at the seam between two real inserters, so it needs a hopper and
   a world.
3. **Write both tests. Run them against the unfixed tree.** The round-trip must fail
   showing the duration reverting to the default; the gametest must fail showing the same
   log burning for two different lengths of time. If either passes, it is aimed at the
   wrong thing — go back to step 2.
4. **Fix.** One default, named once, set on every insertion path, written to NBT and read
   back.
5. **Re-run. Both green.** Then check the save-format constraint the issue names: a bowl
   saved before the fix must still load.
6. **Commit test and fix together**, `fix(blocks): …(#28)`. State in the PR that the tests
   were observed red first, and state whether you launched `runClient`.

Note what step 3 buys that a fix alone does not: two defects in #28 interact — fixing the
persistence without the hopper path leaves the duration still wrong in the other path. Two
tests catch that; eyeballing one path does not.

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

*Last updated: 2026-08-20*
