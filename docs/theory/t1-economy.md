# T1 Economy and macro: theory

Lens T1 of track T (docs/program.md). Scope: cheese income and spending, the rat cost escalation, king upkeep and
starvation, the spawn curve, second and further kings, mine geometry, the round-1200 king rule. Combat, scoring
weights, cats and opponent models belong to T2–T5 and appear here only where they set an economic quantity.

Sources. "rules §n" = docs/rules.md. Engine paths are relative to `engine/src/main/battlecode/` at tag
`engine.1.2.6` (clone: `~/battlecode26-data/engine/ref/battlecode26`). Bot paths are relative to `src/`. **(M)** marks
a measurement from replays or maps, reproducible with the scratch tools in `~/battlecode26-data/theory/t1/`
(`README.txt` there lists tools, inputs and commands). **(A)** marks my arithmetic.

## 0. Headline

1. **The pie is fixed and already eaten.** Every mine yields 2.26 cheese/round whoever harvests it, and cheese never
   decays. From round 200 the champion mirror harvests a median 86–93% of the whole map's output (M). A team's
   income is its share of that pie: it rises by holding more mines, not by mining harder. OBG's and GST's 1.4–2×
   income against awubot is a 58–75% share of the same pie, and it starts from losing fewer rats early.
2. **The army is sized by income and attrition, not by a constant.** In steady state I − 2k = δ·n·c(n−1). At the
   mirror's income (8.2/round) and attrition (0.0126 deaths per rat-round) that gives about 12–16 rats, so the champion's
   fixed cap of 16 fits an even contested mirror, over-builds where trades are fast (the bank drains to ~0) and
   under-builds on low-contact maps (sustainable 20–44 rats).
3. **Kings are the cheapest durable asset.** 150 cheese at round ~10, 300 at 16 rats, plus 2/round. A second king
   cuts the delivery distance by ≥ 25% on 21 of 43 official maps and removes it on 8, must be killed separately in a
   game that ends by king kill 74–79% of the time, and is the largest points term. The champion forms one in 44% of
   official-pool team-games, first at a median r300, and had a third alive in 2 of 306 mirror team-games.
4. **Dominant macro:** size the army from measured income and attrition above a small explicit reserve; form the
   second king early, with exactly 7 recruits, at the mine that best pairs with the first king's; lock in kings 3–5
   before round 1201 in every game still running; assign miners by mine deficit and stop exploring once the map is
   known; spend the surplus on kings, then traps, then rats above the sustainable size.

## 1. Data

| set | content | caveat |
|---|---|---|
| mirror | 153 replays in `matches/` (challenger = awubot code, engine 1.2.5, many seeds), 73 unique games once mirrored duplicates are removed | quick-pool maps are over-represented. "Official-pool subset": the 31 maps with exactly one seat pair (62 files, 31 unique games; identical code in swapped seats replays the same game) |
| opp | 76 validation games `opp_*` vs awubot, engine 1.2.5, seed 0 (copied to `~/battlecode26-data/theory/t1/replays-opp`) | 14 each for OBG and GST, 6 for each other bot |
| maps | the 43 official `maps/*.map26` | path distance: 8-neighbour Dijkstra, walls impassable, dirt cost 3 (a dig adds 25 action cooldown and costs 5 cheese) |

Replays record per round the cheese transferred (king pickups included, rules §1), baby rats alive, and every spawn,
death, transfer, trap, trigger and bite. Global cheese is in them too, found after this lens was written:
`Round.teamAliveRatKings` holds kings + 10 × global (`~/battlecode26-data/scratch/kb/Bank.java` prints it per round).
The claims below were made without it; bank levels here come from the instrumented probe in strategy.md §8 and §10.1.

## 2. Derivation

### 2.1 Supply

- Each mine rolls `rand < 1 − 0.99^r` every round, r = rounds since its pair last spawned. On success 20 cheese lands
  in the box of each mine of the pair and both timers reset (`world/CheeseMine.java:46-51`,
  `world/GameWorld.java:506-551`). A pair rolls twice per round, so P(no spawn for n rounds) = 0.99^(n(n+1)) and the
  mean interval is Σ_{n≥0} 0.99^(n(n+1)) = **8.86 rounds** (A). Per mine: 20 / 8.86 = **2.257 cheese/round**. A
  self-paired mine: 40 per 12.52 rounds = 3.20/round (A; rules §5 agrees).
- Map supply = 2.257·M per round for both teams together, 4514·M over 2000 rounds. Official maps: M from 1 to 18,
  median 6, mean 7.7 (M: `mapstats.csv`). A median map yields 13.5/round, about 6.8 per side.
- Cheese never decays and stacks on a tile (rules §5). Unharvested output is delayed, not lost, and goes to whoever
  arrives first.
