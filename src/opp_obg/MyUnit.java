package opp_obg;

import battlecode.common.*;

import java.util.Random;

public abstract class MyUnit {

    static RobotController rc;
    static int dim1, dim2;
    static int visionRange;
    static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };
    static Random random;

    static Team myTeam;
    static Team opponent;
    static boolean isKing;

    static int carryingTurns = 0;

    Object indicatorString;

    static int creationRound;

    static int health;

    MyUnit(RobotController rc) throws GameActionException {
        MyUnit.rc = rc;
        isKing = rc.getType() == UnitType.RAT_KING;
        random = new Random(rc.getID());
        myTeam = rc.getTeam();
        opponent = rc.getTeam().opponent();
        visionRange = rc.getType().getVisionRadiusSquared();
        dim1 = rc.getMapWidth();
        dim2 = rc.getMapHeight();
        MapData.commMapMax = (dim1*dim2 - 1)/10 + 1;
        MapData.commMap = new int[MapData.commMapMax];
        if (isKing) BFS.initKing();
        //else BFS.initBaby(); //must be after dim1/dim2
        BugNav.init();
        MovementManager.init(); //must be after dim1/dim2
        ExplorationManager.initExploreDir(); //must be called after MovementManager.init();
        creationRound = rc.getRoundNum();
    }

    void startTurn() throws GameActionException {
        MovementManager.movedDir = Direction.CENTER;
        MovementManager.update();
        health = rc.getHealth();
        carryingTurns--;
        if (carryingTurns < 0) carryingTurns = 0;
        MicroManager.roundInit(); //Must be first
        int r = rc.getRoundNum();
        VisionManager.update(); //Must be after MicroManager.roundInit();
        CommunicationManager.update();
        SqueakManager.readSqueaks(r);
        VisionManager.checkFlyingRobots();
        if (r > 0) SqueakManager.readSqueaks(r - 1);
        DirtManager.update();
    }

    abstract void runTurn() throws GameActionException;

    void endTurn() throws GameActionException {
        MovementManager.updateLastTurnLoc();
        SqueakManager.squeak();
        if (MyUnit.isKing && BFS.queue == null && Clock.getBytecodesLeft() > 4000) BFS.initializeQueue();
    }

    MapLocation getAttackLocation(RobotInfo r){
        switch(r.getType()){
            case RAT_KING:
                return r.getLocation().add(r.getLocation().directionTo(MovementManager.myLoc));
            case CAT:
                MapLocation ans = r.getLocation();
                MapLocation ans1 = r.getLocation().add(Direction.NORTH);
                if (ans1.distanceSquaredTo(MovementManager.myLoc) < ans.distanceSquaredTo(MovementManager.myLoc)) ans = ans1;
                MapLocation ans2 = r.getLocation().add(Direction.EAST);
                if (ans2.distanceSquaredTo(MovementManager.myLoc) < ans.distanceSquaredTo(MovementManager.myLoc)) ans = ans2;
                MapLocation ans3 = r.getLocation().add(Direction.NORTHEAST);
                if (ans3.distanceSquaredTo(MovementManager.myLoc) < ans.distanceSquaredTo(MovementManager.myLoc)) ans = ans3;
                return ans;
        }
        return r.getLocation();
    }


    void tryAttack() throws GameActionException {
        if (!rc.isActionReady()) return;
        int atkRange = isKing ? 8 : 2;
        RobotInfo[] robots = rc.senseNearbyRobots(atkRange, opponent);
        RobotInfo bestRobot = null;
        for (RobotInfo r : robots){
            if (isBetterTarget(r, bestRobot)) bestRobot = r;
        }

        if (bestRobot != null){
            MapLocation unitLoc = getAttackLocation(bestRobot);
            if (rc.getCarrying() == null && rc.canCarryRat(unitLoc)){
                rc.carryRat(unitLoc);
                carryingTurns = 10;
                return;
            }
            int extraDmg = 1;
            switch (bestRobot.getHealth()){
                case 11:
                case 21:
                case 22:
                    break;
                case 12:
                case 23:
                case 24:
                    extraDmg = 2;
                    break;
                case 13:
                    extraDmg = 3;
                    break;
                default:
                    if (bestRobot.getHealth() <= 30) extraDmg = 0;
            }
            int otherExtraDmg = switch(bestRobot.getHealth() - rc.getHealth()){
                case 13, 2 -> 3;
                case 12, 1 -> 2;
                case 11, 0 -> 1;
                default -> 0;
            };

            extraDmg = Math.max(extraDmg, otherExtraDmg);


            //if (bestRobot.getHealth() - rc.getHealth() - 10 > 0 && bestRobot.getHealth() - rc.getHealth() - 10 <= 3) extraDmg = Math.max(extraDmg, bestRobot.getHealth() - rc.getHealth() - 10);
            switch(extraDmg){
                case 3:
                    if (rc.canAttack(unitLoc, 9)){
                        rc.attack(unitLoc, 9);
                        rc.setIndicatorDot(unitLoc, 0, 255, 0);
                        return;
                    }
                case 2:
                    if (rc.canAttack(unitLoc, 4)){
                        rc.attack(unitLoc, 4);
                        rc.setIndicatorDot(unitLoc, 0, 255, 0);
                        return;
                    }
                case 1:
                    if (rc.canAttack(unitLoc, 1)){
                        rc.attack(unitLoc, 1);
                        rc.setIndicatorDot(unitLoc, 0, 255, 0);
                        return;
                    }
                default:
                    if (rc.canAttack(unitLoc, 0)){
                        rc.attack(unitLoc, 0);
                        rc.setIndicatorDot(unitLoc, 0, 255, 0);
                        return;
                    }
            }
        }

        robots = rc.senseNearbyRobots(atkRange, Team.NEUTRAL);
        bestRobot = null;
        int bestHP = 0;
        for (RobotInfo r : robots){
            MapLocation unitLoc = getAttackLocation(r);
            if (!rc.canAttack(unitLoc)) continue;
            //if (MovementManager.myLoc.distanceSquaredTo(unitLoc) > atkRange) continue;
            if (bestRobot == null || bestHP > r.getHealth()){
                bestRobot = r;
                bestHP = r.getHealth();
            }
        }

        if (bestRobot != null){
            MapLocation unitLoc = getAttackLocation(bestRobot);
            if (rc.canAttack(unitLoc, 0)){
                rc.attack(unitLoc, 0);
                rc.setIndicatorDot(unitLoc, 0, 255, 0);
            }
        }
    }

    boolean isBetterTarget(RobotInfo A, RobotInfo B) throws GameActionException {
        MapLocation unitLoc = getAttackLocation(A);
        if (!rc.canAttack(unitLoc)) return false;
        if (B == null) return true;
        if (rc.canCarryRat(A.getLocation())){
            if (!rc.canCarryRat(B.getLocation())) return true;
            return A.getHealth() >= B.getHealth();
        }
        if (rc.canCarryRat(B.getLocation())) return false;
        return A.getHealth() < B.getHealth();
    }

    static void pickUpAllCheese() throws GameActionException {
        if (rc.getDirection().ordinal()%2 == 0) {
            MapLocation loc = rc.getLocation().add(rc.getDirection());
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            loc = loc.add(rc.getDirection());
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            loc = rc.getLocation().add(rc.getDirection().rotateLeft());
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            loc = rc.getLocation().add(rc.getDirection().rotateRight());
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
        }
        else {
            MapLocation loc = rc.getLocation().add(rc.getDirection());
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            Direction dir = rc.getDirection().rotateLeft();
            loc = rc.getLocation().add(dir);
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            loc = loc.add(dir);
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            dir = rc.getDirection().rotateRight();
            loc = rc.getLocation().add(dir);
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
            loc = loc.add(dir);
            if (rc.canPickUpCheese(loc)) rc.pickUpCheese(loc);
        }
    }
}
