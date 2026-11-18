# Battlecode 2026 "Uneasy Alliances": rules reference

This is a dense reference for agents writing bot code. Where the engine and the written spec disagree, **the engine wins**, and this file describes the engine. The per-method API (bytecode costs and preconditions) is in [`api-cheatsheet.md`](api-cheatsheet.md).

Source pointers such as `engine/src/main/battlecode/world/InternalRobot.java:481` are relative to a clone of https://github.com/battlecode/battlecode26. Line numbers are valid at tags `engine.1.2.5` and `engine.1.2.6`, which differ only by 4 lines inside `GameWorld.spawnCheese`.

## 0. Versions and sources

| Thing | Value |
|---|---|
| Engine pinned in this repo | `1.2.5` (`engine_version.txt`). The jar in the gradle cache is byte-identical in behaviour to tag `engine.1.2.5` (commit `991c91af`, 2026-01-24). |
| Latest public engine | `1.2.6` (tag `engine.1.2.6`, commit `6c872c8d`, 2026-01-28) |
| Saturn (ranked/tournament servers) | `1.2.7.private2` (commit `20cd2cb7`, 2026-01-29 23:44). **Same engine source as 1.2.6**: the diff only deletes bundled maps and adds an example bot. The Final tournament (bc26final, Jan 31, submission freeze Jan 29 19:00 EST) therefore almost certainly ran 1.2.6 gameplay. |
| `battlecode26-specs.txt` (repo root, and `src/awubot/` copy) | **Outdated** (changelog ends at v1.0.8). The two copies differ only in how the strafe-cooldown sentence is worded. Many numbers in it are wrong now (see §15). |
| Current spec | `specs/specs.pdf` in the engine repo (changelog up to v1.2.5). It is better, but still wrong in places (§15). |
| 1.2.4 → 1.2.5 | No gameplay change (client visualisation of partially picked-up cheese only). |

### 1.2.5 vs 1.2.6: the only gameplay change (`GameWorld.spawnCheese`, lines 510–519)

| | 1.2.5 (this repo) | 1.2.6 / Saturn |
|---|---|---|
| Spawn offset `dx,dy` | `rand.nextInt(-4, 4)` → **[-4, +3]** (an 8×8 box, skewed toward −x/−y) | `nextInt(-4, 5)` → [-4, +4] (9×9 box, as the spec says) |
| Offset at the paired (mirror) mine | VERTICAL: `(dx, -dy)`, HORIZONTAL: `(-dx, dy)`. **Both are wrong**: the paired spawn is the point reflection of the correct mirror. ROTATIONAL `(-dx,-dy)` is correct. | Correct mirror: VERTICAL `(-dx, dy)`, HORIZONTAL `(dx, -dy)`, ROTATIONAL `(-dx,-dy)` |
| RNG stream | A bound of 8 is a power of two (one draw) | A bound of 9 can reject and redraw, so every later cheese roll diverges. The same seed gives a different game. |

So under 1.2.5, cheese placement on reflection-symmetric maps is not mirror-symmetric between the two sides, and nothing ever spawns at offset +4. Bots tuned on 1.2.5 may slightly misjudge where cheese appears on 1.2.6. Everything else (units, actions, cats, scoring) is identical.

**Local harness difference.** This repo overrides `battlecode.server.Server`/`Main`/`world.GameMapIO` (`src/battlecode/`) so that the RNG seed comes from `-Pseed` (default `0`) instead of the seed embedded in the map file. That seed drives cheese spawns and spawned-robot IDs. Official servers use the map's own seed.

---

## 1. Match structure and win conditions

- A **match** is ≥3 **games**, each on a different map. The most game wins advances. Tournaments are double elimination, except the Sprints, which are single elimination. Locally, `best-of-three` stops once one side has 2 wins.
- A **game** lasts up to `GAME_MAX_NUMBER_OF_ROUNDS = 2000` rounds. `rc.getRoundNum()` runs from 1 to 2000.
- The game state is reset per game: cooperation mode, cheese, shared array, and the per-team execution-time budget.

**Game-ending checks.** A winner can be set mid-round, but the game stops only at the **end of that round**, so every other robot still takes its turn. `GameWorld.destroyRobot` → `checkWin` (`GameWorld.java:980`, `:1179`).

| Order | Condition | Result |
|---|---|---|
| 1 | A team's last rat king dies (any cause: damage, starvation, crushed, its own exception, `disintegrate`) | That team loses immediately (`KILL_ALL_RAT_KINGS`) |
| 1b | Both teams lose their last king in the same round | Falls through to the point comparison below |
| 2 | Last cat dies **while in cooperation** | Game ends and is decided by points (cooperation weights) |
| 2b | Last cat dies after a backstab | Nothing happens; play continues |
| 3 | End of round 2000 with no winner | Points → tiebreakers |
| – | `rc.resign()`, or a team exceeds `MAX_TEAM_EXECUTION_TIME` = 1.2e12 ns (1200 s of wall time per team per game) | That team loses (`RESIGNATION`) |

**Points** (`GameWorld.setWinnerIfMorePoints`, `:911`). Each share is `myStat / (statA + statB)`, or 0 if the total is 0.

```
coop:      points = (int)(50*catDmgShare + 30*kingShare + 20*cheeseShare)
backstab:  points = (int)(30*catDmgShare + 50*kingShare + 20*cheeseShare)
```

- The state is read when the game ends. It is **truncated** to int, not rounded as the spec says.
- `catDmg` is bite damage plus cat-trap damage dealt to cats, each hit capped at the cat's remaining HP. Cats crushed or fed by thrown rats count nothing.
- `kingShare` is the number of kings alive at the end.
- `cheeseShare` is the cheese transferred by baby rats to kings (`transferCheese`) **plus cheese picked up directly by rat kings**. Not counted: the 2500 starting cheese, raw cheese absorbed by rat-king formation, and raw cheese still being carried.

**Tiebreakers, in exact order:** (1) more points, (2) more **global** cheese (raw cheese is not counted), (3) more rats alive (baby rats + kings), (4) `Math.random()`, which is non-deterministic even with the same seed (`GameWorld.java:958`).

---

## 2. The alliance mechanic: cooperation and backstab

- Every game starts with `rc.isCooperation() == true`. The first backstab event flips it to false **permanently for that game**, effective immediately, during the very turn it happens.
- `rc.getBackstabbingTeam()` returns the team that is blamed, or `null` while cooperating.

