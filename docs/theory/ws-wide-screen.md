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
- `SPAWN_CAP` 60 or 80 reads −0.2 or worse on 49 of the 100 map-seats; 30 helps a few starving ones by up to +0.2
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

## 3. WS2: the army cap per map

From the 22017 screen: every map where a cap value read positive on both seats, and every map-seat where one read
+0.10 or more, the cap lines already taken left out (17 maps: Meow, QueenOfHearts, cheddarchokehold,
constrictinglattice, externalsustenance, hatefullattice, holeinthewall, insertmeowname, isnotgold, jail,
moonlitriverpark, shell, therearenowalls, trapped, uneruesansfin, where_are_you, worldshardestgame). None is 900 tiles
or smaller, so `SPAWN_CAP_SMALL` never binds there; it was not played.

Matrix WS3 (`SPAWN_CAP` 25, 30, 35, 50, 60; main 40), 100 cells a map, seed base 22019, against main `878973d9`:
ref `1120-070420-ab-3602`, C30 `1120-070504-ab-ae2e`, C25 `1120-070542-ab-cb7b`, C35 `1120-070609-ab-3573`, C50
`1120-070644-ab-3421`, C60 `1120-070713-ab-729c`. Caps 30 and 35 play the same games on every map: a rat costs 10 more
every fourth rat, so both thresholds build the same army; the fit's K of 5 holds 4 distinct candidates.

Picks: p_max < 0.05 (where_are_you A 30 +0.160, p_max 0.012; worldshardestgame B 25 +0.150, 0.040; hatefullattice A 60
+0.170, already booked), and one value positive on both seats (trapped 30 +0.100 / +0.100; cheddarchokehold 60 +0.090
/ +0.150). Confirmed on seed base 22020 against main `bdb370f3` (the fifth book), the map alone, 100 cells:

| map | cap | seat A | seat B | run |
|---|---|---|---|---|
| **worldshardestgame** | **25** | **+0.110 [+0.006, +0.214]** (88 / 77) | **+0.100 [+0.029, +0.171]** (97 / 87) | `1120-073911-ab-a7c9` |
| where_are_you | 30 | +0.030 [−0.060, +0.120] | −0.020 | `1120-073908-ab-6488` |
| trapped | 30 | +0.050 [−0.066, +0.166] | +0.100 [−0.017, +0.217] | `1120-073915-ab-95ab` |
| cheddarchokehold | 60 | +0.040 [−0.033, +0.113] | +0.070 [−0.046, +0.186] | `1120-073916-ab-2880` |

That confirmation was against a main without worldshardestgame's early second king (CM's `CM_KING2_ROUND` 30 and
`KING2_SITE` on both seats, in the sixth book). On top of those four lines (reference `d1d75f2`, arm `a0bcafa`, the
map alone, 120 cells, seed base 22021, `1120-074933-ab-772d`) the reference wins 119 of 120 on each seat and the cap
loses: **A −0.100 [−0.159, −0.041] (107 / 119), B −0.092 [−0.148, −0.035] (108 / 119)**. Both answer the same
starvation and do not add; the cap is not a line.

## 4. WS3: spawning and income constants per map

Twelve candidates (main's value in brackets): `MINES_KING2` 3, 7 [5]; `SPAWN_RICH` 1000, 1500 [2000];
`SPAWN_CHEESE_DIV` 5, 20 [10]; `SPAWN_FLOOR` 0, 250 [100]; `SPAWN_POOR_CUT` 0, 20 [10]; `GOHOME_PCT` 70, 130 [100].
Maps: the 50 holding most of main's losses after the sixth book (`rank-book6.json`, 607 of 722 losses), 40 cells a map,
seed base 22022, against main `e976a786` (matrix WS5; runs, ref first: `1120-080131-ab-68b2`, `1120-080256-ab-b3a6`, `1120-080340-ab-cd70`, `1120-080457-ab-bef9`, `1120-080645-ab-5b1c`, `1120-081347-ab-4710`, `1120-081404-ab-3a0f`, `1120-081633-ab-acc8`, `1120-081738-ab-1728`, `1120-081812-ab-fc42`, `1120-081917-ab-fedb`, `1120-081949-ab-35da`, `1120-082554-ab-896b`). Fit over
the twelve together; no map-seat had the same constant booked.

Picks: p_max < 0.05 (TheHeist B `SPAWN_RICH` 1000 +0.225, p_max 0.014; longwayaround A `MINES_KING2` 7 +0.250, 0.026),
and one value at +0.10 or more on both seats (TheHeist `SPAWN_RICH` 1000 A +0.125; longwayaround `SPAWN_RICH` 1500
+0.175 / +0.125 and `GOHOME_PCT` 130 +0.100 / +0.100; hatefullattice `GOHOME_PCT` 130 +0.100 / +0.200). Confirmed on
seed base 22023 against main `31f0a2aa` (the sixth book), the map alone, 100 cells:

| map | constant | value | seat A | seat B | run |
|---|---|---|---|---|---|
| **TheHeist** | `SPAWN_RICH` | **1000** | **+0.100 [+0.041, +0.159]** (100 / 90) | **+0.050 [+0.007, +0.093]** (100 / 95) | `1120-114149-ab-4242` |
| **longwayaround** | `MINES_KING2` | **7** | **+0.110 [+0.010, +0.210]** (75 / 64) | +0.060 [−0.054, +0.174] | `1120-114203-ab-60fe` |
| longwayaround | `SPAWN_RICH` | 1500 | +0.010 [−0.096, +0.116] | +0.050 [−0.069, +0.169] | `1120-114210-ab-213b` |
| longwayaround | `GOHOME_PCT` | 130 | −0.090 [−0.202, +0.022] | +0.060 [−0.064, +0.184] | `1120-114221-ab-d807` |
| hatefullattice | `GOHOME_PCT` | 130 | +0.000 [−0.084, +0.084] | +0.090 [−0.032, +0.212] | `1120-114228-ab-35c2` |

Lines: TheHeist both seats `SPAWN_RICH` 1000 (on top of PM's `PM_SYM` and `PM_COOP_HOLD` there), longwayaround A
`MINES_KING2` 7 (on top of `SPAWN_CAP` 60).

