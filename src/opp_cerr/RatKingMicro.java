package opp_cerr;

import battlecode.common.*;

public class RatKingMicro {
    static RobotController rc;
    static RobotInfo[] enemyRobots, friendlyRobots, neutralRobots;
    static int roundNumber;
    static MapLocation myLoc;

    static int myMoveNumber;

    public static void init(RobotController r) {
        rc = r;
    }

    public static boolean processTurn(RobotInfo[] er, RobotInfo[] nr, RobotInfo[] fr)
            throws GameActionException {
        try {
            setGlobals(er, nr, fr, -1);
            return movementMicro();
        } catch (Exception e) {
            System.out.println("WHAT???????????????");
            e.printStackTrace();
            return false;
        }
    }

    public static void setGlobals(RobotInfo[] er, RobotInfo[] nr, RobotInfo[] fr, int moveNumber) {
        myMoveNumber = moveNumber;
        enemyRobots = er;
        neutralRobots = nr;
        friendlyRobots = fr;
        roundNumber = rc.getRoundNum();
        myLoc = rc.getLocation();
    }

    static void move(Direction d) throws GameActionException {
        if (!rc.isMovementReady()) {
            return;
        }
        if (rc.getDirection() == d) {
            rc.moveForward();
        } else {
            rc.move(d);
        }
    }

    public static boolean movementMicro() throws GameActionException {
        rc.setIndicatorString("WOWW");
        MicroInfo[] microInfo = new MicroInfo[9];
        for (int i = 9; i-- > 0; ) {
            microInfo[i] = new MicroInfo(Direction.allDirections()[i]);
        }

        for (int i = enemyRobots.length; i-- > 0; ) {
            RobotInfo r = enemyRobots[i];

            microInfo[0].updateEnemy(r);
            microInfo[1].updateEnemy(r);
            microInfo[2].updateEnemy(r);
            microInfo[3].updateEnemy(r);
            microInfo[4].updateEnemy(r);
            microInfo[5].updateEnemy(r);
            microInfo[6].updateEnemy(r);
            microInfo[7].updateEnemy(r);
            microInfo[8].updateEnemy(r);
        }

        for (int i = neutralRobots.length; i-- > 0; ) {
            RobotInfo r = neutralRobots[i];

            microInfo[0].updateNeutral(r);
            microInfo[1].updateNeutral(r);
            microInfo[2].updateNeutral(r);
            microInfo[3].updateNeutral(r);
            microInfo[4].updateNeutral(r);
            microInfo[5].updateNeutral(r);
            microInfo[6].updateNeutral(r);
            microInfo[7].updateNeutral(r);
            microInfo[8].updateNeutral(r);
        }

        for (int i = friendlyRobots.length; i-- > 0; ) {
            RobotInfo r = friendlyRobots[i];

            microInfo[0].updateAlly(r);
            microInfo[1].updateAlly(r);
            microInfo[2].updateAlly(r);
            microInfo[3].updateAlly(r);
            microInfo[4].updateAlly(r);
            microInfo[5].updateAlly(r);
            microInfo[6].updateAlly(r);
            microInfo[7].updateAlly(r);
            microInfo[8].updateAlly(r);
        }

        MicroInfo bestMicro = microInfo[8];
        for (int i = 0; i < 8; ++i) {
            if (microInfo[i].isBetter(bestMicro, rc.getActionCooldownTurns()))
                bestMicro = microInfo[i];
        }
        rc.setIndicatorString("best micro dir is " + bestMicro.dir);

        if (bestMicro.dir == Direction.CENTER) return true;
        if (rc.canMove(bestMicro.dir)) {
            move(bestMicro.dir);
            //            rc.setIndicatorString("moving in that dir" + bestMicro.dir);
            //            rc.move(bestMicro.dir);
            return true;
        } else {
            rc.setIndicatorString("alkdsjflkajsdf huh?");
        }
        rc.setIndicatorString("uh oh");
        return false;
    }

    static class MicroInfo {
        Direction dir;
        MapLocation location;
        boolean canMove = true;
        int dps = 0;
        int friendsNearby = 0;
        int closestDist = 1000;

        public MicroInfo(Direction dir) throws GameActionException {
            this.dir = dir;
            this.location = rc.getLocation().add(dir);
            closestDist = 1000;
            if (dir != Direction.CENTER && !rc.canMove(dir)) {
                canMove = false;
            }
        }

        void updateEnemy(RobotInfo unit) {
            if (unit.getLocation().distanceSquaredTo(location) < 9) {
                dps += 10;
            }
            closestDist = Math.min(closestDist, location.distanceSquaredTo(unit.getLocation()));
        }

        void updateNeutral(RobotInfo unit) {
            if (unit.getLocation().distanceSquaredTo(location) < 49) {
                dps += 33;
            }
            closestDist = Math.min(closestDist, location.distanceSquaredTo(unit.getLocation()));
        }

        void updateAlly(RobotInfo unit) {
            //            if (!canMove) {
            if (unit.getLocation().distanceSquaredTo(location) < 9) {
                friendsNearby++;
            }
            //            }
        }

        boolean isBetter(MicroInfo M, int cooldown) {
            if (!canMove) return false;
            if (!M.canMove) return true;

            if (dps < M.dps) {
                return true;
            } else if (M.dps < dps) {
                return false;
            } else {
                return closestDist > M.closestDist;
            }
        }
    }
}
