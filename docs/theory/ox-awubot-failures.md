# OX: awubot's own failures, and whether our play can provoke them

Package OX (track B, wave 4), measured on main `da9c83f` against awubot (`caterpillow-final`).
Source keys: `A:` `src/awubot/`; `E:` engine 1.2.6 under `engine/src/main/battlecode/` (docs/rules.md §0);
`D:` data and tools in `~/battlecode26-data/scratch/ox/` (§6).

## 0. Summary

1. **awubot's kings never die to their own code against main.** In 760 `goal` games (seed base 16001) its robots
   died to their own uncaught Errors 149 times (0.20 a game, in 117 games), every one a baby rat, every one at
   `TryThrow.run` (`assert(rc.canThrowRat())`, `A:robot/rat/combat/moves/TryThrow.java:151,156`). No king died
   other than by damage (612 king deaths, all by damage). Every king-killing assert in awubot is out of reach
   (§3.1): the cheese-list overflow at 26 mines never fires because we kill those kings first; the > 43-robots sort
   never saw more than 32 robots; the others need geometry the engine forbids.
2. **The rat crashes are the cats' doing, not ours.** At 84% of them the tile in front of the carrier, after its move
   or turn, held a cat; awubot's throw planner marks only rats and kings as obstacles, and the engine refuses a throw
   into an occupied tile. The carried rat was ours in 127 of the 128 crashes where the replay shows it; in a third of the crashes no free
   robot of ours was within 4 tiles. Our play reaches this failure only by having our rats grabbed next to cats.
3. **Turns lost to its own code are rare and self-made.** Rats lose 0.05% of their turns to caught exceptions and
   0.15% to bytecode overruns (ours 0.12%); kings 0.23% and 0.70% (ours 0.16%). Two king freezes exist:
   - **the spawn-to-centre stall**: a king at Chebyshev distance 2 from the map centre, with the centre tile
     buildable and no enemy in view, throws at `rc.turn(CENTER)` every turn (`A:robot/king/KingSpawningStrategy.java:90`),
     so it never moves and never spawns, and since the spawn rotation only advances on a spawn, the team's peacetime
     spawning stalls with it: 28 games, 546 king-turns, runs up to 58 rounds;
   - **the crowd freeze**: a king that senses ≥ 26 robots indexes past `sortedRobots[25]` and loses its turn
     (`A:RobotTracker.java`): 991 king-turns in 9 games, runs up to 175 rounds, 6 of the games on `rose`, crowded by
     awubot's own rats (median 27 sensed, 0 of them ours).
   Frozen rats (≥ 3 lost turns in a row): 323 in 130 games, 307 of them standing in front of a cat in cooperation
   (`A:robot/rat/BabyRat.java:42`, a null `senseRobotAtLocation`; runs up to 55 rounds), with none of our robots
   within 4 tiles at 92% of those turns (that branch needs no enemy known). S1's torn king location (rats freezing in `AntColony.preTick`) happened once, for 3 turns.
4. **Price: no loss of ours is reachable.** In our 128 losses, awubot ended with one king in 85. The most robots any
   awubot king sensed in a loss was 23 (the crowd freeze needs 26, the crowd death 44), and the most of ours 9;
   ≥ 6 of ours in an awubot king's view in 2 losses. A failure that removes or freezes a king needs our robots at its
   king in numbers, which is what killing it already needs. Oracle pairs of awubot copies that differ only where a failure fires
   (§4.2) price them against main on 380 `goal` cells: the throw crash is worth −0.4 pp [−1.3, +0.5] to us, the stall
   +0.3 pp [−0.3, +0.8]; a king that lost its turn whenever it sensed one of ours would be worth +7.1 pp
   [+4.7, +9.5], but no failure of awubot's gives that freeze at a trigger we reach.
5. **No arm is built; nothing to merge.** The failures we could provoke are worth nothing (the throw crash, the
   stall), and the one worth something (a frozen king) needs a crowd at awubot's king that our losses never had. The
   7.1 pp is the value of awubot's king reacting to our robots in view: a number for W3's "killing an exposed king"
   (§5).

---

## 1. Method

- **Games.** `tools/games ab challenger@origin/main awubot --ref none --pool goal --seed-base 16001 --min-cells 380
  --max-cells 380 --replays all --remote` (`1119-173312-ab-175c`): 760 native games with replays, main 632–128 =
  0.832 [0.797, 0.866].
