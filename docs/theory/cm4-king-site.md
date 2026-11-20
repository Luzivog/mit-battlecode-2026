# CM4: the first king's site as book data

Package CM4 (wave 6, the book). Main `9d740112` / `31f0a2aa` (book 6) against awubot. Data and tools:
`~/battlecode26-data/scratch/cm/` (`D:`; `king1.py` the candidate sites, `entries4.jsonl` the lines, `runs.txt`).
"Seat A/B" is our seat.

## 0. Summary

On the maps where we lose the income race, the first king's site decides games: seated from round 1 beside the right
mine it wins +0.12 to +0.26 a map-seat, where a second king sent to the same mine (CM2, AT3) lost −0.2 to −0.5. Holding
the start corner, as awubot's king does, loses on most map-seats (−0.25 to −0.70).

| map | seat | site (`KING1_SITE`, `CM_KING1_SITE` 1) | chosen from (4 candidates a map-seat) | confirmed |
|---|---|---|---|---|
| kingofthehill | A | (25,17), the contested mine on our side | `cm4a`, 23039, 60 cells: +0.25 [+0.10, +0.40], adj +0.19, p_max 0.005 | 23040 `1120-075509-ab-9b9c`: +0.18 [+0.07, +0.29] (26/8) |
| kingofthehill | B | (34,17), contested | `cm4a`: +0.32 [+0.15, +0.48], adj +0.25, p_max 0.001 | 23040: +0.26 [+0.16, +0.36] (30/4) |
| therearenowalls | A | (32,8), contested | `cm4a`: +0.28 [+0.13, +0.44], adj +0.225, p_max 0.001 | 23040: +0.22 [+0.13, +0.32] (25/3) |
| therearenowalls | B | (22,8), contested | `cm4a`: +0.20 [+0.07, +0.33], adj +0.15, p_max 0.015 | 23040: +0.21 [+0.12, +0.30] (23/2) |
| QueenOfHearts | A | (11,8), our nearest mine | `cm4a`: +0.32 [+0.16, +0.47], adj +0.27, p_max 0.000 | 23040: +0.20 [+0.11, +0.29]; on top of SG's `KING_PATH` 1, 23042 `1120-080105-ab-eab8`: +0.12 [+0.04, +0.20] (15/3) |
| shell | B | (42,17), our nearest mine | `cm4b`, 23041, 40 cells: +0.325 [+0.14, +0.51], adj +0.26, p_max 0.005 | 23043 `1120-080421-ab-a33d`: +0.15 [+0.04, +0.26] (24/9) |
| AceOfSpades | A | (36,23), the middle of our mines | `cm4c`, 23044, 40 cells: +0.225 [+0.07, +0.38], adj +0.19, p_max 0.019 | 23046 `1120-082624-ab-6edd`: +0.08 [+0.003, +0.157] (12/4) |
| therainbow | A | (17,3), our nearest mine, on top of its second-king lines | `cm4c`: +0.20 [+0.03, +0.37], adj +0.15, p_max 0.044 | 23046: +0.08 [+0.003, +0.157] (12/4) |
| gelcatinouscube | B | (10,8), the middle of our mines | `cm4b`: +0.15, p_max 0.106 (did not pass); 23043: +0.16 [+0.09, +0.23] (16/0) | 23045 `1120-081449-ab-8fc4`: +0.15 [+0.07, +0.23] (16/1), on top of its second-king line |

## 1. The switch and the datum

- `Tune.CM_KING1_SITE` (0 main; k > 0): the first king (the one alive at round 1) takes the k-th tile of the book datum
  `KING1_SITE` (a tile list; the last when k exceeds it) as its mine target from round 1 and keeps it
  (`robot/king/KingMiningStrategy.runAtSite`): it walks there, collects the cheese within Chebyshev 4 of it and walks
  back, as main does at the mine it picks. Kings formed later keep main's choice.
- "Stay": the cat flee (`KingCombatStrategy`) and the formation coordinator run before mining every turn as in main;
  the king returns to its site afterwards. S3's liveness mark and KS's reach rules are untouched (the site is a
  reachable centre by construction).
- At 0 the switch adds members and changes no existing instruction (javap against main); a matrix screens several
  sites of one map-seat by k alone.
