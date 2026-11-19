# Program: beat the champion

**Goal:** `src/challenger` wins at least 90% of games against `src/awubot` (the 2026 champion's final submission)
under official conditions (engine 1.2.6 rules, the official and tournament maps, both seats), without regressing
against the other top public bots (`src/opp_*`, `docs/opponents.md`). Progress: `docs/progress.md`.

## Tracks

| id | track | output | starts when | agents |
|---|---|---|---|---|
| S | setup: docs, sparring bots, tooling survey | `docs/`, `src/opp_*` | done | xhigh |
| C | Imperial compute: run any command, or a batch of games, on DoC's Condor pool, Slurm and cpucluster1; wait and wake for agents | `tools/compute`, `tools/run`, `.claude/skills/compute/` | done | 1, xhigh |
| E | bot evaluation: exact fast game runner, paired seats, sequential test, map pools with a sealed holdout, gauntlet, cache, local and Imperial backends | `tools/play`, `tools/games`, `docs/evaluation.md` | done | 1, xhigh |
| X | fast engine: profile the official engine, port it to native code, exact agreement with the Java engine | `engine/` | done (every game of the 8,388-game corpus agrees with the official engine; 7.8× less CPU a game on DoC machines) | lead + sub-agents, xhigh |
| XE | engine-speed eval (the `evaluation` skill): cost over a fixed game corpus, `INVALID` unless every output matches | `tools/enginebench/` | done (score: G instructions:u per corpus game, 19 visible + 19 sealed games; main 89.780; 8 validity checks, each proved by a variant) | 1, xhigh |
| XC | engine-speed climb | a branch | running | 1 climber, Opus 5.5 high |
| C2 | warm shared compute pool: pieces take tasks from any open run, submission reuses live capacity, fair across agents | `tools/compute`, `tools/run`, `tools/piece.py` | done (submission to first task: median 20 s → 3 s; sequential tests about half the wall time; one game per task) | 1, xhigh |
| EN | native backend for `tools/games`: games on `engine/build/bcx`, with a same-machine exactness sample on every run | `tools/bcgames/` | done (native by default; 549 same-machine checks, 0 differences; a `goal` harm test 1608 s → 584 s on Imperial) | 1, xhigh |
| TN | tuning: the bot's numeric constants tuned jointly against awubot by measurement (native engine, warm pool) | a branch, a tuner in `tools/` | running | 1, xhigh |
| T | theory: the dominant strategy from first principles, models of the top bots, gaps, work packages | `docs/theory/`, `docs/plan.md` | done (plan reviewed by a critic, fixes applied) | 5 lenses + synthesis + critic, xhigh |
| B | bot improvement: one work package an agent, judged on Imperial, win rates reported | branches merged into main | running: wave 1 (RB, S1, K1, E2 arm 1a, S2, KL, CW, CT, CB1c merged; E1 closed, its refactor merged); wave 3 started from docs/theory/w1-classes.md (MC centre-mine economy, PC1 last-cat rule with PT; AB done: three load-bearing features, no gain; E2b closed); wave 2 started (KB's cooperation king and KS's two fixes merged; PT running; CC+CK, CX closed: a king site reachable for a 3×3; E3 and K2 closed, neither merged); S3 merged (king liveness, torn chunks); S4 merged (miniRun bite, coordinator slot write) | many, xhigh |

## How work flows

- Every agent works in its own worktree on its own branch, commits small, pushes its branch to `origin` and names
  it in its report. Only the integrator (the orchestrating session) merges into `main`. Nobody opens a PR.
- A bot change is derived from the rules or a measurement, built in `src/challenger`, and judged by `tools/games`
  (docs/evaluation.md): challenger against awubot, paired seats, on the tuning pool. Games are deterministic, so a
  result that changed is the change's doing: read the flipped games, not just the totals.
- The integrator merges a judged branch, plays the merged main on the full pool, the sealed holdout and the gauntlet,
  and records the numbers in `docs/progress.md`.
