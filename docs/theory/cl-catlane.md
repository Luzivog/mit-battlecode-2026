# CL: the king on the cat's path

Package CL (track B), measured on main `450223d` (bot source = `1557b1b`, the same as W2's run) against awubot.
Source keys: `E:` engine 1.2.6 under `engine/src/main/battlecode/`; `C:` `src/challenger/`; `D:` data and tools in
`~/battlecode26-data/scratch/cl/` (§6).

## 0. Summary

1. **A CAT loss is a pin.** An EXPLORE cat that our king blocks scratches it every 3 rounds and never gives up: a
   scratch resets the cat's stuck counter, which never reaches the 4 that turns it away (`E:world/InternalRobot.java`
   EXPLORE branch, rules.md §12). In main's 35 CAT losses of W2's 1,140 games, 83% of the scratch HP on our kings is
   such blocks (14,360 of 17,240); the ATTACK phase at waypoints is the rest. 22 of the 35 end in a pin from which
   the king had no way off the cat's path: no 3×3 centre off that path within 10 king steps (§2).
2. **Traps are narrow places.** 54 of 57 such pins (≥ 5 scratches) happened where the king had ≤ 13 free king
   centres in the 5×5 box around it ("room"); 80% of the pins a king walked out of were at room ≥ 14. The king got
   there by itself: in the 8 moves before each trapped CAT loss it walks from room 15–22 into room 5–9, mostly fleeing
   a cat in view along that cat's lane (85 of 157 moves), else moving with a cat in view outside its lane (34) or with
   no cat in view (38). The dead end is often the cat's own waypoint (externalsustenance (8,26), sunandmoon (26,28),
   peaceinourtime (11,47), tminusten (12,2)): a king on a waypoint is pinned for good, since the cat can never arrive
   and so never moves on (§2.3).
3. **Two of the six maps are races, not routes.** On tminusten and peaceinourtime every centre the king can reach
   lies on a cat's loop (§3): both kings are pinned, and the seat decides whose cat comes first. On tminusten our seat-A
   king is pinned at r23 and dies at r102–105; awubot's B king is pinned at r25–29 (it would die about r106) or, when
   its cat pounces past it onto a baby rat, at r86. No route or site saves those 9 losses; only a king outside the
   region does (KD's ground).
4. **The other four have safe ground 0–2 king steps from the start** (externalsustenance, closeup, sunandmoon, bed:
   13 trapped losses): centres off every cat loop exist next to the start or the mine, and the king leaves them.
5. **Both arms that change the flee read H0** (§4). V (no step from open into narrow room near a cat, outside its
   lane) seldom fires: the moves into traps are flees along the lane, which it leaves alone; +0.014 on `L2-cat`.
   A1 (no keep-away from a cat whose lane misses the king) is worse, −0.059: the keep-away is what keeps ATTACK
   scratches down (+27% without it). Nothing to merge.
6. **What is left is outside CL** (§5): the race maps need a king formed outside the cat's region, and a trapped pin
   gives the foresight KD's oracle needed (it fires a median 79 rounds before the death, in 22 of 952 won games);
   bed's king walks from its cat-free corner toward mines it cannot reach (KS's pocket, K2's footprints).

## 1. Method

- **Replays** of W2's main run (`1119-150554-ab-6083`: goal, seed base 9201, 570 cells, both seats, every replay).
- **`D:CLPin.java`** reads a replay and, for each cat, rebuilds its waypoint state from its positions, by the engine's
  rule: the corner reaching `wp[cur]` with `cur != prev` starts 8 ATTACK turns; the next EXPLORE turn at `wp[cur]`
  advances `cur` (`E:world/InternalRobot.java` cat AI; waypoints from the map files, `D:catwp.txt`). Its path to
  `wp[cur]` is the engine's BFS (`E:world/GameWorld.java` `bfsFromTarget`, walls and edges only, the neighbour order
  flipped for chirality 1). Check: every scratch it calls EXPLORE hits a tile in front of the cat (0 exceptions in
  1,140 games).
  - A scratch on our king is a **block** (EXPLORE) or an **ATTACK** scratch.
  - A **pin** is a run of block scratches by one cat on one king, gaps ≤ 6 rounds. Its **exit** is the fewest king
    steps (3×3 through walls and dirt, the cat's body blocked) to a centre whose 3×3 misses that cat's path to
    `wp[cur]`; −1 when there is none within 10.
  - The king's moves, tagged L (a visible cat's forward lane held the king, `C:robot/king/KingCombatStrategy`
    `laneCross`), V (a cat in view, no lane) or untagged.
- **`D:traps.py`, `D:safemap.py`**: per map and starting king, every cat's full loop (BFS from waypoint to
  waypoint), the king's reachable centres, and the safe ones (3×3 off every loop).
- **Room**: free king centres (3×3 with no wall) in the 5×5 box around a centre, 0–25 (`D:boxmob.py`).

