# AT3: the five maps where our king starves

Package AT3 (wave 6, the book): warhammer, moonlitriverpark, cheeseysquare, shell, jail, where AT2 found the lone
king starving after the bank runs out (docs/theory/at2-maps.md §1). Main `5cc6d00a` against awubot; "seat" is ours.
Data and tools: `~/battlecode26-data/scratch/at/at3/` (`runs.txt` every run; `mc.jsonl`, `w3s.jsonl`, `kd.jsonl` the
baseline's readouts by W1's `MineControl`, W3's `W3Stats` and KD's `KDStats`; `mines.py` the per-mine table).

## 0. Entry

| map | seat | entry | chosen | confirmed |
|---|---|---|---|---|
| warhammer | A | `CM_KING2_ROUND` 30, `KING2_SITE` [(11,8), (21,18)] | K 2 (rounds 30, 100), 26014, against main: +0.108 [+0.029, +0.188], `1120-064621-ab-bffa` | 26016, on top of the sixth book's `SPAWN_CAP_SMALL` lines (the caps alone as reference): +0.040 [+0.009, +0.071], `1120-065040-ab-f4b0` |

Seat B's second king (`KING2_SITE` [(8,11), (18,21)]) read +0.083 [+0.024, +0.143] against main but +0.013 [−0.024,
+0.050] on top of the caps: not an entry. With the caps warhammer wins 0.96 (A) and 0.967 (B) on 26016: the map is
done.

## 1. Baseline

Main, 80 cells a map, seed base 26013, every replay kept (`1120-063703-ab-5213`): 633–167 (0.791).

| map | A | B | what the replays show |
|---|---|---|---|
| warhammer | 0.79 | 0.79 | the second-king race decides: with 2 kings to awubot's 1 at r300 we win 33 of 33, with 1 to its 2 18 of 41. Our second king forms at r120–360 by the diagonal mines (8,8), (21,21) (on the symmetry axis, contested), awubot's at r130–200; both first kings stay in their corners |
| shell | 0.78 | 0.66 | no second kings; in losses awubot picks 100–150 more in r0–300 at the two centre mines and at its own; our king wanders between them |
| jail | 0.76 | 0.80 | no second kings; losses lose the contested mines (52,4), (52,35) by ~130 each |
| moonlitriverpark | 0.82 | 0.81 | kings stay home by their own mine; losses lose the far mines (5,3), (54,35) by ~100–150 |
| cheeseysquare | 0.81 | 0.89 | losses come late (r890–2000); each side keeps its own mines |

## 2. Trials

Book entries on branches never merged (`t3/at3-trial-*`), the map alone, both seats, against main.

| trial | maps, seed base | result | run |
|---|---|---|---|
| second king r30 at the diagonal mines | warhammer, 26014 | A +0.108, B +0.083 | `1120-064621-ab-bffa` |
| same from r100 | warhammer, 26014 | A +0.093, B +0.077 | `1120-064625-ab-0a98` |
| second king r30 beside the contested mine on our side (shell A (17,29), B (32,20); jail A (49,35), B (49,4); moonlitriverpark A (5,4), B (54,34)) | shell, jail, moonlitriverpark, 100 cells a map, 26015 | jail −0.265 [−0.344, −0.186], moonlitriverpark −0.425 [−0.505, −0.345], shell −0.460 [−0.551, −0.369] (both seats) | `1120-064707-ab-15be` |

As CM2 found on kingofthehill, therearenowalls and QueenOfHearts: where each side holds its own mines, a second king
at the contested one starves both.

## 3. Stopped

- warhammer: at 0.96 with the caps; the rest is under a game a seat in 30.
- shell, jail, moonlitriverpark, cheeseysquare: the losses are the mine race at the shared mines; the switches and
  data that exist (caps, spawning gates, exploration share, the second king with or without a site, lane memory) do
  not move it, and the contested-mine king loses 0.27–0.46. A remedy needs new code (the miners' targets or the first
  king's site as book data), and these four maps hold about 0.3 pp of `goal` between them at today's rates.
