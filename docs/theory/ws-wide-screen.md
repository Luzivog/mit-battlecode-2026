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

TheHeist B then gained AT's `IDLE_GOHOME` 0 (book 8). On top of it (reference: main `c5ddbdb1` plus that line,
`4d0d4ef`; arm: the same plus `SPAWN_RICH` 1000 on TheHeist B, `469cab7`; the map alone, 120 cells, seed base 22024,
`1120-115343-ab-114a`): seat B **+0.117 [+0.050, +0.183]** (117 / 103); seat A unchanged (99 / 99). The two add.

## 5. WS5: re-tuning around the first-king sites

Books 7 and 8 seat the first king (`KING1_SITE`) on kingofthehill A, B, therearenowalls A, B, QueenOfHearts A, shell B,
gelcatinouscube A, B, AceOfSpades A, therainbow A, diamond B, houseofcards B, externalsustenance B, thunderdome A, B.
Screen on their 11 maps, the map alone, both seats, 60 cells a map, seed base 22025, against `origin/t3/book-8`
(`79f2e78a`): 16 constant candidates (`SPAWN_CAP` 25, 30, 50, 60; `SPAWN_RICH` 1000, 1500; `MINES_KING2` 3, 7;
`SPAWN_CHEESE_DIV` 5, 20; `SPAWN_FLOOR` 0, 250; `GOHOME_PCT` 70, 130; `DIRT_ROUND` 0; `IDLE_GOHOME` 0) as campaign
WS6 (runs: `1120-121419-ab-8352`, `1120-121624-ab-942d`, `1120-122109-ab-f013`, `1120-122348-ab-b0a7`, `1120-122419-ab-8b41`, `1120-122534-ab-dc4d`, `1120-122746-ab-4ba0`, `1120-122811-ab-f024`, `1120-122944-ab-84fe`, `1120-123117-ab-193c`, `1120-123226-ab-2d22`, `1120-123346-ab-be1d`, `1120-123404-ab-1fb5`, `1120-123444-ab-1aea`, `1120-123525-ab-a4b1`, `1120-123628-ab-b29a`, `1120-123925-ab-1f8d`), and one early second king (`CM_KING2_ROUND` 30 with a `KING2_SITE` by
our nearest mine that the first king does not take: the mine nearest the first-king site is the taken one, the site a
free 3x3 off every cat loop within Chebyshev 2 of the next mine, else 3 with dirt allowed; `scratch/sg/tools/k2sites.py`)
on the 11 sited map-seats without a second-king entry (AceOfSpades A and shell B have no such site), `1120-120743-ab-182b`.
`SPAWN_CAP_SMALL` binds only on houseofcards, whose seat B books it: not played. Fit over the 17 together.

- The constants barely move around the new sites: no map-seat passes p_max < 0.05; the best reads are houseofcards A
  `DIRT_ROUND` 0 +0.133 (p_max 0.11) and externalsustenance A `SPAWN_CAP` 30 +0.150 (0.17); no value reads positive on
  both seats of the three maps sited on both (at most +0.083).
- The second king beside a seated first king: houseofcards B **+0.267 [+0.168, +0.365], p_max 0.005**, gelcatinouscube
  A +0.167 (p_max 0.15), and heavy losses elsewhere (QueenOfHearts A −0.683, therearenowalls A −0.633, B −0.500,
  kingofthehill B −0.150, thunderdome B −0.117): where the next mine is the other team's seat or far, it costs.

**Confirmed:** houseofcards B, `CM_KING2_ROUND` 30 and `KING2_SITE` [[16, 4]] (by the mine at (18, 4)), on top of
`origin/t3/book-8` (`667b0472`), the map alone, 120 cells, seed base 22026 (`1120-130216-ab-61c0`): seat B
**+0.208 [+0.095, +0.322]** (95 / 70); seat A unchanged (89 / 89).

houseofcards B lost its first-king site in book 9 (MB: +0.000 on 300 games). Without it, main's first king leaves
(27, 14) for the mine at (18, 4) (six local games: at (20–23, 14–15) by r50), so the rule's free mine is (11, 4). Both
second-king sites against `origin/t3/book-9` (`3c6f4b46`), houseofcards alone, 150 cells, seed base 22028: (16, 4)
seat B **+0.200 [+0.105, +0.295]** (126 / 96, `1120-131439-ab-47cb`); (13, 4), by (11, 4), +0.233 [+0.146, +0.321]
(131 / 96, `1120-131612-ab-22ee`); seat A unchanged in both. The line is (16, 4), the one chosen on its own run.

## 6. WS6: the early second king at a site, on the maps never played with one

Map-seats: the top 60 of `rank-book6.json` minus those already played with a `KING2_SITE` (the campaigns' variants,
main's book, AT3's four starving maps): 44, of which 35 have a free site (`scratch/sg/tools/k2cand.py`: the first
king's mine is its `KING1_SITE` or the mine nearest our start; a formation site beside the nearest other mine, rank 1,
and the next, rank 2; free 3x3 off every cat loop within Chebyshev 2, else 3 with dirt). No site at either mine:
directconfrontation B, cheesefarm B, AceOfSpades A and B, pocketsofcheese A and B, gridlock A, mincemeat A; none at
rank 1: thehallway B; one other mine only: houseofcards A, averyfineline B.