## 2. Step 0: how the cat kills

### 2.1 The losses

| | games | block HP / ATTACK HP | fatal pin trapped (exit −1) | fatal pin with an exit | no fatal pin |
|---|---|---|---|---|---|
| CAT losses | 35 | 14,360 / 2,880 | 22 | 4 | 9 |

- Trapped: tminusten A 5, externalsustenance 5 (A 4, B 1), peaceinourtime A 4, closeup A 3, sunandmoon 3, bed 2.
  The king dies where it was pinned, the same tile on each map: externalsustenance (7,27) (B: the mirror (22,12)),
  closeup (12,10), peaceinourtime (12,47), tminusten (9,3), sunandmoon (26,11).
- With an exit: bed c283A, directconfrontation c175A (2 steps), toomuchcheese c385A (4), popthecork c165B (7).
- No fatal pin (9): toomuchcheese ×3 and hatefullattice (late games, several kings, bites and scratches mixed),
  directconfrontation B ×3 (bitten last), therearenowalls (ATTACK scratches), bed c569B.
- Over all 1,140 games our kings died at the end of a pin 43 times: 33 in lost games (28 trapped).

### 2.2 Where traps are

| room at the pin | ≤ 5 | 6–9 | 10–13 | 14–17 | 18–21 | ≥ 22 |
|---|---|---|---|---|---|---|
| trapped pins (≥ 5 scratches) | 10 | 28 | 16 | 2 | 1 | 0 |
| pins with an exit | 20 | 35 | 40 | 95 | 97 | 185 |

Room ≥ 14 holds 80% of the pins kings left (377 of 472) and 3 of 57 trapped ones. Kings spend 20% of their rounds in
room ≤ 13 (lattices, corridors: seepastyourgreed, hatefullattice, rose), so narrowness alone is not danger.

### 2.3 How the king gets there

The 8 moves before each of the 22 trapped CAT losses: 85 L, 34 V, 38 untagged; the moves from room ≥ 14 into ≤ 13
among them: 11 L, 7 V, 2 untagged. Typical sequences (room before > after, tag):
- sunandmoon c161A `20>20L 20>20L 20>19L 19>18L 18>16L 16>13L 13>9V 9>5`: the king flees east along cat 6's lane
  from the mine (19,30), ahead of the cat, to the cat's next waypoint (26,28).
- closeup c497A `15>15L 15>15V 15>16L 16>18L 18>15V 15>11V 11>8L 8>5L`: from the mine (10,2), next to cat 3's
  waypoint (9,1), up the corridor the cat takes next to (13,13); cat 4 comes down it the other way.
- externalsustenance c184A `17>19V 19>19L 19>19 19>17 17>12V 12>7V`: away from cat 3 at its waypoint (12,23) into
  the wall pocket that is its next waypoint (8,26). In c280A and c534A the king walks into the same pocket with no cat
  in view: its target is a mine outside the closed arena, and the pocket is the nearest point (KS's unreachable
  target).
- The flee (`C:robot/king/KingCombatStrategy`): every visible cat scores −15 toward it and +5/+6 on five other
  directions, so with a cat in view the king moves every time it can; in a cat's lane with no open side it moves along
  the lane (K1). The cat walks 1 tile per 2 rounds, the king 1 per 4, so fleeing along a corridor ends at its end.

## 3. Why the seat

