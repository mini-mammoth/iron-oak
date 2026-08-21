# Agent Instructions — Iron Oak

**You are working on a ticket in this repository. Read this file, then start.**

This file is short on purpose: everything in it applies to writing code here, and your
context window is the scarce resource. Orchestration policy — how workers get dispatched,
gates, issue labels, supervisor loop — lives in
[`docs/ops/orchestration.md`](docs/ops/orchestration.md) and is **not** your concern.

---

## The product

**Iron Oak** — a Minecraft **Fabric** mod that adds ore-infused trees, so you can farm
ingots instead of mining them. Repo: `mini-mammoth/iron-oak`, public source, licensed
under the Iron Oak License 1.0 (all rights reserved, modpack use granted — see
[`LICENSE`](LICENSE)). It is **not** open source; do not describe it as MIT.

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

**Before you change behaviour, read the requirement for it.**
[`docs/requirements/README.md`](docs/requirements/README.md) states what each mechanic must
do, whether it currently does it, and which acceptance criteria prove it. A test cites the
requirement it proves with `@Requirement` — see `docs/strategy/testing.md`. Update the
requirement and its status row in the same PR as the code; a behaviour change with no requirement change means one of the two is wrong. Why
the mod works this way, and every tunable number with its source location, live in
[`docs/concept/`](docs/concept/README.md).

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
| Data generation | Fabric datagen API, custom `runDatagen` Gradle task — the task **inherits `client`**, because Fabric routes model providers through a client-only mixin | `init/ModDataGenerator.java` → `src/main/generated/` |
| Hand-written resources | recipes, loot tables, tags, models, blockstates, textures, lang | `src/main/resources/{data,assets}/` |
| Generated resources | worldgen, and the `assets/iron_oak/items/` client item definitions | `src/main/generated/` |
| Access widener | present but **inert** — its only line is a comment, so it widens nothing | `src/main/resources/iron_oak.accesswidener` |
| Mixins | **config exists but is empty** — no mixin classes, no `mixin` package | `src/main/resources/iron-oak.mixins.json` |
| Resource generator | a Go helper that emits repetitive resource JSON | `scripts/generate.go` |
| CI | GitHub Actions, `./gradlew build` on Linux + Windows | `.github/workflows/main.yml` |

Version facts live in **`gradle.properties`** and nowhere else — `minecraft_version`,
`loader_version`, `loom_version`, `fabric_version`, `mod_version`. Read them before you
write a single API call; this mod is mid-migration and the answer changes.

**Mappings are Mojang official, not Yarn.** Yarn was discontinued after 1.21.11, so this
mod migrated off it. Class names here are the Mojang ones — `Identifier`,
`BlockBehaviour.Properties`, `BuiltInRegistries`. Any Yarn-era snippet you find online
(`AbstractBlock.Settings`, `Registries.BLOCK`, `new Identifier(...)`) needs translating
first: https://mappings.dev

---

## The JDK is the single most common way to waste an hour here

**Fabric Loom has line-specific JDK requirements.** This machine's default is JDK 25 (sdkman).

