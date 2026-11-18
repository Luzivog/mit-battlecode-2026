# Testing, evaluation & tooling

For AI agents improving `src/challenger` against the frozen baseline `src/awubot`. Run everything from the repo
root (or your worktree root). Python is `.venv/bin/python`. Java 21. Engine pinned to **1.2.5**
(`engine_version.txt`); do **not** run `./gradlew update`.

## 0. Cheat sheet

| Purpose | Command | Cost (this box, shared load) |
|---|---|---|
| Regenerate challenger `.java` from `.jinja2` | `.venv/bin/python jinja.py` | seconds |
| Compile every bot package | `./gradlew classes` (or `./gradlew build -x test`) | ~45 s cold, ~2 s no-op |
| One match + replay | `./gradlew runWithoutBuild -PteamA=challenger -PteamB=awubot -Pmaps=DefaultSmall -Pseed=158` | 20-90 s |
| Is my challenger better than main's (both against awubot)? | `tools/games ab challenger awubot --pool POOL`, then `tools/games wait ID` (docs/evaluation.md) | sequential; cached games are free; `--remote` for Imperial |
| Play any list of games | `tools/play SPECS.jsonl OUTDIR --workers 8` | ~13 games/min on 8 workers (§7a) |
| Submission zip + official verify | `scripts/check_submission.sh challenger` | ~15 s |

`autoscrims/ABtest.py` (§7) is superseded by `tools/games`: the same games on the unmodified engine, ~1.3x faster
per worker (§7a), with a sequential test, a cache and exact reruns.

## 1. What actually runs

`gradle` -> JavaExec `battlecode.server.Main -c=-` with `-Dbc.*` properties -> engine loads `maps/<map>.map26`
(fallback: map bundled in the engine jar) -> per team a `TeamClassLoaderFactory` instruments the team's classes
(bytecode counting; instrumented bytes cached per team, classes re-defined per robot so statics are per robot;
`CellTracker.class` alone is 3.4 MB) -> rounds run sequentially, robots act in spawn order ->
stdout gets `[server] <pkg> (A|B) wins (round N)` + `[server] Reason: ...`; replay (gzip flatbuffer) to the save file.

**All of `src/` is one Gradle source set**: every package (`awubot*`, `challenger`, `afk`, `suicide`, `opp_*`,
`battlecode/**`) compiles together into `build/classes`. One compile error anywhere breaks every match and every A/B.

## 2. build.gradle reference

Tasks (`./gradlew tasks --all`):

| Task | What it does | Notes |
|---|---|---|
| `build` | compileJava + test + jar + `unpackClient` (+ `checkNewVersion` network call, prints a "NEW VERSION 1.2.6" banner: ignore) | slow path; use `classes` for compile only |
| `classes` / `compileJava` | compile `src/**` to `build/classes` | |
| `run` | `dependsOn build`, then one match | no `-Xmx` (JVM default = 1/4 RAM) |
| `runWithoutBuild` | same match, `-Xmx350m -XX:+UseSerialGC`, websocket off | still runs `compileJava` (classpath dependency), skips test/jar/client |
| `verify -Pteam=<pkg>` | `battlecode.instrumenter.Verifier` over **all** of `build/classes` | what the official compile server runs; fails in this repo because of `battlecode.*` (see 3) |
| `zipForSubmit [-Pteam=<pkg>]` | `submission.zip` with only `src/<pkg>/**/*.java` (default `teamA`) | patched, see 9 |
| `listMaps`, `listPlayers` | list bundled+`maps/` maps, packages with a RobotPlayer | |
| `version`, `checkNewVersion`, `update` | version info / bump `engine_version.txt` | never `update` |
| `unpackClient` | download/unzip the Electron client to `client/` (280 MB) | GUI only, useless headless |
| `test` | JUnit in `test/` (only a `1+1==2` sanity test) | |

Properties (`-P...`, defaults in `gradle.properties`):

