package fuzz_api;

import battlecode.common.*;

// Random API probes: one RobotController (or value class) call per probe, legal and illegal arguments.
final class P {
    static RobotController rc;
    static int W, H;
    static RobotInfo[] near = new RobotInfo[0];
    static MapLocation[] hot = new MapLocation[0];
    static int cur;
    static String lastMsg = "";
    static int thrown;
    static boolean calm;
    static final Direction[] D8 = {Direction.NORTH, Direction.NORTHEAST, Direction.EAST, Direction.SOUTHEAST,
            Direction.SOUTH, Direction.SOUTHWEST, Direction.WEST, Direction.NORTHWEST};
    static final String ODD = "é中🐀\t\n\u0000|\"\\'<&>";

    static int rnd(int n) {
        return R.next(n);
    }

    static Direction dir8() {
        return D8[rnd(8)];
    }

    static Direction dir() {
        int k = rnd(14);
        if (k == 0) return null;
        if (k == 1) return Direction.CENTER;
        if (k == 2) return rc.getDirection();
        if (k == 3) return rc.getDirection().opposite();
        return dir8();
    }

    static MapLocation loc() {
        MapLocation me = rc.getLocation();
        switch (rnd(22)) {
            case 0:
                return null;
            case 1:
                return me;
            case 2:
                return new MapLocation(me.x, me.y);
            case 3:
            case 4:
            case 5:
                return me.add(dir8());
            case 6:
            case 7:
                return me.translate(rnd(7) - 3, rnd(7) - 3);
            case 8:
                return new MapLocation(rnd(W), rnd(H));
            case 9:
                switch (rnd(6)) {
                    case 0: return new MapLocation(-1, me.y);
                    case 1: return new MapLocation(me.x, -1);
                    case 2: return new MapLocation(W, me.y);
                    case 3: return new MapLocation(me.x, H);
                    case 4: return new MapLocation(-1, -1);
                    default: return new MapLocation(W, H);
                }
            case 10:
                switch (rnd(4)) {
                    case 0: return new MapLocation(Integer.MIN_VALUE, Integer.MIN_VALUE);
                    case 1: return new MapLocation(Integer.MAX_VALUE, 0);
                    case 2: return new MapLocation(0, Integer.MAX_VALUE - 1);
                    default: return new MapLocation(Integer.MIN_VALUE + 1, Integer.MAX_VALUE);
                }
            case 11:
            case 12:
            case 13:
                if (near.length > 0) return near[rnd(near.length)].getLocation();
                return me.add(dir8());
            case 14:
                if (near.length > 0) return near[rnd(near.length)].getLocation().add(dir8());
                return me;
            case 15:
            case 16:
                if (hot.length > 0) return hot[rnd(hot.length)];
                return me.add(rc.getDirection());
            case 17:
                return B.home != null ? B.home : me;
            case 18:
            case 19:
                return rc.adjacentLocation(rc.getDirection());
            default:
                return me.translate(rnd(3) - 1, rnd(3) - 1);
        }
    }

    static int anInt() {
        switch (rnd(14)) {
            case 0: return 0;
            case 1: return 1;
            case 2: return -1;
            case 3: return Integer.MIN_VALUE;
            case 4: return Integer.MAX_VALUE;
            case 5: return rnd(10);
            case 6: return rnd(1024);
            case 7: return 1023;
            case 8: return 1024;
            case 9: return 63;
            case 10: return 64;
            case 11: return rnd(5) + 1;
            default: return R.nextInt();
        }
    }

    static int radius() {
        switch (rnd(12)) {
            case 0: return -2;
            case 1: return -1;
            case 2: return 0;
            case 3: return 1;
            case 4: return 2;
            case 5: return 8;
            case 6: return 20;
            case 7: return 25;
            case 8: return 100;
            case 9: return Integer.MAX_VALUE;
            case 10: return Integer.MIN_VALUE;
            default: return rnd(30);
        }
    }

    static Team team() {
        switch (rnd(6)) {
            case 0: return null;
            case 1: return Team.A;
            case 2: return Team.B;
            case 3: return Team.NEUTRAL;
            case 4: return rc.getTeam();
            default: return rc.getTeam().opponent();
        }
    }

    static int robotId() {
        switch (rnd(8)) {
            case 0: return rc.getID();
            case 1: return -1;
            case 2: return 0;
            case 3: return Integer.MAX_VALUE;
            case 4: {
                RobotInfo c = rc.getCarrying();
                return c != null ? c.getID() : 10000 + rnd(200);
            }
            case 5: return 1 + rnd(30);
            default:
                if (near.length > 0) return near[rnd(near.length)].getID();
                return 10000 + rnd(100);
        }
    }

