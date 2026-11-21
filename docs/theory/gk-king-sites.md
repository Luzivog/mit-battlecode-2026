# GK: king sites in general

Track GK of the clean program (docs/program.md): the book's first-king sites (`KING1_SITE`, `CM_KING1_SITE`) and early
second kings (`KING2_SITE`, `CM_KING2_ROUND`, `MINES_KING2`) replaced by rules the robots compute from what they see.
Data and tools: `~/battlecode26-data/scratch/gk/` (`D:`; `runs.txt`, `py/`). Seed bases 31000-31999.

## 1. The evidence, re-read

Every recorded first-king site screen (CM4, CM5, CM6, CM7, PM3: arms `CM_KING1_SITE` k against the plain reference
on the same cells; `D:py/scanruns.py`, `D:py/k1data.py`): 461 (screen, map-seat, site) effects on 104 map-seats of
53 maps, 40-60 cells each, each site's features from the map file (`D:py/sitefeat.py`).

- Within a map-seat, the sites beside a mine that win are central to our side's mines: the mean rat-step distance
  from the site to our side's mines correlates −0.44 with the effect (260 rows at a mine, 75 map-seats with two or
  more); steps from the start −0.27; the margin of the site's mine (enemy steps − ours) +0.23. Mines within
  Chebyshev 4, walls, openness and cat paths: |r| ≤ 0.11.
- Choosing per map-seat the mine site with the lowest score, against main on the same cells (`D:py/k1choose*.py`):

| score (lowest wins) | mean effect, 104 map-seats | 95% (maps) |
|---|---|---|
| oracle: the best screened mine site (biased up) | +0.061 | |
| mean rat steps to our side's mines + 0.5 × king steps from the start (true sides) | +0.036 | [+0.011, +0.062] |
| mean Chebyshev to our side's mines + 0.5 × Chebyshev from the start (true sides) | +0.029 | [+0.004, +0.057] |
| mean over every mine of min(Chebyshev, 30) + 0.5 × Chebyshev from the start (no sides) | +0.031 | [+0.006, +0.057] |
| the same, our side under every symmetry still possible (round 1) | +0.011 | [−0.026, +0.043] |
| the nearest mine by king steps | +0.017 | [−0.020, +0.050] |
| a random mine site | −0.010 | [−0.051, +0.027] |

  These map-seats are the most-lost ones CM chose, and the effects were measured against books 6-8, so the rule is
  worth at most about +0.03 a map-seat where main loses the income race.
- The siteless early second king (`CM_KING2_ROUND` 30 and 150, 98 and 96 map-seats, `1120-050157-ab-a4d0`,
  `1120-082642-ab-05d2`) reads −0.087 and −0.082; the two screens agree per map-seat (r 0.92 over 75): the round does
  not matter (the rat floor binds), the map-seat does. Mines within 10 king steps of our start predict loss (r −0.29),
  mine density gain (+0.19); no class gains on average.

## 2. The early second king, re-read

Every recorded run where a challenger's effective second-king setting (`CM_KING2_ROUND` from Tune or its book, with
`KING2_SITE`) differs from its reference's on a map-seat (`D:py/k2all.py`): 629 (run, map-seat) effects on 124
map-seats, 372 of them with a book site (WS5-WS8, CM2, AT3, PM6, the confirmations); features in `D:py/k2anal.py`.

- Sited second kings average −0.059. Per map-seat (mean over its screened sites and rounds), the maps whose cats walk
  through a king's start (W4's `cat clock` ≥ 0: tminusten, sunandmoon, peaceinourtime, groveisbeingdesecrated,
  externalsustenance, directconfrontation) read +0.178 over 9 map-seats; the rest −0.203 over 57. Cat count (2: −0.175,
  4: −0.116, ≥ 6: −0.049), mines and area separate nothing that gains.
- Within a map-seat, sites at mines nearer the middle (low margin of enemy over our steps, r −0.47) and later rounds
  (r +0.21) read better.
- The robots do not know the cats' loops. A cat in the first king's view is a poor proxy for them (clean main's
  baseline `1121-050414-ab-05ee`, 760 games, `D:java/CatSeen.java`): by round 100 a cat comes within r² 25 of our king
  in at least half the games of 47 map-seats on 29 maps, nearly all of them small maps where the king walks into a cat
  (bed, canyoudig, dirtfulcat, shell, squaresaremodernart); by round 40, 15 map-seats, two of them cat-clock maps.

## 3. The first king's site at run time (`GK_SITE`)

