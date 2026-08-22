---
domain: Orchestration
domain_code: INF
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - orca-progress-loop.md
  - issue-labels.md
  - version-migration.md
---

# Orchestration Policy

> **Audience: the orchestrator/supervisor only.** Workers read
> [`AGENTS.md`](../../AGENTS.md) instead — it is short so it does not eat their context.
> Never copy this file into a briefing.

Process rules for dispatching and accepting work. Most failure modes here are properties of
Orca and of LLM workers, imported from the board this setup came from; `[iron-oak]` marks
what is specific to this repo.

> **Reviewers:** if a pass finds this file or `AGENTS.md` modified alongside unrelated
> changes, commit it separately. Do not `git checkout`/`restore` it away as stray.

---

## Workers are Claude Sonnet, started through Orca

**Mandatory.** Dispatch only through the Orca CLI (`orca` on `PATH`). Never substitute a
coding assistant's own subagent/fork/Task feature — those carry no Orca dispatch provenance,
no lifecycle preamble and no `worker_done` authority, so nothing below can verify them. If
`orca status --json` does not show a ready runtime, say so plainly instead of falling back.

**The agent is `claude`, pinned to Sonnet.** Not `vibe`, not `codex`, not the runtime default.

```bash
orca status --json                                  # runtime ready?
orca worktree create --repo id:<repoId> --name issue-<n>-<slug> --issue <n> \
  --no-parent --base-branch main --json
orca terminal create --worktree path:<worktreePath> --title issue-<n>-<slug> \
  --command "claude --model sonnet --permission-mode bypassPermissions" --json
orca terminal wait --terminal <handle> --for tui-idle --timeout-ms 120000 --json
orca orchestration task-create --spec "<spec>" --json
orca orchestration dispatch --task <task_id> --to <handle> --json    # register, do NOT inject
# write the preamble to a file OUTSIDE the worktree, then deliver it:
orca orchestration dispatch --task <task_id> --to <handle> --return-preamble --dry-run --json
orca terminal send --terminal <handle> --text "Read <briefing-path> fully and execute it." --enter --json
orca terminal read --terminal <handle> --json       # MANDATORY: prove delivery
```

- `--model sonnet` is not optional. An unpinned invocation silently follows whatever the
  CLI default becomes.
- `--permission-mode bypassPermissions` is what an unattended worker needs; the other
  choices stop and wait for a human who is not watching.
- `orca terminal create --json` does not reliably return `result.handle` — read it from
  `orca terminal list --json`, matching on title or `worktreePath`.
- `orca worktree create --agent <id>` takes an Orca-known agent id and is **not** the path
  used here; `--command` is a plain shell command and is verifiable.
