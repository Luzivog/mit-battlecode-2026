# Dominant strategy

This is the synthesis of track T (docs/program.md). It merges the five lens files (`docs/theory/t1-economy.md`,
`t2-combat.md`, `t3-scoring.md`, `t4-maps.md`, `t5-opponents.md`) into one game plan. It then says where the
champion (`src/awubot`, identical to `src/challenger`) and the top public bots fall short of that plan, and records
how the lenses' disagreements were settled. The work packages and the order to 90% are in `docs/plan.md`.

Source keys:
- `R§n`: docs/rules.md.
- `E:path:line`: engine tag `engine.1.2.6`, under `engine/src/main/battlecode/` (clone:
  `~/battlecode26-data/engine/ref/battlecode26`).
- `C:path:line`: `src/challenger/`. Generated classes are cited by their `.jinja2` template line.
- `O:` and `G:`: `src/opp_obg/` (Old But Gold) and `src/opp_gst/` (GST).
- `T1§n` … `T5§n`: the lens files.
- `strategy§`, `combat§`, `arch§`, `opp§`, `intel§`, `testing§`: the docs of those names.
- `S:file`: synthesis scratch in `~/battlecode26-data/theory/synthesis/` (§11).
- `A`: arithmetic shown inline.

## 0. The plan in one screen

1. **Never lose a king to yourself.** No `Error` may escape `run()`, a king never starves, and no king stands in a
   cat's lane. Each of these kills awubot's kings today (§1, §6 G1–G3).
2. **Win the exchange at Chebyshev distance 2.** Lay a trap on the tile between you and the enemy. Grab when it is
   legal and safe, hold the victim while biting with the free action, and throw only for value: into a cat, the enemy
   king, a second enemy, or a wall at step 2. Bite with the minimum cheese that kills or flips HP order. Commit only
   with 2 on 1 (§4.2).
3. **Turn the exchange into territory, and territory into the enemy's starvation.** Income is a share of a fixed
   supply. Assign miners by deficit to our own and the contested mines, and spend everything above an upkeep
   reserve (§4.1).
4. **More kings, earlier and safe.** Form a second king early (how early is open, §9). Have at least 3 kings before
   round 1201, refill to 2 after any loss, and park kings off cat lanes on home-side mines (§4.3).
5. **Convert at the end.** Deliver all raw cheese, put spare cheese into cat damage, and never hoard. In cooperation,
   only the side that is ahead kills the last cat (§4.5).
6. **Play the map's clock.** On each map class, the clock that runs out first (contact, cat, starvation or points)
   decides which of 1–5 matters most (§5).

## 1. What decides games now

### 1.1 The champion mirror on `goal`, per cell (engine 1.2.6)

Data: plan.md W0.3, run `1118-222544-ab-6dea` (docs/evaluation.md "W0.3 data"): main (= awubot) against awubot on
`goal` (the 43 official and 52 visible tournament maps), 3 seed passes, 285 cells, 570 games. Every cell splits. Each
cell's losing seat is classified by the rules of §1.1b, with one more class (S:w03_classify.py → S:w03_cells.tsv):
- **SELF**: a losing king removed by its own code (an id in `Round.diedIds` with no `DieAction`,
  E:world/GameWorld.java:356-359, :1168-1171), within 5 rounds of the end.
- The HP split comes from T4's ReplayStats, starvation from its KingTrace, as in §1.1b.

| class | cells | share | maps | median end round | the losing kings' HP lost (median): bites / scratches / starvation / throws and rest |
|---|---|---|---|---|---|
| COMBAT | 96 | 34% | 49 | 259 | 315 / 0 / 0 / 248 |
| POINTS | 72 | 25% | 33 | 2000 (62 reach r1000; 10 cooperation endings before r1000) | – |
| STARVE | 62 | 22% | 35 | 598 | 30 / 0 / 480 / 50 |
| CAT | 33 | 12% | 19 | 399 | 10 / 540 / 0 / 26 |
| COMBAT+starve | 19 | 7% | – | 361 | 170 / 20 / 190 / 238 |
| SELF | 3 | 1% | 1 (sieveofriches, every seed) | – | – |

- **Official and tournament maps differ.** Official 129 cells: COMBAT 41%, STARVE 21%, POINTS 21%, CAT 9%,
  COMBAT+starve 8%. Tournament 156 cells: POINTS 29%, COMBAT 28%, STARVE 22%, CAT 13%, COMBAT+starve 6%, SELF 2%.
- **By mine count:**
  - ≤ 4 mines, 75 cells: COMBAT 31, STARVE 25, COMBAT+starve 8, POINTS 6, CAT 5.
  - 5–7 mines, 57 cells: COMBAT 28, STARVE 12, POINTS 9, COMBAT+starve 5, CAT 3.
  - ≥ 8 mines, 153 cells: POINTS 57, COMBAT 37, CAT 25, STARVE 25, COMBAT+starve 6, SELF 3.
- **Classes move with the seed and the engine.** 55 of the 95 maps keep one class in all 3 cells. 20 of the 43
  official pass-0 cells keep their 1.2.5 class (§1.1b). Every count above is per cell, not per map.
- **Points games are less close than on the official 1.2.5 maps.**
  - Kings at the end, winner against loser: 2v2 in 35 cells, 2v1 in 23.
  - Margin: median 15 points; 15% within 2.
  - With the losers' final totals, one more king flips 29 of the 62 points cells that reach r1000, and two more flip
    40 (A).
- **Crashes.** Robots destroyed by their own uncaught exception: 0.64 per game, in 94 of 285 cells (cells.tsv).

### 1.1b The official maps on 1.2.5 (the lenses' data)

Data: challenger against awubot on the 43 official maps, both seats, the map's own seed, **engine 1.2.5 jar**
(`match_results/abtest_2026-11-18_19-30-32.json` records `engine_version 1.2.5`). Every map splits 1–1 by seat
(testing§4). That follows from identical code in a deterministic engine: swapping seats replays the same game. It says
nothing about how close the games are (§2.6). Endings: 32 king kills and 11 points games (9 reach r2000, 2 end by cat death in cooperation;
T3§3.1, T4§2.1).

