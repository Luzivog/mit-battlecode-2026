

package challenger.robot.king;

import battlecode.common.*;
import challenger.robot.Strategy;
import challenger.RobotTracker;
import challenger.Pathfinder;

import static challenger.robot.king.RatKing.*;
import static challenger.Game.*;
import static challenger.util.Util.*;
import challenger.CellTracker;

public class KingCombatStrategy extends Strategy {

    private static final int[] RING_DX = {0, 1, 2, 2, 2, 2, 2, 1, 0, -1, -2, -2, -2, -2, -2, -1};
    private static final int[] RING_DY = {2, 2, 2, 1, 0, -1, -2, -2, -2, -2, -2, -1, 0, 1, 2, 2};
    private static boolean catTrapsExpired = false;

    private boolean placeDefensiveRatTrap() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (rc.isCooperation()) return false;
        int minBank = rc.getHealth() <= 400 ? 150 : 250;
        if (rc.getGlobalCheese() < minBank || rc.getNumberRatTraps() >= TrapType.RAT_TRAP.maxCount) return false;
        if (RobotTracker.numEnemies == 0) return false;

        boolean hasThreat = false;
        int enemyLim = RobotTracker.numEnemies < 8 ? RobotTracker.numEnemies : 8;
        for (int idx = 0; idx < RobotTracker.numEnemies; idx++) {
            RobotInfo robot = RobotTracker.sortedEnemies[idx];
            if (robot == null) continue;
            int d2 = robot.location.distanceSquaredTo(myLoc);
            if (d2 <= 8 && robot.health <= GameConstants.RAT_BITE_DAMAGE && rc.canAttack(robot.location)) {
                return false;
            }
            if (!hasThreat && ((robot.type == UnitType.BABY_RAT && d2 <= 16) || (robot.type == UnitType.RAT_KING && d2 <= 25))) {
                hasThreat = true;
            }
        }
        if (!hasThreat) return false;

        long blockedMask = 0L;
        if (rc.getNumberRatTraps() > 0) {
            for (MapInfo info : rc.senseNearbyMapInfos(18)) {
                if (info.getTrap() == TrapType.RAT_TRAP) {
                    int tx = info.getMapLocation().x - myLocX;
                    int ty = info.getMapLocation().y - myLocY;
                    for (int ox = -1; ox <= 1; ox++) {
                        int nx = tx + ox;
                        if (nx < -2 || nx > 2) continue;
                        for (int oy = -1; oy <= 1; oy++) {
                            int ny = ty + oy;
                            if (ny < -2 || ny > 2) continue;
                            blockedMask |= 1L << ((nx + 2) * 5 + (ny + 2));
                        }
                    }
                }
            }
        }

        int bestScore = 0;
        MapLocation bestLoc = null;
        for (int r = 0; r < 16; r++) {
            int rx = RING_DX[r];
            int ry = RING_DY[r];
            if ((blockedMask & (1L << ((rx + 2) * 5 + (ry + 2)))) != 0L) continue;
            MapLocation loc = new MapLocation(myLocX + rx, myLocY + ry);
            if (!rc.canPlaceRatTrap(loc)) continue;

            int score = 0;
            for (int idx = 0; idx < enemyLim; idx++) {
                RobotInfo e = RobotTracker.sortedEnemies[idx];
                if (e == null) continue;
                int cheb = chebyshevDistance(loc, e);
                if (cheb > 2) continue;
                int eScore = (cheb <= 1 ? 20 : 12);
                if (loc.distanceSquaredTo(e.location) < myLoc.distanceSquaredTo(e.location)) {
                    eScore += 8;
                }
                if (e.type == UnitType.RAT_KING) {
                    eScore += 15;
                } else {
                    if (e.getCarryingRobot() != null) eScore += 12;
                    if (e.health >= 50) eScore += 6;
                    if (e.health <= TrapType.RAT_TRAP.damage && e.health > GameConstants.RAT_BITE_DAMAGE) eScore += 4;
                    int dot = (loc.x - e.location.x) * e.direction.dx + (loc.y - e.location.y) * e.direction.dy;
                    if (dot > 0) eScore += 6;
                }
                score += eScore;
            }
            if (score > bestScore) {
                bestScore = score;
                bestLoc = loc;
            }
        }