- **Sites without touching awubot's play.** `oxawu` (branch `t3/ox-probe`, never merged) is awubot renamed, with
  code added only on its two catch paths: `catch (Exception)` prints `OXEXC` and the stack trace, a new
  `catch (Error)` prints `OXERR` and the stack trace and rethrows (`D:tools/mkprobe.py`). The normal path's
  instructions are unchanged. The same 759 games (the 760th ran past the launch) were played with `oxawu` on the
  Java engine with robot output on (`D:tools/oxplay.py`, `tools/run` run `run-1119-174223-5347`): **all 759 equal
  the native awubot games** in winner, round, win type, end state and the 100-round series of both teams.
- **Join.** Each probe event (robot id, round, site) is joined to the native replay of the same game
  (`D:tools/OXStats.java`, `D:tools/oxagg.py`): the robot's type, position, carry state, the tile in front of it at
  the end of its turn, the robots within Chebyshev 4, our actions within 5 tiles in that round and the one before,
  and for kings the robots in vision (any body tile within r² 25 of the king's centre, as `senseNearbyRobots`).

## 2. The failure table

### 2.1 Robots destroyed by their own uncaught Error (759 games)

| site | type | deaths | games | front tile after the move or turn | carried rat |
|---|---|---|---|---|---|
| `TryThrow.run:156` (move, then `assert(rc.canThrowRat())`) | rat | 112 | 90 | cat 88, empty 23, dirt 1 | ours 94, own 1, not shown 17 |
| `TryThrow.run:151` (turn, then the same assert) | rat | 37 | 34 | cat 37 | ours 33, not shown 4 |
| any king site | king | 0 | 0 | | |

- By round: r0–100 20, r100–200 35, r200–400 33, r400–800 32, r800–1200 11, r1200–2000 18.
- 0.164 a game in the 128 games awubot won, 0.203 in the 631 it lost.
- Our robots nearby: none free (not carried) within 4 tiles at 53 of 149, 1–2 at 64, 3+ at 32; a cat within 4 at 132.
- An "empty" front is a tile whose blocker the replay does not place at that instant: a flying rat (impassable,
  not returned by `senseNearbyRobots`), or a robot that left before the round's record.

### 2.2 Turns lost to its own code

| | rat | king | ours (rat, king) |
|---|---|---|---|
| turns | 9,267,569 | 681,031 | |
| bytecode overruns | 14,149 (0.153%) | 4,745 (0.697%) | 0.117%, 0.161% |
| caught exceptions (rest of turn lost) | 4,263 (0.046%) | 1,537 (0.226%) | not probed (S2: 149 turns in 62 games after its fixes) |
| robots with ≥ 3 consecutive lost turns | 323 (130 games) | 33 (33 games) | |

Caught exceptions by site (events = turns; `D:agg2.txt`):

| site | type | turns | games | longest run | with our robots within 4 | trigger |
|---|---|---|---|---|---|---|
| `BabyRat.considerPlacingCatTrap:42,68` | rat | 2,601 | 180 | 55 | 8% | cooperation, a cat facing the rat within d² 9, no enemy known; `senseRobotAtLocation` of an empty tile beside the cat is null |
| `Game._turn:50` (turning cooldown) | rat | 817 | 331 | 3 | 62% | a turn without `canTurn` (S2's Pathfinder fix) |
| `BabyRat.attack:249` | rat | 523 | 238 | 6 | 30% | attack after a move: action not ready, target out of view, gone |
| `KingSpawningStrategy.trySpawn:90` | king | 546 | 28 | 58 | 25% | §3.2 |
| `RobotTracker.updateRobots*` (`sortedRobots[25]`) | king | 991 | 9 | 175 | 16% | ≥ 26 robots sensed (§3.3) |
| `Game._rawMove:40` (occupied tile) | rat | 137 | 115 | 1 | 95% | a cached `canMove` after its own throw (S2) |
| `GlobalArray.registerGlobalCheeseMine:390` | rat | 79 | 4 | 1 | 14% | a 0 in the cheese list mid-rewrite (S1, S2) |
| `CombatCommon.canMove:41` (null move) | rat | 66 | 52 | 3 | 98% | `TryFight` with no move (S2) |
| `TryTrap.run:107,109` | rat | 32 | 27 | 1 | 94% | action not ready, occupied, trap present |
| `AntColony.preTick:119` | rat | 3 | 1 | 3 | 0% | the torn king location (S1) |

- King overruns happen with none of our robots in its vision in 4,427 of 4,745 (0.69% of such king-turns; 0.81%
  with ours in view): they are not our doing.
- Swarms do not push its rats over the limit: a rat overruns on 0.155% of its turns with none of our free robots
  within Chebyshev 4 (7.5 M turns), 0.16–0.19% with 1–5, 0.24% with 6 or more (79 k turns; `OXStats` with
  `RATOV=1`, `D:ratov/`). Its combat searches are budgeted by the bytecodes left (docs/architecture.md §1.5).

## 3. Triggers, and whether our play can cause them

### 3.1 Errors that would kill a king

| site | trigger | reachable by us |
|---|---|---|
| `GlobalArray.writeCheeseMines:428` `assert(false)` | the king's cheese list holds ≥ 26 mines (`n < slots` fails) | no: only `bridges`, `bridgesCatless`, `quadrants`, `sieveofriches`, `tminusten` have that many; in our 16 games on the two in `goal` we killed every awubot king first (`sieveofriches` r392–945) or the game ended by r107 (`tminusten`); we cannot make awubot see mines faster |
| `RobotTracker` `assert(false)` (`switch` default) | > 43 robots sensed | no: the most any awubot king sensed in 760 games was 32, in our losses 23 |
| `KingCombatStrategy:361,436` | an enemy or cat on the king's centre (`directionTo` = CENTER) | no: that tile is the king's |
| `Game._turn:48` `assert dir != CENTER` | a king turning to CENTER | not seen; the spawn path calls `rc.turn` directly (§3.2) |
| `GlobalArray:94–103, 222` (spawn info) | spawn more than 4 tiles from the king a round later | no: a king moves one tile a round |
| `EnemyManager:128–141, 216` | an enemy encoding out of range | no: positions are clamped, HP over 100 encodes as 31 |

### 3.2 The spawn-to-centre stall

`KingSpawningStrategy.runTick` targets the map centre `(W/2, H/2)` when no enemy is in view and the spawn rotation
(`ratCounter % kings`) names this king (`:52–56`). `trySpawn` picks the buildable ring tile nearest the target; when
the king sits at Chebyshev distance 2 from the centre and the centre is buildable, that tile is the target itself, and
`rc.turn(best.directionTo(target))` throws (turn to CENTER). The catch ends the king's turn: no combat, formation or
mining step, no postTick writes. `ratCounter` advances only on a spawn, so the rotation keeps naming this king, and
the other kings spawn only toward enemies in their own view.

- 28 games (3.7%), 546 king-turns, 77 runs: 29 last one round, 18 last ≥ 10, the longest 58. awubot won 13 of the
  28 (it wins 17% of all games): the stall is on maps where awubot is strong (`EscapeTheNight` 4, `averyfineline` 4,
  `insertmeowname` 2, `gridlock` 3, `UncomfortablyClose` 3, `dirtfulcat` 3).
- What ends a run: 17 of 77 with one of our robots in the king's view in the next round (an enemy becomes the
  target), 58 with none (an awubot rat on the centre tile, cheese under 100, or the cost above the threshold), 2 by
  the king's death or the game's end.
- Our play cannot put an awubot king there. It can only end a stall by being seen, which it did 17 times.

### 3.3 The crowd freeze

`RobotTracker.updateRobotsA/B` copies the sorted robots into `sortedRobots[25]`, so a 26th robot throws in the king's
preTick, after `GlobalArray.preTick` has marked it alive and written its location: the king keeps its rats' liveness
and deliveries but does nothing else. 991 such king-turns in 9 games (`rose` 6, `fullofsecrets` 2, `cheesebottles` 1), runs up to
175 rounds, almost all awubot's own rats (median 27 sensed, 0 of them ours, at most 4); awubot lost all of them. Reaching 26 in another
game needs about 15–20 of our robots inside its king's r² 25, against a most-ever of 9 in a loss (§4).

### 3.4 The throw crash

`TryThrow.run` plans a move `md`, a facing `td` and a throw over a 9×9 occupancy grid filled from sensed rats and
kings only (`populateThrowGrid`, `:3230`): cats are not obstacles. The engine requires the tile in front empty and
passable (`E:world/RobotControllerImpl.java:1405`). So a carrier facing a cat after its move asserts and dies a round
later (the `finally` yields first); the rat it carried is dropped on its tile. To provoke it we would have to be
grabbed next to cats, which is what the throw is for.

## 4. Price

### 4.1 Losses a provoked failure could have turned

Our 128 losses of the 760 (`D:lossA/`): 101 by king kill, 27 on points; awubot ended with 1 king in 85, 2 in 37, 3 in
4, 4 in 2.

| awubot kings' sensing in our losses | losses |
|---|---|
| any of our robots ever in an awubot king's vision | 99 |
| ≥ 3 of ours at once | 35 |
| ≥ 6 of ours at once | 2 |
| ≥ 10 of ours at once | 0 |
| ≥ 20 robots of any team at once | 1 |
| ≥ 26 robots (the crowd freeze) or ≥ 44 (the crowd death) | 0 |

Over all 681,031 awubot king-turns of the 759 games, 39,344 (5.8%) had one of ours in view, 4,239 had ≥ 3, 458 had
≥ 6, 9 had ≥ 10. No loss had a king a reachable failure could remove: the death triggers are out of reach (§3.1),
the crowd freeze needs more robots at the king than any loss ever had, and the stall is placed by awubot's own king.

### 4.2 What the failures are worth: oracle pairs

Each pair is two awubot copies whose normal-path instructions are identical (`D:tools/mkoracle.py`), differing only
where the failure fires, so games without it are the same game in both and the paired difference is the failure's
own effect. Both play main on the same 380 `goal` cells (760 games each, seed base 16002, `--ref` the second copy):

| pair | what differs | awubot's score: failing − fixed, 95% CI | cells changed | run |
|---|---|---|---|---|
| `oxtnull` / `oxtfix` | the throw crash (`TryThrow:151,156`): crash, or keep the move and skip the throw | +0.004 [−0.005, +0.013] | 13 (8 / 5) | `1119-175513-ab-b5d0` |
| `oxsnull` / `oxsfix` | the spawn-to-centre stall: turn to CENTER and throw, or skip the turn and spawn | −0.003 [−0.008, +0.003] | 4 (1 / 3) | `1119-180042-ab-6867` |
| `oxknull` / `oxkfz` | a king that senses any of our robots loses its turn (the freeze, at the most generous trigger) | **−0.071 [−0.095, −0.047]** (0.175 → 0.104) | 76 (12 / 64) | `1119-175520-ab-1e04` |

- The freeze oracle is the value of awubot's king acting when our robots are in its view (fleeing, spawning toward
  them, fighting): 7.1 pp. It is not a failure we can cause: the only freeze our robots feed is the crowd freeze,
  and it needs 26 robots in that view (§3.3, §4.1). It prices W3's "killing an exposed king", not an OX arm.
