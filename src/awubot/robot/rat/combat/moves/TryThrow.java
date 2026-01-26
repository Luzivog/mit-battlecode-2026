

package awubot.robot.rat.combat.moves;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotInfo;
import battlecode.common.UnitType;
import battlecode.common.MapLocation;
import battlecode.common.MapInfo;

import awubot.robot.rat.combat.CombatCommon;
import awubot.robot.rat.combat.CombatStrategy;
import awubot.CellTracker;
import awubot.RobotTracker;
import awubot.comms.GlobalArray;
import awubot.util.Profiler;
import static awubot.Game.*;
import static awubot.util.Util.*;
import static awubot.robot.rat.BabyRat.*;






public final class TryThrow {
    private TryThrow() {
    }

    static Direction md;
    static Direction td;
    static double best;
    static double score;
    static int bytecodeStart, bytecodeBudget;
    static char c;
    static RobotInfo occ_m4_4;
    static RobotInfo occ_m3_4;
    static RobotInfo occ_m2_4;
    static RobotInfo occ_m1_4;
    static RobotInfo occ_0_4;
    static RobotInfo occ_1_4;
    static RobotInfo occ_2_4;
    static RobotInfo occ_3_4;
    static RobotInfo occ_4_4;
    static RobotInfo occ_m4_3;
    static RobotInfo occ_m3_3;
    static RobotInfo occ_m2_3;
    static RobotInfo occ_m1_3;
    static RobotInfo occ_0_3;
    static RobotInfo occ_1_3;
    static RobotInfo occ_2_3;
    static RobotInfo occ_3_3;
    static RobotInfo occ_4_3;
    static RobotInfo occ_m4_2;
    static RobotInfo occ_m3_2;
    static RobotInfo occ_m2_2;
    static RobotInfo occ_m1_2;
    static RobotInfo occ_0_2;
    static RobotInfo occ_1_2;
    static RobotInfo occ_2_2;
    static RobotInfo occ_3_2;
    static RobotInfo occ_4_2;
    static RobotInfo occ_m4_1;
    static RobotInfo occ_m3_1;
    static RobotInfo occ_m2_1;
    static RobotInfo occ_m1_1;
    static RobotInfo occ_0_1;
    static RobotInfo occ_1_1;
    static RobotInfo occ_2_1;
    static RobotInfo occ_3_1;
    static RobotInfo occ_4_1;
    static RobotInfo occ_m4_0;
    static RobotInfo occ_m3_0;
    static RobotInfo occ_m2_0;
    static RobotInfo occ_m1_0;
    static RobotInfo occ_0_0;
    static RobotInfo occ_1_0;
    static RobotInfo occ_2_0;
    static RobotInfo occ_3_0;
    static RobotInfo occ_4_0;
    static RobotInfo occ_m4_m1;
    static RobotInfo occ_m3_m1;
    static RobotInfo occ_m2_m1;
    static RobotInfo occ_m1_m1;
    static RobotInfo occ_0_m1;
    static RobotInfo occ_1_m1;
    static RobotInfo occ_2_m1;
    static RobotInfo occ_3_m1;
    static RobotInfo occ_4_m1;
    static RobotInfo occ_m4_m2;
    static RobotInfo occ_m3_m2;
    static RobotInfo occ_m2_m2;
    static RobotInfo occ_m1_m2;
    static RobotInfo occ_0_m2;
    static RobotInfo occ_1_m2;
    static RobotInfo occ_2_m2;
    static RobotInfo occ_3_m2;
    static RobotInfo occ_4_m2;
    static RobotInfo occ_m4_m3;
    static RobotInfo occ_m3_m3;
    static RobotInfo occ_m2_m3;
    static RobotInfo occ_m1_m3;
    static RobotInfo occ_0_m3;
    static RobotInfo occ_1_m3;
    static RobotInfo occ_2_m3;
    static RobotInfo occ_3_m3;
    static RobotInfo occ_4_m3;
    static RobotInfo occ_m4_m4;
    static RobotInfo occ_m3_m4;
    static RobotInfo occ_m2_m4;
    static RobotInfo occ_m1_m4;
    static RobotInfo occ_0_m4;
    static RobotInfo occ_1_m4;
    static RobotInfo occ_2_m4;
    static RobotInfo occ_3_m4;
    static RobotInfo occ_4_m4;

    // return false if didnt throw -- some other process can figure out how to move
    // TODO: maybe weight targets based on how close they are to friendly rats? might be too effort
    public static boolean run() throws GameActionException {
        if (!rc.isActionReady()) return false;
        bytecodeStart = Clock.getBytecodeNum();
        bytecodeBudget = Math.min(7000, Clock.getBytecodesLeft() - 4000);
        md = null;
        td = null;
        best = -1e9;
        resetThrowGrid();
        populateThrowGrid();
        selectMoves();
        if (md == null || td == null) {
            // debugThrowFail();
            return true;
        }
        if (best < 0.1 && best > -100) { // hack for the case where you cant escape
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TryThrow found no good throws (best=" + best + ")");
            // debugThrowFail();
        }
        assert(CombatStrategy.performMoves(md, td));
        assert(rc.canThrowRat());
        rc.throwRat();
        println("threw a rat! come look ! <--------------------------------------------------------------------------------");
        println("movedir " + md + " throwdir " + td + " score " + best);
        indicate("COMBAT STRAT: THROW");
        return true;
    }

    private static void selectMoves() throws GameActionException {
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveN();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveNE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveSE();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveS();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveSW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveNW();
        if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
        moveC();
    }