        if (bestLoc != null) {
            rc.placeRatTrap(bestLoc);
            return true;
        }
        return false;
    }

    private boolean placeDefensiveCatTrap() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (RobotTracker.numCats == 0) return false;
        if (rc.getAllCheese() < 100 || rc.getNumberCatTraps() >= TrapType.CAT_TRAP.maxCount) return false;
        boolean coop = rc.isCooperation();
        if (!coop && (catTrapsExpired || rc.getBackstabbingTeam() == team)) return false;

        RobotInfo cat = RobotTracker.sortedCats[0];
        if (cat == null) return false;
        int catDistSq = dist(myLoc, cat);
        if (catDistSq > 20) return false;
        if (challenger.robot.LastCat.isHoldEnabled() && !challenger.robot.LastCat.mayDamage(cat, TrapType.CAT_TRAP.damage)) return false;

        long blockedMask = 0L;
        if (rc.getNumberCatTraps() > 0) {
            for (MapInfo info : rc.senseNearbyMapInfos(18)) {
                if (info.getTrap() == TrapType.CAT_TRAP) {
                    int tx = info.getMapLocation().x - myLocX;
                    int ty = info.getMapLocation().y - myLocY;
                    for (int ox = -1; ox <= 1; ox++) {
                        int nx = tx + ox;
                        if (nx < -2 || nx > 2) continue;
                        for (int oy = -1; oy <= 1; oy++) {
                            int ny = ty + oy;
                            if (ny < -2 || ny > 2) continue;
                            blockedMask |= 1L << ((nx + 2) * 5 + (ny + 2));
                        }
                    }
                }
            }
        }

        int cx = cat.location.x;
        int cy = cat.location.y;
        int fx = cat.direction.dx;
        int fy = cat.direction.dy;
        int ncx = cx + fx;
        int ncy = cy + fy;
        int faceDot = (2 * myLocX - 2 * cx - 1) * fx + (2 * myLocY - 2 * cy - 1) * fy;

        int bestScore = Integer.MIN_VALUE;
        MapLocation bestLoc = null;
        for (int r = 0; r < 16; r++) {
            int rx = RING_DX[r];
            int ry = RING_DY[r];
            if ((blockedMask & (1L << ((rx + 2) * 5 + (ry + 2)))) != 0L) continue;
            MapLocation loc = new MapLocation(myLocX + rx, myLocY + ry);
            if (!rc.canPlaceCatTrap(loc)) {
                if (!coop && rc.onTheMap(loc)) {
                    MapInfo info = rc.senseMapInfo(loc);
                    if (info.isPassable() && !info.hasCheeseMine() && info.getTrap() == TrapType.NONE && rc.senseRobotAtLocation(loc) == null) {
                        catTrapsExpired = true;
                        return false;
                    }
                }
                continue;
            }

            int ndx = ncx - loc.x;
            if (ndx < 0) ndx = -ndx - 1;
            int ndy = ncy - loc.y;
            if (ndy < 0) ndy = -ndy - 1;
            int nextCheb = ndx > ndy ? ndx : ndy;
            int curCheb = chebyshevDistance(loc, cat);
            int locCatDistSq = dist(loc, cat);

            if (nextCheb > 1 && !(curCheb <= 2 && locCatDistSq < catDistSq && faceDot >= 0)) continue;

            int score = -locCatDistSq;
            if (nextCheb == 0) score += 50;
            else if (nextCheb == 1) score += 40;
            if (curCheb == 1) score += 25;
            else if (curCheb == 2) score += 10;
            if (locCatDistSq < catDistSq) score += 15;

            if (score > bestScore) {
                bestScore = score;
                bestLoc = loc;
            }
        }

        if (bestLoc != null) {
            rc.placeCatTrap(bestLoc);
            return true;
        }
        return false;
    }

    // TODO: only dig if we havent seen the cat in some time
    private boolean digSurroundingDirt() throws GameActionException {
        if (!rc.isActionReady()) return false;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dy = -2; dy <= 2; dy++) {
                if (Math.max(Math.abs(dx), Math.abs(dy)) < 2) continue;
                MapLocation checkLoc = new MapLocation(myLoc.x + dx, myLoc.y + dy);
                if (rc.canRemoveDirt(checkLoc) && rc.getGlobalCheese() >= 100) {
                    rc.removeDirt(checkLoc);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean runTick() throws GameActionException {
        boolean hasThreat = (RobotTracker.numEnemies + RobotTracker.numCats) > 0;
        if(!hasThreat) return false;
        var score0 = 0;
        var score1 = 0;
        var score2 = 0;
        var score3 = 0;
        var score4 = 0;
        var score5 = 0;
        var score6 = 0;
        var score7 = 0;
                {
            var loc = myLoc.translate(0, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
                score0 = -1000;
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
                score0 -= 2;
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score0 = -1000;
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score0 -= 2;
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
                score2 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
                score2 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, 0);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score1 = -1000;
                score2 = -1000;
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score1 -= 2;
                score2 -= 2;
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score2 = -1000;
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score2 -= 2;
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
            }
        }

                {
            var loc = myLoc.translate(1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
                score4 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
                score4 -= 2;
            }
        }

                {
            var loc = myLoc.translate(0, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score3 = -1000;
                score4 = -1000;
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score3 -= 2;
                score4 -= 2;
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-1, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score4 = -1000;
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score4 -= 2;
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, -2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, -1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
                score6 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
                score6 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 0);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score5 = -1000;
                score6 = -1000;
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score5 -= 2;
                score6 -= 2;
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 1);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score6 = -1000;
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score6 -= 2;
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-2, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
            }
        }

                {
            var loc = myLoc.translate(-1, 2);
            if(!CellTracker.isPassable(loc) || rc.senseRobotAtLocation(loc) != null) {
                score7 = -1000;
                score0 = -1000;
            }
            if(CellTracker.isDirt(loc)) {
                score7 -= 2;
                score0 -= 2;
            }
        }

        for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
            var robot = RobotTracker.sortedEnemies[idx];
            if(robot == null) continue;
            var weight = switch(robot.type) {
                case BABY_RAT -> 1;
                case RAT_KING -> 3;
                case CAT -> 5;
            };
            var dir = myLoc.directionTo(robot.location);
            switch(dir.ordinal()) {
                case 0 -> {
                    score0           -= weight * 3;
                    score6 += weight;
                    score2 += weight;
                    score3 += weight + 1;
                    score4 += weight + 1;
                    score5 += weight + 1;
                }
                case 1 -> {
                    score1           -= weight * 3;
                    score7 += weight;
                    score3 += weight;
                    score4 += weight + 1;
                    score5 += weight + 1;
                    score6 += weight + 1;
                }
                case 2 -> {
                    score2           -= weight * 3;
                    score0 += weight;
                    score4 += weight;
                    score5 += weight + 1;
                    score6 += weight + 1;
                    score7 += weight + 1;
                }
                case 3 -> {
                    score3           -= weight * 3;
                    score1 += weight;
                    score5 += weight;
                    score6 += weight + 1;
                    score7 += weight + 1;
                    score0 += weight + 1;
                }
                case 4 -> {
                    score4           -= weight * 3;
                    score2 += weight;
                    score6 += weight;
                    score7 += weight + 1;
                    score0 += weight + 1;
                    score1 += weight + 1;
                }
                case 5 -> {
                    score5           -= weight * 3;
                    score3 += weight;
                    score7 += weight;
                    score0 += weight + 1;
                    score1 += weight + 1;
                    score2 += weight + 1;
                }
                case 6 -> {
                    score6           -= weight * 3;
                    score4 += weight;
                    score0 += weight;
                    score1 += weight + 1;
                    score2 += weight + 1;
                    score3 += weight + 1;
                }
                case 7 -> {
                    score7           -= weight * 3;
                    score5 += weight;
                    score1 += weight;
                    score2 += weight + 1;
                    score3 += weight + 1;
                    score4 += weight + 1;
                }
                default -> {
                    assert false : "unexpected direction";
                }
            }
        }
        // The king keeps away from every visible cat. A cat whose forward lane (laneCross) holds the king also
        // scores leaving that lane first, getting closer to an exit the king can walk to second.
        for(int idx = 0; idx < RobotTracker.numCats; idx++) {
            var cat = RobotTracker.sortedCats[idx];
            switch(myLoc.directionTo(cat.location).ordinal()) {
                case 0 -> {
                    score0           -= 15;
                    score6 += 5;
                    score2 += 5;
                    score3 += 6;
                    score4 += 6;
                    score5 += 6;
                }
                case 1 -> {
                    score1           -= 15;
                    score7 += 5;
                    score3 += 5;
                    score4 += 6;
                    score5 += 6;
                    score6 += 6;
                }
                case 2 -> {
                    score2           -= 15;
                    score0 += 5;
                    score4 += 5;
                    score5 += 6;
                    score6 += 6;
                    score7 += 6;
                }
                case 3 -> {
                    score3           -= 15;
                    score1 += 5;
                    score5 += 5;
                    score6 += 6;
                    score7 += 6;
                    score0 += 6;
                }
                case 4 -> {
                    score4           -= 15;
                    score2 += 5;
                    score6 += 5;
                    score7 += 6;
                    score0 += 6;
                    score1 += 6;
                }
                case 5 -> {
                    score5           -= 15;
                    score3 += 5;
                    score7 += 5;
                    score0 += 6;
                    score1 += 6;
                    score2 += 6;
                }
                case 6 -> {
                    score6           -= 15;
                    score4 += 5;
                    score0 += 5;
                    score1 += 6;
                    score2 += 6;
                    score3 += 6;
                }
                case 7 -> {
                    score7           -= 15;
                    score5 += 5;
                    score1 += 5;
                    score2 += 6;
                    score3 += 6;
                    score4 += 6;
                }
                default -> {
                }
            }
            int c = laneCross(myLocX, myLocY, cat);
            if(c == NOT_IN_LANE) continue;
            int fx = cat.direction.dx, fy = cat.direction.dy;
            // c grows by `step` per king step along (fy, -fx); the lane ends at |c| > w.
            int w = 3, step = 2;
            if(fx * fy != 0) {
                w = 6;
                step = 4;
            }
            int nPlus = (w + 1 - c + step - 1) / step, nMinus = (c + w + 1 + step - 1) / step;
            boolean plusOpen = isWalkable(fy, -fx, nPlus);
            boolean minusOpen = isWalkable(-fy, fx, nMinus);
            int d = laneMargin(c, w, plusOpen, minusOpen);
            {
                int c2 = c - 2 * fx;
                if(c2 > w || c2 < -w) score0 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score0 += 15;
                    else if(d2 > d) score0 -= 15;
                }
                score0 += fy;
            }
            {
                int c2 = c + 2 * fy - 2 * fx;
                if(c2 > w || c2 < -w) score1 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score1 += 15;
                    else if(d2 > d) score1 -= 15;
                }
                score1 += fx + fy;
            }
            {
                int c2 = c + 2 * fy;
                if(c2 > w || c2 < -w) score2 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score2 += 15;
                    else if(d2 > d) score2 -= 15;
                }
                score2 += fx;
            }
            {
                int c2 = c + 2 * fy + 2 * fx;
                if(c2 > w || c2 < -w) score3 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score3 += 15;
                    else if(d2 > d) score3 -= 15;
                }
                score3 += fx - fy;
            }
            {
                int c2 = c + 2 * fx;
                if(c2 > w || c2 < -w) score4 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score4 += 15;
                    else if(d2 > d) score4 -= 15;
                }
                score4 += -fy;
            }
            {
                int c2 = c - 2 * fy + 2 * fx;
                if(c2 > w || c2 < -w) score5 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score5 += 15;
                    else if(d2 > d) score5 -= 15;
                }
                score5 += -fx - fy;
            }
            {
                int c2 = c - 2 * fy;
                if(c2 > w || c2 < -w) score6 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score6 += 15;
                    else if(d2 > d) score6 -= 15;
                }
                score6 += -fx;
            }
            {
                int c2 = c - 2 * fy - 2 * fx;
                if(c2 > w || c2 < -w) score7 += 30;
                else {
                    int d2 = laneMargin(c2, w, plusOpen, minusOpen);
                    if(d2 < d) score7 += 15;
                    else if(d2 > d) score7 -= 15;
                }
                score7 += -fx + fy;
            }
        }
        // Tune.CM_LANE_FLEE: with the cat lanes known (Book.CAT_LANE), a step that puts more loop footprint under the king loses
        // 4 a tile, so a king that keeps away from a cat also walks off its loop.
        if(challenger.Tune.CM_LANE_FLEE != 0 && RobotTracker.numCats > 0 && challenger.CatLanes.version != 0) {
            int o = challenger.CatLanes.kingOverlap(myLocX, myLocY);
            score0 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + 0, myLocY + 1) - o);
            score1 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + 1, myLocY + 1) - o);
            score2 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + 1, myLocY + 0) - o);
            score3 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + 1, myLocY + -1) - o);
            score4 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + 0, myLocY + -1) - o);
            score5 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + -1, myLocY + -1) - o);
            score6 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + -1, myLocY + 0) - o);
            score7 -= 4 * (challenger.CatLanes.kingOverlap(myLocX + -1, myLocY + 1) - o);
        }
        {
            int best = -1;
            var move = Direction.CENTER;
            if(score0 > best) {
                best = score0;
                move = Direction.NORTH;
            }
            if(score1 > best) {
                best = score1;
                move = Direction.NORTHEAST;
            }
            if(score2 > best) {
                best = score2;
                move = Direction.EAST;
            }
            if(score3 > best) {
                best = score3;
                move = Direction.SOUTHEAST;
            }
            if(score4 > best) {
                best = score4;
                move = Direction.SOUTH;
            }
            if(score5 > best) {
                best = score5;
                move = Direction.SOUTHWEST;
            }
            if(score6 > best) {
                best = score6;
                move = Direction.WEST;
            }
            if(score7 > best) {
                best = score7;
                move = Direction.NORTHWEST;
            }
            // indicate("evading " + move);
            digDirtInDirection(move);
            if(move != Direction.CENTER && rc.canMove(move)) RatKing.rawMove(move);
        }
        if (RobotTracker.numCats > 0) {
            if ((challenger.Tune.KING_TRAPS & 4) != 0) placeDefensiveCatTrap();
        }
        if ((challenger.Tune.KING_TRAPS & 2) != 0 && RobotTracker.numEnemies > 0) {
            placeDefensiveRatTrap();
        }
        if(rc.isActionReady()) {
            RobotInfo bestEnemy = null;
            int bestHp = 1000000000;
            for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
                var robot = RobotTracker.sortedEnemies[idx];
                if(robot == null) continue;
                if(robot.location.distanceSquaredTo(myLoc) > 8) continue;
                if(robot.health < bestHp) {
                    bestEnemy = robot;
                    bestHp = robot.health;
                }
            }
            if(bestEnemy != null && rc.canAttack(bestEnemy.location)) {
                int kCheese = 0;
                int g = rc.getGlobalCheese();
                if (KingSpawningStrategy.isRichMap() && bestEnemy.type == UnitType.BABY_RAT && bestEnemy.health > 10) {
                    if (bestEnemy.health <= 14 && g >= 400) {
                        int need = (bestEnemy.health - 11) * (bestEnemy.health - 11) + 1;
                        if (need <= g) kCheese = need;
                    } else if (g >= 300) {
                        kCheese = 2;
                    }
                }
                rc.attack(bestEnemy.location, kCheese);
            } else if(RobotTracker.numCats > 0) {
                biteCat(RobotTracker.sortedCats[0]);
            } else {
                digSurroundingDirt();
            }
        }
        return true;
    }

    // A bite on a cat costs no cheese and is no backstab (rules §2, §7); it adds cat damage.
    private static void biteCat(RobotInfo cat) throws GameActionException {
        if (challenger.robot.LastCat.isHoldEnabled() && !challenger.robot.LastCat.mayDamage(cat, GameConstants.RAT_BITE_DAMAGE)) return;
        {
            var loc = cat.location.translate(0, 0);
            if(rc.canAttack(loc)) {
                rc.attack(loc);
                return;
            }
        }
        {
            var loc = cat.location.translate(1, 0);
            if(rc.canAttack(loc)) {
                rc.attack(loc);
                return;
            }
        }
        {
            var loc = cat.location.translate(0, 1);
            if(rc.canAttack(loc)) {
                rc.attack(loc);
                return;
            }
        }
        {
            var loc = cat.location.translate(1, 1);
            if(rc.canAttack(loc)) {
                rc.attack(loc);
                return;
            }
        }
    }

    static final int NOT_IN_LANE = 1000;

    // Rules §12: an EXPLORE cat walks its 2x2 along its facing, and a king in the way blocks it and is scratched every
    // 3 rounds. With r = 2·(king centre − cat centre), the king's 3x3 centred at (x, y) meets that forward lane iff
    // r · facing > 0 and |r × facing| ≤ 3 (cardinal facing) or ≤ 6 (diagonal). Returns r × facing, else NOT_IN_LANE.
    static int laneCross(int x, int y, RobotInfo cat) {
        int fx = cat.direction.dx, fy = cat.direction.dy;
        int rx = 2 * (x - cat.location.x) - 1, ry = 2 * (y - cat.location.y) - 1;
        if(rx * fx + ry * fy <= 0) return NOT_IN_LANE;
        int c = rx * fy - ry * fx;
        int w = fx * fy == 0 ? 3 : 6;
        return c > w || c < -w ? NOT_IN_LANE : c;
    }

    // Distance (in units of c) to the nearest lane edge whose side is open, or 1000 if neither is.
    private static int laneMargin(int c, int w, boolean plusOpen, boolean minusOpen) {
        int d = 1000;
        if(plusOpen) d = w + 1 - c;
        if(minusOpen && c + w + 1 < d) d = c + w + 1;
        return d;
    }

    // Whether every 3x3 on n king steps of (dx, dy) is known and passable.
    private static boolean isWalkable(int dx, int dy, int n) {
        for(int k = 1; k <= n; k++) {
            if(!CellTracker.isPassable3x3(new MapLocation(myLocX + k * dx, myLocY + k * dy))) return false;
        }
        return true;
    }

    public static boolean entersCatLane(Direction dir) {
        int x = myLocX + dir.dx, y = myLocY + dir.dy;
        for(int idx = RobotTracker.numCats; --idx >= 0; ) {
            if(laneCross(x, y, RobotTracker.sortedCats[idx]) != NOT_IN_LANE) return true;
        }
        return false;
    }
}