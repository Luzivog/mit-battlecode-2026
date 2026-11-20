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
3. **The switch `SG_LOOK_RIGHT`** (Tune.java, 0 = main, every instruction unchanged, javap) mirrors each left-first
   rule. **Proposed book entry: popthecork, seat B, `SG_LOOK_RIGHT` 1**: chosen at +0.400 [+0.231, +0.569] (p_max
   0.000, seed base 22002), confirmed at **+0.483 [+0.332, +0.634]** on 60 fresh cells (22003); seat A −0.033.
   On the other map-seats the switch's effect did not replicate on fresh seeds (§4).
4. **Seat B on VERTICAL maps:** the right-hand lookahead there, as 27 book entries (one per VERTICAL `goal` map, seat
   B), reads **+0.006 [+0.001, +0.013] on 3,000 fresh `goal` cells on top of KR's king's hand (h7)**, after a 1,000-cell
   screen of +0.005 (§4); smoke clean, the gauntlet no harm. The same rule keyed as h7 (team B, the rats reading their
   chirality and the symmetry from their first scan) screened +0.007 ±0.015, with the same VERTICAL seat-B gain and
   more noise elsewhere. On every map it reads +0.006 [−0.015, +0.026]; the lookahead fixed (either bug's arrival,
   ties by team) +0.004 ±0.017: no general gain.
5. **Open:** knifefight and closeup (no flip here moves them; closeup is ROTATIONAL, so no handedness can), diamond
   B (the lookahead read +0.31 on 22001 and −0.13 on 22002), pocketsofcheese (KR's king hand).

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

The switch on in our bot against main, both on the same cells (paired), win rate change by seat.

| arm | pool | seed base | cells | ours − main, seat A | seat B | run |
|---|---|---|---|---|---|---|
| `sg_lr` (probe copy, the same rules) | the 8 maps | 22001 | 192 | +0.06 (all maps) | +0.11 (all maps) | `1119-225424-ab-9d78` |
| `SG_LOOK_RIGHT` 1: book matrix SGL | the 8 maps, 40 a map | 22002 | 320 | popthecork −0.075, others −0.13..0 | popthecork **+0.400 [+0.231, +0.569]**, p_max 0.000; jail +0.075, pockets +0.075, diamond −0.125 | `1119-232509-ab-d42e` (ref `1119-232310-ab-35ca`) |
| confirmation | `map:popthecork` | 22003 | 60 | −0.033 [−0.099, +0.032] | **+0.483 [+0.332, +0.634]** | `1119-233501-ab-5796` |
| general rule: `SG_LOOK_RIGHT` 1 everywhere | `goal` | 22004 | 1,000 | −0.001 ±0.028 (VERTICAL −0.014, HORIZONTAL −0.033, ROTATIONAL +0.020) | +0.014 ±0.029 (**VERTICAL +0.067 ±0.061**, HORIZONTAL +0.019, ROTATIONAL −0.018) | `1119-232234-ab-3b23`: +0.006 [−0.015, +0.026] |
| arm 2 (code on `t3/sg-v2`, not merged): right for team B except a chirality-1 rat on HORIZONTAL/ROTATIONAL maps (KING_HAND 7's key) | `goal` (main before h7) | 22005 | 1,000 | +0.002 ±0.008 (VERTICAL +0.014) | +0.012 ±0.029 (**VERTICAL +0.043 ±0.056**, HORIZONTAL +0.014, ROTATIONAL −0.006) | `1120-000957-ab-756c`: +0.007 ±0.015 |
| arm 3 (code on `t3/sg-v3`, not merged): the lookahead fixed (either bug's arrival, the shorter path; ties to the team's hand, right for B) | `goal` (main before h7) | 22005 | 1,000 | −0.005 ±0.019 | +0.014 ±0.029 (VERTICAL +0.018, HORIZONTAL +0.028, ROTATIONAL +0.006) | `1120-001000-ab-3e80`: +0.004 ±0.017 |
| book: `SG_LOOK_RIGHT` 1 on the 27 VERTICAL maps' seat B | `goal` (main before h7) | 22005 | 1,000 | +0.001 ±0.006 | +0.010 ±0.017 (**VERTICAL +0.046 ±0.056**) | `1120-001005-ab-1b69`: +0.005 [−0.004, +0.016] |
| **confirmation**, the same 27 entries on main with KING_HAND 7 (h7) | `goal` | 22006 | 3,000 | −0.001 ±0.004 (VERTICAL −0.002) | +0.014 ±0.010 (**VERTICAL +0.043 ±0.032**, HORIZONTAL +0.006, ROTATIONAL +0.001) | `1120-011037-ab-db91`: **+0.006 [+0.001, +0.013]** |
| step order rotated (`sg_morot`) | `map:closeup` | 22001 | 48 | 0.50 → 0.63 | 0.79 → 0.88 (gap kept) | `1119-232700-ab-ccfd` |
| step order x-mirrored (`sg_mox`) | `map:knifefight` | 22001 | 48 | 0.88 → 0.85 | 0.98 → 0.96 (gap kept) | `1119-232713-ab-a30a` |

Proposed entry (both runs above): `{"map": "popthecork", "seat": "B", "set": "SG_LOOK_RIGHT", "value": 1,
"evidence": ["1119-232509-ab-d42e", "1119-233501-ab-5796"], "by": "SG"}`. Its round-1 king turn on popthecork is
15,956 bytecodes in main; the book's scan adds 1,913 (limit 20,000). Once booked, the switch is read twice per
lookahead step (2 bytecodes each).

## 5. What is left

- Confirm "`SG_LOOK_RIGHT` 1 for our seat B on VERTICAL maps" on fresh `goal` cells. A round-1 rule needs the
  symmetry at round 1, which only the book gives (`Book.symmetry`), so it is a set of VERTICAL seat-B entries, or a
  rule once the rats' own symmetry detection settles. KR measures the same shape for the king's hand (h5).
- knifefight, closeup and diamond B keep their gaps. Untried flips: the micro's direction loops (TryFight,
  TryPosition), king spawn and site tie-breaks, the cats' own fixed draws on CENTER-facing cats (`E:InternalRobot`
  static `Random(1092)`, fixed per map; on closeup the first facings came out rotation-consistent).

