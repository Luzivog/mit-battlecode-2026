# engine/

A native build of the official Battlecode 2026 engine that plays exactly the official engine's games with the same
Java bots, many times faster. Why it is built this way: `DESIGN.md`. Exactness fails silently and costly, so every
change here passes the full corpus (`engine/test/imperial.py run --full`) before it lands ("The checks").

## Exactness

A game is fixed by its spec (below). For every game of the corpus, the native engine must give what the reference
gives:

1. **The result**: winner, win type (`DominationFactor`) and final round.
2. **The state hash of every round**: `battlecode.world.BcxStateHash` (in `jvm/src`), computed after each
   `GameWorld.runRound()` over the whole world: every robot in execution order with all its state, every tile, every
   trap, both teams' info, the cats' state, the cheese mines, the shared arrays, the squeak queues and the world RNG.
   Its comment lists what it covers and what it cannot read.
3. **Every robot's bytecodes on every turn**, and every action: the replay (`Turn.bytecodesUsed`, the actions).
4. **The replay bytes**: the `.bc26` file byte for byte, gzip container included (its `digest`), and the gunzipped
   flatbuffer decoded to the first differing round and turn when it differs.

A game the engine ends by coin flip (`WON_BY_DUBIOUS_REASONS`, decided by an unseeded `Math.random()`) must match up
to that flip: same state hashes, same round, win type `WON_BY_DUBIOUS_REASONS`; the winner is not compared.

**The reference** is the official jar, unmodified: `battlecode26-java-1.2.6.jar` (sha256 `ea722532…`; 1.2.5 is
`b9f39d19…`), in a fresh JVM per game, on this machine's JDK (`java -version` is recorded with every recording; the
native build translates the same JDK's class files). It is run by `bcx.RefMain` (`jvm/src`), which is the official
`Server`'s match loop with the official control providers, plus the state hash after each round, read-only.
RefMain's replay must be byte-identical to `battlecode.server.Main`'s for the same game (`test/refmain_test.py`).

**Conditions.** One game per fresh JVM. The engine's only state that outlives a game in a JVM is the cats' RNG
(`InternalRobot.rand`, made `new Random(1092)` by its static initializer), so game N of an official match (one JVM,
`-Pmaps=a,b,c`) starts it where the earlier games left it. Before a game both engines set it to a plain
`new Random(1092)` stepped `cat_rng_skip` draws (`Random.next` calls; `bcx.Match.catRng`), and the result reports the
game's draws (`cat_rng_draws`, as `tools/play` does: the LCG steps from its first to its last state). Game N of a
match is the spec with `cat_rng_skip` = the sum of `cat_rng_draws` of games 1 to N-1 (`test/refmain_test.py` checks
it against `battlecode.server.Main` playing two maps in one JVM). Every other static starts fresh in both engines.

**Seeds.** A spec without a seed plays the map's embedded seed (official conditions). A spec with a seed plays it the
way this repo's `src/battlecode` overrides do: the map's `LiveMap` seed is replaced at load. RefMain does this with
the jar's own `LiveMap` constructor, so the jar stays unmodified.

## Spec and result

The fields of `tools/play` (track E, `docs/evaluation.md`), so either engine can stand in for it. A spec is one JSON
object a line (JSONL); fields it does not name are copied to the result.

| field | | meaning |
|---|---|---|
| `a`, `b` | required | team A's and team B's package (`src/<pkg>`; `tools/play` also takes `pkg@REV`, the engines do not) |
| `map` | required | map name: `maps/<map>.map26`, else the jar's bundled map |
| `seed` | optional int | game seed; absent: the map's embedded seed |
| `engine` | optional | `1.2.6` (default) or `1.2.5` |
| `classes` | optional | the compiled classes dir; default `build/classes` of the checkout |
| `classes_a`, `classes_b` | optional | per-team classes dir, overriding `classes` |
| `id` | optional | names the game's folder; copied to the result |
| `replay` | optional bool | false: no `replay.bc26` kept (the digest is still computed) |
| `indicators` | optional bool | false: indicator dots, lines and strings left out of the replay, as `tools/play` plays; default true, as the official Gradle run (`bc.engine.show-indicators`). A game does not depend on it; the replay and its digest do |
| `cat_rng_skip` | optional int ≥ 0 | the cat RNG starts this many draws after its fresh state (default 0; "Conditions") |

