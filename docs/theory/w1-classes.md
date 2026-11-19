# W1: why main still loses to awubot after wave 1

Re-classification of main's losses after wave 1 (plan §5.2 item 4), by W0.3's method (plan §3, DS§1.1), then the
mechanisms inside each class, the ceiling, and the packages they call for.

Source keys:
- `E:path:line`: engine source at tag `engine.1.2.6`, under `engine/src/main/battlecode/`.
- `D:`: data and tools in `~/battlecode26-data/scratch/w1/` (§6).
- Game ids are the run's (`c<cell><seat>`, our seat); rounds are engine rounds.

**The run.** `1119-065328-ab-12f4`: main `challenger@ca54f76` (S1, RB, E2 arm 1a, S2, KL, CW, CT, S3, CB1c merged)
against awubot on `goal` (43 official + 52 tournament maps), seed base 0, 3 passes, 285 cells, both seats, engine
1.2.6. Result 448–122 (0.786); cells won both 177, split 94, lost both 14. These are the same 285 cells (map, seed) as
W0.3's mirror run `1118-222544-ab-6dea`, so every cell can be compared with its W0.3 class.

---

## 0. Summary

1. **POINTS is now the largest class** (34% of losses), then COMBAT (26%), STARVE (22%), CAT (12%), COMBAT+starve
   (6%), SELF (0%). COMBAT shrank most (96 → 32 losses per 570 games), POINTS least (72 → 41).
2. **By mechanism, 57% of the losses are economic**: points at r2000 (29), mid-game snowballs (21), late fights after
   the income fell behind (12), races to starvation (7). Early fights are 21% (26), cooperation endings 10% (12),
   cats 12% (15).
3. **The economy is decided by the centre mines in r0–600.** Our share of the cheese picked at the centre mines by
   r600 is 0.36 in the long games we lose on rich maps and 0.57 in those we win. The income share at r500 decides the
   long games: below 0.47 we win about half, above 0.53 we win 0.88–0.97.
4. **Rich maps hold the gap**: 75 of the 122 losses are on maps with ≥ 8 mines (we win 0.755 there; 0.84 on ≤ 4 mines).
5. **Cooperation endings are lost by our own hand.** The engine scores the game at the instant the last cat dies.
   In 10 of our 12 cooperation losses our rats dealt the last cat its final damage while behind on cheese delivered;
   when awubot ends the game, we win 5 of 7.
6. **Combat is no longer the bottleneck.** In the lost games that last past r400 we lose fewer rats than awubot by
   r500 (47 vs 55); SELF and own-exception deaths are 0.
7. **The seat decides part of the rest.** Our win rate is 0.832 from the seat the mirror run favoured and 0.740 from the
   other; about 28% of the split cells are seat-decided (tminusten, bed, dirtpassageway, EscapeTheNight, Hike,
   peaceinourtime and others). The cell counts (177/94/14) are what independent games at 0.786 give (176/96/13).
8. **Ranked packages** (§5): PC1 last-cat rule, on top of KB's cooperation king (+0.4 to +1.2 pp, low risk); MC
   centre-mine presence on rich maps (+1 to +3 pp, sign uncertain, study first); E2b army from attrition on small
   mine-poor maps (+0.5 to +0.9 pp); KS on the seat-decided king walks (running; +0.4 to +0.7 pp); a centre-mine
   second king only after MC (+0.3 to +0.9 pp). Together +3 to +6 pp: about 0.82–0.85. 90% needs the rich-map economy
   fixed; nothing measured yet reaches it.

---

## 1. Method

- Every lost game is classified by W0.3's rules (DS§1.1, `synthesis/w03_classify.py`), adapted to a run where cells
  do not split: one row per game, the losing team's kings (`D:w1_classify.py` → `D:games.tsv`):
  - **POINTS**: no king kill.
  - **SELF**: a losing king removed by its own code (KingSelf: a `diedIds` entry with no `DieAction`, or a
    `DieAction` of type exception) within 5 rounds of the end.
  - **STARVE**: starvation ≥ 400 HP or ≥ 40% of the losing kings' HP; **CAT**: scratches ≥ 300 HP or ≥ 40%;
    **COMBAT+starve**: starvation ≥ 200 HP or ≥ 25%; **COMBAT**: the rest.
  - HP split from T4's `ReplayStats` (bites ×10, scratches ×20, residual), starvation from `KingTrace` with kcause.py's
    rule (exact −10, no bite or scratch that round or the one before, runs of ≥ 3).
