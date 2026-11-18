package opp_obg;

import battlecode.common.*;

public class RatKing extends MyUnit {

    int[] buildDirCount = new int[8];
    double[] dists = new double[8];
    static MapLocation bestMine = null;
    static int distCenterToBestMine;

    static int mineIndex = 0;

    static int babyRats = 0;

    static boolean micro;

    static int roundPlaced;

    static final int SAVED_CHEESE = 2100;

    static final int MAX_DIST_SETTLE = 8;

    static final int MIN_DIST_NEW_KING = 300;

    static final int MIN_ROUND_SECOND_KING = 350;

    static final int MIN_DIST_NEW_KING_REMOVE = 170;

    static int internalID;

    RatKing(RobotController rc) throws GameActionException {
        super(rc);
        internalID = CommunicationManager.updateIndex();
        MapLocation posTarget = CommunicationManager.getKingTarget();
        if (posTarget != null){
            CommunicationManager.setMyTarget(posTarget);
            CommunicationManager.removeKingTarget();
        }
        doDirs();
        LocalBFS.init();
    }

    void startTurn() throws GameActionException{
        super.startTurn();
    }

    void runTurn() throws GameActionException{
        buildEmergencyRats();
        tryAttack();
        micro = MicroManager.doMicro();
        findBestMine();
        setNewKingTarget();
        MapLocation newKing = CommunicationManager.getKingTarget();
        if (newKing != null){
            rc.setIndicatorLine(rc.getLocation(), newKing, 0, 255, 0);
        }

        move();
        spawnRats();
        tryAttack();
        buildEmergencyRats();
        tryPlaceDirt();
    }

    void move() throws GameActionException {
        if (micro){
            return;
        }

        MapLocation target = getTarget();
        if (target != null){
            Pathfinder.moveTo(target);
        }
    }

    MapLocation getTarget(){
        if (bestMine != null) return bestMine;
        return ExplorationManager.getExplore3Target();
    }

    int getMaxBabyCost(){
        return switch(MapData.totalCheese){
            case 0,1,2,3,4 -> 30;
            default -> 40;
        };
    }

    void findBestMine() throws GameActionException {
        MapLocation commTarget = CommunicationManager.getSelfTargetKing();
        if (commTarget != null) {
            bestMine = commTarget;
            return;
        }

        MapLocation center = new MapLocation(dim1/2, dim2/2);
        MapLocation center2 = new MapLocation(dim1/2-1, dim2/2);
        MapLocation center3 = new MapLocation(dim1/2, dim2/2-1);
        MapLocation center4 = new MapLocation(dim1/2-1, dim2/2-1);
        int distToBestMine = 0;
        if (bestMine != null) distToBestMine = MovementManager.myLoc.distanceSquaredTo(bestMine);
        if (MapData.totalCheese == 0) return;
        for (int i = 0; i < 10; ++i){
            MapLocation cheeseLoc = MapData.cheeseMineList[mineIndex];
            mineIndex = (mineIndex + 1)% MapData.totalCheese;
            rc.setIndicatorDot(cheeseLoc, 0, 0, 255);
            int newDist = cheeseLoc.distanceSquaredTo(center);
            int newDist2 = cheeseLoc.distanceSquaredTo(center2);
            int newDist3 = cheeseLoc.distanceSquaredTo(center3);
            int newDist4 = cheeseLoc.distanceSquaredTo(center4);
            if (newDist > newDist2) newDist = newDist2;
            if (newDist > newDist3) newDist = newDist3;
            if (newDist > newDist4) newDist = newDist4;
            if (bestMine == null){
                bestMine = cheeseLoc;
                distCenterToBestMine = newDist;
                distToBestMine = MovementManager.myLoc.distanceSquaredTo(cheeseLoc);
                continue;
            }
            if (newDist < distCenterToBestMine){
                bestMine = cheeseLoc;
                distCenterToBestMine = newDist;
                distToBestMine = MovementManager.myLoc.distanceSquaredTo(cheeseLoc);
                continue;
            }
            else if (distCenterToBestMine < newDist) continue;
            int newMyDist = MovementManager.myLoc.distanceSquaredTo(cheeseLoc);
            if (newMyDist < distToBestMine){
                bestMine = cheeseLoc;
                //distCenterToBestMine = newDist; Not needed apparently.
                distToBestMine = newMyDist;
            }
        }

        if (bestMine.distanceSquaredTo(MovementManager.myLoc) <= MAX_DIST_SETTLE && CommunicationManager.getKings() > 1){
            CommunicationManager.setMyTarget(bestMine);
        }
    }