- Candidates from the map file (`D:king1.py`): 1 the start; 2 beside our mine nearest by king steps; 3 beside the
  contested mine on our side (smallest margin of enemy steps over ours); 4 the reachable centre nearest the centroid of
  our side's mines. A site is a reachable 3×3 centre (walls block, dirt does not).

- Round 1: the first path to the site waits for round 2 (`23e2465c`). With the path in round 1 the king's first turn
  hit the 20,000 limit on 7 of the 9 booked map-seats (MB, `1120-084751-ab-9a8f`) and spilled into round 2. Paired
  fixed against present code, the 18 lines in both, seed base 23048, 100 games a map-seat (`1120-085527-ab-541e`):
  kingofthehill A +0.06, B −0.03, therearenowalls A −0.05, B +0.02, shell B +0.03, gelcatinouscube B −0.01,
  AceOfSpades A −0.06 (every interval across 0; mean −0.006). The first king's bytecode on those map-seats (BcStats,
  700 games each): present code round 1 20,003 (overrun in 700 of 700), round 2 1,342 (the spill), round 3 13,250;
  fixed round 1 16,381 (max 17,207, no overrun), round 2 12,735, round 3 13,431.

## 2. Screens

`tools/book/book matrix` over a trial branch holding the candidates as `KING1_SITE` (never merged), the points
`CM_KING1_SITE` 1–4, read by `book fit`.

| screen | maps, seed base, cells a map | map-seats at p_max < 0.05 | runs (ref first) |
|---|---|---|---|
| `cm4a` | kingofthehill, therearenowalls, QueenOfHearts; 23039; 60 | 5 of 6 (QueenOfHearts B: none, best +0.02) | `1120-074340-ab-3489`, s1 `-e23a`, s2 `-d7e8`, s3 `-6aec`, s4 `-5dc5` |
| `cm4b` | shell, jail, moonlitriverpark, cheeseysquare, gelcatinouscube, asabovesobelow, DefaultSmall; 23041; 40 | shell B; leads jail A (+0.20, p 0.12), gelcatinouscube B (+0.15, p 0.11) | `1120-075548-ab-587d`, s1 `-d339`, s2 `-7581`, s3 `-0fbd`, s4 `-88fe` |
| `cm4c` | hatefullattice, dirtpassageway, holeinthewall, AceOfSpades, trapped, constrictinglattice, therainbow; 23044; 40 | AceOfSpades A (36,23) +0.225, p 0.019; therainbow A (17,3) +0.20, p 0.044 | `1120-080527-ab-0040`, s2 `-3caf`, s3 `-9b54`, s4 `-aa5f`, s1 `-ab4d` |

- The start corner (site 1) loses most where it is not neutral: kingofthehill −0.25 / −0.27, QueenOfHearts B −0.25,
  jail −0.30, DefaultSmall −0.65 / −0.70, holeinthewall −0.55 / −0.675, trapped −0.50 / −0.55.
- jail A (8,36) fails confirmation: +0.06 [−0.04, +0.16] (23043).
- Not chosen (no candidate at p_max < 0.05): QueenOfHearts B, shell A, jail B, moonlitriverpark, cheeseysquare,
  asabovesobelow, DefaultSmall, hatefullattice, dirtpassageway, holeinthewall, trapped, constrictinglattice,
  gelcatinouscube A, AceOfSpades B, therainbow B.

## 3. Is there a general form?

Over every map-seat the four screens tested (`cm4a`–`cm4c`, `cm5a`: 49 maps, 98 map-seats; `D:sitetab.py`, data
`D:sitetab.json`), each candidate's paired effect against main on the same cells, by site type (lists with fewer than
four candidates count each type once):

| site type | map-seats | mean effect | 95% (bootstrap over maps) | share > 0 |
|---|---|---|---|---|
| start (hold the start corner) | 98 | −0.175 | [−0.246, −0.106] | 0.27 |
| beside our nearest mine | 90 | +0.004 | [−0.033, +0.037] | 0.53 |
| beside the contested mine on our side | 65 | −0.086 | [−0.154, −0.019] | 0.35 |
| nearest the middle of our mines | 69 | −0.044 | [−0.105, +0.012] | 0.45 |

