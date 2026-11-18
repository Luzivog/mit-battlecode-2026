package opp_obg;

import battlecode.common.*;

public class DirtManager {

    static boolean canPassThroughDirt = false;

    static boolean[] hasDirt;

    static boolean[] dirtLocs;
    static boolean[] wallLocs;

    static boolean shouldMine(){
        return MyUnit.rc.getRawCheese() >= 10 || MyUnit.rc.getGlobalCheese() >= 300;
    }

    static boolean dirt (Direction dir){
        if (!MyUnit.isKing) return hasDirt[dir.ordinal()];
        return switch(dir){
            case NORTH ->   !wallLocs[5] && !wallLocs[6] && !wallLocs[7] && (dirtLocs[5] || dirtLocs[6] || dirtLocs[7]);
            case NORTHEAST -> !wallLocs[6] && !wallLocs[7] && !wallLocs[8] && !wallLocs[9] && !wallLocs[10] && (dirtLocs[6] || dirtLocs[7] || dirtLocs[8] || dirtLocs[9] || dirtLocs[10]);
            case EAST -> !wallLocs[9] && !wallLocs[10] && !wallLocs[11] && (dirtLocs[9] || dirtLocs[10] || dirtLocs[11]);
            case SOUTHEAST -> !wallLocs[10] && !wallLocs[11] && !wallLocs[12] && !wallLocs[13] && !wallLocs[14] && (dirtLocs[10] || dirtLocs[11] || dirtLocs[12] || dirtLocs[13] || dirtLocs[14]);
            case SOUTH -> !wallLocs[13] && !wallLocs[14] && !wallLocs[15] && (dirtLocs[13] || dirtLocs[14] || dirtLocs[15]);
            case SOUTHWEST -> !wallLocs[14] && !wallLocs[15] && !wallLocs[0] && !wallLocs[1] && !wallLocs[2] && (dirtLocs[14] || dirtLocs[15] || dirtLocs[0] || dirtLocs[1] || dirtLocs[2]);
            case WEST -> !wallLocs[1] && !wallLocs[2] && !wallLocs[3] && (dirtLocs[1] || dirtLocs[2] || dirtLocs[3]);
            case NORTHWEST -> !wallLocs[2] && !wallLocs[3] && !wallLocs[4] && !wallLocs[5] && !wallLocs[6] && (dirtLocs[2] || dirtLocs[3] || dirtLocs[4] || dirtLocs[5] || dirtLocs[6]);
            default -> false;
        };


    }

    static void update() throws GameActionException {
        //if (BabyRat.miner()) return;
        if (MyUnit.isKing) {
            updateKing();
            return;
        }
        canPassThroughDirt = !BabyRat.miner() && shouldMine();
        MapLocation m;
        hasDirt = new boolean[9];
        for (int i = 0; i < 9; ++i){
            m = MovementManager.myLoc.add(MicroManager.dirs[i]);
            if (MyUnit.rc.canSenseLocation(m) && MyUnit.rc.senseMapInfo(m).isDirt()) hasDirt[i] = true;
        }
    }

