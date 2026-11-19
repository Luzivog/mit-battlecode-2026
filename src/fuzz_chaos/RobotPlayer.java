package fuzz_chaos;

import battlecode.common.*;

// Bytecode-limit and robot-death fuzzer. Each robot draws a fate from its id: most live and run chaos acts
// (overruns, huge allocations, odd yields, swallowed errors, debug_ methods); some return from run(), let an
// exception, an AssertionError or an Error escape, call System.exit, disintegrate, or loop forever.
public class RobotPlayer {
    static RobotController rc;
    static int fate, deathRound, h = 0xc4a05;
    static long rng;
    static MapLocation home;
    static int built;
    static final Direction[] D8 = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};

    static final class DeathInit {
        static final int V;

        static {
            V = rc.getID();
            if (V != 0) throw new IllegalStateException("clinit death");
        }
    }

    static int mixOf(int a, int b) {
        int x = a * 0x9E3779B1 ^ b * 0x85EBCA77;
        x ^= x >>> 15;
        x *= 0x2C1B3C6D;
        x ^= x >>> 12;
        return x & 0x7fffffff;
    }

    static int next(int n) {
        rng ^= rng << 13;
        rng ^= rng >>> 7;
        rng ^= rng << 17;
        return (int) ((rng >>> 33) % n);
    }

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        C.rc = r;
        int id = rc.getID();
        int W = rc.getMapWidth(), H = rc.getMapHeight();
        if (rc.getType().isRatKingType()) {
            // the king survives, except on some maps: an escaping exception, a failed assert, or run() returning
            int k = (W + H) % 6;
            fate = k == 2 ? 13 : k == 4 ? 14 : k == 5 ? 12 : 0;
            deathRound = 200 + 100 * (k / 2) + id % 200;
        } else {
            int k = mixOf(id, 99) % 20;
            fate = k < 12 ? 0 : k;
            // some die on their very first turn
            deathRound = rc.getRoundNum() - 5 + mixOf(id, 7) % 125;
        }
        while (true) {
            int round = rc.getRoundNum();
            if (fate == 12 && round >= deathRound) {
                T.runs[T.NA - 1]++;
                T.print(rc, "fate");
                return;
            }
            if (fate != 0 && round >= deathRound) die();
            try {
                turn(round);
            } catch (GameActionException e) {
                h = h * 31 + e.getType().ordinal();
            } catch (RuntimeException e) {
                h = h * 31 + 0x55;
            }
            if (round % 100 == 0) T.print(rc, "periodic");
            Clock.yield();
        }
    }

    static void die() throws GameActionException {
        T.runs[T.NA - 1]++;
        T.print(rc, "fate");
        switch (fate) {
            case 13:
                throw new IllegalStateException("fuzz_chaos uncaught " + rc.getID());
            case 14:
                assert rc.getRoundNum() < 0 : "fuzz_chaos assert " + rc.getID();
                throw new IllegalStateException("assertions disabled");
            case 15:
                System.exit(3);
                break;
            case 16:
                try {
                    rc.disintegrate();
                } finally {
                    // never runs: the RobotDeathException passes the injected handler first
                    rc.squeak(12345);
                }
                break;
            case 17:
                h += DeathInit.V;
                break;
            case 18:
                // never yields again
                int x = 0;
                while (true) x = x * 31 + C.spin(100);
            default:
                try {
                    throw new IllegalStateException("from try");
                } catch (IllegalStateException e) {
                    throw new LinkageError("error from a catch handler");
                }
        }
    }

    static void turn(int round) throws GameActionException {
        boolean king = rc.getType().isRatKingType();
        rng = rc.getID() * 0x9E3779B97F4A7C15L + round * 0xC2B2AE3D27D4EB4FL + 3;
        if (king) king(round);
        else rat(round);
        int acts = king ? (next(3) == 0 ? 1 : 0) : next(3);
        for (int i = 0; i < acts; i++) {
            int a = next(T.NA - 1);
            if (king && (a == 11 || a == 12 || a == 13 || a == 23)) a = 0;
            int s = (rc.getID() * 31 + round * 7 + i + (h & 0xff)) & 0x7fffffff;
            int r0 = rc.getRoundNum();
            T.runs[a]++;
            try {
                h = h * 31 + C.act(a, s);
            } catch (GameActionException e) {
                h = h * 31 + 1;
            }
            if (rc.getRoundNum() != r0) T.crossed[a]++;
        }
        rc.setIndicatorString("h" + Integer.toHexString(h) + " f" + fate + " d" + C.dbgAcc + " bc" + Clock.getBytecodeNum());
        rc.squeak(h);
        if (king) rc.writeSharedArray(round % 60, h & 1023);
    }

    static void king(int round) throws GameActionException {
        MapLocation me = rc.getLocation();
        if (home == null) {
            home = me;
            rc.writeSharedArray(60, me.x + 1);
            rc.writeSharedArray(61, me.y + 1);
        }
        for (MapLocation l : rc.getAllPartLocations()) if (rc.canPickUpCheese(l)) rc.pickUpCheese(l);
        if (round % 12 == 0 && rc.getCurrentRatCost() <= 30 && rc.getGlobalCheese() > 500) {
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
            int x = rc.readSharedArray(60), y = rc.readSharedArray(61);
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
        Direction d = D8[(h >>> 5) & 7];
        if (rc.canMove(d)) rc.move(d);
        else if (rc.canTurn()) rc.turn(D8[next(8)]);
    }
}
