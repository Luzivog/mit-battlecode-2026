package opp_parmesan.Utils;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import opp_parmesan.Utils.Globals;
import opp_parmesan.Utils.Navigator;
import opp_parmesan.Utils.Vision;

public class Movement extends Globals {

    static MapLocation targetLoc = null;
    static MapLocation[] explorePoints;
    static int epSize = 0;
    static int turnsStuck = 0;
    static int bestDist;

    public static void indicateAllExplorePoints() throws GameActionException {
        for (MapLocation loc : explorePoints) {
            rc.setIndicatorDot(loc, 100, 255, 255);
        }
    }

    public static void setNextExplorePoint() throws GameActionException {
        targetLoc = explorePoints[rng.nextInt(epSize)];
        turnsStuck = 0;
        bestDist = rc.getLocation().distanceSquaredTo(targetLoc);
    }

    public static void explore() throws GameActionException {
        // if theres no target, or reached target, or see target but unreachable
        if (targetLoc == null || rc.getLocation().equals(targetLoc) || turnsStuck > 14 || (opp_parmesan.Utils.Vision.hasSeenLocation(targetLoc) && !Vision.sensePassability(targetLoc))) {
            setNextExplorePoint();
        }

        opp_parmesan.Utils.Navigator.moveTo(targetLoc, true);

        int newDist = rc.getLocation().distanceSquaredTo(targetLoc);
        if (newDist < bestDist) {
            bestDist = newDist;
            turnsStuck = 0;
        } else turnsStuck++;
    }

    public static void init() {
        explorePoints = new MapLocation[144];

        for (int i = mapWidth / 15; i < mapWidth / 2; i += (i + 6) / 2) {
            for (int j = mapHeight / 15; j < mapHeight / 2; j += (j + 6) / 2) {
                explorePoints[epSize++] = new MapLocation(i, j);
                explorePoints[epSize++] = new MapLocation(mapWidth - i - 1, j);
                explorePoints[epSize++] = new MapLocation(i, mapHeight - j - 1);
                explorePoints[epSize++] = new MapLocation(mapWidth - i - 1, mapHeight - j - 1);
            }
        }
    }

    public static void exploreSafe() throws GameActionException {

    }

    public static void circle (MapLocation loc) throws GameActionException {
        if (!rc.isMovementReady()) return;
        MapLocation target = loc.add(rc.getLocation().directionTo(loc).rotateLeft());
        if (!rc.onTheMap(target)) target = loc;
        Navigator.moveTo(target, false);
    }

}