Three candidates on main (`151e1fbe`), 29 maps, 40 cells a map, seed base 22027: rank 1 at round 30
(`1120-130839-ab-5280`), rank 1 at round 80 (`1120-130844-ab-15a4`), rank 2 at round 30 (`1120-130848-ab-7bf7`);
main's games `1120-132622-ab-89c0` (from the cache). The second king loses on most map-seats:
mean −0.25, −0.20, −0.26 over them (TheHeist A −0.80 to −0.95, uneruesansfin B −0.83, constrictinglattice B −0.78,
longwayaround −0.53 to −0.65, squaresaremodernart B −0.55 to −0.63). No round-1 feature separates gain from loss:
the mine being nearer them than us (−0.15 against −0.35), or fewer than half the mines on our side (−0.09 against
−0.29), lean the right way but every class loses on average; king steps and reachability nothing.

Picks (fit over the three; p_max < 0.05, or the same candidate positive on both seats): mushroom A rank 2 r30 +0.375
(p_max 0.001), asabovesobelow B rank 1 r80 +0.350 (0.001), trapped A rank 1 r80 +0.250 (0.021) and B +0.200 (0.063).
Confirmed on seed base 22029 against `origin/t3/book-9` (`3c6f4b46`, main plus book 9's lines), the map alone, 120 cells:

| map | seat | `CM_KING2_ROUND` | `KING2_SITE` (mine) | effect | wins | run |
|---|---|---|---|---|---|---|
| mushroom | A | 30 | (20, 16) (22, 16) | **+0.217 [+0.136, +0.298]** | 117 / 91 | `1120-132728-ab-3b99` |
| asabovesobelow | B | 80 | (29, 21) (29, 23) | **+0.167 [+0.067, +0.266]** | 96 / 76 | `1120-132734-ab-0ee3` |
| trapped | A | 80 | (6, 19) (8, 19) | **+0.183 [+0.114, +0.253]** | 120 / 98 | `1120-132738-ab-477f` |
| trapped | B | 80 | (6, 30) (8, 30) | **+0.258 [+0.173, +0.344]** | 112 / 81 | `1120-132738-ab-477f` |

The other seats played unchanged (mushroom B 101 / 101, asabovesobelow A 119 / 119).

## 7. WS7: the second king's round and site, finer

Map-seats (`scratch/sg/tools/ws7plan.py`, `ws7plan.json`): WS6's map-seats whose best variant read above −0.10 and not
in book 9 (17), the map-seats that book the second king (therainbow A and B, worldshardestgame A and B, warhammer A,
gelcatinouscube B, and book 9's houseofcards B, mushroom A, asabovesobelow B, trapped A and B), and two skipped by WS6
with a site beside another mine on our side (directconfrontation B, beside (21, 26); cheesefarm B, beside (20, 27)).
Ten variants on `origin/t3/book-9` (`ee4be60a`), each setting one choice on every map-seat at once: rounds 30, 50, 80,
120, 200 at the best (or booked) site; the other WS6 site at rounds 30 and 80 (ALT30, ALT80); round 80 at up to three
further free sites beside mines on our side (O1–O3). 22 maps, 40 cells a map, seed base 22030 (the first launch,
stuck behind a silent Condor submit host, was stopped and played again; `scratch/sg/ws8_runs_stopped.txt`). Runs:
ref `1120-153028-ab-e15d`, R30 `1120-153028-ab-3978`, R50 `1120-153028-ab-ca1c`, R80 `1120-153028-ab-ca5f`, R120 `1120-153029-ab-3ab0`, R200 `1120-153029-ab-d376`,
ALT30 `1120-153029-ab-130a`, ALT80 `1120-153030-ab-d289`, O1 `1120-153030-ab-4f7b`, O2 `1120-153030-ab-57b7`, O3 `1120-153030-ab-9541`.

- No map-seat passes p_max < 0.05 (best: diamond A round 50 +0.125, p_max 0.27; knifefight A round 30 +0.100, 0.33).
- No variant beats a booked line: every other round or site reads 0 or below on the eleven booked map-seats (mushroom
  A −0.15 to −0.83; therainbow B round 200 −0.30, O1 −0.50; asabovesobelow B rounds 120 and 200 −0.25, −0.33). The
  booked rounds stand.
- Positive on both seats with +0.10 together: DefaultSmall (+0.075 / +0.025 under every variant alike: the entry, not
  its round or site) and closeup round 30 (+0.075 / +0.025). Confirmed once against origin/main `7b095ab7` (no waiting
  line on either map), the map alone, 120 cells, seed base 22031: DefaultSmall A +0.017 [−0.060, +0.094], B +0.017
  [−0.040, +0.073] (`1120-183125-ab-16b7`); closeup A +0.108 [−0.012, +0.228], B −0.008 [−0.067, +0.051]
  (`1120-183129-ab-6304`). Neither confirms.

**What the second king does** (series of cheese delivered and kings every 100 rounds, arm against reference on the
same cells; wins from the confirmations, losses from WS6's round-30 variant):
- mushroom A: it forms by r100 (1.8 kings); income to r300 doubles (2,968 against 1,410).
- asabovesobelow B: 1.7 kings; income +35%; games with no king left by r300 fall from 17% to 12%.
- trapped A and B: our income falls (419 against 715), but no king is left by r300 in 0–7% of games against 17–28%,
  and awubot's income halves: the second king is insurance, not income.
- TheHeist A: main's own second king forms by r100–200 and its cooperation entries win 38 of 40 on points before r500;
  a booked site blocks that formation (one king throughout) and drags the games to r2000, lost on points.
- uneruesansfin B: two kings from r100, a little more income, but 34 of 40 lost on points at r2000 (main wins 35):
  the second king's upkeep over a long game costs more than it brings.
- constrictinglattice B: it forms at once, income falls (2,392 against 2,927), 1.18 kings by r300 and none in 20% of
  games; 35 of 40 lost by kills before r1000. The formation thins the army and both kings fall.
The losses split by map kind (long points or cooperation games; early-combat maps); no single switch avoids both.

