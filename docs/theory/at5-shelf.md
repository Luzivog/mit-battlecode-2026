# AT5: what was left on the shelf

Package AT5 (wave 6, the book). Two steps: re-confirm WS4's `DIRT_ROUND` 0 on kingofthehill on book 7, then revive the
past arms that read well on the top-60 map-seats without an entry. Data and tools: `~/battlecode26-data/scratch/at/at5/`
(`runs.txt` every run, `free_mapseats.json`, `fit.txt`, `FirstTurn.java` the first-turn bytecode reader, `bccmp.sh` the
javap comparison). "Seat" is ours.

## 0. Lines

| map | seat | line | chosen | confirmed |
|---|---|---|---|---|
| kingofthehill | both | `DIRT_ROUND` 0 | K 17, 26017 (WS4) | book 6, 26018 `1120-114246-ab-4473`: A +0.130, B +0.070; book 7 (main `de7cba2d`), 26019, 150 cells `1120-115328-ab-47a7`: A +0.067 [−0.013, +0.147], B +0.133 [+0.058, +0.209], both +0.100 [+0.045, +0.155] |
| jail | A | `KING_PATH` 1 | K 1, 26021, 60 cells `1120-121410-ab-cda7`: A +0.150, B +0.167 (against main `58df64c6`) | 26022, 150 cells `1120-122216-ab-5ed6`: A +0.167 [+0.073, +0.260] (B +0.013) |
| externalsustenance | both | `AT_MAP` 1 with `AT_WALL`, `AT_DIRT` (the map's walls and round-1 dirt) | K 2, 26023 (`at5m`): A +0.250 (p_max 0.002), B +0.225 (0.006) | 26026, 100 cells, against main `93dde2c0` + book 9's lines (`1d2d72d2`), arm `44f809b6`, `1120-154307-ab-c5f4`: A +0.210 [+0.108, +0.312], B +0.080 [+0.020, +0.140] (also 26024 at the 2,500 gate, `1120-132828-ab-0053`: A +0.260, B +0.053) |

## 1. The atlas query

Of the top 60 map-seats of `rank-book6.json`, 38 had no entry in main's book or `book8-lines.jsonl`. The past arms that
are not a switch today and read ≥ +0.15 there were MS's map-knowledge oracles and the KP planner variants
(docs/theory/ms-map-seats.md §4, §7); at ~11 games a map-seat they read like MS's null (salted main, which itself reads
≥ +0.15 on 10 of the 38). Their split-half gating across all map-seats did show winners: KP v2 +2.6 pp (null −0.2 ± 0.8),
ms_okb v2 (the true static map in every robot) +1.6 pp (null 0.0 ± 0.6). Screened on their own commits against their
base `419fa329`, 40 cells a map, 26020:

- KP v2 (`72211bd9`, `1120-115614-ab-4238`): only EscapeTheNight B +0.625 and pocketsofcheese B +0.40, which `KING_HAND`
  7 already fixes on main; the rest −0.075 to +0.125.
- ms_okb v2 (`35332f69`, `1120-115619-ab-a786`; again on 26021 `1120-121420-ab-2a68`): jail +0.43 / +0.15 then +0.13 /
  +0.23, shell 0.00 / +0.18 then +0.22 / +0.15. On today's main `KING_PATH` 1 alone carries jail (the line above);
  on shell it reads nothing (+0.05, −0.07).

## 2. `AT_MAP`: the map from the book

`Tune.AT_MAP` (final 0; a book switch): `MapBook.load()` copies the book's `AT_WALL` and `AT_DIRT` tile sets (row
masks) into `CellTracker` (`loadRows`, every cell seen, a wall not passable) at the end of a robot's first turn with
more than 5,000 bytecodes left. Code: branch `t3/at5-shelf`, `ec8bf465`. Default: javap of every class against main
differs only by additions (`Book`'s two fields, `CellTracker.loadRows`, `Tune.AT_MAP`, the new `MapBook`);
`RobotPlayer` is identical. To switch it on for a map-seat the book needs three lines: `AT_WALL` and `AT_DIRT` data
(the walls and round-1 dirt from `~/battlecode26-data/scratch/ms/goalmaps.jsonl`, as in commit `44f809b6`) and
`"set": "AT_MAP", "value": 1`.

First-turn bytecode (FirstTurn over the replays; limits 17,500 a baby rat, 20,000 a king):

| | load on the first turn (screen) | gate 2,500 (`1120-132828-ab-0053`, 300 cells) | gate 5,000 (`1120-153132-ab-7757`, 40 cells) | main, same cells as the gate-5,000 run |
|---|---|---|---|---|
| baby rats' first turns over the limit | 9.6% (31 maps) | 6.9% | 0.35% (14 / 4,023) | 0.42% (15 / 3,554) |
| rats born after r100, first turn over | 9.7% | 5.3% | 0.24% (2 / 829) | 0.00% (0 / 337) |
| all turns over | 0.092% | 0.25% | 0.069% | 0.085% |
| king's worst turn in r1–3 | 17,731 (mushroom) | 19,790 | 16,997 | 16,261 |

The load costs ~3k bytecodes; at the 5,000 gate it never overruns. The king's +736 in r1–3 is the book's round-1
scan (externalsustenance A has no entry on main).

Screen `at5m` (31 maps of the free map-seats, 40 cells, 26023, from trial `6af9e3c3`; main `1120-122642-ab-b4c3`,
`AT_MAP` 1 `1120-122935-ab-a5b5`, with `KING_PATH` 1 `1120-123210-ab-bcd6`): off its picks `AT_MAP` loses on average
(the run lost 692 games to main's 440): it is a per-map line only.

## 3. Failed

On 26026 (`1120-154307-ab-c5f4`, 100 cells, against main + book 9's lines): gridlock A `AT_MAP` 1 +0.070 [−0.019, +0.159]
(screen +0.150, p_max 0.040); cheeseysquare A +0.090, B +0.000 (screen +0.125 / +0.050); diamond A +0.010, B +0.010
(+0.075 / +0.325); houseofcards A −0.120 [−0.245, +0.005], B −0.040 (+0.075 / +0.225); mushroom with `KING_PATH` 1 A −0.020,
B +0.000 (+0.150 / +0.025).
