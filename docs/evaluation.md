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
| the report so far; every game; one game's record and timeline | `tools/games show ID`, `--games`, `--game GAME` |
| is my branch worse than main? (certain-sign fixes, class pools a change must not hurt) | `tools/games ab challenger awubot --test harm --pool POOL` |
| A against B alone, no reference (the integrator's progress rows) | `tools/games ab challenger awubot --ref none --pool goal` |
| crash check (8 maps, one seed) | `tools/games ab challenger awubot --pool smoke --fg` |
| harm test against every sparring bot, paired with main's games | `tools/games gauntlet` |
| games whose result changed between two runs | `tools/games diff OLD_ID NEW_ID` |
| replay files for a run's cells, after the fact | `tools/games replays ID [--cells decisive\|lost\|changed\|all] [--remote]` |
| the replay of one game | `tools/games replay ID GAME` |
| runs, stop one, pools, SPRT sample sizes | `tools/games list`, `stop ID`, `pools [-v]`, `simulate` |
| build the native binaries a test will need, ahead of it (default: your challenger and main's) | `tools/games build [BOT...] [--remote]` |
| play any games from a spec file | `tools/play SPECS.jsonl OUTDIR [--workers N] [--engine 1.2.6\|1.2.5] [--replays none\|losses\|all] [--backend native\|java]` |
| any of these on the official Java engine only | add `--backend java` |
| any of `ab`, `gauntlet`, `replays` on DoC's machines (compute skill) | add `--remote [--where condor,slurm]` |

In Claude Code: run `tools/games ab ...`, then `tools/games wait ID` with the Bash tool's `run_in_background: true`.
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
games in flight, 256), `--replays losses|all`, `--fg` (foreground, prints the report), `--remote`, `--backend
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
- **Gauntlet**: a harm test of A against each sparring bot, paired with the ref's games (default main's challenger;
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

## Local and remote

- **Local** (default): `--workers` games at once (native processes, or JVMs each playing many games). All runs on
  this host share `BCGAMES_SLOTS` (8) games at once, so parallel runs from several agents queue instead of
  overloading the box.
- **`--remote`** plays on DoC's machines through track C's `tools/run` (`.claude/skills/compute/`).
  - Read the compute skill first. Condor needs Bitwarden unlocked; cpucluster1 refuses jobs at times.
  - `--workers` becomes the games in flight (default 256). Each test sends at most one pass (or 32 cells) beyond its
    scored prefix, its next decision point. A batch goes out as soon as games wait, at least 10 s after the test's
    previous batch (`imperial.FLUSH_S`); each task plays one game with `tools/play` (`imperial.CHUNK`), native or
    Java. Results are read as `tools/compute wait` fetches them (every 30 s a batch).
  - The nodes play the jars built here (shipped in `.bcgames-ship/`) on Temurin 21.0.13. It prints the same JDK
    fingerprint as Ubuntu's 21.0.13 here, so remote and local games share one cache. Verified: remote games
    byte-identical to local ones (replay digests, Slurm and Condor).
  - A test reads each task's results as they come back, and cancels the rest of the run when it stops.
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
  host, 8 compile jobs each. A new challenger build takes about 95 s; a change to `engine/` or the JDK rebuilds
  everything once per build slot (about 5 min). `tools/games build` builds them ahead of a test: after a merge,
  `tools/games build challenger@origin/main` spares the next tests main's build.
- **Exactness guard.** Each run also plays some of its native games on Java, on the same machine: every binary's
  first game in the run, and 1 in 32 of the rest, chosen by game key. Locally the Java twin runs beside the run;
  remotely a separate task plays the game on both engines on one machine. Neither holds up a cell, and the run does
  not conclude (or print a verdict) until every check has come back. When the replays differ, Java plays it
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
  weighted by their losses". Seed base 0 on a loss pool is only for reading the listed games.
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
