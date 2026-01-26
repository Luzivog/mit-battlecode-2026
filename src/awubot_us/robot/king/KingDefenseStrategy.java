package awubot_us.robot.king;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import awubot_us.CellTracker;
import awubot_us.Pathfinder;
import awubot_us.comms.GlobalArray;
import awubot_us.comms.Squeaker;
import awubot_us.robot.Strategy;
import static awubot_us.Game.*;
import static awubot_us.util.Util.*;

public class KingDefenseStrategy extends Strategy {
    private static final int MAX_CAT_TRAPS = 4;
    private static final int LURE_SIGNAL_COOLDOWN = 20;
    private static final int OFFENSIVE_LURE_HP = 200;
    private static final int OFFENSIVE_LURE_CHEESE = 150;

    private final ThreatContext context;
    private final KingMiningStrategy miningStrategy;

    private int placedCatTraps = 0;
    private MapLocation[] catTrapLocations = new MapLocation[MAX_CAT_TRAPS];
    private int lastLureSignalRound = 0;
    private int totalLureSignals = 0;

    public KingDefenseStrategy(ThreatContext aContext, KingMiningStrategy aMiningStrategy) {
        context = aContext;
        miningStrategy = aMiningStrategy;
    }

    @Override
    public boolean runTick() throws GameActionException {
        if (time % 50 == 0) {
            System.out.println("[KING] K" + kingId + " t=" + time +
                " hp=" + rc.getHealth() + " cheese=" + rc.getGlobalCheese() +
                " nearestCat=" + context.catDistSq +
                " state=" + context.defenseState);
        }

        switch (context.defenseState) {
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
        return context.defenseState == ThreatContext.DefenseState.EVADING ||
               context.defenseState == ThreatContext.DefenseState.CRITICAL;
    }

    public boolean wantsToFlee() {
        return context.nearestCat != null;
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
        if (context.trackedCatCount > 1 && (context.threatVectorX != 0 || context.threatVectorY != 0)) {
            MapLocation myThreatCenter = new MapLocation(myLoc.x + context.threatVectorX, myLoc.y + context.threatVectorY);
            myAwayDir = myThreatCenter.directionTo(myLoc);
        } else if (context.nearestCat != null) {
            myAwayDir = context.nearestCat.location.directionTo(myLoc);
        } else if (context.nearestEnemyRatKing != null) {
            myAwayDir = context.nearestEnemyRatKing.location.directionTo(myLoc);
        } else if (context.nearestEnemyRat != null) {
            myAwayDir = context.nearestEnemyRat.location.directionTo(myLoc);
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
        if (context.nearestCat == null) return false;

        MapLocation myCatLoc = context.nearestCat.location;
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
        if (placedCatTraps >= MAX_CAT_TRAPS) return false;

        Direction myPriorityDir = Direction.CENTER;
        MapLocation myMineTarget = miningStrategy.getMineTarget();
        MapLocation myCenterTarget = miningStrategy.getCenterTarget();

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
                    catTrapLocations[placedCatTraps] = myTrapLoc;
                    placedCatTraps++;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placeReactiveCatTrap() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.getGlobalCheese() < 10) return false;
        if (placedCatTraps >= MAX_CAT_TRAPS) return false;
        if (context.nearestCat == null) return false;

        MapLocation myCatLoc = context.nearestCat.location;
        Direction myCatToKing = myCatLoc.directionTo(myLoc);
        MapLocation myTrapLoc = myCatLoc.add(myCatToKing).add(myCatToKing);

        if (!isLocationAlreadyTrapped(myTrapLoc) && rc.canPlaceCatTrap(myTrapLoc)) {
            rc.placeCatTrap(myTrapLoc);
            catTrapLocations[placedCatTraps] = myTrapLoc;
            placedCatTraps++;
            return true;
        }
        return false;
    }

    private boolean isLocationAlreadyTrapped(MapLocation aLoc) {
        for (int i = 0; i < placedCatTraps; i++) {
            if (catTrapLocations[i] != null && catTrapLocations[i].equals(aLoc)) {
                return true;
            }
        }
        return false;
    }

    private void signalLureBabyRats() throws GameActionException {
        if (context.nearestCat == null) return;
        if (time - lastLureSignalRound < LURE_SIGNAL_COOLDOWN) return;

        MapLocation myCatLoc = context.nearestCat.location;
        boolean myShouldLure = false;
        String myReason = "";

        if (context.catDistSq <= ThreatContext.CAT_DANGER_DIST_SQ) {
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

        lastLureSignalRound = time;
        totalLureSignals++;
        System.out.println("[KING] K" + kingId + " t=" + time +
            " LURE_SIGNAL reason=" + myReason +
            " hp=" + rc.getHealth() + " cheese=" + rc.getGlobalCheese() +
            " catDist=" + context.catDistSq + " total=" + totalLureSignals);
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