| Property | Default | Effect | Changes outcome? |
|---|---|---|---|
| `teamA`, `teamB` | challenger, awubot | packages; team A's king acts before B's on 40/43 official maps | yes |
| `maps` | DefaultSmall | comma list runs several games in one JVM | yes (see 4) |
| `seed` | **0** (build.gradle `?: 0`) | game seed (only because of the overrides, see 3) | yes |
| `replay` | `matches/<A>-vs-<B>-on-<maps>.bc26` | replay path (overwritten each run, no seed in name) | no |
| `classLocationA/B`, `packageNameA/B` | build/classes, team name | load a team from another class dir (e.g. a snapshot) | - |
| `debug` | true | executes `debug_*` methods; bots have none | no |
| `outputVerbose` | true | robot `System.out` to stdout; bots print nothing | no |
| `showIndicators` | true | indicators into replay; challenger emits none | no |
| `validateMaps` | true | `assertIsValid()` on load | no |
| `alternateOrder` | false | with several maps, swap A/B spawns every other map | yes |
| `waitForClient`, `enableProfiler` | false | client sync / per-method profiler | no |
| `compatibilityClient` | true | Electron instead of Tauri client | - |
| `onSaturn`, `gcloudToken` | - | official-server repo access | - |

`-Dbc.server.map-path=maps` in build.gradle is a no-op; the engine reads `bc.game.map-path` (default `maps`,
relative to the cwd), then falls back to `battlecode/world/resources/<map>.map26` inside the engine jar.

## 3. Engine overrides `src/battlecode/{server/Main,server/Server,world/GameMapIO}.java`

Added by the team in commit `65b4d34` ("alleged tester"). Diff vs engine 1.2.5 sources (tag `engine.1.2.5` of
github.com/battlecode/battlecode26):

| File | Change |
|---|---|
| `GameMapIO` | `loadMap(int seed, ...)`: the `LiveMap` seed is the passed seed instead of the map file's `randomSeed` |
| `Server` | passes `options.getInt("bc.game.seed")` to `loadMap`; prints `[server] Seed: N` |
| `Main` | reads `bc.game.seed` (unused local) |

- **They do shadow the engine at runtime**: runtime classpath order is `build/classes`, `build/resources/main`,
  `battlecode26-java-1.2.5.jar`, `scala-library-2.11.7.jar`. Proof: the `[server] Seed:` line only exists in the
  override. Bots cannot see them (team class loaders refuse `battlecode.*` from team URLs).
