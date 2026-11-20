# CM2: the next maps lost by king kills

Package CM2 (wave 6, round 2 of the book). Main `70d8f983` / `71c8fe8c` (same bot, book 3) against awubot. Data and
tools: `~/battlecode26-data/scratch/cm/` (`D:`; CM's tools, docs/theory/cm-cat-maps.md §5). "Seat A/B" is our seat.
Maps: asabovesobelow, therainbow, therearenowalls, kingofthehill (VERTICAL), closeup, externalsustenance,
gelcatinouscube, worldshardestgame, QueenOfHearts (ROTATIONAL).

## 0. Summary

Work in progress.

| map | seat | entry | choose | confirm |
|---|---|---|---|---|
| gelcatinouscube | B | `CM_KING2_ROUND` 30 (no site: main's mine choice) | `cm2a` (6 candidates), 23018, `1120-025135-ab-38a2` vs `1120-024109-ab-14a9`: +0.20 [+0.10, +0.30], adj +0.14, p_max 0.026 | 23022 `1120-030836-ab-c799`: +0.19 [+0.08, +0.30] (27/8) |

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

Running: the lanes trial's confirmation (23024, `1120-031842-ab-5e24`), gelcatinouscube A's second read (23025,
`1120-032115-ab-0bf5`), `cm2c` (`KING_HAND` 0, 1, `SG_LOOK_RIGHT` 1 on the four VERTICAL maps, 23026), `cm2d`
(`SPAWN_POOR_CUT` 0 on the five maps of ≤ 1,600 tiles, 23027).