- W0.3 had one loss per cell (every cell split): 285 losses in 570 games. This run has 122 losses in the same 570
  games, so counts compare directly.
- Mechanism groups, per lost game (`D:mech.py`), from a per-game timeline (`D:W1Trace.java`):
  - **P-coop**: points, cooperation ending (the last cat died in cooperation).
  - **P-r2000**: points at r2000.
  - **cat-early / cat-late**: class CAT, end before / after r600.
  - **fight-early**: COMBAT, COMBAT+starve or STARVE ending before r450, unless both teams starved.
  - **bank-race**: both teams' global cheese fell below 2.
  - **snowball**: 50 rounds before the end the winner had transferred ≥ 1.5× our cheese and had ≥ 1.5× our babies.
  - **fight-late**: the rest.
- Mine control (`D:MineControl.java`): every pickup is credited to the mine whose spawn box (Chebyshev 4) holds it.
  Each mine is "own", "mid" or "enemy" by Chebyshev distance to the two starting kings (a margin of 3 tiles). The
  same tool counts rat-turns spent in each mine's box.

---

## 2. The loss table

| class | W0.3 losses (share) | now: lost games (share) | change per 570 games | maps | median end round | losing kings' HP lost (median): bites / scratches / starvation / throws and rest |
|---|---|---|---|---|---|---|
| POINTS | 72 (25%) | 41 (34%) | −43% | 20 | 2000 (12 cooperation endings, 29 at r2000) | – |
| COMBAT | 96 (34%) | 32 (26%) | −67% | 23 | 286 | 315 / 0 / 0 / 275 |
| STARVE | 62 (22%) | 27 (22%) | −56% | 20 | 720 | 0 / 0 / 470 / 50 |
| CAT | 33 (12%) | 15 (12%) | −55% | 10 | 425 | 0 / 560 / 0 / 38 |
| COMBAT+starve | 19 (7%) | 7 (6%) | −63% | 7 | 714 | 250 / 0 / 190 / 154 |
| SELF | 3 (1%) | 0 | −100% | – | – | – |
| **all** | 285 of 570 | 122 of 570 | −57% | | | |

**Per-game win probability on W0.3's class cells** (the plan's s_c; cells keep their W0.3 class):

| W0.3 class | cells | games lost now | s measured | plan's wave-1 forecast (low / mid / high) | what the losses are now |
|---|---|---|---|---|---|
| COMBAT | 96 | 35 / 192 | **0.82** | 0.58 / 0.70 / 0.78 | COMBAT 22, STARVE 5, C+s 4, POINTS 3, CAT 1 |
| POINTS | 72 | 40 / 144 | **0.72** | 0.60 / 0.68 / 0.76 | POINTS 32, COMBAT 5, STARVE 3 |
| STARVE | 62 | 22 / 124 | **0.82** | 0.60 / 0.70 / 0.80 | STARVE 14, POINTS 3, COMBAT 3, C+s 2 |
| CAT | 33 | 18 / 66 | **0.73** | 0.58 / 0.68 / 0.78 | CAT 12, STARVE 4, POINTS 2 |
| COMBAT+starve | 19 | 7 / 38 | **0.82** | 0.58 / 0.68 / 0.78 | mixed |
| SELF | 3 | 0 / 6 | **1.00** | 0.85 / 0.95 / 1.00 | – |

- COMBAT, STARVE and COMBAT+starve beat the plan's high column; POINTS and CAT sit between its mid and high.
  The weakest assumption of plan §0 (combat cells stay near 0.65) did not hold: combat cells are the strongest now.