    static int newKingMineIndex = 0;

    void checkNewKing() throws GameActionException {
        MapLocation kingTarget = CommunicationManager.getKingTarget();
        if (kingTarget == null) return;
        if (MyUnit.rc.getCurrentRatCost() < 40 && rc.getGlobalCheese() < 500) CommunicationManager.removeKingTarget();
        if (bestMine != null && kingTarget.distanceSquaredTo(bestMine) < MIN_DIST_NEW_KING_REMOVE && kingTarget.distanceSquaredTo(MovementManager.myLoc) < MIN_DIST_NEW_KING_REMOVE){
            CommunicationManager.removeKingTarget();
        }
    }

    boolean shouldCreateNewKing() throws GameActionException {
        int tCheese = MapData.totalCheese;
        if (tCheese == 0) return false;
        if (SymmetryChecker.getSym() != 0) tCheese/= 2;
        if (tCheese <= CommunicationManager.getKings()*4) return false;
        if (rc.getRoundNum() < MIN_ROUND_SECOND_KING) return false;
        if (MyUnit.rc.getCurrentRatCost() <= 40) return false;
        if (MyUnit.rc.getCurrentRatCost() > 50 && rc.getGlobalCheese() > 500) return true;
        if (rc.getGlobalCheese() < 1000) return false;
        return true;
    }

    void setNewKingTarget() throws GameActionException {
        checkNewKing();
        MapLocation posTarget = CommunicationManager.getKingTarget();
        if (posTarget != null) return;

        if (!shouldCreateNewKing()) return;

        MapLocation center = new MapLocation(dim1/2, dim2/2);
        MapLocation center2 = new MapLocation(dim1/2-1, dim2/2);
        MapLocation center3 = new MapLocation(dim1/2, dim2/2-1);
        MapLocation center4 = new MapLocation(dim1/2-1, dim2/2-1);

        MapLocation trueCenter = center;
        if (MovementManager.myLoc.distanceSquaredTo(trueCenter) > MovementManager.myLoc.distanceSquaredTo(center2)) trueCenter = center2;
        if (MovementManager.myLoc.distanceSquaredTo(trueCenter) > MovementManager.myLoc.distanceSquaredTo(center3)) trueCenter = center3;
        if (MovementManager.myLoc.distanceSquaredTo(trueCenter) > MovementManager.myLoc.distanceSquaredTo(center4)) trueCenter = center4;

        MapLocation base0 = CommunicationManager.getKingLoc(0);
        MapLocation base1 = CommunicationManager.getTarget(0);
        MapLocation base2 = CommunicationManager.getTarget(1);
        MapLocation base3 = CommunicationManager.getTarget(2);
        MapLocation base4 = CommunicationManager.getTarget(3);
        MapLocation base5 = CommunicationManager.getTarget(4);

        MapLocation bestMine = null;
        int bestMineDist = 0;

        rc.setIndicatorString("Checking locs");


        for (int i = 0; i < 10; ++i){
            MapLocation cheeseLoc = MapData.cheeseMineList[newKingMineIndex];
            newKingMineIndex = (newKingMineIndex + 1)% MapData.totalCheese;

            if (cheeseLoc.distanceSquaredTo(MovementManager.myLoc) < MIN_DIST_NEW_KING) continue;
            if (base0 != null && cheeseLoc.distanceSquaredTo(base0) < MIN_DIST_NEW_KING) continue;
            if (base1 != null && cheeseLoc.distanceSquaredTo(base1) < MIN_DIST_NEW_KING) continue;
            if (base2 != null && cheeseLoc.distanceSquaredTo(base2) < MIN_DIST_NEW_KING) continue;
            if (base3 != null && cheeseLoc.distanceSquaredTo(base3) < MIN_DIST_NEW_KING) continue;
            if (base4 != null && cheeseLoc.distanceSquaredTo(base4) < MIN_DIST_NEW_KING) continue;
            if (base5 != null && cheeseLoc.distanceSquaredTo(base5) < MIN_DIST_NEW_KING) continue;

            //rc.setIndicatorLine(MovementManager.myLoc, cheeseLoc, 0, 0, 255);

            int dx = cheeseLoc.x*2 - dim1 + 1, dy = cheeseLoc.y*2 - dim2 + 1;

            int dist = dx*dx + dy*dy;

            if (bestMine == null || dist < bestMineDist){
                bestMineDist = dist;
                bestMine = cheeseLoc;
            }
            else if (dist == bestMineDist){
                if (MovementManager.myLoc.distanceSquaredTo(cheeseLoc) < MovementManager.myLoc.distanceSquaredTo(bestMine)){
                    bestMine = cheeseLoc;
                }
            }
        }

        if (bestMine != null){
            CommunicationManager.setKingTarget(bestMine);
        }

    }

