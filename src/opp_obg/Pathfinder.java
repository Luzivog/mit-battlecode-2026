package opp_obg;
import battlecode.common.*;

public class Pathfinder {

    static void moveTo(MapLocation target) throws GameActionException {
        if (target == null) return;

        //MyUnit.rc.setIndicatorLine(MyUnit.rc.getLocation(), target, 255, 0, 0);

        if (target.distanceSquaredTo(MovementManager.myLoc) == 0) return;
        if (!MyUnit.rc.isMovementReady()) return;

        if (!MyUnit.isKing){
            BugNav.moveTo(target);
            return;
        }


        //BFS
        int targetCode = BFS.encodeLocation(target);
        if (BFS.currentTarget != targetCode){
            BFS.reset(targetCode);
            BugNav.moveTo(target);
            return;
        }

        Direction dir = BFS.getBFSDirection();

        if (dir != null){
            MyUnit.rc.setIndicatorString("Best direction " + dir.name() + " " + BFS.bestDir + " "+ BFS.bestCode + " " + BFS.myLocCode);
            if (dir != Direction.CENTER){
                if (MovementManager.myLoc.add(dir).distanceSquaredTo(target) <= 20) MovementManager.moveWhileLooking(dir, target, DirtManager.canPassThroughDirt);
                else MovementManager.moveFreeDir(dir);
                BugNav.resetPathfinding(false);
                return;
            }
        }

        //BugNav
        BugNav.moveTo(target);
    }

}
