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

**Conditions.** One game per fresh JVM. The official engine's cat RNG is a static `Random(1092)` that carries over
between games in one JVM, so several maps in one JVM (`-Pmaps=a,b`) play differently from one map per JVM; the
native engine plays one game per JVM's worth of state (every engine static starts fresh) unless a spec asks for a
`jvm_game` index (not built: a known gap).

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

A result is one JSON object a line, in spec order:

| field | meaning |
|---|---|
| `id`, `a`, `b`, `map`, `engine` | from the spec |
| `seed` | the seed the game played (the map's when the spec has none) |
| `winner` | `A` or `B` |
| `winner_pkg` | the winner's package |
| `win_type` | `KILL_ALL_RAT_KINGS`, `MORE_POINTS`, `MORE_ROBOTS_ALIVE`, `MORE_CHEESE`, `WON_BY_DUBIOUS_REASONS`, `RESIGNATION` |
| `round` | the last round played |
| `hash` | hex SHA-256 over the per-round state hashes, in order |
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

Filled in as they land.

## Speed

Filled in when the agreement test passes.

## Known gaps

- Wall-clock limits: the official engine resigns a team after 1200 s of its robots' wall time, and `Clock.getTimeLeft`
  / `getTimeElapsed` read wall time. The native engine never times out and reports no wall time.
- Robot stdout (`System.out`, `printStackTrace`) is not part of a game's outcome and is not compared.
