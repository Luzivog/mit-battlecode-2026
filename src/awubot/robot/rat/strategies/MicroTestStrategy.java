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
import static awubot.util.Util.indicate;
import static awubot.util.Util.println;

public class MicroTestStrategy extends Strategy {

    private CombatStrategy combatStrategy;
    private ExploreStrategy exploreStrategy;
    public MicroTestStrategy() {
        this.combatStrategy = new CombatStrategy();
        this.exploreStrategy = new ExploreStrategy();
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }


    private static int id = -1;
    @Override
    public void runTick() throws GameActionException {
        if (combatStrategy.tryTick()) {
            indicate("FIGHTING!!!!!");
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
                    MapLocation newLoc = loc.add(dir);
                    if (newLoc.distanceSquaredTo(target) <= 2) {
                        return () -> {
                            try {
                                println("ATTEMPTING ATTACK");
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
                                println("ATTEMPTING RATNAP");
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
        final Direction _best = best;
        return () -> {
            try {
                println("THROWING RAT " + _best);
                {
                    MapLocation pos = loc;
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
                        println("found thrown guy at " + loc.distanceSquaredTo(ri.getLocation()));
                    }
                }

            } catch (GameActionException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        };
    }
}
