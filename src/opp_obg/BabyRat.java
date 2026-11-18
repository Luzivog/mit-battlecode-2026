package opp_obg;
import battlecode.common.*;


public class BabyRat extends MyUnit {

    static final int MAX_CHEESE = 100;

    static final int ROUNDS_EXPLORATION = 120;
    static final int MAX_DIRT = 15;

    static Target currentTarget;
    static boolean micro;

    static int internalID;
    static int mineIndex = 0;

    static boolean excess;

    static MapLocation assignedMine;

    static boolean slave = false;

    static MapLocation newKingLoc;
    static boolean closeToNewKing;

    BabyRat(RobotController rc) throws GameActionException {
        super(rc);
        internalID = CommunicationManager.getBabyNumber();
        LocalBFS.init();
        excess = CommunicationManager.getCost() > 40;
        assignedMine = CommunicationManager.getAssignedMine();
        slave = (internalID % 4 == 1 || internalID % 4 == 0);
    }

    void startTurn() throws GameActionException{
        CommunicationManager.update();
        int bc = Clock.getBytecodeNum();
        LocalBFS.makeAssignments();
        LocalBFS.computeDistsUnit();
        int diff =  (Clock.getBytecodeNum() - bc);
        indicatorString = ("" + diff);
        super.startTurn();
        checkNewKing();
    }

    void checkNewKing() throws GameActionException {
        newKingLoc = CommunicationManager.getKingTarget();
        closeToNewKing = false;
        if (newKingLoc != null && MovementManager.myLoc.distanceSquaredTo(newKingLoc) <= 2){
            closeToNewKing = true;
        }
    }

    void checkAssignedMine(){
        if (assignedMine == null) return;
        if (!rc.canSenseLocation(assignedMine)) return;
        if (MovementManager.myLoc.distanceSquaredTo(assignedMine) > 8) return;
        if (VisionManager.closestCheese != null) return;
        if (rc.getRawCheese() > 0) return;
        assignedMine = null;
    }

    void runTurn() throws GameActionException{
        checkAssignedMine();
        tryFuse();
        tryThrow();
        tryAttack();
        //tryThrow();
        micro = MicroManager.doMicro();
        manageRawCheese();
        tryPlaceCatTrap();
        updateTarget();
        if (currentTarget != null) {
            rc.setIndicatorLine(rc.getLocation(), currentTarget.location, 0, 0, 255);
        }
        move();
        tryFuse();
        tryThrow();
        tryAttack();
        //tryThrow();
        tryPlaceCatTrap();
        manageRawCheese();
    }

    boolean shouldTargetCat(){
        return rc.isCooperation();
    }

    void tryPlaceCatTrap() throws GameActionException {
        if(!rc.isCooperation() || VisionManager.closestCat == null) return;
        MapLocation bestTrapLoc = null;
        int maxprod = -1;
        Direction catDir = VisionManager.closestCat.direction;

        for (Direction dir : directions){
            MapLocation newLoc = MovementManager.myLoc.add(dir);
            if (!rc.canPlaceCatTrap(newLoc)) continue;
            if (Util.catDist(VisionManager.closestCat.getLocation(), newLoc) > 18) continue;
            //rc.setIndicatorDot(newLoc, 0, 255, 0);
            int dx = 2*(newLoc.x - VisionManager.closestCat.location.x) - 1;
            int dy = 2*(newLoc.y - VisionManager.closestCat.location.y) - 1;
            int prod = catDir.dx*dx + catDir.dy*dy;
            if (prod < 0) continue;
            prod = (prod*prod)/(dx*dx + dy*dy);
            if (prod > maxprod){
                bestTrapLoc = newLoc;
                maxprod = prod;
            }
        }

        if (bestTrapLoc != null){
            rc.placeCatTrap(bestTrapLoc);
        }
    }

    void tryFuse() throws GameActionException {
        if (rc.getRoundNum() < Constants.MIN_ROUND_FUSION) return;
        if (rc.canBecomeRatKing()) rc.becomeRatKing();
    }

