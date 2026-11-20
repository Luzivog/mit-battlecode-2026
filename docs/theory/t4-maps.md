# T4: maps, information and tempo

Lens T4 of track T. Scope: vision and information, symmetry, scouting, rush timing, king safety, dirt and walls,
map classes and the best plan per class, the champion's weak maps. Every claim cites a rule (`docs/rules.md` §),
engine source (paths under `engine/src/main/battlecode/` of the engine repo, as in rules.md), bot code (paths under
`src/<pkg>/`; `.jinja2` lines are template lines), a measurement (§1 data sets D1–D4) or arithmetic shown inline.
Related lens on main: `docs/theory/t5-opponents.md` (T5). Where T5 and this file measure the same thing they agree
(starvation, `tminusten`); this file adds the official-pool decomposition, the map classes and the cat clock.

## 0. Headline

1. **Games are won by keeping your own king alive.** 32 of 43 official-map mirror games end in a king kill (D1).
   The losing kings' HP went 31% to rat bites, 31% to thrown-rat collisions (and cheese-boosted bites), 26% to
   **starvation** and 12% to **cat scratches** (§2.1). Points decided the other 11, mostly maps where the armies meet late or only through
   a cut of ≤ 4 tiles.
2. **Four clocks set the tempo** (§2.2): contact (≈ half the rat path between the kings), the cat clock (when a
   cat's lane first reaches a king), the starvation clock (bank ÷ net burn), and the points clock (round 2000, or
   the last cat's death in cooperation). Each map class is defined by which clock runs out first (§3).
3. **The dominant king behaviour:** never starve (spend only above an upkeep reserve), never stand in a cat's
   lane (learn lanes, move perpendicular before the cat arrives), keep fights and enemy carriers off the
   king's throw lines, and park where all three hold. The champion does none of these deliberately (§5).
4. **Information that matters is cat lanes and threats to the king, not the enemy's position.** Vision ignores walls
   but squeaks reach only r² 16 and only kings write the array, so the king sees ~5 tiles (r² 25) unless rats
   relay. The enemy king's start is known up to 3 symmetry candidates from round 1. Symmetry by walls fails on
   14 of 150 maps; mines resolve 10 of those and initial dirt 2 more (§2.3).
5. **Rushing awubot is dominated, except against a crippled king** (§2.6): a defender spawns adjacent to its king
   (0 travel, 1 rat per round) and awubot fields 12–16 rats by about round 16. Pressure pays when its king is
   starving, being scratched, or cornered, which the four clocks predict from the map.
6. **The champion's weak maps are explained by the clocks** (§4): cat lanes through its parked king (`tminusten`,
   `attack_on_cat`, `sunandmoon`, `diagonalization`, `peaceinourtime`: king killed by scratches against OBG in
   7 games), starvation (`whereisthecheese` and `corridorofdoomanddespair` on 2 mines, `popthecork` and
   `closeup` on 8–10), and mine overflow (≥ 26 mines, T5 P1).

Top packages (§6): **WP1 starvation reserve**, **WP2 cat-safe king**, **WP3 cat defence by rats and dirt**.

---

## 1. Data and method

| id | what | conditions | where |
|---|---|---|---|
| D1 | challenger vs awubot = awubot mirror, the 43 official maps, both seats (86 games, 43 distinct: every pair splits by seat) | `autoscrims/ABtest.py --pool official --engine official`: the **1.2.5** jar with the map's own seed (not 1.2.6; rules.md §0) | `match_results/abtest_2026-11-18_19-30-32.*`, replays in `matches/run-2026-11-18_19-30-32-*` |
| D2 | `opp_obg` vs awubot on keepout, whatsthecatdoin, tminusten, jail, both seats (8 games) | same | `~/battlecode26-data/theory/t4/replays/run-2026-11-18_20-44-09-*` |
| D3 | `opp_obg` vs awubot and the awubot mirror on 10 maps whose cat lane passes within 2.5 tiles of a king start (`maps.tsv` `cat_route`): 9 of the 11 CATPATH maps of §3 plus shell; both seats (40 games) | same | `.../t4/replays/run-2026-11-18_20-46-41-*` (OBG), `run-2026-11-18_20-49-29-*` (mirror) |
| D4 | static analysis of all 150 maps in `maps/` (43 official, 78 tournament, 29 team-made) | engine 1.2.6 `GameMapIO` loader | `.../t4/maps.json`, `maps.tsv`, `classes.json`, `catclock.json`, `catpark.json` |

Tools (all in `~/battlecode26-data/theory/t4/`, each with a usage line):
- `src/MapDump.java`: dumps every map (grid, bodies, cat waypoints, validity) to JSON.
- `classify.py`: per map, the rat path between the kings, mine sides, symmetry candidates, cat lanes, min cuts.
- `catclock.py`, `catpark.py`, `classes2.py`, `symm.py`, `render.py`: cat clock, mines on cat lanes, map classes,
  symmetry discriminators, ASCII render.
- `src/ReplayStats.java`: per game, the backstab round, first sighting, first enemy rat within Chebyshev 2 of each
  king, king HP by source, deaths, and a 50-round series.
- `src/KingTrace.java` and `kcause.py`: per round, each king's HP and the actions near it; starvation detection.

Definitions used in tables:
- **rat path** `kk`: 8-connected steps from team A's spawn ring (Chebyshev 2 around its king) to the ring around
  B's king. A forward move costs 10 cooldown, so 1 step ≈ 1 round (rules.md §4). Dirt costs 3.5 steps (1 step,
  plus 25 dig cooldown = 2.5 rounds; rules.md §4). −1 = no path even through dirt.
- **cut**: the minimum number of non-wall tiles (dirt counted passable) whose removal separates the two kings' 5×5
  boxes (networkx node cut).
- **king HP by source**, for the king that lost:
  - bites: enemy `RatAttack` on the king × 10;
  - scratches: `CatScratch` on a king tile × 20;
  - starvation: −10 king turns with no bite or scratch on that king in the same or previous round, in runs of
    ≥ 3. HP in the replay is recorded after upkeep (`world/InternalRobot.java:1182-1188,1425`);
  - rest = the residual: thrown-rat collisions (no replay action names the robot hit, `:780-812`) plus the cheese
    part of boosted bites.

**Caveat.** D1–D3 ran on the 1.2.5 jar. Cheese placement differs on 1.2.6 (rules.md §0), so economy numbers move.
King upkeep, cats, throws, vision and the four damage channels are identical.

---

## 2. Derivation

### 2.1 What ends games, measured

Awubot mirror on the official maps (D1). Columns 6–9 are the losing king's HP lost by source (§1 definitions).

