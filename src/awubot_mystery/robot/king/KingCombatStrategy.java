package awubot_mystery.robot.king;

import battlecode.common.*;
import awubot_mystery.robot.Strategy;
import awubot_mystery.RobotTracker;
import awubot_mystery.Pathfinder;

import static awubot_mystery.robot.king.RatKing.*;
import static awubot_mystery.Game.*;
import static awubot_mystery.util.Util.*;

public class KingCombatStrategy extends Strategy {
    @Override
    public boolean runTick() throws GameActionException {
        boolean hasThreat = (RobotTracker.numEnemies + RobotTracker.numCats) > 0;
        if(!hasThreat) return false;
        if(rc.isMovementReady()) {
            var score0 = 0;
            var score1 = 0;
            var score2 = 0;
            var score3 = 0;
            var score4 = 0;
            var score5 = 0;
            var score6 = 0;
            var score7 = 0;
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
                        score7 -= weight * 2;
                        score1 -= weight * 2;
                        score3 += weight;
                        score4 += weight;
                        score5 += weight;
                    }
                    case 1 -> {
                        score1           -= weight * 3;
                        score0 -= weight * 2;
                        score2 -= weight * 2;
                        score4 += weight;
                        score5 += weight;
                        score6 += weight;
                    }
                    case 2 -> {
                        score2           -= weight * 3;
                        score1 -= weight * 2;
                        score3 -= weight * 2;
                        score5 += weight;
                        score6 += weight;
                        score7 += weight;
                    }
                    case 3 -> {
                        score3           -= weight * 3;
                        score2 -= weight * 2;
                        score4 -= weight * 2;
                        score6 += weight;
                        score7 += weight;
                        score0 += weight;
                    }
                    case 4 -> {
                        score4           -= weight * 3;
                        score3 -= weight * 2;
                        score5 -= weight * 2;
                        score7 += weight;
                        score0 += weight;
                        score1 += weight;
                    }
                    case 5 -> {
                        score5           -= weight * 3;
                        score4 -= weight * 2;
                        score6 -= weight * 2;
                        score0 += weight;
                        score1 += weight;
                        score2 += weight;
                    }
                    case 6 -> {
                        score6           -= weight * 3;
                        score5 -= weight * 2;
                        score7 -= weight * 2;
                        score1 += weight;
                        score2 += weight;
                        score3 += weight;
                    }
                    case 7 -> {
                        score7           -= weight * 3;
                        score6 -= weight * 2;
                        score0 -= weight * 2;
                        score2 += weight;
                        score3 += weight;
                        score4 += weight;
                    }
                    default -> {
                        assert false : "unexpected direction";
                    }
                }
            }
            for(int idx = 0; idx < RobotTracker.numCats; idx++) {
                var robot = RobotTracker.sortedCats[idx];
                if(robot == null) continue;
                var weight = 5;
                var dir = myLoc.directionTo(robot.location);
                switch(dir.ordinal()) {
                    case 0 -> {
                        score0           -= weight * 3;
                        score7 -= weight * 2;
                        score1 -= weight * 2;
                        score3 += weight;
                        score4 += weight;
                        score5 += weight;
                    }
                    case 1 -> {
                        score1           -= weight * 3;
                        score0 -= weight * 2;
                        score2 -= weight * 2;
                        score4 += weight;
                        score5 += weight;
                        score6 += weight;
                    }
                    case 2 -> {
                        score2           -= weight * 3;
                        score1 -= weight * 2;
                        score3 -= weight * 2;
                        score5 += weight;
                        score6 += weight;
                        score7 += weight;
                    }
                    case 3 -> {
                        score3           -= weight * 3;
                        score2 -= weight * 2;
                        score4 -= weight * 2;
                        score6 += weight;
                        score7 += weight;
                        score0 += weight;
                    }
                    case 4 -> {
                        score4           -= weight * 3;
                        score3 -= weight * 2;
                        score5 -= weight * 2;
                        score7 += weight;
                        score0 += weight;
                        score1 += weight;
                    }
                    case 5 -> {
                        score5           -= weight * 3;
                        score4 -= weight * 2;
                        score6 -= weight * 2;
                        score0 += weight;
                        score1 += weight;
                        score2 += weight;
                    }
                    case 6 -> {
                        score6           -= weight * 3;
                        score5 -= weight * 2;
                        score7 -= weight * 2;
                        score1 += weight;
                        score2 += weight;
                        score3 += weight;
                    }
                    case 7 -> {
                        score7           -= weight * 3;
                        score6 -= weight * 2;
                        score0 -= weight * 2;
                        score2 += weight;
                        score3 += weight;
                        score4 += weight;
                    }
                    default -> {
                        assert false : "unexpected direction";
                    }
                }
            }
            int best = 0;
            var move = Direction.CENTER;
            if(score0 > best && rc.canMove(Direction.NORTH)) {
                best = score0;
                move = Direction.NORTH;
            }
            if(score1 > best && rc.canMove(Direction.NORTHEAST)) {
                best = score1;
                move = Direction.NORTHEAST;
            }
            if(score2 > best && rc.canMove(Direction.EAST)) {
                best = score2;
                move = Direction.EAST;
            }
            if(score3 > best && rc.canMove(Direction.SOUTHEAST)) {
                best = score3;
                move = Direction.SOUTHEAST;
            }
            if(score4 > best && rc.canMove(Direction.SOUTH)) {
                best = score4;
                move = Direction.SOUTH;
            }
            if(score5 > best && rc.canMove(Direction.SOUTHWEST)) {
                best = score5;
                move = Direction.SOUTHWEST;
            }
            if(score6 > best && rc.canMove(Direction.WEST)) {
                best = score6;
                move = Direction.WEST;
            }
            if(score7 > best && rc.canMove(Direction.NORTHWEST)) {
                best = score7;
                move = Direction.NORTHWEST;
            }
            indicate("evading");
            if(move != Direction.CENTER) Pathfinder.makeMove(move);
        }
        if(rc.isActionReady()) {
            RobotInfo best = null;
            int bestHp = 1000000000;
            for(int idx = 0; idx < RobotTracker.numEnemies; idx++) {
                var robot = RobotTracker.sortedEnemies[idx];
                if(robot == null) continue;
                if(robot.location.distanceSquaredTo(myLoc) > 8) continue;
                if(robot.health < bestHp) {
                    best = robot;
                    bestHp = robot.health;
                }
            }
            if(best != null) {
                rc.attack(best.location);
            }
        }
        return true;
    }
}