# Bot architecture (`src/challenger`)

`src/challenger` is byte-identical to the frozen champion `src/awubot` except for the package rename (verified with `diff -r` after `sed s/awubot/challenger/`). Paths below are relative to `src/challenger/`. For generated code, line pointers refer to the **`.jinja2` template**. Templates and regeneration are covered in codegen.md, game rules in rules.md, macro behaviour in strategy.md, combat micro (`robot/rat/combat/**`, `KingCombatStrategy`) in combat.md, and building/running matches in testing.md.

Each robot runs in its own JVM sandbox, so **every `static` field is per-robot state** that lives as long as the robot. Robots share information only through the 64×10-bit shared array (only kings can write it) and squeaks.

---

## 1. Entry point and turn lifecycle

### 1.1 `RobotPlayer.run` (RobotPlayer.java:26-89)

```
run(rc)                                                once per robot
 ├─ Game.init(rc)        rc, map size, myLoc/myDir/time, isRatKing, team, spawn, knownCheeseMines
 ├─ Book.init()          the map's book id and seat; booked Tune constants set (§4.4)
 ├─ Rng.seed(); Rng.init()                             seed = f(robot ID, round, x, y)
 ├─ EnemyManager.init(); GlobalArray.init()            first king at round 1 formats the array (§4.1)
 ├─ CellTracker.init(); MapManager.init()
 └─ while(true) try {
      Game.preTick()          time, myLoc, myLocX/Y, myDir (cached; read these, not rc.getLocation())
      Util.preTick()          no-op
      CellTracker.preTick()   no-op
      GlobalArray.preTick()   read counter/alive/map chunks/symmetry/cheese list; nearestRatKing.
                              Kings also: clear spawn slots (first king of the round), write loc, mark alive, reap dead kings
      RobotTracker.preTick()  senseNearbyRobots → sorted{Robots,Allies,Enemies,Cats}; clears allySet/kingSet/allyCount
      EnemyManager.preTick()  forget last turn's squeaked enemies
      MapManager.preTick()    MapTracker.updateVisibleUnits(); kings enqueue newly seen chunks
      Squeaker.preTick()      process ≤5 squeaks (§4.2)
      if (!hasInitialisedRobot) RatKing.init() | BabyRat.init()   first turn, and again after baby→king
      RatKing.run() | BabyRat.run()
      if (Clock.getBytecodesLeft() > 1000) Squeaker.postTick()   at most one squeak
      GlobalArray.postTick()  kings only: symmetry, 2 map chunks, whole cheese list, loc, delayed spawn info, counters
      Game.postTick()
    } catch (Exception e) {}   swallowed: the rest of the turn is skipped (+500 bytecode penalty)
      finally { kings: GlobalArray.endTurn(); Clock.yield(); }   endTurn marks the round the turn ends in (§4.1, slot 8)
```

### 1.2 Baby rat turn: `BabyRat.run` (robot/rat/BabyRat.java:79-121)

```
isLastMove=false; shouldGlance=true
CellTracker.storeVisibleCells()            scan the 90° cone (~21 cells)
AntColony.preTick()                        follow king moves for the breadcrumb grids
tryPickUpCheese(); tryTransferCheese()     free actions, also repeated after every move/turn
if beingCarried: (hp<15 & enemy carrier → disintegrate) return
if beingThrown: return
if combatStrategy.runTick(): considerPlacingCatTrap(); return      ← combat pre-empts everything
── non-combat only ──
RobotTracker.reset()                       allies list/set + kingSet (3×3 king footprints)
CellTracker.checkSymmetry()                eliminate or apply symmetry (§5.1)
RobotTracker.updateHistoricalCatLocs()     catHist[time%4]
considerPlacingCatTrap()
primaryStrategy.runTick()                  = MinerStrategy (the strategy chain below)
combatStrategy.miniTick()                  opportunistic ratnap/attack/trap with leftover cooldowns
isLastMove=true; executeGlance()           turn ±90° if the turn cooldown is unused
```

`BabyRat.init` (:25) sets `primaryStrategy = new MinerStrategy()` and `combatStrategy = new CombatStrategy()`, then calls `Pathfinder.init()` and `AntColony.init()`. It also calls `GlobalArray.loadSpawnInfo()`, but **discards the 13-bit payload** (§4.1, spawn channel).

**Strategy selection for rats is a fixed priority chain.** There is no scheduler. `Strategy.runTick()` returns true if the strategy "used the turn", and `tryStrategy(s)` returns false for `null` (robot/Strategy.java:14). `MinerStrategy.runTick` (robot/rat/MinerStrategy.java:37-81) runs:
1. Lifecycle bookkeeping: `goHomeStrategy` is nulled when raw cheese is 0, and created when `shouldGoHome()` holds (thresholds depend on distance to king, HP and global cheese).
2. `FormationStrategy`: walk to a king-announced formation point and `becomeRatKing()`.
3. `GoHomeStrategy`: follow breadcrumbs to a king and transfer cheese.
4. `CheeseStrategy`: walk to the largest visible pile it can reach (`Pathfinder.computePickable`).
5. If raw cheese > 50: force GoHome.
6. Loop `TraverseStrategy(target, 2)` until one accepts. The target is an unexplored 3×3 unit (`MapManager.localTracker.sampleUnseenUnitCenter()`) or a known mine weighted by distance (60% explore once mines are known, `Tune.EXPLORE_P`).

`ExploreStrategy` and `MicroTestStrategy` are not reachable from `BabyRat`.

### 1.3 King turn: `RatKing.run` (robot/king/RatKing.java:25-38)

`storeVisibleCellsKing()` (`senseNearbyMapInfos`, 360° r²=25) → `RobotTracker.reset()` → `tryUnburyIfStuck()` (digs out a buried king) → `RatKingStrategy.runTick()` (robot/king/RatKingStrategy.java:24). That last call runs **all four sub-strategies every turn, in order, ignoring their return values**: `KingSpawningStrategy`, `KingCombatStrategy`, `KingFormationCoordinatorStrategy`, `KingMiningStrategy`. Each one must check its own cooldowns.

### 1.4 Baby → king conversion

1. `FormationStrategy.runTick` calls `rc.becomeRatKing()` and then `Game.convertKing()` (Game.java:57).
2. `convertKing` sets `isRatKing=true` and `hasInitialisedRobot=false`, calls `GlobalArray.initRatKing()` (claim a slot, write loc, mark alive, seed the cheese queue), `MapManager.onConvertKing()` and `Pathfinder.bug.clearPath()`, and then **calls `Clock.yield()` in the middle of the turn**.
3. In the next round, execution resumes inside the old baby call stack. No preTicks run. `combatStrategy.miniTick()` and `executeGlance()` run as a king, followed by `Squeaker.postTick`, `GlobalArray.postTick` (now in king mode) and `GlobalArray.endTurn`, which marks the king alive for this round.
4. `RatKing.init()`/`run()` start the round after that.

