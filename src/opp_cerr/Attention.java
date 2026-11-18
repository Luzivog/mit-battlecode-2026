package opp_cerr;

import battlecode.common.*;

/*
As some guy from OpenAI once said, "Attention is all you need."

Ok this one is just an overglorified class for generalizing turning logic
*/

public class Attention {

    private static RobotController rc;

    public static void init(RobotController r) {
        rc = r;
    }

    static MapLocation enemyKingTarget = null;
    static int enemyKingTargetID = -1;

    static MapLocation enemyTarget = null;
    static int enemyTargetID = -1;

    static MapLocation catTarget = null;
    static int catTargetID = -1;

    static MapLocation cheeseTarget = null;
    static MapLocation moveTarget = null;
    static Direction scoutTarget = null;

    // Enum
    public enum AttentionType {
        ENEMY_KING,
        ENEMY,
        CAT,
        CHEESE,
        MOVE,
        SCOUT
    }

    public static MapLocation updateUnitTracking(int matchID) throws GameActionException {
        // for units, if it goes out of sight, we lose track of it
        try {
            RobotInfo ri = rc.senseRobot(matchID);
            return ri.getLocation();
        } catch (Exception e) {
            return null;
        }
    }

    public static void update() throws GameActionException {
        // Reset Cheese Target
        if (cheeseTarget != null) {
            if (rc.canSenseLocation(cheeseTarget)
                    && rc.senseMapInfo(cheeseTarget).getCheeseAmount() == 0) {
                cheeseTarget = null; // if we collect cheese
            } else if (rc.getLocation().distanceSquaredTo(cheeseTarget) > 20) {
                cheeseTarget = null; // if we are too far from cheese, in which turning doesn't help
            }
        }

        // Update Unit IDs
        if (enemyKingTarget != null) {
            enemyKingTarget = updateUnitTracking(enemyKingTargetID);
        }
        if (enemyTarget != null) {
            enemyTarget = updateUnitTracking(enemyTargetID);
        }
        if (catTarget != null) {
            catTarget = updateUnitTracking(catTargetID);
        }

        // MoveTarget --> Automatically updated every turn

        // Scout Target --> Updated on turn()
    }

    public static void turn_maplocation(MapLocation m) throws GameActionException {
        Direction d = rc.getLocation().directionTo(m);
        if (d != Direction.CENTER && d != rc.getDirection()) {
            rc.turn(d);
        }
    }

    public static void turn() throws GameActionException {
        if (!rc.canTurn()) return;

        update();

        // if (enemyKingTarget != null) {
        //     turn_maplocation(enemyKingTarget);
        // } else if (enemyTarget != null) {
        //     turn_maplocation(enemyTarget);
        // } else if (catTarget != null) {
        //     turn_maplocation(catTarget);
        // } else

        if (cheeseTarget != null) {
            turn_maplocation(cheeseTarget);
        } else if (moveTarget != null && rc.isMovementReady()) {
            turn_maplocation(moveTarget);
        } else if (scoutTarget != null) {
            rc.turn(scoutTarget);
        }

        // No focus, do nothing

        moveTarget = null;
        scoutTarget = null;
        // these should be set to null because we only want this to affect turning when we set it
        // right before
    }

    public static AttentionType getCurrentFocusType() {
        if (enemyKingTarget != null) {
            return AttentionType.ENEMY_KING;
        } else if (enemyTarget != null) {
            return AttentionType.ENEMY;
        } else if (catTarget != null) {
            return AttentionType.CAT;
        } else if (cheeseTarget != null) {
            return AttentionType.CHEESE;
        } else if (moveTarget != null) {
            return AttentionType.MOVE;
        } else if (scoutTarget != null) {
            return AttentionType.SCOUT;
        } else {
            return null;
        }
    }

    public static void focus(Direction d, MapLocation m, AttentionType type)
            throws GameActionException {

        if (d == null && m == null) return; // invalid

        if (type == null) return; // invalid

        switch (type) {
            case ENEMY_KING -> {
                if (!rc.canSenseLocation(m)) {
                    System.out.println("Cannot sense enemy king at location!");
                    return;
                }
                RobotInfo ri = rc.senseRobotAtLocation(m);
                if (ri != null) {
                    enemyKingTarget = m;
                    enemyKingTargetID = ri.getID();
                } else {
                    System.out.println("Could not sense enemy king at location!");
                }
            }
            case ENEMY -> {
                if (!rc.canSenseLocation(m)) {
                    System.out.println("Cannot sense enemy at location!");
                    return;
                }
                RobotInfo ri = rc.senseRobotAtLocation(m);
                if (ri != null) {
                    enemyTarget = m;
                    enemyTargetID = ri.getID();
                } else {
                    System.out.println("Could not sense enemy at location!");
                }
            }
            case CAT -> {
                if (!rc.canSenseLocation(m)) {
                    System.out.println("Cannot sense cat at location!");
                    return;
                }
                RobotInfo ri = rc.senseRobotAtLocation(m);
                if (ri != null) {
                    catTarget = m;
                    catTargetID = ri.getID();
                } else {
                    System.out.println("Could not sense cat at location!");
                }
            }
            case CHEESE -> {
                cheeseTarget = m;
            }
            case MOVE -> {
                moveTarget = m;
            }
            case SCOUT -> {
                scoutTarget = d;
            }
        }
    }
}
