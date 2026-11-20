# CM2: the next maps lost by king kills

Package CM2 (wave 6, round 2 of the book). Main `70d8f983` / `71c8fe8c` (same bot, book 3) against awubot. Data and
tools: `~/battlecode26-data/scratch/cm/` (`D:`; CM's tools, docs/theory/cm-cat-maps.md §5). "Seat A/B" is our seat.
Maps: asabovesobelow, therainbow, therearenowalls, kingofthehill (VERTICAL), closeup, externalsustenance,
gelcatinouscube, worldshardestgame, QueenOfHearts (ROTATIONAL).

## 0. Summary

Entries, each chosen on one fresh seed base and confirmed on another against main, on the map alone, both seats; the
lines: `D:entries2.jsonl`. K = candidates the choice was made from.

| map | seat | entry | mechanism | choose | confirm |
|---|---|---|---|---|---|
| therainbow | A, B | `CM_KING2_ROUND` 30, `KING2_SITE` A (17,5), B (22,5) | awubot takes the contested centre mine on our side (421 cheese picked in r0–300 to our 169) and its own (516 to 43); our king walks to the centre, then to its wall mine | K 1, 23030 `1120-034612-ab-d3ce`: A +0.34 [+0.22, +0.46], B +0.225 [+0.10, +0.36] | 23032 `1120-040328-ab-fe20`: A +0.23 [+0.12, +0.34] (30/7), B +0.17 [+0.06, +0.28] (25/8) |
| gelcatinouscube | B | `CM_KING2_ROUND` 30 (no site: main's mine choice) | no mine within the king's reach; the lone king starves at r500–800 | K 6 (`cm2a`), 23018 `1120-025135-ab-38a2` vs `1120-024109-ab-14a9`: +0.20 [+0.10, +0.30], adj +0.14, p_max 0.026 | 23022 `1120-030836-ab-c799`: +0.19 [+0.08, +0.30] (27/8); a third read, 23025 `1120-032115-ab-0bf5`: +0.04 [−0.05, +0.13] |
| closeup | B | `CAT_LANE`, `CM_LANE_MEMORY` 1, `CM_LANE_FLEE` 1 | the king pinned in cat 4's corridor (mirror of A's (12,10)) | K 4 map-seats of the lanes trial, 23024 `1120-031842-ab-5e24`: +0.10 [+0.01, +0.19] (the first trial, 23019 `1120-025412-ab-fd67`: +0.03) | 23028 `1120-034147-ab-d247`: +0.14 [+0.06, +0.23] (23/6) |

| worldshardestgame | A | `CM_KING2_ROUND` 30, `KING2_SITE` (13,15) | no mine within the king's reach (`ms/reach_all.txt`); the lone king starves at r500–700 | K 1 (therainbow's mechanism tried on four maps), 23033 `1120-040816-ab-6a45`: +0.23 [+0.13, +0.34] (14/0) | 23034 `1120-042000-ab-c796`: +0.17 [+0.09, +0.25] (18/1); 23035 `1120-042730-ab-56de`: +0.18 [+0.10, +0.26] |
| worldshardestgame | B | `CM_KING2_ROUND` 30, `KING2_SITE` (26,34) | as A | 23034 `1120-042000-ab-c796`: +0.20 [+0.11, +0.29] (22/2) (23033: +0.07 [−0.01, +0.15]) | 23035 `1120-042730-ab-56de`: +0.10 [+0.04, +0.16] (10/0) |

No remedy found: kingofthehill, therearenowalls and QueenOfHearts (the contested-mine king loses −0.23 to −0.50 there),
asabovesobelow B (SG's line on main; siege does not confirm on top of it), externalsustenance (`KING_PATH`, lanes and
the second king do not confirm), closeup A.

## 1. Baseline

Main against awubot, seed base 23017, 80 games a map-seat (`1120-024049-ab-0403`, losses kept); classes by W0.3's
rules on our kings' HP (`D:classify.py` over KDStats rows, `D:b2_kd.jsonl`; CLPin pins `D:b2_cl.jsonl`):