`GK_SITE` 1: the first king's mine target is the known mine (`Game.knownCheeseMines`, at most 32) with the lowest
score, the mean over the known mines of min(Chebyshev, `GK_CAP`) plus `GK_START_W`/100 per Chebyshev step from its
start; it moves to a new best only when it scores `GK_HYST` lower and keeps its site from `GK_END`; `GK_MAXD` bars far
sites, `GK_MAX_AREA` limits the maps. The sums grow by 2n distances a new mine (≤ 32 a turn in practice); off, the
class is main's but for new members. Mechanism: rats deliver to the nearest king, so a king central to the mines it
knows shortens their trips (CM4's site entries won through income).

Screens: 1,000 `goal` cells a variant, seed base 31002, paired with clean main (ref `1121-034626-ab-e5b2`, 0.834);
`D:py/pair.py`.

| variant | GK_CAP | GK_START_W | other | cells | effect [95%] | run |
|---|---|---|---|---|---|---|
| a | 20 | 50 | | 1,000 | +0.009 [−0.011, +0.029] | `1121-034626-ab-51f7` |
| b | 30 | 50 | | 1,000 | +0.012 [−0.006, +0.030] | `1121-034628-ab-8bce` |
| c | 20 | 100 | | 1,000 | +0.004 [−0.016, +0.025] | `1121-050647-ab-f0fc` |
| d | 30 | 25 | | 1,000 | +0.010 [−0.016, +0.031] | `1121-034801-ab-8b53` |
| e | 40 | 25 | | 1,000 | +0.008 [−0.018, +0.031] | `1121-095135-ab-7ad4` |
| g | 30 | 25 | `GK_MAXD` 15 | 997 | +0.006 [−0.011, +0.023] | `1121-095135-ab-93f5` |
| i | 30 | 25 | `GK_END` 100 | 640 | +0.005 [−0.014, +0.024] | `1121-095136-ab-3d02` |
| f | 30 | 0 | | 223 (stopped) | +0.011 | `1121-055702-ab-3aab` |
| h | 30 | 25 | `GK_HYST` 300 | 248 (stopped) | +0.022 | `1121-055802-ab-4848` |

- Every variant reads +0.004 to +0.012 with an interval across 0: a real effect, if any, of about +1 pp.
- It moves map-seats a lot both ways (a and b agree on them): losses on wallsofparadis A and B (−0.36 to −0.40: our
  nearest mine is 27 steps off, the king walks into the central cluster of four), streetsofnewyork A, cheeseysquare B,
  seepastyourgreed A, cheesebottles A; gains on Meow B, t_minus_five_minutes A and B, dirtfulcat A and B, therainbow B.
- By map size it reads L −0.01, M +0.015, S +0.04, but on seed parity halves S is +0.06..+0.08 in one and ±0.00 in
  the other: no size gate. A margin term (mean over the possible symmetries of enemy minus our distance) adds nothing
  offline.

## 4. A cat-triggered early second king (`GK_K2CAT`), closed

`GK_K2CAT` 1: a lone king that sees a cat by round `GK_K2CAT_END` (and has lost `GK_K2CAT_HP` HP) forms a second king
from `GK_K2_ROUND` as `CM_KING2_ROUND` does, at the known mine on our side (under every symmetry still possible)
nearest it beyond Chebyshev `GK_K2_MIND`, with no cat footprint it saw in the mine's 5x5 (`CatLanes`) and no king's
claim (`gkK2Site`). Same cells as §3:

| variant | trigger | from | cells | effect [95%] | run |
|---|---|---|---|---|---|
| k | a cat in view by r100 | r30 | 66 (stopped) | −0.053 [−0.106, −0.015] | `1121-075505-ab-d3dc` |
| l | by r300 | r30 | 105 (stopped) | −0.057 [−0.101, −0.019] | `1121-075543-ab-c2ad` |
| m | by r100 | r80 | 39 (stopped) | −0.064 | `1121-075734-ab-8830` |
| n | by r150, 40 HP lost | r30 | 1,000 | −0.012 [−0.024, −0.001] | `1121-095136-ab-a282` |
| o | by r300, 100 HP lost | r30 | 71 (stopped) | −0.028 | `1121-095136-ab-200c` |

A second king insures against a cat only where the cats' loops cross the king's start; the robots see cats near their
king on 29 maps by r100, mostly small maps where the formation costs the army and the bank (§2).

The cat trigger on large maps only (`GK_K2CAT_AREA` 1601), against GK6's ref `1121-104117-ab-e2ba`, 1,000 cells:

| variant | trigger | effect [95%] | where it moved games | run |
|---|---|---|---|---|
| c40 | a cat in view by r40, ≥ 1,601 tiles | +0.0025 [−0.0025, +0.0090] | tminusten A, B +0.20, sunandmoon A, B +0.20, shell A −0.20 | `1121-104306-ab-0010` |
| c60 | by r60, ≥ 1,601 tiles | −0.0030 [−0.0119, +0.0060] | the same, plus keepout A −0.18, therearenowalls A −0.27, shell B −0.30 | `1121-104349-ab-0d64` |

On clean main's baseline the r40 condition holds on three `goal` maps only (tminusten in all games, sunandmoon and
shell in 3 of 8): under the program's five-map floor, so not a rule; widening it to r60 adds maps where it loses.
On shell and sunandmoon the king first sees the cat at r38-48, 4-7 steps from its start, in both: nothing the king
sees separates them.

## 5. Holding the start until a mine is known (`GK_WAIT`), closed

With no mine known, main's king walks to the centre target; `GK_WAIT` T holds it at its start until a mine is known or
round T. Same cells, ref `1121-104024-ab-6e9b`: T 50 −0.001 [−0.023, +0.021] (`1121-104154-ab-88c5`), T 100 +0.002
[−0.020, +0.023] (`-261d`), T 300 −0.010 [−0.033, +0.012] (`-7637`). The centre walk is neutral.

## 6. Conclusion

No general king-site or second-king rule found. The book's king entries won on particular mines and particular cat
maps; on `goal` the robots' own knowledge does not name them: the site score that picks +0.03 in hindsight on CM's
screened map-seats confirms at −0.011 on 3,000 cells, and the one observable that tracks the cat maps (a cat in view
early on a large map) holds on three maps. The switches stay in the code, off (`GK_SITE`, `GK_WAIT`, `GK_K2CAT` with
`gkK2Site`, which GC's pocket trigger reuses).