Both bots lose these maps from the same seat (W2 §2.3). On tminusten and peaceinourtime the geometry is symmetric
and every reachable centre is on a cat loop (`D:traps.py`: 32 of 32 and 36 of 36 centres), so both kings are pinned
and the first dead loses. What differs is the engine's play of the cats, not the bots:
- tminusten: at r13 both cats are in ATTACK at waypoint 1 (x 6). Cat 3 (seat B's pen) targets awubot's or our baby rat
  below the B king and pounces past the king to (10,45); cat 4 (seat A's) targets the A king itself (no rat at the
  mirror tile) and stays on its west side. The A king flees east along the lane, ahead of the cat, and is pinned from
  r23; the B king is behind its cat and is pinned only when it comes back, r86. Without the pounce the B king is pinned
  at r25–29 and the race is 2–6 rounds, decided by the ATTACK scratches (A takes 60 HP at waypoint 1, B 0–40).
- peaceinourtime: the cat walks the strip y 47–48 to its waypoint at the far end; the king's strip (5 rows, x ≤ 12)
  cannot let it pass. Seat A's cat arrives first in these games (r390–475); seat B's later or not before the end.

## 4. Arms

Gain tests against main (`challenger@450223d`) on `L2-cat` (32 cells, the maps of W2's CAT losses), each at its own
fresh seed base, δ 0.10, every replay kept; read with `D:armdiag.py`.

| arm | change | seed base | result | run |
|---|---|---|---|---|
| V | with a cat in view and the king outside its forward lane, no king step (flee or mining) from open room (≥ 14) into a narrow centre (≤ 13) | 13101 | +0.014 [−0.037, +0.063], H0 (71 cells: 5 better, 3 worse) | `1119-164702-ab-852a` |
| A1 | a cat moves the king only when its forward lane holds the king (the keep-away from every visible cat deleted outside lanes) | 13102 | −0.059 [−0.125, +0.008], H0 (76 cells: 13 better, 19 worse) | `1119-164822-ab-5033` |

- **V** (`t3/cl-arm-v`): our kings' block HP 32,460 vs the ref's 32,420, trapped fatal pins 34 vs 33 (142 games
  each). The changed games: externalsustenance +3, sunandmoon +1, closeup one each way, toomuchcheese −2. It does not
  reach the bulk: on the ref's games of both seed bases 50 of 88 losses are trapped pins, 17 of them on bed and 17 on
  tminusten, both entered by in-lane flees or by walks with no cat in view.
- **A1** (`t3/cl-arm-a1`): ATTACK scratch HP 10,580 vs 8,320 (+27%), king deaths not by a cat 24 vs 16, wins 102 vs
  111 of 152; bed 0.41 vs 0.59. Keeping away from a cat at its waypoint is what avoids its 8 ATTACK turns; the
  keep-away cannot be traded for fewer dead ends.
- **Not built.** Extending V to in-lane flees: on tminusten the start has room 15 and the next cell 13, so the king
  would wait at its start, where the cat coming from x 1 pins it about r8 instead of r23, from both seats; seat B now
  wins that race by a few rounds or by its cat's pounce past the king (§3). Parking a king that has no reachable mine at its start: on the 29 `KS-pocket` maps
  the starting king's centre is off every cat loop in 54 of 58 cases, but the only map where it changes the cat (bed)
  is worth about 0.3 pp of `goal`, below what `L2-cat` resolves at δ 0.10.

## 5. What is left

- **The race maps** (tminusten, peaceinourtime: 9 of W2's 35 CAT losses, 26 of the ref's 88 losses on `L2-cat` here)
  have no route or site off the cat: only a king formed outside the cat's region survives. A trapped pin is the
  foresight KD's far oracle needed: a pin of ≥ 3 block scratches at room ≤ 13 with one king alive fires in 24 of the
  35 CAT losses, a median 79 rounds before the end (18–160), in 22 of 952 won games (7 of them tminusten seat B, which
  wins the race) and in 6 other losses; KD's HP trigger fired 57 rounds ahead in 40 of 921 wins. KD's CAT arms won
  tminusten 4 of 4 and lost on late or near-king formations elsewhere (KD §3). A far formation on this trigger is
  KD's mechanism with a better trigger, not a CL route.
- **bed** (17 trapped deaths on `L2-cat` here, mostly seat B): the king walks out of its cat-free start corner down
  the 4-wide corridor toward the mines inside the walled box, which it cannot reach, and a cat coming down the
  corridor drives it to the bottom corner. K2's arm D' (no mining step into a seen cat footprint) kept the king in its
  corner (bed 16 of 16 against 6 of 16, K2 seed 0); KS's pocket parking parks at the centre nearest the mine, which is
  the trap.
- **In-lane flees in corridors** (85 of the 157 moves before the trapped CAT losses) end in the dead end, but stopping
  them only pins the king sooner where it stands; the decision that matters is being in the corridor when the cat
  comes, which needs the cat's route (K2's footprints) or the mine's.

## 6. Data and tools (`~/battlecode26-data/scratch/cl/`)

- `CLPin.java` (`CLPin CATWP OUT.jsonl OURPKG replay...`; with env `T0`/`T1` a per-round trace of our kings and the
  cats within 12: state, waypoint, path steps to the king, exit), compiled into `cls/` against the 1.2.6 jar;
  `catwp.txt` (every map's cat waypoints, from T4's `maps.json`).
- `main.jsonl` (W2's 1,140 games), `agg1.py`, `agg2.py` (§2.1), `boxmob.py`, `deadend.py`, `entry.py` (§2.2–2.3),
  `traps.py`, `safemap.py MAP [x0 x1 y0 y1]`, `mapat.py` (maps with kings and cats placed).
- `armdiag.py RUN...` (an arm's gain test with `--replays all`: block/ATTACK HP, fatal pins, king deaths by fate, the
  ref's and the arm's fate on each game; `ARMDIAG=-v` lists the changed games); `diag/RUN.jsonl` its CLPin rows.
- Arms: `t3/cl-arm-v` (V), `t3/cl-arm-a1` (A1). V adds `CellTracker.passableRow`/`kingCentres` and
  `KingCombatStrategy.narrowingMoves` (room of the 9 cells from 7 row masks, a few hundred bytecodes with a cat in
  view).
