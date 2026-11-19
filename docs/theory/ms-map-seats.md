# MS: the worst map-seat pairs

Package MS (track B, wave 5): why we lose almost every game on a few maps from one seat, and the general rules tried.
Source keys: `E:` engine 1.2.6 under `~/battlecode26-data/engine/ref/battlecode26/engine/src/main/battlecode/`; `C:`
`src/challenger/`; `D:` data and tools in `~/battlecode26-data/scratch/ms/` (§6). "Seat A/B" is our seat; on a
symmetric map seat B stands at seat A's mirror image.

## 0. Summary

1. **All six pairs are on VERTICAL-symmetry maps (x mirrored), and that is the pattern.** Over the 27 VERTICAL `goal`
   maps our seat B wins 0.699 and seat A 0.823 (W3's 10,640 pooled main-equivalent games, `mapseat_p.json`); seat A
   leads on every main run alone (+3 to +21 pp). On the 20 HORIZONTAL maps the seats are level (0.840 / 0.835); on the
   48 ROTATIONAL maps seat B leads by 2 pp. Lifting B to A on the VERTICAL maps is worth 1.8 pp of `goal`. It is not
   turn order: of the five VERTICAL maps where seat B's king has the lower id (acts first), seat A wins more on three
   (+27 to +45 pp) and as much on two (start ids and sides from the replays; T4's `maps.json` has the teams swapped on
   10 tournament maps, diamond among them).
   awubot's own mirror shows the same lean (seat A 0.556 on VERTICAL maps, 0.475 and 0.477 on the others).
2. **On four of the six the cause is the king's bug, which always turns left.** A king never uses the bug's lookahead:
   its bug starts left-handed and keeps that side at every obstacle (`C:Pathfinder.java.jinja2`, the `isRatKing` branch of
   the side guess; awubot's is the same). A mirror reverses handedness, so the two kings go round mirrored walls on
   opposite sides, and the seat decides which takes the long way (§2):
   - EscapeTheNight B: the king leaves its sealed room the long way round, then walks back into it for ~200 rounds
     instead of digging along the dirt band to the centre mines, which seat A's king reaches by r250;
   - bed B: the mines are unreachable for a 3×3; seat A's bug wanders north into the cat-free corner, seat B's south
     down the cat's corridor, where the cat pins it (CL's trapped pins);
   - houseofcards B, asabovesobelow B: the seat-B king walks off along the map edge or the long way round.

   Diagnostic **KR** (the kings' bug right-handed, nothing else) closes the VERTICAL gap: seat A 0.827 → 0.741, seat B
   0.642 → 0.735, the mean unchanged (0.735 → 0.738). It flips EscapeTheNight (B 0 → 0.50), bed (A 0.83 → 0.33, B 0.33 →
   1.00), cheddarchokehold (B 0 → 0.83) and mushroom (B 0.50 → 1.00).
3. **popthecork B and peaceinourtime A are not the king** (KR leaves both unchanged), nor the rats' starting side
   (diagnostic RR, the rats' bug right-handed: not one of 564 games changed). popthecork: eight of its ten mines lie in a
   bottle whose only entrance is a 2-wide corked neck on the symmetry line; seat A's rats hold the bottle in 11 of 12
   games whoever plays A (r26–200: 7,627 rat-turns inside against 964), and awubot's mirror loses all 12 from B.
   peaceinourtime: the halves never meet; seat A's cat reaches the kings' strip at r360–460 in all 12 games and seat B's
   later or never, with either bot on either side. Both are out of reach of a rule here (§3).
4. **The fix tried: the king plans its route (KP).** A bitboard search over the king's own map (unseen cells open, dirt
   cost 2, steps round robots, resumable across the turns it cannot move) replaces the king's bug. It does what it
   should on the mechanism: the VERTICAL seat gap halves and, on 3,000 fresh `goal` cells, EscapeTheNight B goes from 2
   to 19 wins of 31, houseofcards B from 2 to 10 of 32 (A 14 → 23). But the king sees only its r² 25 view, and a plan
   optimistic about every unseen cell is wrong wherever the left turn was lucky: bed A 25 → 3 of 31 (both kings now walk
   down the cat's corridor), AceOfSpades 53 → 40 of 64 (a box open only at the top; the plan heads for its closed
   bottom). Three screens read −0.005, +0.006, +0.006; **the confirmation on 3,000 cells reads −0.004 [−0.022, +0.014]**.
   A hybrid that keeps the bug until the king retraces its own steps (KH3) fires too rarely to matter (+0.001
   [−0.010, +0.011]) (§4).
5. **Map knowledge is worth nothing to us as the bot stands (§7).** Oracles that read the true static map (walls,
   dirt, mines) from a compiled table, on 1,000 fresh `goal` cells against a salted null: the kings' route search on
   true walls −3.4 pp, with mine choice by route −1.7 pp, with every rat's bug and mine targeting too −1.5 pp, main with
   only a perfect first trip for its king −0.5 pp; none above the null. Exact plans cost the king turns that cheap free-space plans did not, the rats' paths and targets do
   not change for the better, and which mine to take matters more than the way there. No reachable part was built.
6. **Recommendation: merge nothing to the bot.** Merge the doc and docs/closed.md rows. The seat effect is real and
   explained, but no rule built on what the king knows beats the left turn's luck on average, and §7 shows that
   knowing the whole map would not either.

## 1. The seat effect is a VERTICAL-map effect

| runs (`D:symrun.py`) | VERTICAL A | VERTICAL B | HORIZONTAL A | HORIZONTAL B | ROTATIONAL A | ROTATIONAL B |
|---|---|---|---|---|---|---|
| main `1119-174327-ab-a52f` (14001) | 0.846 | 0.698 | 0.883 | 0.867 | 0.812 | 0.882 |
| main `1119-150554-ab-6083` (9201) | 0.833 | 0.735 | 0.792 | 0.867 | 0.854 | 0.878 |
| main `1119-181328-ab-56b6` (14002) | 0.796 | 0.654 | 0.800 | 0.817 | 0.837 | 0.868 |
| null `1119-174327-ab-4c21` | 0.821 | 0.772 | 0.867 | 0.850 | 0.826 | 0.875 |
| null `1119-174328-ab-6cae` | 0.815 | 0.698 | 0.833 | 0.833 | 0.840 | 0.868 |
| pooled, 10,640 games (`D:symseat.py`) | 0.823 | 0.699 | 0.840 | 0.835 | 0.842 | 0.862 |
| awubot mirror (3 runs, 2,280 games): seat A wins | 0.556 | | 0.475 | | 0.477 | |

- Per-map seat gaps |A − B| average 0.216 on VERTICAL maps, 0.118 on HORIZONTAL and 0.075 on ROTATIONAL (binomial
  noise alone ~0.07). The six pairs under 0.5 are the extreme of a VERTICAL distribution; the per-map list is in
  `D:symseat.py v` (EscapeTheNight B 0.02, popthecork B 0.32, asabovesobelow B 0.36, houseofcards B 0.29,
  pocketsofcheese B 0.54, diamond B 0.64, bed B 0.39, AceOfSpades B 0.59, kingofthehill B 0.50; the other way knifefight
  A 0.61, peaceinourtime A 0.39).
- The engine is mirror-consistent: sensing lists follow chirality (`E:world/GameWorld.java`
  `getAllLocationsWithinConeRadiusSquaredWithoutMap`), cats' BFS and corners are flipped for chirality 1, cheese spawns
  mirror (1.2.6). What is not mirrored is the bots' own fixed choices (directions tried in a fixed order, the bug's
  side), and turn order. The seat follows team A, not the side: on asabovesobelow, pocketsofcheese and AceOfSpades
  team A starts on the right and acts second, and still wins more (+27 to +45 pp).

## 2. The king's bug

`BugnavInstance` is made with `leftTurn = true` at every new target; a rat replaces the side by a lookahead of both
sides, a king keeps it (`else if(isRatKing)`), flipping only at the map edge or on a cell it already tried on that side.
Every king walk round an obstacle is therefore counter-clockwise in absolute terms, which a mirror turns clockwise.

| map, seat | what the king does (main, 14001 replays; `D:ktrack.py`, `D:render.py`) |
|---|---|
| EscapeTheNight | both kings start in a room sealed by dirt. Seat A's bug follows the north wall west to the room's exit and is out by r70; its target, the centre mine (22,24), is reached along the dirt band by r250. Seat B's bug follows the west wall south, the south wall east and leaves by r90; with target (40,36) it then walks back into the room and stays there r100–r260 (debug run of cell 157). In r100–250 seat A's king digs 6–9 dirt tiles per 50 rounds, seat B's 0–3 (6 games a seat, `D:seatecon.py`) |
| bed | the four mines are inside a box no 3×3 can enter (`D:reach.py`: 37 reachable centres, nearest mine 4 away). Seat A's bug turns north, seat B's south down the corridor the cat climbs from (0,0) to (1,11) |
| houseofcards | the mines are reachable through gaps in a diagonal fence; seat A's king stays near (5–11, 13–16), seat B's walks up the map edge to the top row |
| asabovesobelow | seat A's king digs up and across to the centre (29–45, 5–25); seat B's stays on its own column |

KR on `MS-reflect` (the 47 VERTICAL and HORIZONTAL maps, 282 cells, seed base 19001, `1119-185028-ab-55a5`, against main
`236f0d4`):

| | ours A | main A | ours B | main B |
|---|---|---|---|---|
| VERTICAL (162 games a seat) | 0.741 | 0.827 | 0.735 | 0.642 |
| HORIZONTAL (120) | 0.850 | 0.850 | 0.842 | 0.825 |

A fixed side of either hand averages the same; what decides is which side each map's walls favour from each seat.

## 3. The two pairs out of reach

- **popthecork B.** `D:reach.py`: from either start the king reaches 2 of the 10 mines (the outer pair); the other eight
  are in the bottle behind the cork (dirt at x 13–16, y 33–37, a 2-wide neck at x 14–15 on the symmetry line). Both
  teams dig the cork; seat A's team digs it more in r1–100 (110 tiles against 43) and its rats hold the bottle (r26–200:
  7,627 rat-turns inside against 964; 555 pickups against 57). Rat deaths at the neck are not the difference (seat A's team
  loses more there in our seat-B games: 152 against 113 in r1–200). The awubot mirror loses all 12 from B; we win 0.32
  from B. Neither KR nor RR nor KP's arms change it (KP v2 +0.15 on 10 cells a seat, noise level).
