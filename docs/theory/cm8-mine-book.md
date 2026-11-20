# CM8: the map's mines from round 1

Package CM8 (wave 6, the book). Main `7ea436d9` (book 8) against awubot. Data and tools: `~/battlecode26-data/scratch/cm/`
(`D:`; `mt/MineTiming.java` the per-mine reader, `mt/mtab.py` its table). "Seat A/B" is our seat.

## 0. Summary

`Tune.CM_MINE_BOOK` 1 with the book datum `CM_MINES` (every mine of the map): baby rats know the map's mines from their
first turn. Chosen on seed base 23060 (`book matrix cm8a`, 40 cells a map, 4 candidates), confirmed on 23062
(`1120-153033-ab-7908`, 200 games a map-seat, arm `t3/cm8-carm` 70390668 against ref `t3/cm8-cref` 5ad323b1 = main
`7ea436d9` + book 9's waiting lines, jail A's `KING_PATH` 1 among them). The lines: `D:entries8.jsonl`.

| map | seat | entry | chosen (cm8a: best, raw, p_max) | confirmed (23062) |
|---|---|---|---|---|
| jail | A | `CM_MINES`, `CM_MINE_BOOK` 1, `EXPLORE_P` 0.2 | mbexp2 +0.275 [+0.13, +0.43], 0.002 | +0.215 [+0.15, +0.28] (46/3) |
| jail | B | the same | mbexp2 +0.250 [+0.12, +0.39], 0.002 | +0.155 [+0.10, +0.21] (33/2) |
| moonlitriverpark | A | `CM_MINES`, `CM_MINE_BOOK` 1 | mb +0.275 [+0.13, +0.42], 0.000 | +0.195 [+0.13, +0.26] (46/7) |
| moonlitriverpark | B | the same | mb +0.075, same value as A (p_max 0.21) | +0.110 [+0.06, +0.16] (27/5) |
| shell | A | `CM_MINES`, `CM_MINE_BOOK` 1 | mb +0.275 [+0.12, +0.43], 0.003 | +0.100 [+0.02, +0.18] (43/23) |
| shell | B | `CM_MINES`, `CM_MINE_BOOK` 1, `EXPLORE_P` 0.2 | mbexp2 +0.225 [+0.08, +0.37], 0.012 | +0.130 [+0.07, +0.19] (31/5) |

Failed (same value on both seats, `CM_MINE_BOOK` 1): AceOfSpades A +0.005 [−0.03, +0.04], B +0.025 [−0.05, +0.10];
asabovesobelow A −0.005 [−0.03, +0.02], B −0.005 [−0.10, +0.09]. Nothing chosen on cheeseysquare, mushroom,
holeinthewall, therainbow. `EXPLORE_P` alone (0.2, 0.4) passes nowhere; whether jail and shell B need it with the mines
is not resolved (screen: mb alone +0.25 / +0.20 / +0.175 against mbexp2 +0.275 / +0.25 / +0.225): the lines are what
was confirmed.

## 1. What the income gap is made of

Main's losses on the income-race map-seats (CM6's baseline `1120-120705-ab-9ad1`, seed base 23053, and
`1120-132005-ab-1491`, seed base 23059, for hellofromtheotherside, Meow, mushroom, EscapeTheNight), read by
`D:mt/MineTiming.java` (MineControl with windows r0–100, 100–200, 200–300, the round of the first pickup, the distinct
rats picking in r0–300, rat-turns within Chebyshev 4 and rats dying there in r0–300). Each mine on our side (nearer our
starting king), nearest first, against awubot at its mirror mine on its side (`D:mt/mtab.py`). The columns: first pickup
(median round, never = 2000), cheese picked by r300, distinct rats picking in r0–300, our value | awubot's.

