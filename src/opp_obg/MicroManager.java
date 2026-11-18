package opp_obg;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.TrapType;

/**
 * Micro manager
 */

//TODO: ignore everything not reachable with BFS dist.


public class MicroManager {

    static Direction[] dirs = Direction.values();
    static final int myRange = 2;
    static final int maxRange = 18;
    static final int MAX_CHEESE = 20;
    static boolean shouldRetreat;
    static boolean canAttack;
    static boolean tooMuchCheese;

    static MicroInfo microInfo0;
    static MicroInfo microInfo1;
    static MicroInfo microInfo2;
    static MicroInfo microInfo3;
    static MicroInfo microInfo4;
    static MicroInfo microInfo5;
    static MicroInfo microInfo6;
    static MicroInfo microInfo7;
    static MicroInfo microInfo8;

    static MapLocation kingLoc;

    static MicroInfo bestMicro;

    static boolean shouldPutTrap;

    /**
     * Moves according to the best micro direction when there are enemies close enough.
     */
    static boolean doMicro(){
        try {
            canAttack = MyUnit.rc.isActionReady();
            shouldRetreat = tooMuchCheese || MyUnit.isKing;
            shouldPutTrap = canAttack && shouldPlaceTrap();

            //MyUnit.rc.setIndicatorString("Should put trap: " + shouldPutTrap);


            if (MyUnit.isKing){
                if (microInfo8.closestCat != null){
                    fleeFromCats();
                    return true;
                }
            }
            else if (CatSurveillance.lastCatSeen.loc != null && CatSurveillance.round + CatSurveillance.CACHE_CAT_TURNS >= MyUnit.rc.getRoundNum()){
                addCat(CatSurveillance.lastCatSeen.loc, CatSurveillance.lastCatSeen.id, CatSurveillance.lastCatSeen.dir);
            }

            if (!microInfo8.seenByCat() && microInfo8.closestEnemy == null) return false;

            //MyUnit.rc.setIndicatorString("MICRO" + " " + microInfo0.safe() + " " + microInfo1.safe()+  " " + microInfo2.safe()+ " " + microInfo3.safe()+  " " + microInfo4.safe()+ " " + microInfo5.safe()+ " " + microInfo6.safe()+ " " + microInfo7.safe()+ " " + microInfo8.safe());
            if (microInfo8.closestEnemy != null) MyUnit.rc.setIndicatorDot(microInfo8.closestEnemy, 0, 0, 255);

            //MyUnit.rc.setIndicatorString("MICRO" + " " + microInfo0.minDistToCat + " " + microInfo1.minDistToCat+  " " + microInfo2.minDistToCat+ " " + microInfo3.minDistToCat+  " " + microInfo4.minDistToCat+ " " + microInfo5.minDistToCat+ " " + microInfo6.minDistToCat+ " " + microInfo7.minDistToCat+ " " + microInfo8.minDistToCat);

            bestMicro = microInfo8;

            if (microInfo7.isBetter(bestMicro)) bestMicro = microInfo7;
            if (microInfo6.isBetter(bestMicro)) bestMicro = microInfo6;
            if (microInfo5.isBetter(bestMicro)) bestMicro = microInfo5;
            if (microInfo4.isBetter(bestMicro)) bestMicro = microInfo4;
            if (microInfo3.isBetter(bestMicro)) bestMicro = microInfo3;
            if (microInfo2.isBetter(bestMicro)) bestMicro = microInfo2;
            if (microInfo1.isBetter(bestMicro)) bestMicro = microInfo1;
            if (microInfo0.isBetter(bestMicro)) bestMicro = microInfo0;


            //MyUnit.rc.setIndicatorString(bestMicro.dir.name());

            performMovement();


            //checkTrap();

            //if (bestMicro.closestEnemy != null) MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestEnemy, false);
            //else MovementManager.moveFreeDir(bestMicro.dir);

            //MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestEnemy == null ? bestMicro.closestCat : bestMicro.closestEnemy);

        } catch (Exception e){
            e.printStackTrace();
        }
        return true;
    }