- **peaceinourtime A.** A wall at x 17 splits the map: the two teams never meet, and each king's strip lies on its cat's
  loop (CL §3). Seat A's cat (left half) reaches y ≥ 44 at r360–460 in all 12 games of the 14001 run; seat B's at
  r390–420 in 3 games and not before the end in 9, whichever bot plays the left (`D:CatTrack.java`). The cat RNG is no
  cause (0–8 draws a game). The difference lies in how each side's rats meet their cat, for both bots alike. CL closed
  routes and sites here; nothing in this package reaches it.

## 4. Arms

A fixed side of either hand gains nothing (KR), so the fix must choose the side from what the king knows. KP replaces
the king's bug, for its walk to a mine or the centre, by a route planned over the king's own map
(`C:robot/king/KingPath.java`; branches below):

- a breadth-first search over 3×3 king centres from the king, on bitboards copied from `CellTracker` (a new
  `copyRows`): seen walls block, unseen cells are open; a centre whose 3×3 touches an unseen cell is an exit scored by its
  cost plus its Chebyshev distance to the target, the target by its cost; cells that cannot beat the best score are
  dropped (cost + distance never falls along a path), so it stops at the best exit or proves the target closed off;
- it runs in the turns the king cannot move (one move in four) and pauses below 6,500 bytecodes left, going on next
  turn while the king stands still; the king follows a plan for 4 steps, then plans again;
