# Agent Instructions — Iron Oak

**You are working on a ticket in this repository. Read this file, then start.**

This file is short on purpose: everything in it applies to writing code here, and your
context window is the scarce resource. Orchestration policy — how workers get dispatched,
gates, issue labels, supervisor loop — lives in
[`docs/ops/orchestration.md`](docs/ops/orchestration.md) and is **not** your concern.

---

## The product

**Iron Oak** — a Minecraft **Fabric** mod that adds ore-infused trees, so you can farm
ingots instead of mining them. Repo: `mini-mammoth/iron-oak`, MIT, public.

The gameplay loop the mod implements, in order — know it before you touch a recipe:

1. Craft **ore-infused bone meal** (iron / copper / gold).
2. Apply it to a vanilla sapling → the sapling is **infused** (`OreInfusedBoneMeal`).
3. The infused sapling grows into an ore-infused tree; cutting it yields **infused logs**.
   Crafting those logs into planks **destroys** the ore content — that is intended.
4. Burn infused logs in a **fire bowl** → **infused ash** (`BurningRecipe`).
5. Wash the ash with water → **shreds** (`WashingRecipe`).
6. 9 shreds → one raw ore; or smelt a shred into a nugget.

Six wood types are covered (oak, acacia, birch, jungle, spruce, dark oak) × three metals
(iron, copper, gold). That 6×3 matrix is why `ModBlocks`/`ModItems` are long and
repetitive — when you add a metal or a wood type you touch every arm of it.

- Mod id is `iron_oak` (underscore). The Gradle artifact is `iron-oak` (hyphen). Both are
  load-bearing; do not "unify" them.
- User-facing strings live in `src/main/resources/assets/iron_oak/lang/`. Code,
  identifiers, comments and commit messages are **English**.

---

## Stack (as it actually is)

| Layer | What | Where |
|---|---|---|
| Loader | Fabric | `src/main/resources/fabric.mod.json` |
| Build | Gradle + **Fabric Loom** | `build.gradle`, `gradle.properties` |
| Language | Java | `src/main/java/com/minimammoth/ironoak/` |
| Registration | plain `Registry.register` in `init/Mod*.java` — no DeferredRegister (that is Forge/NeoForge) | `init/` |
| Data generation | Fabric datagen API, custom `runDatagen` Gradle task | `init/ModDataGenerator.java` → `src/main/generated/` |
| Hand-written resources | recipes, loot tables, tags, models, textures, lang | `src/main/resources/{data,assets}/` |
| Access widener | one entry, for the cooking-recipe serializer factory | `src/main/resources/iron_oak.accesswidener` |
| Mixins | **config exists but is empty** — no mixin classes, no `mixin` package | `src/main/resources/iron-oak.mixins.json` |
| Resource generator | a Go helper that emits repetitive resource JSON | `scripts/generate.go` |
| CI | GitHub Actions, `./gradlew build` on Linux + Windows | `.github/workflows/main.yml` |

Version facts live in **`gradle.properties`** and nowhere else — `minecraft_version`,
`yarn_mappings`, `loader_version`, `fabric_version`, `mod_version`. Read them before you
write a single API call; this mod is mid-migration and the answer changes.

---

## The JDK is the single most common way to waste an hour here

**Fabric Loom does not run on JDK 22+.** This machine's default is JDK 25 (sdkman), so a
bare `./gradlew build` fails before it compiles anything, and the error does not say
"wrong JDK" — it surfaces as a Gradle/Loom internal failure that reads like a broken
build. The build itself is fine.

Run every Gradle command with JDK 21:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew build
```

Or `sdk use java 21.0.3-ms` for the shell. `.sdkmanrc` in the repo root records this, so
`sdk env` picks it up. If a build fails and you have not checked `java -version`, check it
before you debug anything else.

---

## Quality gates: run these before every commit

This list is a **copy of `.github/workflows/main.yml`** and is the only definition of
"green" in this repo:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew build                 # compiles, remaps, builds the jar — the CI gate
```

Add these when your change touches what they cover:

```bash
./gradlew runDatagen            # if you changed ModDataGenerator or anything it emits
./gradlew runClient             # if you changed rendering, blocks, or in-world behaviour
```

There is **no test suite** in this repo — `./gradlew build` runs no tests, because none
exist. So "green" here is a weaker signal than in a repo with tests: it proves the mod
compiles and remaps, not that it works. Anything touching in-world behaviour (fire bowl
ticking, sapling growth, washing, recipe matching) is **not** verified by CI and must be
checked in `runClient` before you claim it works. If you did not launch the game, say so
in the PR instead of implying you did.

- Done means `gh pr checks <pr>` is green. A green local build is not proof — CI also
  builds on Windows.
- `./gradlew build` is incremental and Loom caches Minecraft; the first run after a
  version bump re-downloads and decompiles and can take several minutes. That is normal,
  not a hang.

---

## Generated resources: regenerate, never hand-edit

`src/main/generated/` is **output**, and it is committed. Editing it by hand works until
the next `runDatagen` silently reverts your change.

1. Change `init/ModDataGenerator.java` (or the providers it registers).
2. `./gradlew runDatagen`.
3. Commit `src/main/generated/` **together with** the Java change.

`src/main/resources/data/` is the opposite: hand-written and authoritative. Recipes, loot
tables and tags live there. `scripts/generate.go` emits repetitive resource JSON — if you
are about to write the twelfth near-identical file by hand, read that script first.

Do not commit `src/main/generated/.cache` (gitignored).

---

## Git

**Conventional Commits, English subject, with the issue number:**