### 1.5 Parts that run only if bytecode remains

| Gate | Where |
|---|---|
| `Squeaker.postTick` only if > 1000 bytecodes are left | RobotPlayer.java:78 |
| Rescan after a mid-turn move or turn only if ≥ 3000 left (≥ 2000 for a forward move, which scans only the new cells). Skipped entirely once `isLastMove` is set | BabyRat.java:189-213 |
| Bug-nav advances only when its lead is short (`lastDist < 4`) or an ally/king blocks the bug position. It then takes ≤ `MAX_BUG_ITERS`=5 steps per call and stops when < 6500 bytecodes are left. Kings advance 1 step | Pathfinder.java.jinja2:507-517 |
| Combat move searches use a budget = min(cap, f(bytecodes left)). Caps: TryThrow 9000, TryTrap 5000, TryAttack/TryRatNap/TryPosition 3000, TryFight left−1500, `CombatStrategy.precomp` 5000 | `moves/Try*.java.jinja2`, `CombatStrategy.java.jinja2:103` (combat.md) |

All of this is **bytecode-adaptive**: adding cost anywhere changes behaviour elsewhere.

### 1.6 Exceptions, assertions and robot death (verified)

- `catch (Exception e) {}` (RobotPlayer.java:84) swallows **Exceptions** only. The turn ends early and 500 bytecodes are charged. Measured at ≈0.02–0.08% of rat turns.
- **Assertions are enabled in matches.** The engine's `TeamClassLoaderFactory$Loader` calls `setDefaultAssertionStatus(true)`, and a probe printed `ea=true`. Two consequences:
  1. **Side-effecting asserts are load-bearing.** `assert(CombatStrategy.performMoves(...))` in `TryAttack.java.jinja2:67,71`, `TryTrap.java.jinja2:99,103`, `TryRatNap.java.jinja2:95` and `TryThrow.java.jinja2:103,105` performs the move. Do not delete them as if they were no-ops.
  2. **A failing assert throws `AssertionError`, which is an `Error` and escapes the catch.** `run()` exits, the engine prints the stack trace and marks the robot terminated (`SandboxedRobotPlayer.java:235`), and `GameWorld.updateRobot` **destroys it** at the end of that turn (`GameWorld.java:356-359`, engine 1.2.5 and 1.2.6). Observed in matches:
     - Rats died at `TryThrow.java.jinja2:106` (`assert(rc.canThrowRat())`) on DefaultSmall.
     - **Every king** hit `assert(false)` in `GlobalArray.writeCheeseMines` (comms/GlobalArray.java:428) once it knew ≥ 26 cheese mines. On bridges (28 mines) challenger (instrumented copy) lost all 4 kings and the game at round 401; on quadrants (38 mines) awubot lost all 5 at round 764. Maps at risk include `bridges` (28), `bridgesCatless` (26), `quadrants` (38), `sieveofriches` (30) and `tminusten` (30); count mines in `maps/` (150 maps, tournament ones included) rather than trusting a list.
- Other reachable `Error` sources: `assert(false)` in RobotTracker when > 43 robots are visible (only possible for kings), and the `encodeEnemy`/`decodeHealth` asserts.

---

## 2. Module map

G = generated from the same-named `.jinja2`; sizes are generated-output lines (see codegen.md §3).

| Path | Lines | Gen | Responsibility |
|---|---|---|---|
| `RobotPlayer.java` | 90 | | Entry point and the per-turn pre/run/post loop |
| `Game.java` | 168 | | Global cached state (`rc`, `myLoc`, `myDir`, `time`, …). `_rawMove`/`_turn` are the **only** `rc.move`/`rc.turn` call sites. `convertKing` |
| `CellTracker.java` | 436,521 | G | Map memory: per-row bitboards (seen/passable/dirt), `mapData` char grid, 11×11 local masks, symmetry detection and mirroring |
| `RobotTracker.java` | 3,008 | G | Nearby robots sorted by distance; ally/king-footprint sets; 4-round cat history |
| `Pathfinder.java` | 18,536 | G | Bug-nav with lookahead + local 11×11 bitboard BFS; also `doBfs`, `multisourceBfs`, `computeReachable`, `computePickable` (§6) |
| `comms/GlobalArray.java` | 575 | | Shared-array protocol (§4.1) |
| `comms/Squeaker.java` | 218 | | Squeak protocol, send priority, receive loop (§4.2) |
| `comms/EnemyManager.java` | 358 | G | 15-bit enemy encoding; enemies heard via squeak this turn |
| `comms/Enemy.java` | 27 | | Value class (location, direction, health) |
| `comms/MapManager.java` | 106 | | Owns global/local `MapTracker`s; chunk apply/diff |
| `comms/MapTracker.java` | 229,189 | G (opt-in) | Explored-area map: 3×3 "units", 16 chunks × 25 bits, unseen-unit bag (§4.3) |
| `robot/Strategy.java` | 20 | | `abstract boolean runTick()`, `tryStrategy` |
| `robot/rat/BabyRat.java` | 251 | | Baby turn driver, movement wrappers (`rawMove`/`turn`/`autoMove`), cheese pickup/transfer, glance, cat-trap placement, `attack` |
| `robot/rat/MinerStrategy.java` | 214 | | Rat priority chain; exploration and mine target picking |
| `robot/rat/{Formation,Cheese,Traverse}Strategy.java` | 152/50/52 | | Become king at a formation point / grab visible cheese / go to a target with a stuck timer |
| `robot/rat/{Explore,MicroTest}Strategy.java` | 23/148 | | Unused (test scaffolding) |
| `robot/rat/goHome/AntColony.java` | 128 | G | Per-king breadcrumb grids (§5.4) |
| `robot/rat/goHome/GoHomeStrategy.java` | 1,855 | G | Return cheese by descending breadcrumbs |
| `robot/rat/combat/CombatStrategy.java` | 500 | G | Combat entry (`runTick`, `miniTick`, `performMoves`, `shouldFight`); see combat.md |
| `robot/rat/combat/{CombatCommon,CombatTracker}.java` | 72/15,960 | G | Cached `canMove*`; threat bitmasks per (move, facing) |
| `robot/rat/combat/moves/Try{Throw,Trap,Attack,RatNap,Fight,Position}.java` | 34,831/5,203/1,345/1,332/1,209/987 | G | Unrolled micro searches |
| `robot/king/RatKing.java` | 230 | | King turn driver, `rawMove`/`turn`/`digDirtInDirection`, unbury |
| `robot/king/RatKingStrategy.java` | 31 | | Runs the 4 king sub-strategies |
| `robot/king/KingSpawningStrategy.java` | 98 | | Spawn thresholds, spawn placement, spawn payload |
| `robot/king/KingFormationCoordinatorStrategy.java` | 349 | | Decides and announces new-king formations |
| `robot/king/KingMiningStrategy.java` | 330 | G | Pick, claim and walk to mines; 5×5 "expand" when stuck |
| `robot/king/KingCombatStrategy.java` | 499 | G | King evasion/defence (combat.md) |
| `util/Util.java` | 4,072 | G | Direction tables, vision lookups, 2×2 location codec, symmetry helpers, distance helpers |
| `util/Rng.java` | 89 | | Per-robot PRNG (`mcg` is the only generator used) |
| `util/FastRng.java`, `util/Profiler.java` | 76/12 | | Unused / empty class (all Profiler calls are commented out) |
| `util/string/FastIndexedIntSet.java` | 54 | | StringBuilder-backed int set with index access (used) |
| `util/string/FastMapLocationSet.java` | 21 | | `"\|x,y\|"` string set (used by RobotTracker, Pathfinder) |
| `util/string/{FastIntSet,FastIntIntMap}.java` | 27/59 | | Unused. `FastIntIntMap` is internally inconsistent (`add` vs `contains` key formats) |
| `templates/common.jinja2` | 125 | | Jinja globals for all templates |
| `CLAUDE.md`, `battlecode26-specs.txt` | | | Original authors' agent notes (partly stale, §9) and an older spec copy (the root `battlecode26-specs.txt` is newer) |

