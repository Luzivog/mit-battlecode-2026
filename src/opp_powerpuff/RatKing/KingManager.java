package opp_powerpuff.RatKing;

import opp_powerpuff.Utils.Calc;
import battlecode.common.*;
import opp_powerpuff.Helpers.Mine;
import opp_powerpuff.Rat;
import opp_powerpuff.DataStructures.Gain;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;
import opp_powerpuff.Managers.VisionManager;
import opp_powerpuff.Rats.RatKing;



public class KingManager {
    private static RobotController rc;

    private static int[] heartbeats = new int[5];
    

    public static int lastSeenEnemyRound = -100;
    public static int consecutiveHighAlarmRounds = 0;
    public static int[] missedBeats = new int[5];

    private static int buildFlagTurnsActive = 0;
    private static int trySpawnKingCooldown = 0;
    public static void init(RobotController robot) {
        rc = robot;
    }

    static boolean newloc = true;

    public static void emergencyAction() throws GameActionException {
        Escape.killEnemyBabyRat();

        Escape.fleeEnemy();
        if (VisionManager.enemies.length > 0) {
            consecutiveHighAlarmRounds++;
            lastSeenEnemyRound = rc.getRoundNum();
            Global.writeAlarm(VisionManager.enemies.length);
            if (VisionManager.enemies.length >= (float)VisionManager.allies.length * 0.7f)
                Spawn.emergencySpawn();
        } else {
            consecutiveHighAlarmRounds = 0;
        }
    }

    public static int calculateGatherCheeseScore() throws GameActionException {
        if(VisionManager.globalCheeseMines.length == 0) return 0;

        if (rc.getAllCheese() > Constants.MAX_CHEESE_THRESHOLD)
            return Constants.MIN_CHEESE_GATHER_SCORE;

        float averageCheeseGain = Gain.getAverage();
        if (averageCheeseGain < Constants.BAD_CHEESE_GAIN_THRESHOLD) 
            return Constants.MAX_CHEESE_GATHER_SCORE;
        if (averageCheeseGain > Constants.GOOD_CHEESE_GAIN_THRESHOLD) 
            return Constants.MIN_CHEESE_GATHER_SCORE;
        return (int) Calc.map(
            averageCheeseGain,
            Constants.BAD_CHEESE_GAIN_THRESHOLD,
            Constants.GOOD_CHEESE_GAIN_THRESHOLD,
            Constants.MAX_CHEESE_GATHER_SCORE,
            Constants.MIN_CHEESE_GATHER_SCORE
        );
    }

    public static void pulseHeart(int idx) throws GameActionException {
        Global.pumpHeartbeat(idx);
    }
    public static void setHeartbeats() throws GameActionException {
        int[] newHeartbeats = Global.readHeartbeats();
        
        for (int i = 0; i < 5; i ++) {
            // heartbeat dies;
            if (heartbeats[i] == newHeartbeats[i] && Global.readBuildKingFlag() != i) {
                missedBeats[i] ++;
                // set maplocation of dead hearts to 1023, 1023
                if (missedBeats[i] > Constants.MISSED_HEARTBEATS) {
                    Global.editRatKingLoc(i, new MapLocation(1023, 1023));
                    Global.killHeartbeat(i);
                }
            }
            else {
                missedBeats[i] = 0;
            }
        }
        heartbeats = newHeartbeats;
    }

    public static void manageHearts(int idx) throws GameActionException {
        pulseHeart(idx);
        setHeartbeats();
    }

    public static void spawnKingHelper(MapLocation destToSpawnKing) throws GameActionException {
        System.out.println("SPAWNING AT :" + destToSpawnKing);
        int emptyIdx = Global.getEmptyRatKingIdx();
        
        // return if no empty space for a new king
        if (emptyIdx == -1)
            return;
        // return if buildKingFlag is already set
        if (Global.readBuildKingFlag() != 1023) return;

        Global.editRatKingLoc(emptyIdx, destToSpawnKing);
        Global.setBuildKingFlag(emptyIdx);
        trySpawnKingCooldown = 150;
    }

    public static int spawnAndTakeGlobalIndex() throws GameActionException {
        int i = Global.readBuildKingFlag();
        if (i == 1023) {
            return Global.getEmptyRatKingIdx();
        }
        Global.setBuildKingFlag(1023);
        return i;
    }
    public static boolean ratKingFailSpawn() throws GameActionException {
        if (buildFlagTurnsActive > Constants.MERGE_RAT_KING_TURNS && Global.readBuildKingFlag() != 1023) {
            int idx = Global.readBuildKingFlag();
            Global.setBuildKingFlag(1023);
            Global.editRatKingLoc(idx, new MapLocation(1023, 1023));
            System.out.println("FAILED SPAWN RATKING");
            buildFlagTurnsActive = 0;
            trySpawnKingCooldown = 50;
            return true;
        }
        else if (Global.readBuildKingFlag() != 1023)
            buildFlagTurnsActive ++;
        else if (Global.readBuildKingFlag() == 1023)
            buildFlagTurnsActive = 0;
        return false;
    }

