package opp_gravy;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

/**
 * Class used to compute / keep track of the symmetry. Only used for the first soldiers spawned from a paint tower.
 */

public class SymmetryManager extends RobotPlayer{

    static final int H = 1, V = 2, R = 4;
    static final int HC = (~H)&7, VC = (~V)&7, RC = (~R)&7;

    static boolean foundH = false, foundR = false, foundV = false;

    static MapLocation base = null;
    static MapLocation bR, bH, bV;

    static MapLocation target;

    static int discardedSyms = 0;

    static MapLocation L;
    static int symX, symY;

    static long w, r;

    static long[] mapWalls = new long[60];
    static long[] mapRuins = new long[60];
    static long[] mapVision = new long[60];

    static void discardH() throws GameActionException{
        // System.out.println("Symmetry is not horizontal");
        discardedSyms |= H;
    }

    static void discardV() throws GameActionException{
        // System.out.println("Symmetry is not vertical");
        discardedSyms |= V;
    }

    static void discardR() throws GameActionException{
        // System.out.println("Symmetry is not rotational");
        discardedSyms |= R;
    }

    public static MapLocation getSymmetricLocation(MapLocation loc, int sym) throws GameActionException {
        if (sym == 0) return new MapLocation(mapWidth - loc.x - 1, loc.y);
        else if (sym == 1) return new MapLocation(loc.x, mapHeight - loc.y - 1);
        else return new MapLocation(mapWidth - loc.x - 1, mapHeight - loc.y - 1);
    }

    public static boolean fetchSymmetry() throws GameActionException {
        updateSymmetryBroken();
        int unbrokenCount = 0;
        int lastUnbroken = -1;
        for (int i = 0; i < 3; i++) {
            if ((discardedSyms & (1<<i)) == 0) {
                unbrokenCount++;
                lastUnbroken = i;
            }
        }
        if (unbrokenCount == 1) {
            symmetry = lastUnbroken;
            return true;
        } else {
            symmetry = -1;
            return false;
        }
    }

    public static boolean hasFoundSymmetryGlobally() throws GameActionException {
        int count = 0;
        for (int i = 0; i < 3; i++) if ((readGlobalArray(63+i, 63+i) == 1)) count++;
        return count == 2;
    }

    private static void updateSymmetryBroken() throws GameActionException {
        if ((discardedSyms & H) == 0 && readGlobalArray(63 + 0, 63 + 0) == 1) discardH();
        if ((discardedSyms & V) == 0 && (readGlobalArray(63 + 1, 63 + 1) == 1)) discardV();
        if ((discardedSyms & R) == 0 && readGlobalArray(63 + 2, 63 + 2) == 1) discardR();
    }

    public static int writeBrokenSymmetry() throws GameActionException {
        int brokenNotWritten = 0;
        if ((discardedSyms & H) > 0 && readGlobalArray(63 + 0, 63 + 0) == 0) brokenNotWritten |= H;
        if ((discardedSyms & V) > 0 && readGlobalArray(63 + 1, 63 + 1) == 0) brokenNotWritten |= V;
        if ((discardedSyms & R) > 0 && readGlobalArray(63 + 2, 63 + 2) == 0) brokenNotWritten |= R;
        return brokenNotWritten;
    }

    static int getSym() throws GameActionException{
        return switch(discardedSyms){
            case HC -> H;
            case VC -> V;
            case RC -> R;
            default -> 0;
        };
    }

    static void checkSym() throws GameActionException{
        if (base == null) return;
        if (Clock.getBytecodesLeft() < 400) return;
        if (getSym() != 0) return;

        MapInfo[] infos = rc.senseNearbyMapInfos();
        for (MapInfo m : infos){
            if (Clock.getBytecodesLeft() < 400) return;
            L = m.getMapLocation();
            mapVision[L.x] |= (1L << L.y);
            w = m.isWall() ? 1L : 0L;
            r = m.hasCheeseMine() ? 1L : 0L;
            mapWalls[L.x] |= (w << L.y);
            mapRuins[L.x] |= (r << L.y);
            symX = mapWidth - L.x - 1;
            symY = mapHeight - L.y - 1;
            if (((mapVision[symX] & (1L << L.y)) != 0) && (((mapWalls[symX] >>> L.y) & 1) != w || ((mapRuins[symX] >>> L.y) & 1) != r)) discardH();
            if (((mapVision[L.x] & (1L << symY)) != 0) && (((mapWalls[L.x] >>> symY) & 1) != w || ((mapRuins[L.x] >>> symY) & 1) != r)) discardV();
            if (((mapVision[symX] & (1L << symY)) != 0) && (((mapWalls[symX] >>> symY) & 1) != w || ((mapRuins[symX] >>> symY) & 1) != r)) discardR();
        }
    }

    static MapLocation getSymmetric(MapLocation loc) throws GameActionException {
        return switch(getSym()){
            case H-> new MapLocation(mapWidth - loc.x - 1, loc.y);
            case V-> new MapLocation(loc.x, mapHeight - loc.y - 1);
            case R-> new MapLocation(mapWidth - loc.x - 1, mapHeight - loc.y - 1);
            default -> null;
        };
    }

    static void setBase(MapLocation loc) throws GameActionException {
        base = loc;
        bR =  new MapLocation(mapWidth - base.x - 1, mapHeight - base.y - 1);
        bV = new MapLocation(base.x, mapHeight - base.y - 1);
        bH = new MapLocation(mapWidth - base.x - 1, base.y);
    }

    static MapLocation getTarget() throws GameActionException {
        if (base == null) return null;
        target = null;
        MapLocation myLoc = rc.getLocation();
        if ((discardedSyms & R) == 0 && !foundR){
            if (myLoc.distanceSquaredTo(bR) < 10) foundR = true;
            else return bR;
        }
        if ((discardedSyms & V) == 0 && !foundV){
            if (myLoc.distanceSquaredTo(bV) < 10) foundV = true;
            else return bV;
        }
        if ((discardedSyms & H) == 0 && !foundH){
            if (myLoc.distanceSquaredTo(bH) < 10) foundH = true;
            else return bH;
        }
        return null;
    }

}