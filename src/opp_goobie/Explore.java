package opp_goobie;

import battlecode.common.*;

public class Explore {
    static RobotController rc;
    static MapLocation earlyExploreTarget;
    static MapLocation exploreTarget;
    static int targetsExplored = 0;
    static Direction exploreDir = null;
    static MapLocation closestMine = null;
    static int closestMineDist = Constants.INF;

    static final Direction[] directions = Direction.values();

    static void init() throws GameActionException {
        rc = Robot.rc;
        getExploreDir();
    }

    static void getExploreDir() throws GameActionException {
        MapLocation ratKing = CommsArray.getClosestVisibleRatKing(rc.getLocation());
        MapLocation myLoc = rc.getLocation();

        if (ratKing != null) {
            // Bias away from rat king and also consider unexplored areas
            Direction fromKing = ratKing.directionTo(myLoc);

            // If we're close to rat king, strongly prefer moving away
            if (myLoc.distanceSquaredTo(ratKing) < 25) {
                assignExploreDir(fromKing);
                return;
            }

            // Otherwise, pick a direction that's generally away from king but also covers
            // map
            // Prefer directions that haven't been explored much
            Direction best = fromKing;
            int bestScore = -1;

            for (Direction d : new Direction[] { fromKing, fromKing.rotateLeft(), fromKing.rotateRight() }) {
                int score = getExplorationScore(myLoc, d);
                if (score > bestScore) {
                    bestScore = score;
                    best = d;
                }
            }
            assignExploreDir(best);
        } else {
            // No rat king visible - pick direction toward least explored quadrant
            Direction best = directions[(int) (Math.random() * 8)];
            int bestScore = -1;

            for (Direction d : directions) {
                if (d == Direction.CENTER)
                    continue;
                int score = getExplorationScore(myLoc, d);
                if (score > bestScore) {
                    bestScore = score;
                    best = d;
                }
            }
            assignExploreDir(best);
        }
    }

    // Score a direction based on how unexplored it is
    static int getExplorationScore(MapLocation from, Direction dir) {
        int unexploredCount = 0;
        int checkDist = Math.min(15, Math.min(Robot.W, Robot.H) / 3);

        for (int i = 1; i <= checkDist; i++) {
            MapLocation check = from.translate(dir.dx * i, dir.dy * i);
            if (!rc.onTheMap(check))
                break;
            if (!MapData.hasSeen(check)) {
                unexploredCount++;
            }
        }
        return unexploredCount;
    }

    static MapLocation getEarlySymTarget() {
        if (Robot.turnCount == 0 && rc.getRoundNum() < 5) {
            MapLocation symmetryTarget = SymmetryManager.getTarget();
            if (symmetryTarget != null) {
                Debug.addIndicatorString("explore sym: " + symmetryTarget + " ");
                targetsExplored++;
                return symmetryTarget;
            }
        }
        return null;
    }

    static void assignExploreDir(Direction dir) {
        exploreDir = dir;
        int diffX = Constants.INF, diffY = Constants.INF;
        if (dir.dx > 0)
            diffX = Robot.W - rc.getLocation().x - 1;
        else if (dir.dx < 0)
            diffX = rc.getLocation().x;
        if (dir.dy > 0)
            diffY = Robot.H - rc.getLocation().y - 1;
        else if (dir.dy < 0)
            diffY = rc.getLocation().y;
        int diff = diffX;
        if (diffY < diffX)
            diff = diffY;
        exploreTarget = rc.getLocation().translate(diff * dir.dx, diff * dir.dy);
    }

    static void checkDirection() {
        if (!movingOutOfMap(exploreDir))
            return;

        Direction[] possibleDirs = new Direction[8];
        int cont = 0;
        Direction d = exploreDir.rotateLeft().rotateLeft();
        if (!movingOutOfMap(d))
            possibleDirs[cont++] = d;
        d = d.rotateLeft();
        if (!movingOutOfMap(d))
            possibleDirs[cont++] = d;
        d = d.rotateLeft();
        if (!movingOutOfMap(d))
            possibleDirs[cont++] = d;
        d = d.rotateLeft();
        if (!movingOutOfMap(d))
            possibleDirs[cont++] = d;
        d = d.rotateLeft();
        if (!movingOutOfMap(d))
            possibleDirs[cont++] = d;
        if (cont == 0) {
            d = d.rotateLeft();
            if (!movingOutOfMap(d))
                possibleDirs[cont++] = d;
            d = d.rotateLeft();
            d = d.rotateLeft();
            if (!movingOutOfMap(d))
                possibleDirs[cont++] = d;
        }

        int randomDir = (int) (Math.random() * cont);

        assignExploreDir(possibleDirs[randomDir]);

    }

