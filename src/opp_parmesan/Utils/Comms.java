package opp_parmesan.Utils;

import battlecode.common.*;
import opp_parmesan.Utils.Globals;
import opp_parmesan.Utils.AdditionalClasses.*;

import static battlecode.common.Direction.EAST;


public class Comms extends Globals {

    public static void init () throws GameActionException {
        if (rc.getType().equals(UnitType.RAT_KING)) {
            rc.writeSharedArray(63, 1);
        }
    }
    public static void startTurn () throws GameActionException {
        if (rc.getType().equals(UnitType.RAT_KING)) {
            // Locations
            int x = (rc.readSharedArray(63)>>3*(rc.getRoundNum()%3))%8;
            rc.writeSharedArray(2*x, rc.getLocation().x);
            rc.writeSharedArray(2*x+1, rc.getLocation().y);

            int v = rc.readSharedArray(63);
            switch (rc.getRoundNum()%3) {
                case 0:
                    v += 1;
                    v &= 0b111000111;
                    break;
                case 1:
                    v += 8;
                    v &= 0b111111;
                    break;
                case 2:
                    v += 64;
                    v &= 0b111111000;
            }
            rc.writeSharedArray(63, v);
        }
        int n = getNumKings();
        for (int i = 0; i < n; i++) {
            kingPos[i] = new MapLocation(rc.readSharedArray(2*i), rc.readSharedArray(2*i+1));
        }
        for (int i = n; i < 5; i++) {
            kingPos[i] = null;
        }
    }
    public static int getNumKings () throws GameActionException {
        return (rc.readSharedArray(63)>>3*((rc.getRoundNum()+2)%3))%8;
    }
    public static int getSymmetry () throws  GameActionException {
        return rc.readSharedArray(10);
    }
    public static void updateSymmetry (int x) throws GameActionException {
        rc.writeSharedArray(10, x);
    }
    public static int encodeDir (Direction dir) {
        return switch (dir) {
            case EAST -> 0;
            case WEST -> 1;
            case NORTH -> 2;
            case SOUTH -> 3;
            case NORTHEAST -> 4;
            case NORTHWEST -> 5;
            case SOUTHEAST -> 6;
            case SOUTHWEST -> 7;
            default -> 0;
        };
    }
    public static Direction decodeDir (int dir) {
        return switch (dir) {
            case 0 -> EAST;
            case 1 -> Direction.WEST;
            case 2 -> Direction.NORTH;
            case 3 -> Direction.SOUTH;
            case 4 -> Direction.NORTHEAST;
            case 5 -> Direction.NORTHWEST;
            case 6 -> Direction.SOUTHEAST;
            case 7 -> Direction.SOUTHWEST;
            default -> EAST;
        };
    }
    public static void squeakEnemies () throws GameActionException {
        // ignore if no threats (trust anson 1v1 micro)
        if ((enemyRobots.length < 1) || hasSqueaked || (catRobots.length > 0 && rc.getType().isRatKingType())) return;
        // evaluate all the threats based off some heuristic
        RobotInfo weakest = null, strongest = null;
        int wScore = 1000000, sScore = 0;
        for (RobotInfo enemy:enemyRobots) {
            // Rat king is always the strongest
            if (enemy.getType() == UnitType.RAT_KING) {
                strongest = enemy;
                sScore = 1000000;
                if (weakest == null) weakest = enemy;
            } else {
                int score = 1000 + 2*enemy.getHealth();
                int penalty = Math.max(0, 6 - (rc.getLocation().distanceSquaredTo(enemy.getLocation()) / 5));
                score += 10*penalty;
                // cheese slows them down
                score -= enemy.getRawCheeseAmount();
                // having a rat is really op
                RobotInfo gun = enemy.getCarryingRobot();
                if (gun != null) {
                    if (gun.getTeam() == myTeam) score += 100*penalty;
                    else score += 60*penalty;
                }
                if (score <= wScore) {
                    weakest = enemy;
                    wScore = score;
                }
                if (score > sScore) {
                    strongest = enemy;
                    sScore = score;
                }
            }
        }


        // Communicate the weakest + strongest
        // encode weakest
        int msg = 0;
        if (weakest != null) {
            // map x, map y, direction
            MapLocation wloc = weakest.getLocation();
            rc.setIndicatorDot(wloc, 0, 255, 0);
            msg += ((wloc.x) << 9) + ((wloc.y) << 3) + encodeDir(weakest.getDirection());
        }
        msg <<= 15;

        msg += rc.getHealth();

        msg += (rc.getDirection().getDirectionOrderNum()-1)<<12;

        msg = -msg;
        if (!hasSqueaked) {
            rc.squeak(msg);
            hasSqueaked = true;
        }
    }
    public static Threat[] readThreats () throws GameActionException {
        int sz = 0;
        for (Message m:newestSqueaks) if (m.getBytes() <= -1) {
            sz += 2;
        }
        Threat[] threats = new Threat[sz];
        int ptr = 0;
        for (Message m:newestSqueaks) if (m.getBytes() <= -1) {
            int msg = -m.getBytes();
            // strongest first
            Direction dir = decodeDir(msg & 0b111);
            msg = msg >> 3;
            int x, y;
            y = (msg & 0b111111);
            msg = msg >> 6;
            x = (msg & 0b111111);
            msg = msg >> 6;
            threats[ptr++] = new Threat(new MapLocation(x, y), dir, true);
            // weakest second
            dir = decodeDir(msg & 0b111);
            msg = msg >> 3;
            y = (msg & 0b111111);
            msg = msg >> 6;
            x = (msg & 0b111111);
            threats[ptr++] = new Threat(new MapLocation(x, y), dir, false);
        }
        return threats;
    }
    public static void alertHunters (RobotInfo cat) throws GameActionException {
        if (cat.getType() != UnitType.CAT) return;
        rc.writeSharedArray(62, cat.getID()%1024);
    }
    public static void updatePreyLoc (MapLocation loc) throws GameActionException {
        int msg = (1 << 27) + (loc.x << 6) + loc.y;
        if (!hasSqueaked) {
            rc.squeak(msg);
            hasSqueaked = true;
        }
    }
    public static MapLocation getPreyLoc () throws GameActionException {
        for (Message m:newestSqueaks) if (m.getBytes() > 0) {
            int msg = m.getBytes();
            if ((msg&(1 << 27)) > 0) {
                int x, y;
                y = msg&0b111111;
                msg = msg >> 6;
                x = msg&0b111111;
                return new MapLocation(x, y);
            }
        }
        return new MapLocation(-1, -1);
    }