- On 1.2.6 the cheese lands uniformly in the 9×9 box around the mine; if the offset is a wall or off the map for
  either mine of the pair, both 20s land on the mine tiles (rules §5, §16.16). A king centred on its mine covers 9 of
  the 81 box tiles.

### 2.2 Costs

| item | rule (source) | arithmetic |
|---|---|---|
| baby rat | c(n) = 10 + 10⌊n/4⌋, n = baby rats alive, kings not counted (`world/RobotControllerImpl.java:965-969`); paid from global only (`:977-999`) | building n from zero: B(n) = 20m² + 20m for n = 4m. B(16) = 400, B(24) = 840, B(32) = 1440, B(40) = 2200, B(48) = 3120. The 2500 bank buys 42 rats at once |
| replacement | a death lowers n, so a replacement costs c(n−1) | 40 at a 16-rat army, 60 at 24, 80 at 32 |
| king upkeep | 2 global per king turn; with global < 2 the king loses 10 HP instead (`world/InternalRobot.java:1181-1189`) | 0.89 of one mine's output; 3980 over a game for a king formed at round 10; a starving 600-HP king lasts 60 rounds |
| new king | 50 global; ≥ 7 allied babies in the 3×3 counting the centre; < 5 kings; not (round > 1200 and kings ≥ 2) (`world/RobotControllerImpl.java:1162-1211`); HP = allies' HP sum capped at 600 (`:1264`) | removes 7 babies from n (the centre and 6 ring allies; 9 if the whole ring is allied), so the rebuild runs at the bottom of the curve: 50 + Σ_{k=n−7}^{n−1} c(k) = **150 at n = 7**, 160 at 8, 230 at 12, **300 at 16**, 370 at 20, 440 at 24 |
| carried cheese | forward move costs (int)(10·(1 + 0.01·raw)), strafe (int)(18·(1 + 0.01·raw)) (rules §4) | speed 1.0 tile/round up to 9 raw, 0.83 at 20, 0.67 at 50, 0.5 at 100, 0.33 at 200 |

### 2.3 The pie is already eaten (M)

Both teams' cheese transferred per window divided by the map supply, mirror games, median over maps (`an/sat.py`,
output `sat.txt`):

| rounds | 0–100 | 100–200 | 200–400 | 400–700 | 700–1000 | 1000–1500 | 1500–2000 |
|---|---|---|---|---|---|---|---|
| maps with data | 43 | 37 | 24 | 19 | 17 | 11 | 9 |
| harvest / supply, median | 0.38 | 0.72 | 0.93 | 0.90 | 0.90 | 0.88 | 0.86 |
| maps ≥ 0.75 | 3 | 18 | 21 | 19 | 17 | 11 | 9 |

The exceptions at 200–400 are peaceinourtime (0.40, walled halves) and cheeseguardians (0.52, 10 cats). This is a
lower bound on harvest: raw cheese a rat spends on traps or bites before delivering is not counted as transferred.

Consequences (A):
- After the opening, a team's income is 2.257·M·share, with share ≈ 0.5 in the mirror. Extra miners on saturated
  mines add nothing.
- The opening (r0–200) is the only phase with spare supply: (0.62 + 0.28)·225.7·M ≈ 200·M cheese lies on the ground
  at r200, half on each side. Taking 60% of that stock instead of 50% is worth ≈ 20·M ≈ 120 cheese on a median map,
  small next to the 2500 bank. The opening is won by fighting, not by mining speed.
- From r200, a 60/40 split is a gap of 0.2·2.257·M ≈ 2.7/round between the teams on a median map, 5.4/round at
  70/30: the size of the OBG and GST income gap.

### 2.4 Income share is territory, and it snowballs

OBG vs awubot, both seats, 14 games, window averages (M: `an/yield.py` on `opp.csv`):

| window | awubot: income / babies / deaths per 100 rounds | OBG: income / babies / deaths per 100 rounds |
|---|---|---|
| 10–200 | 6.0 / 14.1 / 20.4 | 6.0 / 13.6 / 15.1 |
| 200–500 | 6.6 / 12.3 / 16.9 | 8.3 / 15.0 / 12.9 |
| 500–1000 | 5.9 / 8.6 / 15.3 | 13.8 / 16.8 / 13.4 |

Same income and army in the opening with 26% fewer deaths, then a growing share and army. On cheesefarm (M = 10,
supply 22.6/round) OBG took 15.8/round against awubot's 5.2 in r200–500: 21.0 in total, a 75/25 split of a saturated
pie. On keepout with GST in seat A, r500–1000: 18.3 vs 8.7/round with 22.8 vs 15.9 rats.