---

## 3. Static state (all per-robot)

| Owner.field | Written by | Read by / notes |
|---|---|---|
| `Game.rc, mapWidth, mapHeight, team, spawn` | `Game.init` | everyone |
| `Game.time, myLoc, myLocX, myLocY, myDir` | `Game.preTick`; `_rawMove`/`_turn` keep them current mid-turn | everyone. **Stale if anything calls `rc.move`/`rc.turn` directly** |
| `Game.isRatKing`, `Game.kingId` | `init`/`convertKing`; `GlobalArray.init`/`initRatKing` | kingId = shared-array king slot 0–4; **−1 for babies** |
| `Game.knownCheeseMines` (FastIndexedIntSet of encode2x2) | `Squeaker.registerMine` (from CellTracker), `GlobalArray.registerGlobalCheeseMine` | MinerStrategy, KingMiningStrategy, KingFormationCoordinatorStrategy |
| `Game.lastHP` | `Game.postTick` | KingSpawningStrategy (panic spawn) |
| `Game.myIndex, myPreviousLoc, carryStartTime, DISABLE_OUTPUT`, `RobotPlayer.maxBytecode` | | **unused** |
| `GlobalArray.nearestRatKing/nearestRatKingId, ratCounter, numberOfRatsAlive, globalCheeseMines` | `GlobalArray.preTick` | `nearestRatKing` is null when no king is alive, so many callers NPE (caught) |
| `RobotTracker.sorted*`, `num*` (arrays: robots/allies/enemies length **25**, cats 10) | `updateRobots()` in preTick **and after every BabyRat move/turn** | combat, spawning, squeaks |
| `RobotTracker.allies/allyCount/allySet/kingSet` | **cleared** by `updateRobots()`; filled by `reset()` (+ `addAlly` from squeak sources, spawns) | Pathfinder (obstacles). A mid-turn move empties them until the next `reset()` |
| `RobotTracker.catHist[4][10], catHistNum[4]` | `updateHistoricalCatLocs()` (baby non-combat turns only; kings never) | Pathfinder cat avoidance, FormationStrategy, Squeaker |
| `CellTracker.*` rows, `mapData`, `local*` | §5.1 | Pathfinder, combat, symmetry |
| `MapManager.globalTracker/localTracker` | §4.3 | MinerStrategy exploration |
| `EnemyManager.knownEnemies/numKnownEnemies` | reset each preTick; filled by squeaks | CombatStrategy/CombatTracker, BabyRat cat-trap gate |
| `Squeaker.minesToSend`, `lastMessageCount` | registerMine / preTick | squeakMine |
| `AntColony.mark[5][60]` etc. | §5.4 | GoHomeStrategy |
| `Pathfinder.bug`, `lastDist`, … ; `Config.dirtAllowed` | getMove; `Pathfinder.init` sets dirtAllowed for rats spawned at round ≤ 2 | |
| `BabyRat.primaryStrategy/combatStrategy/isLastMove/shouldGlance/transferSwitch` | BabyRat | `isLastMove=true` suppresses the post-move rescans |
| `RatKing.is5x5, spawnedThisTurn`; `KingSpawningStrategy.numSpawned/lastPanicSpawn`; `KingMiningStrategy.stuckTurns/pending5x5` | king strategies | |
| `Rng.s0` | `seed`/`init` once; every `mcg()` call | **One shared stream per robot.** Adding or removing an `mcg()` call shifts every later draw |

---

## 4. Communication

### 4.1 Shared array: `comms/GlobalArray.java`

There are 64 slots of 0–1023. **Only kings may write** (50 bytecodes per write); everyone reads (10 per read). The array starts all-zero. The first king at round 1 (`init`, :155) writes 1023 to slots 8, 2–6 and 63, writes 0 to slot 7, and then claims slot 0. The comment block at :20-38 is stale ("hasn't been updated in like 3 weeks"); the table below is the code truth.