- **Official maps**: 49 losses in 258 games (0.810): COMBAT 18, POINTS 16, STARVE 8, CAT 5, C+s 2.
  **Tournament maps**: 73 in 312 (0.766): POINTS 25, STARVE 19, COMBAT 14, CAT 10, C+s 5.
- **By mine count**:
  - ≤ 4 mines: 24 losses in 150 games (0.840 won): COMBAT 9, POINTS 5, STARVE 4, CAT 3, C+s 3.
  - 5–7 mines: 23 in 114 (0.798): STARVE 9, COMBAT 9, POINTS 3, CAT 2.
  - ≥ 8 mines: **75 in 306 (0.755)**: POINTS 33, STARVE 14, COMBAT 14, CAT 10, C+s 4.
- Other features (loss rate per game): cut 1–2 maps 0.40 (48 games, mostly cooperation and points maps), vertical
  symmetry 0.27, areas 1601–2500 0.29; the rest 0.15–0.22. Map size and cats per map do not separate the losses.
- Endings: our kings were killed in 81 games (median r526), awubot's in 356 (median r392). Robots killed by their own
  exception: ours 0, awubot's 110.

---

## 3. Mechanisms

### 3.1 Mechanism groups

| group | our losses | our wins (awubot's loss of the same kind) | win rate | classes of our losses |
|---|---|---|---|---|
| P-r2000 | 29 | 83 | 0.74 | POINTS |
| fight-early | 26 | 175 | 0.87 | COMBAT 21, STARVE 3, C+s 2 |
| snowball | 21 | 81 | 0.79 | STARVE 15, COMBAT 3, C+s 3 |
| P-coop | 12 | 9 | **0.43** | POINTS |
| fight-late | 12 | 56 | 0.82 | COMBAT 8, STARVE 3, C+s 1 |
| cat-early | 10 | 27 | 0.73 | CAT |
| bank-race | 7 | 5 | **0.42** | STARVE 6, C+s 1 |
| cat-late | 5 | 12 | 0.71 | CAT |

The economic groups (P-r2000, snowball, fight-late, bank-race) hold 69 losses (57%). The two groups we lose more often
than we win are cooperation endings and bank races.

### 3.2 The economy: the centre mines decide the long games

**Income share predicts the long games.** Our share of all cheese transferred, and our win rate in games that last
200 rounds more (`D:games.tsv`, `D:trace.jsonl`):

| share at | < 0.40 | 0.40–0.47 | 0.47–0.53 | 0.53–0.60 | ≥ 0.60 |
|---|---|---|---|---|---|
| r300 | 0.44 (25) | 0.68 (53) | 0.80 (110) | 0.83 (83) | 0.82 (28) |
| r500 | 0.48 (21) | 0.51 (37) | 0.71 (78) | 0.88 (74) | 0.97 (36) |
| r800 | 0.20 (15) | 0.53 (19) | 0.68 (69) | 0.93 (58) | 1.00 (23) |

**Combat does not explain it.** Games lasting ≥ 400 rounds, means over r50–min(end, 500):

| | our babies / theirs | baby deaths ours / theirs | deaths to cats | cheese transferred by r500 |
|---|---|---|---|---|
| lost (80) | 15.9 / 16.9 | 47 / 55 | 6.1 / 5.8 | 2,829 / 3,542 |
| won (261) | 17.3 / 15.6 | 51 / 67 | 5.8 / 5.6 | 4,187 / 3,306 |

We kill more than we lose in the games we lose, with about the same army; we deliver 20% less.

**The centre mines.** Cheese picked by r600 per game, by mine side, in games lasting ≥ 600 rounds (`D:mcan.py`):

| | own-side mines: ours / theirs / spawned | centre mines: ours / theirs / spawned | enemy-side mines: ours / theirs / spawned |
|---|---|---|---|
| lost (64) | 3,239 / 648 / 4,688 | **1,368 / 2,418** / 4,139 | 508 / 3,451 / 4,652 |
| won (201) | 4,747 / 509 / 5,503 | **1,915 / 1,448** / 3,537 | 1,194 / 4,368 / 5,537 |

On ≥ 8-mine maps the split is sharper: our share of the centre cheese is **0.36** in the 48 long losses and **0.57**
in the 162 long wins. Our own-side cheese left unpicked by r600 is **943** a game in those losses and 239 in the wins.

**Presence, not only throughput.** Rat-turns spent in the mines' spawn boxes on ≥ 8-mine maps, r300–600, ours / theirs:

| | own side | centre | enemy side | share of our rat-turns at a mine |
|---|---|---|---|---|
| lost (48) | 1,380 / 222 | **491 / 1,081** | 167 / 1,464 | 39% |
| won (162) | 1,975 / 215 | **721 / 502** | 484 / 1,658 | 48% |

Up to r300 the armies are level on these maps (17.3 vs 17.6 babies in losses); the army gap opens at r300–600
(17.3 vs 20.2) from the income gap. Cheese per rat-turn at our own mines is also lower than awubot's when it raids
them (1.28–1.35 against 1.62–1.83).

**Extra kings follow the centre.** awubot's extra kings formed before r1200 sit at centre mines in 30 of 56 cases in
games we lose (54%) and 37 of 108 in games we win (34%). When awubot forms the first extra king we win 0.61 of games
(89 games); when we form it first, 0.85 (171); neither, 0.80 (310).

Representative games:
- **c089A diamond** (r2000, 31–68): awubot forms a second king at the centre (17,18) at r334. From there its income
  is 8.9 cheese a round against our 4.6 (r400–1000). Our bank falls from about 1,850 at r200 to 94 at r1100; our king
  starves 250 HP. It ends 1 king against 2, and 2,030 cat damage against 5,670. We held 1,400–1,850 cheese at r200–500
  and formed no king before r1200. The coordinator's gates before r800 (babies per king, known mines, a free mine
  outside the taboo zone: `KingFormationCoordinatorStrategy.java:200-266`) did not open; which one held is not in the
  replay.
