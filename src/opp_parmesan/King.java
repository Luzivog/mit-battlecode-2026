package opp_parmesan;

import battlecode.common.*;
import opp_parmesan.Utils.*;
import opp_parmesan.Roles.Rush;

import java.util.ArrayList;

import static opp_parmesan.Utils.BugNavigator.distance1d;

public class King extends Globals {

    static int lastCatTrap = -5;
    static int lastRatTrap = -5;

    // SPAWNING ------------------------------------------------------------ //

    public static void spawn() throws GameActionException {
        MapLocation kingLoc = rc.getLocation();
        MapLocation[] spawnPositionCandidates = new MapLocation[28];
        int spdSize = 0;

        // possible spawning locs
        for (MapLocation cand: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 10)) {
            if (rc.onTheMap(cand) && rc.canBuildRat(cand)) {
                rc.setIndicatorDot(cand, 255, 255, 255);
                spawnPositionCandidates[spdSize++] = cand;
            }
        }

        if (spdSize > 0) {
            // build facing centre
            if (Rush.spawnLoc == null) Rush.spawnLoc = rc.getLocation();
            MapLocation target = Rush.getNewRushLocation(), todo = null;
            int bestDist = 1000000;

            for (int i = 0; i < spdSize; i++) {
                // value of building here
                if (spawnPositionCandidates[i].distanceSquaredTo(target) < bestDist) {
                    bestDist = spawnPositionCandidates[i].distanceSquaredTo(target);
                    todo = spawnPositionCandidates[i];
                }
            }
            if (rc.canTurn()) rc.turn(rc.getLocation().directionTo(todo));
            rc.buildRat(todo);
        }
    }

    public static int spawnAmountThreshold() {
        // return safeCheeseReserve;

        int res = rc.getCurrentRatCost() * (20 - (15*rc.getRoundNum()/2000));

        // should it be dependent on map size?
        // maybe smaller map = lower threshold -> rush

        return res;
    }

    public static int insaneSpawnAmountThreshold() {
        int res = rc.getCurrentRatCost() * (40 - (15*rc.getRoundNum()/2000));
        return Math.min(res, (rc.getRoundNum() > midgame ? 1200 : 2000));
    }

    public static void spawnIf() throws GameActionException {
        if ((rc.getGlobalCheese() - rc.getCurrentRatCost() > spawnAmountThreshold() || (rc.getGlobalCheese() >= minCheese && rc.getCurrentRatCost() < (minNumberRats/4)*10+10))) {
            if (rc.getCurrentRatCost() < (maxNumberRats/4)*10+10 || rc.getGlobalCheese()-rc.getCurrentRatCost() > insaneSpawnAmountThreshold() || rc.getRoundNum() > endgame)
                spawn();
        }
    }

    // SURVIVAL ------------------------------------------------------------ //

    static Direction lastDir = null;
    public static MapLocation[] unsafe = new MapLocation[20];
    public static int[] unsafeTime = new int[20];

    public static void moveSafety() throws GameActionException {
        if (rc.getRoundNum() == spawnRound) return;
        // find the place with the minimum penalty

        int[] score = {0, 0, 0, 0, 0, 0, 0, 0, 0};

        ArrayList<MapLocation> mines = new ArrayList<>();

        boolean[] disabled = {false, false, false, false, false, false, false, false, false};

        for (MapInfo mi: surroundings) {


            if (mi.isWall() && distance1d(rc.getLocation(), mi.getMapLocation()) == 3) {
                /*
                for (int i = 0; i < 9; i++) if (!disabled[i] && BugNavigator.distance1d(rc.getLocation().add(Direction.DIRECTION_ORDER[i]), mi.getMapLocation()) == 2)
                    disabled[i] = true;

                 */
                continue;
            }

            Direction dir = rc.getLocation().directionTo(mi.getMapLocation());
            int penalty = 30 - (rc.getLocation().add(dir).distanceSquaredTo(mi.getMapLocation()));
            score[rc.getLocation().directionTo(mi.getMapLocation()).getDirectionOrderNum()] -= penalty < 0 ? 0 : penalty * mi.getCheeseAmount();

            if (mi.hasCheeseMine()) mines.add(mi.getMapLocation());
        }

        ArrayList<RobotInfo> hasCheese = new ArrayList<>();

        for (RobotInfo ri: allyRobots) {
            if (ri.getRawCheeseAmount() > 0) {
                hasCheese.add(ri);
            }
        }

        for (Direction dir : Direction.DIRECTION_ORDER) {
            int num = dir.getDirectionOrderNum();
            // cat wapoint
//            for (int i = 0; i < 20; i ++) if (unsafe[i] != null && rc.getRoundNum() - unsafeTime[i] < 30) {
//                int penalty = 30 - (rc.getLocation().add(dir).distanceSquaredTo(unsafe[i]));
//                score[num] += 100 * penalty;
//            }
            // robot
            MapLocation loc = rc.getLocation().add(dir);
            for (RobotInfo robot: hasCheese) {
                int penalty = 50 - (loc.distanceSquaredTo(robot.getLocation()));
                score[num] -= (/*20 +*/ robot.getRawCheeseAmount()) * penalty * 2;
            }
            for (RobotInfo robot: enemyRobots) {
                int penalty = 50 - (loc.distanceSquaredTo(robot.getLocation()));
                score[num] += 200000 * penalty;
            }
            for (RobotInfo robot: catRobots) {
                int penalty = 50 - (loc.distanceSquaredTo(robot.getLocation()));
                score[num] += 2000000 * penalty; // cat super scary
            }
            for (MapLocation ml: mines) {
                int penalty = 50 - (loc.distanceSquaredTo(ml));
                penalty = penalty > 40 ? 50 : penalty;
                score[num] -= 2000 * penalty;
            }
        }

        String scores = "";
        for (int i = 0; i < 9; i++) scores += " "+String.valueOf(score[i]);
        rc.setIndicatorString(scores);

        int best = 1000000000;
        Direction dir = null;
        if (rc.canMove(Direction.DIRECTION_ORDER[0]) && score[0] < best && !disabled[0]) {
            best = score[0];
            dir = Direction.DIRECTION_ORDER[0];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[1]) && score[1] < best && !disabled[1]) {
            best = score[1];
            dir = Direction.DIRECTION_ORDER[1];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[2]) && score[2] < best && !disabled[2]) {
            best = score[2];
            dir = Direction.DIRECTION_ORDER[2];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[3]) && score[3] < best && !disabled[3]) {
            best = score[3];
            dir = Direction.DIRECTION_ORDER[3];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[4]) && score[4] < best && !disabled[4]) {
            best = score[4];
            dir = Direction.DIRECTION_ORDER[4];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[5]) && score[5] < best && !disabled[5]) {
            best = score[5];
            dir = Direction.DIRECTION_ORDER[5];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[6]) && score[6] < best && !disabled[6]) {
            best = score[6];
            dir = Direction.DIRECTION_ORDER[6];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[7]) && score[7] < best && !disabled[7]) {
            best = score[7];
            dir = Direction.DIRECTION_ORDER[7];
        }
        if (rc.canMove(Direction.DIRECTION_ORDER[8]) && score[8] < best && !disabled[8]) {
            best = score[8];
            dir = Direction.DIRECTION_ORDER[8];
        }
        if (dir != null && !Direction.CENTER.equals(dir)) {
            if (rc.canMove(dir)) rc.move(dir);
            if (enemyRobots.length == 0 && catLoc == null)
                lastDir = dir;
            else
                lastDir = null;
        }  else {
            boolean doMove = true;
            MapInfo[] mi = rc.senseNearbyMapInfos();
            for (int i = 0; i < mi.length; i++) if (mi[i].hasCheeseMine()) doMove = false;
            if (doMove && lastDir != null && rc.canMove(lastDir) && !disabled[lastDir.getDirectionOrderNum()])
                rc.move(lastDir);
        }
        if (rc.isMovementReady() && enemyRobots.length == 0 && catRobots.length == 0 && lastSeenEnemy > 8) {
            Direction random = adjacentDirections[rng.nextInt(8)];
            if (rc.canMove(random)) rc.move(random);
        }
    }

    public static int[] catID = new int[15];
    public static int[] catRound = new int[15];
    public static int pointer = 0;

    public static void defendCats() throws GameActionException {
        // List of the last 15 seen cats with (id, num)
        for (RobotInfo cat: catRobots) {
            catID[pointer] = cat.getID();
            catRound[pointer] = rc.getRoundNum();
            pointer ++;
            pointer %= 15;
        }
        // Check if the current map location is already documented
//        boolean canSkip = false;
//        for (int j = 0; j < 20; j ++) if (unsafe[j] != null) {
//            if (rc.getLocation().isWithinDistanceSquared(unsafe[j], 8)) {
//                canSkip = true;
//                break;
//            }
//        }
//        // Check if any cats constantly bother us
//        for (int i = 0; !canSkip && i < pointer; i ++) if (catID[i] > 0) {
//            int ID = catID[i], annoying = 0;
//            // check how many times we've seen it in past 30 rounds
//            for (int j = 0; j < pointer; j ++) if (catID[j] == ID && rc.getRoundNum() - catRound[j] <= 20) {
//                annoying ++;
//            }
//            if (annoying >= 4) {
//                // current map location is dangerous
//                MapLocation furthest = unsafe[0];
//                int id = 0;
//                for (int j = 1; j < 20; j ++) {
//                    if (unsafe[j] == null || furthest == null || rc.getRoundNum() - unsafeTime[j] > 30) {
//                        furthest = unsafe[j];
//                        id = j;
//                    } else {
//                        if (rc.getLocation().distanceSquaredTo(unsafe[j]) > rc.getLocation().distanceSquaredTo(furthest)) {
//                            furthest = unsafe[j];
//                            id = j;
//                        }
//                    }
//                }
//                unsafe[id] = rc.getLocation();
//            }
//        }

        // Cat defence
        MapLocation catAttack = null;
        int distCat = 1000000;

        if (catLoc != null) {
            if (rc.isActionReady()) {
                /*
                MapLocation attempt = rc.getLocation().add(rc.getLocation().directionTo(catLoc));
                attempt = attempt.add(attempt.directionTo(catLoc));
                if (rc.canPlaceDirt(attempt)) {
                    rc.placeDirt(attempt);
                } else {
                    attempt = rc.getLocation().add(rc.getLocation().directionTo(catLoc)).add(rc.getLocation().directionTo(catLoc));
                    if (rc.canPlaceDirt(attempt)) {
                        rc.placeDirt(attempt);
                    }
                }
                 */
                // only place cat traps if cat is too close AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH
                for (MapLocation cand : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8)) {
                    if (catLoc != null && rc.canPlaceCatTrap(cand)) {
                        int dist = cand.distanceSquaredTo(catLoc);
                        if (dist < distCat) {
                            distCat = dist;
                            catAttack = cand;
                        }
                    }
                }
            }
        }
        if (rc.getRoundNum() - lastCatTrap >= catSpacing && catAttack != null && rc.canPlaceCatTrap(catAttack)) {
            rc.placeCatTrap(catAttack);
            lastCatTrap = rc.getRoundNum();
        }
    }


    public static void tryAttack () throws GameActionException {
        if (!rc.isActionReady()) return;
        MapLocation[] close = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        for (int i = 0; i < close.length; i++) if (rc.canAttack(close[i])) {
            rc.attack(close[i]);
            return;
        }
    }

    public static void defendRats() throws GameActionException {
        // Only place traps underneath rats
        int minHealth = 120;
        RobotInfo weak = null;
        for (RobotInfo bot:enemyRobots) if (rc.getLocation().distanceSquaredTo(bot.getLocation()) <= 8) {
            if (bot.getHealth() < minHealth) {
                minHealth = bot.getHealth();
                weak = bot;
            }
        }
        if (weak != null && rc.isActionReady()) {
            if (minHealth < 15) {
                // snipe
                int cheeseBoost = 0;
                if (minHealth == 11) cheeseBoost = 1;
                else if (minHealth == 12) cheeseBoost = 2;
                else if (minHealth == 13) cheeseBoost = 5;
                else if (minHealth == 14) cheeseBoost = 10;
                cheeseBoost = Math.min(cheeseBoost, rc.getGlobalCheese());
                if (rc.canAttack(weak.getLocation(), cheeseBoost)) rc.attack(weak.getLocation(), cheeseBoost);
            } else {
                // trap them
                if (rc.canPlaceRatTrap(weak.getLocation())) rc.placeRatTrap(weak.getLocation());
            }
        }

        if (allyRobots.length <= enemyRobots.length && enemyRobots.length > 0) {
            int sumX = 0, sumY = 0;
            for (int i = 0; i < enemyRobots.length; i++) {
                sumX += enemyRobots[i].location.x;
                sumY += enemyRobots[i].location.y;
            }

            sumX = (sumX + (newestSqueaks.length + 1) / 2) / (newestSqueaks.length + 1);
            sumY = (sumY + (newestSqueaks.length + 1) / 2) / (newestSqueaks.length + 1);

            int bestScore = 10000000;
            MapLocation bestLocation = null;

            MapLocation[] nearbyLocations = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);

            MapLocation centroid = new MapLocation(sumX, sumY);

            for (int i = 0; i < nearbyLocations.length; i++) if (rc.canBuildRat(nearbyLocations[i])) {
                int score = nearbyLocations[i].distanceSquaredTo(centroid);
                for (int j = 0; j < enemyRobots.length; j++) {
                    if (nearbyLocations[i].distanceSquaredTo(enemyRobots[j].location) <= 8) {
                        score += 100000;
                    }
                }
                if (score < bestScore) {
                    bestScore = score;
                    bestLocation = nearbyLocations[i];
                }
            }

            if (bestLocation != null) {
                if (rc.canTurn()) rc.turn(rc.getLocation().directionTo(bestLocation));
                rc.buildRat(bestLocation);
            }

        }

    }

    public static void tryDigDirt () throws GameActionException {
        if (catLoc != null) return;
        MapLocation[] mls = rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8);
        for (int i = 0; i < mls.length; i++) {
            if (rc.canRemoveDirt(mls[i])) {
                rc.removeDirt(mls[i]);
                break;
            }
        }
    }

    public static MapLocation newKingPos = null;

    public static int lastSeenEnemy = 0;

    public static void needNewKing() throws GameActionException {
        newKingPos = Comms.needNewKing();
        if (newKingPos != null) {
            for (MapLocation ml : kingPos) {
                if (ml != null && ml.distanceSquaredTo(newKingPos) <= closeToKing) {
                    newKingPos = null;
                    Comms.clearNewKing();
                    break;
                }
            }
        }
        if (newKingPos != null) return;
        MapLocation ml = Comms.checkForNewKings();
        if (ml == null) return;
        if (newKingPos == null) newKingPos = ml;
        System.out.println("RECIEVED!!!");
        for (MapLocation loc: kingPos) {
            if (loc != null && loc.distanceSquaredTo(newKingPos) <= closeToKing) {
                newKingPos = null;
                return;
            }
        }
        Comms.encodeNewKingPos(newKingPos);
    }

    public static void spawnNewKing () throws  GameActionException {
        if (newKingPos == null) return;
        int num = Comms.getNumberBots();
        if (num < botsForNewKing) {
            if (rc.getGlobalCheese()-rc.getCurrentRatCost() > newKingSpawnThreshold) {
                int best = 1000000;
                MapLocation ml = null;
                for (MapLocation loc: rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8)) {
                    int dist = loc.distanceSquaredTo(newKingPos);
                    if (rc.canBuildRat(loc) && dist < best) {
                        best = dist;
                        ml = loc;
                    }
                }
                if (ml != null) {
                    System.out.println("SPWANED!");
                    if (rc.canTurn()) rc.turn(rc.getLocation().directionTo(ml));
                    rc.buildRat(ml);
                    Comms.incNewKingBotCount(num);
                }
            }
        } else {
            Comms.incNewKingBotCount(num);
            newKingPos = null;
            Comms.clearNewKing();
        }
    }

    public static void run() throws GameActionException {

        if (rc.getRoundNum() == spawnRound) {
            Comms.clearNewKing();
            lastDir = rc.getLocation().directionTo(new MapLocation(mapWidth/2, mapHeight/2));
        }

        // symmetry
        Symmetry.updateSymmetryFromGlobal();
        // if (rc.getRoundNum() != 1) Symmetry.updateNearbySymmetry(); // it's bytecode exceeding really bad :(
        Symmetry.hearNearbySymmetrySqueaks();

        // write to global
        if (Comms.getSymmetry() != Symmetry.symm) {
            Comms.updateSymmetry(Symmetry.symm);
        }

        if (enemyRobots.length > 0 || catRobots.length > 0) lastSeenEnemy = 0;
        else lastSeenEnemy++;

        moveSafety();

        //if (Clock.getBytecodeNum() > 12000) return;

        // collect cheese
        for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), 8)) {
            if (rc.canPickUpCheese(loc) && rc.getRawCheese() < maxCheese) pickUpCheese(loc);
        }

        defendCats();
        defendRats();

        if (rc.getRoundNum() <= 1200) {
            needNewKing();
            spawnNewKing();
        } else Comms.clearNewKing();

        spawnIf();

        // try to attack baby -> cat -> king
        if (!Attack.attackBaby()) {
            if (!Attack.attackCat()) {
                Attack.attackKing();
            }
        }

        tryDigDirt();

        tryAttack();

        // send emergency signal
        if (rc.getRoundNum() >= endgame || (rc.getHealth() <= help2Health && enemyRobots.length >= help2Enemies)) {
            rc.writeSharedArray(60, 2);
        }
        else if (rc.getHealth() <= help1Health && enemyRobots.length >= help1Enemies) {
            rc.writeSharedArray(60, 1);
        }
        else {
            rc.writeSharedArray(60, 0);
        }

        Comms.squeakEnemies();

        if (newKingPos != null) rc.setIndicatorString(newKingPos.toString());

    }
}
