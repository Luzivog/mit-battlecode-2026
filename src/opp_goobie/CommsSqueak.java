package opp_goobie;

import battlecode.common.*;
import opp_goobie.Fast.*;

/**
 * Squeak Layout:
 * [bits 0-11: first location] [bits 12-23: second location] [bit 24: combat]
 * [bits 25-28: unused] [bits 29-31: symmetry]
 */
public class CommsSqueak {
    private static final int LOCATION_MASK = 0xFFF; // 12 bits: (1 << 12) - 1
    private static final int SECOND_LOC_SHIFT = 12;
    private static final int DIR_SHIFT = 12; // Direction starts at bit 12 for combat squeaks
    private static final int CARRY_BIT_POSITION = 27;
    private static final int COMBAT_BIT_POSITION = 28;
    private static final int SYMMETRY_SHIFT = 29;
    private static final int SYMMETRY_MASK = 0x7; // 3 bits: (1 << 3) - 1

    private static final int NULL_LOCATION_INT = Util.NULL_LOCATION_INT;

    public static final FastIntSet discoveredPOILocsForKing = new FastIntSet();
    private static final FastIntSet discoveredPOILocs = new FastIntSet();

    public static boolean hasNewSymmetryToRelay = false;
    public static boolean hasNewSymmetryForKing = false;

    static int allies2;
    static int allies5;
    static int allies16;
    static final int RANGE_EXTENDED = 8;
    static FastIntSet checkedIds = new FastIntSet();
    static FastIntSet processedSqueaks = new FastIntSet();
    static FastIntSet combatSqueaks = new FastIntSet();
    static FastIntSet enemies = new FastIntSet();

    public static boolean trySqueak(int data) throws GameActionException {
        return Robot.rc.squeak(data);
    }

    private static int packDualLocationsWithSymmetry(int loc1, int loc2OrDir, int symmetryBits, boolean isCombat) {
        int packed = (loc1 & LOCATION_MASK);

        if (isCombat) {
            // loc2OrDir is the direction in this case
            packed |= (loc2OrDir & 0x7) << DIR_SHIFT;
            packed |= 1 << COMBAT_BIT_POSITION;
            if (Robot.rc.isBeingCarried())
                packed |= 1 << CARRY_BIT_POSITION;
        } else {
            // loc2OrDir is the second location
            packed |= ((loc2OrDir & LOCATION_MASK) << SECOND_LOC_SHIFT);
        }

        packed |= (symmetryBits & SYMMETRY_MASK) << SYMMETRY_SHIFT;
        return packed;
    }

    private static int extractSymmetryBits(int packedSqueak) {
        return (packedSqueak >> SYMMETRY_SHIFT) & SYMMETRY_MASK;
    }

    private static int[] getAndRemoveUpTo2(FastIntSet set) {
        if (set.size() == 0) {
            return null;
        }

        StringBuilder keys = set.keys;
        int loc1 = keys.charAt(0);
        int loc2 = (keys.length() > 1) ? keys.charAt(1) : NULL_LOCATION_INT;

        set.remove(loc1);
        if (loc2 != NULL_LOCATION_INT) {
            set.remove(loc2);
        }

        return new int[] { loc1, loc2 };
    }

    private static int getSymmetryBitsForSqueak() throws GameActionException {
        if (CommsArray.isSymmetryGloballyKnown()) {
            return 0;
        }
        return getCurrentSymmetryBits();
    }

    public static void squeakBufferedInfo() throws GameActionException {
        MapLocation myLoc = Robot.rc.getLocation();
        boolean isRatKingInRange = myLoc.distanceSquaredTo(BabyRat.closestRatKing) <= UnitType.RAT_KING
                .getVisionRadiusSquared();

        int loc1 = NULL_LOCATION_INT;
        int loc2 = NULL_LOCATION_INT;
        boolean isCombatSqueak = false;

        // Squeak for combat as first prio
        RobotInfo[] enemies = Robot.rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.opponent);