| map-seat (losses) | own mines, nearest first: first pickup; picked by r300; rats | reading |
|---|---|---|
| AceOfSpades B (16) | (2,29) never \| r179, 89 \| 285, 1.2 \| 3.6; (2,37) 45 \| 42, 324 \| 459, 4.4 \| 5.7; (2,21) never \| r123, 42 \| 305, 0.5 \| 4.5; (2,13) 52 \| 48, 74 \| 430, 1.2 \| 6.4; (2,4) 50 \| 51, 222 \| 176 | (a) two own mines never started, (b) one worked by a fifth of the rats |
| asabovesobelow B (22) | (14,31) 81 \| 82, 352 \| 505, 5.3 \| 6.5; (18,36) 122 \| 112, 79 \| 210, 1.5 \| 3.5; (9,37) never \| r352, 55 \| 152, 0.6 \| 1.6 | (b), (a) at the farthest |
| cheeseysquare A (13) | (3,20) 18 \| 20, 643 \| 638, 12.1 \| 13.4; (15,13) 58 \| 73, 231 \| 262; (20,13) 77 \| 43, 106 \| 313, 4.1 \| 6.5; (25,13) 116 \| 42, 141 \| 451, 2.5 \| 8.2 | (a) and (b) at the farther two |
| mushroom A (11) | (25,7) 46 \| 40, 251 \| 616, 5.7 \| 13.4; (22,16) 94 \| 82, 131 \| 236; (27,17) 870 \| 131, 5 \| 102, 0.2 \| 1.5 | (b) at the nearest, (a) at the farthest |
| mushroom B (9) | (34,7) 41 \| 32, 388 \| 459; (37,16) 91 \| 99, 118 \| 373, 3.1 \| 6.6; (32,17) 507 \| 130, 11 \| 198 | (b), (a) |
| moonlitriverpark A (11) | (15,25) 14 \| 15, 594 \| 573; (5,3) 53 \| 49, 214 \| 430, 2.9 \| 5.2 | (b) at the far mine |
| shell A (14) | (17,26) 28 \| 29, 314 \| 315 (dead 4.6 \| 5.1); (47,21) 60 \| 60, 294 \| 457, 3.9 \| 6.8; (2,38) 38 \| 34, 400 \| 447 | (b) at (47,21) |
| jail A (16) | (8,36) 22 \| 21, 366 \| 551, 6.1 \| 8.5; (52,35) 28 \| 30, 319 \| 361, 4.7 \| 5.9 | (b) |
| holeinthewall A (15) / B (8) | nearest: 427 \| 559, 13.5 \| 16.1 / 356 \| 547, 11.9 \| 17.1 (dead 5–7 both); third: 253 \| 305, 4.0 \| 5.4 / 282 \| 395, 3.5 \| 7.1 | (b), with the exchange at the nearest mine |
| therainbow B (13) | (22,3) 17 \| 14, 279 \| 548, 8.5 \| 15.1, dead 14.8 \| 8.2; (36,18) 409 \| 506, 6.4 \| 11.5 | (c) at the contested near mine, (b) |
| QueenOfHearts B (17) | (23,36) 669 \| 700, 14.0 \| 16.5; (17,34) 615 \| 512; (29,40) 505 \| 606 | (d): even income, starves later |
| EscapeTheNight B (7) | (40,37) 88 \| 118, 560 \| 526, 4.9 \| 5.0 | (d): no gap at our mine |
| hellofromtheotherside A (11) | four mines, first 8–22 both, picks 529–603 \| 554–591, rats 10–17 \| 12–18 | (d) |
| Meow B (2) | equal | (d) |

