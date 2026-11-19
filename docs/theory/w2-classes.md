# W2: why main still loses to awubot after wave 3, and where a lever of +5 pp is

Re-classification of main's losses after wave 3 (MC, KF, TN merged; PC1, PT, E2b, XA, KD, AB closed) by W1's method
(docs/theory/w1-classes.md §1), the economy gap taken apart term by term, and the candidate levers priced by oracles.

Source keys:
- `E:path:line`: engine source at tag `engine.1.2.6`, under `engine/src/main/battlecode/`.
- `C:` `src/challenger/`, `A:` `src/awubot/`.
- `D:` data and tools in `~/battlecode26-data/scratch/w2/` (§7).
- Game ids are the run's (`c<cell><seat>`, our seat); rounds are engine rounds. "Rich" is a map with ≥ 8 mines;
  "long" a game lasting ≥ r600.

**The run.** `1119-150554-ab-6083`: main (`challenger@2f9375a`, bot source `1557b1b`: MC, KF and TN on W1's main)
against awubot on `goal` (43 official + 52 tournament maps), seed base 9201 (fresh), 6 passes, 570 cells, both seats,
engine 1.2.6, every replay kept. Result **952–188 (0.835 [0.802, 0.870])**; cells won both 402, split 148, lost both
20. The awubot mirror on the same cells (`xa_awucopy` against awubot, identical code, so every cell splits by seat):
`1119-150558-ab-ad39`.

---

## 0. Summary

1. **Points losses collapsed; fights, starvation and cats did not move.** Per 570 games, W1 → now: POINTS 41 → 12,
   COMBAT 32 → 31, STARVE 27 → 27, CAT 15 → 18, COMBAT+starve 7 → 6. By mechanism: points at r2000 29 → 10.5,
   cooperation endings 12 → 2; early fights 26 → 27, snowballs 21 → 20, cat deaths 15 → 17.5 (§2).
2. **The losses sit on maps, not on chance.** 15 maps lose ≥ 5 of their 12 games (binomial at 0.835: 3) and 32 lose
   none (11). 22 maps with ≥ 4 losses hold 103 of the 188. W1's "the cells are what chance gives" no longer holds,
   though half of a map's count is still the draw (per-map losses correlate 0.48 between the run's halves).
   The maps with ≥ 4 losses fall into four clusters: cat deaths from one seat (41 losses on 8 maps, 30 of them CAT),
   small-map early fights (26 on 5), snowballs, mostly on rich maps (33 on 7), points at r2000 (15 on 3) (§2.3).
3. **The economy gap is the centre mines, and it is presence, not efficiency.** In our long rich losses (same maps as
   our wins), awubot's income lead by r600 is +1,380 cheese a game: centre-mine pickups +1,113 (81%), own-side pickups
   +507, king pickups +128, against −227 from death drops we recover and raw awubot spends or loses. Pickups per
   rat-turn in a mine box are equal or better for us; awubot has 2.3× our rat-turns in the centre boxes. It loses
   *more* rats there than we do (centre-box deaths r300–600: 11.5 against our 8.0): the centre is taken by
   allocation, not by fighting. The awubot mirror shows the same mechanism with smaller asymmetry (loser 535 vs winner
   914 centre rat-turns) (§3).
4. **What varies between our wins and losses is awubot's centre commitment, not ours.** Our trips to centre mines in
   r150–300 are 7–8 a game in losses and wins alike; awubot's are 12 in our losses and 6 in our wins. The race opens
   at r150–250 and locks in with a king: in long rich losses from the mirror's losing seat awubot forms 12 kings
   before r300 to our 6, and its king-turns near a centre mine reach 18% (ours 2%) (§3.2).
5. **The centre is worth +8 pp, priced by an oracle; being there ourselves buys none of it.** An awubot copy that
   never takes centre-mine cheese (`w2_awunoc`) loses to main 0.915 against 0.835 on all 1,140 games: **+0.080
   [+0.044, +0.115]**, replicated on both halves (+0.068 on passes 0–2, +0.092 on 3–5), all of it on the 63 maps with
   a centre mine (**+0.130 [+0.086, +0.177]**; −0.018 on the others), with our rich-map cheese share at r500 +5.6 pp.
   Half of awubot's rats abstaining is worth about half against the null (+0.058 of +0.107). But our rats sending half of their trips to a centre mine
   (cheat knowledge of where it is) reads +0.011 against the re-randomised null, in r100–500 only +0.000, and our
   starting king parked at a centre mine −0.035: our presence rises 33%, awubot's barely moves, and we lose as much
   on the other mines as we gain there (§4.1).
6. **The seat is 6 pp and mostly not ours to fix.** We win 0.896 from the seat that wins the awubot mirror on that
   cell and 0.774 from the other; 74% of split cells are lost on the mirror's losing seat. Salting our random stream
   keeps the gap (0.839 / 0.740); salting awubot's breaks the mirror's seat (only 54% of cells still split, 59% of
   those the same way). The seat is awubot's own random luck on that cell, and in rich games it is the centre race of
   item 4 (§4.2).
7. **Read single oracles against re-randomised nulls, not against one main.** Five copies of main or awubot with only
   a salted random stream score 0.789–0.814 (mean 0.802, SD 0.011) on the 285 cells where main scores 0.840: main's
   realization there is a high draw (at seed base 9202 a salted copy reads +0.009). Every re-randomising arm on those
   cells reads about −0.04 against main for that reason alone. Salting only the kings' stream changes 4 of 570 games:
   the rats' streams carry all of it (§4.6).
