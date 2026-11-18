package opp_goobie;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

public class Debug {
    static final boolean VERBOSE = true;
    static final boolean FAIL_FAST = true;
    static final boolean INFO = true;
    static final boolean INDICATORS = true;
    static final boolean PATHING = false;
    static final boolean MICRO = false;
    static final boolean MAP_DATA = false;
    static final boolean CHECK_BYTECODE = true;
    static final int CHECK_BYTECODE_ID = 6969;

    private static StringBuilder sb;
    private static int startRound = -1;

    static void init() {
        sb = new StringBuilder();
    }

    /**
     * Starts bytecode checking by storing the current round number.
     * Should be called at the start of each turn.
     */
    public static void startBytecodeCheck() {
        if (CHECK_BYTECODE && RobotPlayer.rc != null) {
            startRound = RobotPlayer.rc.getRoundNum();
        }
    }

    /**
     * Checks if bytecode was exceeded by comparing round numbers.
     * If round number changed during the turn, we exceeded bytecode and should
     * resign.
     * Should be called at the end of each turn.
     */
    public static void checkBytecode() throws GameActionException {
        if (CHECK_BYTECODE && RobotPlayer.rc != null && startRound != -1) {
            int endRound = RobotPlayer.rc.getRoundNum();
            if (startRound != endRound) {
                String errorMsg = String.format(
                    "BYTECODE EXCEEDED! Robot ID: %d, Start Round: %d, End Round: %d, Turn Count: %d",
                    RobotPlayer.rc.getID(), startRound, endRound, Robot.turnCount);
                System.out.println(errorMsg);
                System.err.println(errorMsg);
                RobotPlayer.rc.resign();
                throw new IllegalStateException(errorMsg);
            }
        }
    }

    static void flush() {
        if (RobotPlayer.rc != null) {
            RobotPlayer.rc.setIndicatorString(sb.toString());
        }
        sb = new StringBuilder();
    }

    public static void addIndicatorString(boolean cond, String s) {
        if (VERBOSE && cond) {
            sb.append(s);
            sb.append(", ");
        }
    }

    public static void addIndicatorString(String s) {
        Debug.addIndicatorString(Debug.INFO, s);
    }

    public static void failFast(GameActionException ex) {
        throw new IllegalStateException(ex);
    }

    public static void failFast(String message) {
        throw new IllegalStateException(message);
    }

    public static void betterAssert(boolean cond, String msg) {
        if (!cond) {
            failFast(msg);
        }
    }

    public static void println(boolean cond, String s) {
        if (VERBOSE && cond) {
            System.out.println(s);
        }
    }

    public static void println(boolean cond, String s, int id) {
        if (VERBOSE && cond && (id < 0 || (RobotPlayer.rc != null && RobotPlayer.rc.getID() == id))) {
            System.out.println(s);
        }
    }

    public static void printBytecode(String s, int id) {
        if (CHECK_BYTECODE && (id < 0 || (RobotPlayer.rc != null && RobotPlayer.rc.getID() == id))) {
            System.out.println("BYTECODE: " + Clock.getBytecodeNum() + " AT " + s);
        }
    }

    public static void println(String s) {
        Debug.println(Debug.INFO, s);
    }

    public static void println(String s, int id) {
        Debug.println(Debug.INFO, s, id);
    }

    public static void print(boolean cond, String s) {
        if (VERBOSE && cond) {
            System.out.print(s);
        }
    }

    public static void setIndicatorDot(boolean cond, MapLocation loc, int r, int g, int b) throws GameActionException {
        if (VERBOSE && INDICATORS && cond && loc != null && RobotPlayer.rc != null) {
            RobotPlayer.rc.setIndicatorDot(loc, r, g, b);
        }
    }

    public static void setIndicatorLine(boolean cond, MapLocation startLoc, MapLocation endLoc, int r, int g, int b)
        throws GameActionException {
        if (VERBOSE && INDICATORS && cond && startLoc != null && endLoc != null && RobotPlayer.rc != null) {
            RobotPlayer.rc.setIndicatorLine(startLoc, endLoc, r, g, b);
        }
    }

    public static void setIndicatorDot(MapLocation loc, int r, int g, int b) throws GameActionException {
        setIndicatorDot(INDICATORS, loc, r, g, b);
    }

    public static void setIndicatorLine(MapLocation startLoc, MapLocation endLoc, int r, int g, int b)
        throws GameActionException {
        setIndicatorLine(INDICATORS, startLoc, endLoc, r, g, b);
    }
}