Both teams start their mines at the same rounds almost everywhere: (a) shows only at a few far own mines (AceOfSpades B,
asabovesobelow B, cheeseysquare A, mushroom). The common gap is (b): at our farther own mines fewer distinct rats work
than awubot puts on the mirror mine, by a factor of 1.5 to 7. Main sends 60% of a miner's trips to exploration once
mines are known (`Tune.EXPLORE_P`, docs/strategy.md §2; awubot 20%), and draws the mining trips over all known mines with
a weight of the distance from the king (`MinerStrategy.pickSeenMine`), the enemy side's included (E3: dropping those
loses, far trips hold the front). (c) appears at contested near mines (therainbow B, holeinthewall's nearest), (d) on
QueenOfHearts B, EscapeTheNight B, hellofromtheotherside A and Meow B.

## 2. The switch

What main does today to spread its miners (`robot/rat/MinerStrategy.java`): a baby rat explores only while it knows no
mine, or at most 2 before round W·H/8; after that each trip explores with probability `EXPLORE_P` (0.6) and otherwise
goes to a known mine drawn with weight √d² from the nearest king (beside the king: every mine it does not see, no mine
a king stands on), the enemy side's included. A rat learns mines by seeing them or from a squeak.

`Tune.CM_MINE_BOOK` (0 in main) with the book datum `CM_MINES` (tiles, every mine of the map from its file): at its first
turn every baby rat adds the map's mines to `Game.knownCheeseMines` (`MineBook.load`, called from `RobotPlayer` after
`GlobalArray.init`). The opening's explore-only phase ends at once and trips draw from the whole list with main's
weighting, enemy side included (E3's arms that dropped those mines lost: far trips hold the front). Kings load nothing:
their mine choice and the formations' mine counts stay main's, and rats squeak a mine when they first see its tile,
known or not, so kings learn mines as in main.

- **Default plays as main:** `D:bccmp.sh` (javap of every class, branch against main built in a worktree): only `Book`
  (the new null field), `MineBook` (new, never called) and `Tune` (the new constant) differ; no method of main changes.
- **Bytecode with the load on** (`1120-132530-ab-e4d4`, asabovesobelow and AceOfSpades, the most mines, 10 cells):

  | turn | main mean / max | load on mean / max | limit |
  |---|---|---|---|
  | king, r1 | — | 16,583 / 17,883 | 20,000 |
  | baby, first turn (r2) | 14,391 / 15,145 | 15,250 / 16,333 | 17,500 |
  | baby, r3 | 12,044 / 14,677 | 12,774 / 16,627 | 17,500 |

  No first turn overruns. Baby overruns by window: r1–3 none, r4–50 0.023%, r51–300 0.052%, after r300 0.256%: they
  come from later play, not from the load.

## 3. Screen

`book matrix cm8a`, branch `t3/cm8-trial` (`CM_MINES` on the nine maps below), main `7ea436d9` (book 8) with its
entries on, 40 cells a map, seed base 23060 (ref `1120-132515-ab-cbf0`, exp2 `-f443`, exp4 `-9db6`, mb `-49e5`,
mbexp2 `-4ded`). Candidates: mb `CM_MINE_BOOK` 1; mbexp2 `CM_MINE_BOOK` 1 + `EXPLORE_P` 0.2; exp2 `EXPLORE_P` 0.2;
exp4 `EXPLORE_P` 0.4.

| map-seat | best | adj | p_max | mb | mbexp2 | exp2 | exp4 |
|---|---|---|---|---|---|---|---|
| shell A | mb +0.275 [+0.12, +0.43] | +0.23 | 0.003 | +0.275 | +0.250 | +0.150 | +0.150 |
| moonlitriverpark A | mb +0.275 [+0.13, +0.42] | +0.23 | 0.000 | +0.275 | +0.250 | 0 | 0 |
| jail A | mbexp2 +0.275 [+0.13, +0.43] | +0.22 | 0.002 | +0.250 | +0.275 | +0.050 | −0.025 |
| jail B | mbexp2 +0.250 [+0.12, +0.39] | +0.20 | 0.002 | +0.200 | +0.250 | +0.075 | −0.025 |
| shell B | mbexp2 +0.225 [+0.08, +0.37] | +0.19 | 0.012 | +0.175 | +0.225 | +0.075 | +0.150 |
| therainbow A | mbexp2 +0.125 | +0.09 | 0.085 | +0.100 | +0.125 | +0.050 | +0.050 |
| AceOfSpades A / B | mb +0.100 / mbexp2 +0.075 | | 0.13 / 0.59 | +0.100 / +0.050 | | | |
| asabovesobelow A / B | mb +0.125 / mbexp2 +0.050 | | 0.25 / 0.65 | +0.125 / +0.025 | | | |
| moonlitriverpark B | exp2 +0.075 | | 0.21 | +0.075 | | | |
| cheeseysquare, mushroom, holeinthewall, therainbow B | ≤ +0.075 | ≤ +0.01 | ≥ 0.56 | | | | |

The book's mines carry the gain: `EXPLORE_P` alone passes nowhere. Picks (p_max < 0.05): shell A mb, moonlitriverpark A
mb, jail A and B mbexp2, shell B mbexp2; on both seats with mb positive (the same value): moonlitriverpark B,
AceOfSpades, asabovesobelow. It does nothing on cheeseysquare, mushroom, holeinthewall and therainbow B, where (a)/(b)
showed too, or (c) did.

## 4. Confirmation

`1120-153033-ab-7908`, seed base 23062, 200 games a map-seat, every replay kept (a first launch, `1120-133712-ab-cfe7`,
was stopped unplayed when Condor's submit host went silent). The arm puts `CM_MINES` + `CM_MINE_BOOK` 1 on shell,
moonlitriverpark, jail, AceOfSpades and asabovesobelow (both seats), `EXPLORE_P` 0.2 on jail and shell B. Overall
+0.092 [+0.072, +0.113] (paired, `D:msdiff.py`); per map-seat in §0.

**Income** (`D:mt/c8inc.py` over `D:mt/mt_c8.jsonl`, all games): cheese picked at mines in r0–300, ours | awubot's,
distinct rats picking (summed over mines), wins.

| map-seat | ref | arm |
|---|---|---|
| jail A | 1,218 \| 1,010, 20.5 rats, 0.765 | 1,538 \| 800, 31.2 rats, 0.980 |
| jail B | 1,285 \| 898, 20.1, 0.825 | 1,543 \| 736, 30.0, 0.980 |
| moonlitriverpark A | 1,272 \| 1,056, 27.0, 0.770 | 1,581 \| 943, 33.7, 0.965 |
| moonlitriverpark B | 1,296 \| 1,063, 27.5, 0.855 | 1,568 \| 950, 33.5, 0.965 |
| shell A | 1,892 \| 1,487, 33.5, 0.780 | 2,173 \| 1,434, 37.9, 0.880 |
| shell B | 1,900 \| 1,543, 34.0, 0.840 | 2,376 \| 1,367, 44.6, 0.970 |

The gain is step 1's (b): more rats on more mines, and awubot picks less, since our rats now work the mines it took.

**First-turn bytecode** (`D:bcf/BcFirst.java`, each robot's first turn; `D:bcf/firstmap.sh`, arm | ref on the same
cells): mean, max, overruns (baby limit 17,500, king 20,000).

| map (mines) | king r1 | baby born r1–3 | baby born later |
|---|---|---|---|
| jail (4) | 15,773 / 16,361 \| 15,220 / 16,310 | 15,545 / 17,158 \| 15,931 / 17,117, 0 | 0.080% \| 0.030% (15 of 18,831 \| 7) |
| moonlitriverpark (4) | 17,799 / 17,803 \| 15,892 / 15,897 | 14,370 / 15,670 \| 14,530 / 16,279, 0 | 0 \| 0.022% |
| shell (6) | 16,762 / 17,354 \| 16,077 / 16,102 | 15,923 / 17,057 \| 15,505 / 17,063, 0 | 0.147% \| 0.104% |
| asabovesobelow (13) | 15,311 / 15,834 \| 15,190 / 15,713 | 13,722 / 15,528 \| 13,310 / 14,832, 0 | 2.54% \| 0.82% |
| AceOfSpades (16) | 17,625 / 17,893 \| 16,612 / 17,211 | 15,492 / 16,679 \| 14,536 / 15,829, 0 | 9.98% \| 1.97% (3,159 of 31,642 \| 765) |

Kings load nothing; their r1 rise on moonlitriverpark and shell is the book's identity scan, which a map-seat without
entries in the ref skips. A rat born later pays the load (early rats' first turns rise by 400–950 on the rich maps) on a first
turn that already averages ~15k: on the two rich maps it overruns 4–5 times as often as main's; on the three confirmed maps the difference is under
0.05 pp. Whole games, baby turns of all five maps: 0.070% overruns against the ref's 0.048%.