```
<type>(<scope>): <imperative subject> (#<issue>)

[optional body — why, not what]
```

`type` ∈ `feat` | `fix` | `docs` | `chore` | `refactor` | `deps` | `ci`.
`scope` is the workstream (`blocks`, `items`, `recipes`, `worldgen`, `datagen`, `client`,
`assets`, `build`).

The existing history is only partly conventional (`deps: Upgrade to Minecraft 1.19.x`,
`ci: Only use Java 17`, alongside bare subjects like `Add iron bone meal`). Follow the
format above going forward; do not rewrite the old subjects.

```
feat(recipes): add washing recipe for copper ash (#23)
fix(blocks): fire bowl keeps burning after its last log (#15)
deps(build): upgrade to Minecraft 1.21.11 (#31)
```

- Work on your issue branch (`issue-<n>-<slug>`); **never commit to `main`**.
- Push that branch and open a PR against `main`. The PR body contains `Closes #<n>` so the
  merge closes the issue — unless your briefing explicitly asks for `Refs #<n>`
  (multi-phase issues must survive until the last phase lands).
- Do **not** force-push and do **not** rewrite history.
- Do not close issues by hand while a PR is open.
- Commit **per work item**, not once at the end: incremental commits survive a context
  compaction and can be harvested; a single final commit can be lost entirely.

### Version branches

`main` tracks the newest supported Minecraft version. Older lines live on their own
branches (`v1.18.x`, `1.19` exist today) and CI builds `main` and `v1.18.x`. A fix that
applies to several lines is committed on `main` first, then cherry-picked — never
developed twice in parallel.

`mod_version` in `gradle.properties` is `<mod>+<mc>` (e.g. `1.2.1+1.20.4`). Bump the
Minecraft half in the same commit as the version bump, never separately.

---

## Scope discipline

- **Stay inside your issue.** If you discover required work outside its scope, finish what
  is in scope and **report the rest back** so a separate issue can be opened. Do not
  silently widen the PR.
- **A version bump is never a side effect.** `gradle.properties`, `build.gradle`, the
  Gradle wrapper and `.github/` belong to `area:build` tickets. If your feature seems to
  need a newer Minecraft or Loom, stop and report it — do not carry a toolchain change in
  a feature PR. It makes the PR unreviewable and unrevertable.
- **The 6×3 matrix is all-or-nothing.** Adding a wood type or a metal means every arm:
  block, block item, sapling generator, configured feature, loot table, tag, model,
  blockstate, texture, lang entry, recipe. A half-filled matrix ships a mod that crashes
  on the missing entry. If you cannot complete the matrix, report instead of shipping part.
- Minimal changes: change what the ticket needs, match the surrounding style, and remove
  debug code before committing.

---

## Reading code without burning your context

The Java here is small — 19 files, the largest around 180 lines — so reading is cheap and
you should not be shy about it. The expensive mistakes in this repo are elsewhere:

- **Never open `src/main/generated/`, `build/`, or `.gradle/`.** Generated worldgen JSON is
  bulky and tells you nothing a provider class won't. Grep it at most.
- **`ModBlocks.java` and `ModItems.java` are long but shallow** — the same three lines
  repeated across the matrix. Read one arm, not all eighteen.
- **Do not read the decompiled Minecraft sources to answer an API question.** Use the
  `minecraft-fabric-lookup` skill — it resolves the question against the jar in seconds.
  Extract one class if you must read vanilla source; opening the sources jar to browse
  will fill your window and often answers a different version's question.
- **Search before reading.** Grep for the exact symbol, then open at that line.

---

## Where the answers are

| Need to know… | Read |
|---|---|
| What the mod does, in player terms | `README.md` |
| Current target versions | `gradle.properties` |
| Dispatch / gate / label process | `docs/ops/orchestration.md` |
| Label taxonomy | `docs/ops/issue-labels.md` |
| Supervisor loop runbook | `docs/ops/orca-progress-loop.md` |
| Migration plan and its stages | `docs/ops/version-migration.md` |
| Fabric API for the current version | https://docs.fabricmc.net/ |
| What a Minecraft symbol is called | https://mappings.dev, or the `minecraft-fabric-lookup` skill |
| Whether a Minecraft class/method still exists in this version | the `minecraft-fabric-lookup` skill — never answer this from memory |

---

## Useful commands

All Gradle commands assume `JAVA_HOME` points at JDK 21 (see above).

| Purpose | Command |
|---|---|
| Build the jar | `./gradlew build` → `build/libs/iron-oak-<version>.jar` |
| Launch the client | `./gradlew runClient` |
| Launch a server | `./gradlew runServer` |
| Regenerate data | `./gradlew runDatagen` |
| Clean | `./gradlew clean` |
| Refresh after a version bump | `./gradlew --refresh-dependencies build` |
| Check available versions | https://modmuss50.me/fabric.html |

---

## When to ask, and when not to

**Ask** when the ticket is ambiguous, when the build fails in a way you cannot attribute,
when a Minecraft API you need does not exist in the target version (that is a real
finding — report it, do not invent a workaround), or when your goal conflicts with a
documented decision. Give context: what you tried, which files you read, the exact error.

**Do not ask** when the answer is in `gradle.properties`, in the Fabric docs, or one Grep
away — and do not ask only to have your understanding confirmed. Test it instead.

---

## Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-20 | 1.0 | Initial version. Ops model adapted from the openkegelbillard setup: worker instructions here, orchestration policy in `docs/ops/`. Records the JDK-21 constraint, the datagen direction, the 6×3 matrix rule, and that there is no test suite. |

*Last updated: 2026-08-20*
