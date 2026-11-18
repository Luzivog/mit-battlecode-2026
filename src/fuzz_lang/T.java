package fuzz_lang;

import battlecode.common.*;

// Tally per feature: runs, exceptions caught inside the feature (expected), throwables that escaped it (odd).
final class T {
    static final String[] F = {"intArith", "longArith", "divZero", "shifts", "floatEdge", "casts", "floatRound",
            "mathFns", "strictMath", "exactMath", "intMethods", "longMethods", "charOps", "random", "concat",
            "doubleToString", "parse", "stringBuilder", "stringMethods", "format", "regex", "boxCache",
            "identityHash", "hashMapPlain", "hashMapString", "lists", "linkedDeque", "priorityQueue", "treeMap",
            "sorting", "arraysUtil", "streams", "jdkQuirks", "iterErrors", "records", "enums", "patternSwitch",
            "interfaces", "innerClasses", "generics", "varargs", "controlFlow", "tryFinally", "customExceptions",
            "arrays", "arraycopy", "recursion", "staticInit", "lambdas", "misc", "atomics", "bitset"};
    static final int NF = F.length;
    static final int[] runs = new int[NF], caught = new int[NF], odd = new int[NF];
    static int lastPrint = -1;

    static void print(RobotController rc, String why) {
        int round = rc.getRoundNum();
        if (round == lastPrint) return;
        lastPrint = round;
        StringBuilder sb = new StringBuilder(2000);
        sb.append("FZT fuzz_lang ").append(rc.getTeam() == Team.A ? 'A' : 'B').append(' ').append(rc.getID())
                .append(" r=").append(round).append(' ').append(why).append(" hp:").append(rc.getHealth());
        for (int f = 0; f < NF; f++) {
            if (runs[f] == 0) continue;
            sb.append(' ').append(F[f]).append('=').append(runs[f]).append(',').append(caught[f]).append(',')
                    .append(odd[f]);
        }
        System.out.println(sb.toString());
    }
}
