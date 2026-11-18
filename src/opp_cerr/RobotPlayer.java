package opp_cerr;

import battlecode.common.Clock;
import battlecode.common.RobotController;

public class RobotPlayer {

    static final CERR_Random rng = new CERR_Random();
    static int round = 2001;

    public static void run(RobotController rc) throws Exception {

        //        StringBuilder sb = new StringBuilder();
        //        sb.append(Clock.getBytecodeNum()).append("\n");

        Robot babyRat = new BabyRat(rc);
        //        sb.append("BR").append(Clock.getBytecodeNum()).append('\n');

        Robot daddyRat = new RatKing(rc);
        //        sb.append("RK").append(Clock.getBytecodeNum()).append('\n');

        Attention.init(rc);
        //        sb.append("AT").append(Clock.getBytecodeNum()).append('\n');
        Navigation.init(rc);
        //        sb.append("NV").append(Clock.getBytecodeNum()).append('\n');
        BugNav.init(rc);
        //        sb.append("BN").append(Clock.getBytecodeNum()).append('\n');
        MacroData.init(rc);
        //        sb.append("MD").append(Clock.getBytecodeNum()).append('\n');
        Comms.init(rc);
        //        sb.append("CM").append(Clock.getBytecodeNum()).append('\n');
        Debug.init(rc);
        //        sb.append("DB").append(Clock.getBytecodeNum()).append('\n');
        BugSim.init(rc);
        //        sb.append("BS").append(Clock.getBytecodeNum()).append('\n');
        BugSimKing.init(rc);
        //        sb.append("BK").append(Clock.getBytecodeNum()).append('\n');

        Explore.init(rc);

        CERR_Random.init(rc.getID());
        //        sb.append("CR").append(Clock.getBytecodeNum()).append('\n');

        //        if (rc.getType() == UnitType.BABY_RAT) {
        //            System.out.print(sb);
        //            rc.resign();
        //        }

        while (true) {
            if (round < rc.getRoundNum() - 1) {
                Debug.println("Bytecode limit reached last turn?");
            }
            round = rc.getRoundNum();

            Comms.startTurn(round);

            try {
                switch (rc.getType()) {
                    case BABY_RAT -> babyRat.play();
                    case RAT_KING -> daddyRat.play();
                    default -> throw new Exception("unhandled unit type");
                }
            } catch (Exception e) {
                e.printStackTrace();
                //                rc.resign();
            }

            Comms.endTurn();
            Clock.yield();
        }
    }
}