    private static void moveN() throws GameActionException {
        if (!CombatCommon.canMoveN) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllyNN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllyNNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllyNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllyNSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllyNS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllyNSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllyNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllyNNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowNN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowNNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowNSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowNS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowNSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowNNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveNE() throws GameActionException {
        if (!CombatCommon.canMoveNE) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllyNEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllyNENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllyNEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllyNESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllyNES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllyNESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllyNEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllyNENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowNEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowNENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowNEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowNESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowNES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowNESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowNEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowNENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveE() throws GameActionException {
        if (!CombatCommon.canMoveE) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllyEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllyENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllyEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllyESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllyES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllyESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllyEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllyENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveSE() throws GameActionException {
        if (!CombatCommon.canMoveSE) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllySEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllySENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllySEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllySESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllySES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllySESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllySEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllySENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowSEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowSENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowSEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSENE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowSESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSEE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowSES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSESE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowSESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSES();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowSEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSESW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowSENW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSEN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSEW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveS() throws GameActionException {
        if (!CombatCommon.canMoveS) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllySN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllySNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllySE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllySSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllySS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllySSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllySW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllySNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowSN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowSNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowSSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowSS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowSSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowSNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveSW() throws GameActionException {
        if (!CombatCommon.canMoveSW) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllySWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllySWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllySWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllySWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllySWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllySWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllySWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllySWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllySWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowSWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowSWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowSWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowSWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowSWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowSWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowSWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowSWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowSWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveW() throws GameActionException {
        if (!CombatCommon.canMoveW) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllyWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllyWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllyWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllyWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllyWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllyWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllyWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllyWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveNW() throws GameActionException {
        if (!CombatCommon.canMoveNW) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllyNWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllyNWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllyNWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllyNWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllyNWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllyNWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllyNWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllyNWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyNWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowNWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowNWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowNWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowNWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowNWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowNWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowNWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowNWNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowNWW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }
    private static void moveC() throws GameActionException {
        if (!CombatCommon.canMoveC) return;
        if (rc.getCarrying().getTeam() == team) {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowAllyCN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowAllyCNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowAllyCE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowAllyCSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowAllyCS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowAllyCSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowAllyCW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowAllyCNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowAllyCW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        } else {
            switch (myDir.ordinal()) {
                case 0 -> {
                    tryThrowCN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 1 -> {
                    tryThrowCNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 2 -> {
                    tryThrowCE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 3 -> {
                    tryThrowCSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 4 -> {
                    tryThrowCS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 5 -> {
                    tryThrowCSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 6 -> {
                    tryThrowCW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                case 7 -> {
                    tryThrowCNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    tryThrowCW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                }
                default -> { }
            }
        }
    }

    private static void resetThrowGrid() {
        occ_m4_4 = null;
        occ_m3_4 = null;
        occ_m2_4 = null;
        occ_m1_4 = null;
        occ_0_4 = null;
        occ_1_4 = null;
        occ_2_4 = null;
        occ_3_4 = null;
        occ_4_4 = null;
        occ_m4_3 = null;
        occ_m3_3 = null;
        occ_m2_3 = null;
        occ_m1_3 = null;
        occ_0_3 = null;
        occ_1_3 = null;
        occ_2_3 = null;
        occ_3_3 = null;
        occ_4_3 = null;
        occ_m4_2 = null;
        occ_m3_2 = null;
        occ_m2_2 = null;
        occ_m1_2 = null;
        occ_0_2 = null;
        occ_1_2 = null;
        occ_2_2 = null;
        occ_3_2 = null;
        occ_4_2 = null;
        occ_m4_1 = null;
        occ_m3_1 = null;
        occ_m2_1 = null;
        occ_m1_1 = null;
        occ_0_1 = null;
        occ_1_1 = null;
        occ_2_1 = null;
        occ_3_1 = null;
        occ_4_1 = null;
        occ_m4_0 = null;
        occ_m3_0 = null;
        occ_m2_0 = null;
        occ_m1_0 = null;
        occ_0_0 = null;
        occ_1_0 = null;
        occ_2_0 = null;
        occ_3_0 = null;
        occ_4_0 = null;
        occ_m4_m1 = null;
        occ_m3_m1 = null;
        occ_m2_m1 = null;
        occ_m1_m1 = null;
        occ_0_m1 = null;
        occ_1_m1 = null;
        occ_2_m1 = null;
        occ_3_m1 = null;
        occ_4_m1 = null;
        occ_m4_m2 = null;
        occ_m3_m2 = null;
        occ_m2_m2 = null;
        occ_m1_m2 = null;
        occ_0_m2 = null;
        occ_1_m2 = null;
        occ_2_m2 = null;
        occ_3_m2 = null;
        occ_4_m2 = null;
        occ_m4_m3 = null;
        occ_m3_m3 = null;
        occ_m2_m3 = null;
        occ_m1_m3 = null;
        occ_0_m3 = null;
        occ_1_m3 = null;
        occ_2_m3 = null;
        occ_3_m3 = null;
        occ_4_m3 = null;
        occ_m4_m4 = null;
        occ_m3_m4 = null;
        occ_m2_m4 = null;
        occ_m1_m4 = null;
        occ_0_m4 = null;
        occ_1_m4 = null;
        occ_2_m4 = null;
        occ_3_m4 = null;
        occ_4_m4 = null;
    }

    private static void markCell(int dx, int dy, RobotInfo info) {
        switch (dx) {
            case -4 -> {
                switch (dy) {
                    case -4 -> { occ_m4_m4 = info; }
                    case -3 -> { occ_m4_m3 = info; }
                    case -2 -> { occ_m4_m2 = info; }
                    case -1 -> { occ_m4_m1 = info; }
                    case 0 -> { occ_m4_0 = info; }
                    case 1 -> { occ_m4_1 = info; }
                    case 2 -> { occ_m4_2 = info; }
                    case 3 -> { occ_m4_3 = info; }
                    case 4 -> { occ_m4_4 = info; }
                    default -> { }
                }
            }
            case -3 -> {
                switch (dy) {
                    case -4 -> { occ_m3_m4 = info; }
                    case -3 -> { occ_m3_m3 = info; }
                    case -2 -> { occ_m3_m2 = info; }
                    case -1 -> { occ_m3_m1 = info; }
                    case 0 -> { occ_m3_0 = info; }
                    case 1 -> { occ_m3_1 = info; }
                    case 2 -> { occ_m3_2 = info; }
                    case 3 -> { occ_m3_3 = info; }
                    case 4 -> { occ_m3_4 = info; }
                    default -> { }
                }
            }
            case -2 -> {
                switch (dy) {
                    case -4 -> { occ_m2_m4 = info; }
                    case -3 -> { occ_m2_m3 = info; }
                    case -2 -> { occ_m2_m2 = info; }
                    case -1 -> { occ_m2_m1 = info; }
                    case 0 -> { occ_m2_0 = info; }
                    case 1 -> { occ_m2_1 = info; }
                    case 2 -> { occ_m2_2 = info; }
                    case 3 -> { occ_m2_3 = info; }
                    case 4 -> { occ_m2_4 = info; }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -4 -> { occ_m1_m4 = info; }
                    case -3 -> { occ_m1_m3 = info; }
                    case -2 -> { occ_m1_m2 = info; }
                    case -1 -> { occ_m1_m1 = info; }
                    case 0 -> { occ_m1_0 = info; }
                    case 1 -> { occ_m1_1 = info; }
                    case 2 -> { occ_m1_2 = info; }
                    case 3 -> { occ_m1_3 = info; }
                    case 4 -> { occ_m1_4 = info; }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -4 -> { occ_0_m4 = info; }
                    case -3 -> { occ_0_m3 = info; }
                    case -2 -> { occ_0_m2 = info; }
                    case -1 -> { occ_0_m1 = info; }
                    case 0 -> { occ_0_0 = info; }
                    case 1 -> { occ_0_1 = info; }
                    case 2 -> { occ_0_2 = info; }
                    case 3 -> { occ_0_3 = info; }
                    case 4 -> { occ_0_4 = info; }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -4 -> { occ_1_m4 = info; }
                    case -3 -> { occ_1_m3 = info; }
                    case -2 -> { occ_1_m2 = info; }
                    case -1 -> { occ_1_m1 = info; }
                    case 0 -> { occ_1_0 = info; }
                    case 1 -> { occ_1_1 = info; }
                    case 2 -> { occ_1_2 = info; }
                    case 3 -> { occ_1_3 = info; }
                    case 4 -> { occ_1_4 = info; }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -4 -> { occ_2_m4 = info; }
                    case -3 -> { occ_2_m3 = info; }
                    case -2 -> { occ_2_m2 = info; }
                    case -1 -> { occ_2_m1 = info; }
                    case 0 -> { occ_2_0 = info; }
                    case 1 -> { occ_2_1 = info; }
                    case 2 -> { occ_2_2 = info; }
                    case 3 -> { occ_2_3 = info; }
                    case 4 -> { occ_2_4 = info; }
                    default -> { }
                }
            }
            case 3 -> {
                switch (dy) {
                    case -4 -> { occ_3_m4 = info; }
                    case -3 -> { occ_3_m3 = info; }
                    case -2 -> { occ_3_m2 = info; }
                    case -1 -> { occ_3_m1 = info; }
                    case 0 -> { occ_3_0 = info; }
                    case 1 -> { occ_3_1 = info; }
                    case 2 -> { occ_3_2 = info; }
                    case 3 -> { occ_3_3 = info; }
                    case 4 -> { occ_3_4 = info; }
                    default -> { }
                }
            }
            case 4 -> {
                switch (dy) {
                    case -4 -> { occ_4_m4 = info; }
                    case -3 -> { occ_4_m3 = info; }
                    case -2 -> { occ_4_m2 = info; }
                    case -1 -> { occ_4_m1 = info; }
                    case 0 -> { occ_4_0 = info; }
                    case 1 -> { occ_4_1 = info; }
                    case 2 -> { occ_4_2 = info; }
                    case 3 -> { occ_4_3 = info; }
                    case 4 -> { occ_4_4 = info; }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static RobotInfo getOccAt(int dx, int dy) {
        switch (dx) {
            case -4 -> {
                switch (dy) {
                    case -4 -> { return occ_m4_m4; }
                    case -3 -> { return occ_m4_m3; }
                    case -2 -> { return occ_m4_m2; }
                    case -1 -> { return occ_m4_m1; }
                    case 0 -> { return occ_m4_0; }
                    case 1 -> { return occ_m4_1; }
                    case 2 -> { return occ_m4_2; }
                    case 3 -> { return occ_m4_3; }
                    case 4 -> { return occ_m4_4; }
                    default -> { return null; }
                }
            }
            case -3 -> {
                switch (dy) {
                    case -4 -> { return occ_m3_m4; }
                    case -3 -> { return occ_m3_m3; }
                    case -2 -> { return occ_m3_m2; }
                    case -1 -> { return occ_m3_m1; }
                    case 0 -> { return occ_m3_0; }
                    case 1 -> { return occ_m3_1; }
                    case 2 -> { return occ_m3_2; }
                    case 3 -> { return occ_m3_3; }
                    case 4 -> { return occ_m3_4; }
                    default -> { return null; }
                }
            }
            case -2 -> {
                switch (dy) {
                    case -4 -> { return occ_m2_m4; }
                    case -3 -> { return occ_m2_m3; }
                    case -2 -> { return occ_m2_m2; }
                    case -1 -> { return occ_m2_m1; }
                    case 0 -> { return occ_m2_0; }
                    case 1 -> { return occ_m2_1; }
                    case 2 -> { return occ_m2_2; }
                    case 3 -> { return occ_m2_3; }
                    case 4 -> { return occ_m2_4; }
                    default -> { return null; }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -4 -> { return occ_m1_m4; }
                    case -3 -> { return occ_m1_m3; }
                    case -2 -> { return occ_m1_m2; }
                    case -1 -> { return occ_m1_m1; }
                    case 0 -> { return occ_m1_0; }
                    case 1 -> { return occ_m1_1; }
                    case 2 -> { return occ_m1_2; }
                    case 3 -> { return occ_m1_3; }
                    case 4 -> { return occ_m1_4; }
                    default -> { return null; }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -4 -> { return occ_0_m4; }
                    case -3 -> { return occ_0_m3; }
                    case -2 -> { return occ_0_m2; }
                    case -1 -> { return occ_0_m1; }
                    case 0 -> { return occ_0_0; }
                    case 1 -> { return occ_0_1; }
                    case 2 -> { return occ_0_2; }
                    case 3 -> { return occ_0_3; }
                    case 4 -> { return occ_0_4; }
                    default -> { return null; }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -4 -> { return occ_1_m4; }
                    case -3 -> { return occ_1_m3; }
                    case -2 -> { return occ_1_m2; }
                    case -1 -> { return occ_1_m1; }
                    case 0 -> { return occ_1_0; }
                    case 1 -> { return occ_1_1; }
                    case 2 -> { return occ_1_2; }
                    case 3 -> { return occ_1_3; }
                    case 4 -> { return occ_1_4; }
                    default -> { return null; }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -4 -> { return occ_2_m4; }
                    case -3 -> { return occ_2_m3; }
                    case -2 -> { return occ_2_m2; }
                    case -1 -> { return occ_2_m1; }
                    case 0 -> { return occ_2_0; }
                    case 1 -> { return occ_2_1; }
                    case 2 -> { return occ_2_2; }
                    case 3 -> { return occ_2_3; }
                    case 4 -> { return occ_2_4; }
                    default -> { return null; }
                }
            }
            case 3 -> {
                switch (dy) {
                    case -4 -> { return occ_3_m4; }
                    case -3 -> { return occ_3_m3; }
                    case -2 -> { return occ_3_m2; }
                    case -1 -> { return occ_3_m1; }
                    case 0 -> { return occ_3_0; }
                    case 1 -> { return occ_3_1; }
                    case 2 -> { return occ_3_2; }
                    case 3 -> { return occ_3_3; }
                    case 4 -> { return occ_3_4; }
                    default -> { return null; }
                }
            }
            case 4 -> {
                switch (dy) {
                    case -4 -> { return occ_4_m4; }
                    case -3 -> { return occ_4_m3; }
                    case -2 -> { return occ_4_m2; }
                    case -1 -> { return occ_4_m1; }
                    case 0 -> { return occ_4_0; }
                    case 1 -> { return occ_4_1; }
                    case 2 -> { return occ_4_2; }
                    case 3 -> { return occ_4_3; }
                    case 4 -> { return occ_4_4; }
                    default -> { return null; }
                }
            }
            default -> { return null; }
        }
    }

    private static void markKingZone(int dx, int dy, RobotInfo info) {
        switch (dx) {
            case -4 -> {
                switch (dy) {
                    case -4 -> {
                        occ_m4_m4 = info;
                        occ_m4_m3 = info;
                        occ_m3_m4 = info;
                        occ_m3_m3 = info;
                    }
                    case -3 -> {
                        occ_m4_m4 = info;
                        occ_m4_m3 = info;
                        occ_m4_m2 = info;
                        occ_m3_m4 = info;
                        occ_m3_m3 = info;
                        occ_m3_m2 = info;
                    }
                    case -2 -> {
                        occ_m4_m3 = info;
                        occ_m4_m2 = info;
                        occ_m4_m1 = info;
                        occ_m3_m3 = info;
                        occ_m3_m2 = info;
                        occ_m3_m1 = info;
                    }
                    case -1 -> {
                        occ_m4_m2 = info;
                        occ_m4_m1 = info;
                        occ_m4_0 = info;
                        occ_m3_m2 = info;
                        occ_m3_m1 = info;
                        occ_m3_0 = info;
                    }
                    case 0 -> {
                        occ_m4_m1 = info;
                        occ_m4_0 = info;
                        occ_m4_1 = info;
                        occ_m3_m1 = info;
                        occ_m3_0 = info;
                        occ_m3_1 = info;
                    }
                    case 1 -> {
                        occ_m4_0 = info;
                        occ_m4_1 = info;
                        occ_m4_2 = info;
                        occ_m3_0 = info;
                        occ_m3_1 = info;
                        occ_m3_2 = info;
                    }
                    case 2 -> {
                        occ_m4_1 = info;
                        occ_m4_2 = info;
                        occ_m4_3 = info;
                        occ_m3_1 = info;
                        occ_m3_2 = info;
                        occ_m3_3 = info;
                    }
                    case 3 -> {
                        occ_m4_2 = info;
                        occ_m4_3 = info;
                        occ_m4_4 = info;
                        occ_m3_2 = info;
                        occ_m3_3 = info;
                        occ_m3_4 = info;
                    }
                    case 4 -> {
                        occ_m4_3 = info;
                        occ_m4_4 = info;
                        occ_m3_3 = info;
                        occ_m3_4 = info;
                    }
                    default -> { }
                }
            }
            case -3 -> {
                switch (dy) {
                    case -4 -> {
                        occ_m4_m4 = info;
                        occ_m4_m3 = info;
                        occ_m3_m4 = info;
                        occ_m3_m3 = info;
                        occ_m2_m4 = info;
                        occ_m2_m3 = info;
                    }
                    case -3 -> {
                        occ_m4_m4 = info;
                        occ_m4_m3 = info;
                        occ_m4_m2 = info;
                        occ_m3_m4 = info;
                        occ_m3_m3 = info;
                        occ_m3_m2 = info;
                        occ_m2_m4 = info;
                        occ_m2_m3 = info;
                        occ_m2_m2 = info;
                    }
                    case -2 -> {
                        occ_m4_m3 = info;
                        occ_m4_m2 = info;
                        occ_m4_m1 = info;
                        occ_m3_m3 = info;
                        occ_m3_m2 = info;
                        occ_m3_m1 = info;
                        occ_m2_m3 = info;
                        occ_m2_m2 = info;
                        occ_m2_m1 = info;
                    }
                    case -1 -> {
                        occ_m4_m2 = info;
                        occ_m4_m1 = info;
                        occ_m4_0 = info;
                        occ_m3_m2 = info;
                        occ_m3_m1 = info;
                        occ_m3_0 = info;
                        occ_m2_m2 = info;
                        occ_m2_m1 = info;
                        occ_m2_0 = info;
                    }
                    case 0 -> {
                        occ_m4_m1 = info;
                        occ_m4_0 = info;
                        occ_m4_1 = info;
                        occ_m3_m1 = info;
                        occ_m3_0 = info;
                        occ_m3_1 = info;
                        occ_m2_m1 = info;
                        occ_m2_0 = info;
                        occ_m2_1 = info;
                    }
                    case 1 -> {
                        occ_m4_0 = info;
                        occ_m4_1 = info;
                        occ_m4_2 = info;
                        occ_m3_0 = info;
                        occ_m3_1 = info;
                        occ_m3_2 = info;
                        occ_m2_0 = info;
                        occ_m2_1 = info;
                        occ_m2_2 = info;
                    }
                    case 2 -> {
                        occ_m4_1 = info;
                        occ_m4_2 = info;
                        occ_m4_3 = info;
                        occ_m3_1 = info;
                        occ_m3_2 = info;
                        occ_m3_3 = info;
                        occ_m2_1 = info;
                        occ_m2_2 = info;
                        occ_m2_3 = info;
                    }
                    case 3 -> {
                        occ_m4_2 = info;
                        occ_m4_3 = info;
                        occ_m4_4 = info;
                        occ_m3_2 = info;
                        occ_m3_3 = info;
                        occ_m3_4 = info;
                        occ_m2_2 = info;
                        occ_m2_3 = info;
                        occ_m2_4 = info;
                    }
                    case 4 -> {
                        occ_m4_3 = info;
                        occ_m4_4 = info;
                        occ_m3_3 = info;
                        occ_m3_4 = info;
                        occ_m2_3 = info;
                        occ_m2_4 = info;
                    }
                    default -> { }
                }
            }
            case -2 -> {
                switch (dy) {
                    case -4 -> {
                        occ_m3_m4 = info;
                        occ_m3_m3 = info;
                        occ_m2_m4 = info;
                        occ_m2_m3 = info;
                        occ_m1_m4 = info;
                        occ_m1_m3 = info;
                    }
                    case -3 -> {
                        occ_m3_m4 = info;
                        occ_m3_m3 = info;
                        occ_m3_m2 = info;
                        occ_m2_m4 = info;
                        occ_m2_m3 = info;
                        occ_m2_m2 = info;
                        occ_m1_m4 = info;
                        occ_m1_m3 = info;
                        occ_m1_m2 = info;
                    }
                    case -2 -> {
                        occ_m3_m3 = info;
                        occ_m3_m2 = info;
                        occ_m3_m1 = info;
                        occ_m2_m3 = info;
                        occ_m2_m2 = info;
                        occ_m2_m1 = info;
                        occ_m1_m3 = info;
                        occ_m1_m2 = info;
                        occ_m1_m1 = info;
                    }
                    case -1 -> {
                        occ_m3_m2 = info;
                        occ_m3_m1 = info;
                        occ_m3_0 = info;
                        occ_m2_m2 = info;
                        occ_m2_m1 = info;
                        occ_m2_0 = info;
                        occ_m1_m2 = info;
                        occ_m1_m1 = info;
                        occ_m1_0 = info;
                    }
                    case 0 -> {
                        occ_m3_m1 = info;
                        occ_m3_0 = info;
                        occ_m3_1 = info;
                        occ_m2_m1 = info;
                        occ_m2_0 = info;
                        occ_m2_1 = info;
                        occ_m1_m1 = info;
                        occ_m1_0 = info;
                        occ_m1_1 = info;
                    }
                    case 1 -> {
                        occ_m3_0 = info;
                        occ_m3_1 = info;
                        occ_m3_2 = info;
                        occ_m2_0 = info;
                        occ_m2_1 = info;
                        occ_m2_2 = info;
                        occ_m1_0 = info;
                        occ_m1_1 = info;
                        occ_m1_2 = info;
                    }
                    case 2 -> {
                        occ_m3_1 = info;
                        occ_m3_2 = info;
                        occ_m3_3 = info;
                        occ_m2_1 = info;
                        occ_m2_2 = info;
                        occ_m2_3 = info;
                        occ_m1_1 = info;
                        occ_m1_2 = info;
                        occ_m1_3 = info;
                    }
                    case 3 -> {
                        occ_m3_2 = info;
                        occ_m3_3 = info;
                        occ_m3_4 = info;
                        occ_m2_2 = info;
                        occ_m2_3 = info;
                        occ_m2_4 = info;
                        occ_m1_2 = info;
                        occ_m1_3 = info;
                        occ_m1_4 = info;
                    }
                    case 4 -> {
                        occ_m3_3 = info;
                        occ_m3_4 = info;
                        occ_m2_3 = info;
                        occ_m2_4 = info;
                        occ_m1_3 = info;
                        occ_m1_4 = info;
                    }
                    default -> { }
                }
            }
            case -1 -> {
                switch (dy) {
                    case -4 -> {
                        occ_m2_m4 = info;
                        occ_m2_m3 = info;
                        occ_m1_m4 = info;
                        occ_m1_m3 = info;
                        occ_0_m4 = info;
                        occ_0_m3 = info;
                    }
                    case -3 -> {
                        occ_m2_m4 = info;
                        occ_m2_m3 = info;
                        occ_m2_m2 = info;
                        occ_m1_m4 = info;
                        occ_m1_m3 = info;
                        occ_m1_m2 = info;
                        occ_0_m4 = info;
                        occ_0_m3 = info;
                        occ_0_m2 = info;
                    }
                    case -2 -> {
                        occ_m2_m3 = info;
                        occ_m2_m2 = info;
                        occ_m2_m1 = info;
                        occ_m1_m3 = info;
                        occ_m1_m2 = info;
                        occ_m1_m1 = info;
                        occ_0_m3 = info;
                        occ_0_m2 = info;
                        occ_0_m1 = info;
                    }
                    case -1 -> {
                        occ_m2_m2 = info;
                        occ_m2_m1 = info;
                        occ_m2_0 = info;
                        occ_m1_m2 = info;
                        occ_m1_m1 = info;
                        occ_m1_0 = info;
                        occ_0_m2 = info;
                        occ_0_m1 = info;
                        occ_0_0 = info;
                    }
                    case 0 -> {
                        occ_m2_m1 = info;
                        occ_m2_0 = info;
                        occ_m2_1 = info;
                        occ_m1_m1 = info;
                        occ_m1_0 = info;
                        occ_m1_1 = info;
                        occ_0_m1 = info;
                        occ_0_0 = info;
                        occ_0_1 = info;
                    }
                    case 1 -> {
                        occ_m2_0 = info;
                        occ_m2_1 = info;
                        occ_m2_2 = info;
                        occ_m1_0 = info;
                        occ_m1_1 = info;
                        occ_m1_2 = info;
                        occ_0_0 = info;
                        occ_0_1 = info;
                        occ_0_2 = info;
                    }
                    case 2 -> {
                        occ_m2_1 = info;
                        occ_m2_2 = info;
                        occ_m2_3 = info;
                        occ_m1_1 = info;
                        occ_m1_2 = info;
                        occ_m1_3 = info;
                        occ_0_1 = info;
                        occ_0_2 = info;
                        occ_0_3 = info;
                    }
                    case 3 -> {
                        occ_m2_2 = info;
                        occ_m2_3 = info;
                        occ_m2_4 = info;
                        occ_m1_2 = info;
                        occ_m1_3 = info;
                        occ_m1_4 = info;
                        occ_0_2 = info;
                        occ_0_3 = info;
                        occ_0_4 = info;
                    }
                    case 4 -> {
                        occ_m2_3 = info;
                        occ_m2_4 = info;
                        occ_m1_3 = info;
                        occ_m1_4 = info;
                        occ_0_3 = info;
                        occ_0_4 = info;
                    }
                    default -> { }
                }
            }
            case 0 -> {
                switch (dy) {
                    case -4 -> {
                        occ_m1_m4 = info;
                        occ_m1_m3 = info;
                        occ_0_m4 = info;
                        occ_0_m3 = info;
                        occ_1_m4 = info;
                        occ_1_m3 = info;
                    }
                    case -3 -> {
                        occ_m1_m4 = info;
                        occ_m1_m3 = info;
                        occ_m1_m2 = info;
                        occ_0_m4 = info;
                        occ_0_m3 = info;
                        occ_0_m2 = info;
                        occ_1_m4 = info;
                        occ_1_m3 = info;
                        occ_1_m2 = info;
                    }
                    case -2 -> {
                        occ_m1_m3 = info;
                        occ_m1_m2 = info;
                        occ_m1_m1 = info;
                        occ_0_m3 = info;
                        occ_0_m2 = info;
                        occ_0_m1 = info;
                        occ_1_m3 = info;
                        occ_1_m2 = info;
                        occ_1_m1 = info;
                    }
                    case -1 -> {
                        occ_m1_m2 = info;
                        occ_m1_m1 = info;
                        occ_m1_0 = info;
                        occ_0_m2 = info;
                        occ_0_m1 = info;
                        occ_0_0 = info;
                        occ_1_m2 = info;
                        occ_1_m1 = info;
                        occ_1_0 = info;
                    }
                    case 0 -> {
                        occ_m1_m1 = info;
                        occ_m1_0 = info;
                        occ_m1_1 = info;
                        occ_0_m1 = info;
                        occ_0_0 = info;
                        occ_0_1 = info;
                        occ_1_m1 = info;
                        occ_1_0 = info;
                        occ_1_1 = info;
                    }
                    case 1 -> {
                        occ_m1_0 = info;
                        occ_m1_1 = info;
                        occ_m1_2 = info;
                        occ_0_0 = info;
                        occ_0_1 = info;
                        occ_0_2 = info;
                        occ_1_0 = info;
                        occ_1_1 = info;
                        occ_1_2 = info;
                    }
                    case 2 -> {
                        occ_m1_1 = info;
                        occ_m1_2 = info;
                        occ_m1_3 = info;
                        occ_0_1 = info;
                        occ_0_2 = info;
                        occ_0_3 = info;
                        occ_1_1 = info;
                        occ_1_2 = info;
                        occ_1_3 = info;
                    }
                    case 3 -> {
                        occ_m1_2 = info;
                        occ_m1_3 = info;
                        occ_m1_4 = info;
                        occ_0_2 = info;
                        occ_0_3 = info;
                        occ_0_4 = info;
                        occ_1_2 = info;
                        occ_1_3 = info;
                        occ_1_4 = info;
                    }
                    case 4 -> {
                        occ_m1_3 = info;
                        occ_m1_4 = info;
                        occ_0_3 = info;
                        occ_0_4 = info;
                        occ_1_3 = info;
                        occ_1_4 = info;
                    }
                    default -> { }
                }
            }
            case 1 -> {
                switch (dy) {
                    case -4 -> {
                        occ_0_m4 = info;
                        occ_0_m3 = info;
                        occ_1_m4 = info;
                        occ_1_m3 = info;
                        occ_2_m4 = info;
                        occ_2_m3 = info;
                    }
                    case -3 -> {
                        occ_0_m4 = info;
                        occ_0_m3 = info;
                        occ_0_m2 = info;
                        occ_1_m4 = info;
                        occ_1_m3 = info;
                        occ_1_m2 = info;
                        occ_2_m4 = info;
                        occ_2_m3 = info;
                        occ_2_m2 = info;
                    }
                    case -2 -> {
                        occ_0_m3 = info;
                        occ_0_m2 = info;
                        occ_0_m1 = info;
                        occ_1_m3 = info;
                        occ_1_m2 = info;
                        occ_1_m1 = info;
                        occ_2_m3 = info;
                        occ_2_m2 = info;
                        occ_2_m1 = info;
                    }
                    case -1 -> {
                        occ_0_m2 = info;
                        occ_0_m1 = info;
                        occ_0_0 = info;
                        occ_1_m2 = info;
                        occ_1_m1 = info;
                        occ_1_0 = info;
                        occ_2_m2 = info;
                        occ_2_m1 = info;
                        occ_2_0 = info;
                    }
                    case 0 -> {
                        occ_0_m1 = info;
                        occ_0_0 = info;
                        occ_0_1 = info;
                        occ_1_m1 = info;
                        occ_1_0 = info;
                        occ_1_1 = info;
                        occ_2_m1 = info;
                        occ_2_0 = info;
                        occ_2_1 = info;
                    }
                    case 1 -> {
                        occ_0_0 = info;
                        occ_0_1 = info;
                        occ_0_2 = info;
                        occ_1_0 = info;
                        occ_1_1 = info;
                        occ_1_2 = info;
                        occ_2_0 = info;
                        occ_2_1 = info;
                        occ_2_2 = info;
                    }
                    case 2 -> {
                        occ_0_1 = info;
                        occ_0_2 = info;
                        occ_0_3 = info;
                        occ_1_1 = info;
                        occ_1_2 = info;
                        occ_1_3 = info;
                        occ_2_1 = info;
                        occ_2_2 = info;
                        occ_2_3 = info;
                    }
                    case 3 -> {
                        occ_0_2 = info;
                        occ_0_3 = info;
                        occ_0_4 = info;
                        occ_1_2 = info;
                        occ_1_3 = info;
                        occ_1_4 = info;
                        occ_2_2 = info;
                        occ_2_3 = info;
                        occ_2_4 = info;
                    }
                    case 4 -> {
                        occ_0_3 = info;
                        occ_0_4 = info;
                        occ_1_3 = info;
                        occ_1_4 = info;
                        occ_2_3 = info;
                        occ_2_4 = info;
                    }
                    default -> { }
                }
            }
            case 2 -> {
                switch (dy) {
                    case -4 -> {
                        occ_1_m4 = info;
                        occ_1_m3 = info;
                        occ_2_m4 = info;
                        occ_2_m3 = info;
                        occ_3_m4 = info;
                        occ_3_m3 = info;
                    }
                    case -3 -> {
                        occ_1_m4 = info;
                        occ_1_m3 = info;
                        occ_1_m2 = info;
                        occ_2_m4 = info;
                        occ_2_m3 = info;
                        occ_2_m2 = info;
                        occ_3_m4 = info;
                        occ_3_m3 = info;
                        occ_3_m2 = info;
                    }
                    case -2 -> {
                        occ_1_m3 = info;
                        occ_1_m2 = info;
                        occ_1_m1 = info;
                        occ_2_m3 = info;
                        occ_2_m2 = info;
                        occ_2_m1 = info;
                        occ_3_m3 = info;
                        occ_3_m2 = info;
                        occ_3_m1 = info;
                    }
                    case -1 -> {
                        occ_1_m2 = info;
                        occ_1_m1 = info;
                        occ_1_0 = info;
                        occ_2_m2 = info;
                        occ_2_m1 = info;
                        occ_2_0 = info;
                        occ_3_m2 = info;
                        occ_3_m1 = info;
                        occ_3_0 = info;
                    }
                    case 0 -> {
                        occ_1_m1 = info;
                        occ_1_0 = info;
                        occ_1_1 = info;
                        occ_2_m1 = info;
                        occ_2_0 = info;
                        occ_2_1 = info;
                        occ_3_m1 = info;
                        occ_3_0 = info;
                        occ_3_1 = info;
                    }
                    case 1 -> {
                        occ_1_0 = info;
                        occ_1_1 = info;
                        occ_1_2 = info;
                        occ_2_0 = info;
                        occ_2_1 = info;
                        occ_2_2 = info;
                        occ_3_0 = info;
                        occ_3_1 = info;
                        occ_3_2 = info;
                    }
                    case 2 -> {
                        occ_1_1 = info;
                        occ_1_2 = info;
                        occ_1_3 = info;
                        occ_2_1 = info;
                        occ_2_2 = info;
                        occ_2_3 = info;
                        occ_3_1 = info;
                        occ_3_2 = info;
                        occ_3_3 = info;
                    }
                    case 3 -> {
                        occ_1_2 = info;
                        occ_1_3 = info;
                        occ_1_4 = info;
                        occ_2_2 = info;
                        occ_2_3 = info;
                        occ_2_4 = info;
                        occ_3_2 = info;
                        occ_3_3 = info;
                        occ_3_4 = info;
                    }
                    case 4 -> {
                        occ_1_3 = info;
                        occ_1_4 = info;
                        occ_2_3 = info;
                        occ_2_4 = info;
                        occ_3_3 = info;
                        occ_3_4 = info;
                    }
                    default -> { }
                }
            }
            case 3 -> {
                switch (dy) {
                    case -4 -> {
                        occ_2_m4 = info;
                        occ_2_m3 = info;
                        occ_3_m4 = info;
                        occ_3_m3 = info;
                        occ_4_m4 = info;
                        occ_4_m3 = info;
                    }
                    case -3 -> {
                        occ_2_m4 = info;
                        occ_2_m3 = info;
                        occ_2_m2 = info;
                        occ_3_m4 = info;
                        occ_3_m3 = info;
                        occ_3_m2 = info;
                        occ_4_m4 = info;
                        occ_4_m3 = info;
                        occ_4_m2 = info;
                    }
                    case -2 -> {
                        occ_2_m3 = info;
                        occ_2_m2 = info;
                        occ_2_m1 = info;
                        occ_3_m3 = info;
                        occ_3_m2 = info;
                        occ_3_m1 = info;
                        occ_4_m3 = info;
                        occ_4_m2 = info;
                        occ_4_m1 = info;
                    }
                    case -1 -> {
                        occ_2_m2 = info;
                        occ_2_m1 = info;
                        occ_2_0 = info;
                        occ_3_m2 = info;
                        occ_3_m1 = info;
                        occ_3_0 = info;
                        occ_4_m2 = info;
                        occ_4_m1 = info;
                        occ_4_0 = info;
                    }
                    case 0 -> {
                        occ_2_m1 = info;
                        occ_2_0 = info;
                        occ_2_1 = info;
                        occ_3_m1 = info;
                        occ_3_0 = info;
                        occ_3_1 = info;
                        occ_4_m1 = info;
                        occ_4_0 = info;
                        occ_4_1 = info;
                    }
                    case 1 -> {
                        occ_2_0 = info;
                        occ_2_1 = info;
                        occ_2_2 = info;
                        occ_3_0 = info;
                        occ_3_1 = info;
                        occ_3_2 = info;
                        occ_4_0 = info;
                        occ_4_1 = info;
                        occ_4_2 = info;
                    }
                    case 2 -> {
                        occ_2_1 = info;
                        occ_2_2 = info;
                        occ_2_3 = info;
                        occ_3_1 = info;
                        occ_3_2 = info;
                        occ_3_3 = info;
                        occ_4_1 = info;
                        occ_4_2 = info;
                        occ_4_3 = info;
                    }
                    case 3 -> {
                        occ_2_2 = info;
                        occ_2_3 = info;
                        occ_2_4 = info;
                        occ_3_2 = info;
                        occ_3_3 = info;
                        occ_3_4 = info;
                        occ_4_2 = info;
                        occ_4_3 = info;
                        occ_4_4 = info;
                    }
                    case 4 -> {
                        occ_2_3 = info;
                        occ_2_4 = info;
                        occ_3_3 = info;
                        occ_3_4 = info;
                        occ_4_3 = info;
                        occ_4_4 = info;
                    }
                    default -> { }
                }
            }
            case 4 -> {
                switch (dy) {
                    case -4 -> {
                        occ_3_m4 = info;
                        occ_3_m3 = info;
                        occ_4_m4 = info;
                        occ_4_m3 = info;
                    }
                    case -3 -> {
                        occ_3_m4 = info;
                        occ_3_m3 = info;
                        occ_3_m2 = info;
                        occ_4_m4 = info;
                        occ_4_m3 = info;
                        occ_4_m2 = info;
                    }
                    case -2 -> {
                        occ_3_m3 = info;
                        occ_3_m2 = info;
                        occ_3_m1 = info;
                        occ_4_m3 = info;
                        occ_4_m2 = info;
                        occ_4_m1 = info;
                    }
                    case -1 -> {
                        occ_3_m2 = info;
                        occ_3_m1 = info;
                        occ_3_0 = info;
                        occ_4_m2 = info;
                        occ_4_m1 = info;
                        occ_4_0 = info;
                    }
                    case 0 -> {
                        occ_3_m1 = info;
                        occ_3_0 = info;
                        occ_3_1 = info;
                        occ_4_m1 = info;
                        occ_4_0 = info;
                        occ_4_1 = info;
                    }
                    case 1 -> {
                        occ_3_0 = info;
                        occ_3_1 = info;
                        occ_3_2 = info;
                        occ_4_0 = info;
                        occ_4_1 = info;
                        occ_4_2 = info;
                    }
                    case 2 -> {
                        occ_3_1 = info;
                        occ_3_2 = info;
                        occ_3_3 = info;
                        occ_4_1 = info;
                        occ_4_2 = info;
                        occ_4_3 = info;
                    }
                    case 3 -> {
                        occ_3_2 = info;
                        occ_3_3 = info;
                        occ_3_4 = info;
                        occ_4_2 = info;
                        occ_4_3 = info;
                        occ_4_4 = info;
                    }
                    case 4 -> {
                        occ_3_3 = info;
                        occ_3_4 = info;
                        occ_4_3 = info;
                        occ_4_4 = info;
                    }
                    default -> { }
                }
            }
            default -> { }
        }
    }

    private static void populateThrowGrid() throws GameActionException {
        for (int i = 0; i < RobotTracker.numEnemies; i++) {
            RobotInfo info = RobotTracker.sortedEnemies[i];
            int dx = info.location.x - myLocX;
            int dy = info.location.y - myLocY;
            if (dx < -4 || dx > 4 || dy < -4 || dy > 4) continue;
            if (info.getType() == UnitType.RAT_KING) {
                markKingZone(dx, dy, info);
            } else if (info.getType() == UnitType.BABY_RAT) {
                markCell(dx, dy, info);
            }
        }
        for (int i = 0; i < RobotTracker.numAllies; i++) {
            RobotInfo info = RobotTracker.sortedAllies[i];
            int dx = info.location.x - myLocX;
            int dy = info.location.y - myLocY;
            if (dx < -4 || dx > 4 || dy < -4 || dy > 4) continue;
            if (info.getType() == UnitType.RAT_KING) {
                markKingZone(dx, dy, info);
            } else if (info.getType() == UnitType.BABY_RAT) {
                markCell(dx, dy, info);
            }
        }
        if (GlobalArray.nearestRatKing != null) {
            int dx = GlobalArray.nearestRatKing.x - myLocX;
            int dy = GlobalArray.nearestRatKing.y - myLocY;
            if (dx >= -4 && dx <= 4 && dy >= -4 && dy <= 4) {
                if (rc.canSenseLocation(GlobalArray.nearestRatKing)) {
                    RobotInfo info = rc.senseRobotAtLocation(GlobalArray.nearestRatKing);
                    if (info != null && info.getTeam() == team && info.getType() == UnitType.RAT_KING) {
                        markKingZone(dx, dy, info);
                    }
                }
            }
        }
    }

    private static void debugThrowFail() throws GameActionException {
        println("TryThrow failed: md=" + md + " td=" + td + " best=" + best);
        println("state: loc=" + myLoc + " dir=" + myDir + " actionReady=" + rc.isActionReady()
                + " moveReady=" + rc.isMovementReady() + " turnReady=" + rc.canTurn());
        println("cooldowns: action=" + rc.getActionCooldownTurns() + " move=" + rc.getMovementCooldownTurns()
                + " turn=" + rc.getTurningCooldownTurns());
        println("bytecodesLeft=" + (Clock.getBytecodeNum() - bytecodeStart) + " budget=" + bytecodeBudget);
        println("robots: enemies=" + RobotTracker.numEnemies + " allies=" + RobotTracker.numAllies
                + " cats=" + RobotTracker.numCats + " nearestKing=" + GlobalArray.nearestRatKing);
    }


    private static void tryThrowNN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_0_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_3 != null) {
                if (occ_0_3.getTeam() == team) {
                    if (occ_0_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_0_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_0_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_4 != null) {
                if (occ_0_4.getTeam() == team) {
                    if (occ_0_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_0_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowNNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_3 != null) {
                if (occ_2_3.getTeam() == team) {
                    if (occ_2_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_4 != null) {
                if (occ_3_4.getTeam() == team) {
                    if (occ_3_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_1 != null) {
                if (occ_2_1.getTeam() == team) {
                    if (occ_2_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_1 != null) {
                if (occ_3_1.getTeam() == team) {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 1)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_1 != null) {
                if (occ_4_1.getTeam() == team) {
                    if (occ_4_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 1)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.EAST;
        }
    }
    private static void tryThrowNSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m1 != null) {
                if (occ_2_m1.getTeam() == team) {
                    if (occ_2_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m2 != null) {
                if (occ_3_m2.getTeam() == team) {
                    if (occ_3_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m3 != null) {
                if (occ_4_m3.getTeam() == team) {
                    if (occ_4_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowNS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m1 != null) {
                if (occ_0_m1.getTeam() == team) {
                    if (occ_0_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_0_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_0_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m2 != null) {
                if (occ_0_m2.getTeam() == team) {
                    if (occ_0_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_0_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m3 != null) {
                if (occ_0_m3.getTeam() == team) {
                    if (occ_0_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_0_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m4 != null) {
                if (occ_0_m4.getTeam() == team) {
                    if (occ_0_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_0_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowNSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m1 != null) {
                if (occ_m2_m1.getTeam() == team) {
                    if (occ_m2_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m2 != null) {
                if (occ_m3_m2.getTeam() == team) {
                    if (occ_m3_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m3 != null) {
                if (occ_m4_m3.getTeam() == team) {
                    if (occ_m4_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_1 != null) {
                if (occ_m2_1.getTeam() == team) {
                    if (occ_m2_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_1 != null) {
                if (occ_m3_1.getTeam() == team) {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 1)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_1 != null) {
                if (occ_m4_1.getTeam() == team) {
                    if (occ_m4_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 1)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.WEST;
        }
    }
    private static void tryThrowNNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_3 != null) {
                if (occ_m2_3.getTeam() == team) {
                    if (occ_m2_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_4 != null) {
                if (occ_m3_4.getTeam() == team) {
                    if (occ_m3_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowNEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_3 != null) {
                if (occ_1_3.getTeam() == team) {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_4 != null) {
                if (occ_1_4.getTeam() == team) {
                    if (occ_1_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_1_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowNENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_2_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_3 != null) {
                if (occ_3_3.getTeam() == team) {
                    if (occ_3_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_4 != null) {
                if (occ_4_4.getTeam() == team) {
                    if (occ_4_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowNEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_1 != null) {
                if (occ_3_1.getTeam() == team) {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_1 != null) {
                if (occ_4_1.getTeam() == team) {
                    if (occ_4_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 1)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 1)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + 1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowNESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m1 != null) {
                if (occ_3_m1.getTeam() == team) {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m2 != null) {
                if (occ_4_m2.getTeam() == team) {
                    if (occ_4_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowNES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m1 != null) {
                if (occ_1_m1.getTeam() == team) {
                    if (occ_1_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m2 != null) {
                if (occ_1_m2.getTeam() == team) {
                    if (occ_1_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_1_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m3 != null) {
                if (occ_1_m3.getTeam() == team) {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m4 != null) {
                if (occ_1_m4.getTeam() == team) {
                    if (occ_1_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_1_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowNESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m1 != null) {
                if (occ_m1_m1.getTeam() == team) {
                    if (occ_m1_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m2 != null) {
                if (occ_m2_m2.getTeam() == team) {
                    if (occ_m2_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m3 != null) {
                if (occ_m3_m3.getTeam() == team) {
                    if (occ_m3_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m4 != null) {
                if (occ_m4_m4.getTeam() == team) {
                    if (occ_m4_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m4_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowNEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_1 != null) {
                if (occ_m1_1.getTeam() == team) {
                    if (occ_m1_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_1 != null) {
                if (occ_m2_1.getTeam() == team) {
                    if (occ_m2_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 1)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_1 != null) {
                if (occ_m3_1.getTeam() == team) {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 1)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_1 != null) {
                if (occ_m4_1.getTeam() == team) {
                    if (occ_m4_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m4_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowNENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_0_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_3 != null) {
                if (occ_m1_3.getTeam() == team) {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_4 != null) {
                if (occ_m2_4.getTeam() == team) {
                    if (occ_m2_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_2 != null) {
                if (occ_1_2.getTeam() == team) {
                    if (occ_1_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_3 != null) {
                if (occ_1_3.getTeam() == team) {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_4 != null) {
                if (occ_1_4.getTeam() == team) {
                    if (occ_1_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_1_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_2 != null) {
                if (occ_3_2.getTeam() == team) {
                    if (occ_3_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_3 != null) {
                if (occ_4_3.getTeam() == team) {
                    if (occ_4_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_0 != null) {
                if (occ_3_0.getTeam() == team) {
                    if (occ_3_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_0;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_0 != null) {
                if (occ_4_0.getTeam() == team) {
                    if (occ_4_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m2 != null) {
                if (occ_3_m2.getTeam() == team) {
                    if (occ_3_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m3 != null) {
                if (occ_4_m3.getTeam() == team) {
                    if (occ_4_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m2 != null) {
                if (occ_1_m2.getTeam() == team) {
                    if (occ_1_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m3 != null) {
                if (occ_1_m3.getTeam() == team) {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m4 != null) {
                if (occ_1_m4.getTeam() == team) {
                    if (occ_1_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_1_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m2 != null) {
                if (occ_m1_m2.getTeam() == team) {
                    if (occ_m1_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m3 != null) {
                if (occ_m2_m3.getTeam() == team) {
                    if (occ_m2_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m4 != null) {
                if (occ_m3_m4.getTeam() == team) {
                    if (occ_m3_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_0 != null) {
                if (occ_m1_0.getTeam() == team) {
                    if (occ_m1_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_0;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_0 != null) {
                if (occ_m2_0.getTeam() == team) {
                    if (occ_m2_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_0 != null) {
                if (occ_m3_0.getTeam() == team) {
                    if (occ_m3_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_0 != null) {
                if (occ_m4_0.getTeam() == team) {
                    if (occ_m4_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m4_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_2 != null) {
                if (occ_m1_2.getTeam() == team) {
                    if (occ_m1_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_3 != null) {
                if (occ_m2_3.getTeam() == team) {
                    if (occ_m2_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_4 != null) {
                if (occ_m3_4.getTeam() == team) {
                    if (occ_m3_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowSEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_1 != null) {
                if (occ_1_1.getTeam() == team) {
                    if (occ_1_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_2 != null) {
                if (occ_1_2.getTeam() == team) {
                    if (occ_1_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_1_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_3 != null) {
                if (occ_1_3.getTeam() == team) {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_4 != null) {
                if (occ_1_4.getTeam() == team) {
                    if (occ_1_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_1_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowSENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_1 != null) {
                if (occ_3_1.getTeam() == team) {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_2 != null) {
                if (occ_4_2.getTeam() == team) {
                    if (occ_4_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowSEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m1 != null) {
                if (occ_3_m1.getTeam() == team) {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m1 != null) {
                if (occ_4_m1.getTeam() == team) {
                    if (occ_4_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -1)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -1)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + -1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowSESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_2_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 3 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m3 != null) {
                if (occ_3_m3.getTeam() == team) {
                    if (occ_3_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_3_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_3_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m4 != null) {
                if (occ_4_m4.getTeam() == team) {
                    if (occ_4_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_4_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 6 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + 6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 6, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowSES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m3 != null) {
                if (occ_1_m3.getTeam() == team) {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m4 != null) {
                if (occ_1_m4.getTeam() == team) {
                    if (occ_1_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_1_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 1 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowSESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_0_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m3 != null) {
                if (occ_m1_m3.getTeam() == team) {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m4 != null) {
                if (occ_m2_m4.getTeam() == team) {
                    if (occ_m2_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowSEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m1 != null) {
                if (occ_m1_m1.getTeam() == team) {
                    if (occ_m1_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m1 != null) {
                if (occ_m2_m1.getTeam() == team) {
                    if (occ_m2_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -1)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m1 != null) {
                if (occ_m3_m1.getTeam() == team) {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -1)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m1 != null) {
                if (occ_m4_m1.getTeam() == team) {
                    if (occ_m4_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m4_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowSENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_1 != null) {
                if (occ_m1_1.getTeam() == team) {
                    if (occ_m1_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 2 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_2 != null) {
                if (occ_m2_2.getTeam() == team) {
                    if (occ_m2_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m2_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_3 != null) {
                if (occ_m3_3.getTeam() == team) {
                    if (occ_m3_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m3_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_4 != null) {
                if (occ_m4_4.getTeam() == team) {
                    if (occ_m4_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m4_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowSN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_1 != null) {
                if (occ_0_1.getTeam() == team) {
                    if (occ_0_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_0_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_0_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_2 != null) {
                if (occ_0_2.getTeam() == team) {
                    if (occ_0_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_0_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_3 != null) {
                if (occ_0_3.getTeam() == team) {
                    if (occ_0_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_0_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_4 != null) {
                if (occ_0_4.getTeam() == team) {
                    if (occ_0_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_0_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowSNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_1 != null) {
                if (occ_2_1.getTeam() == team) {
                    if (occ_2_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_2 != null) {
                if (occ_3_2.getTeam() == team) {
                    if (occ_3_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_3 != null) {
                if (occ_4_3.getTeam() == team) {
                    if (occ_4_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m1 != null) {
                if (occ_2_m1.getTeam() == team) {
                    if (occ_2_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m1 != null) {
                if (occ_3_m1.getTeam() == team) {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -1)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m1 != null) {
                if (occ_4_m1.getTeam() == team) {
                    if (occ_4_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -1)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.EAST;
        }
    }
    private static void tryThrowSSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m3 != null) {
                if (occ_2_m3.getTeam() == team) {
                    if (occ_2_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m4 != null) {
                if (occ_3_m4.getTeam() == team) {
                    if (occ_3_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowSS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_0_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m3 != null) {
                if (occ_0_m3.getTeam() == team) {
                    if (occ_0_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_0_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_0_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m4 != null) {
                if (occ_0_m4.getTeam() == team) {
                    if (occ_0_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_0_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowSSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m3 != null) {
                if (occ_m2_m3.getTeam() == team) {
                    if (occ_m2_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m4 != null) {
                if (occ_m3_m4.getTeam() == team) {
                    if (occ_m3_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m1 != null) {
                if (occ_m2_m1.getTeam() == team) {
                    if (occ_m2_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m1 != null) {
                if (occ_m3_m1.getTeam() == team) {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -1)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m1 != null) {
                if (occ_m4_m1.getTeam() == team) {
                    if (occ_m4_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -1)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.WEST;
        }
    }
    private static void tryThrowSNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_1 != null) {
                if (occ_m2_1.getTeam() == team) {
                    if (occ_m2_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_2 != null) {
                if (occ_m3_2.getTeam() == team) {
                    if (occ_m3_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_3 != null) {
                if (occ_m4_3.getTeam() == team) {
                    if (occ_m4_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowSWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_1 != null) {
                if (occ_m1_1.getTeam() == team) {
                    if (occ_m1_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_2 != null) {
                if (occ_m1_2.getTeam() == team) {
                    if (occ_m1_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m1_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_3 != null) {
                if (occ_m1_3.getTeam() == team) {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_4 != null) {
                if (occ_m1_4.getTeam() == team) {
                    if (occ_m1_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m1_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowSWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_1 != null) {
                if (occ_1_1.getTeam() == team) {
                    if (occ_1_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_2 != null) {
                if (occ_2_2.getTeam() == team) {
                    if (occ_2_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_3 != null) {
                if (occ_3_3.getTeam() == team) {
                    if (occ_3_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_4 != null) {
                if (occ_4_4.getTeam() == team) {
                    if (occ_4_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_4_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowSWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m1 != null) {
                if (occ_1_m1.getTeam() == team) {
                    if (occ_1_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m1 != null) {
                if (occ_2_m1.getTeam() == team) {
                    if (occ_2_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -1)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m1 != null) {
                if (occ_3_m1.getTeam() == team) {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -1)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m1 != null) {
                if (occ_4_m1.getTeam() == team) {
                    if (occ_4_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_4_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowSWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_0_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m3 != null) {
                if (occ_1_m3.getTeam() == team) {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m4 != null) {
                if (occ_2_m4.getTeam() == team) {
                    if (occ_2_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowSWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_m1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m3 != null) {
                if (occ_m1_m3.getTeam() == team) {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m4 != null) {
                if (occ_m1_m4.getTeam() == team) {
                    if (occ_m1_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m1_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowSWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m3 != null) {
                if (occ_m3_m3.getTeam() == team) {
                    if (occ_m3_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_m3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m4 != null) {
                if (occ_m4_m4.getTeam() == team) {
                    if (occ_m4_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY >= 6) {
                  c = CellTracker.mapData[myLocY + -6].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + -6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowSWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m1 != null) {
                if (occ_m3_m1.getTeam() == team) {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m1 != null) {
                if (occ_m4_m1.getTeam() == team) {
                    if (occ_m4_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -1)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -1)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + -1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowSWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_1 != null) {
                if (occ_m3_1.getTeam() == team) {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_2 != null) {
                if (occ_m4_2.getTeam() == team) {
                    if (occ_m4_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + 4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_m1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_2 != null) {
                if (occ_m1_2.getTeam() == team) {
                    if (occ_m1_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_3 != null) {
                if (occ_m1_3.getTeam() == team) {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_4 != null) {
                if (occ_m1_4.getTeam() == team) {
                    if (occ_m1_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m1_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_2 != null) {
                if (occ_1_2.getTeam() == team) {
                    if (occ_1_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_3 != null) {
                if (occ_2_3.getTeam() == team) {
                    if (occ_2_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_4 != null) {
                if (occ_3_4.getTeam() == team) {
                    if (occ_3_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_0 != null) {
                if (occ_1_0.getTeam() == team) {
                    if (occ_1_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_0;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_0 != null) {
                if (occ_2_0.getTeam() == team) {
                    if (occ_2_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_0 != null) {
                if (occ_3_0.getTeam() == team) {
                    if (occ_3_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_0 != null) {
                if (occ_4_0.getTeam() == team) {
                    if (occ_4_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_4_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m2 != null) {
                if (occ_1_m2.getTeam() == team) {
                    if (occ_1_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m3 != null) {
                if (occ_2_m3.getTeam() == team) {
                    if (occ_2_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m4 != null) {
                if (occ_3_m4.getTeam() == team) {
                    if (occ_3_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_m1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m2 != null) {
                if (occ_m1_m2.getTeam() == team) {
                    if (occ_m1_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m3 != null) {
                if (occ_m1_m3.getTeam() == team) {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m4 != null) {
                if (occ_m1_m4.getTeam() == team) {
                    if (occ_m1_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m1_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m2 != null) {
                if (occ_m3_m2.getTeam() == team) {
                    if (occ_m3_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m3 != null) {
                if (occ_m4_m3.getTeam() == team) {
                    if (occ_m4_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_0 != null) {
                if (occ_m3_0.getTeam() == team) {
                    if (occ_m3_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_0;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_0 != null) {
                if (occ_m4_0.getTeam() == team) {
                    if (occ_m4_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_2 != null) {
                if (occ_m3_2.getTeam() == team) {
                    if (occ_m3_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_3 != null) {
                if (occ_m4_3.getTeam() == team) {
                    if (occ_m4_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowNWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_m1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_3 != null) {
                if (occ_m1_3.getTeam() == team) {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_4 != null) {
                if (occ_m1_4.getTeam() == team) {
                    if (occ_m1_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m1_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 1 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowNWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_0_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_3 != null) {
                if (occ_1_3.getTeam() == team) {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_4 != null) {
                if (occ_2_4.getTeam() == team) {
                    if (occ_2_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowNWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_1 != null) {
                if (occ_1_1.getTeam() == team) {
                    if (occ_1_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_1 != null) {
                if (occ_2_1.getTeam() == team) {
                    if (occ_2_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 1)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_1 != null) {
                if (occ_3_1.getTeam() == team) {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 1)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_1 != null) {
                if (occ_4_1.getTeam() == team) {
                    if (occ_4_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_4_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowNWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 1 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + 1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_1_m1 != null) {
                if (occ_1_m1.getTeam() == team) {
                    if (occ_1_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_1_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_1_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 1, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 2 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m2 != null) {
                if (occ_2_m2.getTeam() == team) {
                    if (occ_2_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_2_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m3 != null) {
                if (occ_3_m3.getTeam() == team) {
                    if (occ_3_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_3_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m4 != null) {
                if (occ_4_m4.getTeam() == team) {
                    if (occ_4_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_4_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowNWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 1 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m1 != null) {
                if (occ_m1_m1.getTeam() == team) {
                    if (occ_m1_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m1_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m1_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m2 != null) {
                if (occ_m1_m2.getTeam() == team) {
                    if (occ_m1_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m1_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m3 != null) {
                if (occ_m1_m3.getTeam() == team) {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m1_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 1 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -1);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m1_m4 != null) {
                if (occ_m1_m4.getTeam() == team) {
                    if (occ_m1_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 12;
                    }
                } else {
                    if (occ_m1_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 8.5;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -1, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowNWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY >= 1) {
                  c = CellTracker.mapData[myLocY + -1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m1 != null) {
                if (occ_m3_m1.getTeam() == team) {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_m1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_m1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m2 != null) {
                if (occ_m4_m2.getTeam() == team) {
                    if (occ_m4_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -2)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -3)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + -4)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowNWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_1 != null) {
                if (occ_m3_1.getTeam() == team) {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_1;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 1)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_1 != null) {
                if (occ_m4_1.getTeam() == team) {
                    if (occ_m4_1.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_1.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 1)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 1)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY < mapHeight - 1) {
                  c = CellTracker.mapData[myLocY + 1].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + 1)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowNWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 3 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_3 != null) {
                if (occ_m3_3.getTeam() == team) {
                    if (occ_m3_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m3_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m3_3;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 3)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_4 != null) {
                if (occ_m4_4.getTeam() == team) {
                    if (occ_m4_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m4_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 4)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 5)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 6 && myLocY < mapHeight - 6) {
                  c = CellTracker.mapData[myLocY + 6].charAt(myLocX + -6);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -6, myLocY + 6)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowCN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_2 != null) {
                if (occ_0_2.getTeam() == team) {
                    if (occ_0_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_0_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_0_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_3 != null) {
                if (occ_0_3.getTeam() == team) {
                    if (occ_0_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_0_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_4 != null) {
                if (occ_0_4.getTeam() == team) {
                    if (occ_0_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_0_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowCNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_2 != null) {
                if (occ_2_2.getTeam() == team) {
                    if (occ_2_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_3 != null) {
                if (occ_3_3.getTeam() == team) {
                    if (occ_3_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_4 != null) {
                if (occ_4_4.getTeam() == team) {
                    if (occ_4_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowCE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_0 != null) {
                if (occ_2_0.getTeam() == team) {
                    if (occ_2_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_0;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_0 != null) {
                if (occ_3_0.getTeam() == team) {
                    if (occ_3_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_0 != null) {
                if (occ_4_0.getTeam() == team) {
                    if (occ_4_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.EAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.EAST;
        }
    }
    private static void tryThrowCSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX < mapWidth - 2 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + 2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_2_m2 != null) {
                if (occ_2_m2.getTeam() == team) {
                    if (occ_2_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_2_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_2_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 2, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX < mapWidth - 3 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + 3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_3_m3 != null) {
                if (occ_3_m3.getTeam() == team) {
                    if (occ_3_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_3_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 3, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX < mapWidth - 4 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + 4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_4_m4 != null) {
                if (occ_4_m4.getTeam() == team) {
                    if (occ_4_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_4_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + 4, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX < mapWidth - 5 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + 5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + 5, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTHEAST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowCS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m2 != null) {
                if (occ_0_m2.getTeam() == team) {
                    if (occ_0_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_0_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_0_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m3 != null) {
                if (occ_0_m3.getTeam() == team) {
                    if (occ_0_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_0_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_0_m4 != null) {
                if (occ_0_m4.getTeam() == team) {
                    if (occ_0_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_0_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTH, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowCSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY >= 2) {
                  c = CellTracker.mapData[myLocY + -2].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_m2 != null) {
                if (occ_m2_m2.getTeam() == team) {
                    if (occ_m2_m2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_m2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_m2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + -2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY >= 3) {
                  c = CellTracker.mapData[myLocY + -3].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_m3 != null) {
                if (occ_m3_m3.getTeam() == team) {
                    if (occ_m3_m3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_m3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + -3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY >= 4) {
                  c = CellTracker.mapData[myLocY + -4].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_m4 != null) {
                if (occ_m4_m4.getTeam() == team) {
                    if (occ_m4_m4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_m4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + -4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY >= 5) {
                  c = CellTracker.mapData[myLocY + -5].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + -5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowCW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_0 != null) {
                if (occ_m2_0.getTeam() == team) {
                    if (occ_m2_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_0;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_0 != null) {
                if (occ_m3_0.getTeam() == team) {
                    if (occ_m3_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_0 != null) {
                if (occ_m4_0.getTeam() == team) {
                    if (occ_m4_0.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_0.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5) {
                  c = CellTracker.mapData[myLocY].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.WEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.WEST;
        }
    }
    private static void tryThrowCNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        scan: {
            if (myLocX >= 2 && myLocY < mapHeight - 2) {
                  c = CellTracker.mapData[myLocY + 2].charAt(myLocX + -2);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 36;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m2_2 != null) {
                if (occ_m2_2.getTeam() == team) {
                    if (occ_m2_2.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 36;
                        break scan;
                    }
                } else {
                    if (occ_m2_2.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 102;
                        enemyHit = occ_m2_2;
                        break scan;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -2, myLocY + 2)) {
                score += 36;
                break scan;
            }
            if (myLocX >= 3 && myLocY < mapHeight - 3) {
                  c = CellTracker.mapData[myLocY + 3].charAt(myLocX + -3);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 32;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m3_3 != null) {
                if (occ_m3_3.getTeam() == team) {
                    if (occ_m3_3.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 16;
                    }
                } else {
                    if (occ_m3_3.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 21.0;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -3, myLocY + 3)) {
                score += 32;
                break scan;
            }
            if (myLocX >= 4 && myLocY < mapHeight - 4) {
                  c = CellTracker.mapData[myLocY + 4].charAt(myLocX + -4);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 28;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (occ_m4_4 != null) {
                if (occ_m4_4.getTeam() == team) {
                    if (occ_m4_4.getType() == UnitType.RAT_KING) {
                        score = -1000;
                        break scan;
                    } else {
                        score -= 14;
                    }
                } else {
                    if (occ_m4_4.getType() == UnitType.RAT_KING) {
                        score += 300;
                        break scan;
                    } else {
                        score += 12.666666666666666;
                    }
                }
            }
            if (CellTracker.isDirt(myLocX + -4, myLocY + 4)) {
                score += 28;
                break scan;
            }
            if (myLocX >= 5 && myLocY < mapHeight - 5) {
                  c = CellTracker.mapData[myLocY + 5].charAt(myLocX + -5);
                if (c == '\0') {
                    break scan;
                }
                if (c == '\2') {
                    score += 10 + 24;
                    break scan;
                }
            } else {
                score += 10;
                break scan; 
            }
            if (CellTracker.isDirt(myLocX + -5, myLocY + 5)) {
                score += 24;
                break scan;
            }
        }
        if (enemyHit == null) {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTHWEST, enemyHit);
        }
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHWEST;
        }
    }

    private static void tryThrowAllyNN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_0_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_0_3 == null || occ_0_3.getTeam() != enemyTeam) return;
        if (occ_0_3.getType() != UnitType.BABY_RAT || occ_0_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_0_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTH, occ_0_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllyNNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_3 == null || occ_2_3.getTeam() != enemyTeam) return;
        if (occ_2_3.getType() != UnitType.BABY_RAT || occ_2_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHEAST, occ_2_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllyNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_1 == null || occ_2_1.getTeam() != enemyTeam) return;
        if (occ_2_1.getType() != UnitType.BABY_RAT || occ_2_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.EAST, occ_2_1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllyNSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_m1 == null || occ_2_m1.getTeam() != enemyTeam) return;
        if (occ_2_m1.getType() != UnitType.BABY_RAT || occ_2_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHEAST, occ_2_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllyNS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_0_m1 == null || occ_0_m1.getTeam() != enemyTeam) return;
        if (occ_0_m1.getType() != UnitType.BABY_RAT || occ_0_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_0_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTH, occ_0_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllyNSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_m1 == null || occ_m2_m1.getTeam() != enemyTeam) return;
        if (occ_m2_m1.getType() != UnitType.BABY_RAT || occ_m2_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHWEST, occ_m2_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllyNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_1 == null || occ_m2_1.getTeam() != enemyTeam) return;
        if (occ_m2_1.getType() != UnitType.BABY_RAT || occ_m2_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.WEST, occ_m2_1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllyNNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_3 == null || occ_m2_3.getTeam() != enemyTeam) return;
        if (occ_m2_3.getType() != UnitType.BABY_RAT || occ_m2_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHWEST, occ_m2_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllyNEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_3 == null || occ_1_3.getTeam() != enemyTeam) return;
        if (occ_1_3.getType() != UnitType.BABY_RAT || occ_1_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTH, occ_1_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllyNENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_2_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_3 == null || occ_3_3.getTeam() != enemyTeam) return;
        if (occ_3_3.getType() != UnitType.BABY_RAT || occ_3_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHEAST, occ_3_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllyNEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_1 == null || occ_3_1.getTeam() != enemyTeam) return;
        if (occ_3_1.getType() != UnitType.BABY_RAT || occ_3_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.EAST, occ_3_1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllyNESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_m1 == null || occ_3_m1.getTeam() != enemyTeam) return;
        if (occ_3_m1.getType() != UnitType.BABY_RAT || occ_3_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHEAST, occ_3_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllyNES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m1 == null || occ_1_m1.getTeam() != enemyTeam) return;
        if (occ_1_m1.getType() != UnitType.BABY_RAT || occ_1_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTH, occ_1_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllyNESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m1 == null || occ_m1_m1.getTeam() != enemyTeam) return;
        if (occ_m1_m1.getType() != UnitType.BABY_RAT || occ_m1_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHWEST, occ_m1_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllyNEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_1 == null || occ_m1_1.getTeam() != enemyTeam) return;
        if (occ_m1_1.getType() != UnitType.BABY_RAT || occ_m1_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.WEST, occ_m1_1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllyNENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_0_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_3 == null || occ_m1_3.getTeam() != enemyTeam) return;
        if (occ_m1_3.getType() != UnitType.BABY_RAT || occ_m1_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHWEST, occ_m1_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllyEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_2 == null || occ_1_2.getTeam() != enemyTeam) return;
        if (occ_1_2.getType() != UnitType.BABY_RAT || occ_1_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTH, occ_1_2, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllyENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_2 == null || occ_3_2.getTeam() != enemyTeam) return;
        if (occ_3_2.getType() != UnitType.BABY_RAT || occ_3_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHEAST, occ_3_2, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllyEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_0 == null || occ_3_0.getTeam() != enemyTeam) return;
        if (occ_3_0.getType() != UnitType.BABY_RAT || occ_3_0.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_0.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.EAST, occ_3_0, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllyESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_m2 == null || occ_3_m2.getTeam() != enemyTeam) return;
        if (occ_3_m2.getType() != UnitType.BABY_RAT || occ_3_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHEAST, occ_3_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllyES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m2 == null || occ_1_m2.getTeam() != enemyTeam) return;
        if (occ_1_m2.getType() != UnitType.BABY_RAT || occ_1_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTH, occ_1_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllyESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m2 == null || occ_m1_m2.getTeam() != enemyTeam) return;
        if (occ_m1_m2.getType() != UnitType.BABY_RAT || occ_m1_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHWEST, occ_m1_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllyEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_0 == null || occ_m1_0.getTeam() != enemyTeam) return;
        if (occ_m1_0.getType() != UnitType.BABY_RAT || occ_m1_0.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_0.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.WEST, occ_m1_0, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllyENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_2 == null || occ_m1_2.getTeam() != enemyTeam) return;
        if (occ_m1_2.getType() != UnitType.BABY_RAT || occ_m1_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHWEST, occ_m1_2, 42);
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllySEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_1 == null || occ_1_1.getTeam() != enemyTeam) return;
        if (occ_1_1.getType() != UnitType.BABY_RAT || occ_1_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTH, occ_1_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllySENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_1 == null || occ_3_1.getTeam() != enemyTeam) return;
        if (occ_3_1.getType() != UnitType.BABY_RAT || occ_3_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHEAST, occ_3_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllySEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_m1 == null || occ_3_m1.getTeam() != enemyTeam) return;
        if (occ_3_m1.getType() != UnitType.BABY_RAT || occ_3_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.EAST, occ_3_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllySESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_2_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_3_m3 == null || occ_3_m3.getTeam() != enemyTeam) return;
        if (occ_3_m3.getType() != UnitType.BABY_RAT || occ_3_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_3_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHEAST, occ_3_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllySES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m3 == null || occ_1_m3.getTeam() != enemyTeam) return;
        if (occ_1_m3.getType() != UnitType.BABY_RAT || occ_1_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTH, occ_1_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllySESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_0_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m3 == null || occ_m1_m3.getTeam() != enemyTeam) return;
        if (occ_m1_m3.getType() != UnitType.BABY_RAT || occ_m1_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHWEST, occ_m1_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllySEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m1 == null || occ_m1_m1.getTeam() != enemyTeam) return;
        if (occ_m1_m1.getType() != UnitType.BABY_RAT || occ_m1_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.WEST, occ_m1_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllySENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_1 == null || occ_m1_1.getTeam() != enemyTeam) return;
        if (occ_m1_1.getType() != UnitType.BABY_RAT || occ_m1_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHWEST, occ_m1_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllySN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_0_1 == null || occ_0_1.getTeam() != enemyTeam) return;
        if (occ_0_1.getType() != UnitType.BABY_RAT || occ_0_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_0_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTH, occ_0_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllySNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_1 == null || occ_2_1.getTeam() != enemyTeam) return;
        if (occ_2_1.getType() != UnitType.BABY_RAT || occ_2_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHEAST, occ_2_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllySE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_m1 == null || occ_2_m1.getTeam() != enemyTeam) return;
        if (occ_2_m1.getType() != UnitType.BABY_RAT || occ_2_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.EAST, occ_2_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllySSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_m3 == null || occ_2_m3.getTeam() != enemyTeam) return;
        if (occ_2_m3.getType() != UnitType.BABY_RAT || occ_2_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHEAST, occ_2_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllySS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_0_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_0_m3 == null || occ_0_m3.getTeam() != enemyTeam) return;
        if (occ_0_m3.getType() != UnitType.BABY_RAT || occ_0_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_0_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTH, occ_0_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllySSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_m3 == null || occ_m2_m3.getTeam() != enemyTeam) return;
        if (occ_m2_m3.getType() != UnitType.BABY_RAT || occ_m2_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHWEST, occ_m2_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllySW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_m1 == null || occ_m2_m1.getTeam() != enemyTeam) return;
        if (occ_m2_m1.getType() != UnitType.BABY_RAT || occ_m2_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.WEST, occ_m2_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllySNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_1 == null || occ_m2_1.getTeam() != enemyTeam) return;
        if (occ_m2_1.getType() != UnitType.BABY_RAT || occ_m2_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHWEST, occ_m2_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllySWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_1 == null || occ_m1_1.getTeam() != enemyTeam) return;
        if (occ_m1_1.getType() != UnitType.BABY_RAT || occ_m1_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTH, occ_m1_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllySWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_1 == null || occ_1_1.getTeam() != enemyTeam) return;
        if (occ_1_1.getType() != UnitType.BABY_RAT || occ_1_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHEAST, occ_1_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllySWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m1 == null || occ_1_m1.getTeam() != enemyTeam) return;
        if (occ_1_m1.getType() != UnitType.BABY_RAT || occ_1_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.EAST, occ_1_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllySWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_0_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m3 == null || occ_1_m3.getTeam() != enemyTeam) return;
        if (occ_1_m3.getType() != UnitType.BABY_RAT || occ_1_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHEAST, occ_1_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllySWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_m1_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m3 == null || occ_m1_m3.getTeam() != enemyTeam) return;
        if (occ_m1_m3.getType() != UnitType.BABY_RAT || occ_m1_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTH, occ_m1_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllySWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_m2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_m3 == null || occ_m3_m3.getTeam() != enemyTeam) return;
        if (occ_m3_m3.getType() != UnitType.BABY_RAT || occ_m3_m3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_m3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHWEST, occ_m3_m3, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllySWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_m1 == null || occ_m3_m1.getTeam() != enemyTeam) return;
        if (occ_m3_m1.getType() != UnitType.BABY_RAT || occ_m3_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.WEST, occ_m3_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllySWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_1 == null || occ_m3_1.getTeam() != enemyTeam) return;
        if (occ_m3_1.getType() != UnitType.BABY_RAT || occ_m3_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHWEST, occ_m3_1, 42);
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllyWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_m1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_2 == null || occ_m1_2.getTeam() != enemyTeam) return;
        if (occ_m1_2.getType() != UnitType.BABY_RAT || occ_m1_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTH, occ_m1_2, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllyWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_2 == null || occ_1_2.getTeam() != enemyTeam) return;
        if (occ_1_2.getType() != UnitType.BABY_RAT || occ_1_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHEAST, occ_1_2, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllyWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_0 == null || occ_1_0.getTeam() != enemyTeam) return;
        if (occ_1_0.getType() != UnitType.BABY_RAT || occ_1_0.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_0.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.EAST, occ_1_0, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllyWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m2 == null || occ_1_m2.getTeam() != enemyTeam) return;
        if (occ_1_m2.getType() != UnitType.BABY_RAT || occ_1_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHEAST, occ_1_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllyWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_m1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m2 == null || occ_m1_m2.getTeam() != enemyTeam) return;
        if (occ_m1_m2.getType() != UnitType.BABY_RAT || occ_m1_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTH, occ_m1_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllyWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_m2 == null || occ_m3_m2.getTeam() != enemyTeam) return;
        if (occ_m3_m2.getType() != UnitType.BABY_RAT || occ_m3_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHWEST, occ_m3_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllyWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_0 == null || occ_m3_0.getTeam() != enemyTeam) return;
        if (occ_m3_0.getType() != UnitType.BABY_RAT || occ_m3_0.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_0.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.WEST, occ_m3_0, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllyWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_2 == null || occ_m3_2.getTeam() != enemyTeam) return;
        if (occ_m3_2.getType() != UnitType.BABY_RAT || occ_m3_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHWEST, occ_m3_2, 42);
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllyNWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_m1_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_3 == null || occ_m1_3.getTeam() != enemyTeam) return;
        if (occ_m1_3.getType() != UnitType.BABY_RAT || occ_m1_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTH, occ_m1_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllyNWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_0_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_3 == null || occ_1_3.getTeam() != enemyTeam) return;
        if (occ_1_3.getType() != UnitType.BABY_RAT || occ_1_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHEAST, occ_1_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllyNWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_1 == null || occ_1_1.getTeam() != enemyTeam) return;
        if (occ_1_1.getType() != UnitType.BABY_RAT || occ_1_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.EAST, occ_1_1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllyNWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_0_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_1_m1 == null || occ_1_m1.getTeam() != enemyTeam) return;
        if (occ_1_m1.getType() != UnitType.BABY_RAT || occ_1_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_1_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHEAST, occ_1_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllyNWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m1_m1 == null || occ_m1_m1.getTeam() != enemyTeam) return;
        if (occ_m1_m1.getType() != UnitType.BABY_RAT || occ_m1_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m1_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTH, occ_m1_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllyNWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_m1 == null || occ_m3_m1.getTeam() != enemyTeam) return;
        if (occ_m3_m1.getType() != UnitType.BABY_RAT || occ_m3_m1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_m1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHWEST, occ_m3_m1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllyNWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m2_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_1 == null || occ_m3_1.getTeam() != enemyTeam) return;
        if (occ_m3_1.getType() != UnitType.BABY_RAT || occ_m3_1.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_1.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.WEST, occ_m3_1, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllyNWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 2);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m2_2 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m3_3 == null || occ_m3_3.getTeam() != enemyTeam) return;
        if (occ_m3_3.getType() != UnitType.BABY_RAT || occ_m3_3.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m3_3.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHWEST, occ_m3_3, 42);
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHWEST;
        }
    }
    private static void tryThrowAllyCN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTH);
        if (moveCost == -1) return;
        if (occ_0_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_0_2 == null || occ_0_2.getTeam() != enemyTeam) return;
        if (occ_0_2.getType() != UnitType.BABY_RAT || occ_0_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_0_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTH, occ_0_2, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTH;
        }
    }
    private static void tryThrowAllyCNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTHEAST);
        if (moveCost == -1) return;
        if (occ_1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_2 == null || occ_2_2.getTeam() != enemyTeam) return;
        if (occ_2_2.getType() != UnitType.BABY_RAT || occ_2_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTHEAST, occ_2_2, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHEAST;
        }
    }
    private static void tryThrowAllyCE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.EAST);
        if (moveCost == -1) return;
        if (occ_1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_0 == null || occ_2_0.getTeam() != enemyTeam) return;
        if (occ_2_0.getType() != UnitType.BABY_RAT || occ_2_0.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_0.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.EAST, occ_2_0, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.EAST;
        }
    }
    private static void tryThrowAllyCSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        if (occ_1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_2_m2 == null || occ_2_m2.getTeam() != enemyTeam) return;
        if (occ_2_m2.getType() != UnitType.BABY_RAT || occ_2_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_2_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTHEAST, occ_2_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHEAST;
        }
    }
    private static void tryThrowAllyCS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTH);
        if (moveCost == -1) return;
        if (occ_0_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_0_m2 == null || occ_0_m2.getTeam() != enemyTeam) return;
        if (occ_0_m2.getType() != UnitType.BABY_RAT || occ_0_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_0_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTH, occ_0_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTH;
        }
    }
    private static void tryThrowAllyCSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_m1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_m2 == null || occ_m2_m2.getTeam() != enemyTeam) return;
        if (occ_m2_m2.getType() != UnitType.BABY_RAT || occ_m2_m2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_m2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.SOUTHWEST, occ_m2_m2, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHWEST;
        }
    }
    private static void tryThrowAllyCW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.WEST);
        if (moveCost == -1) return;
        if (occ_m1_0 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_0 == null || occ_m2_0.getTeam() != enemyTeam) return;
        if (occ_m2_0.getType() != UnitType.BABY_RAT || occ_m2_0.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_0.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.WEST, occ_m2_0, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.WEST;
        }
    }
    private static void tryThrowAllyCNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTHWEST);
        if (moveCost == -1) return;
        if (occ_m1_1 != null) return;
        if (!rc.onTheMap(throwTarget)) return;
        if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        score = 0;
        RobotInfo enemyHit = null;
        if (occ_m2_2 == null || occ_m2_2.getTeam() != enemyTeam) return;
        if (occ_m2_2.getType() != UnitType.BABY_RAT || occ_m2_2.getHealth() <= rc.getCarrying().getHealth() + 10) return;
        score += occ_m2_2.getHealth() / 10.0;
        score += CombatStrategy.getCellScoreFor(Direction.CENTER, Direction.NORTHWEST, occ_m2_2, 42);
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHWEST;
        }
    }
}