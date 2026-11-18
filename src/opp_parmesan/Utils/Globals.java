package opp_parmesan.Utils;

import battlecode.common.*;

import java.util.ArrayList;
import java.util.Random;

import opp_parmesan.Baby;
import opp_parmesan.Roles.Multiking;
import opp_parmesan.Utils.AdditionalClasses.*;
import opp_parmesan.Utils.Comms;
import opp_parmesan.Utils.Movement;
import opp_parmesan.Utils.Reachability;
import opp_parmesan.Utils.Vision;

public class Globals {
    public static RobotController rc;

    public static final int trapGlobalCheeseThreshold = 250;
    public static final int safeCheeseReserve = 600;
    public static final int emergencyReserve = 250;
    public static final int catSpacing = 4;
    public static final int ratSpacing = 2;
    public static final int pacifistRounds = 200;
    public static final int help1Health = 250;
    public static final int help1Enemies = 3;
    public static final int help2Health = 100;
    public static final int help2Enemies = 2;
    public static final int endgame = 1960;
    public static final int midgame = 300;
    public static final int cheeseThreshold = 50;
    public static final int minNumberRats = 12;
    public static final int maxNumberRats = 16;
    public static final int minCheese = 110;
    public static final int doSpawnThreshold = 2500;
    public static final int attackerNumber = 2;
    public static final int useTrapCheeseThreshold = 500;
    public static final int globalCheeseTrapSubstitute = 40;
    public static final double secondBestMoveChance = 0.2;
    public static final int disintegrationHP = 10;
    public final static int minerReturnThreshold = 40;
    public final static int maxCheese = 200;
    public final static int minerSecondaryReturnThreshold = 20;
    public final static int secondaryReturnDistance = 200;
    public final static int newKingCheeseThreshold = 200;
    public final static int newKingSquaresThreshold = 5;
    public final static int newKingSpawnThreshold = 1200;
    public final static int botsForNewKing = 9;
    public final static int giveUpNewKing = 25;
    public final static int closeToKing = 100;

    public static Random rng;
    public static final Direction[] directions = {
            Direction.NORTH,
            Direction.NORTHEAST,
            Direction.EAST,
            Direction.SOUTHEAST,
            Direction.SOUTH,
            Direction.SOUTHWEST,
            Direction.WEST,
            Direction.NORTHWEST,
            Direction.CENTER
    };

    public static MapInfo[] surroundings = null;
    public static RobotInfo[] allyRobots = null;
    public static RobotInfo[] enemyRobots = null;
    public static RobotInfo[] catRobots = null;
    public static RobotInfo[] catsLastTurn = new RobotInfo[0];
    public static MapLocation[] kingPos = null;
    public static MapLocation catLoc = null;
    public static Message[] newestSqueaks = null;
    public static Thrown[] alliesThrown = null;
    public static MapLocation closestKing;
    public static RobotInfo closestEnemy;
    public static MapLocation closestEnemyKing;
    public static int squeaksReadLastRound = 0;
    public static MapLocation locationLastRound = null;
    public static ArrayList<RobotInfo> moreAllies;

    public static int symmetry = 0;
    public static boolean hasSqueaked;
    public static boolean enemiesLastRound = false;
    public static boolean cantTurn;
    public static boolean cantMove;

    public static int mapWidth;
    public static int mapHeight;
    public static int spawnRound;

    public static int internalRoundNum;
    public static int myId;
    public static Team myTeam;
    public static Team opponentTeam;
    public static Threat[] threats;
    public static Direction[] allDirections = Direction.values();
    public static Direction[] adjacentDirections = {
            Direction.NORTH,
            Direction.EAST,
            Direction.SOUTH,
            Direction.WEST,
            Direction.NORTHEAST,
            Direction.SOUTHEAST,
            Direction.SOUTHWEST,
            Direction.NORTHWEST
    };