| map | kk | cut | mines | 1st sight | end | bites | scratch | starve | throw+rest |
|---|---|---|---|---|---|---|---|---|---|
| knifefight | 1 | 16 | 6 | 1 | 68 kill | 80 | 0 | 0 | 512 |
| thunderdome | 4 | 16 | 10 | 1 | 116 kill | 370 | 0 | 0 | 224 |
| averyfineline | 16 | 9 | 2 | 5 | 124 kill | 400 | 0 | 0 | 200 |
| DefaultSmall | 17 | 11 | 4 | 8 | 135 kill | 420 | 0 | 0 | 160 |
| dirtfulcat | 11 | 16 | 6 | 5 | 168 kill | 0 | 420 | 90 | 90 |
| 5t4rv4t10n_1337 | 16.5 | 7 | 1 | 6 | 170 kill | 120 | 20 | 260 | 202 |
| canyoudig | 18.5 | 9 | 2 | 15 | 173 kill | 310 | 20 | 60 | 194 |
| tiny | 3.5 | 7 | 2 | 1 | 189 kill | 100 | 160 | 130 | 218 |
| starvation | 27 | 12 | 3 | 12 | 219 kill | 310 | 0 | 0 | 294 |
| whereisthecheese | 45 | 3 | 2 | 13 | 231 kill | 0 | 0 | **600** | 0 |
| Nofreecheese | 55 | 14 | 6 | 23 | 245 kill | 400 | 120 | 0 | 54 |
| DefaultLarge | 25 | 16 | 8 | 13 | 276 kill | 340 | 0 | 0 | 238 |
| streetsofnewyork | 68 | 16 | 6 | 35 | 278 kill | 250 | 0 | 0 | 352 |
| trapped | 69 | 11 | 4 | 26 | 302 kill | 200 | 0 | 70 | 338 |
| whatsthecatdoin | 49 | 4 | 4 | 38 | 304 kill | 530 | 0 | 0 | 62 |
| DefaultMedium | 32 | 16 | 6 | 23 | 332 kill | 120 | 80 | 280 | 112 |
| evileye | 20 | 9 | 4 | 7 | 333 kill | 20 | 60 | 220 | 258 |
| popthecork | 13 | 8 | 10 | 17 | 341 kill | 10 | 40 | **480** | 72 |
| jail | 46 | 13 | 4 | 31 | 379 kill | 430 | 0 | 30 | 128 |
| corridorofdoomanddespair | 47 | 9 | 2 | 30 | 385 kill | 100 | 0 | **440** | 60 |
| TheHeist | 199 | 4 | 18 | – | 390 pts | 0 | 0 | 0 | 0 |
| peaceinourtime | −1 | 0 | 14 | 13 | 451 pts | 0 | 220 | 0 | 0 |
| EscapeTheNight | 121 | 4 | 11 | 72 | 468 kill | 150 | 40 | 210 | 190 |
| dirtpassageway | 39 | 15 | 6 | 19 | 552 kill | 30 | 300 | 200 | 62 |
| arrows | 25 | 8 | 4 | 14 | 562 kill | 110 | 0 | 250 | 244 |
| hatefullattice | 32 | 12 | 14 | 15 | 995 kill | 390 | 0 | 300 | 504 |
| cheesefarm | 22.5 | 10 | 10 | 8 | 1051 kill | 110 | 200 | 40 | 238 |
| closeup | 40.5 | 5 | 8 | 22 | 1218 kill | 0 | 80 | **510** | 10 |
| toomuchcheese | 22 | 10 | 8 | 8 | 1347 kill | 60 | 0 | 340 | 206 |
| cheeseguardians | 47 | 13 | 18 | 26 | 1382 kill | 150 | 0 | 170 | 266 |
| mercifullattice | 26 | 8 | 10 | 13 | 1428 kill | 420 | 0 | 280 | 316 |
| keepout | 35 | 16 | 12 | 17 | 1495 kill | 20 | **1000** | 0 | 176 |
| safelycontained | 55 | 13 | 16 | 34 | 1665 kill | 330 | 0 | 350 | 360 |
| Meow | 17 | 10 | 8 | 12 | 1846 kill | 320 | 0 | 100 | 184 |
| ZeroDay | 30 | 3 | 12 | 19 | 2000 pts | 0 | 0 | 0 | 0 |
| averystrangespace | 252 | 6 | 6 | 101 | 2000 pts | 110 | 140 | 0 | 234 |
| cheesebottles | 57 | 2 | 6 | 34 | 2000 pts | 80 | 60 | 0 | 0 |
| minimaze | 90 | 2 | 12 | 55 | 2000 pts | 270 | 200 | 0 | 56 |
| pipes | 62 | 2 | 10 | 46 | 2000 pts | 0 | 0 | 0 | 0 |
| rift | 88 | 2 | 12 | 102 | 2000 pts | 0 | 0 | 0 | 0 |
| sittingducks | 29 | 9 | 10 | 14 | 2000 pts | 270 | 20 | **660** | 204 |
| uneruesansfin | 225 | 3 | 4 | 107 | 2000 pts | 30 | 0 | 0 | 164 |
| wallsofparadis | 90 | 7 | 8 | 59 | 2000 pts | 10 | 20 | 0 | 0 |

Totals over the 32 king kills: 21 074 HP lost by losing kings (more than 32 × 600 because formed kings count too).
Bites 6 600 (31.3%), scratches 2 540 (12.1%), starvation 5 410 (25.7%), throws and the rest 6 524 (31.0%).

Findings:
- **Starvation is a first-order cause, not a footnote.** ≥ 200 HP of the loser's king went to starvation in 14 of 32
  kills, and in sittingducks (points). On `whereisthecheese` the losing king never met an enemy (no enemy rat within
  Chebyshev 2 of either king all game). It starved from r172 at exactly −10 per round and died at r231 (KingTrace).
- **Thrown collisions are the other half of the combat damage.** On knifefight the loser's king lost 512 of 592 HP
  to the enemy throwing the loser's own rats into it (42 per hit in the trace: `10 + 4·(2·4 + 0)`, rules.md §9).
  awubot's throw scorer gives +300 to the enemy king zone (combat.md §6.1).
