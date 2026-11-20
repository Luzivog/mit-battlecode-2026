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
- Default 0: main's instructions plus one method never called (javap); null run on 1,000 `goal` cells: 995 identical,
  −0.001 [−0.003, +0.001] (`1119-213204-ab-b4e3`).

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
| h6 | 6 | 21002 | 1,000 | TODO | `1119-225802-ab-2392` |
| **h7, confirmation** | 7 | 21003 | 3,000 | TODO | `1119-232537-ab-600c` |

## 4. Data and tools (`~/battlecode26-data/scratch/kr/`)

- `MapChir.java` (`java -cp cls_tools:$(cat cp.txt) MapChir maps MAP...`): symmetry and each team's starting king (id,
  location, chirality) from the map files; `mapchir.txt`, `mapchir.json` for all of `maps/`.
- `chir.py RUN`: a paired run by symmetry, team A's chirality and our seat (games, changed, ours, ref).
- `chg.py RUN`: the same by symmetry and seat only. `runs.txt`: every run of this package.