    public static void runKingSpawn() throws GameActionException {
        // IMPORTANT DO NOT REMOVE
        // MAKES SURE ONLY ONE KING CALLS A SPAWN KING FUNCTION
        if (Global.getFirstRatKingIdx() != RatKing.ratKingIdx)
            return;

        if (Global.getLivingKings() >= 5)
            return;
        // check if building a king fails, set cooldown to 50
        ratKingFailSpawn();
        // do not try to build a king if there is a cooldown
        if (trySpawnKingCooldown > 0) {
            trySpawnKingCooldown --;
            return;
        }
        // already attempting to spawn a new king
        if (Global.readBuildKingFlag() != 1023)
            return;
        // // do not build a king before round 200
        if (Rat.rc.getRoundNum() < Rat.startDuration())
            return;
        // do not try to spawn kings after round 1200 if we already have 2 or more kings
        if (Rat.rc.getRoundNum() > 1200 && Global.getLivingKings() >= 2) {
            return;
        }

        int livingKings = Global.getLivingKings();
        if (
            (
                // Gain.getAverage() > Constants.NEUTRAL_CHEESE_GAIN_THERSHOLD * livingKings &&
                Calc.determineNumBabyRats(Rat.rc.getCurrentRatCost()) >= Constants.MIN_RAT_COUNT_FOR_RAT_KING + 5 * (livingKings - 1) &&
                Rat.rc.getAllCheese() > (0 == livingKings ? 
                    Constants.WEALTHY_CHEESE_THRESHOLD - 100 :
                    Constants.KING_CHEESE_THRESHOLD
                ) 
            )
        ) {
            if (MapData.reflectionCount() == 1) {

                Mine.sortLocations();
    
                MapLocation bestLoc = null;
                double maxDistSq = -1;
                MapLocation myLoc = rc.getLocation(); 
    
                if (livingKings == 1) {
                    System.out.println("TRIGGERED");
                    for (int i = 0; i < Mine.near.length; i++) {
                        MapLocation mineLoc = Mine.near[i];
                        if (mineLoc == null) continue;
        
                        MapLocation closeMineLoc = MapData.nearestReflectionIncludingSelf(myLoc, mineLoc);
                        if (closeMineLoc != null && !Mine.kingOnMine(closeMineLoc, true)) {
                            int distSq = myLoc.distanceSquaredTo(closeMineLoc);
                            if (distSq > maxDistSq) {
                                maxDistSq = distSq;
                                bestLoc = closeMineLoc;
                            }
                        }
                    }
        
                    if (bestLoc != null) {
                        spawnKingHelper(bestLoc);
                        return;
                    }
        
                    maxDistSq = -1;
                    for (int i = 0; i < Mine.far.length; i++) {
                        MapLocation mineLoc = Mine.far[i];
                        if (mineLoc == null) continue;
        
                        MapLocation closeMineLoc = MapData.nearestReflectionIncludingSelf(myLoc, mineLoc);
                        if (closeMineLoc != null && !Mine.kingOnMine(closeMineLoc, true)) {
                            int distSq = myLoc.distanceSquaredTo(closeMineLoc);
                            if (distSq > maxDistSq) {
                                maxDistSq = distSq;
                                bestLoc = closeMineLoc;
                            }
                        }
                    }
        
                    if (bestLoc != null) {
                        spawnKingHelper(bestLoc);
                    }
                }
                else {
                    for (int i = 0; i < Mine.far.length; i++) {
                        MapLocation mineLoc = Mine.far[i];
                        if (mineLoc == null) continue;
        
                        MapLocation closeMineLoc = MapData.nearestReflectionIncludingSelf(myLoc, mineLoc);
                        if (closeMineLoc != null && !Mine.kingOnMine(closeMineLoc, true)) {
                            int distSq = myLoc.distanceSquaredTo(closeMineLoc);
                            if (distSq > maxDistSq) {
                                maxDistSq = distSq;
                                bestLoc = closeMineLoc;
                            }
                        }
                    }
        
                    if (bestLoc != null) {
                        spawnKingHelper(bestLoc);
                    }
    
                    maxDistSq = -1;
                    for (int i = 0; i < Mine.near.length; i++) {
                        MapLocation mineLoc = Mine.near[i];
                        if (mineLoc == null) continue;
        
                        MapLocation closeMineLoc = MapData.nearestReflectionIncludingSelf(myLoc, mineLoc);
                        if (closeMineLoc != null && !Mine.kingOnMine(closeMineLoc, true)) {
                            int distSq = myLoc.distanceSquaredTo(closeMineLoc);
                            if (distSq > maxDistSq) {
                                maxDistSq = distSq;
                                bestLoc = closeMineLoc;
                            }
                        }
                    }
        
                    if (bestLoc != null) {
                        spawnKingHelper(bestLoc);
                        return;
                    }
                }
            } else {
                MapLocation bestLoc = null;
                double maxDistSq = -1;
                MapLocation myLoc = rc.getLocation(); 

                for (MapLocation mine: VisionManager.globalCheeseMines) {
                    if (mine != null && !Mine.kingOnMine(mine, true)) {
                        int distSq = myLoc.distanceSquaredTo(mine);
                        if (distSq > maxDistSq) {
                            maxDistSq = distSq;
                            bestLoc = mine;
                        }
                    }
                }

                if (bestLoc != null) {
                    spawnKingHelper(bestLoc);
                }
            }
        }
    }
}