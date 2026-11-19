# engine/

A native build of the official Battlecode 2026 engine that plays exactly the official engine's games with the same
Java bots, many times faster. Why it is built this way: `DESIGN.md`. Exactness fails silently and costly, so every
change here passes `engine/test/agreement.py --full` before it lands ("The checks").

## Exactness

A game is fixed by its spec (below). For every game of the corpus, the native engine must give what the reference
gives:

1. **The result**: winner, win type (`DominationFactor`) and final round.
2. **The state hash of every round**: `battlecode.world.BcxStateHash` (in `jvm/src`), computed after each
   `GameWorld.runRound()` over the whole world: every robot in execution order with all its state, every tile, every
   trap, both teams' info, the cats' state, the cheese mines, the shared arrays, the squeak queues and the world RNG.
   Its comment lists what it covers and what it cannot read.
3. **Every robot's bytecodes on every turn**, and every action: the replay (`Turn.bytecodesUsed`, the actions).
4. **The replay bytes**: the gunzipped `.bc26` flatbuffer, byte for byte (the gzip container may differ).

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
- **Status** (19 Nov 2026, engine 1.2.6): the full corpus, 8388 games on Imperial, 8387 agree in all four checks;
  the other is the reference's own divergence ("Known gaps"). 1.2.5: 839 of 839 (a 1-in-10 sample). Quick 5/5,
  `refmain_test` 13/13, `conformance` 5/5.
- **Fast paths.** A hand-written replacement for a translated method lands only when the corpus agrees and a
  differential build (`BCX_CHECK_FASTPATH=1`) runs the translated original beside the fast path on every call over the
  corpus and finds no difference in results or state. The original stays in the build behind that flag.

## Build, play, test

```sh
./gradlew classes                                  # the bots' class files (build/classes)
.venv/bin/python engine/build.py                   # engine/build/bcx: engine 1.2.6 and every bot package
.venv/bin/python engine/build.py --engine 1.2.5    # engine/build/bcx-1.2.5 (--engine all: both)
engine/build/bcx play SPECS.jsonl OUTDIR [-j N] [--no-replay] [--dump]   # native games
.venv/bin/python engine/play_java.py SPECS.jsonl OUTDIR [-j N] [--dump]  # the same games on the official jar
.venv/bin/python engine/test/agreement.py --quick  # both engines, compared, here (--specs FILE: any specs)
python3 engine/test/imperial.py run --full         # the whole corpus on Imperial, both engines in each task
python3 engine/test/imperial.py report RUN         # its agreement and CPU time, from runs/RUN/rows.jsonl
.venv/bin/python engine/test/refmain_test.py       # RefMain against battlecode.server.Main
.venv/bin/python engine/test/conformance.py        # translated test programs against the JVM
```

- `bcx play` writes `OUTDIR/<id or index>/result.json`, `hashes.txt`, `replay.bc26` (`--dump`: `dump.txt`) and prints
  the result lines in spec order. Each game runs in a forked process, so every static starts fresh. It refuses a
  spec whose bot class files differ from the ones compiled in (rebuild). It reads `maps/` of the checkout it was
  built in (`--root REPO` or `BCX_ROOT` for another), then the jar's maps (`engine/build/maps-VERSION/`).
- `build.py` fetches the jars and bdwgc once into `~/battlecode26-data/engine/`, compiles `engine/jvm`, translates
  into `~/battlecode26-data/engine/build/gen-VERSION/` and compiles with ninja (`-j`, default 8). Needs clang, ninja,
  cmake, a JDK 21, zlib. The binary is static, so it runs on any x86-64 Linux node.
- Builds are incremental: method files (`m_MODULE_N.cpp`) and headers (`types.h` for the engine and the JDK,
  `types_PKG.h` per bot) are per module and rewritten only when they change. Measured on 8 cores: everything,
  6 to 8 min (about 25 CPU-min); nothing changed, 38 s (the translation); one bot changed, 43 s, when the change
  reaches no JDK code no other bot reaches (else the engine files that change recompile too).
- `imperial.py run` ships `engine/build/dist` (the stripped binaries, the jar's maps, the jars) with `tools/run --kit`;
  a task plays its specs on both engines and keeps both engines' files only for games that disagree. Give games whose
  reference JVM needs more than 1.7 GB `-- --task-mem 6000M` (the JVM peaks at 4.5 GB on `mining` with
  awubot_sus: per-robot class copies).

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
| DoC lab machines, full corpus, 1.2.6 (mean 760 rounds) | 8372 | 86.6 | 11.1 | 7.8 |
| DoC, 1-in-10 corpus sample, 1.2.5 | 839 | 65.9 | 8.1 | 8.1 |
| clankerland, `medium.jsonl` (50 games, mostly DefaultSmall) | 50 | 18.7 | 2.2 | 8.3 |
| clankerland, awubot mirror: DefaultSmall, 170 rounds | 1 | ~30 | 1.3 | ~23 |
| DefaultMedium, 185 rounds | 1 | ~34 | 7.0 | ~5 |
| DefaultLarge, 1198 rounds | 1 | ~85 | 20.3 | ~4 |

- Games per CPU-second on DoC: reference 0.0115, native 0.090. Per game the ratio spans 3.2 (10th percentile) to
  31.7 (90th), median 10.5; by map from 1.9 (maze1, maze3: 27 and 42 native s a game) to 45 (gridlock, averyfineline).
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
- The reference itself: in one game of 8388 (`cups~awubot~opp_obg~s1021211706` on lab machine gpu16) the official
  engine played differently from the official engine everywhere else (from round 58: one king skipped a spawn); the
  native engine and 7 other runs of the reference agree. Unexplained. One candidate: `SandboxedRobotPlayer` hands
  control between threads with an unlooped `Object.wait()`, so a spurious wakeup breaks the lockstep.
- What `BcxStateHash` cannot read: its comment.
- 1.2.5 is checked on a 1-in-10 sample of the corpus, not the whole.
