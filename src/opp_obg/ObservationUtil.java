package opp_obg;

import battlecode.common.*;

public class ObservationUtil {

    static int dx, dy;
    static int unitDist;

    static void addEnemy(){
        dx = MovementManager.myLoc.x - MicroManager.unitLocation.x;
        dy = MovementManager.myLoc.y - MicroManager.unitLocation.y;
        switch(dx){
            case -3:
                switch(dy){
                    case -3:
                        addEnemy76();
                        break;
                    case -2:
                        addEnemy77();
                        break;
                    case -1:
                        addEnemy78();
                        break;
                    case 0:
                        addEnemy79();
                        break;
                    case 1:
                        addEnemy80();
                        break;
                    case 2:
                        addEnemy81();
                        break;
                    case 3:
                        addEnemy82();
                        break;
                    default: addEnemyDefault();
                }
                break;
            case -2:
                switch(dy){
                    case -3:
                        addEnemy83();
                        break;
                    case -2:
                        addEnemy84();
                        break;
                    case -1:
                        addEnemy85();
                        break;
                    case 0:
                        addEnemy86();
                        break;
                    case 1:
                        addEnemy87();
                        break;
                    case 2:
                        addEnemy88();
                        break;
                    case 3:
                        addEnemy89();
                        break;
                    default: addEnemyDefault();
                }
                break;
            case -1:
                switch(dy){
                    case -3:
                        addEnemy90();
                        break;
                    case -2:
                        addEnemy91();
                        break;
                    case -1:
                        addEnemy92();
                        break;
                    case 0:
                        addEnemy93();
                        break;
                    case 1:
                        addEnemy94();
                        break;
                    case 2:
                        addEnemy95();
                        break;
                    case 3:
                        addEnemy96();
                        break;
                    default: addEnemyDefault();
                }
                break;
            case 0:
                switch(dy){
                    case -3:
                        addEnemy97();
                        break;
                    case -2:
                        addEnemy98();
                        break;
                    case -1:
                        addEnemy99();
                        break;
                    case 0:
                        addEnemy100();
                        break;
                    case 1:
                        addEnemy101();
                        break;
                    case 2:
                        addEnemy102();
                        break;
                    case 3:
                        addEnemy103();
                        break;
                    default: addEnemyDefault();
                }
                break;
            case 1:
                switch(dy){
                    case -3:
                        addEnemy104();
                        break;
                    case -2:
                        addEnemy105();
                        break;
                    case -1:
                        addEnemy106();
                        break;
                    case 0:
                        addEnemy107();
                        break;
                    case 1:
                        addEnemy108();
                        break;
                    case 2:
                        addEnemy109();
                        break;
                    case 3:
                        addEnemy110();
                        break;
                    default: addEnemyDefault();
                }
                break;
            case 2:
                switch(dy){
                    case -3:
                        addEnemy111();
                        break;
                    case -2:
                        addEnemy112();
                        break;
                    case -1:
                        addEnemy113();
                        break;
                    case 0:
                        addEnemy114();
                        break;
                    case 1:
                        addEnemy115();
                        break;
                    case 2:
                        addEnemy116();
                        break;
                    case 3:
                        addEnemy117();
                        break;
                    default: addEnemyDefault();
                }
                break;
            case 3:
                switch(dy){
                    case -3:
                        addEnemy118();
                        break;
                    case -2:
                        addEnemy119();
                        break;
                    case -1:
                        addEnemy120();
                        break;
                    case 0:
                        addEnemy121();
                        break;
                    case 1:
                        addEnemy122();
                        break;
                    case 2:
                        addEnemy123();
                        break;
                    case 3:
                        addEnemy124();
                        break;
                    default: addEnemyDefault();
                }
                break;
            default: addEnemyDefault();
        }
    }

