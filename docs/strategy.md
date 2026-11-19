# strategy.md: the macro game plan of `challenger` (= awubot final)

Scope: what the bot does outside combat micro. That means the strategy selection of rats and kings, the economy, spawning, king formation and movement, and non-combat cat handling. Combat micro (`robot/rat/combat/**`, `KingCombatStrategy`) is in **combat.md**. Comms layout, pathfinding and `CellTracker` are in **architecture.md**. Engine facts are in **rules.md**. How to run matches is in **testing.md**.

All paths are relative to `src/challenger/`. Line numbers for generated classes point at the `.jinja2` template unless they say `.java`. "Inference" marks claims that come from reading the code but were not observed directly.

---

## 0. TL;DR: how this bot wins

1. **It is an economy + defence bot that backstabs on first contact.** No strategy code checks `rc.isCooperation()`; the only check is in cat-trap placement (`BabyRat.java:36`). Any visible enemy rat sends a rat into combat and it bites, which triggers the backstab. The first rat always walks to the map centre (`MinerStrategy.java:114`). In 19 instrumented games (18 ran to completion), the backstab came at **rounds 4–21 in 13 of them** and at round 152 in one; 5 games on 2 maps stayed cooperative to the end. Most games end by king kill (15 of the 18 completed). Points decided only the 3 games that reached round 2000.
2. **Every baby rat runs the same `MinerStrategy`.** There are no roles: the spawn payload is random and never read. Each turn the rat checks, in order: combat, formation, go home, visible cheese, `raw>50 → home`, then travel to a mine (80%) or explore (20%). Specialisation exists only through `rc.getID()%3` (cat hunters) and `%10==7` ("troll").
3. **Spawning is capped by rat cost, not by need.** Normally a team gets 16 rats (12 on maps of area ≤ 900 before round 400), 4 fewer on mine-poor maps of area ≤ 1600 (§5.2). The cap rises to 32 when global cheese ≥ `2000 − t/2`, and is removed when global cheese ≥ `3000 − 0.75t`. In contested games the bot sits at ≤ 16 rats and replaces about one dead rat every 5–6 rounds.
4. **More kings = the long-game lever.** A coordinator king picks a far, unassigned mine, and baby rats within 25 tiles of it converge until one of them can `becomeRatKing`. When it works, income jumps (on `far` the cheese transferred per 250 rounds went from ~1k to ~5k). It mostly fails in contested games. On maps larger than 1200 tiles it needs ≥ 20 rats, while the normal spawn cap is 16 (§6.3).
5. **Kings mine.** A king walks toward the centre (stopping just outside a "taboo" box) until it learns of an unassigned mine. It then parks on that mine and picks up cheese inside the mine's spawn box (king pickups count as transferred cheese, see rules.md).

---

## 1. Timeline

| Phase | Rounds | Kings do | Rats do | Transition trigger |
|---|---|---|---|---|
| **Opening** | 1–~20 | Spawn 1 rat per round (action cd 10) from the ring at Chebyshev distance 2, on the tile nearest the map centre, facing the centre (`KingSpawningStrategy.java:76-97`). Start walking toward `centerTarget` (§6.2). | The rat spawned in round 1 acts first in round 2 and walks to the **map centre** (`MinerStrategy.java:114`), allowed to dig dirt (`Pathfinder.init` sets `dirtAllowed` when `time≤2`, `Pathfinder.java.jinja2:469`). Rats with their first turn in rounds ≤ 20 explore along random rays from the king, ending 50–100% of the way to the map edge (`MinerStrategy.java:141-173`). | The cap of 16 rats (12 on area ≤ 900) is reached around rounds 12–16 (observed `ratCounter=16` at round 16–21). |
| **First contact / backstab** | ~4–25 on most maps (152 on `far`; never on `mining` or `peaceinourtime`) | – | The first rats meet near the centre. `CombatStrategy` grabs any rat that sees an enemy, and the bite triggers the backstab. | Contact. Nothing in the code avoids it or chooses its timing. |
| **Early economy** | ~20–300 | Learn mines through comms. Walk to the nearest unassigned known mine (`KingMiningStrategy.java.jinja2:276-301`), park there and pick up cheese. Keep the rat count at the cap. Spawn toward visible enemies when attacked (§5.2). | Explore until ≥ 3 mines are known (or until `time > W·H/8` if ≥ 1 is known). Then pick a mine (80%, weighted by **distance**) or an unseen 3×3 unit (20%). Pick up all cheese in reach, go home at the distance-scaled threshold (§5.3) and retrace the pheromone trail (`AntColony`). | Wealth (§5.2) or formation conditions (§6.3). |
| **Mid game** | ~300–800 | If rich: spawn up to 32 rats, or without limit (§5.2). The coordinator tries to form king #2 (#3 needs ≥ 9 mines and ≥ 40 rats) at a far mine (§6.3). | Rats within 25 tiles of the formation mine drop mining and converge. Combat whenever enemies are seen. From round 501, rats with `id%3==0` attack cats anywhere when they have no raw cheese, the team holds > 1000 cheese, no enemy is visible and ≥ 16 rats are alive (`CombatStrategy.java:447`). Round 800: the king lock-in starts (§6.3). |
| **Lock-in** | 800–1200 | Below 3 kings the coordinator forms without the 20-per-king rat gate and the far-mine floor, with ≥ 12 rats, ≥ 1000 cheese and the known-mine thresholds (§6.3). | Same as mid game. | Round 1200: the engine forbids a new king to a team with ≥ 2 (rules.md §6); formations stop then. |
| **Late game** | 1200–1800 | Spawning and mining continue. A team with only 1 king forms a 2nd at once: ≥ 12 rats, ≥ 150 cheese, any valid mine (§6.3). | Same as mid game. | – |
| **Endgame** | 1800–2000 | As in the late game. | From round 1800, **all** idle rats hunt cats (same gate minus the `id%3`). After round 1900, a rat in an active formation becomes king **anywhere** `canBecomeRatKing` allows (`FormationStrategy.java:68`). | Round 2000: points (rules.md §1). Tiebreaks are global cheese, then rats alive. Nothing else optimises for the scoring. |

---

## 2. Per-unit decision logic

### 2.1 Turn skeleton (`RobotPlayer.java:35-88`)
`Game.preTick → Util → CellTracker → GlobalArray.preTick (king positions, mines, symmetry, nearest king) → RobotTracker.preTick (sort visible robots) → EnemyManager.preTick (clear) → MapManager → Squeaker.preTick (read ≤ 5 squeaks)`, then `RatKing.run()` or `BabyRat.run()`, then `Squeaker.postTick` (only if > 1000 bytecode is left), `GlobalArray.postTick` and `Game.postTick`. Every exception is swallowed by `catch (Exception e)`, which ends the turn silently. `AssertionError` is not an `Exception`.

### 2.2 BabyRat (`robot/rat/BabyRat.java`)

```
init (:25-32): payload = GlobalArray.loadSpawnInfo()   // payload is NEVER used
               primary = MinerStrategy; combat = CombatStrategy; Pathfinder.init; AntColony.init
run (:79-121):
  storeVisibleCells; AntColony.preTick
  tryPickUpCheese()            // own tile + 3 front tiles, every turn, free (:143-154)
  tryTransferCheese()          // raw>0 and any king centre within d²≤18 → transfer ALL raw (:157-176)
  if beingCarried: if hp<15 and carrier is enemy → disintegrate(); return   (:90-96)
  if beingThrown: return
  if combat.runTick():         // any visible enemy, a fightable cat, or a squeaked enemy (combat.md)
      considerPlacingCatTrap(); return
  RobotTracker.reset(); CellTracker.checkSymmetry(); RobotTracker.updateHistoricalCatLocs()
  considerPlacingCatTrap()     // §7
  MinerStrategy.runTick()
  combat.miniTick()            // opportunistic ratnap/attack/trap if action still ready
  executeGlance()              // if turning still ready: alternate ±90° looks (:123-140)
```
Pickup and transfer run again after **every** move and turn (`rawMove`/`turn`, `:179-215`), so rats bank cheese on the way past a king.