- v2 adds a cost of 2 for a step onto a 3×3 holding seen dirt (the king digs first) and, when a robot blocks the planned
  step, a movable step next to the path's following cell. v1's king moved 22% less than main's in r1–300 (38.9 against
  49.6 moves on `MS-reflect`); v2's moves as main's on maps where a mine is reachable (47.7 against 48.7);
- v1 and v2 send a king whose target the known walls close off back to where it started; KP3 instead hands that target
  back to the bug (the old play);
- KH3 keeps the bug, and hands a target to KP3 only once the king has stepped 8 times onto cells it already stood on
  (more than 3 from the mine) since it chose that mine.

Cost: a plan takes ~5k bytecodes at the median (7k p90), one every ~16 rounds; king overruns on `MS-reflect` 0.006% of
turns against main's 0.311% (the plan replaces the bug's 4–5k a turn).

All paired with main on the same cells, `--remote`; seed bases 19001–19008, each used once except 19006 (two arms
screened on the same cells).

| arm | change | pool | seed base | cells | ours − main [95% CI] | run |
|---|---|---|---|---|---|---|
| KR (diagnostic) | kings' bug right-handed | `MS-reflect` | 19001 | 282 | +0.005 [−0.021, +0.034]; VERTICAL seat A −8.6 pp, seat B +9.3 pp | `1119-185028-ab-55a5` |
| RR (diagnostic) | rats' bug right-handed | `MS-reflect` | 19001 | 282 | ±0: no game changed | `1119-193901-ab-0239` |
| KP v1 | king plans its route; cut-off target: home | `MS-reflect` | 19002 | 282 | −0.002 [−0.053, +0.046] | `1119-193429-ab-1786` |
| KP v1 | | `goal` | 19003 | 1,000 | −0.005 [−0.030, +0.020] | `1119-193430-ab-4273` |
| KP v2 | v1 + dirt steps cost 2 + step round robots | `MS-reflect` | 19004 | 282 | +0.016 [−0.032, +0.066] | `1119-194851-ab-23f4` |
| KP v2 | | `goal` | 19005 | 1,000 | +0.006 [−0.022, +0.035]; reach maps +2.4 pp, pocket maps −3.4 pp | `1119-194852-ab-9ad1` |
| KP3 | v2, cut-off target back to the bug | `goal` | 19006 | 1,000 | +0.006 [−0.017, +0.031] | `1119-200029-ab-8c76` |
| KH3 | bug until 8 retraced steps, then KP3 | `goal` | 19006 | 1,000 | +0.001 [−0.010, +0.011] (905 cells unchanged) | `1119-200048-ab-8de2` |
| **KP3, confirmation** | | `goal` | 19008 | 3,000 | **−0.004 [−0.022, +0.014]** (508 cells better, 527 worse) | `1119-201446-ab-29b3` |

