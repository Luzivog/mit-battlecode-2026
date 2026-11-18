package opp_obg;

import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
public class LocalBFS {

    static RobotController rc;

    static void init(){
        rc = MyUnit.rc;
    }

    static int tileCode;

    static MapLocation l14;
    static int v14;
    static Direction d14;
    static boolean p14;

    static MapLocation l15;
    static int v15;
    static Direction d15;
    static boolean p15;

    static MapLocation l16;
    static int v16;
    static Direction d16;
    static boolean p16;

    static MapLocation l17;
    static int v17;
    static Direction d17;
    static boolean p17;

    static MapLocation l18;
    static int v18;
    static Direction d18;
    static boolean p18;

    static MapLocation l24;
    static int v24;
    static Direction d24;
    static boolean p24;

    static MapLocation l25;
    static int v25;
    static Direction d25;
    static boolean p25;

    static MapLocation l26;
    static int v26;
    static Direction d26;
    static boolean p26;

    static MapLocation l27;
    static int v27;
    static Direction d27;
    static boolean p27;

    static MapLocation l28;
    static int v28;
    static Direction d28;
    static boolean p28;

    static MapLocation l29;
    static int v29;
    static Direction d29;
    static boolean p29;

    static MapLocation l30;
    static int v30;
    static Direction d30;
    static boolean p30;

    static MapLocation l34;
    static int v34;
    static Direction d34;
    static boolean p34;

    static MapLocation l35;
    static int v35;
    static Direction d35;
    static boolean p35;

    static MapLocation l36;
    static int v36;
    static Direction d36;
    static boolean p36;

    static MapLocation l37;
    static int v37;
    static Direction d37;
    static boolean p37;

    static MapLocation l38;
    static int v38;
    static Direction d38;
    static boolean p38;

    static MapLocation l39;
    static int v39;
    static Direction d39;
    static boolean p39;

    static MapLocation l40;
    static int v40;
    static Direction d40;
    static boolean p40;

    static MapLocation l41;
    static int v41;
    static Direction d41;
    static boolean p41;

    static MapLocation l42;
    static int v42;
    static Direction d42;
    static boolean p42;

    static MapLocation l45;
    static int v45;
    static Direction d45;
    static boolean p45;

    static MapLocation l46;
    static int v46;
    static Direction d46;
    static boolean p46;

    static MapLocation l47;
    static int v47;
    static Direction d47;
    static boolean p47;

    static MapLocation l48;
    static int v48;
    static Direction d48;
    static boolean p48;

    static MapLocation l49;
    static int v49;
    static Direction d49;
    static boolean p49;

    static MapLocation l50;
    static int v50;
    static Direction d50;
    static boolean p50;

    static MapLocation l51;
    static int v51;
    static Direction d51;
    static boolean p51;

    static MapLocation l52;
    static int v52;
    static Direction d52;
    static boolean p52;

    static MapLocation l53;
    static int v53;
    static Direction d53;
    static boolean p53;

    static MapLocation l56;
    static int v56;
    static Direction d56;
    static boolean p56;

    static MapLocation l57;
    static int v57;
    static Direction d57;
    static boolean p57;

    static MapLocation l58;
    static int v58;
    static Direction d58;
    static boolean p58;

    static MapLocation l59;
    static int v59;
    static Direction d59;
    static boolean p59;

    static MapLocation l60;
    static int v60;
    static Direction d60;
    static boolean p60;

    static MapLocation l61;
    static int v61;
    static Direction d61;
    static boolean p61;

    static MapLocation l62;
    static int v62;
    static Direction d62;
    static boolean p62;

    static MapLocation l63;
    static int v63;
    static Direction d63;
    static boolean p63;

    static MapLocation l64;
    static int v64;
    static Direction d64;
    static boolean p64;

    static MapLocation l67;
    static int v67;
    static Direction d67;
    static boolean p67;

    static MapLocation l68;
    static int v68;
    static Direction d68;
    static boolean p68;

    static MapLocation l69;
    static int v69;
    static Direction d69;
    static boolean p69;

    static MapLocation l70;
    static int v70;
    static Direction d70;
    static boolean p70;

    static MapLocation l71;
    static int v71;
    static Direction d71;
    static boolean p71;

    static MapLocation l72;
    static int v72;
    static Direction d72;
    static boolean p72;

    static MapLocation l73;
    static int v73;
    static Direction d73;
    static boolean p73;

    static MapLocation l74;
    static int v74;
    static Direction d74;
    static boolean p74;

    static MapLocation l75;
    static int v75;
    static Direction d75;
    static boolean p75;

    static MapLocation l78;
    static int v78;
    static Direction d78;
    static boolean p78;

    static MapLocation l79;
    static int v79;
    static Direction d79;
    static boolean p79;

    static MapLocation l80;
    static int v80;
    static Direction d80;
    static boolean p80;

    static MapLocation l81;
    static int v81;
    static Direction d81;
    static boolean p81;

    static MapLocation l82;
    static int v82;
    static Direction d82;
    static boolean p82;

    static MapLocation l83;
    static int v83;
    static Direction d83;
    static boolean p83;

    static MapLocation l84;
    static int v84;
    static Direction d84;
    static boolean p84;

    static MapLocation l85;
    static int v85;
    static Direction d85;
    static boolean p85;

    static MapLocation l86;
    static int v86;
    static Direction d86;
    static boolean p86;

    static MapLocation l90;
    static int v90;
    static Direction d90;
    static boolean p90;

    static MapLocation l91;
    static int v91;
    static Direction d91;
    static boolean p91;

    static MapLocation l92;
    static int v92;
    static Direction d92;
    static boolean p92;

    static MapLocation l93;
    static int v93;
    static Direction d93;
    static boolean p93;

    static MapLocation l94;
    static int v94;
    static Direction d94;
    static boolean p94;

    static MapLocation l95;
    static int v95;
    static Direction d95;
    static boolean p95;

    static MapLocation l96;
    static int v96;
    static Direction d96;
    static boolean p96;

    static MapLocation l102;
    static int v102;
    static Direction d102;
    static boolean p102;

    static MapLocation l103;
    static int v103;
    static Direction d103;
    static boolean p103;

    static MapLocation l104;
    static int v104;
    static Direction d104;
    static boolean p104;

    static MapLocation l105;
    static int v105;
    static Direction d105;
    static boolean p105;

    static MapLocation l106;
    static int v106;
    static Direction d106;
    static boolean p106;


