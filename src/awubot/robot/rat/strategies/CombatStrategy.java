package awubot.robot.rat.strategies;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import experiment.CellTracker;
import awubot.Pathfinder;
import awubot.comms.Squeaker;
import awubot.robot.Strategy;
import awubot.util.Util;

import static awubot.Game.*;
import static awubot.util.Util.allDirections;
import static awubot.util.Util.canSense;
import static awubot.util.Util.chebyshevDistance;
import static awubot.util.Util.directions;
import static awubot.util.Util.orthDirections;
import static awubot.util.Util.println;
import static awubot.util.Util.relatedDirections;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

/*

score every move
1 score approximately should equal 1 damage to another rat

*/

public class CombatStrategy {
    private static final double INF = 1e9;

    public CombatStrategy() {

    }

    /*
    
    the returned score should be a multiplier for any "damage" dealt to it
    
    */

    private static final int RAT_TRAP_COST = 30;
    private static final int RAT_TRAP_CAP = 25;

    // in cheese units
    public double trapCost() {
        return RAT_TRAP_COST; // tf i cant see how many rat traps i have placed?
    }

    // idek why this is returning. check if can move anyways after to squeeze out some juice
    public boolean tryTick() throws GameActionException {
        if (rc.getCarrying() != null) {
            return tryThrow();
        }
        if (tryRatNap()) return true;
        if (tryTrap()) return true;
        if (tryAttack()) return true;
        // TODO: default to avoidance behaviour
        return false;
    }

    // return false if didnt throw -- some other process can figure out how to move
    // TODO: maybe weight targets based on how close they are to friendly rats? might be too effort
    public boolean tryThrow() throws GameActionException {
        println("trying throw");
        Direction md = null, td = null;
        int mt = -1;
        double best = -1;
        for (Direction moveDir : allDirections) {
            if (moveDir != Direction.CENTER && !rc.canMove(moveDir)) continue;
            MapLocation newLoc = myLoc.add(moveDir);
            for (Direction throwDir : directions) {
                int moveType = -1;
                if (moveDir == Direction.CENTER && rc.canTurn()) moveType = 4;
                else if (moveDir == throwDir && (moveDir == myDir || rc.canTurn())) moveType = 0; // (turn or not turn) + straight forward
                else if (moveDir == myDir && rc.canTurn()) moveType = 1; // forward + turn throw
                else if (throwDir == myDir) moveType = 2; // strafe + forward
                else if (rc.canTurn()) moveType = 3; // strafe + turn + throw 
                if (moveType == -1) continue;

                MapLocation throwTarget = newLoc.add(throwDir); 
                if (!rc.canSenseLocation(throwTarget)) continue; // make sure u know whats happening there
                if (!rc.senseMapInfo(throwTarget).isPassable()) continue; // need empty
                // TODO: why does this line below not work?
                // if (!CellTracker.isPassable(throwTarget)) continue; // need empty
                if (throwTarget != myLoc && rc.senseRobotAtLocation(throwTarget) != null) continue; // need empty
                double damage = -1;
                // for now, allow the throw path to go through friendly rats
                for (int rem = 7; rem >= 0; rem--) {
                    throwTarget = throwTarget.add(throwDir);
                    if (!rc.onTheMap(throwTarget)) {
                        damage = rem;
                        break;
                    }
                    if (rc.canSenseLocation(throwTarget)) {
                        if (!rc.senseMapInfo(throwTarget).isPassable()) {
                            damage = rem;
                            break;
                        }
                    } else break;
                }
                if (damage < 0) continue;
                println("yayyy");
                switch (moveType) {
                    case 0:
                    case 1:
                        damage *= 1.5;
                        break;
                    case 2:
                    case 3:
                        break;
                    case 4:
                        damage *= 1.1;
                        break;
                }
                if (damage > best) {
                    best = damage;
                    md = moveDir;
                    td = throwDir;
                    mt = moveType;
                }
            }
        }
        if (best < 0) return false;
        println("THROWING RAT");
        switch (mt) {
            case 0: // forward
                if (myDir != md) turn(md);
                rawMove();
                break;
            case 1:
                rawMove();
                turn(td);
                break;
            case 2:
                rawMove(md);
                break;
            case 3:
                rawMove(md);
                turn(td);
            break;
            case 4:
                rc.turn(td);
                break;
            default:
                assert(false);
        }
        rc.throwRat();
        return true;
    }