    static String str() {
        switch (rnd(9)) {
            case 0: return null;
            case 1: return "";
            case 2: return ODD;
            case 3: return "x".repeat(300);
            case 4: return ("ab" + ODD).repeat(40);
            case 5: return Integer.toHexString(R.h) + " " + lastMsg;
            case 6: return "😀".repeat(140);
            case 7: return "r" + rc.getRoundNum() + " id" + rc.getID() + " " + rc.getLocation();
            default: return lastMsg;
        }
    }

    static boolean rich(int reserve) {
        return rc.getAllCheese() > reserve;
    }

    static void ok(boolean b) {
        T.r(cur, b ? T.OK : T.NO);
        R.mix(cur * 2 + (b ? 1 : 0));
    }

    static void ok() {
        T.r(cur, T.OK);
        R.mix(cur * 2 + 1);
    }

    static void val(int v) {
        T.r(cur, T.OK);
        R.mix(v);
    }

    static void obj(Object o) {
        T.r(cur, o == null ? T.NO : T.OK);
        R.mix(o == null ? -cur : cur);
    }

    static void arr(Object[] a) {
        T.r(cur, a.length == 0 ? T.NO : T.OK);
        R.mix(a.length * 131 + cur);
    }

    // One call of method m, with its can* partner when it has one.
    static void call(int m) throws GameActionException {
        if (calm && (m == T.move || m == T.moveForward || m == T.carryRat || m == T.throwRat || m == T.dropRat
                || m == T.becomeRatKing) && rnd(16) != 0) {
            m = m == T.move ? T.canMove : T.canMoveForward;
        }
        cur = m;
        MapLocation l;
        Direction d;
        switch (m) {
            case T.getRoundNum: val(rc.getRoundNum()); break;
            case T.getMapWidth: val(rc.getMapWidth()); break;
            case T.getMapHeight: val(rc.getMapHeight()); break;
            case T.isCooperation: ok(rc.isCooperation()); break;
            case T.getBackstabbingTeam: obj(rc.getBackstabbingTeam()); break;
            case T.getNumberCatTraps: val(rc.getNumberCatTraps()); break;
            case T.getNumberRatTraps: val(rc.getNumberRatTraps()); break;
            case T.getID: val(rc.getID()); break;
            case T.getTeam: obj(rc.getTeam()); break;
            case T.getLocation: val(rc.getLocation().hashCode()); break;
            case T.getAllPartLocations: arr(rc.getAllPartLocations()); break;
            case T.getDirection: val(rc.getDirection().ordinal()); break;
            case T.getHealth: val(rc.getHealth()); break;
            case T.getRawCheese: val(rc.getRawCheese()); break;
            case T.getGlobalCheese: val(rc.getGlobalCheese()); break;
            case T.getAllCheese: val(rc.getAllCheese()); break;
            case T.getDirt: val(rc.getDirt()); break;
            case T.getType: val(rc.getType().ordinal()); break;
            case T.getCarrying: obj(rc.getCarrying()); break;
            case T.isBeingThrown: ok(rc.isBeingThrown()); break;
            case T.isBeingCarried: ok(rc.isBeingCarried()); break;
            case T.onTheMap: ok(rc.onTheMap(loc())); break;
            case T.canSenseLocation: ok(rc.canSenseLocation(loc())); break;
            case T.isLocationOccupied: ok(rc.isLocationOccupied(loc())); break;
            case T.canSenseRobotAtLocation: ok(rc.canSenseRobotAtLocation(loc())); break;
            case T.senseRobotAtLocation: {
                RobotInfo ri = rc.senseRobotAtLocation(loc());
                obj(ri);
                if (ri != null) R.mix(ri.getID() ^ ri.getHealth() << 8);
                break;
            }
            case T.canSenseRobot: ok(rc.canSenseRobot(robotId())); break;
            case T.senseRobot: {
                RobotInfo ri = rc.senseRobot(robotId());
                obj(ri);
                R.mix(ri.getLocation().hashCode() ^ ri.getRawCheeseAmount());
                break;
            }
            case T.senseNearbyRobots0: near = rc.senseNearbyRobots(); arr(near); break;
            case T.senseNearbyRobots1: arr(rc.senseNearbyRobots(radius())); break;
            case T.senseNearbyRobots2: arr(rc.senseNearbyRobots(radius(), team())); break;
            case T.senseNearbyRobots3: arr(rc.senseNearbyRobots(loc(), radius(), team())); break;
            case T.sensePassability: ok(rc.sensePassability(loc())); break;
            case T.senseMapInfo: {
                MapInfo mi = rc.senseMapInfo(loc());
                obj(mi);
                R.mix(mi.getCheeseAmount() * 7 + mi.getTrap().ordinal() + (mi.isPassable() ? 100 : 0));
                break;
            }
            case T.senseNearbyMapInfos0: arr(rc.senseNearbyMapInfos()); break;
            case T.senseNearbyMapInfos1: arr(rc.senseNearbyMapInfos(radius())); break;
            case T.senseNearbyMapInfos1c: arr(rc.senseNearbyMapInfos(loc())); break;
            case T.senseNearbyMapInfos2: arr(rc.senseNearbyMapInfos(loc(), radius())); break;
            case T.adjacentLocation: obj(rc.adjacentLocation(dir())); break;
            case T.getAllLocationsWithinRadiusSquared: arr(rc.getAllLocationsWithinRadiusSquared(loc(), radius())); break;
            case T.isActionReady: ok(rc.isActionReady()); break;
            case T.getActionCooldownTurns: val(rc.getActionCooldownTurns()); break;
            case T.isMovementReady: ok(rc.isMovementReady()); break;
            case T.isTurningReady: ok(rc.isTurningReady()); break;
            case T.getMovementCooldownTurns: val(rc.getMovementCooldownTurns()); break;
            case T.getTurningCooldownTurns: val(rc.getTurningCooldownTurns()); break;
            case T.canMoveForward: ok(rc.canMoveForward()); break;
            case T.canMove: ok(rc.canMove(dir())); break;
            case T.moveForward: {
                if (rnd(2) == 0) {
                    cur = T.canMoveForward;
                    boolean b = rc.canMoveForward();
                    ok(b);
                    if (!b) break;
                }
                cur = T.moveForward;
                rc.moveForward();
                ok();
                break;
            }
            case T.move: {
                d = dir();
                if (rnd(2) == 0) {
                    cur = T.canMove;
                    boolean b = rc.canMove(d);
                    ok(b);
                    if (!b) break;
                }
                cur = T.move;
                rc.move(d);
                ok();
                break;
            }
            case T.canTurn0: ok(rc.canTurn()); break;
            case T.canTurn1: ok(rc.canTurn(dir())); break;
            case T.turn: {
                d = dir();
                if (rnd(2) == 0) {
                    cur = T.canTurn1;
                    boolean b = rc.canTurn(d);
                    ok(b);
                    if (!b) break;
                }
                cur = T.turn;
                rc.turn(d);
                ok();
                break;
            }
            case T.getCurrentRatCost: val(rc.getCurrentRatCost()); break;
            case T.canBuildRat: ok(rc.canBuildRat(loc())); break;
            case T.buildRat: {
                l = loc();
                if (!rich(1500) || B.babyRats() > 20) {
                    l = rnd(2) == 0 ? null : new MapLocation(-3, 0);
                }
                cur = T.canBuildRat;
                ok(rc.canBuildRat(l));
                cur = T.buildRat;
                rc.buildRat(l);
                ok();
                break;
            }
            case T.canBecomeRatKing: ok(rc.canBecomeRatKing()); break;
            case T.becomeRatKing: {
                cur = T.canBecomeRatKing;
                ok(rc.canBecomeRatKing());
                cur = T.becomeRatKing;
                rc.becomeRatKing();
                ok();
                break;
            }
            case T.canPlaceDirt: ok(rc.canPlaceDirt(loc())); break;
            case T.placeDirt: {
                l = loc();
                if (rnd(2) == 0) {
                    cur = T.canPlaceDirt;
                    boolean b = rc.canPlaceDirt(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.placeDirt;
                rc.placeDirt(l);
                ok();
                break;
            }
            case T.canRemoveDirt: ok(rc.canRemoveDirt(loc())); break;
            case T.removeDirt: {
                l = loc();
                if (!rich(900)) l = null;
                if (rnd(2) == 0) {
                    cur = T.canRemoveDirt;
                    boolean b = rc.canRemoveDirt(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.removeDirt;
                rc.removeDirt(l);
                ok();
                break;
            }
            case T.canPlaceRatTrap: ok(rc.canPlaceRatTrap(loc())); break;
            case T.placeRatTrap: {
                l = loc();
                if (!rich(1500)) l = new MapLocation(W + 1, 0);
                if (rnd(2) == 0) {
                    cur = T.canPlaceRatTrap;
                    boolean b = rc.canPlaceRatTrap(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.placeRatTrap;
                rc.placeRatTrap(l);
                ok();
                break;
            }
            case T.canRemoveRatTrap: ok(rc.canRemoveRatTrap(loc())); break;
            case T.removeRatTrap: {
                l = loc();
                if (rnd(2) == 0) {
                    cur = T.canRemoveRatTrap;
                    boolean b = rc.canRemoveRatTrap(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.removeRatTrap;
                rc.removeRatTrap(l);
                ok();
                break;
            }
            case T.canPlaceCatTrap: ok(rc.canPlaceCatTrap(loc())); break;
            case T.placeCatTrap: {
                l = loc();
                if (!rich(1300)) l = new MapLocation(-2, -2);
                if (rnd(2) == 0) {
                    cur = T.canPlaceCatTrap;
                    boolean b = rc.canPlaceCatTrap(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.placeCatTrap;
                rc.placeCatTrap(l);
                ok();
                break;
            }
            case T.canRemoveCatTrap: ok(rc.canRemoveCatTrap(loc())); break;
            case T.removeCatTrap: {
                l = loc();
                if (rnd(2) == 0) {
                    cur = T.canRemoveCatTrap;
                    boolean b = rc.canRemoveCatTrap(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.removeCatTrap;
                rc.removeCatTrap(l);
                ok();
                break;
            }
            case T.canPickUpCheese: ok(rc.canPickUpCheese(loc())); break;
            case T.pickUpCheese1: {
                l = loc();
                if (rnd(2) == 0) {
                    cur = T.canPickUpCheese;
                    boolean b = rc.canPickUpCheese(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.pickUpCheese1;
                rc.pickUpCheese(l);
                ok();
                break;
            }
            case T.pickUpCheese2: {
                l = loc();
                int a = rnd(3) == 0 ? anInt() : rnd(6) - 1;
                cur = T.pickUpCheese2;
                rc.pickUpCheese(l, a);
                ok();
                break;
            }
            case T.canAttack1: ok(rc.canAttack(loc())); break;
            case T.canAttack2: ok(rc.canAttack(loc(), anInt())); break;
            case T.attack1: {
                l = loc();
                if (rnd(2) == 0) {
                    cur = T.canAttack1;
                    boolean b = rc.canAttack(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.attack1;
                rc.attack(l);
                ok();
                break;
            }
            case T.attack2: {
                l = loc();
                int c = rnd(3) == 0 ? anInt() : rnd(5);
                if (c > 8 && c <= rc.getAllCheese()) c = rc.getAllCheese() + 1 + rnd(3);
                if (rnd(2) == 0) {
                    cur = T.canAttack2;
                    boolean b = rc.canAttack(l, c);
                    ok(b);
                    if (!b) break;
                }
                cur = T.attack2;
                rc.attack(l, c);
                ok();
                break;
            }
            case T.squeak: ok(rc.squeak(rnd(2) == 0 ? anInt() : R.h)); break;
            case T.readSqueaks: {
                int r = rc.getRoundNum();
                int k = rnd(6);
                Message[] ms = rc.readSqueaks(k == 0 ? -1 : k == 1 ? r : k == 2 ? r - 1 - rnd(6) : k == 3 ? r + 1 : k == 4 ? -2 : anInt());
                arr(ms);
                for (int i = 0; i < ms.length && i < 4; i++) R.mix(ms[i].getBytes() ^ ms[i].getSenderID());
                break;
            }
            case T.writeSharedArray: {
                int i = rnd(3) == 0 ? anInt() : 8 + rnd(56);
                if (i >= 0 && i < 8) i += 8;
                int v = rnd(3) == 0 ? anInt() : (R.h & 1023);
                rc.writeSharedArray(i, v);
                ok();
                break;
            }
            case T.readSharedArray: val(rc.readSharedArray(rnd(3) == 0 ? anInt() : rnd(64))); break;
            case T.canTransferCheese: ok(rc.canTransferCheese(loc(), anInt())); break;
            case T.transferCheese: {
                l = rnd(2) == 0 && B.home != null ? B.home : loc();
                int a = rnd(2) == 0 ? rc.getRawCheese() : anInt();
                if (rnd(2) == 0) {
                    cur = T.canTransferCheese;
                    boolean b = rc.canTransferCheese(l, a);
                    ok(b);
                    if (!b) break;
                }
                cur = T.transferCheese;
                rc.transferCheese(l, a);
                ok();
                break;
            }
            case T.throwRat: {
                if (rnd(2) == 0) {
                    cur = T.canThrowRat;
                    boolean b = rc.canThrowRat();
                    ok(b);
                    if (!b) break;
                }
                cur = T.throwRat;
                rc.throwRat();
                ok();
                break;
            }
            case T.canThrowRat: ok(rc.canThrowRat()); break;
            case T.dropRat: {
                d = dir();
                if (rnd(2) == 0) {
                    cur = T.canDropRat;
                    boolean b = rc.canDropRat(d);
                    ok(b);
                    if (!b) break;
                }
                cur = T.dropRat;
                rc.dropRat(d);
                ok();
                break;
            }
            case T.canDropRat: ok(rc.canDropRat(dir())); break;
            case T.canCarryRat: ok(rc.canCarryRat(loc())); break;
            case T.carryRat: {
                l = loc();
                if (rnd(2) == 0) {
                    cur = T.canCarryRat;
                    boolean b = rc.canCarryRat(l);
                    ok(b);
                    if (!b) break;
                }
                cur = T.carryRat;
                rc.carryRat(l);
                ok();
                break;
            }
            case T.setIndicatorString: rc.setIndicatorString(str()); ok(); break;
            case T.setIndicatorDot: {
                l = loc();
                rc.setIndicatorDot(l, anInt(), rnd(256), rnd(2) == 0 ? 255 : anInt());
                ok();
                break;
            }
            case T.setIndicatorLine: {
                l = loc();
                MapLocation l2 = rnd(3) == 0 ? loc() : rc.getLocation();
                rc.setIndicatorLine(l, l2, rnd(256), anInt(), rnd(256));
                ok();
                break;
            }
            case T.setTimelineMarker: {
                if (rnd(30) != 0) { cur = T.getRoundNum; val(rc.getRoundNum()); break; }
                rc.setTimelineMarker(str(), rnd(256), rnd(256), anInt());
                ok();
                break;
            }
            default:
                V.call(m);
        }
    }

    static void gae(GameActionException e) {
        T.r(cur, T.GAE);
        thrown++;
        String msg = e.getMessage();
        if (msg == null) msg = "null";
        lastMsg = msg;
        R.mix(e.getType().ordinal() * 1000 + cur);
        R.mix(msg.hashCode());
    }

    static final class Wrapped extends RuntimeException {
        Wrapped(GameActionException e) {
            super(e);
        }
    }

    static void viaWrap(int m) {
        try {
            call(m);
        } catch (GameActionException e) {
            throw new Wrapped(e);
        }
    }

    static void viaFinally(int m) throws GameActionException {
        boolean done = false;
        try {
            call(m);
            done = true;
        } finally {
            R.mix(done ? 0x51 : 0x52);
        }
    }

    static void viaMulti(int m) throws GameActionException {
        try {
            call(m);
        } catch (NullPointerException | IllegalArgumentException e) {
            T.r(cur, e instanceof NullPointerException ? T.NPE : T.RTE);
            R.mix(0x6d);
        }
    }

    static void run1(int m) {
        if (RobotPlayer.TRACE) System.out.println("TR " + rc.getID() + " " + rc.getRoundNum() + " m" + m + " h" + R.h + " s" + R.s + " bc" + Clock.getBytecodesLeft());
        try {
            switch (rnd(4)) {
                case 0: call(m); break;
                case 1: viaWrap(m); break;
                case 2: viaFinally(m); break;
                default: viaMulti(m);
            }
        } catch (GameActionException e) {
            gae(e);
        } catch (Wrapped w) {
            gae((GameActionException) w.getCause());
        } catch (NullPointerException e) {
            T.r(cur, T.NPE);
            R.mix(0x3e);
        } catch (ArrayIndexOutOfBoundsException e) {
            // no getMessage(): JIT fast-throw exceptions carry no message
            T.r(cur, T.AIOOBE);
            R.mix(0x4a);
        } catch (IndexOutOfBoundsException e) {
            T.r(cur, T.IOOBE);
            R.mix(0x4b);
        } catch (RuntimeException e) {
            T.r(cur, T.RTE);
            R.mix(0x7f);
        }
    }

    static int pick() {
        int k = rnd(100);
        if (k < 70) return rnd(T.setTimelineMarker + 1);
        return rnd(T.NM);
    }
}