    static void addEnemy76(){ // (-3, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 13){
            MicroManager.microInfo0.minDistanceToEnemy = 13;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 8){
            MicroManager.microInfo1.minDistanceToEnemy = 8;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 13){
            MicroManager.microInfo2.minDistanceToEnemy = 13;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 20){
            MicroManager.microInfo3.minDistanceToEnemy = 20;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 25){
            MicroManager.microInfo4.minDistanceToEnemy = 25;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 32){
            MicroManager.microInfo5.minDistanceToEnemy = 32;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 25){
            MicroManager.microInfo6.minDistanceToEnemy = 25;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 20){
            MicroManager.microInfo7.minDistanceToEnemy = 20;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 18){
            MicroManager.microInfo8.minDistanceToEnemy = 18;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy77(){ // (-3, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 10){
            MicroManager.microInfo0.minDistanceToEnemy = 10;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 8){
            MicroManager.microInfo2.minDistanceToEnemy = 8;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 13){
            MicroManager.microInfo3.minDistanceToEnemy = 13;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 18){
            MicroManager.microInfo4.minDistanceToEnemy = 18;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 25){
            MicroManager.microInfo5.minDistanceToEnemy = 25;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 20){
            MicroManager.microInfo6.minDistanceToEnemy = 20;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 17){
            MicroManager.microInfo7.minDistanceToEnemy = 17;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy78(){ // (-3, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 9){
            MicroManager.microInfo0.minDistanceToEnemy = 9;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 4){
            MicroManager.microInfo1.minDistanceToEnemy = 4;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 8){
            MicroManager.microInfo3.minDistanceToEnemy = 8;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 13){
            MicroManager.microInfo4.minDistanceToEnemy = 13;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 20){
            MicroManager.microInfo5.minDistanceToEnemy = 20;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 17){
            MicroManager.microInfo6.minDistanceToEnemy = 17;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 16){
            MicroManager.microInfo7.minDistanceToEnemy = 16;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy79(){ // (-3, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 10){
            MicroManager.microInfo0.minDistanceToEnemy = 10;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 4){
            MicroManager.microInfo2.minDistanceToEnemy = 4;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 10){
            MicroManager.microInfo4.minDistanceToEnemy = 10;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 17){
            MicroManager.microInfo5.minDistanceToEnemy = 17;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 16){
            MicroManager.microInfo6.minDistanceToEnemy = 16;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 17){
            MicroManager.microInfo7.minDistanceToEnemy = 17;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 9){
            MicroManager.microInfo8.minDistanceToEnemy = 9;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy80(){ // (-3, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 13){
            MicroManager.microInfo0.minDistanceToEnemy = 13;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 8){
            MicroManager.microInfo1.minDistanceToEnemy = 8;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 4){
            MicroManager.microInfo3.minDistanceToEnemy = 4;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 9){
            MicroManager.microInfo4.minDistanceToEnemy = 9;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 16){
            MicroManager.microInfo5.minDistanceToEnemy = 16;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 17){
            MicroManager.microInfo6.minDistanceToEnemy = 17;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 20){
            MicroManager.microInfo7.minDistanceToEnemy = 20;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy81(){ // (-3, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 18){
            MicroManager.microInfo0.minDistanceToEnemy = 18;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 13){
            MicroManager.microInfo1.minDistanceToEnemy = 13;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 8){
            MicroManager.microInfo2.minDistanceToEnemy = 8;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 10){
            MicroManager.microInfo4.minDistanceToEnemy = 10;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 17){
            MicroManager.microInfo5.minDistanceToEnemy = 17;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 20){
            MicroManager.microInfo6.minDistanceToEnemy = 20;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 25){
            MicroManager.microInfo7.minDistanceToEnemy = 25;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy82(){ // (-3, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 25){
            MicroManager.microInfo0.minDistanceToEnemy = 25;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 20){
            MicroManager.microInfo1.minDistanceToEnemy = 20;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 13){
            MicroManager.microInfo2.minDistanceToEnemy = 13;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 8){
            MicroManager.microInfo3.minDistanceToEnemy = 8;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 13){
            MicroManager.microInfo4.minDistanceToEnemy = 13;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 20){
            MicroManager.microInfo5.minDistanceToEnemy = 20;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 25){
            MicroManager.microInfo6.minDistanceToEnemy = 25;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 32){
            MicroManager.microInfo7.minDistanceToEnemy = 32;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 18){
            MicroManager.microInfo8.minDistanceToEnemy = 18;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy83(){ // (-2, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 8){
            MicroManager.microInfo0.minDistanceToEnemy = 8;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 10){
            MicroManager.microInfo2.minDistanceToEnemy = 10;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 17){
            MicroManager.microInfo3.minDistanceToEnemy = 17;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 20){
            MicroManager.microInfo4.minDistanceToEnemy = 20;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 25){
            MicroManager.microInfo5.minDistanceToEnemy = 25;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 18){
            MicroManager.microInfo6.minDistanceToEnemy = 18;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 13){
            MicroManager.microInfo7.minDistanceToEnemy = 13;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy84(){ // (-2, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 2){
            MicroManager.microInfo1.minDistanceToEnemy = 2;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 10){
            MicroManager.microInfo3.minDistanceToEnemy = 10;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 13){
            MicroManager.microInfo4.minDistanceToEnemy = 13;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 18){
            MicroManager.microInfo5.minDistanceToEnemy = 18;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 13){
            MicroManager.microInfo6.minDistanceToEnemy = 13;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 10){
            MicroManager.microInfo7.minDistanceToEnemy = 10;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 8){
            MicroManager.microInfo8.minDistanceToEnemy = 8;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy85(){ // (-2, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 4){
            MicroManager.microInfo0.minDistanceToEnemy = 4;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 1){
            MicroManager.microInfo1.minDistanceToEnemy = 1;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 2){
            MicroManager.microInfo2.minDistanceToEnemy = 2;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 8){
            MicroManager.microInfo4.minDistanceToEnemy = 8;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 13){
            MicroManager.microInfo5.minDistanceToEnemy = 13;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 10){
            MicroManager.microInfo6.minDistanceToEnemy = 10;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 9){
            MicroManager.microInfo7.minDistanceToEnemy = 9;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy86(){ // (-2, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 2){
            MicroManager.microInfo1.minDistanceToEnemy = 2;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 1){
            MicroManager.microInfo2.minDistanceToEnemy = 1;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 2){
            MicroManager.microInfo3.minDistanceToEnemy = 2;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 10){
            MicroManager.microInfo5.minDistanceToEnemy = 10;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 9){
            MicroManager.microInfo6.minDistanceToEnemy = 9;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 10){
            MicroManager.microInfo7.minDistanceToEnemy = 10;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 4){
            MicroManager.microInfo8.minDistanceToEnemy = 4;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy87(){ // (-2, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 8){
            MicroManager.microInfo0.minDistanceToEnemy = 8;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 2){
            MicroManager.microInfo2.minDistanceToEnemy = 2;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 1){
            MicroManager.microInfo3.minDistanceToEnemy = 1;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 4){
            MicroManager.microInfo4.minDistanceToEnemy = 4;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 9){
            MicroManager.microInfo5.minDistanceToEnemy = 9;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 10){
            MicroManager.microInfo6.minDistanceToEnemy = 10;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 13){
            MicroManager.microInfo7.minDistanceToEnemy = 13;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy88(){ // (-2, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 13){
            MicroManager.microInfo0.minDistanceToEnemy = 13;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 10){
            MicroManager.microInfo1.minDistanceToEnemy = 10;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 2){
            MicroManager.microInfo3.minDistanceToEnemy = 2;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 10){
            MicroManager.microInfo5.minDistanceToEnemy = 10;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 13){
            MicroManager.microInfo6.minDistanceToEnemy = 13;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 18){
            MicroManager.microInfo7.minDistanceToEnemy = 18;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 8){
            MicroManager.microInfo8.minDistanceToEnemy = 8;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy89(){ // (-2, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 20){
            MicroManager.microInfo0.minDistanceToEnemy = 20;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 17){
            MicroManager.microInfo1.minDistanceToEnemy = 17;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 10){
            MicroManager.microInfo2.minDistanceToEnemy = 10;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 8){
            MicroManager.microInfo4.minDistanceToEnemy = 8;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 13){
            MicroManager.microInfo5.minDistanceToEnemy = 13;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 18){
            MicroManager.microInfo6.minDistanceToEnemy = 18;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 25){
            MicroManager.microInfo7.minDistanceToEnemy = 25;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy90(){ // (-1, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 4){
            MicroManager.microInfo1.minDistanceToEnemy = 4;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 9){
            MicroManager.microInfo2.minDistanceToEnemy = 9;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 16){
            MicroManager.microInfo3.minDistanceToEnemy = 16;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 17){
            MicroManager.microInfo4.minDistanceToEnemy = 17;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 20){
            MicroManager.microInfo5.minDistanceToEnemy = 20;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 13){
            MicroManager.microInfo6.minDistanceToEnemy = 13;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 8){
            MicroManager.microInfo7.minDistanceToEnemy = 8;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy91(){ // (-1, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 2){
            MicroManager.microInfo0.minDistanceToEnemy = 2;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 1){
            MicroManager.microInfo1.minDistanceToEnemy = 1;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 4){
            MicroManager.microInfo2.minDistanceToEnemy = 4;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 9){
            MicroManager.microInfo3.minDistanceToEnemy = 9;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 10){
            MicroManager.microInfo4.minDistanceToEnemy = 10;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 13){
            MicroManager.microInfo5.minDistanceToEnemy = 13;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 8){
            MicroManager.microInfo6.minDistanceToEnemy = 8;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy92(){ // (-1, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 1){
            MicroManager.microInfo0.minDistanceToEnemy = 1;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 0){
            MicroManager.microInfo1.minDistanceToEnemy = 0;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 1){
            MicroManager.microInfo2.minDistanceToEnemy = 1;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 4){
            MicroManager.microInfo3.minDistanceToEnemy = 4;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 8){
            MicroManager.microInfo5.minDistanceToEnemy = 8;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 4){
            MicroManager.microInfo7.minDistanceToEnemy = 4;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 2){
            MicroManager.microInfo8.minDistanceToEnemy = 2;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy93(){ // (-1, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 2){
            MicroManager.microInfo0.minDistanceToEnemy = 2;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 1){
            MicroManager.microInfo1.minDistanceToEnemy = 1;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 0){
            MicroManager.microInfo2.minDistanceToEnemy = 0;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 1){
            MicroManager.microInfo3.minDistanceToEnemy = 1;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 2){
            MicroManager.microInfo4.minDistanceToEnemy = 2;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 4){
            MicroManager.microInfo6.minDistanceToEnemy = 4;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 1){
            MicroManager.microInfo8.minDistanceToEnemy = 1;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy94(){ // (-1, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 4){
            MicroManager.microInfo1.minDistanceToEnemy = 4;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 1){
            MicroManager.microInfo2.minDistanceToEnemy = 1;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 0){
            MicroManager.microInfo3.minDistanceToEnemy = 0;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 1){
            MicroManager.microInfo4.minDistanceToEnemy = 1;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 4){
            MicroManager.microInfo5.minDistanceToEnemy = 4;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 8){
            MicroManager.microInfo7.minDistanceToEnemy = 8;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 2){
            MicroManager.microInfo8.minDistanceToEnemy = 2;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy95(){ // (-1, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 10){
            MicroManager.microInfo0.minDistanceToEnemy = 10;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 9){
            MicroManager.microInfo1.minDistanceToEnemy = 9;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 4){
            MicroManager.microInfo2.minDistanceToEnemy = 4;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 1){
            MicroManager.microInfo3.minDistanceToEnemy = 1;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 2){
            MicroManager.microInfo4.minDistanceToEnemy = 2;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 8){
            MicroManager.microInfo6.minDistanceToEnemy = 8;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 13){
            MicroManager.microInfo7.minDistanceToEnemy = 13;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy96(){ // (-1, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 17){
            MicroManager.microInfo0.minDistanceToEnemy = 17;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 16){
            MicroManager.microInfo1.minDistanceToEnemy = 16;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 9){
            MicroManager.microInfo2.minDistanceToEnemy = 9;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 4){
            MicroManager.microInfo3.minDistanceToEnemy = 4;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 8){
            MicroManager.microInfo5.minDistanceToEnemy = 8;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 13){
            MicroManager.microInfo6.minDistanceToEnemy = 13;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 20){
            MicroManager.microInfo7.minDistanceToEnemy = 20;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy97(){ // (0, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 4){
            MicroManager.microInfo0.minDistanceToEnemy = 4;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 10){
            MicroManager.microInfo2.minDistanceToEnemy = 10;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 17){
            MicroManager.microInfo3.minDistanceToEnemy = 17;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 16){
            MicroManager.microInfo4.minDistanceToEnemy = 16;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 17){
            MicroManager.microInfo5.minDistanceToEnemy = 17;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 10){
            MicroManager.microInfo6.minDistanceToEnemy = 10;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 9){
            MicroManager.microInfo8.minDistanceToEnemy = 9;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy98(){ // (0, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 1){
            MicroManager.microInfo0.minDistanceToEnemy = 1;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 2){
            MicroManager.microInfo1.minDistanceToEnemy = 2;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 10){
            MicroManager.microInfo3.minDistanceToEnemy = 10;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 9){
            MicroManager.microInfo4.minDistanceToEnemy = 9;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 10){
            MicroManager.microInfo5.minDistanceToEnemy = 10;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 2){
            MicroManager.microInfo7.minDistanceToEnemy = 2;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 4){
            MicroManager.microInfo8.minDistanceToEnemy = 4;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy99(){ // (0, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 0){
            MicroManager.microInfo0.minDistanceToEnemy = 0;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 1){
            MicroManager.microInfo1.minDistanceToEnemy = 1;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 2){
            MicroManager.microInfo2.minDistanceToEnemy = 2;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 4){
            MicroManager.microInfo4.minDistanceToEnemy = 4;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 2){
            MicroManager.microInfo6.minDistanceToEnemy = 2;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 1){
            MicroManager.microInfo7.minDistanceToEnemy = 1;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 1){
            MicroManager.microInfo8.minDistanceToEnemy = 1;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy100(){ // (0, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 1){
            MicroManager.microInfo0.minDistanceToEnemy = 1;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 2){
            MicroManager.microInfo1.minDistanceToEnemy = 2;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 1){
            MicroManager.microInfo2.minDistanceToEnemy = 1;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 2){
            MicroManager.microInfo3.minDistanceToEnemy = 2;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 1){
            MicroManager.microInfo4.minDistanceToEnemy = 1;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 2){
            MicroManager.microInfo5.minDistanceToEnemy = 2;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 1){
            MicroManager.microInfo6.minDistanceToEnemy = 1;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 2){
            MicroManager.microInfo7.minDistanceToEnemy = 2;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 0){
            MicroManager.microInfo8.minDistanceToEnemy = 0;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo1.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy101(){ // (0, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 4){
            MicroManager.microInfo0.minDistanceToEnemy = 4;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 2){
            MicroManager.microInfo2.minDistanceToEnemy = 2;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 1){
            MicroManager.microInfo3.minDistanceToEnemy = 1;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 0){
            MicroManager.microInfo4.minDistanceToEnemy = 0;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 1){
            MicroManager.microInfo5.minDistanceToEnemy = 1;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 2){
            MicroManager.microInfo6.minDistanceToEnemy = 2;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 1){
            MicroManager.microInfo8.minDistanceToEnemy = 1;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo2.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy102(){ // (0, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 9){
            MicroManager.microInfo0.minDistanceToEnemy = 9;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 10){
            MicroManager.microInfo1.minDistanceToEnemy = 10;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 2){
            MicroManager.microInfo3.minDistanceToEnemy = 2;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 1){
            MicroManager.microInfo4.minDistanceToEnemy = 1;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 2){
            MicroManager.microInfo5.minDistanceToEnemy = 2;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 10){
            MicroManager.microInfo7.minDistanceToEnemy = 10;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 4){
            MicroManager.microInfo8.minDistanceToEnemy = 4;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo3.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy103(){ // (0, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 16){
            MicroManager.microInfo0.minDistanceToEnemy = 16;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 17){
            MicroManager.microInfo1.minDistanceToEnemy = 17;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 10){
            MicroManager.microInfo2.minDistanceToEnemy = 10;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 4){
            MicroManager.microInfo4.minDistanceToEnemy = 4;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 10){
            MicroManager.microInfo6.minDistanceToEnemy = 10;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 17){
            MicroManager.microInfo7.minDistanceToEnemy = 17;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 9){
            MicroManager.microInfo8.minDistanceToEnemy = 9;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy104(){ // (1, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 8){
            MicroManager.microInfo1.minDistanceToEnemy = 8;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 13){
            MicroManager.microInfo2.minDistanceToEnemy = 13;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 20){
            MicroManager.microInfo3.minDistanceToEnemy = 20;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 17){
            MicroManager.microInfo4.minDistanceToEnemy = 17;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 16){
            MicroManager.microInfo5.minDistanceToEnemy = 16;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 9){
            MicroManager.microInfo6.minDistanceToEnemy = 9;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 4){
            MicroManager.microInfo7.minDistanceToEnemy = 4;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy105(){ // (1, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 2){
            MicroManager.microInfo0.minDistanceToEnemy = 2;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 8){
            MicroManager.microInfo2.minDistanceToEnemy = 8;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 13){
            MicroManager.microInfo3.minDistanceToEnemy = 13;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 10){
            MicroManager.microInfo4.minDistanceToEnemy = 10;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 9){
            MicroManager.microInfo5.minDistanceToEnemy = 9;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 4){
            MicroManager.microInfo6.minDistanceToEnemy = 4;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 1){
            MicroManager.microInfo7.minDistanceToEnemy = 1;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy106(){ // (1, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 1){
            MicroManager.microInfo0.minDistanceToEnemy = 1;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 4){
            MicroManager.microInfo1.minDistanceToEnemy = 4;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 8){
            MicroManager.microInfo3.minDistanceToEnemy = 8;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 4){
            MicroManager.microInfo5.minDistanceToEnemy = 4;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 1){
            MicroManager.microInfo6.minDistanceToEnemy = 1;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 0){
            MicroManager.microInfo7.minDistanceToEnemy = 0;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 2){
            MicroManager.microInfo8.minDistanceToEnemy = 2;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy107(){ // (1, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 2){
            MicroManager.microInfo0.minDistanceToEnemy = 2;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 5){
            MicroManager.microInfo1.minDistanceToEnemy = 5;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 4){
            MicroManager.microInfo2.minDistanceToEnemy = 4;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 2){
            MicroManager.microInfo4.minDistanceToEnemy = 2;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 1){
            MicroManager.microInfo5.minDistanceToEnemy = 1;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 0){
            MicroManager.microInfo6.minDistanceToEnemy = 0;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 1){
            MicroManager.microInfo7.minDistanceToEnemy = 1;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 1){
            MicroManager.microInfo8.minDistanceToEnemy = 1;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo0.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy108(){ // (1, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 8){
            MicroManager.microInfo1.minDistanceToEnemy = 8;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 5){
            MicroManager.microInfo2.minDistanceToEnemy = 5;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 4){
            MicroManager.microInfo3.minDistanceToEnemy = 4;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 1){
            MicroManager.microInfo4.minDistanceToEnemy = 1;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 0){
            MicroManager.microInfo5.minDistanceToEnemy = 0;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 1){
            MicroManager.microInfo6.minDistanceToEnemy = 1;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 4){
            MicroManager.microInfo7.minDistanceToEnemy = 4;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 2){
            MicroManager.microInfo8.minDistanceToEnemy = 2;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo1.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo1.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo1.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo1.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo1.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo8.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.WEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy109(){ // (1, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 10){
            MicroManager.microInfo0.minDistanceToEnemy = 10;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 13){
            MicroManager.microInfo1.minDistanceToEnemy = 13;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 8){
            MicroManager.microInfo2.minDistanceToEnemy = 8;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 5){
            MicroManager.microInfo3.minDistanceToEnemy = 5;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 2){
            MicroManager.microInfo4.minDistanceToEnemy = 2;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 1){
            MicroManager.microInfo5.minDistanceToEnemy = 1;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 4){
            MicroManager.microInfo6.minDistanceToEnemy = 4;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 9){
            MicroManager.microInfo7.minDistanceToEnemy = 9;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo2.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo2.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo2.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo2.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo2.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo4.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy110(){ // (1, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 17){
            MicroManager.microInfo0.minDistanceToEnemy = 17;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 20){
            MicroManager.microInfo1.minDistanceToEnemy = 20;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 13){
            MicroManager.microInfo2.minDistanceToEnemy = 13;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 8){
            MicroManager.microInfo3.minDistanceToEnemy = 8;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 4){
            MicroManager.microInfo5.minDistanceToEnemy = 4;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 9){
            MicroManager.microInfo6.minDistanceToEnemy = 9;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 16){
            MicroManager.microInfo7.minDistanceToEnemy = 16;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo3.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo3.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo3.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo3.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo3.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
        }
    }

    static void addEnemy111(){ // (2, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 8){
            MicroManager.microInfo0.minDistanceToEnemy = 8;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 13){
            MicroManager.microInfo1.minDistanceToEnemy = 13;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 18){
            MicroManager.microInfo2.minDistanceToEnemy = 18;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 25){
            MicroManager.microInfo3.minDistanceToEnemy = 25;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 20){
            MicroManager.microInfo4.minDistanceToEnemy = 20;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 17){
            MicroManager.microInfo5.minDistanceToEnemy = 17;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 10){
            MicroManager.microInfo6.minDistanceToEnemy = 10;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy112(){ // (2, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 10){
            MicroManager.microInfo1.minDistanceToEnemy = 10;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 13){
            MicroManager.microInfo2.minDistanceToEnemy = 13;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 18){
            MicroManager.microInfo3.minDistanceToEnemy = 18;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 13){
            MicroManager.microInfo4.minDistanceToEnemy = 13;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 10){
            MicroManager.microInfo5.minDistanceToEnemy = 10;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 2){
            MicroManager.microInfo7.minDistanceToEnemy = 2;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 8){
            MicroManager.microInfo8.minDistanceToEnemy = 8;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy113(){ // (2, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 4){
            MicroManager.microInfo0.minDistanceToEnemy = 4;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 9){
            MicroManager.microInfo1.minDistanceToEnemy = 9;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 10){
            MicroManager.microInfo2.minDistanceToEnemy = 10;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 13){
            MicroManager.microInfo3.minDistanceToEnemy = 13;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 8){
            MicroManager.microInfo4.minDistanceToEnemy = 8;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 2){
            MicroManager.microInfo6.minDistanceToEnemy = 2;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 1){
            MicroManager.microInfo7.minDistanceToEnemy = 1;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy114(){ // (2, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 5){
            MicroManager.microInfo0.minDistanceToEnemy = 5;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 10){
            MicroManager.microInfo1.minDistanceToEnemy = 10;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 9){
            MicroManager.microInfo2.minDistanceToEnemy = 9;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 10){
            MicroManager.microInfo3.minDistanceToEnemy = 10;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 2){
            MicroManager.microInfo5.minDistanceToEnemy = 2;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 1){
            MicroManager.microInfo6.minDistanceToEnemy = 1;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 2){
            MicroManager.microInfo7.minDistanceToEnemy = 2;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 4){
            MicroManager.microInfo8.minDistanceToEnemy = 4;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo7.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy115(){ // (2, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 8){
            MicroManager.microInfo0.minDistanceToEnemy = 8;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 13){
            MicroManager.microInfo1.minDistanceToEnemy = 13;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 10){
            MicroManager.microInfo2.minDistanceToEnemy = 10;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 9){
            MicroManager.microInfo3.minDistanceToEnemy = 9;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 4){
            MicroManager.microInfo4.minDistanceToEnemy = 4;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 1){
            MicroManager.microInfo5.minDistanceToEnemy = 1;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 2){
            MicroManager.microInfo6.minDistanceToEnemy = 2;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 5){
            MicroManager.microInfo8.minDistanceToEnemy = 5;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo0.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo0.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo0.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo0.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo0.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo6.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy116(){ // (2, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 13){
            MicroManager.microInfo0.minDistanceToEnemy = 13;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 18){
            MicroManager.microInfo1.minDistanceToEnemy = 18;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 13){
            MicroManager.microInfo2.minDistanceToEnemy = 13;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 10){
            MicroManager.microInfo3.minDistanceToEnemy = 10;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 5){
            MicroManager.microInfo4.minDistanceToEnemy = 5;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 2){
            MicroManager.microInfo5.minDistanceToEnemy = 2;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 10){
            MicroManager.microInfo7.minDistanceToEnemy = 10;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 8){
            MicroManager.microInfo8.minDistanceToEnemy = 8;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                MicroManager.microInfo5.enemiesInRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthAtkRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthAtkRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthAtkRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthAtkRange = MicroManager.enemyHealth;
                MicroManager.microInfo8.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo8.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo8.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo8.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo8.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy117(){ // (2, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 20){
            MicroManager.microInfo0.minDistanceToEnemy = 20;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 25){
            MicroManager.microInfo1.minDistanceToEnemy = 25;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 18){
            MicroManager.microInfo2.minDistanceToEnemy = 18;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 13){
            MicroManager.microInfo3.minDistanceToEnemy = 13;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 8){
            MicroManager.microInfo4.minDistanceToEnemy = 8;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 10){
            MicroManager.microInfo6.minDistanceToEnemy = 10;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 17){
            MicroManager.microInfo7.minDistanceToEnemy = 17;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo4.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo4.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo4.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo4.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo4.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy118(){ // (3, -3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 13){
            MicroManager.microInfo0.minDistanceToEnemy = 13;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 20){
            MicroManager.microInfo1.minDistanceToEnemy = 20;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 25){
            MicroManager.microInfo2.minDistanceToEnemy = 25;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 32){
            MicroManager.microInfo3.minDistanceToEnemy = 32;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 25){
            MicroManager.microInfo4.minDistanceToEnemy = 25;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 20){
            MicroManager.microInfo5.minDistanceToEnemy = 20;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 13){
            MicroManager.microInfo6.minDistanceToEnemy = 13;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 8){
            MicroManager.microInfo7.minDistanceToEnemy = 8;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 18){
            MicroManager.microInfo8.minDistanceToEnemy = 18;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy119(){ // (3, -2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 10){
            MicroManager.microInfo0.minDistanceToEnemy = 10;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 17){
            MicroManager.microInfo1.minDistanceToEnemy = 17;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 20){
            MicroManager.microInfo2.minDistanceToEnemy = 20;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 25){
            MicroManager.microInfo3.minDistanceToEnemy = 25;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 18){
            MicroManager.microInfo4.minDistanceToEnemy = 18;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 13){
            MicroManager.microInfo5.minDistanceToEnemy = 13;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 8){
            MicroManager.microInfo6.minDistanceToEnemy = 8;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                break;
            case Direction.EAST:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy120(){ // (3, -1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 9){
            MicroManager.microInfo0.minDistanceToEnemy = 9;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 16){
            MicroManager.microInfo1.minDistanceToEnemy = 16;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 17){
            MicroManager.microInfo2.minDistanceToEnemy = 17;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 20){
            MicroManager.microInfo3.minDistanceToEnemy = 20;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 13){
            MicroManager.microInfo4.minDistanceToEnemy = 13;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 8){
            MicroManager.microInfo5.minDistanceToEnemy = 8;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 4){
            MicroManager.microInfo7.minDistanceToEnemy = 4;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy121(){ // (3, 0)
        if (MicroManager.microInfo0.minDistanceToEnemy > 10){
            MicroManager.microInfo0.minDistanceToEnemy = 10;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 17){
            MicroManager.microInfo1.minDistanceToEnemy = 17;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 16){
            MicroManager.microInfo2.minDistanceToEnemy = 16;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 17){
            MicroManager.microInfo3.minDistanceToEnemy = 17;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 10){
            MicroManager.microInfo4.minDistanceToEnemy = 10;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 4){
            MicroManager.microInfo6.minDistanceToEnemy = 4;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 5){
            MicroManager.microInfo7.minDistanceToEnemy = 5;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 9){
            MicroManager.microInfo8.minDistanceToEnemy = 9;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy122(){ // (3, 1)
        if (MicroManager.microInfo0.minDistanceToEnemy > 13){
            MicroManager.microInfo0.minDistanceToEnemy = 13;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 20){
            MicroManager.microInfo1.minDistanceToEnemy = 20;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 17){
            MicroManager.microInfo2.minDistanceToEnemy = 17;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 16){
            MicroManager.microInfo3.minDistanceToEnemy = 16;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 9){
            MicroManager.microInfo4.minDistanceToEnemy = 9;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 4){
            MicroManager.microInfo5.minDistanceToEnemy = 4;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 5){
            MicroManager.microInfo6.minDistanceToEnemy = 5;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 8){
            MicroManager.microInfo7.minDistanceToEnemy = 8;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 10){
            MicroManager.microInfo8.minDistanceToEnemy = 10;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo7.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo7.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo7.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo7.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo7.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy123(){ // (3, 2)
        if (MicroManager.microInfo0.minDistanceToEnemy > 18){
            MicroManager.microInfo0.minDistanceToEnemy = 18;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 25){
            MicroManager.microInfo1.minDistanceToEnemy = 25;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 20){
            MicroManager.microInfo2.minDistanceToEnemy = 20;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 17){
            MicroManager.microInfo3.minDistanceToEnemy = 17;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 10){
            MicroManager.microInfo4.minDistanceToEnemy = 10;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 5){
            MicroManager.microInfo5.minDistanceToEnemy = 5;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 8){
            MicroManager.microInfo6.minDistanceToEnemy = 8;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 13){
            MicroManager.microInfo7.minDistanceToEnemy = 13;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 13){
            MicroManager.microInfo8.minDistanceToEnemy = 13;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                MicroManager.microInfo6.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo6.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo6.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo6.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo6.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }

    static void addEnemy124(){ // (3, 3)
        if (MicroManager.microInfo0.minDistanceToEnemy > 25){
            MicroManager.microInfo0.minDistanceToEnemy = 25;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistanceToEnemy > 32){
            MicroManager.microInfo1.minDistanceToEnemy = 32;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistanceToEnemy > 25){
            MicroManager.microInfo2.minDistanceToEnemy = 25;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistanceToEnemy > 20){
            MicroManager.microInfo3.minDistanceToEnemy = 20;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistanceToEnemy > 13){
            MicroManager.microInfo4.minDistanceToEnemy = 13;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistanceToEnemy > 8){
            MicroManager.microInfo5.minDistanceToEnemy = 8;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistanceToEnemy > 13){
            MicroManager.microInfo6.minDistanceToEnemy = 13;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistanceToEnemy > 20){
            MicroManager.microInfo7.minDistanceToEnemy = 20;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistanceToEnemy > 18){
            MicroManager.microInfo8.minDistanceToEnemy = 18;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
        switch(MicroManager.unitDir){
            case Direction.NORTH:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.NORTHEAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.EAST:
                MicroManager.microInfo5.enemiesInMoveRange += MicroManager.unitVal;
                if (MicroManager.microInfo5.minHealthMoveRange > MicroManager.enemyHealth) MicroManager.microInfo5.minHealthMoveRange = MicroManager.enemyHealth;
                if (MicroManager.microInfo5.maxHealthMoveRange < MicroManager.enemyHealth) MicroManager.microInfo5.maxHealthMoveRange = MicroManager.enemyHealth;
                break;
            case Direction.SOUTHEAST:
                break;
            case Direction.SOUTH:
                break;
            case Direction.SOUTHWEST:
                break;
            case Direction.WEST:
                break;
            case Direction.NORTHWEST:
                break;
        }
    }


    static void addEnemyDefault(){
        unitDist = MicroManager.microInfo0.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo0.minDistanceToEnemy > unitDist){
            MicroManager.microInfo0.minDistanceToEnemy = unitDist;
            MicroManager.microInfo0.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo1.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo1.minDistanceToEnemy > unitDist){
            MicroManager.microInfo1.minDistanceToEnemy = unitDist;
            MicroManager.microInfo1.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo2.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo2.minDistanceToEnemy > unitDist){
            MicroManager.microInfo2.minDistanceToEnemy = unitDist;
            MicroManager.microInfo2.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo3.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo3.minDistanceToEnemy > unitDist){
            MicroManager.microInfo3.minDistanceToEnemy = unitDist;
            MicroManager.microInfo3.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo4.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo4.minDistanceToEnemy > unitDist){
            MicroManager.microInfo4.minDistanceToEnemy = unitDist;
            MicroManager.microInfo4.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo5.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo5.minDistanceToEnemy > unitDist){
            MicroManager.microInfo5.minDistanceToEnemy = unitDist;
            MicroManager.microInfo5.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo6.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo6.minDistanceToEnemy > unitDist){
            MicroManager.microInfo6.minDistanceToEnemy = unitDist;
            MicroManager.microInfo6.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo7.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo7.minDistanceToEnemy > unitDist){
            MicroManager.microInfo7.minDistanceToEnemy = unitDist;
            MicroManager.microInfo7.closestEnemy = MicroManager.unitLocation;
        }
        unitDist = MicroManager.microInfo8.location.distanceSquaredTo(MicroManager.unitLocation);
        if (MicroManager.microInfo8.minDistanceToEnemy > unitDist){
            MicroManager.microInfo8.minDistanceToEnemy = unitDist;
            MicroManager.microInfo8.closestEnemy = MicroManager.unitLocation;
        }
    }

    static void addCat(){
        dx = MovementManager.myLoc.x - MicroManager.unitLocation.x;
        dy = MovementManager.myLoc.y - MicroManager.unitLocation.y;
        switch(dx){
            case -4:
                switch(dy){
                    case -4:
                        addCat56();
                        break;
                    case -3:
                        addCat57();
                        break;
                    case -2:
                        addCat58();
                        break;
                    case -1:
                        addCat59();
                        break;
                    case 0:
                        addCat60();
                        break;
                    case 1:
                        addCat61();
                        break;
                    case 2:
                        addCat62();
                        break;
                    case 3:
                        addCat63();
                        break;
                    case 4:
                        addCat64();
                        break;
                    case 5:
                        addCat65();
                        break;
                    default: addCatDefault();
                }
                break;
            case -3:
                switch(dy){
                    case -4:
                        addCat66();
                        break;
                    case -3:
                        addCat67();
                        break;
                    case -2:
                        addCat68();
                        break;
                    case -1:
                        addCat69();
                        break;
                    case 0:
                        addCat70();
                        break;
                    case 1:
                        addCat71();
                        break;
                    case 2:
                        addCat72();
                        break;
                    case 3:
                        addCat73();
                        break;
                    case 4:
                        addCat74();
                        break;
                    case 5:
                        addCat75();
                        break;
                    default: addCatDefault();
                }
                break;
            case -2:
                switch(dy){
                    case -4:
                        addCat76();
                        break;
                    case -3:
                        addCat77();
                        break;
                    case -2:
                        addCat78();
                        break;
                    case -1:
                        addCat79();
                        break;
                    case 0:
                        addCat80();
                        break;
                    case 1:
                        addCat81();
                        break;
                    case 2:
                        addCat82();
                        break;
                    case 3:
                        addCat83();
                        break;
                    case 4:
                        addCat84();
                        break;
                    case 5:
                        addCat85();
                        break;
                    default: addCatDefault();
                }
                break;
            case -1:
                switch(dy){
                    case -4:
                        addCat86();
                        break;
                    case -3:
                        addCat87();
                        break;
                    case -2:
                        addCat88();
                        break;
                    case -1:
                        addCat89();
                        break;
                    case 0:
                        addCat90();
                        break;
                    case 1:
                        addCat91();
                        break;
                    case 2:
                        addCat92();
                        break;
                    case 3:
                        addCat93();
                        break;
                    case 4:
                        addCat94();
                        break;
                    case 5:
                        addCat95();
                        break;
                    default: addCatDefault();
                }
                break;
            case 0:
                switch(dy){
                    case -4:
                        addCat96();
                        break;
                    case -3:
                        addCat97();
                        break;
                    case -2:
                        addCat98();
                        break;
                    case -1:
                        addCat99();
                        break;
                    case 0:
                        addCat100();
                        break;
                    case 1:
                        addCat101();
                        break;
                    case 2:
                        addCat102();
                        break;
                    case 3:
                        addCat103();
                        break;
                    case 4:
                        addCat104();
                        break;
                    case 5:
                        addCat105();
                        break;
                    default: addCatDefault();
                }
                break;
            case 1:
                switch(dy){
                    case -4:
                        addCat106();
                        break;
                    case -3:
                        addCat107();
                        break;
                    case -2:
                        addCat108();
                        break;
                    case -1:
                        addCat109();
                        break;
                    case 0:
                        addCat110();
                        break;
                    case 1:
                        addCat111();
                        break;
                    case 2:
                        addCat112();
                        break;
                    case 3:
                        addCat113();
                        break;
                    case 4:
                        addCat114();
                        break;
                    case 5:
                        addCat115();
                        break;
                    default: addCatDefault();
                }
                break;
            case 2:
                switch(dy){
                    case -4:
                        addCat116();
                        break;
                    case -3:
                        addCat117();
                        break;
                    case -2:
                        addCat118();
                        break;
                    case -1:
                        addCat119();
                        break;
                    case 0:
                        addCat120();
                        break;
                    case 1:
                        addCat121();
                        break;
                    case 2:
                        addCat122();
                        break;
                    case 3:
                        addCat123();
                        break;
                    case 4:
                        addCat124();
                        break;
                    case 5:
                        addCat125();
                        break;
                    default: addCatDefault();
                }
                break;
            case 3:
                switch(dy){
                    case -4:
                        addCat126();
                        break;
                    case -3:
                        addCat127();
                        break;
                    case -2:
                        addCat128();
                        break;
                    case -1:
                        addCat129();
                        break;
                    case 0:
                        addCat130();
                        break;
                    case 1:
                        addCat131();
                        break;
                    case 2:
                        addCat132();
                        break;
                    case 3:
                        addCat133();
                        break;
                    case 4:
                        addCat134();
                        break;
                    case 5:
                        addCat135();
                        break;
                    default: addCatDefault();
                }
                break;
            case 4:
                switch(dy){
                    case -4:
                        addCat136();
                        break;
                    case -3:
                        addCat137();
                        break;
                    case -2:
                        addCat138();
                        break;
                    case -1:
                        addCat139();
                        break;
                    case 0:
                        addCat140();
                        break;
                    case 1:
                        addCat141();
                        break;
                    case 2:
                        addCat142();
                        break;
                    case 3:
                        addCat143();
                        break;
                    case 4:
                        addCat144();
                        break;
                    case 5:
                        addCat145();
                        break;
                    default: addCatDefault();
                }
                break;
            case 5:
                switch(dy){
                    case -4:
                        addCat146();
                        break;
                    case -3:
                        addCat147();
                        break;
                    case -2:
                        addCat148();
                        break;
                    case -1:
                        addCat149();
                        break;
                    case 0:
                        addCat150();
                        break;
                    case 1:
                        addCat151();
                        break;
                    case 2:
                        addCat152();
                        break;
                    case 3:
                        addCat153();
                        break;
                    case 4:
                        addCat154();
                        break;
                    case 5:
                        addCat155();
                        break;
                    default: addCatDefault();
                }
                break;
            default: addCatDefault();
        }
    }
    static void addCat56(){ // (-4, -4)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 98){
            MicroManager.microInfo1.minDistToCat = 98;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 170){
            MicroManager.microInfo3.minDistToCat = 170;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 202){
            MicroManager.microInfo4.minDistToCat = 202;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 242){
            MicroManager.microInfo5.minDistToCat = 242;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 202){
            MicroManager.microInfo6.minDistToCat = 202;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 170){
            MicroManager.microInfo7.minDistToCat = 170;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 162){
            MicroManager.microInfo8.minDistToCat = 162;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat57(){ // (-4, -3)
        if (MicroManager.microInfo0.minDistToCat > 106){
            MicroManager.microInfo0.minDistToCat = 106;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 98){
            MicroManager.microInfo2.minDistToCat = 98;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 162){
            MicroManager.microInfo4.minDistToCat = 162;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 202){
            MicroManager.microInfo5.minDistToCat = 202;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 170){
            MicroManager.microInfo6.minDistToCat = 170;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 146){
            MicroManager.microInfo7.minDistToCat = 146;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat58(){ // (-4, -2)
        if (MicroManager.microInfo0.minDistToCat > 90){
            MicroManager.microInfo0.minDistToCat = 90;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 98){
            MicroManager.microInfo3.minDistToCat = 98;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 170){
            MicroManager.microInfo5.minDistToCat = 170;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 146){
            MicroManager.microInfo6.minDistToCat = 146;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat59(){ // (-4, -1)
        if (MicroManager.microInfo0.minDistToCat > 82){
            MicroManager.microInfo0.minDistToCat = 82;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 106){
            MicroManager.microInfo4.minDistToCat = 106;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 146){
            MicroManager.microInfo5.minDistToCat = 146;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 122){
            MicroManager.microInfo7.minDistToCat = 122;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat60(){ // (-4, 0)
        if (MicroManager.microInfo0.minDistToCat > 82){
            MicroManager.microInfo0.minDistToCat = 82;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 90){
            MicroManager.microInfo4.minDistToCat = 90;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 122){
            MicroManager.microInfo6.minDistToCat = 122;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 122){
            MicroManager.microInfo7.minDistToCat = 122;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat61(){ // (-4, 1)
        if (MicroManager.microInfo0.minDistToCat > 90){
            MicroManager.microInfo0.minDistToCat = 90;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 82){
            MicroManager.microInfo4.minDistToCat = 82;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 122){
            MicroManager.microInfo5.minDistToCat = 122;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 122){
            MicroManager.microInfo6.minDistToCat = 122;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat62(){ // (-4, 2)
        if (MicroManager.microInfo0.minDistToCat > 106){
            MicroManager.microInfo0.minDistToCat = 106;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 82){
            MicroManager.microInfo4.minDistToCat = 82;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 122){
            MicroManager.microInfo5.minDistToCat = 122;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 146){
            MicroManager.microInfo7.minDistToCat = 146;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat63(){ // (-4, 3)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 98){
            MicroManager.microInfo1.minDistToCat = 98;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 90){
            MicroManager.microInfo4.minDistToCat = 90;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 146){
            MicroManager.microInfo6.minDistToCat = 146;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 170){
            MicroManager.microInfo7.minDistToCat = 170;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat64(){ // (-4, 4)
        if (MicroManager.microInfo0.minDistToCat > 162){
            MicroManager.microInfo0.minDistToCat = 162;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 98){
            MicroManager.microInfo2.minDistToCat = 98;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 106){
            MicroManager.microInfo4.minDistToCat = 106;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 146){
            MicroManager.microInfo5.minDistToCat = 146;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 170){
            MicroManager.microInfo6.minDistToCat = 170;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 202){
            MicroManager.microInfo7.minDistToCat = 202;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat65(){ // (-4, 5)
        if (MicroManager.microInfo0.minDistToCat > 202){
            MicroManager.microInfo0.minDistToCat = 202;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 170){
            MicroManager.microInfo1.minDistToCat = 170;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 98){
            MicroManager.microInfo3.minDistToCat = 98;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 170){
            MicroManager.microInfo5.minDistToCat = 170;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 202){
            MicroManager.microInfo6.minDistToCat = 202;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 242){
            MicroManager.microInfo7.minDistToCat = 242;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 162){
            MicroManager.microInfo8.minDistToCat = 162;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat66(){ // (-3, -4)
        if (MicroManager.microInfo0.minDistToCat > 98){
            MicroManager.microInfo0.minDistToCat = 98;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 106){
            MicroManager.microInfo2.minDistToCat = 106;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 146){
            MicroManager.microInfo3.minDistToCat = 146;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 170){
            MicroManager.microInfo4.minDistToCat = 170;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 202){
            MicroManager.microInfo5.minDistToCat = 202;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 162){
            MicroManager.microInfo6.minDistToCat = 162;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat67(){ // (-3, -3)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 106){
            MicroManager.microInfo3.minDistToCat = 106;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 162){
            MicroManager.microInfo5.minDistToCat = 162;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 106){
            MicroManager.microInfo7.minDistToCat = 106;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 98){
            MicroManager.microInfo8.minDistToCat = 98;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat68(){ // (-3, -2)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 98){
            MicroManager.microInfo4.minDistToCat = 98;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 106){
            MicroManager.microInfo6.minDistToCat = 106;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 90){
            MicroManager.microInfo7.minDistToCat = 90;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat69(){ // (-3, -1)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 106){
            MicroManager.microInfo5.minDistToCat = 106;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 90){
            MicroManager.microInfo6.minDistToCat = 90;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 82){
            MicroManager.microInfo7.minDistToCat = 82;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat70(){ // (-3, 0)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 90){
            MicroManager.microInfo5.minDistToCat = 90;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 82){
            MicroManager.microInfo6.minDistToCat = 82;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 82){
            MicroManager.microInfo7.minDistToCat = 82;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat71(){ // (-3, 1)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 82){
            MicroManager.microInfo5.minDistToCat = 82;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 82){
            MicroManager.microInfo6.minDistToCat = 82;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 90){
            MicroManager.microInfo7.minDistToCat = 90;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat72(){ // (-3, 2)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 82){
            MicroManager.microInfo5.minDistToCat = 82;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 90){
            MicroManager.microInfo6.minDistToCat = 90;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 106){
            MicroManager.microInfo7.minDistToCat = 106;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat73(){ // (-3, 3)
        if (MicroManager.microInfo0.minDistToCat > 98){
            MicroManager.microInfo0.minDistToCat = 98;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 90){
            MicroManager.microInfo5.minDistToCat = 90;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 106){
            MicroManager.microInfo6.minDistToCat = 106;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat74(){ // (-3, 4)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 106){
            MicroManager.microInfo1.minDistToCat = 106;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 106){
            MicroManager.microInfo5.minDistToCat = 106;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 162){
            MicroManager.microInfo7.minDistToCat = 162;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 98){
            MicroManager.microInfo8.minDistToCat = 98;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat75(){ // (-3, 5)
        if (MicroManager.microInfo0.minDistToCat > 170){
            MicroManager.microInfo0.minDistToCat = 170;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 146){
            MicroManager.microInfo1.minDistToCat = 146;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 106){
            MicroManager.microInfo2.minDistToCat = 106;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 98){
            MicroManager.microInfo4.minDistToCat = 98;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 162){
            MicroManager.microInfo6.minDistToCat = 162;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 202){
            MicroManager.microInfo7.minDistToCat = 202;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat76(){ // (-2, -4)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 90){
            MicroManager.microInfo2.minDistToCat = 90;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 146){
            MicroManager.microInfo4.minDistToCat = 146;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 170){
            MicroManager.microInfo5.minDistToCat = 170;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 98){
            MicroManager.microInfo7.minDistToCat = 98;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat77(){ // (-2, -3)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 90){
            MicroManager.microInfo3.minDistToCat = 90;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 106){
            MicroManager.microInfo4.minDistToCat = 106;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 98){
            MicroManager.microInfo6.minDistToCat = 98;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat78(){ // (-2, -2)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 18){
            MicroManager.microInfo1.minDistToCat = 18;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 98){
            MicroManager.microInfo5.minDistToCat = 98;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat79(){ // (-2, -1)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 18){
            MicroManager.microInfo2.minDistToCat = 18;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat80(){ // (-2, 0)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 18){
            MicroManager.microInfo3.minDistToCat = 18;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat81(){ // (-2, 1)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 18){
            MicroManager.microInfo1.minDistToCat = 18;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat82(){ // (-2, 2)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 18){
            MicroManager.microInfo2.minDistToCat = 18;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat83(){ // (-2, 3)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 18){
            MicroManager.microInfo3.minDistToCat = 18;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 98){
            MicroManager.microInfo7.minDistToCat = 98;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat84(){ // (-2, 4)
        if (MicroManager.microInfo0.minDistToCat > 106){
            MicroManager.microInfo0.minDistToCat = 106;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 90){
            MicroManager.microInfo1.minDistToCat = 90;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 98){
            MicroManager.microInfo6.minDistToCat = 98;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat85(){ // (-2, 5)
        if (MicroManager.microInfo0.minDistToCat > 146){
            MicroManager.microInfo0.minDistToCat = 146;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 90){
            MicroManager.microInfo2.minDistToCat = 90;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 98){
            MicroManager.microInfo5.minDistToCat = 98;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 170){
            MicroManager.microInfo7.minDistToCat = 170;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat86(){ // (-1, -4)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 82){
            MicroManager.microInfo2.minDistToCat = 82;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 122){
            MicroManager.microInfo3.minDistToCat = 122;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 146){
            MicroManager.microInfo5.minDistToCat = 146;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 106){
            MicroManager.microInfo6.minDistToCat = 106;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat87(){ // (-1, -3)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 82){
            MicroManager.microInfo3.minDistToCat = 82;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 90){
            MicroManager.microInfo4.minDistToCat = 90;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 106){
            MicroManager.microInfo5.minDistToCat = 106;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat88(){ // (-1, -2)
        if (MicroManager.microInfo0.minDistToCat > 18){
            MicroManager.microInfo0.minDistToCat = 18;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat89(){ // (-1, -1)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 2){
            MicroManager.microInfo1.minDistToCat = 2;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 18){
            MicroManager.microInfo8.minDistToCat = 18;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat90(){ // (-1, 0)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 2){
            MicroManager.microInfo1.minDistToCat = 2;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 2){
            MicroManager.microInfo2.minDistToCat = 2;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 18){
            MicroManager.microInfo4.minDistToCat = 18;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat91(){ // (-1, 1)
        if (MicroManager.microInfo0.minDistToCat > 18){
            MicroManager.microInfo0.minDistToCat = 18;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 2){
            MicroManager.microInfo2.minDistToCat = 2;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 2){
            MicroManager.microInfo3.minDistToCat = 2;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat92(){ // (-1, 2)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 2){
            MicroManager.microInfo3.minDistToCat = 2;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 18){
            MicroManager.microInfo8.minDistToCat = 18;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat93(){ // (-1, 3)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 18){
            MicroManager.microInfo4.minDistToCat = 18;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat94(){ // (-1, 4)
        if (MicroManager.microInfo0.minDistToCat > 90){
            MicroManager.microInfo0.minDistToCat = 90;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 82){
            MicroManager.microInfo1.minDistToCat = 82;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 106){
            MicroManager.microInfo7.minDistToCat = 106;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat95(){ // (-1, 5)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 122){
            MicroManager.microInfo1.minDistToCat = 122;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 82){
            MicroManager.microInfo2.minDistToCat = 82;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 106){
            MicroManager.microInfo6.minDistToCat = 106;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 146){
            MicroManager.microInfo7.minDistToCat = 146;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat96(){ // (0, -4)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 82){
            MicroManager.microInfo2.minDistToCat = 82;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 122){
            MicroManager.microInfo3.minDistToCat = 122;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 122){
            MicroManager.microInfo4.minDistToCat = 122;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 90){
            MicroManager.microInfo6.minDistToCat = 90;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat97(){ // (0, -3)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 82){
            MicroManager.microInfo3.minDistToCat = 82;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 82){
            MicroManager.microInfo4.minDistToCat = 82;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 90){
            MicroManager.microInfo5.minDistToCat = 90;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat98(){ // (0, -2)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 18){
            MicroManager.microInfo7.minDistToCat = 18;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat99(){ // (0, -1)
        if (MicroManager.microInfo0.minDistToCat > 2){
            MicroManager.microInfo0.minDistToCat = 2;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 2){
            MicroManager.microInfo1.minDistToCat = 2;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 18){
            MicroManager.microInfo6.minDistToCat = 18;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat100(){ // (0, 0)
        if (MicroManager.microInfo0.minDistToCat > 2){
            MicroManager.microInfo0.minDistToCat = 2;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 2){
            MicroManager.microInfo1.minDistToCat = 2;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 2){
            MicroManager.microInfo2.minDistToCat = 2;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 18){
            MicroManager.microInfo5.minDistToCat = 18;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 2){
            MicroManager.microInfo8.minDistToCat = 2;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat101(){ // (0, 1)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 2){
            MicroManager.microInfo2.minDistToCat = 2;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 2){
            MicroManager.microInfo3.minDistToCat = 2;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 2){
            MicroManager.microInfo4.minDistToCat = 2;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 18){
            MicroManager.microInfo7.minDistToCat = 18;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 2){
            MicroManager.microInfo8.minDistToCat = 2;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat102(){ // (0, 2)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 2){
            MicroManager.microInfo3.minDistToCat = 2;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 2){
            MicroManager.microInfo4.minDistToCat = 2;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 18){
            MicroManager.microInfo6.minDistToCat = 18;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat103(){ // (0, 3)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 18){
            MicroManager.microInfo5.minDistToCat = 18;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat104(){ // (0, 4)
        if (MicroManager.microInfo0.minDistToCat > 82){
            MicroManager.microInfo0.minDistToCat = 82;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 82){
            MicroManager.microInfo1.minDistToCat = 82;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 90){
            MicroManager.microInfo7.minDistToCat = 90;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat105(){ // (0, 5)
        if (MicroManager.microInfo0.minDistToCat > 122){
            MicroManager.microInfo0.minDistToCat = 122;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 122){
            MicroManager.microInfo1.minDistToCat = 122;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 82){
            MicroManager.microInfo2.minDistToCat = 82;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 90){
            MicroManager.microInfo6.minDistToCat = 90;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat106(){ // (1, -4)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 90){
            MicroManager.microInfo2.minDistToCat = 90;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 122){
            MicroManager.microInfo4.minDistToCat = 122;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 122){
            MicroManager.microInfo5.minDistToCat = 122;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 82){
            MicroManager.microInfo6.minDistToCat = 82;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat107(){ // (1, -3)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 90){
            MicroManager.microInfo3.minDistToCat = 90;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 82){
            MicroManager.microInfo4.minDistToCat = 82;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 82){
            MicroManager.microInfo5.minDistToCat = 82;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat108(){ // (1, -2)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 18){
            MicroManager.microInfo1.minDistToCat = 18;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat109(){ // (1, -1)
        if (MicroManager.microInfo0.minDistToCat > 2){
            MicroManager.microInfo0.minDistToCat = 2;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 18){
            MicroManager.microInfo2.minDistToCat = 18;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 2){
            MicroManager.microInfo7.minDistToCat = 2;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat110(){ // (1, 0)
        if (MicroManager.microInfo0.minDistToCat > 2){
            MicroManager.microInfo0.minDistToCat = 2;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 10){
            MicroManager.microInfo1.minDistToCat = 10;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 18){
            MicroManager.microInfo3.minDistToCat = 18;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 2){
            MicroManager.microInfo6.minDistToCat = 2;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 2){
            MicroManager.microInfo7.minDistToCat = 2;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 2){
            MicroManager.microInfo8.minDistToCat = 2;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat111(){ // (1, 1)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 18){
            MicroManager.microInfo1.minDistToCat = 18;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 10){
            MicroManager.microInfo2.minDistToCat = 10;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 2){
            MicroManager.microInfo4.minDistToCat = 2;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 2){
            MicroManager.microInfo5.minDistToCat = 2;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 2){
            MicroManager.microInfo6.minDistToCat = 2;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 2){
            MicroManager.microInfo8.minDistToCat = 2;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat112(){ // (1, 2)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 18){
            MicroManager.microInfo2.minDistToCat = 18;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 10){
            MicroManager.microInfo3.minDistToCat = 10;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 2){
            MicroManager.microInfo4.minDistToCat = 2;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 2){
            MicroManager.microInfo5.minDistToCat = 2;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat113(){ // (1, 3)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 18){
            MicroManager.microInfo3.minDistToCat = 18;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat114(){ // (1, 4)
        if (MicroManager.microInfo0.minDistToCat > 82){
            MicroManager.microInfo0.minDistToCat = 82;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 90){
            MicroManager.microInfo1.minDistToCat = 90;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 82){
            MicroManager.microInfo7.minDistToCat = 82;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat115(){ // (1, 5)
        if (MicroManager.microInfo0.minDistToCat > 122){
            MicroManager.microInfo0.minDistToCat = 122;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 90){
            MicroManager.microInfo2.minDistToCat = 90;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 82){
            MicroManager.microInfo6.minDistToCat = 82;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 122){
            MicroManager.microInfo7.minDistToCat = 122;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat116(){ // (2, -4)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 106){
            MicroManager.microInfo2.minDistToCat = 106;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 146){
            MicroManager.microInfo3.minDistToCat = 146;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 122){
            MicroManager.microInfo5.minDistToCat = 122;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 82){
            MicroManager.microInfo6.minDistToCat = 82;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat117(){ // (2, -3)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 106){
            MicroManager.microInfo3.minDistToCat = 106;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 90){
            MicroManager.microInfo4.minDistToCat = 90;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 82){
            MicroManager.microInfo5.minDistToCat = 82;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat118(){ // (2, -2)
        if (MicroManager.microInfo0.minDistToCat > 18){
            MicroManager.microInfo0.minDistToCat = 18;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat119(){ // (2, -1)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 2){
            MicroManager.microInfo7.minDistToCat = 2;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 18){
            MicroManager.microInfo8.minDistToCat = 18;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat120(){ // (2, 0)
        if (MicroManager.microInfo0.minDistToCat > 10){
            MicroManager.microInfo0.minDistToCat = 10;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 26){
            MicroManager.microInfo1.minDistToCat = 26;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 18){
            MicroManager.microInfo4.minDistToCat = 18;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 2){
            MicroManager.microInfo6.minDistToCat = 2;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 2){
            MicroManager.microInfo7.minDistToCat = 2;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat121(){ // (2, 1)
        if (MicroManager.microInfo0.minDistToCat > 18){
            MicroManager.microInfo0.minDistToCat = 18;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 34){
            MicroManager.microInfo1.minDistToCat = 34;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 26){
            MicroManager.microInfo2.minDistToCat = 26;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 2){
            MicroManager.microInfo5.minDistToCat = 2;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 2){
            MicroManager.microInfo6.minDistToCat = 2;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 10){
            MicroManager.microInfo8.minDistToCat = 10;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat122(){ // (2, 2)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 34){
            MicroManager.microInfo2.minDistToCat = 34;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 26){
            MicroManager.microInfo3.minDistToCat = 26;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 10){
            MicroManager.microInfo4.minDistToCat = 10;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 2){
            MicroManager.microInfo5.minDistToCat = 2;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 18){
            MicroManager.microInfo8.minDistToCat = 18;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat123(){ // (2, 3)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 34){
            MicroManager.microInfo3.minDistToCat = 34;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 18){
            MicroManager.microInfo4.minDistToCat = 18;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat124(){ // (2, 4)
        if (MicroManager.microInfo0.minDistToCat > 90){
            MicroManager.microInfo0.minDistToCat = 90;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 106){
            MicroManager.microInfo1.minDistToCat = 106;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 82){
            MicroManager.microInfo7.minDistToCat = 82;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat125(){ // (2, 5)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 146){
            MicroManager.microInfo1.minDistToCat = 146;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 106){
            MicroManager.microInfo2.minDistToCat = 106;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 82){
            MicroManager.microInfo6.minDistToCat = 82;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 122){
            MicroManager.microInfo7.minDistToCat = 122;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat126(){ // (3, -4)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 98){
            MicroManager.microInfo1.minDistToCat = 98;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 170){
            MicroManager.microInfo3.minDistToCat = 170;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 146){
            MicroManager.microInfo4.minDistToCat = 146;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 90){
            MicroManager.microInfo6.minDistToCat = 90;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat127(){ // (3, -3)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 98){
            MicroManager.microInfo2.minDistToCat = 98;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 106){
            MicroManager.microInfo4.minDistToCat = 106;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 90){
            MicroManager.microInfo5.minDistToCat = 90;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat128(){ // (3, -2)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 98){
            MicroManager.microInfo3.minDistToCat = 98;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 18){
            MicroManager.microInfo7.minDistToCat = 18;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat129(){ // (3, -1)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 18){
            MicroManager.microInfo6.minDistToCat = 18;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat130(){ // (3, 0)
        if (MicroManager.microInfo0.minDistToCat > 26){
            MicroManager.microInfo0.minDistToCat = 26;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 50){
            MicroManager.microInfo1.minDistToCat = 50;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 18){
            MicroManager.microInfo5.minDistToCat = 18;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 10){
            MicroManager.microInfo7.minDistToCat = 10;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat131(){ // (3, 1)
        if (MicroManager.microInfo0.minDistToCat > 34){
            MicroManager.microInfo0.minDistToCat = 34;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 58){
            MicroManager.microInfo1.minDistToCat = 58;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 50){
            MicroManager.microInfo2.minDistToCat = 50;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 10){
            MicroManager.microInfo6.minDistToCat = 10;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 18){
            MicroManager.microInfo7.minDistToCat = 18;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 26){
            MicroManager.microInfo8.minDistToCat = 26;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat132(){ // (3, 2)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 74){
            MicroManager.microInfo1.minDistToCat = 74;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 58){
            MicroManager.microInfo2.minDistToCat = 58;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 50){
            MicroManager.microInfo3.minDistToCat = 50;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 26){
            MicroManager.microInfo4.minDistToCat = 26;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 10){
            MicroManager.microInfo5.minDistToCat = 10;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 18){
            MicroManager.microInfo6.minDistToCat = 18;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 34){
            MicroManager.microInfo8.minDistToCat = 34;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat133(){ // (3, 3)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 98){
            MicroManager.microInfo1.minDistToCat = 98;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 74){
            MicroManager.microInfo2.minDistToCat = 74;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 58){
            MicroManager.microInfo3.minDistToCat = 58;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 34){
            MicroManager.microInfo4.minDistToCat = 34;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 18){
            MicroManager.microInfo5.minDistToCat = 18;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat134(){ // (3, 4)
        if (MicroManager.microInfo0.minDistToCat > 106){
            MicroManager.microInfo0.minDistToCat = 106;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 98){
            MicroManager.microInfo2.minDistToCat = 98;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 74){
            MicroManager.microInfo3.minDistToCat = 74;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 90){
            MicroManager.microInfo7.minDistToCat = 90;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat135(){ // (3, 5)
        if (MicroManager.microInfo0.minDistToCat > 146){
            MicroManager.microInfo0.minDistToCat = 146;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 170){
            MicroManager.microInfo1.minDistToCat = 170;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 98){
            MicroManager.microInfo3.minDistToCat = 98;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 90){
            MicroManager.microInfo6.minDistToCat = 90;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat136(){ // (4, -4)
        if (MicroManager.microInfo0.minDistToCat > 98){
            MicroManager.microInfo0.minDistToCat = 98;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 162){
            MicroManager.microInfo2.minDistToCat = 162;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 202){
            MicroManager.microInfo3.minDistToCat = 202;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 170){
            MicroManager.microInfo4.minDistToCat = 170;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 146){
            MicroManager.microInfo5.minDistToCat = 146;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 106){
            MicroManager.microInfo6.minDistToCat = 106;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat137(){ // (4, -3)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 106){
            MicroManager.microInfo1.minDistToCat = 106;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 162){
            MicroManager.microInfo3.minDistToCat = 162;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 106){
            MicroManager.microInfo5.minDistToCat = 106;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 98){
            MicroManager.microInfo8.minDistToCat = 98;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat138(){ // (4, -2)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 90){
            MicroManager.microInfo1.minDistToCat = 90;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 106){
            MicroManager.microInfo2.minDistToCat = 106;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 98){
            MicroManager.microInfo4.minDistToCat = 98;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat139(){ // (4, -1)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 82){
            MicroManager.microInfo1.minDistToCat = 82;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 90){
            MicroManager.microInfo2.minDistToCat = 90;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 106){
            MicroManager.microInfo3.minDistToCat = 106;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat140(){ // (4, 0)
        if (MicroManager.microInfo0.minDistToCat > 50){
            MicroManager.microInfo0.minDistToCat = 50;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 82){
            MicroManager.microInfo1.minDistToCat = 82;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 82){
            MicroManager.microInfo2.minDistToCat = 82;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 90){
            MicroManager.microInfo3.minDistToCat = 90;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 26){
            MicroManager.microInfo7.minDistToCat = 26;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat141(){ // (4, 1)
        if (MicroManager.microInfo0.minDistToCat > 58){
            MicroManager.microInfo0.minDistToCat = 58;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 90){
            MicroManager.microInfo1.minDistToCat = 90;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 82){
            MicroManager.microInfo2.minDistToCat = 82;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 82){
            MicroManager.microInfo3.minDistToCat = 82;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 26){
            MicroManager.microInfo6.minDistToCat = 26;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 34){
            MicroManager.microInfo7.minDistToCat = 34;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 50){
            MicroManager.microInfo8.minDistToCat = 50;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat142(){ // (4, 2)
        if (MicroManager.microInfo0.minDistToCat > 74){
            MicroManager.microInfo0.minDistToCat = 74;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 106){
            MicroManager.microInfo1.minDistToCat = 106;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 90){
            MicroManager.microInfo2.minDistToCat = 90;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 82){
            MicroManager.microInfo3.minDistToCat = 82;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 50){
            MicroManager.microInfo4.minDistToCat = 50;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 26){
            MicroManager.microInfo5.minDistToCat = 26;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 34){
            MicroManager.microInfo6.minDistToCat = 34;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 58){
            MicroManager.microInfo8.minDistToCat = 58;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat143(){ // (4, 3)
        if (MicroManager.microInfo0.minDistToCat > 98){
            MicroManager.microInfo0.minDistToCat = 98;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 106){
            MicroManager.microInfo2.minDistToCat = 106;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 90){
            MicroManager.microInfo3.minDistToCat = 90;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 58){
            MicroManager.microInfo4.minDistToCat = 58;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 34){
            MicroManager.microInfo5.minDistToCat = 34;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 74){
            MicroManager.microInfo8.minDistToCat = 74;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat144(){ // (4, 4)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 162){
            MicroManager.microInfo1.minDistToCat = 162;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 106){
            MicroManager.microInfo3.minDistToCat = 106;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 74){
            MicroManager.microInfo4.minDistToCat = 74;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 106){
            MicroManager.microInfo7.minDistToCat = 106;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 98){
            MicroManager.microInfo8.minDistToCat = 98;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat145(){ // (4, 5)
        if (MicroManager.microInfo0.minDistToCat > 170){
            MicroManager.microInfo0.minDistToCat = 170;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 202){
            MicroManager.microInfo1.minDistToCat = 202;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 162){
            MicroManager.microInfo2.minDistToCat = 162;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 98){
            MicroManager.microInfo4.minDistToCat = 98;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 106){
            MicroManager.microInfo6.minDistToCat = 106;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 146){
            MicroManager.microInfo7.minDistToCat = 146;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat146(){ // (5, -4)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 170){
            MicroManager.microInfo1.minDistToCat = 170;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 202){
            MicroManager.microInfo2.minDistToCat = 202;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 242){
            MicroManager.microInfo3.minDistToCat = 242;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 202){
            MicroManager.microInfo4.minDistToCat = 202;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 170){
            MicroManager.microInfo5.minDistToCat = 170;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 98){
            MicroManager.microInfo7.minDistToCat = 98;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 162){
            MicroManager.microInfo8.minDistToCat = 162;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat147(){ // (5, -3)
        if (MicroManager.microInfo0.minDistToCat > 106){
            MicroManager.microInfo0.minDistToCat = 106;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 146){
            MicroManager.microInfo1.minDistToCat = 146;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 170){
            MicroManager.microInfo2.minDistToCat = 170;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 202){
            MicroManager.microInfo3.minDistToCat = 202;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 162){
            MicroManager.microInfo4.minDistToCat = 162;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 130){
            MicroManager.microInfo5.minDistToCat = 130;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 98){
            MicroManager.microInfo6.minDistToCat = 98;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat148(){ // (5, -2)
        if (MicroManager.microInfo0.minDistToCat > 90){
            MicroManager.microInfo0.minDistToCat = 90;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 146){
            MicroManager.microInfo2.minDistToCat = 146;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 170){
            MicroManager.microInfo3.minDistToCat = 170;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 98){
            MicroManager.microInfo5.minDistToCat = 98;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat149(){ // (5, -1)
        if (MicroManager.microInfo0.minDistToCat > 82){
            MicroManager.microInfo0.minDistToCat = 82;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 122){
            MicroManager.microInfo1.minDistToCat = 122;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 146){
            MicroManager.microInfo3.minDistToCat = 146;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 106){
            MicroManager.microInfo4.minDistToCat = 106;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat150(){ // (5, 0)
        if (MicroManager.microInfo0.minDistToCat > 82){
            MicroManager.microInfo0.minDistToCat = 82;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 122){
            MicroManager.microInfo1.minDistToCat = 122;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 122){
            MicroManager.microInfo2.minDistToCat = 122;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 90){
            MicroManager.microInfo4.minDistToCat = 90;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 50){
            MicroManager.microInfo7.minDistToCat = 50;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat151(){ // (5, 1)
        if (MicroManager.microInfo0.minDistToCat > 90){
            MicroManager.microInfo0.minDistToCat = 90;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 130){
            MicroManager.microInfo1.minDistToCat = 130;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 122){
            MicroManager.microInfo2.minDistToCat = 122;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 122){
            MicroManager.microInfo3.minDistToCat = 122;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 82){
            MicroManager.microInfo4.minDistToCat = 82;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 50){
            MicroManager.microInfo6.minDistToCat = 50;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 58){
            MicroManager.microInfo7.minDistToCat = 58;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 82){
            MicroManager.microInfo8.minDistToCat = 82;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat152(){ // (5, 2)
        if (MicroManager.microInfo0.minDistToCat > 106){
            MicroManager.microInfo0.minDistToCat = 106;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 146){
            MicroManager.microInfo1.minDistToCat = 146;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 130){
            MicroManager.microInfo2.minDistToCat = 130;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 122){
            MicroManager.microInfo3.minDistToCat = 122;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 82){
            MicroManager.microInfo4.minDistToCat = 82;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 50){
            MicroManager.microInfo5.minDistToCat = 50;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 58){
            MicroManager.microInfo6.minDistToCat = 58;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 74){
            MicroManager.microInfo7.minDistToCat = 74;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 90){
            MicroManager.microInfo8.minDistToCat = 90;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat153(){ // (5, 3)
        if (MicroManager.microInfo0.minDistToCat > 130){
            MicroManager.microInfo0.minDistToCat = 130;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 170){
            MicroManager.microInfo1.minDistToCat = 170;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 146){
            MicroManager.microInfo2.minDistToCat = 146;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 130){
            MicroManager.microInfo3.minDistToCat = 130;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 90){
            MicroManager.microInfo4.minDistToCat = 90;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 58){
            MicroManager.microInfo5.minDistToCat = 58;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 74){
            MicroManager.microInfo6.minDistToCat = 74;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 98){
            MicroManager.microInfo7.minDistToCat = 98;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 106){
            MicroManager.microInfo8.minDistToCat = 106;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat154(){ // (5, 4)
        if (MicroManager.microInfo0.minDistToCat > 162){
            MicroManager.microInfo0.minDistToCat = 162;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 202){
            MicroManager.microInfo1.minDistToCat = 202;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 170){
            MicroManager.microInfo2.minDistToCat = 170;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 146){
            MicroManager.microInfo3.minDistToCat = 146;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 106){
            MicroManager.microInfo4.minDistToCat = 106;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 74){
            MicroManager.microInfo5.minDistToCat = 74;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 98){
            MicroManager.microInfo6.minDistToCat = 98;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 130){
            MicroManager.microInfo7.minDistToCat = 130;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 130){
            MicroManager.microInfo8.minDistToCat = 130;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

    static void addCat155(){ // (5, 5)
        if (MicroManager.microInfo0.minDistToCat > 202){
            MicroManager.microInfo0.minDistToCat = 202;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo1.minDistToCat > 242){
            MicroManager.microInfo1.minDistToCat = 242;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo2.minDistToCat > 202){
            MicroManager.microInfo2.minDistToCat = 202;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo3.minDistToCat > 170){
            MicroManager.microInfo3.minDistToCat = 170;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo4.minDistToCat > 130){
            MicroManager.microInfo4.minDistToCat = 130;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo5.minDistToCat > 98){
            MicroManager.microInfo5.minDistToCat = 98;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo6.minDistToCat > 130){
            MicroManager.microInfo6.minDistToCat = 130;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo7.minDistToCat > 170){
            MicroManager.microInfo7.minDistToCat = 170;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        if (MicroManager.microInfo8.minDistToCat > 162){
            MicroManager.microInfo8.minDistToCat = 162;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }


    static void addCatDefault(){
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo0.location);
        if (MicroManager.microInfo0.minDistToCat > unitDist){
            MicroManager.microInfo0.minDistToCat = unitDist;
            MicroManager.microInfo0.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo1.location);
        if (MicroManager.microInfo1.minDistToCat > unitDist){
            MicroManager.microInfo1.minDistToCat = unitDist;
            MicroManager.microInfo1.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo2.location);
        if (MicroManager.microInfo2.minDistToCat > unitDist){
            MicroManager.microInfo2.minDistToCat = unitDist;
            MicroManager.microInfo2.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo3.location);
        if (MicroManager.microInfo3.minDistToCat > unitDist){
            MicroManager.microInfo3.minDistToCat = unitDist;
            MicroManager.microInfo3.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo4.location);
        if (MicroManager.microInfo4.minDistToCat > unitDist){
            MicroManager.microInfo4.minDistToCat = unitDist;
            MicroManager.microInfo4.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo5.location);
        if (MicroManager.microInfo5.minDistToCat > unitDist){
            MicroManager.microInfo5.minDistToCat = unitDist;
            MicroManager.microInfo5.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo6.location);
        if (MicroManager.microInfo6.minDistToCat > unitDist){
            MicroManager.microInfo6.minDistToCat = unitDist;
            MicroManager.microInfo6.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo7.location);
        if (MicroManager.microInfo7.minDistToCat > unitDist){
            MicroManager.microInfo7.minDistToCat = unitDist;
            MicroManager.microInfo7.closestCat = MicroManager.unitLocation;
        }
        unitDist = Util.catDist(MicroManager.unitLocation, MicroManager.microInfo8.location);
        if (MicroManager.microInfo8.minDistToCat > unitDist){
            MicroManager.microInfo8.minDistToCat = unitDist;
            MicroManager.microInfo8.closestCat = MicroManager.unitLocation;
        }
    }

}