| Slot | Const | Encoding | Writer | Reader |
|---|---|---|---|---|
| 0 | `COUNTER` | Number of spawns registered (`ratCounter`); must stay ≤ 1023 | every king postTick (read in preTick, ++ per registration) | KingSpawningStrategy "even out spawning" (`ratCounter % kings == myIndexAmongKings`) |
| 1 | `SYMMETRY` | 0 unknown, 1 Rotational, 2 Horizontal (x→W−1−x), 3 Vertical (y→H−1−y) (`CellTracker.Symmetry` ordinal) | king postTick when it knows the symmetry and has read 0 | everyone: preTick `checkSymmetry` → `CellTracker.setSymmetry` |
| 2–6 | `RAT_KING_LOC`+k | `encode2x2` = `(x>>1) \| (y>>1)<<5` (≤ 957); 1023 = no king | king k (`writeMyLoc`, pre+post: both slots in one round, or neither); reaper writes 1023 | `getRatKing(k)` (2 reads = 20 bytecodes per call, called very often) |
| 7 | `RAT_KING_LSBS` | bits 2k / 2k+1 = x&1 / y&1 of king k | king k | `getRatKing` |
| 8 | `RAT_KING_TURN_COUNTER` | bits 2k..2k+1 = `round%3` of king k's last turn; `0b11` = free/dead | `markKingAlive` in preTick, and `endTurn` when a turn ends in a later round (an overrun, the conversion turn): a living king marks every round it runs in. The reaper (each king's preTick) marks k dead and clears its location when its value == `(round+1)%3`, i.e. k has no mark for the previous round | `findRatKingSlot`, `isFirstKingInRound` (the first king to run in a round, preTick or `endTurn`, clears the spawn slots) |
| 9 | `FORMATION_COORDINATOR` | kingId; 0 = none (**ambiguous with king 0**) | `writeFormationTarget`, `clearFormation` | `abortFormation` |
| 10–11 | `FORMATION_TARGET` | (x+1, y+1); (0,0) = none | coordinator king; cleared by it, or by the baby that became king exactly on the target | FormationStrategy |
| 12–13 | `FORMATION_MINE` | (x+1, y+1) | same | FormationStrategy (join if dist² ≤ 625) |
| 14 | `ALIVE_RAT_COUNT` | `(rc.getCurrentRatCost()-10)*2/5` | every king postTick | `numberOfRatsAlive` (CombatStrategy.shouldFight vs cats) |
| 15 | `BOOK_MAP` | the map's book id (1 + its index in `pools.GOAL`) on a map-seat the book has entries for; 0 elsewhere | the first king, once, at round 1 (`Book.init`), only on a match | every other robot, once, at its first turn (`Book.init`) |
| 16–20 | `KING_MINE_ASSIGNMENT`+k | `encode2x2(mine)+1`; 0 = none; never cleared when a king dies, so `getKingMineAssignment` ignores a dead king's slot | KingMiningStrategy (own k) | kings: `isMineAssignedToOtherKing` (mining: own slot skipped), `isMineAssignedToAnyKing` (formation site) (dist² < 25) |
| 21 | `KING_EXPAND` | bit k = king k is 5×5 | `expandKing()` after 17 stuck turns (`contractKing` unused) | `RobotTracker.reset` → `isKingExpanded(i)`. **Bug:** it tests bit `kingId`, not `i`; babies have kingId −1, so the result is always false |
| 22–31 | `RAT_SPAWN_INFO`+2k, +2k+1 | A = `hi3<<7 \| marker`, where `marker=(dx+4)*9+(dy+4)` is the spawn cell relative to king k; B = low 10 bits of a 13-bit payload; 1023 = empty | king k in the postTick of the **round after** `buildRat` (`toSpawn1→toSpawn0` delay). The first king of each round resets all 10 slots to 1023 | `BabyRat.init → loadSpawnInfo()`: finds the king whose marker equals its own position |
| 32–34, 35–37 | `MAP_CHUNK_START` | Two 30-bit values, little-endian 10/10/10. Bit 29 = empty flag; bits 0–28 = `MapTracker.encodeChunk` = `cx<<27 \| cy<<25 \| 25-bit unit mask` | every king postTick: 2 chunks per turn, round-robin over the chunks it has seen, each chunk's 3 slots all or none (`CHUNK_WRITES_COST`). The last king in turn order wins | everyone preTick → `MapManager.applyGlobalChunk` |
| 38–63 | cheese list (`MIN_CHEESE`=38) | `encode2x2(mine)` from 63 downward, then a 1023 terminator; with ≥ 26 mines the 26 slots hold a rotating window | every king rewrites its **whole** queue each postTick, terminator first, then the mines bottom up | everyone preTick `readCheeseMines` (stops at 1023) |

**Spawn payload channel (works, but unused).** `KingSpawningStrategy.trySpawn` (:93) sends `Rng.mcg() & 8191`. A probe confirmed that every newborn receives exactly its payload (63/63) in its first turn, one round after the spawn. `BabyRat.init` throws it away. This is a ready-made **13-bit king→newborn order** (role assignment) for one spawn per king per round.

**Adding a field.**
- Free space: slot 1 bits 2–9, slot 9 bits 3–9 (values ≤ 4) and slot 21 bits 5–9.
- For more space, raise `MIN_CHEESE` (shrinking the cheese list) and fix the `writeCheeseMines` overflow. Several real maps have ≥ 26 mines (bridges 28, bridgesCatless 26, quadrants 38, sieveofriches 30, tminusten 30).
- Babies cannot write. They must squeak to a king (r² ≤ 16), and the king writes.
- Budget 50 bytecodes per king write. King postTick is already ~1.4–2.0k bytecodes, dominated by the 6 map-chunk writes and the full cheese-list rewrite.
- Every king writes the same chunk and cheese slots, so whichever king runs last in the engine's turn order wins.

### 4.2 Squeaks: `comms/Squeaker.java`, `comms/EnemyManager.java.jinja2`

Engine facts:
- One squeak per robot per round (`MAX_MESSAGES_SENT_ROBOT=1`), cost 50.
- Heard by allies **and cats** within r² 16.
- Squeaks stay readable for 5 rounds. `readSqueaks` costs 10.

| Bits of the 32-bit int | Meaning | Sender | Receiver (`processMessage`, :87) |
|---|---|---|---|
| bit31=1, bit30=0, then 3×10 bits | Cheese mines: `encode2x2` at bits 0–9, 10–19, 20–29; 1023 = empty | babies, `squeakMine` (lowest priority; starts at index `time%len`; drops mines already in `globalCheeseMines`) | `registerGlobalCheeseMine(bits 0–9)`. **Only the first mine is decoded.** Babies re-add it to their own `minesToSend` (gossip). `loc==0` throws |
| bit31=1, bit30=1, then 2×15 bits | Two enemies, A = bits 0–14 and B = 15–29 (0x7FFF = none). Enemy = `loc7<<8 \| dir3<<5 \| hp5`, where `loc=(dx+4)*9+(dy+4)` relative to the **squeaker** (clamped to ±4) and hp is an index into `[24,26,…,80,88,90,100]` (rounded up) | `EnemyManager.trySqueak`: kings send the nearest ≤ 2 within ±4; rats send 1, or a random pair | `EnemyManager.registerPacket` ×2. Kept only if the decoded location is **outside** the receiver's sensing |
| bit31=0, header bits 29–31 = 0 | Map chunk (29-bit `encodeChunk`) | babies within r² 16 of their own king whose local tracker differs from the global one (`squeakMap`, :202); applied locally too | everyone: `MapManager.applyGlobalChunk` |
| header 1 | Symmetry ordinal in the low 2 bits | anyone near their own king when slot 1 is still 0 and they know the symmetry | **kings only**: `setSymmetry`, then the king writes slot 1 |
| header 2 | Lure: `cat2x2<<10 \| enemyKing2x2` | `squeakLureSignal`, **never called** | ignored |
| header 3 | **free** (29-bit payload) | | ignored (falls into `default`) |
| `7<<29` (HEADER_NOP "panic") | Meant as a no-op to distract cats | babies when cats were seen in the last 4 rounds and a visible cat is within dist² 25 of the nearest king | **Bug:** bit31 and bit30 are set, so it decodes as an enemy packet. Two **phantom enemies** (HP 24, facing N) at sender+(−4,−4) and +(3,−3) |

