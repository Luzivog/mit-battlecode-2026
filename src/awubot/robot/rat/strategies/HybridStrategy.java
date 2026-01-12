package awubot.robot.rat.strategies;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.comms.GlobalArray;
import awubot.robot.Strategy;
import awubot.robot.rat.BabyRat;
import awubot.robot.rat.BabyRatRole;
import awubot.util.Util;

import static awubot.Game.*;
import static awubot.util.Util.indicate;

public class HybridStrategy extends Strategy {
    private static final int CAT_MINER_DANGER_DIST_SQ = 36;
    private static final int CAT_RUSHER_DANGER_DIST_SQ = 4;

    private final GoHomeStrategy goHomeStrategy;
    private final CheeseStrategy cheeseStrategy;
    private final MineStrategy mineStrategy;
    private final ExploreStrategy exploreStrategy;

    public HybridStrategy() {
        this.cheeseStrategy = new CheeseStrategy();
        this.goHomeStrategy = new GoHomeStrategy(cheeseStrategy);
        this.mineStrategy = new MineStrategy(cheeseStrategy);
        this.exploreStrategy = new ExploreStrategy();
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        indicate("role: " + BabyRat.role + " cheese: " + rc.getRawCheese());

        if (rc.getRawCheese() == 0) {
            goHomeStrategy.setGoingHome(false);
        }

        if (tryOpportunisticTransfer()) {
            indicate("transferred!");
        }

        boolean carryingCheese = rc.getRawCheese() > 0;
        if (!carryingCheese && BabyRat.role != BabyRatRole.DECOY) {
            MapLocation catLoc = findNearbyCat();
            if (catLoc != null) {
                int dangerDist = (BabyRat.role == BabyRatRole.RUSHER)
                    ? CAT_RUSHER_DANGER_DIST_SQ
                    : CAT_MINER_DANGER_DIST_SQ;
                if (loc.distanceSquaredTo(catLoc) <= dangerDist) {
                    indicate("FLEEING from cat!");
                    fleeFromCat(catLoc);
                    return;
                }
            }
        }

        if (rc.getCarrying() != null) {
            Runnable throwAction = tryThrow();
            if (throwAction != null) {
                throwAction.run();
                return;
            }
            exploreStrategy.runTick();
            return;
        }

        if (BabyRat.role == BabyRatRole.RUSHER) {
            runRusherTick();
        } else {
            runMinerTick();
        }
    }

    private void runMinerTick() throws GameActionException {
        if (goHomeStrategy.tryStrategy(goHomeStrategy)) return;

        Runnable ratnap = tryRatnapManeuver();
        if (ratnap != null) {
            ratnap.run();
            return;
        }

        Runnable attack = tryAttack();
        if (attack != null) {
            attack.run();
            return;
        }

        if (cheeseStrategy.tryStrategy(cheeseStrategy)) return;
        if (mineStrategy.tryStrategy(mineStrategy)) return;

        runRushBehavior();
    }

    private void runRushBehavior() throws GameActionException {
        MapLocation enemyTarget = Util.getOpposite(home);
        Pathfinder.makeMove(enemyTarget);
    }

    private void runRusherTick() throws GameActionException {
        Runnable ratnap = tryRatnapManeuver();
        if (ratnap != null) {
            ratnap.run();
            return;
        }

        Runnable attack = tryAttack();
        if (attack != null) {
            attack.run();
            return;
        }

        if (goHomeStrategy.tryStrategy(goHomeStrategy)) return;
        if (cheeseStrategy.tryStrategy(cheeseStrategy)) return;
        if (mineStrategy.tryStrategy(mineStrategy)) return;

        exploreStrategy.runTick();
    }

    private boolean tryOpportunisticTransfer() throws GameActionException {
        int cheese = rc.getRawCheese();
        if (cheese <= 0) return false;

        MapLocation kingLoc = findNearestKing();
        if (kingLoc == null) return false;

        if (Util.chebyshevDistance(loc, kingLoc) > 2) return false;

        Direction toKing = loc.directionTo(kingLoc);
        MapLocation transferTarget = loc.add(toKing);

        if (rc.canTransferCheese(transferTarget, cheese)) {
            rc.transferCheese(transferTarget, cheese);
            return true;
        }
        return false;
    }

