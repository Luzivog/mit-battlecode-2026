# tools/bcgames: `tools/play` and `tools/games`

How to use them: `docs/evaluation.md`. This file is for changing them.

## Layout

| file | what |
|---|---|
| `java/bcplay/Play.java` | the game loop: `Server.run`/`runMatch` for one map, many games per JVM, one JSON line per game |
| `java/bcplay/MapInfo.java` | what the engine loads from map files (counts, part hashes, the layout), to compare and class maps |
| `java/bcplay/JdkPrint.java` | the fingerprint of the JDK library code bots run (part of the game key) |
| `store.py` | data dir, engine jars (sha256-pinned), bot builds (javac alone, deterministic jar, class hash), map seed variants, the SQLite cache, game keys |
| `play.py` | specs to jobs, worker JVMs (timeouts, restarts), the runner (cache, native or Java, exactness guard), `tools/play` |
| `native.py` | the native engine (`engine/`): which binary plays a game, builds (shared by every process of the host), games through `bcx`, the guard's verdict |
| `stats.py` | GSPRT (multinomial, fishtest's MLE form), Elo, LOS, bootstrap over maps, SPRT simulation |
| `pools.py` | map pools; the holdout list lives only in `~/battlecode26-data/games/SEALED-TEST-DO-NOT-READ/` |
| `mapclass.py` | T4's map classes (docs/theory/t4-maps.md §3), reproduced exactly on all 150 maps, stdlib only |
| `imperial.py` | the `--remote` backend: batches through track C's `tools/run`, ships a test's own `.bcgames-ship/<test>/` (hard links, removed when the test ends) and native binaries (to the volume, once each) |
| `retain.py` | bounded disk: old replays, unused binaries, dead engine build trees, leftover ship folders (`sweep`, hourly at `tools/games` start) |
| `games.py` | runs: paired tests, scheduling, reports, `tools/games` |
| `*_test.py` | `python3 -m unittest discover -s tools/bcgames -t tools -p '*_test.py'` (`play_test.py` plays real games on both engines: a few minutes; its native tests skip unless the shared native store holds a binary for them) |

Python is stdlib only (it runs on remote nodes too). Data: `~/battlecode26-data/games/` (`BCGAMES_DATA` overrides).

## Invariants

1. **A game is a pure function of its key, whichever engine plays it**: `Play.java`'s hash, the JDK fingerprint, engine
   jar, both bots' class hashes and packages, map file hash, seed (`store.game_key`). The JDK fingerprint (`JdkPrint`)
   hashes the JDK classes the instrumenter copies for bots. Ubuntu's 21.0.13 and DoC's Temurin 21.0.13+9 print the same,
   so the local and remote caches are one. JVM flags, load and games-per-JVM are not in the key because they change
   nothing (docs/testing.md "Runner": byte-identical replays). Anything that can change a game goes into the key;
   anything else stays out of it. The engine that played a result is provenance (`backend`, `native_build`), not part of
   the key: the native engine plays the official engine's games, and the exactness guard checks it on every run
   (docs/evaluation.md "Engines").
2. **The reference engine is the unmodified official jar.** `Play.java` calls the engine's own classes in `Server`'s
   order. The only engine state it touches is `InternalRobot.rand` (the static cat RNG), reset to `new Random(1092)`
   before each game, which is exactly a fresh JVM's state. `play_test.py` checks both: byte-identical replay with
   `battlecode.server.Main`, and a game played second in a JVM equals the same game alone.
3. **Seeds are map variants**: `store.map_variant` patches the 4 bytes of `GameMap.randomSeed`. It equals the
   `src/battlecode` override's `-Pseed` on 1.2.5 (byte-identical replays, checked on 3 maps). The native engine takes
   the spec's `seed` and replaces the map's at load, which plays the same game (the guard compares seeded games too).
4. **Bots compile alone** with Gradle's javac flags; the class files are byte-identical to `./gradlew classes`.
5. **A verdict depends only on the cells**: a test scores the complete prefix of its cells in submission order, so
   timing never changes a verdict, and a rerun of unchanged code reads the same verdict from the cache.
   `rescore` rebuilds a run's state from `results.jsonl` alone (`retry` uses it after replaying failed games).
6. **Tests** (`games.Test`): plain (trinomial on the cell score) or against a ref (pentanomial on (A − ref + 1)/2,
   the ref playing the same cells); mode gain (H1 above 0.5) or harm (H1 below). The default ref is
   `challenger@origin/main`, pinned to a commit at submission (`games.pin`). Exit 0 means the test passed.
7. **The native backend never decides a game the Java engine would play differently.** A native error replays the
   game on Java; a guard mismatch the Java engine's own nondeterminism doesn't explain stops the run, marks the binary
   bad and purges its results (`play.Runner.guard`, `native.verdict`). A run never concludes while a check is
   pending (`guards_pending`): locally the twin plays beside the run, remotely in its own task (`ID~guard`, both
   engines on one machine, and its copy `ID~guard~2` on another; the first verdict counts, a mismatch from either
   stops the run), so no cell waits for a Java twin. Remote guards of a test's first min cells go out ahead
   (`imperial.guard_ahead`), counted pending from then.
8. **Pools never hold a sealed map**: every visible pool, class pool and list pool is checked against
   `pools.visible()`.

## Disk

Everything a tool writes is bounded, pruned on its own (`retain.sweep` when `tools/games` starts, at most hourly;
`tools/games sweep [--dry-run] [--list N]` now). What goes, and what never does:

| what | rule | never removed |
|---|---|---|
| a run's `replays/` (`games/runs/ID/`, also the sealed runs', by file times alone) | 24 h after the run's last activity (its state, its replays, their index); sooner, oldest first, while all replays exceed 15 GB | a run marked `tools/games keep ID` (`--off` undoes it); one active within 2 h; the results: `tools/games replays ID` plays the games again |
| native binaries (`native/bin/`) | the most recently used kept: 24, or any used within 6 h, up to 4 GiB in all (`BCGAMES_NATIVE_KEEP_BYTES`) | one used within 30 min; one a live process holds (`native.hold`: a shared flock on `bin/KEY.use`, taken by every process that plays or ships it) |
| engine build trees (`~/battlecode26-data/engine/build/gen-VERSION-H`, H = sha256 of the checkout's path) | once no checkout of this repository (`git worktree list`) nor enginebench's export of main has H, after a day without a build | a live checkout's tree |
| this checkout's `.bcgames-ship/` | a remote test ships its own folder of hard links and removes it when it ends; a folder idle a day goes (its driver died, or the old shared layout) | |
| this checkout's `runs/` (`tools/run`'s results) | a run unchanged for a day: its per-task folders packed into `RUN/tasks.tgz`, each `result.json` a line of `RUN/tasks.jsonl` (`runs.compact_local`, at most hourly when `tools/run` or `tools/compute run` starts) | `summary.json`, `run.json`, gathered files |

