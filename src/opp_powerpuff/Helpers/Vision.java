package opp_powerpuff.Helpers;
import battlecode.common.*;
import opp_powerpuff.Managers.Global;
import opp_powerpuff.Managers.MapData;

public class Vision {
    public static MapLocation getClosestRatKing(MapLocation location, MapLocation[] kingLocations) throws GameActionException {
        MapLocation loc = null;
        for (int i = 0; i < kingLocations.length; i ++) {
            MapLocation kl = kingLocations[i];
            if (kl == null) continue;
            if (kl.x == 1023)
                continue;
            if (Global.readBuildKingFlag() == i)
                continue;
            if (loc == null)
                loc = kl;
            else if (location.distanceSquaredTo(kl) < location.distanceSquaredTo(loc) && Global.readBuildKingFlag() != i)
                loc = kl;
        }
        return loc;
    }
    // 
    public static RobotInfo getClosestRobot(MapLocation location, RobotInfo[] robots) throws GameActionException {
        RobotInfo cRobot = null;
        for (RobotInfo robot : robots) {
            if (cRobot == null || location.distanceSquaredTo(cRobot.getLocation()) > location.distanceSquaredTo(robot.getLocation()))
                cRobot = robot;
        }
        return cRobot;
    }
    // return robots surrounding the square
    public static RobotInfo[] squareSurroundingRobots(MapLocation location, RobotInfo[] robots) {
        int numEnemiesNearSquare = 0;
        for (RobotInfo robot : robots) {
            if (robot.getLocation().isAdjacentTo(location))
                numEnemiesNearSquare ++;
        }
        RobotInfo[] nearbyEnemies = new RobotInfo[numEnemiesNearSquare];
        int i = 0;
        for (RobotInfo robot : robots) {
            if (robot.getLocation().isAdjacentTo(location)) {
                nearbyEnemies[i] = robot;
                i ++;
            }
        }
        return nearbyEnemies;
    }
    // return MapInfo surrounding this square
    public static MapInfo[] senseSurroundingMapInfos(MapLocation location, MapInfo[] vision) {
        int squares = 0;
        for (MapInfo square : vision) {
            if (square.getMapLocation().isAdjacentTo(location))
                squares ++;
        }
        MapInfo[] nearbySquares = new MapInfo[squares];
        int i = 0;
        for (MapInfo square : vision) {
            if (square.getMapLocation().isAdjacentTo(location)) {
                nearbySquares[i] = square;
                i ++;
            }
        }
        return nearbySquares;
    }
    // return MapInfo on this square
    public static MapInfo mapInfoOnSquare(MapLocation location, MapInfo[] vision) {
        for (MapInfo info : vision) {
            if (info.getMapLocation().equals(location))
                return info;
        }
        return null;
    }
    public static MapInfo[] senseNearbyCheeseMines(MapLocation location, MapInfo[] visionCone) throws GameActionException {
        int numCheeseMines = 0;
        for (MapInfo info : visionCone) {
            if (info.hasCheeseMine())
                numCheeseMines ++;
        }
        MapInfo[] cheeseMines = new MapInfo[numCheeseMines];
        int i = 0;
        for (MapInfo info : visionCone) {
            if (info.hasCheeseMine()) {
                cheeseMines[i] = info;
                i ++;
            }
        }
        return cheeseMines;
    }

    public static MapInfo[] senseNearbyCheese(MapLocation location, MapInfo[] visionCone) {
        int numCheese = 0;
        for (MapInfo info : visionCone) {
            if (info.getCheeseAmount() != 0)
                numCheese ++;
        }
        MapInfo[] cheese = new MapInfo[numCheese];
        int i = 0;
        for (MapInfo info : visionCone) {
            if (info.getCheeseAmount() != 0) {
                cheese[i] = info;
                i ++;
            }
        }
        return cheese;
    }
    
    public static MapInfo getClosestMapInfo(MapLocation location, MapInfo[] visionCone) {
        MapInfo closestInfo = null;
        for (MapInfo info : visionCone) {
            if (closestInfo == null || location.distanceSquaredTo(info.getMapLocation()) < location.distanceSquaredTo(closestInfo.getMapLocation()))
                closestInfo = info;
        }
        return closestInfo;
    }

