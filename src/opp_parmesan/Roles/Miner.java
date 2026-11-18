package opp_parmesan.Roles;

import battlecode.common.*;
import opp_parmesan.Utils.*;

import java.util.Map;

import static java.lang.Integer.bitCount;
import static java.lang.Integer.max;

public class Miner extends Globals {

    static final Direction[] mineDirections = {
            Direction.CENTER,
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST
    };

    static FastSet mineBlacklist = new FastSet();

    static MapLocation mine = null;
    static MapLocation cheese = null;
    public static boolean returningToKing = false;
    public static MapLocation newKing = null;

    public static MapLocation needNewKing () {
        MapLocation hasMine = null;
        int totalCheese = 0, cheeseSquares = 0;
        for (MapInfo mi: surroundings) {
            if (mi.hasCheeseMine()) hasMine = mi.getMapLocation();
            totalCheese += mi.getCheeseAmount();
            cheeseSquares += mi.getCheeseAmount() > 0 ? 1 : 0;
        }
        if (totalCheese >= newKingCheeseThreshold && cheeseSquares >= newKingSquaresThreshold)
            return hasMine;
        return null;
    }
    
    public static boolean amOccupyingMine(MapLocation loc) {
        return rc.getLocation().isAdjacentTo(loc);
    }

    public static boolean mineIsOccupied(MapLocation loc) throws GameActionException {
        for (Direction dir : mineDirections) {
            // tile in the 3x3 around the cheese mine

            // if its your current location, then you own the mine :)
            if (loc.add(dir).equals(rc.getLocation())) {
                return false;
            }

            if (!rc.canSenseRobotAtLocation(loc.add(dir))) {
                continue;
            }
            return true;
        }
        return false;
    }

    // returns true if a vacant cheese mine is found
    public static boolean findMine() throws GameActionException {
        for (MapInfo info : surroundings) {
            if (info.hasCheeseMine()) {
                MapLocation loc = info.getMapLocation();
                if (mineIsOccupied(loc)) continue;

                mine = loc;
                return true;
            }
        }
        return false;
    }

    // finds a cheese to go and pickup
    public static boolean findCheese() throws GameActionException {
        for (MapInfo info : surroundings) {
            if (info.getCheeseAmount() > 0) {
                cheese = info.getMapLocation();
                return true;
            }
        }
        return false;
    }

    public static boolean checkMineVacancy() throws GameActionException {
        if (mineIsOccupied(mine)) {
            // return to king if i have a respectable amount of cheese
            if (rc.getRawCheese() >= minerSecondaryReturnThreshold || rc.getRawCheese() >= rc.getGlobalCheese()) {
                returningToKing = true;
                returnToKing(false);
            }
            // otherwise ditch mine and become soldier
            else {
                mine = null;
                cheese = null;
                returningToKing = false;
            }
            return false;
        }
        return true;
    }

    public static void returnToKing(boolean urgent) throws GameActionException {
        if (rc.getRawCheese() == 0 && (rc.readSharedArray(60) == 0 || Comms.getNumKings() > 1)) {
            returningToKing = false;
            return;
        }
        rc.setIndicatorString("miner: returning to king");
        Direction dir = rc.getLocation().directionTo(closestKing);
        MapLocation location = closestKing.add(closestKing.directionTo(rc.getLocation()));
        if (rc.canTransferCheese(location, rc.getRawCheese())){
            rc.transferCheese(location, rc.getRawCheese());
            if (newKing != null) {
                Comms.squeakNewKing(newKing);
                newKing = null;
            }
            if (rc.canTurn()) Vision.turn(dir.opposite());
            return;
        }

        if (rc.getLocation().distanceSquaredTo(location) <= 25) {

            if (rc.canTurn()) Vision.turn(dir);
            if (rc.canTransferCheese(location, rc.getRawCheese())) {
                rc.transferCheese(location, rc.getRawCheese());
                if (newKing != null) {
                    Comms.squeakNewKing(newKing);
                    newKing = null;
                }
                return;
            }

        } else {

            if (!urgent && rc.getRawCheese() < 80) {
                for (MapInfo mi : surroundings) {
                    if (mi.getCheeseAmount() > 5 && rc.getLocation().distanceSquaredTo(mi.getMapLocation()) <= 8) {
                        if (rc.getLocation().distanceSquaredTo(mi.getMapLocation()) < 4) {
                            pickUpCheese(mi.getMapLocation());
                            break;
                        }
                        Direction d = rc.getLocation().directionTo(mi.getMapLocation());
                        if (rc.getLocation().add(d).isAdjacentTo(mi.getMapLocation()) && rc.canMove(d)) {
                            rc.move(d);
                            pickUpCheese(mi.getMapLocation());
                            break;
                        }
                        d = d.rotateLeft();
                        if (rc.getLocation().add(d).isAdjacentTo(mi.getMapLocation()) && rc.canMove(d)) {
                            rc.move(d);
                            pickUpCheese(mi.getMapLocation());
                            break;
                        }
                        d = d.rotateRight().rotateRight();
                        if (rc.getLocation().add(d).isAdjacentTo(mi.getMapLocation()) && rc.canMove(d)) {
                            rc.move(d);
                            pickUpCheese(mi.getMapLocation());
                            break;
                        }
                    }
                }
            }

        }


        Navigator.moveTo(closestKing, true);

        location = closestKing.add(closestKing.directionTo(rc.getLocation()));

        if (!urgent) {
            for (Direction d: adjacentDirections) {
                if (rc.canPickUpCheese(rc.getLocation().add(d))) {
                    pickUpCheese(rc.getLocation().add(d));
                }
            }
        }

        if (rc.canTransferCheese(location, rc.getRawCheese())) {
            rc.transferCheese(location, rc.getRawCheese());
            if (newKing != null) {
                Comms.squeakNewKing(newKing);
                newKing = null;
            }
            cheese = null;
            returningToKing = false;
        }
    }