Classification (S:losstable.py, from the losing king's HP split by source in T4§2.1):
- **STARVE**: starvation is ≥ 400 HP or ≥ 40% of the losing king's HP.
- **CAT**: scratches are ≥ 300 HP or ≥ 40%.
- **COMBAT+starve**: starvation is ≥ 200 HP or ≥ 25%.
- **COMBAT**: everything else.

| class | maps | count |
|---|---|---|
| COMBAT | knifefight, thunderdome, averyfineline, DefaultSmall, canyoudig, tiny, starvation, Nofreecheese, DefaultLarge, streetsofnewyork, trapped, whatsthecatdoin, jail, cheesefarm, Meow | 15 |
| COMBAT+starve | evileye, EscapeTheNight, hatefullattice, cheeseguardians, mercifullattice, safelycontained | 6 |
| STARVE | 5t4rv4t10n_1337, whereisthecheese, DefaultMedium, popthecork, corridorofdoomanddespair, arrows, closeup, toomuchcheese | 8 |
| CAT | dirtfulcat, dirtpassageway, keepout | 3 |
| POINTS | TheHeist, peaceinourtime (cooperation endings); ZeroDay, averystrangespace, cheesebottles, minimaze, pipes, rift, sittingducks, uneruesansfin, wallsofparadis (r2000) | 11 |

Over the 32 kills, the losing kings lost 31% of their HP to bites, 31% to throws and the residual, 26% to
starvation and 12% to scratches (T4§2.1). Of the 11 points games, 8 ended 2 kings against 2. The margin was under
2 points in 6 of them and under 1 point in 3 (T3§3.2). Mine counts: 14 maps have ≤ 4 mines, 8 have 5–7, and 21 have
≥ 8 (S:losstable.txt).

### 1.2 Against OBG and GST

OBG won 9 of 14 games against awubot and GST 6 of 14 (opp§1).
- **Their wins** are on maps with ≥ 8 mines, by king kill after a starvation spiral. In those games they out-trap
  awubot 1.2–2.7×, hold more mines, and awubot loses more rats (T5§4.2, §5.2).
- **Their losses** are on maps with ≤ 4 mines, where awubot's early swarm reaches their king. OBG's king sits on the
  central mine; GST's king starved in 7 of its 7 king-kill losses (T5§4.4, §5.2).
- **On cat-lane maps** OBG won 15 of 20, and awubot's king was scratched to death in 7 of them (T4§2.1).

### 1.3 Tournament losses of the champion

- **Cooperation endings**: groveisbeingdesecrated at r160, ended by awubot itself while behind on cheese, and
  t_minus_five_minutes at r362, after a cat pounced across the wall into Goobie's half (T3§3.4).
- **A king parked in a cat lane**: tminusten, lost 3 times at r103–104 (T5 A12, T4§2.4); attack_on_cat (T4§3).
- **sieveofriches**: an economy ceiling (cheese 30k against 71–91k), plus every king destroyed once it knows
  26 mines (T5 A15).
- **Rushes** in Sprint 1 (intel§2).

### 1.4 Engine version

Every replay used by the lenses ran engine 1.2.5. 1.2.6, the official engine, differs only in where cheese lands
and in mirroring it correctly on H and V maps (R§0). §10 lists which numbers depend on that.

## 2. Principles

### 2.1 The game ends on kings

- A team loses only when its last king dies (R§1). That is 74% of official-pool games and 79% of all unique mirror
  games (T1§2.7).
- Kings are also 50% of the score after a backstab (R§1).
- King HP never heals: every `addHealth` call in `world/` is negative, and only formation sets HP (T2§1.3 L5). A new
  king is therefore the only way to add HP that the enemy must remove to win.
- Price: 50 cheese, 7 babies and 2 cheese of upkeep per round (R§6, T1§2.2).

### 2.2 Income is a share of a fixed pie

- Each mine yields 20/8.862 = **2.257 cheese per round** whoever harvests it (A, T1§2.1). Cheese never decays.
- From r200 the two teams harvest 86–93% of the map's supply (T1§2.3, T5§2.1). So income is share × supply, and
  extra miners on saturated mines add nothing.
- **The share follows the exchange.** Against awubot, OBG has the same income and army in r10–200 with 26% fewer
  deaths. Its share then grows to 58–75% (T1§2.4).
- The side that loses share and exchange runs out of global cheese, and its king starves while being bitten. This
  happened in 9 of awubot's 13 king-kill losses (T5§2.2).

### 2.3 The exchange

- **HP order is permanent** (no healing). A grab is legal from the front only against a rat with strictly less HP,
  and from any of its 5 blind tiles regardless of HP (R§8, T2§1.3).
- **Bite races.** At equal damage the first biter wins a frontal race by one bite, and no grab ever becomes legal. A
  +1 damage edge beats the initiative: 13 against awubot's 12 wins from either seat (T2§2.1).
- **Traps.** 20 cheese buys 50 damage plus 3 turns without movement. Traps fire on an enemy 93–98% of the time,
  because enemy traps are invisible (R§10, T2§2.4, T5§2.3). That is about 2.4 HP per cheese, outside the rat-cost
  curve.
- **Grabs.** A grab is worth about 100 HP-equivalents more than a bite exchange, and holding the victim while biting
  others adds about 80 more (T2§2.2).
- **Throws.** A collision deals at most 42, to both robots (R§9). A throw into a cat kills the thrown rat (R§9).
- **Numbers.** Local numbers are super-linear: two attackers make a rat grabbable within a round, by pincer (L3) or
  bite-then-grab (L4) (T2§1.3, §2.6).
- **Leaving contact.** A retreat that keeps facing the enemy strafes 5 tiles in 9 rounds against the pursuer's 9, so
  leaving contact is expensive (T2§3).

### 2.4 The four clocks (T4§2.2)

| clock | when it runs out | source |
|---|---|---|
| contact | first sight ≈ 2 + (kk − 4.5)/2 rounds, where kk is the rat path between the kings; the backstab follows a median 2 rounds later | T4§2.1–2.2 |
| cat | when a cat's lane first reaches a king; a blocked cat scratches the king 20 HP per 3 rounds and never counts as stuck, so a king that stays dies in about 90 rounds | E:world/InternalRobot.java:1251-1294; T4§2.2 |
| starvation | bank ÷ (spending + 2K − income); a starving 600-HP king dies 60 rounds later | R§11; T4§2.2 |
| points | round 2000, or the last cat's death in cooperation | R§1 |

### 2.5 Points

- D = Σ w_i·(m_i − t_i)/(m_i + t_i). Truncation never creates a tie (T3§1.1–1.2).
- One more king is worth +10 at 3v2 and +16.7 at 2v1 after a backstab (T3§1.3).
- Late in a game heading to r2000, one bite on a live cat is worth about 100× a rat-round of mining (T3§1.4).
- The tiebreaks are nearly worthless (T3§1.5).
- Kings at r2000 ≤ max(kings formed by r1200 that survive, 2) (R§6, T3§1.3).

### 2.6 Close games, and what 90% needs

**Most games are close.** Measured (plan.md W0.1, docs/evaluation.md "Calibration", 1.2.6):
- moving only the kings' random stream flips 3.2% of cells;
- adding about 235 bytecodes a rat turn, with no logic change, flips 46.7% (60 won both, 73 lost both, net −4.6%,
  p = 0.30);
- about 150 a turn for every robot flips 39.2% (net +4.2%).

A cell flips when the same team wins both seats, so a game whose result does not depend on the noise cannot flip it.
If a share q of games become fair coins under the perturbation and the rest keep their result, the flip rate is
0.5·(1 − (1 − q)²) (A), so q ≈ 0.54–0.74. Most mirror games are decided by noise-sized differences. Earlier signs of
the same thing: adding prints flipped the cheesefarm mirror from a r2000 win to a r526 loss (strategy§8), and a
heavier probe lost DefaultLarge at r456 where a lighter one won at r2000 (arch§8). No net bytecode cost is measurable
(−4.6% and +4.2%, both within noise).

**Small consistent edges should therefore move games.** A game holds about 100 throws, 60–130 trap triggers and about
110 bites per side (T2§2.3–2.4: 2,445 awubot throws in 24 games; 755–1,541 traps and 1,267–1,409 bites per 12 games).
Model (A): treat the N ≈ 300 engagements as independent, each won with probability 0.5 + e. We are ahead in the total
with probability Φ(2e√N); for e = 0.05, Φ(1.73) = 0.96. The model's independence is unmeasured. The one measured
lever, a rat cap 4 lower, moved whole map groups: 0.86 on ≤ 4-mine maps, 0.33 on ≥ 8 (plan.md §4.1 E2).

**But 90% needs games that are no longer close.** The goal score is Σ w_c·s_c over the loss classes of §1.1 (plan.md
§5.1). With every class at s = 0.5 today, 90% needs s ≈ 0.9 in every class: about +382 Elo. Close games lost to noise
are a ceiling on any bot that is only slightly better. So the dominant plan stacks levers that change many engagements
per game in the same direction (army size, kings, the spend gate, cat safety, traps), not single-engagement tweaks.

## 3. The game plan by phase

| phase | rounds | kings | rats | economy and points |
|---|---|---|---|---|
| opening | 1 – first contact (r4–25; T3§3.1) | spawn 1 rat per round toward the centre (C:KingSpawningStrategy.java:76-97). Where kk_dirt > 20 (no contact before ≈ r10), test budding a second king from the first 7 spawns (150 cheese; §4.3) | first rat to the centre at r2, ray scouting until r20 (strategy§1). Learn mines, symmetry and cat lanes (§4.6) | only 38% of the r0–100 supply is harvested (T1§2.3); the opening is won by fighting, not by mining speed |
| first contact | contact clock | stay home; no rush (T4§2.6) | hold at Chebyshev 2, trap the gap, grab before biting (§4.2) | the backstab comes at first contact against awubot (T3§3.1) |
| midgame | ≈ 40–900 | second king by r≈200 at the latest (KB arms); park on home-side mines off cat lanes; step out of lanes before the cat arrives | exchange first, then territory by deficit; lone rats never go to enemy-side mines | army n* from income and attrition above the reserve; surplus goes to kings, then traps, then rats (§4.1) |
| lock-in | ≈ 800–1195 | form kings 3 (and 4) while the game is still running: the existing coordinator with its gates relaxed first, budding only if those formations fail (§4.3) | fill the army back to n* | every r2000 mirror seat at r1100 holds 786–2122 global; 9 of the 18 have 1 king, so a third king needs two formations (T3§3.5) |
| late | 1201–1800 | 1 king left: form a second at once (R§6). Do not trade kings: losing one at 2v2 costs 16.7 | hunt cats on stalemate maps from ≈ r1300–1600 (PT arms) | – |
| endgame | ≈ 1800–2000 | turtle when ahead; strike a non-last enemy king when behind (+16.7) | deliver all raw before r2000, distance-aware | spend spare global on cat damage (1–2 cheese bites), never on hoarding (T3§4.4) |
| cooperation (any time) | while `isCooperation()` | build a king lead early on maps likely to end in cooperation | take cat HP from cats the enemy would kill | only the side ahead kills the last cat; bank cheese before the clock runs out (T3§4.2) |

## 4. Rules by domain

### 4.1 Economy

1. **Upkeep reserve on every global spend.** Spend global only above R = max(100, 2K·H), with H ≈ 30–120 rounds
   (T1§2.8, T5 P5). The gate covers spawns, traps whose cost raw cheese does not cover, digs, bite cheese and formation.
   - **What drains the bank** (`critic/Starve.java` on the 62 STARVE cells of §1.1, 1.2.6; S:w03_starve.txt). From r1
     to starvation, the losing seat spent a median of:
     - 2,180 on spawns (52% of its spending);
     - up to 900 on rat traps (≤ 22%: traps pay from raw cheese first);
     - 820 on upkeep (18%);
     - 178 on digs (4%).

     Over all 62 cells, the `cost == 10` exception spent 1,440 cheese. Today's spawn floor is already 100
     (C:KingSpawningStrategy.java:69), but rats spend global with no floor: traps (C:TryTrap.java.jinja2:64-76), digs
     (C:BabyRat.java:222) and king digs.
   - **What a reserve can flip.** Where both kings starved, the one that starved more lost on 4 of 5 maps (T4§2.2). The
     winner also starved in 12 of the 62 STARVE cells (19%). In the rest the loser starved because its income
     collapsed after it lost the exchange and the share (T1§2.8, §2.2). A reserve only delays that; CT, CW, E2 and E3
     address the cause.
   - **Keep the `cost == 10` exception.** It spent 1,440 cheese across 62 starvation losses (about 23 each), and it is
     the only way to rebuild income with ≤ 3 rats (T1§2.8).
   - **No siege waiver below 100.** Starving kings are also being bitten (T4§2.1: bites on 6 of the 8), so a waiver
     would spend the last reserve exactly there.
   - T4's exact floor, max(0, 2K − I)·(2000 − t), is unusable early. With I ≈ 0 it asks for 4000 cheese against a
     2500 bank (A).
2. **Army.** Size it to n* ≈ √((I − 2K)/(2.5δ)), with I the income, δ the deaths per rat-round and a floor of 8–12
   rats; no fixed ceiling (T1§2.5).
   - The champion's cap of 16 fits an even contested mirror. It over-builds under fast attrition (the bank drains)
     and under-builds on low-contact maps, where n* is 20–44 (T1§2.5).
   - **Measured** (plan.md §4.1 E2, `quick`, 1.2.6): a rat cap 4 lower scored 0.86 on maps with ≤ 4 mines, 0.65 on
     5–7 and 0.33 on ≥ 8. The army follows the mine count, not the area: cheesefarm and DefaultSmall are both 900
     tiles, and cheesefarm (10 mines) scored 0.12 where DefaultSmall (4) scored 0.6.
   - δ must exclude rats consumed by king formation: those are a planned spend, not attrition. T1's δ counts them
     (§8 item 8).
3. **Order for surplus above R and n*:** kings before r1201, then traps (2.4 HP per cheese, T2§2.4), then rats above
   n* (T1§3).
4. **Miners by deficit.**
   - Miners needed per mine: n_mine = 2.257·(2d/L + 0.01·d + τ/L), with d the path distance to the nearest king, L
     the load per trip and τ the per-trip overhead (T1§2.6).
   - Assign over our own and the contested mines. Never send lone rats to enemy-side mines: awubot's ⌊√d⌋ lottery puts
     71% of its weight there on keepout (T5 A6).
   - Stop exploring once the symmetry and our half's mines are known (T1 C7).
5. **Loads.** Forward moves and bites cost nothing extra up to 9 raw (R§4). Carry 25–40 on short safe trips
   (T1§2.6). T5 A7 found large loads are not awubot's problem, so this is a minor term.
6. **Dirt.** Each dig costs 5 cheese and 25 action cooldown (R§8). Dig only the path you need, and keep ring dirt as
   armour (T4§2.5).

### 4.2 Combat: the local policy (T2§6, with T5§8.2 and §8.4)

Per rat turn, in priority order:

1. **Invariant: end the turn not grabbable.**
   - Not orthogonally adjacent to a ready enemy (L1).
   - Not inside a pincer (L3), and not where bite-then-grab works (L4).
   - Not on an enemy carrier's step-2 tiles.
   - Not inside a cat's scratch cone or pounce reach.
   - Not on tiles where awubot predictably trapped: it traps when our rat is "strong" (100 HP, or ≥ 80 HP and ≥ its
     HP + 12) and faces it at Chebyshev 2 (C:TryTrap.java.jinja2:64-90; T5 P4).
2. **Carrying an enemy.**
   - Throw if the best line ends in a cat, the enemy king, an enemy rat at step 2 or a wall/dirt at step 2.
   - Also throw at carry turn ≥ 8, or when the carrier is threatened.
   - Otherwise hold: bite or trap with the action (carrying does not block actions,
     E:world/RobotControllerImpl.java:717-727) and move toward the best line, ranked cat > king > enemy cluster > wall.
3. **Grab when it is legal and safe**: HP lead, blind side, a trapped or stunned target, or as the partner in a
   bite-then-grab.
4. **Enemy rat at Chebyshev 2, with spendable cheese:** trap the tile between. Every move it can make except a
   retreat enters the trap's 3×3. Overlap a second trap on its approach tile: 100 damage for 40 cheese (T2§2.4).
5. **Bite with minimal cheese.** Choose the first that applies:
   - kill now, at (h − 11)² + 1 cheese;
   - flip the HP order for my or a partner's grab next turn;
   - win the race: 13 against a 12-biter, 5 cheese;
   - else 12 for 2 cheese.

   Cats get 1–2 cheese per bite when points matter (§4.5).
6. **Move.** Hold at distance 2 on the diagonal, facing the enemy. Advance only with 2 on 1, or against a trapped,
   stunned or weaker target. Flank to build a pincer.

**Targeting:** grabbable now > killable now > trapped or stunned > most raw cheese (slow, and it drops loot) > lowest
HP. Near the enemy king: throws into it from Chebyshev 3, outside its bite ring (T2§4.1) > its ring tiles > finishing
bites with cheese.

**Keep from the champion** (T2§7): the grab-exposure model as a hard constraint, grab before bite, facing the target,
move-plus-act sequencing with hit-and-run secondaries, ally-sacrifice throws, and a search that fits the bytecode
budget.

### 4.3 Kings

1. **Count.**
   - Two early (KB; the best round is open, §9).
   - At least 3 by r1195 in every game still running: lock-in (T1 E1, T3 P1). The lock-in cannot reach the
     cooperation endings that come earlier (TheHeist r390, peaceinourtime r451; T3§3.1); those need the early king.
     On `goal` (§1.1), one more king would flip 29 of the 62 points cells that reach r1000, and two more would flip 40.
   - Refill to 2 after r1200 when down to 1 king (R§6).
   - The champion forms a king in 44% of official-pool team-games, first at a median r300. It had a third alive in 2
     of 306 mirror team-games, and stops trying at r1100 (T1§2.7, C:KingFormationCoordinatorStrategy.java:145).
2. **How to form.**
   - First, simplify: the existing coordinator with its gates relaxed (`MINE_THRESHOLDS`, the 20·K rat gate, the
     `req·K` abort, the r1100 abort and the far-mine score floor; C:KingFormationCoordinatorStrategy.java:13,141-151,
     181-204,241).
     - Its formations complete when the bot is rich or uncontested (strategy§6.3), which the r1100 points seats are
       (T3§3.5).
     - It recruits every rat within 25 tiles (strategy§3), which thins the army while it forms.
   - Second, only if those formations fail: budding. The king spawns 7 recruits next to a chosen 3×3 site, they step
     into fixed slots, and the centre recruit calls `becomeRatKing` once 6 allies stand around it (R§8).
   - **Geometry.** `buildRat` reaches only the 16-tile ring (dist² ≤ 8, R§7). A 3×3 site that does not overlap the
     king therefore contains at most 3 ring tiles, and the other recruits step 1–2 tiles (A).
   - **Cost.** 50 + Σ c(n..n+6): 150 at 0 babies, 290 at 8, 430 at 16, 570 at 24. Recruiting 7 existing rats costs
     160/300/440 at 8/16/24 rats, but empties the front while they walk (A, T1§2.2).
3. **Site.** Choose the home-side mine that minimises delivery to our own mines, with its 3×3 off known cat lanes, on
   our side of the contact line, and with a perpendicular escape route. Break ties toward the centre (T1§2.6, T4§3).
   - Not the most central mine: that is OBG's rule, and it loses on ≤ 4-mine maps (T5§4.4).
   - On 52 of 147 maps, half or more of the mines sit in a straight-line cat lane (T4§2.2).
4. **Safety.**
   - Never starve (§4.1).
   - Step **perpendicular** out of a cat's forward lane before it arrives. The cat moves twice as fast as a king, so
     fleeing along its line fails. Leaving a 2-wide lane takes ≤ 3 king moves (12 rounds), so the warning must come
     ≥ 7–8 tiles out, which needs rats to relay it (T4§2.3–2.4).
   - Keep enemy carriers off the 8 lines through the king, keep the ring dirt, and spawn on the far side (T4§2.4).
5. **The king's own bite.** Use cheese at kill or flip thresholds (T2§4.3). When the king cannot leave a lane, bite
   the cat (the champion's king never bites cats, C:KingCombatStrategy.java.jinja2:180-195).

### 4.4 Cats

- **Mechanics** (R§12):
  - In EXPLORE a cat never targets rats. It crushes what it walks onto and scratches what blocks it.
  - In ATTACK it targets the last robot in its chirality scan, of either team, kings included.
  - Cats pounce over walls (R§16.18).
- **Lanes are deterministic**: a BFS on walls between fixed waypoints, repeated every loop. Two sightings a few rounds
  apart give the local lane (T4§2.3). Rats relay each sighting to a king (squeak r² 16), but only when no cat is within
  r² 16 of themselves. In ATTACK a cat turns toward the oldest squeak it heard, from either team (R§12, §16.4), so a
  relay near our king can steer the cat there.
- **Dirt stocks start at 0.** A rat must dig (5 cheese each) before it can place dirt (R§8).
- **Defending a king that cannot leave** (the tminusten pocket, §8 item 2). Rats:
  - stall the cat with dirt, since two placers out-pace one digging cat (T4§2.5);
  - bite it from outside its 180° cone;
  - throw grabbed enemies into it, so the cat sleeps 2 turns (R§9).

  OBG dealt 7,500–8,000 cat damage by r100 on tminusten, and its king survived (T4§2.1).
- **As a weapon**: feed carried enemies to cats; a feed is a kill (T2§5). Luring cats onto the enemy king is unproven
  (plan.md XA).
- **As points**: §4.5.

### 4.5 Cooperation and points

- **Against awubot**, cooperation ends at first contact (median r20, T3§3.1).
  - The blame choice is small. The victim of a backstab may place cat traps for 100 rounds, and no public bot uses
    that window (T3§2.4).
  - A rat trap that fires during cooperation blames its owner (R§2): 10 of 41 mirror backstabs began this way.
- **Cooperation endings (the cat clock).**
  - Only the side that is ahead kills the last cat. While behind, stop damaging it and remove our own cat traps on
    its path (free, R§8).
  - Deliver cheese at low thresholds before the clock runs out.
  - Take HP from cats the enemy would kill: it counts twice.
  - Build a king lead early on maps likely to end in cooperation. 2v1 is +10 (T3§4.2).
  - The champion ended the grand-final grove game itself while behind (T3§3.4).
- **Backstab phase.**
  - Kings first (§4.3).
  - Cat HP late: 1–2 cheese per bite, and kings bite cats when no enemy is in range.
  - From ≈ r1800: distance-aware delivery of all raw cheese, and spare global spent on cat damage (T3§4.3–4.4).
- **Choosing the regime** (only against bots that do not backstab on contact): D_coop − D_backstab = 40·(c − k), with
  c our cat-damage share and k our king share (T3§2.3).

### 4.6 Information (T4§2.3)

- **What to learn, in order:** cat lanes, threats to the king, our own economy, mines, the enemy king. The enemy
  king's start is known up to 3 symmetry candidates from round 1, and awubot parks on the mine nearest its start
  (T5 A11).
- **Symmetry.** Walls never decide the symmetry on 14 of 150 maps; mines decide 10 of those. The champion compares
  passability only (C:CellTracker.java.jinja2:360-366) and never uses the symmetry for targeting.
- **The starvation signature.** A starving enemy king loses exactly 10 HP per round with no bites. That is when
  pressure pays (T4§2.6).

## 5. Per map class

Classes from T4§3 (`classes2.py`) and mine count (T5§7). A map can be in several classes.

| class | test | what decides it | dominant emphasis | packages (plan.md) |
|---|---|---|---|---|
| FACEOFF | rat path ≤ 12 | king kill by r68–189: throws and bites (knifefight 512 HP of throws) | keep the champion's close-quarters micro; minimal-cheese bites, trap the gap, throw shield at the king; reserve; no bud before contact | CB, CT, CC+CK, E1 |
| MINEPOOR | ≤ 4 mines (14 official; 75 of 285 `goal` cells) | the early swarm (31 COMBAT cells) and starvation (25 STARVE cells) | a smaller army: a rat cap 4 lower scored 0.86 here (§4.1); exchange; a reserve on every global spend where both sides starve; home king; OBG and GST lose these, so do not import a central king | E2, CT, CB, E1 |
| mines 5–7 | 8 official | split: exchange against king exposure | traps plus a home king | CT, E1 |
| mines ≥ 8 | 21 official | exchange, then share, then starvation (OBG and GST win 11 of 12) | traps, territory by deficit, army from income, lock-in | CT, E3, E2, KL |
| CHOKE | cut ≤ 4 | 7 of 11 mirror points games; starvation (whereisthecheese) | reserve; kings and conversion for points; trap or seal the choke when ahead | E1, KL, PT, CH |
| NOCONTACT | no rat path even through dirt | points under cooperation weights, or a king killed by a cat | cat-safe king; early king lead; take cat damage; last-cat rule | K1–K3, KB, PC |
| CATPATH | cat clock ≤ 320 (10 tournament maps, peaceinourtime) | awubot's parked king scratched to death (7 of 20 games against OBG) | lane memory, perpendicular escape, rats defend pockets; let the cat work on awubot's king | K1, K2, K3 |
| MINE-OVERFLOW | ≥ 26 mines (sieveofriches, tminusten; bridges, bridgesCatless and quadrants are team-made) | awubot's kings destroyed by its overflow `assert` | remove the assert; then the economy | S1 |
| DIRT | dirt ≥ 20% | digging is both the path and a cheese sink | dig only the needed path; ring dirt as armour | E1, CC+CK |
| OPEN | none of the above | every channel; keepout's cats (1000 HP), closeup and popthecork starvation | economy, reserve, cat-safe king | E1, E3, K1, KL |

## 6. Where the champion falls short

All `C:` paths are in `src/challenger/` (= awubot).

| # | gap | code | evidence | package |
|---|---|---|---|---|
| G1 | Its own code kills or blinds its robots: the ≥ 26-mine `assert(false)` destroys every king; `assert(rc.canThrowRat())` kills the carrier when a cat stands on the throw tile; `assert(performMoves(...))` in TryThrow and 4 other move classes; RobotTracker throws at ≥ 26 visible robots and asserts at ≥ 44; the top level catches only `Exception`; the panic squeak (`7<<29`, negative) and the king's empty enemy squeak decode as phantom enemies; the king's unguarded `rc.turn` costs it the rest of its turn; the spawn counter's write throws from the 1024th spawn | C:comms/GlobalArray.java:427-428, :108, :225; moves/TryThrow.java.jinja2:101,103,105,106 (cats absent from the grid :258-293); TryAttack:67,71, TryTrap:99,103, TryRatNap:82,95, TryPosition:39, TryFight:55; RobotTracker.java.jinja2:46-49,106,137; RobotPlayer.java:84; comms/Squeaker.java:91-105,215-217; comms/EnemyManager.java.jinja2:73-87; robot/king/KingSpawningStrategy.java:90; E:world/RobotControllerImpl.java:1312 | sieveofriches: kings removed at r457–597 while leading (T5 A15); TryThrow asserts in 15 of 70 validation games, all 5 instrumented failures with a cat on step 1 (T2§9.1); ≥ 26 visible on 11 king turns in 86 mirror games, all on cheesebottles (S:kingcrowd_mirror.txt) | S1 |
| G2 | Starvation: spawns stop at 100 global, but rats spend global with no floor (traps, digs); the loser's income collapses after it loses the exchange | C:robot/king/KingSpawningStrategy.java:69; moves/TryTrap.java.jinja2:64-76; robot/rat/BabyRat.java:222 | 62 STARVE cells, 22% of `goal` (§1.1); the bank went to spawns, traps and upkeep, not to the `cost == 10` exception (§4.1); starvation in 9 of 13 king-kill losses against OBG and GST (T5§2.2) | E2, E1, CT, E3 |
| G3 | The king stands in cat lanes: walks to the centre box, parks on the nearest mine whatever the cats, flees by the direction to the cat's corner rather than its facing; ring-cell bug B3; the panic squeak decodes as two phantom enemies | C:robot/king/KingMiningStrategy.java.jinja2:53-76,173-278; KingCombatStrategy.java.jinja2:102-170,116-117; comms/Squeaker.java:173-217 | CAT class (3 maps); tminusten, attack_on_cat and sunandmoon against OBG (T4§2.1); 117 scratches on its kings against 50 on the opponents' (T5 A12) | K1, K2, K3 |
| G4 | Kings: second king gated on ≥ 20 rats against a cap of 16; recruits every rat within 25 tiles; third king needs ≥ 9 mines and ≥ 40 rats; aborts at r1100 | C:robot/king/KingFormationCoordinatorStrategy.java:13,141-221,145; robot/rat/FormationStrategy.java:19-21,29-33,109-120 | 8 of 11 mirror points games end 2v2 (T3§3.2); formations fail 17 of 17 on cheesefarm and peaceinourtime (strategy§6.3) | KL, KB |
| G5 | Exchange: traps only behind a gate (≥ 24 rats, raw ≥ 20, or a "strong" enemy) and never overlapping; throws at the first ready turn whatever the line, including 22% into open ground; TryPosition has no goal; cats absent from throw scoring; throw values 4 too high, dirt 10 below walls; bites fixed at 12; no trap model | C:moves/TryTrap.java.jinja2:64-90,270-282,357-361; CombatStrategy.java.jinja2:216-231,440-443; TryThrow.java.jinja2:91-95,319-376; TryPosition.java.jinja2:83-87; CombatTracker.java.jinja2:46-67 | awubot walks into 95–98% of enemy traps and lays 0.36–0.83× OBG's and GST's in their wins (T5§2.3, A8); 95% of carries thrown at exactly t+2 (T2§2.3) | CT, CW, CB, CX |
| G6 | Territory: the ⌊√d⌋ mine lottery favours far mines; 20% exploration never stops | C:robot/rat/MinerStrategy.java:70,83-111 | OBG and GST hold more mines in 12 of 15 wins (T5§4.2, §5.2) | E3 |
| G7 | Army: fixed cap of 16, with "rich" lines that release idle cheese hundreds of rounds late | C:KingSpawningStrategy.java:21-32 | 700–1850 global held at the cap in contested mirror games (strategy§10.1) | E2 |
| G8 | Points: no raw-cheese recall; cat bites use 0 cheese; the king never bites cats; no cooperation policy; it kills its own last cat while behind | C:robot/rat/BabyRat.java:245-250; KingCombatStrategy.java.jinja2:180-195; MinerStrategy.java:181-186 | grove (T3§3.4); 2000 raw held at the TheHeist mirror ending (T3§3.2) | PT, CB, PC |
| G9 | Information: symmetry from walls only; cat memory is 4 rounds per rat and never reaches the king's movement | C:CellTracker.java.jinja2:360-366; RobotTracker.java.jinja2:146-148 | T4§2.3 | K2, IS |
| G10 | Action theft: a move or turn calls `tryTransferCheese`, which can spend the action planned for a bite or throw | C:robot/rat/BabyRat.java:179-213 | observed twice on bites (combat§8, B10) | CB |

Strengths to keep (T1 C10, T2§7, T5§7): the micro listed in §4.2; kings mine (17% of income by direct pickup, T1§2.6);
its traps fire 98%; the lean few-mine macro and close-quarters swarm (OBG lost DefaultSmall in both seats, opp§1).

## 7. Where OBG and GST fall short

| bot | shortfall against the dominant plan | source |
|---|---|---|
| OBG | its king parks on the most central mine, which is the rush zone on ≤ 4-mine maps (bitten 30–35 times, dead at r189–190) | T5§4.4 |
| OBG | its throw ranking puts the enemy king and an enemy at distance 1 above a cat, though a feed is a kill | O:ThrowManager.java:8-15; T2§8 |
| OBG | it overpays bite cheese (1/4/9 where 1/2/5 suffice) | O:MyUnit.java:113-150; T2§8 |
| OBG | second king only from r350 and only once (keepout); no lock-in before r1201 | O:RatKing.java:151-226; T5§4.3 |
| OBG | walks into 98% of awubot's traps (no trap model) | T5§2.3 |
| GST | the king starved in 7 of 7 king-kill losses: its only floor is `BUILD_ANYTHING_THRESHOLD` (80 cheese), and it spawns with no cost cap while in distress | G:RatKingBehaviors/RKHeadquarter.java:46-51; T5§5.1-5.2 |
| GST | 13% open landings and 21% later collisions: weaker throws than OBG's | T2§8 |
| GST | spawn spam from r1900 trades tiebreak 2 for tiebreak 3, a net loss at an exact tie | G:RatKing.java:104; T3§1.5 |
| GST | at most 2 kings (3 on > 2500 tiles); army too thin for awubot's early swarm on ≤ 6-mine maps | G:RatKing.java:65; T5§5.2 |
| both | no cooperation policy beyond cat traps; no use of the backstab victim's cat-trap window | T3§5 |

## 8. Lens disagreements and how they were settled

| # | topic | positions | settled | how |
|---|---|---|---|---|
| 1 | Self-inflicted king and rat deaths | T5 P1 (mine overflow), T2 P1 (TryThrow assert), T3 P7 (cat-trap NPE), T4/T5 (king in a cat lane) | One package, S1, for every code path that kills or freezes our own robot. The cat-lane king is behaviour, not a crash, and goes to K1–K3 | The NPE is an `Exception` (turn lost, not a death; C:BabyRat.java:42,68). S1 adds `catch (Throwable)`, which is safe because `RobotDeathException` extends `VirtualMachineError` (E:instrumenter/RobotDeathException.java) and the instrumenter makes every handler re-throw `VirtualMachineError` first (E:instrumenter/bytecode/InstrumentingMethodVisitor.java:278-290). This settles the open item in arch§9 invariant 2 |
| 2 | tminusten: can the king step out? | T5 P1b: perpendicular steps save it. T4: a pocket, only rats can | **T4** | Map render (`t4/render.py maps.json tminusten`): the king's pocket is x 0–13, y 0–4; its exits at (7,5), (9,5) and (11,5) are 1 tile wide; the cat's waypoints (0,2), (6,2), (12,2) sweep rows 1–3 along the whole pocket. Every king position overlaps the lane. K1 cannot save it; K3 can |
| 3 | Extra kings | T1 E1 (lock-in r900–1195, 3–5 kings), T1 E2 (early second king, 7 recruits at the 2-median mine, dose 40/100/200), T3 P1 (≥ 3 by r1200 by budding), T3 P3c (bud before r150 on cooperation maps), T5 P7 (OBG rule from r350) | Two packages: KL (lock-in, first) and KB (early, a dose-response that includes r350 and an opening bud). KL first relaxes the existing coordinator's gates (T1 E1) and builds budding only if those formations fail | Rule 7 (simplify before adding): the relaxed coordinator is 30–80 lines of existing code. Budding keeps the army in place, at +130 cheese over recruiting at 16 rats (A, §4.3). T3's "no walk" and "about 150 cheese at r150" are wrong (§4.3 geometry and costs). The lock-in reaches 8 of the 11 mirror points maps, not 10: TheHeist and peaceinourtime end in cooperation at r390 and r451 (T3§3.1). T1's argument for early formation (29% of official games are over by r300) is testable only by the dose |
| 4 | Where the income edge comes from | T1: fewer early deaths; T5: traps and central mines; T4: central mines sit in cat lanes | One causal chain: exchange (traps; T1's 26% fewer deaths) → share (mines held) → the enemy starves | T1§2.4 measures equal income with fewer deaths before any share gap; T5§4.2 measures the trap ratio and mines held in the same games. T4's lane objection applies to the **king's** site, not to miners |
| 5 | The reserve (shared code) | T4 WP1: max(0, 2K − I)·(2000 − t); T1 E3 and T5 P5: 2K·H; T4 and T5: awubot spends to 0 | max(100, 2K·H) on **every** global spend, no siege waiver below 100, the `cost == 10` exception kept | A: T4's form needs 4000 > 2500 while I ≈ 0 early, which blocks all spawning. `critic/Starve.java`: the bank went to replacement spawns, traps and upkeep, and the exception spent 0–50 per loss (§4.1). A reserve flips only the maps where the winner also starved (3 of 8), so it ranks below CT and CW |
| 6 | Trap-first engagement | T2 P3 (+3–8 pp), T5 P2 (+10–20 pp on mine-rich maps) | One owner, CT. Effect +3–8 pp overall, at the top of that range on ≥ 8-mine maps | SPAARK, powerpuff and food out-trap awubot yet win 1 game of 18 (T5§6). T5's figure is a correlation across bots that also differ in territory |
| 7 | Collision damage | rules and combat fixed to 42; T4§2.4 and T5§2.3 still say 46 | 42 | R§9; E:world/RobotControllerImpl.java:1405 (the step-1 tile must be empty); none of 4,195 replayed throws dealt 46 (T2§1.2) |
| 8 | Death counts | T2 CombatStats, T5 Deep, T1 Timeline | All three count formation crush, which is a `DieAction`, as a death. T5 also counts thread-end removals (disintegrations and Error deaths) from `Round.diedIds`; T2 omits them | E:world/RobotControllerImpl.java:1231-1262 (formation sets HP to 0). Formation crush is about 7 rats per formation, only on keepout, TheHeist and sieveofriches in D1, so no conclusion changes. awubot's 11–57 disintegrations per game against OBG are carried victims under 15 HP, i.e. kills, so T5's ratio is the right one. T1's δ includes formation consumption: E2 removes it |
| 9 | Engine of the "official" mirror | T3: 1.2.6; T4: 1.2.5 jar | **1.2.5** | `abtest_2026-11-18_19-30-32.json`: `engine_version 1.2.5` |
| 10 | Value of a king on its mine | T4: 9 of 81 box tiles, ≈ 0.25 cheese per round; T1: kings pick up 17% of income | **T1**'s measurement | The king chases cheese within Chebyshev 4 of its mine (strategy§6.2; C:KingMiningStrategy.java.jinja2:109-171). T4's figure is a static bound |
| 11 | Go-home thresholds | T1 E5: raise the near-home ones; T5 A7: not a lever; T3: low thresholds at the end | Compatible. E5 is an arm of E3; the end-of-game recall is PT's and depends on time | T5 A7 refutes "large loads hurt awubot", not "small loads near home waste trips" |
| 12 | King site | OBG and opp§4 #2: most central mine; T1, T4 and T5: home-side | Home-side mine that minimises delivery, off lanes (§4.3) | T5§4.4: OBG's central king loses ≤ 4-mine maps; T4§2.2: central mines sit in lanes |
| 13 | Kings seeing > 25 robots | T3 P1 risk | Rare today, capped anyway in S1 | S:kingcrowd_mirror.txt: 11 king turns with ≥ 26 robots in view across 86 mirror games (cheesebottles only), none with ≥ 44. KL packs kings, so S1 caps the sort at 25 |
| 14 | Flight squeak | opp§4 #4; T2§10 | Dropped; CC+CK (pre-throw discipline) instead | T2§10: steps 1–2 fly inside the thrower's turn, and awubot's later collisions are under 400 HP per game |

## 9. Not settled, and what settles it

| question | settled by |
|---|---|
| Do the combat packages lift the COMBAT cells (34% of `goal`) from coin flips to 0.8–0.9? (§2.6; the plan's weakest assumption) | CT, CW, CB, CX results on `L-combat`, read by class |
| The best round for the second king: an opening bud, r40, r100, r200 or r350 | KB dose-response |
| δ as a function of army size; estimating income in-game | E2 step 0: a probe build logging global cheese and spending every 50 rounds |
| Cat lanes around walls (straight segments vs the engine's BFS) | K2 step 0: port `GameWorld.bfsFromTarget` (E:world/GameWorld.java:223) and compare on the cat pool |
| How the bot can estimate the mine count early enough to gate E2's arm 1 | E2 step 0: a probe logging the estimate and the true count |
| Whether E2's arm-1 effect (0.86 on 7 maps, 10 games each) holds across `mines-le4` | E2's gain test on `mines-le4` |
| Whether the relaxed coordinator completes formations in the lock-in window | KL arm A's formation-success diagnostic |
| Whether a cat in ATTACK prefers kings | K1 diagnostics (scratches per cat phase) |
| How exploitable awubot's trap gate is | CX |
| 1.2.6 seat effects on H and V maps | every acceptance run on 1.2.6 (plan.md §1) |
| Kings acting across 1-thick walls (T3§8) | not needed by any package |

## 10. Which numbers depend on 1.2.6 cheese spawning

1.2.6 changes the spawn offsets ([−4, 3] → [−4, 4]), mirrors them correctly on H and V maps, and diverges the
cheese RNG stream (R§0). The spawn probability is unchanged.

| unaffected | affected (re-measure on 1.2.6 before relying on the exact value) |
|---|---|
| supply per mine (2.257 per round), upkeep, rat costs, combat, traps, throws, cats, scoring, king rules | the king's direct-pickup share (17% on 1.2.5; its box share goes from 9/64 to 9/81); mines held and pickup counts (T5); income shares and starvation rounds (T1§2.4, T4 starvation clock), hence E1's H and E2's n*; seat bias on the 22 H/V official maps; which maps reach points (T3§7). Measured on 1.2.6 so far: the loss classes and the starvation spend channels (§1.1, §4.1) |

## 11. Synthesis scratch (`~/battlecode26-data/theory/synthesis/`)

| file | content |
|---|---|
| `KingCrowd.java`, `kingcrowd_mirror.txt` | per game and team: king turns with ≥ 26 and ≥ 44 robots within r² 25 (carried and flying rats counted, an over-estimate). Run: `java --add-opens java.base/java.nio=ALL-UNNAMED -cp "$(cat ../t3/cp.txt):." KingCrowd <replays>` |
| `losstable.py`, `losstable.txt` | the §1.1b classification of the 43 official mirror maps, from T4§2.1's HP split and T4's `maps.tsv` and `classes2.py` |
| `w03_classify.py`, `w03_cells.tsv`, `w03_stats.jsonl` | the §1.1 classification of W0.3's 285 cells: `python3 w03_classify.py RUN_DIR` runs T4's ReplayStats (HP split) and KingTrace (starvation) over each cell's replay; SELF comes from `KingSelf` |
| `KingSelf.java`, `w03_kingself.txt` | per replay and team: kings removed without a `DieAction` (their own code ended) and kings killed by damage |
| `w03_starve.txt` | `critic/Starve.java` over the 62 STARVE cells |
| `../critic/Starve.java` (the plan critic's) | global cheese per round (`Round.teamAliveRatKings` / 10, T3's decoding) and the spend channels from the first round below 100 to the first below 2 |