    static void performMovement() throws GameActionException {
        //checkTrap();

        if (bestMicro.minDistanceToEnemy <= 2){
            MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestEnemy, false, false);
            return;
        }
        else if (bestMicro.minDistanceToEnemy <= 8){
            if (bestMicro.shouldPutTrap()){
                //if (MyUnit.rc.canPlaceRatTrap(bestMicro.location)) MyUnit.rc.placeRatTrap(bestMicro.location);
                //MyUnit.rc.setIndicatorString("" + bestMicro.maxHealthMoveRange);
                //MyUnit.rc.setIndicatorString("Placing trap :D");
                MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestEnemy, false, false);
                Direction dir = MovementManager.myLoc.directionTo(bestMicro.closestEnemy);
                if (MyUnit.rc.canPlaceRatTrap(MovementManager.myLoc.add(dir))) MyUnit.rc.placeRatTrap(MovementManager.myLoc.add(dir));
                return;
            }
        }
        else if (microInfo8.minDistanceToEnemy > 2 && microInfo8.minDistanceToEnemy <= 8 && microInfo8.shouldPutTrap()){
            Direction dir = MovementManager.myLoc.directionTo(microInfo8.closestEnemy);
            if (MyUnit.rc.canPlaceRatTrap(MovementManager.myLoc.add(dir))) MyUnit.rc.placeRatTrap(MovementManager.myLoc.add(dir));
        }
        if (bestMicro.closestEnemy != null) MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestEnemy, false, false);
        else{
            if (bestMicro.minDistToCat <= 50){
                //MyUnit.rc.setIndicatorString("Entering here :D");
                MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestCat, false, false);
            }
            else MovementManager.moveFreeDir(bestMicro.dir);
        }
    }

    static boolean shouldPlaceTrap(){
        if (VisionManager.trap) return false;
        if (MyUnit.rc.getRawCheese() > 0) return true;
        return MyUnit.rc.getGlobalCheese() >= 500 || (MyUnit.rc.getCurrentRatCost() >= 40 && MyUnit.rc.getGlobalCheese() >= 300);
    }

    static void checkTrap() throws GameActionException {
        if (!MyUnit.rc.isActionReady()) return;
        if (!shouldPlaceTrap()) return;
        if (bestMicro.minDistanceToEnemy <= microInfo8.minDistanceToEnemy) return;
        if (bestMicro.minDistanceToEnemy <= 2) return;
        if (microInfo8.minDistanceToEnemy >= 8) return;
        if (microInfo8.enemiesInMoveRange < bestMicro.alliesDist2) return;
        if (MyUnit.rc.getRawCheese() == 0 && MyUnit.rc.getGlobalCheese() < 300) return;
        if (MyUnit.rc.getRawCheese() == 0 && (VisionManager.closestRat == null || VisionManager.closestRat.getHealth() < 80)) return;
        Direction dir = MovementManager.myLoc.directionTo(microInfo8.closestEnemy);
        MapLocation trapLoc = MovementManager.myLoc.add(dir);
        if (MyUnit.rc.canPlaceRatTrap(trapLoc)) MyUnit.rc.placeRatTrap(trapLoc);
    }

    static void fleeFromCats() throws GameActionException {
        //TODO. Maybe there is more than one cat???

        ExplorationManager.resetDirection();

        MicroInfo bestMicro = microInfo8;

        if (microInfo7.isBetterCat(bestMicro)) bestMicro = microInfo7;
        if (microInfo6.isBetterCat(bestMicro)) bestMicro = microInfo6;
        if (microInfo5.isBetterCat(bestMicro)) bestMicro = microInfo5;
        if (microInfo4.isBetterCat(bestMicro)) bestMicro = microInfo4;
        if (microInfo3.isBetterCat(bestMicro)) bestMicro = microInfo3;
        if (microInfo2.isBetterCat(bestMicro)) bestMicro = microInfo2;
        if (microInfo1.isBetterCat(bestMicro)) bestMicro = microInfo1;
        if (microInfo0.isBetterCat(bestMicro)) bestMicro = microInfo0;

        MovementManager.moveWhileLooking(bestMicro.dir, bestMicro.closestCat, false);
    }

    static MapLocation unitLocation;
    static Direction unitDir;
    static FastIntSet AllyIDs = new FastIntSet();
    static FastIntSet EnemyIDs = new FastIntSet();
    static FastIntSet CatIDs = new FastIntSet();
    static final int unitVal = 1;
    static int healthDiff = 0;
    static int enemyHealth;
    static boolean lessHealth, moreHealth, equalHealth;

    static void roundInit() throws GameActionException {
        kingLoc = CommunicationManager.getKingLocation();
        microInfo0 = new MicroInfo(dirs[0]);
        microInfo1 = new MicroInfo(dirs[1]);
        microInfo2 = new MicroInfo(dirs[2]);
        microInfo3 = new MicroInfo(dirs[3]);
        microInfo4 = new MicroInfo(dirs[4]);
        microInfo5 = new MicroInfo(dirs[5]);
        microInfo6 = new MicroInfo(dirs[6]);
        microInfo7 = new MicroInfo(dirs[7]);
        microInfo8 = new MicroInfo(dirs[8]);
        AllyIDs.clear();
        AllyIDs.add(MyUnit.rc.getID()); //Don't want to count myself twice
        EnemyIDs.clear();
        CatIDs.clear();
        tooMuchCheese = MyUnit.rc.getRawCheese() > MAX_CHEESE;
    }

    static void addEnemy(MapLocation enemyLoc, int enemyID, Direction facingDir, int HP) throws GameActionException {
        if (EnemyIDs.contains(enemyID)) return;
        unitLocation = enemyLoc;
        EnemyIDs.add(enemyID);
        int d = unitLocation.distanceSquaredTo(MovementManager.myLoc);
        if (d > 20) return;
        if (tooMuchCheese && unitLocation.distanceSquaredTo(MovementManager.myLoc) > 18) return;
        unitDir = facingDir;
        if (MyUnit.isKing){
            microInfo0.updateEnemy();
            microInfo1.updateEnemy();
            microInfo2.updateEnemy();
            microInfo3.updateEnemy();
            microInfo4.updateEnemy();
            microInfo5.updateEnemy();
            microInfo6.updateEnemy();
            microInfo7.updateEnemy();
            microInfo8.updateEnemy();
        } else{
            enemyHealth = HP;
            healthDiff = MyUnit.health - HP;
            lessHealth = healthDiff < 0;
            moreHealth = healthDiff > 0;
            equalHealth = healthDiff == 0;
            ObservationUtil.addEnemy();
        }
    }

    static void addAlly(MapLocation allyLoc, int allyID) throws GameActionException {
        if (MyUnit.isKing) return;
        if (AllyIDs.contains(allyID)) return;
        //unitLocation = allyLoc;
        AllyIDs.add(allyID);

        switch(allyLoc.x - MovementManager.myLoc.x){
            case -2:
                switch(allyLoc.y - MovementManager.myLoc.y){
                    case -2:
                        microInfo5.alliesDist2+=1;
                        break;
                    case -1:
                        microInfo5.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        break;
                    case 0:
                        microInfo5.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo7.alliesDist2+=1;
                        break;
                    case 1:
                        microInfo6.alliesDist2+=1;
                        microInfo7.alliesDist2+=1;
                        break;
                }
                break;
            case -1:
                switch(allyLoc.y - MovementManager.myLoc.y){
                    case -2:
                        microInfo4.alliesDist2+=1;
                        microInfo5.alliesDist2+=1;
                        break;
                    case -1:
                        microInfo4.alliesDist2+=1;
                        microInfo5.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                    case 0:
                        microInfo0.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        microInfo5.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo7.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                    case 1:
                        microInfo0.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo7.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                }
                break;
            case 0:
                switch(allyLoc.y - MovementManager.myLoc.y){
                    case -2:
                        microInfo3.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        microInfo5.alliesDist2+=1;
                        break;
                    case -1:
                        microInfo2.alliesDist2+=1;
                        microInfo3.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        microInfo5.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                    case 0:
                        microInfo0.alliesDist2+=1;
                        microInfo1.alliesDist2+=1;
                        microInfo2.alliesDist2+=1;
                        microInfo3.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        microInfo5.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo7.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                    case 1:
                        microInfo0.alliesDist2+=1;
                        microInfo1.alliesDist2+=1;
                        microInfo2.alliesDist2+=1;
                        microInfo6.alliesDist2+=1;
                        microInfo7.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                }
                break;
            case 1:
                switch(allyLoc.y - MovementManager.myLoc.y){
                    case -2:
                        microInfo3.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        break;
                    case -1:
                        microInfo2.alliesDist2+=1;
                        microInfo3.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                    case 0:
                        microInfo0.alliesDist2+=1;
                        microInfo1.alliesDist2+=1;
                        microInfo2.alliesDist2+=1;
                        microInfo3.alliesDist2+=1;
                        microInfo4.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                    case 1:
                        microInfo0.alliesDist2+=1;
                        microInfo1.alliesDist2+=1;
                        microInfo2.alliesDist2+=1;
                        microInfo8.alliesDist2+=1;
                        break;
                }
                break;
        }

    }

    static void addCat(MapLocation catLoc, int catID, Direction dir) throws GameActionException {
        if (CatIDs.contains(catID)) return;
        CatIDs.add(catID);
        unitLocation = catLoc;
        unitDir = dir;

        if (MyUnit.isKing) {
            unitLocation = Util.getClosestCatLoc(catLoc);
            microInfo0.updateCat();
            microInfo1.updateCat();
            microInfo2.updateCat();
            microInfo3.updateCat();
            microInfo4.updateCat();
            microInfo5.updateCat();
            microInfo6.updateCat();
            microInfo7.updateCat();
            microInfo8.updateCat();
        } else{
            if (BabyRat.catHunter()) return;
            ObservationUtil.addCat();
        }
    }

    static class MicroInfo{
        Direction dir;
        MapLocation location;
        int minDistanceToEnemy = Constants.INF;
        int enemiesInRange = 0;
        int enemiesInMoveRange = 0;
        int alliesDist2 = 1;
        int minDistToCat = Constants.INF;
        boolean canPlaceTrap;
        boolean forbidden = false;
        int kingDist;
        int minHealthAtkRange = 1000, maxHealthAtkRange = -1;
        int minHealthMoveRange = 1000, maxHealthMoveRange = -1;
        Integer safe = null;
        MapLocation closestEnemy = null;
        MapLocation closestCat = null;

        boolean canMove = true;

        public MicroInfo(Direction dir) throws GameActionException {
            this.dir = dir;
            this.location = MyUnit.rc.getLocation().add(dir);
            kingDist = maxDist(location, kingLoc);
            if (dir != Direction.CENTER && !MovementManager.canMoveNoChecks(dir)) canMove = false;
            if (MovementManager.forbidden[dir.ordinal()]) forbidden = true;
            //if (canAttack) alliesDist2++;
            if (tooMuchCheese) alliesDist2 = 0;
            canPlaceTrap = MyUnit.rc.canPlaceRatTrap(location);
            if (MyUnit.rc.canSenseLocation(location) && MyUnit.rc.senseMapInfo(location).getTrap() == TrapType.RAT_TRAP) ++alliesDist2;
        }

        void updateEnemy(){
            int dist = unitLocation.distanceSquaredTo(location);
            if (dist < minDistanceToEnemy){
                minDistanceToEnemy = dist;
                closestEnemy = unitLocation;
            }
            switch(dist){
                case 0:
                case 1:
                case 2:
                    enemiesInMoveRange += unitVal;
                    enemiesInRange += unitVal;
                    break;
                case 3:
                case 4:
                case 5:
                case 6:
                case 7:
                case 8:
                    enemiesInMoveRange += unitVal;
                    break;
            }
        }

        void updateAlly(){
            if (unitLocation.distanceSquaredTo(location) <= 2) ++alliesDist2;
        }

        void updateCat(){
            int dist = unitLocation.distanceSquaredTo(location);
            if (dist < minDistToCat){
                minDistToCat = dist;
                closestCat = unitLocation;
            }
        }

        boolean shouldPutTrap(){
            if (MyUnit.isKing || !shouldPutTrap) return false;
            if (MyUnit.rc.getRawCheese() > 0 && maxHealthMoveRange > MyUnit.health - 20) return true;
            return maxHealthMoveRange >= 50 && maxHealthMoveRange >= MyUnit.health - 11 * (enemiesInMoveRange - 1);
        }

        boolean aggroAdvance(){
            if (!shouldPutTrap()) return false;
            return maxHealthMoveRange - 50 < MyUnit.health - 11*enemiesInMoveRange;
        }

        static final int NO_MOVEMENT = -2;
        static final int FORBIDDEN = -1;
        static final int KIDNAPPED = 0;
        static final int MOVE_KIDNAPPED = 1;
        static final int VERY_SURROUNDED = 2;
        static final int SURROUNDED = 3;
        static final int FREE_ENEMY_HIT = 4;
        static final int CAT_DANGER = 5;
        static final int NO_DANGER = 6;

        int safe(){
            if (!canMove) return NO_MOVEMENT;
            if (forbidden) return FORBIDDEN;

            if (enemiesInRange > 0){
                if (maxHealthAtkRange > MyUnit.rc.getHealth() + 11) return KIDNAPPED;
                if (maxHealthMoveRange > MyUnit.rc.getHealth()) return MOVE_KIDNAPPED;
            }

            if (!shouldPutTrap() && maxHealthMoveRange + 11*(enemiesInMoveRange - 1)> MyUnit.health) return MOVE_KIDNAPPED;

            if (enemiesInRange > alliesDist2) return VERY_SURROUNDED;
            if (enemiesInMoveRange > microInfo8.alliesDist2) return SURROUNDED;

            if (!aggroAdvance() && microInfo8.minDistanceToEnemy > 2 && alliesDist2 <= enemiesInMoveRange && minDistanceToEnemy > 2) return FREE_ENEMY_HIT;

            if (seenByCat()) return CAT_DANGER;

            return NO_DANGER;
        }

        int getSafe(){
            if (safe == null) safe = safe();
            return safe;
        }

        boolean seenByCat(){
            if (MyUnit.rc.isCooperation()) return minDistToCat <= 18;
            return minDistToCat <= 68;
        }

        boolean inRange(){
            if (shouldRetreat) return true;
            if (!canAttack) return minDistanceToEnemy <= maxRange;
            return minDistanceToEnemy <= myRange;
        }

        /**
         * Comparator to choose which direction to move during micro.
         */
        boolean isBetter(MicroInfo M){

            if (safe() > M.safe()) return true;
            if (safe() < M.safe()) return false;

            if (seenByCat()){
                if (minDistToCat > M.minDistToCat) return true;
                if (minDistToCat < M.minDistToCat) return false;
            }

            if (inRange() && !M.inRange()) return true;
            if (!inRange() && M.inRange()) return false;

            if (!inRange()){
                if (minDistanceToEnemy < M.minDistanceToEnemy) return true;
                if (minDistanceToEnemy > M.minDistanceToEnemy) return false;
            }

            if (minDistanceToEnemy > M.minDistanceToEnemy) return true;
            if (minDistanceToEnemy < M.minDistanceToEnemy) return false;

            if (alliesDist2 > M.alliesDist2) return true;
            if (alliesDist2 < M.alliesDist2) return false;

            if (dir == Direction.CENTER) return true;
            if (M.dir == Direction.CENTER) return false;

            return true;
        }

        boolean isBetterCat(MicroInfo M){
            if (!canMove) return false;
            return minDistToCat > M.minDistToCat;
        }
    }

    static int maxDist(MapLocation A, MapLocation B){
        int dx = A.x - B.x;
        if (dx < 0) dx = -dx;
        int dy = A.y - B.y;
        if (dy < 0) dy = -dy;
        return dx > dy ? dx : dy;
    }
}
