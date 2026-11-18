package opp_goobie;

import battlecode.common.*;

// public class MovementManager {
//     static boolean canMove(Direction dir) {
//         if (!Robot.rc.canMove(dir))
//             return false;
//         return true;
//     }
//
//     static void move(Direction dir) throws GameActionException {
//         if (dir == Direction.CENTER)
//             return;
//         if (Robot.rc.canTurn() && dir != Robot.rc.getDirection())
//             Robot.rc.turn(dir);
//         Robot.rc.move(dir);
//     }
// }
public class MovementManager {
    static final int PASSABLE = 0;
    static final int DIG_PASSABLE = 1;
    static final int IMPASSABLE = 2;
    static int[] movementTypes;

    static boolean faceRight = true;

    static void update() throws GameActionException {
        movementTypes = new int[9];
        for (Direction dir : Robot.DIRECTIONS) {
            if (Robot.rc.canMove(dir))
                movementTypes[dir.ordinal()] = PASSABLE;
            else {
                MapLocation loc = Robot.rc.getLocation().add(dir);
                if (!Robot.rc.onTheMap(loc)) {
                    movementTypes[dir.ordinal()] = IMPASSABLE;
                    continue;
                }

                if (!Robot.rc.canRemoveDirt(loc)) {
                    movementTypes[dir.ordinal()] = IMPASSABLE;
                    continue;
                }
                MapInfo m = Robot.rc.senseMapInfo(loc);
                if (m.isDirt())
                    movementTypes[dir.ordinal()] = DIG_PASSABLE;
                else
                    movementTypes[dir.ordinal()] = IMPASSABLE;
            }
        }
        movementTypes[Direction.CENTER.ordinal()] = PASSABLE;
    }

    static boolean canMove(Direction dir) {
        if (Robot.rc.getType().isBabyRatType() && MicroManager.microInfo[dir.ordinal()].inCatExclusionZone)
            return false;
        return movementTypes[dir.ordinal()] != IMPASSABLE;
    }

    static void move(Direction dir) throws GameActionException {
        if (dir == Direction.CENTER)
            return;
        if (Robot.rc.canTurn() && Robot.rc.getDirection() != dir)
            Robot.rc.turn(dir);

        int m = movementTypes[dir.ordinal()];
        if (m == PASSABLE) {
            if (!Robot.rc.canMove(dir))
                return;
            Robot.rc.move(dir);
        } else if (m == DIG_PASSABLE) {
            if (!Robot.rc.canRemoveDirt(Robot.rc.getLocation().add(dir)))
                return;
            Robot.rc.removeDirt(Robot.rc.getLocation().add(dir));
        }
    }
}
