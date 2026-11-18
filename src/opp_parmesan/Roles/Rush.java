package opp_parmesan.Roles;

import battlecode.common.*;
import opp_parmesan.Utils.*;
import opp_parmesan.Baby;

import static java.lang.Integer.bitCount;

public class Rush extends Globals {

    public static MapLocation spawnLoc = null;
    public static MapLocation rush = null;
    static MapLocation approx = null;
    static int remainingSym = 0;
    static int guessedSym = 0;
    static int[] susFactor = new int[5];
    static int turnsStuck = 0;
    static int bestDist;
    static int approxRound = 0;
    static boolean kingFound = false;

    public static MapLocation getNewRushLocation() throws GameActionException {

//        // If all the symmetries have been tried
//        if (remainingSym == 7) {
//            Baby.explorer = true;
//            guessedSym = 0;
//            return new MapLocation(rng.nextInt(mapWidth), rng.nextInt(mapHeight));
//        }

        int[] possibleSymmetries = Symmetry.getPossibleSymmetries();
        MapLocation[] possibleRushLocations = new MapLocation[6];
        int[] guesses = new int[6];
        int prlSize = 0;

        for (int s : possibleSymmetries) {
            guesses[prlSize] = s;
            possibleRushLocations[prlSize++] = Symmetry.symmetricallyOpposite(spawnLoc, s);

            if (s == 4) {
                guesses[prlSize] = s;
                possibleRushLocations[prlSize++] = Symmetry.symmetricallyOpposite(spawnLoc, s);
                guesses[prlSize] = s;
                possibleRushLocations[prlSize++] = Symmetry.symmetricallyOpposite(spawnLoc, s);
                guesses[prlSize] = s;
                possibleRushLocations[prlSize++] = Symmetry.symmetricallyOpposite(spawnLoc, s);
            }
        }

        int pray = rng.nextInt(prlSize);
        guessedSym = guesses[pray];

        return mapLocationAddNoise(possibleRushLocations[pray], 3);
    }

    public static void run (MapLocation spawn) throws GameActionException {
        if (spawnLoc == null) spawnLoc = spawn;
        spawnLoc = spawn;

        if (rc.canTurn()) {
            for (int i = 0; i < enemyRobots.length; i ++) {
                RobotInfo ri = enemyRobots[i];
                if (ri.getType().equals(UnitType.RAT_KING)) {
                    kingFound = true;
                    Direction dir = rc.getLocation().directionTo(ri.location);
                    Vision.turn(dir);
                    break;
                }
            }
        }

        // return to save the king
        int mode = rc.readSharedArray(60);
        if (mode == 2) {
            // very urgent
            if (!kingFound) {
                Navigator.moveTo(kingPos[0], true);
            }
        } else if (mode == 1) {
            // see which one is closer
            if (rc.getLocation().distanceSquaredTo(rush) > rc.getLocation().distanceSquaredTo(kingPos[0])) {
                Navigator.moveTo(kingPos[0], true);
            }
        }

        // guess symmetry
//        remainingSym |= Symmetry.symm;
        if (kingFound) guessedSym = 0;
        if (guessedSym > 0) {
            if ((Symmetry.symm & guessedSym) > 0) {
                guessedSym = 0;
                rush = null;
            }
        }
        if (rush == null) {
            rush = getNewRushLocation();
            bestDist = rc.getLocation().distanceSquaredTo(rush);
        }
        if (!kingFound && (rc.getLocation().isWithinDistanceSquared(rush, 4))) {
            turnsStuck = 0;
//            susFactor[guessedSym] += 1;
//            if (susFactor[guessedSym] > 3) remainingSym |= guessedSym;
            if (bitCount(Symmetry.symm) == 2) {
                Baby.explorer = true;
                return;
            }
            rush = getNewRushLocation();
            bestDist = rc.getLocation().distanceSquaredTo(rush);
        }
        if (!kingFound && approx != null && rc.getLocation().isWithinDistanceSquared(approx, 2)) {
            approx = new MapLocation(approx.x + rng.nextInt() % 3, approx.y + rng.nextInt() % 3);
        }
        if (!kingFound && (rc.getLocation().isWithinDistanceSquared(rush, 2) || turnsStuck > 19)) {
            turnsStuck = 0;
//            susFactor[guessedSym] += 1;
//            if (susFactor[guessedSym] > 3) remainingSym |= guessedSym;
            rush = getNewRushLocation();
            bestDist = rc.getLocation().distanceSquaredTo(rush);
        }
        // if it can see the enemy king, direct it towards it
        for (RobotInfo robot : enemyRobots) if (robot.getType() == UnitType.RAT_KING) {
            rush = robot.getLocation();
            kingFound = true;
            break;
        }
        // read the squeaks
        for (Message message : rc.readSqueaks(-1)) {
            int m = message.getBytes();
            if ((m&(1 << 30)) == 0) continue;
            m ^= (1 << 30);
            int r = m / 10000;
            int x = (m % 10000) / 100;
            int y = m % 100;
            if (r > approxRound) {
                approxRound = r;
                approx = new MapLocation(x, y);
            }
        }
        if (kingFound) {
            int msg = 10000 * (rc.getRoundNum() + 20) + 100 * rush.x + rush.y;
            msg |= (1 << 30);
            rc.squeak(msg);
            hasSqueaked = true;
        } else if (approx != null) {
            int msg = 10000 * approxRound + 100 * approx.x + approx.y;
            msg |= (1 << 30);
            rc.squeak(msg);
            hasSqueaked = true;
        }
        // no need to return, should be suicidal
        if (rush != null) {
            if (kingFound) Navigator.moveTo(rush, true);
            else if (approx != null) Navigator.moveTo(closestOnMap(approx), true);
            else {
                Navigator.moveTo(rush, true);
                int newDist = rc.getLocation().distanceSquaredTo(rush);
                if (newDist < bestDist) {
                    bestDist = newDist;
                    turnsStuck = 0;
                } else turnsStuck++;
            }
        }

        Attack.attackKing();
        Attack.attackBaby();
    }
}