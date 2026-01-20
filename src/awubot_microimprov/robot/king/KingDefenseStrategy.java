package awubot_microimprov.robot.king;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import awubot_microimprov.CellTracker;
import awubot_microimprov.Pathfinder;
import awubot_microimprov.comms.GlobalArray;
import awubot_microimprov.comms.Squeaker;
import awubot_microimprov.robot.Strategy;
import static awubot_microimprov.Game.*;
import static awubot_microimprov.util.Util.*;

public class KingDefenseStrategy extends Strategy {
    private static final int MAX_CAT_TRAPS = 4;
    private static final int LURE_SIGNAL_COOLDOWN = 20;
    private static final int OFFENSIVE_LURE_HP = 200;
    private static final int OFFENSIVE_LURE_CHEESE = 150;

    private final ThreatContext theContext;
    private final KingMiningStrategy theMiningStrategy;

    private int thePlacedCatTraps = 0;
    private MapLocation[] theCatTrapLocations = new MapLocation[MAX_CAT_TRAPS];
    private int theLastLureSignalRound = 0;
    private int theTotalLureSignals = 0;

    public KingDefenseStrategy(ThreatContext aContext, KingMiningStrategy aMiningStrategy) {
        theContext = aContext;
        theMiningStrategy = aMiningStrategy;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if (time % 50 == 0) {
            System.out.println("[KING] K" + kingId + " t=" + time +
                " hp=" + rc.getHealth() + " cheese=" + rc.getGlobalCheese() +
                " nearestCat=" + theContext.theCatDistSq +
                " state=" + theContext.theDefenseState);
        }

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
        return true;
    }

    public boolean isDefensiveActionTaken() throws GameActionException {
        return theContext.theDefenseState == ThreatContext.DefenseState.EVADING ||
               theContext.theDefenseState == ThreatContext.DefenseState.CRITICAL;
    }

    public boolean wantsToFlee() {
        return theContext.theNearestCat != null;
    }

    private void executeCritical() throws GameActionException {
        indicate("DEFENSE: CRITICAL");
        evadeFromThreat();
        signalLureBabyRats();
    }

    private void executeEvading() throws GameActionException {
        indicate("DEFENSE: EVADING");
        evadeFromThreat();
        signalLureBabyRats();
    }

    private void executeAlert() throws GameActionException {
        indicate("DEFENSE: ALERT");
        evadeFromThreat();
    }

    private boolean evadeFromThreat() throws GameActionException {
        if (!rc.isMovementReady()) return false;

        Direction myAwayDir;
        if (theContext.theTrackedCatCount > 1 && (theContext.theThreatVectorX != 0 || theContext.theThreatVectorY != 0)) {
            MapLocation myThreatCenter = new MapLocation(myLoc.x + theContext.theThreatVectorX, myLoc.y + theContext.theThreatVectorY);
            myAwayDir = myThreatCenter.directionTo(myLoc);
        } else if (theContext.theNearestCat != null) {
            myAwayDir = theContext.theNearestCat.location.directionTo(myLoc);
        } else if (theContext.theNearestEnemyRatKing != null) {
            myAwayDir = theContext.theNearestEnemyRatKing.location.directionTo(myLoc);
        } else if (theContext.theNearestEnemyRat != null) {
            myAwayDir = theContext.theNearestEnemyRat.location.directionTo(myLoc);
        } else {
            return moveTowardSafety();
        }

        Direction[] myPreferredDirs = {myAwayDir, myAwayDir.rotateLeft(), myAwayDir.rotateRight(),
                                myAwayDir.rotateLeft().rotateLeft(),
                                myAwayDir.rotateRight().rotateRight()};

        for (Direction myDir : myPreferredDirs) {
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
                return true;
            }
        }

        Direction[] myDesperateDirs = {myAwayDir.opposite().rotateLeft(),
                                       myAwayDir.opposite().rotateRight(),
                                       myAwayDir.opposite()};

        for (Direction myDir : myDesperateDirs) {
            if (rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
                return true;
            }
        }

        return attemptDigToEscape(myAwayDir);
    }

    private boolean moveTowardSafety() throws GameActionException {
        Direction myTowardHome = myLoc.directionTo(GlobalArray.nearestRatKing);
        Direction[] myTryDirs = {myTowardHome, myTowardHome.rotateLeft(), myTowardHome.rotateRight(),
                                myTowardHome.rotateLeft().rotateLeft(), myTowardHome.rotateRight().rotateRight(),
                                myTowardHome.opposite().rotateLeft(), myTowardHome.opposite().rotateRight(),
                                myTowardHome.opposite()};

        for (Direction myDir : myTryDirs) {
            if (myDir != Direction.CENTER && rc.canMove(myDir)) {
                Pathfinder.makeMove(myDir);
                return true;
            }
        }
        return false;
    }

    private boolean attemptDigToEscape(Direction aPreferredDir) throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.getGlobalCheese() < 10) return false;

        Direction[] myDigDirs = {aPreferredDir, aPreferredDir.rotateLeft(), aPreferredDir.rotateRight(),
                                aPreferredDir.rotateLeft().rotateLeft(),
                                aPreferredDir.rotateRight().rotateRight()};

        for (Direction myDir : myDigDirs) {
            MapLocation myDigTarget = myLoc.add(myDir);
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

        MapLocation myCatLoc = theContext.theNearestCat.location;
        Direction myTowardKing = myCatLoc.directionTo(myLoc);

        MapLocation myBaseLoc = myLoc.add(myTowardKing.opposite()).add(myTowardKing.opposite());
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
            myPriorityDir = myLoc.directionTo(myMineTarget);
        } else if (myCenterTarget != null) {
            myPriorityDir = myLoc.directionTo(myCenterTarget);
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
                MapLocation myTrapLoc = myLoc;
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

        MapLocation myCatLoc = theContext.theNearestCat.location;
        Direction myCatToKing = myCatLoc.directionTo(myLoc);
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
        if (time - theLastLureSignalRound < LURE_SIGNAL_COOLDOWN) return;

        MapLocation myCatLoc = theContext.theNearestCat.location;
        boolean myShouldLure = false;
        String myReason = "";

        if (theContext.theCatDistSq <= ThreatContext.CAT_DANGER_DIST_SQ) {
            myShouldLure = true;
            myReason = "defensive";
        }

        if (rc.getHealth() >= OFFENSIVE_LURE_HP && rc.getGlobalCheese() >= OFFENSIVE_LURE_CHEESE) {
            myShouldLure = true;
            myReason = "offensive";
        }

        MapLocation myMapCenter = new MapLocation(mapWidth / 2, mapHeight / 2);
        if (myCatLoc.distanceSquaredTo(myMapCenter) <= 100) {
            myShouldLure = true;
            myReason = "center_cat";
        }

        if (!myShouldLure) return;

        MapLocation myEnemyKingLoc = getEnemyKingLocation();
        Squeaker.squeakLureSignal(myCatLoc, myEnemyKingLoc);

        theLastLureSignalRound = time;
        theTotalLureSignals++;
        System.out.println("[KING] K" + kingId + " t=" + time +
            " LURE_SIGNAL reason=" + myReason +
            " hp=" + rc.getHealth() + " cheese=" + rc.getGlobalCheese() +
            " catDist=" + theContext.theCatDistSq + " total=" + theTotalLureSignals);
    }

    private MapLocation getEnemyKingLocation() {
        int myX = spawn.x;
        int myY = spawn.y;

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