- **26.x line (main):** Loom 1.17 runs on JDK 25. No setup needed; the default is correct.
- **1.21.x line and earlier:** Loom does not run on JDK 22+, so every Gradle command needs JDK 21:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew build
```

Or `sdk use java 21.0.3-ms` for the shell. `.sdkmanrc` in the repo root records the
current line's JDK, so `sdk env` picks it up. If a build fails and you have not checked
`java -version`, check it before you debug anything else.

---

## Quality gates: run these before every commit

This list is a **copy of `.github/workflows/main.yml`** and is the only definition of
"green" in this repo:

```bash
export JAVA_HOME=~/.sdkman/candidates/java/21.0.3-ms
./gradlew build                 # compiles, remaps, builds the jar, runs the unit tests
./gradlew runGametest           # headless server, real world — the second CI gate
```

Add these when your change touches what they cover:

```bash
./gradlew runDatagen            # if you changed ModDataGenerator or anything it emits
./gradlew runClient             # if you changed rendering, or anything the tests do not reach
```

There **is** a test suite, in two layers, and
[`docs/strategy/testing.md`](docs/strategy/testing.md) says which layer a test belongs at.
Read it before you write one — it is short, and it names the mistakes this codebase is set
up to make.

- **`src/test/`** — plain JUnit with the loader booted and no world, via
  `fabric-loader-junit`. Milliseconds, and `./gradlew build` runs it, so a failing unit
  test fails the build. This is where names, ids, maps, committed resource files and
  numbers belong, and where the 6×3 matrix guard lives.
- **`src/gametest/`** — a headless server, a real world and actual ticking, via
  `fabric-gametest-api-v1`. Seconds, run by `./gradlew runGametest`, which writes JUnit XML
  to `build/gametest/report.xml`. Its own source set, so none of it ships in the mod jar.

"Green" is a stronger signal than it was, but it is not everything. **Rendering, particles,
sound and feel are still checked only by a human**, and so is anything the gametests do not
reach yet — `runClient` remains the gate for those, and if you did not launch the game, say
so in the PR instead of implying you did. Every gametest that lands is one line the human
no longer has to walk; that list is not finished.

When you fix a bug, the fix ships with the test that catches it, and you write the test
first — see
[`docs/strategy/test-driven-development.md`](docs/strategy/test-driven-development.md) for
where that rule applies and, just as importantly, where it does not. **Observe the red
locally and report it in the PR; never commit red.**

- Done means `gh pr checks <pr>` is green. A green local build is not proof — CI also
  builds on Windows, and the workflow pins JDK 21 for the same reason you have to. Both
  layers run on Linux and Windows, and CI uploads the JUnit XML of each.
- `./gradlew build` is incremental and Loom caches Minecraft; the first run after a
  version bump re-downloads and decompiles and can take several minutes. That is normal,
  not a hang.
- **The Gradle wrapper is 9.5.1 on purpose — do not re-pin it to 8.11.1.** That old pin
  is documented history from the 1.20.4 line: Loom **1.5** built an **empty jar and still
  reported `BUILD SUCCESSFUL`** on Gradle 8.12+, so the wrapper was held at 8.11.1. This
  line runs **Loom 1.17**, which requires Gradle 9.x and does not have that bug; pinning
  back to 8.11.1 breaks the build outright.
  The rule the pin protected still holds: when you touch the wrapper, Loom or Gradle,
  **verify the artefact, not the exit code**. The mod jar holds hundreds of files; the bug
  shipped **2**, so the check is an order of magnitude, not an exact count:

  ```bash
  unzip -l "$(find build/libs -name '*.jar' ! -name '*-sources.jar')" | tail -1
  ```

  Not `build/libs/iron-oak-*.jar` — that glob also matches the sources jar, and `unzip -l`
  over two archives prints `0 files`, which reads like the very bug you are checking for.
  Details in [`docs/ops/release.md`](docs/ops/release.md).

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
- **One closing keyword per issue.** GitHub honours the keyword only for the number directly
  after it, so `Closes #57, #50, #52` closes **#57 alone** and leaves the rest open with their
  work already merged. Write `Closes #57, closes #50, closes #52`.
- Do **not** force-push and do **not** rewrite history.
- Do not close issues by hand while a PR is open.
- Commit **per work item**, not once at the end: incremental commits survive a context
  compaction and can be harvested; a single final commit can be lost entirely.

### Version branches

`main` is the **frontier** — it tracks the newest supported Minecraft version. When `main`
moves on, the version it leaves behind is frozen onto its own branch, cut **from that
version's release tag**.

Branches are named `v<exact version>` — `v1.21.11`, not `v1.21.x`. The wildcard form lies as
soon as one Minecraft family holds two supported lines, which is exactly the case for 1.21.