    public static void init(RobotController robotController) throws  GameActionException {
        rc = robotController;

        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();

        spawnRound = rc.getRoundNum();

        internalRoundNum = rc.getRoundNum() - 1;
        myId = rc.getID();
        myTeam = rc.getTeam();
        opponentTeam = myTeam.opponent();

        kingPos = new MapLocation[5];

        newestSqueaks = new Message[0];
        alliesThrown = new Thrown[0];

        cantMove = false;
        cantTurn = false;

        rng = new Random(rc.getID());

        symmetry = opp_parmesan.Utils.Comms.getSymmetry();

        if (rc.getType().equals(UnitType.BABY_RAT))
            Baby.isNewKing = Multiking.isMultiking();

        opp_parmesan.Utils.Vision.init();
        opp_parmesan.Utils.Comms.init();
        Movement.init();


    }

    public static Message[] lastRound, thisRound;

    public static void startTurn () throws GameActionException {

        Comms.startTurn();


        surroundings = rc.senseNearbyMapInfos();
        allyRobots = rc.senseNearbyRobots(-1, myTeam);

        FastSet knownAllies = new FastSet();
        for (RobotInfo r: allyRobots) knownAllies.add(new MapLocation(r.getID()%64, (r.getID()>>6)%64));

        enemyRobots = rc.senseNearbyRobots(-1, opponentTeam);
        catRobots = rc.senseNearbyRobots(-1,Team.NEUTRAL);

        lastRound = rc.readSqueaks(rc.getRoundNum()-1);
        thisRound = rc.readSqueaks(rc.getRoundNum());

        internalRoundNum += 1;

        if (rc.getRoundNum() != internalRoundNum) {
            // oh no
            newestSqueaks = new Message[thisRound.length];
            for (int i = 0; i < thisRound.length; i++) newestSqueaks[i] = thisRound[i];
            internalRoundNum = rc.getRoundNum();
            if (rc.getRoundNum() > spawnRound+5) System.out.println("bytecode limit exceeeeeeeeded :(");
        } else if (lastRound.length >= squeaksReadLastRound) {
            newestSqueaks = new Message[lastRound.length - squeaksReadLastRound + thisRound.length];

            for (int i = squeaksReadLastRound; i < lastRound.length; i++)
                newestSqueaks[i - squeaksReadLastRound] = lastRound[i];
            for (int i = 0; i < thisRound.length; i++)
                newestSqueaks[i + lastRound.length - squeaksReadLastRound] = thisRound[i];
        }

        MapLocation enemyKingHeard = null;

        int sz = 0, sza = 0;
        for (Message msg: newestSqueaks) {
            int m = msg.getBytes();
            if (msg.getBytes() < 0) sz++;
            else if (8 <= msg.getBytes() && msg.getBytes() < 16) sza++;
            else if (enemyKingHeard == null && (m&(1 << 25)) > 0) {
                int x, y;
                y = m&0b111111;
                m = m >> 6;
                x = m&0b111111;
                enemyKingHeard = new MapLocation(x, y);
            }
        }

        Threat[] threats = new Threat[sz];
        alliesThrown = new Thrown[sza];

        moreAllies = new ArrayList<>();


        int ptr = 0;
        sz = 0;
        for (Message m:newestSqueaks) {
            if (m.getBytes() <= -1) {
                int msg = -m.getBytes();
                // strongest first

                int hp = msg%1024;
                msg >>= 12;
                Direction dir = Direction.DIRECTION_ORDER[1+msg%8];
                if (!knownAllies.contains(new MapLocation(m.getSenderID()%64, (m.getSenderID()>>6)%64))) {
                    moreAllies.add(new RobotInfo(m.getSenderID(), myTeam, UnitType.BABY_RAT, hp, m.getSource(), dir, 0, 0, null));
                }
                msg >>= 3;

                // weakest second
                dir = Comms.decodeDir(msg & 0b111);
                msg = msg >> 3;
                int y = (msg & 0b111111);
                msg = msg >> 6;
                int x = (msg & 0b111111);
                threats[ptr++] = new Threat(new MapLocation(x, y), dir, false);
            } else if (8 <= m.getBytes() && m.getBytes() < 16) {
                alliesThrown[sz++] = new Thrown(m.getSource(), Direction.DIRECTION_ORDER[m.getBytes()-7]);
            }
        }




        hasSqueaked = false;
        int closest = 0;
        for (int i = 1; i < 5; i++) {
            if (kingPos[i] == null) break;
            if (rc.getLocation().distanceSquaredTo(kingPos[closest]) > rc.getLocation().distanceSquaredTo(kingPos[i]))
                closest = i;
        }
        closestKing = kingPos[closest];

        if (rc.isBeingThrown())
            Comms.beingThrown();



        Vision.startTurn();
        if (rc.getRoundNum() != spawnRound && rc.getType().equals(UnitType.BABY_RAT) && enemyRobots.length > 0) {

            for (RobotInfo ri : allyRobots) {
                if (ri.getType().equals(UnitType.BABY_RAT)) {
                    Vision.isWall[ri.location.x] |= 1L<<ri.location.y;
                } else {
                    for (MapLocation ml : rc.getAllLocationsWithinRadiusSquared(ri.location, 2)) {
                        if (Vision.hasSeenLocation(ml)) Vision.isWall[ri.location.x] |= 1L<<ri.location.y;
                    }
                }
            }


            Reachability.checkReachability(rc.getDirection());
            //for (MapLocation ml : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), -1))
            //    if (Vision.hasSeenLocation(ml) && Vision.canReach(ml))
            //        rc.setIndicatorDot(ml, 125, 125, 125);


            for (RobotInfo ri : allyRobots) {
                if (ri.getType().equals(UnitType.BABY_RAT)) {
                    Vision.isWall[ri.location.x] &= ~(1L<<ri.location.y);
                } else {
                    for (MapLocation ml : rc.getAllLocationsWithinRadiusSquared(ri.location, 2)) {
                        if (Vision.hasSeenLocation(ml)) Vision.isWall[ri.location.x] &= ~(1L<<ri.location.y);
                    }
                }
            }
        }


        closestEnemy=null;
        closestEnemyKing = null;
        ArrayList<RobotInfo> reachableEnemies = new ArrayList<>();
        for(RobotInfo info: enemyRobots){
            if (Vision.hasSeenLocation(info.location) && !Vision.canReach(info.location)) continue;
            if(info.type.equals(UnitType.RAT_KING)) {
                if(closestEnemyKing == null || rc.getLocation().distanceSquaredTo(info.location) < rc.getLocation().distanceSquaredTo(closestEnemyKing)){
                    closestEnemyKing = info.location;
                }
            }
            else if(closestEnemy==null || rc.getLocation().distanceSquaredTo(info.location) < rc.getLocation().distanceSquaredTo(closestEnemy.location) || (info.health<closestEnemy.health &&rc.getLocation().distanceSquaredTo(info.location) <= rc.getLocation().distanceSquaredTo(closestEnemy.location))){
                closestEnemy = info;
            }
            reachableEnemies.add(info);
        }
        if (closestEnemyKing == null) closestEnemyKing = enemyKingHeard;

        enemyRobots = reachableEnemies.toArray(new RobotInfo[0]);

        for(Threat t: threats){
            rc.setIndicatorDot(t.loc, 255, 0, 0);
            if (Vision.hasSeenLocation(t.loc) && !Vision.canReach(t.loc)) continue;
            if(closestEnemy==null || rc.getLocation().distanceSquaredTo(t.loc) < rc.getLocation().distanceSquaredTo(closestEnemy.location)){
                closestEnemy = new RobotInfo(-1, opponentTeam, UnitType.BABY_RAT, 100, t.loc,t.dir,0, 0, null);
            }
        }

        catLoc = null;
        for (RobotInfo ri: catRobots) {
            if (Vision.hasSeenLocation(ri.location) && !Vision.canReach(ri.location)) continue;
            catLoc = ri.location;
        }


    }

