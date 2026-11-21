# GE: economy by observed features, and a re-tune of clean main

Track GE of the clean program (docs/program.md). The book switched economy constants per map (`SPAWN_CAP`,
`SPAWN_CAP_SMALL`, `SPAWN_RICH`, `MINES_KING2`, `EXPLORE_P`, `DIRT_ROUND`, `IDLE_GOHOME`, `PM_SIEGE`, `PM_COOP_HOLD`,
`PM_SKIP_FAILED`). Job 1: a rule on features a robot observes that picks the maps where each value wins. Job 2: a
re-screen of the economy constants of Tune.java on clean main. Branch `t3/ge-economy-rules`. Scripts and data:
`D:` = `~/battlecode26-data/scratch/ge/` (`matrix.py` per map-seat paired effects of a campaign, `rules.py` the
rule search split by seed halves, `cv.py` the rule search cross-validated by maps, `oracle.py` the per-map-seat
table, `dyn.py` main's in-game series per map-seat, `old.py`; W4's `mapfeat.json` for the static features).

## 1. The book's values on clean main, every map (`gem1`)

Each value alone against clean main (cf20d501, the book emptied), on all 95 `goal` maps, 30 cells a map (2,850
cells, both seats), seed base 32001; ref `1121-095313-ab-423b`, main 0.829. Paired difference a game (value − main):

| value | diff | better / worse games | run |
|---|---|---|---|
| `SPAWN_CAP` 30 | −0.039 | 497 / 721 | `1121-095313-ab-b684` |
| `SPAWN_CAP` 50 | −0.058 | 446 / 776 | `1121-095314-ab-0a04` |
| `SPAWN_CAP` 60 | −0.105 | 428 / 1028 | `1121-095314-ab-339d` |
| `SPAWN_CAP_SMALL` 40 | −0.040 | 119 / 346 | `1121-095314-ab-2737` |
| `SPAWN_CAP_SMALL` 50 | −0.102 | 97 / 676 | `1121-095314-ab-23a2` |
| `SPAWN_RICH` 1500 | −0.003 | 269 / 287 | `1121-095315-ab-939b` |
| `SPAWN_RICH` 1000 | −0.037 | 364 / 575 | `1121-095315-ab-e5aa` |
| `MINES_KING2` 7 | +0.001 | 77 / 69 | `1121-095316-ab-7805` |
| `MINES_KING2` 3 | −0.004 | 42 / 62 | `1121-095316-ab-fb80` |
| `EXPLORE_P` 0.2 | −0.013 | 430 / 503 | `1121-095317-ab-d359` |
| `DIRT_ROUND` 0 | −0.001 | 417 / 419 (2,057 cells: 315 failed) | `1121-095317-ab-9db1` |
| `IDLE_GOHOME` 0 | −0.027 | 534 / 688 | `1121-095318-ab-9fe6` |
| `PM_SIEGE` 1 | −0.007 | 334 / 371 | `1121-095319-ab-8583` |
| `PM_COOP_HOLD` 1 | −0.007 | 73 / 115 | `1121-095320-ab-fa66` |
| `PM_SKIP_FAILED` 2 | −0.003 | 36 / 55 | `1121-095320-ab-33c8` |

No value gains everywhere. The early reads at a third of the cells (`PM_SIEGE` +0.017, `EXPLORE_P` 0.2 +0.015,
`DIRT_ROUND` 0 +0.017) were chance. `MINES_KING2`, `PM_SKIP_FAILED` and `PM_COOP_HOLD` change under 3% of games: on
clean main their gates rarely bind.

**The per-map effects are real.** The best value per map-seat (or main), chosen on odd seeds and scored on even ones
and the reverse, gains +2.0 and +2.7 pp of `goal` (`oracle.py`): the book's economy entries took about this.

## 2. No feature rule carries to other maps

