# Evaluation: is bot A stronger than bot B?

`tools/games` answers with real games: on the native engine (`engine/`), which plays exactly the official engine's
games at a fraction of the CPU, and on the unmodified official engine where the native one can't ("Engines"). Each
cell (a map and a seed) is played in both seats, and a sequential test (GSPRT) stops as soon as the answer is clear.
Results are cached by content, so unchanged code never replays a game. `tools/play` plays any list of games. Engine
and runner details: `docs/testing.md` §7a. Code and its invariants: `tools/bcgames/AGENTS.md`.

## Commands

| what | command |
|---|---|
| **is my branch better than main?** Prints the run's ID at once | `tools/games ab challenger awubot --pool POOL` |
| wait for it: **run it in the background**; it exits when the test stops | `tools/games wait ID` |
| several tests: wake at each one's end (its report and exit code, and the IDs still running) | `tools/games wait --any ID ID...` |
| the report so far; every game; one game's record and timeline | `tools/games show ID`, `--games`, `--game GAME` |
| is my branch worse than main? (certain-sign fixes, class pools a change must not hurt) | `tools/games ab challenger awubot --test harm --pool POOL` |
| A against B alone, no reference (the integrator's progress rows) | `tools/games ab challenger awubot --ref none --pool goal` |
| crash check (8 maps, one seed) | `tools/games ab challenger awubot --pool smoke --fg` |
| harm test against every sparring bot, paired with main's games | `tools/games gauntlet` |
| games whose result changed between two runs | `tools/games diff OLD_ID NEW_ID` |
| replay files for a run's cells, after the fact | `tools/games replays ID [--cells decisive\|lost\|changed\|all] [--remote]` |
| keep a run's replays past the sweep (cite them in a doc: keep them) | `tools/games keep ID [--off]` |
| free disk now (it runs hourly on its own); what it would free | `tools/games sweep [--dry-run] [--list N]` |
| the replay of one game | `tools/games replay ID GAME` |
| runs, stop one, pools, SPRT sample sizes | `tools/games list`, `stop ID`, `pools [-v]`, `simulate` |
| build the native binaries a test will need, ahead of it (default: your challenger and main's) | `tools/games build [BOT...] [--remote]` |
| play any games from a spec file | `tools/play SPECS.jsonl OUTDIR [--workers N] [--engine 1.2.6\|1.2.5] [--replays none\|losses\|all] [--backend native\|java]` |
| any of these on the official Java engine only | add `--backend java` |
| any of `ab`, `gauntlet`, `replays` on DoC's machines (compute skill) | add `--remote [--where condor,slurm]` |

In Claude Code: run `tools/games ab ...`, then `tools/games wait ID` with the Bash tool's `run_in_background: true`.
With several tests running, wait on all of them in one background `tools/games wait --any ID ID...`: it returns
when the first one stops (at once if one has), with that test's report and exit code and the IDs still running; read
it, act, then start `wait --any` again on those. `tools/games wait ID ID...` without `--any` returns when all have
stopped, a verdict line each (exit 0 only if every one passed).
You are woken when the test stops, and the exit code is the verdict:

| exit | gain test (default) | harm test (`--test harm`), gauntlet |
|---|---|---|
| 0 pass | H1: A is better (than the ref, or than 50% with `--ref none`) by δ | H0: no harm (gauntlet: against every opponent) |
| 3 fail | H0: A is not better | H1: A is worse by δ (gauntlet: against some opponent) |
| 4 | inconclusive: the cell cap was reached first | |
| 5 | still running (`show`, or `wait --timeout`) | |
| 1 / 2 | error / usage (1 also when the exactness guard stopped the run: "Engines") | |

Options of `ab` and `gauntlet`: `--pool` (below), `--ref`, `--test gain|harm`, `--delta` (default 0.10),
`--alpha`, `--beta` (0.05), `--min-cells` (default one pass over the pool), `--max-cells` (4/δ²: 400 at δ 0.10, 1600
at 0.05; smoke: one pass), `--seed-base` (below), `--engine` (1.2.6), `--workers` (local games at once, 8; remote:
games in flight, 256), `--replays losses|all` (kept a day after the run, while all replays stay under 15 GB, unless
`tools/games keep ID`: tools/bcgames/AGENTS.md "Disk"), `--fg` (foreground, prints the report), `--remote`, `--backend
native|java` (default native), `--guard RATE` (the guard's sample, default 1/32), `--no-cache` (play every game,
for measurements).

A bot is a package under `src/` (compiled alone, like a submission) or `pkg@REV`, the package at a git revision of
this repo. A revision is pinned to its commit when the test starts (`challenger@origin/main` fetches main first), and
the bots are compiled then: later edits don't reach a running test.

## The tests

- **Gain test against main** (default when A is `challenger`): A and the ref (`--ref`, default `challenger@origin/main`,
  pinned to main's commit) both play B on the same cells. The test is on the paired difference of their cell scores,
  d = A − ref in {−1, −0.5, 0, 0.5, 1}: pentanomial GSPRT on (d + 1)/2 with H0 0.5 (no difference) and H1
  0.5 + δ/2 (A better by δ in score). Any edge main already has over B cancels out, so a package that does nothing
  reads "no gain" whatever main's score.
- **Harm test** (`--test harm`): the same with H1 0.5 − δ/2. It passes on H0.
- **Plain** (`--ref none`; the default when A is not `challenger`): A against B alone, on the cell trinomial, H0
  score 0.5, H1 0.5 + δ (gain) or 0.5 − δ (harm). Use it for progress rows ("challenger@main vs awubot") and
  calibration, never to accept a package once main is above 50%.
- **Gauntlet**: a harm test of A against each sparring bot, paired with the ref's games (default main's challenger
  when A is `challenger`, awubot when A is `challenger@REV`: pass `--ref challenger@origin/main` for a commit;
  `--ref awubot` for the integrator's rows).
  - `opp_obg` and `opp_gst` (the main gate) play on `goal`, up to `--max-cells`.
  - The rest play two passes of `quick`: `opp_spaark`, `opp_cerr`, `opp_parmesan`, `opp_gravy`, `opp_goobie`,
    `opp_powerpuff`, `opp_food`, then `awubot_quals`, `awubot_us`, `awubot_mystery`, `awubot_sus`.
  - The ref's games come from the cache once played, so a gauntlet plays only A's games.

## Reading a report

```
run 1118-222544-ab-c167 [done]  calib_bc200 vs awubot, gain test, no ref  engine 1.2.6  pool goal  seed base 0

calib_bc200 vs awubot: FAIL (H0): calib_bc200 is not better
  LLR -5.80 (stop at -2.94 / +2.94; H0 score = 0.50, H1 score = 0.55)
  cells 285 (map+seed, both seats): won both 60, split 152, lost both 73; flip rate (2-0 + 0-2) 46.7%, net shift (2-0 - 0-2) -4.6%
  games 272-298  score 0.477 [0.439, 0.517]  Elo -16 [-43, +12]  LOS 13.0%  (95% CI, bootstrap over maps)
  points margin per game (ours - theirs, engine scoring): -1.5 [-5.3, +2.5]; in the 115 games that reached round 2000: +1.3
  win types: lost as A: KILL 113 PTS 37; lost as B: KILL 111 PTS 37; won as A: KILL 106 PTS 29; won as B: KILL 108 PTS 29
  mean rounds 866; robots killed by their own uncaught exception: calib_bc200 145, awubot 183
  per map (worst first): map cells won/split/lost score
    mushroom                       3   0/ 0/ 3  0.00
    ...
```

With a ref, the cells line becomes `cells N: challenger better B, worse W; diff counts -1..+1: [...]`, and a line gives
the ref's own score on the same cells and the paired difference with its CI.

- **Cells** are the unit: one map and seed, played with A in each seat. A cell is *won both*, *split* or *lost both*.
  With identical bots every cell splits. With a ref, a cell is *better* or *worse* when A's cell score differs from
  the ref's.
- **Score** = mean cell score = games won / games. Its CI resamples whole maps. **Elo** is the same number on the
  Elo scale. **LOS** is P(A stronger) from won-both vs lost-both cells.
- **Two answers on purpose.** The GSPRT verdict is about the pool's maps: cells are drawn round-robin over them, so a
  pass weighs every map once. The CI by map is about maps in general. When the effect differs between maps, the
  verdict can pass while the CI still crosses 0.5 (seen: a smaller rat cap won every small map and lost the economy
  maps). Read the per-map table, then confirm on `goal`.
- **LLR** is the GSPRT's evidence (fishtest's MLE form). It stops at the bounds, never before `--min-cells`. Only the
  complete prefix of cells (in submission order) is scored, so the verdict depends on the code and the cells, never
  on timing. A rerun of unchanged code reads the same verdict from the cache.
- **Flip rate** (plain mode) = cells won both or lost both. **Net shift** = (won both − lost both) / cells.
- **"no behaviour change"**: every game equals its mirror (plain) or the ref's game on that cell (with a ref): same
  winner seat, round and end state. The change never ran, or did nothing.
- **Points margin**: our points minus theirs as the engine scores round 2000, per game (docs/theory/t3). Much more
  sensitive than win/loss on maps that reach round 2000.
- **Robots killed by their own uncaught exception**: robots the engine destroyed because their code threw an uncaught
  Error or returned from `run()`. awubot's asserts do this.
- **engines** (last lines): how many games each engine played, the native failures replayed on Java, and the
  exactness guard's count of games played on both ("Engines").

## Protocol

1. **Inner loop**, one change at a time: `tools/games ab challenger awubot --pool CLASS` (gain test against main),
   then `--pool goal` at the package's δ, or a harm test there.
   - **Rebase onto origin/main before every test.** The test pins origin/main when it starts; a branch missing a
     merged package reads that package's effect as its own. Naming a commit (`challenger@REV`) as the candidate drops
     the default reference: pass `--ref challenger@origin/main` with it.
   - Pick the pool where the effect concentrates: a T4 class (`cat`, `starve`, `choke`, `faceoff`, `nocontact`,
     `overflow`, ...), `points`, `weak`, `stress`, or a list pool of loss-class cells (below).
   - Keep the seed base while you compare variants of one idea: their cells are the same, so `tools/games diff` lines
     them up game by game.
   - **Arms**: choose the arm on its class pool and its diagnostic, then run one confirmatory test on a fresh
     `--seed-base`. Each extra arm tested at α = 0.05 raises the chance of a false gain (1 − 0.95^k).
2. **Read the flips against the background.** `tools/games diff BEFORE AFTER` lists every game whose result changed.
   Games are deterministic, so each flip comes from the change. But a change that only moves the bytecode or RNG
   streams also flips many cells (W0.1, below). A single flipped cell means nothing on its own. Read flips clustered
   on a map or a class, and the package's diagnostic. Get replays with `tools/games replays ID --cells changed`, which
   plays those games again: test runs stay replay-free. About 1 replay in 50–100 is not the recorded game (its
   digest differs; `same_game: false` in `replays/index.jsonl`): leave those out of per-game reasoning. Two known
   causes (engine/AGENTS.md): HotSpot's `Math` stubs give CPU-dependent results, and the official engine itself
   diverged in 8 of ~18,500 runs on DoC machines.
3. **Before you report a branch**: rebase on main;
   - the gain test on `goal` passes (or, for a certain-sign fix, the harm test passes and the diagnostic moved);
   - `--pool smoke` shows no new robots killed by exceptions;
   - `tools/games gauntlet` reads no harm.
4. **The integrator** merges, then plays the merged main in plain mode (`--ref none`) on `goal` (the 90% metric), on
   `teammade` (robustness), on the sealed holdout (`BCGAMES_INTEGRATOR=1 tools/games ab challenger awubot --ref none
   --pool holdout`) and the gauntlet (`--ref awubot`). The numbers go in `docs/progress.md`. Holdout runs live under
   `SEALED-TEST-DO-NOT-READ/` and never print map names. Climbers never run or read them.
5. **Small gains: the precision route.** A sequential test at δ 0.05 misses most real gains of 1–3 pp, and 90% needs
   several of them stacked. A change whose effect is below δ can merge on two fixed-size paired tests against main
   on `goal`, each on its own fresh seed base:
   - `tools/games ab challenger awubot --pool goal --min-cells 3000 --max-cells 3000 --seed-base N --remote`
     (6,000 games; its 95% interval on the difference is about ±1 pp);
   - the first run (the screen) reads > 0; the second (the confirmation, a seed base no earlier run of the package
     used) has its 95% lower bound > 0;
   - then smoke and the gauntlet as in item 3. Report both run IDs and both intervals.
   A smaller screen (e.g. 1,000 cells) is fine for choosing among arms; only the confirmation decides.

## The book: per-map entries

The book (`src/challenger/book.jsonl`, tools/book/AGENTS.md) sets a switch or a datum per goal map and seat. Its
entries are chosen and confirmed map by map, then as a whole:

1. **Choose** on one fresh seed base: `tools/book/book matrix NAME --point LABEL:CONST=V ... --maps M1,M2
   --cells-per-map N --seed-base B` (in the background) plays every candidate on the maps' `map:` pool, main on the
   same cells, both seats. Then `tools/book/book fit NAME`: per map-seat the best candidate's raw effect, its
   selection-adjusted effect and p_max. The best of K candidates on one map-seat reads high by the null maximum of K
   (simulated nulls, `fit --simulate`: +0.033 for K 2, +0.057 for K 4, +0.075 for K 8 at 30 cells a map-seat); adj
   removes it (null mean within ±0.002), and p_max is the sign-flip test of that maximum. Propose an entry when
   p_max < 0.05 and adj > 0.
2. **Confirm** the one chosen value on the map alone (`--pool map:M`, both seats) on a second fresh seed base: the
   entry's seat must read a 95% lower bound above 0 (`tools/games ab challenger@REV awubot --ref challenger@origin/main
   --pool map:M --min-cells N --max-cells N --seed-base B2 --remote`). The entry's `evidence` holds both run IDs.
3. **The whole book** against main: `tools/book/book confirm --seed-base B3` starts the protocol's precision route
   (3,000 `goal` cells, item 5), the entries' map-seats on their own maps (seed base B3 + 1), and the goal maps
   without entries plus `teammade` (B3 + 2), where the book must change nothing systematic.
   `tools/book/book confirm-show FILE` reads all three. The book merges when, on these fresh seed bases, the entries'
   map-seats together read a 95% lower bound above 0 with no entry's own map-seat below 0 (the entries are those
   that differ from main's book: the ones carried over play as main and would only dilute the reading; an entry
   that reads below 0 on both the entries' run and the `goal` run is taken out before the merge, as houseofcards A
   was from book 4), the maps outside the book
   show no harm (their interval holds 0), and the `goal` run's interval is not below 0. The `goal` run cannot carry
   the verdict alone: a book of k map-seats moves `goal` by about k/190 of its own effect (5 map-seats at +0.23 are
   +0.6 pp), inside the ±1 pp of 3,000 cells; the entries' own maps see it directly (book 1: +0.231 [+0.108, +0.333],
   `1119-232956-ab-98ba`).
   Then smoke and the gauntlet as for any bot change, the book's commit against main:
   `tools/games ab challenger@REV awubot --pool smoke --remote` and `tools/games gauntlet challenger@REV --ref
   challenger@origin/main --remote` (with `challenger@REV` the gauntlet's default reference is awubot, not main).

**Measured (19 Nov, seed bases 25001-25004).**
- Empty book against main, 1,000 `goal` cells (`1119-214049-ab-32c9`): −0.002 [−0.009, +0.005]; 52 cells scored
  differently (24 better, 28 worse), 54 of 2,000 games changed winner. The book costs each robot 15-31 bytecodes on
  its first turn; on 1.84 M rat turns and 129,000 king turns synced with main's (PairBc, 95 cells with replays,
  `1119-214103-ab-b14d`) +0.07 and +0.05 a turn, mean 10,875.2 both.
- A test entry (EscapeTheNight seat B, `KING_HAND` 1) against the empty book on the same 1,000 cells
  (`1119-214054-ab-dab3`): all 10 EscapeTheNight seat-B games changed (+4 wins); off that map-seat 9 of 1,990 games
  changed winner (net −1), from the booked constant's extra instructions in every robot (tools/book/AGENTS.md
  "Settable constants"). On EscapeTheNight alone, 60 cells (`1119-214102-ab-f7a0`): seat B 60 of 60 games changed,
  +39 wins; seat A 9 of 60 changed, +0. Walls-and-dirt scan, 30 cells (`1119-223639-ab-9c7c`): seat B +22 wins of 30.
- The scan costs the round-1 king 1,913 bytecodes (17,016 against 15,103 on EscapeTheNight); a map-seat without
  entries pays 6. Main's round-1 king already overruns on warhammer and superioristhmus (~20,000), knifefight 16,679.

A map-seat holds about 1/190 of `goal`: a +20 pp entry is +0.1 pp of the goal score, so the book's value is the sum
of many entries, and each needs its own map's evidence; a single goal run cannot see one.

## Local and remote

- **Local** (default): `--workers` games at once (native processes, or JVMs each playing many games). All runs on
  this host share `BCGAMES_SLOTS` (8) games at once, so parallel runs from several agents queue instead of
  overloading the box.
- **`--remote`** plays on DoC's machines through track C's `tools/run` (`.claude/skills/compute/`).
  - Read the compute skill first. Condor needs Bitwarden unlocked; cpucluster1 refuses jobs at times.
  - `--workers` becomes the games in flight (default 256), except that the games of a test's first `--min-cells`
    cells, which it plays whatever happens, go out at once (a 190-cell paired test: 760). Each test sends at most
    one pass (or 32 cells) beyond its scored prefix, its next decision point. A batch goes out as soon as games wait, at least 10 s after the test's
    previous batch (`imperial.FLUSH_S`); each task plays one game with `tools/play` (`imperial.CHUNK`), native or
    Java. Results are read as `tools/compute wait` fetches them (every 30 s a batch), from the moment `tools/run`
    has made the batch's run: its placement of pieces goes on beside the follower.
  - A node with under 2 GB free where its games write takes no task, and a game that fails there for want of disk
    goes back to the pool unplayed (`tools/play --give-back`, compute internals "Room to write"), not to Java on
    that node.
  - The nodes play the jars built here (shipped in `.bcgames-ship/`) on Temurin 21.0.13. It prints the same JDK
    fingerprint as Ubuntu's 21.0.13 here, so remote and local games share one cache. Verified: remote games
    byte-identical to local ones (replay digests, Slurm and Condor).
  - A test reads each task's results as they come back, and cancels the rest of the run when it stops. A task that
    ended without a result (it failed on its node, or lost its machine at every start) fails its game as soon as its
    folder comes back, and the game is played once more; a test never waits for a follower's cancels of pieces that
    never started (a test's last 4 games waited 33 min for them on 19 Nov, `1119-180558-ab-2c02`).
  - A game runs 3-6x slower on a DoC CPU, but Condor offers ~2,500 games at once. Use it for `goal`, the gauntlet,
    calibrations and the holdout.

## Engines

- **Native by default.** A game plays on the native engine (`engine/`: the official engine and the bots translated to
  C++) when one binary can hold both its bots, else on the official jar. Two builds of one package can't share a
  binary, so a game of challenger against `challenger@REV` plays on Java. `--backend java` plays every game on Java.
  Each result names its engine (`backend`; native ones also `native_build`, the binary).
- **The same games.** The native engine plays the official engine's games exactly: result, every round's state,
  every robot's bytecodes, the replay bytes (engine/AGENTS.md: all 8,388 games of its corpus). A new bot build can
  reach engine code the corpus never did; such a path stops the native game with an error, never silently, and the
  game is played again on Java (the report counts these "native failures"; report them, engine/AGENTS.md).
- **Binaries.** A binary holds the engine, the sparring bots (awubot and the gauntlet's opponents) and the run's
  other bots. It is built on this host the first time any run needs it, keyed by the bots' class hashes and the
  engine's sources, and kept for every later run and agent (`~/battlecode26-data/games/native/bin/`, the 24 most
  recently used). A run plays its cached games and its games of existing binaries while a missing one builds; a
  second run asking for the same binary waits for the first's build. Builds run niced, at most 2 at once on the
  host, 8 compile jobs each, at `-O2` without ThinLTO (engine/AGENTS.md, "Build configurations"). A new challenger
  build takes about 70 s; a change to `engine/` or the JDK rebuilds
  everything once per build slot (about 5 min). `tools/games build` builds them ahead of a test: after a merge,
  `tools/games build challenger@origin/main` spares the next tests main's build.
- **Exactness guard.** Each run also plays some of its native games on Java, on the same machine: every binary's
  first game in the run, and 1 in 32 of the rest, chosen by game key. Locally the Java twin runs beside the run;
  remotely a separate task plays the game on both engines on one machine. Neither holds up a cell, and the run does
  not conclude (or print a verdict) until every check has come back. Remotely the checks go first: the sampled games
  of every cell a test will play whatever happens (its first `--min-cells`) are checked from its binary's first
  batch, each check twice on two machines (the first verdict counts; a mismatch from either stops the run), and a
  batch's checks come before its games. When the replays differ, Java plays it
  again. If its two runs differ and one equals the native game, the Java engine's own nondeterminism explains it
  (engine/AGENTS.md "Known gaps") and the native result stands. Otherwise the run stops with an error (exit 1), the
  binary is marked bad and never used again, and its results leave the cache. Report it, and rerun with
  `--backend java` meanwhile.
- **Cache.** A native and a Java result of the same game are interchangeable: the game key is unchanged, both are
  the official engine's game, and the guard checks it in every run. Cached Java results serve native runs and the
  reverse. Fields that differ by engine and are not outcomes: `cpu_s`, `wall_ms`, `hash` (native), `engine_errors`
  (Java), and `crashed` in a game of one package against itself (Java's scanner gives both teams' crashes to A; the
  native engine counts per robot).
- **Imperial.** `--remote` builds the binary here, puts it once on the volume (about 30 MB gzip'd, removed after
  3 days unused), and sends native games in batches of their own; the nodes run it as is (static).
- **What changes for an agent: nothing.** The commands, exit codes and reports are the same. Old runs read as
  before (they have no engine lines).

## Pools

`tools/games pools -v` lists them. Pools combine with commas (`--pool weak,stress`). `--pool map:a,b:7` names maps,
or cells as `map:seed`. `--pool file:PATH` reads a list.

| pool | maps | what |
|---|---|---|
| `goal` (alias `tune`) | 95 | **acceptance**: the 43 official + 52 visible tournament maps; pass 0 of seed base 0 = their own seeds |
| `official` | 43 | the public maps; pass 0 replays the official games exactly |
| `tournament` | 52 | the visible two thirds of the 78 tournament maps recovered from replays |
| `teammade` | 25 | the champion's team-made maps: robustness only, not part of the goal |
| `full` | 120 | goal + teammade |
| `quick` | 16 | small and fast: every size and symmetry, 4 champion-weak maps |
| `smoke` | 8 | smallest map, most mines, most cats, heaviest dirt, forced cooperation, maze, degenerate: crashes |
| `weak` | 21 | where the champion lost (intel.md §2, §3.1) |
| `stress` | 16 | ≥ 26 mines, most cats, heaviest dirt, degenerate geometry |
| `points` | 15 | DS§1.1's points-ending maps and T3§6's points-prone tournament maps (the one in the holdout is left out) |
| `cat`, `starve`, `choke`, `faceoff`, `nocontact`, `dirt`, `overflow`, `open` | 9, 35, 36, 8, 6, 22, 5, 39 | T4's map classes (docs/theory/t4-maps.md §3: CATPATH, MINEPOOR, CHOKE, FACEOFF, NOCONTACT, DIRT, ≥ 26 mines, OPEN), computed by `tools/bcgames/mapclass.py` over the visible maps, so a new map is classed the same way |
| list pools | any | `~/battlecode26-data/games/pools/NAME.txt`, one `map` or `map:seed` a line (`#` comments), used as `--pool NAME`: loss-class pools (W0.3). A sealed map in a list is an error |
| `holdout` | 26 | sealed: a stratified (size × symmetry) random third of the tournament maps, leaving out those intel.md names as losses or targets. Integrator only |

Every visible pool is built from the visible maps only. The theory lenses saw some sealed maps (critic §12), so
holdout results are slightly optimistic. Never tune a constant on those games.

**Seeds and cells.**
- With seed base 0 (the default), pass 0 plays each map's own seed: the official game. Later passes play seeds derived
  from (base, pass, entry). A `map:seed` entry plays its seed in pass 0 of seed base 0 only; any other base derives
  every seed, so a confirmatory test on a fresh base never replays the cells an arm was chosen on.
- **A pool of a main's own losses is biased at seed base 0.** Its listed cells were chosen because main lost them, so
  against that main, or any later main that still plays those games the same way, any change looks like a gain:
  the flips that W0.1 measured on every change regress to the mean (E2b: +0.16 on `L1-early` for arms that read
  ≤ 0 on fresh seeds). Run gain tests on loss pools with a fresh `--seed-base`; the pool then means "these maps,
  weighted by their losses". Seed base 0 on a loss pool is only for reading the listed games. Why (MC, `L1-econ`): main two merges after W1 played 98 of the 124 listed games identically to W1's to round 500 and gave W1's winner in 118; MC's reach check read +0.187 there, +0.004 on the same maps at seed base 7 and +0.037 on `goal`.
- Each pass visits every entry once, in a new fixed random order. An early stop has covered the pool once
  (`--min-cells` defaults to one pass).
- Seeds are applied by rewriting the map file's seed field into a cached copy, so the official engine stays
  unmodified. On 1.2.5 this plays exactly what the `src/battlecode` override plays with `-Pseed`.

## Conditions

- **Engine**: the official jar, unmodified, sha256-checked: 1.2.6 by default (the code of the final tournament's
  1.2.7.private2), or 1.2.5 with `--engine 1.2.5`. No `src/battlecode` override is on the classpath. The native
  engine plays the same games ("Engines").
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
- **The JDK** matters: bots' `java.util` classes are instrumented from the running JDK. The cache key holds a
  fingerprint of those classes (`JdkPrint`).
- **Map orientation**: the 31 final-tournament maps are the official repo's files. The other 47 recovered maps come
  from replays. A replay of an alternate-order game swaps the teams of the initial bodies, so some of those maps may
  have A and B exchanged. Both seats are always played, so this only relabels the seats.

## Tuning constants (`tools/tune`, track TN)

`tools/tune` (guide: `tools/bctune/AGENTS.md`) tunes the numbers in `src/challenger/Tune.java` jointly against
awubot. A variant is a commit that changes only Tune.java; it plays a fixed-size `tools/games ab` paired with main
on the same cells, and `fit` estimates every constant's effect from all variants at once.

**Why variants differ only in constants.** Tune.java holds compile-time constants, which javac copies into each use:
moving a number there changed no instruction of main (all 49 classes compared with `javap -c -p`, pool indices
removed), and a variant changes no instruction either. Its games equal main's up to the first decision a constant
changes. To check a refactor the same way:

```sh
for c in $(cd A && find . -name '*.class'); do cmp <(javap -c -p -constants A/$c | sed -E 's/#[0-9]+(, *[0-9]+)?//g;s/ +/ /g') \
  <(javap -c -p -constants B/$c | sed -E 's/#[0-9]+(, *[0-9]+)?//g;s/ +/ /g') >/dev/null || echo "DIFF $c"; done
```

**What a variant costs** (measured 19 Nov, clankerland at load 15-25, Imperial Condor):

| step | native backend | Java backend |
|---|---|---|
| build a variant | 63 s wall, 75 CPU s: the translation (49 s, single-threaded, one at a time on the host), one method file, the link; a code change to challenger takes 98-115 s, 305-363 CPU s (every challenger file recompiles) | javac alone, 3.5 s |
| a game on DoC | 15.7 CPU s (19,421 games of the first screen) | 74.5 CPU s (measured above) |
| a 570-game variant | ~9,000 CPU s | ~42,000 CPU s |

- Native wins: a minute of one build slot saves ~33,000 CPU s of Imperial a variant. `tools/tune play` builds two
  variants at once (`--builds`) and starts each test as soon as its own binary is ready. A variant's build is 45-49 s
  alone (59-64 CPU s), 38 s of it the translation of every package, one translation at a time on the host; two at
  once take 67-75 s each, so a variant's test starts every 37-42 s instead of every 53-65 s (8-variant campaigns, 19
  Nov 17:04-17:29, both slots warm: `tlb3` 331 s from the reference's start to the last variant's, `tlb1` with
  `--builds 1` 615 s; s1 and s5 took 32 min for 32). A slot whose last build was of another engine rebuilds
  everything first (279-337 s): after an engine change, worktrees still on the old engine make the two slots
  alternate, and every build is a full one (a variant took 281-840 s at 16:40-17:00).
- The other options, measured: one binary of many variants can't share a package (below). On Imperial, a lab
  machine has the same compiler (ash09, Ubuntu 26.04: clang 21.1.8, as here) but no ninja, 5.6 GB of memory free for
  a translation that peaks at 3.7 GB, and the compile commands name this host's paths (`-I`, the runtime's sources),
  so a remote build would be another binary, to be checked by the guard; not done. The gain left is in the
  translation: it translates the engine and all 15 packages again for a change to one constant of challenger.
