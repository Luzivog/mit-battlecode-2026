package awubot.robot.rat.strategies;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import awubot.util.Util;

import static awubot.Game.*;
import static awubot.util.Util.directions;
import static awubot.util.Util.println;

public class ExperimentStrategy extends Strategy {

    private ExploreStrategy exploreStrategy;
    public ExperimentStrategy() {
        this.exploreStrategy = new ExploreStrategy();
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }


    private static int id = -1;
    @Override
    public void runTick() throws GameActionException {
        if (id != -1) {
            println("threw guy with id " + id);
        }
        for (RobotInfo ri : rc.senseNearbyRobots()) {
            if (ri.ID == id) {
                println("found thrown guy at " + myLoc.distanceSquaredTo(ri.getLocation()));
            }
        }
        if (rc.getCarrying() != null) {
            Runnable chuck = tryThrow();
            if (chuck != null) {
                chuck.run();
                return;
            }
            exploreStrategy.runTick();
            return;
        }
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
        exploreStrategy.runTick();
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
                    MapLocation newLoc = myLoc.add(dir);
                    if (newLoc.distanceSquaredTo(target) <= 2) {
                        return () -> {
                            try {
                                println("ATTEMPTING ATTACK");
                                rawMove(dir);
                                turn(newLoc.directionTo(target));
                                rc.attack(target);
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
                    MapLocation newLoc = myLoc.add(dir);
                    if (newLoc.distanceSquaredTo(target) <= 2) {
                        return () -> {
                            try {
                                println("ATTEMPTING RATNAP");
                                rawMove(dir);
                                turn(newLoc.directionTo(target));
                                rc.carryRat(target);
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
            MapLocation pos = myLoc;
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
            int d = Util.chebyshevDistance(myLoc, pos);
            if (d <= 1 || d == 8) continue;
            if (d < dist) {
                best = dir;
                dist = d;
            }
        }
        if (best == null) return null;
        final Direction _best = best;
        return () -> {
            try {
                println("THROWING RAT " + _best);
                {
                    MapLocation pos = myLoc;
                    for (int i = 0; i < 8; i++) {
                        pos = pos.add(_best);
                        if (!rc.canSenseLocation(pos)) break;
                        if (!rc.senseMapInfo(pos).isPassable()) break;
                        if (rc.senseRobotAtLocation(pos) != null) break;
                    }
                }
                turn(_best);
                id = rc.getCarrying().ID;
                println("carrying " + id + " at " + rc.getCarrying().getLocation());
                rc.throwRat();
                println("threw guy with id " + id);
                println("now carrying " + rc.getCarrying());
                for (RobotInfo ri : rc.senseNearbyRobots()) {
                    println("neighbour at " + ri.getLocation());
                    if (ri.ID == id) {
                        println("found thrown guy at " + myLoc.distanceSquaredTo(ri.getLocation()));
                    }
                }

            } catch (GameActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };
    }
}
