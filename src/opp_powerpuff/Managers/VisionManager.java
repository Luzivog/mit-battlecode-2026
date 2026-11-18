package opp_powerpuff.Managers;

import battlecode.common.*;
import opp_powerpuff.Rat;
import opp_powerpuff.Helpers.Constants;
import opp_powerpuff.Helpers.Mine;
import opp_powerpuff.Helpers.Vision;

public class VisionManager {
    public static MapInfo[] mapInfos;
    public static MapInfo[] cheeseMines;
    public static MapInfo[] cheese;
    public static MapInfo closestCheeseMine;
    public static MapInfo closestCheese;
    public static RobotInfo[] enemies;
    public static RobotInfo[] allies;
    public static RobotInfo[] cats;
    public static RobotInfo closestEnemy;
    public static RobotInfo closestAlly;
    public static RobotInfo closestCat;
    public static MapLocation allyRatKingLocation;
    public static RobotController rc;

    public static RobotInfo lastSeenClosestCat;
    public static MapLocation learnedEnemyKingLocation;
    public static MapLocation unknownCheeseMine;
    public static MapLocation secondUnknownCheeseMine;
    public static MapLocation[] globalCheeseMines;
    public static int lastRoundSeenCheese = -1;
    public static MapLocation bigCheeseMine;
    public static int totalCheeseSeen;

    public static MapLocation[] allAllyRatKingLocations;
    public static Message[] messages;

    public static void init(RobotController robot) throws GameActionException {
        rc = robot;
        update();
    }
    public static void update() throws GameActionException {
        MapLocation currentLocation = rc.getLocation(); 
        // update all vision
        mapInfos = rc.senseNearbyMapInfos();
        allies = rc.senseNearbyRobots(-1, Rat.rc.getTeam());
        enemies = rc.senseNearbyRobots(-1, Rat.rc.getTeam().opponent());
        cats = rc.senseNearbyRobots(-1, Team.NEUTRAL);
        cheeseMines = Vision.senseNearbyCheeseMines(currentLocation, mapInfos);
        cheese = Vision.senseNearbyCheese(currentLocation, mapInfos);
        // update closest
        closestEnemy = Vision.getClosestRobot(currentLocation, enemies);
        closestAlly = Vision.getClosestRobot(currentLocation, allies);
        closestCat = Vision.getClosestRobot(currentLocation, cats);
        closestCheeseMine = Vision.getClosestMapInfo(currentLocation, cheeseMines);
        closestCheese = Vision.getClosestMapInfo(currentLocation, cheese);
        globalCheeseMines = Global.readCheeseMineLocations();

        messages = Rat.rc.readSqueaks(Rat.rc.getRoundNum() - 1);

        if (lastSeenClosestCat == null) {
            lastSeenClosestCat = closestCat;
        } else if (closestCat != null) {
            lastSeenClosestCat = closestCat;
        }

        updateUnknownCheeseMine();

        if (lastRoundSeenCheese == -1 || closestCheese != null) lastRoundSeenCheese = Rat.rc.getRoundNum();

        allAllyRatKingLocations = Global.readKingLocations();
        allyRatKingLocation = Vision.getClosestRatKing(Rat.rc.getLocation(), allAllyRatKingLocations); //Global.readRatKingLoc(0); // Vision.getClosestRatKingLoc(allies);
    };

    public static void updateEnemiesAndCats() throws GameActionException {
        mapInfos = rc.senseNearbyMapInfos();
        enemies = rc.senseNearbyRobots(-1, Rat.rc.getTeam().opponent());
        cats = rc.senseNearbyRobots(-1, Team.NEUTRAL);
        closestEnemy = Vision.getClosestRobot(Rat.rc.getLocation(), enemies);
        closestCat = Vision.getClosestRobot(Rat.rc.getLocation(), cats);
        if (lastSeenClosestCat == null) {
            lastSeenClosestCat = closestCat;
        } else if (closestCat != null) {
            lastSeenClosestCat = closestCat;
        }
    }
    public static void updateAllies() throws GameActionException {
        allies = rc.senseNearbyRobots(-1, Rat.rc.getTeam());
        closestAlly = Vision.getClosestRobot(Rat.rc.getLocation(), allies);
    }
    public static void updateCheese() throws GameActionException {
        cheeseMines = Vision.senseNearbyCheeseMines(Rat.rc.getLocation(), mapInfos);
        cheese = Vision.senseNearbyCheese(Rat.rc.getLocation(), mapInfos);
        closestCheeseMine = Vision.getClosestMapInfo(Rat.rc.getLocation(), cheeseMines);
        closestCheese = Vision.getClosestMapInfo(Rat.rc.getLocation(), cheese);
        globalCheeseMines = Global.readCheeseMineLocations();
        updateUnknownCheeseMine();
        if (lastRoundSeenCheese == -1 || closestCheese != null) lastRoundSeenCheese = Rat.rc.getRoundNum();
        
        totalCheeseSeen = 0;
        if (closestCheeseMine != null) {
            int totalCheeseSeen = 0;
            for (MapInfo cheeseInfo: cheese) {
                totalCheeseSeen += cheeseInfo.getCheeseAmount();
            }
            if (
                totalCheeseSeen > Constants.CHEESE_RETREAT_LIMIT &&
                VisionManager.allies.length <= 2
            ) bigCheeseMine = closestCheese.getMapLocation();
        }
    }
    public static void updateKingLocation() throws GameActionException {
        allyRatKingLocation = Vision.getClosestRatKing(Rat.rc.getLocation(), Global.readKingLocations()); //Global.readRatKingLoc(0); // Vision.getClosestRatKingLoc(allies);
    }

    public static void updateUnknownCheeseMine() throws GameActionException {
        if (unknownCheeseMine != null && Mine.knownCheeseMine(unknownCheeseMine)) {
            unknownCheeseMine = null;
        }

        if (secondUnknownCheeseMine != null && Mine.knownCheeseMine(secondUnknownCheeseMine)) {
            secondUnknownCheeseMine = null;
        }

        if (unknownCheeseMine == null && secondUnknownCheeseMine != null) {
            unknownCheeseMine = secondUnknownCheeseMine;
            secondUnknownCheeseMine = null;
        }

        if (secondUnknownCheeseMine == null) {
            for (MapInfo info : cheeseMines) {
                MapLocation loc = info.getMapLocation();
                if (Mine.knownCheeseMine(loc)) continue;
                if (loc.equals(unknownCheeseMine)) continue;
                if (unknownCheeseMine == null) {
                    unknownCheeseMine = loc;
                } else {
                    secondUnknownCheeseMine = loc;
                    break; 
                }
            }
        }
    }
}