        if (enemies.length != 0) {
            Debug.addIndicatorString(" !COMB ");
            isCombatSqueak = true;
            // Encode the closest enemy location in loc1 and direction in loc2
            RobotInfo closestEnemy = Util.getClosestEnemyRobot(enemies);
            if (closestEnemy != null) {
                loc1 = Util.locationToInt(closestEnemy.getLocation());
                loc2 = closestEnemy.direction.ordinal();
            }
        } else {
            // Squeak for king POI and symm as second prio
            if (isRatKingInRange) {
                if (discoveredPOILocsForKing.size() > 0) {
                    int[] locs = getAndRemoveUpTo2(discoveredPOILocsForKing);
                    if (locs != null) {
                        loc1 = locs[0];
                        loc2 = locs[1];
                        Debug.addIndicatorString(" !KPOI ");
                    }
                }
                if (getSymmetryBitsForSqueak() != 0 && hasNewSymmetryForKing) {
                    Debug.addIndicatorString(" !KSYM ");
                }
            } else {
                // if (getSymmetryBitsForSqueak() != 0 && hasNewSymmetryToRelay) {
                // Debug.addIndicatorString(" !RSYM ");
                // shouldSqueak = true;
                // }
            }
        }

        // Debug.addIndicatorString(
        // "SQUEAK: K: " + hasNewSymmetryForKing + " R: " + hasNewSymmetryToRelay + " C:
        // " + isCombatSqueak);

        int symmetryBitsToSqueak = 0;
        if (isRatKingInRange && hasNewSymmetryForKing) {
            symmetryBitsToSqueak = getSymmetryBitsForSqueak();
        } else if (!isRatKingInRange && hasNewSymmetryToRelay) {
            symmetryBitsToSqueak = getSymmetryBitsForSqueak();
        }