- **Only effect: the game seed becomes `-Pseed` (default 0) instead of the map's embedded seed.** Verified:
  official-jar-first run of DefaultSmall and override run with `-Pseed=158` (= DefaultSmall's embedded seed) give a
  byte-identical replay. So `-Pseed=<embedded seed>` (table in 5) or `ABtest.py --engine official` reproduces
  official-engine games exactly; any other seed is a *different but equally legal* game (different cheese spawns,
  robot IDs, bot RNG).
- **Fairness**: both bots always run on the same engine and seed, so the overrides favour neither side. They only
  widen the sample space beyond the 1 game/map/side the official engine allows.
- **Submission hazard**: the official compile server runs `verify` over all compiled classes; any `battlecode.*`
  class is rejected as ILLEGAL. Never ship `src/battlecode` (the patched `zipForSubmit` doesn't).
- Override files are identical in 1.2.6, so they keep compiling if the engine is bumped.

### Engine versions

| Version | Status | Difference |
|---|---|---|
| 1.2.5 (pinned) | what awubot was developed/tested on | `spawnCheese` offsets in [-4,+3] and wrong mirror axis on reflection-symmetric (H/V) maps -> cheese not symmetric there |
| 1.2.6 (public latest, 2026-01-28) | only `GameWorld.spawnCheese` fixed ([-4,+4], correct mirroring); RNG stream diverges | jar: `https://releases.battlecode.org/maven/org/battlecode/battlecode26-java/1.2.6/battlecode26-java-1.2.6.jar` |
| 1.2.7.private2 (official servers incl. final tournament, per `api.battlecode.org/api/episode/e/bc26/`) | engine code == 1.2.6 (only bundled maps deleted) | |

22 of the 43 official maps are H/V-symmetric, so on 1.2.5 side matters more there; always play both sides.

## 4. Determinism and randomness

A game is a pure function of (team A code, team B code, map, seed, engine version), with these caveats:

| Source | Seeded by | Notes |
|---|---|---|
| `GameWorld.rand` (cheese spawn chance + position) | game seed | differs per seed |
| `IDGenerator` (robot IDs) | game seed | IDs feed bot RNG |
| Bot `Rng.seed()` | `rc.getID()`, round, location | so bots vary with the seed; `new Random()` / `Math.random()` in bots are also seeded per robot |
| Cat movement (`InternalRobot.rand`, only used when a cat has no direction or is stuck) | **static `new Random(1092)`**, not the game seed | deterministic in a fresh JVM, but **continues across games when one JVM runs several maps** (`-Pmaps=a,b`), so game 2+ differs from running it alone whenever an earlier game drew from it (sunandmoon after t_minus_five_minutes: round 125 instead of 99). Many games draw (`cat_rng_draws` in tools/play results: 0-40). `tools/play` resets it to `new Random(1092)` before each game, which makes every game equal to a fresh JVM's (§7a). |
| Final tiebreak `WON_BY_DUBIOUS_REASONS` | unseeded `Math.random()` in the engine | only after points, cheese, robots-alive all tie; not observed |
| Team time limit (`MAX_TEAM_EXECUTION_TIME` = 1200 s wall per team per game) | wall clock | exceeding -> auto-resign; irrelevant unless a box is absurdly overloaded |

Measured: same (teams, map, seed) twice -> byte-identical replays (gradle vs direct java, indicators on/off, JIT
flags). Different seeds -> different games (cheese spawns + robot IDs + bot RNG all change; DefaultSmall: seed
158 ends round 135, seed 347712782 round 159). So, as the rules notes say: locally (`-Pseed`, default 0) cheese
spawns differ from the official ones unless you pass the map's embedded seed; and on top of that 1.2.5 != 1.2.6.

**Sides**: maps are symmetric but turn order is not (spawn order, team A's initial bodies first), and on 1.2.5 cheese
isn't mirrored on H/V maps. Every map/seed must be played both ways. Because challenger == awubot today, every pair
is split 1-1 (the same side wins both games); a non-1-1 pair is the first sign of a real behaviour change (or of
nondeterminism).

## 5. Maps

`maps/` (150): the 43 official maps (byte-identical to those bundled in the 1.2.5 jar), 29 team-made maps and 78
recovered tournament maps.
`test_maps/` (21) = byte-identical copy of default+sprint1 (the original `run.py` pool). The engine only reads
`maps/`, then the jar; `--maps-dir` in the scripts only chooses *names*.

Official maps (size, symmetry R/H/V, embedded seed = what the official engine uses):

| Batch | Maps |
|---|---|
| default (5) | DefaultSmall 30x30 R 158, DefaultMedium 45x45 R 977, DefaultLarge 60x60 R 370, evileye 20x20 R 886, cheeseguardians 60x60 H 987 |
| sprint 1 (16, Jan 12) | arrows 30x30 R 519, cheesefarm 30x30 H 495, dirtfulcat 30x30 V 888, dirtpassageway 50x50 V 247, keepout 50x50 V 919, Meow 50x30 H 225, Nofreecheese 60x60 R 557, pipes 50x34 V 992, popthecork 30x40 V 136, rift 60x30 R 182, sittingducks 50x25 V 518, starvation 35x25 V 968, thunderdome 45x35 R 251, trapped 50x50 H 374, wallsofparadis 60x60 R 566, ZeroDay 40x34 R 175 |
| sprint 2 (22, Jan 19) | 5t4rv4t10n_1337 21x21 H 443, averyfineline 20x20 R 822, averystrangespace 60x60 R 452, canyoudig 20x20 R 978, cheesebottles 59x20 R 923, closeup 30x30 R 807, corridorofdoomanddespair 20x60 H 503, EscapeTheNight 45x50 V 945, hatefullattice 50x41 H 365, jail 60x40 H 736, knifefight 40x40 V 466, mercifullattice 41x35 R 22, minimaze 34x46 R 239, peaceinourtime 35x50 V 529, safelycontained 60x60 V 856, streetsofnewyork 60x60 V 668, TheHeist 60x40 H 873, tiny 20x20 V 36, toomuchcheese 30x30 R 377, uneruesansfin 60x60 R 811, whatsthecatdoin 40x40 R 644, whereisthecheese 20x20 R 621 |

Team-made (local only, not official): `*Catless` (Default{Small,Medium,Large}, arrows, bridges; same map with
cats walled into corners), bridges, checkerboard, cups, far, hidden, maze1-3, messy, mining, quadrants,
micro1/2, microtest(_wide), pvparena(_mines, _mines2) (combat sandboxes), testpathfinding(2),
`pathfinding test` (name has a space; breaks space-separated `--maps`), coop, troll1/2 (degenerate; skipped by
ABtest's directory listing together with 5t4rv4t10n_1337).

Tournament maps: 78 more, recovered from tournament replays (intel.md §7). The 31 final-tournament maps were then
replaced by the official repo's files (github.com/battlecode/battlecode26 @103abf6b). The replay copies wrote out the
facing the engine computes for `CENTER` (equivalent) and 12 had their teams swapped (alternate-order games).
`maps/arrows` is byte-identical to the official repo's and both jars'. The 47 other recovered maps have no official
source, so some may have A and B swapped (playing both seats makes that a relabelling). Pools, including the sealed
holdout drawn from the tournament maps: docs/evaluation.md.

## 6. Script inventory

| Script | Purpose | Status |
|---|---|---|
| `tools/games`, `tools/play` | **The evaluation tools**: paired seats, GSPRT, cache, gauntlet, diff, replay; the game runner | docs/evaluation.md; §7a |
| `autoscrims/ABtest.py` | the previous A/B tool. Both sides per map/seed, parallel, binomial + sign test, JSON | superseded by tools/games (section 7) |
| `autoscrims/ABtest_old.py` | older ABtest (default maps `test_maps/`; hangs forever after a failed game) | superseded |
| `run_all_maps.py` | `python run_all_maps.py A B [--maps-dir maps] [--workers N] [--seed S]`: all maps in a dir x both sides via `./gradlew run -x build`, prints `Wins: {...}` | works; no stats; needs a Gradle daemon per worker |
| `run.py` | hard-coded teams (`awubot_microtrapless` vs `awubot_microimprov`, which no longer exist) and maps, 16 procs, psutil CPU diagnostics, logs to `match_stdout/` (not gitignored) | edit constants to use; prefer ABtest |
| `autoscrims/scrim_script.py` | requests unranked scrims via api.battlecode.org (`BC_USERNAME`/`BC_PASSWORD` in `.env`) | dead: episode archived 2026-02-02; never run |
| `autoscrims/teams.json` | cached team list from **bc25java** | unused, stale |
| `autoscrims/gen.py`, `autoscrims/test.py` | prints MapLocation offsets / repo path | scratch |
| `check.py` | lint: `rc.move`/`rc.turn` only allowed in `Game.java` (run inside a package dir) | stale: flags `KingSpawningStrategy.java:90` even in awubot |
| `scripts/check_submission.sh` | zip one package, check 5 MiB limit, `verify` it in a fresh scaffold like the official compile server | new |
| `tools/radius.cpp`, `rad.exe`, `install-gpp.ps1` | prints a vision-radius ASCII disc; Windows g++ installer | irrelevant |
| `test/examplefuncsplayer/RobotPlayerTest.java` | JUnit `1+1==2` | irrelevant |
| `jinja.py` | template codegen (`--package`, `--watch`); refuses to regenerate awubot | see `docs/codegen.md` |

## 7. ABtest.py (A/B pipeline; superseded by tools/games)

```
.venv/bin/python autoscrims/ABtest.py P1 P2 [--pool official|sprint1|quick | --maps a,b,c | --maps-dir DIR]
    [--reps N] [--seed S | --seed-base S] [--engine override|official] [--workers N] [--timeout SEC]
    [--runner java|gradle] [--no-build] [--show-indicators]
```

- Each map entry (map x rep) gets one seed and is played twice: P1 as team A, then P1 as team B.
- `--seed-base S` makes the per-entry seeds reproducible (the JSON records `seed_base` and every seed);
  `--seed S` uses one seed everywhere; `--engine official` = unmodified engine jar first on the classpath, seed from
  the map file (exactly the official server's game; only `--reps 1`).
- Default `--runner java`: one `./gradlew -q classes` + classpath query, then **copies `build/classes` to a temp
  snapshot** and runs `java ... battlecode.server.Main` per game. No Gradle daemons per worker, and edits/rebuilds
  during a long run cannot leak into it. `--runner gradle` is the original `./gradlew runWithoutBuild` path.
- Pools: `official` (43), `sprint1` (21 = `test_maps/`), `quick` (12: DefaultSmall, DefaultMedium, evileye,
  arrows, whereisthecheese, cheesefarm, jail, TheHeist, pipes, sittingducks, knifefight, tiny; 5 R / 3 H / 4 V,
  3 points-decided, ~70 s/game), `custom` (25 team-made maps, holdout). Without `--pool`/`--maps` it lists
  `--maps-dir` (default `maps/`, 68 after skipping troll1/2, coop, 5t4rv4t10n_1337); explicit lists are not filtered.
- `--timeout SEC` kills a stuck game (recorded as a failure). `--no-build` skips compiling but still snapshots.
- Output: live table (TTY) or final table (non-TTY, 160 cols). Slot `+KILL` = P1 won by killing all kings,
  `-PTS` = P1 lost on points; conditions KILL, PTS, CHEESE, ALIVE, RNG, RESIGN. Then a plain summary:

```
== A/B summary: challenger vs awubot | pool 'official' (43 maps) ==
challenger: 43/86 games = 50.0% (95% CI 39.7-60.3%), two-sided binomial p = 1
pairs (same map+seed, both sides): won 0 / split 43 / lost 0, sign-test p = 1
challenger as team A: 22/43, as team B: 21/43 | team A won 44/86
challenger wins by: KILL 32, PTS 11 | losses by: KILL 32, PTS 11
wall 1095s with 8 workers = 12.7s/game; match mean 90.1s, max 292.7s
failures: 0
results: .../match_results/abtest_2026-11-18_19-30-32.txt and .../abtest_2026-11-18_19-30-32.json
```

JSON: top-level run metadata (`runner`, `engine`, `engine_version`, `maps`, `reps`, `seed_base`, ...), `summary`
(all numbers above), `matches[]` (`map_name`, `seed`, `team_a`, `team_b`, `winner`, `condition`, `round`,
`duration_s`, `replay_name`, `reverse`), `failures[]`. It is rewritten after every game, so it can be polled.

- Stats: exact two-sided binomial test on games vs 50%, Wilson 95% CI, and an exact sign test on map pairs
  (2-0 vs 0-2, splits ignored). The pair test removes the side effect and is the better primary statistic.

### Measured results and throughput (2026-11-18; 16 cores / 62 GB box shared with other agents)

| Run | Games | Workers | Wall | Throughput | Result |
|---|---|---|---|---|---|
| `--pool official --engine official` | 86 | 8 | 1103 s | 12.8 s/game | challenger 43/86 = 50.0% (CI 39.7-60.3), p = 1; pairs 0 won / 43 split / 0 lost; team A won 44/86; wins KILL 32, PTS 11; mean game 90 s (load 17-50) |
| `--pool quick --reps 2 --seed-base 1` | 48 | 12 | 294 s | 6.1 s/game | 24/48 = 50.0%, pairs 0/24/0, p = 1; team A won 26/48; mean game 62 s (load 8-30) |

Identical code mirrors exactly: every map is 1-1 and both games of a pair end on the same round with the same
side winning. On 1.2.5 the winning side is A on 22 maps and B on 21.

Game length (official run): 68-2000 rounds (mean 875; 9 maps reach round 2000 and are decided on points);
wall 21-293 s per game under load (median 58 s, mean 90 s). Slowest: ZeroDay, minimaze, safelycontained, rift,
cheesebottles, sittingducks (all >170 s).

Per-game time breakdown (sequential runs, load 5-10, DefaultSmall seed 158 = 135 rounds, 17.6 s total):

| Stage | Time |
|---|---|
| JVM start + engine init + map load (`suicide` vs `suicide`) | 0.5 s |
| + instrument one team's classes + its first turn (`challenger` vs `suicide`) | +1.2 s |
| simulation | the rest (~15 s, ~0.1 s/round; scales with robot count; >90% of every game) |
| `./gradlew runWithoutBuild` instead of direct java (warm daemon) | +3-7 s; +15 s if a new daemon must start |
| `debug`, `outputVerbose`, `showIndicators`, `validateMaps` all on vs off | within run-to-run noise (+-10%); replays byte-identical (bots print/draw nothing) |
| replay writing | cannot be disabled (save file required); 0.06-1.2 MB gzip per game, negligible time |
| `-XX:TieredStopAtLevel=1` | same wall, ~20% less CPU; not adopted |

Resources: each game JVM uses 0.5-1.6 GB RSS (1 GB heap cap + metaspace: every robot re-defines the bot classes)
and ~1.1-1.2 cores. CPU is the limit: ~12-14 workers on an idle 16-core box, memory would allow ~30. On the shared
box use 8 (or `nproc` minus current load). Wall time ~= sum(game time) / workers.

Several maps per JVM: `-Pmaps=a,b,c` runs them sequentially in one JVM (one replay, one `wins` line each, teams
fixed; `alternateOrder=true` swaps spawns every other map). It saves only the ~2 s startup per game and leaks the
static cat RNG between games, so ABtest uses one JVM per game.

Direct invocation without Gradle (what `--runner java` builds; put the engine jar *first* for the official engine):

```bash
G=$HOME/.gradle/caches/modules-2/files-2.1
CP=build/classes:$G/org.battlecode/battlecode26-java/1.2.5/7650c727049f89d3fd4ef6cc4b2da5972e046961/battlecode26-java-1.2.5.jar:$G/org.scala-lang/scala-library/2.11.7/f75e7acabd57b213d6f61483240286c07213ec0e/scala-library-2.11.7.jar
java -Xmx1g -XX:+UseSerialGC \
  --add-opens=java.base/jdk.internal.misc=ALL-UNNAMED --add-opens=java.base/jdk.internal.math=ALL-UNNAMED \
  --add-opens=java.base/jdk.internal.util=ALL-UNNAMED --add-opens=java.base/jdk.internal.access=ALL-UNNAMED \
  --add-opens=java.base/sun.security.action=ALL-UNNAMED \
  -Dbc.server.mode=headless -Dbc.server.websocket=false -Dbc.server.wait-for-client=false \
  -Dbc.server.robot-player-to-system-out=false -Dbc.engine.show-indicators=false \
  -Dbc.game.team-a=challenger -Dbc.game.team-b=awubot \
  -Dbc.game.team-a.url=build/classes -Dbc.game.team-b.url=build/classes \
  -Dbc.game.maps=DefaultSmall -Dbc.game.seed=158 -Dbc.server.save-file=/tmp/x.bc26 \
  -cp "$CP" battlecode.server.Main -c=-
```

(The jar hash dirs are from this machine's Gradle cache; `ABtest.py` asks Gradle for the classpath instead.)

## 7a. tools/play: the game runner

`tools/play` (code: `tools/bcgames/`) plays games with the **unmodified official jar** (1.2.6 or 1.2.5, sha256-pinned,
downloaded to `~/battlecode26-data/games/engines/`). There is no Gradle and no `src/battlecode` on the classpath. Each
worker is a JVM running `bcplay.Play`, which plays the games sent on its stdin one after another. Each game repeats
`battlecode.server.Server.run()`/`runMatch()` for one map, in the same order.

**Many games per JVM, each equal to a fresh JVM's game.**
- The engine's only state that outlives a game is the static cat RNG (§4). `Play` resets it to `new Random(1092)`
  before each game.
- Every other static is per robot (the instrumenter redefines `ObjectMethods`, `RobotMonitor`, `System`, `Clock` and
  the bots' classes per robot) or constant. The engine's hashed collections are keyed by `Integer` or only looked up.
- Verified, byte-identical replays:
  - `Play` vs `battlecode.server.Main`;
  - cheesefarm, whatsthecatdoin, DefaultSmall, evileye and tiny in sequence in one JVM vs each alone;
  - sunandmoon after t_minus_five_minutes vs alone. Without the reset it ends at round 125 instead of 99;
  - the same 32 games under three JVM flag sets and with the box under full load.
- `tools/bcgames/play_test.py` keeps checking the official-Main equality and the sequence equality.

**Seeds without the override.** A seed is written into a cached copy of the map file: the 4 bytes of
`GameMap.randomSeed`, present in all 150 maps. The unmodified 1.2.5 jar with a variant gives a replay byte-identical
to the override engine with `-Pseed` (cheesefarm 7, whatsthecatdoin 12345, DefaultMedium 99).

**Bots** compile alone (`javac -g -encoding UTF-8 -source 21 -target 21`, Gradle's flags) into a deterministic jar
per source tree. The class files are byte-identical to `./gradlew classes`. A jar as team URL also skips the file stat
the engine makes per class lookup on a directory.

**Where a game's time goes** (async-profiler, cheesefarm 756 rounds, awubot vs awubot_us):

| | default JVM | `-XX:-BytecodeVerificationRemote` |
|---|---|---|
| class load and link (each robot defines its own copy of every bot class) | 54% (verify 24%, `Rewriter` field lookup 19%) | 55% (`Rewriter` field lookup 38%) |
| JIT compilation (each robot's classes compile separately) | 20-26% | 20% |
| instrumentation (ASM), file lookups | 6-12% | 6% |
| game logic, bot code, GC, replay | ~10% | ~15% |

The `Rewriter` cost is JDK 21's linear field search for each `putfield`/`putstatic` in CellTracker's thousands of
fields. No JVM flag avoids it. A newer JDK would, but it would change bots' instrumented `java.util` classes, and so
the games.

**JVM flags** (all give byte-identical replays):

| flags | DefaultSmall + cheesefarm, one JVM, CPU s | 32 quick games on 8 workers: wall, CPU |
|---|---|---|
| `-Xmx1g -XX:+UseSerialGC` (ABtest's) | 77.7 | 194 s, 1451 s (45 CPU-s a game) |
| + `-XX:+UnlockDiagnosticVMOptions -XX:-BytecodeVerificationRemote` (**used**) | 48.1 | 147 s, 1034 s (32 CPU-s a game) |
| + `-XX:TieredStopAtLevel=1` | 44.8 | 147 s, 1014 s |
| `-Xmx1g -XX:+UseSerialGC -XX:ActiveProcessorCount=1` (no verification flag) | 67.6 | |
| used + `-XX:ActiveProcessorCount=2` | 46.7 | |
| used + `-XX:-DontCompileHugeMethods` | 55.0 | |
| `-Xint` | 91.6 for DefaultSmall alone (vs 22.5) | |

- Skipping bytecode verification of the robots' classes is the one large win. It only skips a check, and the
  instrumented classes are valid.
- C1-only saves 2% CPU and loses per-game latency, so it is not used.
- AppCDS / `-Xshare` would only shorten JVM start (~0.5 s), which a worker pays once per run.
- `BCGAMES_JVM="flags"` overrides the flags, to re-measure elsewhere.

**Remote** (`tools/games --remote`, track C's `tools/run`): the first gauntlet played 3,109 games in 1,764 s on
Condor lab machines plus a 36-CPU Slurm slot, 106 games/min, with 1,200 games in flight. Remote games are
byte-identical to local ones (replay digests, Slurm and Condor).

**Throughput.** Under the same load, the nv flags measured 13.1 games/min on 8 workers (quick pool, official seeds).
ABtest measured 48 games in 294 s on 12 workers (9.8 games/min) and 86 games in 1103 s on 8 workers (4.7 games/min,
official pool). Per-pool rates: docs/evaluation.md.

## 8. Evaluation protocol

docs/evaluation.md: `tools/games`, its pools, the protocol (inner loop, accept gate, holdout, gauntlet) and how to
read a report.

## 9. Submission

- Official pipeline (galaxy/saturn): upload zip (**max 5 MiB**) -> unzipped into a fresh scaffold `src/` ->
  `./gradlew verify -Pteam=<pkg>` (compiles everything in the zip, Verifier loads every class) -> `build/classes`
  stored; matches run `./gradlew run` with `-PoutputVerbose=false -PshowIndicators=false`, several maps per JVM,
  `alternateOrder` per request, map-embedded seeds.
- The stock `zipForSubmit` zipped **all** of `src/` (3.3 MB incl. `battlecode/**` and 5 old snapshots) -> would be
  rejected (`ILLEGAL ... package 'battlecode.'`). Patched to zip only `src/<team>/**/*.java`.
- `scripts/check_submission.sh challenger` -> `submission.zip` 632 KB (44 files, 28 MB of source; `CellTracker.java`
  alone 18.6 MB), compiles alone, passes `verify`. Nothing is uploaded.

## 10. Troubleshooting

| Symptom | Cause / fix |
|---|---|
| `./gradlew verify -Pteam=challenger` -> `ILLEGAL ... can't use the package 'battlecode.'` | expected in this repo (Verifier walks all of `build/classes`, incl. the overrides). Use `scripts/check_submission.sh`. |
| `Starting a Gradle Daemon, 1 busy Daemon could not be reused` (+15 s) | another agent's build holds the daemon. Every concurrent `./gradlew` needs its own daemon (2-3.5 GB RSS, lingers 3 h idle). Use ABtest's default java runner for batches. **Never `./gradlew --stop`** on the shared box (kills other agents' daemons). |
| `NEW BATTLECODE VERSION AVAILABLE ... 1.2.6` banner | `build` finalizes with `checkNewVersion`; ignore, don't `update`. |
| A/B exits 1, Errors shows `compile error: gradle classes / classpath` | some package under `src/` doesn't compile (all packages build together, incl. `opp_*`). Fix or report it; don't edit frozen/other packages. |
| Old ABtest hung forever after a failed game | `sys.exit` inside a ThreadPool worker; fixed (failures are recorded, run continues, exit code 2). `ABtest_old.py` still has it. |
| Non-TTY ABtest output lost who won (colour only) | fixed: `+COND`/`-COND` markers, plain summary, JSON. |
| Single matches 3-4x slower than expected | the box is shared (other agents, test suites; load average 16-50 seen). Wall time scales with load; results don't (bytecode-limited, deterministic). |
| `./gradlew run` result differs from the official server's | `-Pseed` defaults to 0 because of the overrides; pass `-Pseed=<embedded seed>` (section 5) or use `ABtest.py --engine official`. Also 1.2.5 vs tournament 1.2.6 cheese spawning. |
| A map is listed but "Unknown map" | the engine only loads `maps/<name>.map26` or jar maps; copy new maps into `maps/`. `pathfinding test` contains a space: pass it alone via `--maps-dir`. |
| `check.py` fails on clean code | stale lint (`rc.turn` in `KingSpawningStrategy`); not a gate. |
| `run.py` fails immediately | its hard-coded packages don't exist; use ABtest. |
| Disk fills with replays | every A/B game writes `matches/run-<ts>-...bc26` (0.06-1.2 MB). `matches/` and `match_results/` are gitignored; delete old `matches/run-*` freely. |
