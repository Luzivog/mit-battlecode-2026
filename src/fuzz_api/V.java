package fuzz_api;

import battlecode.common.*;

// Probes of the battlecode.common value classes.
final class V {
    static int rotations;

    static void call(int m) throws GameActionException {
        RobotController rc = P.rc;
        P.cur = m;
        MapLocation a = P.loc(), b = P.loc();
        Direction d = P.dir();
        switch (m) {
            case T.ml_add: P.obj(a.add(d)); break;
            case T.ml_subtract: P.obj(a.subtract(d)); break;
            case T.ml_translate: P.obj(a.translate(P.anInt(), P.rnd(5) - 2)); break;
            case T.ml_dist: P.val(a.distanceSquaredTo(b)); break;
            case T.ml_bldist: P.val(Float.floatToIntBits(a.bottomLeftDistanceSquaredTo(b))); break;
            case T.ml_within2: P.ok(a.isWithinDistanceSquared(b, P.radius())); break;
            case T.ml_within4: {
                double th = P.rnd(4) == 0 ? Double.NaN : P.rnd(400) - 20;
                P.ok(a.isWithinDistanceSquared(b, P.radius(), d, th));
                break;
            }
            case T.ml_within5: P.ok(a.isWithinDistanceSquared(b, P.radius(), d, P.rnd(361), P.rnd(2) == 0)); break;
            case T.ml_adjacent: P.ok(a.isAdjacentTo(b)); break;
            case T.ml_directionTo: P.obj(a.directionTo(b)); break;
            case T.ml_equals: P.ok(a.equals(P.rnd(3) == 0 ? (Object) d : b)); break;
            case T.ml_hashCode: P.val(a.hashCode()); break;
            case T.ml_compareTo: P.val(a.compareTo(b)); break;
            case T.ml_toString: {
                String s = a.toString();
                P.val(s.hashCode());
                break;
            }
            case T.ml_valueOf: {
                String s;
                switch (P.rnd(6)) {
                    case 0: s = a.toString(); break;
                    case 1: s = "(" + P.anInt() + "," + P.anInt() + ")"; break;
                    case 2: s = "[1, 2, 3]"; break;
                    case 3: s = "{x, 4}"; break;
                    case 4: s = null; break;
                    default: s = " 7 ,  -9 ";
                }
                P.val(MapLocation.valueOf(s).hashCode());
                break;
            }
            case T.dir_rotate: {
                Direction e = P.rnd(2) == 0 ? d.rotateLeft() : d.rotateRight();
                P.val(e.ordinal());
                break;
            }
            case T.dir_opposite: P.val(d.opposite().ordinal()); break;
            case T.dir_delta: P.val(d.getDeltaX() * 3 + d.getDeltaY() + d.dx * 7 + d.dy * 11); break;
            case T.dir_all: {
                Direction[] x = P.rnd(3) == 0 ? Direction.cardinalDirections()
                        : P.rnd(2) == 0 ? Direction.allDirections() : Direction.values();
                x[0] = Direction.CENTER;
                P.arr(x);
                break;
            }
            case T.dir_fromDelta: P.obj(Direction.fromDelta(P.rnd(5) - 2, P.rnd(5) - 2)); break;
            case T.dir_orderNum: P.val(d.getDirectionOrderNum()); break;
            case T.dir_valueOf: {
                String[] names = {"NORTH", "CENTER", "north", "", null, "SOUTHWEST"};
                P.val(Direction.valueOf(names[P.rnd(names.length)]).ordinal());
                break;
            }
            case T.ri_get: {
                RobotInfo ri = pickInfo();
                if (ri == null) { P.obj(null); break; }
                int v = ri.getID() ^ ri.getHealth() * 3 ^ ri.getTeam().ordinal() * 5 ^ ri.getType().ordinal() * 7
                        ^ ri.getDirection().ordinal() * 11 ^ ri.getChirality() * 13 ^ ri.getRawCheeseAmount() * 17
                        ^ ri.getLocation().hashCode() ^ ri.ID ^ ri.health ^ ri.cheeseAmount ^ ri.chirality;
                RobotInfo c = ri.getCarryingRobot();
                if (c != null) v ^= c.getID() * 31 ^ (c.carryingRobot == null ? 0 : 1);
                P.val(v);
                break;
            }
            case T.ri_equals: {
                RobotInfo x = pickInfo(), y = P.rnd(2) == 0 ? pickInfo() : x;
                P.ok(x != null && x.equals(P.rnd(4) == 0 ? (Object) a : y));
                break;
            }
            case T.ri_toString: {
                RobotInfo x = pickInfo();
                P.val(x == null ? 0 : x.toString().hashCode());
                break;
            }
            case T.mi_get: {
                MapInfo mi = rc.senseMapInfo(P.rnd(2) == 0 ? rc.getLocation() : a);
                RobotInfo f = mi.flyingRobot();
                P.val((mi.isPassable() ? 1 : 0) + (mi.isWall() ? 2 : 0) + (mi.isDirt() ? 4 : 0)
                        + (mi.hasCheeseMine() ? 8 : 0) + mi.getTrap().ordinal() * 16 + mi.getCheeseAmount() * 64
                        + mi.getMapLocation().hashCode() + (f == null ? 0 : f.getID()));
                break;
            }
            case T.mi_toString: P.val(rc.senseMapInfo(a).toString().hashCode()); break;
            case T.msg_get:
            case T.msg_toString:
            case T.msg_copy: {
                Message[] ms = rc.readSqueaks(-1);
                if (ms.length == 0) { P.obj(null); break; }
                Message x = ms[P.rnd(ms.length)];
                if (m == T.msg_get) P.val(x.getBytes() ^ x.getRound() * 31 ^ x.getSenderID() ^ x.getSource().hashCode());
                else if (m == T.msg_toString) P.val(x.toString().hashCode());
                else P.val(x.copy().getBytes());
                break;
            }
            case T.ut_get: {
                UnitType u = UnitType.values()[P.rnd(3)];
                P.val(u.getHealth() + u.getSize() * 3 + u.getVisionRadiusSquared() * 5 + u.getVisionAngle() * 7
                        + u.getActionCooldown() * 11 + u.getMovementCooldown() * 13 + u.getBytecodeLimit()
                        + (u.usesBottomLeftLocationForDistance() ? 1 : 0) + (u.isRobotType() ? 2 : 0)
                        + (u.isThrowableType() ? 4 : 0) + (u.isThrowingType() ? 8 : 0) + (u.isBabyRatType() ? 16 : 0)
                        + (u.isRatKingType() ? 32 : 0) + (u.isCatType() ? 64 : 0) + u.health + u.size
                        + u.visionConeRadiusSquared + u.visionConeAngle + u.actionCooldown + u.movementCooldown
                        + u.bytecodeLimit);
                break;
            }
            case T.ut_allTypeLocations: {
                UnitType u = UnitType.values()[P.rnd(3)];
                P.arr(u.getAllTypeLocations(a));
                break;
            }
            case T.team_ops: {
                Team t = Team.values()[P.rnd(3)];
                P.val(t.opponent().ordinal() * 2 + (t.isPlayer() ? 1 : 0) + Team.valueOf(P.rnd(4) == 0 ? "C" : "B").ordinal());
                break;
            }
            case T.trap_fields: {
                TrapType t = TrapType.values()[P.rnd(3)];
                P.val(t.buildCost + t.damage * 3 + t.stunTime * 5 + t.actionCooldown * 7 + t.maxCount * 11
                        + t.triggerRadiusSquared * 13 + t.name().hashCode());
                break;
            }
            case T.gae_new: {
                GameActionExceptionType ty = GameActionExceptionType.values()[P.rnd(9)];
                GameActionException e = P.rnd(2) == 0 ? new GameActionException(ty, "fuzz " + ty)
                        : new GameActionException(ty, null, new RuntimeException("c"));
                if (P.rnd(2) == 0) throw e;
                P.val(e.getType().ordinal());
                break;
            }
            case T.dir_orderMutate: {
                // Direction.DIRECTION_ORDER is a public static array shared by every robot of both teams.
                Direction[] o = Direction.DIRECTION_ORDER;
                int r = rc.getRoundNum();
                if (rc.getType().isRatKingType() && (r == 777 || r == 1555) && rotations < 2) {
                    Direction t = o[1];
                    for (int i = 1; i < 8; i++) o[i] = o[i + 1];
                    o[8] = t;
                    rotations++;
                }
                P.val(o[1].ordinal() * 9 + o[8].ordinal());
                break;
            }
            default:
                // disintegrate and resign are behaviour-driven only
                R.mix(m);
        }
    }

    static RobotInfo pickInfo() {
        int k = P.rnd(4);
        if (k == 0) return P.rc.getCarrying();
        if (P.near.length == 0) return null;
        return P.near[P.rnd(P.near.length)];
    }
}