| map | A | B | losses by class |
|---|---|---|---|
| asabovesobelow | 0.84 | 0.525 | B: COMBAT 28 of 38 |
| therainbow | 0.64 | 0.59 | STARVE 33, COMBAT 24 |
| kingofthehill | 0.65 | 0.56 | STARVE 34, COMBAT 22 |
| closeup | 0.56 | 0.80 | CAT A 25 of 35, B 11 of 16 |
| externalsustenance | 0.625 | 0.86 | A: CAT 16 of 30, COMBAT 7 |
| gelcatinouscube | 0.79 | 0.71 | STARVE, all 40 |
| worldshardestgame | 0.79 | 0.825 | STARVE, all 31 |
| therearenowalls | 0.76 | 0.775 | STARVE 29 of 37 |
| QueenOfHearts | 0.80 | 0.775 | A: COMBAT 14 of 16; B: STARVE and C+s |

- **Starvation** is the economy race lost early: the bank falls from ~1,400 at r100 to ~300 at r200 with 16 rats;
  by r200 we have delivered less cheese than awubot (therainbow −465, gelcatinouscube −318, kingofthehill −74); on
  therainbow and gelcatinouscube we field 4 rats fewer at r100, in won games too. The lone king starves at r500–800.
  These maps hold 4–6 mines, or none within the king's reach (gelcatinouscube, worldshardestgame: `ms/reach_all.txt`).
- **closeup A**: the king walks from its corner to the mine (10,2), then up cat 3's corridor, and is pinned near
  (12,10) at ~r230 (CL §2.3).
- **externalsustenance A**: within 30–50 rounds the king walks into the wall pocket (7,27)/(8,26), cat 3's waypoint,
  toward mines outside the arena it cannot reach (CL §2.3).

## 2. Screens and arms

Matrices are `tools/book/book matrix` (one run a candidate, main on the same cells, both seats, 40 cells a map),
read by `tools/book/book fit` (best of K, selection-adjusted, p_max). Trials are book entries on a branch never
merged; confirmations are the switch on against main on the map alone.

| screen / arm | candidates | pool, seed base | result | runs |
|---|---|---|---|---|
| `cm2a` | `CM_KING2_ROUND` 30, `CM_LANE_MEMORY` 1, lane memory + `CM_LANE_FLEE`, `KING_PATH` 1, `PM_SIEGE` 1, `MINES_KING2` 3 | the nine maps, 23018 | p_max < 0.05: `KING_PATH` on externalsustenance A (+0.23, adj +0.17), closeup A (+0.25, +0.17), closeup B (+0.175, +0.11); second king on gelcatinouscube B (+0.21, +0.15); `PM_SIEGE` on asabovesobelow B (+0.225, +0.15). `KING_PATH` −0.40 / −0.20 on kingofthehill; lane memory nothing; `MINES_KING2` 3 changed no game | ref `1120-024109-ab-14a9` |
| `cm2b` | `MINES_KING2` 7, `SPAWN_RICH` 1500, `SPAWN_CAP` 30 | the six starvation maps, 23020 | nothing at p_max < 0.2; `SPAWN_CAP` 30 −0.20 / −0.225 on therainbow; `MINES_KING2` 7 changed no game | ref `1120-030640-ab-38c8` |
| `KING_PATH` 1 confirmation | | closeup, externalsustenance, 23021 | closeup A +0.01, B 0.00; externalsustenance A +0.10 [−0.00, +0.20], B +0.01 (~94 cells): fails | `1120-030746-ab-a46a` |
| `PM_SIEGE` 1 on asabovesobelow B, over SG's `SG_LOOK_RIGHT` line | | asabovesobelow, 23023 | +0.08 [−0.01, +0.17] (15/7): fails | `1120-030839-ab-e4bf` |
| trial: second king r30 at sites off every loop | externalsustenance A (2,29), (13,27), B (27,10), (16,12); closeup A (1,8), (6,2), B (28,21), (23,27) | closeup, externalsustenance, 23019 | externalsustenance A +0.11, closeup A +0.075, closeup B −0.16 | `1120-025418-ab-3ebe` |
| trial: cat loops + lane memory + flee | `CAT_LANE`, `CM_LANE_MEMORY` 1, `CM_LANE_FLEE` 1 | closeup, externalsustenance, 23019 | externalsustenance A +0.19 [+0.04, +0.33] (26/11), closeup A +0.09, B +0.03 | `1120-025412-ab-fd67` |