By seat (`goal` screens, ours − main, `D:seatpair.py`):

| | VERTICAL A | VERTICAL B | HORIZONTAL A | HORIZONTAL B | ROTATIONAL A | ROTATIONAL B |
|---|---|---|---|---|---|---|
| KP v1 (19003) | −4.9 | +4.6 | −0.4 | −3.8 | −0.6 | +0.6 |
| KP v2 (19005) | +0.4 | +6.8 | −1.9 | +4.7 | −3.2 | +0.6 |
| KP3 (19006) | −2.8 | +3.6 | −7.5 | −3.2 | +2.7 | +3.9 |
| KP3 confirmation (19008, 3,000 cells) | −3.4 | +2.3 | −1.6 | −0.3 | +0.1 | −0.3 |

The six pairs in the confirmation (wins of 31–32 games a seat, KP3 against main on the same cells):

| map | seat A: KP3 / main | seat B: KP3 / main |
|---|---|---|
| EscapeTheNight | 31 / 31 | **19 / 2** |
| houseofcards | 23 / 14 | **10 / 2** |
| asabovesobelow | 25 / 25 | 19 / 15 |
| popthecork | 31 / 31 | 10 / 11 |
| peaceinourtime | 14 / 15 | 27 / 30 |
| bed | **3 / 25** | 9 / 8 |
| AceOfSpades (not one of the six) | 21 / 28 | 19 / 25 |

