package fuzz_api;

import battlecode.common.*;

// Behaviours: a king that builds rats and stays put, rats with roles (gather, trap field, carrier walk to the
// enemy trap field, thrower, king assembly, wanderer). Shared array: [0..1] home+1, [2] assemble flag,
// [3..4] rally+1, [5] kings formed, [6..7] trap field+1, [8..63] fuzz writes.
final class B {
    static RobotController rc;
    static int W, H, id;
    static MapLocation home, enemyHome, field, enemyField, rally, waypoint;
    static boolean wasRat, escapeTurn;
    static int role, roleUntil = -1, kingSince = -1, stuck;
    static final int GATHER = 0, TRAPPER = 1, CARRIER = 2, THROWER = 3, WANDER = 4, ASSEMBLE = 5;
    static final String[] RN = {"gather", "trapper", "carrier", "thrower", "wander", "assemble"};

    static void init(RobotController r) {
        rc = r;
        W = rc.getMapWidth();
        H = rc.getMapHeight();
        id = rc.getID();
        wasRat = rc.getType().isBabyRatType();
    }

    static int babyRats() {
        return (rc.getCurrentRatCost() - 10) / 10 * 4;
    }

    static int sgn(int v) {
        return v > 0 ? 1 : v < 0 ? -1 : 0;
    }

    static MapLocation clamp(int x, int y) {
        return new MapLocation(Math.max(1, Math.min(W - 2, x)), Math.max(1, Math.min(H - 2, y)));
    }

    static boolean seenEnemyKing;

    static MapLocation fieldOf(MapLocation k) {
        int cx = (W - 1) / 2, cy = (H - 1) / 2;
        return clamp(k.x + 4 * sgn(cx - k.x), k.y + 4 * sgn(cy - k.y));
    }

    static void readHome() throws GameActionException {
        int x = rc.readSharedArray(0), y = rc.readSharedArray(1);
        if (x > 0 && y > 0) home = new MapLocation(x - 1, y - 1);
        if (home == null) home = rc.getLocation();
        if (!seenEnemyKing) enemyHome = new MapLocation(W - 1 - home.x, H - 1 - home.y);
        int fx = rc.readSharedArray(6), fy = rc.readSharedArray(7);
        field = fx > 0 ? new MapLocation(fx - 1, fy - 1) : fieldOf(home);
        if (!seenEnemyKing) enemyField = new MapLocation(W - 1 - field.x, H - 1 - field.y);
        int rx = rc.readSharedArray(3), ry = rc.readSharedArray(4);
        rally = rx > 0 ? new MapLocation(rx - 1, ry - 1) : null;
    }

    // ---------------------------------------------------------------- movement

    static void mv(Direction x) throws GameActionException {
        P.cur = T.move;
        if (rc.getCarrying() == null) {
            if (x == rc.getDirection() && R.next(2) == 0) rc.moveForward();
            else rc.move(x);
            return;
        }
        if (escapeTurn) {
            // no handler anywhere up to run(): the move IndexOutOfBoundsException escapes run()
            rc.move(x);
            return;
        }
        try {
            rc.move(x);
        } catch (IndexOutOfBoundsException e) {
            T.r(T.move, T.IOOBE);
            R.mix(0x89);
        }
    }

    static boolean stepTo(MapLocation t) throws GameActionException {
        if (t == null || !rc.isMovementReady()) return false;
        MapLocation me = rc.getLocation();
        Direction d = me.directionTo(t);
        if (d == Direction.CENTER) return false;
        Direction[] tries = {d, d.rotateLeft(), d.rotateRight(), d.rotateLeft().rotateLeft(),
                d.rotateRight().rotateRight()};
        for (Direction x : tries) {
            if (rc.canMove(x)) {
                if (x != rc.getDirection() && rc.canTurn() && R.next(4) != 0) rc.turn(x);
                mv(x);
                stuck = 0;
                return true;
            }
        }
        stuck++;
        if (rc.canTurn() && R.next(2) == 0) rc.turn(P.dir8());
        P.cur = T.behaviour;
        Direction r = P.dir8();
        if (rc.canMove(r)) {
            mv(r);
            return true;
        }
        return false;
    }

    static void face(MapLocation t) throws GameActionException {
        Direction d = rc.getLocation().directionTo(t);
        if (d != Direction.CENTER && d != rc.getDirection() && rc.canTurn(d)) rc.turn(d);
    }

    // ---------------------------------------------------------------- sensing

    static MapLocation nearestCheese, myKing, cat, enemy, ally, dirt, ownTrap, ownCatTrap;

