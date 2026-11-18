package opp_obg;

import battlecode.common.*;

public class VisionManager {

    static MapLocation closestCheese;
    static int closestCheeseDist;
    static RobotInfo closestCat;
    static int closestCatDist;
    static RobotInfo closestRat;
    static int closestRatDist;
    static RobotInfo[] visibleUnits;
    static boolean trap;

    static boolean carrying = false;

    static MapInfo[] mis;

    static MapLocation collisionObject;
    static int collisionType;
    static MapLocation flyingUnit;

    static RobotInfo[] flyingRobots = new RobotInfo[50];
    static int flyingRobotsAmount = 0;

    /*Store the information about what we currently see in this round. We should use this for all our decision-making*/
    static void update() throws GameActionException {
        if (!MyUnit.isKing){
            ThrowHelper.collisionInit();
            carrying = MyUnit.rc.getCarrying() != null && MyUnit.rc.isActionReady();
        }
        updateMap();
        updateUnits();
    }

    static void updatePostMovement() throws GameActionException {
        if (!MyUnit.isKing){
            ThrowHelper.collisionInit();
            carrying = MyUnit.rc.getCarrying() != null && MyUnit.rc.isActionReady();
        }
        updateMapPostMovement();
        updateUnitsPostMovement();
    }

    private static void updateUnitsArray(){
        visibleUnits = MyUnit.rc.senseNearbyRobots();
    }

    static void updateMap() throws GameActionException {
        trap = false;
        mis = MyUnit.rc.senseNearbyMapInfos();
        int d;
        closestCheese = null;
        flyingRobotsAmount = 0;
        for (MapInfo m : mis){
            if (!m.isPassable()){
                if (m.isWall()) MapData.addWall(m.getMapLocation());
                if (carrying){
                    collisionType = ThrowManager.IMPASSABLE;
                    collisionObject = m.getMapLocation();
                    ThrowHelper.addCollision();
                }
            }
            else if (m.hasCheeseMine()) MapData.addCheese(m.getMapLocation());
            if (m.getTrap() == TrapType.RAT_TRAP) trap = true;

            if (m.getCheeseAmount() > 0){
                MapLocation cheeseLoc = m.getMapLocation();
                if (MyUnit.rc.canPickUpCheese(cheeseLoc)){
                    MyUnit.rc.pickUpCheese(cheeseLoc);
                    continue;
                }
                d = LocalBFS.computeDistance(cheeseLoc);
                if (d > Constants.DIST_INF) continue;
                if (closestCheese == null || closestCheeseDist > d){
                    closestCheese = cheeseLoc;
                    closestCheeseDist = d;
                }
            }
        }
    }

    static void checkFlyingRobots(){
        for (int i = 0; i < flyingRobotsAmount; ++i){
            RobotInfo r = flyingRobots[i];
            if (MovementManager.flyingIDs.contains(r.getID())) return;
            flyingUnit = r.getLocation();
            DodgeManager.addFlying();
        }
    }

    static void updateMapPostMovement(){
        if (!carrying) return;
        mis = MyUnit.rc.senseNearbyMapInfos();
        for (MapInfo m : mis){
            if (!m.isPassable()){
                collisionType = ThrowManager.IMPASSABLE;
                collisionObject = m.getMapLocation();
                ThrowHelper.addCollision();
            }
        }
    }

    static void updateUnitsPostMovement(){
        updateUnitsArray();
        if (MyUnit.isKing || !carrying) return;
        for (RobotInfo r : visibleUnits) {
            addObstacle(r);
        }
    }

    static void updateUnits() throws GameActionException {
        updateUnitsArray();
        closestCat = null;
        closestRat = null;
        int d;
        for (RobotInfo r : visibleUnits){
            switch (r.getType()) {
                case CAT:
                    MapLocation catLoc = Util.getClosestCatLoc(r.getLocation());
                    d = LocalBFS.computeDistance(catLoc);
                    if (d > Constants.DIST_INF) {
                        MicroManager.CatIDs.add(r.getID());
                        break;
                    }
                    MicroManager.addCat(r.getLocation(), r.getID(), r.getDirection()); //Cat Micro
                    if (closestCat == null || d < closestCatDist) {
                        closestCat = r;
                        closestCatDist = d;
                    }
                    break;
                default:
                    d = LocalBFS.computeDistance(r.getLocation());
                    if (r.getTeam() == MyUnit.myTeam){
                        if (d > Constants.DIST_INF){
                            MicroManager.AllyIDs.add(r.getID());
                            break;
                        }
                        MicroManager.addAlly(r.getLocation(), r.getID()); //Ally Micro
                        break;
                    }
                    if (d > Constants.DIST_INF){
                        MicroManager.EnemyIDs.add(r.getID());
                        break;
                    }
                    MicroManager.addEnemy(r.getLocation(), r.getID(), r.getDirection(), r.getHealth()); //Enemy Micro
                    if (closestRat == null || d < closestRatDist) {
                        closestRat = r;
                        closestRatDist = d;
                    }
                    break;
            }
            if (carrying) addObstacle(r);
        }
        if (closestCat != null){
            CatSurveillance.updateCatInfo(closestCat.getLocation(), closestCat.getDirection(), closestCat.getID());
        } else if (CatSurveillance.lastCatSeen.loc != null && MyUnit.rc.canSenseLocation(CatSurveillance.lastCatSeen.loc)){
            CatSurveillance.lastCatSeen.loc = null;
        }
    }

    static void addObstacle(RobotInfo r){
        switch(r.getType()){
            case CAT:
                collisionType = ThrowManager.CAT;
                collisionObject = r.getLocation();
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.NORTH);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.EAST);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.NORTHEAST);
                ThrowHelper.addCollision();
                break;
            case RAT_KING:
                if (r.getTeam() == MyUnit.myTeam) collisionType = ThrowManager.MY_RAT_KING;
                else collisionType = ThrowManager.ENEMY_RAT_KING;
                collisionObject = r.getLocation().add(Direction.NORTH);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.NORTHEAST);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.EAST);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.SOUTHEAST);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.SOUTH);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.SOUTHWEST);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.WEST);
                ThrowHelper.addCollision();
                collisionObject = r.getLocation().add(Direction.NORTHWEST);
                ThrowHelper.addCollision();
            default:
                if (r.getTeam() == MyUnit.myTeam) collisionType = ThrowManager.MY_UNIT;
                else collisionType = ThrowManager.ENEMY_UNIT;
                collisionObject = r.getLocation();
                ThrowHelper.addCollision();
        }
    }

}
