package opp_obg;

import battlecode.common.GameActionException;
import battlecode.common.MapLocation;

/**
 * Stuff related to communication (squeaks + shared array).
 */
public class CommunicationManager {

    static final int MAP_CHANNELS = 23;
    static final int KING_INFO_CHANNEL = 23;
    static final int KING_INDEX = 48;
    static final int TARGET_INDEX = 49;
    static final int MINE_CHANNEL = 52;
    static final int MINE_CHANNELS = 3;
    static final int BABY_RAT_CHANNEL = 58;
    static final int SYM_CHANNEL = 59;
    static final int COST_CHANNEL = 60;
    static final int TARGET_MINE_CHANNEL = 61;

    static MapLocation kingLoc;

    static int knownSym = 0;

    static void update() throws GameActionException {
        if (MyUnit.isKing) return;
        readMap();
        readCheese();
        readKingLocation();
        readSym();
    }

    static int read, stored;

    static int mineReportIndex = 0;

    static int getKings() throws GameActionException {
        return MyUnit.rc.readSharedArray(KING_INDEX);
    }

    static MapLocation getSelfTargetKing() throws GameActionException {
        return getTarget(RatKing.internalID);
    }

    static void setMyTarget(MapLocation loc) throws GameActionException {
        MyUnit.rc.writeSharedArray(KING_INFO_CHANNEL + 5*RatKing.internalID+2, loc.x+1);
        MyUnit.rc.writeSharedArray(KING_INFO_CHANNEL + 5*RatKing.internalID+3, loc.y);
    }

    static void setKingTarget(MapLocation loc) throws GameActionException {
        MyUnit.rc.writeSharedArray(TARGET_INDEX, loc.x+1);
        MyUnit.rc.writeSharedArray(TARGET_INDEX+1, loc.y);
    }

    static MapLocation getTarget(int kingIndex) throws GameActionException {
        int x = MyUnit.rc.readSharedArray(kingIndex*5+2 + KING_INFO_CHANNEL);
        if (x == 0) return null;
        return new MapLocation(x-1, MyUnit.rc.readSharedArray(kingIndex*5+3 + KING_INFO_CHANNEL));
    }

    static void removeKingTarget() throws GameActionException {
        MyUnit.rc.writeSharedArray(TARGET_INDEX, 0);
        MyUnit.rc.writeSharedArray(TARGET_INDEX+1, 0);
    }

    static MapLocation getKingLoc(int kingIndex) throws GameActionException {
        int y = MyUnit.rc.readSharedArray(KING_INFO_CHANNEL + 5*kingIndex + 1);
        int r = 1000*(y>>>8);
        y = (y&0xFF);
        r = r + MyUnit.rc.readSharedArray(KING_INFO_CHANNEL + 5*kingIndex + 4);
        if (r + 3 < MyUnit.rc.getRoundNum()) return null;
        return new MapLocation(MyUnit.rc.readSharedArray(KING_INFO_CHANNEL + 5*kingIndex), y);
    }

    static MapLocation getKingTarget() throws GameActionException {
        int x = MyUnit.rc.readSharedArray(TARGET_INDEX);
        if (x == 0) return null;
        return new MapLocation(x-1, MyUnit.rc.readSharedArray(TARGET_INDEX+1));
    }

    static int updateIndex() throws GameActionException {
        int ans = MyUnit.rc.readSharedArray(KING_INDEX);
        MyUnit.rc.writeSharedArray(KING_INDEX, ans+1);
        return ans;
    }

    static MapLocation getAssignedMine() throws GameActionException {
        int x = MyUnit.rc.readSharedArray(TARGET_MINE_CHANNEL);
        if (x == 0) return null;
        return new MapLocation(x-1, MyUnit.rc.readSharedArray(TARGET_MINE_CHANNEL+1)-1);
    }

    static void reportTargetMine() throws GameActionException {
        if (MapData.totalCheese == 0) return;
        MyUnit.rc.writeSharedArray(TARGET_MINE_CHANNEL, MapData.cheeseMineList[mineReportIndex].x +1);
        MyUnit.rc.writeSharedArray(TARGET_MINE_CHANNEL+1, MapData.cheeseMineList[mineReportIndex].y +1);
        mineReportIndex = (mineReportIndex+1)%MapData.totalCheese;
    }

    static void reportCost() throws GameActionException{
        MyUnit.rc.writeSharedArray(COST_CHANNEL, MyUnit.rc.getCurrentRatCost());
    }

    static int getCost() throws GameActionException {
        return MyUnit.rc.readSharedArray(COST_CHANNEL);
    }

    static void readMap() throws GameActionException {
        int start = MyUnit.rc.getRoundNum()*MAP_CHANNELS % MapData.commMapMax;
        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(0);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;
        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(1);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;
        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(2);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(3);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(4);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(5);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(6);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(7);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(8);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(9);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(10);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(11);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(12);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(13);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(14);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(15);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(16);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(17);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(18);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(19);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(20);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(21);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
        start = (start + 1)%MapData.commMapMax;

        stored = MapData.commMap[start];
        read = MyUnit.rc.readSharedArray(22);
        if ((stored | read) != stored){
            MapData.commMap[start] |= read;
            
        }
    }

