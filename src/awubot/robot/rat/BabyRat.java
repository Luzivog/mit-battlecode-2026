package awubot.robot.rat;

import battlecode.common.*;
import awubot.Pathfinder;
import awubot.CellTracker;
import awubot.robot.Strategy;
import awubot.robot.rat.strategies.MinerStrategy;
import static awubot.Game.*;
import static awubot.util.Util.*;
import awubot.util.Rng;

public class BabyRat {
    private static Direction lastGlance;
    public static boolean shouldGlance;
    public static Strategy primaryStrategy;

    public static void init() throws GameActionException {
        CellTracker.storeVisibleCells();
        primaryStrategy = new MinerStrategy();
    }

    public static void run() throws GameActionException {
        shouldGlance = true;
        CellTracker.checkSymmetry();
        indicate("sym " + CellTracker.hsym + " " + CellTracker.vsym + " " + CellTracker.rsym);
        primaryStrategy.runTick();
        if (rc.canTurn() && shouldGlance) {
            indicate("glancing");
            var leftDir = rc.getDirection().rotateLeft().rotateLeft();
            var rightDir = rc.getDirection().rotateRight().rotateRight();
            if (lastGlance == leftDir) {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            } else if (lastGlance == rightDir || Rng.mcg() >> 30 != 0) {
                Pathfinder.turn(leftDir);
                lastGlance = leftDir;
            } else {
                Pathfinder.turn(rightDir);
                lastGlance = rightDir;
            }
        }
    }

}
