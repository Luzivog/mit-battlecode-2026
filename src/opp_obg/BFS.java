package opp_obg;

import battlecode.common.*;

public class BFS {

    static int[] map;
    static int[] queue;
    static int frontInd = 0;
    static int endInd = 0;
    static RobotController uc;
    static int bfsCount = 0;
    static int currentTarget = -1;
    static int firstCountCurrentTarget = 0;

    static final int WALL = (1 << 30);

    static Direction[] dirs = new Direction[]{
            Direction.EAST,
            Direction.WEST,
            Direction.NORTH,
            Direction.SOUTH,
            Direction.NORTHEAST,
            Direction.NORTHWEST,
            Direction.SOUTHWEST,
            Direction.SOUTHEAST
    };

    static void initKing(){
        map = new int[62*62];
        uc = MyUnit.rc;
        map[(MyUnit.dim1)*62 + 0] = WALL;
        map[62 + 0] = WALL;
        map[62*0+ 1] = WALL;
        map[62*0 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 1] = WALL;
        map[62 + 1] = WALL;
        map[62*1+ 1] = WALL;
        map[62*1 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 2] = WALL;
        map[62 + 2] = WALL;
        map[62*2+ 1] = WALL;
        map[62*2 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 3] = WALL;
        map[62 + 3] = WALL;
        map[62*3+ 1] = WALL;
        map[62*3 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 4] = WALL;
        map[62 + 4] = WALL;
        map[62*4+ 1] = WALL;
        map[62*4 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 5] = WALL;
        map[62 + 5] = WALL;
        map[62*5+ 1] = WALL;
        map[62*5 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 6] = WALL;
        map[62 + 6] = WALL;
        map[62*6+ 1] = WALL;
        map[62*6 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 7] = WALL;
        map[62 + 7] = WALL;
        map[62*7+ 1] = WALL;
        map[62*7 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 8] = WALL;
        map[62 + 8] = WALL;
        map[62*8+ 1] = WALL;
        map[62*8 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 9] = WALL;
        map[62 + 9] = WALL;
        map[62*9+ 1] = WALL;
        map[62*9 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 10] = WALL;
        map[62 + 10] = WALL;
        map[62*10+ 1] = WALL;
        map[62*10 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 11] = WALL;
        map[62 + 11] = WALL;
        map[62*11+ 1] = WALL;
        map[62*11 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 12] = WALL;
        map[62 + 12] = WALL;
        map[62*12+ 1] = WALL;
        map[62*12 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 13] = WALL;
        map[62 + 13] = WALL;
        map[62*13+ 1] = WALL;
        map[62*13 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 14] = WALL;
        map[62 + 14] = WALL;
        map[62*14+ 1] = WALL;
        map[62*14 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 15] = WALL;
        map[62 + 15] = WALL;
        map[62*15+ 1] = WALL;
        map[62*15 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 16] = WALL;
        map[62 + 16] = WALL;
        map[62*16+ 1] = WALL;
        map[62*16 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 17] = WALL;
        map[62 + 17] = WALL;
        map[62*17+ 1] = WALL;
        map[62*17 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 18] = WALL;
        map[62 + 18] = WALL;
        map[62*18+ 1] = WALL;
        map[62*18 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 19] = WALL;
        map[62 + 19] = WALL;
        map[62*19+ 1] = WALL;
        map[62*19 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 20] = WALL;
        map[62 + 20] = WALL;
        map[62*20+ 1] = WALL;
        map[62*20 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 21] = WALL;
        map[62 + 21] = WALL;
        map[62*21+ 1] = WALL;
        map[62*21 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 22] = WALL;
        map[62 + 22] = WALL;
        map[62*22+ 1] = WALL;
        map[62*22 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 23] = WALL;
        map[62 + 23] = WALL;
        map[62*23+ 1] = WALL;
        map[62*23 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 24] = WALL;
        map[62 + 24] = WALL;
        map[62*24+ 1] = WALL;
        map[62*24 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 25] = WALL;
        map[62 + 25] = WALL;
        map[62*25+ 1] = WALL;
        map[62*25 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 26] = WALL;
        map[62 + 26] = WALL;
        map[62*26+ 1] = WALL;
        map[62*26 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 27] = WALL;
        map[62 + 27] = WALL;
        map[62*27+ 1] = WALL;
        map[62*27 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 28] = WALL;
        map[62 + 28] = WALL;
        map[62*28+ 1] = WALL;
        map[62*28 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 29] = WALL;
        map[62 + 29] = WALL;
        map[62*29+ 1] = WALL;
        map[62*29 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 30] = WALL;
        map[62 + 30] = WALL;
        map[62*30+ 1] = WALL;
        map[62*30 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 31] = WALL;
        map[62 + 31] = WALL;
        map[62*31+ 1] = WALL;
        map[62*31 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 32] = WALL;
        map[62 + 32] = WALL;
        map[62*32+ 1] = WALL;
        map[62*32 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 33] = WALL;
        map[62 + 33] = WALL;
        map[62*33+ 1] = WALL;
        map[62*33 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 34] = WALL;
        map[62 + 34] = WALL;
        map[62*34+ 1] = WALL;
        map[62*34 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 35] = WALL;
        map[62 + 35] = WALL;
        map[62*35+ 1] = WALL;
        map[62*35 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 36] = WALL;
        map[62 + 36] = WALL;
        map[62*36+ 1] = WALL;
        map[62*36 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 37] = WALL;
        map[62 + 37] = WALL;
        map[62*37+ 1] = WALL;
        map[62*37 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 38] = WALL;
        map[62 + 38] = WALL;
        map[62*38+ 1] = WALL;
        map[62*38 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 39] = WALL;
        map[62 + 39] = WALL;
        map[62*39+ 1] = WALL;
        map[62*39 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 40] = WALL;
        map[62 + 40] = WALL;
        map[62*40+ 1] = WALL;
        map[62*40 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 41] = WALL;
        map[62 + 41] = WALL;
        map[62*41+ 1] = WALL;
        map[62*41 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 42] = WALL;
        map[62 + 42] = WALL;
        map[62*42+ 1] = WALL;
        map[62*42 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 43] = WALL;
        map[62 + 43] = WALL;
        map[62*43+ 1] = WALL;
        map[62*43 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 44] = WALL;
        map[62 + 44] = WALL;
        map[62*44+ 1] = WALL;
        map[62*44 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 45] = WALL;
        map[62 + 45] = WALL;
        map[62*45+ 1] = WALL;
        map[62*45 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 46] = WALL;
        map[62 + 46] = WALL;
        map[62*46+ 1] = WALL;
        map[62*46 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 47] = WALL;
        map[62 + 47] = WALL;
        map[62*47+ 1] = WALL;
        map[62*47 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 48] = WALL;
        map[62 + 48] = WALL;
        map[62*48+ 1] = WALL;
        map[62*48 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 49] = WALL;
        map[62 + 49] = WALL;
        map[62*49+ 1] = WALL;
        map[62*49 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 50] = WALL;
        map[62 + 50] = WALL;
        map[62*50+ 1] = WALL;
        map[62*50 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 51] = WALL;
        map[62 + 51] = WALL;
        map[62*51+ 1] = WALL;
        map[62*51 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 52] = WALL;
        map[62 + 52] = WALL;
        map[62*52+ 1] = WALL;
        map[62*52 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 53] = WALL;
        map[62 + 53] = WALL;
        map[62*53+ 1] = WALL;
        map[62*53 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 54] = WALL;
        map[62 + 54] = WALL;
        map[62*54+ 1] = WALL;
        map[62*54 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 55] = WALL;
        map[62 + 55] = WALL;
        map[62*55+ 1] = WALL;
        map[62*55 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 56] = WALL;
        map[62 + 56] = WALL;
        map[62*56+ 1] = WALL;
        map[62*56 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 57] = WALL;
        map[62 + 57] = WALL;
        map[62*57+ 1] = WALL;
        map[62*57 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 58] = WALL;
        map[62 + 58] = WALL;
        map[62*58+ 1] = WALL;
        map[62*58 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 59] = WALL;
        map[62 + 59] = WALL;
        map[62*59+ 1] = WALL;
        map[62*59 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 60] = WALL;
        map[62 + 60] = WALL;
        map[62*60+ 1] = WALL;
        map[62*60 + (MyUnit.dim2)] = WALL;
        map[(MyUnit.dim1)*62 + 61] = WALL;
        map[62 + 61] = WALL;
        map[62*61+ 1] = WALL;
        map[62*61 + (MyUnit.dim2)] = WALL;
    }