- The throw crash killed 174 of `oxtnull`'s rats and none of `oxtfix`'s, and changed nothing: the crashing carrier
  dies a round later and drops our rat, the fixed one keeps it and throws it a turn or two later; the trade is even.

## 5. Arms

None was built: every failure is either worth nothing to us or out of our robots' reach.

| failure | worth to us | can our play cause it | arm |
|---|---|---|---|
| king Error deaths (§3.1) | a game each | no trigger reachable: 32 robots sensed at most (44 needed), the 26-mine overflow comes after we have killed those kings | none possible |
| crowd freeze (§3.3) | up to +7.1 pp at the most generous trigger (§4.2) | needs ≥ 26 robots in the king's view; our losses had at most 23, 9 of them ours | where we bring that many we already bite the king; none |
| throw crash (§3.4) | −0.4 pp [−1.3, +0.5] | only by being grabbed next to a cat | none |
| spawn-to-centre stall (§3.2) | +0.3 pp [−0.3, +0.8] | awubot places its own king; we end a stall only by being seen (17 of 77 runs) | none: keeping out of a stalled king's view could lengthen at most those 17 runs, a fraction of 0.3 pp |
| rats frozen before a cat (cooperation) | not priced | only by steering cats onto them (W3: cats as weapons) | left to W3 |
| overruns from swarms (§2.2) | | 0.155% → 0.24% of rat turns with ≥ 6 of ours near | none |

## 6. Tools and runs (`D:`)

- `tools/mkprobe.py` (the `oxawu` probe), `tools/oxplay.py` (Java games with robot output, one game a task),
  `tools/OXStats.java` (replay diagnostics: deaths, overruns, king vision histograms, snapshots at (id, round) with
  the front tile; compile against the 1.2.6 jar into `tools/v2`), `tools/oxagg.py PROBE_RUN NATIVE_RUN OUT` (the join
  and the tables), `tools/mkoracle.py` (the oracle pairs). Output: `agg2/` (`events.jsonl`, `games.jsonl`),
  `agg2.txt`, `lossA/` (our losses), `statsA/` (all 760 replays, OXStats v1), `stall_runs.json`.
- Probe branch `t3/ox-probe` (worktree `D:wt`, never merged): `oxawu`, the oracle packages, `oxplay.py`.
- Runs: `1119-173312-ab-175c` (main vs awubot, replays), `run-1119-174223-5347` (the probe on Java),
  `1119-175513-ab-b5d0` (throw pair), `1119-180042-ab-6867` (stall pair), `1119-175520-ab-1e04` (freeze pair).