A rule is a condition on map features (one threshold, or two), and the value plays where it holds. Its gain in pp of
`goal` is the sum of the value's map-seat effects where it holds, / 190. Features: W4's static table (area, mines,
mines per 1k tiles, wall and dirt shares, cats, king Chebyshev and walking distance, cut, nearest mine, mines within
15, tied mines, symmetry, seat, T4's classes) and, as a stand-in for what the game shows, main's own series per
map-seat (our cheese delivered, rats and kings at r100, r200, r300; theirs; our cheese share).

- **Split by seeds** (choose on half the seeds, score on the other half, same maps) rules look good: e.g.
  `SPAWN_RICH` 1500 on dirt ≥ 9.8% and king walk ≥ 52 (15 maps) +0.7 pp on the other half.
- **Split by maps** (choose on 4/5 of the maps, score on the fifth; 20 random fold splits; `cv.py`) they do not:

  | value | static features | static + main's r100-r300 state |
  |---|---|---|
  | `SPAWN_CAP` 30 | −0.52 [−1.40, −0.11] | −0.20 [−0.70, +0.31] |
  | `SPAWN_CAP` 50 | −0.53 [−1.04, −0.17] | −0.85 [−1.28, −0.44] |
  | `SPAWN_CAP` 60 | −0.05 [−0.61, +0.48] | −0.24 [−0.67, +0.19] |
  | `SPAWN_RICH` 1500 | +0.07 [−0.44, +0.43] | −0.33 [−0.70, +0.14] |
  | `SPAWN_RICH` 1000 | −0.62 [−0.99, −0.03] | −0.52 [−1.18, +0.31] |
  | `EXPLORE_P` 0.2 | −0.58 [−0.81, −0.34] | −0.26 [−0.60, +0.18] |
  | `PM_SIEGE` 1 | −0.09 [−0.34, +0.18] | −0.25 [−0.65, +0.05] |
  | `DIRT_ROUND` 0 | −0.50 [−1.16, +0.21] | +0.31 [+0.02, +0.62] (our cheese share at r200-r300 < 0.52) |
  | `IDLE_GOHOME` 0 | +0.24 [−0.19, +0.62] | **+0.54 [+0.26, +0.82]** (our rats at r200 < 11) |

  (pp of `goal`, mean over the 20 splits [2nd, 19th]; at most one rule a fold.)

So AT4's finding holds on clean main: the best economy value is a per-map fact that the map's features do not
predict, for the cap, the rich line, the second king's mine gate, exploration, digging and the siege spawns. The two
exceptions condition on the game's state: `DIRT_ROUND` 0 where we trail on cheese (not observable: the enemy's
cheese is unknown) and `IDLE_GOHOME` 0 where our army is small, which a robot sees in its rat cost (§4).

## 3. The re-screen (`ges1`)

16 constants ± one step (`SPAWN_POOR_CUT` ± 5), 32 variants × 570 `goal` cells, seed base 32002, against clean main
(`tools/tune fit ges1`; ref `1121-095402-ab-fc50`, 0.824). Every variant loses (−0.03 to −0.35, curvature −0.147
[−0.181, −0.112]): main's economy sits at a sharp optimum, and the main effects below come from a region far from
it, so they rank candidates only.

| constant | effect a step up | z |
|---|---|---|
| `SPAWN_CAP_SMALL` 30 ± 10 | +0.055 [+0.031, +0.081] | +4.4 (20 is far worse; 40 is −0.040 in §1) |
| `SPAWN_FLOOR` 100 ± 50 | −0.008 [−0.012, −0.005] | −4.3 |
| `SPAWN_VRICH` 3000 ± 750 | −0.011 [−0.017, −0.006] | −3.9 |
| `SPAWN_POOR_CUT` 10 ± 5 | −0.016 [−0.025, −0.007] | −3.5 |
| `SPAWN_RICH` 2000 ± 500 | −0.008 [−0.015, −0.001] | −2.2 (1500 is −0.003 in §1) |
| `LOCK_IN_MIN_RATS` 12 ± 4 | +0.005 [+0.001, +0.010] | +2.1 |
| `GOHOME_PCT` 100 ± 30 | +0.005 [+0.000, +0.011] | +1.9 |
| `MINES_KING2`, `SPAWN_CAP`, `EXPLORE_P`, `DIRT_ROUND`, `SPAWN_CHEESE_DIV`, `COOP_ROUND`, `IDLE_GOHOME`, `DIRT_CHEESE`, `EXPLORE_KING_DSQ` | | \|z\| ≤ 1.5 |

