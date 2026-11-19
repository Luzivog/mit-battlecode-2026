# tools/enginebench: the native engine's speed eval

One command scores a checkout's native engine (`engine/`; read `engine/AGENTS.md` and `engine/DESIGN.md`). It
builds the engine, plays a fixed slice of the agreement corpus, and prints what one corpus game costs in user-space
instructions. Lower is better. A candidate whose games differ from the official engine's in any output is `INVALID`.

## Commands

| command | does | wall (clankerland, shared, load 25-50) |
|---|---|---|
| `tools/enginebench/enginebench score` | build, play the 19 visible scenarios (4 at once, `-j N`), check them, print the score and main's | 2.5-4 min |
| `tools/enginebench/enginebench score --only ID[,ID]` | build, play only those scenarios: their rows and validity, no score | build + that game |
| `tools/enginebench/enginebench explain` | the last score's scenarios, worst first, each `INVALID` reason in full | 0.2 s |
| `tools/enginebench/enginebench explain --profile ID` | one scenario under `perf record`: its instructions by function (uses the last build) | that game (5.6 s on a 465-round 20×60 map) |
| `tools/enginebench/enginebench scenarios [--json]` | what each visible scenario holds | 0.1 s |
| `tools/enginebench/enginebench score --main` | origin/main's engine, exported to the data folder, built there, scored, cached | once per main engine |
| `ENGINEBENCH_INTEGRATOR=1 tools/enginebench/enginebench score --sealed [--main]` | the sealed scenarios, integrator only | |

`score` exits 0 when valid, 1 when `INVALID`, 2 when refused (no score; the reason is printed). `--json` prints one
JSON object. Tests: `.venv/bin/pytest tools/enginebench/bench_test.py` (seconds).

**Measured floor.** `score` cannot run in under a second. Each scenario is a whole game, the longest one costs 30
CPU-s, and the 19 games together cost 300 CPU-s. Each `score` also rebuilds the engine from the checkout: the
translation alone takes 45-85 s, even with no change. A slice of a game is not possible: the engine cannot fork or
snapshot a game for another build to resume.

## The score

**score = Σ wᵢ · instructions:uᵢ / 10⁹** over the 17 weighted scenarios: the estimated user-space instructions of
one corpus game, in G. Main: **11.5425** (sealed 13.4128) on openjdk 21.0.13.1. The scenario table and the
validation readings below are from main before XC (89.780).

- **Scenarios.** Each is one real game of the agreement corpus (`engine/test/corpus.py`): awubot against itself,
  its four snapshots and the nine `opp_*` bots, either seat, at the map's seed or the corpus's extra seed. It is
  played whole by `bcx play`, exactly as the corpus plays it (`--no-indicators`, replay written).
- **Population.** The games were drawn at random from the 7,819 non-fuzz corpus games of the full DoC agreement
  run (engine f14753a). That run is the 8,100 non-fuzz corpus games, less 276 it re-ran elsewhere and 5 that
  diverged on DoC machines.
- **Strata.** Map size (tiles: S < 1200 ≤ M < 2400 ≤ L < 3600 ≤ XL) × game length (rounds < 800 or ≥ 800).
  Games were drawn in proportion to each stratum's share of the corpus's native CPU time: at least one per
  stratum, and at most one per map within a stratum.
- **Weights.** wᵢ = the stratum's share of the corpus games / the games drawn in it, so the sum estimates the
  corpus mean. The 60×60 maps hold 42% of the corpus's native CPU time.
- **Fuzz scenarios.** Two fuzz-bot games carry no weight. They are checked for exactness and run with
  `BCX_CHECK_FASTPATH=1`.
- **What is counted.** `perf stat` counts `instructions:u` over the whole sandboxed `bcx play` process tree,
  including the game's fork.
  - Load does not change the count. Address randomization does (by 1.6·10⁻⁴ run to run, through the
    conservative collector), so the eval turns it off inside the sandbox.
  - **Spread** between identical runs: 2.7·10⁻⁵ G on the score (3·10⁻⁷), at most 1.4·10⁻⁶ of one scenario's count.
    A difference under 0.001 G is noise; anything above is the change's doing.
