// Stolen from https://github.com/IvanGeffner/BC25/blob/master/basic45/SymmetryManager.java
// MY GOAT!!!
package opp_goobie;

import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class SymmetryManager {

    public static final int H = 0b001, V = 0b010, R = 0b100;
    static final int HC = (~H) & 0b111, VC = (~V) & 0b111, RC = (~R) & 0b111;

    static MapLocation base = null;
    static MapLocation bR, bH, bV;

    static int discardedSyms = 0;
    private static boolean hasPrintedSymmetryDetermined = false;

    static MapLocation L;
    static int symX, symY;

    public static void discardH() {
        if ((discardedSyms & H) == 0) {
            discardedSyms |= H;
        }
    }

    public static void discardV() {
        if ((discardedSyms & V) == 0) {
            discardedSyms |= V;
        }
    }

    public static void discardR() {
        if ((discardedSyms & R) == 0) {
            discardedSyms |= R;
        }
    }

    public static int getSym() {
        return switch (discardedSyms) {
            case HC -> H;
            case VC -> V;
            case RC -> R;
            default -> 0;
        };
    }

    /**
     * Returns the determined symmetry type as a string for debugging.
     *
     * @return "H" for horizontal, "V" for vertical, "R" for rotational, "?" for
     *         unknown, "NONE" for no symmetry
     */
    public static String getSymString() {
        int sym = getSym();
        return switch (sym) {
            case H -> "H";
            case V -> "V";
            case R -> "R";
            default -> {
                if (discardedSyms == (H | V | R)) {
                    yield "NONE";
                }
                yield "?";
            }
        };
    }

    public static boolean isSymmetryDetermined() {
        boolean determined = getSym() != 0;
        if (determined && !hasPrintedSymmetryDetermined) {
            hasPrintedSymmetryDetermined = true;
            CommsSqueak.hasNewSymmetryToRelay = true;
            CommsSqueak.hasNewSymmetryForKing = true;
        }
        return determined;
    }

    /**
     * Returns the current discarded symmetries bitmask (for debugging).
     */
    public static int getDiscardedSyms() {
        return discardedSyms;
    }

    public static void checkSym() throws GameActionException {
        if (base == null)
            return;
        if (Clock.getBytecodesLeft() < 400)
            return;
        if (getSym() != 0)
            return;

        int previousDiscarded = discardedSyms;
        MapInfo[] infos = Robot.rc.senseNearbyMapInfos();
        MapLocation symLocH, symLocV, symLocR;
        for (MapInfo m : infos) {
            if (Clock.getBytecodesLeft() < 1000)
                return;
            L = m.getMapLocation();
            boolean isWall = m.isWall();
            boolean hasCheeseMine = m.hasCheeseMine();

            symX = Robot.W - L.x - 1;
            symY = Robot.H - L.y - 1;

            symLocH = new MapLocation(symX, L.y);
            if (!Robot.rc.onTheMap(symLocH)) {
                discardH();
            } else if (MapData.hasSeen(symLocH)) {
                if (MapData.isWall(symLocH) != isWall || MapData.hasCheeseMine(symLocH) != hasCheeseMine) {
                    discardH();
                }
            }

            symLocV = new MapLocation(L.x, symY);
            if (!Robot.rc.onTheMap(symLocV)) {
                discardV();
            } else if (MapData.hasSeen(symLocV)) {
                if (MapData.isWall(symLocV) != isWall || MapData.hasCheeseMine(symLocV) != hasCheeseMine) {
                    discardV();
                }
            }

            symLocR = new MapLocation(symX, symY);
            if (!Robot.rc.onTheMap(symLocR)) {
                discardR();
            } else if (MapData.hasSeen(symLocR)) {
                if (MapData.isWall(symLocR) != isWall || MapData.hasCheeseMine(symLocR) != hasCheeseMine) {
                    discardR();
                }
            }
        }

        // Check if we discovered new disqualifications from exploring
        if (previousDiscarded != discardedSyms) {
            boolean isRatKing = Robot.rc.getType().isRatKingType();
            int newlyDiscarded = discardedSyms & ~previousDiscarded;

            if (isRatKing) {
                String discardedStr = "";
                if ((newlyDiscarded & R) != 0)
                    discardedStr += "R ";
                if ((newlyDiscarded & V) != 0)
                    discardedStr += "V ";
                if ((newlyDiscarded & H) != 0)
                    discardedStr += "H ";
                Debug.println("RAT KING: Learning symm disqualification from exploration: " + discardedStr.trim());
            }

            if (getSym() != 0) {
                isSymmetryDetermined();
            } else {
                // Symmetry not determined yet, but we discovered new disqualifications
                // Set flag if global symm isn't already known and disqualification is not in
                // shared array
                if (!CommsArray.isSymmetryGloballyKnown()) {
                    int sharedSymmetryBits = CommsArray.readSymmetryBitsFromSharedArray();

                    // Check if any of the newly discovered disqualifications are not in shared
                    // array
                    if ((newlyDiscarded & R) != 0 && (sharedSymmetryBits & 0x1) == 0) {
                        CommsSqueak.hasNewSymmetryForKing = true;
                    } else if ((newlyDiscarded & V) != 0 && (sharedSymmetryBits & 0x2) == 0) {
                        CommsSqueak.hasNewSymmetryForKing = true;
                    } else if ((newlyDiscarded & H) != 0 && (sharedSymmetryBits & 0x4) == 0) {
                        CommsSqueak.hasNewSymmetryForKing = true;
                    }
                }
            }
        }
    }

    public static MapLocation getSymmetric(MapLocation loc) {
        return switch (getSym()) {
            case H -> new MapLocation(Robot.W - loc.x - 1, loc.y);
            case V -> new MapLocation(loc.x, Robot.H - loc.y - 1);
            case R -> new MapLocation(Robot.W - loc.x - 1, Robot.H - loc.y - 1);
            default -> null;
        };
    }

    public static void setBase(MapLocation loc) {
        base = loc;
        bR = new MapLocation(Robot.W - base.x - 1, Robot.H - base.y - 1);
        bV = new MapLocation(base.x, Robot.H - base.y - 1);
        bH = new MapLocation(Robot.W - base.x - 1, base.y);
    }

    public static MapLocation getTarget() {
        if (base == null)
            return null;

        int round = Robot.rc.getRoundNum();
        boolean isKing = Robot.rc.getType().isRatKingType();

        // Hardcoded strategy: first three rats explore different symmetry targets
        // Round 3: rotational symmetry target
        // Round 4: vertical symmetry target
        // Round 5: horizontal symmetry target
        if ((isKing && Robot.turnCount == 0) || (!isKing && round == 2)) {
            // Only return if rotational symmetry hasn't been discarded
            if ((discardedSyms & R) == 0) {
                // Debug.println("isKing: " + isKing + " | round: " + round + " | turn: " +
                // Robot.turnCount + " R LOC: " + bR);
                return bR;
            }
        } else if ((isKing && Robot.turnCount == 1) || (!isKing && round == 3)) {
            // Only return if vertical symmetry hasn't been discarded
            if ((discardedSyms & V) == 0) {
                // Debug.println("isKing: " + isKing + " | round: " + round + " | turn: " +
                // Robot.turnCount + " V LOC: " + bV);
                return bV;
            }
        } else if ((isKing && Robot.turnCount == 2) || (!isKing && round == 4)) {
            // Only return if horizontal symmetry hasn't been discarded
            if ((discardedSyms & H) == 0) {
                // Debug.println("isKing: " + isKing + " | round: " + round + " | turn: " +
                // Robot.turnCount + " H LOC: " + bH);
                return bH;
            }
        }

        // Check symmetric locations in priority order: R -> V -> H
        // Only return locations that haven't been discarded and haven't been seen yet
        if ((discardedSyms & R) == 0 && !MapData.hasSeen(bR)) {
            return bR;
        }
        if ((discardedSyms & V) == 0 && !MapData.hasSeen(bV)) {
            return bV;
        }
        if ((discardedSyms & H) == 0 && !MapData.hasSeen(bH)) {
            return bH;
        }
        return null;
    }

}
