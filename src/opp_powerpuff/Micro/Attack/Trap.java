package opp_powerpuff.Micro.Attack;

import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import battlecode.common.*;

public class Trap {
    public static Direction[] attack;


    public static boolean cost() throws GameActionException {
        RobotController rc = Rat.rc;
        if (rc.getCarrying() != null || !cheeseCondition()) return false;
        MapLocation currLoc = rc.getLocation();
        Direction d = rc.getDirection();

        attack = new Direction[9];
        boolean found = false;

        int enemiesProcessed = 0;
        for (RobotInfo enemy: VisionManager.enemies) {
            Direction enemyFacing = enemy.getDirection();
            MapLocation enemyLoc = enemy.getLocation();
            
            // UNROLLED LOOP: 9 Iterations for movement directions
            {
                // Iteration 0 : NORTH
                Direction move = Direction.NORTH;
                MapLocation newLoc = currLoc.add(move);
                Direction dirToEnemy = newLoc.directionTo(enemyLoc);  
                MapLocation inBetween = newLoc.add(dirToEnemy);
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[0] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[1] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[2] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[3] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[4] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[5] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[6] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[7] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
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
                
                if (
                    rc.onTheMap(newLoc) &&
                    MapData.mapInfos[inBetween.x][inBetween.y] != null &&
                    MapData.mapInfos[inBetween.x][inBetween.y].getTrap() == TrapType.RAT_TRAP
                ) return false;

                if (
                    rc.onTheMap(newLoc) &&
                    attack[8] == null &&
                    inBetween.isAdjacentTo(enemyLoc) &&
                    ! (
                        MapData.mapInfos[inBetween.x][inBetween.y] == null ||
                        !MapData.mapInfos[inBetween.x][inBetween.y].isPassable() || 
                        MapData.mapInfos[inBetween.x][inBetween.y].getTrap() != TrapType.NONE ||
                        (
                            rc.canSenseLocation(inBetween) &&
                            rc.senseRobotAtLocation(inBetween) != null
                        )
                    ) &&
                    (
                        enemyFacing == dirToEnemy.opposite() ||
                        enemyFacing.rotateLeft() == dirToEnemy.opposite() ||
                        enemyFacing.rotateRight() == dirToEnemy.opposite()
                    ) &&
                    cheeseCondition() &&
                    (
                        dirToEnemy.rotateLeft() == d ||
                        dirToEnemy == d ||
                        dirToEnemy.rotateRight() == d ||
                        rc.canTurn()
                    )
                ) {
                    if (enemy.health >= 50) {
                        attack[8] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }

            enemiesProcessed++;
            if (enemiesProcessed >= 6) break;
        }
        
        return found;
    }

    public static boolean cheeseCondition() {
        return (
            Rat.rc.getRawCheese() >= Constants.CHEESE_RETREAT_LIMIT ||
            Rat.rc.getCurrentRatCost() >= 20
        );
    }
}