**When combat takes over** (`CombatStrategy.java.jinja2:143-206`): the BFS sources are every visible enemy (rat or king), every cat for which `shouldFight(cat)` holds, and, if there are none, the nearest enemy heard by squeak. If the multi-source BFS gives `moveMask == 0` (unreachable), combat returns false and the macro strategy runs. Combat therefore pre-empts mining, going home and formation whenever an enemy can be reached. Cats are pre-empted only near the king or under the hunt gate (§7).

### 2.3 MinerStrategy: the rat state machine (`robot/rat/MinerStrategy.java:37-81`)

```
if BabyRat.transferSwitch: bannedMines.clear()                 // after each successful transfer
if raw == 0:                 goHome = null
elif goHome == null && shouldGoHome(): goHome = new GoHome(); cheese.target = null
1. Formation.runTick()   → return     // active global formation & within join radius (§6.3)
2. goHome?.runTick()     → return     // carrying, threshold hit (§5.3)
3. Cheese.runTick()      → return     // any cheese tile in vision it can get next to: walk to the LARGEST pile
4. if raw > 50: goHome = new; run; return
5. loop: while goSomewhere==null or !goSomewhere.runTick():
       known = knownCheeseMines.size
       if known==0 or (known≤2 and time ≤ W·H/8): target = pickExplorationTarget()
       else: 60% pickExplorationTarget(), 40% pickSeenMine()   // Tune.EXPLORE_P (TN; was 20/80)
       goSomewhere = Traverse(target, d²≤2)
```
- `pickExplorationTarget` (`:113-139`): in round 2, the map centre. In rounds ≤ 20, a ray from the nearest king (§1). Otherwise a random **unseen** 3×3 unit from `MapManager.localTracker`, resampled ≤ 6 times if within d² ≤ 36 of any king. If every unit has been seen, a uniform random tile.
- `pickSeenMine` (`:83-111`): over all known mines, skip any mine currently in vision, any in `bannedMines` (visited since the last transfer) and any with a king centre within d² ≤ 2 (the king works that one). The weight is `⌊√dist⌋`, where dist is from the nearest king if the rat is within 7 tiles of it, else from the rat. **Farther mines are more likely.** The chosen mine is banned until the next transfer.
- `shouldGoHome` / `shouldReturnForHealth`: see §5.3.

### 2.4 RatKing (`robot/king/RatKing.java`, `RatKingStrategy.java`)

```
run (:25-38): storeVisibleCellsKing; RobotTracker.reset(); tryUnburyIfStuck()
  RatKingStrategy.runTick (:24-29). ALL FOUR run every turn, in this order:
    1. KingSpawningStrategy            ≤ 1 spawn per turn (§5.2)
    2. KingCombatStrategy              only if enemies or cats are visible: flee, dirt, bite (combat.md)
    3. KingFormationCoordinatorStrategy state machine (§6.3)
    4. KingMiningStrategy              move to mine / cheese / centre, pick up cheese (§6.2)
```
There is no priority return. The first strategy to move uses the king's movement (cooldown 40), so a combat flee blocks mining moves that turn. `tryUnburyIfStuck` (`RatKing.java:75-227`): if none of the 16 ring tiles at Chebyshev distance 2 is passable non-dirt, dig. It prefers 2–3 rows toward the map centre (whichever axis has less dirt). Otherwise it digs the best-scoring ring dirt: `+25` per king step it would unblock, `+200` if next to a wall, `−10·d²`, `−10·(d² to the nearest passable tile)`. Before moving along a path, kings also dig the dirt in front (`digDirtInDirection`, `:48-73`).

---

## 3. Strategies in detail

| Strategy | Goal | Entry | Exit | Key algorithm | Comms | Weaknesses / blind spots |
|---|---|---|---|---|---|---|
| `MinerStrategy` (`robot/rat/MinerStrategy.java`) | Collect cheese and explore | Always (the only primary strategy) | Never | §2.3 | Reads mines (slots 38–63) and king positions | No roles. Exploration (60% of trips, `Tune.EXPLORE_P`; 20% before TN) never stops, even late or with the map fully seen; it is load-bearing (AB) and more of it gains on rich maps (§9). Distance-weighted mine choice sends rats to far, likely contested mines (`:100-102`). Ignores enemy and cat positions when choosing targets. Mines under a king are excluded. |
| `CheeseStrategy` (`robot/rat/CheeseStrategy.java`) | Pick up visible cheese | Any tile in vision with cheese > 0 | Target emptied or Traverse gave up | Target = the **largest** pile in vision among those it can pick up after ≤ 6 steps through passable cells (`Pathfinder.computePickable`: allies block, and dirt unless dirt pathing is on), ignoring distance; `Traverse(target, d²≤1)`. Vision ignores walls and dirt: before the reach check, chases of piles seen across them took ~1,700 rat-turns a game in r150–600 of long rich-map losses at 0.4 cheese a turn (MC probe) | – | No coordination, so many rats chase the same pile (inference). Scans `senseNearbyMapInfos()` every turn. Ignores danger. Not reached while going home or in a formation, but `tryPickUpCheese` still grabs anything adjacent. |
| `TraverseStrategy` (`robot/rat/TraverseStrategy.java`) | Get within d² of a target | Created by others | Arrived, `Pathfinder.givenUp()`, or (raw < 20 **and** stuck **and** `time%25==0`) | `BabyRat.autoMove(Pathfinder.getMove)`. Every 25 turns it checks displacement (≤ 8 over 25 turns, ≤ 18 over 50) and sets `stuck` | – | Once set, `stuck` is never reset. Rats carrying ≥ 20 raw never give up because of `stuck`. |
| `GoHomeStrategy` + `AntColony` (`robot/rat/goHome/*.jinja2`) | Deliver raw cheese | `goHome != null` and raw > 0 | raw == 0 | Each rat lays its own trail: `mark[king][y][x] = min(old, curMark++)` on each move, and `curMark` resets to `base+5` on reaching home. Going home = BFS to the **lowest mark** among reachable tiles in the 11×11 window (`GoHomeStrategy:57-107`). If there is no mark, `Traverse(king, 8)`. When the king moves 1 tile, `base--` (all marks age). If it moves farther (new king or death), that king's marks reset (`AntColony:50-69`). Within d² ≤ 9 of the king-adjacent tile it turns to face the king so the transfer works (`:28-38`). | King positions (slots 2–8) | The trail is per rat and only covers places that rat has been. A rat that never visited home follows `Traverse`. Raw cheese slows rats ×(1 + 0.01·raw) (rules.md §4), yet pickup is unconditional (largest observed transfer: 891). |
| `FormationStrategy` (`robot/rat/FormationStrategy.java`) | Turn 7+ rats near a far mine into a king | A global formation target exists and, when joining, the rat is within d² ≤ 625 of the formation mine (`:89-121`) | `isComplete()`: target cleared, global < 50, 120 turns since joining, or a cat seen in the last 4 rounds could see the target (`:27-51`) | Path to the target. With `allyCount ≥ 7` and d² ≤ 2, the lowest-ID rat steps onto the exact target (`:79-83,123-151`). `becomeRatKing` when d² ≤ 8 (**or anywhere after round 1900**) and `canBecomeRatKing()`. Yields to going home if raw ≥ 40 and home is ≥ 25 d² closer (`:57-63`). | Slots 9–13. Clears the formation if it formed exactly on the target. | **The timeout is a 1-tick pause**: `tryActivate` re-activates on the next tick with a fresh start time, so rats stay in the formation until the king clears it. `allyCount` counts visible allies plus squeak sources, not the 3×3. Every rat within 25 tiles joins, not just 8, which pulls the army off mining (observed: 9–20 joiners per attempt). A cat sighting skips the strategy for one tick, so rats jitter. |
| `KingSpawningStrategy` | Keep the rat count at the cap; defend | Every king turn | – | §5.2 | Writes the spawn record (slots 0, 22–31), whose payload is unused | The enemy-neighbour count is always 0 (`:44-46` counts allies among enemy-team robots), so the spawn target is simply the nearest enemy. Spawning does not look at how long the king can survive on its cheese, only at ≥ 100 cheese. |
| `KingMiningStrategy` (`.jinja2`) | Park the king on a mine and pick up cheese | Every king turn | – | §6.2 | Writes the mine assignment (slots 16–20) | Walks to the centre while no mine is known (cats spawn there; §8 has a king killed by a cat). On 28 of the 95 `goal` maps the start king has no 3×3 path to any mine (§6.2). |
| `KingFormationCoordinatorStrategy` | Decide when and where to form a new king | Every king turn | – | §6.3 | Slots 9–13 and 16–20 | Before round 800, gated by ≥ 20 rats (unreachable at the cap of 16), ≥ 5 known mines, and a mine ≥ 20 tiles from all kings; from round 800 the lock-in relaxes the gates, and from round 100 so does a game still in cooperation with one king (§6.3). The cat abort (`:329-334`) is **dead**: the `formationTarget` field is shadowed at `:210` and `nearestCat` is only set in the uncalled `isNormal()`. If the coordinator dies, its formation stays in the array (inference). |
| `ExploreStrategy`, `MicroTestStrategy` | – | **Dead code** (never instantiated by the live bot) | | | | |