- **c237A thunderdome** (r166, COMBAT; lost both seats): both banks crash to about 100 by r80. awubot's deliveries
  restart first (435 transferred by r100 against our 120, 1,270 vs 485 by r160); it respawns and kills our king.
- **c045B EscapeTheNight** (r556, STARVE; seat B): by r300 we have transferred 386, awubot 2,744. awubot's second
  king forms at (5,36) at r298. Both bots win from seat A here, 3 of 3 (§4).

**Bank races** (7 losses, 5 wins: worldshardestgame ×3, attack_on_cat ×2, moonlitriverpark, thehallway): both banks
reach zero and our king dies first. Example: **c091B worldshardestgame**, 8 cats eat 33 of our rats and
27 of theirs. Income is 2,120 against 2,695 by r1000, the bank 34 against 136 at r950, and our king dies at r1023.
E1 closed spend gates: starvation follows the income share (docs/closed.md, E1 memory). Rat traps do cost us more than
awubot in every group (by r500: 989 vs 728 cheese in snowball losses, 643 vs 520 in bank races). That is CT's price,
and CT gained +7.6 pp.

### 3.3 Cooperation endings: we end them while behind

- **The rule.** In cooperation, the last cat's death ends the game by points, computed at that instant, mid-round
  (`E:world/GameWorld.java:1181-1182` → `checkWin` → `setWinnerifAllCatsDead` → `setWinnerIfMorePoints`, `:911`). Cheese
  transferred later in the same round does not count. The run's end-of-round points can therefore show the loser
  ahead: c113B and c211A read 50–49 for us, lost.
- **What decides them.** In all 12 cooperation losses the cat damage was equal (4,000–4,000, or 8,000–8,000 on
  t_minus_five_minutes): each team killed its own cat. Kings were equal in 9 of the 12. The cheese share decided.
- **Who ended them** (`D:LastCat.java`: the team whose cat damage rose in the final round):

| | we dealt the last cat damage | awubot did |
|---|---|---|
| cooperation endings won | 4 (c055B, c071A, c113A, c211B) | 5 |
| cooperation endings lost | **10** | 2 (c113B, c198A) |

- **c124B groveisbeingdesecrated** (r214, 41–58): both teams spend the opening biting their cats (60–85 cat bites per
  20 rounds). awubot finishes its cat at r166. Ours dies to our own rats at r214, with 60 cheese transferred against
  awubot's 885.
