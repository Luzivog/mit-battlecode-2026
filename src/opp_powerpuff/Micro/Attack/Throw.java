package opp_powerpuff.Micro.Attack;

import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import battlecode.common.*;

public class Throw {
    public static Direction[] attack;
    public static Direction[] kill;


    public static boolean cost() throws GameActionException {
        RobotController rc = Rat.rc;
        if (rc.getCarrying() == null) return false;
        
        MapLocation currLoc = rc.getLocation();

        attack = new Direction[9];
        kill = new Direction[9];
        
        boolean found = false;

        int enemiesProcessed = 0;
        for (RobotInfo enemy: VisionManager.enemies) {
            MapLocation enemyLoc = enemy.getLocation();
            
            // UNROLLED LOOP: 9 Iterations for movement directions
            {
                // Iteration 0 : NORTH
                Direction move = Direction.NORTH;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[0] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[0] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[0] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 1 : NORTHEAST
                Direction move = Direction.NORTHEAST;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[1] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[1] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[1] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 2 : EAST
                Direction move = Direction.EAST;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[2] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[2] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[2] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 3 : SOUTHEAST
                Direction move = Direction.SOUTHEAST;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[3] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[3] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[3] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 4 : SOUTH
                Direction move = Direction.SOUTH;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[4] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[4] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[4] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 5 : SOUTHWEST
                Direction move = Direction.SOUTHWEST;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[5] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[5] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[5] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 6 : WEST
                Direction move = Direction.WEST;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[6] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[6] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[6] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 7 : NORTHWEST
                Direction move = Direction.NORTHWEST;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[7] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[7] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[7] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 8 : CENTER
                Direction move = Direction.CENTER;
                MapLocation newLoc = currLoc.add(move);            
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);

                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    kill[8] == null &&
                    rc.onTheMap(newLoc) &&
                    inBetween.add(dirToEnemy).equals(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        Rat.rc.getDirection() == dirToEnemy ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health <= Constants.ONE_SHOT_THROW_THRESHOLD) {
                        kill[8] = newLoc.directionTo(enemyLoc);
                        found = true;
                    } else {
                        attack[8] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            enemiesProcessed++;
            if (enemiesProcessed >= 5) break;
        }
        
        return found;
    }
}