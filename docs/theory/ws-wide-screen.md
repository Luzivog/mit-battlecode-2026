# WS: the wide screen

Package WS (wave 6, the book). Round 2's switches that held on several maps were each screened only on their finder's
maps. WS plays ten of them on the 50 `goal` maps that hold 672 of main's 787 losses
(`~/battlecode26-data/scratch/integrator/wide-maps.txt`). "Seat" is ours; a read is ours − main, paired on the same
cells, against awubot. Data: `~/battlecode26-data/scratch/sg/` (`ws_fit_all_10.txt` the fit, `ws_confirm.txt` the
confirmations, `ws_exclude.json` the entries already taken, `tools/seatci.py RUN` a paired run per map and seat).

## 0. Entries

Chosen by `tools/book/book fit` over the ten candidates together (best of 10 on each map-seat), on seed base 22017;
confirmed on 22018 against main `2f2f1c71` (the bot of `767421c1`, the fourth book) on the map alone, 80 cells, both
seats played.

| map | seat | switch | value | chosen (22017, 40 cells, raw / adj / p_max) | confirmed (22018, 80 cells) | run |
|---|---|---|---|---|---|---|
| QueenOfHearts | A | `KING_PATH` | 1 | +0.425 / +0.327 / 0.000 | **+0.150 [+0.024, +0.276]** (68 / 56 wins) | `1120-065754-ab-01e0` |
| cheddarchokehold | B | `SG_LOOK_RIGHT` | 1 | +0.375 / +0.277 / 0.001 | **+0.163 [+0.035, +0.290]** (72 / 59) | `1120-065755-ab-eb06` |
| cheeseguardians | A | `SPAWN_CAP` | 60 | +0.125 / +0.051 / 0.188 (B also positive) | **+0.163 [+0.081, +0.244]** (80 / 67) | `1120-065757-ab-e1e5` |
| cheeseguardians | B | `SPAWN_CAP` | 60 | +0.100 / +0.031 / 0.351 (A also positive) | **+0.125 [+0.044, +0.206]** (79 / 69) | `1120-065757-ab-e1e5` |

The other seat on each confirmation: QueenOfHearts B −0.050 [−0.170, +0.070], cheddarchokehold A −0.075 [−0.166,
+0.016] (neither booked). cheddarchokehold B has `KING_HAND` 1 on main; the lookahead's hand goes on top of it.

## 1. The screen

Two `tools/book/book matrix` campaigns on the same cells (50 maps, 40 cells a map, seed base 22017, 2,000 cells a run),
main's games shared, fitted as one campaign of ten candidates:

| label | switch = value | run |
|---|---|---|
| ref | main `9f2de076` | `1120-045957-ab-b9a4` (and `1120-054802-ab-b6f2`, the same games) |
| DEF100 | `CAT_DEF_DSQ` 100 | `1120-050104-ab-bab6` |
| HUNT500 | `CM_CAT_HUNT_START` 500 | `1120-050144-ab-0def` |
| K2R30 | `CM_KING2_ROUND` 30 | `1120-050157-ab-a4d0` |
| PATH | `KING_PATH` 1 | `1120-050229-ab-751b` |
| SKIP2 | `PM_SKIP_FAILED` 2 (PM's `6d9c69ad`) | `1120-050322-ab-2bb4` |
| LOOK | `SG_LOOK_RIGHT` 1 | `1120-050342-ab-783d` |
| HOLD | `PM_COOP_HOLD` 1 | `1120-050433-ab-28ec` |
| CAP60, CAP80, CAP30 | `SPAWN_CAP` 60, 80, 30 | `1120-054920-ab-1b76`, `1120-055006-ab-1298`, `1120-055020-ab-b483` |

The base is main merged with `6d9c69ad` (javap: every instruction of main's classes unchanged). Map-seats that already
hold the same entry (main's book, the fifth and sixth books' lists) were left out of the reading.

Chosen for confirmation: the three map-seats with p_max < 0.05 (QueenOfHearts A, cheddarchokehold B,
t_minus_five_minutes A), and the maps where one switch read at least +0.1 on both seats (closeup and insertmeowname
`KING_PATH`, cheeseguardians `SPAWN_CAP` 60, t_minus_five_minutes `SG_LOOK_RIGHT`, gelcatinouscube A `CM_KING2_ROUND`
30 beside seat B's entry).

What the screen shows beyond the entries:
- `SPAWN_CAP` 60 and 80 lose on most maps (−0.2 to −0.6 on 20 map-seats); 30 helps a few starving ones by up to +0.2
  at p_max ≥ 0.12 (jail A, constrictinglattice B, dirtpassageway B, which AT2 booked); `PM_SKIP_FAILED` 2 and
  `PM_COOP_HOLD` 1 change almost no game outside their maps; `CM_KING2_ROUND` 30 loses heavily where no site is
  booked (constrictinglattice B −0.70, uneruesansfin B −0.60, mushroom A −0.33).

## 2. Failed on fresh seeds (22018, 80 cells, against `2f2f1c71`)

| map | seat | switch | chosen (22017) | confirmed | run |
|---|---|---|---|---|---|
| t_minus_five_minutes | A | `SG_LOOK_RIGHT` 1 | +0.225, p_max 0.007 | −0.037 [−0.150, +0.075] (B −0.025) | `1120-065756-ab-53b6` |
| closeup | A, B | `KING_PATH` 1 | +0.125 both | A −0.087 [−0.228, +0.053], B −0.025 | `1120-065755-ab-65ad` |
| insertmeowname | A, B | `KING_PATH` 1 | +0.125, +0.100 | +0.000, +0.013 | `1120-065755-ab-d9b5` |
| gelcatinouscube | A | `CM_KING2_ROUND` 30 | +0.125 | +0.062 [−0.032, +0.157] | `1120-065757-ab-a07c` |