    void spawnRats() throws GameActionException {
        buildEmergencyRats();
        while(true){
            if (!spawnRat()) break;
        }
    }

    void buildEmergencyRats() throws GameActionException {
        if (rc.getCurrentRatCost() > rc.getGlobalCheese()/5) return;
        if (VisionManager.closestCat != null){
            boolean closerAlly = false;
            int d = MovementManager.myLoc.distanceSquaredTo(VisionManager.closestCat.getLocation());
            RobotInfo[] allies = rc.senseNearbyRobots(visionRange, myTeam);
            for (RobotInfo r : allies){
                if (r.getLocation().distanceSquaredTo(VisionManager.closestCat.getLocation()) < d){
                    closerAlly = true;
                    break;
                }
            }
            if (!closerAlly){
                spawnEmergency(VisionManager.closestCat.getLocation());
                return;
            }
        }
        int diff = computeRatDiff();
        if (VisionManager.closestRat != null){
            if (enemies > 0) spawnEmergency(VisionManager.closestRat.getLocation());
        }
    }

    static int enemies = 0;

    int computeRatDiff(){
        int diff = 0;
        enemies = 0;
        for (RobotInfo r : VisionManager.visibleUnits){
            if (r.getTeam() == myTeam) ++ diff;
            else if (r.getTeam() == opponent){
                -- diff;
                ++enemies;
            }
        }
        return diff;
    }

    void spawnEmergency(MapLocation targetLoc) throws GameActionException {
        MapLocation bestLocation = null;
        double bestDist = -1;
        MapLocation[] spawnLocs = rc.getAllLocationsWithinRadiusSquared(MovementManager.myLoc, 8);
        int d = MovementManager.myLoc.distanceSquaredTo(targetLoc);
        for (MapLocation loc : spawnLocs){
            if (!rc.canBuildRat(loc)) continue;
            if (bestLocation == null){
                bestLocation = loc;
                bestDist = loc.distanceSquaredTo(targetLoc);
                continue;
            }
            int newDist = loc.distanceSquaredTo(targetLoc);
            if (bestDist < d && newDist >= d) continue;
            if (bestDist >= d && newDist < d){
                bestLocation = loc;
                bestDist = newDist;
                continue;
            }
            if (bestDist >= d && newDist >= bestDist) continue;
            if (bestDist < d && newDist < bestDist) continue;
            bestLocation = loc;
            bestDist = newDist;
        }

        if (bestLocation != null && rc.canBuildRat(bestLocation)) buildWrapper(bestLocation);
    }

    static void buildWrapper(MapLocation loc) throws GameActionException {
        CommunicationManager.reportCost();
        rc.buildRat(loc);
        ++babyRats;
        if (babyRats%4 != 3){
            CommunicationManager.reportTargetMine();
        }
    }