Other dead or unused code: `KingFormationCoordinatorStrategy.isNormal/findFormationSpotNearMine/isValidFormationCenter`, `GlobalArray.tryClaimFormationCoordinator`, `Squeaker.squeakLureSignal`, `Util.guessEnemyLocs/getEnemySpawnList/genAggroTarget/getSymmetricLocation*`. Symmetric mine inference was added in `9e47039` and removed in `5da8de3`.

---

## 4. Comms touchpoints for macro (details in architecture.md)

| Slot(s) | Written by | Read by | Macro use |
|---|---|---|---|
| 0 `COUNTER` | kings (after spawning) | all | round-robin spawning between kings |
| 2–8 | kings | all | king positions and liveness; `nearestRatKing`; `countRatKings()` |
| 9–13 | coordinator king; formed rat clears | rats, kings | formation target = formation mine (the same tile), coordinator id |
| 14 `ALIVE_RAT_COUNT` | kings: `(cost−10)·2/5` = 4·⌊n/4⌋ | rats | cat-hunt gate `≥16` |
| 16–20 | each king (own mine every tick) | kings | mine dedup: d² < 25 of a live king's assignment counts as taken; a dead king's slot is ignored (`getKingMineAssignment`) |
| 21 `KING_EXPAND` | king stuck for 17 turns | `RobotTracker.reset` | `isKingExpanded(i)` tests `kingId`, not `i` (`GlobalArray.java:125`), so for rats (`kingId=-1`) it is always false |
| 22–31 | spawning king | new rat | spawn record. **The 13-bit payload is free for role assignment.** |
| 38–63 | kings | all | known mines, gossiped by rat squeaks within 4 tiles of a king |

Squeak priorities (`Squeaker.postTick`): a rat that saw a cat in the last 4 rounds sends **only** the panic squeak, and only when the cat is within d² < 25 of the nearest king. Otherwise: near a king, symmetry then map chunk. Then a visible enemy. Then mines.

---

## 5. Economy

### 5.1 Flows
- **Income** is ≈ 2.25 cheese per round per mine (rules.md §5), shared with the enemy and the king. King pickups also count as "transferred" for score (rules.md §1). Raw cheese absorbed by a formation does not count.
- **Sinks**:
  - king upkeep, 2 per king per round;
  - rat spawns, `10 + 10·⌊n/4⌋`;
  - bites with bonus damage, 2 cheese for +2 when `allCheese ≥ 300` (`CombatStrategy.java.jinja2:440-443,484-488`);
  - rat traps, 30 each, only if rat cost ≥ 70 or raw ≥ 20 (`TryTrap.java.jinja2:66`);
  - cat traps, 10 (§7);
  - digging, 5 per dig in engine 1.2.5. Rats may path through dirt only when `t ≥ 100 && cheese ≥ 1500 || cheese ≥ 3000` (sticky), or in their first turns (`Pathfinder.java.jinja2:19-26,469`);
  - formation, 50.
- **Observed**: in contested games, rat replacement is the dominant sink. On `sittingducks`, `ratCounter` went 16 → 149 between rounds 21 and 800 while 16–24 rats were alive. On `hatefullattice` it went 16 → 181 by round 900. In several mirror games cheese collapsed to ≈ 0 and the king died (`DefaultMedium` had 97 cheese and 4 rats at round 200; on `starvation` cheese hit 0 by round 200; on `cheesefarm` it was 8 at round 500).

### 5.2 Spawning policy (`robot/king/KingSpawningStrategy.java:22-124`)

```
def = (W·H ≤ 900 and t < 400) ? 30 : 40
if W·H ≤ 1600 and minMines < 5:       def −= 10                  // mine-poor map (E2), below
thresh = min(global/10 + 10, def)
if global ≥ 3000 − 0.75·t:           thresh = 1000            // "very rich": unlimited
elif global ≥ 2000 − t/2 and t ≥ 100: thresh = 80             // threshForNumRats(32)
if cats visible and no allies visible: thresh += 10
target = nearest visible enemy (if any)
if target == null: target = centre only if ratCounter % #kings == myIndexAmongKings   // take turns
elif hp dropped since last turn and >4 rounds since last panic: thresh += 10 (+10 more if hp ≤ 100)
spawn if target != null and cost ≤ thresh and (global ≥ 100 or cost == 10)
   at the ring tile (Chebyshev 2) closest to target; king turns so the rat faces the target
```

| Effective thresh | Max baby rats alive (cost ≤ thresh) | When |
|---|---|---|
| < 20 (global < 100) | 3 (cost 10 only) | broke |
| 20–29 (global 100–199) | 4–8 | poor |
| 20 | 8 | area ≤ 900 before round 400 on a mine-poor map |
| 30 | 12 | area ≤ 900 before round 400, area ≤ 1600 on a mine-poor map, or global 200–299 |
| 40 | **16** | normal |
| 50 / 60 | 20 / 24 | cats near and no allies / king under attack |
| 80 | 32 | global ≥ 2000 − t/2 (1950 at t=100, 1500 at t=1000, 1000 at t=2000) |
| 1000 | unlimited | global ≥ 3000 − 0.75t (2500 at t=667, 2000 at t=1333, 1500 at t=2000). The bot then hovers at that line, turning surplus into rats (observed 144 rats at round 2000 on `mining`). |

**Mine-poor maps (E2).** `minMines` is a lower bound on the map's mines: the known mines (`GlobalArray.globalCheeseMines`) plus their unknown mirrors, the smallest count over the symmetries `CellTracker` still allows (every mine's mirror is a mine). It is recomputed only when the known mines or the symmetry candidates change, and latches at 5. Offline, on all 95 goal maps, it never exceeds the true count. Every map starts mine-poor, because on those maps the bank gap builds from round 1. A probe saw rich maps latch at r51–r208. Measured against main (paired, docs/plan.md E2): +0.061 [+0.015, +0.109] on `goal` (163 cells, fresh seeds), no harm on `mines-ge8` (−0.003), no harm in the gauntlet. Before the area gate it scored +0.34 on `mines-le4` and +0.07 on `mines-5to7`. The cap 4 lower on every map scored +0.01 on `goal`: +0.23 on ≤ 4-mine maps, −0.18 on 10–12. Above 1600 tiles it gained ~0 on poor maps and leaked onto rich ones. The cap 8 lower scored −0.46 against 4 lower on `mines-le4`. Mechanism (`MP-combat`): the same spawns and deaths, but cheaper replacements. The bank at r100 is 1294 against main's 753, and starvation rounds fall from 13.4 to 3.6.

The last change before submission (`e0bd796` "more aggressive spawning when rich (untested)") changed the two "rich" lines from `max(0, t−1000)` to `0.75t` and `t/2`.