    void updateTarget() throws GameActionException {
        if (micro){
            currentTarget = null;
            return;
        }

        if (catHunter() && VisionManager.closestCat != null){
            currentTarget = new Target(Target.ENEMY, VisionManager.closestCat.getLocation());
            return;
        }

        if (rc.getRoundNum() >= Constants.MIN_ROUND_CONVERGENCE) {
            currentTarget = new Target(Target.RAT_KING, CommunicationManager.getKingLocation());
            return;
        }

        if (currentTarget != null && rc.canSenseLocation(currentTarget.location)) currentTarget = null;

        if (closeToNewKing){
            if (rc.canSenseLocation(newKingLoc) && rc.senseRobotAtLocation(newKingLoc) != null) {
                currentTarget = new Target(Target.NEW_RAT_KING, MovementManager.myLoc);
                return;
            }
            currentTarget = new Target(Target.NEW_RAT_KING, newKingLoc);
            return;
        }

        if (VisionManager.closestCheese != null){
            Target newTarget = new Target(Target.CHEESE, VisionManager.closestCheese);
            if (newTarget.isBetterThan(currentTarget)) currentTarget = newTarget;
        }

        if (currentTarget != null && currentTarget.priority == Target.CHEESE && rc.getRawCheese() >= MAX_CHEESE){
            currentTarget = null;
        }

        if (MyUnit.rc.getRawCheese() > 0){
            Target newTarget = new Target(Target.RAT_KING, CommunicationManager.getKingLocation());
            if (newTarget.isBetterThan(currentTarget)) currentTarget = newTarget;
        }

        else if (newKingLoc != null && slave){
            currentTarget = new Target(Target.NEW_RAT_KING, newKingLoc);
            return;
        }

        if (VisionManager.closestCat != null && shouldTargetCat()){
            currentTarget = new Target(Target.ENEMY, VisionManager.closestCat.getLocation());
        }

        if (rc.getRoundNum() > ROUNDS_EXPLORATION && miner()){
            MapLocation mine = getBestMine();
            if (mine != null){
                Target newTarget = new Target(Target.CHEESE_MINE, mine);
                if (newTarget.isBetterThan(currentTarget)) currentTarget = newTarget;
            }
        }

        Target newTarget = new Target(Target.EXPLORATION, ExplorationManager.getExplore3Target());
        if (newTarget.isBetterThan(currentTarget)) currentTarget = newTarget;

        if (rc.getRoundNum() >= Constants.MIN_ROUND_ATTACK_CATS && rc.getRawCheese() == 0 && VisionManager.closestCat != null && rc.getLocation().distanceSquaredTo(Util.getClosestCatLoc(VisionManager.closestCat.getLocation())) <= 2){
            newTarget = new Target(Target.ENEMY, rc.getLocation());
            if (newTarget.isBetterThan(currentTarget)) currentTarget = newTarget;
        }
    }

    static boolean miner(){
        return internalID > 2 && internalID%4 != 3;
    }

    static boolean catHunter(){
        return (excess || internalID%4 == 2) && MyUnit.rc.getRoundNum() >= Constants.MIN_ROUND_ATTACK_CATS;
    }

    static void changeMine(){
        if (MapData.totalCheese == 0) return;
        mineIndex = (mineIndex + 1)% MapData.totalCheese;
    }

    MapLocation getBestMine(){
        if (MapData.totalCheese == 0) return null;
        if (assignedMine != null) return assignedMine;
        MapLocation ans = MapData.cheeseMineList[mineIndex];
        if (MovementManager.myLoc.distanceSquaredTo(ans) <= 8){
            changeMine();
        }
        return ans;
    }

    void move() throws GameActionException {
        if (micro){
            return;
        }
        if (currentTarget != null) {
            rc.setIndicatorDot(currentTarget.location, 255, 0, 0);
            Pathfinder.moveTo(currentTarget.location);
        }
    }

    void manageRawCheese() throws GameActionException {
        MapLocation king = CommunicationManager.getKingLocation();
        king = king.add(king.directionTo(MovementManager.myLoc));
        if (MyUnit.rc.canTransferCheese(king, rc.getRawCheese())){
            MyUnit.rc.transferCheese(king, rc.getRawCheese());
            if (currentTarget != null && currentTarget.priority == Target.RAT_KING) currentTarget = null;
        }
    }

    void tryThrow() throws GameActionException {
        if (ThrowManager.tryThrow(false)) return;
        if (carryingTurns >= ThrowManager.MIN_CARRYING_TURNS) return;
        Direction dirOp = CommunicationManager.getKingLocation().directionTo(MovementManager.myLoc);
        if (rc.canTurn(dirOp)) rc.turn(dirOp);
        if (ThrowManager.tryThrow(true)) return;
        for (Direction dir : directions) {
            if (rc.canDropRat(dir)) rc.dropRat(dir);
        }
    }

    void checkFusion() throws GameActionException {
        if (closeToNewKing && rc.canBecomeRatKing()){
            rc.becomeRatKing();
        }
    }

    void endTurn() throws GameActionException{
        pickUpAllCheese();
        SqueakManager.squeak();
        super.endTurn();
        SymmetryChecker.run();
        checkFusion();
    }
}
