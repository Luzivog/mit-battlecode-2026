package opp_obg;

import battlecode.common.*;

public class DodgeManager {

    static int dx, dy;

    static void addFlying(){
        dx = VisionManager.flyingUnit.x - MovementManager.myLoc.x;
        dy = VisionManager.flyingUnit.y - MovementManager.myLoc.y;
        switch(dx){
            case -4:
                switch(dy){
                    case -4:
                        addFlying60();
                        break;
                    case -3:
                        addFlying61();
                        break;
                    case -2:
                        addFlying62();
                        break;
                    case -1:
                        addFlying63();
                        break;
                    case 0:
                        addFlying64();
                        break;
                    case 1:
                        addFlying65();
                        break;
                    case 2:
                        addFlying66();
                        break;
                    case 3:
                        addFlying67();
                        break;
                    case 4:
                        addFlying68();
                        break;
                }
                break;
            case -3:
                switch(dy){
                    case -4:
                        addFlying69();
                        break;
                    case -3:
                        addFlying70();
                        break;
                    case -2:
                        addFlying71();
                        break;
                    case -1:
                        addFlying72();
                        break;
                    case 0:
                        addFlying73();
                        break;
                    case 1:
                        addFlying74();
                        break;
                    case 2:
                        addFlying75();
                        break;
                    case 3:
                        addFlying76();
                        break;
                    case 4:
                        addFlying77();
                        break;
                }
                break;
            case -2:
                switch(dy){
                    case -4:
                        addFlying78();
                        break;
                    case -3:
                        addFlying79();
                        break;
                    case -2:
                        addFlying80();
                        break;
                    case -1:
                        addFlying81();
                        break;
                    case 0:
                        addFlying82();
                        break;
                    case 1:
                        addFlying83();
                        break;
                    case 2:
                        addFlying84();
                        break;
                    case 3:
                        addFlying85();
                        break;
                    case 4:
                        addFlying86();
                        break;
                }
                break;
            case -1:
                switch(dy){
                    case -4:
                        addFlying87();
                        break;
                    case -3:
                        addFlying88();
                        break;
                    case -2:
                        addFlying89();
                        break;
                    case -1:
                        addFlying90();
                        break;
                    case 0:
                        addFlying91();
                        break;
                    case 1:
                        addFlying92();
                        break;
                    case 2:
                        addFlying93();
                        break;
                    case 3:
                        addFlying94();
                        break;
                    case 4:
                        addFlying95();
                        break;
                }
                break;
            case 0:
                switch(dy){
                    case -4:
                        addFlying96();
                        break;
                    case -3:
                        addFlying97();
                        break;
                    case -2:
                        addFlying98();
                        break;
                    case -1:
                        addFlying99();
                        break;
                    case 0:
                        addFlying100();
                        break;
                    case 1:
                        addFlying101();
                        break;
                    case 2:
                        addFlying102();
                        break;
                    case 3:
                        addFlying103();
                        break;
                    case 4:
                        addFlying104();
                        break;
                }
                break;
            case 1:
                switch(dy){
                    case -4:
                        addFlying105();
                        break;
                    case -3:
                        addFlying106();
                        break;
                    case -2:
                        addFlying107();
                        break;
                    case -1:
                        addFlying108();
                        break;
                    case 0:
                        addFlying109();
                        break;
                    case 1:
                        addFlying110();
                        break;
                    case 2:
                        addFlying111();
                        break;
                    case 3:
                        addFlying112();
                        break;
                    case 4:
                        addFlying113();
                        break;
                }
                break;
            case 2:
                switch(dy){
                    case -4:
                        addFlying114();
                        break;
                    case -3:
                        addFlying115();
                        break;
                    case -2:
                        addFlying116();
                        break;
                    case -1:
                        addFlying117();
                        break;
                    case 0:
                        addFlying118();
                        break;
                    case 1:
                        addFlying119();
                        break;
                    case 2:
                        addFlying120();
                        break;
                    case 3:
                        addFlying121();
                        break;
                    case 4:
                        addFlying122();
                        break;
                }
                break;
            case 3:
                switch(dy){
                    case -4:
                        addFlying123();
                        break;
                    case -3:
                        addFlying124();
                        break;
                    case -2:
                        addFlying125();
                        break;
                    case -1:
                        addFlying126();
                        break;
                    case 0:
                        addFlying127();
                        break;
                    case 1:
                        addFlying128();
                        break;
                    case 2:
                        addFlying129();
                        break;
                    case 3:
                        addFlying130();
                        break;
                    case 4:
                        addFlying131();
                        break;
                }
                break;
            case 4:
                switch(dy){
                    case -4:
                        addFlying132();
                        break;
                    case -3:
                        addFlying133();
                        break;
                    case -2:
                        addFlying134();
                        break;
                    case -1:
                        addFlying135();
                        break;
                    case 0:
                        addFlying136();
                        break;
                    case 1:
                        addFlying137();
                        break;
                    case 2:
                        addFlying138();
                        break;
                    case 3:
                        addFlying139();
                        break;
                    case 4:
                        addFlying140();
                        break;
                }
                break;
        }
    }