    static void scan() throws GameActionException {
        MapLocation me = rc.getLocation();
        nearestCheese = dirt = ownTrap = ownCatTrap = null;
        MapInfo[] infos = rc.senseNearbyMapInfos();
        MapLocation[] hot = new MapLocation[infos.length];
        int n = 0, best = 1 << 30;
        for (MapInfo mi : infos) {
            MapLocation l = mi.getMapLocation();
            boolean h = false;
            if (mi.getCheeseAmount() > 0) {
                int dd = me.distanceSquaredTo(l);
                if (dd < best) {
                    best = dd;
                    nearestCheese = l;
                }
                h = true;
            }
            if (mi.isDirt()) {
                dirt = l;
                h = true;
            }
            if (mi.getTrap() == TrapType.RAT_TRAP) {
                ownTrap = l;
                h = true;
            } else if (mi.getTrap() == TrapType.CAT_TRAP) {
                ownCatTrap = l;
                h = true;
            }
            if (mi.isWall() || mi.hasCheeseMine() || mi.flyingRobot() != null) h = true;
            if (h) hot[n++] = l;
        }
        MapLocation[] hh = new MapLocation[n];
        System.arraycopy(hot, 0, hh, 0, n);
        P.hot = hh;
        RobotInfo[] rs = rc.senseNearbyRobots();
        P.near = rs;
        myKing = cat = enemy = ally = null;
        for (RobotInfo ri : rs) {
            if (ri.team == rc.getTeam()) {
                if (ri.type == UnitType.RAT_KING) myKing = ri.location;
                else if (ally == null) ally = ri.location;
            } else if (ri.team == Team.NEUTRAL) {
                cat = ri.location;
            } else {
                if (enemy == null || ri.type == UnitType.RAT_KING) enemy = ri.location;
                if (ri.type == UnitType.RAT_KING && !seenEnemyKing) {
                    seenEnemyKing = true;
                    enemyHome = ri.location;
                    enemyField = fieldOf(enemyHome);
                }
            }
        }
    }

    // ---------------------------------------------------------------- king

    static String dbg = "";

    static void king() throws GameActionException {
        P.calm = true;
        int round = rc.getRoundNum();
        MapLocation me = rc.getLocation();
        if (kingSince < 0) {
            kingSince = round;
            if (!wasRat) {
                rc.writeSharedArray(0, me.x + 1);
                rc.writeSharedArray(1, me.y + 1);
                home = me;
                field = fieldOf(me);
                rc.writeSharedArray(6, field.x + 1);
                rc.writeSharedArray(7, field.y + 1);
                readHome();
                chooseRally();
            } else {
                // a rat that became king ends the assembly
                rc.writeSharedArray(2, 0);
                rc.writeSharedArray(5, Math.min(1023, rc.readSharedArray(5) + 1));
            }
        }
        if (home == null) readHome();
        if (!wasRat && !me.equals(home)) {
            home = me;
            rc.writeSharedArray(0, me.x + 1);
            rc.writeSharedArray(1, me.y + 1);
        }
        scan();
        for (MapLocation l : rc.getAllPartLocations()) {
            if (rc.canPickUpCheese(l)) {
                if (R.next(3) == 0) rc.pickUpCheese(l, 1 + R.next(30));
                else rc.pickUpCheese(l);
            }
        }
        if (enemy != null && rc.canAttack(enemy)) {
            if (R.next(3) == 0 && rc.canAttack(enemy, 4)) rc.attack(enemy, 4);
            else rc.attack(enemy);
        } else if (cat != null && rc.canAttack(cat)) {
            rc.attack(cat);
        }
        int target = round < 400 ? 10 : 8;
        int reserve = round < 600 ? 700 : 1000;
        if (rc.isActionReady() && babyRats() < target && rc.getGlobalCheese() > rc.getCurrentRatCost() + reserve) {
            for (int i = 0; i < 6; i++) {
                MapLocation l = me.translate(R.next(5) - 2, R.next(5) - 2);
                if (rc.canBuildRat(l)) {
                    rc.buildRat(l);
                    T.r(T.buildRat, T.OK);
                    break;
                }
            }
        }
        if (!wasRat) {
            boolean window = (round >= 250 && round < 330) || (round >= 1120 && round < 1195);
            boolean go = window && babyRats() >= 8 && rc.getGlobalCheese() > 250 && rally != null;
            if (rc.readSharedArray(2) != (go ? 1 : 0)) rc.writeSharedArray(2, go ? 1 : 0);
            dbg = "babies=" + babyRats() + " flag=" + rc.readSharedArray(2) + " rally=" + rally;
        }
        // a rare king step (all nine tiles enter the trap check); home follows
        if (round % 150 == 75 && rc.isMovementReady()) {
            Direction d = P.dir();
            if (d != null && rc.canMove(d)) {
                rc.move(d);
                if (!wasRat) {
                    home = rc.getLocation();
                    rc.writeSharedArray(0, home.x + 1);
                    rc.writeSharedArray(1, home.y + 1);
                }
            }
        }
        V.call(T.dir_orderMutate);
        // tiny maps end early; extra kings disintegrate after a while
        if ((W * H < 500 && !wasRat && round >= 100 + id % 150)
                || (wasRat && round - kingSince > 60 + id % 100)) {
            T.r(T.disintegrate, T.OK);
            T.print(rc, "disintegrate");
            rc.disintegrate();
        }
    }

