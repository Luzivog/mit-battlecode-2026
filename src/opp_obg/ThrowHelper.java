package opp_obg;

import battlecode.common.*;

public class ThrowHelper {

    static int dx, dy;
    static int unitDist;

    static CollisionData bestCollision;
    static CollisionData CENTERNORTH = new CollisionData(Direction.CENTER, Direction.NORTH);
    static CollisionData NORTHEAST = new CollisionData(Direction.NORTH, Direction.EAST);
    static CollisionData NORTHNORTHEAST = new CollisionData(Direction.NORTH, Direction.NORTHEAST);
    static CollisionData NORTHNORTHWEST = new CollisionData(Direction.NORTH, Direction.NORTHWEST);
    static CollisionData NORTHWEST = new CollisionData(Direction.NORTH, Direction.WEST);
    static CollisionData CENTERNORTHEAST = new CollisionData(Direction.CENTER, Direction.NORTHEAST);
    static CollisionData NORTHEASTSOUTHEAST = new CollisionData(Direction.NORTHEAST, Direction.SOUTHEAST);
    static CollisionData NORTHEASTEAST = new CollisionData(Direction.NORTHEAST, Direction.EAST);
    static CollisionData NORTHEASTNORTH = new CollisionData(Direction.NORTHEAST, Direction.NORTH);
    static CollisionData NORTHEASTNORTHWEST = new CollisionData(Direction.NORTHEAST, Direction.NORTHWEST);
    static CollisionData CENTEREAST = new CollisionData(Direction.CENTER, Direction.EAST);
    static CollisionData EASTSOUTH = new CollisionData(Direction.EAST, Direction.SOUTH);
    static CollisionData EASTSOUTHEAST = new CollisionData(Direction.EAST, Direction.SOUTHEAST);
    static CollisionData EASTNORTHEAST = new CollisionData(Direction.EAST, Direction.NORTHEAST);
    static CollisionData EASTNORTH = new CollisionData(Direction.EAST, Direction.NORTH);
    static CollisionData CENTERSOUTHEAST = new CollisionData(Direction.CENTER, Direction.SOUTHEAST);
    static CollisionData SOUTHEASTSOUTHWEST = new CollisionData(Direction.SOUTHEAST, Direction.SOUTHWEST);
    static CollisionData SOUTHEASTSOUTH = new CollisionData(Direction.SOUTHEAST, Direction.SOUTH);
    static CollisionData SOUTHEASTEAST = new CollisionData(Direction.SOUTHEAST, Direction.EAST);
    static CollisionData SOUTHEASTNORTHEAST = new CollisionData(Direction.SOUTHEAST, Direction.NORTHEAST);
    static CollisionData CENTERSOUTH = new CollisionData(Direction.CENTER, Direction.SOUTH);
    static CollisionData SOUTHWEST = new CollisionData(Direction.SOUTH, Direction.WEST);
    static CollisionData SOUTHSOUTHWEST = new CollisionData(Direction.SOUTH, Direction.SOUTHWEST);
    static CollisionData SOUTHSOUTHEAST = new CollisionData(Direction.SOUTH, Direction.SOUTHEAST);
    static CollisionData SOUTHEAST = new CollisionData(Direction.SOUTH, Direction.EAST);
    static CollisionData CENTERSOUTHWEST = new CollisionData(Direction.CENTER, Direction.SOUTHWEST);
    static CollisionData SOUTHWESTNORTHWEST = new CollisionData(Direction.SOUTHWEST, Direction.NORTHWEST);
    static CollisionData SOUTHWESTWEST = new CollisionData(Direction.SOUTHWEST, Direction.WEST);
    static CollisionData SOUTHWESTSOUTH = new CollisionData(Direction.SOUTHWEST, Direction.SOUTH);
    static CollisionData SOUTHWESTSOUTHEAST = new CollisionData(Direction.SOUTHWEST, Direction.SOUTHEAST);
    static CollisionData CENTERWEST = new CollisionData(Direction.CENTER, Direction.WEST);
    static CollisionData WESTNORTH = new CollisionData(Direction.WEST, Direction.NORTH);
    static CollisionData WESTNORTHWEST = new CollisionData(Direction.WEST, Direction.NORTHWEST);
    static CollisionData WESTSOUTHWEST = new CollisionData(Direction.WEST, Direction.SOUTHWEST);
    static CollisionData WESTSOUTH = new CollisionData(Direction.WEST, Direction.SOUTH);
    static CollisionData CENTERNORTHWEST = new CollisionData(Direction.CENTER, Direction.NORTHWEST);
    static CollisionData NORTHWESTNORTHEAST = new CollisionData(Direction.NORTHWEST, Direction.NORTHEAST);
    static CollisionData NORTHWESTNORTH = new CollisionData(Direction.NORTHWEST, Direction.NORTH);
    static CollisionData NORTHWESTWEST = new CollisionData(Direction.NORTHWEST, Direction.WEST);
    static CollisionData NORTHWESTSOUTHWEST = new CollisionData(Direction.NORTHWEST, Direction.SOUTHWEST);