## 4. Arms

- **Mine-poor gate** (`MineEstimate.poorMap()`: ≤ 1,600 tiles, < 5 mines proven; the king's cap cut uses the same
  gate). On those 21 `goal` maps `gem1` reads `IDLE_GOHOME` 0 +0.70 pp (halves +1.13, +0.35), `EXPLORE_P` 0.2 +0.44
  (+0.46, +0.40); on small maps with ≥ 5 mines both lose (−1.28, −1.16). Mechanism: income is a few mines' worth and
  the lone king starves, so an idle rat banks what it carries; the few mines are known early, so exploring wastes
  trips. `POOR_IDLE_GOHOME`, `POOR_EXPLORE_P` (defaults main's).
- **Small army** (`IDLE_ARMY_COST`: while the rat cost is at most this, an idle rat banks any raw cheese). The rat
  cost is 10 + 10·⌊rats/4⌋, read for 1 bytecode by any robot.
- **Single moves from the screen**: `SPAWN_FLOOR` 50, `SPAWN_VRICH` 2250, `SPAWN_POOR_CUT` 5, `GOHOME_PCT` 130,
  `LOCK_IN_MIN_RATS` 16, each alone against main.

Results, every arm against main 06ced9fb on fresh seeds (variant − main, 95% CI by map):

| arm | campaign, pool, seed base | diff |
|---|---|---|
| `POOR_EXPLORE_P` 0.2 | `gep1`, the 48 `goal` maps ≤ 1,600 tiles, 960 cells, 32003 | +0.005 [−0.011, +0.021] (mine-poor maps +0.016, small rich maps −0.004) |
| `POOR_IDLE_GOHOME` 0 + `POOR_EXPLORE_P` 0.2 | same | −0.001 [−0.021, +0.020] |
| `POOR_IDLE_GOHOME` 0 | same | −0.006 [−0.025, +0.011] |
| neutral base (bytecode only) | same | −0.002 [−0.012, +0.009] |
| `IDLE_ARMY_COST` 20 | `gea1`, `goal`, 1,000 cells, 32006 | −0.003 [−0.017, +0.012] |
| `IDLE_ARMY_COST` 30 | same | −0.008 [−0.023, +0.009] |
| `IDLE_ARMY_COST` 40 | same | −0.010 [−0.027, +0.007] |
| `IDLE_ARMY_COST` 30 + the mine-poor pair | same | −0.005 [−0.021, +0.011] |
| `LOCK_IN_MIN_RATS` 16 | `get1`, `goal`, 1,000 cells, 32005 | +0.001 [−0.004, +0.006] |
| `SPAWN_POOR_CUT` 5 | same | 0.000 (no game changes: rat costs move in steps of 10) |
| `SPAWN_VRICH` 2250 | same | −0.001 [−0.011, +0.010] |
| `SPAWN_FLOOR` 50 | same | −0.003 [−0.008, +0.002] |
| `GOHOME_PCT` 130 | same | −0.005 [−0.025, +0.015] |

## 5. Conclusion

Nothing to merge. Clean main's economy constants sit at a sharp optimum (every two-level variant loses, every single
move is flat), and the book's economy values gain only per map: +2.0 to +2.7 pp of `goal` as a per-map-seat table,
none of it reachable by a rule on what a robot observes. The in-sample and seed-split reads of feature rules (+0.5 to
+1.8 pp) are selection: the map-split reads and the fresh-seed arms are at or below 0. The branch's code (the shared
mine-poor gate, `POOR_*`, `IDLE_ARMY_COST`, defaults main's) is not proposed for merge.