    static void chooseRally() throws GameActionException {
        int sx = sgn((W - 1) / 2 - home.x), sy = sgn((H - 1) / 2 - home.y);
        if (sx == 0) sx = 1;
        if (sy == 0) sy = 1;
        int[][] offs = {{5 * sx, -5 * sy}, {-5 * sx, 5 * sy}, {5 * sx, 0}, {0, 5 * sy}, {-5 * sx, 0}, {0, -5 * sy},
                {4 * sx, -4 * sy}, {-4 * sx, 4 * sy}, {6 * sx, 6 * sy}};
        for (int[] o : offs) {
            MapLocation c = home.translate(o[0], o[1]);
            if (c.x < 2 || c.y < 2 || c.x > W - 3 || c.y > H - 3) continue;
            if (Math.max(Math.abs(c.x - field.x), Math.abs(c.y - field.y)) < 4) continue;
            boolean okk = true;
            for (int dx = -1; dx <= 1 && okk; dx++)
                for (int dy = -1; dy <= 1 && okk; dy++) {
                    MapLocation t = c.translate(dx, dy);
                    if (rc.canSenseLocation(t) && !rc.sensePassability(t)) okk = false;
                    if (rc.canSenseLocation(t) && rc.senseMapInfo(t).hasCheeseMine()) okk = false;
                }
            if (okk) {
                rally = c;
                rc.writeSharedArray(3, c.x + 1);
                rc.writeSharedArray(4, c.y + 1);
                return;
            }
        }
    }

    // ---------------------------------------------------------------- rats

    static void rat() throws GameActionException {
        int round = rc.getRoundNum();
        if (home == null || round % 20 == 0) readHome();
        if (rc.isBeingCarried() || rc.isBeingThrown()) {
            // carried by an ally: keep walking with the carrier afterwards
            role = CARRIER;
            roleUntil = round + 40;
            return;
        }
        if (round >= roleUntil) {
            int k = R.mixOf(id, round / 150) % 100;
            if (round < 150) role = k < 35 ? GATHER : k < 58 ? TRAPPER : k < 72 ? CARRIER : k < 82 ? THROWER : WANDER;
            else role = k < 45 ? GATHER : k < 55 ? TRAPPER : k < 72 ? CARRIER : k < 82 ? THROWER : WANDER;
            roleUntil = (round / 150 + 1) * 150;
        }
        int r = role;
        if (rc.readSharedArray(2) == 1 && rally != null && rc.getCarrying() == null) r = ASSEMBLE;
        P.calm = r == ASSEMBLE;
        dbg = RN[r];
        scan();
        // opportunistic bites on cats and enemies
        if (rc.isActionReady()) {
            if (cat != null && rc.canAttack(cat) && R.next(2) == 0) rc.attack(cat);
            else if (enemy != null && rc.canAttack(enemy) && R.next(3) == 0) rc.attack(enemy, R.next(3));
        }
        // pickup is free and repeatable
        if (nearestCheese != null && rc.canPickUpCheese(nearestCheese)) {
            if (R.next(2) == 0) rc.pickUpCheese(nearestCheese);
            else rc.pickUpCheese(nearestCheese, 1 + R.next(20));
        }
        switch (r) {
            case GATHER: gather(); break;
            case TRAPPER: trapper(); break;
            case CARRIER: carrier(); break;
            case THROWER: thrower(); break;
            case ASSEMBLE: assemble(); break;
            default: wander();
        }
    }

