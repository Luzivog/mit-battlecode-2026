# Evaluation: is bot A stronger than bot B?

`tools/games` answers with real games on the unmodified official engine. Each cell (a map and a seed) is played in
both seats, and a sequential test stops as soon as the answer is clear. Results are cached by content, so unchanged
code never replays a game. `tools/play` plays any list of games. Engine and runner details: `docs/testing.md`.
Code and its invariants: `tools/bcgames/AGENTS.md`.

## Commands

| what | command |
|---|---|
| start a test; prints its ID at once | `tools/games ab challenger awubot` (pool `quick`, δ 0.10) |
| wait for it; **run it in the background**, it exits when the test stops | `tools/games wait ID` |
| the report so far, or with every game | `tools/games show ID`, `tools/games show ID --games` |
| crash check (8 maps, one seed) | `tools/games ab challenger awubot --pool smoke --fg` |
| games whose result changed between two runs | `tools/games diff OLD_ID NEW_ID` |
| a replay file of one game | `tools/games replay ID GAME` |
| challenger against every sparring bot, beside awubot's own score | `tools/games gauntlet` |
| runs, stop one, pools, SPRT sample sizes | `tools/games list`, `stop ID`, `pools [-v]`, `simulate` |
| play any games from a spec file | `tools/play SPECS.jsonl OUTDIR [--workers N] [--engine 1.2.6\|1.2.5] [--replays none\|losses\|all]` |
| the same on DoC's machines (compute skill) | add `--remote [--where condor,slurm]` to `ab` or `gauntlet` |

In Claude Code: run `tools/games ab ...`, then `tools/games wait ID` with the Bash tool's `run_in_background: true`.
You are woken when the test stops, and the exit code is the verdict:

| exit | `ab` | `gauntlet` |
|---|---|---|
| 0 | H1 accepted: A is stronger (score ≥ 0.5 + δ more likely than ≤ 0.5) | no opponent reads worse than the reference |
| 3 | H0 accepted: A is not stronger | A is worse than the reference against some opponent |
| 4 | inconclusive: the cell cap was reached first | some opponent inconclusive |
| 5 | still running (`show`, or `wait --timeout`) | |
| 1 / 2 | error / usage | |

Options of `ab` and `gauntlet`: `--pool` (below), `--delta` (default 0.10), `--alpha`, `--beta` (0.05), `--min-cells`
(default one pass over the pool), `--max-cells` (4/δ²: 400 at δ 0.10, 1600 at 0.05; smoke: one pass), `--seed-base` (below), `--engine` (1.2.6),
`--workers` (8, the cap on this shared box), `--replays losses|all`, `--fg` (foreground, prints the report),
`--remote` (below).
A bot is a package under `src/` (compiled alone, like a submission) or `pkg@REV`, the package at a git revision,
e.g. `tools/games ab challenger challenger@main`. The bots are compiled when you start the test. Later edits don't
reach a running test.

## Reading a report

```
run 1118-212133-ab-1f0e [done]  ab calib_handicap vs awubot  engine 1.2.6  pool quick  seed base 0

calib_handicap vs awubot: H0 accepted: calib_handicap is not stronger (score <= 0.50)
  LLR -3.02 (stop at -2.94 / +2.94; H0 score <= 0.50, H1 score >= 0.60)
  cells 16 (map+seed, both seats): won both 0, split 11, lost both 5
  games 11-21  score 0.344 [0.219, 0.469]  Elo -112 [-221, -22]  LOS 1.3%  (95% CI, bootstrap over maps)
  points margin per game (ours - theirs, engine scoring): -12.3 [-25.0, -1.9]; in the 2 games that reached round 2000: -8.0
  win types: lost as A: KILL 9; lost as B: KILL 12; won as A: KILL 7; won as B: KILL 4
  mean rounds 412; robots killed by their own uncaught exception: calib_handicap 2, awubot 3
  per map (worst first): map cells won/split/lost score
    DefaultLarge                   1   0/ 0/ 1  0.00
    ...
```