KP3 fixes the king-bug pairs where the mine can be reached (EscapeTheNight B, houseofcards, asabovesobelow B) and
destroys bed A: the six maps together gain +0.19 pp of `goal`, the other 89 lose about as much.

What the planner does, and why it does not add up:
- **It removes the seat effect on VERTICAL maps** (B up 4–7 pp, A down 0–5 pp in every screen) and wins EscapeTheNight
  from seat B: +0.40, +0.32, +0.41 on the map in the three screens (B 0 of 6 → 5 of 6 on `MS-reflect`). Maps that gain
  in all three screens: EscapeTheNight, RUN, externalsustenance, pocketsofcheese, wallsofparadis, shell, tminusten,
  houseofcards, pipes.
- **The king knows only its own r² 25 view**, so a plan is optimistic about every unseen cell. Where the left-hand rule
  was lucky, the plan is wrong in the other direction: AceOfSpades (both kings in a box open only at its top corners,
  their target below; the bug turns north and finds the exit, the plan goes south to the closed bottom first, v1: 1 of 6
  from each seat against main's 5 of 6). Maps that lose in all three screens: cheesebottles (the walk home; −0.05 in
  KP3), kingofthehill, streetsofnewyork (the plan digs through the dirt streets: 26 tiles in r1–300 against 13),
  seepastyourgreed, mincemeat, bed (both kings now walk south down the cat's corridor), fullofsecrets, FreeTheCheese.
- So the handedness was luck, and a free-space plan is a different luck that is fair to both seats. Neither knows the
  walls ahead; the rats do (their `CellTracker` maps), but only mine positions and seen 3×3 units reach the king.

## 5. What is left

- **The VERTICAL seat deficit stays in main** (1.8 pp of `goal`). A king that took the better side of each obstacle
  would capture it: in KR's run the per-map-seat better of the two hands reads 0.790 on VERTICAL maps against 0.735
  (0.875 against 0.838 on HORIZONTAL), an upper bound inflated by 6-game noise. Choosing it needs walls the king has
  not seen. The rats have them (each rat's `CellTracker`); the king gets only mines and seen 3×3 units (`MapTracker`
  chunks). §7 priced that knowledge with oracles: even the true map, free, gains nothing, so the symmetry route and
  squeaked walls are closed.
- **popthecork B** is a race for a 2-wide neck on the symmetry line that seat A wins in turn order and position, for
  both bots; we already win 0.32 there against the mirror's 0.
- **peaceinourtime A** is decided by when each half's cat reaches its king's strip; the left cat is first in every game
  with either bot there. CL: no route or site survives it.
- **bed** has no reachable mine; the bug's left turn keeps seat A's king in the cat-free corner and every reach-seeking
  rule (KP) walks both kings down the cat's corridor. The king's defensive dirt is what shields a pinned king there
  (RS, docs/theory/rs-shelf.md).

## 6. Data and tools (`~/battlecode26-data/scratch/ms/`)

- `symseat.py [v]`, `symrun.py RUN...`: seat A/B by map symmetry (pooled and per run), the awubot mirror; `v` lists the
  VERTICAL maps.
- `MirrorDiff.java` (a game against its own mirror, round by round: random streams break it from r3, so not used further),
  `MapTrace.java` (per tile and 25-round window, per team: rat turns, pickups, dirt broken, rat deaths, king turns),
  `render.py MAP KIND W0 W1 [all|usA|usB] [box]` (seat A's team beside seat B's mirrored into A's frame; env `TRACE`),
  `tracerun.sh RUN MAP NAME` (MapTrace over a paired run, ours and the ref apart), `ktrack.py`, `ktr.py` (king tracks
  from W3Stats), `seatecon.py MAP TERMS` (W2Econ terms by our seat), `reach.py MAP...` (3×3 centres a starting king
  reaches, mines within 1; `reach_all.txt`: 28 `goal` maps where none is), `kingsim.py` (a Python walk of a planner on
  the true map, unseen counted open).
- `KingMoves.java`, `kmdiag.py RUN [MAP...]` (our starting king in r1–300: moves, turns it ended move-ready without
  moving, dirt dug, distance from start; ours against the ref), `kpdiag.py RUN` (round our king first stands by a mine),
  `CatTrack.java` (cats every 10 rounds), `seatpair.py RUN` (a paired run by seat and symmetry), `mapdiff.py RUN` (per
  map ours − ref, pocket and reach maps apart).
- Map-knowledge oracles (§7): `MapDump.java` (every map as played, from replays: `goalmaps.jsonl`), `mkmsmap.py` /
  `mkmsmap2.py PKG RATS OUT.java` (the cheat table: keys, walls and round-1 dirt rows, mines, per start the reachable
  mines and the offline route to the mine nearest by route), `patch_probe.py`, `patch_logic.py`, `patch_clean.py`
  (the probes' edits; the probe worktree is `wt/`), `osplit.py NULL RUN...` (probe − main and − null, by symmetry ×
  seat, pocket/reach and map class, CI by bootstrap over maps).
- `runs.txt`: every run of this package. Pools `MS-vert`, `MS-six`, `MS-reflect` in `~/battlecode26-data/games/pools/`.
- Local debug runs: `dbg/` (specs and replays; prints via the `robot-player-to-system-out` shim).

## 7. What map knowledge is worth (follow-up)

The planner failed on unseen tiles it assumed open (§4), so the next question was whether knowing the true map is worth
building toward. Oracle copies of challenger (branch `t3/ms-probes`, never merged) read the true static map (walls,
round-1 dirt, mines) from a table compiled per map (`D:mkmsmap2.py`, from every `goal` map as played, dumped from
replays by `D:MapDump.java`). The map is found from its size, the own king's start and an order-free hash of the
walls, dirt and mines in that king's round-1 view; the first king writes the index to shared slot 15 (free in main),
later kings and rats read it. 184 of 187 keys are unique; the three shared ones (cheeseguardians / longwayaround, and
attack_on_cat / holeinthewall from both seats) play without the table. Each robot decodes the table once, when a turn
has 7.7k bytecodes left (the decode costs ~6.8k).

All on the same 1,000 `goal` cells (seed base 19101), paired game by game with main; the null is main with a salted
random stream (`ms_salt`, W2 §4.6). "vs null" = probe − null on the same cells.

| probe | what knows the true map, and for what | run | − main [95% CI] | vs null |
|---|---|---|---|---|
| null `ms_salt` | nobody | `1119-204229-ab-70f7` | +0.004 [−0.017, +0.025] | – |
| KP3 | nobody (KP3's planner, unseen cells open) | `1119-204242-ab-2a8f` | +0.011 [−0.012, +0.032] | +0.007 |
| `ms_kr` | kings, in KP3's route search only (every plan exact) | `1119-212044-ab-46c5` | −0.030 [−0.059, −0.005] | −0.034 |
| `ms_ka` | kings: route search, and the starting king's mine chosen by route on a route computed offline, held; later choices the nearest reachable | `1119-212044-ab-2c8c` | −0.013 [−0.045, +0.017] | −0.017 |
| `ms_kb` | `ms_ka`, and every rat's bug (unseen cells by the true walls) and mine targeting (all mines from round 1) | `1119-212045-ab-a8fc` | −0.011 [−0.043, +0.020] | −0.015 |
| `ms_kf` | main, and only the starting king's first trip: the offline route to the mine nearest by route (no search) | `1119-213530-ab-a1f7` | −0.002 [−0.032, +0.028] | −0.005 |

Earlier versions that also wrote the map into each robot's `CellTracker` leaked it into every strategy (formation
sites, traps, the kings' scans), and in v1 the key was taken at the end of the king's round-1 turn, which fails when
that turn overruns: `ms_oka` v1 −0.005, `ms_okb` v1 +0.004, `ms_oka` v2 −0.029, `ms_okb` v2 −0.013, `ms_okr` (routes only)
−0.026 (runs `1119-204234-ab-50f1`, `-204238-ab-6084`, `-210212-ab-8e53`, `-210216-ab-a8aa`, `-211204-ab-670e`).

By symmetry and seat (vs main; 286 games a seat on VERTICAL maps, 211 HORIZONTAL, 503 ROTATIONAL; `D:osplit.py`):

| | VER A | VER B | HOR A | HOR B | ROT A | ROT B | reach maps | pocket maps |
|---|---|---|---|---|---|---|---|---|
| null | +0.066 | +0.038 | −0.028 | −0.009 | +0.012 | −0.040 | −0.004 | +0.022 |
| KP3 | +0.028 | +0.066 | +0.043 | +0.019 | −0.018 | −0.018 | +0.009 | +0.017 |
| `ms_kr` | −0.003 | +0.010 | −0.038 | −0.038 | −0.038 | −0.054 | −0.047 | +0.010 |
| `ms_ka` | +0.010 | +0.052 | −0.033 | −0.028 | −0.026 | −0.034 | −0.019 | +0.003 |
| `ms_kb` | +0.014 | +0.024 | −0.009 | +0.009 | +0.002 | −0.068 | −0.019 | +0.008 |
| `ms_kf` | +0.087 | +0.042 | −0.005 | −0.033 | −0.040 | −0.024 | −0.002 | +0.000 |

What it says:
- **No oracle is worth 2 pp; against the null none is above zero.** The best 95% upper bound is +2.8 pp (`ms_kf`).
  The cleanest, `ms_kf` (main untouched except a perfect first trip, no search at all), reads −0.002: it wins
  asabovesobelow B 9/11 (main 5/11), AceOfSpades B 9/10 (5/10), EscapeTheNight B 4/11 (0/11) and gives as much back
  elsewhere.
  So the reachable parts (symmetry over seen walls, squeaked walls, scouting) are not built: their ceiling is below
  what a test resolves. The VERTICAL seat deficit (1.8 pp) and the pathing ideas close.
- **True walls make the king's planner worse** (`ms_kr` − KP3: −4.1 pp). With every cell known there is no exit into
  the unknown to stop at, so each search runs on to the target, up to 150 layers over the map's height, across
  several turns; the king walks the bug meanwhile and the plan, made from a cell it has left, starts again. Planning
  cost, not knowledge, decides; under the free-space assumption the cheap, short plans were what worked.
- **The rats gain nothing from the map** (`ms_kb` − `ms_ka`: +0.2 pp): their lookahead bug and their mine weights
  already do what the walls and the full mine list would.
- **Knowing the map does fix the cases KP3 got wrong** (per map, 10–11 games a seat): AceOfSpades A 6/10 (KP3) → 10/10
  (`ms_kr`, `ms_kb`), bed A 5/11 → 10–11/11, asabovesobelow B 7/11 → 11/11 (`ms_ka`). But it loses EscapeTheNight B (KP3
  7/11 → 0–5/11): the mine nearest by route from seat B is the side mine (40,37), behind less dirt, not the centre mines
  seat A takes. Which mine to take matters more than the way to it, and nothing here knows which mine is worth more.