- **Cats kill kings that stand in their lane.** keepout (1000 scratch HP across the loser's kings), dirtfulcat (420),
  dirtpassageway (300).
- **The backstab comes at first contact.** In the mirror the backstab follows the first sighting by a median of
  2 rounds (1–11 on maps with kk ≤ 60; 15 and 47 on averystrangespace and uneruesansfin). awubot fights on sight
  (strategy.md §0).
- **First sighting ≈ 0.4–0.7 × kk** on open maps: both sides walk toward the centre at about 1 tile per round, and
  vision ignores walls (rules.md §7 tests only distance and angle). peaceinourtime has no rat path, yet the sides
  see each other at r13.

Against OBG (D2, D3; 28 games, OBG won 21). awubot's own king HP lost (bites, scratches, throws and the rest) and
the result:

| map | awubot seat | result | bites | scratch | throw+rest |
|---|---|---|---|---|---|
| attack_on_cat | A / B | OBG kill r491 / r453 | 0 / 0 | **580 / 580** | 0 / 0 |
| tminusten | A / B | OBG kill r102 / r104 | 0 / 0 | **580 / 580** | 0 / 0 |
| sunandmoon | A / B | OBG kill r140 / r274 | 0 / 0 | **580** / 180 | 0 / 420 |
| diagonalization | A / B | OBG kill r408 / awubot r134 | 10 / 0 | **560** / 100 | 27 / 42 |
| peaceinourtime | A / B | OBG kill r506 / awubot pts r402 | 0 / 0 | **580** / 0 | 0 / 0 |
| groveisbeingdesecrated | A / B | OBG pts r169 / r307 | 0 / 0 | 420 / 20 | 0 / 0 |
| RUN | A / B | OBG pts r2000 / r2000 | 0 / 0 | 160 / 500 | 0 / 0 |
| directconfrontation | A / B | awubot r427 / OBG r232 | 0 / 0 | 300 / 460 | 202 / 132 |
| t_minus_five_minutes | A / B | OBG pts r223 / r282 | 0 | 0 | 0 |
| externalsustenance | A / B | awubot r130 / r128 | 0 | 80 / 0 | 26 / 0 |
| shell | A / B | awubot r460 / OBG r502 | 50 / 140 | 100 / 40 | 47 / 365 |
| jail | A / B | OBG kill r493 / r464 | 160 / 130 | 0 | 443 / 471 |
| keepout | A / B | OBG kill r1020 / r762 | 330 / 110 | 120 / 440 | 693 / 484 |
| whatsthecatdoin | A / B | awubot r505 / OBG r327 | 70 / 340 | 40 / 100 | 457 / 154 |

- awubot's king took ≥ 560 scratch HP and died in 7 of these games. OBG's king took ≥ 500 scratch HP once
  (diagonalization, the game where awubot was B and won).
- OBG transferred **no more** cheese than awubot at the last sample before the end in 5 of its 21 wins: less on
  tminusten ×2, whatsthecatdoin B and sunandmoon A, equal on t_minus_five_minutes A. Economy is not the whole gap: on cat-lane
  maps the gap is king safety.
- Starvation: whatsthecatdoin B 330 HP, jail A 160 HP (`kcause.py`). T5 finds starvation in 9 of awubot's 13
  king-kill losses over its own set.

### 2.2 The four clocks

**Contact clock.** Two rats walking at 1 tile per round toward each other from spawn rings `kk` apart see each other
at a gap of ≈ 4.5 tiles (r² 20). So first sight ≈ 2 + (kk − 4.5)/2 rounds. D1 agrees within a factor of about 1.5.
After that, awubot's rats pull every reachable rat within ~7 BFS steps into combat (combat.md §3.2). The loser's king
takes its first damage at r11 (knifefight), r38 (DefaultSmall) and r61 (DefaultLarge) (`kcause.py`).

**Cat clock** (cat facts from rules.md §12):
- A cat walks 1 tile per 2 rounds (walk cooldown 20).
- In EXPLORE it follows a BFS on walls only between invisible waypoints. It moves into baby-rat tiles (crushing
  them), but a **king** in the way blocks it, and the cat scratches the king: 20 HP per 3 rounds (scratch cooldown
  30). This repeats while the king stays (`world/InternalRobot.java:1255-1275`). A cat that scratches is not
  "stuck", so it never pounces away (`:1278-1294`), and a pounce cannot land on a king anyway (rules.md §16.18).
- A king parked in a lane therefore loses 600 HP in about 90 rounds of contact.
- `catclock.py` walks each cat along straight segments between its waypoints and reports when its 2×2 first
  touches a king's starting 3×3:

| map (class) | cat clock (rounds) | lane overlaps the king's start |
|---|---|---|
| tminusten* | 12 | yes |
| sunandmoon* | 18 | yes |
| diagonalization* | 20 | yes |
| externalsustenance* | 36 | yes |
| t_minus_five_minutes* | 78 | no (adjacent) |
| directconfrontation* | 80 | no (adjacent) |
| groveisbeingdesecrated* | 98 | yes |
| chocolateheart* | 172 | yes |
| RUN* | 174 | no |
| peaceinourtime | 252 | yes |
| attack_on_cat* | 306 | yes |

\* tournament map. The observed first scratch on tminusten was r14 (`kcause.py`), against a predicted 12.

**Mines sit in cat lanes on most maps.** Map makers route cats through the mines. `catpark.py` measures the
fraction of mines whose 3×3 king footprint overlaps a straight-line cat lane: it is ≥ 0.5 on 52 of the 147 maps
with mines.
Examples: arrows 0.5, with the most central mine fully in a lane (9 of 9 cells); cheesefarm 1.0; keepout 0.58.
So "park the king on a mine", the rule of both awubot and OBG, often means "park in a lane". Kings survive it only
by stepping out when a cat comes.

**Starvation clock** (economy facts from rules.md §5, §11):
- Each mine yields ≈ 2.25 cheese per round. Each king eats 2 per turn, or loses 10 HP when global < 2.
- A side with k mines harvested at efficiency e has a net income of `2.25·k·e − 2·K` per round (K = its kings).
- On a 2-mine map (one mine a side) a king about breaks even. The 2500 starting bank then pays for everything else:
  rats at `10 + 10⌊n/4⌋` and their replacements, digs at 5, traps at 20, bite bonuses.
- Once the bank hits 0, a 600-HP king dies 60 rounds later.
- awubot's spawn floor `global ≥ 100 || cost == 10` (`src/challenger/robot/king/KingSpawningStrategy.java:69`)
  buys a 10-cheese rat at any balance while it has ≤ 3 rats. With replacements flowing, the bank runs out.
- On whereisthecheese the losing side built 56 rats (the winner 66) by r231 (ReplayStats `babyBorn`) on a 2-mine
  map. Both kings starved: the loser from r172 (600 HP), the winner later (150 HP).
- The starvation clock is `bank / (spend rate + 2K − income)`. In D1 the kings that died mostly of starvation
  died at r170–562 on mine-poor maps (5t4rv4t10n_1337, whereisthecheese, evileye, corridorofdoomanddespair,
  arrows) and up to r1218 on richer ones (closeup).
- **Who starves first loses.** The winner also starved on 5 maps. On 4 of them the loser starved more:
  whereisthecheese 600 vs 150, corridorofdoomanddespair 440 vs 80, arrows 250 vs 60, evileye 220 vs 50. tiny is
  the exception: 130 vs 220.

**Points clock.** The game ends at r2000, or when the last cat dies in cooperation (rules.md §1). On maps where the
armies cannot meet:
- no rat path at all (peaceinourtime, t_minus_five_minutes);
- or a long path through a narrow cut (pipes, rift, minimaze, cheesebottles: rat path 57–90, cut 2).

The game then ends on points: cooperation weights 0.5 cat damage, 0.3 kings, 0.2 cheese; backstab weights 0.3,
0.5, 0.2. On t_minus_five_minutes OBG won both seats on cat damage (10 740 vs 4 460, 8 000 vs 6 180; D3). This is
T3's domain. The map class just tells when it applies.

**King cutoff.** No new king after r1200 if the team has ≥ 2 kings (rules.md §6). This is T1's domain. For T4,
a second king only helps safety if it sits in a different threat zone (not in the same cat lane, not on the same
throw lines).

### 2.3 Information

What each unit can know (rules.md §6, §7, §13):
- A baby rat sees 21–22 tiles: a 90° cone, r² 20.
- A king sees 81 tiles: 360°, r² 25.
- Vision is not blocked by walls.
- Squeaks reach allies and cats within r² 16 (4 tiles) and live 5 rounds.
- Only kings write the 64 × 10-bit array.
- So a fact reaches the whole team only after a rat carries it to within 4 tiles of a king. A king's warning radius
  is 5 tiles alone, and about 9 tiles with a picket of rats squeaking from 4 tiles out.

What matters, ranked by its effect on the clocks above:
1. **Cat lanes.** They are deterministic (BFS on walls between fixed waypoints, rules.md §12) and they repeat
   cyclically, so the same lane is walked again every loop. Waypoints are invisible, but each sighting gives a
   position and a facing, and the facing is the BFS direction toward the next waypoint. Two sightings a few rounds
   apart give the lane locally. One full loop gives all of it.
   Warning needed to step out of a lane:
   - the king's 3 columns overlap a 2-wide lane in up to 2 columns;
   - clearing it takes ≤ 3 king moves, i.e. 12 rounds at 4 rounds per move;
   - the cat covers 6 tiles in 12 rounds;
   - so the king must know about the cat when it is ≥ 7–8 tiles away, beyond its own vision (5). Rats must relay.
2. **Threats to the king.** Enemy rats, and above all enemy **carriers** (§2.4), within 8 tiles, by direction.
3. **Own economy state.** Exact: `rc.getGlobalCheese()`, the number of kings.
4. **Mines.** Known by sight. With the symmetry known, every mine's mirror is a mine.
5. **Enemy king position.** Its start is the mirror of ours under the true symmetry, so there are 1–3 candidates
   from round 1. It moves 1 tile per 4 rounds at most. awubot's king parks on the mine nearest its start
   (T5 table, row A11), so its position is predictable from the map alone.

**Symmetry inference** (`symm.py`, D4):
- A wrong symmetry is eliminated by any tile whose mirror differs.
- On 14 of 150 maps the walls are symmetric under all three symmetries, or absent, so walls can never decide:
  Stash, averyfineline, canyoudig, channels, doyoudareenter, holeinthewall, microtest_wide, quadrants,
  streetsofnewyork, stuckinthemiddle, t_minus_five_minutes, therearenowalls, tictactoe, uneasy_alliance.
- Mines decide 10 of those 14. Initial dirt decides tictactoe and uneasy_alliance, but dirt changes during play,
  so only dirt seen before any unit could have touched it is usable. On microtest_wide and stuckinthemiddle walls,
  mines and dirt are symmetric under all three.
- awubot compares **passability only** (seen and not wall; `src/challenger/CellTracker.java.jinja2:360-366`), so it
  never learns the symmetry on those 14 maps.
- awubot uses the symmetry only for comms and for mirroring seen cells. Symmetric mine inference was removed
  (strategy.md §3), and `Util.getSymmetricLocation`/`guessEnemyLocs` are not called by live code
  (`src/challenger/util/Util.java:3723,3779`).
- OBG falls back to rotational after round 340 (`src/opp_obg/SymmetryChecker.java`, `getSym`).

**Scouting.** awubot's first rat walks to the map centre at round 2 (`src/challenger/robot/rat/MinerStrategy.java:114`).
Rats with their first turn in rounds ≤ 20 explore along random rays from the king. Later rats explore 20% of the
time forever (strategy.md §2.3). Theoretical value of scouting, in order:
1. mines (income);
2. cat lanes (king safety);
3. symmetry, which doubles 1 and gives 5;
4. the enemy king (only for pressure, §2.6).

The champion learns 1, gets 3 only from walls, and does not track 2. Its cat memory is the last 4 non-combat turns
of each rat (`catHist`, `src/challenger/RobotTracker.java.jinja2:146-148`), which never reaches the king's movement
logic. Rats near a cat stay silent unless the cat is within d² 25 of the king. That "panic" squeak is decoded as two
phantom enemies (strategy.md §7).

### 2.4 King safety, channel by channel

| channel | mechanism (rules) | what dominates | champion |
|---|---|---|---|
| starvation | −10 HP per king turn at global < 2 (§11) | an exact reserve rule, below | spends to 0 (spawn floor `:69`); 26% of the losers' king HP in D1 |
| cats | a king blocking the lane is scratched 20 per 3 rounds; in ATTACK, kings in the cone can be targeted (§12) | learn lanes; never park in one; on warning, move **perpendicular** to the cat's facing; if boxed in, rats stall or kill the cat (§2.5) | flees only cats within its own r² 25, scored by the direction to the cat's corner (away scores ≥ perpendicular), not by the cat's facing (`KingCombatStrategy.java.jinja2:118-170`); a bug in two ring cells (combat.md B3); parking ignores cats (`KingMiningStrategy.java.jinja2:173-278`) |
| thrown collisions | a flier hitting a king tile deals at most 42 on the throw turn, 38/34 next, to both; no backstab (§9) | keep enemy carriers off the 8 lines through the king's 3×3 within ~8 tiles, and keep own rats (ammunition) out of grab range near the king; dirt on aligned ring tiles absorbs throws (dirt is a collision, rules.md §9; `world/InternalRobot.java:780-812`) | every rat within d² 36 of the king fights (`CombatStrategy.java.jinja2:461`), so fights happen at the king; with enemies visible and no cat, the king **digs out its own ring dirt** (`KingCombatStrategy.java.jinja2:64-78,178`) |
| bites | 10 + ⌈√c⌉ from the 16 ring tiles (§7) | defenders and own spawns; deny ring tiles with dirt when besieged | king flee + bite + spawn toward the threat (combat.md §9) |

**Starvation reserve (exact).** With K kings and a measured net income I per round (cheese transferred over the
last 50–100 rounds), spending s keeps the king fed to r2000 if

    global − s ≥ max(0, 2K − I) · (2000 − t)

- When I ≥ 2K (rich maps) the floor is 0, so the rule changes nothing there.
- When I < 2K (mine-poor maps, or mines lost) it throttles spawns, traps, digs and bite bonuses so the bank
  outlasts the game.
- It turns "who starves first" from a coin flip into awubot's loss. In D1 the side that starved more lost on 4
  of the 5 maps where both starved (§2.2).
- A weaker form also works: a horizon H instead of 2000 − t, so the floor is `2K·H`. H = 60 matches the 60 rounds a
  full king survives starving.

**Perpendicular escape (arithmetic).** A cat moves 2× faster than a king (0.5 vs 0.25 tiles per round). Fleeing
along the cat's line, the cat closes at 0.25 tiles per round and catches the king. Moving perpendicular clears a
2-wide lane in ≤ 3 king moves. food's postmortem says the same (opponents.md §3, food).

**Pocket maps.** On tminusten each king starts in a 13 × 5 pocket bounded by walls at y = 5 and x = 14 (render:
`render.py maps.json tminusten`). The cat's waypoints are (0,2), (6,2), (12,2), so its 2×2 sweeps rows 1–3: T5
observed it stuck at (9,1) against the parked king. A 3×3 king cannot stay off rows 2–3 inside the
pocket, and the exits are 1–2 tiles wide, too narrow for it. The king cannot escape. Only rats can save it: kill
the cat, stall it with dirt, or feed it.

### 2.5 Dirt and walls

Costs (rules.md §4, §8):
- dig: 5 cheese, 25 action cooldown, +1 team dirt;
- place: 0 cheese, 25 cooldown, −1 team dirt;
- a cat digs 1 tile per 3 rounds: 30 cooldown, one tile per action (`world/InternalRobot.java:1255-1265`).

Uses:
- **Stalling a cat.** An orthogonal cat step needs 2 new tiles clear. A dirt wall across its lane costs it 3 rounds
  per tile. One rat places a tile every 2.5 rounds, so two rats re-placing out-pace one digging cat indefinitely
  (0.8 vs 0.33 tiles per round). Stock needed: 1 dirt per placement, so dig first (5 cheese each). Maps with initial
  dirt near the king make this cheap.
- **Pounce denial.** A cat cannot pounce onto dirt or a king (rules.md §16.18). awubot's `placeDefensiveDirt`
  (`KingCombatStrategy.java.jinja2:14-61`) places one tile toward a cat within d² 20. It needs team dirt, which the
  king only gets by digging.
- **Throw shield.** A flier collides with the first dirt tile in its path, and only the flier is damaged. Dirt on
  the ring tiles aligned with enemy carriers protects the king.
  - Cost: a full 16-tile ring is 16 placements, i.e. 40 rounds of king action plus 80 cheese of digging.
  - The ring also blocks spawning (buildRat needs a free ring tile, rules.md §8), so leave a gate on the safe side.
  - Transfers still work across it: the transfer range is d² ≤ 9 to any king tile, i.e. from 1–2 tiles outside
    the ring.
- **Sealing a choke.** On maps with cut ≤ 2 (pipes, rift, minimaze, cheesebottles) two dirt tiles separate the
  armies. Breaking through then takes an enemy rat 2.5 rounds and 5 cheese per tile, under fire. That converts the
  map into a points game. It only helps when we lead on points (T3). Cats still dig through.
- **Digging for tempo.** Digging gives the only path, or a shorter one, between the kings on 22 of 43 official
  maps (`kk_open` −1 or longer than `kk_dirt` in `maps.tsv`). awubot paths through dirt only when rich, or at t ≤ 2 (architecture.md §6). Digging costs
  5 cheese per tile, so on mine-poor maps it feeds the starvation clock.

### 2.6 Rush timing

The defender's edge:
- Spawn rate: 1 rat per round per king, appearing on the ring next to the king (build cooldown 10, rules.md §8).
- Zero travel for new rats.
- The king's own bite.
- awubot's cap: 12 rats on ≤ 900-tile maps before r400, 16 otherwise (`KingSpawningStrategy.java:23-27`), reached
  by about round 16 (strategy.md §1).

An attacker covering kk tiles arrives at about round kk with rats in a stream, against min(kk, 12–16) defenders
plus 1 more per round. So an opening rush on any map with kk ≥ 12 loses the local count, and on face-off maps
(kk ≤ 12) both kings are in the fight anyway. Measured: OBG, the best micro bot, lost DefaultSmall in both seats
(opponents.md §1). awubot won both seats of externalsustenance and split diagonalization and directconfrontation
(D3). The champion's close-quarters fighting is its strength. Don't contest it with a rush.

Pressure pays when the enemy king is crippled:
- **Starving.** It cannot spawn at global < cost, and it loses 10 HP per round. A starving awubot king shows
  exact −10 HP steps to any rat that watches it for 2 rounds.
- **Being scratched** by a cat.
- **Cornered.** Few free ring tiles mean few spawns. This is visible in its 3×3 surroundings.

So the clocks tell when to attack. For a starving king: when its HP drops by 10 every round with no bites.
For a cat: when the cat clock for the enemy's parking spot runs out. Knowing where its king is (§2.3, item 5) is
the prerequisite.

---

## 3. Map classes and the best plan per class

The classes come from `classes2.py` over D4, with thresholds from the arithmetic above. A map can be in several.
Official names plain, tournament `*`, team-made `^`.

| class | test | maps | what decides it (D1–D3) | best plan |
|---|---|---|---|---|
| **FACEOFF** (11) | rat path ≤ 12 | dirtfulcat, knifefight, thunderdome, tiny, diagonalization\*, gridlock\*, heart\*, uneasy_alliance\*, pvparena^ ×3 | king kill by r68–189 in D1; throws (knifefight 512) and bites; the cat on dirtfulcat | keep awubot's micro; add the throw shield (WP4); starvation reserve (tiny: 130) |
| **NOCONTACT** (6) | no rat path even through dirt | peaceinourtime, t_minus_five_minutes\*, coop^, mining^, testpathfinding^ ×2 | cooperation stays; points (cat damage 0.5) or a king killed by a cat | cat-safe king (WP2); maximise cat damage (T3); never starve |
| **CHOKE** (43) | min cut ≤ 4 | official: cheesebottles, EscapeTheNight, minimaze, pipes, rift, TheHeist, uneruesansfin, whatsthecatdoin, whereisthecheese, ZeroDay; 23 tournament (incl. tminusten\*, groveisbeingdesecrated\*, RUN\*, learn_your_lesson\*, insertmeowname\*); 10 team-made (`classes2.py` output) | 7 of D1's 11 points games; starvation (whereisthecheese 600, EscapeTheNight 210) | starvation reserve (WP1); traps/dirt at the choke; points play (T3) |
| **CATPATH** (11) | cat clock ≤ 320 (§2.2) | peaceinourtime; tminusten\*, sunandmoon\*, diagonalization\*, externalsustenance\*, t_minus_five_minutes\*, directconfrontation\*, groveisbeingdesecrated\*, chocolateheart\*, RUN\*, attack_on_cat\* | vs OBG on 10 of them (D2 tminusten + D3, 20 games): OBG won 15; awubot's king scratched to death in 7 | WP2 + WP3; then let the cat work on awubot's king |
| **MINEPOOR** (42) | mines ≤ 4 | 14 official incl. DefaultSmall, arrows, jail, evileye, corridorofdoomanddespair, whereisthecheese, trapped, starvation, tiny | starvation (corridor 440, evileye 220, arrows 250, 5t4rv 260); T5: OBG and GST lose there | WP1; spend the bank slowly; fewer, longer-lived rats |
| **DIRT** (27) | dirt ≥ 20% | 8 official incl. cheesefarm, whatsthecatdoin, canyoudig, streetsofnewyork, trapped | digging is the path and a cheese sink; cats dig too | dig only on the path you need; keep ring dirt as armour |
| **MINE-OVERFLOW** (5) | mines ≥ 26 | sieveofriches\* 30, tminusten\* 30, bridges^ 28, bridgesCatless^ 26, quadrants^ 38 | awubot's `writeCheeseMines` `assert(false)` destroys every king once it knows 26 mines (architecture.md §1.6; T5) | T5 P1 (delete the assert); then economy |
| **OPEN** (46) | none of the above | DefaultMedium, DefaultLarge, keepout, Meow, hatefullattice, mercifullattice, Nofreecheese, safelycontained, toomuchcheese, popthecork, closeup, … | kills at r245–1846; every channel; keepout's cats (1000), closeup/popthecork starvation (510/480) | economy (T1) + WP1 + WP2 |

`5t4rv4t10n_1337` is official but skipped when ABtest lists `maps/` (it is in `--pool official`; testing.md §7).
Any pool built by listing `maps/` must add it back explicitly.

**The champion's weak maps, explained.** intel.md §3.1 and tournament losses:

| map | class | mechanism (this file) |
|---|---|---|
| tminusten (lost ×3 in quals, r103–104) | CATPATH, CHOKE, MINE-OVERFLOW | pocket king in the lane, cat clock 12; scratched 580 HP, dead r102–104 in both seats vs OBG |
| attack_on_cat (final, r452) | CATPATH | lane through the corner start (cat clock 306); scratched 580 HP in both seats vs OBG |
| groveisbeingdesecrated (final, points r160) | CATPATH | cats die in cooperation; points on cheese/cat share (T3); awubot's king scratched 420 HP in one seat |
| peaceinourtime (deterministic local loss) | NOCONTACT, CATPATH | lane through the start (0.5 tiles); king killed by a cat in cooperation |
| t_minus_five_minutes (final, points) | NOCONTACT, CATPATH | cooperation points; lost to OBG on cat damage in both seats (D3) |
| whatsthecatdoin, jail, keepout (OBG ≈ 0) | CHOKE/MINEPOOR, MINEPOOR, OPEN | starvation (330 / 160 HP), throws/rest (443–693 HP), keepout cats (440) |
| corridorofdoomanddespair, dirtfulcat, safelycontained | MINEPOOR, FACEOFF, OPEN | starvation 440, cat 420, starvation 350 in D1 |
| sieveofriches (quals, cheese 30k vs 71–91k) | MINE-OVERFLOW | kings destroyed at 26 known mines (T5) |

**The "mine per new rat, king on the most central mine" hypothesis** (opponents agent): partly refuted for the
king, from this lens.
- What the king's mine is worth: it picks up only the 9 tiles under it (rules.md §7, d² ≤ 2 from the centre).
  That is 9/81 of a mine's spawns on 1.2.6 (9/64 on 1.2.5), ≈ 0.25 cheese per round, plus slow chasing.
- Its real value: shorter delivery trips, and spawning where the rats are needed.
- The most central mine is usually the one closest to the enemy and to the cat lanes: arrows, cheesefarm,
  closeup, krotus, mincemeat and t_minus_five_minutes put 6–9 of the 9 king cells of their most central mine in a
  straight-line lane (`catpark.py`).
- Dominant choice: the mine that minimises the rats' delivery distance to our own-side mines, subject to:
  - its 3×3 is off every known cat lane;
  - it is on our side of the contact line, which is the midpoint of the rat path;
  - the king has a perpendicular escape route there.

  Ties go to central.
- The "mine per new rat" half is T1's (economy), and this lens has no objection to it.

---

## 4. The dominant behaviour in this domain

1. **Upkeep reserve:** spend only above `max(0, 2K − I)·(2000 − t)` (§2.4). Remove the `cost == 10` exception.
2. **Cat lanes as map knowledge:**
   - rats squeak each cat sighting (position, facing, round) when near a king; kings store it in the array;
   - every unit marks the 2-wide forward lane of each sighted cat;
   - kings keep a decaying lane map over 3×3 units, like MapTracker.
3. **King placement:** park on the safest efficient mine (§3, last paragraph). Never re-park into a lane.
4. **King motion:** when a cat's forward lane covers the king's 3×3 within an ETA of ≤ 2 × (moves needed) × 4
   rounds, move perpendicular to the cat's facing.
5. **Rats defend the king from cats before contact.** When a lane hits the king and the king cannot clear it, as in
   a pocket: place dirt across the lane at 4–6 tiles, bite from outside the cat's 180° cone, and throw grabbed
   enemies into the cat (rules.md §9: feeding kills the flier and the cat sleeps for 2 turns).
6. **Throw lines:** with enemy carriers within 8 tiles, keep ring dirt on their side, add dirt on aligned ring
   tiles, never dig own ring dirt, and keep spawning on the far side.
7. **Information:** resolve the symmetry by mines as well as walls. Keep the enemy king's candidates. Watch for the
   starvation signature (−10 per round) on a seen enemy king.
8. **Tempo:** no opening rush. Pressure the enemy king when one of the clocks has already crippled it.
9. **Class-specific:** on NOCONTACT and points-bound CHOKE maps, the score is cat damage, kings and cheese (T3); on
   MINE-OVERFLOW maps the assert must go first.

---

## 5. Deviations of the champion and the top bots

| behaviour | awubot (= challenger) | OBG (`src/opp_obg`) | GST (`src/opp_gst`) | others |
|---|---|---|---|---|
| upkeep reserve | none; floor `global ≥ 100 \|\| cost == 10` (`robot/king/KingSpawningStrategy.java:69`) | spawns while `global − (2100 − round) ≥ 5·cost` above cost 40 (opponents.md §3), an implicit reserve | starved in 7 of 7 king-kill losses (T5) | SPAARK: threshold with `max(0, 1500 − round)`; Parmesan reserve `cost·(20 − 15t/2000)` |
| king placement | centre walk to the edge of the taboo box, then the nearest unassigned mine; cats ignored (`robot/king/KingMiningStrategy.java.jinja2:53-76,173-278`) | most central known mine (`RatKing.java:89-133`) | closest **safe** mine: threat levels from enemies and cats, the symmetric mine marked 127 (opponents.md §3) | cerr: king waits ("CORNER") until safe; Gravy camps a mine |
| king vs cats | flee only with a cat in vision; scored on the direction to the cat's corner; one dirt tile toward a cat within d² 20; ring bug B3 (`KingCombatStrategy.java.jinja2:14-61,102-170`) | dirt between king and cat within d² 8 (`RatKing.java:369-392`); emergency spawns toward a cat with no closer ally | flees squeaked cats; `catPath`/`attemptLeaveCatPath` | Parmesan: cats as walls, anti-cat dirt, suicide attack; food: perpendicular escape |
| cat information | `catHist` per rat, 4 rounds; panic squeak only, decoded as phantom enemies | squeaks a cat only when it is closer to our king than the rat (`SqueakManager.java:161`) | squeaks cats (king flees) | Gravy baits cats into the enemy king |
| throw-line safety | all rats within d² 36 fight at the king; the king digs out its ring dirt when enemies are visible | flier squeaks its direction; allies dodge (opponents.md §4 #4) | – | – |
| symmetry | walls only (`CellTracker.java.jinja2:360-366`); unused for targeting | walls (`SymmetryChecker.java`); rotational assumed after r340 | squeaked symmetry | Parmesan rushes the symmetric spawn images |
| cat damage | engages cats only within d² 36 of its king, or when rich after r500 (`CombatStrategy.java.jinja2:455-456`) | 7 500–8 000 cat damage by r100 on tminusten (D2) | – | – |

Read on the clocks:
- awubot is behind OBG on the cat clock (cat lanes, cat damage) and on the starvation clock (no reserve).
- It is ahead on the contact clock (micro on face-off and small maps).
- GST shares awubot's starvation weakness (T5).

---

## 6. Work packages

Effects are against awubot under official conditions. Expected effects are estimates from the decompositions above,
not measurements. Test protocols use `tools/games` (paired seats, sequential test, gauntlet; docs/evaluation.md
once it lands) and propose these map pools from `classes2.py`:
- `cat`: the 11 CATPATH maps;
- `starve`: the official MINEPOOR maps plus closeup, popthecork and DefaultMedium (D1 starvation ≥ 280 HP);
- `choke`: the official CHOKE maps.

Add `5t4rv4t10n_1337` explicitly. `cat` and `starve` overlap the planned `weak` pool (intel.md §3.1 maps); the
`stress` pool should include the MINE-OVERFLOW maps.

### WP1. Starvation reserve (king upkeep floor)

- **Hypothesis.** A spend rule `global − s ≥ max(0, 2K − I)·(2000 − t)` (§2.4), and dropping the `cost == 10`
  exception, keeps our king fed in every game where awubot's policy starves it.
- **Derivation.** D1: starvation was ≥ 200 HP of the loser's king in 14 of 32 kills, plus sittingducks (points).
  Where both kings starved, the one that starved more lost on 4 of 5 maps. On rich maps (I ≥ 2K) the floor is 0,
  so the change is a no-op.
- **Expected effect.** +5 to +10 points of win rate on the official pool.
  - The 14 starvation maps each split 1–1 by seat today. Flipping the half where we were the starving seat is +7
    games of 86 (+8 points) if starvation was decisive. On the 5 maps with ≥ 400 HP starved it was; elsewhere the
    king was also being bitten.
  - Larger on the `starve` pool.
  - Also helps tiebreak 2 (global cheese) at r2000 (rules.md §1).
- **Test.**
  - Sequential test on `starve` and the tune pool vs awubot.
  - Read the flipped games: the starvation turns of our king (`kcause.py`) must go to 0.
  - Gauntlet: OBG, GST.
- **Risks.** Fewer rats when poor, so fights are lost earlier on mine-poor face-off maps (tiny, DefaultSmall).
  I needs a rolling estimate; a stale I under-spends. The bytecode shift reshuffles close games (AGENTS.md rule 6).
- **Size.** Small: one predicate in `KingSpawningStrategy` and the trap/dig/bite-bonus gates, about 30–60 lines,
  plus an income counter from transfer events.

### WP2. Cat-safe king: lane memory, safe parking, perpendicular escape

- **Hypothesis.** awubot's kings stand in cat lanes: at the start on CATPATH maps, and parked on mines in lanes on
  many others. A king that knows the lanes and steps perpendicular before contact survives, while awubot's dies to
  the same cats.
- **Derivation.** §2.2 cat clock; §2.4 escape arithmetic. D2 + D3: awubot's king was scratched to death in 7 of 20
  games vs OBG on CATPATH maps. D1: scratches were 12% of the losers' king HP (keepout 1000, dirtfulcat 420,
  dirtpassageway 300).
- **Components.**
  1. Rats squeak a cat sighting (id, position, facing) when within r² 16 of a king. This replaces the panic squeak
     and fixes the phantom-enemy header bug (strategy.md §7).
  2. The king writes the last sighting per cat to the array: slot 15 is free (architecture.md §4.1), plus the cheese
     list's spare range.
  3. Kings and pathing mark the cat's 2-wide forward lane for ~10 tiles.
  4. KingMiningStrategy excludes mines whose king footprint was in a lane.
  5. KingCombatStrategy scores moves by leaving the forward lane: perpendicular first, not away. Fix the B3 cells.
- **Expected effect.** CATPATH has 1 official map, so +1 to +3 games of 86 on the official pool. Large on the `cat`
  pool and on tournament maps: from about 50% (a seat split) to 70–85%. Pocket maps like tminusten need WP3.
- **Test.**
  - Sequential test on `cat`, then the tune pool for no regression.
  - Metric beyond wins: scratch HP on our king per game (ReplayStats `kingLoss`) must fall.
  - Gauntlet: OBG (its kings survive cats, so the gain against it is smaller).
- **Risks.** The king leaves its mine more often, so a little less income. Squeak bandwidth competes with enemy
  reports. A wrong lane estimate around walls: cats BFS around walls, and straight lines are only a local
  approximation.
- **Size.** Medium: about 150–250 template lines over Squeaker, GlobalArray, KingMiningStrategy and
  KingCombatStrategy.

### WP3. Rats defend the king from cats before contact (dirt stall, rear bites, feeding)

- **Hypothesis.** When the lane hits the king and the king cannot clear it in time (tminusten pocket,
  corner starts), rats can save it:
  - stall the cat with dirt: 2 rats out-place one cat's digging (§2.5);
  - bite it from outside its cone: cats in EXPLORE never target rats (rules.md §12, §16.2);
  - throw enemies into it.
  This also raises our cat damage, which is 0.5 of the cooperation score.
- **Derivation.** awubot engages cats only within d² 36 of its king, so its rats arrive after the scratching starts.
  OBG dealt 7 500–8 000 cat damage by r100 on tminusten and its king survived (D2).
- **Expected effect.** Target: both seats of tminusten, attack_on_cat, sunandmoon and peaceinourtime, where
  awubot's king died to cats vs OBG (+1 game per map over today's seat split, up to +4 on the `cat` pool). Small
  on the official pool.
