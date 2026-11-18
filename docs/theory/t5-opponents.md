# T5: opponent models (awubot, Old But Gold, GST, the gauntlet)

Lens T5 of track T. It models how the fixed opponent `awubot` and the top public bots actually play, measures
why Old But Gold (OBG) and GST beat awubot, and turns every exploitable regularity of awubot into a work package.

**Conventions.**
- Bot paths are relative to `src/<package>/`; awubot generated code is cited at its `.jinja2` template.
- Engine paths are relative to `engine/src/main/battlecode/` at tag `engine.1.2.6`; the local clone is
  `~/battlecode26-data/engine/ref/battlecode26`.
- Each claim is tagged with its kind of evidence:
  - **code**: read in the source;
  - **obs**: observed in the replays of §1;
  - **arith**: my own arithmetic, shown;
  - **inference**: not checked directly.
- Game tags: `obg-keepout-A` means OBG played as team A against awubot on keepout.

## 0. Headline

**awubot** is a reactive, locally greedy fighter:
- it fields a fixed army of about 16 rats (12 on maps of area ≤ 900 before r400) and turns all of its income into
  replacements;
- it scatters miners toward far mines, mostly on the enemy's half, one rat at a time;
- it parks its king on the nearest home mine, at the map edge;
- it traps only fresh enemies;
- it walks into 95–98% of enemy traps;
- it has two self-inflicted losses:
  - every king is destroyed once it knows 26 cheese mines (`sieveofriches` lost from 2:1 ahead);
  - a king parked in a cat's lane is scratched to death (`tminusten`, r102–104, in both recorded versions: quals
    and final).

**Why OBG and GST beat it on mine-rich maps** (≥ 8 mines: DefaultLarge, cheesefarm, keepout; 73 of the 150 maps
in `maps/`).
- **It is not spawn volume.** Each spawned fewer rats than awubot in 13 of 14 games.
- **Better exchange through traps.**
  - Both trap the tile toward the closest enemy whenever that enemy is at d² 3–8 and they have enough cheese.
  - awubot holds at grab range (Chebyshev 2), which is already inside that trap's 3×3, so any approach triggers it.
  - In 8 of OBG's 9 wins OBG placed ≥ 1.2× awubot's traps, and awubot lost more rats in all 9.
- **Territory.**
  - Cheese production is fixed by the map. The two bots together capture 85–92% of it, so income is a share of a
    fixed pie.
  - OBG and GST hold the contested central mines: 7–8 of 12 on keepout, 6–7 of 10 on cheesefarm, and more
    mines than awubot in 12 of their 15 wins, tied in the other 3 (Appendix A).
- **Starvation.** The side that loses the share and the exchange runs out of global cheese, and its king starves
  while being bitten. That happened in 9 of awubot's 13 king-kill losses.

