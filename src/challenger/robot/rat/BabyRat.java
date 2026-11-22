package challenger.robot.rat;

import battlecode.common.*;
import challenger.CellTracker;
import challenger.Pathfinder;
import challenger.Tune;
import challenger.RobotTracker;
import challenger.comms.GlobalArray;
import challenger.robot.LastCat;
import challenger.robot.Strategy;
import challenger.robot.rat.combat.CombatStrategy;
import challenger.robot.rat.goHome.AntColony;
import challenger.util.Rng;
import static challenger.Game.*;
import static challenger.util.Util.*;
import challenger.util.Profiler;
import challenger.comms.EnemyManager;
import challenger.comms.Enemy;
import challenger.comms.Squeaker;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;
    public static CombatStrategy combatStrategy;

    public static boolean transferSwitch = false;

    public static void init() throws GameActionException {
        int payload = GlobalArray.loadSpawnInfo();
        // println("BabyRat payload: " + payload + " id " + rc.getID() + " index " + myIndex);
        primaryStrategy = new MinerStrategy();
        combatStrategy = new CombatStrategy();
        Pathfinder.init();
        AntColony.init();
    }

    private static boolean hasOwnCatTrapNear(MapLocation targ) throws GameActionException {
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                MapLocation check = targ.translate(dx, dy);
                if (rc.canSenseLocation(check) && rc.senseMapInfo(check).getTrap() == TrapType.CAT_TRAP) {
                    return true;
                }
            }
        }
        return false;
    }

    private static int chebTo2x2(MapLocation loc, int bx, int by) {
        int dx = loc.x < bx ? bx - loc.x : (loc.x > bx + 1 ? loc.x - (bx + 1) : 0);
        int dy = loc.y < by ? by - loc.y : (loc.y > by + 1 ? loc.y - (by + 1) : 0);
        return Math.max(dx, dy);
    }

    private static boolean tryPlaceBestCatTrap(RobotInfo cat) throws GameActionException {
        if (!rc.isActionReady()) return false;
        int cx = cat.location.x, cy = cat.location.y;
        int fx = cat.direction.dx, fy = cat.direction.dy;
        int nx = cx + fx, ny = cy + fy;
        int nx2 = cx + 2 * fx, ny2 = cy + 2 * fy;
        int nextCenterX2 = 2 * nx + 1, nextCenterY2 = 2 * ny + 1;

        MapLocation bestTarg = null;
        int bestScore = Integer.MIN_VALUE;
        for (Direction off : relatedDirections(myDir)) {
            MapLocation targ = myLoc.add(off);
            if (!rc.canPlaceCatTrap(targ)) continue;
            int c1 = chebTo2x2(targ, nx, ny);
            int c2 = chebTo2x2(targ, nx2, ny2);
            if (c1 > 1 && c2 > 1) continue;
            if (hasOwnCatTrapNear(targ)) continue;
            int dx2 = 2 * targ.x - nextCenterX2;
            int dy2 = 2 * targ.y - nextCenterY2;
            int score = (c1 == 0 ? 300 : c1 == 1 ? 200 : c2 == 0 ? 120 : 80) - (dx2 * dx2 + dy2 * dy2);
            if (score > bestScore) {
                bestScore = score;
                bestTarg = targ;
            }
        }
        if (bestTarg != null && rc.canPlaceCatTrap(bestTarg)) {
            rc.placeCatTrap(bestTarg);
            return true;
        }
        return false;
    }

    public static void considerPlacingCatTrap() throws GameActionException {
        if (LastCat.isHoldEnabled() && RobotTracker.numCats > 0) LastCat.defuseTraps();
        if (!rc.isActionReady()) return;
        if (RobotTracker.numCats > 0 && rc.isCooperation() && RobotTracker.numEnemies == 0 && EnemyManager.numKnownEnemies == 0) {
            if ((Tune.COOP_CTRL & 1) != 0) {
                if (rc.getAllCheese() < 200 || rc.getNumberCatTraps() >= TrapType.CAT_TRAP.maxCount) return;
                RobotInfo cat = null;
                for (int i = 0; i < RobotTracker.numCats; i++) {
                    RobotInfo cand = RobotTracker.sortedCats[i];
                    if (cand == null) continue;
                    if (dist(myLoc, cand) > 13) break;
                    if (LastCat.isHoldEnabled() && !LastCat.mayDamage(cand, TrapType.CAT_TRAP.damage)) continue;
                    cat = cand;
                    break;
                }
                if (cat == null) return;

                boolean placed = tryPlaceBestCatTrap(cat);
                int cx = cat.location.x, cy = cat.location.y;
                int fx = cat.direction.dx, fy = cat.direction.dy;
                if (!placed && rc.canTurn()) {
                    MapLocation aim = cat.location.translate(fx, fy);
                    Direction turnDir = myLoc.directionTo(aim);
                    if (turnDir == Direction.CENTER) turnDir = myLoc.directionTo(cat.location);
                    if (turnDir != Direction.CENTER && turnDir != myDir) {
                        turn(turnDir);
                        if (rc.isActionReady() && (!LastCat.isHoldEnabled() || LastCat.mayDamage(cat, TrapType.CAT_TRAP.damage))) {
                            placed = tryPlaceBestCatTrap(cat);
                        }
                    }
                }
                // If standing in the cat's sight cone or next-step crush footprint, step to safety.
                boolean inDanger = canCatSense(myLocX - cx, myLocY - cy, cat.direction)
                    || chebTo2x2(myLoc, cx + fx, cy + fy) == 0;
                if (inDanger && rc.isMovementReady()) {
                    Direction bestMove = null;
                    int bestMoveScore = Integer.MIN_VALUE;
                    for (Direction moveDir : directions) {
                        if (!rc.canMove(moveDir)) continue;
                        MapLocation nxt = myLoc.add(moveDir);
                        boolean nxtSeen = canCatSense(nxt.x - cx, nxt.y - cy, cat.direction);
                        boolean nxtCrush = chebTo2x2(nxt, cx + fx, cy + fy) == 0;
                        int score = (nxtSeen ? 0 : 200) + (nxtCrush ? -300 : 0) + dist(nxt, cat) * 4;
                        if (moveDir == myDir) score += 5;
                        if (score > bestMoveScore) {
                            bestMoveScore = score;
                            bestMove = moveDir;
                        }
                    }
                    if (bestMove != null) {
                        rawMove(bestMove);
                        if (!placed && rc.isActionReady() && (!LastCat.isHoldEnabled() || LastCat.mayDamage(cat, TrapType.CAT_TRAP.damage))) {
                            tryPlaceBestCatTrap(cat);
                        }
                    }
                }
                return;
            }

            RobotInfo cat = RobotTracker.sortedCats[0];
            if (LastCat.isHoldEnabled() && !LastCat.mayDamage(cat, TrapType.CAT_TRAP.damage)) return;
            if (cat.location.isWithinDistanceSquared(myLoc, 9, cat.direction, 90, true)) {
                for (Direction off : relatedDirections(myLoc.directionTo(cat.location))) {
                    MapLocation targ = myLoc.add(off);
                    MapLocation bruh = targ.add(targ.directionTo(cat.location));
                    if (rc.canSenseLocation(bruh)) {
                        RobotInfo r = rc.senseRobotAtLocation(bruh);
                        if (r != null && r.location.equals(cat.location) && rc.canPlaceCatTrap(targ)) {
                            rc.placeCatTrap(targ);
                        }
                    }
                }
                if (rc.canTurn()) {
                    for (Direction dir : directions) {
                        if (dir == myDir) continue;
                        if (canSense(myLoc, cat.location, dir)) {
                            turn(dir);
                            break;
                        }
                    }
                }
                for (Direction _moveDir : relatedDirections(myLoc.directionTo(cat.location))) {
                    Direction moveDir = _moveDir.opposite();
                    if (rc.canMove(moveDir)) {
                        rawMove(moveDir);
                        break;
                    }
                }
                if (cat.location.isWithinDistanceSquared(myLoc, 9, cat.direction, 90, true)) {
                    for (Direction off : relatedDirections(myLoc.directionTo(cat.location))) {
                        MapLocation targ = myLoc.add(off);
                        MapLocation bruh = targ.add(targ.directionTo(cat.location));
                        if (rc.canSenseLocation(bruh)) {
                            RobotInfo r = rc.senseRobotAtLocation(bruh);
                            if (r != null && r.location.equals(cat.location) && rc.canPlaceCatTrap(targ)) {
                                rc.placeCatTrap(targ);
                            }
                        }
                    }
                }
            }
        }
    }

    public static void run() throws GameActionException {
        isLastMove = false;
        // indicate("cheesemines: " + GlobalArray.cheeseMines.set.length());
        shouldGlance = true;

        CellTracker.storeVisibleCells();
        AntColony.preTick();

        tryPickUpCheese();
        tryTransferCheese();

        if (rc.isBeingCarried()) {
            if (rc.getHealth() < challenger.Tune.DISINTEGRATE_HP && rc.senseRobotAtLocation(myLoc).getTeam() != team) {
                rc.disintegrate();
                Clock.yield();
            }
            if (challenger.Tune.RX_RESCUE != 0) {
                RobotInfo carrier = rc.senseRobotAtLocation(myLoc);
                if (carrier != null && carrier.getTeam() != team) {
                    Squeaker.squeakEnemy(EnemyManager.encodeEnemy(new Enemy(carrier)) | (EnemyManager.NULL << EnemyManager.ENC_WIDTH));
                }
            }
            return;
        }

        if (rc.isBeingThrown()) {
            return;
        }

        // indicate("before combat strat: " + Clock.getBytecodeNum());
        if (combatStrategy.runTick()) {
            considerPlacingCatTrap();
            return;
        }

        // all nonessential systems go here, only run if we are not in combat
        RobotTracker.reset();
        CellTracker.checkSymmetry();
        RobotTracker.updateHistoricalCatLocs();

        // indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);

        considerPlacingCatTrap();
        primaryStrategy.runTick();
        combatStrategy.miniTick();

        isLastMove = true;
        executeGlance();
    }

    private static void executeGlance() throws GameActionException {
        if (!rc.canTurn() || !shouldGlance) return;

        // indicate("glancing");
        Direction myLeftDir = myDir.rotateLeft().rotateLeft();
        Direction myRightDir = myDir.rotateRight().rotateRight();

        if (lastGlance == myLeftDir) {
            turn(myRightDir);
            lastGlance = myRightDir;
        } else if (lastGlance == myRightDir || Rng.mcg() >> 30 != 0) {
            turn(myLeftDir);
            lastGlance = myLeftDir;
        } else {
            turn(myRightDir);
            lastGlance = myRightDir;
        }
    }

    // IF TLE: unroll
    public static void tryPickUpCheese() throws GameActionException {
        if(rc.canPickUpCheese(myLoc)) {
            rc.pickUpCheese(myLoc);
        }
        var dirs = relatedDirections(myDir);
        for(var dir : dirs) {
            var loc = myLoc.add(dir);
            if(rc.canPickUpCheese(loc)) {
                rc.pickUpCheese(loc);
            }
        }
    }

    // IF TLE: spam this less / optimise
    private static void tryTransferCheese() throws GameActionException {
        int myCheese = rc.getRawCheese();
        if (myCheese <= 0) return;
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(kingLoc != null && kingLoc.distanceSquaredTo(myLoc) <= 18) {
                AntColony.reachedHome();
                var dir = kingLoc.directionTo(myLoc).rotateLeft();
                for(int j = 0; j < 3; j++) {
                    var loc = kingLoc.add(dir);
                    if(rc.canTransferCheese(loc, myCheese)) {
                        rc.transferCheese(loc, myCheese);
                        transferSwitch = true;
                        return;
                    }
                    dir = dir.rotateRight();
                }
            }
        }
    }

    public static boolean isLastMove;
    public static void rawMove(Direction dir) throws GameActionException {
        if(dir == myDir) {
            rawMove();
            return;
        }
        _rawMove(dir);
        tryPickUpCheese();
        tryTransferCheese();
        AntColony.markCurrent();
        // IF TLE: optimise this
        if (!isLastMove) {
            RobotTracker.updateRobots();
            if(Clock.getBytecodesLeft() >= 3000) CellTracker.storeVisibleCells();
        }
    }

    public static void turn(Direction dir) throws GameActionException {
        if (dir == myDir) return;
        _turn(dir);
        tryPickUpCheese();
        tryTransferCheese();
        if (!isLastMove) {
            RobotTracker.updateRobots();
            if(Clock.getBytecodesLeft() >= 3000) CellTracker.storeVisibleCells();
        }
    }

    public static void rawMove() throws GameActionException {
        _rawMove(myDir);
        tryPickUpCheese();
        tryTransferCheese();
        AntColony.markCurrent();
        if (!isLastMove) {
            RobotTracker.updateRobots();
            if(Clock.getBytecodesLeft() >= 2000) CellTracker.storeVisibleCellsPostMoveForward();
        }
    }

    public static void autoMove(Direction dir) throws GameActionException {
        if(dir == Direction.CENTER) return;
        if(rc.canTurn()) turn(dir);
        var nextLoc = myLoc.add(dir);
        if(rc.onTheMap(nextLoc) && canSense(myLoc, nextLoc, myDir) && rc.senseMapInfo(nextLoc).isDirt()) {
            if(rc.canRemoveDirt(nextLoc)) rc.removeDirt(nextLoc);
            else return;
        }
        if(dir == myDir) {
            // bail out if we see that we will walk into a cat
            for(int i = RobotTracker.numCats - 1; i >= 0; i--) {
                var cat = RobotTracker.sortedCats[i];
                if(cat.location.isWithinDistanceSquared(nextLoc, 5, cat.direction, 180, true)) {
                    // indicate("avoiding cat at " + nextLoc);
                    return;
                }
            }
            if(rc.canMoveForward()) rawMove();
        } else {
            // if we can't turn, then strafe
            if(rc.canMove(dir)) rawMove(dir);
        }
    }

    public static void attack(RobotInfo info) throws GameActionException {
        attack(info, CombatStrategy.getBiteDamage());
    }

    public static void attack(RobotInfo info, int damage) throws GameActionException {
        if (info.getType() != UnitType.BABY_RAT) {
            damage = 10;
        }
        rc.attack(myLoc.add(myLoc.directionTo(info.location)), CombatStrategy.getCheeseNeeded(damage - 10));
    }
}