- **Test.** `cat` pool, both seats. Check that awubot's king dies to the cat and ours does not (`kcause.py`
  scratch column).
- **Risks.**
  - Rats pulled to cats leave fights.
  - Dirt stocks cost 5 cheese per dig, which feeds the starvation clock.
  - Cats in ATTACK target rats in their cone, so bites must stay outside it.
  - Overlaps T2 (combat) and T3 (cat damage): coordinate.
- **Size.** Medium: about 150–300 lines, a new rat strategy gated on "lane hits own king within ETA".

### WP4. Throw shield: keep fights and carriers off the king's lines

- **Hypothesis.** About 31% of the losing kings' HP in D1 is thrown collisions plus boosted bites. Two awubot
  behaviours feed it:
  - every rat within d² 36 fights next to its king, which supplies the enemy's ammunition;
  - the king digs out its ring dirt when enemies are visible, which removes cover.

  The new rules:
  - never dig own ring dirt while enemies are within 8 tiles;
  - place dirt on ring tiles aligned with enemy carriers;
  - spawn on the far side;
  - move the king off a carrier's line, perpendicular, as for cats.
- **Expected effect.** +2 to +5 points on FACEOFF and the small OPEN maps. Uncertain: the throw share includes
  boosted bites.
- **Test.**
  - Sequential test on `faceoff` (FACEOFF + DefaultSmall, DefaultLarge, averyfineline) and the quick pool.
  - Metric: our king's throw+rest HP per game.
  - Gauntlet: OBG and GST, both throw-heavy.
