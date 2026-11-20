# KR: the king's hand and routes, by map and seat

Package KR (wave 6). Main's king bug always starts left-handed (docs/theory/ms-map-seats.md §2); this package makes the
hand a switch and chooses it by what the king knows at round 1. Source keys: `C:` `src/challenger/`; `D:`
`~/battlecode26-data/scratch/kr/` (§4).

## 1. The switch

`Tune.KING_HAND` (`C:Pathfinder.java.jinja2` `kingLeft()`, read at every new bug target of a king):

| value | the king's bug is right-handed for |
|---|---|
| 0 | nobody (main) |
| 1 | every king |
| 2 | chirality 1 |
| 3 | chirality 1 on VERTICAL and HORIZONTAL maps |
| 4 | chirality 1 on VERTICAL maps |
| 5 | team B on VERTICAL maps |
| 6 | team B |
| 7 | team B, except a chirality-1 king on HORIZONTAL or ROTATIONAL maps |

- What the king knows at round 1: its team; its chirality and, for chirality 1, the map's symmetry, from the order of
  its first `senseNearbyMapInfos` (the engine reverses x on VERTICAL maps, y on HORIZONTAL, both on ROTATIONAL for
  chirality 1; `C:CellTracker.java.jinja2` `kingScanOrder`, computed on the first turn when `KING_HAND != 0`). A
  chirality-0 king learns the symmetry only when the rats find it.
- Value 0 is main's instructions plus one method never called (javap); null run on 1,000 `goal` cells: 995 identical,
  −0.001 [−0.003, +0.001] (`1119-213204-ab-b4e3`). The default is 7 (§3). A book entry sets 0 (left) or 1 (right) on
  one map-seat.

## 2. The hand follows the team, not the chirality

Chirality belongs to the map file's start, not the team: on 19 `goal` maps team A's king has chirality 1 (5
VERTICAL: AceOfSpades, asabovesobelow, pipes, pocketsofcheese, therearenowalls; `D:mapchir.txt`, read with the
engine's `GameMapIO`; `theory/t4/maps.json` has the teams reversed on 12 maps, diamond among them). Arm h1 (every king
right-handed), 1,000 `goal` cells, by our team and the map's chirality of team A (`D:chir.py`):

| maps | right-handed team A | right-handed team B |
|---|---|---|
| VERTICAL, team A chirality 0 (230 games a seat) | −10.0 pp | +7.4 pp |
| VERTICAL, team A chirality 1 (52) | −3.8 pp | +9.6 pp |
| HORIZONTAL (209) | +1.6 pp | 0.0 pp |
| ROTATIONAL, team A chirality 0 (436) | −0.7 pp | +0.2 pp |
| ROTATIONAL, team A chirality 1 (73) | −12.3 pp | +6.8 pp |

So the right hand wins for team B and loses for team A wherever it matters, whichever side of the map either stands
on. The arms keyed on chirality (2–4) turn team A right on the chirality-1 maps and lose there; arm 5 keys on the team
but a chirality-0 team-B king rarely knows the symmetry before its first routes (5 of 52 games changed).

## 3. Arms

All paired with main on the same cells, `--remote`, `goal`.

| arm | KING_HAND | seed base | cells | ours − main [95% CI] | run |
|---|---|---|---|---|---|
| null | 0 | 21001 | 1,000 | −0.001 [−0.003, +0.001] | `1119-213204-ab-b4e3` |
| h1 | 1 | 21002 | 1,000 | −0.003 [−0.021, +0.014] | `1119-213037-ab-d113` |
| h2 | 2 | 21002 | 1,000 | +0.003 [−0.013, +0.020] | `1119-213054-ab-4744` |
| h3 | 3 | 21002 | 1,000 | +0.007 [−0.004, +0.021] | `1119-213116-ab-ba08` |
| h4 | 4 | 21002 | 1,000 | +0.006 [−0.004, +0.020] | `1119-213140-ab-279e` |
| h5 | 5 | 21002 | 1,000 | +0.007 [−0.004, +0.021] | `1119-225745-ab-de7a` |
| h6 | 6 | 21002 | 1,000 | +0.014 [−0.002, +0.031] | `1119-225802-ab-2392` |
| **h7, confirmation** | 7 | 21003 | 3,000 | **+0.010 [+0.002, +0.021]** (132 cells better, 72 worse) | `1119-232537-ab-600c` |
| h7 smoke | 7 | – | 8 | no robot killed by its own exception | `1119-232548-ab-0924` |
| h7 gauntlet | 7 | – | | no harm: opp_obg +0.000 [−0.017, +0.017], opp_gst +0.004 [−0.018, +0.024], the rest unchanged | `1119-232721-gauntlet-40d7` |