    public static boolean shouldBeMiner() throws GameActionException {
        if (rc.getRawCheese() == 0) returningToKing = false;
        if (rc.getRawCheese() >= minerReturnThreshold || (rc.getRawCheese() >= minerSecondaryReturnThreshold && rc.getLocation().distanceSquaredTo(closestKing) <= secondaryReturnDistance)) returningToKing = true;
        if (mine != null || cheese != null || returningToKing) return true;
        if (findMine() || findCheese()) return true;
        return false;
    }

    static int knownSym = 0;

    public static void run() throws GameActionException {

        if (newKing == null) {
            boolean mightNeedKing = true;
            MapLocation newMine = needNewKing();
            if (newMine != null) {
                for (MapLocation ml : kingPos)
                    if (ml != null && ml.distanceSquaredTo(newMine) <= closeToKing) mightNeedKing = false;
                if (mightNeedKing) {
                    System.out.println("NEED NEW KING!!!");
                    newKing = newMine;
                }
            }
        }

        if (cheese != null) {
            rc.setIndicatorLine(rc.getLocation(), cheese, 0, 255, 0);
        }

        // read the help signals
        if (rc.readSharedArray(60) > 0 && Comms.getNumKings() == 1) {
            returnToKing(true);
            return;
        }


        MapLocation needSacrifice = Comms.getSacrifices();
        if (needSacrifice != null) {
            boolean squeakToo = true;
            if (needSacrifice.x >= 1000) {
                squeakToo = false;
                needSacrifice = new MapLocation(needSacrifice.x-1000, needSacrifice.y-1000);
            }
            Multiking.newKingPos = needSacrifice;
            Multiking.moveTo();
            if (rc.getLocation().distanceSquaredTo(needSacrifice) <= 8 && rc.canBecomeRatKing())
                rc.becomeRatKing();
            if (squeakToo) Comms.squeakForSacrificesToo(needSacrifice);
            return;
        }


        // return to king
        if (returningToKing) {
            returnToKing(false);
            return;
        }

        // choose the closest thing to attack
        if (enemyRobots.length > 0) {
            if (rc.getGlobalCheese() > emergencyReserve && rc.getRawCheese() <= 20) {
                // attack the enemy
                RobotInfo target = null;
                for (RobotInfo robot:enemyRobots) if (robot.getType() == UnitType.BABY_RAT) {
                    if (target == null) target = robot;
                    else if (rc.getLocation().distanceSquaredTo(target.getLocation()) > rc.getLocation().distanceSquaredTo(robot.getLocation())) {
                        target = robot;
                    }
                }
                if (target != null) {
                    Attack.attackBaby();
                    return;
                }
            } else {
                // run to safety with the cheese
                returningToKing = true;
                returnToKing(true);
                if (returningToKing) return;
            }
        }


        // pick up cheese
        if (cheese != null) {
            if (mine != null && !checkMineVacancy()) return;

            rc.setIndicatorString("miner: picking up cheese");

            // check that the cheese is still there
            if (rc.canSenseLocation(cheese) && rc.senseMapInfo(cheese).getCheeseAmount() <= 0) {
                cheese = null;
            }

            // cheese still there
            else {
                Navigator.moveTo(cheese, true);

                if (rc.canPickUpCheese(cheese)) {
                    if (rc.getRawCheese() < maxCheese) pickUpCheese(cheese);

                    cheese = null;

                    // check to return if i have a lot of cheese
                    if (rc.getRawCheese() >= minerReturnThreshold || rc.getRawCheese() >= rc.getGlobalCheese()) {
                        returningToKing = true;
                        returnToKing(false);
                    }

                    // look for next cheese, and turn towards
                    if (findCheese()) {
                        if (rc.canTurn()) {
                            Vision.turn(rc.getLocation().directionTo(cheese));
                        }
                    }
                    // no cheese found, turn towards the mine
                    else {
                        if (rc.canTurn()) {
                            Vision.turn(rc.getLocation().directionTo(mine));
                        }
                    }
                }
                return;
            }
        }

        // move towards the mine, and then circle on the mine, looking for cheese
        if (mine != null) {
            if (!checkMineVacancy()) return;

            rc.setIndicatorString("miner: occupying mine, looking for cheese");

            // try to place a trap at (+1, +1) and (-1, -1)
            // if (rc.canPlaceRatTrap(mine.add(Direction.NORTHEAST))) rc.placeRatTrap(mine.add(Direction.NORTHEAST));
            // if (rc.canPlaceRatTrap(mine.add(Direction.SOUTHWEST))) rc.placeRatTrap(mine.add(Direction.SOUTHWEST));

            // if im at the mine, scan
            if (rc.getLocation().equals(mine)) {
                if (rc.canTurn()) {
                    if (bitCount(rc.getID())%2 == 0)
                        Vision.turn(rc.getDirection().rotateRight().rotateRight());
                    else
                        Vision.turn(rc.getDirection().rotateLeft().rotateLeft());
                }
            }
            // otherwise move towards the mine
            else {
                Navigator.moveTo(mine, Math.max(Math.abs(mine.x-rc.getLocation().x), Math.abs(mine.y-rc.getLocation().y)) > 2);
                if (rc.canTurn()) {
                    Vision.turn(rc.getLocation().directionTo(mine));
                    checkMineVacancy();
                }
            }

            // look for cheese
            findCheese();
        }
    }
}