    static MapLocation getExploreTarget() throws GameActionException {
        if (earlyExploreTarget != null) {
            return earlyExploreTarget;
        }

        checkDirection();
        if (exploreTarget == null) {
            if (exploreDir != null) {
                assignExploreDir(exploreDir);
            } else {
                getExploreDir();
            }
            targetsExplored++;
        }
        return exploreTarget;
    }

    // static MapLocation getExploitTarget() {
    // return MapData.getClosestNonBlacklistedCheeseMine(rc.getLocation());
    // }

    static boolean shouldExploitOverExplore() throws GameActionException {
        int round = Robot.rc.getRoundNum();
        boolean notTooEarlyToExploit = round > (Robot.W + Robot.H) * 0.75;
        // boolean haveCheeseMineToExploit =
        // MapData.getClosestNonBlacklistedCheeseMine(rc.getLocation()) != null;
        boolean haveCheeseMineToExploit = MapData.knownCheeseMines.size - 2 >= MapData.exploreBlacklistCheeseMines
                .size();

        // // Periodic re-exploration: every ~150-250 rounds, force some rats to explore
        // int explorationCycle = 200;
        // int explorePhaseLength = 50;
        // boolean inExplorationPhase = (round % explorationCycle) < explorePhaseLength;
        //
        // // Use robot ID to stagger which rats explore during the exploration phase
        // boolean myTurnToExplore = (Robot.rc.getID() % 3) == ((round /
        // explorePhaseLength) % 3);
        //
        // if (inExplorationPhase && myTurnToExplore) {
        // Debug.addIndicatorString("Exploit? false (exploration phase)");
        // return false;
        // }

        boolean exploredEnoughTargets = targetsExplored > 0
                || (SymmetryManager.isSymmetryDetermined() || Robot.rc.getRoundNum() > 1000);
        boolean res = notTooEarlyToExploit && exploredEnoughTargets && haveCheeseMineToExploit;
        Debug.addIndicatorString("Exploit? " + res);
        return res;
    }

    static boolean movingOutOfMap(Direction dir) {
        try {
            MapLocation loc = rc.getLocation().add(dir);
            if (!rc.onTheMap(loc)) {
                return true;
            }
            loc = loc.add(dir);
            if (!rc.onTheMap(loc)) {
                return true;
            }
            loc = loc.add(dir);
            if (!rc.onTheMap(loc)) {
                return true;
            }
            loc = loc.add(dir);
            if (rc.canSenseLocation(loc) && !rc.onTheMap(loc)) {
                return true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    static void updateExploreTarget() {
        if (earlyExploreTarget != null) {
            if (MapData.hasSeen(earlyExploreTarget)) {
                Debug.println("cleared earlyExploreTarget");
                earlyExploreTarget = null;
            }
        }

        if (exploreTarget != null) {
            if (MapData.hasSeen(exploreTarget)) {
                exploreTarget = null;
            }
        }
    }

    static void updateClosestMineTarget() throws GameActionException {
        MapLocation me = Robot.rc.getLocation();
        // int now = Robot.rc.getRoundNum();

        MapLocation[] kings = CommsArray.getRatKings();
        // If we can sense our current target mine, we've arrived — blacklist it
        // TODO: play with <= distance to it vs sensing it
        if (closestMine != null && (me.distanceSquaredTo(closestMine) <= 4 || MapData.hasRatKing(closestMine, kings))) {
            MapData.blacklistCheeseMineForExploration(closestMine);
        }

        closestMine = MapData.getClosestNonBlacklistedCheeseMine(me);
    }
}