- h7 was chosen from h6 and h1: their team-B gains lay on VERTICAL maps and on maps whose team-A king has chirality 1
  (h6: +9.1, +6.8, +6.8 pp); a team-B king of chirality 1 on HORIZONTAL or ROTATIONAL maps changed ~250 games a run
  for nothing (h1 +0.0 and +0.2 pp, h6 0.0 and −0.3 pp), so h7 leaves it left-handed.
- In the confirmation the gain is team B on VERTICAL maps: +6.6 pp (694 games, team A chirality 0) and +10.2 pp (157,
  team A chirality 1); team B on ROTATIONAL maps of team-A chirality 1 reads 0.000 (223 games, 185 changed: the
  screen's +6.8 pp did not hold). Elsewhere ±1.3 pp.
- By map-seat (`D:mapseat.py`, 31–32 games each, h7 − main): bed B +0.59 (27 against 8), EscapeTheNight B +0.52 (16
  against 0), pocketsofcheese B +0.31, popthecork B +0.29, QueenOfHearts B +0.28, longwayaround B +0.16, AceOfSpades
  B +0.09; the worst thehallway B −0.13, therainbow B −0.10, mushroom B, squaresaremodernart B, worldshardestgame B
  −0.06. Candidates for book entries (KING_HAND 0 or 1 on one map-seat) are milestone 2's.

## 4. Per map-seat routes (milestone 2)

Candidates against main with `KING_HAND` 7 on the same cells, both seats (`tools/book/book matrix`, 30 cells a map;
`fit`): KP `KING_PATH` 1 (MS's KP3 planner, `C:robot/king/KingPath.java`), L `KING_HAND` 0, R `KING_HAND` 1. Then the
chosen value alone on its map on a fresh seed base (`D:mapseat.py`: per seat, paired 95% interval).

- `kr_m2a`, 18 maps (the brief's, h7's moved seats, bed, RUN, cheddarchokehold), seed base 21005: runs ref
  `1120-011244-ab-2b23`, L `1120-011444-ab-e6b2`, R `1120-011618-ab-13ad`, KP `1120-011719-ab-619e`.
- `kr_m2b`, the other 34 VERTICAL and HORIZONTAL `goal` maps, seed base 21008: ref `1120-015013-ab-70ac`, KP
  `1120-015013-ab-c66e`, L `1120-015014-ab-f024`, R `1120-015014-ab-2a77`. No map-seat reaches p_max < 0.05.

| map-seat | choice (raw, p_max) | confirmation | verdict |
|---|---|---|---|
| RUN A, KP | +0.333 [+0.180, +0.487], 0.001 (21005) | +0.233 [+0.107, +0.360] (21006, 60 cells, `1120-014142-ab-dd56`) | entry |
| RUN B, KP | +0.167 (R +0.200, 0.043) | +0.150 [+0.038, +0.262] (same run) | entry |
| houseofcards A, KP | +0.133, 0.26 | +0.133 [−0.017, +0.284] (21006); +0.125 [+0.017, +0.233] (21007, 120 cells, `1120-015006-ab-ea9e`) | entry |
| houseofcards B, KP | +0.133, 0.24 | +0.133 [−0.038, +0.304] (21006); −0.025 [−0.132, +0.082] (21007) | no |
| squaresaremodernart B, KP | +0.233, 0.09 | +0.083 [−0.087, +0.253] (21006) | no |
| worldshardestgame A, KP | +0.200, 0.08 | +0.083 [−0.066, +0.233]; seat B −0.133 [−0.261, −0.006] (21006) | no |
| therainbow A, KP | +0.167, 0.11 | +0.050 [−0.068, +0.168] (21006) | no |
| mushroom B, L | +0.200, 0.06 | +0.067 [−0.064, +0.197] (21006, `1120-014142-ab-e4b3`) | no |
| kr_m2b's five best (KP: starvation B, cheesefarm B, hatefullattice A, FreeTheCheese A, tiny B) | +0.133 to +0.200, 0.06–0.11 (21008) | −0.167 to +0.067, none above 0; hatefullattice A −0.167 [−0.308, −0.026], cheesefarm A −0.133 (21009, `1120-021347-ab-05cf`) | no |

On main with the third book (5190e18: bed `CM_LANE_MEMORY`, RUN A `CM_CAT_HUNT_START` 500, cheddarchokehold
`MINES_KING2` 3), matrix `kr_m2c` (bed, cheddarchokehold, RUN; 40 cells, seed base 21010; ref `1120-021834-ab-e199`, KP
`1120-021936-ab-def3`, L `1120-022017-ab-1b71`, R `1120-022154-ab-ae0d`):

| map-seat | choice (raw, p_max) | confirmation | verdict |
|---|---|---|---|
| RUN A, KP (on the cat hunt) | +0.025 (21010) | +0.140 [+0.061, +0.219] (21011, 100 cells, `1120-021822-ab-8c39`) | entry holds |
| RUN B, KP | +0.075 (21010) | +0.170 [+0.091, +0.249] (same run) | entry holds |
| cheddarchokehold B, R (`KING_HAND` 1) | +0.225 [+0.087, +0.363], 0.027 | +0.200 [+0.082, +0.318] (21012, 80 cells, `1120-022500-ab-77b2`); seat A −0.075 [−0.166, +0.016] | entry |
| bed, any | KP A −0.675, B −0.425; L, R within ±0.075 | | no: nothing adds to the lane memory |

Proposed entries (book 4):

| map | seat | set | value | evidence |
|---|---|---|---|---|
| RUN | A | `KING_PATH` | 1 | `1120-011719-ab-619e` (choice, 21005), `1120-014142-ab-dd56` (21006), `1120-021822-ab-8c39` (21011, on the cat hunt) |
| RUN | B | `KING_PATH` | 1 | same |
| houseofcards | A | `KING_PATH` | 1 | `1120-011719-ab-619e` (21005), `1120-014142-ab-dd56` (21006), `1120-015006-ab-ea9e` (21007) |
| cheddarchokehold | B | `KING_HAND` | 1 | `1120-022154-ab-ae0d` (choice, 21010), `1120-022500-ab-77b2` (21012) |

- EscapeTheNight B and pocketsofcheese B: KP3 adds nothing over h7's right hand (+0.067, +0.033 on 30 cells); left
  loses there (−0.567, −0.200). AT's KP3 gains on them (+0.72, +0.31) were over the old left hand.
- thehallway B, therainbow B (h7's losses): left reads +0.033 and 0.000. No entry.
- bed: KP and R lose seat A (−0.533, −0.500), as MS found; kingofthehill B: KP −0.300. AceOfSpades, asabovesobelow,
  kingofthehill, QueenOfHearts, diamond, longwayaround, popthecork: no candidate passes.
- `KING_PATH` 0 plays as main: 1,000 `goal` cells, all identical (`1120-011153-ab-7024`, seed base 21004).

KR2, the hand of a chirality-1 team-B king on ROTATIONAL maps by the half it starts in: `KING_HAND` 1 on the 33
ROTATIONAL `goal` maps whose team-B king has chirality 1 and starts in the right half, 100 cells a map, seed base
21013, against main 5f28842b (`book matrix kr2_rotb`; runs `1120-042309-ab-6f2c`, `1120-042412-ab-458c`;
`D:pairruns.py`): seat B +0.006 [−0.004, +0.016] on 3,300 games (1,597 changed), seat A +0.006. No seat moves; the
largest per-map reads (cheeseysquare −0.15, externalsustenance +0.09 on 100 games) are within 2.5 standard errors of
0 over 33 maps. Closed. A chirality-0 team-B king learns the symmetry only when the rats find it, too late for its
first routes (h5, §2), so a left-half rule for it cannot act at round 1; those map-seats stay book entries (SG's
thehallway B and squaresaremodernart B).

## 5. Data and tools (`~/battlecode26-data/scratch/kr/`)

- `MapChir.java` (`java -cp cls_tools:$(cat cp.txt) MapChir maps MAP...`): symmetry and each team's starting king (id,
  location, chirality) from the map files; `mapchir.txt`, `mapchir.json` for all of `maps/`.
- `chir.py RUN`: a paired run by symmetry, team A's chirality and our seat (games, changed, ours, ref).
- `pairruns.py REF ARM [SEAT]`: two runs on the same cells (a matrix's ref and variant), per seat and map, with a
  bootstrap over maps.
- `chg.py RUN`: the same by symmetry and seat only. `mapseat.py RUN [MIN_CHANGED]`: per map-seat, sorted, with a
  paired 95% interval. `runs.txt`: every run of this package.
