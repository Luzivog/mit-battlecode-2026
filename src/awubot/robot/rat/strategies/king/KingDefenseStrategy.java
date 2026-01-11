package awubot.robot.rat.strategies.king;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import awubot.CellTracker;
import awubot.Pathfinder;
import awubot.robot.Strategy;
import static awubot.Game.*;
import static awubot.util.Util.*;

public class KingDefenseStrategy extends Strategy {
    private static final int MAX_CAT_TRAPS = 4;

    private final ThreatContext theContext;
    private final KingMiningStrategy theMiningStrategy;

    private int thePlacedCatTraps = 0;
    private MapLocation[] theCatTrapLocations = new MapLocation[MAX_CAT_TRAPS];

    public KingDefenseStrategy(ThreatContext aContext, KingMiningStrategy aMiningStrategy) {
        theContext = aContext;
        theMiningStrategy = aMiningStrategy;
    }

    @Override
    public boolean isComplete() throws GameActionException {
        return false;
    }

    @Override
    public void runTick() throws GameActionException {
        switch (theContext.theDefenseState) {
            case CRITICAL:
                executeCritical();
                break;
            case EVADING:
                executeEvading();
                break;
            case ALERT:
                executeAlert();
                break;
            case NORMAL:
            default:
                break;
        }
    }

    public boolean isDefensiveActionTaken() throws GameActionException {
        return theContext.theDefenseState == ThreatContext.DefenseState.EVADING ||
               theContext.theDefenseState == ThreatContext.DefenseState.CRITICAL;
    }

    private void executeCritical() throws GameActionException {
        indicate("DEFENSE: CRITICAL");
        placeReactiveCatTrap();
        signalLureBabyRats();
        evadeFromThreat();
    }

    private void executeEvading() throws GameActionException {
        indicate("DEFENSE: EVADING");
        placeReactiveCatTrap();
        placeDefensiveDirt();
        signalLureBabyRats();
        evadeFromThreat();
    }

    private void executeAlert() throws GameActionException {
        indicate("DEFENSE: ALERT");
        placeProactiveCatTrapMinefield();
        placeDefensiveDirt();
    }

    private boolean evadeFromThreat() throws GameActionException {
        if (!rc.isMovementReady()) return false;

        Direction myAwayDir;
        if (theContext.theTrackedCatCount > 1 && (theContext.theThreatVectorX != 0 || theContext.theThreatVectorY != 0)) {
            MapLocation myThreatCenter = new MapLocation(loc.x + theContext.theThreatVectorX, loc.y + theContext.theThreatVectorY);
            myAwayDir = myThreatCenter.directionTo(loc);
        } else if (theContext.theNearestCat != null) {
            myAwayDir = theContext.theNearestCat.getLocation().directionTo(loc);
        } else if (theContext.theNearestEnemyRatKing != null) {
            myAwayDir = theContext.theNearestEnemyRatKing.getLocation().directionTo(loc);
        } else if (theContext.theNearestEnemyRat != null) {
            myAwayDir = theContext.theNearestEnemyRat.getLocation().directionTo(loc);
        } else {
            return false;
        }

        Direction[] myTryDirs = {myAwayDir, myAwayDir.rotateLeft(), myAwayDir.rotateRight(),
                                myAwayDir.rotateLeft().rotateLeft(),
                                myAwayDir.rotateRight().rotateRight()};

        for (Direction myDir : myTryDirs) {
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
                return true;
            }
        }

