package opp_parmesan.Utils;

import battlecode.common.GameActionException;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;
import battlecode.common.Message;
import opp_parmesan.Utils.Comms;
import opp_parmesan.Utils.Globals;
import opp_parmesan.Utils.Vision;

public class Symmetry extends Globals {

    public final static int vertical = 1;
    public final static int horizontal = 2;
    public final static int rotational = 4;

    public static int symm = 0; // 0: possible, 1: impossible

    public static boolean isSymmetryPossible(int s) throws GameActionException {
        return (symm & s) != 0;
    }

    public static MapLocation symmetricallyOpposite(MapLocation loc, int s) {
        if (s == vertical) return new MapLocation(mapWidth - 1 - loc.x, loc.y);
        if (s == horizontal) return new MapLocation(loc.x, mapHeight - 1 - loc.y);
        if (s == rotational) return new MapLocation(mapWidth - 1 - loc.x, mapHeight - 1 - loc.y);
        return null;
    }

    public static int[] getPossibleSymmetries() {
        if (symm == 0) return new int[] {1, 2, 4};
        if (symm == 1) return new int[] {2, 4};
        if (symm == 2) return new int[] {1, 4};
        if (symm == 4) return new int[] {2, 4};
        if (symm == 3) return new int[] {4};
        if (symm == 5) return new int[] {2};
        if (symm == 6) return new int[] {1};
        rc.setIndicatorString("what the hecky? no symmetries!");
        System.out.println("what the hecky? no symmetries!");
        return null;
    }

    public static void updateNearbySymmetry() throws GameActionException {
        int[] possibleSymmetries = getPossibleSymmetries();

        // special case king to not bytecode exceed cuz its vision is bigger
        if (rc.getType().isRatKingType()) {
            for (MapLocation loc : rc.getAllLocationsWithinRadiusSquared(rc.getLocation(), rc.getType().getVisionRadiusSquared())) {
                MapInfo map = rc.senseMapInfo(loc);

                for (int s : possibleSymmetries) {
                    MapLocation opp = symmetricallyOpposite(loc, s);

                    if (opp_parmesan.Utils.Vision.hasSeenLocation(opp)) {
                        if (map.hasCheeseMine() != opp_parmesan.Utils.Vision.isMine(opp)) symm |= s;
                        if (map.isWall() != opp_parmesan.Utils.Vision.isWall(opp)) symm |= s;
                    }
                }
            }
            return;
        }

        // baby rat
        for (MapInfo map : surroundings) {
            MapLocation loc = map.getMapLocation();

            boolean mine = map.hasCheeseMine();
            boolean wall = map.isWall();

            if (!mine && !wall) continue; // save bytecode by only checking if the current cell is special

            for (int s : possibleSymmetries) {
                MapLocation opp = symmetricallyOpposite(loc, s);

                if (opp_parmesan.Utils.Vision.hasSeenLocation(opp)) {
                    if (mine != opp_parmesan.Utils.Vision.isMine(opp)) symm |= s;
                    if (wall != Vision.isWall(opp)) symm |= s;
                }
            }
        }
    }

    public static void updateSymmetryFromGlobal() throws GameActionException {
        int globalSymm = opp_parmesan.Utils.Comms.getSymmetry();
        symm |= globalSymm;
    }

    public static void commSymmetryToNearby() throws GameActionException {
        int globalSymm = Comms.getSymmetry();

        // only if is within hearing distance of king
        if (rc.getLocation().isWithinDistanceSquared(kingPos[0], 16)) {
            if (symm != globalSymm) {
                rc.squeak(symm);
                hasSqueaked = true;
                System.out.println("squeaked new symmetry to king: " + symm);
            }
        }
        // squeak every round because we're doing it anyways
        else if (!hasSqueaked && Symmetry.symm != globalSymm) {
            rc.squeak(Symmetry.symm);
            hasSqueaked = true;
        }
    }

    public static void hearNearbySymmetrySqueaks() throws GameActionException {
        for (Message message : newestSqueaks) {
            if (0 <= message.getBytes() && message.getBytes() < 8) {
                symm |= message.getBytes();
            }
        }
    }
}