## 7. SG2: the gaps left on main with the fourth book

Nine map-seats (main on 3,000 `goal` cells, `1120-022339-ab-124d`): EscapeTheNight B, squaresaremodernart B,
houseofcards B, diamond B, thehallway B, hellofromtheotherside A, knifefight A, mushroom A, Meow B.

**Self-play** (team A's win rate minus team B's, the same bot on both sides; 64 games a map, seed base 22008;
`1120-024038-ab-339f`, `1120-024041-ab-74ee`): our code leans the way of every one of our gaps; awubot's agrees on
squaresaremodernart, diamond, hellofromtheotherside, knifefight, houseofcards, EscapeTheNight (+1.00) and is even or
opposite on thehallway, Meow and mushroom.

| map | challenger | awubot |
|---|---|---|
| thehallway | +0.62 | −0.03 |
| squaresaremodernart | +0.44 | +0.40 |
| Meow | +0.31 | −0.31 |
| diamond | +0.31 | +0.40 |
| houseofcards | +0.31 | +0.15 |
| EscapeTheNight | +0.19 | +1.00 |
| hellofromtheotherside | −0.25 | −0.32 |
| knifefight | −0.19 | −0.31 |
| mushroom | −0.25 | +0.07 |

**Bisection of main's history** (self-play at 10 of the 54 commits into `src/challenger` since `caterpillow-final`,
80 games a map, seed base 22010): thehallway is level (−0.25 to +0.10) up to `ab49c269` and +0.50 from `6017078f`,
KR's `KING_HAND` 7. On thehallway and squaresaremodernart team B's king has chirality 0, so h7 makes it right-handed,
and a rotation keeps hands: the seats stop being images. Meow leans +0.35 at the champion's own code (the first
commit): the shared code. mushroom drifts from −0.10 to −0.55 over the KD to FL commits with no single step.

