package opp_powerpuff.Micro.Attack;

import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import battlecode.common.*;

public class Bite {
    public static Direction[] attack;
    public static Direction[] kill;

public static boolean cost() throws GameActionException {
        RobotController rc = Rat.rc;
        MapLocation currLoc = rc.getLocation();

        attack = new Direction[9];
        kill = new Direction[9];

        Direction d = rc.getDirection();
        
        boolean found = false;

        int enemiesProcessed = 0;
        for (RobotInfo enemy: VisionManager.enemies) {
            // Unrolled logic for each direction
            {
                int i = 0;
                Direction move = Direction.NORTH;  
                if (
                    kill[0] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 1;
                Direction move = Direction.NORTHEAST;  
                if (
                    kill[1] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 2;
                Direction move = Direction.EAST;  
                if (
                    kill[2] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 3;
                Direction move = Direction.SOUTHEAST;  
                if (
                    kill[3] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 4;
                Direction move = Direction.SOUTH;  
                if (
                    kill[4] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 5;
                Direction move = Direction.SOUTHWEST;  
                if (
                    kill[5] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 6;
                Direction move = Direction.WEST;  
                if (
                    kill[6] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 7;
                Direction move = Direction.NORTHWEST;  
                if (
                    kill[7] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            {
                int i = 8;
                Direction move = Direction.CENTER;  
                if (
                    kill[8] == null &&
                    !(move == d.opposite()) &&
                    !(move == d.opposite().rotateLeft()) &&
                    !(move == d.opposite().rotateRight())
                ) {             
                    MapLocation newLoc = currLoc.add(move);
                    MapLocation enemyLoc = enemy.getLocation();
                    Direction dirToEnemy = newLoc.directionTo(enemyLoc);
                    if (
                        enemyLoc.isAdjacentTo(newLoc) &&
                        (
                            dirToEnemy.rotateLeft() == d ||
                            dirToEnemy == d ||
                            dirToEnemy.rotateRight() == d ||
                            rc.canTurn()
                        )
                    ) {
                        if (enemy.health <= Constants.ONE_SHOT_BITE_THRESHOLD) {
                            kill[i] = newLoc.directionTo(enemyLoc);
                            found = true;
                        } else {
                            attack[i] = newLoc.directionTo(enemyLoc);
                        }
                    }
                }
            }
            enemiesProcessed++;
            if (enemiesProcessed >= 6) break;
        }
        
        return found;
    }
}