    public static void endTurn () throws GameActionException {
        // TODO: OPTIMISE THIS
        squeaksReadLastRound = rc.readSqueaks(rc.getRoundNum()).length;
        locationLastRound = rc.getLocation();
        Vision.endTurn();
        if (closestEnemyKing != null) Comms.squeakEnemyKing(closestEnemyKing);
        catsLastTurn = catRobots;
        enemiesLastRound = enemyRobots.length > 0;
    }

    public static void debugBytecode(String tag, int interval) {
        if (rc.getRoundNum() % interval != 0) return;
        System.out.println("Bytecode used: " + Clock.getBytecodeNum() + " - " + tag);
    }

    public static void debug(String string, int interval) {
        if (rc.getRoundNum() % interval != 0) return;
        System.out.println(string);
    }

    public static int approxNumberBabies() {
        return 4 * ((rc.getCurrentRatCost() / 10) - 1);
    }

    public static MapLocation[] getRobotLocations(MapLocation loc, UnitType type) {
        if (type == UnitType.RAT_KING) {
            return new MapLocation[] {
                    loc.add(directions[0]),
                    loc.add(directions[1]),
                    loc.add(directions[2]),
                    loc.add(directions[3]),
                    loc.add(directions[4]),
                    loc.add(directions[5]),
                    loc.add(directions[6]),
                    loc.add(directions[7]),
                    loc.add(directions[8])
            };
        }
        if (type == UnitType.CAT) {
            return new MapLocation[] {
                    loc.add(Direction.NORTH),
                    loc.add(Direction.NORTHEAST),
                    loc.add(Direction.EAST),
                    loc.add(Direction.CENTER)
            };
        }
        return new MapLocation[] {loc};
    }