| Event | Backstab? | Who is marked the backstabber |
|---|---|---|
| Bite (`attack`) on an enemy baby rat or enemy rat king | Yes | The biter's team (`InternalRobot.java:623`) |
| Bite on a cat | No | – |
| An enemy rat (baby or king) triggers your **rat trap** | Yes | **The trap owner**, even though the victim walked in (`GameWorld.java:639-642`) |
| A cat trap triggers | No | – |
| `carryRat` on an enemy baby rat | Yes | The grabber's team (`InternalRobot.java:648`) |
| Thrown-rat collision damage to an enemy | **No** | – |
| Rat-king formation crushes enemy baby rats | **No** | – |
| Cats killing rats, feeding a cat, dirt blocking | No | – |

Effects of a backstab:

1. The score weights switch (see §1).
2. All cats dying no longer ends the game.
3. **Cat traps:** new ones can be placed only by the team that was backstabbed (the non-backstabber), and only while `currentRound - backstabRound <= 100` (`GameWorld.catTrapsAllowed`, `:420`). Existing cat traps keep working.

There is no other "alliance" rule: there are no shared resources and no diplomacy API.

---

## 3. Round and turn lifecycle

1. `processBeginningOfRound`: `round++`. Every robot drops squeaks whose `round <= currentRound - 5`, and indicator strings are cleared.
2. Each robot in **execution order** (`ObjectInfo.eachDynamicBodyByExecOrder`, `:98`) does the following:
   - `processBeginningOfTurn` (`InternalRobot.java:1110`):
     - The first robot of the round runs all cheese mines.
     - Carried and thrown rats are advanced (auto-drop or swap, flight).
     - All three cooldowns decrease by 10, floored at 0, **unless** the robot is being carried, being thrown, or is a sleeping cat.
     - The bytecode limit is reset to the robot's type limit.
   - The player code runs until `Clock.yield()`, or until the bytecode limit pauses it mid-statement (it resumes there next turn).
   - `processEndOfTurn` (`:1176`):
     - Each rat king **eats 2 global cheese now**. If global cheese < 2, the king loses 10 HP instead.
     - **The cat AI runs here** (cats have no player code).
3. `processEndOfRound`: the win check at round 2000.

Execution order rules:

- Robots run in **spawn order**. The initial bodies (kings and cats) are sorted by their ID **as given in the map file** (`LiveMap.java:116,161`).
- Robots spawned this round are not in this round's snapshot, so they first act **next round** (with cooldowns 10 → 0, i.e. ready).
- A rat that becomes a king keeps its slot and its ID.
- On many official maps the cats have the lowest IDs, so **cats act first** (DefaultSmall/Medium/Large, TheHeist, FreeTheCheese). On others the kings come first (cheeseguardians, rift). The spec's "cats act last" is not reliable.
- Initial kings and cats use small map IDs (e.g. 1–24). Spawned robots get IDs ≥ 10000 (`IDGenerator.MIN_ID`), from shuffled blocks seeded by the map seed.

**Robot death by code.** If `run()` returns, an exception escapes `run()`, or the robot calls `rc.disintegrate()`, the robot is destroyed (`SandboxedRobotPlayer.java:200-236`, `GameWorld.updateRobot`). A king dying this way counts as a king loss. Each caught exception also costs 500 bytecodes (`EXCEPTION_BYTECODE_PENALTY`).

---

## 4. Cooldowns

- There are three independent counters: action, movement, turning.
- An action/move/turn is allowed when the counter is `< COOLDOWN_LIMIT (10)`. Each turn subtracts `COOLDOWNS_PER_TURN (10)`, floored at 0, so unused time cannot be banked.
- The sustained rate is `10/cost` uses per round. Because "< 10" is the threshold, a cost-18 strafe can be done on two consecutive rounds (0 → 18 → 8 → 26 → 16 → 6 ...), averaging 1.8 rounds per strafe.
- Being carried, being thrown, or (for a cat) sleeping **freezes** all cooldowns.
- Newly created robots start at 10/10/10, so they are ready on their first turn.

**Formula** (`InternalRobot.java:479-517`). Truncation is via `(int)`, and `c` is the robot's raw cheese at the moment the cooldown is added:

- Baby rat action/move: `cost' = (int)(cost * (1 + 0.01*c))` (`CHEESE_COOLDOWN_PENALTY`). Turning is never penalised.
- `CARRY_COOLDOWN_MULTIPLIER = 1.5` is cast with `(int)` to **1**, so **carrying has no cooldown penalty** (an engine bug).
- Kings and cats have no cheese penalty (kings never hold raw cheese).
- Trap stuns **set** the movement cooldown to the stun value instead of adding to it (§10).

| Cost source | Base | Notes |
|---|---|---|
| Baby rat move forward (in its facing direction) | 10 (`BABY_RAT.movementCooldown`) | ×cheese. With c ≤ 9 there is no penalty, because (int)10.9 = 10. |
| Baby rat strafe (any of the other 7 directions) | 18 (`MOVE_STRAFE_COOLDOWN`) | ×cheese. With c ≤ 5 there is no penalty. |
| Rat king move (any direction) | 40 (`RAT_KING.movementCooldown`) | – |
| Cat walk | 20 | – |
| Cat pounce | 40 | Adds the walk cost twice |
| Turn (any unit) | 10 (`TURNING_COOLDOWN`) | – |
| Bite (baby rat or king) | 10 (`UnitType.actionCooldown`) | ×cheese, using raw cheese *before* the bite's cheese is spent |
| Cat scratch | 30 (`CAT.actionCooldown`) | – |
| Build rat | 10 (`BUILD_ROBOT_COOLDOWN`) | – |
| Dig dirt / place dirt | 25 / 25 (`DIG_COOLDOWN`) | A cat dig costs 25 + `CAT_DIG_ADDITIONAL_COOLDOWN` 5 = 30 |
| Rat trap / cat trap | 15 / 10 (`TrapType.actionCooldown`) | – |
| Transfer cheese | 10 (`CHEESE_TRANSFER_COOLDOWN`) | ×cheese, using raw cheese *after* the transfer |
| carryRat / dropRat / throwRat | 20 / 20 / 20 (`THROW_RAT_COOLDOWN`) | – |
| Pick up cheese, remove trap, squeak, sense, shared array, becomeRatKing | **0** | No readiness check either, except becomeRatKing, which requires action-ready |
| Thrown rat lands normally / hits something | +10 / +20 to **all three** (`HIT_GROUND_COOLDOWN` / `HIT_TARGET_COOLDOWN`) | – |

---

## 5. Map

