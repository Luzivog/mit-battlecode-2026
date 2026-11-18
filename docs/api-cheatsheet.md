# Battlecode 2026 API cheatsheet (engine 1.2.5 / 1.2.6)

This is the companion to [`rules.md`](rules.md), which explains the mechanics. Here, **Cost** is the fixed bytecode charge from `engine/src/main/battlecode/instrumenter/bytecode/resources/MethodCosts.txt`. A method not listed there costs only the invoke itself, about 1–3 bytecodes. Methods that throw raise `GameActionException`; each `canX` returns false instead. Catching any exception costs +500 bytecodes. The methods are identical in 1.2.5 and 1.2.6.

Abbreviations:
- **in-range**: inside the robot's vision cone **and** within the action radius from rules.md §7.
- **A-ready / M-ready / T-ready**: the action / movement / turning cooldown is < 10 and the robot is not carried or thrown.

## RobotController: state queries

| Method | Cost | Notes |
|---|---|---|
| `getRoundNum()` | 1 | 1..2000 |
| `getMapWidth()` / `getMapHeight()` | 1 | – |
| `isCooperation()` | 1 | false after the first backstab |
| `getBackstabbingTeam()` | 0* | `null` while cooperating |
| `getNumberRatTraps()` / `getNumberCatTraps()` | 0* | Your team's active traps (limits 25 / 10) |
| `getID()`, `getTeam()`, `getType()`, `getHealth()`, `getDirection()` | 1 | `getType()` changes to `RAT_KING` right after `becomeRatKing()` |
| `getLocation()` | 1 | King: centre. Carried rat: the carrier's tile. Flying rat: its current air tile. |
| `getAllPartLocations()` | 5 | Allocates. Ordered by chirality. |
| `getRawCheese()` / `getGlobalCheese()` / `getAllCheese()` | 1 | Kings always have raw = 0 |
| `getDirt()` | 1 | Team dirt stash |
| `getCarrying()` | 1 | `RobotInfo` of the rat you carry, or null |
| `isBeingCarried()` / `isBeingThrown()` | 1 | – |
| `getCurrentRatCost()` | 1 | `10 + 10*floor(babyRats/4)` |
| `isActionReady()` / `isMovementReady()` / `isTurningReady()` | 1 | Also false while carried or thrown |
| `getActionCooldownTurns()` / `getMovementCooldownTurns()` / `getTurningCooldownTurns()` | 1 | Raw counters |

\* Not in MethodCosts.txt.

## RobotController: sensing

All sensing respects the vision cone (see rules.md §7). Results are ordered by your chirality's scan order: x-major, then y, reversed per symmetry for chirality 1.