### 5.3 When a rat goes home (`MinerStrategy.java:175-213`)

| Distance² to nearest king | ≤ 25 | ≤ 100 | ≤ 225 | ≤ 400 | ≤ 900 | > 900 |
|---|---|---|---|---|---|---|
| raw-cheese threshold | 10 | 25 | 40 | 60 | 80 | 105 |

The rat also goes home if:
- `raw·2 ≥ thresh` and it has not seen cheese for 3 turns;
- global < 100 and raw ≥ 20;
- global < 30 and raw > 0;
- **health**: raw > 0 and hp ≤ 20, or (raw ≥ 150 and hp ≤ 70) / (≥ 100 and ≤ 60) / (≥ 50 and ≤ 50) / (≥ 20 and ≤ 40);
- the fallback `raw > 50` when there is nothing better to do (`:55`).

---

## 6. Kings

### 6.1 Count and lifecycle
- One king at the start. More come only from baby-rat formation. Engine limits: ≤ 5 kings, and no new king after round 1200 if the team has ≥ 2 (rules.md §6). The coordinator aborts on the same rule (round > 1200 with ≥ 2 kings).
- Each king writes its position every turn. Liveness uses a mod-3 turn counter (`GlobalArray` `markKingAlive`, `updateRatKingTurnCounters`): a king marks every round it runs in, including the round an overrun or conversion turn ends in (`GlobalArray.endTurn`), and a king with no mark for the previous round is declared dead and its slot is freed.
- **Points**: in backstab state, `% living kings` is worth 0.5. Ending with 2 kings against 1 is +16.7 points, which outweighs most cheese or cat-damage gaps.

### 6.2 Movement and mining (`KingMiningStrategy.java.jinja2`)
1. `scanForCheeseMines`: register every visible mine globally. Target the nearest visible mine not within d² < 25 of another live king's assignment (`isMineAssignedToOtherKing`). Counting its own slot made the target flip between two visible mines every turn: 83% of target changes on `goal` were such flips.
2. Otherwise `selectFromKnownMines`: the nearest such mine among the first 10 known. Neither step knows whether the king's 3×3 can reach the mine: on 28 of the 95 `goal` maps the start king has no 3×3 path to any mine (2-wide gaps pass rats, not kings), and on main 21% of king-rounds were spent pressing toward an unreachable target. Parking such kings was measured and rejected (docs/closed.md, KS). This runs only while there is no `mineTarget`; once set, the target is replaced only by step 1.
3. With no mine: `moveTowardCenter` → `centerTarget`. The map centre is always inside the **taboo box**, so the target is the nearest of four points just outside it (`:54-77`). The taboo box is the middle third for maps < 40×40, `[3/7, 4/7]` for < 50×50, else `[4/9, 5/9]`. The mine taboo check is commented out, so kings can still park on central mines.
4. With a mine: within d² < 5 of it, walk to the nearest cheese within d² ≤ 13 of the mine. Otherwise path to the mine. `tryCollectCheese` (`:126-192`) picks up all 9 footprint tiles, chases cheese within Chebyshev distance 4 of the mine, then returns to the exact mine tile.
5. A king moves only when the pathfinder's bug position is > d² 2 away (`moveIfBugTargetFar`, `:226-248`). After 17 turns stuck (spawn turns excluded) it sets `KING_EXPAND` and switches to 5×5 clearance (`is5x5`) once it reaches such a spot. Dirt in the way is dug (`digDirtInDirection`), and `tryUnburyIfStuck` handles full burial (§2.4).
6. Cats (`:87-100`). Mining never steps into the forward lane of a visible cat (`stepIntoCatLane`, which calls `KingCombatStrategy.entersCatLane`; geometry in combat.md §9); the dig toward that step is skipped too. With a cat in view the king paths at most once a turn, and not at all toward a target whose straight-line step is in a lane (`skipPath`): near a cat, `Pathfinder.getMove` costs 4–5k, and the second call overran the king's turn. Combat runs first and flees any visible cat (combat.md §9), so this gate acts only when the flee did not move the king.

### 6.3 Formation: making a new king