Measured on 19 Nov (`tools/games sweep --dry-run`): replays 20.4 GB, all under a day old, 4.65 GB of them over the
cap; binaries 13 of 52 (1.32 GB); a worktree's `runs/` 312 MB to 17 MB packed (126,000 files of 1 KB were 1.2 GB on
disk in one worktree); `./gradlew build` no longer unpacks the 280 MB client into each worktree (`./gradlew
unpackClient` when wanted).

Not pruned: the bot jars (`bots/`, 2.6 GB: `tools/games replays` needs the jars that played a game), bots' class
folders (`native/classes/`, 1.3 GB), the build slots (`native/gen/`, two a version, needed whole), the cache.

## Changing it

- Any change to `java/` changes the runner version, which empties the cache for every key. Batch such changes.
- After changing `Play.java`, run `play_test.py`, then a null run (`tools/games ab challenger awubot --pool smoke --fg`)
  must read "no behaviour change".
- A new result field goes in `Play.java` (cheap per-game state) or `play.finish` (provenance), and in the native
  engine's result (`engine/`) if it is an outcome. Keep the field names of `engine/AGENTS.md` (track X's format).
- Native builds: `native.build` runs `engine/build.py --team ... --gen ... --out ... --glue-opt=-O1 --opt=-O2
  --lto=off` from a copy of the engine's sources: `-O2` without ThinLTO, so a challenger rebuild costs what it did
  before XC (engine/AGENTS.md, "Build configurations"). A binary's key hashes those sources, the JDK, clang,
  `GLUE_OPT`, `OPT`, `LTO` and its bots in order, so any
  engine change rebuilds; a build slot (`gen/<version>-<i>`) keeps its engine copy at a fixed path, so only changed
  files recompile.