    public static MapLocation getClosestRatKingLoc(RobotInfo[] robotInfos) throws GameActionException {
        for (RobotInfo robot: robotInfos) {
            if (robot.getType() == UnitType.RAT_KING) 
                return robot.getLocation();
        }
        return null;
    }
    public static RobotInfo getClosestType(MapLocation location, RobotInfo[] robots, UnitType type) throws GameActionException {
        RobotInfo closestRobot = null;
        for (RobotInfo robot : robots) {
            if ((closestRobot == null || 
                location.distanceSquaredTo(robot.getLocation()) < location.distanceSquaredTo(closestRobot.getLocation())) && 
                robot.getType() == type)
            closestRobot = robot;
        }
        return closestRobot;
    }
    public static boolean checkRobotInfoOnMapInfo(MapInfo[] mapInfo, MapLocation robotLastSeen, RobotInfo RobotInVision) {
        for (MapInfo info : mapInfo) {
            if (info.getMapLocation().equals(robotLastSeen) && RobotInVision == null)
                return false;
        }
        return true;
    }
    public static boolean locationOccupiedByRobot(MapLocation loc, RobotInfo[] robotInfo)  throws GameActionException{
        for (RobotInfo robot : robotInfo) {
            if (robot.getLocation().equals(loc))
                return true;
        }
        return false;
    }
    public static MapInfo getClosestWall(MapLocation loc, MapInfo[] mapInfos) throws GameActionException {
        MapInfo block = null;
        for (MapInfo info : mapInfos) {
            if (block == null && (info.isDirt() || info.isWall()))
                block = info;
            else if ((info.isDirt() || info.isWall()) && loc.distanceSquaredTo(info.getMapLocation()) < loc.distanceSquaredTo(block.getMapLocation()))
                block = info;
        }
        return block;
    }
    public static MapInfo getClosestWallAllDirections(MapLocation loc, int mapWidth, int mapHeight) throws GameActionException {
        MapInfo block = null;
        int xLowEdge = Math.max(loc.x - 5, 0);
        int xHighEdge = Math.min(loc.x + 6, mapWidth);
        int yLowEdge = Math.max(loc.y - 5, 0);
        int yHighEdge = Math.min(loc.y + 6, mapHeight);
        for (int i = xLowEdge; i < xHighEdge; i ++) {
            for (int j = yLowEdge; j < yHighEdge; j ++) {
                MapInfo info = MapData.mapInfos[i][j];
                if (info == null)
                    continue;
                if (
                    block == null 
                    && (info.isDirt() || info.isWall()) 
                    && perfectAngle(loc, info.getMapLocation())
                )
                    block = info;
                else if (block != null && (info.isDirt() || info.isWall()) && 
                            loc.distanceSquaredTo(block.getMapLocation()) > loc.distanceSquaredTo(info.getMapLocation()) && perfectAngle(loc, info.getMapLocation())) {
                    block = info;
                }
            }
        }
        return block;
    }
    public static boolean perfectAngle(MapLocation org, MapLocation target) throws GameActionException {
        int diffX = Math.abs(target.x - org.x);
        int diffY = Math.abs(target.y - org.y);

        if (diffX == 0)
            return diffY > 2;
        if (diffY == 0)
            return diffX > 2;
        if (diffX - diffY == 0)
            return diffX > 2;
        return false;
    }
    public static boolean ratIsFacingLocation(MapLocation location, RobotInfo robot) {
        Direction d = robot.getLocation().directionTo(location);
        Direction face = robot.getDirection();
        if (face == d || face.rotateLeft() == d || face.rotateRight() == d)
            return true;
        return false;
    }
    public static boolean catIsFacingLocation(MapLocation location, RobotInfo robot) {
        Direction d = robot.getLocation().directionTo(location);
        Direction face = robot.getDirection().opposite();
        if (face == d || face.rotateLeft() == d || face.rotateRight() == d)
            return false;
        return true;
    }
}