- Margins are thin: 6 of the 12 losses are 49–50 or 50–49 at the end of the round, 2 more are 47–52.
- The maps: groveisbeingdesecrated, TheHeist, peaceinourtime, t_minus_five_minutes (12 cells, `L1-coop`).
  21 cooperation endings: we win 9 (0.43).

### 3.4 Points at r2000

- 29 losses. 11 are within 3 points (8 by 1 point). In 8 we end with fewer kings (1–2 in 7, 2–3 in 1); in 19 kings are
  equal and in 2 we have more. In 17 of those 21 the cat-damage term is a larger deficit than the cheese term.
- **Cat damage follows the late army.** Mean final cat damage, ours / theirs: 3,214 / 4,875 in our r2000 losses,
  5,617 / 2,834 in our r2000 wins. Bites on cats: 251 / 388 in losses, 442 / 193 in wins. Much of awubot's lead comes
  after r1500 (minimaze 2,810 → 4,780, cheesebottles 600 → 2,010 and 1,260 → 2,870, cheeseguardians 6,240 → 10,570).
- PT measured the late cat hunt: cat term +2.5 to +3.4, kings −0.8 to −0.95, net about 0; r2000 delivery is dead
  (PT memory). So the r2000 class moves only with the economy of §3.2 and the king count.
- r2000 losses are on rich maps: 24 of the 29 have ≥ 8 mines (the others: cheesebottles ×3, uneruesansfin ×2).

### 3.5 Early fights

- 26 losses: COMBAT 21, STARVE 3, COMBAT+starve 2, median r225; we win 175 such games (0.87). The losing kings' HP loss
  (bites 250, throws and boosted bites 230, starvation 82 a game) looks like awubot's in the early fights we win
  (221 / 170 / 182): no damage source is ours alone.
- Income comes first again: transferred by the end, ours / theirs, 536 / 957 in these losses and 895 / 656 in the
  wins. Spawn cheese: 1,498 / 1,952 in losses, 1,594 / 1,867 in wins.
- **Small mine-poor maps under E2's cap** (≤ 1,600 tiles, < 5 mines known: 8 babies by r150 against awubot's 11–12 on
  ≤ 900 tiles). On maps of ≤ 1,600 tiles with ≤ 6 mines we lose 31 of 180 games: the cap works overall. It loses on:
  - **houseofcards**: 4 of 6 lost, 2 cells lost both. In **c136A** (r227) we deliver 32 cheese by r220 against 449; at
    r100–120 our 8 rats bite the cat (18 bites) while awubot's 12 bite our king from 550 to 268 HP.
  - bed (3 of 6, CAT, §3.6), DefaultSmall (2 of 6), dirtfulcat (3 of 6).

  The cap wins all 6 games on arrows, evileye, starvation and whereisthecheese, with the same 8 against 11–12.
- **Seat-decided king walks.** **c086A dirtpassageway** (r158): our king walks from (3,26) toward the centre and dies
  at (18,22). In c086B awubot, as seat A, walks the same way and dies at r153 at (16,22). Seat A loses all 6 games.

### 3.6 Cats

- 15 losses: tminusten 2, bed 3, directconfrontation 2, closeup 2, one each on hellofromtheotherside, peaceinourtime,
  EscapeTheNight, hatefullattice, squaresaremodernart, therearenowalls.
- **c173A tminusten** (r107): every game on this map ends by r107 with a king scratched to death. The seat-A king
  (spawn (8,1)) walks right along the bottom edge to (12,2) and is scratched from r20; the seat-B king retreats to its
  corner (1,46) and loses 140–200 HP by r100. As seat A we lose 2 of 3; awubot as seat A loses c173B the same way.
- **c166B bed** (r240): our seat-B king walks from (18,17) down to the corner (17,1), a dead end the cat visits. It is
  scratched from r170 to its death at r240. Passes 1 and 2 are lost by seat B for both bots (awubot's B king dies to
  the cat in c166A and c271A), pass 0 by seat A. This is K2's dead-end finding (K2 memory: bed, externalsustenance,
  EscapeTheNight).

