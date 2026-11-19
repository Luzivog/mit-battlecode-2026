# CM: the maps where cats decide our losses

Package CM (wave 6, the book). Main `c6ab0ea8` / `c8c85e5` (same bot) against awubot. Source keys: `C:`
`src/challenger/`; `D:` data and tools in `~/battlecode26-data/scratch/cm/` (§5). "Seat A/B" is our seat.
Switches are in `C:Tune.java` under `// CM`; per-map data in `C:CatBook.java` until the book holds it.

## 0. Summary

Work in progress: entries below marked *choose* have one fresh-seed read, *confirmed* two.

| map | seat | mechanism | switch | status |
|---|---|---|---|---|
| bed | A, B | the king leaves its cat-free corner down the cats' corridor and is pinned at (17,1)/(18,1) | `CM_LANE_MEMORY` | confirmed: A +0.25, B +0.56 |
| dirtfulcat | A | kings mine into cat lanes | `CM_LANE_MEMORY` | confirmed: +0.15 |
| tminusten | A, B | every centre the king can reach is on a cat loop; it is pinned at r22 and dies at r101–117 with a bank of 2,000 and 16 babies; the backstab comes before r50, so the cooperation second king never forms | `CM_KING2_ROUND` 30 + site | choose: A +0.43 to +0.48, B +0.29 to +0.32 (three arms) |
| peaceinourtime | A | awubot kills its own cat first (r363–487); ours kills our first king near r500; our own rats, traps and king then kill the last cat at 1 king to 2, and the cooperation ending scores kings 30 | `CM_LAST_CAT_HOLD`; `CM_KING2_ROUND` 30 + box sites | choose: hold +0.22, second king +0.49 |
| sunandmoon | A, B | the king flees along cat 6's (5's) lane from the mine into its waypoint dead end (26,28) / (26,11) | `CM_KING2_ROUND` 30 + site | choose: A +0.175, B +0.15 |
| RUN | A | ends on points at r2000 after the backstab; cat damage share | `CM_LANE_BOOK` + `CM_LANE_FLEE` | choose: +0.225 |
| dirtfulcat | B | as A | `CM_LANE_BOOK` + `CM_LANE_FLEE` | choose: +0.29 (lane memory alone +0.03 to +0.06) |

peaceinourtime B loses with the second king (−0.16) and every map with the lane book loses peaceinourtime (−0.25 A,
−0.15 B): those stay off.

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

Each default leaves main's executed bytecode unchanged: `D:bccmp.sh REF` builds REF and the branch with gradle and
diffs `javap -c` of every class; the only differences are added fields and methods never called. javac drops code
under a false compile-time constant in `if (C)` and `C ? a : b`, but `x || (C && y)` leaves a `goto`, so guards are
written as ternaries; a field initialiser runs in `<clinit>` and is avoided.

- `CM_LANE_MEMORY` (K2 D', `t3/rs-k2d`): `C:CatLanes` remembers every tile a cat's 2×2 was seen on; the king's mining
  takes no step that puts more of them under its 3×3, skips mines with a footprint in their 5×5 and cheese on one.
- `CM_LANE_BOOK`: `CatLanes` starts with the map's full cat loops (§3) at the king's init; lane memory's mining rules
  apply (`CM_LANE_KNOWN`).
- `CM_LANE_FLEE`: with a cat in view and lanes known, each of the king's 8 flee moves loses 4 per loop tile its step
  adds under the 3×3.
- `CM_KING2_ROUND` (2000 = off): from that round, with one king, form a second as the cooperation rule does (no rat
  gate, no known-mine threshold, ≥ 12 rats, global ≥ 150), whatever the cooperation, at the first book site with none
  of our kings within d² 8; without a site, main's mine choice. A backstab does not abort it.
- `CM_LAST_CAT_HOLD` (PC1 A2, `t3/pc1-last-cat`): in cooperation with one king, no bite, king bite or new cat trap
  that leaves a cat at or below the damage our cat traps could deal; rats defuse their own traps around such a cat.
- `CM_DEFDIRT_PIN` (RS DP): the king's defensive dirt only while pinned. On main it reads nothing clear on bed,
  dirtfulcat, sunandmoon, tminusten (`1119-214426-ab-7f77`, stopped at 64 cells a map-seat); no entry.

## 3. Per-map data the book must carry

**Cat lanes** (`CM_LANE_BOOK`, `CatBook.lanes()`). Per map one `long` per row y (bit x set = tile (x, y)), `H` longs:
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

**Second-king sites** (`CM_KING2_ROUND`, `CatBook.king2Sites()`). Per map and seat, a list of 3×3 centres in order of
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
| `CM_LAST_CAT_HOLD` | | peaceinourtime, tminusten, sunandmoon, RUN, 23005 | peaceinourtime A +0.22 [+0.13, +0.31] (23/1); others within ±0.06 (100) | `1119-214120-ab-082d` |
| `COOP_ROUND` 30 / 50 | tools/tune `cm1` | 6 maps, 23003 | 30: tminusten A +0.175, B +0.11; 50: nothing outside peaceinourtime (80) | `1119-215158-ab-20ea`, `1119-214446-ab-3a31` vs `1119-213437-ab-97ce` |
| `LOCK_IN_ROUND` 300 / 450 | tools/tune `cm1` | 6 maps, 23003 | 300: peaceinourtime A −0.05; 450: nothing (80) | `1119-220628-ab-4ca0`, `1119-215922-ab-2080` |

Running: combinations on fresh base 23009 (t1: second king alone; t2: + lane book and flee on tminusten, sunandmoon;
p2: + last-cat hold on peaceinourtime), the lane book and flee on dirtfulcat and RUN (23010), and the default harm check
on `goal` (23011).

## 5. Tools (`~/battlecode26-data/scratch/cm/`)

- `runs.txt` (every run: name, commit, ref, ID), `status.sh [running|done]`.
- `msdiff.py RUN` (paired run per map-seat), `msdiff.py --pair ARM REF` (tools/tune's variant and ref runs).
- `classify.py KD.jsonl [CL.jsonl]` (loss classes from KDStats rows), `base_kd.jsonl`, `base_cl.jsonl` (the baseline).
- `mkbook.py OUT.java MAP...` (lane tables), `sites.py MAP X Y [N]` (formation sites off every loop), `bccmp.sh REF`.
- Branches: `t3/cm-cat-maps` (switches, CatBook), `t3/cm-arm-*` (switches on), `t3/cm-oracle` (probe, never merge).
