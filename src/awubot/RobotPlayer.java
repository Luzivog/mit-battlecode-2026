package awubot;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class RobotPlayer {
    static MapLocation target;
    static int built = 0;


    static int maxBytecode = 0;

    public static void run(RobotController rc) throws GameActionException {
        Game.init(rc);
        Rng.seed();

        switch (rc.getType()) {
            case BABY_RAT -> {
                BabyRat.init();
            }
            case RAT_KING -> {}
            default -> {
                assert false : "illegal unit type " + rc.getType().name();
            }
        }

        while (true) {
            try {
                Rng.init();
                switch (rc.getType()) {
                    case BABY_RAT -> {
                        BabyRat.run();
                    }
                    case RAT_KING -> {
                        indicate(rc.getGlobalCheese());
                        if(built < 10) rc.buildRat(rc.getLocation().add(Direction.NORTHEAST).add(Direction.NORTHEAST));
                        built++;
                    }
                    default -> {
                        assert false : "illegal unit type " + rc.getType().name();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if(rc.getType() == UnitType.BABY_RAT) {
                    maxBytecode = Math.max(maxBytecode, Clock.getBytecodeNum());
                    System.out.println(maxBytecode);
                }
                Clock.yield();
                indicatorString = "";
            }
        }
    }
}
