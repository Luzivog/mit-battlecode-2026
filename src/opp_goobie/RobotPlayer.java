package opp_goobie;

import battlecode.common.*;

public class RobotPlayer {
    static RobotController rc;

    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {
        RobotPlayer.rc = rc;
        Debug.init();

        Robot r = switch (rc.getType()) {
            case BABY_RAT -> new BabyRat(rc);
            case RAT_KING -> new RatKing(rc);
            case CAT -> throw new RuntimeException("We own a cat now apparently.");
        };

        while (true) {
            try {
                if ((r instanceof BabyRat && rc.getType().isRatKingType())) {
                    r = new RatKing(rc);
                }
                r.runOneTurn();
            } catch (GameActionException e) {
                Debug.println("GameActionException");
                e.printStackTrace();
            } catch (Exception e) {
                Debug.println("Exception");
                e.printStackTrace();
            } finally {
                Robot.turnCount++;
                Clock.yield();
            }
        }
    }
}
