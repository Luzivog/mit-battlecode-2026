

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

    private boolean placeDefensiveDirt() throws GameActionException {
        if (!rc.isActionReady()) return false;
        if (RobotTracker.numCats == 0) return false;

        RobotInfo nearestCat = RobotTracker.sortedCats[0];
        MapLocation catLoc = nearestCat.location;
        if (nearestCat == null) return false;
        if (catLoc.distanceSquaredTo(myLoc) >= 20) return false;

        int bestScore = -1000000;
        MapLocation bestLoc = null;

        for (MapInfo info: rc.senseNearbyMapInfos()) {
            MapLocation loc = info.getMapLocation();
            if (!rc.canPlaceDirt(loc)) continue;
            if (loc.distanceSquaredTo(catLoc) > myLoc.distanceSquaredTo(catLoc)) continue;

            int numDirts = 0;
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    if (!rc.onTheMap(new MapLocation(loc.x + dx, loc.y + dy))) continue;
                    MapInfo checkInfo = rc.senseMapInfo(new MapLocation(loc.x + dx, loc.y + dy));
                    if (checkInfo.isDirt()) {
                        numDirts++;
                    }
                }
            }

            // i tested this with thresh = 4
            // it performs basically the same
            if (numDirts >= 2) continue;

            int score = -loc.distanceSquaredTo(catLoc);

            if (score > bestScore) {
                bestScore = score;
                bestLoc = loc;
            }
        }

        if (bestLoc != null) {
            rc.placeDirt(bestLoc);
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
            placeDefensiveDirt();
        } else {
            digSurroundingDirt();
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
            if(bestEnemy != null) {
                rc.attack(bestEnemy.location);
            } else if(RobotTracker.numCats > 0) {
                biteCat(RobotTracker.sortedCats[0]);
            }
        }
        return true;
    }

    // A bite on a cat costs no cheese and is no backstab (rules §2, §7); it adds cat damage.
    private static void biteCat(RobotInfo cat) throws GameActionException {
        if (challenger.Tune.PM_COOP_HOLD != 0 && !challenger.robot.LastCat.mayDamage(cat, GameConstants.RAT_BITE_DAMAGE)) return;
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