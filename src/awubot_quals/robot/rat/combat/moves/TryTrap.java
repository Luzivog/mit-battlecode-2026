package awubot_quals.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.TrapType;
import battlecode.common.UnitType;

import awubot_quals.robot.rat.combat.CombatCommon;
import awubot_quals.robot.rat.combat.CombatStrategy;
import awubot_quals.robot.rat.combat.CombatTracker;
import static awubot_quals.Game.*;
import static awubot_quals.util.Util.*;
import static awubot_quals.robot.rat.BabyRat.*;
import awubot_quals.util.Profiler;
import awubot_quals.CellTracker;
import awubot_quals.RobotTracker;





public final class TryTrap {
    private TryTrap() {
    }

    static boolean confirmedCanPlace;
    static double w_m2_2;
    static double w_m1_2;
    static double w_0_2;
    static double w_1_2;
    static double w_2_2;
    static double w_m2_1;
    static double w_m1_1;
    static double w_0_1;
    static double w_1_1;
    static double w_2_1;
    static double w_m2_0;
    static double w_m1_0;
    static double w_0_0;
    static double w_1_0;
    static double w_2_0;
    static double w_m2_m1;
    static double w_m1_m1;
    static double w_0_m1;
    static double w_1_m1;
    static double w_2_m1;
    static double w_m2_m2;
    static double w_m1_m2;
    static double w_0_m2;
    static double w_1_m2;
    static double w_2_m2;
    static MapLocation bestPlaceLoc;
    static Direction bestMoveDir;
    static Direction bestFacing;
    static double best;
    static MapLocation curMoveLoc;
    static MapLocation weightSource;
    static int weightCompTime;
    static int bytecodeStart, bytecodeBudget;

    public static boolean shouldTrap() throws GameActionException {
        if (!rc.isActionReady() || !confirmCanPlace()) return false;
        if (rc.getAllCheese() > 3000) return true;
        if (rc.getAllCheese() < 300 || rc.getRawCheese() < 20) return false;
        return true;
    }

    public static boolean run() throws GameActionException {
        if (!shouldTrap()) return false;
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Math.min(3000, (int) (Clock.getBytecodesLeft() - 1000) / 2);
        Profiler.push();
        computeWeights();
        Profiler.pop("TryTrap.computeWeights");
        resetBest();
        selectMoves();
        if (best <= 0.5) return false;
        assert(CombatStrategy.performMoves(bestMoveDir, bestFacing));
        if (!rc.canPlaceRatTrap(bestPlaceLoc)) {
        }
        rc.placeRatTrap(bestPlaceLoc);
        println("!!!!!!!!!! placed rat trap");
        indicate("COMBAT STRAT: TRAP");
        return true;
    }

    private static boolean confirmCanPlace() throws GameActionException {
        confirmedCanPlace = false;
        for (MapInfo info : rc.senseNearbyMapInfos(2)) {
            if (rc.canPlaceRatTrap(info.getMapLocation())) {
                confirmedCanPlace = true;
                break;
            }
        }
        return confirmedCanPlace;
    }

    // has room for optimisation if reallllyyy needed
    private static void computeWeights() throws GameActionException {
        weightCompTime = time;
        weightSource = myLoc;
        {
            MapLocation loc = new MapLocation(myLocX  + -2, myLocY  + 2);
            if (!rc.canSenseLocation(loc)) {
                w_m2_2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m2_2 = -1000;
                } else {
                    w_m2_2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -1, myLocY  + 2);
            if (!rc.canSenseLocation(loc)) {
                w_m1_2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m1_2 = -1000;
                } else {
                    w_m1_2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX , myLocY  + 2);
            if (!rc.canSenseLocation(loc)) {
                w_0_2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_0_2 = -1000;
                } else {
                    w_0_2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 1, myLocY  + 2);
            if (!rc.canSenseLocation(loc)) {
                w_1_2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_1_2 = -1000;
                } else {
                    w_1_2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 2, myLocY  + 2);
            if (!rc.canSenseLocation(loc)) {
                w_2_2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_2_2 = -1000;
                } else {
                    w_2_2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -2, myLocY  + 1);
            if (!rc.canSenseLocation(loc)) {
                w_m2_1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m2_1 = -1000;
                } else {
                    w_m2_1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -1, myLocY  + 1);
            if (!rc.canSenseLocation(loc)) {
                w_m1_1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m1_1 = -1000;
                } else {
                    w_m1_1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX , myLocY  + 1);
            if (!rc.canSenseLocation(loc)) {
                w_0_1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_0_1 = -1000;
                } else {
                    w_0_1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 1, myLocY  + 1);
            if (!rc.canSenseLocation(loc)) {
                w_1_1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_1_1 = -1000;
                } else {
                    w_1_1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 2, myLocY  + 1);
            if (!rc.canSenseLocation(loc)) {
                w_2_1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_2_1 = -1000;
                } else {
                    w_2_1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -2, myLocY );
            if (!rc.canSenseLocation(loc)) {
                w_m2_0 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m2_0 = -1000;
                } else {
                    w_m2_0 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -1, myLocY );
            if (!rc.canSenseLocation(loc)) {
                w_m1_0 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m1_0 = -1000;
                } else {
                    w_m1_0 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX , myLocY );
            if (!rc.canSenseLocation(loc)) {
                w_0_0 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine()) {
                    w_0_0 = -1000;
                } else {
                    w_0_0 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 1, myLocY );
            if (!rc.canSenseLocation(loc)) {
                w_1_0 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_1_0 = -1000;
                } else {
                    w_1_0 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 2, myLocY );
            if (!rc.canSenseLocation(loc)) {
                w_2_0 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_2_0 = -1000;
                } else {
                    w_2_0 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -2, myLocY  + -1);
            if (!rc.canSenseLocation(loc)) {
                w_m2_m1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m2_m1 = -1000;
                } else {
                    w_m2_m1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -1, myLocY  + -1);
            if (!rc.canSenseLocation(loc)) {
                w_m1_m1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m1_m1 = -1000;
                } else {
                    w_m1_m1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX , myLocY  + -1);
            if (!rc.canSenseLocation(loc)) {
                w_0_m1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_0_m1 = -1000;
                } else {
                    w_0_m1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 1, myLocY  + -1);
            if (!rc.canSenseLocation(loc)) {
                w_1_m1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_1_m1 = -1000;
                } else {
                    w_1_m1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 2, myLocY  + -1);
            if (!rc.canSenseLocation(loc)) {
                w_2_m1 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_2_m1 = -1000;
                } else {
                    w_2_m1 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -2, myLocY  + -2);
            if (!rc.canSenseLocation(loc)) {
                w_m2_m2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m2_m2 = -1000;
                } else {
                    w_m2_m2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + -1, myLocY  + -2);
            if (!rc.canSenseLocation(loc)) {
                w_m1_m2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_m1_m2 = -1000;
                } else {
                    w_m1_m2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX , myLocY  + -2);
            if (!rc.canSenseLocation(loc)) {
                w_0_m2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_0_m2 = -1000;
                } else {
                    w_0_m2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 1, myLocY  + -2);
            if (!rc.canSenseLocation(loc)) {
                w_1_m2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_1_m2 = -1000;
                } else {
                    w_1_m2 = 0;
                }
            }
        }
        {
            MapLocation loc = new MapLocation(myLocX  + 2, myLocY  + -2);
            if (!rc.canSenseLocation(loc)) {
                w_2_m2 = -1000;
            } else {
                MapInfo info = rc.senseMapInfo(loc);
                if (!info.isPassable() || info.hasCheeseMine() || rc.senseRobotAtLocation(loc) != null) {
                    w_2_m2 = -1000;
                } else {
                    w_2_m2 = 0;
                }
            }
        }