**Where they fail.** On maps with few mines (4: DefaultSmall and arrows; also DefaultMedium's 6 for GST) their kings
are swarmed and awubot wins. The variable is mines, not area: cheesefarm has the same 900 tiles as DefaultSmall but
10 mines, and the opponents won it 4 of 4.
- OBG's king sits on the most central mine and was bitten 30–35 times;
- GST's king starved in 7 of 7 of its king-kill losses.

**Top packages** (§9; the effects are estimates):

| package | what | effect |
|---|---|---|
| P1 | delete the mine-overflow `assert(false)`, keep the king alive against cats | certain wins on `sieveofriches` and the custom 26–38-mine maps; `tminusten` needs P1b |
| P2 | trap the approach tile, as OBG and GST do | estimate +10–20 pp on mine-rich maps |
| P3 | hold the central mines: miner assignment instead of the √d lottery, plus defended mines | estimate +5–10 pp, on top of P2 |

---

## 1. Data, tools, caveats

| set | content | where |
|---|---|---|
| D1 | 28 games: `opp_obg`, `opp_gst` vs awubot on DefaultSmall, DefaultMedium, DefaultLarge, arrows, cheesefarm, keepout, TheHeist; both seats; engine 1.2.5, seed 0 (setup agent's validation runs; docs/opponents.md §1) | `~/battlecode26-data/theory/replays/` (`.bc26` + server logs) |
| D2 | 42 games: the other 7 vendored bots (and merlin) vs awubot on DefaultSmall, DefaultMedium, cheesefarm, both seats, same conditions | `~/battlecode26-data/theory/replays_other/` |
| D3 | 4 games I played: awubot vs `opp_obg` on `tminusten` and `sieveofriches`, both seats, `ABtest.py --engine official` (1.2.5 jar, the map's own seed) | `~/battlecode26-data/theory/replays_bigmines/` |
| D4 | every official tournament game of the champion | `references/tournament/bc26-champion-replay-summary.txt` |

**Analyser.** `~/battlecode26-data/theory/tools/Deep.java`, run as `java -cp $JAR:. Deep STEP replays...`. Its
outputs are in `deep_obg_gst.txt` and `deep_other.txt`. Per team it counts:
- spawns, deaths by cause, bites, ratnaps and throws;
- traps placed and traps walked into, plus transfers;
- king HP every 50 rounds, the bites and scratches on each king, and starvation turns;
- cheese pickups per mine.

The same directory has `Track.java` (king and cat positions) and `MapStats.java` (per-map size, mines and cats;
output in `mapstats.txt`). The B agents can reuse them to read flipped games.

**Metric definitions**, from the engine:

| metric | definition | source |
|---|---|---|
| walked into a trap | `TriggerTrap` carries the **triggering** robot's team | `server/GameMaker.java:627-631`, `world/GameWorld.java:644` |
| no-cause death | an id in `Round.diedIds`, which only a thread end (`disintegrate`, an escaping `Error`, a `run()` return) produces | `world/GameWorld.java:356-359` |
| damage death | a `DieAction`; damage deaths include thrown-rat collisions and rat-king crushes | `world/InternalRobot.java:551-563` |
| starvation turn | a king turn losing exactly 10 HP with no bite, scratch or trap on that king since its previous turn (a lower bound); HP is recorded after upkeep | `world/InternalRobot.java:1182-1188`, `:1425` |
| mines held | the number of mines where a team made more `CheesePickup` calls than the other; a proxy, since it counts calls, not amounts | – |

**Caveats.**
- D1–D3 ran engine 1.2.5. Cheese spawning differs from 1.2.6 (rules.md §0).
- D1 and D2 used one seed per map.
- 28 games is small. Every causal claim below rests on a mechanism in code; the replays only measure its size.
- Replay fields that can't be trusted:
  - `teamAliveRatKings` is garbage;
  - cat damage reads 4000/4000 at the end of cooperation games (intel.md §7).

---

## 2. Frame: what decides awubot's games

### 2.1 Income is fixed by the map; the contest is its split

- **Production per mine.** A mine pair spawns 20+20 cheese about every 8.9 rounds, i.e. 2.25 cheese per round per
  mine. Harvesting does not change this rate (rules.md §5, `world/GameWorld.java` `spawnCheese`).
  - Check (arith): the probability of no spawn in round r of a pair is 0.99^(2r). The expected wait is
    Σ_t 0.99^(t(t+1)) ≈ 8.9.
- **Capture rate** (arith from D1 transfers), production = mines × 2.25 × rounds:

| game | production | transferred (opp + awubot) | captured |
|---|---|---|---|
| obg-keepout-A, r900 | 12 × 2.25 × 900 = 24,300 | 13,197 + 7,560 | 85% |
| obg-cheesefarm-B, r700 | 15,750 | 8,838 + 4,762 | 86% |
| obg-DefaultLarge-B, r900 | 16,200 | 9,235 + 5,604 | 92% |

- **Consequence.** "Economy" against awubot is **mine share**, not harvesting capacity. Extra rats raise income
  only by taking mines from awubot or reaching contested cheese first.
  - This also explains the setup finding (docs/opponents.md §0) that the cheese gap opens by r200–300.

### 2.2 The sink is replacement; the losing side starves

- **What a rat costs.** A rat costs 10 + 10·⌊n/4⌋ cheese (rules.md §6): 40–50 at the usual 12–16 rats alive.
- **awubot's spending.** awubot replaces about one dead rat every 5–6 rounds (strategy.md §0). It spawns more
  rats than either top bot (Appendix A: 13 of 14 games against each).
- **The spawn floor.** Its floor `global ≥ 100 || cost == 10` (`robot/king/KingSpawningStrategy.java:69`) still
  spends the last 10 cheese once ≤ 3 baby rats are left.
- **Starvation.** A king pays 2 cheese per turn or loses 10 HP per turn (rules.md §11).
- **The death spiral** (obs):
  - Starvation turns were detected in **9 of awubot's 13 king-kill losses**: 8 of 9 against OBG, 1 of 4 against
    GST.
  - They were detected in **7 of 7 of GST's king-kill losses**.
  - In OBG's 3 king-kill losses, only 1 had starvation (5 turns). Those were swarm kills.

### 2.3 Prices of damage

| source | cost | damage | notes |
|---|---|---|---|
| rat trap | 20 cheese, action cooldown 15 | 50, and the movement cooldown is **set** to 30 | triggered on entering any tile of its 3×3 (rules.md §10) |
| bite | 0 (awubot pays 2 for +2 when it has ≥ 300 cheese) | 10–12 per action cooldown of 10 | – |
| thrown-rat collision | 20 action cooldown, after a grab | up to 42 to **both** the flier and the robot hit | rules.md §9 |
| rat (to the enemy) | 40–50 cheese at 12–16 rats | 100 HP of target | i.e. 0.4–0.5 cheese per HP |

What a trap buys (arith):
- **Per cheese**, it breaks even. A trap deals 50 damage for 20 cheese (2.5 damage per cheese); the enemy paid
  2.0–2.5 HP per cheese for its rats.
- **Per action**, it is strong. 50 damage per 15 action cooldown is 2.8× a 12-damage bite per 10, and our rat
  takes no counter-damage while dealing it.
- **Side effects.**
  - The stun keeps the victim in place for its next 2 turns.
  - 50 damage flips the HP order that decides who may grab whom (rules.md §8, combat.md §0.2). A trapped
    100→50 HP awubot rat can be grabbed from the front by any of our rats with more than 50 HP, then thrown for
    up to 42 more.

**Hit rate** (obs, D1). Traps walked into ÷ traps placed:

| traps placed by | walked into by | rate |
|---|---|---|
| OBG | awubot | 1,027 / 1,045 = 98.3% |
| GST | awubot | 1,469 / 1,541 = 95.3% |
| awubot | OBG | 747 / 761 = 98.2% |
| awubot | GST | 702 / 755 = 93.0% |

Nobody avoids traps: enemy traps are invisible (`MapInfo.getTrap()` shows own traps only, rules.md §10).

### 2.4 State variables

Three variables decide awubot games:
- territory share s;
- exchange ratio x (enemy rats killed per own rat lost);
- king survival: cheese ≥ 2 per turn, HP, cats.

The number of mines picks which one binds (D1):
- **few mines** (≤ 4: DefaultSmall, arrows): the economy is thin, so the first swarm decides and king exposure
  matters most;
- **mine-rich maps** (≥ 8: DefaultLarge, cheesefarm, keepout): s and x compound, and the loser starves;
- 6 mines (DefaultMedium) is in between: OBG won 2 of 2, GST lost 2 of 2.

---

## 3. Model of awubot (the fixed opponent)

### 3.1 The policy in one paragraph

**King.** Every turn the king runs four sub-strategies in order (`robot/king/RatKingStrategy.java:24`):
1. spawn up to the cap, aiming at the nearest visible enemy;
2. flee and bite if enemies or cats are visible;
3. coordinate a formation (which needs ≥ 20 rats, so it rarely fires in contested play);
4. walk to and park on the nearest unassigned mine.

**Baby rats.** Every baby rat runs one priority chain:
1. combat, if any enemy or eligible cat is reachable within the 11×11 BFS;
2. formation;
3. go home at a distance-scaled raw-cheese threshold;
4. walk to the largest visible pile;
5. otherwise a trip: 80% to a known mine drawn with weight ⌊√d⌋, 20% exploration, forever (strategy.md §2.3).

Combat approaches along the BFS, holds at the edge of grab range, and grabs or bites only when it can't be
grabbed back (combat.md §0, §6.6).

### 3.2 Regularities and their levers

| id | regularity | source | observed | lever |
|---|---|---|---|---|
| A1 | **Spawn cap.** The threshold is `min(g/10+10, 40)` (30 on maps of area ≤ 900 before r400). It becomes 80 (32 rats) at `g ≥ 2000 − t/2`, t ≥ 100, and unlimited at `g ≥ 3000 − 0.75t` | `KingSpawningStrategy.java:21-31` | Plateau at 12–17 rats in contested games (Appendix A; docs/opponents.md §0) | Its army size is predictable; under contact it can't exceed 16–20 |
| A2 | **Defensive spawn.** +10, plus 10 more if HP ≤ 100, only if the king **lost HP since its last turn**, at most once every 5 rounds. Every earlier snapshot added +20 whenever an enemy was visible (§3.5) | `KingSpawningStrategy.java:59-66` | – | Harassing a king without damaging it triggers no extra spawns; awubot answers a siege with ≤ 1 extra rat per 5 rounds |
| A3 | **Spawn floor** `g ≥ 100 \|\| cost == 10` spends the last cheese while the king starves | `KingSpawningStrategy.java:69` | Starvation in 9 of 13 king-kill losses (§2.2) | Win the exchange and its king dies of hunger. Our king needs a reserve (P5) |
| A4 | **The first rat walks to the map centre at r2** and bites on contact | `MinerStrategy.java:114` | Backstab at r4–21 in 13 of 19 games (strategy.md §0) | Games are in backstab mode from the start. We get the 100-round cat-trap window, since the backstabbed team may place cat traps (rules.md §2) |
| A5 | **Combat pre-empts everything.** Any visible enemy reachable within ~7 BFS steps pulls the rat in, even when it carries cheese or heads home. It approaches along the BFS (+30 per step), holds at Chebyshev 2, and kites when threatened | `CombatStrategy.java.jinja2:143-206`, `moves/TryFight.java.jinja2` | – | Its approach path is the straight BFS line, and its parking ring is the grab edge: both are known in advance (P2, P4) |
| A6 | **Mine choice is a lottery weighted by ⌊√d⌋.** Distance is measured from the king when the rat is within 7 tiles of it. Mines under a king are skipped, and a chosen mine is banned until the next transfer | `MinerStrategy.java:83-111` | On keepout, from the king at (47,30), 263 of the 372 weight units (71%) point at enemy-half mines (arith from the D1 mine list). Yet awubot's pickups at OBG-half mines were only 1–41 per mine against 77–108 by OBG (`obg-keepout-B`) | A steady trickle of lone awubot rats walks into our half. Concentrated defence plus traps farms them (P2, P3) |
| A7 | **Go-home thresholds** rise from 10 to 105 raw cheese with distance | `MinerStrategy.java:175-213` | **Refuted as a lever.** OBG rats carry more raw cheese than awubot's: mean 24–29 vs 15–21 on keepout, and more raw cheese dropped at death (Deep `meanRaw`, `rawDroppedAtDeath`) | none |
| A8 | **Trap gate.** It traps only when rat cost ≥ 70 (≥ 24 rats), or raw ≥ 20, or one of the nearest 3 enemies is "strong" (HP ≥ 80 and ≥ its HP + bite, or HP = 100). It places on its front-3 cells, on the tile an enemy facing it would step into | `moves/TryTrap.java.jinja2:64-76,127-217` | 0.57–0.83× OBG's trap count in 8 of OBG's 9 wins; 0.36–0.54× GST's in GST's wins (Appendix A) | Its traps are predictable: our fresh rats, in front of an awubot rat that faces them (P4). Its exchange is starved of traps |
| A9 | **No trap model.** It walks into 95–98% of enemy traps (§2.3) | – | – | P2 |
| A10 | **Disintegrates when an enemy carries it at < 15 HP** | `robot/rat/BabyRat.java:89-95` | 11–57 no-cause deaths per game against OBG, but 0–10 against GST (Appendix A). Inference: OBG holds a grabbed rat up to 8 turns waiting for a good throw (`ThrowManager.java:32`), so low-HP carried rats have time to disintegrate | Neutral for us: it denies the throw but costs the rat |
| A11 | **King placement.** It walks toward the edge of a central taboo box until it knows an unassigned mine, then parks on the nearest one and chases cheese within Chebyshev 4 of it | `KingMiningStrategy.java.jinja2:53-76,173-278` | Parked on a mine near its spawn (`dMine2=0`, 10–24 tiles from the map centre) in every D1 game | Its king position is predictable from the map alone: the nearest mine to its start |
| A12 | **The king stays in a cat's lane.** Combat flee (cat weight 5) competes with mining's return-to-mine, and the leading-cell check has a bug for N/NW (combat.md B3) | `KingCombatStrategy.java.jinja2:102-164` | On `tminusten` (D3, both seats) the cat from (0,1) walks east and sticks at (9,1) against the king parked at (12,2). It scratches the king 29 times; the king dies at r102 and r104 in cooperation (`Track` output). The quals bot lost the same map at r103 and r104 to two other teams (D4). Over D1, awubot kings took 117 cat scratches against 50 for the opponents' kings (bites 298 vs 315), and 4 awubot kings died during a cat's turn | Map-specific auto-loss for awubot, and for us too until fixed (P1b) |
| A13 | **Formation** needs ≥ 20 rats (≥ 16 on area ≤ 1200), ≥ 5 known mines and a mine ≥ 20 tiles away, and recruits every rat within 25 tiles | `KingFormationCoordinatorStrategy.java:141-221`, `FormationStrategy.java` | Formed only on keepout (4 of 4 games), TheHeist (1 of 4) and sieveofriches (2 of 2). Never on DefaultSmall, DefaultMedium, DefaultLarge, arrows or cheesefarm | A second king for us is uncontested on most maps (P7) |
| A14 | **Cats.** It fights cats only within d² 36 of its king, or from r500 under a wealth gate. Cat traps only in cooperation, from in front of the cat. TryThrow ignores cats, and `assert(rc.canThrowRat())` fails when a cat sits on the landing tile | `CombatStrategy.java.jinja2:453-464`, `BabyRat.java:34-77`, `moves/TryThrow.java.jinja2:106` | 9 AssertionErrors in the 28 D1 logs (`TryThrow.java:151,156`) | Feeding its rats to cats is free (combat.md H1). Cooperation cat-damage share is open (P6) |
| A15 | **Mine-list overflow destroys every king.** With 26 slots (`MIN_CHEESE` = 38), knowing ≥ 26 mines reaches `assert(false)`; added in `5da8de3` (2026-01-29) | `comms/GlobalArray.java:413-429` | On `sieveofriches` (30 mines, D3) awubot led 23.1k to 12.0k transferred with 3 kings at full HP. All 3 kings were removed with no cause at r595–597, and both kings at r457 in the other seat | Free wins on maps with ≥ 26 mines (P1) |
| A16 | **No cooperation policy.** Only cat traps check `isCooperation()` | strategy.md §0, intel.md §0.2 | TheHeist (cats die in cooperation): won 3 of 4 on cheese share (Appendix A) | Points play is T3's lens; see P6 |
| A17 | **The panic squeak decodes as an enemy packet**: two phantom 24-HP enemies near its own king when a cat is near | `comms/Squeaker.java:173-217` | – | Inference: it pulls its own rats into phantom fights near its king, and its squeaks may draw cats to its king (A12's scratch excess) |

### 3.3 Correction: an `AssertionError` destroys the robot, it does not freeze it

The engine destroys the robot at the end of the turn in which its thread ends:
- `world/GameWorld.java:356-359`: `if (getTerminated(robot) && ...) destroyRobot(robot.getID())`;
- the thread ends on any `Throwable` that escapes `run()` (`instrumenter/SandboxedRobotPlayer.java:195-236`).

This contradicts architecture.md §1.6 ("stays on the map but never acts again") and docs/opponents.md
("froze rats"). It agrees with rules.md §16.21. Consequences:
- An awubot carrier that hits the TryThrow assert dies and drops the rat it carries (`world/GameWorld.java:1145-1148`).
- A15 kills kings outright. That is why `sieveofriches` ends within 3 rounds of the overflow.

### 3.4 Fingerprints usable for prediction

**Before the game:**
- its king's mine: the nearest mine to its start (A11);
- its cap: 12 or 16 (A1);
- whether it will self-destruct: mines ≥ 26. The maps in `maps/`: `quadrants` 38, `sieveofriches` 30,
  `tminusten` 30, `bridges` 28, `bridgesCatless` 26 (`mapstats.txt`).

**During the game:**
- its rats park at Chebyshev 2 from ours (A5);
- new rats appear on the king's ring (Chebyshev 2), facing the nearest enemy the king sees;
- after a bite on its king, at most one panic spawn per 5 rounds (A2);
- its formation needs ≥ 20 rats, which it reaches only when rich (A13).

### 3.5 Snapshots: what the champion changed and why it matters

| snapshot | spawn threshold | defensive spawn | mine list | evidence |
|---|---|---|---|---|
| `awubot_quals` (01-24) | `min(g/20+10, 40)`; 32 rats at g ≥ 3000; unlimited at g ≥ 5000 | +20 whenever an enemy is visible | 32 slots, no assert | Won the intl quals; lost `tminusten` r103/r104 (×3) and `sieveofriches` (×3) (D4). Beat final on `microtest` from both seats (combat.md §13) |
| `awubot_us` (01-26) | `min(g/20+10, 40)`; lines `2000/3000 − max(0, t−1000)` | +20 when an enemy is visible | 32 slots | – |
| `awubot_sus` (01-28) | as us, with the area-≤ 900 cap of 30 | +20 when an enemy is visible | 26 slots (map chunks moved into 32–37), no assert | – |
| `awubot` final (01-29) | `min(g/10+10, def)`; lines `2000 − t/2` and `3000 − 0.75t` (commit "untested") | +10 only after HP loss, ≤ once per 5 rounds | 26 slots, `assert(false)` on overflow | – |

Sources: `KingSpawningStrategy.java` and `comms/GlobalArray.java` in each package; history.md §2–3.

What the lineage shows:
- The final ramps spending faster when poor (g/10).
- It floods earlier when rich.
- It spends **less on defence** than any earlier snapshot. OBG and GST do the opposite: they spawn without a cost
  cap while an enemy is near their king (§4, §5).
- A15 is a final-day regression.
- `tminusten` is an old king-placement weakness that every version keeps.

---

## 4. Model of Old But Gold (`opp_obg`, `basic51`) and why it beats awubot

### 4.1 Policy

**Macro:**
- **King target.** The king sits on the known mine closest to the map centre (`RatKing.findBestMine`, `:89-138`).
  Once symmetry is known, or from r340 by assuming rotation, the mine list also holds the mirrored (enemy) mines
  (`SymmetryChecker.getSym` `:38`, `runMines` `:64-72` in its file).
- **Regular spawns.** While cost ≤ 40 (30 if ≤ 4 mines are known), it spawns if g > 5·cost. Above that it spawns if
  `g − 2100 + t ≥ 5·cost` (`RatKing.shouldBuildRat` `:322-330`). In practice this is a 16-rat cap until g ≈ 2350 − t.
- **Emergency spawns.** Whenever the king sees an enemy rat, or a cat with no ally nearer to it, it spawns toward
  it while `cost ≤ g/5` (`buildEmergencyRats` `:235-256`).
- **Mine assignment.** Each spawn (3 of every 4) carries a round-robin mine assignment over the whole list,
  enemy-side mines included (`buildWrapper` `:301-308`, `CommunicationManager.reportTargetMine` `:94-99`).
  - `miner()` = 3 of every 4 rats (`BabyRat.java:189-191`).
  - Everyone explores for the first 120 rounds (`ROUNDS_EXPLORATION`).
  - A rat goes home when it carries cheese and none is visible, at most 100 (`BabyRat.updateTarget` `:122-187`).
- **Second king.** From r350, when cost > 40 and known mines > 4·kings, at the most central mine ≥ √300 tiles from
  every base, with "slave" rats (id%4 ∈ {0,1}) (`:151-226`).

**Micro:**
- **Square safety tiers** (`MicroManager.MicroInfo.safe` `:461`): KIDNAPPED, MOVE_KIDNAPPED, VERY_SURROUNDED,
  SURROUNDED, FREE_ENEMY_HIT, CAT_DANGER, NO_DANGER. Within a tier it prefers distance to cats, then range, then
  allies.
  - The damage race counts **numbers**: `maxHP + 11·(enemies − 1) > myHP` means danger.
  - Rats with raw > 20 retreat (`MAX_CHEESE` `:20`, `:196`).
- **Trap rule** (`performMovement` `:100-138`, `shouldPlaceTrap` `:132`):
  - when the closest enemy is at d² 3–8 and global ≥ 500 (or cost ≥ 40 with g ≥ 300, or raw > 0), it places a rat
    trap on `myLoc + dirTo(enemy)`;
  - before that, it steps to the best square.
- **Throws** are ranked enemy king > enemy at distance 1 > **cat** > wall > enemy > nothing (`ThrowManager.java:8-15`).
  It needs at least "enemy unit" while ≥ 2 of its 10 carry turns remain (`:32`).
- **Cooperation**: it targets cats and places cat traps along the cat's heading (`BabyRat.java:86-115`).
- **Squeaks**: every rat that sees an enemy squeaks the nearest one each turn (`SqueakManager.java:133`). A thrown
  rat squeaks its flight line (`:117-128`).

### 4.2 Evidence (D1, Appendix A)

**Wins** (9 of 14; all king kills): DefaultLarge A/B, DefaultMedium A/B, cheesefarm A/B, keepout A/B, arrows B.

| measure | in OBG's 9 wins | in OBG's losses |
|---|---|---|
| awubot lost more rats | all 9, by 1.06–1.6× | arrows-A too (120 vs 81), but awubot's swarm still killed OBG's king |
| OBG's traps ÷ awubot's | 1.20–1.75 in 8 of 9 (DefaultMedium-B: 0.91) | 0.86–1.04 in its 3 king-kill losses |
| OBG transferred more | all 9, 1.15–2.1× | 0.3–0.7× awubot's |
| mines held | more in 6 of 9 (keepout 8:4 and 7:5, cheesefarm 6:4 and 7:3), tied in 3 | – |
| spawns | fewer than awubot in 13 of 14 games | – |

Damage on kings:

| side | bites on kings | cat scratches on kings |
|---|---|---|
| awubot's kings (OBG wins) | 2–33 | 0–19 |
| awubot's kings (all D1) | 298 | 117 |
| opponents' kings (all D1) | 315 | 50 |

### 4.3 Why OBG wins

1. **Exchange: trap the approach tile.**
   - awubot's rats hold at Chebyshev 2 (A5). A trap at `myLoc + dirTo(enemy)` covers every tile within Chebyshev 1
     of it, which includes the enemy's current tile and every tile from which it can reach bite range on that side
     (arith on the 3×3).
   - Standing still triggers nothing. Its next step in, or sideways, does: 50 damage and a 3-round move stun.
   - The 50 HP flips the HP order, so OBG grabs and throws next.
   - At 98% hit rate, the trap ratio is the cleanest measured correlate of the death ratio (§4.2, §6).
2. **Territory.**
   - The king on the most central mine, plus round-robin assignment over all mines, puts OBG's mass on the
     contested centre.
   - awubot's lottery (A6) sends single rats there. They meet OBG rats with traps and squeaked intel.
   - Example, keepout-B: OBG harvests its 6 mines almost alone (pickups 77–108 vs 1–41). It takes awubot's
     central mine (28,47) 89 to 4 and contests (37,3) 42 to 79.
   - The share gap compounds through starvation (§2.2).
3. **King defence.**
   - Emergency spawns (cost ≤ g/5) arm the king against sieges. The micro flees cats (`fleeFromCats`).
   - Its kings took 50 scratches in all of D1, against awubot's 117.

**Refuted or unmeasured:**
- Raw-cheese handling: refuted (A7).
- Bigger spawn budget: refuted as the cause of the rat-count gap (fewer spawns, fewer deaths).
- Flight-line squeaks: real (`SqueakManager.squeakFlying`) but unmeasured.
- Second king: OBG formed one only on keepout (r405–410, r879), and it won 7 other games without one.

### 4.4 Where OBG loses to awubot

- **Few-mine maps** (DefaultSmall ×2, arrows-A; 4 mines each):
  - OBG's king on the most central mine is in the rush zone;
  - it was bitten 30–35 times and died at r189–190 and r634;
  - OBG transferred 0.3–0.7× awubot's cheese there.
- **TheHeist** (×2), decided on points when the last cat died in cooperation:
  - awubot transferred 1,115–1,360 vs 540–610;
  - awubot formed a second king in one of the games (r261).

### 4.5 The setup agent's hypotheses, checked

| hypothesis | verdict |
|---|---|
| A spawn budget scaling with cheese and map size | OBG has no map-size term. Its cheese scaling is a reserve line (`g − 2100 + t`) plus emergency spawns. It spawned fewer rats than awubot in 13 of 14 games. **Not the cause**: the rat-count gap is survival |
| A mine per new rat, king on the most central mine | **Confirmed** in code (§4.1). Territory follows (§4.3.2). The central king is also OBG's liability on few-mine maps (§4.4) |
| A second king from about r350, with a fixed share of rats | **Confirmed** in code; **rare** in play (keepout only). Not decisive in D1 |
| Thrown rats squeak their flight line | **Confirmed** in code; effect unmeasured |
| A backup king when the main king's HP is low | **Not OBG.** It is cerr's `tryPanic` (docs/opponents.md §3). OBG has no such rule, and GST's is commented out (`DefectBRStateUpdater.java:36-39`) |

---

## 5. Model of GST (`opp_gst`, `Version40`)

### 5.1 Policy

**Spawning** (`RatKingBehaviors/RKHeadquarter.java:46-51`). It spawns if `g ≥ 80` and any of:
- cost ≤ 40;
- **inDistress**: an enemy baby rat in the king's vision, or the king inside a cat's vision
  (`RatKing.java:192-224`);
- g ≥ 2000 and t > 100;
- g ≥ 667 and t > 1900.

So GST has **no cost cap while it sees an enemy**. Correction to docs/opponents.md §3: `RAT_BUILD_THRESHOLD` and
`LEAVE_CORNER_ROUND` (`RatKing.java:83,100`) are never read.

**King:**
- walks to the closest *safe* mine. Mines near enemies or cats get a threat level, and the mirror of its own mine is
  marked threat 127 (`RatKing.updateInfo`);
- flees squeaked or seen cats for 15 rounds (`RKHeadquarter.java:65-139`);
- forms a new king when cost > 72 (about ≥ 28 rats), up to 2 kings (3 on > 2500 tiles) (`:143-160`).

**Rats:**
- answer distress within d² 25 (`DefectBRStateUpdater.java:46-55`, `BabyRat.java:96`);
- return home at 20 raw cheese (`BabyRat.java:91`);
- trap on the same rule as OBG: d² 3–8 to the closest enemy, raw ≥ 20 or g > 600 (`BRMicro.java:484-497`,
  `Utilities.attemptTrapTowardsEnemy` `:1620-1650`);
- also trap the enemy-facing side of mines (`attemptTrapMine` `:1695`).

### 5.2 Evidence and verdict

**Wins** (6 of 14): cheesefarm A/B and DefaultLarge-A by king kill, keepout-A by king kill at r1205, keepout-B on
points at r2000, TheHeist-B on points at r152.

| measure | value |
|---|---|
| GST's traps ÷ awubot's | 1.86–2.74 in its wins, 1.07–2.1 in its losses: GST always out-traps awubot |
| mines held | more in all 5 non-TheHeist wins (6:2, 6:4, 6:4, 7:5, 7:5) |
| what finished awubot's king | in 3 of 4 king-kill wins (and the keepout-B points game), an awubot king died during a cat's turn |

**Losses**: DefaultSmall, DefaultMedium and arrows in both seats; DefaultLarge-B.
- GST's king was bitten 4–44 times (median 20).
- It starved in all 7 king-kill losses (2–52 starvation turns).

**Model.** GST is OBG's exchange and territory recipe with an even heavier trap density. Its king is safer from cats,
but its army is too thin to survive awubot's early swarm on maps with ≤ 6 mines. Its 6 of 14 is lower than OBG's 9
of 14 because it lost all 6 games on DefaultSmall, arrows and DefaultMedium; OBG won DefaultMedium twice.

---

## 6. The gauntlet (D2: 3 maps × 2 seats each, against awubot)

| bot | wins | traps bot / awubot | deaths bot / awubot | transferred bot / awubot |
|---|---|---|---|---|
| spaark | 1/6 | 517 / 269 | 411 / 399 | 8.1k / 14.9k |
| powerpuff | 0/6 | 615 / 357 | 499 / 501 | 14.0k / 21.8k |
| food | 0/6 | 495 / 415 | 471 / 434 | 11.2k / 22.5k |
| cerr | 0/6 | 296 / 378 | 535 / 437 | 11.9k / 18.4k |
| gravy | 0/6 | 205 / 479 | 631 / 461 | 15.9k / 18.5k |
| goobie | 0/6 | 147 / 308 | 523 / 381 | 9.9k / 17.7k |
| parmesan | 0/6 | 73 / 406 | 695 / 430 | 16.0k / 23.4k |
| merlin | 0/6 | 54 / 392 | 580 / 351 | 11.8k / 16.8k |

Two regularities across all 10 bots:
- **Traps set the exchange.** Out-trapping awubot brings deaths near 1:1 (spaark, powerpuff, food: 1.00–1.09).
  Being out-trapped 2.1–7.3× costs 1.37–1.65× awubot's losses (gravy, goobie, parmesan, merlin).
- **The exchange alone does not win.** The trap-heavy bots still lose on territory (0.50–0.64× awubot's cheese)
  and die to its swarm on these small and medium maps.

Only OBG and GST win both axes. Each weaker bot's specifics are in docs/opponents.md §3. Their value here is as a
regression gauntlet for styles awubot never plays: SPAARK's mass spawning, cerr's panic king, Parmesan's
cats-as-walls.

---

## 7. Matchup gaps

| map class (examples) | awubot vs OBG/GST | mechanism | gap for us |
|---|---|---|---|
| ≤ 4 mines (DefaultSmall, arrows; 43 of 150 maps) | awubot wins (OBG 1/4, GST 0/4) | thin economy; the early swarm reaches the king; opponents' kings central or roaming | keep awubot's few-mine macro (home king, lean army); don't import the central king |
| 5–7 mines (DefaultMedium; 34 maps) | split (OBG 2/2, GST 0/2) | exchange vs king exposure | traps plus a home king |
| ≥ 8 mines (DefaultLarge, keepout, cheesefarm; 73 maps) | opponents win 11 of 12 | share plus exchange, then starvation | P2, P3, P5 |
| ≥ 26 mines (sieveofriches, tminusten; quadrants, bridges*) | awubot self-destructs or is killed by a cat | A15, A12 | P1 |
| cooperation ends early (TheHeist; t_minus_five_minutes and groveisbeingdesecrated in D4) | awubot 3/4 here; it lost 2 finals games this way (intel.md §2) | cheese share, king count, cat damage | P6, P7 (T3's lens) |
| cat lanes at the king (tminusten, whatsthecatdoin; intel.md §3.1) | awubot loses to the cat | A12 | P1b |

---

## 8. Best response to awubot (the dominant behaviour in this lens)

Derived from §2 and §3. Each item also says where the top bots fall short of it.

1. **Never lose to yourself.**
   - Delete the A15 assert.
   - Make the king leave a cat's lane: a cat blocked by a king scratches it every 3 rounds, 20 HP each (rules.md §12),
     so a king in place dies in about 90 turns.
   - Remove the TryThrow assert path. Nobody else has these bugs.
2. **Trap the grab edge.** If the action is ready and no safe grab or bite is available, and the closest enemy rat is
   at d² 3–8, place a rat trap on `myLoc + dirTo(enemy)` (else ±45°), within the 25-trap cap, when
   `raw ≥ 20 || g ≥ G_trap`.
   - Price: 20 cheese → 50 HP + stun + an HP-order flip, at a 95–98% hit rate against awubot (§2.3).
   - OBG and GST both do this, with G_trap = 500 and 600. awubot gates it on fresh enemies (A8).
3. **Hold the contested mines.**
   - Assign miners to the nearest and central mines of a fixed set (OBG's round-robin), and defend the frontier mines
     with traps (GST's `attemptTrapMine`).
   - Let awubot's lottery (A6) feed lone rats into that zone.
   - Where the top bots fall short: OBG puts its **king** on the most central mine on every map, and that loses small
     maps (§4.4).
4. **Don't walk into awubot's traps.**
   - awubot traps only when one of our rats is "strong" (HP ≥ 80 and ≥ its HP + 12, or 100), or when it is rich or
     carries raw ≥ 20 (A8). It places on a cell adjacent to both rats, on our side.
   - So a full-HP rat of ours facing an awubot rat at Chebyshev 2 should treat the 3×3 around the cells between them
     as trapped.
   - Approach from outside its cone instead: that also allows a grab regardless of HP (rules.md §8).
   - Neither OBG nor GST models this; both walk into 98% of awubot's traps.
5. **The king never starves and answers sieges.**
   - Keep `g ≥ k·2·H` for a horizon H unless the king is under direct attack. awubot's floor shows the failure (A3).
   - Spawn without a cost cap while an enemy is in the king's vision and `cost ≤ g/5` (OBG's rule).
6. **Switch on the number of mines, not the map area.**
   - Few mines (≤ 4): awubot's lean macro is the best of the 10 bots measured; keep it.
   - Mine-rich maps: items 2–5.
7. **Points.**
   - On maps where the cats die in cooperation, cheese share and king count decide (TheHeist, §4.4).
   - A second king before r1100, when rich, is worth 2:1 king share (+10 points in cooperation, +16.7 in backstab;
     strategy.md §6.1). awubot rarely forms one (A13).

---

## 9. Work packages

Test protocol shared by all packages (in `tools/games` terms):
- **Primary test.** Challenger vs awubot, paired seats, sequential test, on the quick pool then the tune pool.
- **Breakdown.** Report by map class (§7) and by win type.
- **Diagnostics.** Read the flipped pairs with the Deep analyser: trap ratio, death ratio, mines held, starvation
  turns, scratches on kings.
- **Gauntlet.** `opp_obg` and `opp_gst` first, then the other seven and the `awubot_*` snapshots. No regression
  beyond noise.

Effects are estimates from the mechanisms and D1 magnitudes, not measurements.

| id | hypothesis | expected effect vs awubot | test | risks | size |
|---|---|---|---|---|---|
| **P1** | Deleting the overflow assert (`comms/GlobalArray.java:427-428`), so we write 26 mines and skip the terminator as `awubot_sus` did, turns every ≥ 26-mine map into a win: awubot's kings die once they know 26 mines (A15) | Certain on `sieveofriches` (both seats, D3 timing r457–597) and on the custom `quadrants`, `bridges`, `bridgesCatless` (holdout). +1 pair per such map in the pool | Both seats on those 4 maps (`--engine official` and 1.2.6 seeds); confirm in the replay that awubot's kings get no-cause removals | None in behaviour on maps with < 26 mines (the code path isn't reached). Rats then read only 26 of the mines (no terminator), as `sus` did | S: 2 lines |
| **P1b** | A king that sees a cat which can reach its footprint along the cat's facing, or that took a scratch in the last 3 turns, moves perpendicular to the cat's heading. Mining may not walk it back while the cat is within d² 36. This survives the `tminusten` cat that kills awubot at r102–104 | +1 pair on `tminusten`. It also cuts the 2.3× cat-scratch excess on awubot-style kings (A12) on cat maps (whatsthecatdoin, dirtfulcat; intel.md §3.1) | `tminusten` both seats: our king alive at r110 → win. Plus the cat-map subset of the tune pool | King flees into enemies or dirt pockets. Fix the B3 ring bug (combat.md §9) in the same change | M |
| **P2** | Trap the approach tile (OBG/GST rule, §8.2) at G_trap ≈ 300–600, in addition to TryTrap, run before TryAttack when no safe grab exists. It lifts the challenger's trap ratio vs awubot from 1.0 to about 1.5 and the exchange toward OBG's 1.06–1.6 (§4.2) | Estimate +10–20 pp on mine-rich maps; about 0 on maps with ≤ 4 mines | Tune pool; sweep G_trap ∈ {300, 500, 800, ∞ = off} as a dose-response; diagnostics trap ratio and death ratio | Cheese drain when poor, so keep the raw ≥ 20 or g gate. Cap of 25 traps. Bytecode in combat turns (p90 ≈ 14k) | S–M: one move class or a TryTrap branch |
| **P3** | Replace the ⌊√d⌋ lottery (`MinerStrategy.java:83-111`) by deterministic assignment: the king hands each spawn a mine through the unused 13-bit spawn payload (architecture.md §4.1), round-robin over known mines **ordered by distance to our king, contested central mines first**, and stop exploring once the mines are known. This raises mine share | Estimate +5–10 pp on mine-rich maps; stacks with P2 (territory needs the exchange to hold) | Tune pool; diagnostic: mines held, transferred-cheese ratio at r300 and r600 | Overloading one mine. Losing awubot's accidental raids on enemy mines. Bytecode is fine (mine choice is cheap) | M |
| **P4** | Trap-avoidance belief: for each visible awubot rat that faces one of our "strong" rats at Chebyshev ≤ 2 (A8 gate), mark the 3×3s of its front cells adjacent to our rat as trapped for N rounds. Treat them as −X in TryFight, TryAttack and TryRatNap (combat.md §6) | Estimate +3–8 pp. OBG walked into 747 awubot traps in 14 games, about 2,700 HP or 27 rats' worth per game (arith); avoiding a third saves about 9 rats per game | Tune pool; diagnostic: awubot traps triggered by us ÷ placed | Over-passivity, standoffs; bytecode in CombatTracker. Note the A8 gate keys on awubot's raw and wealth, which we can't see | M–L |
| **P5** | King survival: (a) a cheese reserve so upkeep never fails (spawn only if `g − cost ≥ 2·kings·min(H, 2000−t)`, H ≈ 60–100, unless the king is attacked); (b) OBG emergency spawns (`cost ≤ g/5` while an enemy is in king vision). This removes the starvation end of the death spiral, as it ended 9 of 13 awubot king-kill losses | Estimate +3–8 pp, mostly converting close losses on mine-rich maps | Tune pool; diagnostic: starvation turns (must reach 0), king HP at death | (a) slows the early economy if H is too large. (b) over-spends on sieges of a cheap harasser | S–M |
| **P6** | Cats as a resource: feed carried awubot rats to cats (throw ranking with cat > wall, as OBG does; TryThrow is cat-blind, combat.md H1). In cooperation, hunt cats near our king and place cat traps along their heading (OBG `BabyRat.java:86-115`) | Estimate +2–5 pp: cat maps and early-cooperation maps (TheHeist class) | Cat-map subset and TheHeist-class maps | Feeding our own rats to cats; overlaps T2's throw work | M |
| **P7** | OBG's reachable second-king rule (from r350, cost > 40, mines > 4·kings, g ≥ 1000; site = most central mine ≥ √300 tiles from every base), using a fixed share of rats, instead of awubot's ≥ 20-rat gate | Estimate +2–5 pp: points maps (11 of 43 mirror games are points-decided, testing.md §7) and late games | Tune pool, points maps first; diagnostic: kings alive at the end | Losing 7 rats mid-fight; formation crushes our own raw cheese into global (fine) | M |

Suggested order:
1. P1, then P1b (certain gains).
2. P2: the single largest measured correlate of beating awubot.
3. P5 and P3: they stack.
4. P4, P6, P7.

P2 and P3 together are the "be OBG on mine-rich maps, stay awubot on few-mine maps" bet. Gate them on the number of
known mines if the few-mine subset regresses.

---

## 10. Mechanics or facts I could not settle

- **Why awubot's `tminusten` king doesn't leave the cat's lane.** Observed (A12); the cause is inferred, not traced.
  Candidates are dirt on the escape cells (`digDirtInDirection` spends the action), the N/NW ring bug (B3), or mining
  pulling it back to the mine.
- **Whether the tminusten king would also hit A15.** Its king would hit A15 too if it survived, since the map has 30
  mines (inference; it dies first).
- **`RatCollision` location semantics** in replays: my king-collision attribution found none, so throw damage on kings
  is unmeasured.
- **Pickup counts vs cheese amounts.** "Mines held" counts `CheesePickup` calls; the amount per call isn't in the
  replay.
- **Cat damage at the end of cooperation games** reads 4000/4000 (intel.md §7), so the TheHeist decisions are
  attributed by cheese share only.
- **Engine version.** All replay data is engine 1.2.5. The official 1.2.6 cheese placement could shift the
  territory numbers, but not A1–A17, which are code.

---

## Appendix A: D1 per-game table

Legend:
- seat: the opponent's team;
- traps: the opponent's traps / traps awubot walked into, then awubot's traps / traps the opponent walked into;
- deaths: damage + no-cause deaths, with awubot's no-cause count in brackets;
- mines held: as defined in §1.

| opp | map | seat | result | how | round | transferred opp/awu | deaths opp/awu (awu no-cause) | opp traps / awu walked in | awu traps / opp walked in | mines held opp/awu | spawns opp/awu |
|---|---|---|---|---|---|---|---|---|---|---|---|
| gst | DefaultLarge | A | W | kill | 400 | 3519/1778 | 60/74 (0) | 110/106 | 55/50 | 6/2 | 74/77 |
| gst | DefaultLarge | B | L | kill | 637 | 4622/4936 | 111/102 (5) | 94/92 | 88/82 | 4/4 | 118/119 |
| gst | DefaultMedium | A | L | kill | 270 | 1010/1540 | 56/59 (1) | 67/66 | 44/41 | 2/4 | 55/75 |
| gst | DefaultMedium | B | L | kill | 221 | 864/1371 | 48/46 (2) | 70/70 | 35/26 | 1/4 | 47/62 |
| gst | DefaultSmall | A | L | kill | 131 | 300/723 | 48/39 (1) | 46/46 | 37/33 | 1/3 | 48/51 |
| gst | DefaultSmall | B | L | kill | 175 | 553/752 | 50/60 (3) | 59/59 | 40/36 | 1/3 | 50/72 |
| gst | TheHeist | A | L | pts | 286 | 625/1445 | 5/0 (0) | 0/0 | 0/0 | 6/5 | 21/25 |
| gst | TheHeist | B | W | pts | 152 | 320/290 | 3/0 (0) | 0/0 | 0/0 | 5/3 | 19/16 |
| gst | arrows | A | L | kill | 243 | 940/1034 | 55/67 (4) | 68/67 | 32/30 | 2/2 | 55/79 |
| gst | arrows | B | L | kill | 282 | 956/1427 | 54/68 (1) | 63/56 | 37/34 | 1/3 | 54/80 |
| gst | cheesefarm | A | W | kill | 436 | 5386/3331 | 80/105 (3) | 148/142 | 54/53 | 6/4 | 96/119 |
| gst | cheesefarm | B | W | kill | 386 | 4450/2828 | 77/84 (3) | 121/112 | 65/61 | 6/4 | 93/95 |
| gst | keepout | A | W | kill | 1205 | 19542/10275 | 175/196 (5) | 271/251 | 109/102 | 7/5 | 200/207 |
| gst | keepout | B | W | pts | 2000 | 29979/20792 | 266/294 (10) | 424/402 | 159/154 | 7/5 | 294/309 |
| obg | DefaultLarge | A | W | kill | 604 | 5835/3239 | 77/100 (32) | 95/93 | 63/59 | 5/3 | 94/105 |
| obg | DefaultLarge | B | W | kill | 958 | 9806/5784 | 119/164 (57) | 149/149 | 86/84 | 4/4 | 139/163 |
| obg | DefaultMedium | A | W | kill | 367 | 2215/1607 | 64/81 (20) | 68/66 | 53/52 | 3/3 | 78/81 |
| obg | DefaultMedium | B | W | kill | 301 | 2197/1212 | 53/76 (16) | 42/38 | 46/45 | 3/2 | 69/76 |
| obg | DefaultSmall | A | L | kill | 190 | 340/1054 | 52/52 (11) | 43/41 | 50/49 | 1/3 | 52/64 |
| obg | DefaultSmall | B | L | kill | 189 | 316/926 | 52/57 (11) | 48/48 | 52/49 | 1/3 | 52/69 |
| obg | TheHeist | A | L | pts | 247 | 540/1115 | 3/0 (0) | 0/0 | 0/0 | 6/5 | 15/22 |
| obg | TheHeist | B | L | pts | 273 | 610/1360 | 4/7 (0) | 0/0 | 0/0 | 1/5 | 19/31 |
| obg | arrows | A | L | kill | 634 | 2034/2919 | 81/120 (21) | 51/51 | 49/49 | 2/2 | 84/128 |
| obg | arrows | B | W | kill | 499 | 2110/1835 | 72/100 (11) | 68/67 | 43/42 | 2/2 | 78/102 |
| obg | cheesefarm | A | W | kill | 402 | 5269/2568 | 73/115 (37) | 91/90 | 52/52 | 6/4 | 89/114 |
| obg | cheesefarm | B | W | kill | 709 | 9118/4802 | 145/154 (40) | 130/130 | 108/107 | 7/3 | 161/157 |
| obg | keepout | A | W | kill | 903 | 13217/7560 | 127/162 (48) | 128/124 | 76/76 | 8/4 | 154/161 |
| obg | keepout | B | W | kill | 977 | 14016/9201 | 126/168 (48) | 132/130 | 83/83 | 7/5 | 152/169 |

D3 (awubot vs `opp_obg`, official seed):

| map | seat | result | round | notes |
|---|---|---|---|---|
| `tminusten` | A | awubot killed | 102 | king scratched 29 times by a cat, cooperation never broken |
| `tminusten` | B | awubot killed | 104 | same |
| `sieveofriches` | A | awubot killed | 597 | led 23.1k to 12.0k transferred; 3 kings removed with no cause at r595–597 |
| `sieveofriches` | B | awubot killed | 457 | led 18.5k to 8.0k transferred; 2 kings removed with no cause at r457, after a third king died to a cat at r411 |
