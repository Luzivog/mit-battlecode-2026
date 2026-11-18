package opp_obg;

import battlecode.common.*;

public class SqueakManager {

    static final int OTHER = 0;
    static final int MAP = 1;
    static final int ENEMY = 2;
    static final int CAT = 3;
    static final int OTHER_CHEESE_MINE = 0;
    static final int OTHER_SYM = 1;
    static final int OTHER_FLYING = 2;

    static final int ROUND_COMM = 5;

    static final int MAX_SQUEAKS = 15;

    static FastIntSet idRead = new FastIntSet();

    static void squeakMap(){
        int index = (3* (MyUnit.rc.getRoundNum() + MyUnit.rc.getID()))% MapData.commMapMax;
        int index2 = (index+1)% MapData.commMapMax;
        int index3 = (index2+1)% MapData.commMapMax;
        int code = (MAP << 30) | (MapData.commMap[index] << 20) | (MapData.commMap[index2] << 10) | MapData.commMap[index3];
        MyUnit.rc.squeak(code);
    }

    static void squeakEnemy(int code, RobotInfo r){
        code = (code << 30);
        code |= ((r.getHealth() & 0xFF) << 22) | (r.getDirection().ordinal() << 19) | (Math.max(0,r.getID() - 10000) << 7) | encodeLocation(r.getLocation());
        MyUnit.rc.squeak(code);
    }

    static void squeakCheese(MapLocation loc){
        int code = (OTHER << 30) | (OTHER_CHEESE_MINE << 28) | (loc.x << 6) | loc.y;
        MyUnit.rc.squeak(code);
    }

    static void readSqueaks(int round) throws GameActionException {
        boolean currentRound = MyUnit.rc.getRoundNum() == round;
        if (currentRound){
            idRead.clear();
        }
        Message[] msgs = MyUnit.rc.readSqueaks(round);
        int len = msgs.length;
        if (len > MAX_SQUEAKS) len = MAX_SQUEAKS;
        Message m;
        for (int i = 0; i < len; ++i){
            m = msgs[i];
            int msg = m.getBytes();
            if (idRead.contains(m.getSenderID())) continue;
            idRead.add(m.getSenderID());
            switch((msg >>> 30)){
                case MAP:
                    int index = (3*(round + m.getSenderID()))% MapData.commMapMax;
                    int code = ((msg >>> 20) & 0x3FF);
                    if ((MapData.commMap[index] | code) != MapData.commMap[index]){
                        MapData.commMap[index] |= code;
                        if (MyUnit.isKing) BFSAdd.add(index, code);
                    }
                    index = (index+1)%MapData.commMapMax;
                    code = ((msg >>> 10) & 0x3FF);
                    if ((MapData.commMap[index] | code) != MapData.commMap[index]){
                        MapData.commMap[index] |= code;
                        if (MyUnit.isKing) BFSAdd.add(index, code);
                    }
                    index = (index+1)%MapData.commMapMax;
                    code = (msg & 0x3FF);
                    if ((MapData.commMap[index] | code) != MapData.commMap[index]){
                        MapData.commMap[index] |= code;
                        if (MyUnit.isKing) BFSAdd.add(index, code);
                    }
                    break;
                case ENEMY:
                    if (LocalBFS.computeDistance(m.getSource()) > Constants.INF) break;
                    MapLocation enemyLoc = decodeLocation(msg&0x7F, m.getSource());
                    if (MyUnit.rc.canSenseLocation(enemyLoc)) break;
                    MicroManager.addEnemy(enemyLoc, 10000 + ((msg >>> 7) & 0xFFF), MicroManager.dirs[(msg >>> 19)&7], (msg >>> 22)&0xFF);
                    MicroManager.addAlly(m.getSource(), m.getSenderID());
                    break;
                case CAT:
                    if (LocalBFS.computeDistance(m.getSource()) > Constants.INF) break;
                    MicroManager.addCat(decodeLocation(msg&0x7F, m.getSource()), ((msg >>> 7) & 0xFFF), MicroManager.dirs[(msg >>> 19)&7]);
                    MicroManager.addAlly(m.getSource(), m.getSenderID());
                    break;
                case OTHER:
                    switch ((msg >>> 28)&3){
                        case OTHER_CHEESE_MINE:
                            MapLocation mineLoc = new MapLocation((msg >>> 6) & 0x3F, msg & 0x3F);
                            MapData.addCheese(mineLoc);
                            break;
                        case OTHER_SYM:
                            SymmetryChecker.discardedSyms |= (msg & 7);
                            break;
                        case OTHER_FLYING:
                            MovementManager.flyingIDs.add(m.getSenderID());
                            MovementManager.markFlying(m.getSource(), MicroManager.dirs[msg&7]);
                            break;
                    }
                    break;
            }
        }
    }

    static boolean closeToKing() throws GameActionException {
        MapLocation kingLoc = CommunicationManager.getKingLocation();
        kingLoc = kingLoc.add(kingLoc.directionTo(MovementManager.myLoc));
        return !MyUnit.isKing && kingLoc.distanceSquaredTo(MovementManager.myLoc) <= GameConstants.SQUEAK_RADIUS_SQUARED;
    }

    static void squeakSym(){
        int code = (OTHER << 30) | (OTHER_SYM << 28) | SymmetryChecker.discardedSyms;
        MyUnit.rc.squeak(code);
    }

    static void squeakFlying(){
        int code = (OTHER << 30) | (OTHER_FLYING << 28) | MovementManager.pushDir.ordinal();
        MyUnit.rc.squeak(code);
    }

    static void squeak() throws GameActionException {
        if (MyUnit.isKing) return;

        if (MyUnit.rc.isBeingThrown()){
            if (MovementManager.pushDir != Direction.CENTER){
                squeakFlying();
            }
        }


        if (VisionManager.closestRat != null){
            squeakEnemy(ENEMY, VisionManager.closestRat);
            return;
        }

        if (!closeToKing()){
            if (canDistractCat()){
                if (VisionManager.closestCat != null){
                    squeakEnemy(CAT, VisionManager.closestCat);
                    return;
                }
                squeakMap();
            }
            return;
        }

        if ((CommunicationManager.knownSym | SymmetryChecker.discardedSyms) != CommunicationManager.knownSym){
            squeakSym();
            return;
        }

        MapLocation cheeseLoc = MapData.getUnknownMine();
        if (cheeseLoc != null){
            squeakCheese(cheeseLoc);
            MyUnit.rc.setIndicatorDot(cheeseLoc, 255, 0, 255);
        }

    }

    static boolean canDistractCat(){
        if (VisionManager.closestCat != null){
            return CommunicationManager.kingLoc.distanceSquaredTo(VisionManager.closestCat.getLocation()) + 5 < MovementManager.myLoc.distanceSquaredTo(CommunicationManager.kingLoc);
        }
        if (CatSurveillance.lastCatSeen.loc != null && CatSurveillance.recentSighting() && MovementManager.myLoc.distanceSquaredTo(CatSurveillance.lastCatSeen.loc) <= 20){
            return CommunicationManager.kingLoc.distanceSquaredTo(CatSurveillance.lastCatSeen.loc) + 5 < MovementManager.myLoc.distanceSquaredTo(CommunicationManager.kingLoc);
        }
        return false;
    }

    static int encodeLocation(MapLocation loc){
        return (loc.x - MovementManager.myLoc.x + 5)*11 + (loc.y - MovementManager.myLoc.y + 5);
    }

    static MapLocation decodeLocation(int code, MapLocation origin){
        return origin.translate(code/11 - 5, code%11 - 5);
    }

}
