package fuzz_api;

import battlecode.common.*;

// Coverage tally: calls per (method, outcome), printed as one "FZT" line.
final class T {
    static final int OK = 0, NO = 1, GAE = 2, NPE = 3, AIOOBE = 4, IOOBE = 5, RTE = 6, NOUT = 7;
    static final String[] ON = {"ok", "no", "gae", "npe", "aioobe", "ioobe", "rte"};

    static final int getRoundNum = 0, getMapWidth = 1, getMapHeight = 2, isCooperation = 3, getBackstabbingTeam = 4,
            getNumberCatTraps = 5, getNumberRatTraps = 6, getID = 7, getTeam = 8, getLocation = 9,
            getAllPartLocations = 10, getDirection = 11, getHealth = 12, getRawCheese = 13, getGlobalCheese = 14,
            getAllCheese = 15, getDirt = 16, getType = 17, getCarrying = 18, isBeingThrown = 19, isBeingCarried = 20,
            onTheMap = 21, canSenseLocation = 22, isLocationOccupied = 23, canSenseRobotAtLocation = 24,
            senseRobotAtLocation = 25, canSenseRobot = 26, senseRobot = 27, senseNearbyRobots0 = 28,
            senseNearbyRobots1 = 29, senseNearbyRobots2 = 30, senseNearbyRobots3 = 31, sensePassability = 32,
            senseMapInfo = 33, senseNearbyMapInfos0 = 34, senseNearbyMapInfos1 = 35, senseNearbyMapInfos1c = 36,
            senseNearbyMapInfos2 = 37, adjacentLocation = 38, getAllLocationsWithinRadiusSquared = 39,
            isActionReady = 40, getActionCooldownTurns = 41, isMovementReady = 42, isTurningReady = 43,
            getMovementCooldownTurns = 44, getTurningCooldownTurns = 45, canMoveForward = 46, canMove = 47,
            moveForward = 48, move = 49, canTurn0 = 50, canTurn1 = 51, turn = 52, getCurrentRatCost = 53,
            canBuildRat = 54, buildRat = 55, canBecomeRatKing = 56, becomeRatKing = 57, canPlaceDirt = 58,
            placeDirt = 59, canRemoveDirt = 60, removeDirt = 61, canPlaceRatTrap = 62, placeRatTrap = 63,
            canRemoveRatTrap = 64, removeRatTrap = 65, canPlaceCatTrap = 66, placeCatTrap = 67,
            canRemoveCatTrap = 68, removeCatTrap = 69, canPickUpCheese = 70, pickUpCheese1 = 71,
            pickUpCheese2 = 72, canAttack1 = 73, canAttack2 = 74, attack1 = 75, attack2 = 76, squeak = 77,
            readSqueaks = 78, writeSharedArray = 79, readSharedArray = 80, canTransferCheese = 81,
            transferCheese = 82, throwRat = 83, canThrowRat = 84, dropRat = 85, canDropRat = 86, canCarryRat = 87,
            carryRat = 88, disintegrate = 89, resign = 90, setIndicatorString = 91, setIndicatorDot = 92,
            setIndicatorLine = 93, setTimelineMarker = 94,
            // value classes
            ml_add = 95, ml_subtract = 96, ml_translate = 97, ml_dist = 98, ml_bldist = 99, ml_within2 = 100,
            ml_within4 = 101, ml_within5 = 102, ml_adjacent = 103, ml_directionTo = 104, ml_equals = 105,
            ml_hashCode = 106, ml_compareTo = 107, ml_toString = 108, ml_valueOf = 109, dir_rotate = 110,
            dir_opposite = 111, dir_delta = 112, dir_all = 113, dir_fromDelta = 114, dir_orderNum = 115,
            dir_valueOf = 116, ri_get = 117, ri_equals = 118, ri_toString = 119, mi_get = 120, mi_toString = 121,
            msg_get = 122, msg_toString = 123, msg_copy = 124, ut_get = 125, ut_allTypeLocations = 126,
            team_ops = 127, trap_fields = 128, gae_new = 129, dir_orderMutate = 130, behaviour = 131, NM = 132;

