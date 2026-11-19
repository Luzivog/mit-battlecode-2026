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

A spec is one JSON object a line (JSONL); unknown fields are an error.

| field | | meaning |
|---|---|---|
| `a`, `b` | required | team A's and team B's package (`src/<pkg>`) |
| `map` | required | map name: `maps/<map>.map26`, else the jar's bundled map |
| `seed` | optional int | game seed; absent: the map's embedded seed |
| `engine` | optional | `1.2.6` (default) or `1.2.5` |
| `classes` | optional | the compiled classes dir; default `build/classes` of the checkout |
| `classes_a`, `classes_b` | optional | per-team classes dir, overriding `classes` |
| `id` | optional | copied to the result |
| `cat_rng_skip` | optional int ≥ 0 | the cat RNG starts this many draws after its fresh state (default 0; "Conditions") |

A result is one JSON object a line, in spec order:

| field | meaning |
|---|---|
| `id`, `a`, `b`, `map`, `engine` | from the spec |
| `seed` | the seed the game played (the map's when the spec has none) |
| `winner` | `A` or `B` |
| `winner_pkg` | the winner's package |
| `win_type` | `KILL_ALL_RAT_KINGS`, `MORE_POINTS`, `MORE_ROBOTS_ALIVE`, `MORE_CHEESE`, `WON_BY_DUBIOUS_REASONS`, `RESIGNATION` |
| `round` | the last round played |
| `hash` | 16 hex digits: `BcxStateHash.chain()` fed each round's number then its state hash, in round order, then `finish()` (the 64-bit mixer of `BcxStateHash`, see its comment); the per-round hashes are `hashes.txt` |
| `cat_rng_skip` | from the spec, when it has one |
| `cat_rng_draws` | draws from the cat RNG during the game |
| `backend` | `native` or `java` |
| `cpu_s` | the process's CPU seconds for the game |
| `error` | present only when the game could not be played; then no outcome fields |

## The checks

- **`test/agreement.py`** plays specs on both engines and compares the result, every round's state hash and the
  replay bytes. Quick mode (a few games, seconds of native time) for every change; `--full` for the whole corpus.
- **The corpus** (`test/corpus.py`): awubot against itself and against `awubot_{quals,us,mystery,sus}` and every
  `opp_*`, both seats, on every map in `maps/` at the map's seed and one more seed; the fuzz bots (`src/fuzz_*`)
  against each other and against awubot, on maps that make every call, refusal and exception happen.
- **The fuzz bots** call every `RobotController` method with legal and illegal arguments, catch and leak exceptions,
  overrun the bytecode limit mid-statement, fail asserts, resign, disintegrate, and use the JDK features the corpus
  uses (collections, strings, boxing, lambdas, records, switches, `Math`, `Random`).
- **Fast paths.** A hand-written replacement for a translated method lands only when the corpus agrees and a
  differential build (`BCX_CHECK_FASTPATH=1`) runs the translated original beside the fast path on every call over the
  corpus and finds no difference in results or state. The original stays in the build behind that flag.

## Build, play, test

```sh
./gradlew classes                                  # the bots' class files (build/classes)
.venv/bin/python engine/build.py                   # engine/build/bcx: engine 1.2.6 and every bot package
.venv/bin/python engine/build.py --engine 1.2.5    # engine/build/bcx-1.2.5
.venv/bin/python engine/build.py --bots awubot,opp_obg   # only these bots (faster)
engine/build/bcx play SPECS.jsonl OUTDIR [-j N] [--no-replay] [--dump]   # native games
.venv/bin/python engine/play_java.py SPECS.jsonl OUTDIR [-j N] [--dump]  # the same games on the official jar
.venv/bin/python engine/test/agreement.py --quick  # both engines, compared (--full: the whole corpus)
```

- `bcx play` writes `OUTDIR/<id or index>/result.json`, `hashes.txt`, `replay.bc26` (`--dump`: `dump.txt`) and prints
  the result lines in spec order. Each game runs in a forked process, so every static starts fresh. It refuses a
  spec whose bot class files differ from the ones compiled in (rebuild).
- `build.py` fetches the jars and bdwgc once into `~/battlecode26-data/engine/`, compiles `engine/jvm`, translates
  into `~/battlecode26-data/engine/build/gen-VERSION/` and compiles with ninja (`-j`, default 8). Generated files
  are rewritten only when they change, so a rebuild recompiles only what moved. Needs clang, ninja, cmake, a JDK 21,
  zlib. Every bot of the corpus compiled is a few minutes of 8 cores; one bot changed, about a minute.

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

Filled in when the agreement test passes.

## Known gaps

- Wall-clock limits: the official engine resigns a team after 1200 s of its robots' wall time, and `Clock.getTimeLeft`
  / `getTimeElapsed` read wall time. The native engine never times out and reports no wall time.
- Robot stdout (`System.out`, `printStackTrace`) is not part of a game's outcome and is not compared.
