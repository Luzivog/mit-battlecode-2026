package opp_cerr;

import battlecode.common.*;

public abstract class Robot {
    static final int NAV_MIN_BYTECODE = 5000;

    static RobotController rc;

    static CERR_Random random = new CERR_Random();
    static CERR_Random idRandom;
    static int r;
    static int g;
    static int b;

    static RobotInfo[] enemyRobots, allyRobots, neutralRobots;
    static RobotInfo[] allEnemyRobots, allAllyRobots, allNeutralRobots;
    static RobotInfo allyKing; // probably wont see more than 1 ally king in range
    static int spawnTurn = 0;
    static MapLocation spawnLoc = null;

    static final boolean IGNORE_UNREACHABLE = true;

    // random.nextInt(10) > 0;

    public Robot(RobotController R) throws GameActionException {
        rc = R;
        spawnLoc = rc.getLocation();

        idRandom = new CERR_Random();
        r = Robot.idRandom.nextInt(256);
        g = Robot.idRandom.nextInt(256);
        b = Robot.idRandom.nextInt(256);

        spawnTurn = rc.getRoundNum();
    }

    public abstract void turn() throws Exception;

    public void updateGlobalsRobot() throws GameActionException {
        Navigation.nearbyMapInfos = rc.senseNearbyMapInfos();

        Navigation.refreshTurn();
        Navigation.scoutVisible();

        allAllyRobots = rc.senseNearbyRobots(-1, rc.getTeam());
        allEnemyRobots = rc.senseNearbyRobots(-1, rc.getTeam().opponent());
        allNeutralRobots = rc.senseNearbyRobots(-1, Team.NEUTRAL);

        if (rc.getType() == UnitType.BABY_RAT && IGNORE_UNREACHABLE) {
            if (!Navigation.localReachCenter.equals(rc.getLocation()))
                Navigation.recalcLocalReach(rc.getLocation());
            allyRobots = Navigation.filterLocalReachable(allAllyRobots);
            enemyRobots = Navigation.filterLocalReachable(allEnemyRobots);

            allyKing = null;
            for (RobotInfo ri : allAllyRobots) {
                if (ri.getType() == UnitType.RAT_KING) {
                    allyKing = ri;
                    break;
                }
            }

            //            neutralRobots = Navigation.filterLocalReachable(allNeutralRobots);
        } else {
            allyRobots = allAllyRobots;
            enemyRobots = allEnemyRobots;
        }
        neutralRobots = allNeutralRobots;
    }

    public void play() throws Exception {
        updateGlobalsRobot();

        turn();

        if (rc.getRoundNum() >= 2 && Clock.getBytecodesLeft() > NAV_MIN_BYTECODE) {
            Navigation.updateSymmetry();
        }
    }
}