### 3.7 What is no longer the problem

- Combat micro: kills per game favour us in the games we lose (above). CX's baseline found the same ("in lost games we
  still kill more rats: kings decide those").
- Own code: no SELF loss, no own-exception death (S1, S2, S3).
- Spending: E1's arms closed it (docs/closed.md).

---

## 4. The ceiling: split and lost-both cells

- **The cell counts are what chance gives.** Independent games at p = 0.786 give 176 / 96 / 13 cells won both /
  split / lost both; the run has 177 / 94 / 14. The maps are a little more uneven than that: 29 maps lose no game
  (22 expected) and 5 lose ≥ 4 of 6 (2 expected: groveisbeingdesecrated, diamond, asabovesobelow, worldshardestgame,
  houseofcards).
- **The seat.** In W0.3's mirror the seat decided every cell, so each cell has a favoured seat. Our win rate is
  **0.832** from the favoured seat and **0.740** from the other. In the split cells our loss is on the mirror's losing
  seat in 60 of 94 (z = 2.7). If a fraction f of split cells is seat-decided and the rest are coin flips,
  f = 2·60/94 − 1 ≈ 0.28: about 26 cells. Seats A and B lose equally (47 / 47).
- **Seat-decided maps** (all ≥ 2 losses in one seat, `L1-seat`):
  - peaceinourtime (A, 3 of 3) and TheHeist (A, 2): cooperation endings, §3.3.
  - EscapeTheNight (B, 3 of 3): the seat-A side has transferred 7× more by r300 and forms a second king near r300.
    Hike (B, 2): the seat-A side forms a second king at r757 and ends with 10× our transferred cheese. Both bots win
    from A.
  - dirtpassageway (A, 3 of 3): the A king walks into the centre (§3.5).
  - tminusten (A, 2): the A king's cat lane (§3.6).
  - popthecork, isnotgold, uneruesansfin, trapped (one seat, 2 each); bed (B, 2 of 3).

  Both bots lose these from the bad seat, so a fix must play that seat differently. The others are chance, which only
  a larger edge removes.
- **The 14 lost-both cells:**

| cell | map | W0.3 class | seat A | seat B | what is wrong |
|---|---|---|---|---|---|
| 60, 198 | groveisbeingdesecrated | POINTS, STARVE | POINTS r425, r264 | POINTS r282, r243 | cooperation endings we close (§3.3) |
| 89, 114 | diamond | POINTS | POINTS r2000, r2000 (49–50) | COMBAT r1579, POINTS r2000 | awubot's centre king, our income (§3.2) |
| 90 | RUN | POINTS | POINTS r2000 46–53 | POINTS r2000 48–51 | cat damage 4,070 vs 5,480 and 6,130 vs 7,580; kings 2–2 |
| 144 | fullofsecrets | POINTS | POINTS 49–50 | POINTS 37–62 | cheese 6,585 vs 22,804 in B |
| 189 | cheesebottles | POINTS | POINTS 39–60 | POINTS 39–60 | cat damage 460–500 vs 2,010–2,870; kings 2–2 |
| 42 | mushroom | POINTS | STARVE r1228 | POINTS r2000 | awubot's extra king; our bank |
| 136, 248 | houseofcards | STARVE, COMBAT | COMBAT r227, r276 | C+s r269, COMBAT r217 | 8 rats against 12 under E2's cap; no income (§3.5) |
| 237 | thunderdome | COMBAT | COMBAT r166 | COMBAT r190 | banks crash; awubot's income restarts first (§3.2) |
| 32 | QueenOfHearts | COMBAT | COMBAT r1603 | COMBAT r295 | A: our bank 30 and 4 babies before the end; B: our bank empty at r280 |
| 155 | asabovesobelow | COMBAT | C+s r714 | COMBAT r112 | awubot 40 babies and 3 kings vs our 6 and 1 at r664 |
| 138 | worldshardestgame | STARVE | STARVE r654 | STARVE r722 | bank race under 8 cats (§3.2) |

  Eight of the 14 are points or cooperation cells. Three are small-map fights decided by income (houseofcards ×2,
  thunderdome).