- **Printed beside the score, not scored.**
  - Kernel instructions: they vary ±8% run to run. The memory cap keeps a candidate from moving work into page
    faults.
  - The tree's CPU seconds (perf's task-clock).
- **What runs.** The checkout's own `engine/build.py --engine 1.2.6 --bots BOTS` (the 18 frozen packages of the
  corpus, pinned in `bench.py`), then the `engine/build/bcx` it makes. Nothing is modelled except the corpus, by
  the weights.
- **Not scored:** engine 1.2.5, indicators on, `--dump`, robot stdout, wall time, build time.

## How to climb

0. Once per worktree: `./gradlew classes` (the bots' class files the build reads). The first `score` builds
   everything (about 34 CPU-min; 10-20 min here).
1. Run `score` on the unchanged checkout. That is your starting score: it equals main's.
2. Make one change in `engine/` at a time, then `score`. Keep the change only if the score is valid and lower;
   otherwise revert it.
3. Log every scored try as one line of `runs/climb.jsonl` in your worktree:
   `{"t": EPOCH_SECONDS, "score": SCORE, "kept": true|false, "what": "the change"}`.
4. Commit each kept change on your branch and push the branch. Never merge, never open a PR.

**Where the cost is.** `explain` lists the scenarios by weighted cost; `explain --profile ID` gives one game's cost by
function. On a 465-round 20×60 game, the top of the profile is:

| function | share |
|---|---|
| the collector's mark (`GC_mark_from`) | 13.5% |
| `Direction.fromDelta` | 10.1% |
| `GameWorld.bfsFromTarget` (the cats' BFS tables, quadratic in tiles) | 9.5% |
| allocation (`GC_allochblk_nth`, `GC_malloc`, `rt_new`) | 14% |
| `Direction.values()` clones (`rt_array_clone`) | 2.7% |

`engine/AGENTS.md` "Speed" explains these. The per-round state hash (`bcx.Match`, `BcxStateHash`) is part of every
game's output; one extra hash a round costs +5.5%, and its Random-state solver about 2%. The bots' own code is a
small share.

**What a loop costs.** Measured here at 8 build jobs, with the games at 4 at once; with other agents' load, the
games take 85-150 s:

| change | build | then games |
|---|---|---|
| none, or one runtime `.cpp` (`main.cpp`, `rt.cpp`, `natives.cpp`) | 45-95 s | 85-150 s |
| engine Java (`bcx/Match.java`, `BcxStateHash.java`, a substitute) | 70-90 s | 85-150 s |
| `runtime/rt.h`, the translator (`jvm/src/bcx/tr/`), or anything that rewrites most generated files | about 600 s (≈500 objects) | 85-150 s |

- Build only through `score` in your climbing worktree. A build with another bot set (plain `engine/build.py`)
  rewrites most generated files, and the next `score` then recompiles about 500 objects (595 s measured).
- `score --only ID` checks one scenario's exactness and count fast. Only a full `score` is a reading.

## Rules

- Never edit `tools/enginebench/`, its data folder, the bots (`src/`), `maps/`, or the frozen bots' class files.
  The eval refuses a checkout whose `tools/enginebench` differs from origin/main's.
- Never read `~/battlecode26-data/enginebench/SEALED-TEST-DO-NOT-READ/`. Never submit real jobs.
- **Exactness** is the official engine's (`engine/AGENTS.md`, "Exactness"): every output of every game, not only
  these. A change must be right for any game.
  - The sealed scenarios (other maps, matchups and seeds, drawn the same way) score your reported branch once.
  - A merge also needs the whole corpus to agree on Imperial (`engine/test/imperial.py run --full`).
- **Fast paths.** A fast path is a hand-written replacement for a translated method.
  - Keep the translated original in the build.
  - When `BCX_CHECK_FASTPATH=1` is set, run both on every call and abort on any difference in result or state
    (`engine/AGENTS.md`, "The checks").
  - The two fuzz scenarios run with that variable set and must stay exact.
- **Nothing about the scenarios in the engine.** No map, seed, bot or spec is special-cased. Nothing is computed at
  build time from maps or games, and no state is kept between games or runs. A game must be a function of its spec
  and its inputs.
- **Portability.** The binary must run on any x86-64-v2 machine (Imperial runs it on DoC's), so no `-march` or
  `-mavx…` flags, target attributes or AVX intrinsics. Everything else is yours to change:
  - compiler and linker options (`-O3`, LTO, PGO trained on games that are not scenarios);
  - the collector and its tuning, within the memory cap;
  - the translator and the runtime.

## Validity

Each check, the false win it blocks, and the variant that proved it (see "Validation"):

| check | `INVALID` when | blocks |
|---|---|---|
| result | a `result.json` field other than `cpu_s`, `wall_ms` or `backend` differs from the official engine's | a game played differently |
| state hashes | `hashes.txt` differs (the first differing round is printed) | any state difference, round by round, even one the result hides |
| replay | `replay.bc26` differs byte for byte (the reason names the gzip container, or the first differing flatbuffer offset) | wrong bytecode counts or actions; a cheaper gzip level; a skipped replay |
| memory | peak RSS (the largest process of the tree) exceeds 1.25 × main's on that scenario | memory traded for instructions: a larger heap, fewer collections |
| outputs only | the game leaves anything but its three outputs in its output folder, `/tmp` or `$HOME` | a cache between runs (BFS tables, results) |
| fast-path flag | a fuzz scenario, run with `BCX_CHECK_FASTPATH=1`, is not exact | a fast path whose differential check fails |
| names | code under test (`engine/` minus `engine/test/` and `*.md`) newly names the eval, its data, its sandbox paths, a scenario's map (quoted) or a scenario's seed. Checked before the build; no game is played | special-casing the scored games |
| portable | the build's flags tie it to a CPU or to inexact math, or engine sources carry target attributes or AVX intrinsics. Checked after the build; no game is played | a binary that runs only here |
| error, timeout, count | the game ends in an error, runs past its timeout (10 × main's wall, at least 300 s), or perf counts nothing | crashes, hangs |

**By construction**, each game runs in a bubblewrap sandbox:
- It sees only the binary, the scenario's map and the bots' class files: the eval's own copies, read-only.
- It also gets three empty writable folders, and nothing else: no network, no IPC, an empty environment.
- The sandbox's first process is the eval's runner (`runner.c`). It turns address randomization off and reaps every
  process of the tree, so a process that leaves its parent still counts toward peak RSS.
- The game therefore cannot read the reference outputs or any cache.

**Refused** (no score):
- an edited eval;
- a machine whose CPU model, `java -version` or engine jar differs from the reference's (HotSpot's `Math` stubs and
  the translated JDK make a game machine-specific);
- a reference file, map or bot class file that differs from the pinned manifest.

Changed bots also come out `INVALID`: the binary refuses class files that differ from the ones it was built from, and
a different bot plays a different game.

## Scenarios

17 weighted scenarios and 2 for validity only. Main's counts are from eval 08567fa:

| scenario (map~A~B~seed) | stratum | weight | map | rounds | G instr (main) | share of score | CPU s | cap MB |
|---|---|---|---|---|---|---|---|---|
| `far~awubot_us~awubot~mapseed` | L-long | 0.0325 | 60x40 | 1774 | 174.4 | 6.3% | 21.6 | 654 |
| `channels~opp_goobie~awubot~s1930748598` | L-long | 0.0325 | 60x40 | 2000 | 125.2 | 4.5% | 15.7 | 625 |
| `jail~opp_powerpuff~awubot~s1290900721` | L-short | 0.0414 | 60x40 | 630 | 71.7 | 3.3% | 8.7 | 579 |
| `TheHeist~awubot~opp_powerpuff~s606717240` | L-short | 0.0414 | 60x40 | 552 | 104.0 | 4.8% | 12.9 | 609 |
| `asabovesobelow~awubot~opp_cerr~mapseed` | M-long | 0.0734 | 59x40 | 2000 | 200.4 | 16.4% | 24.1 | 679 |
| `ZeroDay~awubot~awubot~s726518` | M-long | 0.0734 | 40x34 | 1410 | 68.9 | 5.6% | 8.4 | 474 |
| `dangerous_shortcut~awubot~opp_goobie~mapseed` | M-short | 0.1230 | 20x60 | 465 | 35.0 | 4.8% | 4.3 | 437 |
| `popthecork~opp_gst~awubot~mapseed` | M-short | 0.1230 | 30x40 | 357 | 27.6 | 3.8% | 3.5 | 437 |
| `cheesebottles~opp_obg~awubot~mapseed` | S-long | 0.0262 | 59x20 | 2000 | 108.4 | 3.2% | 14.6 | 516 |
| `Stash~awubot~opp_gravy~mapseed` | S-short | 0.2598 | 30x30 | 531 | 24.2 | 7.0% | 2.9 | 437 |
| `streetsofnewyork~opp_spaark~awubot~mapseed` | XL-long | 0.0252 | 60x60 | 841 | 246.3 | 6.9% | 29.9 | 1029 |
| `RUN~opp_parmesan~awubot~s1188161098` | XL-long | 0.0252 | 60x60 | 2000 | 187.0 | 5.3% | 22.4 | 1107 |
| `averystrangespace~opp_parmesan~awubot~mapseed` | XL-long | 0.0252 | 60x60 | 1288 | 165.5 | 4.6% | 21.9 | 1057 |
| `uneruesansfin~awubot_mystery~awubot~s184781485` | XL-long | 0.0252 | 60x60 | 1148 | 196.9 | 5.5% | 24.6 | 1038 |
| `neitheroptionisgood~awubot_us~awubot~s1159725803` | XL-short | 0.0241 | 60x60 | 791 | 238.2 | 6.4% | 29.4 | 1029 |
| `maze2~awubot_mystery~awubot~s757200739` | XL-short | 0.0241 | 60x60 | 791 | 200.0 | 5.4% | 26.7 | 1029 |
| `streetsofnewyork~awubot~opp_parmesan~s576019278` | XL-short | 0.0241 | 60x60 | 569 | 228.1 | 6.1% | 28.3 | 1028 |
| `dirtfulcat~fuzz_api~fuzz_api~mapseed` | fuzz | 0 | 30x30 | 641 | 16.5 | | 1.9 | 437 |
| `whereisthecheese~fuzz_lang~fuzz_chaos~mapseed` | fuzz | 0 | 20x20 | 423 | 3.6 | | 0.5 | 437 |

Every scenario uses the binary's 350 MB floor: the collector's 256 MB initial heap plus the binary.

## Validation

- **Where the readings ran.** Every reading was taken on clankerland with design mode (`ENGINEBENCH_DESIGN=1`,
  which skips the pin to origin/main), against the pinned manifests.
- **The reference.** The official engine was recorded here on 19 Nov 2026 (`engine/play_java.py`, indicators off,
  one fresh JVM per game). Without any change, the native engine agreed with it on all 19 visible and 19 sealed
  scenarios on the first run. On openjdk 21.0.13.1 every reference replayed byte-identical; the manifests record
  that JDK.
- **The variants.** Each variant is a commit on branch `t3/xe-validation`, followed by its revert.
- **Eval commits.** Variants were scored with eval cb2036f. Later eval commits (08567fa, 8d0e76d) changed output,
  error wording and the main export; they change no reading's number or verdict.

| variant | commit | change | reading |
|---|---|---|---|
| no-op | 1a6df81, cb2036f, 08567fa | none | 89.7802 valid, three runs (spread 2.7·10⁻⁵ G) |
| main | origin/main's `engine/` (unchanged from 6008051 to 232fe64) | `score --main` | 89.7802, 89.7803 valid |
| known speedup | ec5e3ad | `BcxStateHash.rngState` stops at its match instead of scanning all 65,536 candidates | **88.9017 valid** (−0.98%) |
| known slowdown | e74d55a | `bcx.Match` computes the state hash twice a round | **94.6951 valid** (+5.47%) |
| result (and hashes, replay) | aa64242 | robots get one bytecode less on every turn after their first (`rt_pause`) | **INVALID** 19/19: result (`digest`, `hash` in all; `A`, `B`, `round`, `series`, `winner` in the 4 games that turned), state hashes from round 2, replay; it would have read 89.0460 |
| state hashes | 105fff4 | one more value absorbed into each round's hash | **INVALID** 19/19: state hashes from round 1, result `hash`; 89.7803 |
| replay | b0c8e34 | replay gzip at level 1 instead of 6 | **INVALID** 19/19: replay ("the gzip container differs, the flatbuffer is the same"), result `digest`; it would have read 89.5688 |
| memory | e58b442 | the collector runs at most once per GB allocated (`GC_set_min_bytes_allocd`) | **INVALID** 19/19: peak RSS 503-1412 MB against caps of 437-1107 MB; it would have read 79.8828 (−11%) |
| outputs only | 2ead05c | each game leaves `$HOME/.bcx-bfs-cache` | **INVALID** 19/19: "wrote outside its outputs: home/.bcx-bfs-cache" |
| fast-path flag | c8d38b4 | the binary aborts when `BCX_CHECK_FASTPATH=1` is set (a differential check that found a difference) | **INVALID** on the 2 fuzz scenarios ("died of signal 6"), the 17 others valid |
| names | a5d10c0 | `main.cpp` compares the spec's map with `"far"` | **INVALID** before the build, 0.2 s: `engine/runtime/main.cpp:424 names '"far"'` |
| portable | eb2a609 | `-march=native` on `hsmath.c` in `engine/build.py` | **INVALID** after the build (49 s), no game: "build flag -march=native" |
| pinned | test | an edited or an extra file in `tools/enginebench` | refused (`test_pin_refuses_an_edited_or_extra_eval_file`) |
| machine | test | another CPU model in the manifest | refused (`test_another_machine_is_refused`) |
| sandbox, peak RSS of the tree | test | a static program that reads `/etc/passwd`, a secret file and the PATH, writes to the classes, `$HOME` and `/tmp`, and touches 96 MB in an orphaned grandchild | it sees none of them, the classes are read-only, its leftovers are named, and the 96 MB is counted (`test_sandbox_shows_only_the_inputs_and_keeps_nothing`) |
| each output check, error, timeout, count | test | the reference's own outputs, then each one altered | each altered case names its reason (`test_validate_accepts_the_reference_and_names_each_difference`) |

The speedup saves 0.98 M instructions a round (0.879 G over 899 weighted rounds): half of a 65,536-step scan, twice a
round, at about 15 instructions a step.

## Data and upkeep (designer, integrator)

`~/battlecode26-data/enginebench/` holds:

| folder | contents |
|---|---|
| `visible/` | `manifest.json` (pinned by its sha256 in `bench.py`), `ref/ID/` (the official engine's `result.json`, `hashes.txt`, `replay.bc26`), `maps/` (read-only) |
| `classes/` | the frozen bots' class files, used by the references and the sandbox |
| `main/` | main's cached readings and its exported checkout |
| `runs/` | each checkout's last games |
| `bin/` | the compiled runner |
| `sources/` | the corpus run the draw used, and the map sizes |
| `SEALED-TEST-DO-NOT-READ/` | the sealed manifest and references, the draw script (`select.py`) and its secret seed |

- References are recorded here, on this CPU and JDK. On DoC machines the official engine diverged in 8 of ~18,500
  runs; here it never has.
- When the JDK, the CPU or the jar changes, the eval refuses. To recover, run `ENGINEBENCH_DESIGN=1 enginebench
  record KIND …` from a fresh `engine/play_java.py` run of the same specs, then `calibrate KIND` from two `--main`
  runs' `last.json`, then re-pin `MANIFEST_SHA256`. If the fresh run's outputs equal the references (a JDK patch
  release did), it is enough to update the manifest's `recorded` fields and re-pin.
- When main's `engine/` changes, run `score --main` and `ENGINEBENCH_INTEGRATOR=1 score --sealed --main` once. The
  cache is keyed by main's `engine/` tree, the manifests and the eval's code.
- **The climb's end.** The integrator runs `score --sealed` once on the reported branch; it should improve by about
  as much as `score`. Then the integrator runs the engine's own checks (`agreement.py --quick`, the full corpus on
  Imperial).