The king's `trySqueakKing` has two more bugs: it shifts by loop index `i*15` instead of the number of enemies already packed, and when every enemy is out of ±4 it still squeaks `data=0`, i.e. a phantom at (−4,−4).

**Send priority** (`postTick`, :171; one squeak per turn, only if > 1000 bytecodes are left):
1. Baby with any cat in `catHist`: panic squeak (conditions above), then return. Kings return early if `catHistNum[0] > 0`. Kings never update `catHist`, so this normally never triggers. A converted king keeps its stale baby `catHist`, though, and if `catHistNum[0] > 0` it never squeaks again.
2. Near your own king (r² ≤ 16): symmetry, otherwise (baby) map diff.
3. Enemies.
4. (baby) Mines.

**Receive** (`preTick`, :145): newest first, **at most 5 messages per turn**. It reads this round's squeaks, then the previous round's squeaks that arrived after our last read (`lastMessageCount`). In crowds, low-priority info is dropped. Every message source is added as an ally location.

### 4.3 Map coverage: `MapManager` + `MapTracker.java.jinja2`

- A **unit** is a 3×3 cell block. The map holds 20×20 units, grouped into chunks of 5×5 units (one `int` field `chunkXY` per chunk, 25 bits) and 4×4 chunks.
- A unit counts as seen only when all 9 cells are inside **one** baby-cone view (r² ≤ 20, facing). Partially off-map units are never marked.
- `updateVisibleUnits()` runs every preTick: `switch(dir)` → one method per (dir, row) → `switch(x)` → inline bit sets. The template precomputes all 8×60×60 states (MapTracker.java.jinja2:445-478).
- Babies keep two trackers. `globalTracker` mirrors king broadcasts. `localTracker` = global ∪ own vision, plus a StringBuilder bag of unseen unit ids; `sampleUnseenUnitCenter()` picks the next exploration target.
- Near a king, a baby squeaks the first chunk where local ≠ global (`findDiffChunk`).
- Kings hold one tracker. When it gains new bits they enqueue all non-empty chunks (`GlobalArray.noteMapChunkSeen`, deduplicated by a 16-bit seen mask) and broadcast 2 per turn round-robin.

### 4.4 The book: `Book.java` (generated by `tools/book/book gen` from `book.jsonl`)

The per-map book (tools/book/AGENTS.md) switches Tune constants and sets per-map data by map and seat. At round 1
the first king keys on its team, the map's size and its location; on a key of a book map-seat it reads its r² 25
view into wall and dirt masks and compares them and its ID with the map file's, and on a match writes the map's
id to slot 15. Every robot after it reads slot 15 in its first turn; `Book.map` is the id (0 off the book),
`Book.seat` 0 for team A, 1 for B, `Book.symmetry` the map's symmetry (slot 1's encoding). A converted king keeps its baby's statics, so nothing runs again.
Invariants:
- `Book.init` is the first call after `Game.init`: no class that reads `Tune` may load before it, so a booked
  constant's value reaches every use, `static final` copies included. A booked constant must never be read into
  state before then.
- Only the starting king writes slot 15, once, and only a non-zero id; nobody else writes it.
- A goal map-seat without entries is never keyed: its games are main's but for the slot read (~15 bytecodes a robot).
- `Book.init` runs outside the turn's `try`: it must never throw (it calls only `senseNearbyMapInfos`, `getID` and
  slot 15).

---

## 5. Perception and memory

### 5.1 `CellTracker.java.jinja2`

- **Rows.** `static long seen{y}, passable{y}, dirt{y}` for y ∈ [0, 60), where bit x is cell (x, y) (:39-77).
  - `passable` = seen and not wall. It is set once on first sight; walls are permanent.
  - `dirt` is set or cleared on **every** sighting.
  - `init` marks rows ≥ mapHeight as seen (switch fall-through, :28-33). `isSeen` returns true off-map.
  - Accessors `isSeen/isPassable/isDirt(loc | x,y)` are `switch(y)` + a bit test.
  - Also: `isWallAdjacent`, `isPassable3x3/5x5`, `isKingPassable` (3×3, or 5×5 when `RatKing.is5x5`).
- **`mapData[60]`.** One StringBuilder per row y; char x is `'\0'` unknown, `'\1'` non-wall, `'\2'` wall. It is written only by baby `storeCell`: not by `storeCellKing`, and not by symmetry mirroring. Combat reads it with `charAt` (1 bytecode).
- **Scans.**
  - `storeVisibleCells()` (:192) covers the unrolled 90° cone (21–22 cells: `onTheMap` + `senseMapInfo` + `storeCell`). It runs at the start of `BabyRat.run` and after mid-turn moves/turns.
  - `storeVisibleCellsPostMoveForward()` (:226) covers only the newly visible cells.
  - `storeVisibleCellsKing()` (:260) is `senseNearbyMapInfos` (cost 100) over ~80 cells.
  - The first sighting of a cheese mine calls `Squeaker.registerMine`.
- **Local window.** `computeLocalBfsMasks()` (:306) builds `localPassable0/1` and `localDirt0/1`: an 11×11 window around `myLoc` packed into 2 longs. Bit index = `(dy+5)*11 + (dx+5)`; bits 0–60 go in `*0`, bits 61–120 in `*1` as bit−61; the centre is bit 60. The result is cached per `myLoc`.
- **Symmetry** (:315-545).
  - Candidates `hsym/vsym/rsym` start at 1. `checkHor/Ver/Rot` compare mirrored rows within ±5 rows of the robot (per map height, unrolled) and zero a candidate on any seen-passability mismatch.
  - Once exactly one candidate is left, `apply*` copies seen/passable across the mirror for the rows near the robot, on every non-combat baby turn. This gives free map knowledge.
  - The result is also shared via slot 1 / squeak header 1.

### 5.2 `RobotTracker.java.jinja2`

