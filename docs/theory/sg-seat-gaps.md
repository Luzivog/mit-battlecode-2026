# SG: seat gaps on eight maps

Package SG (wave 6): every map is symmetric and the seats are mirror images, so a gap between our two seats on one
map comes from something that is not mirror-symmetric. Source keys: `E:` engine 1.2.6
(`~/battlecode26-data/engine/ref/battlecode26/engine/src/main/battlecode/`); `C:` `src/challenger/`; `D:`
`~/battlecode26-data/scratch/sg/` (§6). "Seat A/B" is our seat.

## 0. Summary

1. **The cause on popthecork, jail and hellofromtheotherside, and most of diamond's: the rats' bug lookahead prefers
   its left-hand bug.** `C:Pathfinder.java.jinja2` `doLookahead` advances the left bug first when both have walked
   equally far, takes the left bug's arrival but never the right's (`else if(leftBug.arrived)` repeats the left), and
   breaks score ties to the right (§2). A mirror swaps the hands, so the two teams' rats leave a wall on opposite
   sides and the side that pays changes with the seat. awubot has the same code; in r1–20 both bots play the same
   game on the same seed, so the seat effect there is the shared code's.
2. **Ruled out by diagnostics that flip one thing (all on the same 22001 cells, §1):** turn order (king IDs swapped in
   the map file), sensing scan order (chiralities swapped), the team label (both kings' teams swapped), our king's
   hand (explains pocketsofcheese only), awubot's king's hand, the map centre `W/2, H/2` (rounded toward +x/+y, flipped
   in both bots).
3. **The switch `SG_LOOK_RIGHT`** (Tune.java, 0 = main, every instruction unchanged) mirrors each left-first rule.
   Turned on in our bot only, on the eight maps: popthecork B 0.38 → 0.92 (A 1.00 → 0.96), diamond B 0.48 → 0.79,
   jail A 0.71 → 0.88 (B 0.88 → 0.71), hellofromtheotherside A 0.73 → 0.83 (B 0.90 → 0.83).
4. Book entries and the general rule: §4.

## 1. The gaps and the diagnostics

Every run: challenger (main or a probe copy) against awubot (or a probe copy), plain, the same cells (seed base
22001, or the same seeds on patched maps), both seats; win rate of our seat A / seat B, 48 games a seat unless noted.

| run | knifefight | closeup | jail | wh.game | hello… | diamond | pockets | popthecork |
|---|---|---|---|---|---|---|---|---|
| main `1119-212637-ab-e8c2` (ref) | 0.88/0.98 | 0.50/0.79 | 0.71/0.88 | 0.65/0.79 | 0.73/0.90 | 0.94/0.48 | 0.94/0.67 | 1.00/0.38 |
| X: king IDs swapped (turn order) `1119-213221-ab-a750` | 0.73/0.98 | 0.62/0.83 | 0.75/0.85 | 0.75/0.81 | 0.79/0.98 | 0.92/0.56 | 0.92/0.50 | 0.98/0.40 |
| Y: chiralities swapped `1119-213223-ab-c0a6` | 0.60/0.96 | 0.62/0.79 | 0.71/0.81 | 0.83/0.83 | 0.75/0.90 | 0.92/0.48 | 0.94/0.65 | 0.98/0.46 |
| Z: kings' teams swapped, 24 a seat `1119-225843-ab-26bd` | 1.00/0.83 | 0.83/0.46 | 0.83/0.79 | – | 0.96/0.83 | 0.38/0.92 | 0.67/0.96 | 0.42/1.00 |
| KR: our kings right-handed `1119-212637-ab-e8c2` | 0.85/0.98 | 0.67/0.73 | 0.75/0.81 | 0.75/0.88 | 0.73/0.98 | 0.94/0.50 | 0.69/0.83 | 0.98/0.49 |
| awubot's kings right-handed `1119-213415-ab-a2ec` | 0.88/0.98 | 0.46/0.79 | 0.79/0.92 | 0.75/0.85 | 0.71/0.94 | 0.94/0.58 | 0.92/0.75 | 1.00/0.46 |
| centre `(W-1)/2, (H-1)/2` in both bots `1119-214801-ab-0b14` | 0.79/0.96 | 0.50/0.77 | 0.81/0.83 | 0.67/0.85 | 0.75/0.98 | 0.92/0.62 | 0.94/0.71 | 1.00/0.31 |
| lookahead mirrored in both bots, 24 a seat `1119-225414-ab-3436` | 0.88/1.00 | 0.52/0.79 | 0.88/0.67 | 0.62/0.91 | 0.88/0.88 | 0.91/0.61 | 0.96/0.78 | 0.52/0.96 |
| lookahead mirrored in ours, 24 a seat `1119-225424-ab-9d78` | 0.83/1.00 | 0.62/0.92 | 0.88/0.71 | 0.79/0.83 | 0.83/0.83 | 0.92/0.79 | 0.96/0.71 | 0.96/0.92 |

- The gaps hold on fresh seeds except worldshardestgame's (−0.15, −0.06, 0.00 over three unswapped runs): dropped.
- The Z maps keep each king's position, ID and chirality and swap only its team: the seats swap on every map, so the
  team label means nothing and the gap is the bots' own code against the geometry.
- The map patches (`D:tools/MapPatch.java`) edit the king records in place; `GameMapIO.writeMap` would recompute
  every body's facing, which changes the cats' first moves.

## 2. The lookahead

A rat's bug (`BugnavInstance`) at an obstacle starts a lookahead: a left-hand and a right-hand bug walk ahead
alternately until each has 5 steps or hits an edge, leaves sight or arrives; then one is chosen:

- advance: `leftBug.nextPathIndex > rightBug.nextPathIndex ? advance right : advance left`, so the left goes first;
- `if(leftBug.arrived) choose left; else if(leftBug.arrived) choose right` (the second test repeats the first): the
  left bug's arrival wins at once, the right bug's is never looked at;
- ties of the scores (`leftScore < rightScore ? leftBug : rightBug`) go right;
- otherwise a draw weighted by score^15.

On popthecork the two kings start on either side of a corked 2-wide neck on the symmetry line, the only way into the
bottle holding eight of ten mines. Team A's rats dig their side of the neck top, (14,35), by r25 in 45 of 48 games
(main, both bots), team B's dig theirs, (15,35), in 17 to 19: team A's rats go up to the neck and team B's take the
outer road round the bottle (`D:tools/neck.py`, `D:tools/BoxTrace.java`). With the lookahead mirrored in both bots
team A digs first in 11 of 24 and team B in 19–20; mirrored in ours only, the race is even when we are team B
(16 against 18) and we win it.

## 3. The others

- **pocketsofcheese** is the king's hand (KR swaps it: A 0.94 → 0.69, B 0.67 → 0.83); KR's `KING_HAND`.
- **knifefight** and **closeup** keep their gaps under every flip here. closeup (ROTATIONAL) cannot be a handedness
  effect at all: a 180° rotation keeps hands. Its seat-A losses are our king killed by the cats at (11–12, 9–10) at
  r280–310 (12 of 24 losses). Both maps are left open.

## 4. Arms and book entries

(numbers to come)

## 6. Data and tools (`~/battlecode26-data/scratch/sg/`)

- `tools/seats.py LABEL=RUN[:ROLE]...`: per map our win rate by seat, patched maps folded onto their original.
- `tools/MapPatch.java IN.map26 OUTDIR ids|chir|teams`: swaps the kings' IDs, chiralities or teams in place; the map is
  renamed by its first letter (X, Y, Z). Play patched maps with `--pool "map:NAME:SEED,..."` from a worktree holding
  them in `maps/` (a `file:` pool refuses maps outside the visible set); seeds of another run's cells by
  `bcgames.games.cell_seed(map, base, pass)`.
- `tools/Same.java` (a map file's bodies), `tools/BoxTrace.java REPLAY R0 R1 X0 X1 Y0 Y1` (every turn in a box),
  `tools/neck.py RUN ROLE` (popthecork's neck race), `tools/gtab.py`, `tools/econ.py` (per seat: games, series).
- Probe worktree `wt/` on branch `t3/sg-probe-kr` (never merge): `sg_awukr`, `sg_cr`, `sg_awucr`, `sg_lr`, `sg_awulr`,
  `sg_dbg` (prints via the robot-output shim), the patched maps in `wt/maps/`.
- Trap: `~/battlecode26-data/theory/t4/maps.json` has the teams reversed on some maps (diamond): read the map file.
