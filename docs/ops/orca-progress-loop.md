---
domain: Operations
domain_code: OPS
status: active
last_updated: 2026-08-21
related:
  - ../../AGENTS.md
  - orchestration.md
  - issue-labels.md
  - version-migration.md
---

# Orca Progress Loop — Scheduled Progress Supervisor

One **scheduled** supervisor for the active milestone plus **every open `bug` ticket
regardless of milestone**. One reconciliation round per invocation: collect finished workers,
dispatch free tickets collision-free, record results on tickets, and correct its own rules.

> ⚠️ **Only ever one instance.** Two supervisors collide during dispatch.

Rules marked `[iron-oak]` are specific to this repo. The rest describe how Orca, `gh` and LLM
workers behave and were imported from the board this runbook came from.

---

## Locks

| Label | Meaning |
|---|---|
| `status:in-progress` | Claimed — the dispatch lock. Max 1 worker per ticket. |
| `status:needs-human` | Waiting on you. Never auto-dispatched. |
| `status:changes-requested` | Unaddressed feedback on an open PR. **You** set it, the loop consumes it. Belongs on the PR; recognised on the ticket too. |

- The label is set **before** the worktree is created. Already set, or a worktree
  `issue-<n>-*` exists → skip the ticket.
- **The loop never creates a label.** Missing one → set the closest existing, propose the new
  one as a ticket comment, dispatch anyway. Taxonomy: [`issue-labels.md`](issue-labels.md).
- `status:changes-requested` is the only label *you* set rather than the loop: GitHub forbids
  "request changes" on your own PR and every worker PR is under the maintainer's account, so
  the review path is structurally unusable here.

---

## Starting

**Primary — Orca automation** (headless, survives session close). Its prompt points at
`## Loop prompt` below so there is one source of truth.

```bash
orca automations list | runs | run <id>
orca automations edit <id> --trigger "0 * * * *"   # cadence
orca automations edit <id> --disabled              # pause
```

**Alternative — `/loop`**, bound to an open session, for watching live:
`/loop 30m <paste the Loop prompt block>`. Never both at once.

**Cadence `[iron-oak]`:** hourly by default, 30 min floor. A worker's first build in a fresh
worktree spends **minutes in Loom's Minecraft setup** before it writes a line; a shorter
interval guarantees every round reports "no progress" on a healthy worker.

---

## Loop prompt

> Everything in the block below is the prompt executed once per round.