**The mirror probe** (`sg_mir`: the worse seat plays through a `RobotController` that hands the bot the image of the
world under the map's symmetry, every location and direction transformed, chiralities flipped, the king's hand that
of the image's team; `sg_mir0` its twin, the same calls untransformed). The probe costs bytecode, so both play on the
Java engine with the limits raised tenfold (a patched `UnitType` on the boot class path, no cache); 32 cells a map,
seed base 22009, `run-1120-025100-8452e6`. Wins of the worse seat, twin / probe: thehallway B 17 / 29, Meow B 23 / 29,
knifefight A 26 / 31, houseofcards B 9 / 16, squaresaremodernart B 18 / 24, diamond B 17 / 20, hellofromtheotherside
A 25 / 27, EscapeTheNight B 21 / 21, mushroom A 24 / 25 (no gap in that run). Our own code carries thehallway, Meow
and knifefight and part of houseofcards and squaresaremodernart; EscapeTheNight's gap is awubot's.

**Choosing among the image's two known switches** (the king's hand and the lookahead's hand):

| matrix | candidates | maps | cells a map | seed base | best per worse seat | run IDs |
|---|---|---|---|---|---|---|
| SGK | `KING_HAND` 0 | the 7 ROTATIONAL maps whose team-B king has chirality 0 | 40 | 22011 | thehallway B +0.100 (p_max 0.16), squaresaremodernart B +0.050, QueenOfHearts B −0.175 | ref `1120-031922-ab-c511`, `1120-032019-ab-9b98` |
| SGM | `KING_HAND` 1, `SG_LOOK_RIGHT` 1, both | Meow, hellofromtheotherside, knifefight, houseofcards, diamond | 40 | 22012 | Meow B +0.125 (p_max 0.11), diamond B +0.125 (0.18), knifefight A +0.025, houseofcards B 0, hello A −0.025 | ref `1120-033705-ab-bacc`, `-5d23`, `-0eda`, `1120-033807-ab-e0e7` |

None passes p_max < 0.05. On 22011 main's thehallway seat B wins 34 of 40, so its gap moves with the seed base.

| SGN | `SG_LOOK_RIGHT` 1, `KING_HAND` 0 | Meow, diamond, thehallway, squaresaremodernart | 60 | 22013 | **squaresaremodernart B `KING_HAND` 0 +0.200 [+0.036, +0.364], p_max 0.026**; thehallway B `KING_HAND` 0 +0.117 (0.14); diamond B +0.117 (0.22); Meow B −0.033 | ref `1120-034736-ab-dc40`, `1120-035525-ab-3732`, `1120-035824-ab-a39d` |

**Confirmed** (against main with the fourth book, 80 cells a map, seed base 22014):
- squaresaremodernart B `KING_HAND` 0: 60/80 against 44/80, **+0.200 [+0.068, +0.332]** (`1120-040920-ab-f67d`);
  seat A 74/80 both.
- thehallway B `KING_HAND` 0: 70/80 against 59/80, **+0.138 [+0.018, +0.257]** (`1120-040920-ab-7945`); seat A
  73/80 both. Chosen on two bases at +0.100 and +0.117, neither alone at p_max < 0.05. It follows the self-play's
  bisection (h7), not KR's +0.033 for the left hand there.
- The seven ROTATIONAL maps whose team-B king has chirality 0, by where that king starts, `KING_HAND` 0 on seat B
  (SGK): squaresaremodernart (4,4) and thehallway (5,5), bottom-left, +0.05 and +0.10 (+0.20, +0.12 on 22013);
  warhammer (1,28) +0.075; stuckinthemiddle (11,1) 0; worldshardestgame (35,36) −0.025; stareintotheabyss (22,37)
  −0.075; QueenOfHearts (27,27), top-right, −0.175. KR's h1 and h6 runs split by start half agree; KR tests the rule.

**The rats' BFS step order** mirrored for the image (seed base 22013, 60 cells, plain against awubot): Meow
y-mirrored seat B 45/60 → 41/59 (`1120-041711-ab-10fd`, main `1120-034736-ab-dc40`); houseofcards x-mirrored A 44 → 48,
B 15 → 19 (`1120-041714-ab-bd16`, main `1120-041725-ab-c4c0`): not the cause.

**Left open:** Meow B, knifefight A and houseofcards B, where the mirror probe moves the worse seat (23 → 29, 26 → 31,
9 → 16) and neither hand nor the step order does; diamond B (17 → 20); hellofromtheotherside A; EscapeTheNight B
(awubot's own lean, +1.00 in its self-play; the probe moves nothing); mushroom A (no gap in the probe's run). The
probe's other transformed parts (the micro's direction loops, spawn and site tie-breaks, coordinate comparisons) are
untried; a probe that mirrors only some of them must keep the whole team in one frame (shared array and squeaks hold
locations), so it has to be built per module, not per unit type.

## 8. SG3: what the mirror probe moves on Meow, knifefight, houseofcards, diamond, hellofromtheotherside

- **Direction tables.** Almost every direction loop in the bot is unrolled by the generator (44 `{% for %}` loops over
  `directions` in the templates: Pathfinder, CombatCommon, CombatTracker and the six move generators), so a runtime
  table read by every robot covers only four loops (`BabyRat`'s cat-trap turn, `RatKing`'s, `LastCat`, `Util`'s
  exploration helper). A table switch would leave the rest of the order untouched; it was not built.
- **The micro's hand.** The generator orders the micro's candidate moves clockwise first relative to the facing
  (`jinja.py`: `try_ord = [0, 4, 1, -1, 3, -3, 2, -2]`, `wide_related_map_center`), and `Util`'s related-direction
  tables the same way: a hand that a reflection reverses and a rotation keeps, and all five map-seats are on
  reflective maps. A probe generated with all of them mirrored (`sg_mh`), paired with main:

| run | seed base | cells | Meow B | knifefight A | houseofcards B | diamond B | hello A |
|---|---|---|---|---|---|---|---|
| `1120-042625-ab-aa6b` | 22015 | 60 a map | −0.050 | −0.133 | +0.067 | **+0.150 [+0.012, +0.288]** | 0.000 |
| `1120-043333-ab-477e` | 22016 | 80 a map | – | – | +0.037 (A −0.100) | +0.013 [−0.120, +0.145] | – |

  It does not carry the probe's gain on Meow or knifefight, and its diamond read did not replicate.
- **What is left in the probe's transform:** coordinate comparisons and ties inside the unrolled code (the
  generator's fixed offsets, bitboard scans in bit order), the order the bot walks each sensed array (its own loops
  index forward), the cats' bottom-left convention. Each sits inside generated code, so a switch for any of them is a
  second copy of the generated module: the module-by-module rebuild. SG3 stops here; the five map-seats have no entry.

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
