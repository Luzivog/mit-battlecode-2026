# CM: the maps where cats decide our losses

Package CM (wave 6, the book). Main `c6ab0ea8` / `c8c85e5` (same bot) against awubot. Source keys: `C:`
`src/challenger/`; `D:` data and tools in `~/battlecode26-data/scratch/cm/` (§5). "Seat A/B" is our seat.
Switches are 0/1 (or round) tune ints in `C:Tune.java` under `// CM`; per-map data are book data (`CAT_LANE`,
`KING2_SITE`, declared in `C:book.jsonl`).

## 0. Summary

Proposed book entries, each chosen on one fresh seed base and confirmed on another (switch on vs main, `map:` pool,
both seats; diff [95%], games better/worse of the confirmation). The entry lines: `D:entries.jsonl` (`D:mkentries.py`).

| map | seat | mechanism | entry | choose | confirm |
|---|---|---|---|---|---|
| bed | A, B | the king leaves its cat-free corner down the cats' corridor and is pinned at (17,1)/(18,1) | `CM_LANE_MEMORY` 1 | 23004 `1119-213857-ab-1fd0`: A +0.175, B +0.55 | 23008 `1119-221505-ab-0aaa`: A +0.25 [+0.17, +0.34] (25/0), B +0.56 [+0.45, +0.67] (60/4) |
| dirtfulcat | A | kings mine into cat lanes | `CM_LANE_MEMORY` 1 | 23004 same run: +0.11 | 23008 same run: +0.15 [+0.06, +0.24] (19/4) |
| dirtfulcat | B | as A; memory alone +0.03 to +0.06 | `CAT_LANE`, `CM_LANE_MEMORY` 1, `CM_LANE_FLEE` 1 | 23004 `1119-213916-ab-2c3b`: +0.29 | 23010 `1119-230504-ab-5b8e`: +0.21 [+0.11, +0.31] (25/4) |
| tminusten | A, B | every centre the king can reach is on a cat loop; it is pinned at r22 and dies at r101–117 with a bank of 2,000 and 16 babies; the backstab comes before r50, so the cooperation second king never forms | `CM_KING2_ROUND` 30, `KING2_SITE` A (12,15), B (12,34) | 23007 `1119-223519-ab-64a3`: A +0.475, B +0.30 | 23009 `1119-225939-ab-b3f0`: A +0.475 [+0.37, +0.59] (38/0), B +0.21 [+0.12, +0.30] (17/0) |
| peaceinourtime | A | awubot kills its own cat first (r363–487); ours kills our first king near r500; our own rats, traps and king then kill the last cat at 1 king to 2, and the cooperation ending scores kings 30 | `CM_KING2_ROUND` 30, `KING2_SITE` (11,31), (14,34), `PM_COOP_HOLD` 1 | 23009 `1119-230006-ab-646c`: +0.44; the hold over the second king alone on the same cells +0.075 [+0.01, +0.14] (7/1) | 23013 `1119-231831-ab-eddf`: +0.45 [+0.34, +0.56] (49/4) |
| sunandmoon | A, B | the king flees along cat 6's (5's) lane from the mine into its waypoint dead end (26,28) / (26,11) | `CM_KING2_ROUND` 30, `KING2_SITE` A (19,23), B (19,16), `CAT_LANE`, `CM_LANE_MEMORY` 1, `CM_LANE_FLEE` 1 | 23009 `1119-225957-ab-2215`: A +0.19, B +0.36 (the lanes over the second king alone: B +0.125 [+0.02, +0.23]) | 23012 `1119-231544-ab-1b88`: A +0.21 [+0.10, +0.32] (29/8), B +0.17 [+0.06, +0.29] (27/10) |
| RUN | A | ends on points at r2000 after the backstab; cat damage counts 30 | `CM_CAT_HUNT_START` 500 | 23014 `1119-231919-ab-a923`: +0.21 [+0.10, +0.32] (800: +0.10) | 23015 `1119-232514-ab-45ee`: +0.15 [+0.04, +0.26] (25/10) |

Off: peaceinourtime B loses with the second king (−0.11 to −0.18 in every run) and both seats with the lane book
(−0.25 A, −0.15 B); the lane book does not confirm on RUN A (+0.225 on 23004, +0.07 [−0.04, +0.18] on 23010); on
tminusten it changes no game over the second king alone.