| Track | Branch | State |
|---|---|---|
| 26.x | `main` | frontier |
| 1.21.11 | `v1.21.11` | supported |
| 1.21.1 | `v1.21.1` | planned — #54 |
| 1.20.4, 1.19, 1.18.2 | none, `1.19`, `v1.18.x` | **archived** — no CI, no releases, no backports |

Which lines are supported and why is [`docs/ops/multiloader.md`](docs/ops/multiloader.md).
**CI builds `main` plus every supported line and nothing else** — both branch lists in
`.github/workflows/main.yml` are exactly that set. An archive keeps its published jars and
its history, and gets no further work.

**Work goes forward first, then backwards.** Implement on `main`, then port down to the older
lines — never develop the same change twice in parallel. But here "backport" means **port**,
not `git cherry-pick`, because it crosses two real boundaries:

- **Renames.** `Identifier` on 26.x and 1.21.11 is `ResourceLocation` on 1.21.1;
  `ChunkSectionLayer` is `RenderType`. A cherry-pick across that does not apply.
- **Build regime.** `main` is unobfuscated on JDK 25; the 1.21.x lines are obfuscated on
  JDK 21 with a different Loom plugin id. Anything touching the build never cherry-picks.

So budget a hand-port per backport, and expect that a change written against 26.x may need
reworking — or rejecting — on an older line. Say which lines you ported to in the PR, and say
it plainly when you did not port to one.

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

The Java here is small — twenty-odd files, none longer than a few hundred lines — so reading
is cheap and you should not be shy about it. The expensive mistakes in this repo are elsewhere:

- **Never open `src/main/generated/`, `build/`, or `.gradle/`.** Generated worldgen JSON is
  bulky and tells you nothing a provider class won't. Grep it at most.
- **`ModBlocks.java` and `ModItems.java` are long but shallow** — the same three lines
  repeated across the matrix. Read one arm, not all eighteen.
- **The `java` skill is the checklist; `docs/strategy/java.md` is the reasoning.** Invoke the
  skill before you write Java here — it is short and it names the traps. Read `java.md` when
  you need to know why a rule exists, or before your first non-trivial change to `src/`.
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
| What a mechanic must do, and whether it does | `docs/requirements/README.md` — the status matrix |
| Why the mod works this way, and what a number is | `docs/concept/README.md`, `docs/concept/balance.md` |
| Which layer a test belongs at, and what a test here looks like | `docs/strategy/testing.md` |
| Whether your change needs a test first | `docs/strategy/test-driven-development.md` |
| Which requirement a test proves, and how to cite it | `docs/strategy/testing.md` — the `@Requirement` section |
| Current target versions | `gradle.properties` |
| Dispatch / gate / label process | `docs/ops/orchestration.md` |
| Label taxonomy | `docs/ops/issue-labels.md` |
| Supervisor loop runbook | `docs/ops/orca-progress-loop.md` |
| Migration plan and its stages | `docs/ops/version-migration.md` |
| Which versions and loaders we ship, and why | `docs/ops/multiloader.md` |
| How to cut a release / publish to Modrinth or CurseForge | `docs/ops/release.md` |
| Fabric API for the current version | https://docs.fabricmc.net/ |
| The rules for writing Java here, as a checklist | the `java` skill |
| Why the Java is shaped that way | `docs/strategy/java.md` |
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
| Dry-run a release upload | `./gradlew publishMods` (no tokens set = dry run) |
| Check available versions | https://modmuss50.me/fabric.html |

---

## When to ask, and when not to

**Ask** when the ticket is ambiguous, when the build fails in a way you cannot attribute,
when a Minecraft API you need does not exist in the target version (that is a real
finding — report it, do not invent a workaround), or when your goal conflicts with a
documented decision. Give context: what you tried, which files you read, the exact error.

**Do not ask** when the answer is in `gradle.properties`, in the Fabric docs, or one Grep
away — and do not ask only to have your understanding confirmed. Test it instead.