Coordinator side (`robot/king/KingFormationCoordinatorStrategy.java`), on every king every turn:
```
kings = king slots alive now                                                                          (:151)
numRats = 4·⌊n/4⌋ ; req = (W·H ≤ 1200) ? 8 : 12
lockIn = t ≥ 800 and kings < 3                                                                        (:173)
coop = kings == 1 and 100 ≤ t < 800 and still in cooperation                                         (:176)
ABORT (clears if I'm coordinator) if kings ≥ 5 or (t > 1200 and kings ≥ 2)
      or numRats < (lockIn ? 12 : req·kings)                                                          (:160-169)
ABORT if the king count just rose (a formation completed)                                             (:171-176)
IDLE → tryStartFormation (:199-243):
   danger = (hp < 100 and kings == 1)        // skips the checks below (not the aborts above)
   else need: global ≥ (lockIn and t ≤ 1200 ? 1000 : 150); kings < 5;
        t ≤ 1200 and not coop: knownMines ≥ MINE_THRESHOLDS[kings] = {_,5,9,15,∞}[kings]
        coop or lockIn: numRats ≥ 12
        otherwise:      numRats ≥ 20·kings (−4 if W·H ≤ 1200)
   need no active formation; pick the mine (first 10 known, unassigned, outside taboo; coop: on our side, nearer the
     first king's start than its mirror under every symmetry still possible) with max
     score = −50000·[d²(mine, any king) < max(400, W·H/5)] − Σ d²(mine, king) − d²(mine, centre) − 50·[d²(me, mine) > 400]
   if not lockIn and not coop and best score < −40000: no formation      // every candidate is too close to some king
   write target = mine, coordinator = me
COORDINATING: abort after 120 rounds, if global < 50, or on a backstab during a coop formation; go IDLE once the
   target is cleared.
```
The lock-in (KL, docs/plan.md §4.1): kings formed by round 1200 survive to the end and score (rules.md §16.19), and after
round 1200 a team with 1 king can still form a 2nd. Its gates are measured (git log of the coordinator): the 1000-cheese
reserve and the mine thresholds keep lock-in kings from starving on a weak economy or a mine-poor map; sites beside the
king or at the mine nearest a king starved more than the far mine (they share the first king's income).

The cooperation king (KB, docs/plan.md §4.2): a game that ends in cooperation scores kings at 30 (rules.md §1), so 2
kings to 1 is +10 points, and main lost 19 of its 34 cooperation endings on `goal`, most by 1–3 points. A game still in
cooperation at r100 is the signal. Without the mine threshold, because kings learn mines from their own vision (TheHeist:
none known by r200). The site is restricted to our side: the far-mine floor alone picked enemy-side mines behind the
walls of these maps, and the formations timed out. Measured on the five maps whose games end in cooperation
(t_minus_five_minutes, peaceinourtime, groveisbeingdesecrated, TheHeist, fullofsecrets): second king formed in 67% of
games (main 33%) at a median r187 (main r331), 16 tiles from home and 27 from the enemy, none starved. TheHeist does not
move: no own-side mine is known before its cats die (~r280). The same gates from an early round in contested games
(r40–r350) lost: the second king starves (docs/closed.md). From r800 a cooperation game forms under the lock-in's rules
(KF, docs/theory/kf-formations.md §2.5): on rose, in cooperation to r2000, no known mine is on our side under every
symmetry still possible, the search found no site all game, and main lost 5 of 6 games at 1 king to 2; with the
lock-in's rules it forms after the cutoff and won 79 of 80 games (main 47 of 80) on 40 fresh rose cells.

Rat side (`FormationStrategy`): §3. In practice, many rats from up to 25 tiles away walk to the mine. Once ≥ 7 allies are visible, the lowest-ID rat steps onto the tile. When ≥ 7 allied rats stand in its 3×3, `canBecomeRatKing` passes and the centre rat converts. That kills the other 8 tiles' rats and absorbs their raw cheese into global (on `far`, global cheese went 1483 at round 759 → 5418 at round 761, from rats that had picked up cheese while waiting by the mine; that cheese does not count as transferred).

Observed (§8): formations completed in 6 of 19 games (`mining` vs quals ×2, `mining` mirror ×2, `far` ×2, `sittingducks`, `Meow`, `hatefullattice`). They failed every time on `cheesefarm` (8 attempts in 2 games) and `peaceinourtime` (9 attempts in 3 games). Every completed formation came in a game where the bot was rich or uncontested at that moment.

---

## 7. Cats outside combat micro

| Mechanism | Where | Behaviour |
|---|---|---|
| Don't walk into a cat | `BabyRat.autoMove`, `BabyRat.java:226-233` | Cancel a forward step if the next tile is within d² ≤ 5 of a cat **and** inside the cat's 180° facing cone. Strafes are not checked. |
| Cat traps | `BabyRat.considerPlacingCatTrap`, `:34-77` | Only in **cooperation**, with no enemy visible or squeaked, and the rat within d² ≤ 9 inside the cat's **90°** front cone. Place a cat trap (10 cheese) on an adjacent tile one step from the cat, turn to keep the cat in view, step away, and try again. **Bug (observed):** `rc.senseRobotAtLocation(bruh).location` at `:42,68` throws a NullPointerException when that tile is empty. `RobotPlayer` swallows it, so the rest of the turn is lost (step away, `MinerStrategy`, squeaks) while the rat stands in front of the cat. Seen 2× against 11 trap placements on `peaceinourtime`. |
| Kings avoid the centre | `KingMiningStrategy` taboo box | Initial walk stops outside the central box where cats spawn. |
| Formation pause | `FormationStrategy.java:35-48` | Skip the strategy while any cat seen in the last 4 rounds could see the target (cone for this round, 360° for older sightings; r² = 17). |
| Panic / lure squeak | `Squeaker.java:173-178,215-217`; commit `25af0f0` | A rat that has seen a cat in the last 4 rounds sends a squeak, but only if a visible cat is within d² < 25 of the nearest king. Cats in EXPLORE mode chase squeaks, so this pulls the cat to the rat. **Bug**: `7<<29` is negative, so receivers decode it as an *enemy* packet and register two phantom 24-HP enemies at the sender's position + (−4,−4) and + (3,−3) (`Squeaker.java:91-105`, `EnemyManager.decodeEnemy`). When nothing real is visible, those phantoms can pull nearby rats into combat mode (`funnyTarget`, combat.md) (inference). They also block cat-trap placement for the rats that receive them (the `numKnownEnemies == 0` gate). |
| Squeak silence near cats | `Squeaker.java:172-181` | Rats that saw a cat in the last 4 rounds send no mine, map or enemy squeaks, so information stops flowing near cats. `catHist` is only refreshed outside combat, so it can be stale. |
| Fight cats near the king | `CombatStrategy.java:446` | Any cat within d² ≤ 36 of the nearest king is a combat source for every rat that sees it. |
| Cat hunting | `CombatStrategy.java:447` | Rounds > 500, `allCheese > 1000`, no visible enemy, raw == 0, slot 14 shows ≥ 16 rats, and (`id%3==0` or `t ≥ 1800`). Those rats fight cats anywhere. |
| Spawn on cat | `KingSpawningStrategy.java:34-36` | +10 threshold if the king sees a cat and no allies. |
| King flee, lane exit, dirt walls, cat bite | `KingCombatStrategy` (combat.md §9) | Flees every visible cat (weight 5). When the king's 3×3 is in a cat's forward lane, a move that leaves the lane scores +30 and one toward an open exit +15, so the king steps across the lane instead of fleeing along it. B3 (wrong ring cells for N/NW) is fixed. With a cat within d² 20 it places dirt toward the cat. When no enemy is within d² 8, it bites the nearest cat: 10 cat damage, no cheese, no backstab. |
| King stays out of lanes while mining | `KingMiningStrategy` (§6.2 item 6) | No mining step into a visible cat's lane; with a cat in view, at most one path computation a turn. |

Cat damage is worth 0.5 of the score in cooperation and 0.3 in backstab. Apart from the round-500 hunt gate and opportunistic traps, the bot does not farm it.

---

## 8. Observed behaviour (instrumented copy; see the caveat)

The method: a copy of `challenger` renamed `probe` in `/tmp`, with `System.out.println` in `RatKing.run` every 100 rounds and at formation, transfer and backstab events. Engine 1.2.5, seed 0, probe always team A. **Caveat:** prints cost bytecode and change outcomes. The `cheesefarm` mirror flipped from a win (round 2000) to a loss (round 526) after more prints were added. Treat this table as qualitative.

| Map | Opponent | Result | Backstab round | Kings formed (round) | Notes |
|---|---|---|---|---|---|
| DefaultMedium | quals | W r238 (kill) | 21 | – | |
| DefaultLarge | awubot | L r1826 (kill) | 16 | – | 16 rats at most; cheese 200–1100 for 1000 rounds |
| DefaultSmall | awubot | W r154 (kill) | 11 | – | |
| cheesefarm | awubot | W r2000 (points) | 15 | 0 / 7 attempts | |
| toomuchcheese | awubot | L r880 (kill) | 12 | – | only 4 mines known, so a formation was never possible |
| starvation | awubot | L r280 (kill) | 14 | – | cheese 0 by round 200 |
| peaceinourtime | awubot | L r480 (kill) | never (coop) | 0 / 3 | **King killed by a cat during cooperation** (still coop at death, so only a cat could deal damage), with 28 rats and 1700 cheese |
| DefaultLarge | quals | L r238 (kill) | 16 | danger attempt r236 | quals rush |
| mining | quals | W r2000 (points) | never | r126, r302 → 3 kings | about 144 rats at the end (unlimited spawning) |
| far | quals | W r1523 (kill) | 152 | r759, r957 | king #2 killed by a cat around r950, replaced |
| sittingducks | quals | W r854 (kill) | 21 | r370 | |
| thunderdome | quals | W r50 (kill) | 4 | – | |
| Meow | quals | W r680 (kill) | 16 | r436 | |
| cheesefarm | awubot | L r526 (kill) | 15 | 0 / 1 | cheese 8 at round 500 |
| DefaultMedium | awubot | L r413 (kill) | 19 | – | 97 cheese and 4 rats at round 200 |
| hatefullattice | awubot | (run killed ≈ r950) | 18 | r156 | |
| peaceinourtime (rerun) | awubot | L r480 (kill) | never (coop) | 0 / 3 | identical outcome; 11 cat traps placed, 2 cat-trap NPEs |
| mining | awubot | L r2000 (points) | never (coop) | r132, r291 → 3 kings | 144 rats at the end; lost on points (coop: cat damage 50%) |

Against `awubot_quals` the result was 6–1. Against `awubot` (mirror), probe as team A won 2 of 11 completed games. Possible reasons are a side bias or the instrumentation cost; testing.md covers how to evaluate.

---

## 9. Tunable constants (macro)

Sensitivity is a guess: **H** means likely to change win rate by itself, **M** means it matters on some map types, **L** means it is cosmetic or an edge case. Constants in `src/challenger/Tune.java` were measured by `tools/tune` (track TN, docs/evaluation.md "Tuning constants"): their column gives the score change for one step, from the screens s1 (around main, seed base 61001) and s2 (around EXPLORE_P 0.3, TRAP_RAW 10, COOP_ROUND 50, seed base 61002), with 95% CIs by map. "Flat" means no step moved the score measurably.

### Baby rats
| Name / expression | Value | File:line | Meaning | Sens. |
|---|---|---|---|---|
| cat-trap gate | coop && cats>0 && no enemies (visible or squeaked) | `robot/rat/BabyRat.java:36` | when to try cat traps | M |
| cat-trap trigger | d² ≤ 9, inside the cat's 90° cone | `BabyRat.java:38,64` | rat in front of a nearby cat | M |
| suicide when napped | hp < 15 and carrier is enemy | `BabyRat.java:91` | deny a throw | L |
| glance coin | `Rng.mcg()>>30 != 0` (50%) | `BabyRat.java:133` | which side to glance first | L |
| transfer range | king centre d² ≤ 18; tries the 3 king tiles facing the rat | `BabyRat.java:162-173` | cheese delivery | L |
| storeVisibleCells guard | 3000 / 3000 / 2000 bytecode | `BabyRat.java:191,202,213` | skip re-sensing after a move when low on bytecode | L |
| cat-avoid step | d² ≤ 5, inside the cat's 180° cone | `BabyRat.java:229` | refuse a forward step toward a cat | M |
| `CRITICAL_HP` | 20 | `robot/rat/MinerStrategy.java:16` | carrying cheese with hp ≤ 20 → go home | L |
| `EARLY_EXPLORE_TURNS` | 20 | `MinerStrategy.java:17` | ray exploration window | M |
| `EXPLORE_DIR_X/Y` | 16 rays | `MinerStrategy.java:19-20` | early exploration directions | L |
| idle go-home | raw > 50 (`Tune.IDLE_GOHOME`) | `MinerStrategy.java` (`runTick`) | go home when nothing else to do | flat (TN): +0.40 [−0.29, +1.14] pp per +25 (s1), +0.20 [−0.29, +0.72] (s2) |
| explore-only phase | known==0 or (known ≤ 2 and t ≤ W·H/8) | `MinerStrategy.java:65-66` | before mining starts | M |
| explore probability | **0.6** (`Tune.EXPLORE_P`; 0.2 before TN) | `MinerStrategy.java` (`runTick`) | share of trips that explore once mines are known | **H, measured** (TN: pp per step, 95% CI by map; docs/evaluation.md "Tuning constants"): +1.4 pp per +0.1 at 0.2 (s1), +1.3 at 0.3 (s2); against main on fresh seeds, 760 cells each: 0.3 +0.001, 0.4 +0.022, 0.5 +0.016 and +0.020, 0.6 **+0.036 and +0.034**, 0.7 +0.024, 0.8 +0.026, 1.0 +0.018 (parabola peak 0.70). The gain is on ≥ 8-mine maps (0.6: +0.039 there), ≤ 4-mine maps flat. Our share of all cheese delivered by r500 +1.0 pp (+1.5 on ≥ 8 mines): explorers pick the unpicked cheese of w1-classes §3.2. Confirmation on fresh seeds: `goal` +0.019 [−0.007, +0.044] (gain test at δ 0.05 failed), `mines-ge8` +0.038 [+0.004, +0.075], `teammade` +0.041 [+0.003, +0.075], gauntlet no harm. Around 0.6 (TN2): +0.40 [−0.05, +0.81] pp per +0.1 (s5); 0.4 −0.001, 0.5 −0.003 (s6), 0.7 −0.003 (s7) against 0.6. A separate share on maps proven to hold ≥ 8 mines (a latched gate) gained nothing: 0.7 +0.005 [−0.005, +0.014], 0.8 +0.011 (s6) and −0.002 (s7), 1.0 +0.000; reverted |
| mine traverse radius | d² ≤ 2 | `MinerStrategy.java:77` | arrival at a mine | L |
| near-king radius | d² ≤ 49 | `MinerStrategy.java:84` | distance origin for mine weights | M |
| king-on-mine exclusion | d² ≤ 2 | `MinerStrategy.java:94` | skip the king's mine | M |
| mine weight | `⌊√d²⌋` (far favoured) | `MinerStrategy.java:100,102` | mine selection bias | **H** |
| first-rat target | `time == 2` → centre | `MinerStrategy.java:114` | first contact, early backstab | **H** |
| explore resamples | 6 | `MinerStrategy.java:123` | bytecode guard | L |
| explore king exclusion | d² ≤ 36 (`Tune.EXPLORE_KING_DSQ`) | `MinerStrategy.java` (`pickExplorationTarget`) | don't explore near home | flat (TN2): +0.08 [−0.33, +0.53] pp per +20 (s5) |
| early ray distance | `[⌈len/2⌉, len]` | `MinerStrategy.java:165-168` | spread of the opening | L |
| go-home thresholds | 25→10, 100→25, 225→40, 400→60, 900→80, else 105, each × `Tune.GOHOME_PCT`/100 | `MinerStrategy.java` (`shouldGoHome`) | trip length vs slowdown | flat (TN): +0.20 [−0.72, +1.07] pp per +30% (s1), +0.43 [−0.38, +1.30] (s2) |
| no-cheese-seen rule | `2·raw ≥ thresh && lastSeen < t−3` | `MinerStrategy.java:189` | return early when the mine is dry | M |
| low-global rules | g < 100 & raw ≥ 20; g < 30 & raw > 0 | `MinerStrategy.java:192-193` | emergency delivery | M |
| health-return table | raw ≥ 150/100/50/20 → hp ≤ 70/60/50/40 | `MinerStrategy.java:206-210` | save carried cheese | M |
| cheese traverse radius | d² ≤ 1 | `robot/rat/CheeseStrategy.java:47` | approach to a pile | L |
| cheese reach | ≤ 6 steps, then 1 ring (11×11 window) | `Pathfinder.computePickable` | which visible piles a rat chases | **H** |
| traverse stuck window | 25 turns; d² ≤ 8 (25 turns), ≤ 18 (50 turns) | `robot/rat/TraverseStrategy.java:20,34,37,42` | stuck detection | L |
| traverse give-up | raw < 20 && stuck && t%25 == 0 | `TraverseStrategy.java:44` | abandon target | L |
| `FORMATION_TIMEOUT` | 120 (effectively a 1-tick pause, §3) | `robot/rat/FormationStrategy.java:19,30` | rat-side formation timeout | M |
| `MIN_ALLIES_FOR_FORMATION` | 7 (visible + squeakers) | `FormationStrategy.java:20,79` | start stepping onto the centre | M |
| `MAX_DIST_TO_JOIN` | 625 = `Tune.JOIN_DIST`² (25 tiles) | `FormationStrategy.java` | recruiting radius | flat (TN): +0.04 [−0.51, +0.60] pp per +5 tiles (s1), −0.05 [−0.46, +0.37] (s5) |
| formation cheese floor | global < 50 → stop | `FormationStrategy.java:31` | can't afford upgrade | L |
| formation go-home | raw ≥ 40 and home ≥ 25 d² closer | `FormationStrategy.java:60` | deliver first | M |
| become-king radius | d² ≤ 8, or anywhere after **t > 1900** | `FormationStrategy.java:68` | late-game king | M |
| centre-step radius | d² ≤ 2; lowest ID wins | `FormationStrategy.java:80,145` | who becomes centre | L |
| go-home turn-to-transfer | d² ≤ 9 | `robot/rat/goHome/GoHomeStrategy.java.jinja2:34` | face the king to transfer | L |
| go-home window | 11×11 (`±5`) | `GoHomeStrategy.java.jinja2:57-58` | pheromone search radius | L |
| go-home fallback radius | `Traverse(king, 8)` | `GoHomeStrategy.java.jinja2:112` | no trail | L |
| pheromone base / start | `65536−3600`, `base+5` | `robot/rat/goHome/AntColony.java.jinja2:18,32,37` | mark encoding | L |

### Kings
| Name / expression | Value | File:line | Meaning | Sens. |
|---|---|---|---|---|
| `defaultThresh` | 40 (`Tune.SPAWN_CAP`) | `robot/king/KingSpawningStrategy.java` | **normal rat cap of 16** | **H** (not tuned: its −10 side lowers E2's mine-poor cap into E2's −8 arm) |
| small-map thresh | 30 (`Tune.SPAWN_CAP_SMALL`) if W·H ≤ 900 and t < 400 | `KingSpawningStrategy.java` | cap of 12 on small maps | M |
| mine-poor thresh | −10 if W·H ≤ 1600 and `minMines` < `RICH_MINES` = 5 | `KingSpawningStrategy.java:22-77` | cap 4 lower on mine-poor maps (§5.2, measured) | **H** |
| cheese-scaled thresh | `global/10 + 10` (`Tune.SPAWN_CHEESE_DIV`) | `KingSpawningStrategy.java` | cap when poor | flat (TN): −0.13 [−0.69, +0.50] pp per +3 (s1) |
| very-rich line | `global ≥ 3000 − 3t/4` → 1000 (`Tune.SPAWN_VRICH`) | `KingSpawningStrategy.java` | unlimited spawn | load-bearing (AB); its number flat (TN): +0.25 [−0.45, +0.95] pp per +750 (s1) |
| rich line | `global ≥ 2000 − t/2 && t ≥ 100` → 80 (32 rats) (`Tune.SPAWN_RICH`) | `KingSpawningStrategy.java` | doubled cap | load-bearing (AB); its number flat in wins (TN): −0.06 [−1.07, +0.86] pp per +500 (s1), though a lower line raises our share of cheese delivered by r500 (−0.74 pp of share per +500); −0.39 [−1.21, +0.37] (s5, after KF) |
| `threshForNumRats(x)` | `⌊(x−1)/4⌋·10 + 10` | `KingSpawningStrategy.java:18-20` | cost of the x-th rat | – |
| cat, no allies | +10 | `KingSpawningStrategy.java:86-87` | defend the king vs cats | M |
| panic spawn | +10 (+10 if hp ≤ 100), ≥ 5 rounds apart, needs a visible enemy and hp loss | `KingSpawningStrategy.java:111-118` | reinforce when attacked | M |
| spawn floor | `global ≥ 100 || cost == 10` (`Tune.SPAWN_FLOOR`) | `KingSpawningStrategy.java` | starvation guard (weak) | small (TN): −0.41 [−0.82, +0.01] pp per +50 (s1), −0.36 [−0.79, +0.06] (s2); 50 added to EXPLORE_P 0.4: −0.002 [−0.007, +0.003] (s3); −0.35 [−0.85, +0.13] (s5); 50 against 100 +0.001 [−0.003, +0.005] (s7): it rarely binds |
| spawn ring | Chebyshev 2, tile nearest the target | `KingSpawningStrategy.java:78-87` | spawn side | L |
| taboo box | <40: [1/3,2/3]; <50: [3/7,4/7]; else [4/9,5/9] | `KingMiningStrategy.java.jinja2:29-44`; duplicated in `KingFormationCoordinatorStrategy.java:44-59` | king walk target; formation mine exclusion | M |
| cheese chase box | Chebyshev ≤ 4 of the mine | `KingMiningStrategy.java.jinja2:133` | king pickup area | L |
| bug-far gate | bug position d² > 2 | `KingMiningStrategy.java.jinja2:229` | king moves only toward a far bug position | L |
| expand trigger | stuck 17 turns | `KingMiningStrategy.java.jinja2` (`moveIfBugTargetFar`) | switch to 5×5 clearance | L |
| near-mine radius | d² < 5, cheese within d² 13 | `KingMiningStrategy.java.jinja2:256,260` | local cheese chase | L |
| known-mine scan | first 10 | `KingMiningStrategy.java.jinja2:283`; `KingFormationCoordinatorStrategy.java:228` | bytecode guard (later mines ignored) | M |
| mine dedup radius | d² < 25 | `comms/GlobalArray.java:569` | one king per mine | M |
| `MINE_THRESHOLDS` | {3, 5 (`Tune.MINES_KING2`), 9, 15, 1e7} indexed by #kings | `robot/king/KingFormationCoordinatorStrategy.java` | known mines needed for the next king, until round 1200 | the second king's 5 flat (TN): +0.76 [−0.01, +1.51] pp per +2 (s1), −0.06 [−1.06, +0.65] (s2), +0.66 [−0.18, +1.50] (s5); 7 against 5 +0.005 [−0.008, +0.016] (s7) |
| `UPGRADE_COST`+100 | global ≥ 150 to start | `KingFormationCoordinatorStrategy.java:14,207` | formation cheese gate (outside the lock-in, and the lone-king refill after round 1200) | L |
| `LOCK_IN_ROUND`, `KING_TARGET` | 800 (`Tune.LOCK_IN_ROUND`), 3 (`Tune.KING_TARGET`) | `KingFormationCoordinatorStrategy.java` | lock-in window and king target | flat (TN): the round +0.26 [−0.24, +0.71] pp per +200 (s1), +0.06 [−0.30, +0.47] (s5, after KF); the target −0.11 [−0.42, +0.19] per +1 (s5) |
| `LOCK_IN_MIN_RATS` | 12 (`Tune.LOCK_IN_MIN_RATS`) | `KingFormationCoordinatorStrategy.java` | lock-in and cooperation-king rat floor (start; lock-in abort) | flat (TN2): −0.26 [−0.77, +0.18] pp per +4 (s5) |
| `COOP_ROUND` | 100 (`Tune.COOP_ROUND`) | `KingFormationCoordinatorStrategy.java` | from it to `LOCK_IN_ROUND`, a team still in cooperation with one king forms a second on our side; from the lock-in, under the lock-in's rules (KF) | not confirmed (TN): −0.86 [−1.58, −0.24] pp per +50 at 100 (s1), but +0.15 [−0.55, +0.86] per +25 at 50 (s2) and 50 added to EXPLORE_P 0.4: +0.003 [−0.009, +0.020] (s3); after KF −0.05 [−0.47, +0.34] per +50 (s5): flat |
| `LOCK_IN_MIN_CHEESE` | 1000 (`Tune.LOCK_IN_MIN_CHEESE`) | `KingFormationCoordinatorStrategy.java` | lock-in cheese gate before round 1200 | flat (TN): +0.15 [−0.29, +0.60] pp per +400 (s1), +0.18 [−0.21, +0.57] (s2), +0.07 [−0.19, +0.35] (s5, after KF) |
| `MAX_FORMATION_DIST_TO_MINE_SQ` | 25 (unused) | `KingFormationCoordinatorStrategy.java:15` | dead | – |
| `COORDINATION_TIMEOUT` | 120 | `KingFormationCoordinatorStrategy.java:16,332` | king-side formation timeout | M |
| `MIN_DIST_FROM_KINGS_SQ` | `max(400, W·H/5)` | `KingFormationCoordinatorStrategy.java:41,254` | new king far from existing ones | **H** |
| isNormal constants | 16, 36, 150, 25, 8, 250 | `KingFormationCoordinatorStrategy.java:69-71,118,126` | dead (call commented out) | – |
| `req` | 12, or 8 if W·H ≤ 1200 | `KingFormationCoordinatorStrategy.java:155-158` | abort if `numRats < req·kings` | M |
| late-king abort | t > 1200 && kings ≥ 2 | `KingFormationCoordinatorStrategy.java:162` | the engine's rule | – |
| danger formation | hp < 100 and 1 king | `KingFormationCoordinatorStrategy.java:201` | emergency second king | M |
| formation rat gate | `numRats ≥ 20·kings` (area > 1200) or `20·kings−4` | `KingFormationCoordinatorStrategy.java:218-223` | **unreachable at the 16 cap**; skipped in the lock-in | **H** |
| lock-in rat gate | lockIn → numRats ≥ 12 only | `KingFormationCoordinatorStrategy.java:230` | 2nd and 3rd king from round 800; lone-king refill after 1200 | M |
| score floor | not lockIn && best < −40000 → none | `KingFormationCoordinatorStrategy.java:268` | require a far mine | M |
| mine score terms | −50000 (too close), −Σd²(kings), −d²(centre), −50 if d²(self) > 400 | `KingFormationCoordinatorStrategy.java:250-269` | formation site choice | M |
| cat abort | d² < 625 (dead code) | `KingFormationCoordinatorStrategy.java:329-334` | – | – |
| unbury scoring | +25 per step, +200 wall-adjacent, −10·d², −10·free-dist | `robot/king/RatKing.java:182,187,190,214` | dig-out choice | L |

### Cross-cutting gates in other files (owned elsewhere, but they drive macro)
| Name / expression | Value | File:line | Meaning | Sens. |
|---|---|---|---|---|
| cat near king | d² ≤ 36 → fight | `robot/rat/combat/CombatStrategy.java:446` (tmpl `:455`) | defend the king vs cats | **H** |
| cat hunt gate | t > 500, allCheese > 1000, no enemies, raw == 0, ≥ 16 rats, id%3==0 or t ≥ 1800 | `CombatStrategy.java:447` (tmpl `:456`) | cat-damage share | **H** |
| bite bonus | +2 damage for 2 cheese when allCheese ≥ 300 (`Tune.BITE_BONUS_CHEESE`) | `CombatStrategy.java.jinja2` (`getBiteDamage`) | cheese sink | flat (TN): −0.07 [−0.54, +0.44] pp per +150 (s1) |
| trap gate | rat cost ≥ 70 or raw ≥ 20 (`Tune.TRAP_RAW`, also the band trap's raw gate; band from global ≥ `Tune.BAND_GLOBAL_CHEESE` = 400) | `robot/rat/combat/moves/TryTrap.java.jinja2` | cheese sink | raw gate not confirmed (TN): −1.23 [−1.92, −0.58] pp per +10 at 20 (s1), +0.06 [−0.27, +0.38] per +5 at 10 (s2), 10 added to EXPLORE_P 0.4: −0.005 [−0.017, +0.007] (s3); band global flat: +0.20 [−0.33, +0.71] per +200 (s1) |
| dirt pathing | (t ≥ 100 && g ≥ 1500 (`Tune.DIRT_CHEESE`)) or g ≥ 3000 (sticky); also t ≤ 2 at init | `Pathfinder.java.jinja2` (`Config.allowDirt`) | rats dig through dirt | flat (TN): +0.04 [−0.42, +0.50] pp per +500 (s1) |
| panic squeak | cat within d² < 25 of king; 4-round memory | `comms/Squeaker.java:173-174` | lure cat | M |
| troll | `id%10==7` | `CombatStrategy.java:157` | combat.md | L |

---

## 10. Improvement surface (ranked hypotheses)

1. **Raise or adapt the contested rat cap.** *Evidence:* `defaultThresh=40` caps the bot at 16 rats (`KingSpawningStrategy.java:23`). In mirror games cheese sat at 700–1850 for hundreds of rounds with ≤ 16 rats (DefaultLarge r200–1000, hatefullattice r200–900, sittingducks r500–800). The final spawn change was committed "untested" (`e0bd796`). *Try:* a cap based on a reserve, e.g. spawn while `global − cost > reserve(t) = 2·kings·min(300, 2000−t)`, or `defaultThresh` of 50–60. Watch attrition maps (DefaultMedium, starvation, cheesefarm), where cheese already collapses.
2. **Make king #2 happen in contested games.** *Evidence:* the formation needs `numRats ≥ 20` (`:197-201`) while the normal cap is 16, and also ≥ 5 known mines (`MINE_THRESHOLDS`) and a mine ≥ max(20, √(W·H/5)) tiles from the king. It failed in all 17 attempts on cheesefarm and peaceinourtime. When it succeeds, income rises up to 5× (`far`), and at round 2000 two kings against one is +16.7 points. *Try:* lower or decouple the rat gate; allow sites 10–15 tiles away; recruit exactly 8 rats (use the unused spawn payload in slots 22–31 or spawn recruits straight toward the site); fix the rat-side timeout (`FormationStrategy.java:29-33` vs `:109-118`); form well before the round-1200 cutoff.
3. **King safety against cats.** *Evidence:* on `peaceinourtime` the king died in cooperation, where only cats deal damage, with 28 rats and 1700 cheese, in all 3 runs (deterministic auto-loss). King #2 on `far` also died to a cat. The king walks toward the centre first (`moveTowardCenter`). Mine choice ignores cat paths. The coordinator's cat abort is dead code. The panic squeak creates phantom enemies. *Try:* keep kings away from the cat's last seen position and heading, bring more rats to cats near the king (radius 36 → 64?), fix the panic-squeak header, and stop moving toward the centre once any mine is known.
4. **Cat damage share.** *Evidence:* hunting is gated to t > 500, > 1000 cheese, ≥ 16 rats and `id%3==0` (`CombatStrategy.java:447`). Cat traps go down only in coop, from in front of the cat. Cat damage is 50% of the score in coop and 30% in backstab. All 3 games that reached round 2000 were decided on points, and the `mining` mirror was lost on points despite 3 kings and 144 rats. *Try:* start hunting earlier or use a larger fraction when not in contact; place cat traps along the cat's path; this matters most against passive opponents.
5. **Directed pressure on the enemy king.** *Evidence:* 15 of 18 games ended by king kill, yet nothing seeks the enemy king. Symmetry is computed and shared (slot 1) but only used for comms, and `Util.guessEnemyLocs` is unused. *Try:* when ahead (more rats or kings), send a group to the symmetric spawn or the last seen enemy king. Risky: defending near your own king (the `shouldFight` king-radius rules) may be the source of strength.
6. **Mine targeting and logistics.** The `√d²` weight favours far mines (`MinerStrategy.java:100-102`). Exploration never ends (60% of trips since TN, which measured +3.5 pp for 20% → 60%, §9). The go-home thresholds go up to 105 raw, which is a ×2 slowdown. Rats skip the king's mine. *Try:* prefer near or safe mines, decay exploration once `localTracker` is mostly seen, tune the threshold table, and consider spawn-tile-aware deliveries.
7. **Backstab policy.** First contact causes an instant backstab, and every combat entry ignores `rc.isCooperation()`. Against awubot this is symmetric. Against coop-farming teams, staying cooperative while ahead on cat damage could win on points. Low priority for beating awubot.
8. **Endgame scoring.** At t ≥ 1800 the only special logic is: hunt cats with everyone, relaxed formation (1 king only), and becoming king anywhere after round 1900 (only rats in an active formation). *Try:* secure a 2nd king before round 1100; in the last ~50 rounds bank all raw cheese (transfers count, carried raw does not); stop non-essential spending to win tiebreak 1.
9. **Cheap bug fixes (unknown impact; run A/B tests).**
   - NullPointerException in `considerPlacingCatTrap` freezes rats in front of cats (`BabyRat.java:42,68`; observed).
   - Panic squeak decoded as an enemy packet (`Squeaker.java:216` header `7<<29` < 0).
   - `isKingExpanded(i)` uses `kingId` (`GlobalArray.java:125`).
   - The spawn-target neighbour count is always 0 (`KingSpawningStrategy.java:44-46`).
   - Fixed (KS): dead-king mine assignments are ignored; the king's `mineTarget` flip between two visible mines.
   - The formation cat abort is dead (`KingFormationCoordinatorStrategy.java:210,329`).
   - A formation stays in the array after the coordinator dies.
   - `catHist` goes stale while in combat (`BabyRat.java:103-111`).
   - `RobotTracker.updateRobots()` after every non-final move resets `allyCount` and `kingSet` mid-turn (see architecture.md).

---

## 11. Notes for agents changing macro code

- These classes are hand-written `.java`: `MinerStrategy`, `CheeseStrategy`, `TraverseStrategy`, `FormationStrategy`, `KingSpawningStrategy`, `KingFormationCoordinatorStrategy`, `RatKing`, `BabyRat`. `KingMiningStrategy`, `GoHomeStrategy`, `AntColony` and `CombatStrategy` are generated: **edit the `.jinja2` and regenerate** (codegen.md).
- `src/challenger/CLAUDE.md` comes from the original team. It says "Never edit `BabyRat.java`" (add strategies instead) and "use `Pathfinder.makeMove`, not `rc.move`". `makeMove` no longer exists: the live API is `BabyRat.autoMove(Pathfinder.getMove(target))` for rats and `Pathfinder.getMove` + `RatKing.rawMove` for kings.
- `numRats` in king code is `4·⌊n/4⌋`, derived from `rc.getCurrentRatCost()`; it is not an exact count.
- To observe macro behaviour without touching `src/`, copy the repo to `/tmp`, rename the package and add periodic king-only prints (§8). Expect prints to perturb results.
