package opp_powerpuff.Micro;

import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Comms;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Vision;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Micro.Attack.Decision;
import opp_powerpuff.Micro.Attack.Fight;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;

public class MicroBabyManager {
    public static MicroInfo[] microInfos = new MicroInfo[9];
    
    public static boolean doMicro() throws GameActionException {
        Rat.rc.setIndicatorDot(VisionManager.closestEnemy.getLocation(), 244, 244, 244);

        microInfos[0] = new MicroInfo(Direction.NORTH);
        microInfos[1] = new MicroInfo(Direction.NORTHEAST);
        microInfos[2] = new MicroInfo(Direction.EAST);
        microInfos[3] = new MicroInfo(Direction.SOUTHEAST);
        microInfos[4] = new MicroInfo(Direction.SOUTH);
        microInfos[5] = new MicroInfo(Direction.SOUTHWEST);
        microInfos[6] = new MicroInfo(Direction.WEST);
        microInfos[7] = new MicroInfo(Direction.NORTHWEST);
        microInfos[8] = new MicroInfo(Direction.CENTER);

        if (VisionManager.enemies.length >= 7) {
            VisionManager.enemies = Rat.rc.senseNearbyRobots(12, Rat.rc.getTeam().opponent());
        }
        Decision.calculate();

        // UNROLL BYTECODE AND UPDATE ALL MICROINFOS
        microInfos[0].updateEnemies();
        microInfos[0].updateCats();
        microInfos[0].updateCarry();
        microInfos[0].updateCooldowns();

        microInfos[1].updateEnemies();
        microInfos[1].updateCats();
        microInfos[1].updateCarry();
        microInfos[1].updateCooldowns();

        microInfos[2].updateEnemies();
        microInfos[2].updateCats();
        microInfos[2].updateCarry();
        microInfos[2].updateCooldowns();

        microInfos[3].updateEnemies();
        microInfos[3].updateCats();
        microInfos[3].updateCarry();
        microInfos[3].updateCooldowns();

        microInfos[4].updateEnemies();
        microInfos[4].updateCats();
        microInfos[4].updateCarry();
        microInfos[4].updateCooldowns();

        microInfos[5].updateEnemies();
        microInfos[5].updateCats();
        microInfos[5].updateCarry();
        microInfos[5].updateCooldowns();

        microInfos[6].updateEnemies();
        microInfos[6].updateCats();
        microInfos[6].updateCarry();
        microInfos[6].updateCooldowns();

        microInfos[7].updateEnemies();
        microInfos[7].updateCats();
        microInfos[7].updateCarry();
        microInfos[7].updateCooldowns();

        microInfos[8].updateEnemies();
        microInfos[8].updateCats();
        microInfos[8].updateCarry();
        microInfos[8].updateCooldowns(); 

        // COMPUTE BEST MOVE+ATTACK COMBINATION
        MicroInfo bestMicro = microInfos[8];
        if (microInfos[0].score() > bestMicro.score()) bestMicro = microInfos[0];
        if (microInfos[1].score() > bestMicro.score()) bestMicro = microInfos[1];
        if (microInfos[2].score() > bestMicro.score()) bestMicro = microInfos[2];
        if (microInfos[3].score() > bestMicro.score()) bestMicro = microInfos[3];
        if (microInfos[4].score() > bestMicro.score()) bestMicro = microInfos[4];
        if (microInfos[5].score() > bestMicro.score()) bestMicro = microInfos[5];
        if (microInfos[6].score() > bestMicro.score()) bestMicro = microInfos[6];
        if (microInfos[7].score() > bestMicro.score()) bestMicro = microInfos[7];
        if (microInfos[8].score() > bestMicro.score()) bestMicro = microInfos[8];
        
//         System.out.println(
//     microInfos[0].dir + ":" + microInfos[0].score() + ", " +
//     microInfos[1].dir + ":" + microInfos[1].score() + ", " +
//     microInfos[2].dir + ":" + microInfos[2].score() + ", " +
//     microInfos[3].dir + ":" + microInfos[3].score() + ", " +
//     microInfos[4].dir + ":" + microInfos[4].score() + ", " +
//     microInfos[5].dir + ":" + microInfos[5].score() + ", " +
//     microInfos[6].dir + ":" + microInfos[6].score() + ", " +
//     microInfos[7].dir + ":" + microInfos[7].score() + ", " +
//     microInfos[8].dir + ":" + microInfos[8].score()
// );

        // IF ATTACK+MOVE IS BETTER -> if best move == center, attack+move is better
        if (bestMicro.dir == Direction.CENTER) {
            // SHOULD WE RESET SCORES HERE?

            // do the action
            Fight.attack(Direction.CENTER, bestMicro.atkId, bestMicro.atkDir);
            
            // calculate new best move
            if (microInfos[0].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[0];
            if (microInfos[1].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[1];
            if (microInfos[2].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[2];
            if (microInfos[3].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[3];
            if (microInfos[4].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[4];
            if (microInfos[5].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[5];
            if (microInfos[6].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[6];
            if (microInfos[7].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[7];
            // if (microInfos[8].moveScore() > bestMicro.moveScore()) bestMicro = microInfos[8];
            // move
            if (bestMicro.dir != Direction.CENTER && Rat.rc.canMove(bestMicro.dir)) {
                Rat.rc.move(bestMicro.dir);
            }
        }
        else {
            Rat.rc.setIndicatorLine(Rat.rc.getLocation(), Rat.rc.getLocation().add(bestMicro.dir), 255, 0, 0);
            if (Rat.rc.canMove(bestMicro.dir)) {
                // Check if we can turn to the direction and still attack the opponent
                Fight.attack(bestMicro.dir, bestMicro.atkId, bestMicro.atkDir);
            }
            else
                Rat.rc.setIndicatorString("COULDN'T MOVE AT MICROMANAGER");
        }

        return true;
    }

    public static class MicroInfo {
        RobotInfo[] enemies;
        RobotInfo[] adjEnemies;
        RobotInfo closestEnemy;
        int estimatedAllies = 0;
        RobotInfo[] cats;
        RobotInfo[] adjCats;
        RobotInfo closestCat;
        Direction dir;
        MapLocation loc;
        boolean isAccessible = true;
        boolean actionReady = false;
        
        boolean isCarrying = false;
        boolean canThrow = false;

        public int moveScore = 0;
        public int atkId;
        public Direction atkDir;
        
        MicroInfo(Direction d) throws GameActionException {
            dir = d;
            if (d != Direction.CENTER && !Rat.rc.canMove(d)) {
                isAccessible = false;
            }
            actionReady = Rat.rc.isActionReady();
            if (Rat.rc.getCarrying() != null)
                isCarrying = true;
            canThrow = Rat.rc.canThrowRat();
            loc = Rat.rc.getLocation().add(dir);
            atkId = 100;
        }


        void updateAllies() throws GameActionException {
            // to be implemented once rats can learn how many allies are around them
        }
        void updateCooldowns() throws GameActionException {
            if (closestEnemy == null)
                return;
            if (actionReady)
                moveScore -= Math.max(9 - loc.distanceSquaredTo(closestEnemy.getLocation()), 0);
                if (dir == Direction.CENTER && Rat.distTo(closestEnemy.getLocation()) <= 2) moveScore += 50;
            else
                moveScore += Math.max(9 - loc.distanceSquaredTo(closestEnemy.getLocation()), 0);
        }
        void updateCarry() throws GameActionException {
            if (Rat.rc.getCarrying() != null)
                isCarrying = true;
            if (Rat.rc.canThrowRat())
                canThrow = true;
            if (closestEnemy == null)
                return;
            if (isCarrying && !canThrow)
                moveScore -= Math.max(9 - loc.distanceSquaredTo(closestEnemy.getLocation()), 0);
        }
        void updateEnemies() throws GameActionException {
            enemies = VisionManager.enemies;
            adjEnemies = Vision.squareSurroundingRobots(loc, enemies);
            closestEnemy = Vision.getClosestRobot(loc, enemies);
            estimatedAllies = Math.max(Rat.rc.readSqueaks(Rat.rc.getRoundNum() - 1).length, Rat.rc.readSqueaks(Rat.rc.getRoundNum()).length);
            //System.out.println(estimatedAllies);
            MapLocation cen = Rat.rc.getLocation();
            Direction dirToEnemy = cen.directionTo(VisionManager.closestEnemy.getLocation());
            // center
            if (loc.equals(cen)) {
                moveScore += 0;
            }
            // square closer to enemy
            if (loc.equals(cen.add(dirToEnemy))) {
                if (closestEnemy.getType() == UnitType.RAT_KING) {
                    moveScore += 500;
                }
                // we should always try to move to enemy, so give a little incentive to move forward, should be more than the cost of one rat
                moveScore += 70;
                moveScore -= 50 * (enemies.length - Math.max(estimatedAllies - 1, 0));
                if (closestEnemy.getHealth() <= Rat.rc.getHealth()) {
                    moveScore += 10;
                }
            }
            // square closer to enemy on left and on right 
            else if (loc.equals(cen.add(dirToEnemy.rotateLeft())) || loc.equals(cen.add(dirToEnemy.rotateRight()))) {
                if (closestEnemy.getType() == UnitType.RAT_KING) {
                    moveScore += 400;
                }
                moveScore += 60;
                moveScore -= 45 * (enemies.length - Math.max(estimatedAllies - 1, 0));
                if (closestEnemy.getHealth() <= Rat.rc.getHealth()) {
                    moveScore += 10;
                }
            }
            // square is away from enemy
            else if (loc.equals(cen.add(dirToEnemy.opposite()))) {
                moveScore -= 50;
                moveScore += 50 * (enemies.length - Math.max(estimatedAllies - 1, 0));
            }
            else if (loc.equals(cen.add(dirToEnemy.opposite().rotateLeft())) || loc.equals(cen.add(dirToEnemy.opposite().rotateRight()))) {
                moveScore -= 45;
                moveScore += 45 * (enemies.length - Math.max(estimatedAllies - 1, 0));
            }
            // 90 left or 90 right
            // if moving forward, go to the square with the least amount of enemies around it
            moveScore -= adjEnemies.length * 10;
            // go to a square where enemy isn't looking
            if (closestEnemy != null && !Vision.ratIsFacingLocation(loc, closestEnemy))
                moveScore += 40;
        }
        void updateCats() throws GameActionException {
            cats = VisionManager.cats;
            adjCats = Vision.squareSurroundingRobots(loc, cats);
            closestCat = Vision.getClosestRobot(loc, cats);
            if (closestCat != null)
                moveScore += cats.length * loc.distanceSquaredTo(closestCat.getLocation());
        }


        // scoring functions
        int moveScore() {
            if (!isAccessible)
                return -100000;
            return moveScore;
        }

        int attackScore() throws GameActionException {
            // if no attack available on the square
            if (atkId == 100 || !Rat.rc.isActionReady())
                return 0;
            if (atkId == 4) {
                if (Rat.rc.onTheMap(loc)) Rat.rc.setIndicatorDot(loc, 0, 255, 0);
                return 30;      // Bite
            }            
            if (atkId == 3) {
                if (Rat.rc.onTheMap(loc)) Rat.rc.setIndicatorDot(loc, 0, 255, 0);
                return 70;      // Trap
            }
            if (atkId == 2) {
                if (Rat.rc.onTheMap(loc)) Rat.rc.setIndicatorDot(loc, 255, 0, 255);
                return 80;      // Carry
            }
            if (atkId == 1) {
                if (Rat.rc.onTheMap(loc)) Rat.rc.setIndicatorDot(loc, 0, 255, 0);
                return 100;      // Kill with Bite
            }
            if (atkId == 0) {
                if (Rat.rc.onTheMap(loc)) Rat.rc.setIndicatorDot(loc, 255, 0, 0);
                return 120;     // Throw
            }





            return 0;
        }

        int score() throws GameActionException {
            return moveScore() + attackScore();
        }
    }
}