- Several variants in one binary would not help: each must be its own package (two builds of one package can't share
  a binary), so each compiles all ~105 of its files (the 305-363 CPU s of a code change) instead of one, and the
  translation grows with it.
- The first screen (32 variants, 285 cells each): 48 min from the first build to the last result, 19,422 games,
  305,000 CPU s on Imperial. About 600 of those games were main's, replayed by runs that started before the cache
  held them.

**Noise** (the first screen: 32 variants, each 16 constants one step from main, 285 `goal` cells):
- No variant game equalled main's game on the same cell and seat: with 16 constants moved, every game diverges
  early. With 3 constants moved, 8% did; with one, 4-13% (below).
- Per-cell SD of the paired diff (variant − main): 0.38. A variant's cell score varies with SD 0.26 across variants
  (the chaos of W0.1); 25 of 285 cells scored the same for all 32 (22 of them won both games by every variant).
- The variants' mean scores spread with SD 0.026, against 0.016 from the chaos alone: the constants move the score.
- A single variant's diff on 285 cells has a 95% CI of about ±0.04; an effect fitted from all 32 has one of
  ±0.005 to ±0.010 for one step.

**Method: two-level screens, a step, a dose-response, then the ordinary protocol.** Builds are the scarce resource
and games are cheap and parallel, so the search uses few variants with many games each, and lets every game inform
every constant:
- A screen sets each constant to its value ± one step in a two-level resolution-IV design (2n variants for up to n
  constants; main effects are not mixed with two-constant interactions). Its effects are least-squares fits, with
  CIs by resampling maps. SPSA (fishtest's method) moves along a new random direction every iteration and needs
  hundreds of iterations, each two builds: about 7 hours of builds for 200 iterations, where a screen takes 32.
  One-at-a-time sweeps spend a build per point on one constant; a screen's 32 builds estimate 16 constants, each
  from all 18,000 games.
- Then the constants with |z| ≥ 2 move one step, and a second screen around the new centre, on fresh seeds, both
  checks them (a winner of 16 is biased upward) and asks whether to go further. Then a dose-response on the
  confirmed ones, and the chosen vector goes through the protocol above on a seed base the search never used.

**The first campaigns** (19 Nov, main `9bfd416` with MC; every campaign on its own seed base; effects in pp of
score per step, 95% CI by map; tables: docs/strategy.md §9, docs/closed.md):

| campaign | design | result |
|---|---|---|
| s1, seed base 61001 | 16 constants ± 1 step around main, 32 variants × 285 cells | three \|z\| ≥ 2: `EXPLORE_P` 0.2 ± 0.1 +1.43 [+0.59, +2.26] (+2.55 on ≥ 8-mine maps, flat on the rest), `TRAP_RAW` 20 ± 10 −1.23 [−1.92, −0.58], `COOP_ROUND` 100 ± 50 −0.86 [−1.58, −0.24]; the other 13 within ±0.8 (12 within ±0.5); curvature (variants' mean − main) −0.018 [−0.046, +0.007] |
| s2, seed base 61002 | the three moved one step (0.3, 10, 50) as the centre; 8 constants ± a step around it, 16 variants + the centre × 475 cells | `EXPLORE_P` replicated: +1.30 [+0.82, +1.83] at 0.3; `TRAP_RAW` (+0.06 per +5) and `COOP_ROUND` (+0.15 per +25) did not; the centre against main +0.005 [−0.022, +0.035], where s1's linear model said +0.035 |
| s3, seed base 61003 | `EXPLORE_P` 0.3, 0.4, 0.5, 0.6 from main, and 0.4 plus each other candidate, 7 points × 760 cells | against main: 0.3 +0.001, 0.4 +0.022 [+0.005, +0.041], 0.5 +0.016, 0.6 +0.036 [+0.015, +0.057]; added to 0.4 (paired with it): `COOP_ROUND` 50 +0.003 [−0.009, +0.020], `SPAWN_FLOOR` 50 −0.002 [−0.007, +0.003], `TRAP_RAW` 10 −0.005 [−0.017, +0.007] |
| s4, seed base 61004 | `EXPLORE_P` 0.5, 0.6, 0.7, 0.8, 1.0, 5 points × 760 cells | 0.5 +0.020, **0.6 +0.034 [+0.011, +0.056]**, 0.7 +0.024, 0.8 +0.026, 1.0 +0.018; parabola peak 0.70; past 0.6 the 5–7-mine maps fall off (1.0: −0.016 there), ≥ 8-mine maps hold (+0.039 to +0.044) |

- **Confirmation of `EXPLORE_P` 0.6** (branch `t3/tune-numeric-constants`, seed bases the search never used):
  - `goal` gain test, δ 0.05, seed base 61005: **FAIL (H0)**, +0.019 [−0.007, +0.044] (638 cells; 106 better,
    81 worse; `1119-130502-ab-3c03`). By richness: ≥ 8 mines +0.041 [+0.003, +0.081], 5–7 −0.011, ≤ 4 0.000.
  - `mines-ge8` gain test, δ 0.08 (plan §1 item 3's class-pool route), seed base 61006: FAIL (H0), +0.038
    [+0.004, +0.075] (252 cells, `1119-134907-ab-38e6`); `goal` harm test, δ 0.05: PASS, +0.037 [−0.027, +0.097]
    (`1119-134908-ab-f997`).
  - `teammade` (never used in tuning) gain test, δ 0.05: PASS, +0.041 [+0.003, +0.075] (303 cells,
    `1119-130505-ab-25a1`).
  - Gauntlet `1119-130506-gauntlet-fe4c`: OBG −0.003 [−0.050, +0.043], GST +0.025 [−0.011, +0.060], both no harm;
    the 2-pass `quick` opponents pass or end at their 32-cell cap (parmesan −0.062 [−0.141, 0.000], food and
    awubot_us −0.016, awubot_sus 0.000). Smoke: no robot of ours killed by its own exception.
  - Rebased onto main with KF (`c774154`), `goal` harm test, δ 0.05, seed base 61007: PASS, +0.026 [−0.013, +0.068]
    (192 cells, `1119-141554-ab-7d5f`). The branch differs from that main in one instruction operand
    (`MinerStrategy`: `ldc2_w 0.2` → `0.6`).
  - Every unbiased reading of 0.6 against main is positive (s4 +0.034, `goal` +0.019, `mines-ge8` +0.038,
    `teammade` +0.041, after KF +0.026): about +2.7 pp on `goal`, concentrated on rich maps, which a δ 0.05 test passes about half
    the time. s3's +0.036 was the best of four points, so it is left out.
- One constant moved: `EXPLORE_P` 0.2 → 0.6, measured +0.036 and +0.034 against main on two seed bases. Its
  mechanism shows in the diagnostic: our share of all cheese delivered rises by 1.0 pp at r500 and 1.2 pp at r1000
  (+1.5 pp at r500 on ≥ 8-mine maps; `fit --metric share500`), the unpicked cheese of docs/theory/w1-classes.md §3.2.
- Two of s1's three selected effects did not replicate: with 16 constants, |z| ≥ 2 picks about one flat constant by
  chance, and the chosen ones read high. Trust a constant once a second seed base agrees.
- Single-constant variants share 4-13% of their games with main (identical outcome), multi-constant ones 0-8%; the
  paired diff between two single-constant variants is far tighter than either against main (`SPAWN_FLOOR` 50 vs
  without: ±0.005 on 760 cells), because a rarely-binding constant leaves most games untouched.
- Cost of s1-s4: 61 variant builds (about 65 min of one build slot) and 68,626 games on Imperial (1.06 M CPU s), of
  which about 11,000 were main's games replayed by paired runs started together, or failed batches played again
  (`play` now plays main once, in its own run); each campaign ran 35-50 min from its first build to its last result. Transient SSH refusals on the login node failed
  some batches in s2; `tools/games retry` and a second `tools/tune play` (it plays again a variant short of its cells)
  filled them from the cache.

**TN2** (19 Nov, main with KF and EXPLORE_P 0.6; seed bases 61011-61013):
- **The mine-rich gate.** `MineEstimate.reached(Tune.RICH_MAP_MINES)` (8, latched) picks `EXPLORE_P_RICH` over
  `EXPLORE_P` for miners and `SPAWN_CAP_RICH` over `SPAWN_CAP` for kings. Its cost at neutral values (both rich
  constants equal to their plain ones), measured on the 760 cells of s6 (seed base 61012):
  - score against main −0.006 [−0.019, +0.007]; 43% of its games equal main's;
  - bytecode on synced turns (PairBc over the 1,520 replay pairs): rats +1.3 a turn (10,605 vs 10,604), kings +42
    (median +53: until it latches, the king asks the estimate every turn); overruns unchanged (kings 106 vs 109,
    rats 3,574 vs 3,552 in 7.0 M turns);
  - when it starts to act (FirstDiff, EXPLORE_P_RICH 1.0 against the centre): on ≥ 8-mine maps 720 of 816 games
    diverge, the first divergent round at p10 r54, p25 r79, median r127, p75 r216, p90 r354; 96 never latch. On the
    other maps no game diverges: the estimate is a lower bound and never reaches 8 there.
- **s5** (seed base 61011): 16 constants ± a step around main with the gate, 32 variants + the centre × 380 cells.
  Curvature −0.048 [−0.071, −0.025]: main sits near a local optimum in this box. Load-bearing as they are: the
  grab weight (lowering it to 5 costs about 2.3 pp) and the threat penalty (40 costs about 1.2 pp). The formation and
  lock-in constants re-screened after KF are flat (|effect| ≤ 0.26 pp a step), and so are the exploration radius and
  the rich-map cap. Leads at |z| ≥ 1.5: `EXPLORE_P_RICH` and `EXPLORE_P` up, `FIGHT_STEP` and `SPAWN_FLOOR` down,
  `MINES_KING2` up.
- **s6, s7** (seed bases 61012, 61013): the leads as single points against the gated centre, 760 cells each. None
  gained: the best read +0.005 (rich share 0.7, mines 7); both bundles ±0.01. Two of the screen's leads changed sign.
- **Result: no constant moved.** The gate was reverted; the branch keeps the newly tunable constants in Tune.java
  (main's instructions unchanged). Within the box these screens cover, main is at a local optimum except where TN
  already moved `EXPLORE_P`.
- Cost of s5-s7: 47 variant builds and 50,173 games on Imperial (647,000 CPU s), plus 4,560 replays for the gate's
  bytecode and latch measurements. Main moved
  between two `play` calls of s5 (docs-only commits) and the second re-pinned it, replaying 20 runs from the cache:
  `play` now keeps a campaign's first reference.

## Measured: time to verdict (track TL, 2026-11-19)

**What the tail was** (185 remote native tests of 19 Nov, 152,896 tasks; `~/battlecode26-data/scratch/tl/`:
`collect.py` joins each test's batches to their tasks, `measure.py` and `summary.py` read tests):
- A guard task ended last in 130 of 173 finished tests, a restarted task (start 2) in 99.
- Restarts: 3,191 tasks (2.1%) ran a second time, 427 s after their batch at the median: 61% of the slowest 1% of
  tasks. Their pieces were alive but stalled: a piece serving more than 4 other runs' contents deleted each checkout
  it had just made and made it again for every claim (281,305 checkouts for 339,682 task starts on 19 Nov; one piece
  399 in a row), so for minutes it neither beat nor reaped; other pieces took its claims as dead after 300 s. A
  start that found its checkout gone killed the piece (440 of 14,156 pieces). 96% of the game tasks over 120 s were
  slow outside their game (the game 19 s at the median, the task 132 s more): reaped late by a stalled piece.
- Guards: a guard task takes 78 s at the median (214 s p90), its Java game 2-3 times slower on the slowest DoC CPUs
  than on the fastest; a test's guards went out with their games, so the last batch's guards ended last.
- Placement: `tools/run --detach` took 16 s at the median (74 s p90, 314 s p99) placing pieces before it returned,
  and the next batch waited behind it; 31% of tasks had ended before their batch's follower started.
- Hosts that can't write: 111 native games failed for want of disk on 4 lab machines (betta 87) and played again on
  Java there, 78 s median, up to 358 s; betta also timed out 25 native games at 600 s. A lab machine's execute
  folder is on its root (8.4 GB free on ash09); each task unpacked its own 100 MB binary, each checkout ~350 MB.
- A binary upload whose prune `find` saw another upload's temporary file vanish failed, and sent every game of two
  tests to Java (424 games).

**The fixes**: the checkout prune keeps the new checkout (compute internals "Another run's checkout"); a piece takes
no task under 2 GB free and gives back a task whose game found no room ("Room to write"); a piece's tasks unpack a
binary once (`JOB_CACHE`); guards ahead, twice, first in their batch ("Engines"); a batch is followed as soon as its
run exists; the cancels come after the verdict; a test's sure games go out at once; the upload's prune can't fail it;
a follower reads every 30 s from its last read's start. Runs of these tools are served by their pieces alone
(`POOL_V` 2): pieces of version 1 still stall and crash on the runs they serve (seen at 16:48-16:59: a task run a
third time, 10 min late, in both arms).

**Before and after**: `tools/games ab challenger@origin/main awubot --ref none` (380 games) and `tools/games ab
challenger awubot` (760, paired with main) on `goal`, `--min-cells 190 --max-cells 190 --no-cache --remote`, the same
seed base for both arms, the two arms started together: main's tools from a worktree at main (the shared pool) and
these (their own pool, cold at each round's start). Engine of 19 Nov 17:00 (after XC), rounds 5 and 6:

| arm | tests | time to verdict, median / p90 | its last game task | tail share, median / p90 | restarted tasks |
|---|---|---|---|---|---|
| main's tools | 8 | 347 / 387 s | 142 s | 0.68 / 0.74 | 0 |
| these | 8 | 239 / 316 s | 192 s | 0.54 / 0.58 | 0 |

- Main's games ended at 93-127 s in round 6; its verdicts waited for guards until 299-387 s. These tools' guards
  ended at 162-168 s, and the verdicts came at 190-193 s for both sizes.
- Tail share = (verdict − the 95th percentile task end) / time to verdict. Here the last 5% of tasks are mostly the
  guards (18 of 398 tasks).
- Runs: main's `1119-170233-ab-ee0a`, `-170235-ab-927b`, `-170234-ab-23df`, `-170236-ab-ef2e`, `-171037-ab-76cb`,
  `-171040-ab-8d50`, `-171038-ab-0178`, `-171042-ab-e3b7`; these `1119-170233-ab-b0d0`, `-170235-ab-393b`,
  `-170234-ab-3642`, `-170236-ab-8a7d`, `-171036-ab-f537`, `-171039-ab-a380`, `-171038-ab-fdd1`, `-171041-ab-337a`.
  Earlier rounds (old engine; `~/battlecode26-data/scratch/tl/rounds.txt`): main's tools 414-527 s with 7-10 restarted
  tasks a test (round 1); in round 4, with one pool for both arms, every test of both took 753-789 s (version-1
  pieces).
- Game tasks run by fixed pieces against main's, same window (16:30-17:00): overhead (task − game) p99 4 s against
  67 s, none over 30 s against 3.2%. With every test's sure games out at once on a cold pool (rounds 5, 6), the first
  wave on cpucluster1 and GPU-cluster pieces paid up to 60 s before `tools/play` began (2.6% of tasks): a start-up
  cost, early in the test, not in its tail.

## Measured: the native engine (2026-11-19)

**One test on all four**: `tools/games ab challenger awubot --test harm --pool goal --no-cache` (challenger = main's,
so the test stops at H0 after one pass, 95 cells; every game played, none from the cache). clankerland at load
28-50 (other projects and the XE bench), all 8 local slots ours; Imperial: Condor.

| where, engine | wall | games | CPU a game | CPU: games + guard | run |
|---|---|---|---|---|---|
| clankerland, native | 1153 s (+83 s when the binary is new) | 380 | 11.2 s | 4256 + 552 s | `1119-052236-ab-8cca` |
| clankerland, Java | 4583 s | 387 | 45.2 s | 17473 s | `1119-054226-ab-4440` |
| Imperial, native | 584 s | 424 | 9.2 s | 3916 + 663 s | `1119-051034-ab-c173` |
| Imperial, Java | 1608 s | 681 | 74.5 s | 50741 s | `1119-051035-ab-6278` |
| Imperial, native, C2's dispatch (a game a task), guards in their own tasks | 745 s | 636 | 14.1 s | 8886 s + 27 guard tasks | `1119-090957-ab-7475` |

- A game takes 8.1x less CPU natively on DoC's machines, 4.0x less on clankerland at load 30-50 (the native engine's
  allocation-heavy world suffers more from a full box than the JVM).
- Remote runs play past the decision point (424 and 681 games for a 380-game decision): batches land at once.
  The first four ran before track C2's dispatch changes, with tasks of 8 native or 3 Java games (now one). In the
  last, 8 games fell back to Java: a node's disk was full (now unpacked on the volume instead).
- Every game two of these runs share is the same game (replay digest): local native against local Java 190 of 190
  game keys, remote native against remote Java 212 of 212, local native against remote native 190 of 190.

**Small tests**: the same test on each backend, wall time:

| test | clankerland, native | Imperial, native | Imperial, Java |
|---|---|---|---|
| `--pool smoke` harm, challenger = main (32 games) | 72 s (128 s when main's binary was new) | 491 s | 959 s |
| `--pool quick` harm, challenger behind main (H1 after 52-69 cells) | 405 s, 228 games | 1135 s, 216 games | 1505 s, 276 games |

- Below a few hundred games a local native test finishes first: Imperial's per-batch startup (1-4 min a batch,
  being reworked by track C2) dominates, and native games are short. On `goal` (380+ games) Imperial's parallelism
  wins at this load (584 s against 1153 s).
- Runs `1119-075646-ab-6b7c`, `1119-080013-ab-2c4a`, `1119-050004-ab-b371`, `1119-054917-ab-0c94`,
  `1119-080707-ab-58f6`, `1119-080708-ab-3640`. The local `quick` run and the remote Java one share 228 of 228
  games, all the same.

**Remote dispatch after track C2** (the shared pool), `ab challenger@71402d9 awubot --ref challenger@origin/main
--test harm --pool quick --no-cache --remote` (H1 after 52-77 cells), runs started together:
- Per-task overhead of a native task (task time minus game time): median 2.9 s against a median native game of
  9.9 s on DoC (`1119-090957-ab-7475`, 636 tasks); a piece's first task about 30 s (unpacking the binary).
- Games a native task: 1 game, 430 and 470 s wall, 7,774-7,956 task-seconds; 4 games, 870 and 872 s,
  5,780-6,054 task-seconds (`1119-095954-ab-97f1`, `-095955-ab-4ad5`, `-095956-ab-8433`, `-095957-ab-53ef`). One
  game a task: twice as fast for a quarter more slot time.
- The batch floor (`FLUSH_S`), batches sent as soon as games wait: a batch's oldest game waited 5-7 s, mostly the
  `tools/run` call (about 5 s); at 30 s the floor bound once in 21 refills (26 s), and 10 s and 30 s made the same 5-6
  batches a test (`1119-094802-ab-9547`, `-094803-ab-19e6`, `-094803-ab-0196`, `-094804-ab-ce62`). Refills are paced
  by results, which arrive with each batch's 30 s poll; that, and pieces lost mid-run (tasks restarted after about
  5 min, seen in 2 of 3 runs at 09:29), are the latency left.

**Builds** (`~/battlecode26-data/games/native/bin/*/build.json`), niced, 8 compile jobs:
- From nothing (14 sparring bots and challenger), or after any change to `engine/` or the JDK: 279-315 s, 1626-1707
  CPU s, once per build slot.
- A challenger change: 83-98 s (315-348 CPU s) at load 20-30: the translation (about 40 s, single-threaded), then all
  ~105 challenger files (their shared header changes with almost any edit), then the link.
- Under load 50, four builds started at once (four agents' challengers) on the two slots: ready after 272, 593, 700
  and 769 s, one of them the second slot's first build (691 s).
- A translation peaks at 3.7 GB with the serial collector (4.9 GB without), so translations run one at a time; the
  host's OOM killer took two concurrent ones on 19 Nov.
- `klasses.cpp` is compiled at -O1: at -O2 it alone took 177-238 s of each build.
- The host's JDK was upgraded by apt on 19 Nov (21.0.13.0.3 to 21.0.13.1): binaries are keyed by the JDK, so all were
  rebuilt; the game key's JDK fingerprint did not change.

**Exactness guard**: 1 in 32 by game key plus each binary's first game of the run guarded 1.7% of native games
(7 of 380 locally, 7 of 424 remotely), at 13% (local) and 17% (remote) more CPU than the native games alone. Every
guarded pair played the same game: 26 on clankerland and 523 on DoC (476 of them one remote run with `--guard 1`,
every game on both engines in the same task, `1119-054304-ab-de6d`); no Java game had to be played a second time.
False alarms: 0 of 549 pairs. A pair the Java engine's own nondeterminism splits (engine/AGENTS.md: 8 of ~18,500
reference runs on DoC) is played on Java a second time and explained, so a run stops falsely only if Java misbehaves
twice on one game.

## Measured (2026-11-18)

**Exactness**
- `bcplay.Play` replays are byte-identical to `battlecode.server.Main`'s.
- A game equals itself alone, in sequence in one JVM, under full load, under three JVM flag sets, and on DoC's
  Slurm and Condor nodes.
- Seed variants equal the 1.2.5 override's `-Pseed`.
- Details: docs/testing.md §7a.

**Throughput** (games a minute)

| where | what | games/min |
|---|---|---|
| clankerland, 8 workers, load 11-17 | 32 quick-pool games, official seeds | 13.1 (32 CPU-s a game) |
| clankerland, 8 workers, load 20-40 | quick-pool sequential tests | 11 |
| clankerland, `autoscrims/ABtest.py` (before) | quick, 12 workers / official, 8 workers | 9.8 / 4.7 |
| Imperial: Condor lab machines and a 36-CPU Slurm slot | first gauntlet, 3,109 games, 1,200 in flight | 106 |
| Imperial, same | W0: 3 runs of 570 games at once (one keeping replays) | 84 |

**Cells the GSPRT needs** (`tools/games simulate`, α = β = 0.05, min 16 cells; a cell is 2 games). Mean / p90 at the
H0 point, the midpoint and the H1 point. A change that moves bytecode splits ~53% of cells (W0.1), so read the
split-0.5 rows:

| δ | split 0.5 | split 0.7 |
|---|---|---|
| 0.20 | 22/37, 35/65, 21/30 | 19/26, 28/51, — |
| 0.10 | 67/131, 114/247, 67/130 | 46/87, 74/159, 40/74 |
| 0.05 | 261/545, 440/915, 279/521 | 174/325, 290/667, 165/336 |

Error rates measured in the same runs: P(H1) at the H0 point 0.02-0.07, at the H1 point 0.94-0.98.

**Calibration** (engine 1.2.6, plain mode against awubot)

| arm | pool, cells | won both / split / lost both | flip rate | net shift | score [95% CI by map] | verdict |
|---|---|---|---|---|---|---|
| challenger = awubot (W0.1 plain, = W0.3) | goal, 3 passes, 285 | 0 / 285 / 0 | 0% | 0 | 0.500 | H0, "no behaviour change" |
| RNG only: discarded spawn payload set to 0 (W0.1) | goal, 285 | 4 / 276 / 5 | 3.2% | −0.4% | 0.498 [0.488, 0.509] | H0 (δ 0.05) |
| dead loop, ~235 bytecodes a rat turn (W0.1) | goal, 285 | 60 / 152 / 73 | 46.7% | −4.6% (p = 0.30) | 0.477 [0.439, 0.517] | H0 (δ 0.05) |
| dead loop, ~150 bytecodes a turn, every robot | full, pass 0, 120 | 26 / 73 / 21 | 39.2% | +4.2% (p = 0.56) | 0.521 [0.467, 0.579] | inconclusive at the cap |
| worse: baby rats skip 1 turn in 8 | quick, 18 | 2 / 4 / 12 | | −55.6% | 0.222 [0.059, 0.405] | H0 after 18 cells |
| the same, reversed (awubot vs it) | quick, 24 | 17 / 5 / 2 | | +62.5% | 0.812 [0.650, 0.940] | H1 after 24 cells |
| rat cap −4 (spawn threshold 40→30, 30→20 on small maps) | quick, 74 | 30 / 30 / 14 | | +21.6% | 0.608 [0.465, 0.740] | H1: wins every small map, loses the economy maps |

- **The background is bytecode, not the RNG.** Moving only the king's random stream flips 3% of cells. Moving
  bytecode, with no logic change, flips 40-47%. Nearly every package changes bytecode, so about half of its cells
  flip whatever it does. A single flipped cell carries no information. Read class totals, clusters and the package's
  diagnostic (the plan's §1 item 5; its 20% threshold is exceeded).
- **No net bytecode tax was measured.** −4.6% and +4.2%, both within noise. The sign tests (p = 0.30, 0.56) cannot
  exclude a tax of a few points.
- **Robots killed by their own exception** move with bytecode too (145 vs 183 in the 235-bytecode arm), because
  awubot's crashes depend on timing.
- **Gauntlet baseline** (`tools/games gauntlet`, harm tests against main, 2,564 games, 1,320 played remotely in
  19 min). It passes against all 13 opponents: challenger equals main.
  - Main's challenger (= awubot) scores 0.521 against `opp_obg` (119 cells of `goal`) and 0.668 against `opp_gst`
    (173 cells).
  - Against the rest, two passes of `quick`: `opp_spaark` 0.77, `opp_cerr` 0.80, `opp_parmesan` 0.78,
    `opp_gravy` 0.82, `opp_goobie` 0.81, `opp_powerpuff` 0.83, `opp_food` 0.91, `awubot_quals` 0.86,
    `awubot_us` 0.75, `awubot_mystery` 0.88, `awubot_sus` 0.72.

**W0.3 data** (challenger@main = awubot, `goal`, 3 passes, 285 cells, 570 games, replays kept):
`~/battlecode26-data/games/runs/1118-222544-ab-6dea/`.
- `results.jsonl`: one line a game.
- `cells.tsv`: one line a cell: map, seed, winning and losing seat, ending, round, points, crashes, both replays.
- `replays/<game id>.bc26`: every game.
- Every cell splits. The seat that wins is A in 136 cells and B in 149. 213 cells end by king kill, 72 on points.
