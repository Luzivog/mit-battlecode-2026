package opp_goobie;

import battlecode.common.*;

import java.util.Random;

public abstract class Robot {
    public static final Random RNG = new Random(6767);
    public static final Direction[] DIRECTIONS = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
    };

    // Income tracking
    private static final int CHEESE_HISTORY_LEN = 41;
    private static int lastSeenGlobalCheese = -1;
    private static final int[] cheeseHistory = new int[CHEESE_HISTORY_LEN];
    private static int cheeseHistorySize = 0;
    private static int cheeseHistoryWrite = 0;

    public static int globalCheeseDeltaPerTurn = 0;
    public static int avgGlobalCheeseDeltaPerTurn10 = 0;
    public static int avgGlobalCheeseDeltaPerTurn20 = 0;
    public static int avgGlobalCheeseDeltaPerTurn40 = 0;

    /**
     * Enumeration of buildable objects in the game.
     */
    public enum Buildable {
        BABY_RAT,
        RAT_TRAP,
        CAT_TRAP
    }

    public static RobotController rc;
    public static int ID;
    public static int turnCount = 0;
    public static int H, W;
    public static Team team;
    public static Team opponent;
    public static int visionRadiusSquared;
    public static int visionAngle;
    public static MapLocation centerLoc;
    public static MapLocation helpTarget;

    public static boolean clockwisePriority = true;

    public static int estimatedMinRats;
    public static int estimatedMaxRats;

    public Integer spawnerRatKingIndex = null;

    public int avg;

    static boolean right;

    Robot(RobotController rc) throws GameActionException {
        Robot.rc = rc;
        ID = rc.getID();
        H = rc.getMapHeight();
        W = rc.getMapWidth();
        team = rc.getTeam();
        opponent = team.opponent();
        visionRadiusSquared = rc.getType().visionConeRadiusSquared;
        visionAngle = rc.getType().getVisionAngle();
        centerLoc = new MapLocation(W / 2, H / 2);
        right = rc.getID() % 2 == 0;

        avg = 0;

        Debug.printBytecode("BEFORE INIT", Debug.CHECK_BYTECODE_ID);
        MapData.init();
        CommsArray.init();
        Debug.printBytecode("AFTER INIT", Debug.CHECK_BYTECODE_ID);
    }

    public void generalBeforeTurn() throws GameActionException {
        Debug.printBytecode("BEFORE GENERAL TURN", Debug.CHECK_BYTECODE_ID);

        updateGlobalCheeseDeltaAverages();

        Debug.printBytecode("BEFORE REWRITE ARRAY", Debug.CHECK_BYTECODE_ID);
        CommsArray.checkForRewriteAndResetPointer();
        Debug.printBytecode("AFTER REWRITE ARRAY", Debug.CHECK_BYTECODE_ID);
        CommsArray.updateLivingRatKings();

        if (rc.getType().isRatKingType()) {
            if (CommsArray.isCheeseMinesInitialized()) {
                CommsArray.readCheeseMinesFromSharedArray();
            }
        } else {
            Debug.printBytecode("BEFORE CHEESE SHARED ARRAY", Debug.CHECK_BYTECODE_ID);
            CommsArray.readCheeseMinesFromSharedArray();
            Debug.printBytecode("AFTER CHEESE SHARED ARRAY", Debug.CHECK_BYTECODE_ID);
        }

        calculateRatCountRange();
        Debug.printBytecode("AFTER GENERAL TURN", Debug.CHECK_BYTECODE_ID);
    }

    public void generalAfterTurn() throws GameActionException {
        // Re-read cheese mines after rewrite with spare bytecode
        int spareBytecode = Clock.getBytecodesLeft() - 500;
        if (spareBytecode > 1000 && CommsArray.isCheeseMinesRewritten()) {
            CommsArray.rereadCheeseMinesAfterRewrite(spareBytecode);
        }

        avg += UnitType.BABY_RAT.bytecodeLimit - Clock.getBytecodesLeft();

//        Debug.addIndicatorString("Avg BC: " + (avg / (turnCount + 1)));

        Debug.addIndicatorString("avg10: " + avgGlobalCheeseDeltaPerTurn10
                + " avg20: " + avgGlobalCheeseDeltaPerTurn20
                + " avg40: " + avgGlobalCheeseDeltaPerTurn40);
        Debug.flush();
    }

    public void runOneTurn() throws GameActionException {
        generalBeforeTurn();
        beforeTurn();
        turn();
        afterTurn();
        generalAfterTurn();
    }

    abstract void beforeTurn() throws GameActionException;

    abstract void afterTurn() throws GameActionException;

    abstract void turn() throws GameActionException;

    private void calculateRatCountRange() {
        int currentCost = rc.getCurrentRatCost();
        int costIncrement = (currentCost - 10) / 10;
        estimatedMinRats = costIncrement * 4;
        estimatedMaxRats = estimatedMinRats + 3;
    }

    private static void updateGlobalCheeseDeltaAverages() {
        int cur = rc.getGlobalCheese();
        if (lastSeenGlobalCheese == -1) {
            lastSeenGlobalCheese = cur;
            cheeseHistory[0] = cur;
            cheeseHistorySize = 1;
            cheeseHistoryWrite = 1;
            return;
        }
        int delta = cur - lastSeenGlobalCheese;
        lastSeenGlobalCheese = cur;
        globalCheeseDeltaPerTurn = delta;

        cheeseHistory[cheeseHistoryWrite] = cur;
        cheeseHistoryWrite = (cheeseHistoryWrite + 1) % CHEESE_HISTORY_LEN;
        if (cheeseHistorySize < CHEESE_HISTORY_LEN) {
            cheeseHistorySize++;
        }

        int w10 = 10, w20 = 20, w40 = 40;
        if (cheeseHistorySize > w10) {
            int idx10 = (cheeseHistoryWrite - 1 - w10 + CHEESE_HISTORY_LEN) % CHEESE_HISTORY_LEN;
            avgGlobalCheeseDeltaPerTurn10 = (cur - cheeseHistory[idx10]) / w10;
        }
        if (cheeseHistorySize > w20) {
            int idx20 = (cheeseHistoryWrite - 1 - w20 + CHEESE_HISTORY_LEN) % CHEESE_HISTORY_LEN;
            avgGlobalCheeseDeltaPerTurn20 = (cur - cheeseHistory[idx20]) / w20;
        }
        if (cheeseHistorySize > w40) {
            int idx40 = (cheeseHistoryWrite - 1 - w40 + CHEESE_HISTORY_LEN) % CHEESE_HISTORY_LEN;
            avgGlobalCheeseDeltaPerTurn40 = (cur - cheeseHistory[idx40]) / w40;
        }
    }
}