The loop: share → income → sustainable army (§2.5) → mines held → share. It starts from a combat edge, not a spawn
formula: OBG's and GST's base caps are the champion's 16 rats (§4.2). The economy cannot create the first edge; it
decides how fast an edge compounds, and whether the champion's edges compound.

### 2.5 How many rats: the sustainable army

Steady state with n baby rats, k kings, income I (transferred plus king pickups per round), δ deaths per rat-round:

    I − 2k − (traps, digs, bite cheese) = δ · n · c(n−1)

c grows linearly in n, so spending grows as n², and the army an income buys is roughly n* ≈ √((I − 2k)/(2.5·δ)) (A).
Mirror, r200–500, 88 team-windows (M: `an/sustain.py`): mean I = 8.2, n = 14.6, δ = 0.0126, spawn spend 5.1/round.

n* for k = 1, other sinks ignored (A):

| I \ δ | 0.005 | 0.008 | 0.011 | 0.015 | 0.02 | 0.03 |
|---|---|---|---|---|---|---|
| 5 | 15 | 12 | 9 | 8 | 7 | 5 |
| 8 | 20 | 16 | 13 | 12 | 10 | 8 |
| 14 | 30 | 24 | 20 | 16 | 15 | 12 |
| 20 | 36 | 28 | 24 | 20 | 18 | 15 |

Per-game n* against the army actually fielded, mirror r200–500 (M):
- contested maps match: cheesefarm n* 8–16 vs 12–19 fielded, Meow 11–12 vs 12, sittingducks 14–23 vs 16–21;
- low-contact maps sit far below n*: averystrangespace 32–37 vs 16, minimaze 40–44 vs 28–32, ZeroDay 20 vs 14,
  wallsofparadis 20–25 vs 16–21, cheesebottles 21–26 vs 18–19;
- fast-attrition maps run above n* until they collapse: DefaultMedium 97 cheese and 4 rats at r200, starvation 0
  cheese at r200, cheesefarm 8 cheese at r500 (strategy.md §5.1, §8).

So the fixed cap is right for an even contested mirror and wrong at both ends. Under fast attrition each death costs
30–40 and buys a rat that dies again. On low-contact maps the bank idles while the army could be 1.5–2.5× larger;
the rich lines (`2000 − t/2`, `3000 − 0.75t`) release that cheese only after it has idled for hundreds of rounds
(strategy.md §10.1: 700–1850 global held at the cap in mirror games).

Limits: δ depends on n and on the opponent (T2). The equation sets a budget, not a target the army must fill; under
saturation a larger army adds income only through territory.

### 2.6 How many miners: delivery geometry

A rat harvesting a mine at path distance d from the nearest king, carrying L per trip with τ rounds of overhead per
trip (pickup detours, turning to transfer; assumed 5, not measured), walks out at 1 tile/round and back at
1/(1 + 0.01·L): a trip lasts d·(2 + 0.01·L) + τ. Rats needed to carry one mine's 2.257/round (A):

    n_mine = 2.257 · (2d/L + 0.01·d + τ/L)

| d \ L | 10 | 25 | 40 | 60 | 100 |
|---|---|---|---|---|---|
| 5 | 3.5 | 1.5 | 1.0 | 0.7 | 0.5 |
| 10 | 5.9 | 2.5 | 1.6 | 1.2 | 0.8 |
| 20 | 10.6 | 4.5 | 3.0 | 2.1 | 1.5 |
| 40 | 20.1 | 8.6 | 5.7 | 4.1 | 2.8 |

- Miners scale with d/L. Large loads are cheap: the penalty only slows the return leg and is zero up to 9 raw. The
  champion's go-home thresholds (10 raw within d² 25, 25 within d² 100, `robot/rat/MinerStrategy.java:181-186`) make
  short trips small and frequent.
- A king parked on a mine harvests it with no miners. The champion's kings collect **17% of its income** by direct
  pickup (M, in the mirror and against the opponents; OBG 6%, GST 9%), and 41–47% on DefaultSmall, arrows and
  popthecork.
- Kings set d. Mean path distance from the best king site (a mine) to the side's other mines, with one king and with
  two kings on the best pair (M: `MineMedian.java`; side = own mines plus mines within 4 path tiles of equidistant):