- **Risks.** Dirt blocks our own spawns and flight paths. King action cooldown is spent on dirt instead of bites.
  This is the most behaviour-changing package here.
- **Size.** Small to medium: about 80–150 lines in KingCombatStrategy (delete `digSurroundingDirt` first, rule 7).

### WP5. Map-class pools for evaluation (no bot change)

- **Hypothesis.** The tune pool dilutes every package above. Each acts on a known class (§3). Judging on the class
  pool first, then checking for no regression on the full pool, finds effects with fewer games.
- **Deliverable.** `cat`, `starve`, `choke`, `faceoff` lists in `tools/games`. Put the classifier
  (`classify.py`, `catclock.py`, `classes2.py`) next to it so new maps are classed the same way.
- **Size.** Small. Owner: track E. Map lists only.

### WP6. Information: symmetry by mines, enemy-king candidates, the starvation signature

- **Hypothesis.** Mines decide the symmetry on 10 of the 14 maps where walls cannot (early dirt on 2 more). Tracking the enemy
  king and its −10 per round signature enables the pressure of §2.6.
- **Expected effect.** Alone, < 2 points. It is an enabler for a pressure package (T2/T5) and for mirrored mine
  knowledge (T1).
- **Test.** Unit check: instrument a scratch copy to log the round the symmetry is resolved per map, before and
  after. Then the tune pool for no regression (bytecode).