| Property | Rule (engine) |
|---|---|
| Size | `MAP_MIN_* = 20` to `MAP_MAX_* = 60` per side; **odd sizes allowed** (21×21, 59×20, 41×35 ...). Origin (0,0) is bottom-left; +x is East, +y is North. |
| Symmetry | `ROTATIONAL` (x→W-1-x, y→H-1-y), `HORIZONTAL` (y flipped, x kept), `VERTICAL` (x flipped, y kept). This is not checked by the validator; map makers guarantee it. |
| Tiles | Empty, **wall** (permanent, impassable), **dirt** (impassable, removable). A cheese mine sits on a passable tile. Cheese is an integer amount on a tile and may sit on dirt. |
| Passable | `!wall && !dirt && no flying (thrown) rat on the tile` (`GameWorld.isPassable`, `:478`) |
| Validation (`LiveMap.assertIsValid`, `:421`) | Exactly 1 king per team. Walls `< 20%` (`MAX_WALL_PERCENTAGE`), dirt `< 50%` (`MAX_DIRT_PERCENTAGE`). Mines pairwise dist² ≥ 25 (`MIN_CHEESE_MINE_SPACING_SQUARED`). No wall within dist² ≤ 8 of a mine. No wall on a mine. |
| Initial bodies | 1 rat king per team (team A chirality 0, team B chirality 1) plus N cats (team `NEUTRAL`, alternating chirality), each at a map-defined position. **Cats do not spawn at the map centre.** Initial facing is the direction toward `((W-1)/2, (H-1)/2)`. |
| Cat waypoints | Per-cat lists stored in the map file. They are invisible to the API. |
| Team swap | With `alternateOrder`, teams A and B swap positions; chirality stays with the position. |

**Official map statistics.** These cover 42 public maps and the 31 final-tournament maps added to the engine repo at `103abf6b`:

- Cats: 2–20 per map, always an even number. Most maps have 2 or 4; sunandmoon has 20, cheeseguardians 10.
- Cheese mines: 1–24. Odd counts exist (1, 3, 11). With an odd count, one mine is its own mirror pair.
- Walls: 0–19.9%. Dirt: 0–49.7%. No map has initial cheese on the ground.
- Waypoints per cat: 2–30.

### Cheese mines (`GameWorld.spawnCheese`, `:506`; `CheeseMine.java`)

- Each mine is paired with its symmetric mine (a centre mine may pair with itself).
- Every round, each mine in tile-index order (row by row from y=0) rolls `rand.nextFloat() < 1 - 0.99^r`, where `r` is the number of rounds since its pair last spawned. At game start, r = the round number. `rand` is seeded by the map seed.
- On success, `CHEESE_SPAWN_AMOUNT = 20` is added at `mine + (dx,dy)` and 20 at `pair + mirrored(dx,dy)`. The offsets are described in §0. Both mines' timers reset, so the partner's roll that round has p = 0.
- Validity check: both target tiles must be on the map and **not a wall**. Dirt is allowed; "cat-accessible" is not checked.
- If the check fails, the "5 retries" loop never re-rolls. **The cheese then spawns on the two mine tiles themselves** (`:517-539`).
- Expected time between spawns: **≈ 8.9 rounds per pair**, giving ≈ 40 cheese per 8.9 rounds, i.e. ≈ 2.25 cheese/round per mine. A self-paired mine: ≈ 12.5 rounds, 40 cheese per event.
- Cheese accumulates: a tile can hold more than 20.

---

## 6. Units

| | Baby rat | Rat king | Cat (NPC, team NEUTRAL) |
|---|---|---|---|
| Footprint | 1×1 | 3×3; `getLocation()` is the **centre** | 2×2; `getLocation()` is the **bottom-left** tile |
| HP (`UnitType.health`) | 100 | 600 (start, and the cap at formation) | 4000 |
| Vision | r² 20, **90° cone** | r² 25, 360°, from the centre | r² 17, 180° cone, distance measured from the 2×2 centre `(x+0.5, y+0.5)` |
| Visible tiles | 21 (orthogonal facing) / 22 (diagonal facing) | 81 | 27 / 30 |
| Move cooldown | 10 forward / 18 strafe | 40 any direction | 20 walk, 40 pounce |
| Action cooldown base | 10 | 10 | 30 (scratch) |
| Bytecode limit | 17 500 | 20 000 | n/a |
| Damage | Bite 10 + ⌈√cheeseSpent⌉ | Same bite, reaching the 16-tile ring | Scratch 20 (`CAT_SCRATCH_DAMAGE`); crush/pounce = instant kill on baby rats |
| Can carry / be carried | yes / yes | no / no | no / no |
| Cost | `10 + 10*floor(numBabyRats/4)` cheese, from global | 50 global cheese; the centre rat and its ≥ 6 allied neighbours are consumed | – |
| Cap | – | `MAX_NUMBER_OF_RAT_KINGS = 5` alive. **After round 1200, no new king if the team has ≥ 2** (`RAT_KING_CUTOFF_ROUND`, `MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF`). Existing kings are never removed. | – |
| Upkeep | – | 2 global cheese per king turn; if global < 2, −10 HP instead | – |

---

## 7. Geometry: sensing and action ranges

- **Cone test** (`MapLocation.isWithinDistanceSquared(...,useBottomLeft)`, `:180`): a tile is visible iff `dist² ≤ r²` **and** the angle between the facing vector and the vector to the tile is `≤ θ/2` (+1e-3°, so edges are inclusive). The robot's own tile is always visible.
- A baby rat therefore sees exactly **3 of its 8 neighbours**: the front one and the two diagonals beside it. For a diagonal facing, that is the diagonal tile plus the two orthogonal tiles beside it.
- **Every action that targets a tile also requires that tile to be in the actor's vision.** A baby rat can therefore act only on its 3 front neighbours (plus its own tile, for cheese pickup).
- A multi-tile robot is sensed if any of its parts is visible. `RobotInfo.location` is the king's centre or the cat's bottom-left tile.
- **Carried and flying (thrown) rats are not in the robot grid.** `senseNearbyRobots` / `senseRobotAtLocation` / `isLocationOccupied` do not return them.
  - A carried rat appears as `RobotInfo.getCarryingRobot()` of its carrier, and `senseRobot(id)` works on it.
  - A flying rat appears via `MapInfo.flyingRobot()` and makes its tile impassable. `canSenseRobot(id)` is false for flying rats.
- Sensing order follows **chirality**. Chirality 0 scans x ascending, then y ascending. Chirality 1 reverses x (VERTICAL symmetry), y (HORIZONTAL), or both (ROTATIONAL). Read your own chirality with `rc.senseRobot(rc.getID()).getChirality()`. It belongs to the starting position: the map's team-A king is 0, team-B is 1, and spawned rats inherit it.

Range table. "dist²" is measured from the actor's `getLocation()`; for a cat, it is float dist² from the cat centre.