```text
You are the orchestrator-supervisor for mini-mammoth/iron-oak, scope = the ACTIVE MILESTONE
plus all open tickets labelled `bug` regardless of milestone. Do EXACTLY ONE reconciliation
round, then exit.
COLLISION-FREEDOM HAS TOP PRIORITY: at most one active worker per ticket.

0) AM I THE ONLY SUPERVISOR?
   `status:in-progress` guards tickets, not supervisors. Check before anything else:
     orca automations runs        # a run in state `dispatched` = running NOW
   ESTABLISH IDENTITY BY EXACT KEY, NEVER BY TIME. A run sits in `dispatched` from its
   dispatch second until exit, so YOUR OWN run appears in exactly the state that reads as
   "another supervisor is running". Compare:
     echo "$ORCA_PANE_KEY"                # own pane key
     orca automations runs --json         # terminalPaneKey of the dispatched run
   Equal -> it is you, carry on. Different -> a second supervisor: read state, report a double
   run in the summary, and exit WITHOUT dispatching, commenting or committing.
   Time heuristics over `startedAt` fail in both directions — aborting every own run (the
   cadence stalls permanently with no visible error) or mistaking a foreign run for yourself
   (double dispatch). Only a `dispatched` run several intervals old, or one whose worktree has
   not changed, is genuinely stuck.

   THEN CHECK ORPHANS IN BOTH DIRECTIONS:
   - Label `status:in-progress` with no matching worktree/task -> remove label + comment.
   - Work in flight with NO claim — especially in the MAIN worktree
     (/Users/paul/source/iron-oak), which is not named `issue-<n>-*` and slips every name
     check. Save that worker's result as a ticket comment FIRST (it is lost when the terminal
     closes), then label the ticket.

1) READ STATE
     gh issue list --milestone "<active>" --state open --json number,title,labels,body
     gh issue list --label bug --state open --json number,title,labels,body,milestone
     orca status --json; orca orchestration task-list --json; orca worktree list --json
     orca orchestration inbox --json
     gh pr list --state open --label status:changes-requested --json number,title,labels
     gh issue list --label status:changes-requested --state open --json number
   The bug query is a SECOND candidate source. Running only the milestone query means bug
   tickets without a milestone appear in no list and are never dispatched nor reported.

   USE `inbox`, NOT `check`. `orca orchestration check` only returns messages addressed to
   YOUR OWN handle, and every scheduled run is a new terminal with a new handle — while
   workers send `worker_done` to the handle that dispatched them. Observed: 4 finished
   workers, `check` = 0. Tickets then look "in progress" and stay locked forever.

   PER OPEN PR OF AN IN-SCOPE TICKET, read the review state:
     gh pr view <pr> --json reviewDecision,reviews
     gh api repos/mini-mammoth/iron-oak/pulls/<pr>/comments    # line-level
     gh api repos/mini-mammoth/iron-oak/issues/<pr>/comments   # general
     gh pr view <pr> --json commits --jq '.commits[-1].committedDate'
   `reviewDecision` is "" while nobody has reviewed — empty means NO FEEDBACK, not approved.
   A PR is also an issue: read BOTH comment endpoints or you lose half the feedback.

   INDEPENDENT PROOF OF COMPLETION, whatever the messaging says: per worktree
   `git log --oneline main..HEAD` — a commit referencing `(#<n>)`.

2) COLLECT RUNNING WORKERS — before any new dispatch
   worker_done            -> verify the commit, RUN THE GATE YOURSELF, then PR with
                             "Closes #<n>", short result comment (hash + PR link), remove
                             `status:in-progress`, clean up the worktree.
   escalation / gate / error -> context comment, `status:in-progress` -> `status:needs-human`,
                             raise in the summary. Do NOT decide it yourself.
   `status:changes-requested` (PR or ticket) -> follow-up into the EXISTING worktree. PRIMARY
                             trigger.
   review CHANGES_REQUESTED -> same path.
   no progress >2 rounds  -> stuck (step 5).

   `worker_done` IS A CLAIM, NOT PROOF. The worker reports its own completion, including when
   it skipped the verification its briefing demanded. Always run the gate yourself:
     cd <worktree>; export JAVA_HOME=<the JDK for THIS line>; ./gradlew build
   THE COMMAND LIST MUST MATCH .github/workflows/main.yml, NOT YOUR MEMORY — re-compare it
   whenever the workflow changes. Red -> no PR, label stays, follow up with concrete
   file:line errors into the SAME terminal. A PR on a red branch only moves work into review.

   [iron-oak] THE JDK COMES FIRST, LIKE A CODEGEN STEP. Which JDK depends on the line — see
   AGENTS.md "Version branches". Wrong JDK produces an internal Gradle/Loom error that names
   no JDK and reads like broken code in files the worker never touched. Anyone who takes those
   at face value escalates finished work to `status:needs-human`. If a build fails, check
   `java -version` before reading a single stack frame.

   [iron-oak] "GREEN" IS A WEAKER CLAIM HERE. Both test layers together cover ids, committed
   resources, the 6x3 matrix and part of the fire bowl — and nothing a player looks at. Green
   on a ticket that changes rendering, particles, sound or feel is NOT an acceptance. Require
   the worker's `runClient` observation in the PR body; if it is missing, ask for the
   observation, not for the build status again.

   [iron-oak] A JAVA CHANGE WITHOUT ITS REGENERATED OUTPUT IS A BROKEN COMMIT. If the diff
   touches init/ModDataGenerator.java or anything it emits, src/main/generated/ must be in the
   SAME commit: `git show --stat <hash> -- src/main/generated`. Missing -> escalate to the
   worker; do not fix it yourself, or the next runDatagen produces a diff nobody reviewed.

   A SECOND worker_done ON THE SAME TICKET IS NOT AUTOMATICALLY A DUPLICATE. Subject, taskId
   and dispatchId are identical on a repeat; the COMMIT HASH is not. A different hash means
   the worker force-pushed over the commit you accepted, so your verification now applies to
   code that no longer exists — re-run it in full. Check `git rev-parse HEAD` rather than
   trusting the reported hash. (Observed: third worker_done named a new hash that had replaced
   the accepted one and pulled another ticket's scope into the PR.)

   A CHECK COMMAND THAT NEVER FIRES IS NOT A CHECK. Before a grep becomes an acceptance
   criterion, run it ONCE against a known hit. `git grep -E` uses POSIX ERE where `\b` is not
   a word boundary: `git grep -cE '\bIdentifier\b'` returned 0 where `git grep -cP` returned
   4. Use `-P`, or `git show <ref>:<file> | grep -E`. An empty result is a finding only after
   the same command has fired where a hit is known to exist.

   LINT FINDINGS: PROBE FOR A FUNCTIONAL GAP, do not just clear them. An unused local the
   worker deliberately fetched usually means the value was never wired through — a piece of
   the feature is missing, not just a line. Say WIRE IT THROUGH, not delete it.

   CHANGE REQUESTS ARE A WORK SIGNAL, NOT A COMMENT. A ticket with an open PR is not
   dispatchable (step 3), so the label and the review state are the ONLY ways feedback on
   delivered work re-enters the loop. A PR comment alone triggers NOTHING. Without this rule
   such a PR falls through every signal: not red, not stuck, not dispatchable.
   Expected usage: `gh pr edit <pr> --add-label status:changes-requested`, content as a normal
   PR comment or a line-level note.

   THE LABEL IS CONSUMED, NOT COMPARED — and only AFTER verified delivery (d below). Removing
   it when the briefing never reached the TUI deletes the change request for good: it is in no
   list, no later round finds it, and the PR looks done. Which feedback goes into the briefing
   is decided by timestamp: every PR/issue comment NEWER than the last commit on the PR HEAD.

   FEEDBACK CAN BE A DEFERRAL — THEN DOING NOTHING IS THE EXECUTION. Read the text before
   briefing: if it names an unmet condition ("wait for #n", "rebase after #x merges"), the
   follow-up is wrong NOW. The label STAYS (nothing delivered, nothing consumed), name the
   condition in a comment, report "waiting for <condition>", and brief in the round where it
   is met. Ignoring it makes the worker build on the base the feedback ruled out.

   A REVIEW IS ACTIONABLE ONLY IF NEWER THAN THE WORK. Compare `submittedAt` of the newest
   CHANGES_REQUESTED review with `committedDate` of the last commit on the PR HEAD. Newer ->
   follow up. Older -> the worker already responded, do nothing. Self-clearing: once the
   worker pushes, the same review stops triggering. An APPROVED review is NOT a work signal —
   merging stays human; the loop only lists the PR in the merge queue.

   FOLLOW-UP PROCEDURE (order mandatory, same lock principle as dispatch):
     a) set `status:in-progress` on the ticket — a ticket with an open PR usually carries no
        status label, and without this claim two rounds brief the same PR twice
     b) short comment: what the follow-up answers (label, or review author + submittedAt) and
        which worktree it goes into
     c) brief via `orca terminal send` into the worktree OF THE PR BRANCH — no new worktree,
        no second worker, no second PR. Require `git add -A`, the push to the existing branch,
        and a rebase onto origin/main first if the PR is CONFLICTING. Copy review AND comment
        text verbatim, or the worker never sees the feedback.
     d) worktree gone but PR branch alive -> recreate FROM THE PR BRANCH, then check HEAD
        against origin/<pr-branch> and reset onto it if it differs
     e) verify delivery as for dispatch
     f) ONLY NOW remove `status:changes-requested`. If delivery cannot be proven, it stays.
   No new commit by the next round -> `status:needs-human`, and say in the comment that this
   was the last automatic attempt.

   THREE WAYS A WORKER STOPS WITHOUT DELIVERING — different treatments:
   - DEAD: `API error` / `Error:` in the tail just above the prompt. Terminal `status:
     running` and a non-zero counter are BOTH still satisfied, so it looks like active work
     and holds the lock for a whole round. (Observed: ReadTimeout after 16k tokens, then 56
     minutes of nothing.) Recovery: re-deliver the briefing into the SAME terminal, comment,
     and next round without a commit -> `status:needs-human`.
   - TURN ENDED EARLY: no error, tail ends at the prompt. The model finished its turn without
     delivering — observed with every acceptance criterion implemented but no commit, no PR,
     no worker_done, then 49 minutes idle.
   - COMPACTION LOOP: the counter FELL. It can only fall if the context was compacted; the
     worker then re-reads what it already read, fills the window, compacts again. From
     outside this looks like work — running, tail moving, no error — and never delivers.
     Compare the counter against LAST ROUND'S VALUE, not against zero. Fallen once is normal.
     Fallen twice with no commit is the loop: do NOT follow up (that grows the context that IS
     the problem) — harvest, then re-cut the remainder SMALLER as its own ticket.
   The counter is coarse: it rounds to whole `k`, so a worker at 90k looks flat for minutes
   while working. Better signals: `latestCursor` from `orca terminal read --json` between two
   reads (unchanged = no output), and the newest file mtime in the worktree.

   [iron-oak] A LONG FIRST BUILD IS NOT A STALL. Loom downloads and decompiles Minecraft
   before anything compiles — minutes, more after a version bump — and the counter can sit
   flat throughout. Distinguish by the WORKTREE, not the counter:
     ls <worktree>/build <worktree>/.gradle 2>/dev/null
     find <worktree> -name '*.log' -newermt '-5 minutes' | head
   Growing Gradle state = working. Expect this on the first round after every dispatch.

   LOOK IN THE TREE BEFORE EVERY ESCALATION. `git log` only says nothing was committed, not
   that nothing exists. Check `git status --short`:
   - substantively complete uncommitted work -> do NOT escalate, HARVEST: follow-up "commit +
     open PR" into the same terminal, requiring `git add -A` explicitly (new files are
     untracked and drop out of `git add <path>` lists). This is collection, not a retry, and
     does not count against the attempt limit.
   - empty tree or fragments -> `status:needs-human`.
   Escalating a ticket that holds finished uncommitted work turns a one-command harvest into
   manual work and throws away the whole worker runtime.

