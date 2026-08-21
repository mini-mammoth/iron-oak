---
domain: Orchestration
domain_code: INF
status: active
last_updated: 2026-08-21
version: 1
related:
  - ../../AGENTS.md
  - ops/orca-progress-loop.md
  - ops/issue-labels.md
  - ops/version-migration.md
---

# Orchestration Policy

> **Audience: the orchestrator/supervisor only.** Workers do not read this file — they
> read [`AGENTS.md`](../../AGENTS.md), which is deliberately short so it does not eat
> their context window. Do not copy this file into a worker briefing.

This file holds the process rules for dispatching and accepting work: the Orca CLI
protocol, the GitHub-issue loop, the gates, and the failure modes learned the hard way.
It is process policy, not task output.

Most of the failure modes below were **imported** from the board this setup came from,
not learned in this repo. They are kept because they are properties of Orca and of
LLM workers, not of that project. Where a rule has not yet been re-observed here, it says
so — do not treat it as this repo's history.

**Note to reviewers:** if a review/commit pass finds this file or `AGENTS.md` modified
alongside unrelated task changes, commit it separately (or leave it — do not
`git checkout`/`restore` it away) rather than discarding it as stray.

---

## 🎛️ Orchestrator Tooling Rule (MANDATORY)

Any agent acting as **orchestrator** for this project MUST start and dispatch worker
sessions through the **Orca CLI** (`orca` on `PATH`). Never substitute an in-process or
ad-hoc subagent mechanism (a coding assistant's own built-in subagent/fork/Task feature)
for starting a worker — those may produce useful work, but they carry no Orca
task/dispatch provenance, no injected lifecycle preamble and no `worker_done` authority,
so the orchestrator cannot verify or track them the way this file requires.

**To start a worker session:**

```bash
orca status --json                                             # confirm runtime is ready
orca terminal create --worktree active --title <task-name> --command "vibe" --json
orca terminal wait --terminal <handle> --for tui-idle --timeout-ms 60000 --json
orca orchestration task-create --spec "<task spec>" --json     # if the task doesn't exist yet
orca orchestration dispatch --task <task_id> --to <handle> --inject --json
orca orchestration check --wait --types worker_done,escalation,decision_gate --timeout-ms <n> --json
```

Use `orca worktree create` instead of `terminal create` when the task needs an isolated
worktree rather than the active one — which for this repo is **almost always**, see
[Worktrees](#worktrees-and-the-gradle-cache).

The full protocol (messaging, gates, task DAGs, dispatching to other agent types) is
documented in the `orchestration` skill. Before claiming a worker was orchestrated,
verify the task/dispatch actually exists with `orca orchestration task-list --json` /
`dispatch-show`. If `orca` is not on `PATH` or `orca status --json` does not show a ready
runtime, say so plainly rather than silently falling back to a different subagent tool.

---

## 🎯 Workflow

The source setup ran a four-phase gated workflow over human-owned EARS requirements. This
repo has **no requirements corpus** — it is a small mod whose intended behaviour is
documented in `README.md` and in the code. So the workflow here is three phases, and the
design phase is conditional:

### Phase 1: Design (CONDITIONAL — orchestrator + human gate)

Required only for work that changes **player-visible mechanics** or adds a subsystem:
a new processing step, a new machine block, a networking layer, a new metal or wood type
(the 6×3 matrix). Then:

- Orchestrator dispatches a design task; the worker writes `docs/design/<feature>.md`.
- Worker commits the design and sends `worker_done`.
- **Orchestrator creates a human gate**: "Approve design for {feature}?"
- Human resolves: approve / revise / reject.

**Skipped** for bug fixes, texture/model work, recipe corrections, CI and toolchain work.
Do not manufacture a design doc for a one-line fix.

### Phase 2: Implementation (orchestrator)

- Orchestrator breaks the issue into implementation tasks and dispatches with
  dependencies.
- **Workers MUST commit per work item** before sending `worker_done`.
- Orchestrator verifies the commit exists and matches the format.

### Phase 3: Review (conditional — auto or human)

- **Auto-approval** requires all of: the change is confined to one `area:*`, `./gradlew
  build` and `./gradlew runGametest` are green in CI on both Linux and Windows, and the
  change does **not** touch in-world behaviour the gametests do not reach.
- **Human review otherwise** — and in this repo that still catches more than it would
  elsewhere, because **the tests do not reach everything the player sees**. CI proves the mod
  compiles and remaps and that both test layers pass; it proves nothing about whether the
  fire bowl still *looks* like it is burning. Anything touching runtime behaviour outside
  what the gametests cover goes to a human gate with the worker's `runClient` observations
  attached.
- **Any gate failure or human request → human decision.**

### Human steering

The human can steer with: `status`, `pause all`, `resume`, `show gates`,
`resolve gate <id> <choice>`, `require review <issue>`, `skip review <issue>`.

---

## 🎫 GitHub Issue Tracking

GitHub Issues are the **single source of truth for the backlog**. Orca worktrees and
workers are the **execution layer**. The issue tracks, Orca does the work, the PR closes
the issue.

> Rule of thumb: **plan and track in GitHub, execute in Orca.** Never do substantial work
> with no GitHub Issue behind it — if a task is worth dispatching, it is worth an issue.

### Structure

| Element | Convention |
|---------|------------|
| **Milestone** | A release target (e.g. `1.21.11 Port`, `26.2 Port`). Every scoped issue is assigned to one. |
| **Labels — type** | Exactly one of `bug`, `enhancement`, `documentation`. |
| **Labels — area** | `area:blocks`, `area:items`, `area:recipes`, `area:worldgen`, `area:datagen`, `area:client`, `area:assets`, `area:build`. One or two; three means the issue is too big and must be split. |
| **Labels — flow** | `blocker` (blocks the next release), `decision` (needs a human decision). Neither blocks dispatch — only `status:*` does. |
| **Labels — status** | `status:in-progress` (the **dispatch lock**, max 1 worker/issue), `status:needs-human`, `status:changes-requested` (human-set only). Never two at once. |
| **Labels — closed set** | **Only a human creates, edits or deletes labels.** Agents never run `gh label create|edit|delete`. Need a label that does not exist? Set the nearest existing one, propose the new label as an issue comment, and carry on — the proposal is not a blocker. Full taxonomy: [`issue-labels.md`](issue-labels.md). |
| **Issue body** | Goal, acceptance criteria (checkbox list), dependencies (`Blocked by #n`), and a `Ref:` pointer to the relevant file(s). For a bug: the Minecraft + mod version it was seen on. |

### Bug reports from players need triage before dispatch

This is a public mod, so issues arrive from users, not only from the board. A player's
bug report is **not dispatchable as filed** — it usually lacks the mod version, the
Minecraft version, the loader and whether other mods were present. Before dispatch, the
orchestrator either establishes those from the report or sets `status:needs-human` and
asks. Dispatching a worker at "washing ash doesn't work" with no version produces a
worker that reads the whole recipe subsystem and reports that it looks fine.

An open bug against a Minecraft version the current `main` no longer targets is a
**version-branch decision**, not a code task: it goes to a human.

### Per-issue execution loop (orchestrator)

```bash
# 1. Pick a ready issue (no open "Blocked by", not decision/blocker-human)
gh issue view <n> --json title,labels,body

# 2. Isolated worktree for that issue.
#    Note: "vibe" is NOT a known TUI agent id — `--agent vibe` fails with "Unknown TUI agent".
orca worktree create --repo id:<repoId> --name issue-<n>-<slug> --issue <n> \
  --no-parent --base-branch main --json

# 2b. --base-branch takes the LOCAL ref. If local main is behind origin/main, the worker
#     is missing the most recently merged work — often exactly what unblocked its ticket.
git -C <worktreePath> rev-parse HEAD; git rev-parse origin/main   # must match
git -C <worktreePath> reset --hard origin/main                    # if they diverge

# 3. Worker inside it. `terminal create --json` does NOT return result.handle —
#    read the handle from `orca terminal list --json` (match on title/worktreePath).
orca terminal create --worktree path:<worktreePath> --title issue-<n>-<slug> --command "vibe" --json
orca terminal list --json      # -> handle
orca terminal wait --terminal <handle> --for tui-idle --timeout-ms 120000 --json

# 4. Task + dispatch
orca orchestration task-create --spec "Resolve GitHub issue #<n>: <title>. Acceptance: <criteria>" --json
orca orchestration dispatch --task <task_id> --to <handle> --inject --json

# 5. VERIFY DELIVERY (mandatory — see rule below). `--inject` can report ok and mark the
#    task `dispatched` without anything reaching the TUI.
orca terminal read --terminal <handle> --json      # token counter must be > 0

# 6. Worker commits referencing the issue, e.g.  fix(recipes): washing ash yields shreds (#15)
#    and opens a PR whose body contains  "Closes #<n>"  so merge auto-closes it.

# 7. Verify worker_done has a valid commit that references #<n>, then let the PR close it.
```

### Dispatch delivery is not proven by `status: dispatched`

`dispatch --inject` has been observed returning `ok` — with the task moving to
`dispatched` — while the worker's TUI stayed empty at `0/200k tokens`. A supervisor that
trusts the task status then reports running workers that are in fact idle, silently losing
a whole interval. So after every dispatch, confirm the worker actually picked the work up.

> **Check first, then re-deliver — never both unchecked.** `--inject` has failed silently
> in some runs and succeeded in others; delivery is unreliable in BOTH directions and must
> be measured. Order: dispatch, poll for ~10–30 s (`orca terminal read`), and re-deliver
> explicitly **only if the counter is still 0**. A single read immediately after dispatch
> still shows `0/200k` and is not evidence of failure — poll first, then judge.
>
> **`tui-idle` does not mean the TUI accepts input — but that does NOT explain the
> failures.** `orca terminal wait --for tui-idle` has reported `ok` while the tail still
> showed `⠈⠉ Initializing…`, and the dispatch that followed never arrived. The obvious
> explanation — a race with TUI initialisation — was disproved in the same run: another
> ticket proved the tail was past `Initializing…` before dispatch and `--inject` still
> failed silently (0 tokens over 7 polls, ~40 s). Delivery via `terminal send` started
> immediately in both cases. Take two lessons, not one: the `Initializing` check is
> worthwhile, but it is NO safeguard against the inject failure. There is currently no
> known precondition that makes `--inject` reliable. So measuring after EVERY dispatch
> stays mandatory, and anyone who writes down an explanation for the failures verifies it
> on the next dispatch instead of filing it as solved.
>
> **The counter reads `0/200k` with NO `k` before the slash.** A poll pattern like
> `[0-9]+k/200k` therefore does not match zero, returns empty — and a `case` branch that
> reads "empty" as "no longer zero" reports a worker as active that never started. Match
> the counter against `[0-9]+k?/200k` and treat an EMPTY read as `0`. A false-positive
> activity check locks the ticket for a whole interval.
>
> **A `terminal send` to a terminal that already got the briefing via `--inject` is NOT
> harmless:** the message lands in the queue and the worker executes the briefing a SECOND
> time after having finished it. Observed on two workers in one run: one restarted its
> entire migration at 82 % context, and the other discarded its already-accepted commit,
> replaced it via force-push, and pulled another ticket's full scope into its PR. Double
> delivery does not just cost tokens — it invalidates a completed review.

```bash
orca orchestration dispatch --task <task_id> --to <handle> --return-preamble --dry-run --json
# write result.preamble to a file OUTSIDE the worktree (it contains the full spec +
# worker protocol), replace the ctx_dryrun placeholder with the real dispatch id from
# `orca orchestration dispatch-show --task <task_id> --json`, then:
orca terminal send --terminal <handle> --text "Read <briefing-path> fully and execute it." --enter --json
```

### Worktrees and the Gradle cache

Every issue gets its own worktree, and each worktree gets its own `.gradle/` and
`build/`. That matters more here than in a JS project: **a fresh worktree's first
`./gradlew build` re-runs Loom's Minecraft setup**, which downloads and decompiles and can
take several minutes. Consequences for dispatch:

- Do not read a long first build as a hung worker. Check the token counter, not the clock.
- The shared Gradle user home (`~/.gradle`) caches the decompiled Minecraft across
  worktrees, so the *second* worktree on the same Minecraft version is fast. The first one
  after a version bump is slow for everybody.
- **Two workers on different Minecraft versions will thrash that cache.** This is the
  concrete reason version bumps are serial (below).
- Every briefing must state the JDK requirement, because a worktree does not inherit the
  parent shell's `JAVA_HOME`: workers run Gradle with JDK 21 or they get a failure that
  looks like broken code. See [`AGENTS.md`](../../AGENTS.md).

### Version migrations are serial

A ticket labelled `area:build` that changes `minecraft_version`, `yarn_mappings`,
`loader_version`, `fabric_version`, the Loom version or the Gradle wrapper is **never
dispatched in parallel with anything else**, and no other ticket is dispatched while it is
open. Reasons, in order of how much they cost:

1. Every other in-flight PR is written against the old API. After the bump lands they do
   not merge — they do not even compile.
2. The mapping migration (yarn → Mojang official) **rewrites every source file**. Any
   branch not merged before it is a manual re-port.
3. Concurrent builds on two Minecraft versions thrash the Loom cache.

So: drain the queue, land the bump, then resume. The staged plan and its gates live in
[`version-migration.md`](version-migration.md).

### Conventions

- **Commit / PR references**: every commit for an issue includes `(#<n>)`; the PR body
  uses a closing keyword (`Closes #<n>`) so the merge closes the issue. Do **not** close
  issues by hand while a PR is open. The commit format is defined once, in
  [`AGENTS.md`](../../AGENTS.md#git) — briefings point there instead of restating it.

- **Green means green with the CI command, not one of your own choosing.** The binding
  command list is in [`AGENTS.md`](../../AGENTS.md#quality-gates-run-these-before-every-commit)
  and is a copy of `.github/workflows/main.yml`; **whoever changes the workflow updates it
  in the same commit** — otherwise the gap migrates silently into every future briefing.
  A worker reports `worker_done` only once `gh pr checks <pr>` is green. A green local
  build is not proof: CI also builds on Windows.

- **"Compiles" is not "works" — say which one you verified.** This repo has no tests, so
  the usual shortcut (green CI ⇒ behaviour intact) does not hold. A briefing that touches
  runtime behaviour must ask for a `runClient` check and for the observation to be written
  into the PR body. A worker that reports "build green" for a fire-bowl timing fix has
  verified nothing about the fix. Ask for the observation, not the build status.

- **Dependencies**: declare with `Blocked by #n` in the body. The orchestrator does not
  dispatch an issue whose blockers are still open. Prose like "depends on most of the
  other issues" is **not** a dependency — the loop cannot read it and will treat the issue
  as ready. Gate and checklist issues must spell their blockers out as `Blocked by #n`.

- **Human-owned by body, not just by label**: an issue whose goal requires acting outside
  the repo (publishing a release to Modrinth/CurseForge, replying to a bug reporter,
  choosing which Minecraft versions to support) is human-owned even without a label. The
  supervisor sets `status:needs-human` rather than dispatching, so later intervals do not
  pick it up.

- **Gated (multi-phase) issues** are dispatched one phase at a time. The phase PR uses
  `Refs #<n>` — **never** a closing keyword, because the issue must survive until the last
  phase merges. After the phase lands, the supervisor moves the issue from
  `status:in-progress` to `status:needs-human` and names the concrete decision in the
  comment; the next phase is dispatched only after the human replies.

- **A rule and a scope boundary must not contradict each other in a briefing.** "Migrate
  the deprecated API calls in every file you touch" and "the registry rewrite belongs to
  #31" are mutually exclusive the moment the worker must touch one of those files. The
  worker then resolves the contradiction itself — and not necessarily the same way twice.
  Observed: on the first pass the scope boundary won, on the second the migration rule
  did, rebuilding the PR and breaking "1 PR = 1 issue". So phrase the boundary as an
  exception INSIDE the rule, not as a separate paragraph after it: "Migrate deprecated
  calls in the files you touch — **except** `init/Mod*.java`, which belong to #31; move
  those unchanged."

- **Brief workers positively, never through a negation.** A briefing of the form "do NOT
  open a PR with `Closes #n` — use `Refs #n` instead" was read as "do not open a PR at
  all"; the worker reported `worker_done` with no PR and the supervisor had to build it.
  State the required action as the only imperative: "Open a PR against main. The body
  contains exactly `Refs #n`." Prohibitions go in a separate sentence afterwards.

- **Parallelism**: issues with no shared blocker and different `area:*` labels may be
  dispatched concurrently, each in its own worktree. **The same `area:*` is not a free
  pass** — before dispatching two tickets in one area, compare their expected file surface
  and start only if it is disjoint. In this repo the classic collision is
  `area:items`/`area:blocks` against `area:datagen`: a datagen change rewrites
  `src/main/generated/`, and any ticket that adds a block or item also regenerates it. Two
  correct PRs, not mergeable together. On foreseeable overlap: dispatch sequentially, or
  name the already-open PR to the second worker as its base.

- **One briefing per work item — 200k context is the hard limit.** A worker has a 200k
  context window. When that is not enough the model compacts, loses context, re-reads the
  same files and compacts again: a **compaction loop** that looks like work from outside
  and never delivers. The detection signal is a **falling** token counter. In this repo
  the Java is small (twenty-odd files, none longer than a few hundred lines), so the driver
  is different from a typical web project — the reading surface that actually blows the window here is
  **generated worldgen JSON, decompiled Minecraft sources, and `runClient` log output**.
  Consequences for scoping:
  - A ticket with several independent work items is **split** into several tickets and
    dispatched in sequence, not written as one briefing with a numbered list. Rule of
    thumb: a task that needs at most two files read.
  - Require **incremental commits per work item**, so progress survives a compaction and
    can be harvested.
  - Pass findings as `file:line` instead of making the worker search — the cheapest lever
    against context pressure.
  - Tell the worker explicitly **not** to read `src/main/generated/` or decompiled
    Minecraft sources. Left to itself it will, to "get oriented", and that is the single
    biggest context sink available to it here.

- **Cross-cutting rebuilds do not belong in a feature ticket**: if a worker finds its goal
  is only reachable through a repo-wide structural change (rewriting all registrations,
  changing the mapping set, touching the toolchain), it reports back instead of shipping it
  in the feature PR. The supervisor opens a separate ticket and orders the merges.

- **New work mid-flight**: if a worker discovers required work outside its issue's scope,
  it does **not** silently expand scope — it reports back so the orchestrator opens a new
  issue (`gh issue create --milestone <ms> --label <type> --label <area>`).

- **Status**: issue open = not done; PR merged / issue closed = done. The milestone view
  (`gh issue list --milestone <ms>`) is the board.

### Scheduled progress supervision

A single **scheduled supervisor** reconciles the board each interval: collect
finished/escalated workers, dispatch ready tickets **collision-free** (claim via
`status:in-progress` **before** creating the worktree), append intermediate results to each
issue, and self-correct the process. Full runbook and copy-paste loop prompt:
[`orca-progress-loop.md`](orca-progress-loop.md). Run **only one** loop instance at a time.

---

## 🤖 Orchestrator Responsibilities

### Mandatory actions

- [ ] Start every worker session via the Orca CLI — never via a non-Orca subagent
- [ ] Verify delivery after every dispatch (token counter > 0) before reporting a worker
      as running
- [ ] Verify every `worker_done` has a valid commit
- [ ] State the JDK-21 requirement in every briefing that runs Gradle
- [ ] Never dispatch an `area:build` version ticket in parallel with anything
- [ ] Triage player bug reports for version info before dispatch
- [ ] For runtime-behaviour changes, require a `runClient` observation, not a build status
- [ ] Ensure every dispatched task is backed by a GitHub Issue; open one before dispatch
      if missing
- [ ] Create a human gate at every phase transition that has one
- [ ] Block workers that bypass the commit protocol
- [ ] Create an exception gate on any gate failure

### Commit verification

The orchestrator MUST verify:

1. The commit hash exists in the repository.
2. The message follows the Conventional-Commit format defined in
   [`AGENTS.md`](../../AGENTS.md#git) and carries the issue reference `(#<n>)`.
3. The commit is on the issue's branch (`issue-<n>-<slug>`), not on `main`.
4. The commit contains changes relevant to the task.
5. If the change touches `init/ModDataGenerator.java` or anything it emits, that
   `src/main/generated/` is committed **in the same commit** — a Java change without its
   regenerated output is a broken build for the next person.
6. `gh pr checks <pr>` is green — a green local check is not a substitute.

If verification fails, send an escalation to the worker ("Commit does not match the
required format. Amend it and resend `worker_done`") and do **not** mark the task
complete.

---

## 🚦 Gate Types Reference

### design_approval
- **Created when:** a design task completes (conditional phase 1)
- **Question:** "Approve design for {feature}? Review {design_doc_path}"
- **Options:** approve, revise, reject
- **Timeout:** none — the human must explicitly resolve

### implementation_acceptance
- **Created when:** the change touches in-world behaviour, OR risk is high, OR the human
  asks
- **Question:** "Accept implementation for {feature}? {review_summary}"
- **Options:** accept, revise, reject

### exception_gate
- **Created when:** the build fails in a way the worker cannot attribute, OR a needed
  Minecraft API does not exist in the target version
- **Question:** "Exception for {feature}: {reason}. Accept implementation?"
- **Options:** accept (override), fix, reject

The second trigger is worth its own note: **"the API I need does not exist in this
version" is a legitimate finding, not worker failure.** Minecraft removes and reshapes
API across versions, and a worker that invents a workaround around a removed method
produces something that compiles and misbehaves. Briefings should say so explicitly, and
the orchestrator should route such a report to a gate rather than re-dispatching it.

---

## 📊 Task Status Flow

```
PENDING → READY → DISPATCHED → COMPLETE (with commit) → VERIFIED → REVIEW
                                                           │
                                                           ▼
                                        Auto-approve? ───→ COMPLETED
                                                or
                                        Human gate ─────→ WAITING_HUMAN
                                                             │
                          ┌──────────────────────────────────┼──────────────────────┐
                          ▼                                  ▼                      ▼
                      ACCEPTED                            REVISED                REJECTED
                     (complete)                    (orchestrator creates      (cancelled)
                                                    fix tasks → back to
                                                    Implementation)
```

Auto-approval requires all of: single `area:*`, CI green on Linux **and** Windows, and no
in-world behaviour touched.

### Dispatch protocol

1. Only dispatch tasks whose dependencies are complete.
2. Inject a preamble containing: task ID and spec, coordinator handle, the required commit
   format, the JDK-21 requirement, and the `worker_done` function.
3. Verify the worker is ready (`tui-idle`) before dispatch — and note that this is **not**
   proof that the inject arrived (see the delivery rule above).
4. Track the dispatch ID for each task.

### Exception handling

| Exception | Action |
|-----------|--------|
| Missing commit | Escalate to worker |
| Invalid commit format | Escalate to worker |
| Java change without regenerated `src/main/generated/` | Escalate to worker |
| Build failure the worker cannot attribute | Exception gate |
| Required API absent in target version | Exception gate |
| In-world behaviour changed | Human review gate |

---

## 📋 Human Commands Reference

| Command | Example | Action |
|---------|---------|--------|
| `status` | `status` | Show all active work |
| `pause all` | `pause all` | Pause dispatching immediately |
| `resume` | `resume` | Resume dispatching |
| `cancel <task>` | `cancel task_impl_washing` | Cancel a task |
| `retry <task>` | `retry task_impl_washing` | Retry a failed task |
| `require review <n>` | `require review 15` | Force human review |
| `skip review <n>` | `skip review 15` | Force auto-approval (logged) |
| `show gates` | `show gates` | List pending human decisions |
| `resolve gate <id> <choice>` | `resolve gate_123 approve` | Resolve a gate |

Board commands:

```bash
gh issue create --milestone <ms> --label <type> --label <area> --title <t> --body <b>
gh issue list --milestone <ms>
gh issue edit <n> --add-label blocker
gh issue comment <n> --body <text>
gh label create <name> --description <d> --color <hex>   # human-only
```

---

## 📅 Version History

| Date | Version | Changes |
|------|---------|---------|
| 2026-08-21 | 1.1 | The review gate names both test layers (#47). Auto-approval requires `runGametest` as well as `build`, and human review is justified by what the tests do not reach rather than by their absence — #40 built them. The Java-size figure in the context-budget section is an order of magnitude now, not a count. |
| 2026-08-20 | 1.0 | Adapted from the openkegelbillard orchestration policy. Kept the Orca CLI protocol and the dispatch/briefing failure modes (imported, flagged as such). Replaced the EARS/requirements phase with a conditional design phase; added the rules this repo needs: JDK-21 in every briefing, serial version migrations, Loom cache behaviour across worktrees, datagen output committed with its source, "compiles ≠ works" because there is no test suite, and triage of player bug reports. |

*Last updated: 2026-08-21*