    private static MapLocation findNearestKing() throws GameActionException {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.RAT_KING && robot.getTeam() == rc.getTeam()) {
                return robot.getLocation();
            }
        }

        MapLocation bestKing = null;
        int bestDist = Integer.MAX_VALUE;
        for (int i = 0; i < 5; i++) {
            MapLocation kingLoc = GlobalArray.getRat(i);
            if (kingLoc != null && rc.onTheMap(kingLoc)) {
                int dist = loc.distanceSquaredTo(kingLoc);
                if (dist < bestDist) {
                    bestDist = dist;
                    bestKing = kingLoc;
                }
            }
        }
        return bestKing;
    }

    private static Runnable tryAttack() {
        if (rc.getActionCooldownTurns() >= 10 || rc.getMovementCooldownTurns() >= 18 || rc.getTurningCooldownTurns() >= 10) return null;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            if (info.getHealth() - GameConstants.RAT_BITE_DAMAGE >= rc.getHealth()) continue;
            MapLocation target = info.getLocation();
            for (Direction dir : Direction.values()) {
                if (dir == Direction.CENTER || rc.canMove(dir)) {
                    MapLocation newLoc = loc.add(dir);
                    if (newLoc.distanceSquaredTo(target) <= 2) {
                        return () -> {
                            try {
                                rawMove(dir);
                                turn(newLoc.directionTo(target));
                                rc.attack(target);
                                loc = rc.getLocation();
                                locX = loc.x;
                                locY = loc.y;
                            } catch (GameActionException e) {
                                e.printStackTrace();
                            }
                        };
                    }
                }
            }
        }
        return null;
    }

    private static Runnable tryRatnapManeuver() {
        if (rc.getCarrying() != null || rc.getActionCooldownTurns() >= 10 || rc.getMovementCooldownTurns() >= 18 || rc.getTurningCooldownTurns() >= 10) return null;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            if (info.getHealth() >= rc.getHealth()) continue;
            MapLocation target = info.getLocation();
            for (Direction dir : Direction.values()) {
                if (dir == Direction.CENTER || rc.canMove(dir)) {
                    MapLocation newLoc = loc.add(dir);
                    if (newLoc.distanceSquaredTo(target) <= 2) {
                        return () -> {
                            try {
                                rawMove(dir);
                                turn(newLoc.directionTo(target));
                                rc.carryRat(target);
                                loc = rc.getLocation();
                                locX = loc.x;
                                locY = loc.y;
                            } catch (GameActionException e) {
                                e.printStackTrace();
                            }
                        };
                    }
                }
            }
        }
        return null;
    }

    private static Runnable tryThrow() throws GameActionException {
        if (rc.getCarrying() == null || rc.getTurningCooldownTurns() >= 10) return null;
        Direction best = null;
        int dist = 67;
        for (Direction dir : Direction.values()) {
            MapLocation pos = loc;
            for (int i = 0; i < 8; i++) {
                pos = pos.add(dir);
                if (!rc.canSenseLocation(pos)) {
                    pos = null;
                    break;
                }
                if (rc.senseRobotAtLocation(pos) != null) break;
                if (!rc.senseMapInfo(pos).isPassable()) break;
            }
            if (pos == null) continue;
            int d = Util.chebyshevDistance(loc, pos);
            if (d <= 1 || d == 8) continue;
            if (d < dist) {
                best = dir;
                dist = d;
            }
        }
        if (best == null) return null;
        final Direction myBest = best;
        return () -> {
            try {
                turn(myBest);
                rc.throwRat();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
        };
    }

    private static MapLocation findNearbyCat() {
        for (RobotInfo robot : rc.senseNearbyRobots()) {
            if (robot.getType() == UnitType.CAT) {
                return robot.getLocation();
            }
        }
        return null;
    }

    private void fleeFromCat(MapLocation catLoc) throws GameActionException {
        Direction awayFromCat = catLoc.directionTo(loc);
        Pathfinder.makeMove(loc.add(awayFromCat));
    }
}
