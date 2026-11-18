package fuzz_chaos;

import battlecode.common.*;

// Tally per act: runs, runs that crossed a turn boundary (overrun or yield), exceptions caught.
final class T {
    static final String[] A = {"spinLoop", "spinUntilLeft", "jdkSort", "jdkHashMap", "jdkArraysSort", "heavyClinit",
            "bigClinit", "yieldClinit", "catchOverrun", "finallyOverrun", "lambdaOverrun", "newIntHuge",
            "newMultiHuge", "arraycopyHuge", "yieldInFinally", "yieldInLoop", "yieldInCatch", "yieldComparator",
            "yieldPendingThrow", "yieldInRecursion", "swallowAssert", "swallowClinitError", "debugMethods",
            "infiniteLoop", "deepOverrun", "stringOverrun", "exceptionStorm", "fate"};
    static final int NA = A.length;
    static final int[] runs = new int[NA], crossed = new int[NA], caught = new int[NA];
    static int lastPrint = -1;

    static void print(RobotController rc, String why) {
        int round = rc.getRoundNum();
        if (round == lastPrint && why.equals("periodic")) return;
        lastPrint = round;
        StringBuilder sb = new StringBuilder(1500);
        sb.append("FZT fuzz_chaos ").append(rc.getTeam() == Team.A ? 'A' : 'B').append(' ').append(rc.getID())
                .append(" r=").append(round).append(' ').append(why).append(" fate:").append(RobotPlayer.fate);
        for (int f = 0; f < NA; f++) {
            if (runs[f] == 0) continue;
            sb.append(' ').append(A[f]).append('=').append(runs[f]).append(',').append(crossed[f]).append(',')
                    .append(caught[f]);
        }
        System.out.println(sb.toString());
    }
}