    public static MapLocation closestOnMap(MapLocation ml) {
        return new MapLocation(ml.x < mapWidth ? (ml.x >= 0 ? ml.x : 0) : mapWidth-1, ml.y < mapHeight ? (ml.y >= 0 ? ml.y : 0) : mapHeight-1);
    }

    public static boolean isWithinVision (MapLocation robotLocation, Direction robotDirection, MapLocation location, int visionRadiusSquared) {
        if (visionRadiusSquared != -1 && visionRadiusSquared < robotLocation.distanceSquaredTo(location))
            return false;
        location = new MapLocation(location.x- robotLocation.x, location.y- robotLocation.y);
        if (robotDirection.getDirectionOrderNum() <= 4) {
            if (robotDirection.getDirectionOrderNum() <= 2) {
                if (robotDirection.getDirectionOrderNum() == 1) {
                    // WEST
                    return location.x <= location.y && location.y <= -location.x;
                }
                // NORTHWEST
                return location.x <= 0 && location.y >= 0;
            }
            if (robotDirection.getDirectionOrderNum() == 3) {
                // NORTH
                return -location.y <= location.x && location.x <= location.y;
            }
            // NORTHEAST
            return location.x >= 0 && location.y >= 0;
        }
        if (robotDirection.getDirectionOrderNum() <= 6) {
            if (robotDirection.getDirectionOrderNum() == 5) {
                // EAST
                return -location.x <= location.y && location.y <= location.x;
            }
            // SOUTHEAST
            return location.x >= 0 && location.y <= 0;
        }
        if (robotDirection.getDirectionOrderNum() == 7) {
            // SOUTH
            return location.y <= location.x && location.x <= -location.y;
        }
        // SOUTHWEST
        return location.x <= 0 && location.y <= 0;
    }

    public static MapLocation mapLocationAddNoise(MapLocation ml, int maxDist) {
        ml = ml.translate(rng.nextInt() % maxDist, rng.nextInt() % maxDist);
        return closestOnMap(ml);
    }

    public static void dontTurn() { cantTurn = false;}
    public static void dontMove() { cantMove = false;}

    public static void pickUpCheese (MapLocation ml) throws GameActionException {
        if (!rc.canPickUpCheese(ml)) return;
        int x = rc.senseMapInfo(ml).getCheeseAmount();
        if (x+rc.getRawCheese() > maxCheese)
            rc.pickUpCheese(ml, maxCheese-rc.getRawCheese());
        else
            rc.pickUpCheese(ml);
    }

}