| Method | Cost | Notes |
|---|---|---|
| `onTheMap(loc)` | 5 | – |
| `canSenseLocation(loc)` | 5 | Cone + radius; your own tile is always true |
| `isLocationOccupied(loc)` | 5 | Throws if the tile is not sensable. Ground robots only: carried or flying rats do not count. |
| `canSenseRobotAtLocation(loc)` | 5 | Same as above, but returns false instead of throwing |
| `senseRobotAtLocation(loc)` | 15 | `RobotInfo` of whatever occupies the tile (any king/cat part returns that robot), or null |
| `canSenseRobot(id)` / `senseRobot(id)` | 5 / 25 | Works for carried rats (at the carrier's tile); false/throws for flying rats |
| `senseNearbyRobots()`, `(r2)`, `(r2, team)`, `(center, r2, team)` | 100 | `r2 = -1` means full vision. `r2` is capped at vision. Excludes yourself and carried/flying rats. `team = null` means all, including cats (`Team.NEUTRAL`). |
| `sensePassability(loc)` | 5 | False for wall, dirt, or a flying rat |
| `senseMapInfo(loc)` | 5 | – |
| `senseNearbyMapInfos()`, `(r2)`, `(center)`, `(center, r2)` | 100 | Allocates one `MapInfo` per visible tile (up to 81 for a king), which is expensive in real time |
| `adjacentLocation(dir)` | 1 | – |
| `getAllLocationsWithinRadiusSquared(center, r2)` | 100 | Only the tiles you can sense |

## RobotController: movement and turning

| Method | Cost | Preconditions / effects |
|---|---|---|
| `canMove(d)` / `canMoveForward()` | 10 | M-ready; every shifted part is on the map, passable, and unoccupied (cats may enter baby-rat tiles) |
| `move(d)` / `moveForward()` | 1 | Facing is **not** changed. Cost: forward 10 / strafe 18 for baby rats (×(1+0.01·raw), truncated); king 40; cat 20. Enemy traps around the new tiles fire. |
| `canTurn()` / `canTurn(d)` | 1 | T-ready; `d` not null or CENTER |
| `turn(d)` | 1 | +10 turning cooldown (no cheese penalty) |

## RobotController: building and economy

| Method | Cost | Preconditions / effects |
|---|---|---|
| `canBuildRat(loc)` / `buildRat(loc)` | 10 / 20 | King only; A-ready; loc at dist² ≤ 8 from the centre (the 16-tile ring); empty and passable; **global** cheese ≥ cost. The new rat faces the king's direction and first acts next round. +10 action cooldown. |
| `canBecomeRatKing()` / `becomeRatKing()` | 10 / 20 | Baby rat; A-ready; global ≥ 50; king-cap rules; 3×3 on the map and passable; no king or cat in it; ≥ 7 allied baby rats including self. Kills the other 8 occupants (enemies too), with **no backstab**. HP = sum of allies' HP, capped at 600. **No cooldown added.** |
| `canPickUpCheese(loc)` / `pickUpCheese(loc)` / `pickUpCheese(loc, amt)` | 5 / 5 / 5 | Rat; tile within dist² ≤ 2 and sensable; cheese > 0; not flying. **No readiness check, no cooldown, repeatable, works while carried.** A king's pickup goes to global and counts toward the score. |
| `canTransferCheese(loc, amt)` / `transferCheese(loc, amt)` | 5 / 5 | Baby rat → allied king tile within dist² ≤ 9 and in vision; A-ready; `0 < amt ≤ raw`. +10 action cooldown (×cheese penalty on the remaining raw). |
| `canRemoveDirt(loc)` / `removeDirt(loc)` | 5 / 5 | A-ready; in-range (baby rat dist² ≤ 2, king ≤ 8); dirt there; raw + global ≥ 5. Costs 5 cheese (raw first); team dirt +1; +25 action cooldown. |
| `canPlaceDirt(loc)` / `placeDirt(loc)` | 5 / 5 | A-ready; in-range; team dirt ≥ 1; not wall, dirt, mine, or ground robot. **Cheese or trap tiles are allowed.** Costs 0 cheese; +25 action cooldown. |
| `canPlaceRatTrap(loc)` / `placeRatTrap(loc)` | 5 / 5 | A-ready; in-range; passable; empty; no trap of yours on the tile; not a mine; fewer than 25 active; raw + global ≥ 20. +15 action cooldown. |
| `canPlaceCatTrap(loc)` / `placeCatTrap(loc)` | 5 / 5 | As above (max 10, cost 10, +10 cooldown). Only while cooperating, or if you are the **backstabbed** team and ≤ 100 rounds have passed since the backstab. |
| `canRemoveRatTrap(loc)` / `removeRatTrap(loc)`, and the cat-trap versions | 5 | In-range; your own trap of that type. No readiness check, no cooldown, no refund. |

## RobotController: combat and ratnapping

| Method | Cost | Preconditions / effects |
|---|---|---|
| `canAttack(loc)` / `canAttack(loc, cheese)` | 10 | A-ready; loc sensable; dist² in (0, 2] (king: (0, 8]); passable; non-allied robot on the tile; `0 ≤ cheese ≤ raw + global` |
| `attack(loc)` / `attack(loc, cheese)` | 10 | Damage `10 + ceil(sqrt(cheese))`. The cheese is spent raw first. Backstab unless the target is a cat. +10 action cooldown (×cheese penalty, computed before spending). |
| `canCarryRat(loc)` / `carryRat(loc)` | 5 / 10 | Baby rat; A-ready; adjacent and in your cone; not already carrying; target is a baby rat (not flying, not self) that **cannot see you**, or is an ally, or has strictly lower HP; plus the 2-turn re-grab block for enemies you just carried. Enemy target → backstab. +20 action cooldown. |
| `canDropRat(dir)` / `dropRat(dir)` | 10 / 10 | Carrying; A-ready; adjacent tile on the map, passable, and empty. +20 action cooldown. Traps fire on the dropped rat. |
| `canThrowRat()` / `throwRat()` | 10 / 25 | Carrying; A-ready; the tile in **your facing direction** is on the map, passable, and empty. The rat flies 2 tiles per step (6–8 tiles total) and takes 10 damage on landing, or `10+4*(...)` on a collision (rules.md §9). +20 action cooldown. |

## RobotController: communication, debug, lifecycle

| Method | Cost | Notes |
|---|---|---|
| `squeak(int msg)` | 50 | 1 per turn (returns false otherwise). Reaches allies and **cats** with any tile within dist² ≤ 16, instantly. Never reaches enemies or yourself. |
| `readSqueaks(round)` | 10 | `-1` returns all of the last 5 rounds; otherwise only that round. Allocates an array. |
| `writeSharedArray(i, v)` | 50 | Kings only; `0 ≤ i < 64`; `0 ≤ v ≤ 1023` |
| `readSharedArray(i)` | 10 | Anyone. An index out of range throws `ArrayIndexOutOfBoundsException`. |
| `setIndicatorString(s)` / `setIndicatorDot(loc,r,g,b)` / `setIndicatorLine(a,b,r,g,b)` / `setTimelineMarker(label,r,g,b)` | 1 | Strings are truncated to 256 characters, timeline labels to 64 |
| `disintegrate()` | 0 | Kills this robot. Your last king doing so = you lose. |
| `resign()` | 0 | Destroys your whole team; the opponent wins |
| `Clock.yield()` | 0 | Ends the turn |
| `Clock.getBytecodesLeft()` / `getBytecodeNum()` / `getTimeLeft()` / `getTimeElapsed()` | 0 | Time values are in ns of the per-team per-game budget (1200 s) |

## Value classes

| Class | Members (cost) | Notes |
|---|---|---|
| `MapLocation` | `x`, `y` fields (free); `add`, `subtract`, `translate`, `distanceSquaredTo`, `isWithinDistanceSquared`, `isAdjacentTo`, `directionTo`, `equals`, `hashCode`, `compareTo`, `toString` (2 each); `valueOf` (25); `bottomLeftDistanceSquaredTo` (not listed, so ~free) | `directionTo` uses a 2.414 slope test, giving 8 directions or CENTER. `isAdjacentTo` is true for the same tile. |
| `Direction` | `dx`, `dy` fields; `rotateLeft/Right`, `opposite`, `getDeltaX/Y`, `allDirections` (**includes CENTER**, 9 values), `cardinalDirections`, `fromDelta`, `getDirectionOrderNum` (1 each) | Ordinals: N=0, NE, E, SE, S, SW, W, NW=7, CENTER=8 |
| `RobotInfo` | Fields `ID, team, type, health, location, direction, chirality, cheeseAmount, carryingRobot` (field access is cheapest). Getters: `getID/getTeam/getType/getChirality` (1), `getHealth/getLocation/getDirection/getRawCheeseAmount` (2), `getCarryingRobot` (not listed); `equals/hashCode/toString` (15) | `location` is the king's centre or the cat's bottom-left tile. `cheeseAmount` is the raw cheese carried. |
| `MapInfo` | `getMapLocation` (1); `isPassable`, `isWall`, `isDirt`, `hasCheeseMine`, `getTrap`, `getCheeseAmount` (2); `flyingRobot()` (not listed) | `getTrap()` shows **your own** trap only (`TrapType.NONE` otherwise). `isPassable` is false under a flying rat. |
| `Message` | `getBytes`, `getSenderID`, `getRound`, `getSource` (3) | `getSource` is the sender's location when it squeaked |
| `UnitType` | `BABY_RAT`, `RAT_KING`, `CAT`; fields `health, size, visionConeRadiusSquared, visionConeAngle, actionCooldown, movementCooldown, bytecodeLimit`; getters and `is*Type()` (3) | – |
| `TrapType` | `RAT_TRAP`, `CAT_TRAP`, `NONE`; fields `buildCost, damage, stunTime, actionCooldown, maxCount, triggerRadiusSquared` | – |
| `Team` | `A`, `B`, `NEUTRAL` (cats); `opponent()` (`NEUTRAL.opponent()` is `NEUTRAL`), `isPlayer()` (1) | – |
| `Math` | `abs/min/max/sqrt/floor/ceil/pow/atan2` and the like (1) | `Math.random` is counted as your own code |

`GameActionExceptionType`: `INTERNAL_ERROR, NOT_ENOUGH_RESOURCE, CANT_MOVE_THERE, IS_NOT_READY, CANT_SENSE_THAT, OUT_OF_RANGE, CANT_DO_THAT, NO_ROBOT_THERE, ROUND_OUT_OF_RANGE`.

Not available to players: `RobotControllerImpl.read/writePersistentArray` exist but are not in the `RobotController` interface, and their backing array is never initialised.