No type reads above 0 across maps; the best of the four per map-seat averages +0.054 (chosen, biased up). Split by what
the king sees at round 1 (king steps from the start to the nearest mine ≤ 8 / 9–16 / > 16, whether that mine is nearer
to us than to them, size, symmetry, cats ≤ 2 / > 2, mines ≤ 6 / 7–12 / > 12), no split moves the start, contested or
middle types above 0. The nearest-mine site reads +0.051 [+0.008, +0.093] (36 map-seats, 0.64 positive) where the
nearest mine is more than 16 king steps away, and +0.061 [+0.022, +0.118] (9) where it is nearer to them than to us:
the best two of 24 splits looked at, so unconfirmed. Main's own king (toward the centre, then the nearest visible mine)
already stands close to the "nearest mine" type on average; the gains come from map-seats where one particular mine
decides the income race, which no round-1 feature here names.

## 4. CM5: the other maps and the near misses

Screens on the fixed code's base (the 32 most-lost maps not screened in CM4, PM's four left out; then neighbours of
CM4's near misses: the site itself, the second-nearest own mine, the second contested mine, three king steps toward
the enemy). Entries (`D:entries5.jsonl`), each confirmed against main on the map alone, 100 games a seat:

| map | seat | site | chosen from | confirmed |
|---|---|---|---|---|
| externalsustenance | B | (15,7), our nearest mine | `cm5a`, 23047, 40 cells, 4 candidates: +0.325 [+0.14, +0.51], adj +0.27, p_max 0.001 (`1120-084927-ab-5e5d` vs `1120-084755-ab-fcb5`) | 23049 `1120-114443-ab-c290`: +0.14 [+0.04, +0.24] (21/7) |
| thunderdome | A | (14,21), our nearest mine | `cm5a`: +0.175 [+0.05, +0.30], adj +0.15, p_max 0.011 | 23049: +0.12 [+0.06, +0.18] (12/0); 23051: +0.11 [+0.05, +0.17] |
| thunderdome | B | (32,13), the middle of our mines | `cm5a`: +0.15, p_max 0.058 (did not pass); 23049: +0.22 [+0.13, +0.31] (23/1) | 23051 `1120-115111-ab-e765`: +0.29 [+0.20, +0.38] (29/0) |
| gelcatinouscube | A | (26,34), the middle of our mines | `cm5b`, 23050, 60 cells, 4 neighbours: +0.18 [+0.08, +0.29], adj +0.17, p_max 0.002 (`1120-114707-ab-2ce3` vs `1120-114526-ab-e64e`) | 23052 `1120-115154-ab-0140`: +0.16 [+0.08, +0.24] (18/2) |

- `cm5a` (32 maps, runs `1120-084755-ab-fcb5` ref, s1 `-f266`, s2 `-5e5d`, s3 `-041a`, s4 `-5087`): only the two above
  pass; groveisbeingdesecrated B +0.125 (p 0.095), pocketsofcheese A +0.125 (0.135), the rest below. The start corner
  loses heavily where it is not neutral (DefaultMedium A −0.55, cheesefarm A −0.45, thehallway −0.33 / −0.43,
  uneruesansfin −0.70 / −0.80).
- `cm5b` (runs `1120-114526-ab-e64e` ref, n1 `-2ce3`, n2 `-a3b3`, n3 `-52dd`, n4 `-067b`): jail B's gain moves with the
  tile (the second contested mine (8,3) +0.20, p_max 0.003, against CM4's (52,4) +0.13) but does not confirm (+0.05
  [−0.04, +0.14], 23052); gelcatinouscube A's does not move (its CM4 site, the second-nearest mine and the tile toward
  the enemy read +0.18, +0.15, +0.18); holeinthewall, moonlitriverpark and jail A pass nothing.
- In the 23052 run gelcatinouscube B read −0.18 [−0.27, −0.09]: the arm lacked book 7's (10,8) line, the reference had
  it, a fourth read of that line (main with it 0.97, without it 0.79).
- Why it works where it does: through income. In the confirmation games the seated king's side delivers 130–300 more
  cheese by r300 than main on the same cells, and its margin over awubot grows by 170–300 (kingofthehill B +445 against
  +172 at r300, therearenowalls A +454 against +179, QueenOfHearts A +344 against +48, already +169 against −55 at
  r100). On kingofthehill and therearenowalls the r100 margin is slightly lower (the king walks to the contested mine)
  and the gain shows from r200: the miners' trips end beside that mine and the king picks its cheese itself.

