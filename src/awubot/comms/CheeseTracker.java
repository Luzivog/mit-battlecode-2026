package awubot.comms;

import static awubot.Game.*;
import static awubot.util.Util.*;

public class CheeseTracker {
    public static int lastCheese, lastDirt;
    private static boolean inCombat = false;
    
    private static int lastCounter;
    private static int cheeseSpentOnRats;
    private static int[] cheeseHistory = new int[40];

    public static void init() {
        lastCheese = rc.getAllCheese();
        for (int i = 0; i < cheeseHistory.length; i++) {
            cheeseHistory[i] = lastCheese;
        }
        lastDirt = rc.getDirt();
        lastCounter = 0;
        cheeseSpentOnRats = rc.getAllCheese();
    }

    public static double getCheeseGainRate() {
        return (rc.getAllCheese() + cheeseSpentOnRats - cheeseHistory[(time + 1) % cheeseHistory.length]) / (double) cheeseHistory.length;
    }

    public static boolean isInCombat() {
        return inCombat;
    }

    public static boolean hasGainedCheese() {
        return GlobalArray.globalCheeseMines.size() > 0;
    }

    public static void preTick() {
        if (!isRatKing) return;
        if (GlobalArray.countRatKings() > 1) {
            inCombat = true;
            return;
        }
        int currentCounter = GlobalArray.ratCounter;
        if (GlobalArray.ratCounter < lastCounter) currentCounter += 1024;
        int spawned = currentCounter - lastCounter;
        cheeseSpentOnRats += spawned * rc.getCurrentRatCost();
        cheeseHistory[time % cheeseHistory.length] = rc.getAllCheese() + cheeseSpentOnRats;

        int dirtMined = rc.getDirt() - lastDirt;
        int minCheese = lastCheese - dirtMined * 10 - 2;
        // indicate("minCheese: " + minCheese + " (dirtMined: " + dirtMined + ", lastCheese: " + lastCheese + ", currentCheese: " + rc.getAllCheese() + ")");
        if (rc.getAllCheese() < minCheese) {
            inCombat = true;
        }
        // indicate("isInCombat: " + inCombat);
    }

    public static void postTick() {
        if (!isRatKing) return;
        lastCheese = rc.getAllCheese();
        lastDirt = rc.getDirt();
        lastCounter = GlobalArray.ratCounter;
    }
}
