---
name: evaluation
description: Use when building, changing or reviewing an evaluation that agents hill-climb (one fast command that scores a candidate checkout on an outcome we care about), when asked to make something climbable, or when briefing or working as a climber.
---

# Evaluations

An evaluation (an eval) is one command that scores a candidate checkout, so an agent can climb it: change the code, run
the command, keep what improves the score. It exists so the climb never waits for a real run. `docs/program.md` says
which evals exist and who climbs them.

## Which eval

- **The one whose climb wins the most.** A fast, accurate, unhackable eval, given to an agent told to make its number
  better, gets better: choosing what to score is the decision.
- **Connected parts get one eval.** Score the whole decision when its parts connect.
- **Be creative.** Step back from the piece in front of you: what number, if it moved, would move the games?

## What it scores

- **One number, the real objective.** Diagnostics sit beside it, never in its place.
- **Outcomes, never behaviours.** Any route to a better outcome counts, deleting steps and redesigns included: those
  are what the climb is for.
- **Same outputs or no score**, where the eval holds a part's outputs fixed: record the reference's outputs once (byte
  for byte, or by an equivalence the eval states). A candidate whose outputs differ is `INVALID`.
- **Real conditions.** Real games, real bots, real maps, never an idealised case. Score over several scenarios, so one
  quirk can't carry the climb.
- **What we can't change but that shapes the score is priced in**, each price measured once, with its source written
  beside it, and pinned in the eval.

## Speed

- **Every command under a second; aim for milliseconds.** Print each command's own wall time. Over a second is a bug to
  fix before going on, unless the eval's AGENTS.md states the measured floor and why.
- **Never wait for real time.** A sleep, a poll or a remote job inside the score is the slow measurement back.
- **Where compute is the cost, count it with `perf stat -e instructions:u`** (exact and the same on every run; wall
  time on a shared machine is noise), or time a command with hyperfine. No counter of our own.
- **Cache the reference's score**, keyed on its commit and the eval's own code; recompute only when either changes.
- **Small paired samples**: the same seeds and inputs for reference and candidate, enough to separate a real gain from
  noise, no more.

## Running the candidate

- **Run the candidate's own code in place**, through its real entry points. An eval that re-implements the code under
  test scores itself, not the code.
- **What it can't run, it models, and says so.** A change to a modelled part prints `not simulated` and doesn't count.
- **An unknown call is `INVALID`**, never a guess.

## Not hackable

- **Pinned.** The eval refuses a checkout whose eval differs from main's, and code under test that names the eval.
- **A validity check for each false win.** Each `INVALID` line says why.
- **Sealed scenarios.** Other inputs of the same kind, under `SEALED-TEST-DO-NOT-READ/`, never shown to the climber.
  Only the branch a climber reports scores them, once.
- **Each check is proven**: break the behaviour it guards by hand, see `INVALID`, restore.

## The commands a climber needs

Think as the agent climbing: after `score`, what does it need to find the next gain? Every command under a second.
- `score`: the objective over the visible scenarios, the reference's beside it, each scenario's row, `score` or
  `INVALID` with each reason; `--json`.
- `explain`: where the score was lost, worst first.
- `scenarios`: what each holds.

## Validation

Before anyone climbs it, the designer scores at least: the reference (no-op), a change known to be better, a change
known to be worse, and one variant per validity check that must read `INVALID`. Each reading and its commit go in the
eval's AGENTS.md. A wrong eval is fixed in the eval. A climb never gates on a real job: the score is the measure.

## Its tests

Each test runs a small scenario through the real path and asserts what a user would see. A test that still passes
with its behaviour broken by hand is a tautology: rewrite it.

## Layout

- `tools/NAME/`: the eval's code, its tests (`*_test.py`), and an AGENTS.md that is the climber's guide: commands,
  how to climb, what runs and what is modelled, the rules, the scenarios, validation.
- Data (scenarios, their sources, caches) under `~/battlecode26-data/NAME/`; sealed scenarios in its
  `SEALED-TEST-DO-NOT-READ/`. A cache is deleted when the build it serves is replaced.
- Nothing the score doesn't need: no samplers, journals or history pulls "for later".

## The climb

The designer of an eval never climbs it, and a climber never designs or changes the eval it climbs.

A climber is a sub-agent started with `delegate_task`, in a worktree of its own, on the model and effort
`docs/program.md` gives it.
- It reads the eval's AGENTS.md whole with the Read tool, now and after each compaction.
- It makes one change at a time, scored before and after, and keeps it only if the score improves and is valid. It
  never edits the eval, reads the sealed folder or submits a real job.
- It logs each scored try as one JSON line in its worktree's `runs/climb.jsonl`:
  `{"t": EPOCH_SECONDS, "score": SCORE, "kept": true|false, "what": "the change"}`. Its score at a moment is its best
  kept score up to then.
- It commits each kept change on its worktree's branch, pushes the branch to origin and names it in its report. It
  never merges to main.
- When a climb ends, the eval is rethought before another climb on it. Does it score the real objective? Is the climb
  boxed in by what the eval fixes? Is another eval, a connected one or one of a wider scope, worth more?