- **Cells** are the unit: one map and seed, played with A in each seat. *Won both* and *lost both* carry the signal;
  a *split* (each seat won once) says the seat decided that cell. With identical bots every cell splits.
- **Score** = mean cell score (1, 0.5, 0) = games won / games. Its CI resamples whole maps, because the cells of one
  map are correlated. **Elo** is the same number on the Elo scale, and **LOS** is P(A stronger), from won-both vs
  lost-both cells.
- **LLR** is the sequential test's evidence, a GSPRT on the cell trinomial as fishtest computes it. It stops at the
  bounds, never before `--min-cells`. Only the complete prefix of cells (in submission order) is scored, so the
  verdict is a function of the code and the cells, never of timing: rerun unchanged code and you read the same
  verdict from the cache.
- **"no behaviour change: every game equals its mirror"** means A played exactly like B in every cell (same winner
  seat, round and end state in both seats). The change never ran, or did nothing.
- **Points margin** is our points minus theirs as the engine scores round 2000, averaged per game (docs/theory/t3).
  It is much more sensitive than win/loss for changes that act on points games.
- **Robots killed by their own uncaught exception**: robots the engine destroyed because their code threw an
  uncaught Error or returned from `run()`. awubot's asserts do this. Watch it after every change.
- **Per map**: where the cells were won and lost. Then `tools/games diff` shows which games flipped, and
  `tools/games replay` gives the replay of a game.

## Protocol

1. **Inner loop** (one change at a time): `tools/games ab challenger awubot` (quick, 16 maps, δ 0.10). A change aimed
   at a map class goes on its class pool first (`--pool cat`, `starve`, `choke`, `faceoff`, `nocontact`, `overflow`,
   ...), then on `full` to check for regressions. Keep `--seed-base 0` while you compare variants of one idea:
   their cells are the same, so `tools/games diff` lines them up game by game. Change the seed base now and then, so
   you don't tune on fixed cells.
2. **Read the flips, not just the total.** `tools/games diff BEFORE AFTER` lists every game whose result changed.
   Games are deterministic, so each flip is caused by your change. Flips scattered in both directions are chaos;
   flips in one direction, clustered on a map, are an effect. Replay them, fix, play again.
3. **Before you report a branch**:
   - `tools/games ab challenger awubot --pool full --delta 0.05` reads H1;
   - `--pool smoke` shows no new robots killed by exceptions;
   - `tools/games gauntlet` reads no opponent worse.
4. **The integrator** merges, then runs `full`, the sealed holdout (`BCGAMES_INTEGRATOR=1 tools/games ab challenger
   awubot --pool holdout`) and the gauntlet on the merged main, and writes the numbers in `docs/progress.md`. Holdout
   runs live under `SEALED-TEST-DO-NOT-READ/` and never print map names. Climbers never run or read them.

**Gauntlet.** `tools/games gauntlet [A] [--ref awubot]` plays A and the reference against each sparring bot on the
same cells. It reports A's score, the reference's score and their paired difference. Its test is non-inferiority:
H0 "A is worse than the reference by δ" against H1 "A is not worse".
- `opp_obg` and `opp_gst` (the main gate) play sequentially on `tune`, up to `--max-cells`.
- The rest play one pass of `quick`: `opp_spaark`, `opp_cerr`, `opp_parmesan`, `opp_gravy`, `opp_goobie`,
  `opp_powerpuff`, `opp_food`, then `awubot_quals`, `awubot_us`, `awubot_mystery`, `awubot_sus`.

The reference's games come from the cache after the first gauntlet, so only A's games are played.

## Local and remote

- **Local** (default): `--workers` JVMs, each playing many games. Every run on this host shares `BCGAMES_SLOTS` (8)
  games at once, so parallel runs from several agents queue for those 8 slots instead of overloading the box.