| effect of a second king | maps (mean d, one king → two kings) |
|---|---|
| removes delivery (2 side mines) | arrows 8, tiny 15, starvation 16, canyoudig 26, jail 44, trapped 45, uneruesansfin 51, whatsthecatdoin 57 |
| cuts d by ≥ 25% (21 maps) | peaceinourtime 58→14, minimaze 38→14, Nofreecheese 18→7, popthecork 11→6, evileye 9→5, EscapeTheNight 27→15, keepout 21→12, sittingducks 14→8, rift 16→10, thunderdome 12→8, closeup 18→11, pipes 20→13, toomuchcheese 16→11, knifefight 18→12, TheHeist 21→14, cheesebottles 45→30, hatefullattice 14→10, wallsofparadis 21→14, DefaultMedium 13→10, mercifullattice 11→8, cheesefarm 12→9 |
| cuts d by < 25% (10 maps) | DefaultSmall 6→5, DefaultLarge 20→18, cheeseguardians 17→13, dirtfulcat 12→10, dirtpassageway 28→27, Meow 10→9, ZeroDay 9→8, averystrangespace 102→102, safelycontained 20→16, streetsofnewyork 32→31 |
| one side mine | 5t4rv4t10n_1337, averyfineline, corridorofdoomanddespair, whereisthecheese |

Examples (A, L = 40, τ = 5). keepout, 6 side mines: one king leaves 5 mines at d = 21.4, 5 × 3.2 = 16 miners, the
whole 16-rat cap; two kings leave 4 at d = 12.3, 4 × 1.9 = 8. minimaze, 7 side mines: 6 × 5.4 = 32 miners vs 5 × 2.2
= 11. On spread maps one king and 16 rats cannot both mine and fight; this is where the mirror reaches 0.9 of supply
only with 28–31 rats (minimaze) and where OBG and GST out-earn awubot (keepout).

### 2.7 Kings

**Price.** 150–440 cheese depending on the baby count at formation (§2.2), plus 2/round while alive. The 7 rats are
rebuilt at the bottom of the cost curve. Raw cheese of every robot crushed in the 3×3 goes to global, and enemy
babies in the 3×3 die without a backstab (rules §8, §16.5).

**Value.**
1. *Survival.* A team loses only when its last king dies. Mirror: 60 of 73 unique games end by king kill (79%);
   official-pool subset 23 of 31 (74%). Races are frequent: in 9 of the 60 unique mirror kill games (15%) the
   winner's own king total was below 300 HP over the last 60 rounds, in 19 (32%) below 500 (M: `an/race.py`). A
   second king away from the first converts such races. King HP never heals; forming a king is the only way to add
   to the HP the enemy must remove to win.
2. *Points.* Backstab points = 30·cat + 50·kingShare + 20·cheese shares (rules §1). From 2 vs 2 kings, a third moves
   the margin by 10 points, a fourth by 16.7, a fifth by 21.4 (A). Official-pool subset: 8 of 31 unique games (26%)
   were points games; 5 ended 2–2 in kings and 3 ended 2–1, all 3 won by the side with more kings (M). The 5 ties
   were decided by margins of 0.7–6.3 points, mostly cat damage (uneruesansfin: 8174 vs 7708 transferred and 4020
   vs 3980 cat damage, a 0.7-point margin; rift 6.3) (A), so one extra king (10 points) decides each of them. Upkeep
   is paid from global, which is not in the score: a king costs points only through a weaker army.
3. *Delivery and harvest.* §2.6: a second king cuts d by ≥ 25% on 21 maps and removes it on 8; a king on a mine
   harvests it without miners.
4. *Production.* `buildRat` adds 10 action cooldown, so a king spawns at most one rat per round (rules §4). Two kings
   double the surge rate when defending or rebuilding, at two places.

**Timing (M, official-pool subset).** Games still running at r150: 94%; r200: 84%; r300: 71%; r400: 58%;
r600: 48%; r1200: 45%; r2000: 23%. The champion formed a king in 27 of 62 team-games (44%): first formation at
r130–310 in 15 (rift, toomuchcheese, mercifullattice, keepout, safelycontained, peaceinourtime, hatefullattice,
EscapeTheNight, minimaze), r460–1040 in 5, and r1630–1910 in 7 (the relaxed endgame gate); median first formation
r300. Over all 306 mirror team-games, 3 kings were alive at once in 2.

**The round-1200 rule.** After round 1200 a team with ≥ 2 kings can never add one; a team with 1 can add one
(`world/RobotControllerImpl.java:1173-1179`). Kings 3–5 exist only if formed by round 1200, and formed kings are
never removed (rules §16.19). 45% of official-pool games are still running at r1200, and 23% reach r2000.

**Cheapest moment.** The earliest: the first 7 rats cost 100, the formation 50, and the baby count drops to 0, so the
next rats cost 10 again (150 in total). First contact comes at r4–25 on most maps (strategy.md §0), so an opening
king trades 7 rats' early presence for the insurance. A king at r40–150 with 12–16 rats costs 230–300.

**How many kings income carries.** Upkeep 2k against a side's half of the supply, 1.13·M (A): k ≤ 0.56·M with no
army at all. On M = 4 maps two kings eat 4 of a side's 4.5/round, so there each king must sit on a mine (a parked king
harvests about its own upkeep) and the extra king is insurance and points, not income.

