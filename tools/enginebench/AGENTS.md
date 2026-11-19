# tools/enginebench: the native engine's speed eval

One command scores a checkout's native engine (`engine/`, read `engine/AGENTS.md` and `engine/DESIGN.md`): it
builds the engine, plays a fixed slice of the agreement corpus, and prints the engine's cost per game in user-space
instructions. Lower is better. A candidate whose games differ in any output from the official engine's is `INVALID`.
It exists so an engine speed-up is measured in one command, exactly, without a corpus run.

## Commands

| command | does | wall |
|---|---|---|
| `tools/enginebench/enginebench score [-j N] [--json]` | build, play the 19 visible scenarios, check them, print the score and main's | WALL_SCORE |
| `tools/enginebench/enginebench score --only ID[,ID]` | build, play only those scenarios: their rows and validity, no score | build + that game |
| `tools/enginebench/enginebench explain` | the last score's scenarios, worst first, each `INVALID` reason in full | < 0.2 s |
| `tools/enginebench/enginebench explain --profile ID` | one scenario under `perf record`: its instructions by function | that game |
| `tools/enginebench/enginebench scenarios [--json]` | what each visible scenario holds | < 0.2 s |
| `tools/enginebench/enginebench score --main` | the same for origin/main's engine (exported to the data folder), cached | once per main |
| `ENGINEBENCH_INTEGRATOR=1 tools/enginebench/enginebench score --sealed [--main]` | the sealed scenarios: integrator only | |

Exit codes: 0 valid, 1 `INVALID`, 2 refused (no score: the reason is printed). Tests:
`.venv/bin/pytest tools/enginebench/bench_test.py`.

## The score

**score = Σ wᵢ · instructions:uᵢ / 10⁹** over the 17 scored scenarios: the estimated user-space instructions of one
game of the corpus, in G. Each scenario is one real corpus game (awubot against itself, its snapshots and the
`opp_*` bots, both seats, map seed or the corpus's extra seed), played whole by `bcx play` exactly as the corpus plays
it, with `--no-indicators` and the replay written. The scenarios are a stratified random draw from the 7,819 non-fuzz
corpus games: map size (tiles: S < 1200 ≤ M < 2400 ≤ L < 3600 ≤ XL) × game length (rounds < 800, ≥ 800), drawn in
proportion to each stratum's share of the corpus's native CPU time (at least one a stratum, one map at most once a
stratum). wᵢ = the stratum's share of the corpus games / the games drawn in it, so the sum estimates the mean over
the corpus, where 42% of the native CPU time is the 60×60 maps. Two more scenarios (fuzz bots) are checked, not
weighted.

- **instructions:u** of the whole sandboxed `bcx play` process tree (`perf stat`), the game's fork included. Load
  does not change it; address randomization would (1.6·10⁻⁴ run to run, through the conservative collector), so the
  game runs with it off. Measured spread: SPREAD.
- Kernel instructions are printed, not scored: they vary ±8% run to run. The memory cap (below) keeps a candidate
  from moving work into page faults.
- **What runs**: the checkout's own `engine/build.py --engine 1.2.6 --bots BOTS` (the 18 frozen packages of the
  corpus, pinned in `bench.py`), then its `engine/build/bcx`. Nothing is modelled except the corpus, by the weights.
- **Not scored**: engine 1.2.5, indicators on, `--dump`, robot stdout, wall time, build time.

## How to climb