- **`updateRobots()`** (:62) calls `senseNearbyRobots()` (cost 100) and sorts by distance. Each robot's index is inserted as a char before the marker `\u0FFF+d²` in a StringBuilder; then the 43 markers are deleted. The template is unrolled for 0–43 robots and per team.
  - `MAX_DIST=42` covers the largest anchor distance² (41) of a cat or king partly inside a king's r²=25 view.
  - **Limits:** with > 25 robots visible, `sortedRobots[25]` throws `ArrayIndexOutOfBoundsException` and the whole turn is lost. With > 43, `assert(false)` kills the robot (§1.6). Only kings (360°, r² 25) can see that many.
  - Called in preTick and after every BabyRat move/turn unless `isLastMove`.
- **`reset()`** (:164) adds all ally baby rats to `allies`/`allySet` and every king's 3×3 (or 5×5) footprint to `kingSet`. Pathfinder avoids both.
- **`catHist[time%4]`** keeps up to 10 cats from each of the last 4 baby non-combat turns. Pathfinder nulls entries that it can sense are gone.

### 5.3 MapTracker

See §4.3.

### 5.4 `AntColony.java.jinja2` (go-home memory)

- `mark[k][y].charAt(x)` is a per-king breadcrumb grid; lower values mean "more recently connected to king k".
- Every BabyRat move calls `markCurrent()`, which stores `min(curMark, old)` and then increments `curMark`. Being in transfer range of a king calls `reachedHome()`, which resets `curMark` to `base[k]+5`.
- When king k steps 1 cell, `base[k]--` (aging every mark) and the 4 diagonals of its old cell are stamped. If king k teleports, or is seen for the first time, its grid is reset.
- `GoHomeStrategy` picks the lowest mark among cells reachable within 5 steps (`Pathfinder.computeReachable`), using an unrolled 11×11 serpentine scan, then walks there with `Pathfinder.doBfs`. With no mark it falls back to `TraverseStrategy(king, 8)`.

---

## 6. Movement: `Pathfinder.java.jinja2` + BabyRat wrappers

**API**

| Call | Purpose |
|---|---|
| `Direction Pathfinder.getMove(MapLocation target)` (:491) | Next step toward `target`, or `CENTER`. **It can turn the robot itself**: when the bug position equals `myLoc` it calls `turn(dir)` to look at an out-of-sight cell. `turn` is `BabyRat.turn` via a static import, **even for kings**. |
| `BabyRat.autoMove(Direction)` (BabyRat.java:217) | Executes a step. Turns to face `dir` if it can turn; if the next cell is visible dirt, removes it (or returns); refuses a forward step into a cat's 180° cone at r² ≤ 5; then moves forward (cooldown 10), or strafes with `rc.canMove` (18) if it could not turn. |
| `BabyRat.rawMove(dir)`, `rawMove()`, `turn(dir)` | Low-level steps for babies. After acting they pick up and transfer cheese, drop a breadcrumb (moves only) and, unless `isLastMove`, run `RobotTracker.updateRobots()` + a cell rescan (bytecode-gated). |
| `RatKing.rawMove/turn/digDirtInDirection` | King equivalents (no side effects). `digDirtInDirection` removes the first removable dirt among the 3 leading-edge cells 2 away. |
| `Pathfinder.doBfs(target)` | Local BFS seeded at `target` with no bug-nav (used by GoHome). |
| `multisourceBfs(sources, n) → {dirMask, iters}` | Combat approach directions (≤ 6 iterations). |
| `computeReachable()` → `reach0/1` | Cells within 5 steps. |
| `computePickable()` → `pickable0/1`, `isPickable(loc)` | Cells a rat can pick cheese up from: cells reached in ≤ 6 steps through passable cells (no allies; no dirt unless dirt pathing is on), grown by one ring, plus its own 3×3. `loc` must be within 4 cells. |
| `givenUp()`, `reset()`, `getBugPos()` | Bug-nav state queries/reset. |

Typical use is `BabyRat.autoMove(Pathfinder.getMove(target))` (TraverseStrategy.java:30). **`Pathfinder.makeMove` does not exist**: `src/challenger/CLAUDE.md` is stale on this point.

**Algorithm**

1. **Bug-nav waypoint generator** (`BugnavInstance`, :29-465). It walks along known passable cells from `pos` toward the target, using the last 13 positions as a ring buffer (`path0..path12`). A stack-size/winding counter detects wall-following. The two `tryDirsLeft/Right` variants differ in turning direction, and `possiblyPassable` treats unseen cells as open.
   - On a fresh obstacle, a **lookahead** spawns left and right child bugs, advances them alternately, and picks one by `sqrt(dist)+0.7·len`. When the choice is unclear it picks randomly with weight `score^15`.
   - Loop/edge detection sets `givenUp` (TraverseStrategy then re-targets).
   - Only a few steps are advanced per turn (§1.5); it never goes more than 5 Chebyshev cells from `myLoc`.
2. **Local bitboard BFS** (`doBugnavBfs`, :732). The obstacle mask is built from passable cells minus allies, king footprints (3×3; Minkowski-expanded for a moving king), sensed cats and their facing cones, and dirt when cheese < 5. The BFS is seeded with the bug path points and unrolled for 13 iterations of shift/or/and on the 2-long 11×11 window (`bfsIteration`, :593).
   - Dirt cells propagate one iteration late, so a dig costs 2 steps.
   - At the first iteration that touches a neighbour of `myLoc`, the 8 moves are scored. Score = `dist²(next,target) + dist²(next,bugPos)`, then: −1200 if inside the BFS frontier, −100 if not wall-adjacent, +2700 next to historical cats, `catPenalty` (+3000 or +1300 per cat whose vision covers the cell), +1000 for dirt, −500 for moving straight when the robot cannot turn.
   - If no path is found, rats take a random legal step and reset the bug.
3. **Dirt policy** (`Config.allowDirt`, :17). Pathing through dirt is allowed for rats spawned at round ≤ 2, after round 100 when global cheese ≥ 1500, or whenever it is ≥ 3000. Digging needs `DIG_DIRT_CHEESE_COST`=5.