8. **Ranked packages** (§5): (1) **CD**, centre denial on maps with a centre mine: the only lever with a measured
   ceiling ≥ +5 pp (+8.0 against main, +10.7 against the null; half-denial +1.9 / +5.8), but presence does not reach
   it, so it needs a mechanism that takes awubot's rats or their cheese off the centre (traps, carrier interception),
   unpriced; (2) **CL**, the cat lane from one seat (35 CAT losses, ceiling 3.1 pp, realistic about +1); (3) the king
   race and the small-map fights are closed or small (awubot's formations now +2.1 pp; E2b). Nothing measured reaches
   0.90 except a large share of CD.

---

## 1. Method

- **Classes and mechanisms**: exactly W1's rules and code (w1-classes §1; `scratch/w1/w1_classify.py`, `mech.py`),
  run on the new replays by `D:pipeline.sh` (ReplayStats, KingSelf, KingTrace, W1Trace, MineControl, LastCat,
  W2Econ). The classifier reproduces W1's table exactly from W1's replays (122 losses, same classes per game).
- **Counts compare per 570 games**: W1 played 285 cells (570 games), this run 570 cells (1,140 games), on different
  seeds (W1: seed base 0, whose pass 0 is the maps' own seeds; here 9201, all derived).
- **The economy reader** (`D:W2Econ.java`): per game, team and 50-round bucket, an exact account of raw cheese, from
  the replay's actions (`E:world/RobotControllerImpl.java:490-526` pickups, `:1390` transfers,
  `E:world/GameWorld.java:549,1161` mine spawns and death drops, `E:world/InternalRobot.java:251` raw spending first):
  - income = king pickups (a king's pickup is recorded as a transfer to itself) + rat deliveries;
  - rat deliveries = pickups (at own-side, centre or enemy-side mine boxes, or loose: death drops) − raw spent
    (bite bonus, traps, digs) − raw at death − raw absorbed by formations − change in raw held.
  The identity holds exactly in every game tested (`picks − deliveries − spent − lost − absorbed − held = 0`, and
  deliveries + king pickups = the round's `teamCheeseTransferred`).
  - Mine sides as W1: Chebyshev distance to the two starting kings, a margin of 3 ("centre" = within 3 of
    equidistant). A mine box is Chebyshev 4 around the mine (the spawn box, rules.md §5).
  - Rat-turns by activity: combat (a bite on a rat or king, a nap, a throw, a rat trap, or an enemy rat within
    d² 20 or king within d² 32 that took a turn this round or the last), cat, in a mine box (by side), loaded or empty
    outside the boxes; idle (no move, no action) as a flag.
  - Trips: from the first pickup with no raw to the delivery; outbound leg (previous delivery or birth to first
    pickup), return leg (last pickup to delivery), return distance; the side of each trip's first pickup.
  - Kings: turns near a centre mine (Chebyshev 4), on a mine, distance to the map centre.
- **Same-map comparison**: losses and wins are averaged per map, then over the maps that have both (24 rich maps
  for r0–600), so the map mix does not drive the comparison.
- **Oracles** (XA's method, xa-formations §1): copies of a bot under another package name on branch `t3/w2-probes`,
  changed in one place or given cheat knowledge, played on the first 285 cells of the run (3 passes, 570 games), paired
  game by game with main's games there (`D:oracle.py`, CI by bootstrap over maps). The cheat table (`D:mkmaptable.py`)
  lists each goal map's centre mines keyed by map size and the own king's start; the first king writes the key's index
  to the free shared slot 15 on its first turn (7 of 179 keys are shared by 2–3 maps; 0.7% of mine-box tiles get the
  wrong side).

---

## 2. The loss table

### 2.1 Classes

| class | W1 losses per 570 (share) | now: lost games (share) | per 570 | change | maps | median end round | losing kings' HP lost (median): bites / scratches / starvation / throws and rest |
|---|---|---|---|---|---|---|---|
| POINTS | 41 (34%) | 25 (13%) | 12 | −70% | 13 | 2000 (4 cooperation, 21 at r2000) | – |
| COMBAT | 32 (26%) | 62 (33%) | 31 | −3% | 35 | 328 | 285 / 0 / 5 / 240 |
| STARVE | 27 (22%) | 54 (29%) | 27 | 0% | 29 | 619 | 20 / 0 / 475 / 40 |
| CAT | 15 (12%) | 35 (19%) | 18 | +17% | 11 | 283 | 10 / 560 / 0 / 50 |
| COMBAT+starve | 7 (6%) | 12 (6%) | 6 | −14% | 11 | 605 | 200 / 20 / 205 / 244 |
| SELF | 0 | 0 | 0 | – | – | – | – |
| **all** | 122 (0.786 won) | 188 (0.835 won) | 94 | −23% | 63 | | |

- **By mine count** (won): ≤ 4 mines 0.873 (W1 0.840), 5–7 0.829 (0.798), ≥ 8 **0.819** (0.755). Rich-map losses
  (111): STARVE 30, CAT 30, POINTS 23, COMBAT 21, C+s 7. Official maps 0.860 (72 losses), tournament 0.814 (116).
- Endings: our kings killed in 163 games (median r419), awubot's in 799 (median r396). Robots killed by their own
  exception: ours 0, awubot's 226.
- **Income share still predicts the long games** (games lasting 200 rounds past the sample; W1 §3.2):

| our share of cheese transferred at | < 0.40 | 0.40–0.47 | 0.47–0.53 | 0.53–0.60 | ≥ 0.60 |
|---|---|---|---|---|---|
| r300 | 0.35 (40) | 0.78 (60) | 0.81 (191) | 0.91 (171) | 0.93 (89) |
| r500 | 0.35 (20) | 0.54 (39) | 0.84 (118) | 0.93 (135) | 0.97 (76) |
| r800 | 0.17 (12) | 0.46 (28) | 0.79 (80) | 0.93 (101) | 1.00 (47) |

### 2.2 Mechanism groups (`scratch/w1/mech.py`)

| group | W1 losses per 570 | now (of 188) | per 570 | our wins of the same kind | classes now |
|---|---|---|---|---|---|
| fight-early | 26 | 54 | 27 | 395 | COMBAT 40, STARVE 11, C+s 3 |
| snowball | 21 | 40 | 20 | 202 | STARVE 29, COMBAT 10, C+s 1 |
| cat-early | 10 | 27 | 13.5 | 54 | CAT |
| fight-late | 12 | 21 | 10.5 | 109 | STARVE 8, COMBAT 7, C+s 6 |
| P-r2000 | 29 | 21 | 10.5 | 124 | POINTS |
| bank-race | 7 | 13 | 6.5 | 13 | STARVE 6, COMBAT 5, C+s 2 |
| cat-late | 5 | 8 | 4 | 26 | CAT |
| P-coop | 12 | 4 | 2 | 29 | POINTS |

KF, MC, TN and the cooperation king took the points endings; the fight, snowball and cat groups are where they were.
The economic groups (snowball, fight-late, bank-race, P-r2000) still hold 95 of 188 losses (51%).

### 2.3 Map clusters

Losses per map, of 12 games (`D:maptable.py`), against a binomial at p = 0.835:

| games lost | 0 | 1 | 2 | 3 | 4 | 5 | ≥ 6 |
|---|---|---|---|---|---|---|---|
| maps observed | 32 | 16 | 15 | 9 | 8 | 10 | 5 |
| binomial | 10.9 | 25.9 | 28.1 | 18.5 | 8.2 | 2.6 | 0.7 |

The maps with ≥ 4 losses (22 maps, 103 losses), by their dominant mechanism:

| cluster | maps (losses of 12; seat of the losses A/B) | losses |
|---|---|---|
| cat on a seat's lane | tminusten 5 (5/0), peaceinourtime 5 (5/0), externalsustenance 5 (4/1), bed 7 (4/3), closeup 5 (3/2), sunandmoon 5 (1/4), toomuchcheese 5 (3/2), directconfrontation 4 (1/3) | 41 (CAT 30) |
| small-map early fight | houseofcards 8 (3/5), Nofreecheese 5, DefaultSmall 5, DefaultMedium 4 (3/1), streetsofnewyork 4 | 26 |
| snowball (mostly rich maps) | cheddarchokehold 6, EscapeTheNight 6 (0/6), worldshardestgame 5, kingofthehill 4 (0/4), popthecork 4 (0/4), jail 4 (3/1), hatefullattice 4 | 33 |
| points at r2000 | where_are_you 6, longwayaround 5, RUN 4 | 15 |

**How structural.** Per-map losses correlate 0.48 between the run's two halves (passes 0–2 and 3–5, different
seeds) and 0.39 between main and a salted copy on the same cells; over 6 games a map the variance is 1.28 against a
binomial 0.80. About half of a map's loss count is the map, half the draw: on new seeds a cluster map's excess over
the mean shrinks by about half.

**The 20 lost-both cells**: houseofcards ×3 (early fights), bed ×2, toomuchcheese ×2 (cats), DefaultSmall,
Nofreecheese, QueenOfHearts (early fights), externalsustenance, sunandmoon, closeup (cats), cheddarchokehold,
hatefullattice, worldshardestgame (snowballs), RUN, where_are_you (points 48–51, 47–52; 43–56, 41–58),
longwayaround, cheeseysquare (points and late starvation).

---

## 3. The economy gap at its root

### 3.1 The decomposition

Rounds 0–600, long rich games, per game, ours / awubot's / gap (awubot − ours). Our losses and wins on the same 24 maps
(64 losses, 139 wins); the mirror's loser and winner (414 games) (`D:decomp.py main mirror 0 600`):

| term | our losses | our wins | mirror loser / winner |
|---|---|---|---|
| **income** (king pickups + deliveries) | 4,250 / 5,630 / **+1,380** | 6,623 / 4,157 / −2,466 | 4,961 / 6,072 / +1,111 |
| king pickups | 384 / 512 / +128 | 584 / 407 / −176 | 550 / 623 / +72 |
| pickups at own-side mines | 2,800 / 3,307 / +507 | 3,340 / 2,966 / −374 | 4,263 / 4,590 / +327 |
| **pickups at centre mines** | 2,006 / 3,119 / **+1,113** | 3,520 / 2,034 / −1,486 | 1,387 / 2,069 / +682 |
| pickups at enemy-side mines | 835 / 824 / −11 | 1,268 / 538 / −729 | 739 / 960 / +222 |
| loose pickups (death drops) | 559 / 429 / −130 | 604 / 453 / −150 | 369 / 383 / +15 |
| − raw spent (traps, digs, bite bonus) | −583 / −661 / −78 | −700 / −553 / +147 | −637 / −692 / −56 |
| − raw lost at death | −1,203 / −1,265 / −62 | −1,252 / −1,341 / −89 | −1,205 / −1,292 / −87 |
| − absorbed by formations, held raw | −549 / −636 / −87 | −740 / −348 / +392 | −505 / −569 / −65 |
| rat-turns | 9,897 / 11,173 / +1,276 | 11,511 / 9,656 / −1,855 | 10,691 / 11,709 / +1,018 |
| in combat | 1,490 / 1,650 | 1,636 / 1,747 | 1,635 / 1,662 |
| in own-side mine boxes | 1,938 / 2,261 | 2,090 / 1,972 | 2,888 / 3,083 |
| **in centre mine boxes** | **593 / 1,339** | 1,409 / 756 | 535 / 914 |
| in enemy-side mine boxes | 269 / 331 | 479 / 234 | 223 / 331 |
| empty, outside the boxes | 4,086 (41%) / 3,840 (34%) | 4,150 (36%) / 3,568 (37%) | 3,792 (35%) / 3,969 (34%) |
| pickups per rat-turn: own / centre / enemy boxes | 1.44, 3.38, 3.11 / 1.46, 2.33, 2.49 | 1.60, 2.50, 2.65 / 1.50, 2.69, 2.30 | 1.48, 2.60, 3.32 / 1.49, 2.26, 2.90 |
| deliveries; carry per delivery | 85, 45.5 / 119, 43.1 | 135, 44.8 / 86, 43.5 | 112, 39.4 / 135, 40.4 |
| outbound leg / return leg (rounds); return distance | 48, 19; 9.0 / 40, 17; 7.8 | 39, 16; 7.8 / 46, 18; 8.4 | 41, 17; 8.1 / 39, 16; 7.8 |
| trips starting at a centre mine | 33 / 60 | 65 / 37 | 26 / 41 |
| baby deaths (of them carrying) | 64 (16) / 76 (19) | 66 (18) / 82 (21) | 71 (20) / 69 (20) |
| king-turns near a centre mine | 5% / 18% | 16% / 11% | 8% / 11% |

- **The largest term is the centre mines: 81% of the gap in our losses** (60% of our lead in wins, 61% of the
  mirror's gap). Own-side pickups are second (+507: awubot works its side harder; our own-side cheese left on the
  ground at r600 is 975 a game against awubot's 646 on its side, all 67 long rich losses). King pickups +128. Death
  drops, spending and raw lost to deaths net −227 in our favour: we pick up more of the death drops (559 against 429).
- **It is presence, not efficiency.** Per rat-turn in a centre box we pick 3.38 against awubot's 2.33 (fewer of us
  share the cheese there); in own-side boxes 1.44 against 1.46. W1's "cheese per rat-turn at our mines 1.28–1.35
  against 1.62–1.83" does not recur on this run. In log terms our rats' delivered cheese trails by ln 1.32: army
  (rat-turns) 43%, deliveries per rat-turn 76%, carry size −19%.
- **Where our rats are instead**: empty outside the mine boxes, 41% of rat-turns in losses (36% in wins, 34–35% for
  awubot and the mirror), with an outbound leg of 48 rounds against 40. In wins the same habit pays: exploration finds
  the enemy side (enemy-side pickups 1,268 against 538; TN's `EXPLORE_P` 0.6).

### 3.2 When it opens, and what it is not

Long rich games by the seat the awubot mirror favours on that cell and by result; per 50 rounds, ours / awubot's:

| | r100–150 | r150–200 | r200–250 | r250–300 | kings formed before r300 (ours / awubot's) | income share r0–300 |
|---|---|---|---|---|---|---|
| lost from the mirror's losing seat (44) — centre rat-turns | 33 / 49 | 41 / 77 | 45 / 104 | 59 / 156 | 6 / 12 | 0.444 |
| — centre trips; king-turns near a centre mine | 2.7, 0.0 / 2.2, 0.0 | 2.0, 0.0 / 3.0, 0.9 | 2.3, 0.3 / 4.7, 5.0 | 3.1, 2.7 / 6.9, 13.0 | | |
| won from the mirror's losing seat (148) — centre rat-turns | 32 / 39 | 40 / 41 | 53 / 42 | 56 / 42 | 63 / 41 | 0.530 |
| lost from the mirror's winning seat (23) — centre rat-turns | 32 / 45 | 37 / 55 | 45 / 71 | 51 / 92 | 5 / 4 | 0.506 |
| won from the mirror's winning seat (160) — centre rat-turns | 36 / 34 | 41 / 41 | 51 / 48 | 63 / 50 | 63 / 43 | 0.542 |

- **The race opens at r150–250 and locks in with a king.** Up to r150 presence is level within ~15 rat-turns. In
  the losses awubot's centre presence doubles every 50 rounds from r150, its king-turns near a centre mine go from 0
  to 13 per 50 rounds by r250 (its second king, formed there), and the income share by r300 is 0.444.
- **Our side does not change between wins and losses; awubot's does.** Our centre trips in r150–300 are 7.4–8.4 a
  game in all four groups (7.0–8.4). awubot's are 6.2–6.3 in our wins, 7.4 in our losses from the favoured seat and
  14.7 in those from the unfavoured seat. The game turns
  on whether awubot commits to the centre, which the seat predicts (§4.2).
- **It is not fighting.** Centre-box deaths, ours / awubot's, long rich losses: r150–300 2.5 / 3.4, r300–600 8.0 /
  11.5 (wins: 2.5 / 2.7, 5.7 / 6.9; mirror 2.7 / 2.9, 7.1 / 7.2). Combat rat-turns in centre boxes are within 15%
  (losses r300–600: 175 / 207). awubot does not take the centre from us; it is there with more rats while ours are
  elsewhere.
- **Early income differs by map, not by race.** The unfavoured-seat losses start poorer for both sides (r0–50
  income 59 / 58 against 95 / 104 in the wins): harder maps, not a worse opening.

### 3.3 Can our code change it?

- The centre term is an allocation: which known mine a rat picks (`C:robot/rat/MinerStrategy.java` `pickSeenMine`,
  weight ⌊√d⌋ from the king, 40% of trips with `Tune.EXPLORE_P` 0.6) and where kings form
  (`C:robot/king/KingFormationCoordinatorStrategy.java`). Neither looks at the enemy's presence. Both bots share this
  code, so the mirror shows the same race. The asymmetry in our losses is larger than the mirror's (2.3× against
  1.7×), and our rats spend more turns empty outside the boxes (41% against 34%); but sending those trips to the
  centre buys nothing (§4.1), so the extra exploration is not what loses the centre.
- awubot's commitment is its own: its rats pick known mines 80% of the time with the same distance lottery, and its
  coordinator starts a formation at a far mine once rich (xa-formations §2.1). We cannot stop the commitment
  (XA: denial unreachable). Being there first does not take it from awubot (§4.1): the centre-commit oracle raised
  our centre rat-turns 297 → 396 in r100–300 and awubot's fell only 261 → 242.
- The own-side term (+507) is the other half of the same allocation: rats sent exploring leave own-side cheese on the
  ground (975 a game unpicked at r600 in long rich losses).
- Army size follows income (+1,276 rat-turns in losses, from +1,339 spawn cost paid); king pickups follow where the
  kings are. Neither is a separate lever.

---

## 4. Levers and their ceilings

All oracles play the first 285 cells of the run (passes 0–2, 570 games), paired game by game with main's games there
(`D:oracle_table.py`). A copy that changes most games' course is read against the mean of five re-randomised nulls
(copies with only a salted random stream, §4.6), one that changes few against main.

| run | score | vs main [95% CI] | vs null mean | results changed | our rich-map cheese share r300 / r500 |
|---|---|---|---|---|---|
| main | 0.840 | – | +0.038 | – | 0.549 / 0.565 |
| null: challenger, salted stream (`1119-152527-ab-2af9`) | 0.789 | −0.051 [−0.093, −0.009] | −0.012 | 137 | 0.546 / 0.557 |
| null: challenger, salt 2 (`1119-153256-ab-9136`) | 0.809 | −0.032 [−0.072, +0.007] | +0.007 | 122 | 0.545 / 0.562 |
| null: challenger, salt 3 (`1119-153312-ab-0741`) | 0.814 | −0.026 [−0.070, +0.016] | +0.012 | 127 | 0.552 / 0.573 |
| null: challenger, rats' stream salted (`1119-154754-ab-6c6f`) | 0.791 | −0.049 [−0.089, −0.007] | −0.011 | 134 | 0.545 / 0.558 |
| null: awubot, salted stream (`1119-153716-ab-73ad`) | 0.805 | −0.035 [−0.074, +0.004] | +0.004 | 128 | 0.538 / 0.557 |
| challenger, kings' stream salted (`1119-154751-ab-0917`) | 0.840 | +0.000 [−0.007, +0.007] | – | 4 | 0.548 / 0.565 |
| **awubot never takes centre-mine cheese** (`w2_awunoc`, `1119-152258-ab-1802`) | **0.909** | **+0.068 [+0.032, +0.105]** | **+0.107** | 101 | **0.597 / 0.617** |
| the same, all 570 cells (`1119-154358-ab-3e67`; main 0.835 there) | 0.915 | +0.080 [+0.044, +0.115] | – | 223 of 1,140 | 0.605 / 0.621 (main 0.548 / 0.565) |
| awubot: even-id rats never take centre cheese (`w2_awunoc50`, `1119-154330-ab-d5e8`) | 0.860 | +0.019 [−0.012, +0.051] | +0.058 | 105 | 0.556 / 0.578 |
| awubot never starts a formation (`xa_awunf`, `1119-153154-ab-938d`) | 0.861 | +0.021 [−0.004, +0.046] | – | 34 | 0.555 / 0.590 |
| challenger: half of all trips go to a centre mine (`w2_chctr`, `1119-153111-ab-fa9f`) | 0.812 | −0.028 [−0.072, +0.016] | +0.011 | 128 | 0.539 / 0.574 |
| challenger: the same in r100–500 only (`w2_chctr2`, `1119-154109-ab-1b17`) | 0.802 | −0.039 [−0.081, +0.005] | +0.000 | 138 | 0.539 / 0.572 |
| challenger: the starting king parks at the centre mine nearest its start (`w2_chkc`, `1119-154117-ab-1292`) | 0.767 | −0.074 [−0.130, −0.019] | −0.035 | 154 | 0.527 / 0.560 |

The null mean is 0.802 (SD 0.011 over the five), share 0.545 / 0.562.

### 4.1 The centre

- **The ceiling is large and real.** awubot that never takes centre-mine cheese (its rats skip centre piles and mines,
  its kings skip centre mines as targets and in their 3×3 pickups) loses to main 0.915 against 0.835 over all 1,140
  games, +0.080 [+0.044, +0.115]; passes 0–2 +0.068, passes 3–5 +0.092. By maps: with a centre mine (63 maps, 756
  games) **+0.130 [+0.086, +0.177]**, without one (384 games) 0.870 → 0.852. By richness: ≤ 4 mines +0.023, 5–7 +0.110, ≥ 8
  +0.096. Main's losses it wins: 70 of 91 on the first 285 cells (fight-early 19 of 26, snowball 16 of 21, cat-early
  11 of 14, P-r2000 8 of 8), against 54 of 91 for the first salted null; main's wins it loses: 31 of 479 (that null: 83). Our rich-map
  cheese share at r500 rises 0.565 → 0.621. The gains are on the cluster maps of §2.3: bed, cheddarchokehold,
  EscapeTheNight, longwayaround 3 → 6 of 6 (first 285 cells), cheeseysquare 3 → 5, popthecork, DefaultSmall,
  hatefullattice, peaceinourtime, DefaultMedium, knifefight 4 → 6.
- **Dose.** With only its even-id rats abstaining (kings and odd ids unchanged), +0.019 against main and +0.058
  against the null; the share moves +1.1 / +1.6 pp against the null (r300 / r500), a third of the full denial. A
  denial that halves awubot's centre take is worth roughly +2 to +5 pp.
- **Presence does not reach it.** Three oracles put us at the centre with the exact centre mines given:
  - half of every rat's trips (after r20) go to a random centre mine: +0.011 against the null;
  - the same only in r100–500, the race window of §3.2: +0.000;
  - the starting king parks at the centre mine nearest its start: −0.035 (it is exposed there; main's own kings sit
    on centre mines in 5–16% of king-turns, §3.1).
  Replays of the first (`tools/games replays … --cells all`, `D:chctr/`), r100–300 on rich maps with a centre mine,
  per game, main → oracle: our centre rat-turns 297 → 396 and centre pickups 929 → 1,096; awubot's centre rat-turns
  261 → 242 and pickups 750 → 734. We lose 55 on our side and 49 on the enemy's; awubot gains 50 on its side and 32
  on ours. awubot's rats are not displaced: a centre pile goes to whoever stands next to it, and both teams' miners
  arrive by the same lottery. The share at r500 is 0.574 against the null's 0.562: +1.2 pp, no wins.
- **So the lever is to take awubot's rats or their cheese off the centre, not to add ours.** What could do it, none
  priced: rat traps on the centre boxes' tiles toward awubot's side (CT's band traps, 20 cheese, 50 damage and a
  stun: a trapped carrier drops its raw, which we recover better than awubot, §3.1: 559 against 429 a game);
  interception of awubot carriers leaving the centre (they carry 40+ raw, and the death drop is ours); dirt on the
  approach (awubot paths through dirt only from global 1,500, xa-formations §0 item 6). XA showed our rats are
  rarely near awubot's formation crowds; miners are another matter: centre combat rat-turns in our rich losses are
  175 against 207 (r300–600), so the two sides do meet there.

### 4.2 The seat

- **Size.** We win 0.896 from the seat that wins the awubot mirror on that cell and 0.774 from the other (W1: 0.832 /
  0.740); seats A and B are equal (0.835 each). 109 of the 148 split cells (74%) are lost on the mirror's losing
  seat: if a fraction f of split cells is seat-decided and the rest coin flips, f = 0.47 (W1: 0.28). The seat gap is
  70 games of 1,140 (6.1 pp); 11 maps hold 64% of it.
- **Structural seats** (the mirror favours one seat in all 6 cells): EscapeTheNight (A; we lose all 6 as B),
  peaceinourtime (B; 5 of 6 lost as A, 4 to the cat), popthecork (A), kingofthehill (A); tminusten favours B in 5 of 6
  (5 cat deaths as A). These are map features both bots lose to from the bad seat.
- **Seed-dependent seats**: on jail and longwayaround the favoured seat changes with the seed (3 A, 3 B), yet we win
  6 of 6 from it and 2 of 6 from the other. Something the seed sets decides both bots' games.
- **Not our random stream.** Both bots seed `Rng` from the robot id (`A:util/Rng.java:58-63`), and spawned ids come
  from the map seed, so our first rats draw what awubot's would in the mirror. Salting our seed
  (`w2_chsalt`, one constant) leaves the correlation: 0.839 from the favoured seat, 0.740 from the other (main on the
  same cells 0.905 / 0.775); f falls from 0.47 to 0.32.
- **awubot's random stream.** An awubot copy with its seed salted (`w2_awusalt`) against awubot no longer follows
  the mirror: only 152 of 281 cells split (the mirror splits all 285), and in 90 of those 152 (59%) the winner holds
  the original mirror's winning seat (50% by chance). The mirror's seat is mostly the luck of awubot's own random
  draws on that cell and seat, and our games inherit it from awubot's side. It is the rats' draws: salting only the
  kings' stream changes 4 of 570 games (§4.6). Which draws (exploration rays, the mine lottery) is inference.
- **Who holds which stream.** Both starting kings have ids 1 and 2 on every map and seed (initial bodies keep the
  map's ids), so a starting king's stream depends only on its seat, the round and its position: the same on every
  seed of a map. Spawned rat ids come from the map seed (`E:world/IDGenerator.java`), and the same ids go to the same
  seat whichever bot holds it (checked on c026 and c156 of both runs): seat A's rats in our game draw from the
  streams seat A's rats had in the mirror.
- **What the seat is, in rich games**: the centre race of §3.2. From the mirror's losing seat our rich long losses
  are centre losses (awubot 2.3× our presence, income share 0.444 at r300); from the winning seat they are not
  (1.6×, 0.506).
- **Ceiling.** Playing the bad seat as well as the good one is +6.1 pp. On structural-seat maps it needs a seat-aware
  plan for that map; elsewhere it is the centre race (§4.1).

### 4.3 Map clusters

- Bringing the 15 maps that lose ≥ 5 of 12 to one loss in 12 would save 68 of 188 losses: 0.895. Half of that
  excess is the draw (§2.3), so the structural reservoir is nearer 35 games (3 pp). They are four different problems:
  - **Cats on a seat's lane** (35 CAT losses, 3.1 pp): tminusten A, peaceinourtime A, externalsustenance A,
    closeup A, toomuchcheese A, directconfrontation B. KD's oracle priced a far second king at +0.9 pp; K2's lane
    memory moved cat deaths but not wins (docs/closed.md). A king route or site that keeps out of the cat's
    waypoints from that seat is untested.
  - **Small-map early fights** (houseofcards, Nofreecheese, DefaultSmall, DefaultMedium, streetsofnewyork; 26):
    transferred by the end 672 against 1,200 in all 62 early king-kill losses: the bank race of E2/E2b (closed).
  - **Snowballs** (33) and **points at r2000** (15): the centre (§4.1), and cheese at r2000 on where_are_you
    (28k against 33k) and longwayaround (29k against 43k).
- **Gains of the no-centre oracle by map** (games won of 6, main → oracle): bed 3 → 6, cheddarchokehold 3 → 6,
  EscapeTheNight 3 → 6, longwayaround 3 → 6, cheeseysquare 3 → 5, popthecork, DefaultSmall, hatefullattice,
  peaceinourtime, DefaultMedium, knifefight 4 → 6: most loss-cluster maps are centre maps, small ones included.

### 4.4 awubot's formations, re-priced

- `xa_awunf` (awubot whose coordinator never starts a formation, XA's oracle) against main on the first 285 cells:
  **+0.021 [−0.004, +0.046]** (34 results changed: games are identical up to awubot's first formation attempt, so
  this reads against main). On ≥ 8-mine maps +0.046 [+0.010, +0.085]; ≤ 7 mines −0.008. Our king lead at r500 on rich
  maps +0.36 → +0.68. XA measured +0.063 on the main of its time (0.784): KF, TN and MC took two thirds of the value
  of awubot's kings, and what is left is rich maps.
- Denial stays unreachable (XA), and the remaining value overlaps with the centre: awubot's early kings form at
  centre mines once rich (§3.2).

### 4.5 Split and lost-both cells

- Cells won both / split / lost both: 402 / 148 / 20. Independent games at p = 0.835 give 398 / 157 / 16; with the
  seat (0.896 / 0.774), 395 / 161 / 13. The lost-both excess (20 against 13) is the map clusters of §2.3.
- 90% needs the losses at 114 of 1,140. Split cells alone lose 148 games; with every lost-both cell split we would
  still lose 168. The seat gap (70 games) and the cluster maps (68 games above the median map) are the two
  reservoirs, and they overlap on the centre maps.

### 4.6 Null calibration

- **The nulls.** Copies that differ from main or awubot only in the seed of their random stream (`Rng.seed`,
  `A:util/Rng.java:58-63`: `s0 = id × 0x41C64E6D`, with the id XORed by a constant) score 0.789, 0.809, 0.814, 0.791
  (rats only) and 0.805 (awubot salted) on the 285 cells where main scores 0.840. All five read −0.026 to −0.051
  against main because they share one reference: main's realization of those cells is a high draw, 3.5 SD above
  the five (SD 0.011; TN measured 0.016 for a 285-cell variant).
- **It is that run's draw, not a tax.** On `goal` at seed base 9202 (285 cells) the salted copy scores 0.814 against
  main's 0.805 (+0.009 [−0.030, +0.047]); on `teammade` at 9201 (150 cells) 0.763 against 0.793 (−0.030 [−0.087,
  +0.030]). Salting only the kings' stream changes 4 of 570 games (the starting kings always have ids 1 and 2, but
  their random draws barely matter); salting only the rats' reproduces the whole −0.049. The replays of a remote run
  equal its recorded games (570 of 570, `same_game`), so recording is not the cause.
- **What it means for testing.** A change that alters the course of most games re-draws about 21–27% of their
  results (122–154 of 570 here); the reference's own draw on the pool's cells then shifts every arm compared with it
  by the same amount, in the same direction. Several arms read against one main on one seed base share that error:
  their agreement in sign is not replication. Read an oracle against salted copies of main on the same cells, and
  confirm a gain on a second seed base (docs/evaluation.md already asks for the latter).
- **Main's level.** The run's 0.835 (570 cells) sits at the high end: salted copies average 0.802 on its first 285
  cells, main reads 0.805 at seed base 9202. Main beats awubot about 0.80–0.835 depending on the draw.

---

## 5. Packages, ranked

Gains are pp of `goal` (1,140 games at 0.835: 1 pp ≈ 11 games). "Ceiling" is an oracle's value; "expected" is
what a buildable version could get, bounded by the ceiling and the dose oracle.

| rank | id | change | evidence | ceiling / expected | test | risk |
|---|---|---|---|---|---|---|
| 1 | **CD** | Centre denial on maps with a centre mine, in r100–600: take awubot's miners or their cheese off the centre-mine boxes. Arms, one at a time: (a) rat traps on a centre box's tiles on awubot's side, placed by our rats already there; (b) combat targets awubot carriers (raw ≥ 20) leaving a centre box; (c) dirt on the box's approach from awubot's side. Centre mines from the king starts and the symmetry (`CellTracker`): within 3 of equidistant, as here. | §3: 81% of the income gap in long rich losses is centre pickups, by presence; §4.1: awubot never taking centre cheese +0.080 [+0.044, +0.115] (+0.130 on centre maps), half of its rats +0.019 (+0.058 against the null); our presence alone +0.000 to +0.011 against the null | ceiling +8 pp (against main), +10.7 (against the null); expected +1 to +4 if an arm halves awubot's centre take, 0 if it only adds our presence | diagnostic first, on replays (`D:W2Econ`): awubot's centre-box rat-turns and pickups, our rich-map share at r300/r500 (the half-denial oracle moved it +1.1 / +1.6 pp). Gain test on the centre maps at a fresh seed base, δ 0.08 (`L2-rich-long` maps or `mines-ge8`), then `goal` at δ 0.05; read against a salted main on the same cells | high: presence costs elsewhere (§4.1); traps cost 20 each and CT's band already spends; XA found our rats rarely near awubot crowds (formations), CX that trap placement near enemies is costly |
| 2 | **CL** | The king's route or site from the losing seat of a cat map: keep the king off the cat's path during its first walk to a mine. | §2.3: 35 CAT losses, 30 on 6 seat-specific maps (tminusten A 5, peaceinourtime A 4, externalsustenance A 4, closeup A 3, toomuchcheese A 3, directconfrontation B 3); KD: a far second king wins 13 of 25 CAT losses with foresight; K2's D' cut cat-involved king deaths 30% without wins | ceiling 3.1 pp (every CAT loss won); expected +0.5 to +1.5 | `L2-cat` maps at a fresh seed base, δ 0.10; harm on `goal` | medium: K2 and KD closed nearby arms; enemies kill the kings the cats no longer do (K2 D') |
| 3 | **EV** (evaluation, track E) | Paired tests against salted copies of main as well as main: the ref's own draw on a pool's cells shifts every arm by the same amount (§4.6). | five salted nulls 0.789–0.814 where main reads 0.840; at seed base 9202 +0.009 | no bot gain; removes a shared ±3–4 pp error from oracle and arm reads on one seed base | – | none |
| – | king race | Deny awubot's formations. | awubot without formations is now +0.021 [−0.004, +0.046] (XA on its main: +0.063); denial unreachable (XA) | ≤ 2 pp, unreachable | closed (XA) | – |
| – | small-map fights | houseofcards, Nofreecheese, DefaultSmall, DefaultMedium, streetsofnewyork: 26 losses. | E2b's cap arms all H0 on fresh seeds; the no-centre oracle wins DefaultSmall and DefaultMedium 4 → 6 of 6: part of it is the centre | ≤ 2.3 pp, covered by CD where it is the centre | closed (E2b) | – |
| – | seat | Play the mirror's losing seat differently. | the seat is awubot's random luck (§4.2), acting through the centre race | – | not a separate package: CD | – |

**What 90% needs.** From 0.80–0.835 to 0.90 is 74–110 of 1,140 games. The only measured reservoir of that size is
the centre: denying it outright is worth +8 pp against this main (+10.7 against the re-randomised null), and the
games it converts are the fight-early, snowball, cat and points losses of the cluster maps. Half of the denial is
worth about half. Everything else measured is ≤ 3 pp. CD is a bet on finding a mechanism that removes awubot's
miners from the centre; nothing in this report prices that mechanism, only what it would be worth.

---

## 6. Against docs/closed.md

- **Already closed, and why W2 does not reopen them**:
  - our presence at mines: MC's box holding (not built), E3's four miner-targeting arms, MC's arm F; W2 adds three
    oracles with the centre mines given (§4.1), all ≈ 0 or below against the null. Presence is closed; CD is not
    presence.
  - where the second king sits: KF arms S1, S2, K (KB-centre); W2's starting king at a centre mine −0.035 against
    the null.
  - denying awubot's formations (XA); re-priced here at +0.021: smaller still.
  - rat caps and siege spawns on small maps (E2, E2b); emergency kings (KD); cat-lane memory (K2).
  - exploration: load-bearing (AB, MC arm F); TN raised it. In our losses our rats spend 41% of turns empty outside
    the mine boxes against awubot's 34%, but sending those trips to the centre instead gains nothing (§4.1).
- **New rows** (docs/closed.md, W2): the three presence oracles.
- **Not covered by closed.md**: CD's denial arms (traps, interception, dirt at centre boxes), CL's king route on a
  cat map's losing seat, EV.

---

## 7. Data, tools and pools

- `D:` = `~/battlecode26-data/scratch/w2/`, Java compiled against `~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar`
  into `D:cls/`:
  - `pipeline.sh RUN_DIR OUT_DIR [JOBS]`: every W1 replay reader (ReplayStats, KingSelf, KingTrace, W1Trace,
    MineControl, LastCat) and `W2Econ` over a run's replays, into `stats.jsonl`, `kingself.txt`, `kt/`, `trace.jsonl`,
    `mc.jsonl`, `lastcat.txt`, `econ.jsonl`; then `scratch/w1/w1_classify.py RUN_DIR games.tsv` in OUT_DIR.
    `econ_only.sh RUN_DIR OUT_DIR` runs W2Econ alone.
  - `W2Econ.java` (`W2Econ OUT.jsonl replay...`): §1's economy account per team and 50-round bucket.
  - `losstable.py NEW OLD` (§2.1), `maptable.py DIR [MIRROR_RUN]` (§2.3), `econ.py DIR [--mirror] [--minmines]
    [--minround] [--win]` (windows, per group), `decomp.py MAIN MIRROR R0 R1` (§3.1), `centre.py` (win rate by
    centre share and king position), `seat.py MAIN_RUN MIRROR_RUN` (§4.2), `oracle.py BASE_RUN RUN...` (paired diffs
    by mine count, centre maps, and the result series' cheese share and king lead), `oracle_table.py` (§4's table).
  - `Ids.java` (`Ids replay...`): the starting kings' ids and the first spawned rat ids per team.
  - `mkmaptable.py PKG OUT.java`: the oracles' cheat table (`maps_goal.json`: every goal map's size, king starts and
    mines with W1's sides, read from the mirror's replays).
  - Data: `main/` (this run), `mirror/` (the mirror), `w1run/` (W1's run through the same pipeline), `mirror0/`
    (W0.3's mirror, W2Econ only), `chctr/` (the centre-commit oracle's replays, W2Econ only).
- Probe branch `t3/w2-probes` (never merged; worktree `D:wt/`), each a copy under its own package name:
  `w2_awunoc` (awubot that never takes centre-mine cheese), `w2_awunoc50` (only its even-id rats abstain),
  `w2_chctr` (challenger sending half of its trips to a centre mine), `w2_chctr2` (the same in r100–500),
  `w2_chkc` (the starting king parks at the nearest centre mine), `w2_chsalt`, `w2_chsalt2`, `w2_chsalt3`
  (challenger with a salted random stream), `w2_chsaltK`, `w2_chsaltR` (only the kings' or the rats' stream),
  `w2_awusalt` (awubot with a salted stream). Each centre probe holds `W2Map.java` from `D:mkmaptable.py`.
- Runs: main `1119-150554-ab-6083`; mirror `1119-150558-ab-ad39`; oracles and nulls in §4's table; the no-centre
  oracle on all 570 cells `1119-154358-ab-3e67`; salted awubot against awubot `1119-152541-ab-098c`; null checks: main
  and the salted copy on `teammade` (`1119-154633-ab-4d68`, `1119-154634-ab-89fb`) and on `goal` at seed base 9202
  (`1119-154635-ab-ed76`, `1119-154636-ab-4678`). Main's run had 8 native-engine failures, replayed on Java (state
  `backends`); every guard pair matched (28).
- **Pools** (`~/battlecode26-data/games/pools/`, one `map:seed` a line, from this run). **Biased at seed base 0**:
  pass 0 plays the listed seeds, which are main's own losses, so any change reads as a gain there (regression to the
  mean, docs/evaluation.md). Run gain tests on them with a fresh `--seed-base`; the pool then means "these maps,
  weighted by their losses".
  - by class: `L2-points` (23 cells), `L2-combat` (59), `L2-starve` (52), `L2-cat` (32), `L2-combat-starve` (12);
  - by mechanism: `L2-early` (fight-early, bank-race; 61), `L2-snowball` (snowball, fight-late; 59);
  - `L2-seat` (lost on the mirror's losing seat; 129), `L2-rich-long` (rich maps, ≥ r600; 59), `L2-all` (168).