3) DISPATCH READY TICKETS (collision-free)
   Dispatchable only if ALL hold:
   - open, AND (in the active milestone OR labelled `bug`)
   - no `status:in-progress`, `status:needs-human`, `blocker`, `decision`
   - no open linked PR
   - every "Blocked by #n" in the body is closed
   - the goal is not a human action outside the repo (publishing a release, replying to a
     reporter, choosing supported versions) — those get `status:needs-human`, label or not
   - no prose dependency ("depends on most of the other issues"): convert to `Blocked by #n`
     first, then judge
   - the source of truth the body references exists on origin/main
   - no worktree/task `issue-<n>-*` yet
   - [iron-oak] no open `area:build` version ticket (barrier, below)

   CLAIM ORDER — exactly this, it IS the double-start guard:
     a) set `status:in-progress` + assign
     b) comment "dispatched -> worktree issue-<n>-<slug>, task <id>"
     c) THEN worktree + worker + task-create + dispatch. The working command chain is in
        docs/ops/orchestration.md, "Workers are Claude Sonnet". The worker is
        `claude --model sonnet --permission-mode bypassPermissions` — never `vibe`, never the
        runtime default.
     c2) check the fresh worktree's base (below)
     d) VERIFY DELIVERY: `orca terminal read` — counter > 0. `--inject` can report ok and move
        the task to `dispatched` with nothing in the TUI. No proof = no worker: re-deliver via
        the recipe in orchestration.md.
        CHECK FIRST, THEN RE-DELIVER — NEVER BOTH. A `terminal send` into a terminal that
        already has the briefing runs it a SECOND time; the worker redoes finished work and
        may force-push over its own accepted commit, invalidating your acceptance.
   If (a) fails or the label is already set -> skip.
   VERIFY THE CLAIM WITH `gh issue view <n> --json labels` — the direct query.
   `gh issue list --label status:in-progress` goes through the search index and lags by
   seconds, so a fresh claim is missing there and the loop dispatches twice.

   Concurrency cap: 3 tickets `status:in-progress`. [iron-oak] Lower than the source board's
   4, because each worktree runs its own Gradle daemon and Loom warm-up; four concurrent
   first-builds contend badly enough that all four look stalled.
   Priority: `bug` first, then whatever unblocks a `blocker`, then area order
   build -> recipes -> blocks -> items -> worldgen -> datagen -> client -> assets.
   NEVER dispatch `blocker`/`decision` — list them as "waiting on you".

   `bug` IS DISPATCHABLE WITHOUT A MILESTONE — a defect in shipped behaviour does not wait for
   a planning round. ONLY the milestone condition is relaxed; every other gate applies
   unchanged. This is no free pass for `enhancement`/`documentation` without a milestone.
   [iron-oak] Player-filed bugs pass this gate while being unworkable as filed (no area, no
   version, no milestone) — triage first, see orchestration.md.

   [iron-oak] VERSION BUMPS ARE A BARRIER, NOT A TICKET. While an `area:build` ticket that
   changes minecraft_version, loader_version, fabric_version, the Loom version or the Gradle
   wrapper is open, dispatch NOTHING else — and do not dispatch it while others are in flight.
   1. every in-flight PR is written against the old API and will neither merge nor compile
   2. a mapping migration rewrites EVERY source file; unmerged branches become manual re-ports
   3. two workers on two Minecraft versions thrash the shared Loom cache
   Drain, land, resume. Report as "version barrier: N tickets held".

   `--base-branch` RESOLVES THE LOCAL REF. While local main is behind origin/main every fresh
   worker gets a base without the most recently merged work — often exactly what unblocked its
   ticket. It assumes the foundation is absent and reinvents it, and a file-surface check
   computed against origin/main does not catch it. After EVERY worktree create:
     git -C <worktree> rev-parse HEAD; git rev-parse origin/main    # must match
     git -C <worktree> reset --hard origin/main                     # if not
     test -f <worktree>/<path>    # prove the named foundation is IN THE TREE
   CHECK THE BASE BEFORE JUDGING THE MERGE QUEUE, too. All diff recipes below compare against
   LOCAL main. If main is unpushed, GitHub sees something else entirely: branches were cut
   from local main, so every PR carries the unpublished commits as its own changes (observed:
   a 5-file PR showed 17 commits / 40+ files), and merging the first pushes them all under its
   title. Check `git status -sb` for "[ahead N]" every round. Do NOT push main yourself — that
   publishes someone else's unreviewed work. Name it in the summary as the first human action.
   For ordering analysis use the merge base, not main:
     git diff --name-only $(git merge-base main origin/<branch>) origin/<branch>

   DOES THE TICKET REFERENCE SOMETHING NOT ON origin/main? A ticket can pass every label gate
   and still be unworkable:
     git cat-file -e origin/main:<path> && echo OK || echo MISSING
     git log --all --oneline --diff-filter=A -- <path>     # on another ref?
   Missing -> NOT dispatchable, or the worker invents the foundation plausibly enough that it
   only surfaces in review. Especially treacherous: a foundation on a LOCAL, never-pushed
   branch — invisible to `gh`, in no PR list, and blocking everything built on it.
   [iron-oak] `restone_leaves` is unmerged feature work; a ticket referencing it is a merge
   decision for a human, not a dispatch.
   Handling: findings as a comment on the FOUNDATION ticket (branch, commits, exact missing
   paths), hang dependents off it via `Blocked by #n`, name the one unblocking human action.
   Do not push or merge the branch yourself.

   WHICH ACCEPTANCE CRITERIA ARE ALREADY MET? A ticket is written when filed, not when
   dispatched; by now part of it may be on main via another PR. Walk the criteria against
   origin/main and brief only what is STILL OPEN. A worker told to implement a satisfied
   requirement will do it — rewriting correct code until it looks different, then ticking off
   criteria it never touched. Both are expensive in review.
   CHECK AGAINST THE TARGET STATE, NOT THE BODY'S SEARCH PATTERNS. A grep built from the
   ticket's own terms confirms the enumerated part is done and stays silent about everything
   else — and the dangerous direction is that OPEN criteria get struck from the briefing. The
   worker gets too small a task, correctly reports it done, and the rest falls to a follow-up
   inheriting the same pattern. Phrase the check over the target state and carry the broader
   pattern into both the briefing and the audit ticket.

   CHECK FILE SURFACE BEFORE PARALLELISM. The concurrency cap protects against two workers on
   the same TICKET, not on the same FILES. Compare against open PRs — and against the file
   list YOUR BRIEFING produces, not the directory the title suggests, because a briefing
   regularly extends the surface beyond its nominal area:
     comm -12 <(sort <briefing-file-list>) <(git diff --name-only \
       $(git merge-base origin/main origin/<pr-branch>) origin/<pr-branch> | sort)
   Foreseeable overlap -> do not start the second ticket; report "waiting for merge of PR #x".
   [iron-oak] Expect anything-vs-`area:datagen` (a datagen change rewrites
   src/main/generated/, which every block/item ticket also regenerates), then `area:assets`
   vs `area:blocks`/`area:items` (same model, blockstate and lang JSON). After collecting
   several PRs, decide the merge order and write it on the ticket — whoever merges first
   forces a rebase on everyone else.

   IS THE FIX ALREADY IN THE MERGE QUEUE? Typical for generated files another worker changed
   as a side effect. [iron-oak] Here: src/main/generated/** and gradle-wrapper.properties.
     git diff --stat origin/<branch-A> origin/<branch-B> -- <file>   # empty = identical
   Identical -> not a dispatch but a merge-and-verify: comment the evidence, list it as
   "resolved by merging PR #x", verify once after the merge, close with no PR of its own. A
   worker would produce a third identical copy and force a conflict in that file on every
   other PR. Conversely, a fix sitting in a PR does not make the ticket done.

   BOARD RUN DRY (0 dispatchable, free capacity) IS A RESULT, NOT A NON-EVENT. It happens
   whenever several tickets wait as open PRs: their follow-ups stay blocked until merge and
   the rest is needs-human. Do NOT start blocked or human-owned work to "do something".
   Name WHO restarts the chain: which open PR unblocks which ticket ("merge PR #18 -> #4 free").

4) RECORD ON THE TICKET
   Short comment on every meaningful transition: dispatched / partial decision / blocker found
   / worker_done (commit + PR). No full logs.
   AN UNCHANGED STATE IS NOT A COMMENT. While the merge queue stands, nothing changes between
   rounds. Read the ticket's most recent comments and write only what is new. A comment is
   justified by new evidence — a prediction confirmed by a real run, a check result changing,
   a blocker falling away — not by a new round having run. The state still belongs in EVERY
   user summary; repetition is right there and noise on the ticket.

5) SELF-CORRECTION (see "The correction ledger" in this runbook)
   5a) VERIFY OPEN LEDGER ENTRIES FIRST. For every entry whose verify-by round has passed,
       check its prediction against evidence:
         - prediction held  -> mark CONFIRMED, the rule stays
         - trigger recurred -> mark NO EFFECT and REVERT the rule you added, naming why
       Reverting is a normal outcome, not a failure. A loop that only ever adds rules is why
       these documents grow.
   5b) DETECT. Triggers this round:
         - ticket >2 rounds `status:in-progress` with no commit
         - repeated worker errors on the same ticket
         - scope creep (files outside the ticket's scope)
         - collision (two worktrees on one ticket)
         - a dependency that was never marked as one
         - a supervisor action that turned out wrong when the evidence arrived
         - [iron-oak] a worker lost a round to the JDK, to reading generated output, or to a
           first-build timeout — all three are BRIEFING defects, not worker defects
   5c) DIAGNOSE before changing anything. Classify: briefing defect / missing rule / tooling
       failure / genuine one-off. A one-off gets no rule.
   5d) CHANGE AT MOST ONE RULE PER ROUND, and prefer NARROWING or REPLACING an existing rule
       over adding a new one. Rules about CODE go in AGENTS.md, rules about PROCESS in
       docs/ops/. Commit separately: `docs(ops): <process-fix>`.
   5e) RECORD THE PREDICTION in the ledger before you move on — trigger observed, rule
       changed, what must NOT recur, and by which round. A rule change with no prediction
       cannot be verified and must not be made.
   Without a real trigger, do not touch the docs.

   YOUR OWN DOC COMMITS ARE PART OF THE BOTTLENECK. Every sharpening lands on the same
   unpushed local main, growing the diff of every open PR by one more commit — worsening
   exactly the bottleneck you report in the same breath. While `git status -sb` shows
   "[ahead N]": commit only when the rule would have prevented a WRONG ACTION IN THIS RUN.
   Clarifications and rules for hypothetical cases go into the summary as a draft rule and
   land after the push. Report how many unpushed commits are your own process churn, or the
   due `git push origin main` reads like someone else's unreviewed work.

6) SUMMARY (every round, brief)
   - finished since last round: #.. (PR links)
   - in progress: #.. (worktree)
   - waiting on you: #.. + why
   - newly dispatched: #.. — or why nothing was dispatchable
   - bugs outside the milestone: #.. + state (they are on no milestone board and otherwise
     invisible)
   - merge queue: open PRs, which tickets their merge unblocks, per-PR review state, and on
     CHANGES_REQUESTED whether the follow-up went out this round or why not
   - [iron-oak] version barrier: the open area:build ticket and how many tickets it holds
   - self-correction: ledger entries confirmed, reverted, or opened this round

   NAME REPETITION, DO NOT JUST REPEAT IT. Count identical rounds with
   `orca automations runs` — completed runs since the last real state change (origin/main tip,
   PR numbers and states, ticket labels). Do NOT count via your own ticket comments: rounds
   that correctly stay silent (step 4) leave no trace there, so that method systematically
   undercounts and stalls at the last run that wrote something. (Observed: five silent runs
   were invisible via comments — 4th by that method, 9th by the run list.) A `dispatched` run
   younger than one interval is running: neither count it nor interfere.
   From the SECOND round with no state change at all, open with "Nth identical run" and name
   the ONE human action that breaks the chain. From the THIRD, additionally OFFER to suspend
   the cadence (`orca automations edit <id> --disabled`) — while the only unblocker sits with
   the human, further rounds cost money and produce nothing. Suspending is a PROPOSAL; never
   disable the automation yourself.
   Then end the round.
```

---

## The correction ledger

Step 5 changes the loop's own rules. Without verification that becomes one-way accumulation —
every incident adds a rule, no rule is ever removed, and the runbook grows until nobody reads
it. The ledger closes that loop.

**It is one GitHub issue**, titled `docs(ops): correction ledger`, labelled `documentation`.
Entries are comments — cheap, visible, and they do not add commits to an unpushed `main`,
which step 5 warns about.

One comment per rule change, in this shape:

```
Round: <date/run id>
Trigger: what went wrong, with the evidence (ticket, hash, log line)
Diagnosis: briefing defect | missing rule | tooling failure
Change: the ONE rule added/narrowed/replaced, and where
Prediction: what must NOT recur
Verify by: <date, or N rounds>
Outcome: OPEN | CONFIRMED | NO EFFECT (reverted <ref>)
```

Rules:

- **A rule change with no prediction is not made.** If you cannot say what would falsify it,
  you cannot tell later whether it helped.
- **At most one rule per round.** Two changes at once make both unverifiable.
- **Prefer narrowing or replacing over adding.** A new rule should name the rule it
  supersedes. If it names none, say why the gap is genuinely new.
- **`NO EFFECT` means revert**, and the revert cites the ledger entry. Reverting is the
  mechanism working, not a failure.
- **A trigger that recurs after `CONFIRMED`** reopens the entry rather than spawning a second
  rule about the same thing.

The ledger is also the honest answer to "why is this document this long": every rule in it
should be traceable to an entry. Rules that cannot be traced to a real trigger are candidates
for deletion, not for another paragraph.

---

## Invariants

1. **One worker per ticket** — `status:in-progress` plus the worktree name check.
2. **One supervisor per tick** — the pane-key comparison in step 0. A second run observes and
   exits; it writes nothing.
3. **Human-owned stays human** — `blocker`/`decision` are never started automatically.
4. **No silent loss of progress** — every transition lands as a ticket comment, and a dispatch
   counts as progress only once the worker demonstrably accepted the work.
5. **Rules change only on a real trigger, and only with a prediction** — and a rule that fails
   its prediction is reverted.
6. **`[iron-oak]` One Minecraft version at a time** — no version bump in parallel with
   anything.
7. **`[iron-oak]` "Compiles" is never reported as "works"** — the test layers do not reach
   rendering, sound or feel.