    static void addFlying60(){ // (-4, -4)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying61(){ // (-4, -3)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying62(){ // (-4, -2)
        MovementManager.forbidden[7] = true;
    }

    static void addFlying63(){ // (-4, -1)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
    }

    static void addFlying64(){ // (-4, 0)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying65(){ // (-4, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying66(){ // (-4, 2)
        MovementManager.forbidden[5] = true;
    }

    static void addFlying67(){ // (-4, 3)
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying68(){ // (-4, 4)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying69(){ // (-3, -4)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
    }

    static void addFlying70(){ // (-3, -3)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying71(){ // (-3, -2)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying72(){ // (-3, -1)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying73(){ // (-3, 0)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying74(){ // (-3, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying75(){ // (-3, 2)
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying76(){ // (-3, 3)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying77(){ // (-3, 4)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
    }

    static void addFlying78(){ // (-2, -4)
        MovementManager.forbidden[3] = true;
    }

    static void addFlying79(){ // (-2, -3)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
    }

    static void addFlying80(){ // (-2, -2)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying81(){ // (-2, -1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying82(){ // (-2, 0)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying83(){ // (-2, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying84(){ // (-2, 2)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying85(){ // (-2, 3)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
    }

    static void addFlying86(){ // (-2, 4)
        MovementManager.forbidden[1] = true;
    }

    static void addFlying87(){ // (-1, -4)
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying88(){ // (-1, -3)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying89(){ // (-1, -2)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying90(){ // (-1, -1)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying91(){ // (-1, 0)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying92(){ // (-1, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying93(){ // (-1, 2)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying94(){ // (-1, 3)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying95(){ // (-1, 4)
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying96(){ // (0, -4)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying97(){ // (0, -3)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying98(){ // (0, -2)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying99(){ // (0, -1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying100(){ // (0, 0)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying101(){ // (0, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying102(){ // (0, 2)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying103(){ // (0, 3)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying104(){ // (0, 4)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying105(){ // (1, -4)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
    }

    static void addFlying106(){ // (1, -3)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[5] = true;
    }

    static void addFlying107(){ // (1, -2)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying108(){ // (1, -1)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying109(){ // (1, 0)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying110(){ // (1, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying111(){ // (1, 2)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying112(){ // (1, 3)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying113(){ // (1, 4)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
    }

    static void addFlying114(){ // (2, -4)
        MovementManager.forbidden[5] = true;
    }

    static void addFlying115(){ // (2, -3)
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying116(){ // (2, -2)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying117(){ // (2, -1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
    }

    static void addFlying118(){ // (2, 0)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying119(){ // (2, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying120(){ // (2, 2)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying121(){ // (2, 3)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying122(){ // (2, 4)
        MovementManager.forbidden[7] = true;
    }

    static void addFlying123(){ // (3, -4)
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying124(){ // (3, -3)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying125(){ // (3, -2)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
    }

    static void addFlying126(){ // (3, -1)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
    }

    static void addFlying127(){ // (3, 0)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying128(){ // (3, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying129(){ // (3, 2)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
    }

    static void addFlying130(){ // (3, 3)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying131(){ // (3, 4)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[6] = true;
    }

    static void addFlying132(){ // (4, -4)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[7] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying133(){ // (4, -3)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[2] = true;
    }

    static void addFlying134(){ // (4, -2)
        MovementManager.forbidden[1] = true;
    }

    static void addFlying135(){ // (4, -1)
        MovementManager.forbidden[3] = true;
        MovementManager.forbidden[4] = true;
        MovementManager.forbidden[5] = true;
    }

    static void addFlying136(){ // (4, 0)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[6] = true;
        MovementManager.forbidden[8] = true;
    }

    static void addFlying137(){ // (4, 1)
        MovementManager.forbidden[0] = true;
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[7] = true;
    }

    static void addFlying138(){ // (4, 2)
        MovementManager.forbidden[3] = true;
    }

    static void addFlying139(){ // (4, 3)
        MovementManager.forbidden[2] = true;
        MovementManager.forbidden[4] = true;
    }

    static void addFlying140(){ // (4, 4)
        MovementManager.forbidden[1] = true;
        MovementManager.forbidden[5] = true;
        MovementManager.forbidden[8] = true;
    }


}
