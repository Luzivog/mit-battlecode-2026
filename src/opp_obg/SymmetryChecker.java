package opp_obg;

import battlecode.common.Clock;
import battlecode.common.MapInfo;
import battlecode.common.MapLocation;

public class SymmetryChecker {

    static final int H = 1, V = 2, R = 4;
    static final int HC = (~H)&7, VC = (~V)&7, RC = (~R)&7;

    static boolean foundH = false, foundR = false, foundV = false;

    static int discardedSyms = 0;

    static MapLocation L;
    static int symX, symY;

    static int code;

    static void discardH(){
        discardedSyms |= H;
    }

    static void discardV(){
        discardedSyms |= V;
    }

    static void discardR(){
        discardedSyms |= R;
    }

    static int getSym(){
        return switch(discardedSyms){
            case HC -> H;
            case VC -> V;
            case RC -> R;
            default -> MyUnit.rc.getRoundNum() > 340 ? R : 0;
        };
    }

    static void run(){
        if (Clock.getBytecodesLeft() < 400) return;
        if (getSym() != 0) return;

        MapInfo[] infos = MyUnit.rc.senseNearbyMapInfos();
        for (MapInfo m : infos){
            if (Clock.getBytecodesLeft() < 400) return;
            if (m.isWall()) continue;
            L = m.getMapLocation();
            symX = MyUnit.dim1 - L.x - 1;
            symY = MyUnit.dim2 - L.y - 1;
            code = symX* MyUnit.dim2 + L.y;
            if ((MapData.commMap[code/10] & (1 << (code%10))) != 0) discardH();
            code = symX* MyUnit.dim2 + symY;
            if ((MapData.commMap[code/10] & (1 << (code%10))) != 0) discardR();
            code = L.x* MyUnit.dim2 + symY;
            if ((MapData.commMap[code/10] & (1 << (code%10))) != 0) discardV();
        }
    }

    static int mineIndex = 0;

    static void runMines(){
        if (Clock.getBytecodesLeft() < 400 || getSym() == 0) return;
        for (int i = 0; i < 10 && mineIndex < MapData.totalCheese; ++i){
            if (Clock.getBytecodesLeft() < 400) return;
            MapLocation newCheese = getSymmetric(MapData.cheeseMineList[mineIndex++]);
            if (newCheese == null) return;
            MapData.addCheese(newCheese);
        }
    }

    static MapLocation getSymmetric(MapLocation loc){
        return switch(getSym()){
            case H-> new MapLocation(MyUnit.dim1 - loc.x - 1, loc.y);
            case V-> new MapLocation(loc.x, MyUnit.dim2 - loc.y - 1);
            case R-> new MapLocation(MyUnit.dim1 - loc.x - 1, MyUnit.dim2 - loc.y - 1);
            default -> null;
        };
    }
}
