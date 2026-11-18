package opp_powerpuff.Micro.Attack;

import opp_powerpuff.Rat;
import opp_powerpuff.Managers.VisionManager;
import battlecode.common.*;

public class Carry {
    public static Direction[] attack;

    int lastCarried = 0;


    public static boolean cost() throws GameActionException {
        RobotController rc = Rat.rc;
        if (rc.getCarrying() != null) return false;
        MapLocation currLoc = rc.getLocation();

        attack = new Direction[9];

        Direction d = rc.getDirection();
        
        boolean found = false;
        int health = rc.getHealth();

        int enemiesProcessed = 0;
        for (RobotInfo enemy: VisionManager.enemies) {
            MapLocation enemyLoc = enemy.getLocation();
            Direction enemyFacing = enemy.getDirection();
            int enemyHealth = enemy.health;
            
            // UNROLLED LOOP: 9 Iterations for movement directions
            {
                // Iteration 0 : NORTH
                Direction move = Direction.NORTH;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[0] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[0] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 1 : NORTHEAST
                Direction move = Direction.NORTHEAST;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[1] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[1] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 2 : EAST
                Direction move = Direction.EAST;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[2] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[2] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 3 : SOUTHEAST
                Direction move = Direction.SOUTHEAST;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[3] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[3] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 4 : SOUTH
                Direction move = Direction.SOUTH;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[4] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[4] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 5 : SOUTHWEST
                Direction move = Direction.SOUTHWEST;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[5] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[5] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 6 : WEST
                Direction move = Direction.WEST;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[6] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[6] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 7 : NORTHWEST
                Direction move = Direction.NORTHWEST;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[7] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
                        attack[7] = newLoc.directionTo(enemyLoc);
                        found = true;
                    }
                }
            }
            {
                // Iteration 8 : CENTER
                Direction move = Direction.CENTER;
                
                // Logic inverted from 'continue' to 'if' block to strictly unroll without breaking the outer loop
                if (
                    attack[8] == null &&
                    move != d.opposite() &&
                    move != d.opposite().rotateLeft() &&
                    move != d.opposite().rotateRight()
                ) {
                    MapLocation newLoc = currLoc.add(move);
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        (enemyLoc.isAdjacentTo(newLoc)) &&
                        (
                            enemyHealth < health ||
                            (
                                enemyFacing != dirToEnemy.opposite() &&
                                enemyFacing.rotateLeft() != dirToEnemy.opposite() &&
                                enemyFacing.rotateRight() != dirToEnemy.opposite() 
                            )
                        ) &&
                        enemy.ID != Fight.lastCarried &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    )   {
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
}