    static void makeAssignments() throws GameActionException {
        l60 = rc.getLocation();
        v60 = 0;
        l61 = l60.add(Direction.NORTH);
        if (rc.onTheMap(l61)) {
            tileCode = l61.x * MyUnit.dim2 + l61.y;
            p61 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p61 =  false;
        l50 = l61.add(Direction.WEST);
        if (rc.onTheMap(l50)) {
            tileCode = l50.x * MyUnit.dim2 + l50.y;
            p50 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p50 =  false;
        l49 = l50.add(Direction.SOUTH);
        if (rc.onTheMap(l49)) {
            tileCode = l49.x * MyUnit.dim2 + l49.y;
            p49 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p49 =  false;
        l48 = l49.add(Direction.SOUTH);
        if (rc.onTheMap(l48)) {
            tileCode = l48.x * MyUnit.dim2 + l48.y;
            p48 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p48 =  false;
        l59 = l48.add(Direction.EAST);
        if (rc.onTheMap(l59)) {
            tileCode = l59.x * MyUnit.dim2 + l59.y;
            p59 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p59 =  false;
        l70 = l59.add(Direction.EAST);
        if (rc.onTheMap(l70)) {
            tileCode = l70.x * MyUnit.dim2 + l70.y;
            p70 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p70 =  false;
        l71 = l70.add(Direction.NORTH);
        if (rc.onTheMap(l71)) {
            tileCode = l71.x * MyUnit.dim2 + l71.y;
            p71 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p71 =  false;
        l72 = l71.add(Direction.NORTH);
        if (rc.onTheMap(l72)) {
            tileCode = l72.x * MyUnit.dim2 + l72.y;
            p72 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p72 =  false;
        l73 = l72.add(Direction.NORTH);
        if (rc.onTheMap(l73)) {
            tileCode = l73.x * MyUnit.dim2 + l73.y;
            p73 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p73 =  false;
        l62 = l73.add(Direction.WEST);
        if (rc.onTheMap(l62)) {
            tileCode = l62.x * MyUnit.dim2 + l62.y;
            p62 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p62 =  false;
        l51 = l62.add(Direction.WEST);
        if (rc.onTheMap(l51)) {
            tileCode = l51.x * MyUnit.dim2 + l51.y;
            p51 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p51 =  false;
        l40 = l51.add(Direction.WEST);
        if (rc.onTheMap(l40)) {
            tileCode = l40.x * MyUnit.dim2 + l40.y;
            p40 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p40 =  false;
        l39 = l40.add(Direction.SOUTH);
        if (rc.onTheMap(l39)) {
            tileCode = l39.x * MyUnit.dim2 + l39.y;
            p39 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p39 =  false;
        l38 = l39.add(Direction.SOUTH);
        if (rc.onTheMap(l38)) {
            tileCode = l38.x * MyUnit.dim2 + l38.y;
            p38 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p38 =  false;
        l37 = l38.add(Direction.SOUTH);
        if (rc.onTheMap(l37)) {
            tileCode = l37.x * MyUnit.dim2 + l37.y;
            p37 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p37 =  false;
        l36 = l37.add(Direction.SOUTH);
        if (rc.onTheMap(l36)) {
            tileCode = l36.x * MyUnit.dim2 + l36.y;
            p36 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p36 =  false;
        l47 = l36.add(Direction.EAST);
        if (rc.onTheMap(l47)) {
            tileCode = l47.x * MyUnit.dim2 + l47.y;
            p47 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p47 =  false;
        l58 = l47.add(Direction.EAST);
        if (rc.onTheMap(l58)) {
            tileCode = l58.x * MyUnit.dim2 + l58.y;
            p58 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p58 =  false;
        l69 = l58.add(Direction.EAST);
        if (rc.onTheMap(l69)) {
            tileCode = l69.x * MyUnit.dim2 + l69.y;
            p69 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p69 =  false;
        l80 = l69.add(Direction.EAST);
        if (rc.onTheMap(l80)) {
            tileCode = l80.x * MyUnit.dim2 + l80.y;
            p80 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p80 =  false;
        l81 = l80.add(Direction.NORTH);
        if (rc.onTheMap(l81)) {
            tileCode = l81.x * MyUnit.dim2 + l81.y;
            p81 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p81 =  false;
        l82 = l81.add(Direction.NORTH);
        if (rc.onTheMap(l82)) {
            tileCode = l82.x * MyUnit.dim2 + l82.y;
            p82 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p82 =  false;
        l83 = l82.add(Direction.NORTH);
        if (rc.onTheMap(l83)) {
            tileCode = l83.x * MyUnit.dim2 + l83.y;
            p83 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p83 =  false;
        l84 = l83.add(Direction.NORTH);
        if (rc.onTheMap(l84)) {
            tileCode = l84.x * MyUnit.dim2 + l84.y;
            p84 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p84 =  false;
        l74 = l84.add(Direction.NORTHWEST);
        if (rc.onTheMap(l74)) {
            tileCode = l74.x * MyUnit.dim2 + l74.y;
            p74 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p74 =  false;
        l63 = l74.add(Direction.WEST);
        if (rc.onTheMap(l63)) {
            tileCode = l63.x * MyUnit.dim2 + l63.y;
            p63 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p63 =  false;
        l52 = l63.add(Direction.WEST);
        if (rc.onTheMap(l52)) {
            tileCode = l52.x * MyUnit.dim2 + l52.y;
            p52 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p52 =  false;
        l41 = l52.add(Direction.WEST);
        if (rc.onTheMap(l41)) {
            tileCode = l41.x * MyUnit.dim2 + l41.y;
            p41 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p41 =  false;
        l29 = l41.add(Direction.SOUTHWEST);
        if (rc.onTheMap(l29)) {
            tileCode = l29.x * MyUnit.dim2 + l29.y;
            p29 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p29 =  false;
        l28 = l29.add(Direction.SOUTH);
        if (rc.onTheMap(l28)) {
            tileCode = l28.x * MyUnit.dim2 + l28.y;
            p28 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p28 =  false;
        l27 = l28.add(Direction.SOUTH);
        if (rc.onTheMap(l27)) {
            tileCode = l27.x * MyUnit.dim2 + l27.y;
            p27 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p27 =  false;
        l26 = l27.add(Direction.SOUTH);
        if (rc.onTheMap(l26)) {
            tileCode = l26.x * MyUnit.dim2 + l26.y;
            p26 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p26 =  false;
        l25 = l26.add(Direction.SOUTH);
        if (rc.onTheMap(l25)) {
            tileCode = l25.x * MyUnit.dim2 + l25.y;
            p25 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p25 =  false;
        l35 = l25.add(Direction.SOUTHEAST);
        if (rc.onTheMap(l35)) {
            tileCode = l35.x * MyUnit.dim2 + l35.y;
            p35 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p35 =  false;
        l46 = l35.add(Direction.EAST);
        if (rc.onTheMap(l46)) {
            tileCode = l46.x * MyUnit.dim2 + l46.y;
            p46 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p46 =  false;
        l57 = l46.add(Direction.EAST);
        if (rc.onTheMap(l57)) {
            tileCode = l57.x * MyUnit.dim2 + l57.y;
            p57 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p57 =  false;
        l68 = l57.add(Direction.EAST);
        if (rc.onTheMap(l68)) {
            tileCode = l68.x * MyUnit.dim2 + l68.y;
            p68 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p68 =  false;
        l79 = l68.add(Direction.EAST);
        if (rc.onTheMap(l79)) {
            tileCode = l79.x * MyUnit.dim2 + l79.y;
            p79 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p79 =  false;
        l91 = l79.add(Direction.NORTHEAST);
        if (rc.onTheMap(l91)) {
            tileCode = l91.x * MyUnit.dim2 + l91.y;
            p91 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p91 =  false;
        l92 = l91.add(Direction.NORTH);
        if (rc.onTheMap(l92)) {
            tileCode = l92.x * MyUnit.dim2 + l92.y;
            p92 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p92 =  false;
        l93 = l92.add(Direction.NORTH);
        if (rc.onTheMap(l93)) {
            tileCode = l93.x * MyUnit.dim2 + l93.y;
            p93 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p93 =  false;
        l94 = l93.add(Direction.NORTH);
        if (rc.onTheMap(l94)) {
            tileCode = l94.x * MyUnit.dim2 + l94.y;
            p94 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p94 =  false;
        l95 = l94.add(Direction.NORTH);
        if (rc.onTheMap(l95)) {
            tileCode = l95.x * MyUnit.dim2 + l95.y;
            p95 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p95 =  false;
        l85 = l95.add(Direction.NORTHWEST);
        if (rc.onTheMap(l85)) {
            tileCode = l85.x * MyUnit.dim2 + l85.y;
            p85 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p85 =  false;
        l75 = l85.add(Direction.NORTHWEST);
        if (rc.onTheMap(l75)) {
            tileCode = l75.x * MyUnit.dim2 + l75.y;
            p75 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p75 =  false;
        l64 = l75.add(Direction.WEST);
        if (rc.onTheMap(l64)) {
            tileCode = l64.x * MyUnit.dim2 + l64.y;
            p64 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p64 =  false;
        l53 = l64.add(Direction.WEST);
        if (rc.onTheMap(l53)) {
            tileCode = l53.x * MyUnit.dim2 + l53.y;
            p53 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p53 =  false;
        l42 = l53.add(Direction.WEST);
        if (rc.onTheMap(l42)) {
            tileCode = l42.x * MyUnit.dim2 + l42.y;
            p42 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p42 =  false;
        l30 = l42.add(Direction.SOUTHWEST);
        if (rc.onTheMap(l30)) {
            tileCode = l30.x * MyUnit.dim2 + l30.y;
            p30 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p30 =  false;
        l18 = l30.add(Direction.SOUTHWEST);
        if (rc.onTheMap(l18)) {
            tileCode = l18.x * MyUnit.dim2 + l18.y;
            p18 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p18 =  false;
        l17 = l18.add(Direction.SOUTH);
        if (rc.onTheMap(l17)) {
            tileCode = l17.x * MyUnit.dim2 + l17.y;
            p17 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p17 =  false;
        l16 = l17.add(Direction.SOUTH);
        if (rc.onTheMap(l16)) {
            tileCode = l16.x * MyUnit.dim2 + l16.y;
            p16 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p16 =  false;
        l15 = l16.add(Direction.SOUTH);
        if (rc.onTheMap(l15)) {
            tileCode = l15.x * MyUnit.dim2 + l15.y;
            p15 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p15 =  false;
        l14 = l15.add(Direction.SOUTH);
        if (rc.onTheMap(l14)) {
            tileCode = l14.x * MyUnit.dim2 + l14.y;
            p14 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p14 =  false;
        l24 = l14.add(Direction.SOUTHEAST);
        if (rc.onTheMap(l24)) {
            tileCode = l24.x * MyUnit.dim2 + l24.y;
            p24 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p24 =  false;
        l34 = l24.add(Direction.SOUTHEAST);
        if (rc.onTheMap(l34)) {
            tileCode = l34.x * MyUnit.dim2 + l34.y;
            p34 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p34 =  false;
        l45 = l34.add(Direction.EAST);
        if (rc.onTheMap(l45)) {
            tileCode = l45.x * MyUnit.dim2 + l45.y;
            p45 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p45 =  false;
        l56 = l45.add(Direction.EAST);
        if (rc.onTheMap(l56)) {
            tileCode = l56.x * MyUnit.dim2 + l56.y;
            p56 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p56 =  false;
        l67 = l56.add(Direction.EAST);
        if (rc.onTheMap(l67)) {
            tileCode = l67.x * MyUnit.dim2 + l67.y;
            p67 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p67 =  false;
        l78 = l67.add(Direction.EAST);
        if (rc.onTheMap(l78)) {
            tileCode = l78.x * MyUnit.dim2 + l78.y;
            p78 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p78 =  false;
        l90 = l78.add(Direction.NORTHEAST);
        if (rc.onTheMap(l90)) {
            tileCode = l90.x * MyUnit.dim2 + l90.y;
            p90 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p90 =  false;
        l102 = l90.add(Direction.NORTHEAST);
        if (rc.onTheMap(l102)) {
            tileCode = l102.x * MyUnit.dim2 + l102.y;
            p102 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p102 =  false;
        l103 = l102.add(Direction.NORTH);
        if (rc.onTheMap(l103)) {
            tileCode = l103.x * MyUnit.dim2 + l103.y;
            p103 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p103 =  false;
        l104 = l103.add(Direction.NORTH);
        if (rc.onTheMap(l104)) {
            tileCode = l104.x * MyUnit.dim2 + l104.y;
            p104 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p104 =  false;
        l105 = l104.add(Direction.NORTH);
        if (rc.onTheMap(l105)) {
            tileCode = l105.x * MyUnit.dim2 + l105.y;
            p105 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p105 =  false;
        l106 = l105.add(Direction.NORTH);
        if (rc.onTheMap(l106)) {
            tileCode = l106.x * MyUnit.dim2 + l106.y;
            p106 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p106 =  false;
        l96 = l106.add(Direction.NORTHWEST);
        if (rc.onTheMap(l96)) {
            tileCode = l96.x * MyUnit.dim2 + l96.y;
            p96 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p96 =  false;
        l86 = l96.add(Direction.NORTHWEST);
        if (rc.onTheMap(l86)) {
            tileCode = l86.x * MyUnit.dim2 + l86.y;
            p86 = (MapData.commMap[tileCode/10] & (1 << (tileCode%10))) == 0;
        }
        else p86 =  false;
    }


    static void computeDistsUnit() throws GameActionException {
        v49 = v60;
        d49 = Direction.WEST;
        if (p49) v49 ++;
        else v49+= 1000000;
        if (v60 > v49) {
            v59 = v49;
            d59 = d49;
        }
        else {
            v59 = v60;
            d59 = Direction.SOUTH;
        }
        if (p59) v59 ++;
        else v59+= 1000000;
        if (v60 > v49) {
            v61 = v49;
            d61 = d49;
        }
        else {
            v61 = v60;
            d61 = Direction.NORTH;
        }
        if (p61) v61 ++;
        else v61+= 1000000;
        if (v60 > v61) {
            if (v61 > v59) {
                v71 = v59;
                d71 = d59;
            }
            else {
                v71 = v61;
                d71 = d61;
            }
        }
        else {
            if (v60 > v59) {
                v71 = v59;
                d71 = d59;
            }
            else {
                v71 = v60;
                d71 = Direction.EAST;
            }
        }
        if (p71) v71 ++;
        else v71+= 1000000;
        if (v60 > v49) {
            if (v49 > v59) {
                v48 = v59;
                d48 = d59;
            }
            else {
                v48 = v49;
                d48 = d49;
            }
        }
        else {
            if (v60 > v59) {
                v48 = v59;
                d48 = d59;
            }
            else {
                v48 = v60;
                d48 = Direction.SOUTHWEST;
            }
        }
        if (p48) v48 ++;
        else v48+= 1000000;
        if (v60 > v49) {
            if (v49 > v61) {
                v50 = v61;
                d50 = d61;
            }
            else {
                v50 = v49;
                d50 = d49;
            }
        }
        else {
            if (v60 > v61) {
                v50 = v61;
                d50 = d61;
            }
            else {
                v50 = v60;
                d50 = Direction.NORTHWEST;
            }
        }
        if (p50) v50 ++;
        else v50+= 1000000;
        if (v60 > v71) {
            if (v71 > v59) {
                v70 = v59;
                d70 = d59;
            }
            else {
                v70 = v71;
                d70 = d71;
            }
        }
        else {
            if (v60 > v59) {
                v70 = v59;
                d70 = d59;
            }
            else {
                v70 = v60;
                d70 = Direction.SOUTHEAST;
            }
        }
        if (p70) v70 ++;
        else v70+= 1000000;
        if (v60 > v61) {
            if (v61 > v71) {
                v72 = v71;
                d72 = d71;
            }
            else {
                v72 = v61;
                d72 = d61;
            }
        }
        else {
            if (v60 > v71) {
                v72 = v71;
                d72 = d71;
            }
            else {
                v72 = v60;
                d72 = Direction.NORTHEAST;
            }
        }
        if (p72) v72 ++;
        else v72+= 1000000;
        if (v49 > v48) {
            if (v48 > v50) {
                v38 = v50;
                d38 = d50;
            }
            else {
                v38 = v48;
                d38 = d48;
            }
        }
        else {
            if (v49 > v50) {
                v38 = v50;
                d38 = d50;
            }
            else {
                v38 = v49;
                d38 = d49;
            }
        }
        if (p38) v38 ++;
        else v38+= 1000000;
        if (v59 > v48) {
            if (v48 > v70) {
                v58 = v70;
                d58 = d70;
            }
            else {
                v58 = v48;
                d58 = d48;
            }
        }
        else {
            if (v59 > v70) {
                v58 = v70;
                d58 = d70;
            }
            else {
                v58 = v59;
                d58 = d59;
            }
        }
        if (p58) v58 ++;
        else v58+= 1000000;
        if (v61 > v50) {
            if (v50 > v72) {
                v62 = v72;
                d62 = d72;
            }
            else {
                v62 = v50;
                d62 = d50;
            }
        }
        else {
            if (v61 > v72) {
                v62 = v72;
                d62 = d72;
            }
            else {
                v62 = v61;
                d62 = d61;
            }
        }
        if (p62) v62 ++;
        else v62+= 1000000;
        if (v71 > v72) {
            if (v72 > v70) {
                v82 = v70;
                d82 = d70;
            }
            else {
                v82 = v72;
                d82 = d72;
            }
        }
        else {
            if (v71 > v70) {
                v82 = v70;
                d82 = d70;
            }
            else {
                v82 = v71;
                d82 = d71;
            }
        }
        if (p82) v82 ++;
        else v82+= 1000000;
        if (v49 > v48) {
            if (v48 > v38) {
                v37 = v38;
                d37 = d38;
            }
            else {
                v37 = v48;
                d37 = d48;
            }
        }
        else {
            if (v49 > v38) {
                v37 = v38;
                d37 = d38;
            }
            else {
                v37 = v49;
                d37 = d49;
            }
        }
        if (p37) v37 ++;
        else v37+= 1000000;
        if (v49 > v50) {
            if (v50 > v38) {
                v39 = v38;
                d39 = d38;
            }
            else {
                v39 = v50;
                d39 = d50;
            }
        }
        else {
            if (v49 > v38) {
                v39 = v38;
                d39 = d38;
            }
            else {
                v39 = v49;
                d39 = d49;
            }
        }
        if (p39) v39 ++;
        else v39+= 1000000;
        if (v59 > v48) {
            if (v48 > v58) {
                if (v58 > v37) {
                    v47 = v37;
                    d47 = d37;
                }
                else {
                    v47 = v58;
                    d47 = d58;
                }
            }
            else {
                if (v48 > v37) {
                    v47 = v37;
                    d47 = d37;
                }
                else {
                    v47 = v48;
                    d47 = d48;
                }
            }
        }
        else {
            if (v59 > v58) {
                if (v58 > v37) {
                    v47 = v37;
                    d47 = d37;
                }
                else {
                    v47 = v58;
                    d47 = d58;
                }
            }
            else {
                if (v59 > v37) {
                    v47 = v37;
                    d47 = d37;
                }
                else {
                    v47 = v59;
                    d47 = d59;
                }
            }
        }
        if (p47) v47 ++;
        else v47+= 1000000;
        if (v61 > v50) {
            if (v50 > v62) {
                if (v62 > v39) {
                    v51 = v39;
                    d51 = d39;
                }
                else {
                    v51 = v62;
                    d51 = d62;
                }
            }
            else {
                if (v50 > v39) {
                    v51 = v39;
                    d51 = d39;
                }
                else {
                    v51 = v50;
                    d51 = d50;
                }
            }
        }
        else {
            if (v61 > v62) {
                if (v62 > v39) {
                    v51 = v39;
                    d51 = d39;
                }
                else {
                    v51 = v62;
                    d51 = d62;
                }
            }
            else {
                if (v61 > v39) {
                    v51 = v39;
                    d51 = d39;
                }
                else {
                    v51 = v61;
                    d51 = d61;
                }
            }
        }
        if (p51) v51 ++;
        else v51+= 1000000;
        if (v59 > v70) {
            if (v70 > v58) {
                v69 = v58;
                d69 = d58;
            }
            else {
                v69 = v70;
                d69 = d70;
            }
        }
        else {
            if (v59 > v58) {
                v69 = v58;
                d69 = d58;
            }
            else {
                v69 = v59;
                d69 = d59;
            }
        }
        if (p69) v69 ++;
        else v69+= 1000000;
        if (v61 > v72) {
            if (v72 > v62) {
                v73 = v62;
                d73 = d62;
            }
            else {
                v73 = v72;
                d73 = d72;
            }
        }
        else {
            if (v61 > v62) {
                v73 = v62;
                d73 = d62;
            }
            else {
                v73 = v61;
                d73 = d61;
            }
        }
        if (p73) v73 ++;
        else v73+= 1000000;
        if (v71 > v70) {
            if (v70 > v82) {
                if (v82 > v69) {
                    v81 = v69;
                    d81 = d69;
                }
                else {
                    v81 = v82;
                    d81 = d82;
                }
            }
            else {
                if (v70 > v69) {
                    v81 = v69;
                    d81 = d69;
                }
                else {
                    v81 = v70;
                    d81 = d70;
                }
            }
        }
        else {
            if (v71 > v82) {
                if (v82 > v69) {
                    v81 = v69;
                    d81 = d69;
                }
                else {
                    v81 = v82;
                    d81 = d82;
                }
            }
            else {
                if (v71 > v69) {
                    v81 = v69;
                    d81 = d69;
                }
                else {
                    v81 = v71;
                    d81 = d71;
                }
            }
        }
        if (p81) v81 ++;
        else v81+= 1000000;
        if (v71 > v72) {
            if (v72 > v82) {
                if (v82 > v73) {
                    v83 = v73;
                    d83 = d73;
                }
                else {
                    v83 = v82;
                    d83 = d82;
                }
            }
            else {
                if (v72 > v73) {
                    v83 = v73;
                    d83 = d73;
                }
                else {
                    v83 = v72;
                    d83 = d72;
                }
            }
        }
        else {
            if (v71 > v82) {
                if (v82 > v73) {
                    v83 = v73;
                    d83 = d73;
                }
                else {
                    v83 = v82;
                    d83 = d82;
                }
            }
            else {
                if (v71 > v73) {
                    v83 = v73;
                    d83 = d73;
                }
                else {
                    v83 = v71;
                    d83 = d71;
                }
            }
        }
        if (p83) v83 ++;
        else v83+= 1000000;
        if (v48 > v37) {
            if (v37 > v47) {
                v36 = v47;
                d36 = d47;
            }
            else {
                v36 = v37;
                d36 = d37;
            }
        }
        else {
            if (v48 > v47) {
                v36 = v47;
                d36 = d47;
            }
            else {
                v36 = v48;
                d36 = d48;
            }
        }
        if (p36) v36 ++;
        else v36+= 1000000;
        if (v50 > v39) {
            if (v39 > v51) {
                v40 = v51;
                d40 = d51;
            }
            else {
                v40 = v39;
                d40 = d39;
            }
        }
        else {
            if (v50 > v51) {
                v40 = v51;
                d40 = d51;
            }
            else {
                v40 = v50;
                d40 = d50;
            }
        }
        if (p40) v40 ++;
        else v40+= 1000000;
        if (v70 > v81) {
            if (v81 > v69) {
                v80 = v69;
                d80 = d69;
            }
            else {
                v80 = v81;
                d80 = d81;
            }
        }
        else {
            if (v70 > v69) {
                v80 = v69;
                d80 = d69;
            }
            else {
                v80 = v70;
                d80 = d70;
            }
        }
        if (p80) v80 ++;
        else v80+= 1000000;
        if (v72 > v73) {
            if (v73 > v83) {
                v84 = v83;
                d84 = d83;
            }
            else {
                v84 = v73;
                d84 = d73;
            }
        }
        else {
            if (v72 > v83) {
                v84 = v83;
                d84 = d83;
            }
            else {
                v84 = v72;
                d84 = d72;
            }
        }
        if (p84) v84 ++;
        else v84+= 1000000;
        if (v38 > v37) {
            if (v37 > v39) {
                v27 = v39;
                d27 = d39;
            }
            else {
                v27 = v37;
                d27 = d37;
            }
        }
        else {
            if (v38 > v39) {
                v27 = v39;
                d27 = d39;
            }
            else {
                v27 = v38;
                d27 = d38;
            }
        }
        if (p27) v27 ++;
        else v27+= 1000000;
        if (v58 > v47) {
            if (v47 > v69) {
                v57 = v69;
                d57 = d69;
            }
            else {
                v57 = v47;
                d57 = d47;
            }
        }
        else {
            if (v58 > v69) {
                v57 = v69;
                d57 = d69;
            }
            else {
                v57 = v58;
                d57 = d58;
            }
        }
        if (p57) v57 ++;
        else v57+= 1000000;
        if (v62 > v51) {
            if (v51 > v73) {
                v63 = v73;
                d63 = d73;
            }
            else {
                v63 = v51;
                d63 = d51;
            }
        }
        else {
            if (v62 > v73) {
                v63 = v73;
                d63 = d73;
            }
            else {
                v63 = v62;
                d63 = d62;
            }
        }
        if (p63) v63 ++;
        else v63+= 1000000;
        if (v82 > v83) {
            if (v83 > v81) {
                v93 = v81;
                d93 = d81;
            }
            else {
                v93 = v83;
                d93 = d83;
            }
        }
        else {
            if (v82 > v81) {
                v93 = v81;
                d93 = d81;
            }
            else {
                v93 = v82;
                d93 = d82;
            }
        }
        if (p93) v93 ++;
        else v93+= 1000000;
        if (v38 > v37) {
            if (v37 > v36) {
                if (v36 > v27) {
                    v26 = v27;
                    d26 = d27;
                }
                else {
                    v26 = v36;
                    d26 = d36;
                }
            }
            else {
                if (v37 > v27) {
                    v26 = v27;
                    d26 = d27;
                }
                else {
                    v26 = v37;
                    d26 = d37;
                }
            }
        }
        else {
            if (v38 > v36) {
                if (v36 > v27) {
                    v26 = v27;
                    d26 = d27;
                }
                else {
                    v26 = v36;
                    d26 = d36;
                }
            }
            else {
                if (v38 > v27) {
                    v26 = v27;
                    d26 = d27;
                }
                else {
                    v26 = v38;
                    d26 = d38;
                }
            }
        }
        if (p26) v26 ++;
        else v26+= 1000000;
        if (v38 > v39) {
            if (v39 > v40) {
                if (v40 > v27) {
                    v28 = v27;
                    d28 = d27;
                }
                else {
                    v28 = v40;
                    d28 = d40;
                }
            }
            else {
                if (v39 > v27) {
                    v28 = v27;
                    d28 = d27;
                }
                else {
                    v28 = v39;
                    d28 = d39;
                }
            }
        }
        else {
            if (v38 > v40) {
                if (v40 > v27) {
                    v28 = v27;
                    d28 = d27;
                }
                else {
                    v28 = v40;
                    d28 = d40;
                }
            }
            else {
                if (v38 > v27) {
                    v28 = v27;
                    d28 = d27;
                }
                else {
                    v28 = v38;
                    d28 = d38;
                }
            }
        }
        if (p28) v28 ++;
        else v28+= 1000000;
        if (v58 > v47) {
            if (v47 > v36) {
                if (v36 > v57) {
                    v46 = v57;
                    d46 = d57;
                }
                else {
                    v46 = v36;
                    d46 = d36;
                }
            }
            else {
                if (v47 > v57) {
                    v46 = v57;
                    d46 = d57;
                }
                else {
                    v46 = v47;
                    d46 = d47;
                }
            }
        }
        else {
            if (v58 > v36) {
                if (v36 > v57) {
                    v46 = v57;
                    d46 = d57;
                }
                else {
                    v46 = v36;
                    d46 = d36;
                }
            }
            else {
                if (v58 > v57) {
                    v46 = v57;
                    d46 = d57;
                }
                else {
                    v46 = v58;
                    d46 = d58;
                }
            }
        }
        if (p46) v46 ++;
        else v46+= 1000000;
        if (v62 > v51) {
            if (v51 > v40) {
                if (v40 > v63) {
                    v52 = v63;
                    d52 = d63;
                }
                else {
                    v52 = v40;
                    d52 = d40;
                }
            }
            else {
                if (v51 > v63) {
                    v52 = v63;
                    d52 = d63;
                }
                else {
                    v52 = v51;
                    d52 = d51;
                }
            }
        }
        else {
            if (v62 > v40) {
                if (v40 > v63) {
                    v52 = v63;
                    d52 = d63;
                }
                else {
                    v52 = v40;
                    d52 = d40;
                }
            }
            else {
                if (v62 > v63) {
                    v52 = v63;
                    d52 = d63;
                }
                else {
                    v52 = v62;
                    d52 = d62;
                }
            }
        }
        if (p52) v52 ++;
        else v52+= 1000000;
        if (v58 > v69) {
            if (v69 > v80) {
                if (v80 > v57) {
                    v68 = v57;
                    d68 = d57;
                }
                else {
                    v68 = v80;
                    d68 = d80;
                }
            }
            else {
                if (v69 > v57) {
                    v68 = v57;
                    d68 = d57;
                }
                else {
                    v68 = v69;
                    d68 = d69;
                }
            }
        }
        else {
            if (v58 > v80) {
                if (v80 > v57) {
                    v68 = v57;
                    d68 = d57;
                }
                else {
                    v68 = v80;
                    d68 = d80;
                }
            }
            else {
                if (v58 > v57) {
                    v68 = v57;
                    d68 = d57;
                }
                else {
                    v68 = v58;
                    d68 = d58;
                }
            }
        }
        if (p68) v68 ++;
        else v68+= 1000000;
        if (v62 > v73) {
            if (v73 > v84) {
                if (v84 > v63) {
                    v74 = v63;
                    d74 = d63;
                }
                else {
                    v74 = v84;
                    d74 = d84;
                }
            }
            else {
                if (v73 > v63) {
                    v74 = v63;
                    d74 = d63;
                }
                else {
                    v74 = v73;
                    d74 = d73;
                }
            }
        }
        else {
            if (v62 > v84) {
                if (v84 > v63) {
                    v74 = v63;
                    d74 = d63;
                }
                else {
                    v74 = v84;
                    d74 = d84;
                }
            }
            else {
                if (v62 > v63) {
                    v74 = v63;
                    d74 = d63;
                }
                else {
                    v74 = v62;
                    d74 = d62;
                }
            }
        }
        if (p74) v74 ++;
        else v74+= 1000000;
        if (v82 > v81) {
            if (v81 > v80) {
                if (v80 > v93) {
                    v92 = v93;
                    d92 = d93;
                }
                else {
                    v92 = v80;
                    d92 = d80;
                }
            }
            else {
                if (v81 > v93) {
                    v92 = v93;
                    d92 = d93;
                }
                else {
                    v92 = v81;
                    d92 = d81;
                }
            }
        }
        else {
            if (v82 > v80) {
                if (v80 > v93) {
                    v92 = v93;
                    d92 = d93;
                }
                else {
                    v92 = v80;
                    d92 = d80;
                }
            }
            else {
                if (v82 > v93) {
                    v92 = v93;
                    d92 = d93;
                }
                else {
                    v92 = v82;
                    d92 = d82;
                }
            }
        }
        if (p92) v92 ++;
        else v92+= 1000000;
        if (v82 > v83) {
            if (v83 > v84) {
                if (v84 > v93) {
                    v94 = v93;
                    d94 = d93;
                }
                else {
                    v94 = v84;
                    d94 = d84;
                }
            }
            else {
                if (v83 > v93) {
                    v94 = v93;
                    d94 = d93;
                }
                else {
                    v94 = v83;
                    d94 = d83;
                }
            }
        }
        else {
            if (v82 > v84) {
                if (v84 > v93) {
                    v94 = v93;
                    d94 = d93;
                }
                else {
                    v94 = v84;
                    d94 = d84;
                }
            }
            else {
                if (v82 > v93) {
                    v94 = v93;
                    d94 = d93;
                }
                else {
                    v94 = v82;
                    d94 = d82;
                }
            }
        }
        if (p94) v94 ++;
        else v94+= 1000000;
        if (v37 > v36) {
            if (v36 > v26) {
                v25 = v26;
                d25 = d26;
            }
            else {
                v25 = v36;
                d25 = d36;
            }
        }
        else {
            if (v37 > v26) {
                v25 = v26;
                d25 = d26;
            }
            else {
                v25 = v37;
                d25 = d37;
            }
        }
        if (p25) v25 ++;
        else v25+= 1000000;
        if (v39 > v40) {
            if (v40 > v28) {
                v29 = v28;
                d29 = d28;
            }
            else {
                v29 = v40;
                d29 = d40;
            }
        }
        else {
            if (v39 > v28) {
                v29 = v28;
                d29 = d28;
            }
            else {
                v29 = v39;
                d29 = d39;
            }
        }
        if (p29) v29 ++;
        else v29+= 1000000;
        if (v47 > v36) {
            if (v36 > v46) {
                if (v46 > v25) {
                    v35 = v25;
                    d35 = d25;
                }
                else {
                    v35 = v46;
                    d35 = d46;
                }
            }
            else {
                if (v36 > v25) {
                    v35 = v25;
                    d35 = d25;
                }
                else {
                    v35 = v36;
                    d35 = d36;
                }
            }
        }
        else {
            if (v47 > v46) {
                if (v46 > v25) {
                    v35 = v25;
                    d35 = d25;
                }
                else {
                    v35 = v46;
                    d35 = d46;
                }
            }
            else {
                if (v47 > v25) {
                    v35 = v25;
                    d35 = d25;
                }
                else {
                    v35 = v47;
                    d35 = d47;
                }
            }
        }
        if (p35) v35 ++;
        else v35+= 1000000;
        if (v51 > v40) {
            if (v40 > v52) {
                if (v52 > v29) {
                    v41 = v29;
                    d41 = d29;
                }
                else {
                    v41 = v52;
                    d41 = d52;
                }
            }
            else {
                if (v40 > v29) {
                    v41 = v29;
                    d41 = d29;
                }
                else {
                    v41 = v40;
                    d41 = d40;
                }
            }
        }
        else {
            if (v51 > v52) {
                if (v52 > v29) {
                    v41 = v29;
                    d41 = d29;
                }
                else {
                    v41 = v52;
                    d41 = d52;
                }
            }
            else {
                if (v51 > v29) {
                    v41 = v29;
                    d41 = d29;
                }
                else {
                    v41 = v51;
                    d41 = d51;
                }
            }
        }
        if (p41) v41 ++;
        else v41+= 1000000;
        if (v69 > v80) {
            if (v80 > v68) {
                v79 = v68;
                d79 = d68;
            }
            else {
                v79 = v80;
                d79 = d80;
            }
        }
        else {
            if (v69 > v68) {
                v79 = v68;
                d79 = d68;
            }
            else {
                v79 = v69;
                d79 = d69;
            }
        }
        if (p79) v79 ++;
        else v79+= 1000000;
        if (v73 > v84) {
            if (v84 > v74) {
                v85 = v74;
                d85 = d74;
            }
            else {
                v85 = v84;
                d85 = d84;
            }
        }
        else {
            if (v73 > v74) {
                v85 = v74;
                d85 = d74;
            }
            else {
                v85 = v73;
                d85 = d73;
            }
        }
        if (p85) v85 ++;
        else v85+= 1000000;
        if (v81 > v80) {
            if (v80 > v92) {
                if (v92 > v79) {
                    v91 = v79;
                    d91 = d79;
                }
                else {
                    v91 = v92;
                    d91 = d92;
                }
            }
            else {
                if (v80 > v79) {
                    v91 = v79;
                    d91 = d79;
                }
                else {
                    v91 = v80;
                    d91 = d80;
                }
            }
        }
        else {
            if (v81 > v92) {
                if (v92 > v79) {
                    v91 = v79;
                    d91 = d79;
                }
                else {
                    v91 = v92;
                    d91 = d92;
                }
            }
            else {
                if (v81 > v79) {
                    v91 = v79;
                    d91 = d79;
                }
                else {
                    v91 = v81;
                    d91 = d81;
                }
            }
        }
        if (p91) v91 ++;
        else v91+= 1000000;
        if (v83 > v84) {
            if (v84 > v94) {
                if (v94 > v85) {
                    v95 = v85;
                    d95 = d85;
                }
                else {
                    v95 = v94;
                    d95 = d94;
                }
            }
            else {
                if (v84 > v85) {
                    v95 = v85;
                    d95 = d85;
                }
                else {
                    v95 = v84;
                    d95 = d84;
                }
            }
        }
        else {
            if (v83 > v94) {
                if (v94 > v85) {
                    v95 = v85;
                    d95 = d85;
                }
                else {
                    v95 = v94;
                    d95 = d94;
                }
            }
            else {
                if (v83 > v85) {
                    v95 = v85;
                    d95 = d85;
                }
                else {
                    v95 = v83;
                    d95 = d83;
                }
            }
        }
        if (p95) v95 ++;
        else v95+= 1000000;
        if (v27 > v26) {
            if (v26 > v28) {
                v16 = v28;
                d16 = d28;
            }
            else {
                v16 = v26;
                d16 = d26;
            }
        }
        else {
            if (v27 > v28) {
                v16 = v28;
                d16 = d28;
            }
            else {
                v16 = v27;
                d16 = d27;
            }
        }
        if (p16) v16 ++;
        else v16+= 1000000;
        if (v57 > v46) {
            if (v46 > v68) {
                v56 = v68;
                d56 = d68;
            }
            else {
                v56 = v46;
                d56 = d46;
            }
        }
        else {
            if (v57 > v68) {
                v56 = v68;
                d56 = d68;
            }
            else {
                v56 = v57;
                d56 = d57;
            }
        }
        if (p56) v56 ++;
        else v56+= 1000000;
        if (v63 > v52) {
            if (v52 > v74) {
                v64 = v74;
                d64 = d74;
            }
            else {
                v64 = v52;
                d64 = d52;
            }
        }
        else {
            if (v63 > v74) {
                v64 = v74;
                d64 = d74;
            }
            else {
                v64 = v63;
                d64 = d63;
            }
        }
        if (p64) v64 ++;
        else v64+= 1000000;
        if (v93 > v94) {
            if (v94 > v92) {
                v104 = v92;
                d104 = d92;
            }
            else {
                v104 = v94;
                d104 = d94;
            }
        }
        else {
            if (v93 > v92) {
                v104 = v92;
                d104 = d92;
            }
            else {
                v104 = v93;
                d104 = d93;
            }
        }
        if (p104) v104 ++;
        else v104+= 1000000;
        if (v27 > v26) {
            if (v26 > v25) {
                if (v25 > v16) {
                    v15 = v16;
                    d15 = d16;
                }
                else {
                    v15 = v25;
                    d15 = d25;
                }
            }
            else {
                if (v26 > v16) {
                    v15 = v16;
                    d15 = d16;
                }
                else {
                    v15 = v26;
                    d15 = d26;
                }
            }
        }
        else {
            if (v27 > v25) {
                if (v25 > v16) {
                    v15 = v16;
                    d15 = d16;
                }
                else {
                    v15 = v25;
                    d15 = d25;
                }
            }
            else {
                if (v27 > v16) {
                    v15 = v16;
                    d15 = d16;
                }
                else {
                    v15 = v27;
                    d15 = d27;
                }
            }
        }
        if (p15) v15 ++;
        else v15+= 1000000;
        if (v27 > v28) {
            if (v28 > v29) {
                if (v29 > v16) {
                    v17 = v16;
                    d17 = d16;
                }
                else {
                    v17 = v29;
                    d17 = d29;
                }
            }
            else {
                if (v28 > v16) {
                    v17 = v16;
                    d17 = d16;
                }
                else {
                    v17 = v28;
                    d17 = d28;
                }
            }
        }
        else {
            if (v27 > v29) {
                if (v29 > v16) {
                    v17 = v16;
                    d17 = d16;
                }
                else {
                    v17 = v29;
                    d17 = d29;
                }
            }
            else {
                if (v27 > v16) {
                    v17 = v16;
                    d17 = d16;
                }
                else {
                    v17 = v27;
                    d17 = d27;
                }
            }
        }
        if (p17) v17 ++;
        else v17+= 1000000;
        if (v57 > v46) {
            if (v46 > v35) {
                if (v35 > v56) {
                    v45 = v56;
                    d45 = d56;
                }
                else {
                    v45 = v35;
                    d45 = d35;
                }
            }
            else {
                if (v46 > v56) {
                    v45 = v56;
                    d45 = d56;
                }
                else {
                    v45 = v46;
                    d45 = d46;
                }
            }
        }
        else {
            if (v57 > v35) {
                if (v35 > v56) {
                    v45 = v56;
                    d45 = d56;
                }
                else {
                    v45 = v35;
                    d45 = d35;
                }
            }
            else {
                if (v57 > v56) {
                    v45 = v56;
                    d45 = d56;
                }
                else {
                    v45 = v57;
                    d45 = d57;
                }
            }
        }
        if (p45) v45 ++;
        else v45+= 1000000;
        if (v63 > v52) {
            if (v52 > v41) {
                if (v41 > v64) {
                    v53 = v64;
                    d53 = d64;
                }
                else {
                    v53 = v41;
                    d53 = d41;
                }
            }
            else {
                if (v52 > v64) {
                    v53 = v64;
                    d53 = d64;
                }
                else {
                    v53 = v52;
                    d53 = d52;
                }
            }
        }
        else {
            if (v63 > v41) {
                if (v41 > v64) {
                    v53 = v64;
                    d53 = d64;
                }
                else {
                    v53 = v41;
                    d53 = d41;
                }
            }
            else {
                if (v63 > v64) {
                    v53 = v64;
                    d53 = d64;
                }
                else {
                    v53 = v63;
                    d53 = d63;
                }
            }
        }
        if (p53) v53 ++;
        else v53+= 1000000;
        if (v57 > v68) {
            if (v68 > v79) {
                if (v79 > v56) {
                    v67 = v56;
                    d67 = d56;
                }
                else {
                    v67 = v79;
                    d67 = d79;
                }
            }
            else {
                if (v68 > v56) {
                    v67 = v56;
                    d67 = d56;
                }
                else {
                    v67 = v68;
                    d67 = d68;
                }
            }
        }
        else {
            if (v57 > v79) {
                if (v79 > v56) {
                    v67 = v56;
                    d67 = d56;
                }
                else {
                    v67 = v79;
                    d67 = d79;
                }
            }
            else {
                if (v57 > v56) {
                    v67 = v56;
                    d67 = d56;
                }
                else {
                    v67 = v57;
                    d67 = d57;
                }
            }
        }
        if (p67) v67 ++;
        else v67+= 1000000;
        if (v63 > v74) {
            if (v74 > v85) {
                if (v85 > v64) {
                    v75 = v64;
                    d75 = d64;
                }
                else {
                    v75 = v85;
                    d75 = d85;
                }
            }
            else {
                if (v74 > v64) {
                    v75 = v64;
                    d75 = d64;
                }
                else {
                    v75 = v74;
                    d75 = d74;
                }
            }
        }
        else {
            if (v63 > v85) {
                if (v85 > v64) {
                    v75 = v64;
                    d75 = d64;
                }
                else {
                    v75 = v85;
                    d75 = d85;
                }
            }
            else {
                if (v63 > v64) {
                    v75 = v64;
                    d75 = d64;
                }
                else {
                    v75 = v63;
                    d75 = d63;
                }
            }
        }
        if (p75) v75 ++;
        else v75+= 1000000;
        if (v93 > v92) {
            if (v92 > v91) {
                if (v91 > v104) {
                    v103 = v104;
                    d103 = d104;
                }
                else {
                    v103 = v91;
                    d103 = d91;
                }
            }
            else {
                if (v92 > v104) {
                    v103 = v104;
                    d103 = d104;
                }
                else {
                    v103 = v92;
                    d103 = d92;
                }
            }
        }
        else {
            if (v93 > v91) {
                if (v91 > v104) {
                    v103 = v104;
                    d103 = d104;
                }
                else {
                    v103 = v91;
                    d103 = d91;
                }
            }
            else {
                if (v93 > v104) {
                    v103 = v104;
                    d103 = d104;
                }
                else {
                    v103 = v93;
                    d103 = d93;
                }
            }
        }
        if (p103) v103 ++;
        else v103+= 1000000;
        if (v93 > v94) {
            if (v94 > v95) {
                if (v95 > v104) {
                    v105 = v104;
                    d105 = d104;
                }
                else {
                    v105 = v95;
                    d105 = d95;
                }
            }
            else {
                if (v94 > v104) {
                    v105 = v104;
                    d105 = d104;
                }
                else {
                    v105 = v94;
                    d105 = d94;
                }
            }
        }
        else {
            if (v93 > v95) {
                if (v95 > v104) {
                    v105 = v104;
                    d105 = d104;
                }
                else {
                    v105 = v95;
                    d105 = d95;
                }
            }
            else {
                if (v93 > v104) {
                    v105 = v104;
                    d105 = d104;
                }
                else {
                    v105 = v93;
                    d105 = d93;
                }
            }
        }
        if (p105) v105 ++;
        else v105+= 1000000;
        if (v36 > v25) {
            if (v25 > v35) {
                v24 = v35;
                d24 = d35;
            }
            else {
                v24 = v25;
                d24 = d25;
            }
        }
        else {
            if (v36 > v35) {
                v24 = v35;
                d24 = d35;
            }
            else {
                v24 = v36;
                d24 = d36;
            }
        }
        if (p24) v24 ++;
        else v24+= 1000000;
        if (v40 > v29) {
            if (v29 > v41) {
                v30 = v41;
                d30 = d41;
            }
            else {
                v30 = v29;
                d30 = d29;
            }
        }
        else {
            if (v40 > v41) {
                v30 = v41;
                d30 = d41;
            }
            else {
                v30 = v40;
                d30 = d40;
            }
        }
        if (p30) v30 ++;
        else v30+= 1000000;
        if (v80 > v91) {
            if (v91 > v79) {
                v90 = v79;
                d90 = d79;
            }
            else {
                v90 = v91;
                d90 = d91;
            }
        }
        else {
            if (v80 > v79) {
                v90 = v79;
                d90 = d79;
            }
            else {
                v90 = v80;
                d90 = d80;
            }
        }
        if (p90) v90 ++;
        else v90+= 1000000;
        if (v84 > v85) {
            if (v85 > v95) {
                v96 = v95;
                d96 = d95;
            }
            else {
                v96 = v85;
                d96 = d85;
            }
        }
        else {
            if (v84 > v95) {
                v96 = v95;
                d96 = d95;
            }
            else {
                v96 = v84;
                d96 = d84;
            }
        }
        if (p96) v96 ++;
        else v96+= 1000000;
        if (v26 > v25) {
            if (v25 > v15) {
                if (v15 > v24) {
                    v14 = v24;
                    d14 = d24;
                }
                else {
                    v14 = v15;
                    d14 = d15;
                }
            }
            else {
                if (v25 > v24) {
                    v14 = v24;
                    d14 = d24;
                }
                else {
                    v14 = v25;
                    d14 = d25;
                }
            }
        }
        else {
            if (v26 > v15) {
                if (v15 > v24) {
                    v14 = v24;
                    d14 = d24;
                }
                else {
                    v14 = v15;
                    d14 = d15;
                }
            }
            else {
                if (v26 > v24) {
                    v14 = v24;
                    d14 = d24;
                }
                else {
                    v14 = v26;
                    d14 = d26;
                }
            }
        }
        if (p14) v14 ++;
        else v14+= 1000000;
        if (v28 > v29) {
            if (v29 > v17) {
                if (v17 > v30) {
                    v18 = v30;
                    d18 = d30;
                }
                else {
                    v18 = v17;
                    d18 = d17;
                }
            }
            else {
                if (v29 > v30) {
                    v18 = v30;
                    d18 = d30;
                }
                else {
                    v18 = v29;
                    d18 = d29;
                }
            }
        }
        else {
            if (v28 > v17) {
                if (v17 > v30) {
                    v18 = v30;
                    d18 = d30;
                }
                else {
                    v18 = v17;
                    d18 = d17;
                }
            }
            else {
                if (v28 > v30) {
                    v18 = v30;
                    d18 = d30;
                }
                else {
                    v18 = v28;
                    d18 = d28;
                }
            }
        }
        if (p18) v18 ++;
        else v18+= 1000000;
        if (v46 > v35) {
            if (v35 > v45) {
                if (v45 > v24) {
                    v34 = v24;
                    d34 = d24;
                }
                else {
                    v34 = v45;
                    d34 = d45;
                }
            }
            else {
                if (v35 > v24) {
                    v34 = v24;
                    d34 = d24;
                }
                else {
                    v34 = v35;
                    d34 = d35;
                }
            }
        }
        else {
            if (v46 > v45) {
                if (v45 > v24) {
                    v34 = v24;
                    d34 = d24;
                }
                else {
                    v34 = v45;
                    d34 = d45;
                }
            }
            else {
                if (v46 > v24) {
                    v34 = v24;
                    d34 = d24;
                }
                else {
                    v34 = v46;
                    d34 = d46;
                }
            }
        }
        if (p34) v34 ++;
        else v34+= 1000000;
        if (v52 > v41) {
            if (v41 > v53) {
                if (v53 > v30) {
                    v42 = v30;
                    d42 = d30;
                }
                else {
                    v42 = v53;
                    d42 = d53;
                }
            }
            else {
                if (v41 > v30) {
                    v42 = v30;
                    d42 = d30;
                }
                else {
                    v42 = v41;
                    d42 = d41;
                }
            }
        }
        else {
            if (v52 > v53) {
                if (v53 > v30) {
                    v42 = v30;
                    d42 = d30;
                }
                else {
                    v42 = v53;
                    d42 = d53;
                }
            }
            else {
                if (v52 > v30) {
                    v42 = v30;
                    d42 = d30;
                }
                else {
                    v42 = v52;
                    d42 = d52;
                }
            }
        }
        if (p42) v42 ++;
        else v42+= 1000000;
        if (v68 > v79) {
            if (v79 > v67) {
                if (v67 > v90) {
                    v78 = v90;
                    d78 = d90;
                }
                else {
                    v78 = v67;
                    d78 = d67;
                }
            }
            else {
                if (v79 > v90) {
                    v78 = v90;
                    d78 = d90;
                }
                else {
                    v78 = v79;
                    d78 = d79;
                }
            }
        }
        else {
            if (v68 > v67) {
                if (v67 > v90) {
                    v78 = v90;
                    d78 = d90;
                }
                else {
                    v78 = v67;
                    d78 = d67;
                }
            }
            else {
                if (v68 > v90) {
                    v78 = v90;
                    d78 = d90;
                }
                else {
                    v78 = v68;
                    d78 = d68;
                }
            }
        }
        if (p78) v78 ++;
        else v78+= 1000000;
        if (v74 > v85) {
            if (v85 > v75) {
                if (v75 > v96) {
                    v86 = v96;
                    d86 = d96;
                }
                else {
                    v86 = v75;
                    d86 = d75;
                }
            }
            else {
                if (v85 > v96) {
                    v86 = v96;
                    d86 = d96;
                }
                else {
                    v86 = v85;
                    d86 = d85;
                }
            }
        }
        else {
            if (v74 > v75) {
                if (v75 > v96) {
                    v86 = v96;
                    d86 = d96;
                }
                else {
                    v86 = v75;
                    d86 = d75;
                }
            }
            else {
                if (v74 > v96) {
                    v86 = v96;
                    d86 = d96;
                }
                else {
                    v86 = v74;
                    d86 = d74;
                }
            }
        }
        if (p86) v86 ++;
        else v86+= 1000000;
        if (v92 > v91) {
            if (v91 > v103) {
                if (v103 > v90) {
                    v102 = v90;
                    d102 = d90;
                }
                else {
                    v102 = v103;
                    d102 = d103;
                }
            }
            else {
                if (v91 > v90) {
                    v102 = v90;
                    d102 = d90;
                }
                else {
                    v102 = v91;
                    d102 = d91;
                }
            }
        }
        else {
            if (v92 > v103) {
                if (v103 > v90) {
                    v102 = v90;
                    d102 = d90;
                }
                else {
                    v102 = v103;
                    d102 = d103;
                }
            }
            else {
                if (v92 > v90) {
                    v102 = v90;
                    d102 = d90;
                }
                else {
                    v102 = v92;
                    d102 = d92;
                }
            }
        }
        if (p102) v102 ++;
        else v102+= 1000000;
        if (v94 > v95) {
            if (v95 > v105) {
                if (v105 > v96) {
                    v106 = v96;
                    d106 = d96;
                }
                else {
                    v106 = v105;
                    d106 = d105;
                }
            }
            else {
                if (v95 > v96) {
                    v106 = v96;
                    d106 = d96;
                }
                else {
                    v106 = v95;
                    d106 = d95;
                }
            }
        }
        else {
            if (v94 > v105) {
                if (v105 > v96) {
                    v106 = v96;
                    d106 = d96;
                }
                else {
                    v106 = v105;
                    d106 = d105;
                }
            }
            else {
                if (v94 > v96) {
                    v106 = v96;
                    d106 = d96;
                }
                else {
                    v106 = v94;
                    d106 = d94;
                }
            }
        }
        if (p106) v106 ++;
        else v106+= 1000000;
    }


    static Direction getBestDirection(MapLocation target){
        if (rc.getLocation().distanceSquaredTo(target) <= 20 && computeDistance(target) > Constants.DIST_INF) return null;
        if (rc.getLocation().distanceSquaredTo(target) <= 2) return MovementManager.canMove(rc.getLocation().directionTo(target), DirtManager.canPassThroughDirt) ? rc.getLocation().directionTo(target) : null;
        int dx = target.x - l60.x;
        int dy = target.y - l60.y;
        switch(dx){
            case -4:
                switch(dy){
                    case -2: return d14;
                    case -1: return d15;
                    case 0: return d16;
                    case 1: return d17;
                    case 2: return d18;
                }
                break;
            case -3:
                switch(dy){
                    case -3: return d24;
                    case -2: return d25;
                    case -1: return d26;
                    case 0: return d27;
                    case 1: return d28;
                    case 2: return d29;
                    case 3: return d30;
                }
                break;
            case -2:
                switch(dy){
                    case -4: return d34;
                    case -3: return d35;
                    case -2: return d36;
                    case -1: return d37;
                    case 0: return d38;
                    case 1: return d39;
                    case 2: return d40;
                    case 3: return d41;
                    case 4: return d42;
                }
                break;
            case -1:
                switch(dy){
                    case -4: return d45;
                    case -3: return d46;
                    case -2: return d47;
                    case -1: return d48;
                    case 0: return d49;
                    case 1: return d50;
                    case 2: return d51;
                    case 3: return d52;
                    case 4: return d53;
                }
                break;
            case 0:
                switch(dy){
                    case -4: return d56;
                    case -3: return d57;
                    case -2: return d58;
                    case -1: return d59;
                    case 0: return d60;
                    case 1: return d61;
                    case 2: return d62;
                    case 3: return d63;
                    case 4: return d64;
                }
                break;
            case 1:
                switch(dy){
                    case -4: return d67;
                    case -3: return d68;
                    case -2: return d69;
                    case -1: return d70;
                    case 0: return d71;
                    case 1: return d72;
                    case 2: return d73;
                    case 3: return d74;
                    case 4: return d75;
                }
                break;
            case 2:
                switch(dy){
                    case -4: return d78;
                    case -3: return d79;
                    case -2: return d80;
                    case -1: return d81;
                    case 0: return d82;
                    case 1: return d83;
                    case 2: return d84;
                    case 3: return d85;
                    case 4: return d86;
                }
                break;
            case 3:
                switch(dy){
                    case -3: return d90;
                    case -2: return d91;
                    case -1: return d92;
                    case 0: return d93;
                    case 1: return d94;
                    case 2: return d95;
                    case 3: return d96;
                }
                break;
            case 4:
                switch(dy){
                    case -2: return d102;
                    case -1: return d103;
                    case 0: return d104;
                    case 1: return d105;
                    case 2: return d106;
                }
                break;
        }

        Direction ans = null;
        double bestEstimation = 0;
        double initialDist = Math.sqrt(l60.distanceSquaredTo(target));

        if (v14 < Constants.DIST_INF){
            double dist14 = (initialDist - Math.sqrt(l14.distanceSquaredTo(target)))/v14;
            if (dist14 > bestEstimation) {
                bestEstimation = dist14;
                ans = d14;
            }
        }
        if (v15 < Constants.DIST_INF){
            double dist15 = (initialDist - Math.sqrt(l15.distanceSquaredTo(target)))/v15;
            if (dist15 > bestEstimation) {
                bestEstimation = dist15;
                ans = d15;
            }
        }
        if (v16 < Constants.DIST_INF){
            double dist16 = (initialDist - Math.sqrt(l16.distanceSquaredTo(target)))/v16;
            if (dist16 > bestEstimation) {
                bestEstimation = dist16;
                ans = d16;
            }
        }
        if (v17 < Constants.DIST_INF){
            double dist17 = (initialDist - Math.sqrt(l17.distanceSquaredTo(target)))/v17;
            if (dist17 > bestEstimation) {
                bestEstimation = dist17;
                ans = d17;
            }
        }
        if (v18 < Constants.DIST_INF){
            double dist18 = (initialDist - Math.sqrt(l18.distanceSquaredTo(target)))/v18;
            if (dist18 > bestEstimation) {
                bestEstimation = dist18;
                ans = d18;
            }
        }
        if (v24 < Constants.DIST_INF){
            double dist24 = (initialDist - Math.sqrt(l24.distanceSquaredTo(target)))/v24;
            if (dist24 > bestEstimation) {
                bestEstimation = dist24;
                ans = d24;
            }
        }
        if (v25 < Constants.DIST_INF){
            double dist25 = (initialDist - Math.sqrt(l25.distanceSquaredTo(target)))/v25;
            if (dist25 > bestEstimation) {
                bestEstimation = dist25;
                ans = d25;
            }
        }
        if (v29 < Constants.DIST_INF){
            double dist29 = (initialDist - Math.sqrt(l29.distanceSquaredTo(target)))/v29;
            if (dist29 > bestEstimation) {
                bestEstimation = dist29;
                ans = d29;
            }
        }
        if (v30 < Constants.DIST_INF){
            double dist30 = (initialDist - Math.sqrt(l30.distanceSquaredTo(target)))/v30;
            if (dist30 > bestEstimation) {
                bestEstimation = dist30;
                ans = d30;
            }
        }
        if (v34 < Constants.DIST_INF){
            double dist34 = (initialDist - Math.sqrt(l34.distanceSquaredTo(target)))/v34;
            if (dist34 > bestEstimation) {
                bestEstimation = dist34;
                ans = d34;
            }
        }
        if (v35 < Constants.DIST_INF){
            double dist35 = (initialDist - Math.sqrt(l35.distanceSquaredTo(target)))/v35;
            if (dist35 > bestEstimation) {
                bestEstimation = dist35;
                ans = d35;
            }
        }
        if (v41 < Constants.DIST_INF){
            double dist41 = (initialDist - Math.sqrt(l41.distanceSquaredTo(target)))/v41;
            if (dist41 > bestEstimation) {
                bestEstimation = dist41;
                ans = d41;
            }
        }
        if (v42 < Constants.DIST_INF){
            double dist42 = (initialDist - Math.sqrt(l42.distanceSquaredTo(target)))/v42;
            if (dist42 > bestEstimation) {
                bestEstimation = dist42;
                ans = d42;
            }
        }
        if (v45 < Constants.DIST_INF){
            double dist45 = (initialDist - Math.sqrt(l45.distanceSquaredTo(target)))/v45;
            if (dist45 > bestEstimation) {
                bestEstimation = dist45;
                ans = d45;
            }
        }
        if (v53 < Constants.DIST_INF){
            double dist53 = (initialDist - Math.sqrt(l53.distanceSquaredTo(target)))/v53;
            if (dist53 > bestEstimation) {
                bestEstimation = dist53;
                ans = d53;
            }
        }
        if (v56 < Constants.DIST_INF){
            double dist56 = (initialDist - Math.sqrt(l56.distanceSquaredTo(target)))/v56;
            if (dist56 > bestEstimation) {
                bestEstimation = dist56;
                ans = d56;
            }
        }
        if (v64 < Constants.DIST_INF){
            double dist64 = (initialDist - Math.sqrt(l64.distanceSquaredTo(target)))/v64;
            if (dist64 > bestEstimation) {
                bestEstimation = dist64;
                ans = d64;
            }
        }
        if (v67 < Constants.DIST_INF){
            double dist67 = (initialDist - Math.sqrt(l67.distanceSquaredTo(target)))/v67;
            if (dist67 > bestEstimation) {
                bestEstimation = dist67;
                ans = d67;
            }
        }
        if (v75 < Constants.DIST_INF){
            double dist75 = (initialDist - Math.sqrt(l75.distanceSquaredTo(target)))/v75;
            if (dist75 > bestEstimation) {
                bestEstimation = dist75;
                ans = d75;
            }
        }
        if (v78 < Constants.DIST_INF){
            double dist78 = (initialDist - Math.sqrt(l78.distanceSquaredTo(target)))/v78;
            if (dist78 > bestEstimation) {
                bestEstimation = dist78;
                ans = d78;
            }
        }
        if (v79 < Constants.DIST_INF){
            double dist79 = (initialDist - Math.sqrt(l79.distanceSquaredTo(target)))/v79;
            if (dist79 > bestEstimation) {
                bestEstimation = dist79;
                ans = d79;
            }
        }
        if (v85 < Constants.DIST_INF){
            double dist85 = (initialDist - Math.sqrt(l85.distanceSquaredTo(target)))/v85;
            if (dist85 > bestEstimation) {
                bestEstimation = dist85;
                ans = d85;
            }
        }
        if (v86 < Constants.DIST_INF){
            double dist86 = (initialDist - Math.sqrt(l86.distanceSquaredTo(target)))/v86;
            if (dist86 > bestEstimation) {
                bestEstimation = dist86;
                ans = d86;
            }
        }
        if (v90 < Constants.DIST_INF){
            double dist90 = (initialDist - Math.sqrt(l90.distanceSquaredTo(target)))/v90;
            if (dist90 > bestEstimation) {
                bestEstimation = dist90;
                ans = d90;
            }
        }
        if (v91 < Constants.DIST_INF){
            double dist91 = (initialDist - Math.sqrt(l91.distanceSquaredTo(target)))/v91;
            if (dist91 > bestEstimation) {
                bestEstimation = dist91;
                ans = d91;
            }
        }
        if (v95 < Constants.DIST_INF){
            double dist95 = (initialDist - Math.sqrt(l95.distanceSquaredTo(target)))/v95;
            if (dist95 > bestEstimation) {
                bestEstimation = dist95;
                ans = d95;
            }
        }
        if (v96 < Constants.DIST_INF){
            double dist96 = (initialDist - Math.sqrt(l96.distanceSquaredTo(target)))/v96;
            if (dist96 > bestEstimation) {
                bestEstimation = dist96;
                ans = d96;
            }
        }
        if (v102 < Constants.DIST_INF){
            double dist102 = (initialDist - Math.sqrt(l102.distanceSquaredTo(target)))/v102;
            if (dist102 > bestEstimation) {
                bestEstimation = dist102;
                ans = d102;
            }
        }
        if (v103 < Constants.DIST_INF){
            double dist103 = (initialDist - Math.sqrt(l103.distanceSquaredTo(target)))/v103;
            if (dist103 > bestEstimation) {
                bestEstimation = dist103;
                ans = d103;
            }
        }
        if (v104 < Constants.DIST_INF){
            double dist104 = (initialDist - Math.sqrt(l104.distanceSquaredTo(target)))/v104;
            if (dist104 > bestEstimation) {
                bestEstimation = dist104;
                ans = d104;
            }
        }
        if (v105 < Constants.DIST_INF){
            double dist105 = (initialDist - Math.sqrt(l105.distanceSquaredTo(target)))/v105;
            if (dist105 > bestEstimation) {
                bestEstimation = dist105;
                ans = d105;
            }
        }
        if (v106 < Constants.DIST_INF){
            double dist106 = (initialDist - Math.sqrt(l106.distanceSquaredTo(target)))/v106;
            if (dist106 > bestEstimation) {
                bestEstimation = dist106;
                ans = d106;
            }
        }
        return ans;
    }

    static int computeRealDistance(MapLocation target){
        return 0;
    }

    static int computeDistance(MapLocation target){
        if (target.distanceSquaredTo(MovementManager.myLoc) <= 2) return 1;
        if (MyUnit.isKing) return MovementManager.myLoc.distanceSquaredTo(target);
        int dx = target.x - rc.getLocation().x;
        int dy = target.y - rc.getLocation().y;
        switch(dx){
            case -4:
                switch(dy){
                    case -2: return v14;
                    case -1: return v15;
                    case 0: return v16;
                    case 1: return v17;
                    case 2: return v18;
                }
                break;
            case -3:
                switch(dy){
                    case -3: return v24;
                    case -2: return v25;
                    case -1: return v26;
                    case 0: return v27;
                    case 1: return v28;
                    case 2: return v29;
                    case 3: return v30;
                }
                break;
            case -2:
                switch(dy){
                    case -4: return v34;
                    case -3: return v35;
                    case -2: return v36;
                    case -1: return v37;
                    case 0: return v38;
                    case 1: return v39;
                    case 2: return v40;
                    case 3: return v41;
                    case 4: return v42;
                }
                break;
            case -1:
                switch(dy){
                    case -4: return v45;
                    case -3: return v46;
                    case -2: return v47;
                    case -1: return v48;
                    case 0: return v49;
                    case 1: return v50;
                    case 2: return v51;
                    case 3: return v52;
                    case 4: return v53;
                }
                break;
            case 0:
                switch(dy){
                    case -4: return v56;
                    case -3: return v57;
                    case -2: return v58;
                    case -1: return v59;
                    case 0: return v60;
                    case 1: return v61;
                    case 2: return v62;
                    case 3: return v63;
                    case 4: return v64;
                }
                break;
            case 1:
                switch(dy){
                    case -4: return v67;
                    case -3: return v68;
                    case -2: return v69;
                    case -1: return v70;
                    case 0: return v71;
                    case 1: return v72;
                    case 2: return v73;
                    case 3: return v74;
                    case 4: return v75;
                }
                break;
            case 2:
                switch(dy){
                    case -4: return v78;
                    case -3: return v79;
                    case -2: return v80;
                    case -1: return v81;
                    case 0: return v82;
                    case 1: return v83;
                    case 2: return v84;
                    case 3: return v85;
                    case 4: return v86;
                }
                break;
            case 3:
                switch(dy){
                    case -3: return v90;
                    case -2: return v91;
                    case -1: return v92;
                    case 0: return v93;
                    case 1: return v94;
                    case 2: return v95;
                    case 3: return v96;
                }
                break;
            case 4:
                switch(dy){
                    case -2: return v102;
                    case -1: return v103;
                    case 0: return v104;
                    case 1: return v105;
                    case 2: return v106;
                }
                break;
        }
        return 1000000;
    }

}
