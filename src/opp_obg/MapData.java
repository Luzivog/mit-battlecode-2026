package opp_obg;

import battlecode.common.MapLocation;

public class MapData {

    static int[] commMap;
    static long[] wallMap = new long[60];
    static int commMapMax;
    static long[] cheeseMinesLocs = new long[60];
    static long[] cheeseMinesLocsKnown = new long[60];
    static long[] exploredLocs = new long[60];
    static int totalCheese = 0;
    static MapLocation[] cheeseMineList = new MapLocation[150];

    static void addCheese(MapLocation loc){
        if ((cheeseMinesLocs[loc.x] & (1L << loc.y)) == 0){
            cheeseMinesLocs[loc.x] |= (1L << loc.y);
            cheeseMineList[totalCheese++] = loc;
        }
    }

    static void addCheeseArrayKnown(MapLocation loc){
        addCheese(loc);
        cheeseMinesLocsKnown[loc.x] |= (1L << loc.y);
    }

    static void addExploredLoc(MapLocation loc){
        exploredLocs[loc.x] |= (1L << loc.y);
    }

    static boolean isWall(MapLocation loc){
        int code = loc.x*MyUnit.dim2 + loc.y;
        return (commMap[code/10] & (1 << (code%10))) != 0;
    }

    static void addWall(MapLocation loc){
        int code = loc.x* MyUnit.dim2 + loc.y;
        commMap[code/10] |= (1 << (code%10));
        wallMap[loc.x] |= (1L << loc.y);
        if (MyUnit.isKing) BFS.addWallKing((loc.x+1)*62 + loc.y+1);
        //else BFS.addWall((loc.x+1)*62 + loc.y+1);
    }

    static int index = 0;

    static MapLocation getUnknownMine(){
        if (totalCheese == 0) return null;
        MapLocation loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        loc = cheeseMineList[index];
        if ((cheeseMinesLocsKnown[loc.x] & (1L << loc.y)) == 0) return loc;
        index = (index + 1)%totalCheese;
        return null;
    }



}