| Action | Baby rat | Rat king (from centre) | Notes |
|---|---|---|---|
| Bite | dist² ≤ 2 (`ATTACK_DISTANCE_SQUARED`), adjacent, in cone | dist² ≤ 8 (`RAT_KING_ATTACK_DISTANCE_SQUARED`): the 16-tile ring | Target tile must be passable and hold a non-allied robot |
| Dig / place dirt, place / remove traps | ≤ 2 (`BUILD_DISTANCE_SQUARED`) | ≤ 8 (`RAT_KING_BUILD_DISTANCE_SQUARED`) | Cat dig: ≤ 4.5 (`CAT_BUILD_DISTANCE_SQUARED`), the 12-tile ring around the 2×2 |
| Pick up cheese | ≤ 2 (`CHEESE_PICK_UP_RADIUS_SQUARED`): own tile + 3 front tiles | ≤ 2 from centre: **only the 9 tiles under the king** | – |
| Transfer cheese to king | ≤ 9 (`CHEESE_TRANSFER_RADIUS_SQUARED`) to **any tile of the king**, in cone | – | Only baby rat → allied king |
| Build rat | – | ≤ 8 (`BUILD_ROBOT_RADIUS_SQUARED`): the 16-tile ring | – |
| Carry rat | ≤ 2, adjacent, in cone | – | – |
| Squeak | r² 16 (`SQUEAK_RADIUS_SQUARED`) around the sender's location; reaches robots with **any** tile inside | Same, from the centre | – |
| Cat scratch | – | – | Anywhere in the cat's vision (r² 17, 180°) |
| Cat pounce | – | – | Target tile within float dist² ≤ 13 (`CAT_POUNCE_MAX_DISTANCE_SQUARED`) of the cat centre (44 tiles) |

---

## 8. Actions: preconditions and effects

"In range" means within the §7 range **and** inside vision. "Ready" means the relevant cooldown is < 10 and the robot is not being carried or thrown. Each `canX` mirrors the asserts of `X`.

| Action (method) | Who | Preconditions | Effects / cost |
|---|---|---|---|
| `move(d)` / `moveForward()` | all | Move-ready. Every new part tile is on the map, passable, and not occupied by another robot. A cat may enter baby-rat tiles, but not king or cat tiles. | Translate. Then process traps for **every** new part tile. Then add the move cooldown (strafe if `d != facing`, baby rats only). Facing does **not** change. |
| `turn(d)` | all | Turn-ready; `d` not null or CENTER. Turning to the current facing is allowed and wastes the cooldown. | Set facing; +10 turn cooldown. |
| `attack(loc[, cheese])` | baby, king | Action-ready; loc in vision; dist² in (0, 2] (king (0, 8]); passable; `cheese ≥ 0` and `≤ raw + global`; a robot at loc not on your team (cats are allowed) | Damage `10 + ceil(sqrt(cheese))` if cheese > 0. The cheese is spent **raw first, then global**. Backstab if the target is not a cat. +10 action cooldown (×cheese penalty). |
| `buildRat(loc)` | king | Action-ready; loc in the ring (dist² ≤ 8); **global** cheese ≥ `getCurrentRatCost()`; loc unoccupied and passable | Spawns a baby rat facing **the king's facing**, with the king's chirality. Cost `10 + 10*floor(numBabyRats/4)` (counts baby rats only). +10 action cooldown. The new rat acts next round. Spawning on an enemy trap's area does **not** trigger it. |
| `becomeRatKing()` | baby rat (center) | Action-ready. **Global** cheese ≥ 50. Kings < 5, and not (round > 1200 and kings ≥ 2). The whole 3×3 is on the map, all passable (no wall, dirt, or flying rat), with no king or cat in it. **≥ 7 allied baby rats in the 3×3, counting itself** (i.e. 6 neighbours). | The 8 surrounding robots die: allies **and enemies**, plus whatever they carry. Their raw cheese goes to your **global** pool. A rat carried by the centre rat dies. HP = sum of allied HP (self included), capped at 600. −50 global, and the centre's raw cheese goes to global. Then enemy traps whose trigger area covers the 8 outer tiles fire. The ID, cooldowns and facing are kept. **No action cooldown is added**, so the new king can `buildRat` the same turn. **No backstab.** |
| `removeDirt(loc)` | all | Action-ready; in range; dirt at loc; rats need raw + global ≥ 5 | Tile becomes passable; your team's dirt count +1 (a cat's dig is just destroyed). Rats pay 5 cheese (`DIG_DIRT_CHEESE_COST`). +25 action cooldown. |
| `placeDirt(loc)` | rats | Action-ready; in range; team dirt ≥ 1; loc is not a wall, not dirt, not a mine, and has no ground robot | Dirt placed; team dirt −1; costs 0 cheese (`PLACE_DIRT_CHEESE_COST`). +25 action cooldown. **Allowed on tiles with cheese or traps** (the spec says otherwise). |
| `placeRatTrap(loc)` | rats | Action-ready; in range; passable (no wall, dirt, or flying rat); no robot; **no trap of your own team** on the tile (any type; an enemy trap there is fine); team rat traps < 25; raw + global ≥ 20; not a mine tile | Trap placed; −20 cheese (raw first); +15 action cooldown. |
| `placeCatTrap(loc)` | rats | As above with the cat-trap numbers (≤ 10 active, cost 10, +10 cooldown), plus `catTrapsAllowed(team)` (§2) | – |
| `removeRatTrap` / `removeCatTrap(loc)` | rats | In range (no readiness check, works even while carried); **your own** trap of that type is at loc | Removed; no refund; **no cooldown**. |
| `pickUpCheese(loc[, amount])` | rats | In range; cheese > 0 at loc; not being thrown. **No readiness check, no cooldown, unlimited calls per turn, and it works while being carried.** | Baby rat: adds to raw. King: adds to global and counts as "transferred" for scoring. The amount is clamped to [0, available]. |
| `transferCheese(loc, amt)` | baby rat | Action-ready; loc in range (≤ 9) and in vision; robot at loc is an allied **king**; `0 < amt ≤ raw` | raw → global; counts as transferred. +10 action cooldown (penalty uses the remaining raw). |
| `carryRat(loc)` | baby rat | Action-ready; adjacent and in cone; not already carrying; not carried; target is a **baby rat**, not flying, not self. The target qualifies if **any** of: it **cannot see you** (you are outside its cone), it is your teammate, or `targetHP < yourHP` (strictly; `HEALTH_GRAB_THRESHOLD = 0`). Re-grab block: an enemy you most recently carried cannot be re-grabbed until 2 of its turns have passed since it was dropped or landed (`SAME_ROBOT_CARRY_COOLDOWN_TURNS`). | Target removed from the grid and moves with you. If it was itself carrying a rat, that rat is dropped on its tile. Enemy target → **backstab**. +20 action cooldown. |
| `dropRat(dir)` | carrier | Action-ready; adjacent tile in **any** direction is on the map, passable, and has no robot | Dropped rat keeps its pre-carry facing; traps at the drop tile fire. +20 action cooldown. |
| `throwRat()` | carrier | Action-ready; the tile in **your facing direction** is on the map, passable, and empty | §9. +20 action cooldown. |
| `squeak(int)` | all players | Max 1 per turn (`MAX_MESSAGES_SENT_ROBOT`); returns false otherwise. Allowed while carried or flying. | §13 |
| `writeSharedArray(i, v)` | **king only** | 0 ≤ i < 64; 0 ≤ v ≤ 1023 | Visible to the team immediately |
| `resign()` | any | – | Your whole team is destroyed and the opponent wins |