- **`--remote`** plays on DoC's machines through track C's `tools/run` (`.claude/skills/compute/`).
  - Read the compute skill first. Condor needs Bitwarden unlocked; cpucluster1 refuses jobs at times.
  - `--workers` becomes the games in flight (default 256), sent in batches every 30 s. Each task plays 3 games
    with `tools/play`.
  - The nodes play the jars built here (shipped in `.bcgames-ship/`) on Temurin 21.0.13. Ubuntu's 21.0.13 here
    prints the same JDK fingerprint, so remote and local games share one cache.
  - A test reads each task's results as they come back, and cancels the rest of the run when it stops.
  - A game runs 3-6x slower on a DoC CPU, but Condor offers ~2,500 at once. Use it for `full`, the gauntlet and the
    holdout.

## Pools

`tools/games pools -v` lists them. Pools combine with commas (`--pool weak,stress`); `--pool map:a,b` names maps.

| pool | maps | what |
|---|---|---|
| `smoke` | 8 | smallest map, most mines, most cats, heaviest dirt, forced cooperation, maze, degenerate: crashes |
| `quick` | 16 | the inner loop: every size and symmetry, 4 champion-weak maps |
| `official` | 43 | the public maps; pass 0 of seed base 0 replays the official games exactly |
| `tune` | 68 | official + the champion's team-made maps |
| `tournament` | 52 | the visible two thirds of the 78 tournament maps recovered from replays |
| `full` | 120 | tune + tournament |
| `weak` | 21 | where the champion lost (intel.md §2, §3.1) |
| `stress` | 16 | ≥ 26 mines, most cats, heaviest dirt, degenerate geometry |
| `cat`, `starve`, `choke`, `faceoff`, `nocontact`, `dirt`, `overflow`, `open` | 9, 35, 36, 8, 6, 22, 5, 39 | the map classes of docs/theory/t4-maps.md §3 (CATPATH, MINEPOOR, CHOKE, FACEOFF, NOCONTACT, DIRT, ≥ 26 mines, OPEN), computed by `tools/bcgames/mapclass.py` over the visible maps, so a new map is classed the same way |
| `holdout` | 26 | sealed: a stratified (size × symmetry) random third of the tournament maps, leaving out those intel.md names as losses or targets. Integrator only |

**Seeds.**
- With seed base 0 (the default), pass 0 plays each map's embedded seed: the official game. Passes 1, 2, ... play
  seeds derived from (base, pass, map).
- Each pass visits every map once, in a new fixed random order. An early stop has covered every map once
  (`--min-cells` defaults to one pass).
- Seeds are applied by rewriting the map file's seed field into a cached copy, so the official engine stays
  unmodified. On 1.2.5 this plays exactly what the `src/battlecode` override plays with `-Pseed`.

## Conditions

- **Engine**: the official jar, unmodified, sha256-checked: 1.2.6 by default (the code of the final tournament's
  1.2.7.private2), or 1.2.5 with `--engine 1.2.5`. No `src/battlecode` override is on the classpath.
- **One game equals a fresh JVM's game**, although each worker JVM plays many games. The engine's only state that
  outlives a game is the static cat RNG (`InternalRobot.rand = new Random(1092)`), which the runner resets before each
  game. Verified:
  - replays byte-identical to `battlecode.server.Main`;
  - 5 maps played in sequence in one JVM equal each played alone;
  - without the reset, sunandmoon played after t_minus_five_minutes ends at round 125 instead of 99.
- **What official games we reproduce exactly**: a single-map official run, or the first game of an official match.
  Within an official match the cat RNG carries over from game to game (one JVM, `-Pmaps=a,b,c`), and many games draw
  from it (`cat_rng_draws` in every result). So games 2+ of an official match can differ from the same game played
  alone. Nothing local reproduces that state, because it depends on the earlier games.
- **Bots** compile alone with Gradle's javac flags; the classes are byte-identical to `./gradlew classes`.
- **The JDK** matters: bots' `java.util` classes are instrumented from the running JDK. Its version is part of every
  cache key. All results here are from OpenJDK 21.0.13.
- **Map orientation**: the 31 final-tournament maps are the official repo's files. The other 47 recovered maps come
  from replays. A replay of an alternate-order game swaps the teams of the initial bodies, so some of those maps may
  have A and B exchanged. Both seats are always played, so this only relabels the seats.

## Measured (2026-11-18, clankerland, 8 workers, box shared with load 11-17)

MEASUREMENTS