    static void gather() throws GameActionException {
        int raw = rc.getRawCheese();
        if (raw >= 20 || (raw > 0 && nearestCheese == null)) {
            if (myKing != null) {
                face(myKing);
                if (rc.canTransferCheese(myKing, raw)) {
                    rc.transferCheese(myKing, raw);
                    T.r(T.transferCheese, T.OK);
                    return;
                }
                for (Direction d : P.D8) {
                    MapLocation t = myKing.add(d);
                    if (rc.canTransferCheese(t, raw)) {
                        rc.transferCheese(t, raw);
                        T.r(T.transferCheese, T.OK);
                        return;
                    }
                }
            }
            stepTo(home);
            return;
        }
        if (nearestCheese != null) {
            stepTo(nearestCheese);
            return;
        }
        explore();
    }

    static void explore() throws GameActionException {
        int round = rc.getRoundNum();
        if (waypoint == null || round % 40 == id % 40 || rc.getLocation().distanceSquaredTo(waypoint) <= 2
                || stuck > 6) {
            int k = R.mixOf(id, round);
            waypoint = new MapLocation(k % W, (k / W) % H);
            stuck = 0;
        }
        if (!stepTo(waypoint) && rc.canTurn()) rc.turn(P.dir8());
    }

    static void trapper() throws GameActionException {
        MapLocation me = rc.getLocation();
        if (rc.getNumberRatTraps() < 10 && rc.getGlobalCheese() > (rc.getRoundNum() < 200 ? 400 : 1200)) {
            if (me.distanceSquaredTo(field) > 8) {
                stepTo(field);
                return;
            }
            for (int dx = -1; dx <= 1; dx++)
                for (int dy = -1; dy <= 1; dy++) {
                    MapLocation t = field.translate(dx, dy);
                    if (rc.canPlaceRatTrap(t)) {
                        rc.placeRatTrap(t);
                        T.r(T.placeRatTrap, T.OK);
                        return;
                    }
                }
            // look around the field
            Direction d = me.directionTo(field);
            if (d == Direction.CENTER) d = P.dir8();
            if (rc.canTurn()) rc.turn(R.next(2) == 0 ? d : d.rotateLeft().rotateLeft());
            else stepTo(field.add(P.dir8()));
            return;
        }
        // cat traps near cats, removal of stray own traps, dirt work
        if (cat != null && rc.getGlobalCheese() > 1300) {
            for (Direction d : P.D8) {
                MapLocation t = me.add(d);
                if (rc.canPlaceCatTrap(t)) {
                    rc.placeCatTrap(t);
                    T.r(T.placeCatTrap, T.OK);
                    return;
                }
            }
            stepTo(cat);
            return;
        }
        if (ownTrap != null && ownTrap.distanceSquaredTo(field) > 2 && rc.canRemoveRatTrap(ownTrap)) {
            rc.removeRatTrap(ownTrap);
            T.r(T.removeRatTrap, T.OK);
        }
        if (ownCatTrap != null && R.next(3) == 0 && rc.canRemoveCatTrap(ownCatTrap)) rc.removeCatTrap(ownCatTrap);
        dirtWork();
        explore();
    }

    static void dirtWork() throws GameActionException {
        if (dirt != null && rc.getGlobalCheese() > 300 && rc.canRemoveDirt(dirt)) {
            rc.removeDirt(dirt);
            T.r(T.removeDirt, T.OK);
            return;
        }
        if (rc.getDirt() > 0 && R.next(3) == 0) {
            MapLocation t = rc.adjacentLocation(rc.getDirection());
            if (rc.canPlaceDirt(t)) {
                rc.placeDirt(t);
                T.r(T.placeDirt, T.OK);
                return;
            }
        }
        if (dirt != null) stepTo(dirt);
    }