### 2.8 Reserve, float and starvation

- A reserve covers (1) upkeep through an income outage, 2k per round, and (2) a defensive surge when a king is
  attacked: 8 rats above a 16-rat army cost 4·50 + 4·60 = 440, 16 rats 1040 (A). The champion's own defence raises
  the cost cap by 10–30 (panic +10, +10 more at ≤ 100 HP, cats +10; `robot/king/KingSpawningStrategy.java:34-36,59-66`),
  i.e. 4–12 extra rats for 200–720 cheese. Cheese above that is spent only when the rich lines come down to it.
- The champion holds 700–1850 global for hundreds of rounds at its cap in contested mirror games (strategy.md §10.1):
  up to ~1300 above its defence needs, the price of 1–4 kings.
- Starvation: the spawn rule `global ≥ 100 || cost == 10` (`KingSpawningStrategy.java:69`) lets a broke team spend to
  0 on cost-10 rats and leave nothing for upkeep (observed: 0 cheese at r200 on starvation, 8 at r500 on cheesefarm,
  strategy.md §5.1). A cost-10 rat that delivers ≥ 10 before dying pays for 5 rounds of upkeep, so the failure is
  the income collapse; still, the floor should keep 2k·H (H ≈ 30–60 rounds) unless a king is under attack.

### 2.9 Other sinks (M, ratio to cheese transferred, end of game)

| bot | spawn spend | rat traps (20 each) | bite cheese (≤ 2 per bite, upper bound) | digs (5 each) |
|---|---|---|---|---|
| champion, mirror | 0.80 | 0.28 | ≤ 0.08 | 0.04 |
| champion vs opp_* | 0.95 | 0.36 | ≤ 0.09 | 0.05 |
| OBG | 0.76 | 0.31 | ≤ 0.06 | 0.06 |
| GST | 0.72 | 0.42 | ≤ 0.08 | 0.05 |

Rat traps fire on an enemy for 94–100% of those placed, for every bot (champion 98% in the mirror; M: `TriggerTrap`
events of the opposing team ÷ `PlaceTrap`). A trap turns 20 cheese into 50 damage plus a movement-cooldown stun
almost surely: 2.5 damage per cheese, outside the rat-cost escalation, using no rat slot (≤ 25 active, rules §10). A
cheese bite gives +2 damage for 2 cheese and falls to 0.1 damage per cheese at 100 (rules §8). The 28% of income
the champion puts into traps is well spent; with the army at n* and the bank above reserve, more traps are a better
use of surplus than rats above n* (placement is T2).

### 2.10 Endgame economy

- The score uses cheese transferred, kings alive and cat damage (rules §1); global cheese only breaks exact point
  ties (tiebreak 2). In a game heading for r2000, global cheese is worth only what it becomes: kings (before r1201),
  cat damage, defence, delivered raw. Spending it costs no score.
- Raw cheese carried at the end counts for nothing. With go-home thresholds up to 105 (`MinerStrategy.java:186`),
  a 16-rat army can hold a few hundred raw at r2000 (A: 16 × 20–40).
- The champion's unlimited mode (`global ≥ 3000 − 0.75t`) builds rats at c(n) up to 370 each (144 rats on `mining`,
  strategy.md §5.2); under saturation they buy cat damage and defence only.

## 3. Dominant macro (policy)

1. **Measure.** Kings keep moving averages of income Î (transferred plus own pickups per round) and deaths per
   rat-round δ̂ (spawns minus the change in n), shared through the array.
2. **Reserve.** R = 2k·H plus a surge (≈ 200–450) while enemies are near a king. Spend below R only to defend a king
   under attack.
3. **Army.** Spawn while n < n*(Î − 2k, δ̂), or while global − c(n) > R plus the price of the next planned king.
   Floor of 8–12 rats; no fixed ceiling. Surplus above R with n ≥ n* goes to kings (rules 4–5), then traps (§2.9),
   then rats above n*.
4. **Second king, early and exact.** When global ≥ R + 300 and no enemy is near the site: recruit exactly 7 rats
   (spawn payload, architecture.md §4.1), send them to fixed slots around the site, and form. Site: the mine that,
   with the first king's mine, minimises the side's delivery distance (§2.6), ≥ ~10 tiles from the first king, off
   cat paths. The earliest worthwhile round comes from a dose-response test (§5 E2).
5. **Lock-in before r1201.** In every game still running at r≈900–1150 with fewer than 5 kings: form one king at a
   time until 5, or until the army reaches its defence floor, at sites near existing kings and away from the front.
   After r1200 with one king: form a second at once.
6. **Miners by deficit.** Each new rat gets a mine with a miner deficit (target per mine from §2.6), own and
   contested mines first. Kings sit on mines. Exploration stops once the symmetry is known and every mine is seen.
   Loads ≥ 40 on safe routes.
