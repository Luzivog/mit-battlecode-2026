package opp_powerpuff.Rats;
import opp_powerpuff.Debug.*;
import opp_powerpuff.Rat;
import opp_powerpuff.DataStructures.Gain;

import battlecode.common.*;
import opp_powerpuff.Helpers.Comms;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.RatKing.CheeseMove;
import opp_powerpuff.RatKing.Escape;
import opp_powerpuff.RatKing.KingManager;
import opp_powerpuff.RatKing.Spawn;


public class RatKing extends Rat {
    public static int lastSeenEnemyRound = -100;
    public static int prevCheeseAmountExcludingRatCost = 2500;
    Message[] messages;

    public static int ratKingIdx;
    public static int turnsSinceLastCat = 100;
    public RatKing(RobotController robot) throws GameActionException {
        super(robot);
        if (rc.getRoundNum() == 1) {
            Global.init();            
            Global.writeToSpawn(rc.getLocation().x, rc.getLocation().y);
        }
        Gain.init((int) ((Rat.H + Rat.W) / 8));

        KingManager.init(robot);
        ratKingIdx = KingManager.spawnAndTakeGlobalIndex();
        Global.editRatKingLoc(ratKingIdx, rc.getLocation());
        MapData.init();
        VisionManager.init(rc);

        // ratKingIdx = Global.addRatKing(rc.getLocation().x, rc.getLocation().y);
        this.state = RatState.SPAWN;
        
    }

    public void start() throws GameActionException {
        super.start();
        VisionManager.update();
        MapData.update(VisionManager.mapInfos);
        Global.editRatKingLoc(ratKingIdx, rc.getLocation());
    }

    public void turn() throws GameActionException {
        // pulse the heart and kill dead rat kings from global array
        KingManager.manageHearts(ratKingIdx);

        Global.writeAlarm(0);
        Global.writeGatherCheeseScore(KingManager.calculateGatherCheeseScore());
        Gain.add(rc.getAllCheese() - prevCheeseAmountExcludingRatCost);

        KingManager.emergencyAction();

        KingManager.runKingSpawn();
        
        if (VisionManager.cats.length > 0)
            turnsSinceLastCat = 0;
        else
            turnsSinceLastCat++;

        if (VisionManager.cats.length > 0 || turnsSinceLastCat < Constants.ROUNDS_AFTER_CAT) {
            Escape.fleeCat();
            if (VisionManager.closestCat != null && Global.getLivingKings() == 1)
                Escape.blockEnemyWithDirt(VisionManager.closestCat.getLocation());
        }

        if (Spawn.check()) {
            Spawn.defaultSpawn();
        }

        Escape.unClog();
        if (Rat.rc.getRoundNum() % 4 == 0)
            Escape.removeSurroundingDirt();
        CheeseMove.run();
        CheeseMove.stutterMove();

        if (VisionManager.closestCheese != null && rc.canPickUpCheese(VisionManager.closestCheese.getMapLocation()))
            rc.pickUpCheese(VisionManager.closestCheese.getMapLocation());

        prevCheeseAmountExcludingRatCost = rc.getAllCheese();



        // Debug.printMapLocArray(Global.readKingLocations());
    }

    public void end() throws GameActionException {
        messages = rc.readSqueaks(rc.getRoundNum() - 1);
        int amountToRead = Math.min(messages.length, 3);
        for (int i = 0; i < amountToRead; i++) {
            Comms.ratKingParse(messages[i].getBytes());
        }
    }
}