        int packed = packDualLocationsWithSymmetry(loc1, loc2, symmetryBitsToSqueak, isCombatSqueak);
        if (trySqueak(packed)) {
            if (symmetryBitsToSqueak != 0) {
                if (isRatKingInRange) {
                    hasNewSymmetryForKing = false;
                } else {
                    hasNewSymmetryToRelay = false;
                }
            }
        } else {
            // prob never happens
            if (!isCombatSqueak) {
                // Only restore POI locations if this wasn't a combat squeak
                if (isRatKingInRange) {
                    if (loc1 != NULL_LOCATION_INT) {
                        discoveredPOILocsForKing.add(loc1);
                    }
                    if (loc2 != NULL_LOCATION_INT) {
                        discoveredPOILocsForKing.add(loc2);
                    }
                } else {
                    if (loc1 != NULL_LOCATION_INT) {
                        discoveredPOILocs.add(loc1);
                    }
                    if (loc2 != NULL_LOCATION_INT) {
                        discoveredPOILocs.add(loc2);
                    }
                }
            }
        }
    }

    public static void readSqueaks() throws GameActionException {
        allies2 = 0;
        allies5 = 0;
        checkedIds.clear();
        RobotInfo[] robots = Robot.rc.senseNearbyRobots(Robot.visionRadiusSquared, Robot.team);
        Message[] squeaks = Robot.rc.readSqueaks(Robot.rc.getRoundNum());
        for (int i = squeaks.length; --i >= 0;) {
            int content = squeaks[i].getBytes();
            if (processedSqueaks.contains(content))
                continue;

            processDualLocationSqueak(squeaks[i].getSource(), content);
            processedSqueaks.add(content);
        }
        countAllies(squeaks, robots);
        squeaks = Robot.rc.readSqueaks(Robot.rc.getRoundNum() - 1);
        for (int i = squeaks.length; --i >= 0;) {
            int content = squeaks[i].getBytes();
            if (processedSqueaks.contains(content))
                continue;

            processDualLocationSqueak(squeaks[i].getSource(), content);
            processedSqueaks.add(content);
        }
        if (Robot.rc.getType().isRatKingType())
            return;
        countAllies(squeaks, new RobotInfo[0]);
    }

    static void countAllies(Message[] squeaks, RobotInfo[] robots) throws GameActionException {
        MapLocation me = Robot.rc.getLocation();

        for (Message m : squeaks) {
            int id = m.getSenderID();
            if (checkedIds.contains(id))
                continue;
            int dist = me.distanceSquaredTo(m.getSource());

            if (dist <= GameConstants.ATTACK_DISTANCE_SQUARED) {
                ++allies2;
            } else if (dist <= RANGE_EXTENDED) {
                ++allies5;
            }
            checkedIds.add(id);
        }

        for (RobotInfo r : robots) {
            int dist = me.distanceSquaredTo(r.getLocation());

            if (dist <= GameConstants.ATTACK_DISTANCE_SQUARED) {
                --allies2;
            } else if (dist <= RANGE_EXTENDED) {
                --allies5;
            }
        }
    }

    public static void processDualLocationSqueak(MapLocation squeakLoc, int packedSqueak) throws GameActionException {
        int combatBit = packedSqueak & (1 << COMBAT_BIT_POSITION);
        boolean isRatKing = Robot.rc.getType().isRatKingType();

        // Rat kings ignore squeaks with combat bit set
        if (isRatKing && combatBit != 0) {
            return;
        }

        int symmetryBits = extractSymmetryBits(packedSqueak);
        int loc1 = packedSqueak & LOCATION_MASK;
        int loc2 = (packedSqueak >> SECOND_LOC_SHIFT) & LOCATION_MASK;

        if (combatBit != 0) {

            int dirOrd = (packedSqueak >> DIR_SHIFT) & 0x7;
            boolean carrying = ((packedSqueak >> CARRY_BIT_POSITION) & 1) == 1;

            if (loc1 != NULL_LOCATION_INT && loc1 < 0xFFC) {
                MapLocation enemyLoc = Util.intToLocation(loc1);
                if (enemyLoc != null) {
                    Robot.helpTarget = enemyLoc;
                    int combined = loc1 | (dirOrd << DIR_SHIFT);

                    if (carrying) {
                        int dist = Robot.rc.getLocation().distanceSquaredTo(enemyLoc);
                        if (dist <= GameConstants.ATTACK_DISTANCE_SQUARED) {
                            --allies2;
                        }
                        if (dist <= RANGE_EXTENDED) {
                            --allies5;
                        }
                    }

                    if (!enemies.contains(combined) &&
                            !Robot.rc.canSenseLocation(enemyLoc))
                        enemies.add(combined);
                }
            }
            // Don't process loc1/loc2 as POI locations for combat squeaks
            // Still process symmetry if present
            if (symmetryBits != 0) {
                processSymmetryInfo(symmetryBits);
            }
            return;
        }

        // Non-combat squeak: process as POI locations
        boolean hasLoc1 = loc1 != NULL_LOCATION_INT && loc1 < 0xFFC;
        boolean hasLoc2 = loc2 != NULL_LOCATION_INT && loc2 < 0xFFC;
        boolean hasSymmetry = symmetryBits != 0;

        if (!hasLoc1 && !hasLoc2 && !hasSymmetry) {
            return;
        }

        if (hasSymmetry) {
            processSymmetryInfo(symmetryBits);
        }

        if (hasLoc1) {
            processLocationInt(loc1);
        }
        if (hasLoc2) {
            processLocationInt(loc2);
        }
    }

    private static void processSymmetryInfo(int symmetryBits) {
        int previousSymmetry = getCurrentSymmetryBits();
        boolean isRatKing = Robot.rc.getType().isRatKingType();

        if ((symmetryBits & 0x1) != 0) {
            SymmetryManager.discardR();
        }
        if ((symmetryBits & 0x2) != 0) {
            SymmetryManager.discardV();
        }
        if ((symmetryBits & 0x4) != 0) {
            SymmetryManager.discardH();
        }

        int currentSymmetry = getCurrentSymmetryBits();
        if (currentSymmetry != previousSymmetry) {
            if (!isRatKing) {
                hasNewSymmetryToRelay = true;
                hasNewSymmetryForKing = true;
            }
        }
    }

    private static int getCurrentSymmetryBits() {
        int discarded = SymmetryManager.getDiscardedSyms();
        int result = 0;
        if ((discarded & SymmetryManager.R) != 0)
            result |= 0x1;
        if ((discarded & SymmetryManager.V) != 0)
            result |= 0x2;
        if ((discarded & SymmetryManager.H) != 0)
            result |= 0x4;
        return result;
    }

    private static void processLocationInt(int locationInt) throws GameActionException {
        boolean isRatKing = Robot.rc.getType().isRatKingType();

        if (locationInt >= 0xFFC) {
            if (Debug.FAIL_FAST && isRatKing)
                throw new IllegalStateException("RAT KING: Skipping invalid location int: " + locationInt);
            return;
        }

        int x = (locationInt >> 6) & 0x3F;
        int y = locationInt & 0x3F;
        if (x >= 60 || y >= 60) {
            if (Debug.FAIL_FAST && isRatKing)
                throw new IllegalStateException("RAT KING: Skipping invalid location int: " + locationInt);
            return;
        }

        MapLocation mapLoc = new MapLocation(x, y);

        if (!MapData.hasCheeseMine(mapLoc)) {
            MapData.addCheeseMineToCache(mapLoc);

            if (isRatKing) {
                Debug.addIndicatorString(" Mine from squeak: " + mapLoc);
            }

            if (SymmetryManager.isSymmetryDetermined() && CommsArray.isSymmetryGloballyKnown()) {
                MapLocation symLoc = SymmetryManager.getSymmetric(mapLoc);
                if (symLoc != null && !MapData.hasCheeseMine(symLoc)) {
                    MapData.addCheeseMineToCache(symLoc);
                    if (isRatKing) {
                        Debug.addIndicatorString("Symm mine from squeak: " + symLoc);
                    }
                }

                if (CommsArray.isCheeseMinesRewritten()) {
                    CommsArray.addToSidedCheeseMines(mapLoc);
                    if (symLoc != null) {
                        CommsArray.addToSidedCheeseMines(symLoc);
                    }
                }
            }
        }

        if (isRatKing) {
            if (SymmetryManager.isSymmetryDetermined() && CommsArray.isSymmetryGloballyKnown()
                    && !CommsArray.isCheeseMinesRewritten()) {
                MapLocation symLoc = SymmetryManager.getSymmetric(mapLoc);
                if (symLoc != null) {
                    if (!CommsArray.isLocationInSharedArray(mapLoc)) {
                        CommsArray.writeCheeseMineToSharedArray(mapLoc);
                    }
                    if (!CommsArray.isLocationInSharedArray(symLoc)) {
                        CommsArray.writeCheeseMineToSharedArray(symLoc);
                    }
                } else {
                    CommsArray.writeCheeseMineToSharedArray(mapLoc);
                }
            } else {
                CommsArray.writeCheeseMineToSharedArray(mapLoc);
            }
        }
    }

    public static void addMineToPendingBuffer(MapLocation mineLoc) throws GameActionException {
        boolean isRatKing = Robot.rc.getType().isRatKingType();
        if (isRatKing || mineLoc == null) {
            return;
        }

        if (CommsArray.isLocationInSharedArray(mineLoc)) {
            return;
        }

        int intLoc = Util.locationToInt(mineLoc);
        discoveredPOILocsForKing.add(intLoc);
        discoveredPOILocs.add(intLoc);
    }

    public static void purgeKingBuffersAtStartOfTurn() throws GameActionException {
        // Purge POI locations that are already in shared array
        if (discoveredPOILocsForKing.size() > 0) {
            StringBuilder keys = discoveredPOILocsForKing.keys;
            int[] toRemove = new int[keys.length()];
            int removeCount = 0;

            // First pass: identify what to remove
            for (int i = 0; i < keys.length(); i++) {
                int locationInt = keys.charAt(i);
                MapLocation loc = Util.intToLocation(locationInt);
                if (loc != null && CommsArray.isLocationInSharedArray(loc)) {
                    toRemove[removeCount++] = locationInt;
                }
            }

            // Second pass: remove them
            for (int i = 0; i < removeCount; i++) {
                discoveredPOILocsForKing.remove(toRemove[i]);
            }
        }

        // Purge symmetry flag if the info is already in shared array
        if (hasNewSymmetryForKing) {
            if (CommsArray.isSymmetryGloballyKnown()) {
                Debug.addIndicatorString("PURGED KING SYMM");
                hasNewSymmetryForKing = false;
            } else {
                int currentSymmetryBits = getCurrentSymmetryBits();
                if (currentSymmetryBits != 0) {
                    int sharedSymmetryBits = CommsArray.readSymmetryBitsFromSharedArray();
                    // If our symmetry bits are a subset of what's in shared array, clear the flag
                    // (sharedSymmetryBits contains at least what we wanted to share)
                    if ((sharedSymmetryBits & currentSymmetryBits) == currentSymmetryBits) {
                        hasNewSymmetryForKing = false;
                        Debug.addIndicatorString("PURGED KING SYMM");
                    }
                }
            }
        }
    }
}