7. **Endgame.** From ~r1950 deliver all raw cheese; keep turning global into cat damage and defence (details: T3).

## 4. Where the champion and the top bots deviate

### 4.1 Champion (`src/challenger` = awubot)

| # | deviation | code | consequence |
|---|---|---|---|
| C1 | Fixed cap: 16 rats (12 on area ≤ 900 before r400), `global/10 + 10` when poor, 32 when global ≥ 2000 − t/2 (t ≥ 100), unlimited when ≥ 3000 − 0.75t | `robot/king/KingSpawningStrategy.java:23-32` | over-builds under fast attrition, under-builds on low-contact maps, idles 700–1850 cheese (§2.5, §2.8) |
| C2 | No reserve: spawns at cost 10 down to 0 cheese | `KingSpawningStrategy.java:69` | starvation after an income collapse (§2.8) |
| C3 | Second king gated on rats it rarely has: numRats ≥ 20·kings (area > 1200) or 20·kings − 4, ≥ 5 known mines, a mine ≥ max(20, √(area/5)) tiles from every king and outside the centre box, global ≥ 150 | `robot/king/KingFormationCoordinatorStrategy.java:13,41,188-204,232-233,241` | 44% of official-pool team-games, median first formation r300, while 29% of games are over by r300 (§2.7). At the 16 cap the rat gate is met only on maps of area ≤ 1200 (numRats = 4⌊n/4⌋ = 16), and there only after r400 if area ≤ 900; elsewhere only once the rich line lifts the cap to 32 |
| C4 | Recruitment: every rat within 25 tiles of the site joins; the rat-side 120-turn timeout re-arms the next tick; the coordinator's cat abort is dead code | `robot/rat/FormationStrategy.java:19-21,29-33,109-120`; `KingFormationCoordinatorStrategy.java:210,329-334` | 9–20 rats leave mining per attempt and attempts fail repeatedly (0 of 17 on cheesefarm and peaceinourtime, strategy.md §3, §6.3) |
| C5 | No third king in practice: needs ≥ 9 known mines and numRats ≥ 40 (36 on area ≤ 1200); aborts at t ≥ 1100 with ≥ 2 kings | `KingFormationCoordinatorStrategy.java:13,145,195-201` | 3 kings alive in 2 of 306 mirror team-games; never more than 2 at the end of a points game; no lock-in before r1201 (§2.7) |
| C6 | Reactive backup only: "danger" formation when hp < 100 with one king | `KingFormationCoordinatorStrategy.java:183` | too late: gathering 7 rats takes tens of rounds; a king below 100 HP under attack dies in a few |
| C7 | Miner targets: 20% of trips explore for the whole game; mine weight ⌊√d⌋ favours far mines; no per-mine miner count | `robot/rat/MinerStrategy.java:70,84-111` | more miners per cheese, and lone miners spread onto enemy mines (§2.6) |
| C8 | Short trips carry little: go home at 10 raw within d² 25, 25 within d² 100 | `MinerStrategy.java:181-186` | 1.5–3.5 miners per near mine instead of about 1 (§2.6) |
| C9 | King site: walks toward the centre box edge until a mine is known, then to the nearest unassigned mine; only the first 10 known mines are scanned | `robot/king/KingMiningStrategy.java.jinja2:95-96,227-229,253-278` | no 1-median choice; the centre walk exposes the king to cats (strategy.md §8, peaceinourtime) |
| C10 | Strengths to keep: kings mine (17% of income), traps fire 98% | `KingMiningStrategy.java.jinja2:109-171`; `robot/rat/combat/moves/TryTrap.java.jinja2:66` | do not remove |

### 4.2 Top bots

- **OBG** (`src/opp_obg/RatKing.java`). Base cap cost ≤ 40 (16 rats), 30 with ≤ 4 known mines (`:82-87`), and only
  while global > 5·cost; above the cap it needs global − 2100 + round ≥ 5·cost (`:20,322-329`), so it floats more
  than the champion. The king parks on the known mine closest to the centre (`:89-149`). Second king: round ≥ 350,
  cost > 40, known mines > 4·kings (halved once the symmetry is known), global ≥ 1000 (or cost > 50 with global >
  500); site = the mine nearest the centre at d² ≥ 300 from every base (`:24-26,151-161,163-221`). Deviations: a
  constant base cap, a second king after 29% of games are over, a central king that buys territory with king risk.
  Its edge over awubot is fewer early deaths (§2.4), then the snowball.