    static void addWallKing(int code){
        map[code] |= WALL;
        map[code+1] |= WALL;
        map[code-1] |= WALL;
        map[code+62] |= WALL;
        map[code-62] |= WALL;
        map[code+61] |= WALL;
        map[code-61] |= WALL;
        map[code+63] |= WALL;
        map[code-63] |= WALL;
    }

    static void reset(int targetEncoded){
        if (queue == null) return;
        bfsCount++;
        frontInd = 0;
        endInd = 1;
        queue[0] = targetEncoded;
        map[targetEncoded] &= WALL;
        map[targetEncoded] |= ((bfsCount << 12) | 0xFFF);
        if (currentTarget != targetEncoded){
            firstCountCurrentTarget = bfsCount;
            currentTarget = targetEncoded;
        }
    }

    static void run() throws GameActionException {
        if (queue == null) return;
        //int iter = 0;
        //int bytecode1 = Clock.getBytecodesLeft();
        int distCode;
        int tileCode;
        //uc.setIndicatorString("Entering BFS " + frontInd + " " + endInd);

        while(frontInd < endInd && Clock.getBytecodesLeft() > 300){
            //++iter;
            tileCode = queue[frontInd++];
            uc.setIndicatorDot(decodeLocation(tileCode), 255, 0, 0);
            distCode = map[tileCode] - 1;

            if (distCode >= WALL) continue;

            if (distCode > map[tileCode+1]){
                map[tileCode+1] = distCode;
                queue[endInd++] = tileCode+1;
            }
            if (distCode > map[tileCode-1]){
                map[tileCode-1] = distCode;
                queue[endInd++] = tileCode-1;
            }
            if (distCode > map[tileCode+62]){
                map[tileCode+62] = distCode;
                queue[endInd++] = tileCode+62;
            }
            if (distCode > map[tileCode-62]){
                map[tileCode-62] = distCode;
                queue[endInd++] = tileCode-62;
            }
            if (distCode > map[tileCode+63]){
                map[tileCode+63] = distCode;
                queue[endInd++] = tileCode+63;
            }
            if (distCode > map[tileCode+61]){
                map[tileCode+61] = distCode;
                queue[endInd++] = tileCode+61;
            }
            if (distCode > map[tileCode-61]){
                map[tileCode-61] = distCode;
                queue[endInd++] = tileCode-61;
            }
            if (distCode > map[tileCode-63]){
                map[tileCode-63] = distCode;
                queue[endInd++] = tileCode-63;
            }
        }
        /*if (iter != 0){
            uc.setIndicatorString("Did " + iter + " iterations. Average bytecode: " + (bytecode1 - Clock.getBytecodesLeft())/iter);
        }*/
    }

