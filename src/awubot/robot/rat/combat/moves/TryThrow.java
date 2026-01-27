

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
    static boolean isSecondary;
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
        bytecodeBudget = Math.min(9000, Clock.getBytecodesLeft() - 2000);
        md = null;
        td = null;
        isSecondary = false;
        best = -1e9;
        resetThrowGrid();
        populateThrowGrid();
        selectMoves();
        if (md == null || td == null) {
            return true;
        }
        if (best < 0 && best > -100) {
            println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!! TryThrow found no good throws (best=" + best + ")");
            // debugThrowFail();
        }
        if (isSecondary) {
            if (td != myDir) turn(td);
            assert(rc.canThrowRat());
            rc.throwRat();
            assert(CombatStrategy.performMoves(md, td));
        } else {
            assert(CombatStrategy.performMoves(md, td));
            assert(rc.canThrowRat());
            rc.throwRat();
        }
        println("threw a rat! come look ! <--------------------------------------------------------------------------------");
        println("movedir " + md + " throwdir " + td + " score " + best);
        indicate("COMBAT STRAT: THROW");
        return true;
    }

    private static void selectMoves() throws GameActionException {
        switch (myDir.ordinal()) {
            case 0 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySNW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWNW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySEN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySENE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySENW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 1 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 2 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySWSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 3 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWS();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNS();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWS();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryWE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 4 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNS();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNSW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWS();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWSE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNWSW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNES();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNESE();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNESW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 5 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNESW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNES();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNSW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNS();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryESW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryES();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 6 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryENW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryESW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySENW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySESW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNENW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryNESW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            case 7 -> {
                    moveC();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySENW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySEN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySNW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondarySW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryENW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryEN();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                                tryThrowSecondaryEW();
                                if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveN();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveS();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveNE();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
                    moveSW();
                    if (Clock.getBytecodeNum() - bytecodeStart > bytecodeBudget) return;
            }
            default -> { }
        }
    }

    private static void moveN() throws GameActionException {
        if (!CombatCommon.canMoveN) return;
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
    private static void moveNE() throws GameActionException {
        if (!CombatCommon.canMoveNE) return;
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
    private static void moveE() throws GameActionException {
        if (!CombatCommon.canMoveE) return;
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
    private static void moveSE() throws GameActionException {
        if (!CombatCommon.canMoveSE) return;
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
    private static void moveS() throws GameActionException {
        if (!CombatCommon.canMoveS) return;
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
    private static void moveSW() throws GameActionException {
        if (!CombatCommon.canMoveSW) return;
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
    private static void moveW() throws GameActionException {
        if (!CombatCommon.canMoveW) return;
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
    private static void moveNW() throws GameActionException {
        if (!CombatCommon.canMoveNW) return;
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
    private static void moveC() throws GameActionException {
        if (!CombatCommon.canMoveC) return;
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
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowNNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowNSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNEN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowNENE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNEE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNESE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNES() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowNESW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNEW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNENW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHEAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowEN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowENE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowEE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowESE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowES() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowESW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowEW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowENW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.EAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.EAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSEN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowSENE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSEE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSESE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 2, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_2_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSES() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowSESW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSEW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSENW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHEAST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHEAST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowSNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowSSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTH, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTH);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSWN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowSWNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSWE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSWSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowSWS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowSWSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_m2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSWW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowSWNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.SOUTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.SOUTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowWN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowWNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowWE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowWSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowWS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowWSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowWW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowWNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.WEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.WEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNWN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowNWNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNWE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNWSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowNWS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowNWSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNWW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowNWNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.NORTHWEST, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -2, myLocY + 2);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.NORTHWEST);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m2_2 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowCN() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTH;
            isSecondary = false;
        }
    }
    private static void tryThrowCNE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowCE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.EAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.EAST;
            isSecondary = false;
        }
    }
    private static void tryThrowCSE() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTHEAST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_1_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHEAST;
            isSecondary = false;
        }
    }
    private static void tryThrowCS() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTH);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_0_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTH;
            isSecondary = false;
        }
    }
    private static void tryThrowCSW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.SOUTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_m1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHWEST;
            isSecondary = false;
        }
    }
    private static void tryThrowCW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.WEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_0 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.WEST;
            isSecondary = false;
        }
    }
    private static void tryThrowCNW() throws GameActionException {
        int moveCost = CombatCommon.movementCost(Direction.CENTER, Direction.NORTHWEST);
        if (moveCost == -1) return;
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        // can optimise statically
        MapLocation newLoc = myLoc.add(Direction.CENTER);
        if (myLoc.distanceSquaredTo(throwTarget) <= 2) {
            if (!CombatCommon.canMove(myLoc.directionTo(throwTarget))) return;
        } else {
            if (!rc.canSenseLocation(throwTarget)) return;
            if (occ_m1_1 != null) return;
            if (!rc.onTheMap(throwTarget)) return;
            if (!rc.senseMapInfo(throwTarget).isPassable()) return;
        }
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHWEST;
            isSecondary = false;
        }
    }

    public static boolean canSecondary(Direction moveDir, Direction throwDir) {
        assert(moveDir != Direction.CENTER);
        if (!rc.canTurn() && throwDir != myDir) return false;
        if (!rc.isMovementReady() || !CombatCommon.canMove(moveDir)) return false;
        return true;
    }

    private static void tryThrowSecondaryNN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTH, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTH, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTH;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHEAST, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHEAST, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHEAST;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.EAST, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.EAST, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.EAST;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySEN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySENE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySEE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySESE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySES() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySESW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySEW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySENW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHEAST, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHEAST, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHEAST;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTH, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTH, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTH;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondarySWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.SOUTHWEST, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.SOUTHWEST, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.SOUTHWEST;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.WEST, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.WEST, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.WEST;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.NORTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.NORTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.EAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.EAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.EAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.SOUTHEAST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHEAST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHEAST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.SOUTH)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTH);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTH, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.SOUTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.SOUTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.WEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.WEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.WEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryNWNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.NORTHWEST, Direction.NORTHWEST)) return;
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
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHWEST);
        } else {
            score += CombatStrategy.getCellScoreFor(Direction.NORTHWEST, Direction.NORTHWEST, enemyHit);
        }
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.NORTHWEST;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCN() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.NORTH)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCNE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.NORTHEAST)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.EAST)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.EAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCSE() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + 1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.SOUTHEAST)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHEAST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCS() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.SOUTH)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTH;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCSW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + -1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.SOUTHWEST)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.SOUTHWEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.WEST)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.WEST;
            isSecondary = true;
        }
    }
    private static void tryThrowSecondaryCNW() throws GameActionException {
        MapLocation throwTarget = new MapLocation(myLocX + -1, myLocY + 1);
        if (!rc.canSenseLocation(throwTarget)) return;
        if (!canSecondary(Direction.CENTER, Direction.NORTHWEST)) return;
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
        // if (score <= 0.1) return;
        if (score > best) {
            best = score;
            md = Direction.CENTER;
            td = Direction.NORTHWEST;
            isSecondary = true;
        }
    }
}