    void endTurn() throws GameActionException{
        if (creationRound < 10) CommunicationManager.reportMap();
        CommunicationManager.reportLocation();
        CommunicationManager.reportKnownMines();
        CommunicationManager.reportBabyRats();
        CommunicationManager.reportSym();
        super.endTurn();
        SymmetryChecker.run();
        SymmetryChecker.runMines();
        BFS.run();
    }

    boolean shouldBuildRat(){
        int maxCost = getMaxBabyCost();
        if (MyUnit.rc.getCurrentRatCost() <= maxCost){
            return MyUnit.rc.getGlobalCheese() > 5*MyUnit.rc.getCurrentRatCost();
        }
        int cheese = MyUnit.rc.getGlobalCheese() - SAVED_CHEESE + rc.getRoundNum();
        return cheese >= 5*rc.getCurrentRatCost();

    }

    void doDirs(){
        int dx, dy;
        MapLocation myLoc = rc.getLocation();
        dists[Direction.EAST.ordinal()] = dim1 - myLoc.x - 1;;
        dx = dim1 - myLoc.x - 1;
        dy = dim2 - myLoc.y - 1;
        dists[Direction.NORTHEAST.ordinal()] = Math.sqrt(2)*Math.min(dx,dy);
        dists[Direction.NORTH.ordinal()] = dim2 - myLoc.y - 1;
        dx = dim1 - myLoc.x - 1;
        dy = myLoc.y;
        dists[Direction.SOUTHEAST.ordinal()] = Math.sqrt(2)*Math.min(dx,dy);
        dists[Direction.WEST.ordinal()] = myLoc.x;;
        dx = myLoc.x;
        dy = dim2 - myLoc.y - 1;
        dists[Direction.NORTHWEST.ordinal()] = Math.sqrt(2)*Math.min(dx,dy);
        dy = myLoc.y;
        dists[Direction.SOUTHWEST.ordinal()] = Math.sqrt(2)*Math.min(dx,dy);
        dists[Direction.SOUTH.ordinal()] = myLoc.y;
    }

    MapLocation getBestLoc() throws GameActionException {
        MapLocation ans = null;
        double dAns = -1;
        MapLocation[] spawnLocs = rc.getAllLocationsWithinRadiusSquared(MovementManager.myLoc, 8);

        for (MapLocation loc : spawnLocs){
            if (!rc.canBuildRat(loc)) continue;
            int ord = rc.getLocation().directionTo(loc).ordinal();
            double ndAns = dists[ord]/((buildDirCount[ord])+1);
            if (ndAns > dAns){
                dAns = ndAns;
                ans = loc;
            }
        }
        return ans;
    }

    void tryPlaceDirt() throws GameActionException {
        if (VisionManager.closestCat == null) return;
        if (rc.getRoundNum() > roundPlaced + 5) return;
        MapLocation catLoc = Util.getClosestCatLoc(VisionManager.closestCat.getLocation());
        int cd = MovementManager.myLoc.add(MovementManager.myLoc.directionTo(catLoc)).distanceSquaredTo(catLoc);
        if (cd > 8) return;

        MapLocation[] locs = rc.getAllLocationsWithinRadiusSquared(MovementManager.myLoc, 8);
        MapLocation bestLoc = null;
        int minDist = 0;
        for (MapLocation loc : locs){
            if (!rc.canPlaceDirt(loc)) continue;
            int newDist = loc.distanceSquaredTo(catLoc);
            if (newDist >= cd) continue;
            if (bestLoc == null || newDist < minDist){
                bestLoc = loc;
                minDist = newDist;
            }
        }
        if (bestLoc != null){
            roundPlaced = rc.getRoundNum();
            rc.placeDirt(bestLoc);
        }
    }

    boolean spawnRat() throws GameActionException {
        if (!shouldBuildRat()) return false;
        MapLocation loc = getBestLoc();
        if (loc == null) return false;
        if (rc.canBuildRat(loc)){
            buildWrapper(loc);
            buildDirCount[MovementManager.myLoc.directionTo(loc).ordinal()]++;
            return true;
        }
        return false;
    }
}