Bytecode: the book's round-1 scan (1,913) fits the round-1 king turn on these maps (main's peak 15,973 on sunandmoon
B, 15,940 dirtfulcat; plus the lane table's setting and copy, ~700); off the book the switches cost a king 2
bytecodes once (the `Book.CAT_LANE` null test at its init).

## 1. Baseline

Main against awubot, seed base 23001, 80 cells a map (`1119-212725-ab-b1cb`, losses kept), read by KDStats and
CLPin (CL's tools):

| map | A | B | losses by class (A / B) |
|---|---|---|---|
| peaceinourtime | 0.41 | 0.975 | POINTS 29 + CAT 7 / 2 |
| tminusten | 0.50 | 0.85 | CAT 30 of 40 / 9 of 12 |
| bed | 0.74 | 0.34 | CAT-killed kings 20 of 21 / 46 of 53 |
| sunandmoon | 0.71 | 0.625 | about half CAT, half enemy bites |
| RUN | 0.65 | 0.90 | POINTS at r2000, backstab, 2 kings each |
| dirtfulcat | 0.78 | 0.78 | mostly enemy bites |

## 2. Switches

Each default leaves main's executed bytecode unchanged but for the `Book.CAT_LANE` test at a king's init:
`D:bccmp.sh REF` builds REF and the branch with gradle and diffs `javap -c` of every class. javac drops code under a
false compile-time constant in `if (C)` and `C ? a : b`, but `x || (C && y)` leaves a `goto`, so guards are written as
ternaries or `C != 0 &&`; a field initialiser runs in `<clinit>` and is avoided. A booked constant is no longer final
(tools/book/AGENTS.md), so each use is one read; a constant derived from another in `Tune`'s own initialiser would
keep its default (Book.apply writes after `Tune` loads), so there is none. Default harm check against main: `goal`,
100 cells, seed base 23011, 0 cells changed (`1119-230524-ab-8623`).

- `CM_LANE_MEMORY` (K2 D', `t3/rs-k2d`): `C:CatLanes` remembers every tile a cat's 2×2 was seen on; the king's mining
  takes no step that puts more of them under its 3×3, skips mines with a footprint in their 5×5 and cheese on one.
- Lane data (`CAT_LANE`, a book tileset): where the book sets it, `CatLanes` starts with the map's full cat loops (§3)
  at the king's init; with `CM_LANE_MEMORY` the mining rules use them from round 1.
- `CM_LANE_FLEE`: with a cat in view and lanes known, each of the king's 8 flee moves loses 4 per loop tile its step
  adds under the 3×3.
- `CM_KING2_ROUND` (2000 = off): from that round, with one king, form a second as the cooperation rule does (no rat
  gate, no known-mine threshold, ≥ 12 rats, global ≥ 150), whatever the cooperation, at the first book site with none
  of our kings within d² 8; without a site, main's mine choice. A backstab does not abort it.
- `PM_COOP_HOLD` (was `PM_COOP_HOLD`, the same code as PM's switch, merged under PM's name; PC1 A2, `t3/pc1-last-cat`): in cooperation with one king, no bite, king bite or new cat trap
  that leaves a cat at or below the damage our cat traps could deal; rats defuse their own traps around such a cat.
- `CM_CAT_HUNT_START` (main 1300): the round PT 2d's late cat hunt starts.

## 3. Per-map data the book must carry

**Cat lanes** (book data `CAT_LANE`, a tileset). In the bot one `long` per row y (bit x set = tile (x, y)), `H` longs:
8·H bytes, at most 480 bytes (60 rows, `where_are_you`). A tile is set when any cat's 2×2 body covers it at some
point of its EXPLORE loop, computed from the map file alone (`D:mkbook.py`, using CL's `traps.py` `cat_loop`):
- the cat starts at its body in the map file; its reference corner is the bottom-left tile for chirality 0 and the
  mirrored corner for chirality 1 (VERTICAL bottom-right, HORIZONTAL top-left, ROTATIONAL top-right; rules.md §12);
- it walks waypoints 0, 1, …, n−1, 0, 1 in order (the cycle and its wrap), each leg the engine's BFS from the waypoint
  (`GameWorld.bfsFromTarget`: walls and edges only, the 2×2 body must fit, 8 neighbours in the order N, E, S, W, NE, SE,
  SW, NW, mirrored for chirality 1), each step to the BFS parent;
- ATTACK excursions (pounces, chases to a target) and dirt are not in it.
On 22 cats of the 150 visible maps a waypoint is unreachable by that BFS (chocolateheart, diamond, mincemeat,
mushroom, …); `cat_loop` stops there, and those maps need the partial loop or no entry. Loops cover 14% of a map's
tiles on average. 17 map sizes are shared by more than one map, so a key needs more than W×H (MB's map
identification).

**Second-king sites** (`CM_KING2_ROUND`, book data `KING2_SITE`, a tiles list). Per map and seat, a list of 3×3 centres in order of
preference: the 3×3 off every loop tile above and free of walls and dirt, near a mine on our side
(`D:sites.py MAP X Y` lists the nearest such centres). Seat B is seat A's mirror. Tested:
- tminusten A (12,15), B (12,34); (12,13)/(12,36) read the same but touch cat 6's loop at row 12;
- sunandmoon A (19,23), B (19,16) (the centre room); the west pocket (8,33)/(8,6) read 0;
- peaceinourtime A (11,31) then (14,34), B (23,31) then (20,34) (the walled box only rats enter).

## 4. Runs

Arms turn one or more switches on everywhere; each is read per map-seat, paired with main on the same cells
(`D:msdiff.py`). Diff = arm − main, 95% normal interval over paired games.

| arm | change | pool, seed base | per map-seat (cells a map-seat) | run |
|---|---|---|---|---|
| K2 D' (old base) | rs-k2d vs its base | 6 maps, 23002 | bed A +0.31, B +0.51; dirtfulcat A +0.11, B +0.10; RUN B −0.10 (80) | `1119-213002-ab-72c4` |
| K2 D' on main | cherry-pick | 6 maps, 23004 | bed A +0.175 [+0.08, +0.27], B +0.55 [+0.43, +0.68]; dirtfulcat A +0.11 [+0.01, +0.22], B +0.06 (80) | `1119-213857-ab-1fd0` |
| `CM_LANE_MEMORY` | confirmation | bed, dirtfulcat, 23008 | bed A +0.25 [+0.17, +0.34] (25/0), B +0.56 [+0.45, +0.67] (60/4); dirtfulcat A +0.15 [+0.06, +0.24], B +0.03 [−0.05, +0.11] (100) | `1119-221505-ab-0aaa` |
| O1 (probe) | lanes preloaded | 6 maps, 23004 | dirtfulcat B +0.29; sunandmoon A +0.14; bed as D'; peaceinourtime A −0.25, B −0.14; RUN, tminusten 0 (80) | `1119-213909-ab-d089` |
| O2 (probe) | O1 + lane flee | 6 maps, 23004 | RUN A +0.225 [+0.11, +0.34] (22/4); dirtfulcat B +0.29 [+0.18, +0.40]; tminusten A +0.30, B +0.11; bed A +0.19, B +0.60; peaceinourtime −0.25 / −0.15 (80) | `1119-213916-ab-2c3b` |
| KD CAT (old base) | danger king at a far mine | 6 maps, 23002 | tminusten A +0.475, B +0.20; RUN A −0.16, dirtfulcat A −0.14 (80) | `1119-213004-ab-afbf` |
| `CM_KING2_ROUND` 60 | site (12,13) | tminusten, 23007 | A +0.30 [+0.21, +0.39] (37/1), B +0.19 [+0.11, +0.27] (120) | `1119-214932-ab-8182` |
| `CM_KING2_ROUND` 30 | site (12,13) | tminusten, 23007 | A +0.43 [+0.34, +0.52] (52/0), B +0.29 [+0.21, +0.37] (35/0) (120) | `1119-214918-ab-0cca` |
| `CM_KING2_ROUND` 15 | site (12,13) | tminusten, 23007 | A +0.44, B +0.29 (120) | `1119-223333-ab-05e2` |
| `CM_KING2_ROUND` 30, sites k2s1 | §3 sites | tminusten, sunandmoon, peaceinourtime, 23007 | tminusten A +0.475, B +0.30; sunandmoon A +0.175 [+0.04, +0.31], B +0.15 [+0.02, +0.28]; peaceinourtime A +0.49 [+0.36, +0.62], B −0.16 [−0.26, −0.07] (80) | `1119-223519-ab-64a3` |
| `CM_KING2_ROUND` 30, sites k2s2 | alternatives | same, 23007 | sunandmoon ≈ 0; peaceinourtime A +0.30, B −0.20 (60, stopped) | `1119-223543-ab-7bf2` |
| `PM_COOP_HOLD` | | peaceinourtime, tminusten, sunandmoon, RUN, 23005 | peaceinourtime A +0.22 [+0.13, +0.31] (23/1); others within ±0.06 (100) | `1119-214120-ab-082d` |
| `COOP_ROUND` 30 / 50 | tools/tune `cm1` | 6 maps, 23003 | 30: tminusten A +0.175, B +0.11; 50: nothing outside peaceinourtime (80) | `1119-215158-ab-20ea`, `1119-214446-ab-3a31` vs `1119-213437-ab-97ce` |
| `LOCK_IN_ROUND` 300 / 450 | tools/tune `cm1` | 6 maps, 23003 | 300: peaceinourtime A −0.05; 450: nothing (80) | `1119-220628-ab-4ca0`, `1119-215922-ab-2080` |

| second king + lanes + flee | `t2` | tminusten, sunandmoon, 23009 | tminusten A +0.475, B +0.21 (no game differs from the second king alone); sunandmoon A +0.19 [+0.06, +0.32], B +0.36 [+0.23, +0.49] (80) | `1119-225957-ab-2215` |
| second king alone | `t1` | tminusten, peaceinourtime, sunandmoon, 23009 | tminusten A +0.475, B +0.21; peaceinourtime A +0.36 [+0.22, +0.50], B −0.11; sunandmoon A +0.125, B +0.24 (80) | `1119-225939-ab-b3f0` |
| second king + last-cat hold | `p2` | peaceinourtime, 23009 | A +0.44 [+0.31, +0.57], B −0.175 (80) | `1119-230006-ab-646c` |
| lanes + flee | confirmation | dirtfulcat, RUN, 23010 | dirtfulcat B +0.21 [+0.11, +0.31], A +0.06; RUN A +0.07, B +0.01 (100) | `1119-230504-ab-5b8e` |
| `t2` | confirmation | sunandmoon, 23012 | A +0.21 [+0.10, +0.32], B +0.17 [+0.06, +0.29] (100) | `1119-231544-ab-1b88` |
| `p2` | confirmation | peaceinourtime, 23013 | A +0.45 [+0.34, +0.56], B −0.14 (100) | `1119-231831-ab-eddf` |
| `CM_CAT_HUNT_START` 800 / 500 | | RUN, 23014 | 800: A +0.10, B +0.04; 500: A +0.21 [+0.10, +0.32], B +0.06 (100) | `1119-231913-ab-b159`, `1119-231919-ab-a923` |

| `CM_CAT_HUNT_START` 500 | confirmation | RUN, 23015 | A +0.15 [+0.04, +0.26], B +0.06 (100) | `1119-232514-ab-45ee` |

| the 17 entries through `book.jsonl` (all but RUN) | book trial, `t3/cm-book-trial` | tminusten, peaceinourtime, sunandmoon, dirtfulcat, bed, 23016 | bed A +0.17 [+0.05, +0.28], B +0.43 [+0.29, +0.58]; tminusten A +0.52 [+0.39, +0.64], B +0.23 [+0.13, +0.34]; peaceinourtime A +0.47 [+0.32, +0.61], B unchanged (no entry); sunandmoon A +0.08 [−0.06, +0.23], B +0.25 [+0.10, +0.40]; dirtfulcat A +0.05, B +0.05 (main 0.88 there) (60) | `1119-232632-ab-e89f` |

## 5. Tools (`~/battlecode26-data/scratch/cm/`)

- `runs.txt` (every run: name, commit, ref, ID), `status.sh [running|done]`.
- `msdiff.py RUN` (paired run per map-seat), `msdiff.py --pair ARM REF` (tools/tune's variant and ref runs).
- `classify.py KD.jsonl [CL.jsonl]` (loss classes from KDStats rows), `base_kd.jsonl`, `base_cl.jsonl` (the baseline).
- `mkentries.py OUT.jsonl` (the proposed entries, lanes from the map files), `mkbook.py OUT.java MAP...` (the probe's lane tables), `sites.py MAP X Y [N]` (formation sites off every loop), `bccmp.sh REF`.
- Branches: `t3/cm-cat-maps` (switches, declarations), `t3/cm-arm-*` (switches on), `t3/cm-oracle` (probe, never merge),
  `t3/cm-book-trial` (the entries in the book, never merge).