    static final String[] M = {"getRoundNum", "getMapWidth", "getMapHeight", "isCooperation",
            "getBackstabbingTeam", "getNumberCatTraps", "getNumberRatTraps", "getID", "getTeam", "getLocation",
            "getAllPartLocations", "getDirection", "getHealth", "getRawCheese", "getGlobalCheese", "getAllCheese",
            "getDirt", "getType", "getCarrying", "isBeingThrown", "isBeingCarried", "onTheMap", "canSenseLocation",
            "isLocationOccupied", "canSenseRobotAtLocation", "senseRobotAtLocation", "canSenseRobot", "senseRobot",
            "senseNearbyRobots()", "senseNearbyRobots(r)", "senseNearbyRobots(r,t)", "senseNearbyRobots(c,r,t)",
            "sensePassability", "senseMapInfo", "senseNearbyMapInfos()", "senseNearbyMapInfos(r)",
            "senseNearbyMapInfos(c)", "senseNearbyMapInfos(c,r)", "adjacentLocation",
            "getAllLocationsWithinRadiusSquared", "isActionReady", "getActionCooldownTurns", "isMovementReady",
            "isTurningReady", "getMovementCooldownTurns", "getTurningCooldownTurns", "canMoveForward", "canMove",
            "moveForward", "move", "canTurn()", "canTurn(d)", "turn", "getCurrentRatCost", "canBuildRat",
            "buildRat", "canBecomeRatKing", "becomeRatKing", "canPlaceDirt", "placeDirt", "canRemoveDirt",
            "removeDirt", "canPlaceRatTrap", "placeRatTrap", "canRemoveRatTrap", "removeRatTrap",
            "canPlaceCatTrap", "placeCatTrap", "canRemoveCatTrap", "removeCatTrap", "canPickUpCheese",
            "pickUpCheese(l)", "pickUpCheese(l,a)", "canAttack(l)", "canAttack(l,c)", "attack(l)", "attack(l,c)",
            "squeak", "readSqueaks", "writeSharedArray", "readSharedArray", "canTransferCheese", "transferCheese",
            "throwRat", "canThrowRat", "dropRat", "canDropRat", "canCarryRat", "carryRat", "disintegrate",
            "resign", "setIndicatorString", "setIndicatorDot", "setIndicatorLine", "setTimelineMarker",
            "MapLocation.add", "MapLocation.subtract", "MapLocation.translate", "MapLocation.distanceSquaredTo",
            "MapLocation.bottomLeftDistanceSquaredTo", "MapLocation.isWithinDistanceSquared(2)",
            "MapLocation.isWithinDistanceSquared(4)", "MapLocation.isWithinDistanceSquared(5)",
            "MapLocation.isAdjacentTo", "MapLocation.directionTo", "MapLocation.equals", "MapLocation.hashCode",
            "MapLocation.compareTo", "MapLocation.toString", "MapLocation.valueOf", "Direction.rotate",
            "Direction.opposite", "Direction.getDelta", "Direction.all/cardinal/values", "Direction.fromDelta",
            "Direction.getDirectionOrderNum", "Direction.valueOf", "RobotInfo.get", "RobotInfo.equals",
            "RobotInfo.toString", "MapInfo.get", "MapInfo.toString", "Message.get", "Message.toString",
            "Message.copy", "UnitType.get", "UnitType.getAllTypeLocations", "Team.ops", "TrapType.fields",
            "GameActionException.new", "Direction.DIRECTION_ORDER.mutate", "behaviour"};

    static final int[] c = new int[NM * NOUT];
    static int lastPrint = -1;

    static void r(int m, int o) {
        c[m * NOUT + o]++;
    }

    static void print(RobotController rc, String why) {
        int round = rc.getRoundNum();
        if (round == lastPrint) return;
        lastPrint = round;
        StringBuilder sb = new StringBuilder(4000);
        sb.append("FZT fuzz_api ").append(rc.getTeam() == Team.A ? 'A' : 'B').append(' ').append(rc.getID())
                .append(" r=").append(round).append(' ').append(why).append(" type=").append(rc.getType())
                .append(" hp=").append(rc.getHealth()).append(" cheese=").append(rc.getGlobalCheese())
                .append(" at=").append(rc.getLocation()).append(" {").append(B.dbg).append('}');
        for (int m = 0; m < NM; m++) {
            int b = m * NOUT;
            boolean any = false;
            for (int o = 0; o < NOUT; o++) if (c[b + o] != 0) { any = true; break; }
            if (!any) continue;
            sb.append(' ').append(M[m]).append('=');
            for (int o = 0; o < NOUT; o++) {
                if (o > 0) sb.append(',');
                sb.append(c[b + o]);
            }
        }
        System.out.println(sb.toString());
    }
}