**Why `rc.move`/`rc.turn` are forbidden** (`check.py`, which allows them only in Game.java):
- `Game._rawMove`/`_turn` keep `Game.myLoc/myLocX/myLocY/myDir/myPreviousLoc` correct mid-turn, and all code reads those caches.
- The BabyRat wrappers add perception refresh, cheese pickup/transfer and breadcrumbs.
- `KingSpawningStrategy.java:90` violates this (`rc.turn(...)`, leaving `myDir` stale for the rest of the king's turn), so `check.py` fails on the shipped code.

---

## 7. Utilities

- **`util/Util.java.jinja2`**
  - `directions`/`allDirections`/`orthDirections` arrays.
  - `canSense(dx,dy,dir)` / `canSense(src,dst,dir)`: baby cone r² ≤ 20, via lookup switches.
  - `canCatSense` (cat 2×2, r² 17, 180°), `canHardSense` (3×3 neighbourhood fully visible), `getSenseLevel`.
  - `dist`/`chebyshevDistance(RobotInfo)`, size-aware by `UnitType.ordinal()` (0 baby, 1 king 3×3, 2 cat 2×2).
  - `relatedDirections` and wide variants (`new` arrays, so not free); `cw`/`ccw`; `dirFromDelta`.
  - `encode2x2`/`decode2x2` (10-bit 2×2-cell code; 0 = cells (0..1, 0..1)) and `encodeLoc`/`decodeLoc` (x·60+y).
  - Symmetry mirrors and `genPassive/Aggro/ExplorationTarget`.
  - **There is no `indicate()`/`println()` any more.** Every `// indicate(...)` in the code is dead. Debug with `rc.setIndicatorString/Dot` (1 bytecode) or `System.out.println`, and remove it afterwards.
- **`util/Rng.java`.** xorshift `mcg()` → [0, 2³¹). It is seeded once from the robot ID, round and spawn position (`seed`+`init`; the "call every round" comments are wrong). Matches are deterministic for a given engine seed (`-Pseed`), maps and code. Any change in bytecode use or in `mcg()` call count changes outcomes, so evaluate on many maps and seeds. `FastRng` is unused.
- **`util/Profiler.java`** is empty. Measure with `Clock.getBytecodeNum()` deltas in a scratch copy of the bot (testing.md).
- **`util/string/*`.** StringBuilder-backed sets keyed by `(char)int` or `"|x,y|"`, cheap because `indexOf`/`append` cost 1 bytecode. `FastIndexedIntSet` also gives O(1) `at(i)` and `size()`.

---

## 8. Bytecode budget

The limits are **17,500 per baby rat and 20,000 per king**. If a turn runs over, execution pauses mid-code and resumes next round. Measured by instrumenting a scratch copy (`Clock.getBytecodeNum()` at the end of the preTicks, at the end of `run()`, and at the end of the turn), challenger vs awubot:

| Map | Unit | Turns sampled | Avg total | preTicks | run() | postTicks | Max | Turns > 15k | Overran* |
|---|---|---|---|---|---|---|---|---|---|
| DefaultLarge (2000 rds) | rat | 20,504 | 11,276 | 1,445 | 9,634 | 197 | 17,342 | 3.7% | 0.13% |
| DefaultLarge | king | 2,295 | 13,178 | 2,552 | 8,999 | 1,626 | 19,988 | 14% | 0.09% |
| cheesefarm | rat / king | 5,447 / 1,298 | 10,925 / 13,008 | 1,596 / 2,682 | 9,107 / 8,449 | 221 / 1,876 | 17,369 / 19,050 | 5% / 10% | |
| bridges | rat / king | 8,030 / 485 | 12,329 / 13,499 | 2,033 / 2,724 | 10,079 / 8,780 | 216 / 1,994 | 17,492 / 19,981 | **17%** / 26% | 1.3% / 1.4% |

\*"Overran" = the round number changed during the turn. It includes deliberate mid-turn `Clock.yield()`s (baby→king conversion, disintegrate).

There is little headroom: rats average ~65% of the limit and kings ~65%, with a long tail. Much of the rat budget is consumed adaptively (bug iterations, combat budgets), so **new features displace existing computation rather than adding to idle time**.

**Engine costs that dominate** (MethodCosts.txt):

| Call | Cost |
|---|---|
| `senseNearbyRobots`, `senseNearbyMapInfos` | 100 |
| `writeSharedArray`, `squeak` | 50 |
| `buildRat` | 20 |
| `senseRobotAtLocation` | 15 |
| `readSharedArray`, `readSqueaks`, `canMove`, `canMoveForward`, `canBuildRat`, `attack` | 10 |
| `senseMapInfo`, `onTheMap`, `canSenseLocation`, `canPickUpCheese`, `canRemoveDirt` | 5 |
| `MapLocation` methods | 2 |
| `rc.get*`, `rc.isXReady`, `rc.canTurn`, `rc.move`/`turn` | 1 |
| `Clock.*` | 0 |
| A thrown exception | 500 |
| `new T[n]` | n |

**Measured per-component cost**

These numbers come from a second scratch probe that brackets calls with `Clock.getBytecodeNum()`. Source: DefaultLarge rounds 1–456. On the same map and seed, the lighter first probe won at round 2000 while this heavier probe lost at round 456, which shows how sensitive outcomes are to small bytecode changes. "Calls/turn" < 1 means the call is skipped on some turns, e.g. in combat.

| Unit | Component | Bytecodes per call | Calls/turn | Bytecodes per turn |
|---|---|---|---|---|
| rat | `CellTracker.storeVisibleCells` (turn start) | 1,375 | 1.00 | 1,378 |
| rat | **rescan after mid-turn move/turn** (`updateRobots` + `storeVisibleCells*`) | 1,351 | 1.26 | **1,706** |
| rat | `GlobalArray.preTick` | 799 | 1.00 | 800 (max 3,949) |
| rat | `RobotTracker.preTick` (`updateRobots`) | 446 | 1.00 | 447 |
| rat | `RobotTracker.reset` / `CellTracker.checkSymmetry` / `AntColony.preTick` | 455 / 423 / 241 | 0.85 / 0.85 / 1 | 386 / 359 / 241 |
| rat | `Squeaker.preTick` / `postTick`, `MapManager.preTick` | 90 / 193, 34 | 1 | ≈ 320 |
| rat | `CombatStrategy.runTick` (mostly "no fight"; max 12k) | 1,023 | 0.99 | 1,010 |
| rat | `MinerStrategy.runTick` (includes the moves and rescans it triggers) | 6,789 | 0.85 | 5,753 |
| rat | ⤷ `Pathfinder.getMove` inside TraverseStrategy | 3,888 | 0.58 | 2,262 (max 9,162) |
| king | `CellTracker.storeVisibleCellsKing` | **3,362** | 1 | 3,362 |
| king | `GlobalArray.preTick` / `postTick` | 1,870 / 1,435 | 1 | 3,305 |
| king | `RobotTracker.preTick` | 544 | 1 | 544 |
| king | `RatKingStrategy.runTick` (4 sub-strategies) | 4,789 | 1 | 4,789 (max 9,418) |

Cheesefarm (rounds 1–570) matches these numbers to within ~15% on every row. The exception is `checkSymmetry`, which falls to ~160 once the symmetry is known.

**Where to look first**
- **King perception.** `storeVisibleCellsKing` loops `storeCellKing` over ~80 `MapInfo`s every turn, about a quarter of the king's budget. Scanning only the changed ring after a move, or a few rows per turn, would free ~3k.
- **Shared-array traffic.** About 30 writes per king turn at 50 each: the whole cheese list and 6 chunk slots are rewritten every turn. `GlobalArray.getRatKing(i)` costs 20+ per call and is called 5× in many loops (`RobotTracker.reset`, `findNearestRatKing`, `AntColony.preTick`, `Pathfinder.prepareBfsMasks`, `MinerStrategy`, formation scoring). Caching the 5 king locations once per turn is an easy win.
- **Rat rescans.** Every mid-turn move/turn repeats `updateRobots` + a cone scan (~1.35k). One idea: skip the robot re-sense (~0.45k) when nothing later in the turn reads it.
- **Pathfinding.** ~3.9k per `getMove`: bug steps plus a 13-iteration BFS that scores 8 moves with `rc.canMove` (10 each) and `catPenalty` loops.
- **Combat.** Engaged turns go up to the budget caps (combat.md).
- `Squeaker.postTick` is skipped below 1,000 bytecodes left, so the busiest rats go silent.

---

## 9. Extension points and invariants

**New rat behaviour.**
1. Create `robot/rat/XStrategy extends Strategy` with `runTick()` returning true iff it consumed the turn.
2. Instantiate it **once**: as a field built in the `MinerStrategy` constructor, or in `BabyRat.init` for a different `primaryStrategy`.
3. Insert it into the `MinerStrategy.runTick` priority chain with `if (tryStrategy(x)) return true;`.
4. Move with `BabyRat.autoMove(Pathfinder.getMove(t))` or `BabyRat.rawMove/turn`.
5. Remember that combat pre-empts the primary strategy entirely (§1.2), and that `RobotTracker.allies/kingSet` are empty after any mid-turn move until `RobotTracker.reset()`.

**Per-rat roles.** Use the spawn payload (§4.1): choose the bits in `KingSpawningStrategy.trySpawn` and switch `primaryStrategy` on `loadSpawnInfo()` in `BabyRat.init`. `-1` means not found (e.g. a rat that existed at game start).

**New king behaviour.** Add a sub-strategy to `RatKingStrategy.runTick`. All of them run every turn, so check `rc.isActionReady()`/`isMovementReady()`/`canTurn()` yourself. Kings use `RatKing.rawMove/turn`.

**New message.**
- **Squeak:** use header 3, or header 2 if you drop the lure stub, for a 29-bit non-negative payload. Add a `case` in `Squeaker.processMessage` and a branch in the `Squeaker.postTick` priority chain. Remember: one squeak per robot per turn, cats hear squeaks (keep the cat gate), receivers handle ≤ 5 messages per turn, and the sender must be within r² 16 of its listeners.
- **Shared array:** use slot 15 or the spare bits (§4.1). Only kings write.
- **Never construct negative payloads ad hoc.** Bit 31 selects the legacy mine/enemy parser.

**New tracked state.**
- Use static fields updated in the right phase: global/comms data in a `preTick`, perception at the top of `BabyRat.run`/`RatKing.run`.
- Prefer row bitboards or unrolled templates over arrays.
- Watch the per-turn budget (§8).
- If it must survive a baby→king conversion, handle that in `Game.convertKing`.

**Invariants (do not break)**
1. No `rc.move`/`rc.moveForward`/`rc.turn` outside `Game.java`. Read `Game.myLoc`/`myDir`/`time`, not `rc.getLocation()`.
2. Nothing may throw an `Error`. **No new `assert`s that can fail, and no side effects inside new `assert`s.** Treat the existing side-effecting asserts as load-bearing. `RobotPlayer` catches `Throwable` (S1), so an `Error` now costs the rest of the turn instead of the robot; the engine's kill signal is re-thrown first by the handler the instrumenter injects (`InstrumentingMethodVisitor.addRobotDeathHandler`). `GlobalArray.writeCheeseMines` no longer asserts on ≥ 26 mines: the list rotates (S1).
3. Shared-array writes only from kings, only in owned slots, values 0–1023. Spawn slots are cleared by the first king each round.
   A value spread over several slots must never be read half-written. The engine charges an engine call before running it
   and pauses there when the turn is out of bytecode; robots acting until the turn resumes read whatever is in the array.
   So either write all slots only with enough bytecode left (`writeMyLoc`, `writeChunkPayload`), or write in an order whose
   every prefix reads as a valid value (`writeCheeseMines`). Not yet done: the spawn info (2 slots).
4. One squeak per turn. All squeaking goes through `Squeaker.postTick` (EnemyManager is called from there).
5. `BabyRat.isLastMove` must be true before the final action of the turn, otherwise you pay for useless rescans.
6. Keep templates and generated `.java` in sync (codegen.md).
7. Preserve the order of `Rng.mcg()` calls unless you intend to reshuffle randomness.

**The original authors' `src/challenger/CLAUDE.md` rules, evaluated**
- *"Never edit BabyRat.java; add behaviour only as new strategies"*: **keep it as a strong default, but it is not absolute.** The intent was to keep the turn driver free of decision logic, and new behaviour does belong in strategies. But `BabyRat` already contains behaviour (cat traps, glance, transfer), and it is the only place to change turn order, combat pre-emption, bytecode gates, role selection at `init`, or the movement wrappers. Allow such plumbing edits when they are deliberate, and keep the wrapper side effects intact.
- *"Use `Pathfinder.makeMove(target)` / `makeMove(dir)`"*: **stale.** The API is `getMove` + `BabyRat.autoMove`.
- *"Pathfinder handles cell tracking automatically"*: **inaccurate.** The rescans are done by `BabyRat.rawMove/turn`.
- *"Prefer Util over rc"*: still right, but `Util.indicate`/`println` no longer exist.

**Other verified quirks and bugs (candidates for cheap fixes)**
- Lookahead: the `else if(leftBug.arrived)` branch should test `rightBug` (Pathfinder.java.jinja2:230).
- `multisourceBfs` writes king footprints into the wrong mask (`localPassableExceptAllies`, Pathfinder.java.jinja2:943), so kings are not obstacles in the combat BFS.
- `CombatStrategy.computeCellScores` returns immediately (`if (1 == 1) return;`), leaving dead code.
- `for_related`/`for_wide_related` macros are never called.
- Kings calling `Pathfinder.getMove` may run `BabyRat.turn`'s baby side effects.
- The cheese-mine squeak decodes 1 of 3 mines; panic and empty king enemy squeaks inject phantom enemies (§4.2).
- `isKingExpanded` uses the wrong index; slot 9 uses 0 as "none" although 0 is a valid kingId.