- **Risks.** Bytecode: `checkSymmetry` costs ~400 per turn already (architecture.md §8). Mines are rare, so the
  check is cheap if keyed on mine sightings only.
- **Size.** Small: about 50–100 lines in CellTracker/Squeaker.

### WP7. Choke play (traps and dirt at cut ≤ 2), low priority

- **Hypothesis.** On cut-2 maps (pipes, rift, minimaze, cheesebottles) two dirt tiles or two traps hold the only
  path. Holding it when ahead on points freezes the result, and opening it when behind forces fights.
- **Expected effect.** Unknown. These maps already go to points in D1 (coin flips by seat). Depends on T3's points
  model.
- **Size.** Medium. Do it only after T3 settles the points policy.

---

## 7. Mechanics not settled here

- **Cat lanes around walls.** All cat-lane numbers use straight segments between waypoints. The engine's cats BFS
  over walls with their 2×2 footprint (`world/GameWorld.java:223`). `catclock.py` matched tminusten (12 vs 14
  observed), but lanes on wall-heavy maps can differ. Settled by K2's port of `bfsFromTarget` and the EXPLORE loop
  (`~/battlecode26-data/k2/catlanes.py`, the 19 `L-cat` maps): the BFS loop equals the straight segments on 11 maps
  (Jaccard 1.0) and differs on wall-heavy ones (bed 0.59, closeup 0.64, shell 0.46–0.53, QueenOfHearts 0.49); mines
  whose king 3×3 meets the lane are nearly the same count either way. From one sighting, a straight projection along
  the cat's facing holds 81% of the tiles it covers in its next 4 steps and 55% in 10.
- **Whether a cat in ATTACK prefers a king.** rules.md §12 says it picks the last baby rat or king in the chirality
  scan order. A king spans 3×3, so how often it ends up "last" depends on the scan; not measured.
- **Starvation detection is a lower bound.** A bite by a rat that acts after the king in turn order lands in the
  previous round's log. The filter excludes those rounds, so some starvation is missed; only an exact −10 with no
  bite or scratch nearby counts, so false positives should be rare.
- **1.2.6.** All replays ran the 1.2.5 jar with the map seed. The class structure and the king channels do not
  depend on the cheese RNG; exact rounds and incomes do.
