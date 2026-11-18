package fuzz_api;

import battlecode.common.*;

// API fuzzer: every RobotController method with legal and illegal arguments, results folded into a hash that
// goes to squeaks, the shared array and indicator strings. Tally lines "FZT ..." on stdout.
public class RobotPlayer {
    static RobotController rc;
    // TRACE prints the hash before every probe: diff two runs' logs to find where they diverge
    static final boolean TRACE = false;

    public static void run(RobotController r) throws GameActionException {
        rc = r;
        P.rc = r;
        P.W = r.getMapWidth();
        P.H = r.getMapHeight();
        B.init(r);
        while (true) {
            int round = rc.getRoundNum();
            R.seed(rc.getID(), round, 7);
            B.escapeTurn = rc.getCarrying() != null && R.next(3) == 0;
            if (B.escapeTurn) {
                // no catch at all this turn: anything thrown escapes run() and kills the robot
                if (rc.getType().isBabyRatType()) B.carrier();
            } else {
                try {
                    turn();
                } catch (GameActionException e) {
                    P.gae(e);
                } catch (P.Wrapped w) {
                    P.gae((GameActionException) w.getCause());
                } catch (NullPointerException e) {
                    T.r(P.cur, T.NPE);
                    R.mix(0x99);
                } catch (IndexOutOfBoundsException e) {
                    T.r(P.cur, e instanceof ArrayIndexOutOfBoundsException ? T.AIOOBE : T.IOOBE);
                    R.mix(0x88);
                } catch (RuntimeException e) {
                    T.r(P.cur, T.RTE);
                    R.mix(0x77);
                }
            }
            if (TRACE) System.out.println("TE " + rc.getID() + " " + round + " h" + R.h + " cur" + P.cur);
            if (round % 50 == 0) T.print(rc, "periodic");
            Clock.yield();
        }
    }

    static void turn() throws GameActionException {
        boolean king = rc.getType().isRatKingType();
        boolean probesFirst = R.next(4) == 0;
        if (probesFirst) probes(king);
        P.cur = T.behaviour;
        if (king) B.king();
        else B.rat();
        if (!probesFirst) probes(king);
        finish();
        if (R.next(20) == 0) {
            // escapes into run()'s handlers
            P.call(P.pick());
        }
        if (!king && B.wasRat && R.mixOf(B.id, 3) % 60 == 0 && rc.getRoundNum() > 150 + B.id % 600) {
            T.r(T.disintegrate, T.OK);
            T.print(rc, "disintegrate");
            rc.disintegrate();
        }
    }

    static void probes(boolean king) {
        int reserve = king ? 6000 : 5000;
        for (int n = 0; n < 30 && Clock.getBytecodesLeft() > reserve; n++) P.run1(P.pick());
    }

    static void finish() throws GameActionException {
        P.cur = T.behaviour;
        String s = "h" + Integer.toHexString(R.h) + " " + B.RN[B.role] + " " + P.lastMsg;
        rc.setIndicatorString(s);
        rc.squeak(R.h);
        if (rc.getType().isRatKingType()) {
            rc.writeSharedArray(8 + rc.getRoundNum() % 56, R.h & 1023);
        }
        if (B.home != null && rc.onTheMap(B.home)) rc.setIndicatorDot(B.home, 255, R.h & 255, 0);
    }
}