    static MapLocation getDirt(Direction dir){
        if (!MyUnit.isKing) return hasDirt[dir.ordinal()] ? MovementManager.myLoc.add(dir) : null;
        switch(dir){
            case Direction.NORTH:
                if (dirtLocs[6]) return MovementManager.myLoc.translate(0, 2);
                if (dirtLocs[5]) return MovementManager.myLoc.translate(-1, 2);
                if (dirtLocs[7]) return MovementManager.myLoc.translate(1, 2);
                return null;
            case Direction.NORTHEAST:
                if (dirtLocs[6]) return MovementManager.myLoc.translate(0, 2);
                if (dirtLocs[10]) return MovementManager.myLoc.translate(2, 0);
                if (dirtLocs[7]) return MovementManager.myLoc.translate(1, 2);
                if (dirtLocs[9]) return MovementManager.myLoc.translate(2, 1);
                if (dirtLocs[8]) return MovementManager.myLoc.translate(2, 2);
                return null;

            case Direction.EAST:
                if (dirtLocs[10]) return MovementManager.myLoc.translate(2, 0);
                if (dirtLocs[9]) return MovementManager.myLoc.translate(2, 1);
                if (dirtLocs[11]) return MovementManager.myLoc.translate(2, -1);
                return null;
            case Direction.SOUTHEAST:
                if (dirtLocs[10]) return MovementManager.myLoc.translate(2, 0);
                if (dirtLocs[14]) return MovementManager.myLoc.translate(0, -2);
                if (dirtLocs[11]) return MovementManager.myLoc.translate(2, -1);
                if (dirtLocs[13]) return MovementManager.myLoc.translate(1, -2);
                if (dirtLocs[12]) return MovementManager.myLoc.translate(2, -2);
                return null;

            case Direction.SOUTH:
                if (dirtLocs[14]) return MovementManager.myLoc.translate(0, -2);
                if (dirtLocs[13]) return MovementManager.myLoc.translate(1, -2);
                if (dirtLocs[15]) return MovementManager.myLoc.translate(-1, -2);
                return null;
            case Direction.SOUTHWEST:
                if (dirtLocs[14]) return MovementManager.myLoc.translate(0, -2);
                if (dirtLocs[2]) return MovementManager.myLoc.translate(-2, 0);
                if (dirtLocs[15]) return MovementManager.myLoc.translate(-1, -2);
                if (dirtLocs[1]) return MovementManager.myLoc.translate(-2, -1);
                if (dirtLocs[0]) return MovementManager.myLoc.translate(-2, -2);
                return null;

            case Direction.WEST:
                if (dirtLocs[2]) return MovementManager.myLoc.translate(-2, 0);
                if (dirtLocs[1]) return MovementManager.myLoc.translate(-2, -1);
                if (dirtLocs[3]) return MovementManager.myLoc.translate(-2, 1);
                return null;
            case Direction.NORTHWEST:
                if (dirtLocs[2]) return MovementManager.myLoc.translate(-2, 0);
                if (dirtLocs[6]) return MovementManager.myLoc.translate(0, 2);
                if (dirtLocs[3]) return MovementManager.myLoc.translate(-2, 1);
                if (dirtLocs[5]) return MovementManager.myLoc.translate(-1, 2);
                if (dirtLocs[4]) return MovementManager.myLoc.translate(-2, 2);
                return null;
        }
        return null;
    }

    static void updateKing() throws GameActionException {
        canPassThroughDirt = false;
        dirtLocs = new boolean [16];
        wallLocs = new boolean [16];
        MapLocation m = MovementManager.myLoc.translate(-2, -2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[0] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[0] = true;
        }
        m = MovementManager.myLoc.translate(-2, -1);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[1] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[1] = true;
        }
        m = MovementManager.myLoc.translate(-2, 0);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[2] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[2] = true;
        }
        m = MovementManager.myLoc.translate(-2, 1);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[3] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[3] = true;
        }
        m = MovementManager.myLoc.translate(-2, 2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[4] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[4] = true;
        }
        m = MovementManager.myLoc.translate(-1, 2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[5] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[5] = true;
        }
        m = MovementManager.myLoc.translate(0, 2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[6] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[6] = true;
        }
        m = MovementManager.myLoc.translate(1, 2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[7] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[7] = true;
        }
        m = MovementManager.myLoc.translate(2, 2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[8] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[8] = true;
        }
        m = MovementManager.myLoc.translate(2, 1);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[9] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[9] = true;
        }
        m = MovementManager.myLoc.translate(2, 0);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[10] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[10] = true;
        }
        m = MovementManager.myLoc.translate(2, -1);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[11] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[11] = true;
        }
        m = MovementManager.myLoc.translate(2, -2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[12] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[12] = true;
        }
        m = MovementManager.myLoc.translate(1, -2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[13] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[13] = true;
        }
        m = MovementManager.myLoc.translate(0, -2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[14] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[14] = true;
        }
        m = MovementManager.myLoc.translate(-1, -2);
        if (MyUnit.rc.canSenseLocation(m)){
            if (MyUnit.rc.senseMapInfo(m).isDirt()) dirtLocs[15] = true;
            else if (MyUnit.rc.senseMapInfo(m).isWall() || MyUnit.rc.senseRobotAtLocation(m) != null) wallLocs[15] = true;
        }
    }

}