    public static void squeakEnemyKing (MapLocation loc) throws GameActionException {
        int msg = (1 << 25) + (loc.x << 6) + loc.y;
        if (!hasSqueaked) {
            rc.squeak(msg);
            hasSqueaked = true;
        }
    }

    public static MapLocation getEnemyKing () throws GameActionException {
        for (Message m:newestSqueaks) if (m.getBytes() > 0) {
            int msg = m.getBytes();
            if ((msg&(1 << 25)) > 0) {
                int x, y;
                y = msg&0b111111;
                msg = msg >> 6;
                x = msg&0b111111;
                return new MapLocation(x, y);
            }
        }
        return null;
    }

    public static void squeakNewKing (MapLocation mine) throws GameActionException {
        int msg = (1 << 28) + (mine.x<<6) + mine.y;
        if (!hasSqueaked) {
            rc.squeak(msg);
            hasSqueaked = true;
        }
    }
    public static MapLocation checkForNewKings () throws GameActionException {
        for (Message m:newestSqueaks) if (m.getBytes() > 0) {
            int msg = m.getBytes();
            if ((msg&(1 << 28)) > 0) {
                int x, y;
                y = msg&0b111111;
                msg = msg >> 6;
                x = msg&0b111111;
                return new MapLocation(x, y);
            }
        }
        return null;
    }

    public static void encodeNewKingPos (MapLocation mine) throws GameActionException {
        rc.writeSharedArray(57, 0);
        rc.writeSharedArray(58, mine.x+1);
        rc.writeSharedArray(59, mine.y+1);
    }

    public static void clearNewKing () throws GameActionException {
        rc.writeSharedArray(58, 0);
    }

    public static int getNumberBots () throws GameActionException {
        return rc.readSharedArray(57);
    }

    public static void incNewKingBotCount (int cur) throws GameActionException {
        rc.writeSharedArray(57, Math.min(cur+1, 20));
    }

    public static MapLocation needNewKing () throws GameActionException {
        int x = rc.readSharedArray(58), y = rc.readSharedArray(59);
        return x > 0 ? new MapLocation(x-1, y-1) : null;
    }


    public static void beingThrown () throws GameActionException {
        if (!hasSqueaked){
            rc.squeak(7 + locationLastRound.directionTo(rc.getLocation()).getDirectionOrderNum());
        }
        hasSqueaked = true;
    }
    public static Thrown[] getAlliesThrown () {
        int sz = 0;
        for (Message msg: newestSqueaks) if (8 <= msg.getBytes() && msg.getBytes() < 16)
            sz++;
        Thrown[] ret = new Thrown[sz];
        sz = 0;
        for (Message msg: newestSqueaks) if (8 <= msg.getBytes() && msg.getBytes() < 16) {
            // TODO: FINISH THIS ONCE THE STUFF IS ANSWERED IN #BUG REPORTS
            ret[sz++] = new Thrown(rc.getLocation(), rc.getDirection());
        }
        return ret;
    }

    public static void squeakForSacrifices (MapLocation loc) throws GameActionException {
        int msg = (1 << 24) + (loc.x << 6) + loc.y;
        if (!hasSqueaked) {
            rc.squeak(msg);
            hasSqueaked = true;
        }
    }


        public static void squeakForSacrificesToo (MapLocation loc) throws GameActionException {
        int msg = (1 << 29) + (loc.x << 6) + loc.y;
        if (!hasSqueaked) {
            rc.squeak(msg);
            hasSqueaked = true;
        }
    }

    public static MapLocation getSacrifices () throws GameActionException {
        MapLocation secondary = null;
        for (Message m:newestSqueaks) if (m.getBytes() > 0) {
            int msg = m.getBytes();
            if ((msg&(1 << 24)) > 0) {
                int x, y;
                y = msg&0b111111;
                msg = msg >> 6;
                x = msg&0b111111;
                return new MapLocation(x, y);
            }
            if ((msg&(1 << 29)) > 0) {
                int x, y;
                y = msg&0b111111;
                msg = msg >> 6;
                x = msg&0b111111;
                secondary = new MapLocation(x+1000, y+1000);
            }
        }
        return secondary;
    }
}