- **GST** (`src/opp_gst/RatKing.java:65,83-104`, `RatKingBehaviors/RKHeadquarter.java:45-50,144-152`). Base cap
  cost ≤ 40; unlimited spawning whenever all cheese ≥ 2000 after r100, with no time decay; formation when rat cost >
  72 (≥ 28 rats); at most 2 kings (3 above 2500 tiles); kings go to the closest safe mine; traps on the enemy-facing
  side of mines (42% of income into traps). Deviations: a permanent float of ≥ 2000, formation gated on a large
  army, 2–3 kings.
- **Others** (docs/opponents.md §3): cerr caps by area and known mines and forms a panic king below 200 HP; goobie
  forms on the income trend; SPAARK budgets from a 100-round average of rat cost; powerpuff forms a king on the spot
  from a rat carrying ≥ 200 raw. None sizes the army from income and attrition; none locks in kings before r1201.

### 4.3 The setup hypotheses, judged

| hypothesis | verdict |
|---|---|
| A spawn budget that scales with cheese and map size | Partly. The budget follows income and attrition (§2.5). Map size is a weak proxy; mine count and delivery distance (§2.6) are the right map inputs. Global cheese is the wrong signal: it is high both when rich and when a cap blocks spending. |
| A mine per new rat, the king parked on the most central mine | Assignment: yes, by deficit (§2.6). Most central mine: only as the 1-median of own plus contested mines, with a safety margin; where the kings start close (knifefight path distance 5, thunderdome 8, M) the centre is the front line. |
| A second king from about round 350, with a fixed share of rats | Fixed share (exactly 7 recruits): yes. Round 350: too late; 29% of official-pool games are over by r300 and the king is cheapest early (§2.7). |
| Thrown rats squeak their flight line | T2; no economic content. |
| A backup king when the main king's HP is low | Proactive beats reactive: a second king formed before the decisive fight is the backup; a reactive one needs tens of rounds that a dying king does not have (C6). |

## 5. Work packages

Effects are win-rate changes against awubot (both seats), estimated from the share of games each package can change
(§2.7); they are ranges to test, not results. Protocol for every package: `tools/games` with paired seats, the
sequential test against awubot on the quick pool for the inner loop and on the tune pool to accept, then the
gauntlet (`opp_obg` and `opp_gst` first). Per-game diagnostics on flipped games: `Timeline.java` (income, rats,
kings, formations, deaths, traps per 10 rounds).

### E1. King lock-in before round 1201

- **Hypothesis.** Forming kings 3–5 between r≈900 and r1200 in games still running wins the king-tie points games
  and makes a late king kill need 3–5 kills.
- **Change.** `KingFormationCoordinatorStrategy`: in [T_lock, 1195] with kings < 5 and no enemy seen near the site
  recently, skip `MINE_THRESHOLDS` and the 20·kings rat gate (keep a defence floor of ≈ 12 rats); move the t ≥ 1100
  abort to 1195; site = a passable 3×3 whose centre is ≥ 3 tiles from an existing king's centre, on the side away
  from the enemy, off cat paths; one formation at a time; recruit only the 7–8 nearest rats. After r1200 with one
  king, form a second at once.
- **Effect.** Points games are 26% of official-pool games; 5 of 8 were 2–2 ties, 3 were 2–1 (§2.7). Winning the
  ties that now split 50/50 is worth up to +8%; with failed formations and lost kings, +4–8%.
- **Test.** Arms: T_lock ∈ {900, 1050}, cap 3 vs 5 kings, and the champion as the zero arm. Per game: kings alive at
  r2000, formation rounds, kill losses in r1000–1300 (must not rise).
- **Risks.** 14–21 rats consumed around r1100 weaken defence; kings side by side share a cat path; 2–6/round more
  upkeep (affordable: points games transferred 8–26k per side, M).
- **Size.** Small: 50–120 lines (coordinator gates and site choice; formation code reused).

### E2. Early, reliable second king at the 2-median mine

- **Hypothesis.** A second king formed before the decisive fights, at the mine that best pairs with the first
  king's, wins king races, shortens delivery on 29 of 43 maps and secures the points term.
- **Change.** (a) The next 7 spawns are recruits, flagged through the unused 13-bit spawn payload
  (architecture.md §4.1). (b) Site = the side's 2-median mine (§2.6), ≥ 10 tiles from king 1, not in a cat's
  last-seen cone. (c) Recruits take fixed slots of the 3×3; the centre forms when 6 allies stand in the ring.
  (d) Abort only when an enemy or a cat reaches the site. (e) Gate: global ≥ R + 300. Replaces the gates of C3 and
  the recruitment of C4.
- **Effect.** (1) Races: 15% of kill games ended with the winner's king below 300 HP; turning half of those we lose:
  +2–4%. (2) Points: a second king in every long game instead of 44% of team-games: +1–3%. (3) Delivery: 3–8 rats
  freed on spread maps: +0–3%. Total +3–8%, with a loss possible if recruiting during first contact weakens the
  opening.