    // return true if successfully ratnapped someone
    // TODO: do danger levels and verify you dont die on the next turn
    public boolean tryRatNap() throws GameActionException {
        if (!rc.isActionReady()) return false;
        RobotInfo target = null;
        Direction bestMove = null;
        double bestScore = -1;
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == team) continue;
            if (chebyshevDistance(myLoc, info.getLocation()) > 2) continue; 
            if (info.getHealth() >= rc.getHealth()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            MapLocation t = info.getLocation();
            // double weight = scoreEnemy(info);
            double weight = 1;
            Direction md = null;

            println("found nearby rat that is tasty\n");
            double best = -1;
            for (Direction moveDir : allDirections) {
                if (moveDir != Direction.CENTER && !rc.canMove(moveDir)) continue;
                MapLocation newLoc = myLoc.add(moveDir); 
                if (newLoc.distanceSquaredTo(t) > 2) continue; 
                if (!canSense(newLoc, t, myDir) && !rc.canTurn()) continue;
                println("passed checks");
                double score = weight;
                // TODO: account for cell danger levels
                if (score > best) {
                    best = score;
                    md = moveDir;
                }
            }
            if (best > bestScore) {
                bestScore = best;
                target = info;
                bestMove = md;
            }
        }
        if (bestScore < 0) return false; 
        if (bestMove != Direction.CENTER) rawMove(bestMove);
        if (!rc.canSenseLocation(target.getLocation())) turn(myLoc.directionTo(target.getLocation()));
        rc.carryRat(target.getLocation());
        println("napped rat");
        return true;
    }

    public boolean tryTrap() throws GameActionException {
        boolean confirmedCanPlace = false;
        for (MapInfo info : rc.senseNearbyMapInfos(2)) {
            if (rc.canPlaceRatTrap(info.getMapLocation())) {
                confirmedCanPlace = true;
                break;
            }
        }
        if (!confirmedCanPlace) return false;
        MapLocation bestPlaceLoc = null;
        Direction bestMoveDir = null;
        double best = -1;

        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() != UnitType.BABY_RAT) continue;
            for (Direction dir : relatedDirections(info.getDirection())) {
                MapLocation placeLoc = info.getLocation().add(dir);
                if (!rc.canSenseLocation(placeLoc)) continue;
                if (!rc.senseMapInfo(placeLoc).isPassable()) continue;
                if (rc.senseMapInfo(placeLoc).hasCheeseMine()) continue;
                if (rc.senseRobotAtLocation(placeLoc) != null) continue;
                if (dir == info.getDirection().opposite()) continue;
                if (placeLoc.distanceSquaredTo(myLoc) > 4) continue; // impossible
                for (Direction moveDir : allDirections) {
                    if (moveDir != Direction.CENTER && !rc.canMove(moveDir)) continue;
                    MapLocation newLoc = myLoc.add(moveDir);
                    if (newLoc == placeLoc) continue;
                    if (newLoc.distanceSquaredTo(placeLoc) > 2) continue; // unreachable
                    if (canSense(newLoc, placeLoc, moveDir == Direction.CENTER ? myDir : moveDir)) {
                        // dont need strafe move
                        double score = 1.2 * newLoc.distanceSquaredTo(info.getLocation());
                        if (score > best) {
                            best = score;
                            bestPlaceLoc = placeLoc;
                            bestMoveDir = moveDir;                            
                        }
                    } else if (rc.canTurn(moveDir)) {
                        // need strafe move
                        double score = newLoc.distanceSquaredTo(info.getLocation());
                        if (score > best) {
                            best = score;
                            bestPlaceLoc = placeLoc;
                            bestMoveDir = moveDir;                            
                        }
                    }
                }
            }
        }

        if (best < 0) return false;
        println("TRAPPING RAT");
        if (bestMoveDir != Direction.CENTER) rawMove(bestMoveDir);
        if (!rc.canSenseLocation(bestPlaceLoc)) turn(myLoc.directionTo(bestPlaceLoc));
        rc.placeRatTrap(bestPlaceLoc);
        return true;
    }

    /*
    
    attacking another solo rat is safe as if:
        - you end the turn with at least as much health (no ratnap)
        - they can't see you
    if you are in a horde, it should be safe to run at them
    
    */
    public boolean tryAttack() throws GameActionException {
        if (!rc.isActionReady()) return false;
        MapLocation bestTarget = null;
        Direction bestMoveDir = Direction.CENTER;
        int bestAction = -1;
        double bestScore = -1;
        boolean canMove = rc.isMovementReady();
        boolean canTurn = rc.canTurn();
        boolean canMoveForward = canMove && rc.canMoveForward();
        for (RobotInfo info : rc.senseNearbyRobots()) {
            if (info.getTeam() == rc.getTeam()) continue;
            if (info.getType() == UnitType.CAT) continue;
            // account for buffed bites later
            if (Squeaker.squeaksSinceLastTurn == 0 
                && info.getType() == UnitType.BABY_RAT 
                && info.getHealth() - GameConstants.RAT_BITE_DAMAGE >= rc.getHealth()) continue;
            MapLocation target = info.getLocation();
            double baseScore = scoreEnemy(info);
            if (rc.canAttack(target)) {
                double score = 1.4 * baseScore;
                if (score > bestScore) {
                    bestScore = score;
                    bestTarget = target;
                    bestMoveDir = Direction.CENTER;
                    bestAction = 0;
                }
            }
            if (!canMove) continue;
            if (canMoveForward) { // try attack without turn
                MapLocation newLoc = myLoc.add(myDir);
                if (newLoc.distanceSquaredTo(target) <= 2) {
                    boolean canSenseForward = canSense(newLoc, target, myDir);
                    if (canSenseForward || canTurn) {
                        double score = (canSenseForward ? 1.2 : 1.0) * baseScore;
                        if (score > bestScore) {
                            bestScore = score;
                            bestTarget = target;
                            bestMoveDir = myDir;
                            bestAction = canSenseForward ? 1 : 2;
                        }
                    }
                }
            }
            if (canTurn) {
                for (Direction dir : directions) {
                    if (dir == myDir) continue;
                    if (!rc.canMove(dir)) continue;
                    MapLocation newLoc = myLoc.add(dir);
                    if (newLoc.distanceSquaredTo(target) > 2) continue;
                    if (!canSense(newLoc, target, dir)) continue;
                    double score = baseScore;
                    if (score > bestScore) {
                        bestScore = score;
                        bestTarget = target;
                        bestMoveDir = dir;
                        bestAction = 3;
                    }
                }
            }
        }
        switch (bestAction) {
            case 0:
                rc.attack(bestTarget);
                return true;
            case 1:
                rawMove();
                rc.attack(bestTarget);
                return true;
            case 2:
                rawMove();
                if (!rc.canSenseLocation(bestTarget)) turn(myLoc.directionTo(bestTarget));
                rc.attack(bestTarget);
                return true;
            case 3:
                turn(bestMoveDir);
                rawMove();
                rc.attack(bestTarget);
                return true;
            default:
                return false;
        }
    }

    // currently given arbitrary weights
    public double scoreEnemy(RobotInfo info) {
        if (info.getType() == UnitType.RAT_KING) return 5;
        // one tap this guy
        if (info.getHealth() - GameConstants.RAT_BITE_DAMAGE <= 0) {
            return 3.0;
        }
        if (info.getHealth() <= 30) { 
            // try to get rid of low hp rats
            return 1.3;
        }
        if (info.getHealth() >= rc.getHealth()) {
            // try to bring enemy rats into napping distance
            return 1.2;
        }
        return 1;
    }

    // currently given arbitrary weights
    public double enemyDanger(MapLocation s, RobotInfo info) {
        if (info.getType() == UnitType.RAT_KING) return 0.1;
        int dist = chebyshevDistance(s, info.getLocation());
        if (info.getHealth() > rc.getHealth() && dist <= 2) {
            return 3;
        }
        double mult = (1.0 / Math.sqrt(dist)); // incredible formula
        if (rc.getHealth() > 1.5 * info.getHealth()) {
            mult = 0.7;
            // u can beat them
        }
        return mult; 
    } 

    public static int getCheeseNeeded(int damageBuff) {
        if (damageBuff == 0) return 0;
        int base = damageBuff - 1;
        return base * base + 1;
    }
}
