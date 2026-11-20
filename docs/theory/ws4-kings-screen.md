# WS4: the kings' and timing constants, screened wide

Package WS4 (wave 6, the book), by WS's method (docs/theory/ws-wide-screen.md): one `tools/book/book matrix` over the
50 `goal` maps holding 607 of the sixth book's 722 losses (`~/battlecode26-data/scratch/integrator/rank-book6.json`,
run `1120-071459-ab-d6bd`), 40 cells a map, seed base 26017, against main `31f0a2aa`; a pick passes at p_max < 0.05
or reads ≥ +0.1 for the same value on both seats; confirmed on 26018 at 100 cells against main `3bd51f40` (the book 6
bot), the map alone, both seats played, on top of the map's entries. "Seat" is ours. Data:
`~/battlecode26-data/scratch/at/ws4/` (`maps.txt`, `fit.txt`, `runs.txt`).

## 0. Entries

Chosen from 17 candidates (the best of 17 on the map-seat, or the same value on both seats).

| map | seat | constant | value | chosen (26017, 40 cells) | confirmed (26018, 100 cells) | run |
|---|---|---|---|---|---|---|
| kingofthehill | A | `DIRT_ROUND` | 0 | A +0.100, B +0.125 (same value both seats) | **A +0.130 [+0.010, +0.250]** (B +0.070 [−0.058, +0.198]; both +0.100 [+0.012, +0.188]) | `1120-114246-ab-4473` |
| TheHeist | B | `IDLE_GOHOME` | 0 | A +0.150, B +0.100 (same value both seats) | **B +0.100 [+0.002, +0.198]** (A +0.040 [−0.068, +0.148]), on top of `PM_SYM` 1 and `PM_COOP_HOLD` 1 | `1120-114248-ab-4764` |

Both are final constants today: booking them drops `final`; each is read once (`Pathfinder`'s dirt gate,
`MinerStrategy`'s idle go-home), a plain constant use, so the instruction count is unchanged.

## 1. The screen

Candidates (main in brackets): `LOCK_IN_ROUND` 500, 1100 [800]; `LOCK_IN_MIN_CHEESE` 500 [1000]; `LOCK_IN_MIN_RATS` 8,
16 [12]; `KING_TARGET` 2, 4 [3]; `COOP_ROUND` 50, 200 [100]; `JOIN_DIST` 45 [25]; `DIRT_ROUND` 0, 300 [100];
`EXPLORE_KING_DSQ` 0, 100 [36]; `IDLE_GOHOME` 0, 100 [50]; added: `CM_KING2_ROUND` 150 [never] (the siteless second
king at a later round than WS's 30). Every one is read by live code. The lock-in constants were not read on
where_are_you and longwayaround (PM).

- Mean over the 100 map-seats: every candidate within −0.022 and +0.004 except `CM_KING2_ROUND` 150, −0.081: the
  siteless second king loses at r150 as at r30.
- Picks: `DIRT_ROUND` 0 on asabovesobelow (B +0.275, p_max 0.005; A +0.175), houseofcards B (+0.250, 0.020), diamond
  (+0.125, +0.100), kingofthehill (+0.100, +0.125); `IDLE_GOHOME` 0 on TheHeist (+0.150, +0.100); `EXPLORE_KING_DSQ` 100
  on closeup (+0.175, +0.100). Near: `DIRT_ROUND` 0 on dirtfulcat A, hellofromtheotherside A, fullofsecrets B
  (+0.15 to +0.20, p_max 0.056–0.067), not confirmed.
- τ (empirical Bayes) 0.046 over 1,700 estimates: little map-seat structure for these constants beyond the dirt round.

Runs: main `1120-080200-ab-cf4b` and the 17 variants listed in `matrix.log` (campaign `ws4`), all kept.

## 2. Failed on fresh seeds (26018, 100 cells)

| map | seat | constant | chosen | confirmed | run |
|---|---|---|---|---|---|
| asabovesobelow | A, B | `DIRT_ROUND` 0 | B +0.275 (p_max 0.005), A +0.175 | A −0.110 [−0.188, −0.032], B +0.050 [−0.069, +0.169] | `1120-114236-ab-3604` |
| houseofcards | B | `DIRT_ROUND` 0 | +0.250 (p_max 0.020) | +0.030 [−0.093, +0.153] | `1120-114238-ab-de18` |
| diamond | A, B | `DIRT_ROUND` 0 | +0.125, +0.100 | A −0.050, B +0.030 | `1120-114245-ab-92b7` |
| closeup | A, B | `EXPLORE_KING_DSQ` 100 | +0.175, +0.100 | A −0.050, B −0.010 | `1120-114248-ab-4a2c` |

Of seven map-seats with p_max < 0.1 or a both-seats read, two confirmed; the strongest pick of the screen
(asabovesobelow B, p_max 0.005) did not.
