# CM6: the high-loss map-seats without a lever

Package CM6 (wave 6, the book). Main `58df64c6` (book 7) against awubot; book 8 adds nothing on these maps. Data and
tools: `~/battlecode26-data/scratch/cm/` (`D:`; `king1all.py` every reachable mine as a first-king site,
`entries6.jsonl` the lines, `runs.txt`). "Seat A/B" is our seat.

## 0. Summary

| map | seat | entry | chosen from | confirmed |
|---|---|---|---|---|
| asabovesobelow | A | `KING1_SITE` (40,36), `CM_KING1_SITE` 1: beside its fifth-nearest mine | `cm6a`, 23054, 40 cells, 6 candidates (every reachable mine, nearest first): +0.275 [+0.11, +0.44], adj +0.20, p_max 0.021 (`1120-122452-ab-2332` vs `1120-120839-ab-f43a`) | 23055 `1120-123411-ab-14cb`: +0.13 [+0.06, +0.20] (14/1) |

Stopped, with what the losses are made of: jail A (the planner, AT's `KING_PATH` 1, already does what a site would),
therainbow B (the site raises income, too little to confirm), gridlock and Excavation (the middle-mine fight lost by
r200), moonlitriverpark, cheeseysquare, shell A (no mine within the king's reach), holeinthewall, AceOfSpades B,
asabovesobelow B, QueenOfHearts B (no site or switch passes, or the pick fails on fresh seeds).

## 1. Baseline

Main, seed base 23053, 60 games a map-seat, every loss kept (`1120-120705-ab-9ad1`), read by KDStats (king HP by
source) and MineControl (cheese picked per mine and window):

| map-seat | win | losses | class | lone king dies (median) | net cheese a loss, r0–300 (us − them, all mines) |
|---|---|---|---|---|---|
| asabovesobelow B | 0.63 | 22 | STARVE 11, COMBAT 10 | r452 | −813 |
| QueenOfHearts B | 0.72 | 17 | STARVE 9, C+s 4 | r1023 | +6 |
| AceOfSpades B | 0.73 | 16 | STARVE 8, points 4 | r373 | −1,089 |
| jail A | 0.73 | 16 | STARVE 9, COMBAT 6 | r582 | −316 |
| holeinthewall A | 0.75 | 15 | STARVE 8, COMBAT 6 | r451 | −466 |
| shell A | 0.77 | 14 | STARVE 12 | r498 | −409 |
| cheeseysquare A | 0.78 | 13 | STARVE 11 | r1441 | −442 |
| therainbow B | 0.78 | 13 | STARVE 6, C+s 5 (two kings, both dead) | r238 | −623 |
| Excavation A / B | 0.82 / 0.85 | 11 / 9 | COMBAT 8 / 8 | r209 / r207 | −548 / −451 (middle mines) |
| moonlitriverpark A | 0.82 | 11 | STARVE 10 | r689 | −406 |
| gridlock A / B | 0.88 / 0.82 | 7 / 11 | COMBAT | r183 / r181 | −789 / −879 (all mines in the middle) |
| cheddarchokehold A / B | 0.97 / 0.90 | 2 / 6 | STARVE | | |

In every income race we out-pick awubot at the mines nearer our king; the deficit is how much more it takes from its
own side (and the middle). QueenOfHearts B starves at even income.

## 2. Levers

- **Every reachable mine as the first king's seat** (`cm6a`, `t3/cm6-trial-all`, `CM_KING1_SITE` 1–6 over
  `D:king1all.py`'s list, the existing site entries left in; ref `1120-120839-ab-f43a`, k1 `-9385`, k2 `-020d`,
  k3 `-8234`, k4 `-b57e`, k5 `-2332`, k6 `-efea`): passes on jail A (8,36) +0.30, asabovesobelow A (40,36) +0.275,
  therainbow B (22,3) +0.225; asabovesobelow B best +0.20 (p 0.14); nothing on holeinthewall, Excavation, gridlock,
  AceOfSpades B, QueenOfHearts B. Sites lose up to −0.55 (jail A beside (8,3)).
  cheeseysquare, cheddarchokehold and moonlitriverpark have no mine the king can stand beside; shell A only the
  enemy-side centre mine.
- **Confirmation** (23055, `1120-123411-ab-14cb`, every replay kept; jail A on top of AT's `KING_PATH` 1, in both arm
  and reference): asabovesobelow A +0.13 [+0.06, +0.20]; jail A −0.02 [−0.14, +0.10] (its second failure after CM5's
  +0.06); therainbow B +0.09 [−0.02, +0.20].
- **After r300 where the site did not pass** (that run's paired replays, 100 games a map-seat): jail A seated at (8,36)
  picks less in r0–300 (1,184 against 1,242), its kings die earlier (median r497 against r664) and twice as many of its
  losses are rat kills (8 against 4): the king at the mine is caught; the planner already gives jail A what the seat
  would. therainbow B seated raises the r0–300 net (+427 against +264) and cuts starvation losses (3 against 7), wins 88
  against 79: real in kind, under what 100 games resolve.
- **Existing switches where a seat cannot help** (`cm6b`, 23056, 40 cells, 5 candidates, ref `1120-123448-ab-f9a1`,
  siege `-4b4b`, cap60 `-f7ba`, k2r30 `-7cd0`, kpath `-b32f`, mk3 `-dbbe`): only QueenOfHearts B `KING_PATH` 1 passes
  (+0.225 [+0.08, +0.37], adj +0.16, p_max 0.023) and fails its confirmation (23057 `1120-125412-ab-295c`: +0.05 [−0.06, +0.16]); `SPAWN_CAP` 60 loses
  −0.40 to −0.525 on Excavation A and moonlitriverpark, `PM_SIEGE` −0.20 on gridlock B; nothing passes on gridlock,
  Excavation, moonlitriverpark, cheeseysquare, shell, holeinthewall.

## 3. CM7: every reachable mine on the rest of the top 60

`cm6a`'s asabovesobelow A seat is its fifth-nearest mine, a seat the four site types of CM4/CM5 do not name. CM7 plays
every mine within the first king's reach (`D:king1all.py`, nearest first, 7 candidates: with main's run that is the
8-run cap; only TheHeist A had an eighth) on the top 60 map-seats of `rank-book6.json` that `cm6a` did not play, have
no first-king site in main's book and were not read as out of a seat's reach. `book matrix cm7a`, against main
`bbafb1f0` (book 8), the map-seats' other entries on, 40 cells a map, seed base 23058 (ref `1120-130409-ab-eb72`,
k1 `-a714`, k2 `-89d1`, k3 `-0a04`, k4 `-7246`, k5 `-ba66`, k6 `-7d9f`, k7 `-6b4d`):

| map-seat | candidates | best | p_max |
|---|---|---|---|
| squaresaremodernart B | 6 | (6,13), second-nearest: +0.250 [+0.044, +0.456] | 0.073 |
| thehallway B | 4 | (5,33), nearest: +0.100 | 0.163 |
| where_are_you B | 1 | +0.075 | 0.196 |
| Nofreecheese A | 6 | (42,15): +0.150 | 0.260 |
| popthecork B, uneruesansfin B, dirtfulcat A, averyfineline B | 2–6 | ≤ +0.050 | ≥ 0.62 |
| diamond A, TheHeist A, shell A, closeup B | 1–7 | ≤ 0 | ≥ 0.86 |

No map-seat passes p_max < 0.05; nothing to confirm. Far seats lose heavily where they exist (Nofreecheese A −0.675,
uneruesansfin B −0.475 at its fourth to seventh). Left out before the screen: the `cm6a` maps; the map-seats with a
site; PM4's twelve (the rat exchange at our king); cheddarchokehold, cheeseysquare, moonlitriverpark, mincemeat A (no
mine within the king's reach); mushroom, EscapeTheNight B, houseofcards A, knifefight A, longwayaround (closed).

