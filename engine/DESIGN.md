# Native engine: design

The goal is many more games per CPU-second than the official Java engine, with exactly the official engine's games.
This file holds the measurements behind the design and the design. The rules for working on it are in `AGENTS.md`.

## 1. Where the official engine spends its time

Measured on clankerland (shared, load 12-28), engine 1.2.6 jar, Java 21.0.13, async-profiler 4.1 (cpu, 1 ms),
one game per JVM, `bc.server.robot-player-to-system-out=false`. Profiles and replays:
`~/battlecode26-data/engine/scratch/prof/`.

| game | rounds | counted bytecodes | CPU s (user+sys) | CPU per round |
|---|---|---|---|---|
| awubot vs awubot, DefaultSmall | 170 | 37.6 M | 32.0 | 188 ms |
| awubot vs awubot, DefaultMedium | 185 | 58.7 M | 34.0 | 184 ms |
| awubot_sus vs awubot, DefaultMedium | 216 | 66.6 M | 34.7 | 161 ms |
| awubot vs awubot_quals, DefaultLarge | 892 | 319.5 M | 69.1 | 77 ms |
| awubot vs awubot, DefaultLarge | 1198 | 432.5 M | 85.1 | 71 ms |

That is about 190 ns of CPU per counted bytecode. Share of CPU samples:

| where | small | medium | large (1198 r) |
|---|---|---|---|
| per-robot class linking: verification and the Rewriter (its field lookup is linear in a class's fields, and the generated classes have thousands of static fields), triggered by each robot's first calls | 54% | 50% | 55% |
| JIT compiling (each robot's classes are separate copies, compiled separately) | 26% | 25% | 17% |
| instrumenting (ASM), once per team | 12% | 11% | 4% |
| game logic | 2% | 7% | 9% |
| ⤷ of which the cats' BFS tables at game start (`GameWorld.bfsFromTarget`, quadratic in tiles) | 1.7% | | 7.7% (6.4 s) |
| bot code, compiled | 2% | 3% | 6% |
| engine calls made by bots (mostly `GameActionException` stack traces) | 1% | 2% | 3% |
| GC | 1% | 2% | 3% |
| thread switching between robots, bytecode counting, replay writing | < 1% each | | |

So the official engine spends most of its time making per-robot copies of the bots' classes ready to run, not running
them. The work a game needs (the bots' bytecodes, the world's rules) is a small part.

## 2. Options

| option | what stays slow | estimate per 1200-round large game |
|---|---|---|
| (a) C++ world, bots in an embedded JVM | per-robot class loading, linking and JIT stay (≥ 70% of the time); the world is < 10% | ≥ 60 CPU s |
| (c) Java fast engine: classes shared by robots, statics moved to a per-robot context, continuations | JVM start, JIT warm-up, the world's BFS; the measured compiled bot code alone is 5 s | ≥ 12 CPU s |
| (b) ahead-of-time translation of the bots' bytecode to C++, per-robot statics, coroutines | the bots' own work: 432 M counted bytecodes at ~1-2 ns | ~1-2 CPU s plus the world |

(b) is the only option that removes both the per-robot class costs and the JVM. The bots need a translator in any
case, and the same translator can translate the official engine's own classes. That gives the design.

## 3. Design: translate the official engine and the bots

`engine/jvm` (Java, ASM) translates JVM classes to C++; `engine/runtime` (C++) is the small native layer under them.

- **Bots.** Each team's classes are instrumented by the official jar's own instrumenter (`TeamClassLoaderFactory`),
  exactly as in a game, so every `RobotMonitor.incrementBytecodes(n)`, every method cost from `MethodCosts.txt`,
  every array-cost call and every rewritten call (`ObjectMethods.hashCode`, `InstrumentableFunctions`, the injected
  `System`) is the official one. The translator then compiles the instrumented bytecode: `incrementBytecodes` becomes
  an inline counter decrement whose slow path follows `RobotMonitor` line for line (debt carried to the next turn,
  `bytecodesToRemove`, debug levels). Preemption points are therefore exactly the official ones.
- **Per-robot state.** A bot's classes, its instrumented JDK classes (`instrumented/java/util/...`) and the
  `alwaysRedefine` classes have their statics in a per-robot block and run their `<clinit>` per robot, on first active
  use as the JVM does (and counted, as in the official engine).
- **The world.** The official jar's `battlecode.world`, `battlecode.common`, `battlecode.server.GameMaker` and what
  they use are translated too, uninstrumented. The game rules, the RNG streams, the replay writer and the collections'
  iteration orders are then the official code itself. Only `PlayerControlProvider`/`SandboxedRobotPlayer` (robot
  threads) are replaced: by a native provider that runs each robot as a coroutine on its own stack.
- **The JDK.** Library classes are translated from this machine's JDK class files (the JDK the reference runs on).
  A short list of methods tied to the VM (`System.arraycopy`, `Object.getClass`, identity hashes, `Throwable`'s
  stack traces, `Math` intrinsics) is implemented natively. `Math.pow` is HotSpot's x86-64 stub (Intel LIBM), not
  `StrictMath.pow`: the world's cheese spawn chance and some bots call it, so it is ported bit-exactly.
- **Closed world.** A rapid type analysis from the entry points decides which methods to translate. A method the
  native layer lacks fails the build with its name; a path cut on purpose (`natives.txt`: threads, reflection,
  file systems the games never reach) is fatal when reached, never silent.
- **Exceptions** are a pending-exception field checked after each call that can throw (no C++ unwinding), so a
  `GameActionException` costs what an ordinary return costs.
- **Memory**: Boehm GC (bdwgc), conservative on the coroutine stacks.
- **Builds.** One static binary per engine version holds the engine, the JDK subset, the runtime and every bot
  package (one closed world, so calls between them are direct). Generated C++ is rewritten only when it changes, so a
  changed bot recompiles its own files and relinks. Each game runs in a forked process, so every static starts
  fresh, as in a fresh JVM.
- **JVM behaviour a bot can see** is reproduced where the corpus or the fuzz bots reach it: linkage errors from the
  instrumenter's rewrites (`NoSuchMethodError` for `System.nanoTime`, `IllegalAccessError`, sealed-class failures at
  link time), `InstrumentationException`s, uncaught `Error`s ending `run()` (the robot destroyed at the end of that
  turn), HotSpot's NaN bit patterns (no constant folding of FP operations). What is not: `AGENTS.md`, "Known gaps".

The reference path is the translated official code. A hand-written fast path (for example the cats' BFS tables) may
replace a translated method only behind the differential check in `AGENTS.md`.

## 4. Measured result

The full corpus (8388 games: awubot against itself, its 4 older snapshots and 9 other teams' bots on all 150 maps,
both seats, two seeds; the 4 fuzz bots on 6 maps) plays the reference's games: for every game a reference run and a
native run agree in result, every round's state hash, the replay file and every result field. The only
disagreements were reference runs that disagreed with the reference itself on other machines (`AGENTS.md`, "Known
gaps"). On DoC's machines a game costs 10.8 CPU s natively against 85.0 for the official engine (7.8×; median per
game 10.3×, from about 2× on large mazes to 45× on small maps).

The estimate in §2 assumed the world would stay a small share. It does not: translated, the world's start (the
cats' BFS tables, quadratic in tiles) and the collector behind its allocations take most of the native time, where
the JVM's compiled code and young-generation collector made them cheap (the BFS tables were 6.4 s of an 85 s
DefaultLarge game, §1). The per-robot class costs and the JVM are gone as planned. Details: `AGENTS.md`, "Speed".