    // Carriers stage next to the enemy trap field, grab an ally, then walk into the field. A carrier killed by a
    // trap drops its ally, whose own trap check shrinks the trigger list under the carrier's loop: rc.move throws
    // IndexOutOfBoundsException (engine 1.2.5 and 1.2.6, RobotControllerImpl.processTrapsAtLocation).
    static void carrier() throws GameActionException {
        MapLocation me = rc.getLocation();
        RobotInfo carrying = rc.getCarrying();
        if (carrying != null) {
            if (me.distanceSquaredTo(enemyField) <= 2 && rc.isActionReady() && R.next(3) == 0) {
                face(enemyHome);
                if (rc.canThrowRat()) {
                    rc.throwRat();
                    T.r(T.throwRat, T.OK);
                    return;
                }
            }
            stepTo(me.equals(enemyField) ? enemyHome : enemyField);
            return;
        }
        // grab an ally in front
        for (Direction d : P.D8) {
            MapLocation t = me.add(d);
            if (rc.canCarryRat(t)) {
                RobotInfo ri = rc.senseRobotAtLocation(t);
                if (ri != null && ri.team == rc.getTeam()) {
                    rc.carryRat(t);
                    T.r(T.carryRat, T.OK);
                    return;
                }
            }
        }
        MapLocation stage = enemyField.translate(4 * sgn(home.x - enemyField.x), 4 * sgn(home.y - enemyField.y));
        if (me.distanceSquaredTo(stage) > 8) {
            stepTo(stage);
            return;
        }
        // at the stage: face a nearby ally, else look around
        if (ally != null && me.distanceSquaredTo(ally) <= 8) {
            if (!me.isAdjacentTo(ally)) stepTo(ally);
            face(ally);
        } else if (rc.canTurn()) {
            rc.turn(rc.getDirection().rotateRight().rotateRight());
        }
    }

    static void thrower() throws GameActionException {
        MapLocation me = rc.getLocation();
        if (rc.getCarrying() != null) {
            if (!rc.isActionReady()) {
                stepTo(cat != null ? cat : waypoint != null ? waypoint : enemyHome);
                return;
            }
            int k = R.next(5);
            if (k == 0) {
                Direction d = P.dir();
                P.cur = T.dropRat;
                rc.dropRat(d);
                T.r(T.dropRat, T.OK);
                return;
            }
            MapLocation tgt = k == 1 && cat != null ? cat : k == 2 && enemy != null ? enemy
                    : k == 3 && P.hot.length > 0 ? P.hot[R.next(P.hot.length)] : me.add(P.dir8());
            face(tgt);
            P.cur = T.throwRat;
            if (rc.canThrowRat() || R.next(4) == 0) rc.throwRat();
            return;
        }
        for (Direction d : P.D8) {
            MapLocation t = me.add(d);
            if (rc.canCarryRat(t)) {
                rc.carryRat(t);
                T.r(T.carryRat, T.OK);
                return;
            }
        }
        MapLocation t = enemy != null ? enemy : ally;
        if (t != null) {
            stepTo(t);
            face(t);
        } else {
            explore();
        }
    }

    static void assemble() throws GameActionException {
        MapLocation me = rc.getLocation();
        if (me.equals(rally)) {
            boolean can = rc.canBecomeRatKing();
            T.r(T.canBecomeRatKing, can ? T.OK : T.NO);
            if (can) {
                rc.becomeRatKing();
                T.r(T.becomeRatKing, T.OK);
                rc.writeSharedArray(2, 0);
            }
            return;
        }
        if (me.isAdjacentTo(rally)) {
            Direction d = me.directionTo(rally);
            if (rc.canMove(d)) rc.move(d);
            return;
        }
        MapLocation best = null;
        int bd = 1 << 30;
        for (Direction d : Direction.allDirections()) {
            MapLocation t = rally.add(d);
            if (rc.canSenseLocation(t) && rc.isLocationOccupied(t)) continue;
            int dd = me.distanceSquaredTo(t);
            if (dd < bd) {
                bd = dd;
                best = t;
            }
        }
        if (best == null) best = rally;
        if (!rc.isMovementReady()) return;
        // greedy with wide detours around the cluster
        Direction d = me.directionTo(best);
        Direction l = d, r = d;
        for (int i = 0; i < 4; i++) {
            if (rc.canMove(l)) {
                rc.move(l);
                return;
            }
            if (rc.canMove(r)) {
                rc.move(r);
                return;
            }
            l = l.rotateLeft();
            r = r.rotateRight();
        }
        if (rc.canTurn()) face(best);
    }

    static void wander() throws GameActionException {
        MapLocation me = rc.getLocation();
        if (cat != null && rc.isActionReady() && R.next(3) == 0) {
            stepTo(cat);
            if (rc.canAttack(cat)) rc.attack(cat);
            return;
        }
        if (enemy != null && R.next(2) == 0) {
            stepTo(enemy);
            if (rc.canAttack(enemy)) rc.attack(enemy);
            return;
        }
        dirtWork();
        if (rc.isMovementReady()) {
            if (R.next(6) == 0) {
                Direction d = P.dir();
                if (d != null && rc.canMove(d)) rc.move(d);
            } else {
                explore();
            }
        }
        if (me.equals(rc.getLocation()) && rc.canTurn() && R.next(2) == 0) rc.turn(P.dir8());
    }
}