| `cm2c` | `KING_HAND` 0, 1, `SG_LOOK_RIGHT` 1 | asabovesobelow, therainbow, therearenowalls, kingofthehill, 23026 | only asabovesobelow B `SG_LOOK_RIGHT` +0.35 (SG's line); A −0.275 | ref `1120-032500-ab-9093` |
| `cm2d` | `SPAWN_POOR_CUT` 0 | therainbow, gelcatinouscube, QueenOfHearts, closeup, externalsustenance, 23027 | nothing (therainbow −0.075) | ref `1120-033447-ab-0c58` |
| `cm2e` | `EXPLORE_P` 0.3, 0.9, `SPAWN_FLOOR` 200, `GOHOME_PCT` 70 | the five starvation maps, 23029 | nothing (p_max ≥ 0.16) | ref `1120-034633-ab-10c9` |
| `cm2f` | `CAT_DEF_DSQ` 100, 200 | closeup, externalsustenance, 23031 | seat A −0.15 to −0.175, B ≤ +0.10 n.s. | ref `1120-040149-ab-6399` |
| lanes trial, confirmations | | closeup, externalsustenance, 23024; closeup, 23028 | per map both seats: externalsustenance +0.125 then +0.035; closeup +0.056, +0.105, +0.067; closeup B +0.03, +0.10, +0.14 | `1120-031842-ab-5e24`, `1120-034147-ab-d247` |
| second king at the contested mine | sites per map (`D:sites.py`) | therainbow 23030; kingofthehill, therearenowalls, worldshardestgame, QueenOfHearts 23033 | therainbow +0.34 / +0.225; worldshardestgame A +0.23, B +0.07; the other three −0.23 to −0.50 | `1120-034612-ab-d3ce`, `1120-040816-ab-6a45` |

## 3. CM3: main's own history on the maps without a remedy

Eight revisions of `src/challenger` (the awubot copy `1ff4b32d`, `83c17482` after S2/KL/CW, `3dc0efd3` after KB/KS,
`1435494c` KF, `5b73501f` `EXPLORE_P` 0.6, `93e83115` `STRAFE_LO` 0, `6017078f` `KING_HAND` 7, today's main
`0a05b160`), each against awubot on kingofthehill, therearenowalls, asabovesobelow, QueenOfHearts, externalsustenance
and closeup, 60 cells a map on seed base 23036 (runs in `D:hist.txt`, table `D:histtab.py`), each paired with today's
main on the same cells.

- No older main beats today's on QueenOfHearts, asabovesobelow, therearenowalls, kingofthehill A or closeup A: no merged
  change costs there, and a finer bisection can only find changes that read 0 on those maps. Stopped.
- kingofthehill B: `1435494c` +0.17 ± 0.13 and `5b73501f` +0.10 ± 0.13 over today's 0.57, back to today's level from
  `93e83115` on; externalsustenance A `1435494c` +0.12 ± 0.14; closeup B +0.07 on several revisions.
- The switched changes in that span screened as `book matrix cm3a` (kingofthehill, externalsustenance, closeup, 40
  cells a map, seed base 23037, 3 candidates, ref `1120-051628-ab-6c4a`): `EXPLORE_P` 0.2 kingofthehill B +0.15 (adj
  +0.11, p_max 0.047), closeup B +0.175 (adj +0.13, p_max 0.049); `STRAFE_LO` 3 nothing at p_max < 0.15; the cat hunt
  off (`CM_CAT_HUNT_START` 2000) changed no game.
- `EXPLORE_P` 0.2 confirmation, seed base 23038 (`1120-052215-ab-8764`, 100 cells a map): kingofthehill B +0.05
  [−0.03, +0.13], closeup B −0.10 [−0.20, +0.00]: fails. Stopped: the three reads on kingofthehill B (+0.10, +0.15,
  +0.05) average about +0.1 a map-seat, 0.05 pp of `goal`, below what a 100-cell confirmation resolves.