    static void collisionInit(){
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTH.reset();
                NORTHNORTHEAST.reset();
                NORTHNORTHWEST.reset();
                CENTERNORTHEAST.reset();
                NORTHEASTNORTH.reset();
                NORTHEASTNORTHWEST.reset();
                CENTERNORTHWEST.reset();
                NORTHWESTNORTHEAST.reset();
                NORTHWESTNORTH.reset();
                break;
            case NORTHEAST:
                CENTERNORTH.reset();
                NORTHEAST.reset();
                NORTHNORTHEAST.reset();
                CENTERNORTHEAST.reset();
                NORTHEASTEAST.reset();
                NORTHEASTNORTH.reset();
                CENTEREAST.reset();
                EASTNORTHEAST.reset();
                EASTNORTH.reset();
                break;
            case EAST:
                CENTERNORTHEAST.reset();
                NORTHEASTSOUTHEAST.reset();
                NORTHEASTEAST.reset();
                CENTEREAST.reset();
                EASTSOUTHEAST.reset();
                EASTNORTHEAST.reset();
                CENTERSOUTHEAST.reset();
                SOUTHEASTEAST.reset();
                SOUTHEASTNORTHEAST.reset();
                break;
            case SOUTHEAST:
                CENTEREAST.reset();
                EASTSOUTH.reset();
                EASTSOUTHEAST.reset();
                CENTERSOUTHEAST.reset();
                SOUTHEASTSOUTH.reset();
                SOUTHEASTEAST.reset();
                CENTERSOUTH.reset();
                SOUTHSOUTHEAST.reset();
                SOUTHEAST.reset();
                break;
            case SOUTH:
                CENTERSOUTHEAST.reset();
                SOUTHEASTSOUTHWEST.reset();
                SOUTHEASTSOUTH.reset();
                CENTERSOUTH.reset();
                SOUTHSOUTHWEST.reset();
                SOUTHSOUTHEAST.reset();
                CENTERSOUTHWEST.reset();
                SOUTHWESTSOUTH.reset();
                SOUTHWESTSOUTHEAST.reset();
                break;
            case SOUTHWEST:
                CENTERSOUTH.reset();
                SOUTHWEST.reset();
                SOUTHSOUTHWEST.reset();
                CENTERSOUTHWEST.reset();
                SOUTHWESTWEST.reset();
                SOUTHWESTSOUTH.reset();
                CENTERWEST.reset();
                WESTSOUTHWEST.reset();
                WESTSOUTH.reset();
                break;
            case WEST:
                CENTERSOUTHWEST.reset();
                SOUTHWESTNORTHWEST.reset();
                SOUTHWESTWEST.reset();
                CENTERWEST.reset();
                WESTNORTHWEST.reset();
                WESTSOUTHWEST.reset();
                CENTERNORTHWEST.reset();
                NORTHWESTWEST.reset();
                NORTHWESTSOUTHWEST.reset();
                break;
            case NORTHWEST:
                CENTERNORTH.reset();
                NORTHNORTHWEST.reset();
                NORTHWEST.reset();
                CENTERWEST.reset();
                WESTNORTH.reset();
                WESTNORTHWEST.reset();
                CENTERNORTHWEST.reset();
                NORTHWESTNORTH.reset();
                NORTHWESTWEST.reset();
                break;
        }
    }

    static void findBestCollision(){
        bestCollision = null;
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                if(CENTERNORTH.isBetterThan(bestCollision)) bestCollision = CENTERNORTH;
                if(NORTHNORTHEAST.isBetterThan(bestCollision)) bestCollision = NORTHNORTHEAST;
                if(NORTHNORTHWEST.isBetterThan(bestCollision)) bestCollision = NORTHNORTHWEST;
                if(CENTERNORTHEAST.isBetterThan(bestCollision)) bestCollision = CENTERNORTHEAST;
                if(NORTHEASTNORTH.isBetterThan(bestCollision)) bestCollision = NORTHEASTNORTH;
                if(NORTHEASTNORTHWEST.isBetterThan(bestCollision)) bestCollision = NORTHEASTNORTHWEST;
                if(CENTERNORTHWEST.isBetterThan(bestCollision)) bestCollision = CENTERNORTHWEST;
                if(NORTHWESTNORTHEAST.isBetterThan(bestCollision)) bestCollision = NORTHWESTNORTHEAST;
                if(NORTHWESTNORTH.isBetterThan(bestCollision)) bestCollision = NORTHWESTNORTH;
                break;
            case NORTHEAST:
                if(CENTERNORTH.isBetterThan(bestCollision)) bestCollision = CENTERNORTH;
                if(NORTHEAST.isBetterThan(bestCollision)) bestCollision = NORTHEAST;
                if(NORTHNORTHEAST.isBetterThan(bestCollision)) bestCollision = NORTHNORTHEAST;
                if(CENTERNORTHEAST.isBetterThan(bestCollision)) bestCollision = CENTERNORTHEAST;
                if(NORTHEASTEAST.isBetterThan(bestCollision)) bestCollision = NORTHEASTEAST;
                if(NORTHEASTNORTH.isBetterThan(bestCollision)) bestCollision = NORTHEASTNORTH;
                if(CENTEREAST.isBetterThan(bestCollision)) bestCollision = CENTEREAST;
                if(EASTNORTHEAST.isBetterThan(bestCollision)) bestCollision = EASTNORTHEAST;
                if(EASTNORTH.isBetterThan(bestCollision)) bestCollision = EASTNORTH;
                break;
            case EAST:
                if(CENTERNORTHEAST.isBetterThan(bestCollision)) bestCollision = CENTERNORTHEAST;
                if(NORTHEASTSOUTHEAST.isBetterThan(bestCollision)) bestCollision = NORTHEASTSOUTHEAST;
                if(NORTHEASTEAST.isBetterThan(bestCollision)) bestCollision = NORTHEASTEAST;
                if(CENTEREAST.isBetterThan(bestCollision)) bestCollision = CENTEREAST;
                if(EASTSOUTHEAST.isBetterThan(bestCollision)) bestCollision = EASTSOUTHEAST;
                if(EASTNORTHEAST.isBetterThan(bestCollision)) bestCollision = EASTNORTHEAST;
                if(CENTERSOUTHEAST.isBetterThan(bestCollision)) bestCollision = CENTERSOUTHEAST;
                if(SOUTHEASTEAST.isBetterThan(bestCollision)) bestCollision = SOUTHEASTEAST;
                if(SOUTHEASTNORTHEAST.isBetterThan(bestCollision)) bestCollision = SOUTHEASTNORTHEAST;
                break;
            case SOUTHEAST:
                if(CENTEREAST.isBetterThan(bestCollision)) bestCollision = CENTEREAST;
                if(EASTSOUTH.isBetterThan(bestCollision)) bestCollision = EASTSOUTH;
                if(EASTSOUTHEAST.isBetterThan(bestCollision)) bestCollision = EASTSOUTHEAST;
                if(CENTERSOUTHEAST.isBetterThan(bestCollision)) bestCollision = CENTERSOUTHEAST;
                if(SOUTHEASTSOUTH.isBetterThan(bestCollision)) bestCollision = SOUTHEASTSOUTH;
                if(SOUTHEASTEAST.isBetterThan(bestCollision)) bestCollision = SOUTHEASTEAST;
                if(CENTERSOUTH.isBetterThan(bestCollision)) bestCollision = CENTERSOUTH;
                if(SOUTHSOUTHEAST.isBetterThan(bestCollision)) bestCollision = SOUTHSOUTHEAST;
                if(SOUTHEAST.isBetterThan(bestCollision)) bestCollision = SOUTHEAST;
                break;
            case SOUTH:
                if(CENTERSOUTHEAST.isBetterThan(bestCollision)) bestCollision = CENTERSOUTHEAST;
                if(SOUTHEASTSOUTHWEST.isBetterThan(bestCollision)) bestCollision = SOUTHEASTSOUTHWEST;
                if(SOUTHEASTSOUTH.isBetterThan(bestCollision)) bestCollision = SOUTHEASTSOUTH;
                if(CENTERSOUTH.isBetterThan(bestCollision)) bestCollision = CENTERSOUTH;
                if(SOUTHSOUTHWEST.isBetterThan(bestCollision)) bestCollision = SOUTHSOUTHWEST;
                if(SOUTHSOUTHEAST.isBetterThan(bestCollision)) bestCollision = SOUTHSOUTHEAST;
                if(CENTERSOUTHWEST.isBetterThan(bestCollision)) bestCollision = CENTERSOUTHWEST;
                if(SOUTHWESTSOUTH.isBetterThan(bestCollision)) bestCollision = SOUTHWESTSOUTH;
                if(SOUTHWESTSOUTHEAST.isBetterThan(bestCollision)) bestCollision = SOUTHWESTSOUTHEAST;
                break;
            case SOUTHWEST:
                if(CENTERSOUTH.isBetterThan(bestCollision)) bestCollision = CENTERSOUTH;
                if(SOUTHWEST.isBetterThan(bestCollision)) bestCollision = SOUTHWEST;
                if(SOUTHSOUTHWEST.isBetterThan(bestCollision)) bestCollision = SOUTHSOUTHWEST;
                if(CENTERSOUTHWEST.isBetterThan(bestCollision)) bestCollision = CENTERSOUTHWEST;
                if(SOUTHWESTWEST.isBetterThan(bestCollision)) bestCollision = SOUTHWESTWEST;
                if(SOUTHWESTSOUTH.isBetterThan(bestCollision)) bestCollision = SOUTHWESTSOUTH;
                if(CENTERWEST.isBetterThan(bestCollision)) bestCollision = CENTERWEST;
                if(WESTSOUTHWEST.isBetterThan(bestCollision)) bestCollision = WESTSOUTHWEST;
                if(WESTSOUTH.isBetterThan(bestCollision)) bestCollision = WESTSOUTH;
                break;
            case WEST:
                if(CENTERSOUTHWEST.isBetterThan(bestCollision)) bestCollision = CENTERSOUTHWEST;
                if(SOUTHWESTNORTHWEST.isBetterThan(bestCollision)) bestCollision = SOUTHWESTNORTHWEST;
                if(SOUTHWESTWEST.isBetterThan(bestCollision)) bestCollision = SOUTHWESTWEST;
                if(CENTERWEST.isBetterThan(bestCollision)) bestCollision = CENTERWEST;
                if(WESTNORTHWEST.isBetterThan(bestCollision)) bestCollision = WESTNORTHWEST;
                if(WESTSOUTHWEST.isBetterThan(bestCollision)) bestCollision = WESTSOUTHWEST;
                if(CENTERNORTHWEST.isBetterThan(bestCollision)) bestCollision = CENTERNORTHWEST;
                if(NORTHWESTWEST.isBetterThan(bestCollision)) bestCollision = NORTHWESTWEST;
                if(NORTHWESTSOUTHWEST.isBetterThan(bestCollision)) bestCollision = NORTHWESTSOUTHWEST;
                break;
            case NORTHWEST:
                if(CENTERNORTH.isBetterThan(bestCollision)) bestCollision = CENTERNORTH;
                if(NORTHNORTHWEST.isBetterThan(bestCollision)) bestCollision = NORTHNORTHWEST;
                if(NORTHWEST.isBetterThan(bestCollision)) bestCollision = NORTHWEST;
                if(CENTERWEST.isBetterThan(bestCollision)) bestCollision = CENTERWEST;
                if(WESTNORTH.isBetterThan(bestCollision)) bestCollision = WESTNORTH;
                if(WESTNORTHWEST.isBetterThan(bestCollision)) bestCollision = WESTNORTHWEST;
                if(CENTERNORTHWEST.isBetterThan(bestCollision)) bestCollision = CENTERNORTHWEST;
                if(NORTHWESTNORTH.isBetterThan(bestCollision)) bestCollision = NORTHWESTNORTH;
                if(NORTHWESTWEST.isBetterThan(bestCollision)) bestCollision = NORTHWESTWEST;
                break;
        }
    }

    static void addCollision(){
        dx = VisionManager.collisionObject.x - MovementManager.myLoc.x;
        dy = VisionManager.collisionObject.y - MovementManager.myLoc.y;
        switch(dx){
            case -4:
                switch(dy){
                    case -4:
                        addCollision60();
                        break;
                    case -3:
                        addCollision61();
                        break;
                    case -2:
                        addCollision62();
                        break;
                    case -1:
                        addCollision63();
                        break;
                    case 0:
                        addCollision64();
                        break;
                    case 1:
                        addCollision65();
                        break;
                    case 2:
                        addCollision66();
                        break;
                    case 3:
                        addCollision67();
                        break;
                    case 4:
                        addCollision68();
                        break;
                }
                break;
            case -3:
                switch(dy){
                    case -4:
                        addCollision69();
                        break;
                    case -3:
                        addCollision70();
                        break;
                    case -2:
                        addCollision71();
                        break;
                    case -1:
                        addCollision72();
                        break;
                    case 0:
                        addCollision73();
                        break;
                    case 1:
                        addCollision74();
                        break;
                    case 2:
                        addCollision75();
                        break;
                    case 3:
                        addCollision76();
                        break;
                    case 4:
                        addCollision77();
                        break;
                }
                break;
            case -2:
                switch(dy){
                    case -4:
                        addCollision78();
                        break;
                    case -3:
                        addCollision79();
                        break;
                    case -2:
                        addCollision80();
                        break;
                    case -1:
                        addCollision81();
                        break;
                    case 0:
                        addCollision82();
                        break;
                    case 1:
                        addCollision83();
                        break;
                    case 2:
                        addCollision84();
                        break;
                    case 3:
                        addCollision85();
                        break;
                    case 4:
                        addCollision86();
                        break;
                }
                break;
            case -1:
                switch(dy){
                    case -4:
                        addCollision87();
                        break;
                    case -3:
                        addCollision88();
                        break;
                    case -2:
                        addCollision89();
                        break;
                    case -1:
                        addCollision90();
                        break;
                    case 0:
                        addCollision91();
                        break;
                    case 1:
                        addCollision92();
                        break;
                    case 2:
                        addCollision93();
                        break;
                    case 3:
                        addCollision94();
                        break;
                    case 4:
                        addCollision95();
                        break;
                }
                break;
            case 0:
                switch(dy){
                    case -4:
                        addCollision96();
                        break;
                    case -3:
                        addCollision97();
                        break;
                    case -2:
                        addCollision98();
                        break;
                    case -1:
                        addCollision99();
                        break;
                    case 0:
                        addCollision100();
                        break;
                    case 1:
                        addCollision101();
                        break;
                    case 2:
                        addCollision102();
                        break;
                    case 3:
                        addCollision103();
                        break;
                    case 4:
                        addCollision104();
                        break;
                }
                break;
            case 1:
                switch(dy){
                    case -4:
                        addCollision105();
                        break;
                    case -3:
                        addCollision106();
                        break;
                    case -2:
                        addCollision107();
                        break;
                    case -1:
                        addCollision108();
                        break;
                    case 0:
                        addCollision109();
                        break;
                    case 1:
                        addCollision110();
                        break;
                    case 2:
                        addCollision111();
                        break;
                    case 3:
                        addCollision112();
                        break;
                    case 4:
                        addCollision113();
                        break;
                }
                break;
            case 2:
                switch(dy){
                    case -4:
                        addCollision114();
                        break;
                    case -3:
                        addCollision115();
                        break;
                    case -2:
                        addCollision116();
                        break;
                    case -1:
                        addCollision117();
                        break;
                    case 0:
                        addCollision118();
                        break;
                    case 1:
                        addCollision119();
                        break;
                    case 2:
                        addCollision120();
                        break;
                    case 3:
                        addCollision121();
                        break;
                    case 4:
                        addCollision122();
                        break;
                }
                break;
            case 3:
                switch(dy){
                    case -4:
                        addCollision123();
                        break;
                    case -3:
                        addCollision124();
                        break;
                    case -2:
                        addCollision125();
                        break;
                    case -1:
                        addCollision126();
                        break;
                    case 0:
                        addCollision127();
                        break;
                    case 1:
                        addCollision128();
                        break;
                    case 2:
                        addCollision129();
                        break;
                    case 3:
                        addCollision130();
                        break;
                    case 4:
                        addCollision131();
                        break;
                }
                break;
            case 4:
                switch(dy){
                    case -4:
                        addCollision132();
                        break;
                    case -3:
                        addCollision133();
                        break;
                    case -2:
                        addCollision134();
                        break;
                    case -1:
                        addCollision135();
                        break;
                    case 0:
                        addCollision136();
                        break;
                    case 1:
                        addCollision137();
                        break;
                    case 2:
                        addCollision138();
                        break;
                    case 3:
                        addCollision139();
                        break;
                    case 4:
                        addCollision140();
                        break;
                }
                break;
        }
    }

    static void addCollision60(){ // (-4, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                CENTERSOUTHWEST.add(4);
                break;
            case SOUTHWEST:
                CENTERSOUTHWEST.add(4);
                break;
            case WEST:
                CENTERSOUTHWEST.add(4);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision61(){ // (-4, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                WESTSOUTHWEST.add(3);
                break;
            case WEST:
                WESTSOUTHWEST.add(3);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision62(){ // (-4, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                NORTHWESTSOUTHWEST.add(3);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision63(){ // (-4, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                SOUTHWEST.add(4);
                SOUTHWESTWEST.add(3);
                break;
            case WEST:
                SOUTHWESTWEST.add(3);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision64(){ // (-4, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                CENTERWEST.add(4);
                break;
            case WEST:
                CENTERWEST.add(4);
                break;
            case NORTHWEST:
                CENTERWEST.add(4);
                break;
        }
    }

    static void addCollision65(){ // (-4, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                NORTHWESTWEST.add(3);
                break;
            case NORTHWEST:
                NORTHWEST.add(4);
                NORTHWESTWEST.add(3);
                break;
        }
    }

    static void addCollision66(){ // (-4, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                SOUTHWESTNORTHWEST.add(3);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision67(){ // (-4, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                WESTNORTHWEST.add(3);
                break;
            case NORTHWEST:
                WESTNORTHWEST.add(3);
                break;
        }
    }

    static void addCollision68(){ // (-4, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHWEST.add(4);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                CENTERNORTHWEST.add(4);
                break;
            case NORTHWEST:
                CENTERNORTHWEST.add(4);
                break;
        }
    }

    static void addCollision69(){ // (-3, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHSOUTHWEST.add(3);
                break;
            case SOUTHWEST:
                SOUTHSOUTHWEST.add(3);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision70(){ // (-3, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                CENTERSOUTHWEST.add(3);
                break;
            case SOUTHWEST:
                CENTERSOUTHWEST.add(3);
                break;
            case WEST:
                CENTERSOUTHWEST.add(3);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision71(){ // (-3, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                WESTSOUTHWEST.add(2);
                break;
            case WEST:
                WESTSOUTHWEST.add(2);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision72(){ // (-3, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                SOUTHWEST.add(3);
                SOUTHWESTWEST.add(2);
                break;
            case WEST:
                SOUTHWESTWEST.add(2);
                NORTHWESTSOUTHWEST.add(2);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision73(){ // (-3, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                CENTERWEST.add(3);
                break;
            case WEST:
                CENTERWEST.add(3);
                break;
            case NORTHWEST:
                CENTERWEST.add(3);
                break;
        }
    }

    static void addCollision74(){ // (-3, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                SOUTHWESTNORTHWEST.add(2);
                NORTHWESTWEST.add(2);
                break;
            case NORTHWEST:
                NORTHWEST.add(3);
                NORTHWESTWEST.add(2);
                break;
        }
    }

    static void addCollision75(){ // (-3, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                WESTNORTHWEST.add(2);
                break;
            case NORTHWEST:
                WESTNORTHWEST.add(2);
                break;
        }
    }

    static void addCollision76(){ // (-3, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHWEST.add(3);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                CENTERNORTHWEST.add(3);
                break;
            case NORTHWEST:
                CENTERNORTHWEST.add(3);
                break;
        }
    }

    static void addCollision77(){ // (-3, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHNORTHWEST.add(3);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                NORTHNORTHWEST.add(3);
                break;
        }
    }

    static void addCollision78(){ // (-2, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHEASTSOUTHWEST.add(3);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision79(){ // (-2, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHSOUTHWEST.add(2);
                break;
            case SOUTHWEST:
                SOUTHSOUTHWEST.add(2);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision80(){ // (-2, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                CENTERSOUTHWEST.add(2);
                break;
            case SOUTHWEST:
                CENTERSOUTHWEST.add(2);
                break;
            case WEST:
                CENTERSOUTHWEST.add(2);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision81(){ // (-2, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                SOUTHWEST.add(2);
                SOUTHWESTWEST.add(1);
                WESTSOUTHWEST.add(1);
                break;
            case WEST:
                SOUTHWESTWEST.add(1);
                WESTSOUTHWEST.add(1);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision82(){ // (-2, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                CENTERWEST.add(2);
                break;
            case WEST:
                SOUTHWESTNORTHWEST.add(1);
                CENTERWEST.add(2);
                NORTHWESTSOUTHWEST.add(1);
                break;
            case NORTHWEST:
                CENTERWEST.add(2);
                break;
        }
    }

    static void addCollision83(){ // (-2, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                WESTNORTHWEST.add(1);
                NORTHWESTWEST.add(1);
                break;
            case NORTHWEST:
                NORTHWEST.add(2);
                WESTNORTHWEST.add(1);
                NORTHWESTWEST.add(1);
                break;
        }
    }

    static void addCollision84(){ // (-2, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHWEST.add(2);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                CENTERNORTHWEST.add(2);
                break;
            case NORTHWEST:
                CENTERNORTHWEST.add(2);
                break;
        }
    }

    static void addCollision85(){ // (-2, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHNORTHWEST.add(2);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                NORTHNORTHWEST.add(2);
                break;
        }
    }

    static void addCollision86(){ // (-2, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHEASTNORTHWEST.add(3);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision87(){ // (-1, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHWESTSOUTH.add(3);
                break;
            case SOUTHWEST:
                SOUTHWESTSOUTH.add(3);
                WESTSOUTH.add(4);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision88(){ // (-1, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHEASTSOUTHWEST.add(2);
                SOUTHWESTSOUTH.add(2);
                break;
            case SOUTHWEST:
                SOUTHWESTSOUTH.add(2);
                WESTSOUTH.add(3);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision89(){ // (-1, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHSOUTHWEST.add(1);
                SOUTHWESTSOUTH.add(1);
                break;
            case SOUTHWEST:
                SOUTHSOUTHWEST.add(1);
                SOUTHWESTSOUTH.add(1);
                WESTSOUTH.add(2);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision90(){ // (-1, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                CENTERSOUTHWEST.add(1);
                SOUTHWESTSOUTH.add(0);
                SOUTHWESTSOUTHEAST.add(0);
                break;
            case SOUTHWEST:
                SOUTHWEST.add(1);
                CENTERSOUTHWEST.add(1);
                SOUTHWESTWEST.add(0);
                SOUTHWESTSOUTH.add(0);
                WESTSOUTH.add(1);
                break;
            case WEST:
                CENTERSOUTHWEST.add(1);
                SOUTHWESTNORTHWEST.add(0);
                SOUTHWESTWEST.add(0);
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision91(){ // (-1, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                CENTERWEST.add(1);
                WESTSOUTHWEST.add(0);
                WESTSOUTH.add(0);
                break;
            case WEST:
                CENTERWEST.add(1);
                WESTNORTHWEST.add(0);
                WESTSOUTHWEST.add(0);
                break;
            case NORTHWEST:
                CENTERWEST.add(1);
                WESTNORTH.add(0);
                WESTNORTHWEST.add(0);
                break;
        }
    }

    static void addCollision92(){ // (-1, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHWEST.add(1);
                NORTHWESTNORTHEAST.add(0);
                NORTHWESTNORTH.add(0);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                CENTERNORTHWEST.add(1);
                NORTHWESTWEST.add(0);
                NORTHWESTSOUTHWEST.add(0);
                break;
            case NORTHWEST:
                NORTHWEST.add(1);
                WESTNORTH.add(1);
                CENTERNORTHWEST.add(1);
                NORTHWESTNORTH.add(0);
                NORTHWESTWEST.add(0);
                break;
        }
    }

    static void addCollision93(){ // (-1, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHNORTHWEST.add(1);
                NORTHWESTNORTH.add(1);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                NORTHNORTHWEST.add(1);
                WESTNORTH.add(2);
                NORTHWESTNORTH.add(1);
                break;
        }
    }

    static void addCollision94(){ // (-1, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHEASTNORTHWEST.add(2);
                NORTHWESTNORTH.add(2);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                WESTNORTH.add(3);
                NORTHWESTNORTH.add(2);
                break;
        }
    }

    static void addCollision95(){ // (-1, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHWESTNORTH.add(3);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                WESTNORTH.add(4);
                NORTHWESTNORTH.add(3);
                break;
        }
    }

    static void addCollision96(){ // (0, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                CENTERSOUTH.add(4);
                break;
            case SOUTH:
                CENTERSOUTH.add(4);
                break;
            case SOUTHWEST:
                CENTERSOUTH.add(4);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision97(){ // (0, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                CENTERSOUTH.add(3);
                break;
            case SOUTH:
                CENTERSOUTH.add(3);
                break;
            case SOUTHWEST:
                CENTERSOUTH.add(3);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision98(){ // (0, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                CENTERSOUTH.add(2);
                break;
            case SOUTH:
                SOUTHEASTSOUTHWEST.add(1);
                CENTERSOUTH.add(2);
                SOUTHWESTSOUTHEAST.add(1);
                break;
            case SOUTHWEST:
                CENTERSOUTH.add(2);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision99(){ // (0, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                CENTERSOUTH.add(1);
                SOUTHSOUTHEAST.add(0);
                SOUTHEAST.add(0);
                break;
            case SOUTH:
                CENTERSOUTH.add(1);
                SOUTHSOUTHWEST.add(0);
                SOUTHSOUTHEAST.add(0);
                break;
            case SOUTHWEST:
                CENTERSOUTH.add(1);
                SOUTHWEST.add(0);
                SOUTHSOUTHWEST.add(0);
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision100(){ // (0, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTH.add(0);
                CENTERNORTHEAST.add(0);
                CENTERNORTHWEST.add(0);
                break;
            case NORTHEAST:
                CENTERNORTH.add(0);
                CENTERNORTHEAST.add(0);
                CENTEREAST.add(0);
                break;
            case EAST:
                CENTERNORTHEAST.add(0);
                CENTEREAST.add(0);
                CENTERSOUTHEAST.add(0);
                break;
            case SOUTHEAST:
                CENTEREAST.add(0);
                CENTERSOUTHEAST.add(0);
                CENTERSOUTH.add(0);
                break;
            case SOUTH:
                CENTERSOUTHEAST.add(0);
                CENTERSOUTH.add(0);
                CENTERSOUTHWEST.add(0);
                break;
            case SOUTHWEST:
                CENTERSOUTH.add(0);
                CENTERSOUTHWEST.add(0);
                CENTERWEST.add(0);
                break;
            case WEST:
                CENTERSOUTHWEST.add(0);
                CENTERWEST.add(0);
                CENTERNORTHWEST.add(0);
                break;
            case NORTHWEST:
                CENTERNORTH.add(0);
                CENTERWEST.add(0);
                CENTERNORTHWEST.add(0);
                break;
        }
    }

    static void addCollision101(){ // (0, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTH.add(1);
                NORTHNORTHEAST.add(0);
                NORTHNORTHWEST.add(0);
                break;
            case NORTHEAST:
                CENTERNORTH.add(1);
                NORTHEAST.add(0);
                NORTHNORTHEAST.add(0);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                CENTERNORTH.add(1);
                NORTHNORTHWEST.add(0);
                NORTHWEST.add(0);
                break;
        }
    }

    static void addCollision102(){ // (0, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTH.add(2);
                NORTHEASTNORTHWEST.add(1);
                NORTHWESTNORTHEAST.add(1);
                break;
            case NORTHEAST:
                CENTERNORTH.add(2);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                CENTERNORTH.add(2);
                break;
        }
    }

    static void addCollision103(){ // (0, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTH.add(3);
                break;
            case NORTHEAST:
                CENTERNORTH.add(3);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                CENTERNORTH.add(3);
                break;
        }
    }

    static void addCollision104(){ // (0, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTH.add(4);
                break;
            case NORTHEAST:
                CENTERNORTH.add(4);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                CENTERNORTH.add(4);
                break;
        }
    }

    static void addCollision105(){ // (1, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                EASTSOUTH.add(4);
                SOUTHEASTSOUTH.add(3);
                break;
            case SOUTH:
                SOUTHEASTSOUTH.add(3);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision106(){ // (1, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                EASTSOUTH.add(3);
                SOUTHEASTSOUTH.add(2);
                break;
            case SOUTH:
                SOUTHEASTSOUTH.add(2);
                SOUTHWESTSOUTHEAST.add(2);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision107(){ // (1, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                EASTSOUTH.add(2);
                SOUTHEASTSOUTH.add(1);
                SOUTHSOUTHEAST.add(1);
                break;
            case SOUTH:
                SOUTHEASTSOUTH.add(1);
                SOUTHSOUTHEAST.add(1);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision108(){ // (1, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                CENTERSOUTHEAST.add(1);
                SOUTHEASTEAST.add(0);
                SOUTHEASTNORTHEAST.add(0);
                break;
            case SOUTHEAST:
                EASTSOUTH.add(1);
                CENTERSOUTHEAST.add(1);
                SOUTHEASTSOUTH.add(0);
                SOUTHEASTEAST.add(0);
                SOUTHEAST.add(1);
                break;
            case SOUTH:
                CENTERSOUTHEAST.add(1);
                SOUTHEASTSOUTHWEST.add(0);
                SOUTHEASTSOUTH.add(0);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision109(){ // (1, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                CENTEREAST.add(1);
                EASTNORTHEAST.add(0);
                EASTNORTH.add(0);
                break;
            case EAST:
                CENTEREAST.add(1);
                EASTSOUTHEAST.add(0);
                EASTNORTHEAST.add(0);
                break;
            case SOUTHEAST:
                CENTEREAST.add(1);
                EASTSOUTH.add(0);
                EASTSOUTHEAST.add(0);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision110(){ // (1, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHEAST.add(1);
                NORTHEASTNORTH.add(0);
                NORTHEASTNORTHWEST.add(0);
                break;
            case NORTHEAST:
                NORTHEAST.add(1);
                CENTERNORTHEAST.add(1);
                NORTHEASTEAST.add(0);
                NORTHEASTNORTH.add(0);
                EASTNORTH.add(1);
                break;
            case EAST:
                CENTERNORTHEAST.add(1);
                NORTHEASTSOUTHEAST.add(0);
                NORTHEASTEAST.add(0);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision111(){ // (1, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHNORTHEAST.add(1);
                NORTHEASTNORTH.add(1);
                break;
            case NORTHEAST:
                NORTHNORTHEAST.add(1);
                NORTHEASTNORTH.add(1);
                EASTNORTH.add(2);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision112(){ // (1, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHEASTNORTH.add(2);
                NORTHWESTNORTHEAST.add(2);
                break;
            case NORTHEAST:
                NORTHEASTNORTH.add(2);
                EASTNORTH.add(3);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision113(){ // (1, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHEASTNORTH.add(3);
                break;
            case NORTHEAST:
                NORTHEASTNORTH.add(3);
                EASTNORTH.add(4);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision114(){ // (2, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                SOUTHWESTSOUTHEAST.add(3);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision115(){ // (2, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                SOUTHSOUTHEAST.add(2);
                break;
            case SOUTH:
                SOUTHSOUTHEAST.add(2);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision116(){ // (2, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                CENTERSOUTHEAST.add(2);
                break;
            case SOUTHEAST:
                CENTERSOUTHEAST.add(2);
                break;
            case SOUTH:
                CENTERSOUTHEAST.add(2);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision117(){ // (2, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                EASTSOUTHEAST.add(1);
                SOUTHEASTEAST.add(1);
                break;
            case SOUTHEAST:
                EASTSOUTHEAST.add(1);
                SOUTHEASTEAST.add(1);
                SOUTHEAST.add(2);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision118(){ // (2, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                CENTEREAST.add(2);
                break;
            case EAST:
                NORTHEASTSOUTHEAST.add(1);
                CENTEREAST.add(2);
                SOUTHEASTNORTHEAST.add(1);
                break;
            case SOUTHEAST:
                CENTEREAST.add(2);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision119(){ // (2, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                NORTHEAST.add(2);
                NORTHEASTEAST.add(1);
                EASTNORTHEAST.add(1);
                break;
            case EAST:
                NORTHEASTEAST.add(1);
                EASTNORTHEAST.add(1);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision120(){ // (2, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHEAST.add(2);
                break;
            case NORTHEAST:
                CENTERNORTHEAST.add(2);
                break;
            case EAST:
                CENTERNORTHEAST.add(2);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision121(){ // (2, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHNORTHEAST.add(2);
                break;
            case NORTHEAST:
                NORTHNORTHEAST.add(2);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision122(){ // (2, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHWESTNORTHEAST.add(3);
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision123(){ // (3, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                break;
            case SOUTHEAST:
                SOUTHSOUTHEAST.add(3);
                break;
            case SOUTH:
                SOUTHSOUTHEAST.add(3);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision124(){ // (3, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                CENTERSOUTHEAST.add(3);
                break;
            case SOUTHEAST:
                CENTERSOUTHEAST.add(3);
                break;
            case SOUTH:
                CENTERSOUTHEAST.add(3);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision125(){ // (3, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                EASTSOUTHEAST.add(2);
                break;
            case SOUTHEAST:
                EASTSOUTHEAST.add(2);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision126(){ // (3, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                NORTHEASTSOUTHEAST.add(2);
                SOUTHEASTEAST.add(2);
                break;
            case SOUTHEAST:
                SOUTHEASTEAST.add(2);
                SOUTHEAST.add(3);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision127(){ // (3, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                CENTEREAST.add(3);
                break;
            case EAST:
                CENTEREAST.add(3);
                break;
            case SOUTHEAST:
                CENTEREAST.add(3);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision128(){ // (3, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                NORTHEAST.add(3);
                NORTHEASTEAST.add(2);
                break;
            case EAST:
                NORTHEASTEAST.add(2);
                SOUTHEASTNORTHEAST.add(2);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision129(){ // (3, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                EASTNORTHEAST.add(2);
                break;
            case EAST:
                EASTNORTHEAST.add(2);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision130(){ // (3, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHEAST.add(3);
                break;
            case NORTHEAST:
                CENTERNORTHEAST.add(3);
                break;
            case EAST:
                CENTERNORTHEAST.add(3);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision131(){ // (3, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                NORTHNORTHEAST.add(3);
                break;
            case NORTHEAST:
                NORTHNORTHEAST.add(3);
                break;
            case EAST:
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision132(){ // (4, -4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                CENTERSOUTHEAST.add(4);
                break;
            case SOUTHEAST:
                CENTERSOUTHEAST.add(4);
                break;
            case SOUTH:
                CENTERSOUTHEAST.add(4);
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision133(){ // (4, -3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                EASTSOUTHEAST.add(3);
                break;
            case SOUTHEAST:
                EASTSOUTHEAST.add(3);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision134(){ // (4, -2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                NORTHEASTSOUTHEAST.add(3);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision135(){ // (4, -1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                SOUTHEASTEAST.add(3);
                break;
            case SOUTHEAST:
                SOUTHEASTEAST.add(3);
                SOUTHEAST.add(4);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision136(){ // (4, 0)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                CENTEREAST.add(4);
                break;
            case EAST:
                CENTEREAST.add(4);
                break;
            case SOUTHEAST:
                CENTEREAST.add(4);
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision137(){ // (4, 1)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                NORTHEAST.add(4);
                NORTHEASTEAST.add(3);
                break;
            case EAST:
                NORTHEASTEAST.add(3);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision138(){ // (4, 2)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                break;
            case EAST:
                SOUTHEASTNORTHEAST.add(3);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision139(){ // (4, 3)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                break;
            case NORTHEAST:
                EASTNORTHEAST.add(3);
                break;
            case EAST:
                EASTNORTHEAST.add(3);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }

    static void addCollision140(){ // (4, 4)
        switch(MyUnit.rc.getDirection()){
            case NORTH:
                CENTERNORTHEAST.add(4);
                break;
            case NORTHEAST:
                CENTERNORTHEAST.add(4);
                break;
            case EAST:
                CENTERNORTHEAST.add(4);
                break;
            case SOUTHEAST:
                break;
            case SOUTH:
                break;
            case SOUTHWEST:
                break;
            case WEST:
                break;
            case NORTHWEST:
                break;
        }
    }


}