        return attemptDigToEscape(myAwayDir);
    }

    private boolean attemptDigToEscape(Direction aPreferredDir) throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.getGlobalCheese() < 10) return false;

        Direction[] myDigDirs = {aPreferredDir, aPreferredDir.rotateLeft(), aPreferredDir.rotateRight(),
                                aPreferredDir.rotateLeft().rotateLeft(),
                                aPreferredDir.rotateRight().rotateRight()};

        for (Direction myDir : myDigDirs) {
            MapLocation myDigTarget = loc.add(myDir);
            if (rc.canSenseLocation(myDigTarget)) {
                MapInfo myInfo = rc.senseMapInfo(myDigTarget);
                if (myInfo.isDirt() && rc.canRemoveDirt(myDigTarget)) {
                    rc.removeDirt(myDigTarget);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeDefensiveDirt() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.getGlobalCheese() < 60) return false;
        if (theContext.theNearestCat == null) return false;

        MapLocation myCatLoc = theContext.theNearestCat.getLocation();
        Direction myTowardKing = myCatLoc.directionTo(loc);

        MapLocation myBaseLoc = loc.add(myTowardKing.opposite()).add(myTowardKing.opposite());
        Direction myPerp = myTowardKing.rotateLeft().rotateLeft();

        for (Direction myD : new Direction[]{myPerp, myPerp.opposite(), Direction.CENTER}) {
            MapLocation myDirtLoc = myBaseLoc.add(myD);
            myDirtLoc = snapTo2x2Corner(myDirtLoc, myCatLoc);

            if (rc.canPlaceDirt(myDirtLoc)) {
                rc.placeDirt(myDirtLoc);
                return true;
            }
        }
        return false;
    }

    private MapLocation snapTo2x2Corner(MapLocation aLoc, MapLocation aCatLoc) {
        int myGridX = (aLoc.x / 2) * 2;
        int myGridY = (aLoc.y / 2) * 2;
        Direction myTowardCat = aLoc.directionTo(aCatLoc);
        int myCornerX = myGridX + (myTowardCat.dx > 0 ? 1 : 0);
        int myCornerY = myGridY + (myTowardCat.dy > 0 ? 1 : 0);
        return new MapLocation(myCornerX, myCornerY);
    }

    private boolean placeProactiveCatTrapMinefield() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.getGlobalCheese() < 200) return false;
        if (thePlacedCatTraps >= MAX_CAT_TRAPS) return false;

        Direction myPriorityDir = Direction.CENTER;
        MapLocation myMineTarget = theMiningStrategy.getMineTarget();
        MapLocation myCenterTarget = theMiningStrategy.getCenterTarget();

        if (myMineTarget != null) {
            myPriorityDir = loc.directionTo(myMineTarget);
        } else if (myCenterTarget != null) {
            myPriorityDir = loc.directionTo(myCenterTarget);
        }

        Direction[] myTrapDirections;
        if (myPriorityDir != Direction.CENTER) {
            myTrapDirections = new Direction[]{
                myPriorityDir,
                myPriorityDir.rotateLeft(),
                myPriorityDir.rotateRight()
            };
        } else {
            myTrapDirections = new Direction[]{
                Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST
            };
        }

        for (int myRing = 2; myRing <= 3; myRing++) {
            for (Direction myDir : myTrapDirections) {
                MapLocation myTrapLoc = loc;
                for (int i = 0; i < myRing; i++) {
                    myTrapLoc = myTrapLoc.add(myDir);
                }

                if (!isLocationAlreadyTrapped(myTrapLoc) && rc.canPlaceCatTrap(myTrapLoc)) {
                    rc.placeCatTrap(myTrapLoc);
                    theCatTrapLocations[thePlacedCatTraps] = myTrapLoc;
                    thePlacedCatTraps++;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeReactiveCatTrap() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.getGlobalCheese() < 10) return false;
        if (thePlacedCatTraps >= MAX_CAT_TRAPS) return false;
        if (theContext.theNearestCat == null) return false;

        MapLocation myCatLoc = theContext.theNearestCat.getLocation();
        Direction myCatToKing = myCatLoc.directionTo(loc);
        MapLocation myTrapLoc = myCatLoc.add(myCatToKing).add(myCatToKing);

        if (!isLocationAlreadyTrapped(myTrapLoc) && rc.canPlaceCatTrap(myTrapLoc)) {
            rc.placeCatTrap(myTrapLoc);
            theCatTrapLocations[thePlacedCatTraps] = myTrapLoc;
            thePlacedCatTraps++;
            return true;
        }
        return false;
    }

    private boolean isLocationAlreadyTrapped(MapLocation aLoc) {
        for (int i = 0; i < thePlacedCatTraps; i++) {
            if (theCatTrapLocations[i] != null && theCatTrapLocations[i].equals(aLoc)) {
                return true;
            }
        }
        return false;
    }

    private void signalLureBabyRats() throws GameActionException {
        if (theContext.theNearestCat == null) return;

        MapLocation myEnemyKingLoc = getEnemyKingLocation();

        int myEncodedCatLoc = encode2x2(theContext.theNearestCat.getLocation());
        int myEncodedEnemyLoc = encode2x2(myEnemyKingLoc);
        int mySqueakContent = (0b11 << 30) | (myEncodedCatLoc << 10) | myEncodedEnemyLoc;
        rc.squeak(mySqueakContent);
    }

    private MapLocation getEnemyKingLocation() {
        int myX = home.x;
        int myY = home.y;

        switch (CellTracker.getSymmetry()) {
            case Rotational:
                return new MapLocation(mapWidth - 1 - myX, mapHeight - 1 - myY);
            case Horizontal:
                return new MapLocation(mapWidth - 1 - myX, myY);
            case Vertical:
                return new MapLocation(myX, mapHeight - 1 - myY);
            default:
                return new MapLocation(mapWidth - 1 - myX, mapHeight - 1 - myY);
        }
    }
}
