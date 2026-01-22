package awubot_aggrosqueaks.robot.rat.goHome;

import battlecode.common.*;
import static awubot_aggrosqueaks.Game.*;
import awubot_aggrosqueaks.comms.GlobalArray;
import static awubot_aggrosqueaks.util.Util.*;

public class AntColony {
    public static StringBuilder[][] mark;
    private static char[] base = new char[5];
    private static char curMark;
    private static int curKing = 0;
    private static MapLocation[] lastKingLocs = new MapLocation[5];
    public static boolean shouldMark = true;
    private static void resetMarks(int i) {
        base[i] = (char)(65536 - 3600);
        for(int j = 0; j < mapHeight; j++) {
            mark[i][j] = new StringBuilder();
            mark[i][j].repeat('\uFFFF', mapWidth);
        }
    }
    public static void init() {
        mark = new StringBuilder[5][mapHeight];
        resetMarks(0);
        curMark = base[0];
    }
    public static void reachedHome() {
        curKing = GlobalArray.nearestRatKingId;
        shouldMark = true;
        curMark = base[curKing];
    }
    public static void markCurrent() {
        if(!shouldMark) {
            indicate("don't mark");
            return;
        }
        indicate("mark " + (int)curMark);
        var row = mark[curKing][myLocY];
        row.setCharAt(myLocX, (char)Math.min(curMark, row.charAt(myLocX)));
        curMark++;
    }
    public static void preTick() {
        for(int i = 0; i < 5; i++) {
            var kingLoc = GlobalArray.getRatKing(i);
            if(lastKingLocs[i] == null || !kingLoc.isAdjacentTo(lastKingLocs[i])) {
                // detected teleportation or first time, reset this king's marks
                resetMarks(i);
            } else if(!kingLoc.equals(lastKingLocs[i])) {
                // king moved, decrease base mark (equivalent to incrementing all existing marks)
                base[i]--;
            }
            lastKingLocs[i] = kingLoc;
        }
    }
}