    static void readCheese() throws GameActionException {
        int code = MyUnit.rc.readSharedArray(MINE_CHANNEL);
        if (code == 0) return;
        MapData.addCheeseArrayKnown(new MapLocation(code - 1, MyUnit.rc.readSharedArray(MINE_CHANNEL + 1)));
        MapData.addCheeseArrayKnown(new MapLocation(MyUnit.rc.readSharedArray(MINE_CHANNEL + 2) - 1, MyUnit.rc.readSharedArray(MINE_CHANNEL + 3)));
        MapData.addCheeseArrayKnown(new MapLocation(MyUnit.rc.readSharedArray(MINE_CHANNEL + 4) - 1, MyUnit.rc.readSharedArray(MINE_CHANNEL + 5)));
        //MapData.addCheeseArrayKnown(new MapLocation(MyUnit.rc.readSharedArray(MINE_CHANNEL + 6) - 1, MyUnit.rc.readSharedArray(MINE_CHANNEL + 7)));
    }

    static void readKingLocation() throws GameActionException {
        kingLoc = null;
        int kingDist = 0;
        int kings = MyUnit.rc.readSharedArray(KING_INDEX);
        for (int i = 0; i < kings; ++i) {
            int y = MyUnit.rc.readSharedArray(KING_INFO_CHANNEL + 5*i + 1);
            int r = 1000*(y>>>8);
            y = (y&0xFF);
            r = r + MyUnit.rc.readSharedArray(KING_INFO_CHANNEL + 5*i + 4);
            if (r + 3 < MyUnit.rc.getRoundNum()) continue;
            MapLocation newLoc = new MapLocation(MyUnit.rc.readSharedArray(KING_INFO_CHANNEL + 5*i), y);
            int newDist = newLoc.distanceSquaredTo(MovementManager.myLoc);
            if (kingLoc == null || kingDist > newDist){
                kingLoc = newLoc;
                kingDist = newDist;
            }
        }
    }


    static void reportMap() throws GameActionException {
        int start = MyUnit.rc.getRoundNum()*MAP_CHANNELS;
        MyUnit.rc.writeSharedArray(0, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(1, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(2, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(3, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(4, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(5, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(6, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(7, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(8, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(9, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(10, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(11, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(12, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(13, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(14, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(15, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(16, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(17, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(18, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(19, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(20, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(21, MapData.commMap[start++ % MapData.commMapMax]);
        MyUnit.rc.writeSharedArray(22, MapData.commMap[start++ % MapData.commMapMax]);
    }

    static void reportLocation() throws GameActionException {
        MyUnit.rc.writeSharedArray(KING_INFO_CHANNEL + 5*RatKing.internalID, MovementManager.myLoc.x);
        int f = MyUnit.rc.getRoundNum()/1000;
        MyUnit.rc.writeSharedArray(KING_INFO_CHANNEL + 5*RatKing.internalID + 1, (f << 8) | MovementManager.myLoc.y);
        MyUnit.rc.writeSharedArray(KING_INFO_CHANNEL + 5*RatKing.internalID + 4, MyUnit.rc.getRoundNum()%1000);
    }

    static void reportBabyRats() throws GameActionException {
        MyUnit.rc.writeSharedArray(BABY_RAT_CHANNEL, RatKing.babyRats);
    }

    static void reportSym() throws GameActionException {
        MyUnit.rc.writeSharedArray(SYM_CHANNEL, SymmetryChecker.discardedSyms);
    }

    static void readSym() throws GameActionException {
        knownSym = MyUnit.rc.readSharedArray(SYM_CHANNEL);
        SymmetryChecker.discardedSyms |= knownSym;
    }

    static int getBabyNumber() throws GameActionException {
        return MyUnit.rc.readSharedArray(BABY_RAT_CHANNEL);
    }

    static int mineIndex = 0;

    static void reportKnownMines() throws GameActionException {
        if (MapData.totalCheese == 0) return;
        MyUnit.rc.writeSharedArray(MINE_CHANNEL, MapData.cheeseMineList[mineIndex].x + 1);
        MyUnit.rc.writeSharedArray(MINE_CHANNEL+1, MapData.cheeseMineList[mineIndex].y);
        //MyUnit.rc.setIndicatorDot(MapData.cheeseMineList[mineIndex], 255, 255, 0);
        mineIndex = (mineIndex + 1)% MapData.totalCheese;
        MyUnit.rc.writeSharedArray(MINE_CHANNEL+2, MapData.cheeseMineList[mineIndex].x + 1);
        MyUnit.rc.writeSharedArray(MINE_CHANNEL+3, MapData.cheeseMineList[mineIndex].y);
        //MyUnit.rc.setIndicatorDot(MapData.cheeseMineList[mineIndex], 255, 255, 0);
        mineIndex = (mineIndex + 1)% MapData.totalCheese;
        MyUnit.rc.writeSharedArray(MINE_CHANNEL+4, MapData.cheeseMineList[mineIndex].x + 1);
        MyUnit.rc.writeSharedArray(MINE_CHANNEL+5, MapData.cheeseMineList[mineIndex].y);
        //MyUnit.rc.setIndicatorDot(MapData.cheeseMineList[mineIndex], 255, 255, 0);
        mineIndex = (mineIndex + 1)% MapData.totalCheese;
        /*MyUnit.rc.writeSharedArray(MINE_CHANNEL+6, MapData.cheeseMineList[mineIndex].x + 1);
        MyUnit.rc.writeSharedArray(MINE_CHANNEL+7, MapData.cheeseMineList[mineIndex].y);
        MyUnit.rc.setIndicatorDot(MapData.cheeseMineList[mineIndex], 255, 255, 0);
        mineIndex = (mineIndex + 1)% MapData.totalCheese;*/
    }

    static MapLocation getKingLocation() throws GameActionException {
        return kingLoc;
    }

}