        applyTrapWeights();
        applyEnemyWeights();

    }

    private static void clearWeights() {
        w_m2_2 = 0;
        w_m1_2 = 0;
        w_0_2 = 0;
        w_1_2 = 0;
        w_2_2 = 0;
        w_m2_1 = 0;
        w_m1_1 = 0;
        w_0_1 = 0;
        w_1_1 = 0;
        w_2_1 = 0;
        w_m2_0 = 0;
        w_m1_0 = 0;
        w_0_0 = 0;
        w_1_0 = 0;
        w_2_0 = 0;
        w_m2_m1 = 0;
        w_m1_m1 = 0;
        w_0_m1 = 0;
        w_1_m1 = 0;
        w_2_m1 = 0;
        w_m2_m2 = 0;
        w_m1_m2 = 0;
        w_0_m2 = 0;
        w_1_m2 = 0;
        w_2_m2 = 0;
    }

    private static void applyEnemyWeights() {
        for (int i = 0; i < RobotTracker.numEnemies && i < 5; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
            if (info.getType() != UnitType.BABY_RAT) continue;
            applyEnemyWeight(info);
        }
    }

    private static double getWeight(int dx, int dy) {
        switch (dx) {
            case -2 -> {
                switch (dy) {
                    case -2 -> {
                        return w_m2_m2;
                    }
                    case -1 -> {
                        return w_m2_m1;
                    }
                    case 0 -> {
                        return w_m2_0;
                    }
                    case 1 -> {
                        return w_m2_1;
                    }
                    case 2 -> {
                        return w_m2_2;
                    }
                    default -> { return -1000; }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -2 -> {
                        return w_m1_m2;
                    }
                    case -1 -> {
                        return w_m1_m1;
                    }
                    case 0 -> {
                        return w_m1_0;
                    }
                    case 1 -> {
                        return w_m1_1;
                    }
                    case 2 -> {
                        return w_m1_2;
                    }
                    default -> { return -1000; }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -2 -> {
                        return w_0_m2;
                    }
                    case -1 -> {
                        return w_0_m1;
                    }
                    case 0 -> {
                        return w_0_0;
                    }
                    case 1 -> {
                        return w_0_1;
                    }
                    case 2 -> {
                        return w_0_2;
                    }
                    default -> { return -1000; }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -2 -> {
                        return w_1_m2;
                    }
                    case -1 -> {
                        return w_1_m1;
                    }
                    case 0 -> {
                        return w_1_0;
                    }
                    case 1 -> {
                        return w_1_1;
                    }
                    case 2 -> {
                        return w_1_2;
                    }
                    default -> { return -1000; }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -2 -> {
                        return w_2_m2;
                    }
                    case -1 -> {
                        return w_2_m1;
                    }
                    case 0 -> {
                        return w_2_0;
                    }
                    case 1 -> {
                        return w_2_1;
                    }
                    case 2 -> {
                        return w_2_2;
                    }
                    default -> { return -1000; }
                }
            }
            default -> { return -1000; }
        }
    }

    private static void applyEnemyWeight(RobotInfo rinfo) {
        if (rinfo.getHealth() <= 30) return;
        int x = rinfo.location.x - myLocX;
        int y = rinfo.location.y - myLocY;
        switch (x) {
            case -3 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m2_m2 += 2;
}

                            case 1 -> {

w_m2_m2 += 3;
}

                            case 2 -> {

w_m2_m2 += 2;
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {

w_m2_m2 += 2;
w_m2_m1 += 3;
}

                            case 2 -> {

w_m2_m2 += 3;
w_m2_m1 += 2;
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {

w_m2_m1 += 2;
w_m2_0 += 3;
}

                            case 2 -> {

w_m2_m2 += 2;
w_m2_m1 += 3;
w_m2_0 += 2;
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {

w_m2_0 += 2;
w_m2_1 += 3;
}

                            case 2 -> {

w_m2_m1 += 2;
w_m2_0 += 3;
w_m2_1 += 2;
}

                            case 3 -> {

w_m2_m1 += 3;
w_m2_0 += 2;
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {

w_m2_0 += 2;
w_m2_1 += 3;
w_m2_2 += 2;
}

                            case 3 -> {

w_m2_0 += 3;
w_m2_1 += 2;
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {

w_m2_1 += 2;
w_m2_2 += 3;
}

                            case 3 -> {

w_m2_1 += 3;
w_m2_2 += 2;
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {

w_m2_2 += 2;
}

                            case 3 -> {

w_m2_2 += 3;
}

                            case 4 -> {

w_m2_2 += 2;
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -2 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m2_m2 += 3;
w_m1_m2 += 2;
}

                            case 1 -> {

w_m2_m2 += 2;
w_m1_m2 += 3;
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m2_m1 += 3;
w_m1_m1 += 2;
}

                            case 1 -> {

w_m2_m1 += 2;
w_m1_m2 += 2;
w_m1_m1 += 3;
}

                            case 2 -> {

w_m1_m2 += 3;
w_m1_m1 += 2;
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {

w_m2_0 += 2;
w_m1_m1 += 2;
w_m1_0 += 3;
}

                            case 2 -> {

w_m1_m2 += 2;
w_m1_m1 += 3;
w_m1_0 += 2;
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {

w_m2_1 += 2;
w_m1_0 += 2;
w_m1_1 += 3;
}

                            case 2 -> {

w_m1_m1 += 2;
w_m1_0 += 3;
w_m1_1 += 2;
}

                            case 3 -> {

w_m2_m1 += 2;
w_m1_m1 += 3;
w_m1_0 += 2;
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {

w_m1_0 += 2;
w_m1_1 += 3;
w_m1_2 += 2;
}

                            case 3 -> {

w_m2_0 += 2;
w_m1_0 += 3;
w_m1_1 += 2;
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {

w_m1_1 += 2;
w_m1_2 += 3;
}

                            case 3 -> {

w_m2_1 += 2;
w_m1_1 += 3;
w_m1_2 += 2;
}

                            case 4 -> {

w_m2_1 += 3;
w_m1_1 += 2;
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {

w_m2_2 += 2;
w_m1_2 += 3;
}

                            case 4 -> {

w_m2_2 += 3;
w_m1_2 += 2;
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m2_m2 += 2;
w_m1_m2 += 3;
w_0_m2 += 2;
}

                            case 1 -> {

w_m1_m2 += 2;
w_0_m2 += 3;
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m2_m1 += 2;
w_m1_m1 += 3;
w_0_m1 += 2;
}

                            case 1 -> {

w_m1_m1 += 2;
w_0_m2 += 2;
w_0_m1 += 3;
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m2_0 += 2;
w_m1_0 += 3;
w_0_0 += 2;
}

                            case 1 -> {

w_m1_0 += 2;
w_0_m1 += 2;
w_0_0 += 3;
}

                            case 2 -> {

w_0_m2 += 2;
w_0_m1 += 3;
w_0_0 += 2;
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {

w_m1_1 += 2;
w_0_0 += 2;
w_0_1 += 3;
}

                            case 2 -> {

w_0_m1 += 2;
w_0_0 += 3;
w_0_1 += 2;
}

                            case 3 -> {

w_m1_m1 += 2;
w_0_m1 += 3;
w_0_0 += 2;
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {

w_0_0 += 2;
w_0_1 += 3;
w_0_2 += 2;
}

                            case 3 -> {

w_m1_0 += 2;
w_0_0 += 3;
w_0_1 += 2;
}

                            case 4 -> {

w_m2_0 += 2;
w_m1_0 += 3;
w_0_0 += 2;
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {

w_m1_1 += 2;
w_0_1 += 3;
w_0_2 += 2;
}

                            case 4 -> {

w_m2_1 += 2;
w_m1_1 += 3;
w_0_1 += 2;
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {

w_m1_2 += 2;
w_0_2 += 3;
}

                            case 4 -> {

w_m2_2 += 2;
w_m1_2 += 3;
w_0_2 += 2;
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m1_m2 += 2;
w_0_m2 += 3;
w_1_m2 += 2;
}

                            case 1 -> {

w_0_m2 += 2;
w_1_m2 += 3;
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {

w_m1_m2 += 3;
w_0_m2 += 2;
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m1_m1 += 2;
w_0_m1 += 3;
w_1_m1 += 2;
}

                            case 1 -> {

w_0_m1 += 2;
w_1_m2 += 2;
w_1_m1 += 3;
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {

w_m1_m2 += 2;
w_m1_m1 += 3;
w_0_m1 += 2;
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m1_0 += 2;
w_0_0 += 3;
w_1_0 += 2;
}

                            case 1 -> {

w_0_0 += 2;
w_1_m1 += 2;
w_1_0 += 3;
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {

w_m1_m1 += 2;
w_m1_0 += 3;
w_0_0 += 2;
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_m1_1 += 2;
w_0_1 += 3;
w_1_1 += 2;
}

                            case 1 -> {

w_0_1 += 2;
w_1_0 += 2;
w_1_1 += 3;
}

                            case 2 -> {

w_1_m1 += 2;
w_1_0 += 3;
w_1_1 += 2;
}

                            case 3 -> {

w_0_m1 += 2;
w_1_m1 += 3;
w_1_0 += 2;
}

                            case 4 -> {

w_m1_m1 += 2;
w_0_m1 += 3;
w_1_m1 += 2;
}

                            case 5 -> {

w_m1_m1 += 3;
w_m1_0 += 2;
w_0_m1 += 2;
}

                            case 6 -> {

w_m1_m1 += 2;
w_m1_0 += 3;
w_m1_1 += 2;
}

                            case 7 -> {

w_m1_0 += 2;
w_m1_1 += 3;
w_0_1 += 2;
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {

w_0_0 += 2;
w_1_0 += 3;
w_1_1 += 2;
}

                            case 4 -> {

w_m1_0 += 2;
w_0_0 += 3;
w_1_0 += 2;
}

                            case 5 -> {

w_m1_0 += 3;
w_m1_1 += 2;
w_0_0 += 2;
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {

w_0_1 += 2;
w_1_1 += 3;
w_1_2 += 2;
}

                            case 4 -> {

w_m1_1 += 2;
w_0_1 += 3;
w_1_1 += 2;
}

                            case 5 -> {

w_m1_1 += 3;
w_m1_2 += 2;
w_0_1 += 2;
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {

w_0_2 += 2;
w_1_2 += 3;
}

                            case 4 -> {

w_m1_2 += 2;
w_0_2 += 3;
w_1_2 += 2;
}

                            case 5 -> {

w_m1_2 += 3;
w_0_2 += 2;
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_0_m2 += 2;
w_1_m2 += 3;
w_2_m2 += 2;
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {

w_0_m2 += 3;
w_1_m2 += 2;
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_0_m1 += 2;
w_1_m1 += 3;
w_2_m1 += 2;
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {

w_0_m2 += 2;
w_0_m1 += 3;
w_1_m1 += 2;
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_0_0 += 2;
w_1_0 += 3;
w_2_0 += 2;
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {

w_0_m2 += 2;
w_0_m1 += 3;
w_0_0 += 2;
}

                            case 7 -> {

w_0_m1 += 2;
w_0_0 += 3;
w_1_0 += 2;
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {

w_0_m1 += 3;
w_0_0 += 2;
w_1_m1 += 2;
}

                            case 6 -> {

w_0_m1 += 2;
w_0_0 += 3;
w_0_1 += 2;
}

                            case 7 -> {

w_0_0 += 2;
w_0_1 += 3;
w_1_1 += 2;
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {

w_0_0 += 2;
w_1_0 += 3;
w_2_0 += 2;
}

                            case 5 -> {

w_0_0 += 3;
w_0_1 += 2;
w_1_0 += 2;
}

                            case 6 -> {

w_0_0 += 2;
w_0_1 += 3;
w_0_2 += 2;
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {

w_0_1 += 2;
w_1_1 += 3;
w_2_1 += 2;
}

                            case 5 -> {

w_0_1 += 3;
w_0_2 += 2;
w_1_1 += 2;
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {

w_0_2 += 2;
w_1_2 += 3;
w_2_2 += 2;
}

                            case 5 -> {

w_0_2 += 3;
w_1_2 += 2;
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_1_m2 += 2;
w_2_m2 += 3;
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {
}

                            case 7 -> {

w_1_m2 += 3;
w_2_m2 += 2;
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_1_m1 += 2;
w_2_m1 += 3;
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {

w_1_m2 += 3;
w_1_m1 += 2;
}

                            case 7 -> {

w_1_m2 += 2;
w_1_m1 += 3;
w_2_m1 += 2;
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {

w_1_m2 += 2;
w_1_m1 += 3;
w_1_0 += 2;
}

                            case 7 -> {

w_1_m1 += 2;
w_1_0 += 3;
w_2_0 += 2;
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {

w_1_m1 += 3;
w_1_0 += 2;
w_2_m1 += 2;
}

                            case 6 -> {

w_1_m1 += 2;
w_1_0 += 3;
w_1_1 += 2;
}

                            case 7 -> {

w_1_0 += 2;
w_1_1 += 3;
w_2_1 += 2;
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {

w_1_0 += 3;
w_1_1 += 2;
w_2_0 += 2;
}

                            case 6 -> {

w_1_0 += 2;
w_1_1 += 3;
w_1_2 += 2;
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {

w_1_1 += 2;
w_2_1 += 3;
}

                            case 5 -> {

w_1_1 += 3;
w_1_2 += 2;
w_2_1 += 2;
}

                            case 6 -> {

w_1_1 += 2;
w_1_2 += 3;
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {

w_1_2 += 2;
w_2_2 += 3;
}

                            case 5 -> {

w_1_2 += 3;
w_2_2 += 2;
}

                            case 6 -> {
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            case 3 -> {
                switch (y) {
                    case -3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {

w_2_m2 += 2;
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {

w_2_m2 += 2;
}

                            case 7 -> {

w_2_m2 += 3;
}
                            default -> { }
                        }
                    }
                    case -2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {

w_2_m2 += 3;
w_2_m1 += 2;
}

                            case 7 -> {

w_2_m2 += 2;
w_2_m1 += 3;
}
                            default -> { }
                        }
                    }
                    case -1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {
}

                            case 6 -> {

w_2_m2 += 2;
w_2_m1 += 3;
w_2_0 += 2;
}

                            case 7 -> {

w_2_m1 += 2;
w_2_0 += 3;
}
                            default -> { }
                        }
                    }
                    case 0 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {

w_2_m1 += 3;
w_2_0 += 2;
}

                            case 6 -> {

w_2_m1 += 2;
w_2_0 += 3;
w_2_1 += 2;
}

                            case 7 -> {

w_2_0 += 2;
w_2_1 += 3;
}
                            default -> { }
                        }
                    }
                    case 1 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {

w_2_0 += 3;
w_2_1 += 2;
}

                            case 6 -> {

w_2_0 += 2;
w_2_1 += 3;
w_2_2 += 2;
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 2 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {
}

                            case 5 -> {

w_2_1 += 3;
w_2_2 += 2;
}

                            case 6 -> {

w_2_1 += 2;
w_2_2 += 3;
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    case 3 -> {
                        switch (rinfo.direction.ordinal()) {

                            case 0 -> {
}

                            case 1 -> {
}

                            case 2 -> {
}

                            case 3 -> {
}

                            case 4 -> {

w_2_2 += 2;
}

                            case 5 -> {

w_2_2 += 3;
}

                            case 6 -> {

w_2_2 += 2;
}

                            case 7 -> {
}
                            default -> { }
                        }
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void applyTrapWeights() {
        switch (CellTracker.numNearbyRatTraps) {
            case 29: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[28];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 28: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[27];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 27: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[26];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 26: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[25];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 25: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[24];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 24: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[23];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 23: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[22];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 22: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[21];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 21: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[20];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 20: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[19];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 19: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[18];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 18: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[17];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 17: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[16];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 16: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[15];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 15: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[14];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 14: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[13];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 13: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[12];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 12: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[11];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 11: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[10];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 10: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[9];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 9: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[8];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 8: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[7];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 7: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[6];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 6: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[5];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 5: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[4];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 4: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[3];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 3: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[2];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 2: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[1];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 1: {
                MapLocation trapLoc = CellTracker.nearbyRatTraps[0];
                int x = trapLoc.x - myLocX;
                int y = trapLoc.y - myLocY;
                applyTrapWeight(x, y);
            } 
            case 0:
                break;
            default:
                assert(false);
                break;
        }
    }

    private static void applyTrapWeight(int x, int y) {
        // println("\napplying trap weight for trap at " + x + ", " + y + "\n");
        switch (x) {
            case -2 -> {
                switch (y) {
                    case -2 -> {
                        w_m2_m2 -= 100;                        w_m2_m1 -= 100;                        w_m1_m2 -= 100;                        w_m1_m1 -= 100;                    }
                    case -1 -> {
                        w_m2_m2 -= 100;                        w_m2_m1 -= 100;                        w_m2_0 -= 100;                        w_m1_m2 -= 100;                        w_m1_m1 -= 100;                        w_m1_0 -= 100;                    }
                    case 0 -> {
                        w_m2_m1 -= 100;                        w_m2_0 -= 100;                        w_m2_1 -= 100;                        w_m1_m1 -= 100;                        w_m1_0 -= 100;                        w_m1_1 -= 100;                    }
                    case 1 -> {
                        w_m2_0 -= 100;                        w_m2_1 -= 100;                        w_m2_2 -= 100;                        w_m1_0 -= 100;                        w_m1_1 -= 100;                        w_m1_2 -= 100;                    }
                    case 2 -> {
                        w_m2_1 -= 100;                        w_m2_2 -= 100;                        w_m1_1 -= 100;                        w_m1_2 -= 100;                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (y) {
                    case -2 -> {
                        w_m2_m2 -= 100;                        w_m2_m1 -= 100;                        w_m1_m2 -= 100;                        w_m1_m1 -= 100;                        w_0_m2 -= 100;                        w_0_m1 -= 100;                    }
                    case -1 -> {
                        w_m2_m2 -= 100;                        w_m2_m1 -= 100;                        w_m2_0 -= 100;                        w_m1_m2 -= 100;                        w_m1_m1 -= 100;                        w_m1_0 -= 100;                        w_0_m2 -= 100;                        w_0_m1 -= 100;                        w_0_0 -= 100;                    }
                    case 0 -> {
                        w_m2_m1 -= 100;                        w_m2_0 -= 100;                        w_m2_1 -= 100;                        w_m1_m1 -= 100;                        w_m1_0 -= 100;                        w_m1_1 -= 100;                        w_0_m1 -= 100;                        w_0_0 -= 100;                        w_0_1 -= 100;                    }
                    case 1 -> {
                        w_m2_0 -= 100;                        w_m2_1 -= 100;                        w_m2_2 -= 100;                        w_m1_0 -= 100;                        w_m1_1 -= 100;                        w_m1_2 -= 100;                        w_0_0 -= 100;                        w_0_1 -= 100;                        w_0_2 -= 100;                    }
                    case 2 -> {
                        w_m2_1 -= 100;                        w_m2_2 -= 100;                        w_m1_1 -= 100;                        w_m1_2 -= 100;                        w_0_1 -= 100;                        w_0_2 -= 100;                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (y) {
                    case -2 -> {
                        w_m1_m2 -= 100;                        w_m1_m1 -= 100;                        w_0_m2 -= 100;                        w_0_m1 -= 100;                        w_1_m2 -= 100;                        w_1_m1 -= 100;                    }
                    case -1 -> {
                        w_m1_m2 -= 100;                        w_m1_m1 -= 100;                        w_m1_0 -= 100;                        w_0_m2 -= 100;                        w_0_m1 -= 100;                        w_0_0 -= 100;                        w_1_m2 -= 100;                        w_1_m1 -= 100;                        w_1_0 -= 100;                    }
                    case 0 -> {
                        w_m1_m1 -= 100;                        w_m1_0 -= 100;                        w_m1_1 -= 100;                        w_0_m1 -= 100;                        w_0_0 -= 100;                        w_0_1 -= 100;                        w_1_m1 -= 100;                        w_1_0 -= 100;                        w_1_1 -= 100;                    }
                    case 1 -> {
                        w_m1_0 -= 100;                        w_m1_1 -= 100;                        w_m1_2 -= 100;                        w_0_0 -= 100;                        w_0_1 -= 100;                        w_0_2 -= 100;                        w_1_0 -= 100;                        w_1_1 -= 100;                        w_1_2 -= 100;                    }
                    case 2 -> {
                        w_m1_1 -= 100;                        w_m1_2 -= 100;                        w_0_1 -= 100;                        w_0_2 -= 100;                        w_1_1 -= 100;                        w_1_2 -= 100;                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (y) {
                    case -2 -> {
                        w_0_m2 -= 100;                        w_0_m1 -= 100;                        w_1_m2 -= 100;                        w_1_m1 -= 100;                        w_2_m2 -= 100;                        w_2_m1 -= 100;                    }
                    case -1 -> {
                        w_0_m2 -= 100;                        w_0_m1 -= 100;                        w_0_0 -= 100;                        w_1_m2 -= 100;                        w_1_m1 -= 100;                        w_1_0 -= 100;                        w_2_m2 -= 100;                        w_2_m1 -= 100;                        w_2_0 -= 100;                    }
                    case 0 -> {
                        w_0_m1 -= 100;                        w_0_0 -= 100;                        w_0_1 -= 100;                        w_1_m1 -= 100;                        w_1_0 -= 100;                        w_1_1 -= 100;                        w_2_m1 -= 100;                        w_2_0 -= 100;                        w_2_1 -= 100;                    }
                    case 1 -> {
                        w_0_0 -= 100;                        w_0_1 -= 100;                        w_0_2 -= 100;                        w_1_0 -= 100;                        w_1_1 -= 100;                        w_1_2 -= 100;                        w_2_0 -= 100;                        w_2_1 -= 100;                        w_2_2 -= 100;                    }
                    case 2 -> {
                        w_0_1 -= 100;                        w_0_2 -= 100;                        w_1_1 -= 100;                        w_1_2 -= 100;                        w_2_1 -= 100;                        w_2_2 -= 100;                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (y) {
                    case -2 -> {
                        w_1_m2 -= 100;                        w_1_m1 -= 100;                        w_2_m2 -= 100;                        w_2_m1 -= 100;                    }
                    case -1 -> {
                        w_1_m2 -= 100;                        w_1_m1 -= 100;                        w_1_0 -= 100;                        w_2_m2 -= 100;                        w_2_m1 -= 100;                        w_2_0 -= 100;                    }
                    case 0 -> {
                        w_1_m1 -= 100;                        w_1_0 -= 100;                        w_1_1 -= 100;                        w_2_m1 -= 100;                        w_2_0 -= 100;                        w_2_1 -= 100;                    }
                    case 1 -> {
                        w_1_0 -= 100;                        w_1_1 -= 100;                        w_1_2 -= 100;                        w_2_0 -= 100;                        w_2_1 -= 100;                        w_2_2 -= 100;                    }
                    case 2 -> {
                        w_1_1 -= 100;                        w_1_2 -= 100;                        w_2_1 -= 100;                        w_2_2 -= 100;                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void resetBest() {
        bestPlaceLoc = null;
        bestMoveDir = null;
        bestFacing = null;
        best = -1;
    }

    private static void selectMoves() throws GameActionException {
        switch (myDir.ordinal()) {
            case 0 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveW();
            }
            case 1 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSE();
            }
            case 2 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveS();
            }
            case 3 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSW();
            }
            case 4 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveW();
            }
            case 5 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveS();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSE();
            }
            case 6 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveS();
            }
            case 7 -> {
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveN();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveW();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveNE();
                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) break;
                moveSW();
            }
            default -> { }
        }
    }

    private static void moveN() throws GameActionException {
        if (!CombatCommon.canMoveN) return;
        curMoveLoc = myLoc.add(Direction.NORTH);
        faceNN();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceNEN();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceNWN();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceNN() throws GameActionException {
        if (CombatTracker.threatNN != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTH);
        if (moveCost == -1) return;
        placeNNN();
        placeNENN();
        placeNWNN();
    }

private static void placeNNN() {
        if (w_0_2 >= 0) {
            double score = w_0_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTH;
            }
        }
    }
private static void placeNENN() {
        if (w_1_2 >= 0) {
            double score = w_1_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTH;
            }
        }
    }
private static void placeNWNN() {
        if (w_m1_2 >= 0) {
            double score = w_m1_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTH;
            }
        }
    }
    private static void faceNEN() throws GameActionException {
        if (CombatTracker.threatNNE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        placeNENEN();
        placeNNEN();
        placeENEN();
    }

private static void placeNENEN() {
        if (w_1_2 >= 0) {
            double score = w_1_2;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
private static void placeNNEN() {
        if (w_0_2 >= 0) {
            double score = w_0_2;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
private static void placeENEN() {
        if (w_1_1 >= 0) {
            double score = w_1_1;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
    private static void faceNWN() throws GameActionException {
        if (CombatTracker.threatNNW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        placeNWNWN();
        placeNNWN();
        placeWNWN();
    }

private static void placeNWNWN() {
        if (w_m1_2 >= 0) {
            double score = w_m1_2;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
private static void placeNNWN() {
        if (w_0_2 >= 0) {
            double score = w_0_2;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
private static void placeWNWN() {
        if (w_m1_1 >= 0) {
            double score = w_m1_1;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.NORTH;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
    private static void moveNE() throws GameActionException {
        if (!CombatCommon.canMoveNE) return;
        curMoveLoc = myLoc.add(Direction.NORTHEAST);
        faceNENE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceNNE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceENE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceNENE() throws GameActionException {
        if (CombatTracker.threatNENE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        placeNENENE();
        placeNNENE();
        placeENENE();
    }

private static void placeNENENE() {
        if (w_2_2 >= 0) {
            double score = w_2_2;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
private static void placeNNENE() {
        if (w_1_2 >= 0) {
            double score = w_1_2;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
private static void placeENENE() {
        if (w_2_1 >= 0) {
            double score = w_2_1;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
    private static void faceNNE() throws GameActionException {
        if (CombatTracker.threatNEN != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        placeNNNE();
        placeNENNE();
        placeNWNNE();
    }

private static void placeNNNE() {
        if (w_1_2 >= 0) {
            double score = w_1_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.NORTH;
            }
        }
    }
private static void placeNENNE() {
        if (w_2_2 >= 0) {
            double score = w_2_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.NORTH;
            }
        }
    }
private static void placeNWNNE() {
        if (w_0_2 >= 0) {
            double score = w_0_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.NORTH;
            }
        }
    }
    private static void faceENE() throws GameActionException {
        if (CombatTracker.threatNEE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        placeEENE();
        placeNEENE();
        placeSEENE();
    }

private static void placeEENE() {
        if (w_2_1 >= 0) {
            double score = w_2_1;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.EAST;
            }
        }
    }
private static void placeNEENE() {
        if (w_2_2 >= 0) {
            double score = w_2_2;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.EAST;
            }
        }
    }
private static void placeSEENE() {
        if (w_2_0 >= 0) {
            double score = w_2_0;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.NORTHEAST;
                bestFacing = Direction.EAST;
            }
        }
    }
    private static void moveE() throws GameActionException {
        if (!CombatCommon.canMoveE) return;
        curMoveLoc = myLoc.add(Direction.EAST);
        faceEE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceNEE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceSEE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceEE() throws GameActionException {
        if (CombatTracker.threatEE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.EAST);
        if (moveCost == -1) return;
        placeEEE();
        placeNEEE();
        placeSEEE();
    }

private static void placeEEE() {
        if (w_2_0 >= 0) {
            double score = w_2_0;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.EAST;
            }
        }
    }
private static void placeNEEE() {
        if (w_2_1 >= 0) {
            double score = w_2_1;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.EAST;
            }
        }
    }
private static void placeSEEE() {
        if (w_2_m1 >= 0) {
            double score = w_2_m1;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.EAST;
            }
        }
    }
    private static void faceNEE() throws GameActionException {
        if (CombatTracker.threatENE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        placeNENEE();
        placeNNEE();
        placeENEE();
    }

private static void placeNENEE() {
        if (w_2_1 >= 0) {
            double score = w_2_1;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
private static void placeNNEE() {
        if (w_1_1 >= 0) {
            double score = w_1_1;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
private static void placeENEE() {
        if (w_2_0 >= 0) {
            double score = w_2_0;
            if (Direction.NORTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.NORTHEAST;
            }
        }
    }
    private static void faceSEE() throws GameActionException {
        if (CombatTracker.threatESE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        placeSESEE();
        placeSSEE();
        placeESEE();
    }

private static void placeSESEE() {
        if (w_2_m1 >= 0) {
            double score = w_2_m1;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
private static void placeSSEE() {
        if (w_1_m1 >= 0) {
            double score = w_1_m1;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
private static void placeESEE() {
        if (w_2_0 >= 0) {
            double score = w_2_0;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.EAST;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
    private static void moveSE() throws GameActionException {
        if (!CombatCommon.canMoveSE) return;
        curMoveLoc = myLoc.add(Direction.SOUTHEAST);
        faceSESE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceSSE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceESE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceSESE() throws GameActionException {
        if (CombatTracker.threatSESE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        placeSESESE();
        placeSSESE();
        placeESESE();
    }

private static void placeSESESE() {
        if (w_2_m2 >= 0) {
            double score = w_2_m2;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
private static void placeSSESE() {
        if (w_1_m2 >= 0) {
            double score = w_1_m2;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
private static void placeESESE() {
        if (w_2_m1 >= 0) {
            double score = w_2_m1;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
    private static void faceSSE() throws GameActionException {
        if (CombatTracker.threatSES != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        placeSSSE();
        placeSESSE();
        placeSWSSE();
    }

private static void placeSSSE() {
        if (w_1_m2 >= 0) {
            double score = w_1_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.SOUTH;
            }
        }
    }
private static void placeSESSE() {
        if (w_2_m2 >= 0) {
            double score = w_2_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.SOUTH;
            }
        }
    }
private static void placeSWSSE() {
        if (w_0_m2 >= 0) {
            double score = w_0_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.SOUTH;
            }
        }
    }
    private static void faceESE() throws GameActionException {
        if (CombatTracker.threatSEE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        placeEESE();
        placeNEESE();
        placeSEESE();
    }

private static void placeEESE() {
        if (w_2_m1 >= 0) {
            double score = w_2_m1;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.EAST;
            }
        }
    }
private static void placeNEESE() {
        if (w_2_0 >= 0) {
            double score = w_2_0;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.EAST;
            }
        }
    }
private static void placeSEESE() {
        if (w_2_m2 >= 0) {
            double score = w_2_m2;
            if (Direction.EAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.SOUTHEAST;
                bestFacing = Direction.EAST;
            }
        }
    }
    private static void moveS() throws GameActionException {
        if (!CombatCommon.canMoveS) return;
        curMoveLoc = myLoc.add(Direction.SOUTH);
        faceSS();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceSES();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceSWS();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceSS() throws GameActionException {
        if (CombatTracker.threatSS != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTH);
        if (moveCost == -1) return;
        placeSSS();
        placeSESS();
        placeSWSS();
    }

private static void placeSSS() {
        if (w_0_m2 >= 0) {
            double score = w_0_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTH;
            }
        }
    }
private static void placeSESS() {
        if (w_1_m2 >= 0) {
            double score = w_1_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTH;
            }
        }
    }
private static void placeSWSS() {
        if (w_m1_m2 >= 0) {
            double score = w_m1_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTH;
            }
        }
    }
    private static void faceSES() throws GameActionException {
        if (CombatTracker.threatSSE != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        placeSESES();
        placeSSES();
        placeESES();
    }

private static void placeSESES() {
        if (w_1_m2 >= 0) {
            double score = w_1_m2;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
private static void placeSSES() {
        if (w_0_m2 >= 0) {
            double score = w_0_m2;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
private static void placeESES() {
        if (w_1_m1 >= 0) {
            double score = w_1_m1;
            if (Direction.SOUTHEAST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.EAST);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTHEAST;
            }
        }
    }
    private static void faceSWS() throws GameActionException {
        if (CombatTracker.threatSSW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        placeSWSWS();
        placeSSWS();
        placeWSWS();
    }

private static void placeSWSWS() {
        if (w_m1_m2 >= 0) {
            double score = w_m1_m2;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
private static void placeSSWS() {
        if (w_0_m2 >= 0) {
            double score = w_0_m2;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
private static void placeWSWS() {
        if (w_m1_m1 >= 0) {
            double score = w_m1_m1;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.SOUTH;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
    private static void moveSW() throws GameActionException {
        if (!CombatCommon.canMoveSW) return;
        curMoveLoc = myLoc.add(Direction.SOUTHWEST);
        faceSWSW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceSSW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceWSW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceSWSW() throws GameActionException {
        if (CombatTracker.threatSWSW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        placeSWSWSW();
        placeSSWSW();
        placeWSWSW();
    }

private static void placeSWSWSW() {
        if (w_m2_m2 >= 0) {
            double score = w_m2_m2;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
private static void placeSSWSW() {
        if (w_m1_m2 >= 0) {
            double score = w_m1_m2;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
private static void placeWSWSW() {
        if (w_m2_m1 >= 0) {
            double score = w_m2_m1;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
    private static void faceSSW() throws GameActionException {
        if (CombatTracker.threatSWS != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        placeSSSW();
        placeSESSW();
        placeSWSSW();
    }

private static void placeSSSW() {
        if (w_m1_m2 >= 0) {
            double score = w_m1_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.SOUTH;
            }
        }
    }
private static void placeSESSW() {
        if (w_0_m2 >= 0) {
            double score = w_0_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHEAST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.SOUTH;
            }
        }
    }
private static void placeSWSSW() {
        if (w_m2_m2 >= 0) {
            double score = w_m2_m2;
            if (Direction.SOUTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.SOUTH;
            }
        }
    }
    private static void faceWSW() throws GameActionException {
        if (CombatTracker.threatSWW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        placeWWSW();
        placeNWWSW();
        placeSWWSW();
    }

private static void placeWWSW() {
        if (w_m2_m1 >= 0) {
            double score = w_m2_m1;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.WEST;
            }
        }
    }
private static void placeNWWSW() {
        if (w_m2_0 >= 0) {
            double score = w_m2_0;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.WEST;
            }
        }
    }
private static void placeSWWSW() {
        if (w_m2_m2 >= 0) {
            double score = w_m2_m2;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.SOUTHWEST;
                bestFacing = Direction.WEST;
            }
        }
    }
    private static void moveW() throws GameActionException {
        if (!CombatCommon.canMoveW) return;
        curMoveLoc = myLoc.add(Direction.WEST);
        faceWW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceNWW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceSWW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceWW() throws GameActionException {
        if (CombatTracker.threatWW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.WEST);
        if (moveCost == -1) return;
        placeWWW();
        placeNWWW();
        placeSWWW();
    }

private static void placeWWW() {
        if (w_m2_0 >= 0) {
            double score = w_m2_0;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.WEST;
            }
        }
    }
private static void placeNWWW() {
        if (w_m2_1 >= 0) {
            double score = w_m2_1;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.WEST;
            }
        }
    }
private static void placeSWWW() {
        if (w_m2_m1 >= 0) {
            double score = w_m2_m1;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.WEST;
            }
        }
    }
    private static void faceNWW() throws GameActionException {
        if (CombatTracker.threatWNW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        placeNWNWW();
        placeNNWW();
        placeWNWW();
    }

private static void placeNWNWW() {
        if (w_m2_1 >= 0) {
            double score = w_m2_1;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
private static void placeNNWW() {
        if (w_m1_1 >= 0) {
            double score = w_m1_1;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
private static void placeWNWW() {
        if (w_m2_0 >= 0) {
            double score = w_m2_0;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
    private static void faceSWW() throws GameActionException {
        if (CombatTracker.threatWSW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        placeSWSWW();
        placeSSWW();
        placeWSWW();
    }

private static void placeSWSWW() {
        if (w_m2_m1 >= 0) {
            double score = w_m2_m1;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
private static void placeSSWW() {
        if (w_m1_m1 >= 0) {
            double score = w_m1_m1;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTH);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
private static void placeWSWW() {
        if (w_m2_0 >= 0) {
            double score = w_m2_0;
            if (Direction.SOUTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.WEST;
                bestFacing = Direction.SOUTHWEST;
            }
        }
    }
    private static void moveNW() throws GameActionException {
        if (!CombatCommon.canMoveNW) return;
        curMoveLoc = myLoc.add(Direction.NORTHWEST);
        faceNWNW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceNNW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        faceWNW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
    }

    private static void faceNWNW() throws GameActionException {
        if (CombatTracker.threatNWNW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        placeNWNWNW();
        placeNNWNW();
        placeWNWNW();
    }

private static void placeNWNWNW() {
        if (w_m2_2 >= 0) {
            double score = w_m2_2;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
private static void placeNNWNW() {
        if (w_m1_2 >= 0) {
            double score = w_m1_2;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
private static void placeWNWNW() {
        if (w_m2_1 >= 0) {
            double score = w_m2_1;
            if (Direction.NORTHWEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.NORTHWEST;
            }
        }
    }
    private static void faceNNW() throws GameActionException {
        if (CombatTracker.threatNWN != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        placeNNNW();
        placeNENNW();
        placeNWNNW();
    }

private static void placeNNNW() {
        if (w_m1_2 >= 0) {
            double score = w_m1_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTH);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.NORTH;
            }
        }
    }
private static void placeNENNW() {
        if (w_0_2 >= 0) {
            double score = w_0_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHEAST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.NORTH;
            }
        }
    }
private static void placeNWNNW() {
        if (w_m2_2 >= 0) {
            double score = w_m2_2;
            if (Direction.NORTH != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.NORTH;
            }
        }
    }
    private static void faceWNW() throws GameActionException {
        if (CombatTracker.threatNWW != 0L) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        placeWWNW();
        placeNWWNW();
        placeSWWNW();
    }

private static void placeWWNW() {
        if (w_m2_1 >= 0) {
            double score = w_m2_1;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.WEST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.WEST;
            }
        }
    }
private static void placeNWWNW() {
        if (w_m2_2 >= 0) {
            double score = w_m2_2;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.NORTHWEST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.WEST;
            }
        }
    }
private static void placeSWWNW() {
        if (w_m2_0 >= 0) {
            double score = w_m2_0;
            if (Direction.WEST != myDir) score *= 1.1;
            if (score > best) {
                best = score;
                bestPlaceLoc = curMoveLoc.add(Direction.SOUTHWEST);
                bestMoveDir = Direction.NORTHWEST;
                bestFacing = Direction.WEST;
            }
        }
    }

    
    public static boolean miniRun() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (!shouldTrap()) return false;
        if (weightCompTime != time) {
            clearWeights();
            weightSource = myLoc;
        }
        int x = myLocX;
        int y = myLocY;
        switch (myDir.ordinal()) {
            case 0 -> {
                MapLocation loc0 = new MapLocation(x , y  + 1);
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x  + 1, y  + 1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + -1, y  + 1);
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 1 -> {
                MapLocation loc0 = new MapLocation(x  + 1, y  + 1);
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x , y  + 1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + 1, y );
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 2 -> {
                MapLocation loc0 = new MapLocation(x  + 1, y );
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x  + 1, y  + 1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + 1, y  + -1);
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 3 -> {
                MapLocation loc0 = new MapLocation(x  + 1, y  + -1);
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x , y  + -1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + 1, y );
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 4 -> {
                MapLocation loc0 = new MapLocation(x , y  + -1);
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x  + 1, y  + -1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + -1, y  + -1);
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 5 -> {
                MapLocation loc0 = new MapLocation(x  + -1, y  + -1);
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x , y  + -1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + -1, y );
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 6 -> {
                MapLocation loc0 = new MapLocation(x  + -1, y );
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x  + -1, y  + 1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + -1, y  + -1);
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
            case 7 -> {
                MapLocation loc0 = new MapLocation(x  + -1, y  + 1);
                double s0 = getWeight(loc0.x - weightSource.x, loc0.y - weightSource.y);
                MapLocation loc1 = new MapLocation(x , y  + 1);
                double s1 = getWeight(loc1.x - weightSource.x, loc1.y - weightSource.y);
                MapLocation loc2 = new MapLocation(x  + -1, y );
                double s2 = getWeight(loc2.x - weightSource.x, loc2.y - weightSource.y);
                for (int i = 0; i < RobotTracker.numEnemies && i < 2; i++) {
                    RobotInfo rinfo = RobotTracker.sortedEnemies[i];
                    if (rinfo.location.distanceSquaredTo(myLoc) > 2) break;
                    MapLocation eloc = rinfo.location;
                    {
                        if (eloc.distanceSquaredTo(loc0) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc0));
                            if (ang <= 3) s0 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc1) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc1));
                            if (ang <= 3) s1 += 4 - ang;
                        }
                    }
                    {
                        if (eloc.distanceSquaredTo(loc2) <= 2) {
                            int ang = angleDistance(rinfo.direction, eloc.directionTo(loc2));
                            if (ang <= 3) s2 += 4 - ang;
                        }
                    }
                }
                double bestScore = -1;
                MapLocation bestLoc = null;
                if (rc.canPlaceRatTrap(loc0) && s0 > bestScore) {
                    bestScore = s0;
                    bestLoc = loc0;
                }
                if (rc.canPlaceRatTrap(loc1) && s1 > bestScore) {
                    bestScore = s1;
                    bestLoc = loc1;
                }
                if (rc.canPlaceRatTrap(loc2) && s2 > bestScore) {
                    bestScore = s2;
                    bestLoc = loc2;
                }
                if (bestScore <= 0) return false;
                rc.placeRatTrap(bestLoc);
                println("!!!!!!!!!! placed rat trap MINI");
                indicate("COMBAT STRAT: MINI TRAP");
                return true;
            }
    
            default -> { return false; }
        }
    }
}