A result is one JSON object a line, in spec order:

| field | meaning |
|---|---|
| `id`, `a`, `b`, `map`, `engine`, other fields | from the spec |
| `seed` | the seed the game played (the map's when the spec has none) |
| `winner` | `A` or `B` |
| `winner_pkg` | the winner's package |
| `win_type` | `KILL_ALL_RAT_KINGS`, `MORE_POINTS`, `MORE_ROBOTS_ALIVE`, `MORE_CHEESE`, `WON_BY_DUBIOUS_REASONS`, `RESIGNATION` |
| `round` | the last round played |
| `hash` | 16 hex digits: `BcxStateHash.chain()` fed each round's number then its state hash, in round order, then `finish()` (the 64-bit mixer of `BcxStateHash`, see its comment); the per-round hashes are `hashes.txt` |
| `cat_rng_skip` | from the spec, when it has one |
| `cat_rng_draws` | draws from the cat RNG during the game |
| `cooperation` | `GameWorld.isCooperation()` at the end |
| `A`, `B` | each team's `points` (what `setWinnerIfMorePoints` would award at the end), `cheese_transferred`, `cat_damage`, `rats`, `kings`, `cheese` (`TeamInfo`) and `crashed`: robots whose `run()` ended by a `Throwable` other than `RobotDeathException` or by returning (what the engine reports as "Stack trace" and "froze"; disintegrating is not a crash) |
| `series` | `{"every": 100, "fields": [...], "A": [...], "B": [...]}`: each team's `[cheese_transferred, rats, kings, cat_damage]` every 100 rounds and at the end, as `tools/play` |
| `digest` | the first 16 hex digits of the SHA-256 of the replay file (gzip and all), as `tools/play`'s |
| `backend` | `native` or `java` |
| `cpu_s`, `wall_ms` | the game process's CPU seconds and wall milliseconds |
| `error` | present only when the game could not be played; then no outcome fields |

`tools/play` also reports `engine_errors` (crash reports with no frame of either team); the engines do not. Its
crash scanner gives both teams' crashes to A when both teams are the same package; the engines attribute each crash
to the robot's team.

## The checks

- **`test/agreement.py`** plays specs on both engines and compares the result, every round's state hash and the
  replay bytes. Quick mode (a few games, seconds of native time) for every change; `--full` for the whole corpus.
- **The corpus** (`test/corpus.py`): awubot against itself and against `awubot_{quals,us,mystery,sus}` and every
  `opp_*`, both seats, on every map in `maps/` at the map's seed and one more seed; the fuzz bots (`src/fuzz_*`)
  against each other and against awubot, on maps that make every call, refusal and exception happen.
- **The fuzz bots** call every `RobotController` method with legal and illegal arguments, catch and leak exceptions,
  overrun the bytecode limit mid-statement, fail asserts, resign, disintegrate, and use the JDK features the corpus
  uses (collections, strings, boxing, lambdas, records, switches, `Math`, `Random`).
- **Status** (19 Nov 2026, engine 1.2.6, commit f14753a): every game of the full corpus (8388) has a reference run
  and a native run that agree in all the checks and in every result field. 6 more reference runs on DoC machines
  disagreed with the native run on the same machine; each time the native run equalled the reference's run of that
  game elsewhere ("Known gaps"). 1.2.5: the same for a 1-in-10 sample (839 games, 1 such run). Quick 5/5,
  `refmain_test` 13/13, `conformance` 5/5.
  After track XC's fast paths (19 Nov, run-1119-141114-8d31, `--task-mem 6000M`): 8385/8388, the other 3 the
  reference's DoC fault ("Known gaps"), agreeing when re-run (run-1119-145839-9f88, 9/9).
- **Fast paths.** A hand-written replacement for a translated method lands only when the corpus agrees and a
  differential build (`BCX_CHECK_FASTPATH=1`) runs the translated original beside the fast path on every call over the
  corpus and finds no difference in results or state. The original stays in the build behind that flag.
  - How: a patch class (`jvm/subst/bcx/subst/*_p.java`, a `patch` line in `natives.txt`) defines the method and an
    `@Orig` stub `NAME$orig` with the same descriptor; the translator keeps the target's method under that name.
    `bcx.subst.FastPath` holds the flag (`CHECK`), `fail` and the fast paths' natives (`runtime/natives.cpp`).
    A patch class's name stands for its target in every patch, so one patch can call what another adds.
  - The fast paths: `GameWorld` (the cats' BFS tables, the vision cone's tiles, `bfsHash`'s table reads),
    `InternalRobot` (`canSenseLocation`, `getAllRatLocations`), `RobotControllerImpl.canSenseLocation`,
    `BcxStateHash` (`rngState`, `tiles`, `bfsHash`), `MapLocation.toString`, `Unsafe`'s unaligned reads.

## Build, play, test

```sh
./gradlew classes                                  # the bots' class files (build/classes)
.venv/bin/python engine/build.py                   # engine/build/bcx: engine 1.2.6 and every bot package
.venv/bin/python engine/build.py --engine 1.2.5    # engine/build/bcx-1.2.5 (--engine all: both)
engine/build.py --team PKG=CLASSES_DIR... --gen DIR --out FILE [--glue-opt=-O1] [--opt=-O2 --lto=off]   # native.py's
engine/build/bcx play SPECS.jsonl OUTDIR [-j N] [--no-replay] [--no-indicators] [--dump]   # native games
.venv/bin/python engine/play_java.py SPECS.jsonl OUTDIR [-j N] [--dump]  # the same games on the official jar
.venv/bin/python engine/test/agreement.py --quick  # both engines, compared, here (--specs FILE: any specs)
python3 engine/test/imperial.py run --full -- --task-mem 6000M   # the whole corpus on Imperial, both engines a task
python3 engine/test/imperial.py report RUN         # its agreement and CPU time, from runs/RUN/rows.jsonl
.venv/bin/python engine/test/refmain_test.py       # RefMain against battlecode.server.Main
.venv/bin/python engine/test/conformance.py        # translated test programs against the JVM
```

- `bcx play` writes `OUTDIR/<id or index>/result.json`, `hashes.txt`, `replay.bc26` (`--dump`: `dump.txt`) and prints
  the result lines in spec order. Each game runs in a forked process, so every static starts fresh. It refuses a
  spec whose bot class files differ from the ones compiled in (rebuild). It reads `maps/` of the checkout it was
  built in (`--root REPO` or `BCX_ROOT` for another), then the jar's maps (`engine/build/maps-VERSION/`).
- `build.py` fetches the jars and bdwgc once into `~/battlecode26-data/engine/`, compiles `engine/jvm`, translates
  into `~/battlecode26-data/engine/build/gen-VERSION-CHECKOUT/` (one per checkout, so worktrees build at once) and
  compiles with ninja (`-j`, default 8). Needs clang, lld (for ThinLTO), ninja, cmake, a JDK 21, zlib. The binary is
  static, so it runs on any x86-64 Linux node.
- **Build configurations.** `build.py`'s default, `-O3` and ThinLTO (`--lto thin`, lld; the link inlines the
  translated code's small cross-file calls), is the standalone build, enginebench's and `imperial.py`'s: the climbed
  one. `tools/play` and `tools/games` (`tools/bcgames/native.py`) build `-O2 --lto off --glue-opt=-O1`, because their
  builds sit on every test's critical path. Measured on clankerland (19 Nov), a challenger change on 14 sparring bots:

  | configuration | incremental build, wall / CPU | full build, wall / CPU | enginebench (G a game) |
  |---|---|---|---|
  | main's engine before XC (-O2) | 57-75 s / 210-295 s | 241 s / 1480 s | 89.78 |
  | `-O2 --lto off` (native.py) | 68-75 s / 240-270 s | 204 s / 1290 s | 15.02 |
  | `-O3 --lto off` | 112 s / 356 s | 276 s / 1620 s | 15.10 |
  | `-O2 --lto thin` | 124 s / 557 s | 567 s / 3250 s | not measured (-O3 saved 1%) |
  | `-O3 --lto thin` (default) | 119 s / 566 s | 522 s / 3200 s | 11.48 |

  The flags change no result: the full corpus agrees under the default ("The checks"), and under `-O2 --lto off`
  `agreement.py --quick` 5/5 and a 1-in-25 DoC sample 336/336 (run-1119-161950-59c3, 19 Nov).
- `tools/play` and `tools/games` build their own binaries through `--team` (each bot from its own classes folder, in
  that order: a team's module index is its position), `--gen` and `--out` (docs/evaluation.md "Engines"), with
  `--glue-opt=-O1`: `klasses.cpp` (class tables, class-init wrappers, dispatch chains, cut stubs; no game logic) is
  one 4.7 MB file that changes with any bot and takes 177-238 s at -O2 (8 s at -O1, 1 s at -O0).
- Builds are incremental: method files (`m_MODULE_N.cpp`) and headers (`types.h` for the engine and the JDK,
  `types_PKG.h` per bot) are per module and rewritten only when they change. Measured on 8 cores: everything,
  6 to 8 min (about 25 CPU-min); nothing changed, 38 s (the translation); one bot changed, 43 s, when the change
  reaches no JDK code no other bot reaches (else the engine files that change recompile too).
- `imperial.py run` ships `engine/build/dist` (the stripped binaries, the jar's maps, the jars) with `tools/run --kit`;
  a task plays its specs on both engines and keeps both engines' files only for games that disagree. Run the full
  corpus with `-- --task-mem 6000M`: at the default 1.7 GB the reference JVM of `clockwisdom`, `coop`, `mining` and
  `where_are_you` games dies with no result (it peaks at 4.5 GB on `mining` with awubot_sus: per-robot class copies).
- A DoC node's disk can fill under a run: on 19 Nov cedar06 and rowan24 failed 7 native games with `bcx: fatal:
  cannot write .../replay.bc26` (other tasks there: `OSError: [Errno 28] No space left on device`), and rowan25 lost
  a 4 KB block of a `hashes.txt`. A failed write is now always a game error (`cannot write`); such an error is the
  node's: re-run the game elsewhere.

### How the translation works (what to read before changing it)

- `jvm/src/bcx/tr/`: `Pool` (where classes come from: a team's classes through the official instrumenter, the
  JDK, the jar, substitutes), `Rta` (reachability), `Linkage` (the JVM's linkage errors a robot can hit), `Layout`
  (vtables, itables, devirtualization), `Indy` (invokedynamic lowered to plain bytecode), `Gen` (one method to C++),
  `Emit` and `Glue` (the program). `natives.txt` lists everything not translated: C++ natives
  (`runtime/natives.cpp`), whole-class substitutes and method patches (`jvm/subst/`), cut paths (reaching one is
  fatal at run time, never silent).
- `runtime/`: `rt.h`/`rt.cpp` (objects, type checks, Java arithmetic, the bytecode monitor: RobotMonitor line for
  line), `sched.cpp` (robots as coroutines: SandboxedRobotPlayer and PlayerControlProvider), `natives.cpp`,
  `hsmath.c` (HotSpot's Math stubs), `main.cpp` (the CLI).
- The robot's bytecode counting, preemption points and per-robot statics come from the official instrumenter's own
  output; nothing about counting is re-implemented except RobotMonitor itself.

## Speed

CPU seconds of each game's process, both engines on the same machine (the reference's include JVM start,
instrumentation and JIT).

| where | games | reference s/game | native s/game | ratio |
|---|---|---|---|---|
| DoC lab and cluster machines, full corpus, 1.2.6 (mean 760 rounds) | 8388 | 85.0 | 10.8 | 7.8 |
| DoC, 1-in-10 corpus sample, 1.2.5 | 839 | 68.7 | 9.1 | 7.5 |
| clankerland: awubot against every package, both seats, DefaultSmall; fuzz pairs | 50 | 19.6 | 2.3 | 8.6 |
| clankerland, awubot mirror: DefaultSmall, 170 rounds | 1 | ~30 | 1.3 | ~23 |
| DefaultMedium, 185 rounds | 1 | ~34 | 7.0 | ~5 |
| DefaultLarge, 1198 rounds | 1 | ~85 | 20.3 | ~4 |

- Games per CPU-second on DoC: reference 0.0118, native 0.092. Per game the ratio spans 3.1 (10th percentile) to
  31.2 (90th), median 10.3; by map from 1.9 (maze1, maze3: 27 and 42 native s a game) to 45 (gridlock, averyfineline).
- Native time goes to the world's start (`GameWorld.bfsFromTarget`, the cats' BFS tables, quadratic in tiles) and to
  the collector (the translated world allocates a `MapLocation` per step and clones `Direction.values()`: 36 GB on a
  DefaultLarge game): together about 85% on DefaultMedium. The bots' own code is a small share. Those are the
  fast-path candidates ("The checks").
- Memory: a native game peaks under 1 GB (0.9 GB on DefaultLarge). A larger collector heap trades memory for little
  time (3 GB: 17.1 s instead of 20.3 s on DefaultLarge).

## Known gaps

- Wall-clock limits: the official engine resigns a team after 1200 s of its robots' wall time, and `Clock.getTimeLeft`
  / `getTimeElapsed` read wall time. The native engine never times out and reports no wall time.
- Robot stdout (`System.out`, `printStackTrace`) is not part of a game's outcome and is not compared.
- `StackOverflowError`: robots run on 16 MB stacks and never throw it; past that the game process dies (an error
  result). The official engine throws it at a depth set by HotSpot's frame sizes, which depend on the JIT, so neither
  engine reproduces such a game.
- A team class the official instrumenter refuses makes the official engine fail every later class load of that team
  (`TeamClassLoaderFactory.hasError`). The native engine ends the game with an error when a robot reaches the class.
  No corpus bot has one (the build prints any).
- Identity hashes of engine objects are not HotSpot's; no outcome in the corpus depends on them. A bot's identity
  hashes come from the instrumenter's `ObjectMethods`, so they are exact.
- Class loading by the verifier is modelled only where the corpus needs it (a sealed parent, `EnumSet`).
- CPU models: HotSpot's `Math.pow` and `Math.log` use `rcpps`, whose result depends on the CPU model. The native engine
  runs the same instruction, so both engines agree on one machine, and a game can differ between machines in both.
  Compare engines on the same machine (`imperial.py` does).
- The reference itself is not deterministic on DoC's machines: in 8 of about 18,500 reference runs there (two full
  corpus passes and a 1.2.5 sample, both seats), one robot's turn recorded a different bytecode count (once 0, once
  34, against about 7,000 to 15,000 natively) and the game went another way from that turn. On 19 Nov, 12 more in
  about 16,900 (two full passes and re-runs): each time the reference's turn did less (no move, turn, attack or
  indicator) than the native engine's, on that machine and here, with main's engine and XC's alike. Every time, the
  native run on the same machine equalled both engines run here, and the reference run again elsewhere agreed with
  it. None in about 300 reference runs here. No error was printed. One candidate: `SandboxedRobotPlayer` hands
  control between the engine's and the robot's threads with an unlooped `Object.wait()`, so a spurious wakeup lets
  a turn end early or run on. The native engine plays the intended lockstep.
- What `BcxStateHash` cannot read: its comment.
- 1.2.5 is checked on a 1-in-10 sample of the corpus, not the whole.