    static int encodeLocation(MapLocation loc){
        return (loc.x+1)*62 + loc.y + 1;
    }

    static MapLocation decodeLocation(int code){
        return new MapLocation(code/62 - 1, (code%62) - 1);
    }

    static void initializeQueue(){
        queue = new int [3605];
    }

    static int encLoc, locCode, locCount;
    static Direction bestDir;
    static boolean withoutDirt;
    static int bestCode, bestLoc, maxCount;

    static int minCountValid, myLocCode, myLocCount;


    static Direction getBFSDirection(){
        if (queue == null) return null;
        minCountValid = Math.max(firstCountCurrentTarget, bfsCount-1);
        myLocCode = map[(MovementManager.myLoc.x+1)*62 + MovementManager.myLoc.y+1];
        myLocCount = (myLocCode >>> 12)&0xFFFF;

        withoutDirt = false;
        bestCode = 0;
        bestLoc = 0;
        maxCount = -1;
        bestDir = null;

        for (Direction dir : dirs){
            if (!MovementManager.canMove(dir, true)) continue;
            boolean canMoveNoDirt = !DirtManager.canPassThroughDirt && MovementManager.canMove(dir,false);
            MapLocation loc = MovementManager.myLoc.add(dir);
            encLoc = encodeLocation(loc);
            locCode = (map[encLoc] & 0x1FFFFFFF);
            locCount = (locCode >>> 12)&0xFFFF;
            if (locCount < minCountValid) continue;
            if (withoutDirt && !canMoveNoDirt && bestCode > myLocCode) continue;
            if (maxCount < locCount) maxCount = locCount;
            if ((canMoveNoDirt && !withoutDirt && locCode > myLocCode) || (locCode > bestCode)){
                bestCode = locCode;
                bestDir = dir;
                bestLoc = encLoc;
                withoutDirt = canMoveNoDirt;
            }
        }

        boolean reset = false;
        if (myLocCount >= bfsCount) reset = true;

        if (maxCount >= minCountValid){
            if (((map[bestLoc] & (1 << 30)) != 0)){
                reset = true;
                bestDir = null;
            }

            if (bestCode <= myLocCode){
                if (reset) reset(currentTarget);
                return Direction.CENTER; //fail
            }

        }

        return bestDir;
    }
}