---

## 5. Packages, ranked by expected gain

Each is derived from a mechanism above. "Flips" are games of the 570, expected over the run's games. The pools are in
`~/battlecode26-data/games/pools/` (§6).

| rank | id | change | evidence | flips | test | risk |
|---|---|---|---|---|---|---|
| 1 | **PC1** | Last-cat rule. In cooperation, our rats stop damaging a cat that may be the last one alive once its HP is low enough to die to the next bites, unless our transferred cheese is clearly ahead. They deliver raw cheese instead. Arm B: one deliberate backstab bite on an enemy rat just before our final blow, which keeps the game going under backstab weights. Build it on KB's cooperation king, which targets the same maps. | §3.3: 10 of 12 cooperation losses ended by our own final damage while behind on cheese; 5–2 when awubot ends it; points at the instant (`E:world/GameWorld.java:911`, `:1181`). Kings were equal in 9 of the 12, so KB's cooperation king (+0.21 on the coop maps) converts some of the same games first. | +4 to +7 of the 10 on today's main, fewer after KB's arm; −1 of the 4 wins we ended. +0.4 to +1.2 pp | gain on `L1-coop` (12 cells) at δ 0.10, against a main that holds KB's cooperation king; harm on `goal`. Diagnostic: who deals the last cat damage (`D:LastCat.java`), cheese share at the end. | low. Fires only in cooperation near a cat's death. A cat held at low HP may wander into our rats or king. Arm B changes the scoring and lets awubot trap cats for 100 rounds (R§2). |
| 2 | **MC** (new) | Centre-mine presence on rich maps in r150–600: a fixed share of rats holds the boxes of the centre mines, and miners keep our own-side mines picked. Study first: why 943 cheese a game sits unpicked on our side in rich-map losses (rat positions by role from replays), then one arm. | §3.2: centre share 0.36 vs 0.57; presence 491 vs 1,081 rat-turns at the centre in losses; 39% vs 48% of our rat-turns at mines. Income share at r500 decides the long games. | `L1-econ` holds 69 losses; moving the centre share to 0.5 in a third of them: +10 to +15 if it works. +1 to +3 pp, sign uncertain | gain on `L1-econ` (62 cells) at δ 0.08, then `goal`. Diagnostics: centre share and own-side unpicked by r600, our rats' kills per game (must not fall). | high. E3's four miner-targeting arms lost (docs/closed.md); CC+CK showed that rats moved to one job leave another. Presence is untested. |
| 3 | **E2b** (E2's later arm) | On maps under E2's cap, lift the cap back to awubot's once we see a larger enemy army near our king or mines (attrition), not from the map. | §3.5: houseofcards 4 of 6 lost with 8 rats against 12 and almost no income; the cap wins 6 of 6 on eight other gated maps, so a map gate cannot separate them. | houseofcards, DefaultSmall, bed, dirtfulcat, Excavation: +3 to +5. +0.5 to +0.9 pp | gain on `L1-early` (23 cells) at δ 0.10; harm on `mines-le4` (E2's gain). | medium. It can give back E2's measured gain. |
| 4 | **KS** (running) and K-seat | The king does not leave a safe home for a mine whose path a cat or the enemy controls. tminusten: the king walks along the edge into the cats. dirtpassageway: into the centre. bed: into the cat's dead end. | §3.5, §3.6, §4: 8 losses on these 3 maps, all in the seat that both bots lose. K2: parked kings take 82 HP of scratches per 1,000 king-rounds, against 379–557 at 2–8 tiles from a mine. | +2 to +4. +0.4 to +0.7 pp | `L1-seat` (33 cells) and `L-cat`. | medium. Parked kings mine less (KS, K2). |
| 5 | **KB-centre** (after MC) | Second king at a centre mine cluster on maps with ≥ 8 mines, once our centre income can feed it. | §3.2: awubot's extra kings sit at centre mines in 54% of our losses (34% of wins); 0.61 when awubot forms first, 0.85 when we do. That is correlation: awubot forms them once it is rich. KB's early-king round arms starved (H0 ×4, KB memory) and KL's kings beside ours starved. | r2000 with fewer kings 8, part of snowball 21: +2 to +5. +0.3 to +0.9 pp, only after MC | KB's diagnostics plus the centre share (`D:mcan.py`), on `L1-econ`. | high: a second king without the centre income starves (KB, KL). |