---

## 9. Carrying and throwing in detail

**Carried rat** (`InternalRobot.processBeginningOfTurn`, `:1116-1141`):

- Shares the carrier's location and is invisible to grid sensing.
- Immune to bites and scratches.
- Its cooldowns are frozen.
- It can sense, squeak, pick up cheese, remove its own traps, and read the shared array. It cannot move, turn, or do any action that requires readiness.

**Auto-release.** `remainingCarriedDuration` is set to 10 (`MAX_CARRY_DURATION`) on grab and decremented on each of the carried rat's turns. On the turn it is 0 (the 11th carried turn):

- If `carrierLoc + carriedRat's own facing` is on the map, passable, and empty, the rat is dropped there ("wriggles free"). The spec says "in front of the carrier", which is **wrong**.
- Otherwise the roles **swap**: the carried rat becomes the carrier of its former carrier, with a fresh 10-turn timer (`swapGrabber`, `:659`).
- On the turn of release the rat's cooldowns tick normally, so it can act immediately.

**Carrier dies** (bitten etc.): the carried rat is dropped on the carrier's tile and traps fire.

**Crush:** a cat walking or pouncing onto a carrier kills **both** rats.

**Throw** (`getThrown` `:718`, `travelFlying` `:815`, `hitGround` `:754`, `hitTarget` `:780`):

- The thrown rat becomes a flying rat: impassable, untargetable, does not trigger traps, cannot pick up cheese, and its cooldowns are frozen.
- Flight speed is 2 tiles per step (`TILES_FLOWN_PER_TURN`), with `remaining = 4` (`THROW_DURATION`):