- Use `terminal create --worktree active` only for work in the current checkout. For a
  ticket it is `worktree create` — see [Worktrees](#worktrees-and-the-gradle-cache).

**Agent binaries self-update and exit.** Observed on the #52 dispatch: the agent updated
itself, quit to a bare shell, and two `--inject` attempts failed as `agent_prompt_stalled` —
indistinguishable from the known inject bug until the pane was read. Before diagnosing a
stalled dispatch, read the tail: a shell prompt instead of a TUI means restart the agent, not
retry the dispatch.

---

## Workflow

### Phase 1 — Design (conditional)

Required only for **player-visible mechanics or a new subsystem**: a processing step, a
machine block, a networking layer, a new metal or wood type (the 6×3 matrix).

Dispatch a design task → worker writes `docs/design/<feature>.md` and commits → orchestrator
opens a human gate → human approves / revises / rejects.

**Skipped** for bug fixes, texture and model work, recipe corrections, CI and toolchain work.
Never manufacture a design doc for a one-line fix.

### Phase 2 — Implementation

Break the issue into tasks, dispatch with dependencies. **Workers commit per work item**
before `worker_done`; the orchestrator verifies the commit exists and matches the format.

### Phase 3 — Review

**Auto-approve** requires *all* of: the change is confined to one `area:*`; `./gradlew build`
and `./gradlew runGametest` are green in CI on Linux **and** Windows; the change does not
touch in-world behaviour the gametests miss.

**Human review otherwise** — and here that catches more than elsewhere, because the tests do
not reach everything the player sees. CI proves the mod compiles, remaps and passes both
layers; it proves nothing about whether the fire bowl still *looks* like it is burning. Attach
the worker's `runClient` observation to the gate.

Any gate failure or human request → human decision.

**Human steering:** `status`, `pause all`, `resume`, `cancel <task>`, `retry <task>`,
`show gates`, `resolve gate <id> <choice>`, `require review <n>`, `skip review <n>`.

---

## GitHub issues are the backlog

> Plan and track in GitHub, execute in Orca. Never do substantial work with no issue behind
> it — if a task is worth dispatching, it is worth an issue.

| Element | Convention |
|---|---|
| **Milestone** | A release target (`1.21.11 Port`, `26.2 Port`). Every scoped issue has one. |
| **Type label** | Exactly one of `bug`, `enhancement`, `documentation`. |
| **Area label** | `area:` + `blocks`/`items`/`recipes`/`worldgen`/`datagen`/`client`/`assets`/`build`. One or two; three means split the issue. |
| **Flow label** | `blocker` (blocks the next release), `decision` (needs a human). Neither blocks dispatch — only `status:*` does. |
| **Status label** | `status:in-progress` (the dispatch lock, max 1 worker/issue), `status:needs-human`, `status:changes-requested` (human-set only). Never two at once. |
| **Labels are a closed set** | **Only a human creates, edits or deletes labels.** Agents never run `gh label create|edit|delete`. Need one that does not exist? Set the nearest existing label, propose the new one as an issue comment, carry on. Taxonomy: [`issue-labels.md`](issue-labels.md). |
| **Issue body** | Goal, acceptance criteria as checkboxes, dependencies as `Blocked by #n`, a `Ref:` to the relevant files. For a bug: the Minecraft and mod version it was seen on. |

### Player bug reports need triage first

This is a public mod, so issues arrive from users. A player report is **not dispatchable as
filed** — it usually lacks mod version, Minecraft version, loader, and whether other mods were
present. Establish those or set `status:needs-human` and ask. Dispatching a worker at "washing
ash doesn't work" with no version produces a worker that reads the whole recipe subsystem and
reports it looks fine.

A bug against a Minecraft version `main` no longer targets is a **version-branch decision**,
not a code task. It goes to a human.

### Per-issue execution loop

```bash
gh issue view <n> --json title,labels,body        # 1. ready? no open "Blocked by", not human-owned
# 2. claim BEFORE creating anything: label status:in-progress, then comment
# 3. isolated worktree + Claude Sonnet worker  (command chain above)
# 4. verify the worktree base:
git -C <worktree> rev-parse HEAD; git rev-parse origin/main     # must match
git -C <worktree> reset --hard origin/main                      # if they diverge
# 5. task-create + dispatch + PROVE DELIVERY (below)
# 6. worker commits `fix(recipes): … (#<n>)` and opens a PR whose body has "Closes #<n>"
# 7. verify worker_done's commit references #<n>, then let the PR close the issue
```

`--base-branch` resolves against the **local** ref. While local `main` is behind
`origin/main`, every fresh worker gets a base without the most recently merged work — often
exactly what unblocked its ticket. It then assumes the foundation is absent and reinvents it.
Hence step 4, every time.

### Dispatch delivery is not proven by `status: dispatched`

`dispatch --inject` has returned `ok`, moving the task to `dispatched`, while the worker's TUI
stayed idle (observed under `vibe`, whose status line read `0/200k`). A supervisor that trusts
the task status reports running workers that are idle
and loses a whole interval.

**Deliver with `terminal send`, not `--inject`.** `--inject` has failed on every dispatch
attempted in this repo so far (#52, #20 — ledger #61), each time costing two round-trips before
the fallback ran. `terminal send` has worked every time. So register the dispatch without
`--inject` to get a live context, then deliver the preamble by hand. Replace `ctx_dryrun` in
the preamble with the real id from `dispatch-show`, and write the file **outside** the worktree
so it never lands in a commit.

**Prove delivery either way.** Poll `orca terminal read` for 10–30 s. A single read immediately
after delivery always shows zero and is not evidence.

**Two ordering traps in the fallback**, both observed:
- A task already in `dispatched` cannot be dispatched again (`only ready tasks can be
  dispatched`). Register **once**, then deliver into that context.
- A `--inject` attempt that fails leaves the dispatch `failed` with `capability_revoked_at`
  set. Delivering that preamble gives the worker no `worker_done` authority — register a fresh
  dispatch instead of reusing the dead one.

- **Never match the agent's status-line format.** It is agent-specific and changes under you.
  `vibe` printed `0/200k`; `claude` prints `↓ 24.7k tokens` with no denominator at all, so a
  pattern written for one silently never fires for the other — a poll loop then hangs, or worse,
  reports a working worker as dead. Measured the day workers moved to Claude Sonnet.
  **Liveness comes from `heartbeat` messages** in `orca orchestration inbox --json` — a
  first-class Orca signal with a `phase`, emitted by every agent this board has used, and
  independent of any pane rendering. Filter the inbox to your own run and handles: it is shared
  across every repo on the machine, and issue numbers collide.

  **Progress comes from the worktree, not the pane:**
  ```bash
  git -C <wt> log --oneline <base>..HEAD          # a commit is proof
  git -C <wt> status --porcelain                  # uncommitted progress
  find <wt> -type f -not -path '*/.git/*' -not -path '*/build/*' -newermt '-3 minutes'
  ```
  `latestCursor` from `orca terminal read --json` is **not** portable either: it advanced with
  `vibe` and sits fixed at `5` for `claude`, so cursor movement reports every Claude worker as
  idle. Measured on the #54 dispatch. Read the tail with a human eye when diagnosing — a
  spinner line with an elapsed time means working, the bare prompt means the turn ended — but
  do not build an automated check on either the cursor or the tail's shape.
- **`tui-idle` does not mean the TUI accepts input, and does not explain the failures.**
  `wait --for tui-idle` has reported `ok` while the tail still showed `Initializing…`. But the
  race theory was disproved in the same run: another dispatch was provably past init and
  `--inject` still failed silently. Keep the check; do not treat it as a safeguard. There is
  no known precondition that makes `--inject` reliable, so measuring after **every** dispatch
  stays mandatory.
- **Re-delivery recipe** when the counter stays zero:

  ```bash
  orca orchestration dispatch --task <id> --to <handle> --json            # live ctx, no inject
  orca orchestration dispatch --task <id> --to <handle> --return-preamble --dry-run --json
  # write result.preamble OUTSIDE the worktree, replace ctx_dryrun with the id from dispatch-show
  orca terminal send --terminal <handle> --text "Read <path> fully and execute it." --enter --json
  ```

- **A `terminal send` to a terminal that already got the briefing is not harmless.** It
  executes a second time. Observed on two workers in one run: one restarted its entire
  migration at 82 % context, the other discarded its accepted commit, force-pushed a
  replacement, and pulled another ticket's scope into its PR. Double delivery invalidates a
  completed review — it does not merely cost tokens.
- A task already in `dispatched` cannot be dispatched again (`only ready tasks can be
  dispatched`). Deliver into the existing dispatch instead.

### Worktrees and the Gradle cache

Every issue gets its own worktree, and each gets its own `.gradle/` and `build/`.

- `[iron-oak]` **A fresh worktree's first `./gradlew build` re-runs Loom's Minecraft setup** —
  download plus decompile, several minutes. Do not read it as a hung worker; check the token
  counter, not the clock.
- `~/.gradle` caches decompiled Minecraft across worktrees, so the *second* worktree on a
  version is fast. The first after a bump is slow for everybody.
- **Two workers on different Minecraft versions thrash that cache.** This is the concrete
  reason version bumps are serial.
- **Every briefing states the JDK**, because a worktree does not inherit the parent shell's
  `JAVA_HOME`. Which JDK depends on the line — see `AGENTS.md`.

### Version migrations are serial

An `area:build` ticket that changes `minecraft_version`, `loader_version`, `fabric_version`,
the Loom version or the Gradle wrapper is **never dispatched in parallel with anything**, and
nothing else is dispatched while it is open. In cost order:

1. Every in-flight PR is written against the old API. After the bump they do not merge and do
   not compile.
2. A mapping migration **rewrites every source file**. Any branch not merged first is a manual
   re-port.
3. Concurrent builds on two Minecraft versions thrash the Loom cache.

Drain the queue, land the bump, resume. Plan: [`version-migration.md`](version-migration.md).

---

## Briefing rules

- **Commit and PR format** is defined once, in [`AGENTS.md`](../../AGENTS.md#git). Point
  there; do not restate it.
- **A PR closing several issues needs one keyword each** — `Closes #a, closes #b`. A
  comma-separated list after a single keyword closes only the first, and the others stay open
  with their work merged, which no later round detects: they are not stuck, not dispatchable
  (work exists) and carry no signal. Verify after every multi-issue merge that each issue
  actually closed.
- **Green means the CI command list**, which lives in `AGENTS.md` and is a copy of
  `.github/workflows/main.yml`. Whoever changes the workflow updates it in the same commit,
  or the gap migrates silently into every future briefing. `worker_done` requires
  `gh pr checks <pr>` green — a green local build is not proof, CI also builds on Windows.
- **"Compiles" is not "works" — say which you verified.** Both test layers exist, but between
  them they cover ids, committed resources, the 6×3 matrix and part of the fire bowl, and
  nothing a player looks at. A briefing touching rendering, particles, sound or feel asks for
  a `runClient` observation in the PR body. If it is missing, ask for the observation — not
  for the build status again.
- **Phrase a scope boundary as an exception inside the rule, not a paragraph after it.**
  "Migrate deprecated calls in every file you touch" plus "the registry rewrite belongs to
  #31" are mutually exclusive the moment both apply, and the worker resolves the
  contradiction itself — not the same way twice. Write: "Migrate deprecated calls in the files
  you touch, **except** `init/Mod*.java`, which belong to #31; move those unchanged."
- **Brief positively, never through a negation.** "Do NOT open a PR with `Closes #n`, use
  `Refs #n`" was read as "do not open a PR at all": `worker_done` with no PR. State the
  required action as the only imperative, then put prohibitions in a separate sentence.
- **One briefing per work item — the context window is a hard limit.** Past it the model
  compacts, re-reads, compacts again: a loop that looks like work and never delivers. Detect it
  from the worktree, not from a counter — **no new commit across rounds while the pane keeps
  producing output** is the shape, whatever the agent's status line looks like.
  - Split a multi-item ticket into several tickets dispatched in sequence, rather than one
    briefing with a numbered list. Rule of thumb: at most two files to read.
  - Require incremental commits per work item, so progress survives compaction and can be
    harvested.
  - Pass findings as `file:line` — the cheapest lever there is.
  - `[iron-oak]` **Name the context sinks explicitly.** The Java is small, so the usual "three
    big files" driver does not apply. What blows the window here is `src/main/generated/`,
    decompiled Minecraft sources, and `runClient` log output. Tell the worker not to read the
    first two (use https://mappings.dev and https://docs.fabricmc.net) and to quote only the
    relevant log lines.
- **Dependencies** are `Blocked by #n` in the body. Prose like "depends on most of the other
  issues" is not a dependency — the loop cannot read it and treats the issue as ready.
- **Human-owned by body, not just by label.** An issue whose goal requires acting outside the
  repo — publishing a release, replying to a bug reporter, choosing supported versions — is
  human-owned with or without a label. Set `status:needs-human`.
- **Gated (multi-phase) issues** are dispatched one phase at a time, and the phase PR uses
  `Refs #<n>` — never a closing keyword, or the issue dies before the last phase. After a
  phase lands, move to `status:needs-human` and name the concrete decision.
- **Parallelism** needs disjoint *file surfaces*, not just different `area:*` labels.
  `[iron-oak]` The classic collision is anything against `area:datagen`: a datagen change
  rewrites `src/main/generated/`, and every ticket adding a block or item regenerates it too —
  two correct PRs, not mergeable together. Second: `area:assets` against
  `area:blocks`/`area:items`, which write the same model, blockstate and lang JSON.
- **A version line is delivered by its branch existing, never by a PR into `main`.** A ticket
  that cuts or ports a line has **two halves, delivered differently**: the branch is pushed
  directly, and only the *documentation* recording the line goes to `main` through a PR. Say
  both halves separately in the briefing. Collapsing them into "push the branch and open a PR
  against `main`" produced #68 — a PR that would have taken the frontier backwards from 26.2 to
  1.21.1 across 90 files, stopped only by GitHub's conflict marker.
- **Cross-cutting rebuilds do not belong in a feature ticket.** A worker whose goal needs a
  repo-wide structural change (all registrations, the mapping set, the toolchain) reports back;
  the orchestrator opens a separate ticket and orders the merges.
- **New work mid-flight** is reported, never silently absorbed. The orchestrator opens the
  issue.
- **Status** is: issue open = not done; PR merged = done.

---

## Orchestrator checklist

- [ ] Every worker started via Orca, as `claude --model sonnet`
- [ ] Delivery proven after every dispatch before reporting a worker as running
- [ ] Every `worker_done` backed by a verified commit
- [ ] The correct JDK named in every briefing that runs Gradle
- [ ] No `area:build` version ticket dispatched in parallel with anything
- [ ] Player bug reports triaged for version info before dispatch
- [ ] `runClient` observation required for runtime-behaviour changes
- [ ] Every dispatched task backed by a GitHub issue
- [ ] A human gate at every phase transition that has one
- [ ] An exception gate on any gate failure

### Commit verification

1. The hash exists.
2. The message is Conventional Commit per `AGENTS.md` and carries `(#<n>)`.
3. It is on the issue branch, not `main`.
4. It contains changes relevant to the task.
5. `[iron-oak]` If it touches `init/ModDataGenerator.java` or anything it emits,
   `src/main/generated/` is in the **same** commit. A Java change without its regenerated
   output is a broken commit for the next person.
6. `gh pr checks <pr>` is green.

Failure → escalate to the worker ("commit does not match the required format; amend and
resend `worker_done`") and do not mark the task complete.

---

## Gates

| Gate | Created when | Options |
|---|---|---|
| `design_approval` | a design task completes (phase 1) | approve / revise / reject |
| `implementation_acceptance` | in-world behaviour touched, risk is high, or the human asks | accept / revise / reject |
| `exception_gate` | build fails unattributably, **or** a needed Minecraft API does not exist in the target version | accept / fix / reject |

Neither times out; a human resolves them.

**"The API I need does not exist in this version" is a legitimate finding, not worker
failure.** Minecraft removes and reshapes API across versions, and a worker that invents a
workaround around a removed method produces something that compiles and misbehaves. Say so in
briefings, and route such a report to a gate rather than re-dispatching it.

### Status flow

```
PENDING → READY → DISPATCHED → COMPLETE (commit) → VERIFIED → REVIEW
                                                       ├─ auto-approve → COMPLETED
                                                       └─ human gate → WAITING_HUMAN
                                                            ├─ ACCEPTED  (complete)
                                                            ├─ REVISED   (fix tasks → Implementation)
                                                            └─ REJECTED  (cancelled)
```

| Exception | Action |
|---|---|
| Missing or malformed commit | escalate to worker |
| Java change without regenerated output | escalate to worker |
| Unattributable build failure | exception gate |
| Required API absent in target version | exception gate |
| In-world behaviour changed | human review gate |

---

## Scheduled supervision

One scheduled supervisor reconciles the board each interval — collect finished workers,
dispatch collision-free, comment intermediate results, and self-correct. Runbook and loop
prompt: [`orca-progress-loop.md`](orca-progress-loop.md). **Run only one instance at a time.**

Board commands:

```bash
gh issue create --milestone <ms> --label <type> --label <area> --title <t> --body <b>
gh issue list --milestone <ms>
gh issue edit <n> --add-label blocker
gh issue comment <n> --body <text>
```