- **Test.** Dose-response on the earliest formation round ∈ {40, 100, 200}, with the champion's gate as the zero
  arm. Per game: formation round, second king alive at r600 and r2000, income share r200–600.
- **Risks.** A second king near the front hands the enemy 600 HP for 300 cheese. Recruiting drains the opening. On
  M ≤ 4 maps the upkeep eats a large part of the side's supply (§2.7).
- **Size.** Medium: 300–500 lines (payload roles, site choice, a new formation strategy; `FormationStrategy`
  retires).

### E3. Spawn budget from income and attrition, with an explicit reserve

- **Hypothesis.** Replacing the fixed caps and cheese lines with n*(Î, δ̂, k) above a reserve R stops the bank drain
  under fast attrition, spends idle cheese on low-contact maps, and keeps upkeep safe.
- **Change.** `KingSpawningStrategy`: moving averages of income and deaths in the shared array (slot 15 or spare
  bits, architecture.md §4.1); the spawn rule of §3.3; the panic and cat bumps stay on top of n*; starvation floor
  2k·H.
- **Effect.** §2.5 puts many maps away from the cap's sweet spot: +2–5%, mostly on attrition maps (DefaultMedium,
  evileye, arrows, cheesefarm) and low-contact maps (minimaze, averystrangespace, wallsofparadis, ZeroDay).
- **Test.** Arms: the champion's rule (zero), n* alone, n* with reserve. Quick pool (attrition-heavy) and tune pool.
  Per game: deaths per rat-round, income share; global cheese at r200 and r500 needs a probe build. OBG and GST beat
  awubot on economy, so a regression against them is a warning.
- **Risks.** δ̂ is noisy early and depends on n; on small maps more rats lost games for food (intel.md §4).
- **Size.** Small: 80–150 lines.

### E4. Miners by deficit; exploration that ends

- **Hypothesis.** Assigning mines by deficit and stopping exploration once the map is known frees 2–4 rats for
  territory at the same cost.
- **Change.** Target miners per mine from §2.6 (d to the nearest king, planned load); the king writes each new rat's
  mine into the spawn payload; `MinerStrategy` explores only while the symmetry is unknown or a mine is unseen.
- **Effect.** +1–3%. **Test.** Zero arm, assignment, assignment plus exploration stop; per game: income share
  r200–600. **Risks.** Assignments to contested mines send lone rats into fights; exploration also finds enemies and
  cats. **Size.** Medium: 150–250 lines.

### E5. Larger loads on short, safe trips

- **Hypothesis.** Raising the short-range go-home thresholds (10 → 25–40 within d² 25, 25 → 40 within d² 100) cuts
  trips per cheese 2–3× on near mines (§2.6) at little speed cost.
- **Effect.** +0–2%. **Risks.** Raw cheese slows bites (action cooldown × (1 + 0.01·raw)) and drops on death.
  **Size.** Tiny: constants at `MinerStrategy.java:181-186`.

### E6. King site = 1-median; no walk to the centre

- **Hypothesis.** Parking the first king at the 1-median of own plus contested mines, with a margin from the enemy
  king's path, raises the king's own pickup share and shortens trips; dropping the centre walk removes a cat risk.
- **Effect.** +0–2%. **Size.** Small: 60–120 lines in `KingMiningStrategy.java.jinja2`. Shares its site code with E2.

### E7. Surplus into traps (with T2)

- **Hypothesis.** With the army at n* and global above R, more traps at mine approaches turn idle cheese into damage
  at 2.5 per cheese (98% fire rate, §2.9). **Effect.** Unknown; placement is T2's. **Size.** Small.

Order by expected effect per line: E1, E3, E2 (E2 has the largest ceiling and the largest risk; E1 and E3 are cheap
and independent). E4–E6 follow; E6 merges into E2's site code.

## 6. Not settled

- δ as a function of army size and opponent, needed to turn n* from a budget into a target (T2).
- τ, the per-trip overhead in §2.6 (assumed 5 rounds); not measured.
- The fraction of its own mine's 2.26/round a parked king collects: measured only as 17% of team income overall
  (≈ 1.4/round at the mirror's 8.2/round, A), not per king.
- The float and starvation numbers rest on strategy.md's probe (§8, §10.1); replays hold global cheese
  (`teamAliveRatKings` = kings + 10 × global, KB's `Bank.java`), so they can be re-measured from any run.
- The replays are engine 1.2.5. On 1.2.6 the spawn box is centred and mirror-correct (rules §0): this moves where a
  parked king's pickups land, not the supply.
- Whether an opening king (r≈10–40) or a later one (r100–200) is better depends on how much the first contact
  decides (T2, T4); only E2's dose-response can settle it.