| Moment | Tiles flown | `remaining` |
|---|---|---|
| Throw turn (thrower's turn) | 2 | 4 |
| Each later turn of the thrown rat | 2 | `remaining--` first. If the rat was thrown earlier this same round, it decrements **without** travelling. When `remaining` reaches 1 after travelling, it lands. |

- Total flight is therefore **8 tiles if the thrown rat acts before the thrower in turn order, 6 tiles if after.**
- **Normal landing** (end of flight, or the map edge, which is not an obstacle): 10 damage (`THROW_DAMAGE`); +10 to all three cooldowns; traps at the landing tile fire.
- **Collision** (next tile has a ground robot, wall, dirt, or a flying rat):
  - The rat stops on its current tile.
  - It **and the ground robot it hit** (any team, kings included) take `10 + 4*(2*remaining + (first step of the pair ? 1 : 0))`. The throw needs the tile in front empty and passable (`RobotControllerImpl.java:1405`), so the first step never collides: **42** at most on the throw turn, then 38/34, 30/26, 22/18 later. No collision in 4,195 replayed throws dealt 46 (docs/theory/t2-combat.md).
  - The flier gets +20 to all three cooldowns. The robot hit is not stunned.
  - Hitting another flying rat: that rat just gets `remaining = 1` and lands normally on its next turn.
  - **None of this triggers a backstab.**
- **Cat in the path:** the thrown rat dies ("feeding") and the cat sleeps for 2 cat turns (`CAT_SLEEP_TIME`): no AI, cooldowns frozen. Any rat can be fed, including your own.

---

## 10. Traps

| | Rat trap | Cat trap |
|---|---|---|
| Cost / action cooldown | 20 / 15 | 10 / 10 |
| Max active per team | 25 | 10 |
| Trigger area | r² 2 = the trap's 3×3 | Same |
| Triggered by | Enemy baby rats or kings **entering** a tile of the area: move, landing from a throw, being dropped, rat-king formation, auto-release | A cat moving or landing (pounce) with any of its 4 tiles in the area |
| Effect | 50 damage; **movement cooldown set to 30**; backstab (owner blamed) | 100 damage (credited to the owner's cat-damage stat); **movement cooldown set to 20** |
| Visibility | Only to the owner team (`MapInfo.getTrap()` shows own traps only); hidden from enemies and cats | Same |

- A trap fires once and is then removed. Several traps entered at the same moment each deal damage (stacking), but the stun does not stack because it is a set, not an add.
- Order inside `move`: translate → traps fire (cooldown set to the stun) → the move cost is added, so a stunned rat ends at 30 + 10. Cat walk: 20 + 20. Cat pounce: 20 + 40. Rat-king formation and drops: cooldown set to 30 exactly. A rat already carrying a large cooldown gets it *lowered* to 30.
- Standing still in an area, or being spawned into one, does not trigger a trap. A later move to another tile inside the area does.
- Both teams may have a trap on the same tile. You cannot stack two of your own traps on one tile.

---

## 11. Economy

| Item | Value |
|---|---|
| Starting cheese | 2500 global (`INITIAL_TEAM_CHEESE`) per team; team dirt starts at 0 |
| Global vs raw | Kings never hold raw cheese; their pickups go straight to global. Baby rats hold raw cheese until `transferCheese` (or until they die: raw cheese **drops on their tile**, or goes to the formation team if crushed by `becomeRatKing`). |
| Spending order | Bite, traps and dig: **raw first, then global** (`InternalRobot.addCheese`, `:251`). Build rat and becomeRatKing check and spend **global only**. |
| King upkeep | `RAT_KING_CHEESE_CONSUMPTION = 2` per king turn, from global; if global < 2, −10 HP (`RAT_KING_HEALTH_LOSS`). A starving 600-HP king survives 60 rounds. 5 kings cost 10 per round. |
| Income | ≈ 2.25 cheese/round per mine (§5). The 2500 starting cheese alone covers 1250 rounds of one king's upkeep, so roughly one mine per king is needed to break even. |
| Raw-cheese slowdown | §4 (×(1 + 0.01·c) on baby-rat move and action costs) |
| Rat cost curve | 10 (0–3 baby rats alive), 20 (4–7), 30 (8–11), ..., 60 at 20 rats, 110 at 40 |

---

## 12. Cats (NPC) in detail

The AI runs in `InternalRobot.processEndOfTurn`, `:1191-1418`. The state is `EXPLORE` or `ATTACK`, and every game starts in `EXPLORE` toward waypoint 0. A sleeping cat skips the AI entirely. A null or CENTER facing is replaced by a random direction, using a **static** `Random(1092)` that is shared across games in the same JVM (`:59`).

**Reference corner.** The cat navigates with a chirality corner. Chirality 0 uses its bottom-left tile; chirality 1 uses the mirrored corner (VERTICAL: bottom-right, HORIZONTAL: top-left, ROTATIONAL: top-right).

**Pathing.** BFS is precomputed per target and chirality over 8 directions (`GameWorld.bfsFromTarget`, `:223`). It treats **only walls and map edges** as obstacles for the 2×2 footprint. Dirt, rats and kings are ignored by the BFS and dealt with on contact.

### EXPLORE (each cat turn)

1. If `stuck ≥ 4`: turn to a random direction (if turn-ready).
2. Else if `stuck == 0`:
   - If the corner is on the current waypoint and `currentWaypoint != previousWaypoint`, switch to `ATTACK` (the rest of this turn is skipped).
   - Otherwise (it has already attacked there), advance to the next waypoint (cyclic).
   - Both indices start at 0, and on almost every official map the cat starts on or next to waypoint 0. So the **first** visit to waypoint 0 never triggers ATTACK; every later arrival at a waypoint does.
   - Set facing to the BFS direction toward the waypoint. This **facing change is free**: no turn cooldown.
3. If `canMove(facing)`, move (crushing baby rats, including any rat they carry); stuck = 0.
4. Otherwise, for each of its 4 tiles' next tile in the facing direction: if there is dirt there, dig it; else if a non-cat robot is there, scratch it. Blocking kings get scratched, and so can baby rats in those tiles.
5. If neither worked, try to **pounce 2 tiles ahead** (corner + 2·facing). If that also fails, `stuck++`.

**In EXPLORE the cat never targets rats.** It only kills what it walks or pounces onto, or scratches whatever blocks it. Squeaks received in EXPLORE stay queued, and are dropped after 5 rounds.

### ATTACK (8 cat turns, `catTurns > 8` → back to EXPLORE, which also skips that turn)

1. Take the **oldest** queued squeak, then clear the queue. Sense nearby robots **using the current facing**. Then, if a squeak was taken, set facing toward its source location (free).
2. If the previous target's ID is still in the sensed list, keep it. Note that `catTargetLoc` is taken from the **stale RobotInfo captured when the target was first acquired** (`:1326`).
3. Otherwise pick a new target: the **last** baby rat or king in the sensed list. The list follows chirality scan order, so the target is the max-x (then max-y) rat for chirality 0 on rotational maps, mirrored for chirality 1. Team is ignored.
4. With a target:
   - a. If `canAttack(target's stale location)`, scratch: 20 damage, +30 action cooldown, done. This needs action-ready, the location in the *current* cone, passable, and a non-cat robot there.
   - b. Else pounce to the stale location if possible (move-ready; all 4 landing tiles on the map, passable, no cat (including **itself**), no king) and done. A pounce kills every baby rat (and its cargo) under the landing tiles, then cat traps fire.
   - c. Else set facing to the BFS direction toward the location and move if possible, done.
   - d. Otherwise dig the blocking dirt.
5. If no `done` was reached (no target, dig, or failure): rotate 45° (right for chirality 0, left for chirality 1) if turn-ready.

After 8 turns the target is forgotten and the cat returns to its last waypoint. Reaching that waypoint again advances it to the next one, without a second attack phase there.

**Damage to cats:** bites (10 + ⌈√c⌉) and cat traps (100). Cats cannot be carried. They cannot be damaged by thrown collisions: a thrown rat is eaten instead.

---

## 13. Communication

| Channel | Rules |
|---|---|
| Shared array | 64 ints (`SHARED_ARRAY_SIZE`), each 0..1023 (`COMM_ARRAY_MAX_VALUE`, i.e. 10 bits), one array per team. **Only kings write** (50 bytecodes); everyone reads (10). Writes are visible immediately to robots acting later in the same round. Initialised to 0; persists for the game. A bad read index throws `ArrayIndexOutOfBoundsException`, not a GameActionException. |
| Squeak | `squeak(int)` carries a full 32-bit int, plus sender ID, sender location (the king's centre) and round. 1 per turn. Delivered **instantly** to every robot with any tile within r² 16 of the sender that is an ally **or any cat**. Never delivered to enemies, the sender itself, or carried/flying rats (they are not in the grid). |
| Squeak lifetime | `readSqueaks(-1)` returns all queued squeaks; `readSqueaks(r)` returns those from round r. A squeak from round R is readable through round R+4 (`MESSAGE_ROUND_DURATION = 5`). Squeaks sent after your turn in round R are seen in R+1. |
| Squeaks and cats | Cats hear squeaks from both teams. They use them only in ATTACK: each turn they turn toward the oldest queued squeak and clear the queue (§12). |

---

## 14. Computation limits

| Limit | Value |
|---|---|
| Bytecodes per turn | Baby rat 17 500, king 20 000. A rat that becomes king mid-turn keeps 17 500 for that turn. Unused bytecodes are lost. Going over the limit pauses the code mid-statement until next turn (the world will have changed). |
| Exception | +500 bytecodes on entering a catch handler. An uncaught exception kills the robot. |
| Array allocation | NEWARRAY/ANEWARRAY/MULTIANEWARRAY cost the total array length |
| `System.arraycopy` | 1 bytecode per element |
| Library code | `java.util`, `java.math` and scala are counted as your own code. `Math.*` is mostly 1 bytecode. `Clock.*` is 0. API costs are in `api-cheatsheet.md` (source: `engine/src/main/battlecode/instrumenter/bytecode/resources/MethodCosts.txt`). Methods not listed there cost only the call. |
| Allowed packages | `java.io/lang/math/util(.function/.regex/.stream)/text`, `battlecode.common`, `scala.*`, minus `DisallowedClasses.txt`. Also banned: `Object.wait/notify`, `Class.forName`, `String.intern`, `System.*` except `out`, `arraycopy` and `getProperty("bc.testing.*")`. |
| Memory | About 8 MB heap per robot ("may explode") |
| Wall time | 1200 s per team per game (`MAX_TEAM_EXECUTION_TIME`); exceeding it means resignation. Check with `Clock.getTimeLeft()`. |
| Python | Native ops are counted ×3; engine calls use the table. Irrelevant for this Java repo. |

---

## 15. Spec vs engine conflicts

"repo spec" = `battlecode26-specs.txt` (≈ v1.0.8); "pdf" = engine `specs/specs.pdf` (v1.2.5).

| Topic | repo spec | pdf | Engine 1.2.5 / 1.2.6 (truth) |
|---|---|---|---|
| Map size | 30–60, even sides | 20–60 | 20–60, odd sides allowed |
| Cat HP | 10 000 | 4 000 | 4 000 |
| Cat scratch | 50 dmg, cooldown 15 | 20 dmg, cooldown 15 | 20 dmg, cooldown **30**; range = whole vision cone |
| Pounce cooldown | 20 | 40 | 40 |
| Cat vision | "17 units 180°" and also "cone of 30" | Same | r² 17, 180°, from the 2×2 centre |
| Cat spawn | Map centre | Map centre | Map-defined positions |
| Cat AI | explore / chase / search / attack | explore + 8-turn attack | explore + 8-turn attack, with the quirks in §12/§16 |
| Cat turning | ≤ 90° per turn | Once per turn | Facing is assigned freely by BFS and squeak handling; cooldown-limited turns happen only on rotate/stuck |
| King HP | 500 (cap 500) | 500 start, cap 600 | 600 start, cap 600 |
| King count | ≤ 5 | ≤ 5, plus the round-1200 rule | Same as pdf |
| King formation | "7 allied rats in the 3×3" | Same | 7 **including the centre rat** |
| Rat trap | Cost 30, stun +20 | Cost 20, stun +30 | Cost 20; movement cooldown **set** to 30 |
| Dirt costs | Dig 10, place 10 | Dig 5, place 0 | Dig 5, place 0 |
| Dirt placement | Not on cheese | Not on cheese | Cheese tiles and trap tiles allowed |
| Cat traps after backstab | Never | Backstabbed team ≤ 100 rounds | Same as pdf |
| Remove traps | Any trap | Allied traps | Own traps only; no cooldown, no readiness check |
| Carry / drop cooldown | – | 20 | 20; the 1.5× carry multiplier has no effect (bug) |
| Auto-release tile | In front of the carrier | Same | Carrier tile + **carried rat's** facing |
| Turn order | Cats act last | Spawn order, "cats act last at start" | Spawn order with initial bodies sorted by map ID; cats are often **first** |
| Initial IDs | ≥ 10 000 | Same | Map IDs (small) for the initial kings and cats; spawned robots ≥ 10 000 |
| Points | `round(...)` | `round(...)` | `(int)` truncation |
| Cheese transferred | Baby rats → king | Same | Also counts king pickups |
| King upkeep timing | End of round | Same | End of each king's own turn |
| Cheese spawn area | 9×9 | 9×9 | 8×8 (1.2.5) / 9×9 (1.2.6); falls back to the mine tile; may spawn on dirt; "cat-accessible" not enforced |
| Throw collision damage | `4*remaining tiles` | `4*remaining tiles` | `10 + 4*(2*remaining + firstStep)` to both the flier and the robot hit |
| Squeak recipients | Allied rats + cats | Same, minus self | Same, minus carried and flying rats |

---

## 16. Exploitable / non-obvious mechanics

Paths are relative to the engine repo, `engine/src/main/battlecode/`.

1. **Cat stale-target bug.** Once a cat targets a rat, it keeps scratching, pouncing on and walking toward the location where it *first* saw that rat, for as long as the rat stays anywhere in its cone. A targeted rat can step aside and bait the cat into a fixed spot, such as a ring of cat traps. `world/InternalRobot.java:1326,1351`
2. **Cats are passive in EXPLORE.** Biting a walking cat is safe unless you stand on the tiles it is about to enter (it moves every 2 rounds along a deterministic BFS path), or it reaches a waypoint and enters its 8-turn ATTACK. Cat paths are BFS on walls only, so they can be predicted. `world/InternalRobot.java:1204-1298`, `world/GameWorld.java:223`
3. **Cat target choice is deterministic.** It picks the *last* rat in its chirality scan order (the max-x, then max-y rat for chirality 0). Teams are ignored, so you can make the cat pick an enemy rat. `world/InternalRobot.java:1338-1344`, `world/GameWorld.java:767-790`
4. **Squeak steering.** In ATTACK the cat turns toward the **oldest** queued squeak, which may be up to 4 rounds old and from either team. It senses with its pre-turn facing but checks scratch range with the post-turn facing, so a squeak from behind can make its scratch fail and force a pounce or move instead. `world/InternalRobot.java:1310-1317`
5. **Damage without backstab:** thrown-rat collisions (up to 42 damage to enemy rats or **kings**) and rat-king formation (kills enemy baby rats in the 3×3 and steals their raw cheese) never flip cooperation. `world/InternalRobot.java:785-788`, `world/RobotControllerImpl.java:1231-1256`
6. **The rat-trap owner is blamed for the backstab** even though the enemy walked in. The victim team keeps the right to place cat traps for 100 rounds and is not the "backstabber". `world/GameWorld.java:639-642`, `:420`
7. **Stun is a set, not an add.** Trap stuns overwrite the movement cooldown, which can even lower it, e.g. for a king that just moved (40 → 30). `world/GameWorld.java:633`
8. **Carrying is free.** `(int)1.5 == 1`, so carriers move and act at normal cooldowns. `world/InternalRobot.java:481,500,515`
9. **Raw-cheese penalty truncation:** ≤ 9 raw cheese costs nothing on forward moves and actions; ≤ 5 costs nothing on strafes. `world/InternalRobot.java:483-505`
10. **Free actions:** `pickUpCheese` (any number of tiles per turn, even while carried), `removeRatTrap`/`removeCatTrap`, `squeak`, sensing and `becomeRatKing` add no cooldown. `world/RobotControllerImpl.java:461-526,325-447,1227`
11. **Immediate king actions:** `becomeRatKing` adds no action cooldown, so the new king can `buildRat` in the same turn. `world/RobotControllerImpl.java:1227-1279`
12. **Grab rule:** any enemy baby rat that is *facing away* (you are outside its 90° cone, i.e. 5 of its 8 neighbours) can be grabbed regardless of HP. You need it in your own 3-tile front cone. Grabbing an enemy backstabs. `world/RobotControllerImpl.java:1521-1536`
13. **Auto-release uses the carried rat's own facing.** If that tile is blocked, the carrier and the carried rat swap roles, which lets an enemy you hold capture your rat. `world/InternalRobot.java:1122-1134`
14. **Throw distance depends on turn order:** 8 tiles if the thrown rat acts before the thrower, 6 if after. `world/InternalRobot.java:1144-1159`
15. **Feeding** any rat (even your own) into a cat freezes that cat for 2 of its turns, cooldowns included. `world/InternalRobot.java:825-832,1164,1192`
16. **Cheese fallback:** if a spawn offset hits a wall or goes off the map on either side, the cheese spawns on the mine tiles themselves. Mines near walls or edges therefore often drop cheese on the mine tile. `world/GameWorld.java:517-539`
17. **Cheese on dirt** can be picked up: pickup has no passability check. You can also bury cheese or traps under your own dirt, since placement does not check for either. `world/RobotControllerImpl.java:253-274,461-477`
18. **Pounce cannot overlap the cat's own tiles** (its own body counts as a "cat" obstacle), cannot land on a king or dirt, and needs all 4 landing tiles passable. Dirt or kings around rats deny pounces. **The path is never checked**: a cat pounces over walls and dirt, e.g. across a dividing wall (seen on `t_minus_five_minutes`, docs/theory/t3-scoring.md). `world/InternalRobot.java:882-939`
19. **King count edge:** the round-1200 rule only blocks *new* kings when you have ≥ 2, so 3–5 kings formed before round 1201 all survive and score. `world/RobotControllerImpl.java:1168-1179`
20. **Scoring:** kings picking up cheese directly counts as "transferred". A king standing on a mine's spawn box earns score with no baby-rat trips. `world/RobotControllerImpl.java:498-501,522-525`
21. **Losing the last king to your own bug loses the game:** an uncaught exception or a return from `run()` destroys the unit. `instrumenter/SandboxedRobotPlayer.java:200-236`, `world/GameWorld.java:356-359`
22. **Turn order:** on most official maps the cats come before both kings, and therefore before every rat, in execution order. A cat's AI (at the end of its turn) then always reacts to rat positions from the *previous* round, and rats always move after the cat has committed. Spawned units act after all older units. `world/LiveMap.java:116`, `world/ObjectInfo.java:98`
23. **Sensing blind spots:** carried and flying rats are invisible to `senseNearbyRobots`. Check `RobotInfo.getCarryingRobot()` and `MapInfo.flyingRobot()`. A flying rat makes its tile impassable for everyone. `world/GameWorld.java:478`, `world/RobotControllerImpl.java:74-84,561-564`
24. **Non-determinism:** the final tiebreak uses `Math.random()`, and the cat's random turns use a static RNG that carries over between games run in the same JVM. Identical replays across a multi-map run are not guaranteed. `world/GameWorld.java:958`, `world/InternalRobot.java:59`

---

## 17. GameConstants quick table (1.2.5 = 1.2.6)

| Constant | Value | Constant | Value |
|---|---|---|---|
| `GAME_MAX_NUMBER_OF_ROUNDS` | 2000 | `INITIAL_TEAM_CHEESE` | 2500 |
| `MAP_MIN_WIDTH/HEIGHT` | 20 | `MAP_MAX_WIDTH/HEIGHT` | 60 |
| `MAX_WALL_PERCENTAGE` | 20 | `MAX_DIRT_PERCENTAGE` | 50 |
| `MIN_CHEESE_MINE_SPACING_SQUARED` | 25 | `SQ_CHEESE_SPAWN_RADIUS` | 4 |
| `CHEESE_MINE_SPAWN_PROBABILITY` | 0.01 | `CHEESE_SPAWN_AMOUNT` | 20 |
| `COOLDOWN_LIMIT` | 10 | `COOLDOWNS_PER_TURN` | 10 |
| `TURNING_COOLDOWN` | 10 | `MOVE_STRAFE_COOLDOWN` | 18 |
| `CHEESE_COOLDOWN_PENALTY` | 0.01 | `CARRY_COOLDOWN_MULTIPLIER` | 1.5 (no effect) |
| `BUILD_ROBOT_BASE_COST` | 10 | `BUILD_ROBOT_COST_INCREASE` | 10 |
| `NUM_ROBOTS_FOR_COST_INCREASE` | 4 | `BUILD_ROBOT_COOLDOWN` | 10 |
| `BUILD_ROBOT_RADIUS_SQUARED` | 8 | `BUILD_DISTANCE_SQUARED` | 2 |
| `RAT_KING_BUILD_DISTANCE_SQUARED` | 8 | `CAT_BUILD_DISTANCE_SQUARED` | 4.5 |
| `ATTACK_DISTANCE_SQUARED` | 2 | `RAT_KING_ATTACK_DISTANCE_SQUARED` | 8 |
| `RAT_BITE_DAMAGE` | 10 | `CAT_SCRATCH_DAMAGE` | 20 |
| `CAT_POUNCE_MAX_DISTANCE_SQUARED` | 13 | `CAT_DIG_ADDITIONAL_COOLDOWN` | 5 |
| `CAT_SLEEP_TIME` | 2 | `CAT_TRAP_ROUNDS_AFTER_BACKSTAB` | 100 |
| `RAT_KING_UPGRADE_CHEESE_COST` | 50 | `MAX_NUMBER_OF_RAT_KINGS` | 5 |
| `MAX_NUMBER_OF_RAT_KINGS_AFTER_CUTOFF` | 2 | `RAT_KING_CUTOFF_ROUND` | 1200 |
| `RAT_KING_CHEESE_CONSUMPTION` | 2 | `RAT_KING_HEALTH_LOSS` | 10 |
| `NUMBER_INITIAL_RAT_KINGS` | 1 | `DIG_COOLDOWN` | 25 |
| `DIG_DIRT_CHEESE_COST` | 5 | `PLACE_DIRT_CHEESE_COST` | 0 |
| `CHEESE_PICK_UP_RADIUS_SQUARED` | 2 | `CHEESE_TRANSFER_RADIUS_SQUARED` | 9 |
| `CHEESE_TRANSFER_COOLDOWN` | 10 | `HEALTH_GRAB_THRESHOLD` | 0 |
| `MAX_CARRY_DURATION` | 10 | `SAME_ROBOT_CARRY_COOLDOWN_TURNS` | 2 |
| `THROW_DURATION` | 4 | `TILES_FLOWN_PER_TURN` | 2 |
| `THROW_RAT_COOLDOWN` | 20 | `THROW_DAMAGE` | 10 |
| `THROW_DAMAGE_PER_TILE` | 4 | `HIT_GROUND_COOLDOWN` | 10 |
| `HIT_TARGET_COOLDOWN` | 20 | `MAX_CARRY_TOWER_HEIGHT` | 2 (unused) |
| `SHARED_ARRAY_SIZE` | 64 | `COMM_ARRAY_MAX_VALUE` | 1023 |
| `SQUEAK_RADIUS_SQUARED` | 16 | `MESSAGE_ROUND_DURATION` | 5 |
| `MAX_MESSAGES_SENT_ROBOT` | 1 | `EXCEPTION_BYTECODE_PENALTY` | 500 |
| `MAX_TEAM_EXECUTION_TIME` | 1.2e12 ns | `INDICATOR_STRING_MAX_LENGTH` | 256 |

`TrapType`: `RAT_TRAP(cost 20, dmg 50, stun 30, cd 15, max 25, r² 2)`, `CAT_TRAP(10, 100, 20, 10, 10, 2)`.
`UnitType`: `BABY_RAT(100 hp, size 1, r² 20, 90°, act 10, move 10, 17500)`, `RAT_KING(600, 3, 25, 360°, 10, 40, 20000)`, `CAT(4000, 2, 17, 180°, 30, 20, 17500)`.
