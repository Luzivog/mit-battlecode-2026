package awubot.util;

import battlecode.common.Clock;
import battlecode.common.UnitType;

import static awubot.Game.isRatKing;
import static awubot.Game.rc;
import static awubot.util.Util.*;

import java.util.ArrayList;

public class Profiler {
    private static int cnt = 0, turnNum = 0;
    private static int acc = 0;
    private static boolean running = false;
    public static void begin() {
        cnt = Clock.getBytecodesLeft();
        turnNum = rc.getRoundNum();
        running = true;
    }
    public static void end(Object obj) {
        if(!running) return;
        pause();
        report(obj);
    }

    public static void end_print(Object obj) {
        if(!running) return;
        pause();
        report_print(obj);
    }

    private static ArrayList<Integer> stk = new ArrayList<>();
    public static void push() {
        stk.add(Clock.getBytecodeNum());
    }
    public static void pop(Object obj) {
        int res = Clock.getBytecodeNum() - stk.remove(stk.size() - 1);
        if (res < 0) {
            if (isRatKing) res += 20000;
            else res += 17500;
        }
        println(obj.toString() + ": " + res + (res >= 17500 ? " EXCEEDED" : ""));
    }

    public static void pause() {
        if(!running) return;
        int res = cnt - Clock.getBytecodesLeft() + (rc.getRoundNum() - turnNum) * 17500;
        acc += res;
        running = false;
    }

    public static void report(Object obj) {
        // println(obj.toString() + ": " + (acc - 12) + (acc >= 17500 ? " EXCEEDED" : ""));
        indicate(obj.toString() + ": " + (acc - 12) + (acc >= 17500 ? " EXCEEDED" : ""));
        acc = 0;
    }

    public static void report_print(Object obj) {
        println(obj.toString() + ": " + (acc - 12) + (acc >= 17500 ? " EXCEEDED" : ""));
        indicate(obj.toString() + ": " + (acc - 12) + (acc >= 17500 ? " EXCEEDED" : ""));
        acc = 0;
    }
}