1. `score` on the unchanged checkout: that is your starting score (it equals main's).
2. One change in `engine/` at a time. `score`. Keep it only if it is valid and lower; else revert it.
3. Log every scored try as one line of `runs/climb.jsonl` in your worktree:
   `{"t": EPOCH_SECONDS, "score": SCORE, "kept": true|false, "what": "the change"}`.
4. Commit each kept change on your branch and push the branch. Never merge, never open a PR.

Where the cost is: `explain` (scenarios by weighted cost), `explain --profile ID` (functions of one game), and
`engine/AGENTS.md` "Speed": at the start the cats' BFS tables (`GameWorld.bfsFromTarget`, quadratic in tiles), and
the collector behind the translated world's allocations (a `MapLocation` per step, `Direction.values()` clones),
together about 85% on DefaultMedium; the round-1 state hash also walks every pair of tiles. The bots' own code is a
small share.

Loop cost (clankerland, shared, 8 build jobs): LOOP_COSTS.

- Build only through `score` in your climbing worktree: a build with another bot set (plain `engine/build.py`)
  rewrites most generated files, and the next `score` recompiles about 500 objects (FULL_REBUILD).
- `score --only ID` checks one scenario's exactness and count fast; only a full `score` is a reading.

## Rules

- Never edit `tools/enginebench/`, the data folder, the bots (`src/`), `maps/` or `build/classes` of the frozen bots.
  The eval refuses a checkout whose `tools/enginebench` differs from origin/main's.
- Never read `~/battlecode26-data/enginebench/SEALED-TEST-DO-NOT-READ/`. Never submit real jobs.
- Exactness is the official engine's (`engine/AGENTS.md`, "Exactness"): every output of every game, not just these.
  A change must be right for any game: the sealed scenarios (other maps, bots and seeds) score your reported branch
  once, and a merge also needs the full corpus to agree on Imperial (`engine/test/imperial.py run --full`).
- **Fast paths** (a hand-written replacement for a translated method): keep the translated original in the build and,
  when `BCX_CHECK_FASTPATH=1` is set, run both on every call and abort on any difference in result or state
  (`engine/AGENTS.md`, "The checks"). The two fuzz scenarios run with that variable set and must stay exact.
- Nothing about the scenarios in the engine: no map, seed, bot or spec special-cased, nothing computed at build time
  from maps or games, no state kept between games or runs. A game must be a function of its spec and inputs.
- The binary must run on any x86-64-v2 machine (Imperial runs it on DoC's): no `-march`/`-mavx…` flags, target
  attributes or AVX intrinsics. Compiler and linker options are otherwise yours (`-O3`, LTO, PGO trained on games
  that are not scenarios), as is the collector's tuning within the memory cap.

## Validity

Each check, the false win it blocks, and the variant that proved it (below, "Validation"):

| check | `INVALID` when | blocks |
|---|---|---|
| result | a `result.json` field other than `cpu_s`, `wall_ms`, `backend` differs from the official engine's | a game played differently |
| state hashes | `hashes.txt` differs (the first differing round is printed) | any state difference, round by round, even one the result hides |
| replay | `replay.bc26` differs byte for byte (gzip, else the first differing flatbuffer offset) | wrong bytecode counts or actions; a cheaper gzip level; a skipped replay |
| memory | peak RSS (the largest process of the tree) over 1.25 × main's on that scenario | memory traded for instructions: a larger heap, fewer collections |
| outputs only | the game leaves anything but its three outputs in its output folder, `/tmp` or `$HOME` | a cache between runs (BFS tables, results) |
| fast-path flag | a fuzz scenario, run with `BCX_CHECK_FASTPATH=1`, is not exact | a fast path whose differential check fails |
| names | code under test (`engine/` but `engine/test/` and `*.md`) newly names the eval, its data, its sandbox paths, a scenario's map (quoted) or seed | special-casing the scored games |
| portable | the build's flags tie it to a CPU or to inexact math, or engine sources carry target attributes or AVX intrinsics | a binary that runs only here |
| error, timeout, count | the game ends in an error, runs over its timeout, or perf counts nothing | crashes, hangs |

By construction: each game runs in a bubblewrap sandbox that shows it only the binary, the scenario's map and the
bots' class files (read-only, the eval's own copies) and three empty writable folders; no network, no IPC, an empty
environment. The game cannot read the reference outputs or any cache. Refused (no score): an edited eval; a machine
whose CPU model, `java -version` or engine jar differs from the reference's (HotSpot's `Math` stubs and the
translated JDK make a game machine-specific); a reference file or map that differs from the pinned manifest.

## Scenarios

SCENARIOS

## Validation

READINGS

## Data and upkeep (designer, integrator)

`~/battlecode26-data/enginebench/`: `visible/` (`manifest.json`, pinned by its sha256 in `bench.py`; `ref/ID/` the
official engine's `result.json`, `hashes.txt`, `replay.bc26`; `maps/`), `classes/` (the frozen bots' class files the
references and the sandbox use), `main/` (main's cached readings and its exported checkout), `runs/` (each checkout's
last games), `sources/` (the corpus run the draw used, map sizes), `SEALED-TEST-DO-NOT-READ/` (the sealed manifest,
references, the draw script and its secret seed).

- References were recorded here (`engine/play_java.py`, one fresh JVM a game, indicators off), on this CPU and JDK:
  the official engine diverged on DoC machines in 8 of ~18,500 runs, never here.
- When the JDK or the CPU changes, the eval refuses: re-record (`record`, `calibrate`, re-pin `MANIFEST_SHA256`).
- When main's engine changes, run `score --main` (and `--sealed --main`) once: the cache is keyed by main's `engine/`
  tree, the manifests and the eval's code.
