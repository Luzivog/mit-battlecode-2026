package awubot;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import awubot.Game;
import awubot.util.Util;
import awubot.util.Rng;

public class RobotPlayer {
    static MapLocation target;
    static boolean built = false;

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();
        int cnt = 0;

        if(rc.getType() == UnitType.BABY_RAT) {
            CellTracker.storeVisibleCells();
        }

        // for(int i = 0; i < rc.getID() % 200; i++) Math.random();
        while (true) {
            try {
                Rng.init();
                switch (rc.getType()) {
                    case BABY_RAT:
                        if(target == null || target.distanceSquaredTo(rc.getLocation()) < (cnt == 2 ? 2 : 18)) {
                            target = new MapLocation((int)(Math.random() * rc.getMapWidth()), (int)(Math.random() * rc.getMapHeight()));
                            cnt++;
                        }
                        rc.setIndicatorLine(rc.getLocation(), target, 255, 0, 0);
                        if(rc.isMovementReady()) Pathfinder.makeMove(target);
                        // CellTracker.storeVisibleCells();
                        // CellTracker.computeLocalPassable();
                        // CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
                        // rc.resign();
                        break;
                    case RAT_KING:
                        if(!built) rc.buildRat(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST));
                        built = true;
                        break;
                    default:
                        assert false : "illegal unit type " + rc.getType().name();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                Clock.yield();
                Util.indicatorString = "";
            }
        }
    }
}