**The plan's wave-3 list against this evidence** (plan §4.3):
- **PC**: yes, as PC1 above. Its points estimator (slot 15) matters for the "unless clearly ahead" gate. In the
  cooperation losses cat damage was always equal and kings equal in 9 of 12: the cheese share and the king count are
  the open terms, and KB's cooperation king takes the second.
- **XA**: lure cats onto awubot's king. The cat races (tminusten) are seat-decided, and K2's squeak arms showed that
  steering is not what scratches kings. Low.
- **IS**: no loss traced to a wrong enemy-king guess. Low.
- **CH**: cut 1–2 maps lose 0.40, but those losses are cooperation and points endings (grove, cheesebottles, bed),
  not choke fights. Low.
- **BL**: no rat traps or first bite in cooperation. Our cooperation losses already place no rat traps (0 a game).
  Its "keep cooperation" direction is the opposite of PC1's arm B. Not supported.
- **QD, JS**: combat search. Combat is not what loses now (§3.7). Low.
- **AB** (running): ablations. Of the three features, "eternal exploration" bears on MC's unpicked own-side cheese.

**The packages running now:**
- **KB**: its cooperation king (passed, +0.21 on the coop maps) fits §3.3: kings were equal in 9 of the 12
  cooperation losses. Its early round arms starved; §3.2 does not reopen them, because awubot's centre kings follow
  its centre income. A centre site waits for MC (rank 5).
- **PT**: the r2000 losses move with the army and the kings, not delivery (§3.4). The cooperation lever is PC1's,
  not PT's delivery deadline.
- **KS**: supported on the seat-decided king walks (rank 5).
- **CX**: no evidence for more threat modelling; we out-trade awubot in the games we lose.
- **CC+CK, E1**: closed. Nothing here reopens them.

**What 90% needs.** The five packages give about +3 to +6 pp: 0.82–0.85. Reaching 0.90 means cutting the losses from
122 to 57. Rich maps hold 75 of them at 0.755; at 0.90 there, the total would be about 0.88. The remaining gap is the
centre-mine economy of rich maps (MC, then a centre king). Nothing measured yet shows a lever of that size.

---

## 6. Data, tools and pools

- `D:` = `~/battlecode26-data/scratch/w1/`, all compiled against `~/battlecode26-data/engine/jars/battlecode26-java-1.2.6.jar`
  into `D:cls/`:
  - `w1_classify.py RUN_DIR` → `games.tsv`: one row per game (class, HP split, points, kings, cheese, cat damage, kings
    formed). Needs `stats.jsonl` (T4 ReplayStats), `kingself.txt` (synthesis/KingSelf) and `kt/` (T4 KingTrace).
  - `W1Trace.java` → `trace.jsonl`: per game, a 10-round series per team (global cheese from
    `teamAliveRatKings / 10`, `E:world/GameWorld.java:1013`; babies; kings; transferred; cat damage; rat traps; kings'
    HP; babies near each king), deaths by cause, spend counts, and every king's life.
  - `tl.py ID [EVERY] [FROM] [TO]`: one game's timeline, ours first.
  - `summ.py CLASS`, `mech.py` (mechanism groups), `mcan.py` (mine-side pickups).
  - `MineControl.java` → `mc.jsonl`: per game and team, cheese picked and rat-turns spent at each mine by window.
  - `LastCat.java`: which team dealt the last cat damage.
- Pools, one `map:seed` a line:
  - by class: `L1-points` (35 cells), `L1-combat` (29), `L1-starve` (26), `L1-cat` (15), `L1-combat-starve` (7);
  - by mechanism: `L1-coop` (12), `L1-econ` (62), `L1-early` (23), `L1-seat` (33).
