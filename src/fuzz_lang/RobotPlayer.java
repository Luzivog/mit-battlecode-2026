package fuzz_lang;

import battlecode.common.*;

// Java/JDK fuzzer: each turn runs language and library features chosen from (id, round); every result is folded
// into a hash that drives movement, squeaks, the shared array and the indicator string. Tally lines "FZT ...".
public class RobotPlayer {
    static RobotController rc;
    static int h = 0x5eed;
    static long rng;
    static int built;
    static MapLocation home;
    static final Direction[] D8 = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

    public static void run(RobotController r) {
        rc = r;
        while (true) {
            try {
                turn();
            } catch (GameActionException e) {
                h = LA.h(h, e.getType().ordinal());
            } catch (RuntimeException e) {
                h = LA.h(h, 0x77);
            }
            if (rc.getRoundNum() % 100 == 0) T.print(rc, "periodic");
            Clock.yield();
        }
    }

    static int next(int n) {
        rng ^= rng << 13;
        rng ^= rng >>> 7;
        rng ^= rng << 17;
        return (int) ((rng >>> 33) % n);
    }

    static void turn() throws GameActionException {
        int round = rc.getRoundNum();
        boolean king = rc.getType().isRatKingType();
        rng = rc.getID() * 0x9E3779B97F4A7C15L + round * 0xC2B2AE3D27D4EB4FL + 1;
        if (king) king(round);
        else rat(round);
        int reserve = king ? 5000 : 4000;
        for (int n = 0; n < 8 && Clock.getBytecodesLeft() > reserve; n++) {
            int f = next(T.NF);
            int s = rc.getID() * 31 + round * 7 + n + (h & 0xff);
            run1(f, s & 0x7fffffff);
        }
        rc.setIndicatorString("h" + Integer.toHexString(h) + " " + LS.last);
        rc.squeak(h);
        if (king) rc.writeSharedArray(round % 62, h & 1023);
        else {
            Direction d = D8[(h >>> 3) & 7];
            if (rc.canMove(d)) rc.move(d);
            else if (rc.canTurn()) rc.turn(d);
        }
    }

    static void run1(int f, int s) {
        int c0 = K.caughtCount;
        T.runs[f]++;
        try {
            h = LA.h(h, feature(f, s));
        } catch (Throwable t) {
            T.odd[f]++;
            h = LA.h(h, 0x99);
        }
        T.caught[f] += K.caughtCount - c0;
    }

    static int feature(int f, int s) {
        switch (f) {
            case 0: return LA.intArith(s);
            case 1: return LA.longArith(s);
            case 2: return LA.divZero(s);
            case 3: return LA.shifts(s);
            case 4: return LA.floatEdge(s);
            case 5: return LA.casts(s);
            case 6: return LA.floatRound(s);
            case 7: return LA.mathFns(s);
            case 8: return LA.strictMath(s);
            case 9: return LA.exactMath(s);
            case 10: return LA.intMethods(s);
            case 11: return LA.longMethods(s);
            case 12: return LA.charOps(s);
            case 13: return LA.random(s);
            case 14: return LS.concat(s);
            case 15: return LS.doubleToString(s);
            case 16: return LS.parse(s);
            case 17: return LS.stringBuilder(s);
            case 18: return LS.stringMethods(s);
            case 19: return LS.format(s);
            case 20: return LS.regex(s);
            case 21: return LC.boxCache(s);
            case 22: return LC.identityHash(s);
            case 23: return LC.hashMapPlain(s);
            case 24: return LC.hashMapString(s);
            case 25: return LC.lists(s);
            case 26: return LC.linkedDeque(s);
            case 27: return LC.priorityQueue(s);
            case 28: return LC.treeMap(s);
            case 29: return LC.sorting(s);
            case 30: return LC.arraysUtil(s);
            case 31: return LC.streams(s);
            case 32: return LC.jdkQuirks(s);
            case 33: return LC.iterErrors(s);
            case 34: return LO.records(s);
            case 35: return LO.enums(s);
            case 36: return LO.patternSwitch(s);
            case 37: return LO.interfaces(s);
            case 38: return LO.innerClasses(s);
            case 39: return LO.generics(s);
            case 40: return LO.varargs(s);
            case 41: return LO.controlFlow(s);
            case 42: return LO.tryFinally(s);
            case 43: return LO.customExceptions(s);
            case 44: return LX.arrays(s);
            case 45: return LX.arraycopy(s);
            case 46: return LX.recursion(s);
            case 47: return LX.staticInit(s);
            case 48: return LX.lambdas(s);
            case 49: return LC.misc(s);
            case 50: return LC.atomics(s);
            default: return LC.bitset(s);
        }
    }

    static void king(int round) throws GameActionException {
        MapLocation me = rc.getLocation();
        if (home == null) {
            home = me;
            rc.writeSharedArray(62, me.x + 1);
            rc.writeSharedArray(63, me.y + 1);
        }
        for (MapLocation l : rc.getAllPartLocations()) if (rc.canPickUpCheese(l)) rc.pickUpCheese(l);
        if (built < 4 || (built < 10 && round % 150 == 0 && rc.getGlobalCheese() > 600)) {
            Direction d = D8[next(8)];
            MapLocation t = me.add(d).add(d);
            if (rc.canBuildRat(t)) {
                rc.buildRat(t);
                built++;
            }
        }
    }

    static void rat(int round) throws GameActionException {
        if (home == null) {
            int x = rc.readSharedArray(62), y = rc.readSharedArray(63);
            if (x > 0) home = new MapLocation(x - 1, y - 1);
        }
        MapLocation me = rc.getLocation();
        if (rc.getRawCheese() >= 15 && home != null) {
            for (Direction d : Direction.allDirections()) {
                MapLocation t = home.add(d);
                if (rc.canTransferCheese(t, rc.getRawCheese())) {
                    rc.transferCheese(t, rc.getRawCheese());
                    return;
                }
            }
            Direction d = me.directionTo(home);
            if (rc.canTurn(d) && rc.getDirection() != d) rc.turn(d);
            if (rc.canMove(d)) rc.move(d);
            return;
        }
        if (round % 3 == rc.getID() % 3) {
            MapInfo[] infos = rc.senseNearbyMapInfos();
            for (MapInfo mi : infos) {
                if (mi.getCheeseAmount() <= 0) continue;
                MapLocation t = mi.getMapLocation();
                if (rc.canPickUpCheese(t)) rc.pickUpCheese(t);
                else {
                    Direction d = me.directionTo(t);
                    if (rc.canMove(d)) rc.move(d);
                }
                return;
            }
        }
    }
}
