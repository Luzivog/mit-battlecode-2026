package awubot;

import battlecode.common.*;
import static awubot.util.Util.*;
import awubot.util.Profiler;
import static awubot.Game.*;



class BugnavInstance {
    MapLocation target;
    // false for the instances spawned when guessing
    boolean isMaster;
    // i guessed last turn, so i don't need to guess again
    boolean justGuessed = false;

    Direction bottomDir, topDir;

    // state (you can reconstruct the entire state from these variables + target/bottomDir)
    MapLocation pos;
    int stackSize;
    int emptyStackTurns;
    boolean leftTurn;

    public int pathLength;
    
    
    public MapLocation path0;
    int stackSize0;
    int emptyStackTurns0;
    boolean leftTurn0;
    
    public MapLocation path1;
    int stackSize1;
    int emptyStackTurns1;
    boolean leftTurn1;
    
    public MapLocation path2;
    int stackSize2;
    int emptyStackTurns2;
    boolean leftTurn2;
    
    public MapLocation path3;
    int stackSize3;
    int emptyStackTurns3;
    boolean leftTurn3;
    
    public MapLocation path4;
    int stackSize4;
    int emptyStackTurns4;
    boolean leftTurn4;
    
    public MapLocation path5;
    int stackSize5;
    int emptyStackTurns5;
    boolean leftTurn5;
    
    public MapLocation path6;
    int stackSize6;
    int emptyStackTurns6;
    boolean leftTurn6;
    
    public MapLocation path7;
    int stackSize7;
    int emptyStackTurns7;
    boolean leftTurn7;
    
    public MapLocation path8;
    int stackSize8;
    int emptyStackTurns8;
    boolean leftTurn8;
    
    public MapLocation path9;
    int stackSize9;
    int emptyStackTurns9;
    boolean leftTurn9;
    
    public MapLocation path10;
    int stackSize10;
    int emptyStackTurns10;
    boolean leftTurn10;
    
    public MapLocation path11;
    int stackSize11;
    int emptyStackTurns11;
    boolean leftTurn11;
    
    public MapLocation path12;
    int stackSize12;
    int emptyStackTurns12;
    boolean leftTurn12;
    
    public MapLocation path13;
    int stackSize13;
    int emptyStackTurns13;
    boolean leftTurn13;
    
    public MapLocation path14;
    int stackSize14;
    int emptyStackTurns14;
    boolean leftTurn14;
    
    public MapLocation path15;
    int stackSize15;
    int emptyStackTurns15;
    boolean leftTurn15;
    
    public MapLocation path16;
    int stackSize16;
    int emptyStackTurns16;
    boolean leftTurn16;
    
    public MapLocation path17;
    int stackSize17;
    int emptyStackTurns17;
    boolean leftTurn17;
    
    public MapLocation path18;
    int stackSize18;
    int emptyStackTurns18;
    boolean leftTurn18;
    
    public MapLocation path19;
    int stackSize19;
    int emptyStackTurns19;
    boolean leftTurn19;
    
    public MapLocation path20;
    int stackSize20;
    int emptyStackTurns20;
    boolean leftTurn20;
    
    public MapLocation path21;
    int stackSize21;
    int emptyStackTurns21;
    boolean leftTurn21;
    
    public MapLocation path22;
    int stackSize22;
    int emptyStackTurns22;
    boolean leftTurn22;
    
    public MapLocation path23;
    int stackSize23;
    int emptyStackTurns23;
    boolean leftTurn23;
    
    public MapLocation path24;
    int stackSize24;
    int emptyStackTurns24;
    boolean leftTurn24;
    
    public MapLocation path25;
    int stackSize25;
    int emptyStackTurns25;
    boolean leftTurn25;
    
    public MapLocation path26;
    int stackSize26;
    int emptyStackTurns26;
    boolean leftTurn26;
    
    public MapLocation path27;
    int stackSize27;
    int emptyStackTurns27;
    boolean leftTurn27;
    
    public MapLocation path28;
    int stackSize28;
    int emptyStackTurns28;
    boolean leftTurn28;
    
    public MapLocation path29;
    int stackSize29;
    int emptyStackTurns29;
    boolean leftTurn29;
    
    public MapLocation path30;
    int stackSize30;
    int emptyStackTurns30;
    boolean leftTurn30;
    
    public MapLocation path31;
    int stackSize31;
    int emptyStackTurns31;
    boolean leftTurn31;
    
    public MapLocation path32;
    int stackSize32;
    int emptyStackTurns32;
    boolean leftTurn32;
    
    public MapLocation path33;
    int stackSize33;
    int emptyStackTurns33;
    boolean leftTurn33;
    
    public MapLocation path34;
    int stackSize34;
    int emptyStackTurns34;
    boolean leftTurn34;
    
    public MapLocation path35;
    int stackSize35;
    int emptyStackTurns35;
    boolean leftTurn35;
    
    public MapLocation path36;
    int stackSize36;
    int emptyStackTurns36;
    boolean leftTurn36;
    
    public MapLocation path37;
    int stackSize37;
    int emptyStackTurns37;
    boolean leftTurn37;
    
    public MapLocation path38;
    int stackSize38;
    int emptyStackTurns38;
    boolean leftTurn38;
    
    public MapLocation path39;
    int stackSize39;
    int emptyStackTurns39;
    boolean leftTurn39;
    
    public MapLocation path40;
    int stackSize40;
    int emptyStackTurns40;
    boolean leftTurn40;
    
    public MapLocation path41;
    int stackSize41;
    int emptyStackTurns41;
    boolean leftTurn41;
    
    public MapLocation path42;
    int stackSize42;
    int emptyStackTurns42;
    boolean leftTurn42;
    
    public MapLocation path43;
    int stackSize43;
    int emptyStackTurns43;
    boolean leftTurn43;
    
    public MapLocation path44;
    int stackSize44;
    int emptyStackTurns44;
    boolean leftTurn44;
    
    public MapLocation path45;
    int stackSize45;
    int emptyStackTurns45;
    boolean leftTurn45;
    
    public MapLocation path46;
    int stackSize46;
    int emptyStackTurns46;
    boolean leftTurn46;
    
    public MapLocation path47;
    int stackSize47;
    int emptyStackTurns47;
    boolean leftTurn47;
    
    public MapLocation path48;
    int stackSize48;
    int emptyStackTurns48;
    boolean leftTurn48;
    
    public MapLocation path49;
    int stackSize49;
    int emptyStackTurns49;
    boolean leftTurn49;
    
    public MapLocation path50;
    int stackSize50;
    int emptyStackTurns50;
    boolean leftTurn50;
    
    public MapLocation path51;
    int stackSize51;
    int emptyStackTurns51;
    boolean leftTurn51;
    
    public MapLocation path52;
    int stackSize52;
    int emptyStackTurns52;
    boolean leftTurn52;
    
    public MapLocation path53;
    int stackSize53;
    int emptyStackTurns53;
    boolean leftTurn53;
    
    public MapLocation path54;
    int stackSize54;
    int emptyStackTurns54;
    boolean leftTurn54;
    
    public MapLocation path55;
    int stackSize55;
    int emptyStackTurns55;
    boolean leftTurn55;
    
    public MapLocation path56;
    int stackSize56;
    int emptyStackTurns56;
    boolean leftTurn56;
    
    public MapLocation path57;
    int stackSize57;
    int emptyStackTurns57;
    boolean leftTurn57;
    
    public MapLocation path58;
    int stackSize58;
    int emptyStackTurns58;
    boolean leftTurn58;
    
    public MapLocation path59;
    int stackSize59;
    int emptyStackTurns59;
    boolean leftTurn59;
    
    public MapLocation path60;
    int stackSize60;
    int emptyStackTurns60;
    boolean leftTurn60;
    
    public MapLocation path61;
    int stackSize61;
    int emptyStackTurns61;
    boolean leftTurn61;
    
    public MapLocation path62;
    int stackSize62;
    int emptyStackTurns62;
    boolean leftTurn62;
    
    public MapLocation path63;
    int stackSize63;
    int emptyStackTurns63;
    boolean leftTurn63;
    
    public MapLocation path64;
    int stackSize64;
    int emptyStackTurns64;
    boolean leftTurn64;
    
    public MapLocation path65;
    int stackSize65;
    int emptyStackTurns65;
    boolean leftTurn65;
    
    public MapLocation path66;
    int stackSize66;
    int emptyStackTurns66;
    boolean leftTurn66;
    
    public MapLocation path67;
    int stackSize67;
    int emptyStackTurns67;
    boolean leftTurn67;
    
    public MapLocation path68;
    int stackSize68;
    int emptyStackTurns68;
    boolean leftTurn68;
    
    public MapLocation path69;
    int stackSize69;
    int emptyStackTurns69;
    boolean leftTurn69;
    
    public MapLocation path70;
    int stackSize70;
    int emptyStackTurns70;
    boolean leftTurn70;
    
    public MapLocation path71;
    int stackSize71;
    int emptyStackTurns71;
    boolean leftTurn71;
    
    public MapLocation path72;
    int stackSize72;
    int emptyStackTurns72;
    boolean leftTurn72;
    
    public MapLocation path73;
    int stackSize73;
    int emptyStackTurns73;
    boolean leftTurn73;
    
    public MapLocation path74;
    int stackSize74;
    int emptyStackTurns74;
    boolean leftTurn74;
    
    public MapLocation path75;
    int stackSize75;
    int emptyStackTurns75;
    boolean leftTurn75;
    
    public MapLocation path76;
    int stackSize76;
    int emptyStackTurns76;
    boolean leftTurn76;
    
    public MapLocation path77;
    int stackSize77;
    int emptyStackTurns77;
    boolean leftTurn77;
    
    public MapLocation path78;
    int stackSize78;
    int emptyStackTurns78;
    boolean leftTurn78;
    
    public MapLocation path79;
    int stackSize79;
    int emptyStackTurns79;
    boolean leftTurn79;
    
    public MapLocation path80;
    int stackSize80;
    int emptyStackTurns80;
    boolean leftTurn80;
    
    public MapLocation path81;
    int stackSize81;
    int emptyStackTurns81;
    boolean leftTurn81;
    
    public MapLocation path82;
    int stackSize82;
    int emptyStackTurns82;
    boolean leftTurn82;
    
    public MapLocation path83;
    int stackSize83;
    int emptyStackTurns83;
    boolean leftTurn83;
    
    public MapLocation path84;
    int stackSize84;
    int emptyStackTurns84;
    boolean leftTurn84;
    
    public MapLocation path85;
    int stackSize85;
    int emptyStackTurns85;
    boolean leftTurn85;
    
    public MapLocation path86;
    int stackSize86;
    int emptyStackTurns86;
    boolean leftTurn86;
    
    public MapLocation path87;
    int stackSize87;
    int emptyStackTurns87;
    boolean leftTurn87;
    
    public MapLocation path88;
    int stackSize88;
    int emptyStackTurns88;
    boolean leftTurn88;
    
    public MapLocation path89;
    int stackSize89;
    int emptyStackTurns89;
    boolean leftTurn89;
    
    public MapLocation path90;
    int stackSize90;
    int emptyStackTurns90;
    boolean leftTurn90;
    
    public MapLocation path91;
    int stackSize91;
    int emptyStackTurns91;
    boolean leftTurn91;
    
    public MapLocation path92;
    int stackSize92;
    int emptyStackTurns92;
    boolean leftTurn92;
    
    public MapLocation path93;
    int stackSize93;
    int emptyStackTurns93;
    boolean leftTurn93;
    
    public MapLocation path94;
    int stackSize94;
    int emptyStackTurns94;
    boolean leftTurn94;
    
    public MapLocation path95;
    int stackSize95;
    int emptyStackTurns95;
    boolean leftTurn95;
    
    public MapLocation path96;
    int stackSize96;
    int emptyStackTurns96;
    boolean leftTurn96;
    
    public MapLocation path97;
    int stackSize97;
    int emptyStackTurns97;
    boolean leftTurn97;
    
    public MapLocation path98;
    int stackSize98;
    int emptyStackTurns98;
    boolean leftTurn98;
    
    public MapLocation path99;
    int stackSize99;
    int emptyStackTurns99;
    boolean leftTurn99;
    
    public MapLocation path100;
    int stackSize100;
    int emptyStackTurns100;
    boolean leftTurn100;
    
    public MapLocation path101;
    int stackSize101;
    int emptyStackTurns101;
    boolean leftTurn101;
    
    public MapLocation path102;
    int stackSize102;
    int emptyStackTurns102;
    boolean leftTurn102;
    
    public MapLocation path103;
    int stackSize103;
    int emptyStackTurns103;
    boolean leftTurn103;
    
    public MapLocation path104;
    int stackSize104;
    int emptyStackTurns104;
    boolean leftTurn104;
    
    public MapLocation path105;
    int stackSize105;
    int emptyStackTurns105;
    boolean leftTurn105;
    
    public MapLocation path106;
    int stackSize106;
    int emptyStackTurns106;
    boolean leftTurn106;
    
    public MapLocation path107;
    int stackSize107;
    int emptyStackTurns107;
    boolean leftTurn107;
    
    public MapLocation path108;
    int stackSize108;
    int emptyStackTurns108;
    boolean leftTurn108;
    
    public MapLocation path109;
    int stackSize109;
    int emptyStackTurns109;
    boolean leftTurn109;
    
    public MapLocation path110;
    int stackSize110;
    int emptyStackTurns110;
    boolean leftTurn110;
    
    public MapLocation path111;
    int stackSize111;
    int emptyStackTurns111;
    boolean leftTurn111;
    
    public MapLocation path112;
    int stackSize112;
    int emptyStackTurns112;
    boolean leftTurn112;
    
    public MapLocation path113;
    int stackSize113;
    int emptyStackTurns113;
    boolean leftTurn113;
    
    public MapLocation path114;
    int stackSize114;
    int emptyStackTurns114;
    boolean leftTurn114;
    
    public MapLocation path115;
    int stackSize115;
    int emptyStackTurns115;
    boolean leftTurn115;
    
    public MapLocation path116;
    int stackSize116;
    int emptyStackTurns116;
    boolean leftTurn116;
    
    public MapLocation path117;
    int stackSize117;
    int emptyStackTurns117;
    boolean leftTurn117;
    
    public MapLocation path118;
    int stackSize118;
    int emptyStackTurns118;
    boolean leftTurn118;
    
    public MapLocation path119;
    int stackSize119;
    int emptyStackTurns119;
    boolean leftTurn119;
    
    public MapLocation path120;
    int stackSize120;
    int emptyStackTurns120;
    boolean leftTurn120;
    
    public MapLocation path121;
    int stackSize121;
    int emptyStackTurns121;
    boolean leftTurn121;
    
    public MapLocation path122;
    int stackSize122;
    int emptyStackTurns122;
    boolean leftTurn122;
    
    public MapLocation path123;
    int stackSize123;
    int emptyStackTurns123;
    boolean leftTurn123;
    
    public MapLocation path124;
    int stackSize124;
    int emptyStackTurns124;
    boolean leftTurn124;
    
    public MapLocation path125;
    int stackSize125;
    int emptyStackTurns125;
    boolean leftTurn125;
    
    public MapLocation path126;
    int stackSize126;
    int emptyStackTurns126;
    boolean leftTurn126;
    
    public MapLocation path127;
    int stackSize127;
    int emptyStackTurns127;
    boolean leftTurn127;
    
    public MapLocation path128;
    int stackSize128;
    int emptyStackTurns128;
    boolean leftTurn128;
    
    public MapLocation path129;
    int stackSize129;
    int emptyStackTurns129;
    boolean leftTurn129;
    
    public MapLocation path130;
    int stackSize130;
    int emptyStackTurns130;
    boolean leftTurn130;
    
    public MapLocation path131;
    int stackSize131;
    int emptyStackTurns131;
    boolean leftTurn131;
    
    public MapLocation path132;
    int stackSize132;
    int emptyStackTurns132;
    boolean leftTurn132;
    
    public MapLocation path133;
    int stackSize133;
    int emptyStackTurns133;
    boolean leftTurn133;
    
    public MapLocation path134;
    int stackSize134;
    int emptyStackTurns134;
    boolean leftTurn134;
    
    public MapLocation path135;
    int stackSize135;
    int emptyStackTurns135;
    boolean leftTurn135;
    
    public MapLocation path136;
    int stackSize136;
    int emptyStackTurns136;
    boolean leftTurn136;
    
    public MapLocation path137;
    int stackSize137;
    int emptyStackTurns137;
    boolean leftTurn137;
    
    public MapLocation path138;
    int stackSize138;
    int emptyStackTurns138;
    boolean leftTurn138;
    
    public MapLocation path139;
    int stackSize139;
    int emptyStackTurns139;
    boolean leftTurn139;
    
    public MapLocation path140;
    int stackSize140;
    int emptyStackTurns140;
    boolean leftTurn140;
    
    public MapLocation path141;
    int stackSize141;
    int emptyStackTurns141;
    boolean leftTurn141;
    
    public MapLocation path142;
    int stackSize142;
    int emptyStackTurns142;
    boolean leftTurn142;
    
    public MapLocation path143;
    int stackSize143;
    int emptyStackTurns143;
    boolean leftTurn143;
    
    public MapLocation path144;
    int stackSize144;
    int emptyStackTurns144;
    boolean leftTurn144;
    
    public MapLocation path145;
    int stackSize145;
    int emptyStackTurns145;
    boolean leftTurn145;
    
    public MapLocation path146;
    int stackSize146;
    int emptyStackTurns146;
    boolean leftTurn146;
    
    public MapLocation path147;
    int stackSize147;
    int emptyStackTurns147;
    boolean leftTurn147;
    
    public MapLocation path148;
    int stackSize148;
    int emptyStackTurns148;
    boolean leftTurn148;
    
    public MapLocation path149;
    int stackSize149;
    int emptyStackTurns149;
    boolean leftTurn149;
    
    public MapLocation path150;
    int stackSize150;
    int emptyStackTurns150;
    boolean leftTurn150;
    
    public MapLocation path151;
    int stackSize151;
    int emptyStackTurns151;
    boolean leftTurn151;
    
    public MapLocation path152;
    int stackSize152;
    int emptyStackTurns152;
    boolean leftTurn152;
    
    public MapLocation path153;
    int stackSize153;
    int emptyStackTurns153;
    boolean leftTurn153;
    
    public MapLocation path154;
    int stackSize154;
    int emptyStackTurns154;
    boolean leftTurn154;
    
    public MapLocation path155;
    int stackSize155;
    int emptyStackTurns155;
    boolean leftTurn155;
    
    public MapLocation path156;
    int stackSize156;
    int emptyStackTurns156;
    boolean leftTurn156;
    
    public MapLocation path157;
    int stackSize157;
    int emptyStackTurns157;
    boolean leftTurn157;
    
    public MapLocation path158;
    int stackSize158;
    int emptyStackTurns158;
    boolean leftTurn158;
    
    public MapLocation path159;
    int stackSize159;
    int emptyStackTurns159;
    boolean leftTurn159;
    
    public MapLocation path160;
    int stackSize160;
    int emptyStackTurns160;
    boolean leftTurn160;
    
    public MapLocation path161;
    int stackSize161;
    int emptyStackTurns161;
    boolean leftTurn161;
    
    public MapLocation path162;
    int stackSize162;
    int emptyStackTurns162;
    boolean leftTurn162;
    
    public MapLocation path163;
    int stackSize163;
    int emptyStackTurns163;
    boolean leftTurn163;
    
    public MapLocation path164;
    int stackSize164;
    int emptyStackTurns164;
    boolean leftTurn164;
    
    public MapLocation path165;
    int stackSize165;
    int emptyStackTurns165;
    boolean leftTurn165;
    
    public MapLocation path166;
    int stackSize166;
    int emptyStackTurns166;
    boolean leftTurn166;
    
    public MapLocation path167;
    int stackSize167;
    int emptyStackTurns167;
    boolean leftTurn167;
    
    public MapLocation path168;
    int stackSize168;
    int emptyStackTurns168;
    boolean leftTurn168;
    
    public MapLocation path169;
    int stackSize169;
    int emptyStackTurns169;
    boolean leftTurn169;
    
    public MapLocation path170;
    int stackSize170;
    int emptyStackTurns170;
    boolean leftTurn170;
    
    public MapLocation path171;
    int stackSize171;
    int emptyStackTurns171;
    boolean leftTurn171;
    
    public MapLocation path172;
    int stackSize172;
    int emptyStackTurns172;
    boolean leftTurn172;
    
    public MapLocation path173;
    int stackSize173;
    int emptyStackTurns173;
    boolean leftTurn173;
    
    public MapLocation path174;
    int stackSize174;
    int emptyStackTurns174;
    boolean leftTurn174;
    
    public MapLocation path175;
    int stackSize175;
    int emptyStackTurns175;
    boolean leftTurn175;
    
    public MapLocation path176;
    int stackSize176;
    int emptyStackTurns176;
    boolean leftTurn176;
    
    public MapLocation path177;
    int stackSize177;
    int emptyStackTurns177;
    boolean leftTurn177;
    
    public MapLocation path178;
    int stackSize178;
    int emptyStackTurns178;
    boolean leftTurn178;
    
    public MapLocation path179;
    int stackSize179;
    int emptyStackTurns179;
    boolean leftTurn179;
    
    public MapLocation path180;
    int stackSize180;
    int emptyStackTurns180;
    boolean leftTurn180;
    
    public MapLocation path181;
    int stackSize181;
    int emptyStackTurns181;
    boolean leftTurn181;
    
    public MapLocation path182;
    int stackSize182;
    int emptyStackTurns182;
    boolean leftTurn182;
    
    public MapLocation path183;
    int stackSize183;
    int emptyStackTurns183;
    boolean leftTurn183;
    
    public MapLocation path184;
    int stackSize184;
    int emptyStackTurns184;
    boolean leftTurn184;
    
    public MapLocation path185;
    int stackSize185;
    int emptyStackTurns185;
    boolean leftTurn185;
    
    public MapLocation path186;
    int stackSize186;
    int emptyStackTurns186;
    boolean leftTurn186;
    
    public MapLocation path187;
    int stackSize187;
    int emptyStackTurns187;
    boolean leftTurn187;
    
    public MapLocation path188;
    int stackSize188;
    int emptyStackTurns188;
    boolean leftTurn188;
    
    public MapLocation path189;
    int stackSize189;
    int emptyStackTurns189;
    boolean leftTurn189;
    
    public MapLocation path190;
    int stackSize190;
    int emptyStackTurns190;
    boolean leftTurn190;
    
    public MapLocation path191;
    int stackSize191;
    int emptyStackTurns191;
    boolean leftTurn191;
    
    public MapLocation path192;
    int stackSize192;
    int emptyStackTurns192;
    boolean leftTurn192;
    
    public MapLocation path193;
    int stackSize193;
    int emptyStackTurns193;
    boolean leftTurn193;
    
    public MapLocation path194;
    int stackSize194;
    int emptyStackTurns194;
    boolean leftTurn194;
    
    public MapLocation path195;
    int stackSize195;
    int emptyStackTurns195;
    boolean leftTurn195;
    
    public MapLocation path196;
    int stackSize196;
    int emptyStackTurns196;
    boolean leftTurn196;
    
    public MapLocation path197;
    int stackSize197;
    int emptyStackTurns197;
    boolean leftTurn197;
    
    public MapLocation path198;
    int stackSize198;
    int emptyStackTurns198;
    boolean leftTurn198;
    
    public MapLocation path199;
    int stackSize199;
    int emptyStackTurns199;
    boolean leftTurn199;
    
    public MapLocation path200;
    int stackSize200;
    int emptyStackTurns200;
    boolean leftTurn200;
    
    public MapLocation path201;
    int stackSize201;
    int emptyStackTurns201;
    boolean leftTurn201;
    
    public MapLocation path202;
    int stackSize202;
    int emptyStackTurns202;
    boolean leftTurn202;
    
    public MapLocation path203;
    int stackSize203;
    int emptyStackTurns203;
    boolean leftTurn203;
    
    public MapLocation path204;
    int stackSize204;
    int emptyStackTurns204;
    boolean leftTurn204;
    
    public MapLocation path205;
    int stackSize205;
    int emptyStackTurns205;
    boolean leftTurn205;
    
    public MapLocation path206;
    int stackSize206;
    int emptyStackTurns206;
    boolean leftTurn206;
    
    public MapLocation path207;
    int stackSize207;
    int emptyStackTurns207;
    boolean leftTurn207;
    
    public MapLocation path208;
    int stackSize208;
    int emptyStackTurns208;
    boolean leftTurn208;
    
    public MapLocation path209;
    int stackSize209;
    int emptyStackTurns209;
    boolean leftTurn209;
    
    public MapLocation path210;
    int stackSize210;
    int emptyStackTurns210;
    boolean leftTurn210;
    
    public MapLocation path211;
    int stackSize211;
    int emptyStackTurns211;
    boolean leftTurn211;
    
    public MapLocation path212;
    int stackSize212;
    int emptyStackTurns212;
    boolean leftTurn212;
    
    public MapLocation path213;
    int stackSize213;
    int emptyStackTurns213;
    boolean leftTurn213;
    
    public MapLocation path214;
    int stackSize214;
    int emptyStackTurns214;
    boolean leftTurn214;
    
    public MapLocation path215;
    int stackSize215;
    int emptyStackTurns215;
    boolean leftTurn215;
    
    public MapLocation path216;
    int stackSize216;
    int emptyStackTurns216;
    boolean leftTurn216;
    
    public MapLocation path217;
    int stackSize217;
    int emptyStackTurns217;
    boolean leftTurn217;
    
    public MapLocation path218;
    int stackSize218;
    int emptyStackTurns218;
    boolean leftTurn218;
    
    public MapLocation path219;
    int stackSize219;
    int emptyStackTurns219;
    boolean leftTurn219;
    
    public MapLocation path220;
    int stackSize220;
    int emptyStackTurns220;
    boolean leftTurn220;
    
    public MapLocation path221;
    int stackSize221;
    int emptyStackTurns221;
    boolean leftTurn221;
    
    public MapLocation path222;
    int stackSize222;
    int emptyStackTurns222;
    boolean leftTurn222;
    
    public MapLocation path223;
    int stackSize223;
    int emptyStackTurns223;
    boolean leftTurn223;
    
    public MapLocation path224;
    int stackSize224;
    int emptyStackTurns224;
    boolean leftTurn224;
    
    public MapLocation path225;
    int stackSize225;
    int emptyStackTurns225;
    boolean leftTurn225;
    
    public MapLocation path226;
    int stackSize226;
    int emptyStackTurns226;
    boolean leftTurn226;
    
    public MapLocation path227;
    int stackSize227;
    int emptyStackTurns227;
    boolean leftTurn227;
    
    public MapLocation path228;
    int stackSize228;
    int emptyStackTurns228;
    boolean leftTurn228;
    
    public MapLocation path229;
    int stackSize229;
    int emptyStackTurns229;
    boolean leftTurn229;
    
    public MapLocation path230;
    int stackSize230;
    int emptyStackTurns230;
    boolean leftTurn230;
    
    public MapLocation path231;
    int stackSize231;
    int emptyStackTurns231;
    boolean leftTurn231;
    
    public MapLocation path232;
    int stackSize232;
    int emptyStackTurns232;
    boolean leftTurn232;
    
    public MapLocation path233;
    int stackSize233;
    int emptyStackTurns233;
    boolean leftTurn233;
    
    public MapLocation path234;
    int stackSize234;
    int emptyStackTurns234;
    boolean leftTurn234;
    
    public MapLocation path235;
    int stackSize235;
    int emptyStackTurns235;
    boolean leftTurn235;
    
    public MapLocation path236;
    int stackSize236;
    int emptyStackTurns236;
    boolean leftTurn236;
    
    public MapLocation path237;
    int stackSize237;
    int emptyStackTurns237;
    boolean leftTurn237;
    
    public MapLocation path238;
    int stackSize238;
    int emptyStackTurns238;
    boolean leftTurn238;
    
    public MapLocation path239;
    int stackSize239;
    int emptyStackTurns239;
    boolean leftTurn239;
    
    public MapLocation path240;
    int stackSize240;
    int emptyStackTurns240;
    boolean leftTurn240;
    
    public MapLocation path241;
    int stackSize241;
    int emptyStackTurns241;
    boolean leftTurn241;
    
    public MapLocation path242;
    int stackSize242;
    int emptyStackTurns242;
    boolean leftTurn242;
    
    public MapLocation path243;
    int stackSize243;
    int emptyStackTurns243;
    boolean leftTurn243;
    
    public MapLocation path244;
    int stackSize244;
    int emptyStackTurns244;
    boolean leftTurn244;
    
    public MapLocation path245;
    int stackSize245;
    int emptyStackTurns245;
    boolean leftTurn245;
    
    public MapLocation path246;
    int stackSize246;
    int emptyStackTurns246;
    boolean leftTurn246;
    
    public MapLocation path247;
    int stackSize247;
    int emptyStackTurns247;
    boolean leftTurn247;
    
    public MapLocation path248;
    int stackSize248;
    int emptyStackTurns248;
    boolean leftTurn248;
    
    public MapLocation path249;
    int stackSize249;
    int emptyStackTurns249;
    boolean leftTurn249;
    
    public MapLocation path250;
    int stackSize250;
    int emptyStackTurns250;
    boolean leftTurn250;
    
    public MapLocation path251;
    int stackSize251;
    int emptyStackTurns251;
    boolean leftTurn251;
    
    public MapLocation path252;
    int stackSize252;
    int emptyStackTurns252;
    boolean leftTurn252;
    
    public MapLocation path253;
    int stackSize253;
    int emptyStackTurns253;
    boolean leftTurn253;
    
    public MapLocation path254;
    int stackSize254;
    int emptyStackTurns254;
    boolean leftTurn254;
    
    public MapLocation path255;
    int stackSize255;
    int emptyStackTurns255;
    boolean leftTurn255;
    
    public MapLocation path256;
    int stackSize256;
    int emptyStackTurns256;
    boolean leftTurn256;
    
    public MapLocation path257;
    int stackSize257;
    int emptyStackTurns257;
    boolean leftTurn257;
    
    public MapLocation path258;
    int stackSize258;
    int emptyStackTurns258;
    boolean leftTurn258;
    
    public MapLocation path259;
    int stackSize259;
    int emptyStackTurns259;
    boolean leftTurn259;
    
    public MapLocation path260;
    int stackSize260;
    int emptyStackTurns260;
    boolean leftTurn260;
    
    public MapLocation path261;
    int stackSize261;
    int emptyStackTurns261;
    boolean leftTurn261;
    
    public MapLocation path262;
    int stackSize262;
    int emptyStackTurns262;
    boolean leftTurn262;
    
    public MapLocation path263;
    int stackSize263;
    int emptyStackTurns263;
    boolean leftTurn263;
    
    public MapLocation path264;
    int stackSize264;
    int emptyStackTurns264;
    boolean leftTurn264;
    
    public MapLocation path265;
    int stackSize265;
    int emptyStackTurns265;
    boolean leftTurn265;
    
    public MapLocation path266;
    int stackSize266;
    int emptyStackTurns266;
    boolean leftTurn266;
    
    public MapLocation path267;
    int stackSize267;
    int emptyStackTurns267;
    boolean leftTurn267;
    
    public MapLocation path268;
    int stackSize268;
    int emptyStackTurns268;
    boolean leftTurn268;
    
    public MapLocation path269;
    int stackSize269;
    int emptyStackTurns269;
    boolean leftTurn269;
    
    public MapLocation path270;
    int stackSize270;
    int emptyStackTurns270;
    boolean leftTurn270;
    
    public MapLocation path271;
    int stackSize271;
    int emptyStackTurns271;
    boolean leftTurn271;
    
    public MapLocation path272;
    int stackSize272;
    int emptyStackTurns272;
    boolean leftTurn272;
    
    public MapLocation path273;
    int stackSize273;
    int emptyStackTurns273;
    boolean leftTurn273;
    
    public MapLocation path274;
    int stackSize274;
    int emptyStackTurns274;
    boolean leftTurn274;
    
    public MapLocation path275;
    int stackSize275;
    int emptyStackTurns275;
    boolean leftTurn275;
    
    public MapLocation path276;
    int stackSize276;
    int emptyStackTurns276;
    boolean leftTurn276;
    
    public MapLocation path277;
    int stackSize277;
    int emptyStackTurns277;
    boolean leftTurn277;
    
    public MapLocation path278;
    int stackSize278;
    int emptyStackTurns278;
    boolean leftTurn278;
    
    public MapLocation path279;
    int stackSize279;
    int emptyStackTurns279;
    boolean leftTurn279;
    
    public MapLocation path280;
    int stackSize280;
    int emptyStackTurns280;
    boolean leftTurn280;
    
    public MapLocation path281;
    int stackSize281;
    int emptyStackTurns281;
    boolean leftTurn281;
    
    public MapLocation path282;
    int stackSize282;
    int emptyStackTurns282;
    boolean leftTurn282;
    
    public MapLocation path283;
    int stackSize283;
    int emptyStackTurns283;
    boolean leftTurn283;
    
    public MapLocation path284;
    int stackSize284;
    int emptyStackTurns284;
    boolean leftTurn284;
    
    public MapLocation path285;
    int stackSize285;
    int emptyStackTurns285;
    boolean leftTurn285;
    
    public MapLocation path286;
    int stackSize286;
    int emptyStackTurns286;
    boolean leftTurn286;
    
    public MapLocation path287;
    int stackSize287;
    int emptyStackTurns287;
    boolean leftTurn287;
    
    public MapLocation path288;
    int stackSize288;
    int emptyStackTurns288;
    boolean leftTurn288;
    
    public MapLocation path289;
    int stackSize289;
    int emptyStackTurns289;
    boolean leftTurn289;
    
    public MapLocation path290;
    int stackSize290;
    int emptyStackTurns290;
    boolean leftTurn290;
    
    public MapLocation path291;
    int stackSize291;
    int emptyStackTurns291;
    boolean leftTurn291;
    
    public MapLocation path292;
    int stackSize292;
    int emptyStackTurns292;
    boolean leftTurn292;
    
    public MapLocation path293;
    int stackSize293;
    int emptyStackTurns293;
    boolean leftTurn293;
    
    public MapLocation path294;
    int stackSize294;
    int emptyStackTurns294;
    boolean leftTurn294;
    
    public MapLocation path295;
    int stackSize295;
    int emptyStackTurns295;
    boolean leftTurn295;
    
    public MapLocation path296;
    int stackSize296;
    int emptyStackTurns296;
    boolean leftTurn296;
    
    public MapLocation path297;
    int stackSize297;
    int emptyStackTurns297;
    boolean leftTurn297;
    
    public MapLocation path298;
    int stackSize298;
    int emptyStackTurns298;
    boolean leftTurn298;
    
    public MapLocation path299;
    int stackSize299;
    int emptyStackTurns299;
    boolean leftTurn299;
    
    public MapLocation path300;
    int stackSize300;
    int emptyStackTurns300;
    boolean leftTurn300;
    
    public MapLocation path301;
    int stackSize301;
    int emptyStackTurns301;
    boolean leftTurn301;
    
    public MapLocation path302;
    int stackSize302;
    int emptyStackTurns302;
    boolean leftTurn302;
    
    public MapLocation path303;
    int stackSize303;
    int emptyStackTurns303;
    boolean leftTurn303;
    
    public MapLocation path304;
    int stackSize304;
    int emptyStackTurns304;
    boolean leftTurn304;
    
    public MapLocation path305;
    int stackSize305;
    int emptyStackTurns305;
    boolean leftTurn305;
    
    public MapLocation path306;
    int stackSize306;
    int emptyStackTurns306;
    boolean leftTurn306;
    
    public MapLocation path307;
    int stackSize307;
    int emptyStackTurns307;
    boolean leftTurn307;
    
    public MapLocation path308;
    int stackSize308;
    int emptyStackTurns308;
    boolean leftTurn308;
    
    public MapLocation path309;
    int stackSize309;
    int emptyStackTurns309;
    boolean leftTurn309;
    
    public MapLocation path310;
    int stackSize310;
    int emptyStackTurns310;
    boolean leftTurn310;
    
    public MapLocation path311;
    int stackSize311;
    int emptyStackTurns311;
    boolean leftTurn311;
    
    public MapLocation path312;
    int stackSize312;
    int emptyStackTurns312;
    boolean leftTurn312;
    
    public MapLocation path313;
    int stackSize313;
    int emptyStackTurns313;
    boolean leftTurn313;
    
    public MapLocation path314;
    int stackSize314;
    int emptyStackTurns314;
    boolean leftTurn314;
    
    public MapLocation path315;
    int stackSize315;
    int emptyStackTurns315;
    boolean leftTurn315;
    
    public MapLocation path316;
    int stackSize316;
    int emptyStackTurns316;
    boolean leftTurn316;
    
    public MapLocation path317;
    int stackSize317;
    int emptyStackTurns317;
    boolean leftTurn317;
    
    public MapLocation path318;
    int stackSize318;
    int emptyStackTurns318;
    boolean leftTurn318;
    
    public MapLocation path319;
    int stackSize319;
    int emptyStackTurns319;
    boolean leftTurn319;
    
    public MapLocation path320;
    int stackSize320;
    int emptyStackTurns320;
    boolean leftTurn320;
    
    public MapLocation path321;
    int stackSize321;
    int emptyStackTurns321;
    boolean leftTurn321;
    
    public MapLocation path322;
    int stackSize322;
    int emptyStackTurns322;
    boolean leftTurn322;
    
    public MapLocation path323;
    int stackSize323;
    int emptyStackTurns323;
    boolean leftTurn323;
    
    public MapLocation path324;
    int stackSize324;
    int emptyStackTurns324;
    boolean leftTurn324;
    
    public MapLocation path325;
    int stackSize325;
    int emptyStackTurns325;
    boolean leftTurn325;
    
    public MapLocation path326;
    int stackSize326;
    int emptyStackTurns326;
    boolean leftTurn326;
    
    public MapLocation path327;
    int stackSize327;
    int emptyStackTurns327;
    boolean leftTurn327;
    
    public MapLocation path328;
    int stackSize328;
    int emptyStackTurns328;
    boolean leftTurn328;
    
    public MapLocation path329;
    int stackSize329;
    int emptyStackTurns329;
    boolean leftTurn329;
    
    public MapLocation path330;
    int stackSize330;
    int emptyStackTurns330;
    boolean leftTurn330;
    
    public MapLocation path331;
    int stackSize331;
    int emptyStackTurns331;
    boolean leftTurn331;
    
    public MapLocation path332;
    int stackSize332;
    int emptyStackTurns332;
    boolean leftTurn332;
    
    public MapLocation path333;
    int stackSize333;
    int emptyStackTurns333;
    boolean leftTurn333;
    
    public MapLocation path334;
    int stackSize334;
    int emptyStackTurns334;
    boolean leftTurn334;
    
    public MapLocation path335;
    int stackSize335;
    int emptyStackTurns335;
    boolean leftTurn335;
    
    public MapLocation path336;
    int stackSize336;
    int emptyStackTurns336;
    boolean leftTurn336;
    
    public MapLocation path337;
    int stackSize337;
    int emptyStackTurns337;
    boolean leftTurn337;
    
    public MapLocation path338;
    int stackSize338;
    int emptyStackTurns338;
    boolean leftTurn338;
    
    public MapLocation path339;
    int stackSize339;
    int emptyStackTurns339;
    boolean leftTurn339;
    
    public MapLocation path340;
    int stackSize340;
    int emptyStackTurns340;
    boolean leftTurn340;
    
    public MapLocation path341;
    int stackSize341;
    int emptyStackTurns341;
    boolean leftTurn341;
    
    public MapLocation path342;
    int stackSize342;
    int emptyStackTurns342;
    boolean leftTurn342;
    
    public MapLocation path343;
    int stackSize343;
    int emptyStackTurns343;
    boolean leftTurn343;
    
    public MapLocation path344;
    int stackSize344;
    int emptyStackTurns344;
    boolean leftTurn344;
    
    public MapLocation path345;
    int stackSize345;
    int emptyStackTurns345;
    boolean leftTurn345;
    
    public MapLocation path346;
    int stackSize346;
    int emptyStackTurns346;
    boolean leftTurn346;
    
    public MapLocation path347;
    int stackSize347;
    int emptyStackTurns347;
    boolean leftTurn347;
    
    public MapLocation path348;
    int stackSize348;
    int emptyStackTurns348;
    boolean leftTurn348;
    
    public MapLocation path349;
    int stackSize349;
    int emptyStackTurns349;
    boolean leftTurn349;
    
    public MapLocation path350;
    int stackSize350;
    int emptyStackTurns350;
    boolean leftTurn350;
    
    public MapLocation path351;
    int stackSize351;
    int emptyStackTurns351;
    boolean leftTurn351;
    
    public MapLocation path352;
    int stackSize352;
    int emptyStackTurns352;
    boolean leftTurn352;
    
    public MapLocation path353;
    int stackSize353;
    int emptyStackTurns353;
    boolean leftTurn353;
    
    public MapLocation path354;
    int stackSize354;
    int emptyStackTurns354;
    boolean leftTurn354;
    
    public MapLocation path355;
    int stackSize355;
    int emptyStackTurns355;
    boolean leftTurn355;
    
    public MapLocation path356;
    int stackSize356;
    int emptyStackTurns356;
    boolean leftTurn356;
    
    public MapLocation path357;
    int stackSize357;
    int emptyStackTurns357;
    boolean leftTurn357;
    
    public MapLocation path358;
    int stackSize358;
    int emptyStackTurns358;
    boolean leftTurn358;
    
    public MapLocation path359;
    int stackSize359;
    int emptyStackTurns359;
    boolean leftTurn359;
    
    public MapLocation path360;
    int stackSize360;
    int emptyStackTurns360;
    boolean leftTurn360;
    
    public MapLocation path361;
    int stackSize361;
    int emptyStackTurns361;
    boolean leftTurn361;
    
    public MapLocation path362;
    int stackSize362;
    int emptyStackTurns362;
    boolean leftTurn362;
    
    public MapLocation path363;
    int stackSize363;
    int emptyStackTurns363;
    boolean leftTurn363;
    
    public MapLocation path364;
    int stackSize364;
    int emptyStackTurns364;
    boolean leftTurn364;
    
    public MapLocation path365;
    int stackSize365;
    int emptyStackTurns365;
    boolean leftTurn365;
    
    public MapLocation path366;
    int stackSize366;
    int emptyStackTurns366;
    boolean leftTurn366;
    
    public MapLocation path367;
    int stackSize367;
    int emptyStackTurns367;
    boolean leftTurn367;
    
    public MapLocation path368;
    int stackSize368;
    int emptyStackTurns368;
    boolean leftTurn368;
    
    public MapLocation path369;
    int stackSize369;
    int emptyStackTurns369;
    boolean leftTurn369;
    
    public MapLocation path370;
    int stackSize370;
    int emptyStackTurns370;
    boolean leftTurn370;
    
    public MapLocation path371;
    int stackSize371;
    int emptyStackTurns371;
    boolean leftTurn371;
    
    public MapLocation path372;
    int stackSize372;
    int emptyStackTurns372;
    boolean leftTurn372;
    
    public MapLocation path373;
    int stackSize373;
    int emptyStackTurns373;
    boolean leftTurn373;
    
    public MapLocation path374;
    int stackSize374;
    int emptyStackTurns374;
    boolean leftTurn374;
    
    public MapLocation path375;
    int stackSize375;
    int emptyStackTurns375;
    boolean leftTurn375;
    
    public MapLocation path376;
    int stackSize376;
    int emptyStackTurns376;
    boolean leftTurn376;
    
    public MapLocation path377;
    int stackSize377;
    int emptyStackTurns377;
    boolean leftTurn377;
    
    public MapLocation path378;
    int stackSize378;
    int emptyStackTurns378;
    boolean leftTurn378;
    
    public MapLocation path379;
    int stackSize379;
    int emptyStackTurns379;
    boolean leftTurn379;
    
    public MapLocation path380;
    int stackSize380;
    int emptyStackTurns380;
    boolean leftTurn380;
    
    public MapLocation path381;
    int stackSize381;
    int emptyStackTurns381;
    boolean leftTurn381;
    
    public MapLocation path382;
    int stackSize382;
    int emptyStackTurns382;
    boolean leftTurn382;
    
    public MapLocation path383;
    int stackSize383;
    int emptyStackTurns383;
    boolean leftTurn383;
    
    public MapLocation path384;
    int stackSize384;
    int emptyStackTurns384;
    boolean leftTurn384;
    
    public MapLocation path385;
    int stackSize385;
    int emptyStackTurns385;
    boolean leftTurn385;
    
    public MapLocation path386;
    int stackSize386;
    int emptyStackTurns386;
    boolean leftTurn386;
    
    public MapLocation path387;
    int stackSize387;
    int emptyStackTurns387;
    boolean leftTurn387;
    
    public MapLocation path388;
    int stackSize388;
    int emptyStackTurns388;
    boolean leftTurn388;
    
    public MapLocation path389;
    int stackSize389;
    int emptyStackTurns389;
    boolean leftTurn389;
    
    public MapLocation path390;
    int stackSize390;
    int emptyStackTurns390;
    boolean leftTurn390;
    
    public MapLocation path391;
    int stackSize391;
    int emptyStackTurns391;
    boolean leftTurn391;
    
    public MapLocation path392;
    int stackSize392;
    int emptyStackTurns392;
    boolean leftTurn392;
    
    public MapLocation path393;
    int stackSize393;
    int emptyStackTurns393;
    boolean leftTurn393;
    
    public MapLocation path394;
    int stackSize394;
    int emptyStackTurns394;
    boolean leftTurn394;
    
    public MapLocation path395;
    int stackSize395;
    int emptyStackTurns395;
    boolean leftTurn395;
    
    public MapLocation path396;
    int stackSize396;
    int emptyStackTurns396;
    boolean leftTurn396;
    
    public MapLocation path397;
    int stackSize397;
    int emptyStackTurns397;
    boolean leftTurn397;
    
    public MapLocation path398;
    int stackSize398;
    int emptyStackTurns398;
    boolean leftTurn398;
    
    public MapLocation path399;
    int stackSize399;
    int emptyStackTurns399;
    boolean leftTurn399;
    
    public MapLocation path400;
    int stackSize400;
    int emptyStackTurns400;
    boolean leftTurn400;
    
    public MapLocation path401;
    int stackSize401;
    int emptyStackTurns401;
    boolean leftTurn401;
    
    public MapLocation path402;
    int stackSize402;
    int emptyStackTurns402;
    boolean leftTurn402;
    
    public MapLocation path403;
    int stackSize403;
    int emptyStackTurns403;
    boolean leftTurn403;
    
    public MapLocation path404;
    int stackSize404;
    int emptyStackTurns404;
    boolean leftTurn404;
    
    public MapLocation path405;
    int stackSize405;
    int emptyStackTurns405;
    boolean leftTurn405;
    
    public MapLocation path406;
    int stackSize406;
    int emptyStackTurns406;
    boolean leftTurn406;
    
    public MapLocation path407;
    int stackSize407;
    int emptyStackTurns407;
    boolean leftTurn407;
    
    public MapLocation path408;
    int stackSize408;
    int emptyStackTurns408;
    boolean leftTurn408;
    
    public MapLocation path409;
    int stackSize409;
    int emptyStackTurns409;
    boolean leftTurn409;
    
    public MapLocation path410;
    int stackSize410;
    int emptyStackTurns410;
    boolean leftTurn410;
    
    public MapLocation path411;
    int stackSize411;
    int emptyStackTurns411;
    boolean leftTurn411;
    
    public MapLocation path412;
    int stackSize412;
    int emptyStackTurns412;
    boolean leftTurn412;
    
    public MapLocation path413;
    int stackSize413;
    int emptyStackTurns413;
    boolean leftTurn413;
    
    public MapLocation path414;
    int stackSize414;
    int emptyStackTurns414;
    boolean leftTurn414;
    
    public MapLocation path415;
    int stackSize415;
    int emptyStackTurns415;
    boolean leftTurn415;
    
    public MapLocation path416;
    int stackSize416;
    int emptyStackTurns416;
    boolean leftTurn416;
    
    public MapLocation path417;
    int stackSize417;
    int emptyStackTurns417;
    boolean leftTurn417;
    
    public MapLocation path418;
    int stackSize418;
    int emptyStackTurns418;
    boolean leftTurn418;
    
    public MapLocation path419;
    int stackSize419;
    int emptyStackTurns419;
    boolean leftTurn419;
    
    public MapLocation path420;
    int stackSize420;
    int emptyStackTurns420;
    boolean leftTurn420;
    
    public MapLocation path421;
    int stackSize421;
    int emptyStackTurns421;
    boolean leftTurn421;
    
    public MapLocation path422;
    int stackSize422;
    int emptyStackTurns422;
    boolean leftTurn422;
    
    public MapLocation path423;
    int stackSize423;
    int emptyStackTurns423;
    boolean leftTurn423;
    
    public MapLocation path424;
    int stackSize424;
    int emptyStackTurns424;
    boolean leftTurn424;
    
    public MapLocation path425;
    int stackSize425;
    int emptyStackTurns425;
    boolean leftTurn425;
    
    public MapLocation path426;
    int stackSize426;
    int emptyStackTurns426;
    boolean leftTurn426;
    
    public MapLocation path427;
    int stackSize427;
    int emptyStackTurns427;
    boolean leftTurn427;
    
    public MapLocation path428;
    int stackSize428;
    int emptyStackTurns428;
    boolean leftTurn428;
    
    public MapLocation path429;
    int stackSize429;
    int emptyStackTurns429;
    boolean leftTurn429;
    
    public MapLocation path430;
    int stackSize430;
    int emptyStackTurns430;
    boolean leftTurn430;
    
    public MapLocation path431;
    int stackSize431;
    int emptyStackTurns431;
    boolean leftTurn431;
    
    public MapLocation path432;
    int stackSize432;
    int emptyStackTurns432;
    boolean leftTurn432;
    
    public MapLocation path433;
    int stackSize433;
    int emptyStackTurns433;
    boolean leftTurn433;
    
    public MapLocation path434;
    int stackSize434;
    int emptyStackTurns434;
    boolean leftTurn434;
    
    public MapLocation path435;
    int stackSize435;
    int emptyStackTurns435;
    boolean leftTurn435;
    
    public MapLocation path436;
    int stackSize436;
    int emptyStackTurns436;
    boolean leftTurn436;
    
    public MapLocation path437;
    int stackSize437;
    int emptyStackTurns437;
    boolean leftTurn437;
    
    public MapLocation path438;
    int stackSize438;
    int emptyStackTurns438;
    boolean leftTurn438;
    
    public MapLocation path439;
    int stackSize439;
    int emptyStackTurns439;
    boolean leftTurn439;
    
    public MapLocation path440;
    int stackSize440;
    int emptyStackTurns440;
    boolean leftTurn440;
    
    public MapLocation path441;
    int stackSize441;
    int emptyStackTurns441;
    boolean leftTurn441;
    
    public MapLocation path442;
    int stackSize442;
    int emptyStackTurns442;
    boolean leftTurn442;
    
    public MapLocation path443;
    int stackSize443;
    int emptyStackTurns443;
    boolean leftTurn443;
    
    public MapLocation path444;
    int stackSize444;
    int emptyStackTurns444;
    boolean leftTurn444;
    
    public MapLocation path445;
    int stackSize445;
    int emptyStackTurns445;
    boolean leftTurn445;
    
    public MapLocation path446;
    int stackSize446;
    int emptyStackTurns446;
    boolean leftTurn446;
    
    public MapLocation path447;
    int stackSize447;
    int emptyStackTurns447;
    boolean leftTurn447;
    
    public MapLocation path448;
    int stackSize448;
    int emptyStackTurns448;
    boolean leftTurn448;
    
    public MapLocation path449;
    int stackSize449;
    int emptyStackTurns449;
    boolean leftTurn449;
    
    public MapLocation path450;
    int stackSize450;
    int emptyStackTurns450;
    boolean leftTurn450;
    
    public MapLocation path451;
    int stackSize451;
    int emptyStackTurns451;
    boolean leftTurn451;
    
    public MapLocation path452;
    int stackSize452;
    int emptyStackTurns452;
    boolean leftTurn452;
    
    public MapLocation path453;
    int stackSize453;
    int emptyStackTurns453;
    boolean leftTurn453;
    
    public MapLocation path454;
    int stackSize454;
    int emptyStackTurns454;
    boolean leftTurn454;
    
    public MapLocation path455;
    int stackSize455;
    int emptyStackTurns455;
    boolean leftTurn455;
    
    public MapLocation path456;
    int stackSize456;
    int emptyStackTurns456;
    boolean leftTurn456;
    
    public MapLocation path457;
    int stackSize457;
    int emptyStackTurns457;
    boolean leftTurn457;
    
    public MapLocation path458;
    int stackSize458;
    int emptyStackTurns458;
    boolean leftTurn458;
    
    public MapLocation path459;
    int stackSize459;
    int emptyStackTurns459;
    boolean leftTurn459;
    
    public MapLocation path460;
    int stackSize460;
    int emptyStackTurns460;
    boolean leftTurn460;
    
    public MapLocation path461;
    int stackSize461;
    int emptyStackTurns461;
    boolean leftTurn461;
    
    public MapLocation path462;
    int stackSize462;
    int emptyStackTurns462;
    boolean leftTurn462;
    
    public MapLocation path463;
    int stackSize463;
    int emptyStackTurns463;
    boolean leftTurn463;
    
    public MapLocation path464;
    int stackSize464;
    int emptyStackTurns464;
    boolean leftTurn464;
    
    public MapLocation path465;
    int stackSize465;
    int emptyStackTurns465;
    boolean leftTurn465;
    
    public MapLocation path466;
    int stackSize466;
    int emptyStackTurns466;
    boolean leftTurn466;
    
    public MapLocation path467;
    int stackSize467;
    int emptyStackTurns467;
    boolean leftTurn467;
    
    public MapLocation path468;
    int stackSize468;
    int emptyStackTurns468;
    boolean leftTurn468;
    
    public MapLocation path469;
    int stackSize469;
    int emptyStackTurns469;
    boolean leftTurn469;
    
    public MapLocation path470;
    int stackSize470;
    int emptyStackTurns470;
    boolean leftTurn470;
    
    public MapLocation path471;
    int stackSize471;
    int emptyStackTurns471;
    boolean leftTurn471;
    
    public MapLocation path472;
    int stackSize472;
    int emptyStackTurns472;
    boolean leftTurn472;
    
    public MapLocation path473;
    int stackSize473;
    int emptyStackTurns473;
    boolean leftTurn473;
    
    public MapLocation path474;
    int stackSize474;
    int emptyStackTurns474;
    boolean leftTurn474;
    
    public MapLocation path475;
    int stackSize475;
    int emptyStackTurns475;
    boolean leftTurn475;
    
    public MapLocation path476;
    int stackSize476;
    int emptyStackTurns476;
    boolean leftTurn476;
    
    public MapLocation path477;
    int stackSize477;
    int emptyStackTurns477;
    boolean leftTurn477;
    
    public MapLocation path478;
    int stackSize478;
    int emptyStackTurns478;
    boolean leftTurn478;
    
    public MapLocation path479;
    int stackSize479;
    int emptyStackTurns479;
    boolean leftTurn479;
    
    public MapLocation path480;
    int stackSize480;
    int emptyStackTurns480;
    boolean leftTurn480;
    
    public MapLocation path481;
    int stackSize481;
    int emptyStackTurns481;
    boolean leftTurn481;
    
    public MapLocation path482;
    int stackSize482;
    int emptyStackTurns482;
    boolean leftTurn482;
    
    public MapLocation path483;
    int stackSize483;
    int emptyStackTurns483;
    boolean leftTurn483;
    
    public MapLocation path484;
    int stackSize484;
    int emptyStackTurns484;
    boolean leftTurn484;
    
    public MapLocation path485;
    int stackSize485;
    int emptyStackTurns485;
    boolean leftTurn485;
    
    public MapLocation path486;
    int stackSize486;
    int emptyStackTurns486;
    boolean leftTurn486;
    
    public MapLocation path487;
    int stackSize487;
    int emptyStackTurns487;
    boolean leftTurn487;
    
    public MapLocation path488;
    int stackSize488;
    int emptyStackTurns488;
    boolean leftTurn488;
    
    public MapLocation path489;
    int stackSize489;
    int emptyStackTurns489;
    boolean leftTurn489;
    
    public MapLocation path490;
    int stackSize490;
    int emptyStackTurns490;
    boolean leftTurn490;
    
    public MapLocation path491;
    int stackSize491;
    int emptyStackTurns491;
    boolean leftTurn491;
    
    public MapLocation path492;
    int stackSize492;
    int emptyStackTurns492;
    boolean leftTurn492;
    
    public MapLocation path493;
    int stackSize493;
    int emptyStackTurns493;
    boolean leftTurn493;
    
    public MapLocation path494;
    int stackSize494;
    int emptyStackTurns494;
    boolean leftTurn494;
    
    public MapLocation path495;
    int stackSize495;
    int emptyStackTurns495;
    boolean leftTurn495;
    
    public MapLocation path496;
    int stackSize496;
    int emptyStackTurns496;
    boolean leftTurn496;
    
    public MapLocation path497;
    int stackSize497;
    int emptyStackTurns497;
    boolean leftTurn497;
    
    public MapLocation path498;
    int stackSize498;
    int emptyStackTurns498;
    boolean leftTurn498;
    
    public MapLocation path499;
    int stackSize499;
    int emptyStackTurns499;
    boolean leftTurn499;
    

    // used to speed up the hot path of setCurrentState (which is a noop)
    static MapLocation lastLoc;
    void reset() {
        pos = rc.getLocation();
        pathLength = 0;
        stackSize = 0;
        emptyStackTurns = 1000;
        bottomDir = topDir = pos.directionTo(target);
        lastLoc = new MapLocation(-1, -1);
    }

    BugnavInstance(boolean leftTurn, MapLocation target) {
        this.leftTurn = leftTurn;
        this.target = target;
        this.isMaster = true;
        reset();
    }

    BugnavInstance(MapLocation pos, boolean leftTurn, MapLocation target, boolean isMaster) {
        indicate("RESET");
        this.leftTurn = leftTurn;
        this.target = target;
        this.isMaster = isMaster;
        reset();
        this.pos = pos;
        appendCurrentState();
    }

    
    void setCurrentState1() {
        pos = path0;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize0;
        leftTurn = leftTurn0;
        emptyStackTurns = emptyStackTurns0;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 1;
    }

    void appendCurrentState0() {
        path0 = pos;
        stackSize0 = stackSize;
        leftTurn0 = leftTurn;
        emptyStackTurns0 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState2() {
        pos = path1;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize1;
        leftTurn = leftTurn1;
        emptyStackTurns = emptyStackTurns1;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 2;
    }

    void appendCurrentState1() {
        path1 = pos;
        stackSize1 = stackSize;
        leftTurn1 = leftTurn;
        emptyStackTurns1 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState3() {
        pos = path2;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize2;
        leftTurn = leftTurn2;
        emptyStackTurns = emptyStackTurns2;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 3;
    }

    void appendCurrentState2() {
        path2 = pos;
        stackSize2 = stackSize;
        leftTurn2 = leftTurn;
        emptyStackTurns2 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState4() {
        pos = path3;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize3;
        leftTurn = leftTurn3;
        emptyStackTurns = emptyStackTurns3;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 4;
    }

    void appendCurrentState3() {
        path3 = pos;
        stackSize3 = stackSize;
        leftTurn3 = leftTurn;
        emptyStackTurns3 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState5() {
        pos = path4;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize4;
        leftTurn = leftTurn4;
        emptyStackTurns = emptyStackTurns4;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 5;
    }

    void appendCurrentState4() {
        path4 = pos;
        stackSize4 = stackSize;
        leftTurn4 = leftTurn;
        emptyStackTurns4 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState6() {
        pos = path5;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize5;
        leftTurn = leftTurn5;
        emptyStackTurns = emptyStackTurns5;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 6;
    }

    void appendCurrentState5() {
        path5 = pos;
        stackSize5 = stackSize;
        leftTurn5 = leftTurn;
        emptyStackTurns5 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState7() {
        pos = path6;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize6;
        leftTurn = leftTurn6;
        emptyStackTurns = emptyStackTurns6;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 7;
    }

    void appendCurrentState6() {
        path6 = pos;
        stackSize6 = stackSize;
        leftTurn6 = leftTurn;
        emptyStackTurns6 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState8() {
        pos = path7;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize7;
        leftTurn = leftTurn7;
        emptyStackTurns = emptyStackTurns7;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 8;
    }

    void appendCurrentState7() {
        path7 = pos;
        stackSize7 = stackSize;
        leftTurn7 = leftTurn;
        emptyStackTurns7 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState9() {
        pos = path8;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize8;
        leftTurn = leftTurn8;
        emptyStackTurns = emptyStackTurns8;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 9;
    }

    void appendCurrentState8() {
        path8 = pos;
        stackSize8 = stackSize;
        leftTurn8 = leftTurn;
        emptyStackTurns8 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState10() {
        pos = path9;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize9;
        leftTurn = leftTurn9;
        emptyStackTurns = emptyStackTurns9;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 10;
    }

    void appendCurrentState9() {
        path9 = pos;
        stackSize9 = stackSize;
        leftTurn9 = leftTurn;
        emptyStackTurns9 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState11() {
        pos = path10;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize10;
        leftTurn = leftTurn10;
        emptyStackTurns = emptyStackTurns10;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 11;
    }

    void appendCurrentState10() {
        path10 = pos;
        stackSize10 = stackSize;
        leftTurn10 = leftTurn;
        emptyStackTurns10 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState12() {
        pos = path11;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize11;
        leftTurn = leftTurn11;
        emptyStackTurns = emptyStackTurns11;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 12;
    }

    void appendCurrentState11() {
        path11 = pos;
        stackSize11 = stackSize;
        leftTurn11 = leftTurn;
        emptyStackTurns11 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState13() {
        pos = path12;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize12;
        leftTurn = leftTurn12;
        emptyStackTurns = emptyStackTurns12;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 13;
    }

    void appendCurrentState12() {
        path12 = pos;
        stackSize12 = stackSize;
        leftTurn12 = leftTurn;
        emptyStackTurns12 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState14() {
        pos = path13;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize13;
        leftTurn = leftTurn13;
        emptyStackTurns = emptyStackTurns13;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 14;
    }

    void appendCurrentState13() {
        path13 = pos;
        stackSize13 = stackSize;
        leftTurn13 = leftTurn;
        emptyStackTurns13 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState15() {
        pos = path14;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize14;
        leftTurn = leftTurn14;
        emptyStackTurns = emptyStackTurns14;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 15;
    }

    void appendCurrentState14() {
        path14 = pos;
        stackSize14 = stackSize;
        leftTurn14 = leftTurn;
        emptyStackTurns14 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState16() {
        pos = path15;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize15;
        leftTurn = leftTurn15;
        emptyStackTurns = emptyStackTurns15;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 16;
    }

    void appendCurrentState15() {
        path15 = pos;
        stackSize15 = stackSize;
        leftTurn15 = leftTurn;
        emptyStackTurns15 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState17() {
        pos = path16;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize16;
        leftTurn = leftTurn16;
        emptyStackTurns = emptyStackTurns16;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 17;
    }

    void appendCurrentState16() {
        path16 = pos;
        stackSize16 = stackSize;
        leftTurn16 = leftTurn;
        emptyStackTurns16 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState18() {
        pos = path17;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize17;
        leftTurn = leftTurn17;
        emptyStackTurns = emptyStackTurns17;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 18;
    }

    void appendCurrentState17() {
        path17 = pos;
        stackSize17 = stackSize;
        leftTurn17 = leftTurn;
        emptyStackTurns17 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState19() {
        pos = path18;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize18;
        leftTurn = leftTurn18;
        emptyStackTurns = emptyStackTurns18;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 19;
    }

    void appendCurrentState18() {
        path18 = pos;
        stackSize18 = stackSize;
        leftTurn18 = leftTurn;
        emptyStackTurns18 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState20() {
        pos = path19;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize19;
        leftTurn = leftTurn19;
        emptyStackTurns = emptyStackTurns19;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 20;
    }

    void appendCurrentState19() {
        path19 = pos;
        stackSize19 = stackSize;
        leftTurn19 = leftTurn;
        emptyStackTurns19 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState21() {
        pos = path20;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize20;
        leftTurn = leftTurn20;
        emptyStackTurns = emptyStackTurns20;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 21;
    }

    void appendCurrentState20() {
        path20 = pos;
        stackSize20 = stackSize;
        leftTurn20 = leftTurn;
        emptyStackTurns20 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState22() {
        pos = path21;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize21;
        leftTurn = leftTurn21;
        emptyStackTurns = emptyStackTurns21;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 22;
    }

    void appendCurrentState21() {
        path21 = pos;
        stackSize21 = stackSize;
        leftTurn21 = leftTurn;
        emptyStackTurns21 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState23() {
        pos = path22;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize22;
        leftTurn = leftTurn22;
        emptyStackTurns = emptyStackTurns22;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 23;
    }

    void appendCurrentState22() {
        path22 = pos;
        stackSize22 = stackSize;
        leftTurn22 = leftTurn;
        emptyStackTurns22 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState24() {
        pos = path23;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize23;
        leftTurn = leftTurn23;
        emptyStackTurns = emptyStackTurns23;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 24;
    }

    void appendCurrentState23() {
        path23 = pos;
        stackSize23 = stackSize;
        leftTurn23 = leftTurn;
        emptyStackTurns23 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState25() {
        pos = path24;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize24;
        leftTurn = leftTurn24;
        emptyStackTurns = emptyStackTurns24;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 25;
    }

    void appendCurrentState24() {
        path24 = pos;
        stackSize24 = stackSize;
        leftTurn24 = leftTurn;
        emptyStackTurns24 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState26() {
        pos = path25;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize25;
        leftTurn = leftTurn25;
        emptyStackTurns = emptyStackTurns25;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 26;
    }

    void appendCurrentState25() {
        path25 = pos;
        stackSize25 = stackSize;
        leftTurn25 = leftTurn;
        emptyStackTurns25 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState27() {
        pos = path26;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize26;
        leftTurn = leftTurn26;
        emptyStackTurns = emptyStackTurns26;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 27;
    }

    void appendCurrentState26() {
        path26 = pos;
        stackSize26 = stackSize;
        leftTurn26 = leftTurn;
        emptyStackTurns26 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState28() {
        pos = path27;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize27;
        leftTurn = leftTurn27;
        emptyStackTurns = emptyStackTurns27;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 28;
    }

    void appendCurrentState27() {
        path27 = pos;
        stackSize27 = stackSize;
        leftTurn27 = leftTurn;
        emptyStackTurns27 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState29() {
        pos = path28;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize28;
        leftTurn = leftTurn28;
        emptyStackTurns = emptyStackTurns28;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 29;
    }

    void appendCurrentState28() {
        path28 = pos;
        stackSize28 = stackSize;
        leftTurn28 = leftTurn;
        emptyStackTurns28 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState30() {
        pos = path29;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize29;
        leftTurn = leftTurn29;
        emptyStackTurns = emptyStackTurns29;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 30;
    }

    void appendCurrentState29() {
        path29 = pos;
        stackSize29 = stackSize;
        leftTurn29 = leftTurn;
        emptyStackTurns29 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState31() {
        pos = path30;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize30;
        leftTurn = leftTurn30;
        emptyStackTurns = emptyStackTurns30;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 31;
    }

    void appendCurrentState30() {
        path30 = pos;
        stackSize30 = stackSize;
        leftTurn30 = leftTurn;
        emptyStackTurns30 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState32() {
        pos = path31;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize31;
        leftTurn = leftTurn31;
        emptyStackTurns = emptyStackTurns31;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 32;
    }

    void appendCurrentState31() {
        path31 = pos;
        stackSize31 = stackSize;
        leftTurn31 = leftTurn;
        emptyStackTurns31 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState33() {
        pos = path32;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize32;
        leftTurn = leftTurn32;
        emptyStackTurns = emptyStackTurns32;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 33;
    }

    void appendCurrentState32() {
        path32 = pos;
        stackSize32 = stackSize;
        leftTurn32 = leftTurn;
        emptyStackTurns32 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState34() {
        pos = path33;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize33;
        leftTurn = leftTurn33;
        emptyStackTurns = emptyStackTurns33;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 34;
    }

    void appendCurrentState33() {
        path33 = pos;
        stackSize33 = stackSize;
        leftTurn33 = leftTurn;
        emptyStackTurns33 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState35() {
        pos = path34;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize34;
        leftTurn = leftTurn34;
        emptyStackTurns = emptyStackTurns34;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 35;
    }

    void appendCurrentState34() {
        path34 = pos;
        stackSize34 = stackSize;
        leftTurn34 = leftTurn;
        emptyStackTurns34 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState36() {
        pos = path35;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize35;
        leftTurn = leftTurn35;
        emptyStackTurns = emptyStackTurns35;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 36;
    }

    void appendCurrentState35() {
        path35 = pos;
        stackSize35 = stackSize;
        leftTurn35 = leftTurn;
        emptyStackTurns35 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState37() {
        pos = path36;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize36;
        leftTurn = leftTurn36;
        emptyStackTurns = emptyStackTurns36;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 37;
    }

    void appendCurrentState36() {
        path36 = pos;
        stackSize36 = stackSize;
        leftTurn36 = leftTurn;
        emptyStackTurns36 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState38() {
        pos = path37;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize37;
        leftTurn = leftTurn37;
        emptyStackTurns = emptyStackTurns37;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 38;
    }

    void appendCurrentState37() {
        path37 = pos;
        stackSize37 = stackSize;
        leftTurn37 = leftTurn;
        emptyStackTurns37 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState39() {
        pos = path38;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize38;
        leftTurn = leftTurn38;
        emptyStackTurns = emptyStackTurns38;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 39;
    }

    void appendCurrentState38() {
        path38 = pos;
        stackSize38 = stackSize;
        leftTurn38 = leftTurn;
        emptyStackTurns38 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState40() {
        pos = path39;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize39;
        leftTurn = leftTurn39;
        emptyStackTurns = emptyStackTurns39;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 40;
    }

    void appendCurrentState39() {
        path39 = pos;
        stackSize39 = stackSize;
        leftTurn39 = leftTurn;
        emptyStackTurns39 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState41() {
        pos = path40;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize40;
        leftTurn = leftTurn40;
        emptyStackTurns = emptyStackTurns40;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 41;
    }

    void appendCurrentState40() {
        path40 = pos;
        stackSize40 = stackSize;
        leftTurn40 = leftTurn;
        emptyStackTurns40 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState42() {
        pos = path41;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize41;
        leftTurn = leftTurn41;
        emptyStackTurns = emptyStackTurns41;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 42;
    }

    void appendCurrentState41() {
        path41 = pos;
        stackSize41 = stackSize;
        leftTurn41 = leftTurn;
        emptyStackTurns41 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState43() {
        pos = path42;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize42;
        leftTurn = leftTurn42;
        emptyStackTurns = emptyStackTurns42;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 43;
    }

    void appendCurrentState42() {
        path42 = pos;
        stackSize42 = stackSize;
        leftTurn42 = leftTurn;
        emptyStackTurns42 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState44() {
        pos = path43;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize43;
        leftTurn = leftTurn43;
        emptyStackTurns = emptyStackTurns43;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 44;
    }

    void appendCurrentState43() {
        path43 = pos;
        stackSize43 = stackSize;
        leftTurn43 = leftTurn;
        emptyStackTurns43 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState45() {
        pos = path44;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize44;
        leftTurn = leftTurn44;
        emptyStackTurns = emptyStackTurns44;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 45;
    }

    void appendCurrentState44() {
        path44 = pos;
        stackSize44 = stackSize;
        leftTurn44 = leftTurn;
        emptyStackTurns44 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState46() {
        pos = path45;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize45;
        leftTurn = leftTurn45;
        emptyStackTurns = emptyStackTurns45;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 46;
    }

    void appendCurrentState45() {
        path45 = pos;
        stackSize45 = stackSize;
        leftTurn45 = leftTurn;
        emptyStackTurns45 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState47() {
        pos = path46;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize46;
        leftTurn = leftTurn46;
        emptyStackTurns = emptyStackTurns46;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 47;
    }

    void appendCurrentState46() {
        path46 = pos;
        stackSize46 = stackSize;
        leftTurn46 = leftTurn;
        emptyStackTurns46 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState48() {
        pos = path47;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize47;
        leftTurn = leftTurn47;
        emptyStackTurns = emptyStackTurns47;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 48;
    }

    void appendCurrentState47() {
        path47 = pos;
        stackSize47 = stackSize;
        leftTurn47 = leftTurn;
        emptyStackTurns47 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState49() {
        pos = path48;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize48;
        leftTurn = leftTurn48;
        emptyStackTurns = emptyStackTurns48;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 49;
    }

    void appendCurrentState48() {
        path48 = pos;
        stackSize48 = stackSize;
        leftTurn48 = leftTurn;
        emptyStackTurns48 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState50() {
        pos = path49;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize49;
        leftTurn = leftTurn49;
        emptyStackTurns = emptyStackTurns49;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 50;
    }

    void appendCurrentState49() {
        path49 = pos;
        stackSize49 = stackSize;
        leftTurn49 = leftTurn;
        emptyStackTurns49 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState51() {
        pos = path50;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize50;
        leftTurn = leftTurn50;
        emptyStackTurns = emptyStackTurns50;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 51;
    }

    void appendCurrentState50() {
        path50 = pos;
        stackSize50 = stackSize;
        leftTurn50 = leftTurn;
        emptyStackTurns50 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState52() {
        pos = path51;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize51;
        leftTurn = leftTurn51;
        emptyStackTurns = emptyStackTurns51;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 52;
    }

    void appendCurrentState51() {
        path51 = pos;
        stackSize51 = stackSize;
        leftTurn51 = leftTurn;
        emptyStackTurns51 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState53() {
        pos = path52;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize52;
        leftTurn = leftTurn52;
        emptyStackTurns = emptyStackTurns52;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 53;
    }

    void appendCurrentState52() {
        path52 = pos;
        stackSize52 = stackSize;
        leftTurn52 = leftTurn;
        emptyStackTurns52 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState54() {
        pos = path53;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize53;
        leftTurn = leftTurn53;
        emptyStackTurns = emptyStackTurns53;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 54;
    }

    void appendCurrentState53() {
        path53 = pos;
        stackSize53 = stackSize;
        leftTurn53 = leftTurn;
        emptyStackTurns53 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState55() {
        pos = path54;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize54;
        leftTurn = leftTurn54;
        emptyStackTurns = emptyStackTurns54;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 55;
    }

    void appendCurrentState54() {
        path54 = pos;
        stackSize54 = stackSize;
        leftTurn54 = leftTurn;
        emptyStackTurns54 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState56() {
        pos = path55;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize55;
        leftTurn = leftTurn55;
        emptyStackTurns = emptyStackTurns55;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 56;
    }

    void appendCurrentState55() {
        path55 = pos;
        stackSize55 = stackSize;
        leftTurn55 = leftTurn;
        emptyStackTurns55 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState57() {
        pos = path56;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize56;
        leftTurn = leftTurn56;
        emptyStackTurns = emptyStackTurns56;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 57;
    }

    void appendCurrentState56() {
        path56 = pos;
        stackSize56 = stackSize;
        leftTurn56 = leftTurn;
        emptyStackTurns56 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState58() {
        pos = path57;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize57;
        leftTurn = leftTurn57;
        emptyStackTurns = emptyStackTurns57;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 58;
    }

    void appendCurrentState57() {
        path57 = pos;
        stackSize57 = stackSize;
        leftTurn57 = leftTurn;
        emptyStackTurns57 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState59() {
        pos = path58;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize58;
        leftTurn = leftTurn58;
        emptyStackTurns = emptyStackTurns58;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 59;
    }

    void appendCurrentState58() {
        path58 = pos;
        stackSize58 = stackSize;
        leftTurn58 = leftTurn;
        emptyStackTurns58 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState60() {
        pos = path59;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize59;
        leftTurn = leftTurn59;
        emptyStackTurns = emptyStackTurns59;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 60;
    }

    void appendCurrentState59() {
        path59 = pos;
        stackSize59 = stackSize;
        leftTurn59 = leftTurn;
        emptyStackTurns59 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState61() {
        pos = path60;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize60;
        leftTurn = leftTurn60;
        emptyStackTurns = emptyStackTurns60;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 61;
    }

    void appendCurrentState60() {
        path60 = pos;
        stackSize60 = stackSize;
        leftTurn60 = leftTurn;
        emptyStackTurns60 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState62() {
        pos = path61;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize61;
        leftTurn = leftTurn61;
        emptyStackTurns = emptyStackTurns61;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 62;
    }

    void appendCurrentState61() {
        path61 = pos;
        stackSize61 = stackSize;
        leftTurn61 = leftTurn;
        emptyStackTurns61 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState63() {
        pos = path62;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize62;
        leftTurn = leftTurn62;
        emptyStackTurns = emptyStackTurns62;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 63;
    }

    void appendCurrentState62() {
        path62 = pos;
        stackSize62 = stackSize;
        leftTurn62 = leftTurn;
        emptyStackTurns62 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState64() {
        pos = path63;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize63;
        leftTurn = leftTurn63;
        emptyStackTurns = emptyStackTurns63;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 64;
    }

    void appendCurrentState63() {
        path63 = pos;
        stackSize63 = stackSize;
        leftTurn63 = leftTurn;
        emptyStackTurns63 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState65() {
        pos = path64;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize64;
        leftTurn = leftTurn64;
        emptyStackTurns = emptyStackTurns64;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 65;
    }

    void appendCurrentState64() {
        path64 = pos;
        stackSize64 = stackSize;
        leftTurn64 = leftTurn;
        emptyStackTurns64 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState66() {
        pos = path65;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize65;
        leftTurn = leftTurn65;
        emptyStackTurns = emptyStackTurns65;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 66;
    }

    void appendCurrentState65() {
        path65 = pos;
        stackSize65 = stackSize;
        leftTurn65 = leftTurn;
        emptyStackTurns65 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState67() {
        pos = path66;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize66;
        leftTurn = leftTurn66;
        emptyStackTurns = emptyStackTurns66;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 67;
    }

    void appendCurrentState66() {
        path66 = pos;
        stackSize66 = stackSize;
        leftTurn66 = leftTurn;
        emptyStackTurns66 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState68() {
        pos = path67;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize67;
        leftTurn = leftTurn67;
        emptyStackTurns = emptyStackTurns67;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 68;
    }

    void appendCurrentState67() {
        path67 = pos;
        stackSize67 = stackSize;
        leftTurn67 = leftTurn;
        emptyStackTurns67 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState69() {
        pos = path68;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize68;
        leftTurn = leftTurn68;
        emptyStackTurns = emptyStackTurns68;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 69;
    }

    void appendCurrentState68() {
        path68 = pos;
        stackSize68 = stackSize;
        leftTurn68 = leftTurn;
        emptyStackTurns68 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState70() {
        pos = path69;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize69;
        leftTurn = leftTurn69;
        emptyStackTurns = emptyStackTurns69;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 70;
    }

    void appendCurrentState69() {
        path69 = pos;
        stackSize69 = stackSize;
        leftTurn69 = leftTurn;
        emptyStackTurns69 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState71() {
        pos = path70;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize70;
        leftTurn = leftTurn70;
        emptyStackTurns = emptyStackTurns70;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 71;
    }

    void appendCurrentState70() {
        path70 = pos;
        stackSize70 = stackSize;
        leftTurn70 = leftTurn;
        emptyStackTurns70 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState72() {
        pos = path71;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize71;
        leftTurn = leftTurn71;
        emptyStackTurns = emptyStackTurns71;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 72;
    }

    void appendCurrentState71() {
        path71 = pos;
        stackSize71 = stackSize;
        leftTurn71 = leftTurn;
        emptyStackTurns71 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState73() {
        pos = path72;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize72;
        leftTurn = leftTurn72;
        emptyStackTurns = emptyStackTurns72;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 73;
    }

    void appendCurrentState72() {
        path72 = pos;
        stackSize72 = stackSize;
        leftTurn72 = leftTurn;
        emptyStackTurns72 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState74() {
        pos = path73;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize73;
        leftTurn = leftTurn73;
        emptyStackTurns = emptyStackTurns73;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 74;
    }

    void appendCurrentState73() {
        path73 = pos;
        stackSize73 = stackSize;
        leftTurn73 = leftTurn;
        emptyStackTurns73 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState75() {
        pos = path74;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize74;
        leftTurn = leftTurn74;
        emptyStackTurns = emptyStackTurns74;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 75;
    }

    void appendCurrentState74() {
        path74 = pos;
        stackSize74 = stackSize;
        leftTurn74 = leftTurn;
        emptyStackTurns74 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState76() {
        pos = path75;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize75;
        leftTurn = leftTurn75;
        emptyStackTurns = emptyStackTurns75;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 76;
    }

    void appendCurrentState75() {
        path75 = pos;
        stackSize75 = stackSize;
        leftTurn75 = leftTurn;
        emptyStackTurns75 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState77() {
        pos = path76;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize76;
        leftTurn = leftTurn76;
        emptyStackTurns = emptyStackTurns76;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 77;
    }

    void appendCurrentState76() {
        path76 = pos;
        stackSize76 = stackSize;
        leftTurn76 = leftTurn;
        emptyStackTurns76 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState78() {
        pos = path77;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize77;
        leftTurn = leftTurn77;
        emptyStackTurns = emptyStackTurns77;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 78;
    }

    void appendCurrentState77() {
        path77 = pos;
        stackSize77 = stackSize;
        leftTurn77 = leftTurn;
        emptyStackTurns77 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState79() {
        pos = path78;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize78;
        leftTurn = leftTurn78;
        emptyStackTurns = emptyStackTurns78;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 79;
    }

    void appendCurrentState78() {
        path78 = pos;
        stackSize78 = stackSize;
        leftTurn78 = leftTurn;
        emptyStackTurns78 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState80() {
        pos = path79;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize79;
        leftTurn = leftTurn79;
        emptyStackTurns = emptyStackTurns79;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 80;
    }

    void appendCurrentState79() {
        path79 = pos;
        stackSize79 = stackSize;
        leftTurn79 = leftTurn;
        emptyStackTurns79 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState81() {
        pos = path80;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize80;
        leftTurn = leftTurn80;
        emptyStackTurns = emptyStackTurns80;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 81;
    }

    void appendCurrentState80() {
        path80 = pos;
        stackSize80 = stackSize;
        leftTurn80 = leftTurn;
        emptyStackTurns80 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState82() {
        pos = path81;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize81;
        leftTurn = leftTurn81;
        emptyStackTurns = emptyStackTurns81;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 82;
    }

    void appendCurrentState81() {
        path81 = pos;
        stackSize81 = stackSize;
        leftTurn81 = leftTurn;
        emptyStackTurns81 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState83() {
        pos = path82;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize82;
        leftTurn = leftTurn82;
        emptyStackTurns = emptyStackTurns82;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 83;
    }

    void appendCurrentState82() {
        path82 = pos;
        stackSize82 = stackSize;
        leftTurn82 = leftTurn;
        emptyStackTurns82 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState84() {
        pos = path83;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize83;
        leftTurn = leftTurn83;
        emptyStackTurns = emptyStackTurns83;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 84;
    }

    void appendCurrentState83() {
        path83 = pos;
        stackSize83 = stackSize;
        leftTurn83 = leftTurn;
        emptyStackTurns83 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState85() {
        pos = path84;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize84;
        leftTurn = leftTurn84;
        emptyStackTurns = emptyStackTurns84;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 85;
    }

    void appendCurrentState84() {
        path84 = pos;
        stackSize84 = stackSize;
        leftTurn84 = leftTurn;
        emptyStackTurns84 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState86() {
        pos = path85;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize85;
        leftTurn = leftTurn85;
        emptyStackTurns = emptyStackTurns85;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 86;
    }

    void appendCurrentState85() {
        path85 = pos;
        stackSize85 = stackSize;
        leftTurn85 = leftTurn;
        emptyStackTurns85 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState87() {
        pos = path86;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize86;
        leftTurn = leftTurn86;
        emptyStackTurns = emptyStackTurns86;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 87;
    }

    void appendCurrentState86() {
        path86 = pos;
        stackSize86 = stackSize;
        leftTurn86 = leftTurn;
        emptyStackTurns86 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState88() {
        pos = path87;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize87;
        leftTurn = leftTurn87;
        emptyStackTurns = emptyStackTurns87;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 88;
    }

    void appendCurrentState87() {
        path87 = pos;
        stackSize87 = stackSize;
        leftTurn87 = leftTurn;
        emptyStackTurns87 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState89() {
        pos = path88;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize88;
        leftTurn = leftTurn88;
        emptyStackTurns = emptyStackTurns88;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 89;
    }

    void appendCurrentState88() {
        path88 = pos;
        stackSize88 = stackSize;
        leftTurn88 = leftTurn;
        emptyStackTurns88 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState90() {
        pos = path89;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize89;
        leftTurn = leftTurn89;
        emptyStackTurns = emptyStackTurns89;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 90;
    }

    void appendCurrentState89() {
        path89 = pos;
        stackSize89 = stackSize;
        leftTurn89 = leftTurn;
        emptyStackTurns89 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState91() {
        pos = path90;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize90;
        leftTurn = leftTurn90;
        emptyStackTurns = emptyStackTurns90;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 91;
    }

    void appendCurrentState90() {
        path90 = pos;
        stackSize90 = stackSize;
        leftTurn90 = leftTurn;
        emptyStackTurns90 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState92() {
        pos = path91;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize91;
        leftTurn = leftTurn91;
        emptyStackTurns = emptyStackTurns91;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 92;
    }

    void appendCurrentState91() {
        path91 = pos;
        stackSize91 = stackSize;
        leftTurn91 = leftTurn;
        emptyStackTurns91 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState93() {
        pos = path92;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize92;
        leftTurn = leftTurn92;
        emptyStackTurns = emptyStackTurns92;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 93;
    }

    void appendCurrentState92() {
        path92 = pos;
        stackSize92 = stackSize;
        leftTurn92 = leftTurn;
        emptyStackTurns92 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState94() {
        pos = path93;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize93;
        leftTurn = leftTurn93;
        emptyStackTurns = emptyStackTurns93;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 94;
    }

    void appendCurrentState93() {
        path93 = pos;
        stackSize93 = stackSize;
        leftTurn93 = leftTurn;
        emptyStackTurns93 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState95() {
        pos = path94;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize94;
        leftTurn = leftTurn94;
        emptyStackTurns = emptyStackTurns94;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 95;
    }

    void appendCurrentState94() {
        path94 = pos;
        stackSize94 = stackSize;
        leftTurn94 = leftTurn;
        emptyStackTurns94 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState96() {
        pos = path95;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize95;
        leftTurn = leftTurn95;
        emptyStackTurns = emptyStackTurns95;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 96;
    }

    void appendCurrentState95() {
        path95 = pos;
        stackSize95 = stackSize;
        leftTurn95 = leftTurn;
        emptyStackTurns95 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState97() {
        pos = path96;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize96;
        leftTurn = leftTurn96;
        emptyStackTurns = emptyStackTurns96;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 97;
    }

    void appendCurrentState96() {
        path96 = pos;
        stackSize96 = stackSize;
        leftTurn96 = leftTurn;
        emptyStackTurns96 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState98() {
        pos = path97;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize97;
        leftTurn = leftTurn97;
        emptyStackTurns = emptyStackTurns97;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 98;
    }

    void appendCurrentState97() {
        path97 = pos;
        stackSize97 = stackSize;
        leftTurn97 = leftTurn;
        emptyStackTurns97 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState99() {
        pos = path98;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize98;
        leftTurn = leftTurn98;
        emptyStackTurns = emptyStackTurns98;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 99;
    }

    void appendCurrentState98() {
        path98 = pos;
        stackSize98 = stackSize;
        leftTurn98 = leftTurn;
        emptyStackTurns98 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState100() {
        pos = path99;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize99;
        leftTurn = leftTurn99;
        emptyStackTurns = emptyStackTurns99;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 100;
    }

    void appendCurrentState99() {
        path99 = pos;
        stackSize99 = stackSize;
        leftTurn99 = leftTurn;
        emptyStackTurns99 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState101() {
        pos = path100;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize100;
        leftTurn = leftTurn100;
        emptyStackTurns = emptyStackTurns100;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 101;
    }

    void appendCurrentState100() {
        path100 = pos;
        stackSize100 = stackSize;
        leftTurn100 = leftTurn;
        emptyStackTurns100 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState102() {
        pos = path101;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize101;
        leftTurn = leftTurn101;
        emptyStackTurns = emptyStackTurns101;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 102;
    }

    void appendCurrentState101() {
        path101 = pos;
        stackSize101 = stackSize;
        leftTurn101 = leftTurn;
        emptyStackTurns101 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState103() {
        pos = path102;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize102;
        leftTurn = leftTurn102;
        emptyStackTurns = emptyStackTurns102;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 103;
    }

    void appendCurrentState102() {
        path102 = pos;
        stackSize102 = stackSize;
        leftTurn102 = leftTurn;
        emptyStackTurns102 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState104() {
        pos = path103;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize103;
        leftTurn = leftTurn103;
        emptyStackTurns = emptyStackTurns103;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 104;
    }

    void appendCurrentState103() {
        path103 = pos;
        stackSize103 = stackSize;
        leftTurn103 = leftTurn;
        emptyStackTurns103 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState105() {
        pos = path104;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize104;
        leftTurn = leftTurn104;
        emptyStackTurns = emptyStackTurns104;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 105;
    }

    void appendCurrentState104() {
        path104 = pos;
        stackSize104 = stackSize;
        leftTurn104 = leftTurn;
        emptyStackTurns104 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState106() {
        pos = path105;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize105;
        leftTurn = leftTurn105;
        emptyStackTurns = emptyStackTurns105;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 106;
    }

    void appendCurrentState105() {
        path105 = pos;
        stackSize105 = stackSize;
        leftTurn105 = leftTurn;
        emptyStackTurns105 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState107() {
        pos = path106;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize106;
        leftTurn = leftTurn106;
        emptyStackTurns = emptyStackTurns106;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 107;
    }

    void appendCurrentState106() {
        path106 = pos;
        stackSize106 = stackSize;
        leftTurn106 = leftTurn;
        emptyStackTurns106 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState108() {
        pos = path107;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize107;
        leftTurn = leftTurn107;
        emptyStackTurns = emptyStackTurns107;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 108;
    }

    void appendCurrentState107() {
        path107 = pos;
        stackSize107 = stackSize;
        leftTurn107 = leftTurn;
        emptyStackTurns107 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState109() {
        pos = path108;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize108;
        leftTurn = leftTurn108;
        emptyStackTurns = emptyStackTurns108;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 109;
    }

    void appendCurrentState108() {
        path108 = pos;
        stackSize108 = stackSize;
        leftTurn108 = leftTurn;
        emptyStackTurns108 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState110() {
        pos = path109;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize109;
        leftTurn = leftTurn109;
        emptyStackTurns = emptyStackTurns109;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 110;
    }

    void appendCurrentState109() {
        path109 = pos;
        stackSize109 = stackSize;
        leftTurn109 = leftTurn;
        emptyStackTurns109 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState111() {
        pos = path110;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize110;
        leftTurn = leftTurn110;
        emptyStackTurns = emptyStackTurns110;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 111;
    }

    void appendCurrentState110() {
        path110 = pos;
        stackSize110 = stackSize;
        leftTurn110 = leftTurn;
        emptyStackTurns110 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState112() {
        pos = path111;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize111;
        leftTurn = leftTurn111;
        emptyStackTurns = emptyStackTurns111;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 112;
    }

    void appendCurrentState111() {
        path111 = pos;
        stackSize111 = stackSize;
        leftTurn111 = leftTurn;
        emptyStackTurns111 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState113() {
        pos = path112;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize112;
        leftTurn = leftTurn112;
        emptyStackTurns = emptyStackTurns112;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 113;
    }

    void appendCurrentState112() {
        path112 = pos;
        stackSize112 = stackSize;
        leftTurn112 = leftTurn;
        emptyStackTurns112 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState114() {
        pos = path113;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize113;
        leftTurn = leftTurn113;
        emptyStackTurns = emptyStackTurns113;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 114;
    }

    void appendCurrentState113() {
        path113 = pos;
        stackSize113 = stackSize;
        leftTurn113 = leftTurn;
        emptyStackTurns113 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState115() {
        pos = path114;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize114;
        leftTurn = leftTurn114;
        emptyStackTurns = emptyStackTurns114;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 115;
    }

    void appendCurrentState114() {
        path114 = pos;
        stackSize114 = stackSize;
        leftTurn114 = leftTurn;
        emptyStackTurns114 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState116() {
        pos = path115;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize115;
        leftTurn = leftTurn115;
        emptyStackTurns = emptyStackTurns115;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 116;
    }

    void appendCurrentState115() {
        path115 = pos;
        stackSize115 = stackSize;
        leftTurn115 = leftTurn;
        emptyStackTurns115 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState117() {
        pos = path116;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize116;
        leftTurn = leftTurn116;
        emptyStackTurns = emptyStackTurns116;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 117;
    }

    void appendCurrentState116() {
        path116 = pos;
        stackSize116 = stackSize;
        leftTurn116 = leftTurn;
        emptyStackTurns116 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState118() {
        pos = path117;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize117;
        leftTurn = leftTurn117;
        emptyStackTurns = emptyStackTurns117;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 118;
    }

    void appendCurrentState117() {
        path117 = pos;
        stackSize117 = stackSize;
        leftTurn117 = leftTurn;
        emptyStackTurns117 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState119() {
        pos = path118;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize118;
        leftTurn = leftTurn118;
        emptyStackTurns = emptyStackTurns118;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 119;
    }

    void appendCurrentState118() {
        path118 = pos;
        stackSize118 = stackSize;
        leftTurn118 = leftTurn;
        emptyStackTurns118 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState120() {
        pos = path119;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize119;
        leftTurn = leftTurn119;
        emptyStackTurns = emptyStackTurns119;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 120;
    }

    void appendCurrentState119() {
        path119 = pos;
        stackSize119 = stackSize;
        leftTurn119 = leftTurn;
        emptyStackTurns119 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState121() {
        pos = path120;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize120;
        leftTurn = leftTurn120;
        emptyStackTurns = emptyStackTurns120;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 121;
    }

    void appendCurrentState120() {
        path120 = pos;
        stackSize120 = stackSize;
        leftTurn120 = leftTurn;
        emptyStackTurns120 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState122() {
        pos = path121;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize121;
        leftTurn = leftTurn121;
        emptyStackTurns = emptyStackTurns121;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 122;
    }

    void appendCurrentState121() {
        path121 = pos;
        stackSize121 = stackSize;
        leftTurn121 = leftTurn;
        emptyStackTurns121 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState123() {
        pos = path122;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize122;
        leftTurn = leftTurn122;
        emptyStackTurns = emptyStackTurns122;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 123;
    }

    void appendCurrentState122() {
        path122 = pos;
        stackSize122 = stackSize;
        leftTurn122 = leftTurn;
        emptyStackTurns122 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState124() {
        pos = path123;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize123;
        leftTurn = leftTurn123;
        emptyStackTurns = emptyStackTurns123;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 124;
    }

    void appendCurrentState123() {
        path123 = pos;
        stackSize123 = stackSize;
        leftTurn123 = leftTurn;
        emptyStackTurns123 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState125() {
        pos = path124;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize124;
        leftTurn = leftTurn124;
        emptyStackTurns = emptyStackTurns124;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 125;
    }

    void appendCurrentState124() {
        path124 = pos;
        stackSize124 = stackSize;
        leftTurn124 = leftTurn;
        emptyStackTurns124 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState126() {
        pos = path125;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize125;
        leftTurn = leftTurn125;
        emptyStackTurns = emptyStackTurns125;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 126;
    }

    void appendCurrentState125() {
        path125 = pos;
        stackSize125 = stackSize;
        leftTurn125 = leftTurn;
        emptyStackTurns125 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState127() {
        pos = path126;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize126;
        leftTurn = leftTurn126;
        emptyStackTurns = emptyStackTurns126;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 127;
    }

    void appendCurrentState126() {
        path126 = pos;
        stackSize126 = stackSize;
        leftTurn126 = leftTurn;
        emptyStackTurns126 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState128() {
        pos = path127;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize127;
        leftTurn = leftTurn127;
        emptyStackTurns = emptyStackTurns127;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 128;
    }

    void appendCurrentState127() {
        path127 = pos;
        stackSize127 = stackSize;
        leftTurn127 = leftTurn;
        emptyStackTurns127 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState129() {
        pos = path128;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize128;
        leftTurn = leftTurn128;
        emptyStackTurns = emptyStackTurns128;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 129;
    }

    void appendCurrentState128() {
        path128 = pos;
        stackSize128 = stackSize;
        leftTurn128 = leftTurn;
        emptyStackTurns128 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState130() {
        pos = path129;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize129;
        leftTurn = leftTurn129;
        emptyStackTurns = emptyStackTurns129;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 130;
    }

    void appendCurrentState129() {
        path129 = pos;
        stackSize129 = stackSize;
        leftTurn129 = leftTurn;
        emptyStackTurns129 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState131() {
        pos = path130;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize130;
        leftTurn = leftTurn130;
        emptyStackTurns = emptyStackTurns130;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 131;
    }

    void appendCurrentState130() {
        path130 = pos;
        stackSize130 = stackSize;
        leftTurn130 = leftTurn;
        emptyStackTurns130 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState132() {
        pos = path131;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize131;
        leftTurn = leftTurn131;
        emptyStackTurns = emptyStackTurns131;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 132;
    }

    void appendCurrentState131() {
        path131 = pos;
        stackSize131 = stackSize;
        leftTurn131 = leftTurn;
        emptyStackTurns131 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState133() {
        pos = path132;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize132;
        leftTurn = leftTurn132;
        emptyStackTurns = emptyStackTurns132;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 133;
    }

    void appendCurrentState132() {
        path132 = pos;
        stackSize132 = stackSize;
        leftTurn132 = leftTurn;
        emptyStackTurns132 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState134() {
        pos = path133;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize133;
        leftTurn = leftTurn133;
        emptyStackTurns = emptyStackTurns133;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 134;
    }

    void appendCurrentState133() {
        path133 = pos;
        stackSize133 = stackSize;
        leftTurn133 = leftTurn;
        emptyStackTurns133 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState135() {
        pos = path134;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize134;
        leftTurn = leftTurn134;
        emptyStackTurns = emptyStackTurns134;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 135;
    }

    void appendCurrentState134() {
        path134 = pos;
        stackSize134 = stackSize;
        leftTurn134 = leftTurn;
        emptyStackTurns134 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState136() {
        pos = path135;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize135;
        leftTurn = leftTurn135;
        emptyStackTurns = emptyStackTurns135;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 136;
    }

    void appendCurrentState135() {
        path135 = pos;
        stackSize135 = stackSize;
        leftTurn135 = leftTurn;
        emptyStackTurns135 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState137() {
        pos = path136;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize136;
        leftTurn = leftTurn136;
        emptyStackTurns = emptyStackTurns136;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 137;
    }

    void appendCurrentState136() {
        path136 = pos;
        stackSize136 = stackSize;
        leftTurn136 = leftTurn;
        emptyStackTurns136 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState138() {
        pos = path137;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize137;
        leftTurn = leftTurn137;
        emptyStackTurns = emptyStackTurns137;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 138;
    }

    void appendCurrentState137() {
        path137 = pos;
        stackSize137 = stackSize;
        leftTurn137 = leftTurn;
        emptyStackTurns137 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState139() {
        pos = path138;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize138;
        leftTurn = leftTurn138;
        emptyStackTurns = emptyStackTurns138;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 139;
    }

    void appendCurrentState138() {
        path138 = pos;
        stackSize138 = stackSize;
        leftTurn138 = leftTurn;
        emptyStackTurns138 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState140() {
        pos = path139;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize139;
        leftTurn = leftTurn139;
        emptyStackTurns = emptyStackTurns139;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 140;
    }

    void appendCurrentState139() {
        path139 = pos;
        stackSize139 = stackSize;
        leftTurn139 = leftTurn;
        emptyStackTurns139 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState141() {
        pos = path140;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize140;
        leftTurn = leftTurn140;
        emptyStackTurns = emptyStackTurns140;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 141;
    }

    void appendCurrentState140() {
        path140 = pos;
        stackSize140 = stackSize;
        leftTurn140 = leftTurn;
        emptyStackTurns140 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState142() {
        pos = path141;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize141;
        leftTurn = leftTurn141;
        emptyStackTurns = emptyStackTurns141;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 142;
    }

    void appendCurrentState141() {
        path141 = pos;
        stackSize141 = stackSize;
        leftTurn141 = leftTurn;
        emptyStackTurns141 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState143() {
        pos = path142;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize142;
        leftTurn = leftTurn142;
        emptyStackTurns = emptyStackTurns142;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 143;
    }

    void appendCurrentState142() {
        path142 = pos;
        stackSize142 = stackSize;
        leftTurn142 = leftTurn;
        emptyStackTurns142 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState144() {
        pos = path143;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize143;
        leftTurn = leftTurn143;
        emptyStackTurns = emptyStackTurns143;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 144;
    }

    void appendCurrentState143() {
        path143 = pos;
        stackSize143 = stackSize;
        leftTurn143 = leftTurn;
        emptyStackTurns143 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState145() {
        pos = path144;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize144;
        leftTurn = leftTurn144;
        emptyStackTurns = emptyStackTurns144;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 145;
    }

    void appendCurrentState144() {
        path144 = pos;
        stackSize144 = stackSize;
        leftTurn144 = leftTurn;
        emptyStackTurns144 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState146() {
        pos = path145;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize145;
        leftTurn = leftTurn145;
        emptyStackTurns = emptyStackTurns145;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 146;
    }

    void appendCurrentState145() {
        path145 = pos;
        stackSize145 = stackSize;
        leftTurn145 = leftTurn;
        emptyStackTurns145 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState147() {
        pos = path146;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize146;
        leftTurn = leftTurn146;
        emptyStackTurns = emptyStackTurns146;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 147;
    }

    void appendCurrentState146() {
        path146 = pos;
        stackSize146 = stackSize;
        leftTurn146 = leftTurn;
        emptyStackTurns146 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState148() {
        pos = path147;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize147;
        leftTurn = leftTurn147;
        emptyStackTurns = emptyStackTurns147;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 148;
    }

    void appendCurrentState147() {
        path147 = pos;
        stackSize147 = stackSize;
        leftTurn147 = leftTurn;
        emptyStackTurns147 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState149() {
        pos = path148;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize148;
        leftTurn = leftTurn148;
        emptyStackTurns = emptyStackTurns148;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 149;
    }

    void appendCurrentState148() {
        path148 = pos;
        stackSize148 = stackSize;
        leftTurn148 = leftTurn;
        emptyStackTurns148 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState150() {
        pos = path149;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize149;
        leftTurn = leftTurn149;
        emptyStackTurns = emptyStackTurns149;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 150;
    }

    void appendCurrentState149() {
        path149 = pos;
        stackSize149 = stackSize;
        leftTurn149 = leftTurn;
        emptyStackTurns149 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState151() {
        pos = path150;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize150;
        leftTurn = leftTurn150;
        emptyStackTurns = emptyStackTurns150;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 151;
    }

    void appendCurrentState150() {
        path150 = pos;
        stackSize150 = stackSize;
        leftTurn150 = leftTurn;
        emptyStackTurns150 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState152() {
        pos = path151;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize151;
        leftTurn = leftTurn151;
        emptyStackTurns = emptyStackTurns151;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 152;
    }

    void appendCurrentState151() {
        path151 = pos;
        stackSize151 = stackSize;
        leftTurn151 = leftTurn;
        emptyStackTurns151 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState153() {
        pos = path152;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize152;
        leftTurn = leftTurn152;
        emptyStackTurns = emptyStackTurns152;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 153;
    }

    void appendCurrentState152() {
        path152 = pos;
        stackSize152 = stackSize;
        leftTurn152 = leftTurn;
        emptyStackTurns152 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState154() {
        pos = path153;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize153;
        leftTurn = leftTurn153;
        emptyStackTurns = emptyStackTurns153;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 154;
    }

    void appendCurrentState153() {
        path153 = pos;
        stackSize153 = stackSize;
        leftTurn153 = leftTurn;
        emptyStackTurns153 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState155() {
        pos = path154;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize154;
        leftTurn = leftTurn154;
        emptyStackTurns = emptyStackTurns154;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 155;
    }

    void appendCurrentState154() {
        path154 = pos;
        stackSize154 = stackSize;
        leftTurn154 = leftTurn;
        emptyStackTurns154 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState156() {
        pos = path155;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize155;
        leftTurn = leftTurn155;
        emptyStackTurns = emptyStackTurns155;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 156;
    }

    void appendCurrentState155() {
        path155 = pos;
        stackSize155 = stackSize;
        leftTurn155 = leftTurn;
        emptyStackTurns155 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState157() {
        pos = path156;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize156;
        leftTurn = leftTurn156;
        emptyStackTurns = emptyStackTurns156;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 157;
    }

    void appendCurrentState156() {
        path156 = pos;
        stackSize156 = stackSize;
        leftTurn156 = leftTurn;
        emptyStackTurns156 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState158() {
        pos = path157;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize157;
        leftTurn = leftTurn157;
        emptyStackTurns = emptyStackTurns157;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 158;
    }

    void appendCurrentState157() {
        path157 = pos;
        stackSize157 = stackSize;
        leftTurn157 = leftTurn;
        emptyStackTurns157 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState159() {
        pos = path158;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize158;
        leftTurn = leftTurn158;
        emptyStackTurns = emptyStackTurns158;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 159;
    }

    void appendCurrentState158() {
        path158 = pos;
        stackSize158 = stackSize;
        leftTurn158 = leftTurn;
        emptyStackTurns158 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState160() {
        pos = path159;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize159;
        leftTurn = leftTurn159;
        emptyStackTurns = emptyStackTurns159;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 160;
    }

    void appendCurrentState159() {
        path159 = pos;
        stackSize159 = stackSize;
        leftTurn159 = leftTurn;
        emptyStackTurns159 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState161() {
        pos = path160;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize160;
        leftTurn = leftTurn160;
        emptyStackTurns = emptyStackTurns160;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 161;
    }

    void appendCurrentState160() {
        path160 = pos;
        stackSize160 = stackSize;
        leftTurn160 = leftTurn;
        emptyStackTurns160 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState162() {
        pos = path161;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize161;
        leftTurn = leftTurn161;
        emptyStackTurns = emptyStackTurns161;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 162;
    }

    void appendCurrentState161() {
        path161 = pos;
        stackSize161 = stackSize;
        leftTurn161 = leftTurn;
        emptyStackTurns161 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState163() {
        pos = path162;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize162;
        leftTurn = leftTurn162;
        emptyStackTurns = emptyStackTurns162;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 163;
    }

    void appendCurrentState162() {
        path162 = pos;
        stackSize162 = stackSize;
        leftTurn162 = leftTurn;
        emptyStackTurns162 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState164() {
        pos = path163;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize163;
        leftTurn = leftTurn163;
        emptyStackTurns = emptyStackTurns163;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 164;
    }

    void appendCurrentState163() {
        path163 = pos;
        stackSize163 = stackSize;
        leftTurn163 = leftTurn;
        emptyStackTurns163 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState165() {
        pos = path164;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize164;
        leftTurn = leftTurn164;
        emptyStackTurns = emptyStackTurns164;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 165;
    }

    void appendCurrentState164() {
        path164 = pos;
        stackSize164 = stackSize;
        leftTurn164 = leftTurn;
        emptyStackTurns164 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState166() {
        pos = path165;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize165;
        leftTurn = leftTurn165;
        emptyStackTurns = emptyStackTurns165;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 166;
    }

    void appendCurrentState165() {
        path165 = pos;
        stackSize165 = stackSize;
        leftTurn165 = leftTurn;
        emptyStackTurns165 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState167() {
        pos = path166;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize166;
        leftTurn = leftTurn166;
        emptyStackTurns = emptyStackTurns166;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 167;
    }

    void appendCurrentState166() {
        path166 = pos;
        stackSize166 = stackSize;
        leftTurn166 = leftTurn;
        emptyStackTurns166 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState168() {
        pos = path167;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize167;
        leftTurn = leftTurn167;
        emptyStackTurns = emptyStackTurns167;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 168;
    }

    void appendCurrentState167() {
        path167 = pos;
        stackSize167 = stackSize;
        leftTurn167 = leftTurn;
        emptyStackTurns167 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState169() {
        pos = path168;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize168;
        leftTurn = leftTurn168;
        emptyStackTurns = emptyStackTurns168;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 169;
    }

    void appendCurrentState168() {
        path168 = pos;
        stackSize168 = stackSize;
        leftTurn168 = leftTurn;
        emptyStackTurns168 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState170() {
        pos = path169;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize169;
        leftTurn = leftTurn169;
        emptyStackTurns = emptyStackTurns169;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 170;
    }

    void appendCurrentState169() {
        path169 = pos;
        stackSize169 = stackSize;
        leftTurn169 = leftTurn;
        emptyStackTurns169 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState171() {
        pos = path170;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize170;
        leftTurn = leftTurn170;
        emptyStackTurns = emptyStackTurns170;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 171;
    }

    void appendCurrentState170() {
        path170 = pos;
        stackSize170 = stackSize;
        leftTurn170 = leftTurn;
        emptyStackTurns170 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState172() {
        pos = path171;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize171;
        leftTurn = leftTurn171;
        emptyStackTurns = emptyStackTurns171;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 172;
    }

    void appendCurrentState171() {
        path171 = pos;
        stackSize171 = stackSize;
        leftTurn171 = leftTurn;
        emptyStackTurns171 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState173() {
        pos = path172;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize172;
        leftTurn = leftTurn172;
        emptyStackTurns = emptyStackTurns172;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 173;
    }

    void appendCurrentState172() {
        path172 = pos;
        stackSize172 = stackSize;
        leftTurn172 = leftTurn;
        emptyStackTurns172 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState174() {
        pos = path173;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize173;
        leftTurn = leftTurn173;
        emptyStackTurns = emptyStackTurns173;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 174;
    }

    void appendCurrentState173() {
        path173 = pos;
        stackSize173 = stackSize;
        leftTurn173 = leftTurn;
        emptyStackTurns173 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState175() {
        pos = path174;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize174;
        leftTurn = leftTurn174;
        emptyStackTurns = emptyStackTurns174;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 175;
    }

    void appendCurrentState174() {
        path174 = pos;
        stackSize174 = stackSize;
        leftTurn174 = leftTurn;
        emptyStackTurns174 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState176() {
        pos = path175;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize175;
        leftTurn = leftTurn175;
        emptyStackTurns = emptyStackTurns175;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 176;
    }

    void appendCurrentState175() {
        path175 = pos;
        stackSize175 = stackSize;
        leftTurn175 = leftTurn;
        emptyStackTurns175 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState177() {
        pos = path176;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize176;
        leftTurn = leftTurn176;
        emptyStackTurns = emptyStackTurns176;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 177;
    }

    void appendCurrentState176() {
        path176 = pos;
        stackSize176 = stackSize;
        leftTurn176 = leftTurn;
        emptyStackTurns176 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState178() {
        pos = path177;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize177;
        leftTurn = leftTurn177;
        emptyStackTurns = emptyStackTurns177;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 178;
    }

    void appendCurrentState177() {
        path177 = pos;
        stackSize177 = stackSize;
        leftTurn177 = leftTurn;
        emptyStackTurns177 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState179() {
        pos = path178;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize178;
        leftTurn = leftTurn178;
        emptyStackTurns = emptyStackTurns178;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 179;
    }

    void appendCurrentState178() {
        path178 = pos;
        stackSize178 = stackSize;
        leftTurn178 = leftTurn;
        emptyStackTurns178 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState180() {
        pos = path179;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize179;
        leftTurn = leftTurn179;
        emptyStackTurns = emptyStackTurns179;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 180;
    }

    void appendCurrentState179() {
        path179 = pos;
        stackSize179 = stackSize;
        leftTurn179 = leftTurn;
        emptyStackTurns179 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState181() {
        pos = path180;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize180;
        leftTurn = leftTurn180;
        emptyStackTurns = emptyStackTurns180;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 181;
    }

    void appendCurrentState180() {
        path180 = pos;
        stackSize180 = stackSize;
        leftTurn180 = leftTurn;
        emptyStackTurns180 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState182() {
        pos = path181;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize181;
        leftTurn = leftTurn181;
        emptyStackTurns = emptyStackTurns181;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 182;
    }

    void appendCurrentState181() {
        path181 = pos;
        stackSize181 = stackSize;
        leftTurn181 = leftTurn;
        emptyStackTurns181 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState183() {
        pos = path182;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize182;
        leftTurn = leftTurn182;
        emptyStackTurns = emptyStackTurns182;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 183;
    }

    void appendCurrentState182() {
        path182 = pos;
        stackSize182 = stackSize;
        leftTurn182 = leftTurn;
        emptyStackTurns182 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState184() {
        pos = path183;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize183;
        leftTurn = leftTurn183;
        emptyStackTurns = emptyStackTurns183;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 184;
    }

    void appendCurrentState183() {
        path183 = pos;
        stackSize183 = stackSize;
        leftTurn183 = leftTurn;
        emptyStackTurns183 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState185() {
        pos = path184;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize184;
        leftTurn = leftTurn184;
        emptyStackTurns = emptyStackTurns184;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 185;
    }

    void appendCurrentState184() {
        path184 = pos;
        stackSize184 = stackSize;
        leftTurn184 = leftTurn;
        emptyStackTurns184 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState186() {
        pos = path185;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize185;
        leftTurn = leftTurn185;
        emptyStackTurns = emptyStackTurns185;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 186;
    }

    void appendCurrentState185() {
        path185 = pos;
        stackSize185 = stackSize;
        leftTurn185 = leftTurn;
        emptyStackTurns185 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState187() {
        pos = path186;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize186;
        leftTurn = leftTurn186;
        emptyStackTurns = emptyStackTurns186;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 187;
    }

    void appendCurrentState186() {
        path186 = pos;
        stackSize186 = stackSize;
        leftTurn186 = leftTurn;
        emptyStackTurns186 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState188() {
        pos = path187;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize187;
        leftTurn = leftTurn187;
        emptyStackTurns = emptyStackTurns187;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 188;
    }

    void appendCurrentState187() {
        path187 = pos;
        stackSize187 = stackSize;
        leftTurn187 = leftTurn;
        emptyStackTurns187 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState189() {
        pos = path188;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize188;
        leftTurn = leftTurn188;
        emptyStackTurns = emptyStackTurns188;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 189;
    }

    void appendCurrentState188() {
        path188 = pos;
        stackSize188 = stackSize;
        leftTurn188 = leftTurn;
        emptyStackTurns188 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState190() {
        pos = path189;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize189;
        leftTurn = leftTurn189;
        emptyStackTurns = emptyStackTurns189;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 190;
    }

    void appendCurrentState189() {
        path189 = pos;
        stackSize189 = stackSize;
        leftTurn189 = leftTurn;
        emptyStackTurns189 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState191() {
        pos = path190;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize190;
        leftTurn = leftTurn190;
        emptyStackTurns = emptyStackTurns190;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 191;
    }

    void appendCurrentState190() {
        path190 = pos;
        stackSize190 = stackSize;
        leftTurn190 = leftTurn;
        emptyStackTurns190 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState192() {
        pos = path191;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize191;
        leftTurn = leftTurn191;
        emptyStackTurns = emptyStackTurns191;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 192;
    }

    void appendCurrentState191() {
        path191 = pos;
        stackSize191 = stackSize;
        leftTurn191 = leftTurn;
        emptyStackTurns191 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState193() {
        pos = path192;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize192;
        leftTurn = leftTurn192;
        emptyStackTurns = emptyStackTurns192;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 193;
    }

    void appendCurrentState192() {
        path192 = pos;
        stackSize192 = stackSize;
        leftTurn192 = leftTurn;
        emptyStackTurns192 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState194() {
        pos = path193;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize193;
        leftTurn = leftTurn193;
        emptyStackTurns = emptyStackTurns193;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 194;
    }

    void appendCurrentState193() {
        path193 = pos;
        stackSize193 = stackSize;
        leftTurn193 = leftTurn;
        emptyStackTurns193 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState195() {
        pos = path194;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize194;
        leftTurn = leftTurn194;
        emptyStackTurns = emptyStackTurns194;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 195;
    }

    void appendCurrentState194() {
        path194 = pos;
        stackSize194 = stackSize;
        leftTurn194 = leftTurn;
        emptyStackTurns194 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState196() {
        pos = path195;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize195;
        leftTurn = leftTurn195;
        emptyStackTurns = emptyStackTurns195;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 196;
    }

    void appendCurrentState195() {
        path195 = pos;
        stackSize195 = stackSize;
        leftTurn195 = leftTurn;
        emptyStackTurns195 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState197() {
        pos = path196;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize196;
        leftTurn = leftTurn196;
        emptyStackTurns = emptyStackTurns196;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 197;
    }

    void appendCurrentState196() {
        path196 = pos;
        stackSize196 = stackSize;
        leftTurn196 = leftTurn;
        emptyStackTurns196 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState198() {
        pos = path197;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize197;
        leftTurn = leftTurn197;
        emptyStackTurns = emptyStackTurns197;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 198;
    }

    void appendCurrentState197() {
        path197 = pos;
        stackSize197 = stackSize;
        leftTurn197 = leftTurn;
        emptyStackTurns197 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState199() {
        pos = path198;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize198;
        leftTurn = leftTurn198;
        emptyStackTurns = emptyStackTurns198;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 199;
    }

    void appendCurrentState198() {
        path198 = pos;
        stackSize198 = stackSize;
        leftTurn198 = leftTurn;
        emptyStackTurns198 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState200() {
        pos = path199;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize199;
        leftTurn = leftTurn199;
        emptyStackTurns = emptyStackTurns199;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 200;
    }

    void appendCurrentState199() {
        path199 = pos;
        stackSize199 = stackSize;
        leftTurn199 = leftTurn;
        emptyStackTurns199 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState201() {
        pos = path200;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize200;
        leftTurn = leftTurn200;
        emptyStackTurns = emptyStackTurns200;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 201;
    }

    void appendCurrentState200() {
        path200 = pos;
        stackSize200 = stackSize;
        leftTurn200 = leftTurn;
        emptyStackTurns200 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState202() {
        pos = path201;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize201;
        leftTurn = leftTurn201;
        emptyStackTurns = emptyStackTurns201;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 202;
    }

    void appendCurrentState201() {
        path201 = pos;
        stackSize201 = stackSize;
        leftTurn201 = leftTurn;
        emptyStackTurns201 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState203() {
        pos = path202;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize202;
        leftTurn = leftTurn202;
        emptyStackTurns = emptyStackTurns202;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 203;
    }

    void appendCurrentState202() {
        path202 = pos;
        stackSize202 = stackSize;
        leftTurn202 = leftTurn;
        emptyStackTurns202 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState204() {
        pos = path203;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize203;
        leftTurn = leftTurn203;
        emptyStackTurns = emptyStackTurns203;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 204;
    }

    void appendCurrentState203() {
        path203 = pos;
        stackSize203 = stackSize;
        leftTurn203 = leftTurn;
        emptyStackTurns203 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState205() {
        pos = path204;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize204;
        leftTurn = leftTurn204;
        emptyStackTurns = emptyStackTurns204;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 205;
    }

    void appendCurrentState204() {
        path204 = pos;
        stackSize204 = stackSize;
        leftTurn204 = leftTurn;
        emptyStackTurns204 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState206() {
        pos = path205;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize205;
        leftTurn = leftTurn205;
        emptyStackTurns = emptyStackTurns205;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 206;
    }

    void appendCurrentState205() {
        path205 = pos;
        stackSize205 = stackSize;
        leftTurn205 = leftTurn;
        emptyStackTurns205 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState207() {
        pos = path206;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize206;
        leftTurn = leftTurn206;
        emptyStackTurns = emptyStackTurns206;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 207;
    }

    void appendCurrentState206() {
        path206 = pos;
        stackSize206 = stackSize;
        leftTurn206 = leftTurn;
        emptyStackTurns206 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState208() {
        pos = path207;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize207;
        leftTurn = leftTurn207;
        emptyStackTurns = emptyStackTurns207;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 208;
    }

    void appendCurrentState207() {
        path207 = pos;
        stackSize207 = stackSize;
        leftTurn207 = leftTurn;
        emptyStackTurns207 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState209() {
        pos = path208;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize208;
        leftTurn = leftTurn208;
        emptyStackTurns = emptyStackTurns208;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 209;
    }

    void appendCurrentState208() {
        path208 = pos;
        stackSize208 = stackSize;
        leftTurn208 = leftTurn;
        emptyStackTurns208 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState210() {
        pos = path209;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize209;
        leftTurn = leftTurn209;
        emptyStackTurns = emptyStackTurns209;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 210;
    }

    void appendCurrentState209() {
        path209 = pos;
        stackSize209 = stackSize;
        leftTurn209 = leftTurn;
        emptyStackTurns209 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState211() {
        pos = path210;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize210;
        leftTurn = leftTurn210;
        emptyStackTurns = emptyStackTurns210;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 211;
    }

    void appendCurrentState210() {
        path210 = pos;
        stackSize210 = stackSize;
        leftTurn210 = leftTurn;
        emptyStackTurns210 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState212() {
        pos = path211;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize211;
        leftTurn = leftTurn211;
        emptyStackTurns = emptyStackTurns211;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 212;
    }

    void appendCurrentState211() {
        path211 = pos;
        stackSize211 = stackSize;
        leftTurn211 = leftTurn;
        emptyStackTurns211 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState213() {
        pos = path212;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize212;
        leftTurn = leftTurn212;
        emptyStackTurns = emptyStackTurns212;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 213;
    }

    void appendCurrentState212() {
        path212 = pos;
        stackSize212 = stackSize;
        leftTurn212 = leftTurn;
        emptyStackTurns212 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState214() {
        pos = path213;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize213;
        leftTurn = leftTurn213;
        emptyStackTurns = emptyStackTurns213;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 214;
    }

    void appendCurrentState213() {
        path213 = pos;
        stackSize213 = stackSize;
        leftTurn213 = leftTurn;
        emptyStackTurns213 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState215() {
        pos = path214;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize214;
        leftTurn = leftTurn214;
        emptyStackTurns = emptyStackTurns214;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 215;
    }

    void appendCurrentState214() {
        path214 = pos;
        stackSize214 = stackSize;
        leftTurn214 = leftTurn;
        emptyStackTurns214 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState216() {
        pos = path215;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize215;
        leftTurn = leftTurn215;
        emptyStackTurns = emptyStackTurns215;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 216;
    }

    void appendCurrentState215() {
        path215 = pos;
        stackSize215 = stackSize;
        leftTurn215 = leftTurn;
        emptyStackTurns215 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState217() {
        pos = path216;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize216;
        leftTurn = leftTurn216;
        emptyStackTurns = emptyStackTurns216;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 217;
    }

    void appendCurrentState216() {
        path216 = pos;
        stackSize216 = stackSize;
        leftTurn216 = leftTurn;
        emptyStackTurns216 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState218() {
        pos = path217;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize217;
        leftTurn = leftTurn217;
        emptyStackTurns = emptyStackTurns217;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 218;
    }

    void appendCurrentState217() {
        path217 = pos;
        stackSize217 = stackSize;
        leftTurn217 = leftTurn;
        emptyStackTurns217 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState219() {
        pos = path218;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize218;
        leftTurn = leftTurn218;
        emptyStackTurns = emptyStackTurns218;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 219;
    }

    void appendCurrentState218() {
        path218 = pos;
        stackSize218 = stackSize;
        leftTurn218 = leftTurn;
        emptyStackTurns218 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState220() {
        pos = path219;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize219;
        leftTurn = leftTurn219;
        emptyStackTurns = emptyStackTurns219;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 220;
    }

    void appendCurrentState219() {
        path219 = pos;
        stackSize219 = stackSize;
        leftTurn219 = leftTurn;
        emptyStackTurns219 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState221() {
        pos = path220;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize220;
        leftTurn = leftTurn220;
        emptyStackTurns = emptyStackTurns220;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 221;
    }

    void appendCurrentState220() {
        path220 = pos;
        stackSize220 = stackSize;
        leftTurn220 = leftTurn;
        emptyStackTurns220 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState222() {
        pos = path221;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize221;
        leftTurn = leftTurn221;
        emptyStackTurns = emptyStackTurns221;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 222;
    }

    void appendCurrentState221() {
        path221 = pos;
        stackSize221 = stackSize;
        leftTurn221 = leftTurn;
        emptyStackTurns221 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState223() {
        pos = path222;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize222;
        leftTurn = leftTurn222;
        emptyStackTurns = emptyStackTurns222;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 223;
    }

    void appendCurrentState222() {
        path222 = pos;
        stackSize222 = stackSize;
        leftTurn222 = leftTurn;
        emptyStackTurns222 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState224() {
        pos = path223;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize223;
        leftTurn = leftTurn223;
        emptyStackTurns = emptyStackTurns223;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 224;
    }

    void appendCurrentState223() {
        path223 = pos;
        stackSize223 = stackSize;
        leftTurn223 = leftTurn;
        emptyStackTurns223 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState225() {
        pos = path224;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize224;
        leftTurn = leftTurn224;
        emptyStackTurns = emptyStackTurns224;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 225;
    }

    void appendCurrentState224() {
        path224 = pos;
        stackSize224 = stackSize;
        leftTurn224 = leftTurn;
        emptyStackTurns224 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState226() {
        pos = path225;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize225;
        leftTurn = leftTurn225;
        emptyStackTurns = emptyStackTurns225;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 226;
    }

    void appendCurrentState225() {
        path225 = pos;
        stackSize225 = stackSize;
        leftTurn225 = leftTurn;
        emptyStackTurns225 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState227() {
        pos = path226;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize226;
        leftTurn = leftTurn226;
        emptyStackTurns = emptyStackTurns226;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 227;
    }

    void appendCurrentState226() {
        path226 = pos;
        stackSize226 = stackSize;
        leftTurn226 = leftTurn;
        emptyStackTurns226 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState228() {
        pos = path227;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize227;
        leftTurn = leftTurn227;
        emptyStackTurns = emptyStackTurns227;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 228;
    }

    void appendCurrentState227() {
        path227 = pos;
        stackSize227 = stackSize;
        leftTurn227 = leftTurn;
        emptyStackTurns227 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState229() {
        pos = path228;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize228;
        leftTurn = leftTurn228;
        emptyStackTurns = emptyStackTurns228;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 229;
    }

    void appendCurrentState228() {
        path228 = pos;
        stackSize228 = stackSize;
        leftTurn228 = leftTurn;
        emptyStackTurns228 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState230() {
        pos = path229;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize229;
        leftTurn = leftTurn229;
        emptyStackTurns = emptyStackTurns229;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 230;
    }

    void appendCurrentState229() {
        path229 = pos;
        stackSize229 = stackSize;
        leftTurn229 = leftTurn;
        emptyStackTurns229 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState231() {
        pos = path230;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize230;
        leftTurn = leftTurn230;
        emptyStackTurns = emptyStackTurns230;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 231;
    }

    void appendCurrentState230() {
        path230 = pos;
        stackSize230 = stackSize;
        leftTurn230 = leftTurn;
        emptyStackTurns230 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState232() {
        pos = path231;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize231;
        leftTurn = leftTurn231;
        emptyStackTurns = emptyStackTurns231;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 232;
    }

    void appendCurrentState231() {
        path231 = pos;
        stackSize231 = stackSize;
        leftTurn231 = leftTurn;
        emptyStackTurns231 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState233() {
        pos = path232;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize232;
        leftTurn = leftTurn232;
        emptyStackTurns = emptyStackTurns232;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 233;
    }

    void appendCurrentState232() {
        path232 = pos;
        stackSize232 = stackSize;
        leftTurn232 = leftTurn;
        emptyStackTurns232 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState234() {
        pos = path233;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize233;
        leftTurn = leftTurn233;
        emptyStackTurns = emptyStackTurns233;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 234;
    }

    void appendCurrentState233() {
        path233 = pos;
        stackSize233 = stackSize;
        leftTurn233 = leftTurn;
        emptyStackTurns233 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState235() {
        pos = path234;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize234;
        leftTurn = leftTurn234;
        emptyStackTurns = emptyStackTurns234;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 235;
    }

    void appendCurrentState234() {
        path234 = pos;
        stackSize234 = stackSize;
        leftTurn234 = leftTurn;
        emptyStackTurns234 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState236() {
        pos = path235;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize235;
        leftTurn = leftTurn235;
        emptyStackTurns = emptyStackTurns235;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 236;
    }

    void appendCurrentState235() {
        path235 = pos;
        stackSize235 = stackSize;
        leftTurn235 = leftTurn;
        emptyStackTurns235 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState237() {
        pos = path236;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize236;
        leftTurn = leftTurn236;
        emptyStackTurns = emptyStackTurns236;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 237;
    }

    void appendCurrentState236() {
        path236 = pos;
        stackSize236 = stackSize;
        leftTurn236 = leftTurn;
        emptyStackTurns236 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState238() {
        pos = path237;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize237;
        leftTurn = leftTurn237;
        emptyStackTurns = emptyStackTurns237;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 238;
    }

    void appendCurrentState237() {
        path237 = pos;
        stackSize237 = stackSize;
        leftTurn237 = leftTurn;
        emptyStackTurns237 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState239() {
        pos = path238;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize238;
        leftTurn = leftTurn238;
        emptyStackTurns = emptyStackTurns238;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 239;
    }

    void appendCurrentState238() {
        path238 = pos;
        stackSize238 = stackSize;
        leftTurn238 = leftTurn;
        emptyStackTurns238 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState240() {
        pos = path239;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize239;
        leftTurn = leftTurn239;
        emptyStackTurns = emptyStackTurns239;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 240;
    }

    void appendCurrentState239() {
        path239 = pos;
        stackSize239 = stackSize;
        leftTurn239 = leftTurn;
        emptyStackTurns239 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState241() {
        pos = path240;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize240;
        leftTurn = leftTurn240;
        emptyStackTurns = emptyStackTurns240;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 241;
    }

    void appendCurrentState240() {
        path240 = pos;
        stackSize240 = stackSize;
        leftTurn240 = leftTurn;
        emptyStackTurns240 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState242() {
        pos = path241;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize241;
        leftTurn = leftTurn241;
        emptyStackTurns = emptyStackTurns241;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 242;
    }

    void appendCurrentState241() {
        path241 = pos;
        stackSize241 = stackSize;
        leftTurn241 = leftTurn;
        emptyStackTurns241 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState243() {
        pos = path242;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize242;
        leftTurn = leftTurn242;
        emptyStackTurns = emptyStackTurns242;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 243;
    }

    void appendCurrentState242() {
        path242 = pos;
        stackSize242 = stackSize;
        leftTurn242 = leftTurn;
        emptyStackTurns242 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState244() {
        pos = path243;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize243;
        leftTurn = leftTurn243;
        emptyStackTurns = emptyStackTurns243;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 244;
    }

    void appendCurrentState243() {
        path243 = pos;
        stackSize243 = stackSize;
        leftTurn243 = leftTurn;
        emptyStackTurns243 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState245() {
        pos = path244;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize244;
        leftTurn = leftTurn244;
        emptyStackTurns = emptyStackTurns244;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 245;
    }

    void appendCurrentState244() {
        path244 = pos;
        stackSize244 = stackSize;
        leftTurn244 = leftTurn;
        emptyStackTurns244 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState246() {
        pos = path245;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize245;
        leftTurn = leftTurn245;
        emptyStackTurns = emptyStackTurns245;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 246;
    }

    void appendCurrentState245() {
        path245 = pos;
        stackSize245 = stackSize;
        leftTurn245 = leftTurn;
        emptyStackTurns245 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState247() {
        pos = path246;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize246;
        leftTurn = leftTurn246;
        emptyStackTurns = emptyStackTurns246;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 247;
    }

    void appendCurrentState246() {
        path246 = pos;
        stackSize246 = stackSize;
        leftTurn246 = leftTurn;
        emptyStackTurns246 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState248() {
        pos = path247;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize247;
        leftTurn = leftTurn247;
        emptyStackTurns = emptyStackTurns247;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 248;
    }

    void appendCurrentState247() {
        path247 = pos;
        stackSize247 = stackSize;
        leftTurn247 = leftTurn;
        emptyStackTurns247 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState249() {
        pos = path248;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize248;
        leftTurn = leftTurn248;
        emptyStackTurns = emptyStackTurns248;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 249;
    }

    void appendCurrentState248() {
        path248 = pos;
        stackSize248 = stackSize;
        leftTurn248 = leftTurn;
        emptyStackTurns248 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState250() {
        pos = path249;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize249;
        leftTurn = leftTurn249;
        emptyStackTurns = emptyStackTurns249;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 250;
    }

    void appendCurrentState249() {
        path249 = pos;
        stackSize249 = stackSize;
        leftTurn249 = leftTurn;
        emptyStackTurns249 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState251() {
        pos = path250;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize250;
        leftTurn = leftTurn250;
        emptyStackTurns = emptyStackTurns250;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 251;
    }

    void appendCurrentState250() {
        path250 = pos;
        stackSize250 = stackSize;
        leftTurn250 = leftTurn;
        emptyStackTurns250 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState252() {
        pos = path251;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize251;
        leftTurn = leftTurn251;
        emptyStackTurns = emptyStackTurns251;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 252;
    }

    void appendCurrentState251() {
        path251 = pos;
        stackSize251 = stackSize;
        leftTurn251 = leftTurn;
        emptyStackTurns251 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState253() {
        pos = path252;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize252;
        leftTurn = leftTurn252;
        emptyStackTurns = emptyStackTurns252;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 253;
    }

    void appendCurrentState252() {
        path252 = pos;
        stackSize252 = stackSize;
        leftTurn252 = leftTurn;
        emptyStackTurns252 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState254() {
        pos = path253;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize253;
        leftTurn = leftTurn253;
        emptyStackTurns = emptyStackTurns253;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 254;
    }

    void appendCurrentState253() {
        path253 = pos;
        stackSize253 = stackSize;
        leftTurn253 = leftTurn;
        emptyStackTurns253 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState255() {
        pos = path254;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize254;
        leftTurn = leftTurn254;
        emptyStackTurns = emptyStackTurns254;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 255;
    }

    void appendCurrentState254() {
        path254 = pos;
        stackSize254 = stackSize;
        leftTurn254 = leftTurn;
        emptyStackTurns254 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState256() {
        pos = path255;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize255;
        leftTurn = leftTurn255;
        emptyStackTurns = emptyStackTurns255;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 256;
    }

    void appendCurrentState255() {
        path255 = pos;
        stackSize255 = stackSize;
        leftTurn255 = leftTurn;
        emptyStackTurns255 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState257() {
        pos = path256;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize256;
        leftTurn = leftTurn256;
        emptyStackTurns = emptyStackTurns256;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 257;
    }

    void appendCurrentState256() {
        path256 = pos;
        stackSize256 = stackSize;
        leftTurn256 = leftTurn;
        emptyStackTurns256 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState258() {
        pos = path257;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize257;
        leftTurn = leftTurn257;
        emptyStackTurns = emptyStackTurns257;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 258;
    }

    void appendCurrentState257() {
        path257 = pos;
        stackSize257 = stackSize;
        leftTurn257 = leftTurn;
        emptyStackTurns257 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState259() {
        pos = path258;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize258;
        leftTurn = leftTurn258;
        emptyStackTurns = emptyStackTurns258;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 259;
    }

    void appendCurrentState258() {
        path258 = pos;
        stackSize258 = stackSize;
        leftTurn258 = leftTurn;
        emptyStackTurns258 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState260() {
        pos = path259;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize259;
        leftTurn = leftTurn259;
        emptyStackTurns = emptyStackTurns259;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 260;
    }

    void appendCurrentState259() {
        path259 = pos;
        stackSize259 = stackSize;
        leftTurn259 = leftTurn;
        emptyStackTurns259 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState261() {
        pos = path260;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize260;
        leftTurn = leftTurn260;
        emptyStackTurns = emptyStackTurns260;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 261;
    }

    void appendCurrentState260() {
        path260 = pos;
        stackSize260 = stackSize;
        leftTurn260 = leftTurn;
        emptyStackTurns260 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState262() {
        pos = path261;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize261;
        leftTurn = leftTurn261;
        emptyStackTurns = emptyStackTurns261;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 262;
    }

    void appendCurrentState261() {
        path261 = pos;
        stackSize261 = stackSize;
        leftTurn261 = leftTurn;
        emptyStackTurns261 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState263() {
        pos = path262;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize262;
        leftTurn = leftTurn262;
        emptyStackTurns = emptyStackTurns262;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 263;
    }

    void appendCurrentState262() {
        path262 = pos;
        stackSize262 = stackSize;
        leftTurn262 = leftTurn;
        emptyStackTurns262 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState264() {
        pos = path263;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize263;
        leftTurn = leftTurn263;
        emptyStackTurns = emptyStackTurns263;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 264;
    }

    void appendCurrentState263() {
        path263 = pos;
        stackSize263 = stackSize;
        leftTurn263 = leftTurn;
        emptyStackTurns263 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState265() {
        pos = path264;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize264;
        leftTurn = leftTurn264;
        emptyStackTurns = emptyStackTurns264;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 265;
    }

    void appendCurrentState264() {
        path264 = pos;
        stackSize264 = stackSize;
        leftTurn264 = leftTurn;
        emptyStackTurns264 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState266() {
        pos = path265;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize265;
        leftTurn = leftTurn265;
        emptyStackTurns = emptyStackTurns265;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 266;
    }

    void appendCurrentState265() {
        path265 = pos;
        stackSize265 = stackSize;
        leftTurn265 = leftTurn;
        emptyStackTurns265 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState267() {
        pos = path266;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize266;
        leftTurn = leftTurn266;
        emptyStackTurns = emptyStackTurns266;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 267;
    }

    void appendCurrentState266() {
        path266 = pos;
        stackSize266 = stackSize;
        leftTurn266 = leftTurn;
        emptyStackTurns266 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState268() {
        pos = path267;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize267;
        leftTurn = leftTurn267;
        emptyStackTurns = emptyStackTurns267;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 268;
    }

    void appendCurrentState267() {
        path267 = pos;
        stackSize267 = stackSize;
        leftTurn267 = leftTurn;
        emptyStackTurns267 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState269() {
        pos = path268;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize268;
        leftTurn = leftTurn268;
        emptyStackTurns = emptyStackTurns268;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 269;
    }

    void appendCurrentState268() {
        path268 = pos;
        stackSize268 = stackSize;
        leftTurn268 = leftTurn;
        emptyStackTurns268 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState270() {
        pos = path269;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize269;
        leftTurn = leftTurn269;
        emptyStackTurns = emptyStackTurns269;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 270;
    }

    void appendCurrentState269() {
        path269 = pos;
        stackSize269 = stackSize;
        leftTurn269 = leftTurn;
        emptyStackTurns269 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState271() {
        pos = path270;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize270;
        leftTurn = leftTurn270;
        emptyStackTurns = emptyStackTurns270;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 271;
    }

    void appendCurrentState270() {
        path270 = pos;
        stackSize270 = stackSize;
        leftTurn270 = leftTurn;
        emptyStackTurns270 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState272() {
        pos = path271;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize271;
        leftTurn = leftTurn271;
        emptyStackTurns = emptyStackTurns271;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 272;
    }

    void appendCurrentState271() {
        path271 = pos;
        stackSize271 = stackSize;
        leftTurn271 = leftTurn;
        emptyStackTurns271 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState273() {
        pos = path272;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize272;
        leftTurn = leftTurn272;
        emptyStackTurns = emptyStackTurns272;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 273;
    }

    void appendCurrentState272() {
        path272 = pos;
        stackSize272 = stackSize;
        leftTurn272 = leftTurn;
        emptyStackTurns272 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState274() {
        pos = path273;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize273;
        leftTurn = leftTurn273;
        emptyStackTurns = emptyStackTurns273;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 274;
    }

    void appendCurrentState273() {
        path273 = pos;
        stackSize273 = stackSize;
        leftTurn273 = leftTurn;
        emptyStackTurns273 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState275() {
        pos = path274;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize274;
        leftTurn = leftTurn274;
        emptyStackTurns = emptyStackTurns274;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 275;
    }

    void appendCurrentState274() {
        path274 = pos;
        stackSize274 = stackSize;
        leftTurn274 = leftTurn;
        emptyStackTurns274 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState276() {
        pos = path275;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize275;
        leftTurn = leftTurn275;
        emptyStackTurns = emptyStackTurns275;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 276;
    }

    void appendCurrentState275() {
        path275 = pos;
        stackSize275 = stackSize;
        leftTurn275 = leftTurn;
        emptyStackTurns275 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState277() {
        pos = path276;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize276;
        leftTurn = leftTurn276;
        emptyStackTurns = emptyStackTurns276;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 277;
    }

    void appendCurrentState276() {
        path276 = pos;
        stackSize276 = stackSize;
        leftTurn276 = leftTurn;
        emptyStackTurns276 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState278() {
        pos = path277;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize277;
        leftTurn = leftTurn277;
        emptyStackTurns = emptyStackTurns277;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 278;
    }

    void appendCurrentState277() {
        path277 = pos;
        stackSize277 = stackSize;
        leftTurn277 = leftTurn;
        emptyStackTurns277 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState279() {
        pos = path278;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize278;
        leftTurn = leftTurn278;
        emptyStackTurns = emptyStackTurns278;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 279;
    }

    void appendCurrentState278() {
        path278 = pos;
        stackSize278 = stackSize;
        leftTurn278 = leftTurn;
        emptyStackTurns278 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState280() {
        pos = path279;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize279;
        leftTurn = leftTurn279;
        emptyStackTurns = emptyStackTurns279;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 280;
    }

    void appendCurrentState279() {
        path279 = pos;
        stackSize279 = stackSize;
        leftTurn279 = leftTurn;
        emptyStackTurns279 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState281() {
        pos = path280;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize280;
        leftTurn = leftTurn280;
        emptyStackTurns = emptyStackTurns280;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 281;
    }

    void appendCurrentState280() {
        path280 = pos;
        stackSize280 = stackSize;
        leftTurn280 = leftTurn;
        emptyStackTurns280 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState282() {
        pos = path281;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize281;
        leftTurn = leftTurn281;
        emptyStackTurns = emptyStackTurns281;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 282;
    }

    void appendCurrentState281() {
        path281 = pos;
        stackSize281 = stackSize;
        leftTurn281 = leftTurn;
        emptyStackTurns281 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState283() {
        pos = path282;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize282;
        leftTurn = leftTurn282;
        emptyStackTurns = emptyStackTurns282;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 283;
    }

    void appendCurrentState282() {
        path282 = pos;
        stackSize282 = stackSize;
        leftTurn282 = leftTurn;
        emptyStackTurns282 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState284() {
        pos = path283;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize283;
        leftTurn = leftTurn283;
        emptyStackTurns = emptyStackTurns283;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 284;
    }

    void appendCurrentState283() {
        path283 = pos;
        stackSize283 = stackSize;
        leftTurn283 = leftTurn;
        emptyStackTurns283 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState285() {
        pos = path284;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize284;
        leftTurn = leftTurn284;
        emptyStackTurns = emptyStackTurns284;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 285;
    }

    void appendCurrentState284() {
        path284 = pos;
        stackSize284 = stackSize;
        leftTurn284 = leftTurn;
        emptyStackTurns284 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState286() {
        pos = path285;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize285;
        leftTurn = leftTurn285;
        emptyStackTurns = emptyStackTurns285;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 286;
    }

    void appendCurrentState285() {
        path285 = pos;
        stackSize285 = stackSize;
        leftTurn285 = leftTurn;
        emptyStackTurns285 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState287() {
        pos = path286;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize286;
        leftTurn = leftTurn286;
        emptyStackTurns = emptyStackTurns286;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 287;
    }

    void appendCurrentState286() {
        path286 = pos;
        stackSize286 = stackSize;
        leftTurn286 = leftTurn;
        emptyStackTurns286 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState288() {
        pos = path287;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize287;
        leftTurn = leftTurn287;
        emptyStackTurns = emptyStackTurns287;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 288;
    }

    void appendCurrentState287() {
        path287 = pos;
        stackSize287 = stackSize;
        leftTurn287 = leftTurn;
        emptyStackTurns287 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState289() {
        pos = path288;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize288;
        leftTurn = leftTurn288;
        emptyStackTurns = emptyStackTurns288;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 289;
    }

    void appendCurrentState288() {
        path288 = pos;
        stackSize288 = stackSize;
        leftTurn288 = leftTurn;
        emptyStackTurns288 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState290() {
        pos = path289;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize289;
        leftTurn = leftTurn289;
        emptyStackTurns = emptyStackTurns289;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 290;
    }

    void appendCurrentState289() {
        path289 = pos;
        stackSize289 = stackSize;
        leftTurn289 = leftTurn;
        emptyStackTurns289 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState291() {
        pos = path290;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize290;
        leftTurn = leftTurn290;
        emptyStackTurns = emptyStackTurns290;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 291;
    }

    void appendCurrentState290() {
        path290 = pos;
        stackSize290 = stackSize;
        leftTurn290 = leftTurn;
        emptyStackTurns290 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState292() {
        pos = path291;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize291;
        leftTurn = leftTurn291;
        emptyStackTurns = emptyStackTurns291;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 292;
    }

    void appendCurrentState291() {
        path291 = pos;
        stackSize291 = stackSize;
        leftTurn291 = leftTurn;
        emptyStackTurns291 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState293() {
        pos = path292;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize292;
        leftTurn = leftTurn292;
        emptyStackTurns = emptyStackTurns292;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 293;
    }

    void appendCurrentState292() {
        path292 = pos;
        stackSize292 = stackSize;
        leftTurn292 = leftTurn;
        emptyStackTurns292 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState294() {
        pos = path293;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize293;
        leftTurn = leftTurn293;
        emptyStackTurns = emptyStackTurns293;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 294;
    }

    void appendCurrentState293() {
        path293 = pos;
        stackSize293 = stackSize;
        leftTurn293 = leftTurn;
        emptyStackTurns293 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState295() {
        pos = path294;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize294;
        leftTurn = leftTurn294;
        emptyStackTurns = emptyStackTurns294;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 295;
    }

    void appendCurrentState294() {
        path294 = pos;
        stackSize294 = stackSize;
        leftTurn294 = leftTurn;
        emptyStackTurns294 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState296() {
        pos = path295;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize295;
        leftTurn = leftTurn295;
        emptyStackTurns = emptyStackTurns295;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 296;
    }

    void appendCurrentState295() {
        path295 = pos;
        stackSize295 = stackSize;
        leftTurn295 = leftTurn;
        emptyStackTurns295 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState297() {
        pos = path296;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize296;
        leftTurn = leftTurn296;
        emptyStackTurns = emptyStackTurns296;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 297;
    }

    void appendCurrentState296() {
        path296 = pos;
        stackSize296 = stackSize;
        leftTurn296 = leftTurn;
        emptyStackTurns296 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState298() {
        pos = path297;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize297;
        leftTurn = leftTurn297;
        emptyStackTurns = emptyStackTurns297;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 298;
    }

    void appendCurrentState297() {
        path297 = pos;
        stackSize297 = stackSize;
        leftTurn297 = leftTurn;
        emptyStackTurns297 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState299() {
        pos = path298;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize298;
        leftTurn = leftTurn298;
        emptyStackTurns = emptyStackTurns298;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 299;
    }

    void appendCurrentState298() {
        path298 = pos;
        stackSize298 = stackSize;
        leftTurn298 = leftTurn;
        emptyStackTurns298 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState300() {
        pos = path299;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize299;
        leftTurn = leftTurn299;
        emptyStackTurns = emptyStackTurns299;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 300;
    }

    void appendCurrentState299() {
        path299 = pos;
        stackSize299 = stackSize;
        leftTurn299 = leftTurn;
        emptyStackTurns299 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState301() {
        pos = path300;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize300;
        leftTurn = leftTurn300;
        emptyStackTurns = emptyStackTurns300;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 301;
    }

    void appendCurrentState300() {
        path300 = pos;
        stackSize300 = stackSize;
        leftTurn300 = leftTurn;
        emptyStackTurns300 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState302() {
        pos = path301;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize301;
        leftTurn = leftTurn301;
        emptyStackTurns = emptyStackTurns301;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 302;
    }

    void appendCurrentState301() {
        path301 = pos;
        stackSize301 = stackSize;
        leftTurn301 = leftTurn;
        emptyStackTurns301 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState303() {
        pos = path302;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize302;
        leftTurn = leftTurn302;
        emptyStackTurns = emptyStackTurns302;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 303;
    }

    void appendCurrentState302() {
        path302 = pos;
        stackSize302 = stackSize;
        leftTurn302 = leftTurn;
        emptyStackTurns302 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState304() {
        pos = path303;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize303;
        leftTurn = leftTurn303;
        emptyStackTurns = emptyStackTurns303;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 304;
    }

    void appendCurrentState303() {
        path303 = pos;
        stackSize303 = stackSize;
        leftTurn303 = leftTurn;
        emptyStackTurns303 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState305() {
        pos = path304;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize304;
        leftTurn = leftTurn304;
        emptyStackTurns = emptyStackTurns304;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 305;
    }

    void appendCurrentState304() {
        path304 = pos;
        stackSize304 = stackSize;
        leftTurn304 = leftTurn;
        emptyStackTurns304 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState306() {
        pos = path305;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize305;
        leftTurn = leftTurn305;
        emptyStackTurns = emptyStackTurns305;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 306;
    }

    void appendCurrentState305() {
        path305 = pos;
        stackSize305 = stackSize;
        leftTurn305 = leftTurn;
        emptyStackTurns305 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState307() {
        pos = path306;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize306;
        leftTurn = leftTurn306;
        emptyStackTurns = emptyStackTurns306;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 307;
    }

    void appendCurrentState306() {
        path306 = pos;
        stackSize306 = stackSize;
        leftTurn306 = leftTurn;
        emptyStackTurns306 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState308() {
        pos = path307;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize307;
        leftTurn = leftTurn307;
        emptyStackTurns = emptyStackTurns307;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 308;
    }

    void appendCurrentState307() {
        path307 = pos;
        stackSize307 = stackSize;
        leftTurn307 = leftTurn;
        emptyStackTurns307 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState309() {
        pos = path308;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize308;
        leftTurn = leftTurn308;
        emptyStackTurns = emptyStackTurns308;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 309;
    }

    void appendCurrentState308() {
        path308 = pos;
        stackSize308 = stackSize;
        leftTurn308 = leftTurn;
        emptyStackTurns308 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState310() {
        pos = path309;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize309;
        leftTurn = leftTurn309;
        emptyStackTurns = emptyStackTurns309;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 310;
    }

    void appendCurrentState309() {
        path309 = pos;
        stackSize309 = stackSize;
        leftTurn309 = leftTurn;
        emptyStackTurns309 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState311() {
        pos = path310;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize310;
        leftTurn = leftTurn310;
        emptyStackTurns = emptyStackTurns310;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 311;
    }

    void appendCurrentState310() {
        path310 = pos;
        stackSize310 = stackSize;
        leftTurn310 = leftTurn;
        emptyStackTurns310 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState312() {
        pos = path311;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize311;
        leftTurn = leftTurn311;
        emptyStackTurns = emptyStackTurns311;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 312;
    }

    void appendCurrentState311() {
        path311 = pos;
        stackSize311 = stackSize;
        leftTurn311 = leftTurn;
        emptyStackTurns311 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState313() {
        pos = path312;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize312;
        leftTurn = leftTurn312;
        emptyStackTurns = emptyStackTurns312;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 313;
    }

    void appendCurrentState312() {
        path312 = pos;
        stackSize312 = stackSize;
        leftTurn312 = leftTurn;
        emptyStackTurns312 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState314() {
        pos = path313;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize313;
        leftTurn = leftTurn313;
        emptyStackTurns = emptyStackTurns313;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 314;
    }

    void appendCurrentState313() {
        path313 = pos;
        stackSize313 = stackSize;
        leftTurn313 = leftTurn;
        emptyStackTurns313 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState315() {
        pos = path314;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize314;
        leftTurn = leftTurn314;
        emptyStackTurns = emptyStackTurns314;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 315;
    }

    void appendCurrentState314() {
        path314 = pos;
        stackSize314 = stackSize;
        leftTurn314 = leftTurn;
        emptyStackTurns314 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState316() {
        pos = path315;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize315;
        leftTurn = leftTurn315;
        emptyStackTurns = emptyStackTurns315;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 316;
    }

    void appendCurrentState315() {
        path315 = pos;
        stackSize315 = stackSize;
        leftTurn315 = leftTurn;
        emptyStackTurns315 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState317() {
        pos = path316;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize316;
        leftTurn = leftTurn316;
        emptyStackTurns = emptyStackTurns316;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 317;
    }

    void appendCurrentState316() {
        path316 = pos;
        stackSize316 = stackSize;
        leftTurn316 = leftTurn;
        emptyStackTurns316 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState318() {
        pos = path317;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize317;
        leftTurn = leftTurn317;
        emptyStackTurns = emptyStackTurns317;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 318;
    }

    void appendCurrentState317() {
        path317 = pos;
        stackSize317 = stackSize;
        leftTurn317 = leftTurn;
        emptyStackTurns317 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState319() {
        pos = path318;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize318;
        leftTurn = leftTurn318;
        emptyStackTurns = emptyStackTurns318;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 319;
    }

    void appendCurrentState318() {
        path318 = pos;
        stackSize318 = stackSize;
        leftTurn318 = leftTurn;
        emptyStackTurns318 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState320() {
        pos = path319;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize319;
        leftTurn = leftTurn319;
        emptyStackTurns = emptyStackTurns319;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 320;
    }

    void appendCurrentState319() {
        path319 = pos;
        stackSize319 = stackSize;
        leftTurn319 = leftTurn;
        emptyStackTurns319 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState321() {
        pos = path320;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize320;
        leftTurn = leftTurn320;
        emptyStackTurns = emptyStackTurns320;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 321;
    }

    void appendCurrentState320() {
        path320 = pos;
        stackSize320 = stackSize;
        leftTurn320 = leftTurn;
        emptyStackTurns320 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState322() {
        pos = path321;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize321;
        leftTurn = leftTurn321;
        emptyStackTurns = emptyStackTurns321;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 322;
    }

    void appendCurrentState321() {
        path321 = pos;
        stackSize321 = stackSize;
        leftTurn321 = leftTurn;
        emptyStackTurns321 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState323() {
        pos = path322;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize322;
        leftTurn = leftTurn322;
        emptyStackTurns = emptyStackTurns322;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 323;
    }

    void appendCurrentState322() {
        path322 = pos;
        stackSize322 = stackSize;
        leftTurn322 = leftTurn;
        emptyStackTurns322 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState324() {
        pos = path323;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize323;
        leftTurn = leftTurn323;
        emptyStackTurns = emptyStackTurns323;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 324;
    }

    void appendCurrentState323() {
        path323 = pos;
        stackSize323 = stackSize;
        leftTurn323 = leftTurn;
        emptyStackTurns323 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState325() {
        pos = path324;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize324;
        leftTurn = leftTurn324;
        emptyStackTurns = emptyStackTurns324;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 325;
    }

    void appendCurrentState324() {
        path324 = pos;
        stackSize324 = stackSize;
        leftTurn324 = leftTurn;
        emptyStackTurns324 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState326() {
        pos = path325;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize325;
        leftTurn = leftTurn325;
        emptyStackTurns = emptyStackTurns325;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 326;
    }

    void appendCurrentState325() {
        path325 = pos;
        stackSize325 = stackSize;
        leftTurn325 = leftTurn;
        emptyStackTurns325 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState327() {
        pos = path326;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize326;
        leftTurn = leftTurn326;
        emptyStackTurns = emptyStackTurns326;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 327;
    }

    void appendCurrentState326() {
        path326 = pos;
        stackSize326 = stackSize;
        leftTurn326 = leftTurn;
        emptyStackTurns326 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState328() {
        pos = path327;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize327;
        leftTurn = leftTurn327;
        emptyStackTurns = emptyStackTurns327;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 328;
    }

    void appendCurrentState327() {
        path327 = pos;
        stackSize327 = stackSize;
        leftTurn327 = leftTurn;
        emptyStackTurns327 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState329() {
        pos = path328;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize328;
        leftTurn = leftTurn328;
        emptyStackTurns = emptyStackTurns328;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 329;
    }

    void appendCurrentState328() {
        path328 = pos;
        stackSize328 = stackSize;
        leftTurn328 = leftTurn;
        emptyStackTurns328 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState330() {
        pos = path329;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize329;
        leftTurn = leftTurn329;
        emptyStackTurns = emptyStackTurns329;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 330;
    }

    void appendCurrentState329() {
        path329 = pos;
        stackSize329 = stackSize;
        leftTurn329 = leftTurn;
        emptyStackTurns329 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState331() {
        pos = path330;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize330;
        leftTurn = leftTurn330;
        emptyStackTurns = emptyStackTurns330;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 331;
    }

    void appendCurrentState330() {
        path330 = pos;
        stackSize330 = stackSize;
        leftTurn330 = leftTurn;
        emptyStackTurns330 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState332() {
        pos = path331;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize331;
        leftTurn = leftTurn331;
        emptyStackTurns = emptyStackTurns331;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 332;
    }

    void appendCurrentState331() {
        path331 = pos;
        stackSize331 = stackSize;
        leftTurn331 = leftTurn;
        emptyStackTurns331 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState333() {
        pos = path332;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize332;
        leftTurn = leftTurn332;
        emptyStackTurns = emptyStackTurns332;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 333;
    }

    void appendCurrentState332() {
        path332 = pos;
        stackSize332 = stackSize;
        leftTurn332 = leftTurn;
        emptyStackTurns332 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState334() {
        pos = path333;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize333;
        leftTurn = leftTurn333;
        emptyStackTurns = emptyStackTurns333;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 334;
    }

    void appendCurrentState333() {
        path333 = pos;
        stackSize333 = stackSize;
        leftTurn333 = leftTurn;
        emptyStackTurns333 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState335() {
        pos = path334;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize334;
        leftTurn = leftTurn334;
        emptyStackTurns = emptyStackTurns334;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 335;
    }

    void appendCurrentState334() {
        path334 = pos;
        stackSize334 = stackSize;
        leftTurn334 = leftTurn;
        emptyStackTurns334 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState336() {
        pos = path335;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize335;
        leftTurn = leftTurn335;
        emptyStackTurns = emptyStackTurns335;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 336;
    }

    void appendCurrentState335() {
        path335 = pos;
        stackSize335 = stackSize;
        leftTurn335 = leftTurn;
        emptyStackTurns335 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState337() {
        pos = path336;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize336;
        leftTurn = leftTurn336;
        emptyStackTurns = emptyStackTurns336;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 337;
    }

    void appendCurrentState336() {
        path336 = pos;
        stackSize336 = stackSize;
        leftTurn336 = leftTurn;
        emptyStackTurns336 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState338() {
        pos = path337;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize337;
        leftTurn = leftTurn337;
        emptyStackTurns = emptyStackTurns337;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 338;
    }

    void appendCurrentState337() {
        path337 = pos;
        stackSize337 = stackSize;
        leftTurn337 = leftTurn;
        emptyStackTurns337 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState339() {
        pos = path338;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize338;
        leftTurn = leftTurn338;
        emptyStackTurns = emptyStackTurns338;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 339;
    }

    void appendCurrentState338() {
        path338 = pos;
        stackSize338 = stackSize;
        leftTurn338 = leftTurn;
        emptyStackTurns338 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState340() {
        pos = path339;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize339;
        leftTurn = leftTurn339;
        emptyStackTurns = emptyStackTurns339;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 340;
    }

    void appendCurrentState339() {
        path339 = pos;
        stackSize339 = stackSize;
        leftTurn339 = leftTurn;
        emptyStackTurns339 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState341() {
        pos = path340;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize340;
        leftTurn = leftTurn340;
        emptyStackTurns = emptyStackTurns340;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 341;
    }

    void appendCurrentState340() {
        path340 = pos;
        stackSize340 = stackSize;
        leftTurn340 = leftTurn;
        emptyStackTurns340 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState342() {
        pos = path341;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize341;
        leftTurn = leftTurn341;
        emptyStackTurns = emptyStackTurns341;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 342;
    }

    void appendCurrentState341() {
        path341 = pos;
        stackSize341 = stackSize;
        leftTurn341 = leftTurn;
        emptyStackTurns341 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState343() {
        pos = path342;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize342;
        leftTurn = leftTurn342;
        emptyStackTurns = emptyStackTurns342;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 343;
    }

    void appendCurrentState342() {
        path342 = pos;
        stackSize342 = stackSize;
        leftTurn342 = leftTurn;
        emptyStackTurns342 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState344() {
        pos = path343;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize343;
        leftTurn = leftTurn343;
        emptyStackTurns = emptyStackTurns343;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 344;
    }

    void appendCurrentState343() {
        path343 = pos;
        stackSize343 = stackSize;
        leftTurn343 = leftTurn;
        emptyStackTurns343 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState345() {
        pos = path344;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize344;
        leftTurn = leftTurn344;
        emptyStackTurns = emptyStackTurns344;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 345;
    }

    void appendCurrentState344() {
        path344 = pos;
        stackSize344 = stackSize;
        leftTurn344 = leftTurn;
        emptyStackTurns344 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState346() {
        pos = path345;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize345;
        leftTurn = leftTurn345;
        emptyStackTurns = emptyStackTurns345;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 346;
    }

    void appendCurrentState345() {
        path345 = pos;
        stackSize345 = stackSize;
        leftTurn345 = leftTurn;
        emptyStackTurns345 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState347() {
        pos = path346;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize346;
        leftTurn = leftTurn346;
        emptyStackTurns = emptyStackTurns346;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 347;
    }

    void appendCurrentState346() {
        path346 = pos;
        stackSize346 = stackSize;
        leftTurn346 = leftTurn;
        emptyStackTurns346 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState348() {
        pos = path347;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize347;
        leftTurn = leftTurn347;
        emptyStackTurns = emptyStackTurns347;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 348;
    }

    void appendCurrentState347() {
        path347 = pos;
        stackSize347 = stackSize;
        leftTurn347 = leftTurn;
        emptyStackTurns347 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState349() {
        pos = path348;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize348;
        leftTurn = leftTurn348;
        emptyStackTurns = emptyStackTurns348;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 349;
    }

    void appendCurrentState348() {
        path348 = pos;
        stackSize348 = stackSize;
        leftTurn348 = leftTurn;
        emptyStackTurns348 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState350() {
        pos = path349;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize349;
        leftTurn = leftTurn349;
        emptyStackTurns = emptyStackTurns349;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 350;
    }

    void appendCurrentState349() {
        path349 = pos;
        stackSize349 = stackSize;
        leftTurn349 = leftTurn;
        emptyStackTurns349 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState351() {
        pos = path350;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize350;
        leftTurn = leftTurn350;
        emptyStackTurns = emptyStackTurns350;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 351;
    }

    void appendCurrentState350() {
        path350 = pos;
        stackSize350 = stackSize;
        leftTurn350 = leftTurn;
        emptyStackTurns350 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState352() {
        pos = path351;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize351;
        leftTurn = leftTurn351;
        emptyStackTurns = emptyStackTurns351;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 352;
    }

    void appendCurrentState351() {
        path351 = pos;
        stackSize351 = stackSize;
        leftTurn351 = leftTurn;
        emptyStackTurns351 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState353() {
        pos = path352;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize352;
        leftTurn = leftTurn352;
        emptyStackTurns = emptyStackTurns352;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 353;
    }

    void appendCurrentState352() {
        path352 = pos;
        stackSize352 = stackSize;
        leftTurn352 = leftTurn;
        emptyStackTurns352 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState354() {
        pos = path353;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize353;
        leftTurn = leftTurn353;
        emptyStackTurns = emptyStackTurns353;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 354;
    }

    void appendCurrentState353() {
        path353 = pos;
        stackSize353 = stackSize;
        leftTurn353 = leftTurn;
        emptyStackTurns353 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState355() {
        pos = path354;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize354;
        leftTurn = leftTurn354;
        emptyStackTurns = emptyStackTurns354;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 355;
    }

    void appendCurrentState354() {
        path354 = pos;
        stackSize354 = stackSize;
        leftTurn354 = leftTurn;
        emptyStackTurns354 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState356() {
        pos = path355;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize355;
        leftTurn = leftTurn355;
        emptyStackTurns = emptyStackTurns355;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 356;
    }

    void appendCurrentState355() {
        path355 = pos;
        stackSize355 = stackSize;
        leftTurn355 = leftTurn;
        emptyStackTurns355 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState357() {
        pos = path356;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize356;
        leftTurn = leftTurn356;
        emptyStackTurns = emptyStackTurns356;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 357;
    }

    void appendCurrentState356() {
        path356 = pos;
        stackSize356 = stackSize;
        leftTurn356 = leftTurn;
        emptyStackTurns356 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState358() {
        pos = path357;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize357;
        leftTurn = leftTurn357;
        emptyStackTurns = emptyStackTurns357;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 358;
    }

    void appendCurrentState357() {
        path357 = pos;
        stackSize357 = stackSize;
        leftTurn357 = leftTurn;
        emptyStackTurns357 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState359() {
        pos = path358;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize358;
        leftTurn = leftTurn358;
        emptyStackTurns = emptyStackTurns358;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 359;
    }

    void appendCurrentState358() {
        path358 = pos;
        stackSize358 = stackSize;
        leftTurn358 = leftTurn;
        emptyStackTurns358 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState360() {
        pos = path359;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize359;
        leftTurn = leftTurn359;
        emptyStackTurns = emptyStackTurns359;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 360;
    }

    void appendCurrentState359() {
        path359 = pos;
        stackSize359 = stackSize;
        leftTurn359 = leftTurn;
        emptyStackTurns359 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState361() {
        pos = path360;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize360;
        leftTurn = leftTurn360;
        emptyStackTurns = emptyStackTurns360;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 361;
    }

    void appendCurrentState360() {
        path360 = pos;
        stackSize360 = stackSize;
        leftTurn360 = leftTurn;
        emptyStackTurns360 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState362() {
        pos = path361;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize361;
        leftTurn = leftTurn361;
        emptyStackTurns = emptyStackTurns361;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 362;
    }

    void appendCurrentState361() {
        path361 = pos;
        stackSize361 = stackSize;
        leftTurn361 = leftTurn;
        emptyStackTurns361 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState363() {
        pos = path362;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize362;
        leftTurn = leftTurn362;
        emptyStackTurns = emptyStackTurns362;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 363;
    }

    void appendCurrentState362() {
        path362 = pos;
        stackSize362 = stackSize;
        leftTurn362 = leftTurn;
        emptyStackTurns362 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState364() {
        pos = path363;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize363;
        leftTurn = leftTurn363;
        emptyStackTurns = emptyStackTurns363;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 364;
    }

    void appendCurrentState363() {
        path363 = pos;
        stackSize363 = stackSize;
        leftTurn363 = leftTurn;
        emptyStackTurns363 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState365() {
        pos = path364;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize364;
        leftTurn = leftTurn364;
        emptyStackTurns = emptyStackTurns364;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 365;
    }

    void appendCurrentState364() {
        path364 = pos;
        stackSize364 = stackSize;
        leftTurn364 = leftTurn;
        emptyStackTurns364 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState366() {
        pos = path365;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize365;
        leftTurn = leftTurn365;
        emptyStackTurns = emptyStackTurns365;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 366;
    }

    void appendCurrentState365() {
        path365 = pos;
        stackSize365 = stackSize;
        leftTurn365 = leftTurn;
        emptyStackTurns365 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState367() {
        pos = path366;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize366;
        leftTurn = leftTurn366;
        emptyStackTurns = emptyStackTurns366;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 367;
    }

    void appendCurrentState366() {
        path366 = pos;
        stackSize366 = stackSize;
        leftTurn366 = leftTurn;
        emptyStackTurns366 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState368() {
        pos = path367;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize367;
        leftTurn = leftTurn367;
        emptyStackTurns = emptyStackTurns367;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 368;
    }

    void appendCurrentState367() {
        path367 = pos;
        stackSize367 = stackSize;
        leftTurn367 = leftTurn;
        emptyStackTurns367 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState369() {
        pos = path368;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize368;
        leftTurn = leftTurn368;
        emptyStackTurns = emptyStackTurns368;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 369;
    }

    void appendCurrentState368() {
        path368 = pos;
        stackSize368 = stackSize;
        leftTurn368 = leftTurn;
        emptyStackTurns368 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState370() {
        pos = path369;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize369;
        leftTurn = leftTurn369;
        emptyStackTurns = emptyStackTurns369;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 370;
    }

    void appendCurrentState369() {
        path369 = pos;
        stackSize369 = stackSize;
        leftTurn369 = leftTurn;
        emptyStackTurns369 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState371() {
        pos = path370;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize370;
        leftTurn = leftTurn370;
        emptyStackTurns = emptyStackTurns370;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 371;
    }

    void appendCurrentState370() {
        path370 = pos;
        stackSize370 = stackSize;
        leftTurn370 = leftTurn;
        emptyStackTurns370 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState372() {
        pos = path371;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize371;
        leftTurn = leftTurn371;
        emptyStackTurns = emptyStackTurns371;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 372;
    }

    void appendCurrentState371() {
        path371 = pos;
        stackSize371 = stackSize;
        leftTurn371 = leftTurn;
        emptyStackTurns371 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState373() {
        pos = path372;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize372;
        leftTurn = leftTurn372;
        emptyStackTurns = emptyStackTurns372;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 373;
    }

    void appendCurrentState372() {
        path372 = pos;
        stackSize372 = stackSize;
        leftTurn372 = leftTurn;
        emptyStackTurns372 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState374() {
        pos = path373;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize373;
        leftTurn = leftTurn373;
        emptyStackTurns = emptyStackTurns373;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 374;
    }

    void appendCurrentState373() {
        path373 = pos;
        stackSize373 = stackSize;
        leftTurn373 = leftTurn;
        emptyStackTurns373 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState375() {
        pos = path374;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize374;
        leftTurn = leftTurn374;
        emptyStackTurns = emptyStackTurns374;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 375;
    }

    void appendCurrentState374() {
        path374 = pos;
        stackSize374 = stackSize;
        leftTurn374 = leftTurn;
        emptyStackTurns374 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState376() {
        pos = path375;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize375;
        leftTurn = leftTurn375;
        emptyStackTurns = emptyStackTurns375;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 376;
    }

    void appendCurrentState375() {
        path375 = pos;
        stackSize375 = stackSize;
        leftTurn375 = leftTurn;
        emptyStackTurns375 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState377() {
        pos = path376;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize376;
        leftTurn = leftTurn376;
        emptyStackTurns = emptyStackTurns376;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 377;
    }

    void appendCurrentState376() {
        path376 = pos;
        stackSize376 = stackSize;
        leftTurn376 = leftTurn;
        emptyStackTurns376 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState378() {
        pos = path377;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize377;
        leftTurn = leftTurn377;
        emptyStackTurns = emptyStackTurns377;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 378;
    }

    void appendCurrentState377() {
        path377 = pos;
        stackSize377 = stackSize;
        leftTurn377 = leftTurn;
        emptyStackTurns377 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState379() {
        pos = path378;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize378;
        leftTurn = leftTurn378;
        emptyStackTurns = emptyStackTurns378;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 379;
    }

    void appendCurrentState378() {
        path378 = pos;
        stackSize378 = stackSize;
        leftTurn378 = leftTurn;
        emptyStackTurns378 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState380() {
        pos = path379;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize379;
        leftTurn = leftTurn379;
        emptyStackTurns = emptyStackTurns379;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 380;
    }

    void appendCurrentState379() {
        path379 = pos;
        stackSize379 = stackSize;
        leftTurn379 = leftTurn;
        emptyStackTurns379 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState381() {
        pos = path380;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize380;
        leftTurn = leftTurn380;
        emptyStackTurns = emptyStackTurns380;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 381;
    }

    void appendCurrentState380() {
        path380 = pos;
        stackSize380 = stackSize;
        leftTurn380 = leftTurn;
        emptyStackTurns380 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState382() {
        pos = path381;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize381;
        leftTurn = leftTurn381;
        emptyStackTurns = emptyStackTurns381;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 382;
    }

    void appendCurrentState381() {
        path381 = pos;
        stackSize381 = stackSize;
        leftTurn381 = leftTurn;
        emptyStackTurns381 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState383() {
        pos = path382;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize382;
        leftTurn = leftTurn382;
        emptyStackTurns = emptyStackTurns382;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 383;
    }

    void appendCurrentState382() {
        path382 = pos;
        stackSize382 = stackSize;
        leftTurn382 = leftTurn;
        emptyStackTurns382 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState384() {
        pos = path383;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize383;
        leftTurn = leftTurn383;
        emptyStackTurns = emptyStackTurns383;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 384;
    }

    void appendCurrentState383() {
        path383 = pos;
        stackSize383 = stackSize;
        leftTurn383 = leftTurn;
        emptyStackTurns383 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState385() {
        pos = path384;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize384;
        leftTurn = leftTurn384;
        emptyStackTurns = emptyStackTurns384;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 385;
    }

    void appendCurrentState384() {
        path384 = pos;
        stackSize384 = stackSize;
        leftTurn384 = leftTurn;
        emptyStackTurns384 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState386() {
        pos = path385;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize385;
        leftTurn = leftTurn385;
        emptyStackTurns = emptyStackTurns385;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 386;
    }

    void appendCurrentState385() {
        path385 = pos;
        stackSize385 = stackSize;
        leftTurn385 = leftTurn;
        emptyStackTurns385 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState387() {
        pos = path386;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize386;
        leftTurn = leftTurn386;
        emptyStackTurns = emptyStackTurns386;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 387;
    }

    void appendCurrentState386() {
        path386 = pos;
        stackSize386 = stackSize;
        leftTurn386 = leftTurn;
        emptyStackTurns386 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState388() {
        pos = path387;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize387;
        leftTurn = leftTurn387;
        emptyStackTurns = emptyStackTurns387;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 388;
    }

    void appendCurrentState387() {
        path387 = pos;
        stackSize387 = stackSize;
        leftTurn387 = leftTurn;
        emptyStackTurns387 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState389() {
        pos = path388;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize388;
        leftTurn = leftTurn388;
        emptyStackTurns = emptyStackTurns388;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 389;
    }

    void appendCurrentState388() {
        path388 = pos;
        stackSize388 = stackSize;
        leftTurn388 = leftTurn;
        emptyStackTurns388 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState390() {
        pos = path389;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize389;
        leftTurn = leftTurn389;
        emptyStackTurns = emptyStackTurns389;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 390;
    }

    void appendCurrentState389() {
        path389 = pos;
        stackSize389 = stackSize;
        leftTurn389 = leftTurn;
        emptyStackTurns389 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState391() {
        pos = path390;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize390;
        leftTurn = leftTurn390;
        emptyStackTurns = emptyStackTurns390;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 391;
    }

    void appendCurrentState390() {
        path390 = pos;
        stackSize390 = stackSize;
        leftTurn390 = leftTurn;
        emptyStackTurns390 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState392() {
        pos = path391;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize391;
        leftTurn = leftTurn391;
        emptyStackTurns = emptyStackTurns391;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 392;
    }

    void appendCurrentState391() {
        path391 = pos;
        stackSize391 = stackSize;
        leftTurn391 = leftTurn;
        emptyStackTurns391 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState393() {
        pos = path392;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize392;
        leftTurn = leftTurn392;
        emptyStackTurns = emptyStackTurns392;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 393;
    }

    void appendCurrentState392() {
        path392 = pos;
        stackSize392 = stackSize;
        leftTurn392 = leftTurn;
        emptyStackTurns392 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState394() {
        pos = path393;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize393;
        leftTurn = leftTurn393;
        emptyStackTurns = emptyStackTurns393;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 394;
    }

    void appendCurrentState393() {
        path393 = pos;
        stackSize393 = stackSize;
        leftTurn393 = leftTurn;
        emptyStackTurns393 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState395() {
        pos = path394;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize394;
        leftTurn = leftTurn394;
        emptyStackTurns = emptyStackTurns394;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 395;
    }

    void appendCurrentState394() {
        path394 = pos;
        stackSize394 = stackSize;
        leftTurn394 = leftTurn;
        emptyStackTurns394 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState396() {
        pos = path395;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize395;
        leftTurn = leftTurn395;
        emptyStackTurns = emptyStackTurns395;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 396;
    }

    void appendCurrentState395() {
        path395 = pos;
        stackSize395 = stackSize;
        leftTurn395 = leftTurn;
        emptyStackTurns395 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState397() {
        pos = path396;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize396;
        leftTurn = leftTurn396;
        emptyStackTurns = emptyStackTurns396;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 397;
    }

    void appendCurrentState396() {
        path396 = pos;
        stackSize396 = stackSize;
        leftTurn396 = leftTurn;
        emptyStackTurns396 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState398() {
        pos = path397;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize397;
        leftTurn = leftTurn397;
        emptyStackTurns = emptyStackTurns397;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 398;
    }

    void appendCurrentState397() {
        path397 = pos;
        stackSize397 = stackSize;
        leftTurn397 = leftTurn;
        emptyStackTurns397 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState399() {
        pos = path398;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize398;
        leftTurn = leftTurn398;
        emptyStackTurns = emptyStackTurns398;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 399;
    }

    void appendCurrentState398() {
        path398 = pos;
        stackSize398 = stackSize;
        leftTurn398 = leftTurn;
        emptyStackTurns398 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState400() {
        pos = path399;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize399;
        leftTurn = leftTurn399;
        emptyStackTurns = emptyStackTurns399;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 400;
    }

    void appendCurrentState399() {
        path399 = pos;
        stackSize399 = stackSize;
        leftTurn399 = leftTurn;
        emptyStackTurns399 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState401() {
        pos = path400;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize400;
        leftTurn = leftTurn400;
        emptyStackTurns = emptyStackTurns400;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 401;
    }

    void appendCurrentState400() {
        path400 = pos;
        stackSize400 = stackSize;
        leftTurn400 = leftTurn;
        emptyStackTurns400 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState402() {
        pos = path401;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize401;
        leftTurn = leftTurn401;
        emptyStackTurns = emptyStackTurns401;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 402;
    }

    void appendCurrentState401() {
        path401 = pos;
        stackSize401 = stackSize;
        leftTurn401 = leftTurn;
        emptyStackTurns401 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState403() {
        pos = path402;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize402;
        leftTurn = leftTurn402;
        emptyStackTurns = emptyStackTurns402;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 403;
    }

    void appendCurrentState402() {
        path402 = pos;
        stackSize402 = stackSize;
        leftTurn402 = leftTurn;
        emptyStackTurns402 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState404() {
        pos = path403;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize403;
        leftTurn = leftTurn403;
        emptyStackTurns = emptyStackTurns403;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 404;
    }

    void appendCurrentState403() {
        path403 = pos;
        stackSize403 = stackSize;
        leftTurn403 = leftTurn;
        emptyStackTurns403 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState405() {
        pos = path404;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize404;
        leftTurn = leftTurn404;
        emptyStackTurns = emptyStackTurns404;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 405;
    }

    void appendCurrentState404() {
        path404 = pos;
        stackSize404 = stackSize;
        leftTurn404 = leftTurn;
        emptyStackTurns404 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState406() {
        pos = path405;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize405;
        leftTurn = leftTurn405;
        emptyStackTurns = emptyStackTurns405;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 406;
    }

    void appendCurrentState405() {
        path405 = pos;
        stackSize405 = stackSize;
        leftTurn405 = leftTurn;
        emptyStackTurns405 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState407() {
        pos = path406;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize406;
        leftTurn = leftTurn406;
        emptyStackTurns = emptyStackTurns406;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 407;
    }

    void appendCurrentState406() {
        path406 = pos;
        stackSize406 = stackSize;
        leftTurn406 = leftTurn;
        emptyStackTurns406 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState408() {
        pos = path407;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize407;
        leftTurn = leftTurn407;
        emptyStackTurns = emptyStackTurns407;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 408;
    }

    void appendCurrentState407() {
        path407 = pos;
        stackSize407 = stackSize;
        leftTurn407 = leftTurn;
        emptyStackTurns407 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState409() {
        pos = path408;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize408;
        leftTurn = leftTurn408;
        emptyStackTurns = emptyStackTurns408;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 409;
    }

    void appendCurrentState408() {
        path408 = pos;
        stackSize408 = stackSize;
        leftTurn408 = leftTurn;
        emptyStackTurns408 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState410() {
        pos = path409;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize409;
        leftTurn = leftTurn409;
        emptyStackTurns = emptyStackTurns409;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 410;
    }

    void appendCurrentState409() {
        path409 = pos;
        stackSize409 = stackSize;
        leftTurn409 = leftTurn;
        emptyStackTurns409 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState411() {
        pos = path410;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize410;
        leftTurn = leftTurn410;
        emptyStackTurns = emptyStackTurns410;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 411;
    }

    void appendCurrentState410() {
        path410 = pos;
        stackSize410 = stackSize;
        leftTurn410 = leftTurn;
        emptyStackTurns410 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState412() {
        pos = path411;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize411;
        leftTurn = leftTurn411;
        emptyStackTurns = emptyStackTurns411;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 412;
    }

    void appendCurrentState411() {
        path411 = pos;
        stackSize411 = stackSize;
        leftTurn411 = leftTurn;
        emptyStackTurns411 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState413() {
        pos = path412;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize412;
        leftTurn = leftTurn412;
        emptyStackTurns = emptyStackTurns412;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 413;
    }

    void appendCurrentState412() {
        path412 = pos;
        stackSize412 = stackSize;
        leftTurn412 = leftTurn;
        emptyStackTurns412 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState414() {
        pos = path413;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize413;
        leftTurn = leftTurn413;
        emptyStackTurns = emptyStackTurns413;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 414;
    }

    void appendCurrentState413() {
        path413 = pos;
        stackSize413 = stackSize;
        leftTurn413 = leftTurn;
        emptyStackTurns413 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState415() {
        pos = path414;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize414;
        leftTurn = leftTurn414;
        emptyStackTurns = emptyStackTurns414;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 415;
    }

    void appendCurrentState414() {
        path414 = pos;
        stackSize414 = stackSize;
        leftTurn414 = leftTurn;
        emptyStackTurns414 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState416() {
        pos = path415;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize415;
        leftTurn = leftTurn415;
        emptyStackTurns = emptyStackTurns415;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 416;
    }

    void appendCurrentState415() {
        path415 = pos;
        stackSize415 = stackSize;
        leftTurn415 = leftTurn;
        emptyStackTurns415 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState417() {
        pos = path416;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize416;
        leftTurn = leftTurn416;
        emptyStackTurns = emptyStackTurns416;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 417;
    }

    void appendCurrentState416() {
        path416 = pos;
        stackSize416 = stackSize;
        leftTurn416 = leftTurn;
        emptyStackTurns416 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState418() {
        pos = path417;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize417;
        leftTurn = leftTurn417;
        emptyStackTurns = emptyStackTurns417;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 418;
    }

    void appendCurrentState417() {
        path417 = pos;
        stackSize417 = stackSize;
        leftTurn417 = leftTurn;
        emptyStackTurns417 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState419() {
        pos = path418;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize418;
        leftTurn = leftTurn418;
        emptyStackTurns = emptyStackTurns418;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 419;
    }

    void appendCurrentState418() {
        path418 = pos;
        stackSize418 = stackSize;
        leftTurn418 = leftTurn;
        emptyStackTurns418 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState420() {
        pos = path419;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize419;
        leftTurn = leftTurn419;
        emptyStackTurns = emptyStackTurns419;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 420;
    }

    void appendCurrentState419() {
        path419 = pos;
        stackSize419 = stackSize;
        leftTurn419 = leftTurn;
        emptyStackTurns419 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState421() {
        pos = path420;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize420;
        leftTurn = leftTurn420;
        emptyStackTurns = emptyStackTurns420;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 421;
    }

    void appendCurrentState420() {
        path420 = pos;
        stackSize420 = stackSize;
        leftTurn420 = leftTurn;
        emptyStackTurns420 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState422() {
        pos = path421;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize421;
        leftTurn = leftTurn421;
        emptyStackTurns = emptyStackTurns421;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 422;
    }

    void appendCurrentState421() {
        path421 = pos;
        stackSize421 = stackSize;
        leftTurn421 = leftTurn;
        emptyStackTurns421 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState423() {
        pos = path422;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize422;
        leftTurn = leftTurn422;
        emptyStackTurns = emptyStackTurns422;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 423;
    }

    void appendCurrentState422() {
        path422 = pos;
        stackSize422 = stackSize;
        leftTurn422 = leftTurn;
        emptyStackTurns422 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState424() {
        pos = path423;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize423;
        leftTurn = leftTurn423;
        emptyStackTurns = emptyStackTurns423;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 424;
    }

    void appendCurrentState423() {
        path423 = pos;
        stackSize423 = stackSize;
        leftTurn423 = leftTurn;
        emptyStackTurns423 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState425() {
        pos = path424;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize424;
        leftTurn = leftTurn424;
        emptyStackTurns = emptyStackTurns424;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 425;
    }

    void appendCurrentState424() {
        path424 = pos;
        stackSize424 = stackSize;
        leftTurn424 = leftTurn;
        emptyStackTurns424 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState426() {
        pos = path425;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize425;
        leftTurn = leftTurn425;
        emptyStackTurns = emptyStackTurns425;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 426;
    }

    void appendCurrentState425() {
        path425 = pos;
        stackSize425 = stackSize;
        leftTurn425 = leftTurn;
        emptyStackTurns425 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState427() {
        pos = path426;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize426;
        leftTurn = leftTurn426;
        emptyStackTurns = emptyStackTurns426;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 427;
    }

    void appendCurrentState426() {
        path426 = pos;
        stackSize426 = stackSize;
        leftTurn426 = leftTurn;
        emptyStackTurns426 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState428() {
        pos = path427;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize427;
        leftTurn = leftTurn427;
        emptyStackTurns = emptyStackTurns427;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 428;
    }

    void appendCurrentState427() {
        path427 = pos;
        stackSize427 = stackSize;
        leftTurn427 = leftTurn;
        emptyStackTurns427 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState429() {
        pos = path428;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize428;
        leftTurn = leftTurn428;
        emptyStackTurns = emptyStackTurns428;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 429;
    }

    void appendCurrentState428() {
        path428 = pos;
        stackSize428 = stackSize;
        leftTurn428 = leftTurn;
        emptyStackTurns428 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState430() {
        pos = path429;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize429;
        leftTurn = leftTurn429;
        emptyStackTurns = emptyStackTurns429;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 430;
    }

    void appendCurrentState429() {
        path429 = pos;
        stackSize429 = stackSize;
        leftTurn429 = leftTurn;
        emptyStackTurns429 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState431() {
        pos = path430;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize430;
        leftTurn = leftTurn430;
        emptyStackTurns = emptyStackTurns430;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 431;
    }

    void appendCurrentState430() {
        path430 = pos;
        stackSize430 = stackSize;
        leftTurn430 = leftTurn;
        emptyStackTurns430 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState432() {
        pos = path431;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize431;
        leftTurn = leftTurn431;
        emptyStackTurns = emptyStackTurns431;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 432;
    }

    void appendCurrentState431() {
        path431 = pos;
        stackSize431 = stackSize;
        leftTurn431 = leftTurn;
        emptyStackTurns431 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState433() {
        pos = path432;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize432;
        leftTurn = leftTurn432;
        emptyStackTurns = emptyStackTurns432;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 433;
    }

    void appendCurrentState432() {
        path432 = pos;
        stackSize432 = stackSize;
        leftTurn432 = leftTurn;
        emptyStackTurns432 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState434() {
        pos = path433;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize433;
        leftTurn = leftTurn433;
        emptyStackTurns = emptyStackTurns433;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 434;
    }

    void appendCurrentState433() {
        path433 = pos;
        stackSize433 = stackSize;
        leftTurn433 = leftTurn;
        emptyStackTurns433 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState435() {
        pos = path434;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize434;
        leftTurn = leftTurn434;
        emptyStackTurns = emptyStackTurns434;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 435;
    }

    void appendCurrentState434() {
        path434 = pos;
        stackSize434 = stackSize;
        leftTurn434 = leftTurn;
        emptyStackTurns434 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState436() {
        pos = path435;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize435;
        leftTurn = leftTurn435;
        emptyStackTurns = emptyStackTurns435;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 436;
    }

    void appendCurrentState435() {
        path435 = pos;
        stackSize435 = stackSize;
        leftTurn435 = leftTurn;
        emptyStackTurns435 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState437() {
        pos = path436;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize436;
        leftTurn = leftTurn436;
        emptyStackTurns = emptyStackTurns436;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 437;
    }

    void appendCurrentState436() {
        path436 = pos;
        stackSize436 = stackSize;
        leftTurn436 = leftTurn;
        emptyStackTurns436 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState438() {
        pos = path437;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize437;
        leftTurn = leftTurn437;
        emptyStackTurns = emptyStackTurns437;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 438;
    }

    void appendCurrentState437() {
        path437 = pos;
        stackSize437 = stackSize;
        leftTurn437 = leftTurn;
        emptyStackTurns437 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState439() {
        pos = path438;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize438;
        leftTurn = leftTurn438;
        emptyStackTurns = emptyStackTurns438;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 439;
    }

    void appendCurrentState438() {
        path438 = pos;
        stackSize438 = stackSize;
        leftTurn438 = leftTurn;
        emptyStackTurns438 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState440() {
        pos = path439;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize439;
        leftTurn = leftTurn439;
        emptyStackTurns = emptyStackTurns439;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 440;
    }

    void appendCurrentState439() {
        path439 = pos;
        stackSize439 = stackSize;
        leftTurn439 = leftTurn;
        emptyStackTurns439 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState441() {
        pos = path440;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize440;
        leftTurn = leftTurn440;
        emptyStackTurns = emptyStackTurns440;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 441;
    }

    void appendCurrentState440() {
        path440 = pos;
        stackSize440 = stackSize;
        leftTurn440 = leftTurn;
        emptyStackTurns440 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState442() {
        pos = path441;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize441;
        leftTurn = leftTurn441;
        emptyStackTurns = emptyStackTurns441;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 442;
    }

    void appendCurrentState441() {
        path441 = pos;
        stackSize441 = stackSize;
        leftTurn441 = leftTurn;
        emptyStackTurns441 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState443() {
        pos = path442;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize442;
        leftTurn = leftTurn442;
        emptyStackTurns = emptyStackTurns442;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 443;
    }

    void appendCurrentState442() {
        path442 = pos;
        stackSize442 = stackSize;
        leftTurn442 = leftTurn;
        emptyStackTurns442 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState444() {
        pos = path443;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize443;
        leftTurn = leftTurn443;
        emptyStackTurns = emptyStackTurns443;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 444;
    }

    void appendCurrentState443() {
        path443 = pos;
        stackSize443 = stackSize;
        leftTurn443 = leftTurn;
        emptyStackTurns443 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState445() {
        pos = path444;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize444;
        leftTurn = leftTurn444;
        emptyStackTurns = emptyStackTurns444;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 445;
    }

    void appendCurrentState444() {
        path444 = pos;
        stackSize444 = stackSize;
        leftTurn444 = leftTurn;
        emptyStackTurns444 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState446() {
        pos = path445;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize445;
        leftTurn = leftTurn445;
        emptyStackTurns = emptyStackTurns445;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 446;
    }

    void appendCurrentState445() {
        path445 = pos;
        stackSize445 = stackSize;
        leftTurn445 = leftTurn;
        emptyStackTurns445 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState447() {
        pos = path446;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize446;
        leftTurn = leftTurn446;
        emptyStackTurns = emptyStackTurns446;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 447;
    }

    void appendCurrentState446() {
        path446 = pos;
        stackSize446 = stackSize;
        leftTurn446 = leftTurn;
        emptyStackTurns446 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState448() {
        pos = path447;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize447;
        leftTurn = leftTurn447;
        emptyStackTurns = emptyStackTurns447;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 448;
    }

    void appendCurrentState447() {
        path447 = pos;
        stackSize447 = stackSize;
        leftTurn447 = leftTurn;
        emptyStackTurns447 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState449() {
        pos = path448;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize448;
        leftTurn = leftTurn448;
        emptyStackTurns = emptyStackTurns448;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 449;
    }

    void appendCurrentState448() {
        path448 = pos;
        stackSize448 = stackSize;
        leftTurn448 = leftTurn;
        emptyStackTurns448 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState450() {
        pos = path449;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize449;
        leftTurn = leftTurn449;
        emptyStackTurns = emptyStackTurns449;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 450;
    }

    void appendCurrentState449() {
        path449 = pos;
        stackSize449 = stackSize;
        leftTurn449 = leftTurn;
        emptyStackTurns449 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState451() {
        pos = path450;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize450;
        leftTurn = leftTurn450;
        emptyStackTurns = emptyStackTurns450;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 451;
    }

    void appendCurrentState450() {
        path450 = pos;
        stackSize450 = stackSize;
        leftTurn450 = leftTurn;
        emptyStackTurns450 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState452() {
        pos = path451;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize451;
        leftTurn = leftTurn451;
        emptyStackTurns = emptyStackTurns451;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 452;
    }

    void appendCurrentState451() {
        path451 = pos;
        stackSize451 = stackSize;
        leftTurn451 = leftTurn;
        emptyStackTurns451 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState453() {
        pos = path452;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize452;
        leftTurn = leftTurn452;
        emptyStackTurns = emptyStackTurns452;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 453;
    }

    void appendCurrentState452() {
        path452 = pos;
        stackSize452 = stackSize;
        leftTurn452 = leftTurn;
        emptyStackTurns452 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState454() {
        pos = path453;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize453;
        leftTurn = leftTurn453;
        emptyStackTurns = emptyStackTurns453;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 454;
    }

    void appendCurrentState453() {
        path453 = pos;
        stackSize453 = stackSize;
        leftTurn453 = leftTurn;
        emptyStackTurns453 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState455() {
        pos = path454;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize454;
        leftTurn = leftTurn454;
        emptyStackTurns = emptyStackTurns454;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 455;
    }

    void appendCurrentState454() {
        path454 = pos;
        stackSize454 = stackSize;
        leftTurn454 = leftTurn;
        emptyStackTurns454 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState456() {
        pos = path455;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize455;
        leftTurn = leftTurn455;
        emptyStackTurns = emptyStackTurns455;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 456;
    }

    void appendCurrentState455() {
        path455 = pos;
        stackSize455 = stackSize;
        leftTurn455 = leftTurn;
        emptyStackTurns455 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState457() {
        pos = path456;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize456;
        leftTurn = leftTurn456;
        emptyStackTurns = emptyStackTurns456;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 457;
    }

    void appendCurrentState456() {
        path456 = pos;
        stackSize456 = stackSize;
        leftTurn456 = leftTurn;
        emptyStackTurns456 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState458() {
        pos = path457;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize457;
        leftTurn = leftTurn457;
        emptyStackTurns = emptyStackTurns457;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 458;
    }

    void appendCurrentState457() {
        path457 = pos;
        stackSize457 = stackSize;
        leftTurn457 = leftTurn;
        emptyStackTurns457 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState459() {
        pos = path458;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize458;
        leftTurn = leftTurn458;
        emptyStackTurns = emptyStackTurns458;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 459;
    }

    void appendCurrentState458() {
        path458 = pos;
        stackSize458 = stackSize;
        leftTurn458 = leftTurn;
        emptyStackTurns458 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState460() {
        pos = path459;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize459;
        leftTurn = leftTurn459;
        emptyStackTurns = emptyStackTurns459;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 460;
    }

    void appendCurrentState459() {
        path459 = pos;
        stackSize459 = stackSize;
        leftTurn459 = leftTurn;
        emptyStackTurns459 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState461() {
        pos = path460;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize460;
        leftTurn = leftTurn460;
        emptyStackTurns = emptyStackTurns460;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 461;
    }

    void appendCurrentState460() {
        path460 = pos;
        stackSize460 = stackSize;
        leftTurn460 = leftTurn;
        emptyStackTurns460 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState462() {
        pos = path461;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize461;
        leftTurn = leftTurn461;
        emptyStackTurns = emptyStackTurns461;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 462;
    }

    void appendCurrentState461() {
        path461 = pos;
        stackSize461 = stackSize;
        leftTurn461 = leftTurn;
        emptyStackTurns461 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState463() {
        pos = path462;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize462;
        leftTurn = leftTurn462;
        emptyStackTurns = emptyStackTurns462;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 463;
    }

    void appendCurrentState462() {
        path462 = pos;
        stackSize462 = stackSize;
        leftTurn462 = leftTurn;
        emptyStackTurns462 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState464() {
        pos = path463;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize463;
        leftTurn = leftTurn463;
        emptyStackTurns = emptyStackTurns463;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 464;
    }

    void appendCurrentState463() {
        path463 = pos;
        stackSize463 = stackSize;
        leftTurn463 = leftTurn;
        emptyStackTurns463 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState465() {
        pos = path464;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize464;
        leftTurn = leftTurn464;
        emptyStackTurns = emptyStackTurns464;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 465;
    }

    void appendCurrentState464() {
        path464 = pos;
        stackSize464 = stackSize;
        leftTurn464 = leftTurn;
        emptyStackTurns464 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState466() {
        pos = path465;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize465;
        leftTurn = leftTurn465;
        emptyStackTurns = emptyStackTurns465;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 466;
    }

    void appendCurrentState465() {
        path465 = pos;
        stackSize465 = stackSize;
        leftTurn465 = leftTurn;
        emptyStackTurns465 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState467() {
        pos = path466;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize466;
        leftTurn = leftTurn466;
        emptyStackTurns = emptyStackTurns466;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 467;
    }

    void appendCurrentState466() {
        path466 = pos;
        stackSize466 = stackSize;
        leftTurn466 = leftTurn;
        emptyStackTurns466 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState468() {
        pos = path467;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize467;
        leftTurn = leftTurn467;
        emptyStackTurns = emptyStackTurns467;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 468;
    }

    void appendCurrentState467() {
        path467 = pos;
        stackSize467 = stackSize;
        leftTurn467 = leftTurn;
        emptyStackTurns467 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState469() {
        pos = path468;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize468;
        leftTurn = leftTurn468;
        emptyStackTurns = emptyStackTurns468;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 469;
    }

    void appendCurrentState468() {
        path468 = pos;
        stackSize468 = stackSize;
        leftTurn468 = leftTurn;
        emptyStackTurns468 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState470() {
        pos = path469;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize469;
        leftTurn = leftTurn469;
        emptyStackTurns = emptyStackTurns469;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 470;
    }

    void appendCurrentState469() {
        path469 = pos;
        stackSize469 = stackSize;
        leftTurn469 = leftTurn;
        emptyStackTurns469 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState471() {
        pos = path470;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize470;
        leftTurn = leftTurn470;
        emptyStackTurns = emptyStackTurns470;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 471;
    }

    void appendCurrentState470() {
        path470 = pos;
        stackSize470 = stackSize;
        leftTurn470 = leftTurn;
        emptyStackTurns470 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState472() {
        pos = path471;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize471;
        leftTurn = leftTurn471;
        emptyStackTurns = emptyStackTurns471;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 472;
    }

    void appendCurrentState471() {
        path471 = pos;
        stackSize471 = stackSize;
        leftTurn471 = leftTurn;
        emptyStackTurns471 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState473() {
        pos = path472;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize472;
        leftTurn = leftTurn472;
        emptyStackTurns = emptyStackTurns472;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 473;
    }

    void appendCurrentState472() {
        path472 = pos;
        stackSize472 = stackSize;
        leftTurn472 = leftTurn;
        emptyStackTurns472 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState474() {
        pos = path473;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize473;
        leftTurn = leftTurn473;
        emptyStackTurns = emptyStackTurns473;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 474;
    }

    void appendCurrentState473() {
        path473 = pos;
        stackSize473 = stackSize;
        leftTurn473 = leftTurn;
        emptyStackTurns473 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState475() {
        pos = path474;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize474;
        leftTurn = leftTurn474;
        emptyStackTurns = emptyStackTurns474;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 475;
    }

    void appendCurrentState474() {
        path474 = pos;
        stackSize474 = stackSize;
        leftTurn474 = leftTurn;
        emptyStackTurns474 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState476() {
        pos = path475;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize475;
        leftTurn = leftTurn475;
        emptyStackTurns = emptyStackTurns475;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 476;
    }

    void appendCurrentState475() {
        path475 = pos;
        stackSize475 = stackSize;
        leftTurn475 = leftTurn;
        emptyStackTurns475 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState477() {
        pos = path476;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize476;
        leftTurn = leftTurn476;
        emptyStackTurns = emptyStackTurns476;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 477;
    }

    void appendCurrentState476() {
        path476 = pos;
        stackSize476 = stackSize;
        leftTurn476 = leftTurn;
        emptyStackTurns476 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState478() {
        pos = path477;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize477;
        leftTurn = leftTurn477;
        emptyStackTurns = emptyStackTurns477;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 478;
    }

    void appendCurrentState477() {
        path477 = pos;
        stackSize477 = stackSize;
        leftTurn477 = leftTurn;
        emptyStackTurns477 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState479() {
        pos = path478;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize478;
        leftTurn = leftTurn478;
        emptyStackTurns = emptyStackTurns478;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 479;
    }

    void appendCurrentState478() {
        path478 = pos;
        stackSize478 = stackSize;
        leftTurn478 = leftTurn;
        emptyStackTurns478 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState480() {
        pos = path479;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize479;
        leftTurn = leftTurn479;
        emptyStackTurns = emptyStackTurns479;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 480;
    }

    void appendCurrentState479() {
        path479 = pos;
        stackSize479 = stackSize;
        leftTurn479 = leftTurn;
        emptyStackTurns479 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState481() {
        pos = path480;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize480;
        leftTurn = leftTurn480;
        emptyStackTurns = emptyStackTurns480;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 481;
    }

    void appendCurrentState480() {
        path480 = pos;
        stackSize480 = stackSize;
        leftTurn480 = leftTurn;
        emptyStackTurns480 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState482() {
        pos = path481;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize481;
        leftTurn = leftTurn481;
        emptyStackTurns = emptyStackTurns481;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 482;
    }

    void appendCurrentState481() {
        path481 = pos;
        stackSize481 = stackSize;
        leftTurn481 = leftTurn;
        emptyStackTurns481 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState483() {
        pos = path482;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize482;
        leftTurn = leftTurn482;
        emptyStackTurns = emptyStackTurns482;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 483;
    }

    void appendCurrentState482() {
        path482 = pos;
        stackSize482 = stackSize;
        leftTurn482 = leftTurn;
        emptyStackTurns482 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState484() {
        pos = path483;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize483;
        leftTurn = leftTurn483;
        emptyStackTurns = emptyStackTurns483;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 484;
    }

    void appendCurrentState483() {
        path483 = pos;
        stackSize483 = stackSize;
        leftTurn483 = leftTurn;
        emptyStackTurns483 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState485() {
        pos = path484;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize484;
        leftTurn = leftTurn484;
        emptyStackTurns = emptyStackTurns484;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 485;
    }

    void appendCurrentState484() {
        path484 = pos;
        stackSize484 = stackSize;
        leftTurn484 = leftTurn;
        emptyStackTurns484 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState486() {
        pos = path485;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize485;
        leftTurn = leftTurn485;
        emptyStackTurns = emptyStackTurns485;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 486;
    }

    void appendCurrentState485() {
        path485 = pos;
        stackSize485 = stackSize;
        leftTurn485 = leftTurn;
        emptyStackTurns485 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState487() {
        pos = path486;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize486;
        leftTurn = leftTurn486;
        emptyStackTurns = emptyStackTurns486;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 487;
    }

    void appendCurrentState486() {
        path486 = pos;
        stackSize486 = stackSize;
        leftTurn486 = leftTurn;
        emptyStackTurns486 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState488() {
        pos = path487;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize487;
        leftTurn = leftTurn487;
        emptyStackTurns = emptyStackTurns487;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 488;
    }

    void appendCurrentState487() {
        path487 = pos;
        stackSize487 = stackSize;
        leftTurn487 = leftTurn;
        emptyStackTurns487 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState489() {
        pos = path488;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize488;
        leftTurn = leftTurn488;
        emptyStackTurns = emptyStackTurns488;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 489;
    }

    void appendCurrentState488() {
        path488 = pos;
        stackSize488 = stackSize;
        leftTurn488 = leftTurn;
        emptyStackTurns488 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState490() {
        pos = path489;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize489;
        leftTurn = leftTurn489;
        emptyStackTurns = emptyStackTurns489;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 490;
    }

    void appendCurrentState489() {
        path489 = pos;
        stackSize489 = stackSize;
        leftTurn489 = leftTurn;
        emptyStackTurns489 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState491() {
        pos = path490;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize490;
        leftTurn = leftTurn490;
        emptyStackTurns = emptyStackTurns490;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 491;
    }

    void appendCurrentState490() {
        path490 = pos;
        stackSize490 = stackSize;
        leftTurn490 = leftTurn;
        emptyStackTurns490 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState492() {
        pos = path491;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize491;
        leftTurn = leftTurn491;
        emptyStackTurns = emptyStackTurns491;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 492;
    }

    void appendCurrentState491() {
        path491 = pos;
        stackSize491 = stackSize;
        leftTurn491 = leftTurn;
        emptyStackTurns491 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState493() {
        pos = path492;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize492;
        leftTurn = leftTurn492;
        emptyStackTurns = emptyStackTurns492;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 493;
    }

    void appendCurrentState492() {
        path492 = pos;
        stackSize492 = stackSize;
        leftTurn492 = leftTurn;
        emptyStackTurns492 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState494() {
        pos = path493;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize493;
        leftTurn = leftTurn493;
        emptyStackTurns = emptyStackTurns493;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 494;
    }

    void appendCurrentState493() {
        path493 = pos;
        stackSize493 = stackSize;
        leftTurn493 = leftTurn;
        emptyStackTurns493 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState495() {
        pos = path494;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize494;
        leftTurn = leftTurn494;
        emptyStackTurns = emptyStackTurns494;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 495;
    }

    void appendCurrentState494() {
        path494 = pos;
        stackSize494 = stackSize;
        leftTurn494 = leftTurn;
        emptyStackTurns494 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState496() {
        pos = path495;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize495;
        leftTurn = leftTurn495;
        emptyStackTurns = emptyStackTurns495;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 496;
    }

    void appendCurrentState495() {
        path495 = pos;
        stackSize495 = stackSize;
        leftTurn495 = leftTurn;
        emptyStackTurns495 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState497() {
        pos = path496;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize496;
        leftTurn = leftTurn496;
        emptyStackTurns = emptyStackTurns496;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 497;
    }

    void appendCurrentState496() {
        path496 = pos;
        stackSize496 = stackSize;
        leftTurn496 = leftTurn;
        emptyStackTurns496 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState498() {
        pos = path497;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize497;
        leftTurn = leftTurn497;
        emptyStackTurns = emptyStackTurns497;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 498;
    }

    void appendCurrentState497() {
        path497 = pos;
        stackSize497 = stackSize;
        leftTurn497 = leftTurn;
        emptyStackTurns497 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState499() {
        pos = path498;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize498;
        leftTurn = leftTurn498;
        emptyStackTurns = emptyStackTurns498;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 499;
    }

    void appendCurrentState498() {
        path498 = pos;
        stackSize498 = stackSize;
        leftTurn498 = leftTurn;
        emptyStackTurns498 = emptyStackTurns;
        pathLength++;
    }
    
    void setCurrentState500() {
        pos = path499;
        bottomDir = pos.directionTo(target);
        stackSize = stackSize499;
        leftTurn = leftTurn499;
        emptyStackTurns = emptyStackTurns499;
        topDir = bottomDir;
        if(leftTurn) {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateLeft();
            }
        } else {
            for(int i = stackSize; i > 0; i--) {
                topDir = topDir.rotateRight();
            }
        }
        pathLength = 500;
    }

    void appendCurrentState499() {
        path499 = pos;
        stackSize499 = stackSize;
        leftTurn499 = leftTurn;
        emptyStackTurns499 = emptyStackTurns;
        pathLength++;
    }
    

    void setCurrentState() {
        switch(pathLength) {
            
            case 500:
                if(chebyshevDistance(path499, rc.getLocation()) <= 5) {
                    if(pathLength != 500) setCurrentState500();
                    break;
                }
                
            case 499:
                if(chebyshevDistance(path498, rc.getLocation()) <= 5) {
                    if(pathLength != 499) setCurrentState499();
                    break;
                }
                
            case 498:
                if(chebyshevDistance(path497, rc.getLocation()) <= 5) {
                    if(pathLength != 498) setCurrentState498();
                    break;
                }
                
            case 497:
                if(chebyshevDistance(path496, rc.getLocation()) <= 5) {
                    if(pathLength != 497) setCurrentState497();
                    break;
                }
                
            case 496:
                if(chebyshevDistance(path495, rc.getLocation()) <= 5) {
                    if(pathLength != 496) setCurrentState496();
                    break;
                }
                
            case 495:
                if(chebyshevDistance(path494, rc.getLocation()) <= 5) {
                    if(pathLength != 495) setCurrentState495();
                    break;
                }
                
            case 494:
                if(chebyshevDistance(path493, rc.getLocation()) <= 5) {
                    if(pathLength != 494) setCurrentState494();
                    break;
                }
                
            case 493:
                if(chebyshevDistance(path492, rc.getLocation()) <= 5) {
                    if(pathLength != 493) setCurrentState493();
                    break;
                }
                
            case 492:
                if(chebyshevDistance(path491, rc.getLocation()) <= 5) {
                    if(pathLength != 492) setCurrentState492();
                    break;
                }
                
            case 491:
                if(chebyshevDistance(path490, rc.getLocation()) <= 5) {
                    if(pathLength != 491) setCurrentState491();
                    break;
                }
                
            case 490:
                if(chebyshevDistance(path489, rc.getLocation()) <= 5) {
                    if(pathLength != 490) setCurrentState490();
                    break;
                }
                
            case 489:
                if(chebyshevDistance(path488, rc.getLocation()) <= 5) {
                    if(pathLength != 489) setCurrentState489();
                    break;
                }
                
            case 488:
                if(chebyshevDistance(path487, rc.getLocation()) <= 5) {
                    if(pathLength != 488) setCurrentState488();
                    break;
                }
                
            case 487:
                if(chebyshevDistance(path486, rc.getLocation()) <= 5) {
                    if(pathLength != 487) setCurrentState487();
                    break;
                }
                
            case 486:
                if(chebyshevDistance(path485, rc.getLocation()) <= 5) {
                    if(pathLength != 486) setCurrentState486();
                    break;
                }
                
            case 485:
                if(chebyshevDistance(path484, rc.getLocation()) <= 5) {
                    if(pathLength != 485) setCurrentState485();
                    break;
                }
                
            case 484:
                if(chebyshevDistance(path483, rc.getLocation()) <= 5) {
                    if(pathLength != 484) setCurrentState484();
                    break;
                }
                
            case 483:
                if(chebyshevDistance(path482, rc.getLocation()) <= 5) {
                    if(pathLength != 483) setCurrentState483();
                    break;
                }
                
            case 482:
                if(chebyshevDistance(path481, rc.getLocation()) <= 5) {
                    if(pathLength != 482) setCurrentState482();
                    break;
                }
                
            case 481:
                if(chebyshevDistance(path480, rc.getLocation()) <= 5) {
                    if(pathLength != 481) setCurrentState481();
                    break;
                }
                
            case 480:
                if(chebyshevDistance(path479, rc.getLocation()) <= 5) {
                    if(pathLength != 480) setCurrentState480();
                    break;
                }
                
            case 479:
                if(chebyshevDistance(path478, rc.getLocation()) <= 5) {
                    if(pathLength != 479) setCurrentState479();
                    break;
                }
                
            case 478:
                if(chebyshevDistance(path477, rc.getLocation()) <= 5) {
                    if(pathLength != 478) setCurrentState478();
                    break;
                }
                
            case 477:
                if(chebyshevDistance(path476, rc.getLocation()) <= 5) {
                    if(pathLength != 477) setCurrentState477();
                    break;
                }
                
            case 476:
                if(chebyshevDistance(path475, rc.getLocation()) <= 5) {
                    if(pathLength != 476) setCurrentState476();
                    break;
                }
                
            case 475:
                if(chebyshevDistance(path474, rc.getLocation()) <= 5) {
                    if(pathLength != 475) setCurrentState475();
                    break;
                }
                
            case 474:
                if(chebyshevDistance(path473, rc.getLocation()) <= 5) {
                    if(pathLength != 474) setCurrentState474();
                    break;
                }
                
            case 473:
                if(chebyshevDistance(path472, rc.getLocation()) <= 5) {
                    if(pathLength != 473) setCurrentState473();
                    break;
                }
                
            case 472:
                if(chebyshevDistance(path471, rc.getLocation()) <= 5) {
                    if(pathLength != 472) setCurrentState472();
                    break;
                }
                
            case 471:
                if(chebyshevDistance(path470, rc.getLocation()) <= 5) {
                    if(pathLength != 471) setCurrentState471();
                    break;
                }
                
            case 470:
                if(chebyshevDistance(path469, rc.getLocation()) <= 5) {
                    if(pathLength != 470) setCurrentState470();
                    break;
                }
                
            case 469:
                if(chebyshevDistance(path468, rc.getLocation()) <= 5) {
                    if(pathLength != 469) setCurrentState469();
                    break;
                }
                
            case 468:
                if(chebyshevDistance(path467, rc.getLocation()) <= 5) {
                    if(pathLength != 468) setCurrentState468();
                    break;
                }
                
            case 467:
                if(chebyshevDistance(path466, rc.getLocation()) <= 5) {
                    if(pathLength != 467) setCurrentState467();
                    break;
                }
                
            case 466:
                if(chebyshevDistance(path465, rc.getLocation()) <= 5) {
                    if(pathLength != 466) setCurrentState466();
                    break;
                }
                
            case 465:
                if(chebyshevDistance(path464, rc.getLocation()) <= 5) {
                    if(pathLength != 465) setCurrentState465();
                    break;
                }
                
            case 464:
                if(chebyshevDistance(path463, rc.getLocation()) <= 5) {
                    if(pathLength != 464) setCurrentState464();
                    break;
                }
                
            case 463:
                if(chebyshevDistance(path462, rc.getLocation()) <= 5) {
                    if(pathLength != 463) setCurrentState463();
                    break;
                }
                
            case 462:
                if(chebyshevDistance(path461, rc.getLocation()) <= 5) {
                    if(pathLength != 462) setCurrentState462();
                    break;
                }
                
            case 461:
                if(chebyshevDistance(path460, rc.getLocation()) <= 5) {
                    if(pathLength != 461) setCurrentState461();
                    break;
                }
                
            case 460:
                if(chebyshevDistance(path459, rc.getLocation()) <= 5) {
                    if(pathLength != 460) setCurrentState460();
                    break;
                }
                
            case 459:
                if(chebyshevDistance(path458, rc.getLocation()) <= 5) {
                    if(pathLength != 459) setCurrentState459();
                    break;
                }
                
            case 458:
                if(chebyshevDistance(path457, rc.getLocation()) <= 5) {
                    if(pathLength != 458) setCurrentState458();
                    break;
                }
                
            case 457:
                if(chebyshevDistance(path456, rc.getLocation()) <= 5) {
                    if(pathLength != 457) setCurrentState457();
                    break;
                }
                
            case 456:
                if(chebyshevDistance(path455, rc.getLocation()) <= 5) {
                    if(pathLength != 456) setCurrentState456();
                    break;
                }
                
            case 455:
                if(chebyshevDistance(path454, rc.getLocation()) <= 5) {
                    if(pathLength != 455) setCurrentState455();
                    break;
                }
                
            case 454:
                if(chebyshevDistance(path453, rc.getLocation()) <= 5) {
                    if(pathLength != 454) setCurrentState454();
                    break;
                }
                
            case 453:
                if(chebyshevDistance(path452, rc.getLocation()) <= 5) {
                    if(pathLength != 453) setCurrentState453();
                    break;
                }
                
            case 452:
                if(chebyshevDistance(path451, rc.getLocation()) <= 5) {
                    if(pathLength != 452) setCurrentState452();
                    break;
                }
                
            case 451:
                if(chebyshevDistance(path450, rc.getLocation()) <= 5) {
                    if(pathLength != 451) setCurrentState451();
                    break;
                }
                
            case 450:
                if(chebyshevDistance(path449, rc.getLocation()) <= 5) {
                    if(pathLength != 450) setCurrentState450();
                    break;
                }
                
            case 449:
                if(chebyshevDistance(path448, rc.getLocation()) <= 5) {
                    if(pathLength != 449) setCurrentState449();
                    break;
                }
                
            case 448:
                if(chebyshevDistance(path447, rc.getLocation()) <= 5) {
                    if(pathLength != 448) setCurrentState448();
                    break;
                }
                
            case 447:
                if(chebyshevDistance(path446, rc.getLocation()) <= 5) {
                    if(pathLength != 447) setCurrentState447();
                    break;
                }
                
            case 446:
                if(chebyshevDistance(path445, rc.getLocation()) <= 5) {
                    if(pathLength != 446) setCurrentState446();
                    break;
                }
                
            case 445:
                if(chebyshevDistance(path444, rc.getLocation()) <= 5) {
                    if(pathLength != 445) setCurrentState445();
                    break;
                }
                
            case 444:
                if(chebyshevDistance(path443, rc.getLocation()) <= 5) {
                    if(pathLength != 444) setCurrentState444();
                    break;
                }
                
            case 443:
                if(chebyshevDistance(path442, rc.getLocation()) <= 5) {
                    if(pathLength != 443) setCurrentState443();
                    break;
                }
                
            case 442:
                if(chebyshevDistance(path441, rc.getLocation()) <= 5) {
                    if(pathLength != 442) setCurrentState442();
                    break;
                }
                
            case 441:
                if(chebyshevDistance(path440, rc.getLocation()) <= 5) {
                    if(pathLength != 441) setCurrentState441();
                    break;
                }
                
            case 440:
                if(chebyshevDistance(path439, rc.getLocation()) <= 5) {
                    if(pathLength != 440) setCurrentState440();
                    break;
                }
                
            case 439:
                if(chebyshevDistance(path438, rc.getLocation()) <= 5) {
                    if(pathLength != 439) setCurrentState439();
                    break;
                }
                
            case 438:
                if(chebyshevDistance(path437, rc.getLocation()) <= 5) {
                    if(pathLength != 438) setCurrentState438();
                    break;
                }
                
            case 437:
                if(chebyshevDistance(path436, rc.getLocation()) <= 5) {
                    if(pathLength != 437) setCurrentState437();
                    break;
                }
                
            case 436:
                if(chebyshevDistance(path435, rc.getLocation()) <= 5) {
                    if(pathLength != 436) setCurrentState436();
                    break;
                }
                
            case 435:
                if(chebyshevDistance(path434, rc.getLocation()) <= 5) {
                    if(pathLength != 435) setCurrentState435();
                    break;
                }
                
            case 434:
                if(chebyshevDistance(path433, rc.getLocation()) <= 5) {
                    if(pathLength != 434) setCurrentState434();
                    break;
                }
                
            case 433:
                if(chebyshevDistance(path432, rc.getLocation()) <= 5) {
                    if(pathLength != 433) setCurrentState433();
                    break;
                }
                
            case 432:
                if(chebyshevDistance(path431, rc.getLocation()) <= 5) {
                    if(pathLength != 432) setCurrentState432();
                    break;
                }
                
            case 431:
                if(chebyshevDistance(path430, rc.getLocation()) <= 5) {
                    if(pathLength != 431) setCurrentState431();
                    break;
                }
                
            case 430:
                if(chebyshevDistance(path429, rc.getLocation()) <= 5) {
                    if(pathLength != 430) setCurrentState430();
                    break;
                }
                
            case 429:
                if(chebyshevDistance(path428, rc.getLocation()) <= 5) {
                    if(pathLength != 429) setCurrentState429();
                    break;
                }
                
            case 428:
                if(chebyshevDistance(path427, rc.getLocation()) <= 5) {
                    if(pathLength != 428) setCurrentState428();
                    break;
                }
                
            case 427:
                if(chebyshevDistance(path426, rc.getLocation()) <= 5) {
                    if(pathLength != 427) setCurrentState427();
                    break;
                }
                
            case 426:
                if(chebyshevDistance(path425, rc.getLocation()) <= 5) {
                    if(pathLength != 426) setCurrentState426();
                    break;
                }
                
            case 425:
                if(chebyshevDistance(path424, rc.getLocation()) <= 5) {
                    if(pathLength != 425) setCurrentState425();
                    break;
                }
                
            case 424:
                if(chebyshevDistance(path423, rc.getLocation()) <= 5) {
                    if(pathLength != 424) setCurrentState424();
                    break;
                }
                
            case 423:
                if(chebyshevDistance(path422, rc.getLocation()) <= 5) {
                    if(pathLength != 423) setCurrentState423();
                    break;
                }
                
            case 422:
                if(chebyshevDistance(path421, rc.getLocation()) <= 5) {
                    if(pathLength != 422) setCurrentState422();
                    break;
                }
                
            case 421:
                if(chebyshevDistance(path420, rc.getLocation()) <= 5) {
                    if(pathLength != 421) setCurrentState421();
                    break;
                }
                
            case 420:
                if(chebyshevDistance(path419, rc.getLocation()) <= 5) {
                    if(pathLength != 420) setCurrentState420();
                    break;
                }
                
            case 419:
                if(chebyshevDistance(path418, rc.getLocation()) <= 5) {
                    if(pathLength != 419) setCurrentState419();
                    break;
                }
                
            case 418:
                if(chebyshevDistance(path417, rc.getLocation()) <= 5) {
                    if(pathLength != 418) setCurrentState418();
                    break;
                }
                
            case 417:
                if(chebyshevDistance(path416, rc.getLocation()) <= 5) {
                    if(pathLength != 417) setCurrentState417();
                    break;
                }
                
            case 416:
                if(chebyshevDistance(path415, rc.getLocation()) <= 5) {
                    if(pathLength != 416) setCurrentState416();
                    break;
                }
                
            case 415:
                if(chebyshevDistance(path414, rc.getLocation()) <= 5) {
                    if(pathLength != 415) setCurrentState415();
                    break;
                }
                
            case 414:
                if(chebyshevDistance(path413, rc.getLocation()) <= 5) {
                    if(pathLength != 414) setCurrentState414();
                    break;
                }
                
            case 413:
                if(chebyshevDistance(path412, rc.getLocation()) <= 5) {
                    if(pathLength != 413) setCurrentState413();
                    break;
                }
                
            case 412:
                if(chebyshevDistance(path411, rc.getLocation()) <= 5) {
                    if(pathLength != 412) setCurrentState412();
                    break;
                }
                
            case 411:
                if(chebyshevDistance(path410, rc.getLocation()) <= 5) {
                    if(pathLength != 411) setCurrentState411();
                    break;
                }
                
            case 410:
                if(chebyshevDistance(path409, rc.getLocation()) <= 5) {
                    if(pathLength != 410) setCurrentState410();
                    break;
                }
                
            case 409:
                if(chebyshevDistance(path408, rc.getLocation()) <= 5) {
                    if(pathLength != 409) setCurrentState409();
                    break;
                }
                
            case 408:
                if(chebyshevDistance(path407, rc.getLocation()) <= 5) {
                    if(pathLength != 408) setCurrentState408();
                    break;
                }
                
            case 407:
                if(chebyshevDistance(path406, rc.getLocation()) <= 5) {
                    if(pathLength != 407) setCurrentState407();
                    break;
                }
                
            case 406:
                if(chebyshevDistance(path405, rc.getLocation()) <= 5) {
                    if(pathLength != 406) setCurrentState406();
                    break;
                }
                
            case 405:
                if(chebyshevDistance(path404, rc.getLocation()) <= 5) {
                    if(pathLength != 405) setCurrentState405();
                    break;
                }
                
            case 404:
                if(chebyshevDistance(path403, rc.getLocation()) <= 5) {
                    if(pathLength != 404) setCurrentState404();
                    break;
                }
                
            case 403:
                if(chebyshevDistance(path402, rc.getLocation()) <= 5) {
                    if(pathLength != 403) setCurrentState403();
                    break;
                }
                
            case 402:
                if(chebyshevDistance(path401, rc.getLocation()) <= 5) {
                    if(pathLength != 402) setCurrentState402();
                    break;
                }
                
            case 401:
                if(chebyshevDistance(path400, rc.getLocation()) <= 5) {
                    if(pathLength != 401) setCurrentState401();
                    break;
                }
                
            case 400:
                if(chebyshevDistance(path399, rc.getLocation()) <= 5) {
                    if(pathLength != 400) setCurrentState400();
                    break;
                }
                
            case 399:
                if(chebyshevDistance(path398, rc.getLocation()) <= 5) {
                    if(pathLength != 399) setCurrentState399();
                    break;
                }
                
            case 398:
                if(chebyshevDistance(path397, rc.getLocation()) <= 5) {
                    if(pathLength != 398) setCurrentState398();
                    break;
                }
                
            case 397:
                if(chebyshevDistance(path396, rc.getLocation()) <= 5) {
                    if(pathLength != 397) setCurrentState397();
                    break;
                }
                
            case 396:
                if(chebyshevDistance(path395, rc.getLocation()) <= 5) {
                    if(pathLength != 396) setCurrentState396();
                    break;
                }
                
            case 395:
                if(chebyshevDistance(path394, rc.getLocation()) <= 5) {
                    if(pathLength != 395) setCurrentState395();
                    break;
                }
                
            case 394:
                if(chebyshevDistance(path393, rc.getLocation()) <= 5) {
                    if(pathLength != 394) setCurrentState394();
                    break;
                }
                
            case 393:
                if(chebyshevDistance(path392, rc.getLocation()) <= 5) {
                    if(pathLength != 393) setCurrentState393();
                    break;
                }
                
            case 392:
                if(chebyshevDistance(path391, rc.getLocation()) <= 5) {
                    if(pathLength != 392) setCurrentState392();
                    break;
                }
                
            case 391:
                if(chebyshevDistance(path390, rc.getLocation()) <= 5) {
                    if(pathLength != 391) setCurrentState391();
                    break;
                }
                
            case 390:
                if(chebyshevDistance(path389, rc.getLocation()) <= 5) {
                    if(pathLength != 390) setCurrentState390();
                    break;
                }
                
            case 389:
                if(chebyshevDistance(path388, rc.getLocation()) <= 5) {
                    if(pathLength != 389) setCurrentState389();
                    break;
                }
                
            case 388:
                if(chebyshevDistance(path387, rc.getLocation()) <= 5) {
                    if(pathLength != 388) setCurrentState388();
                    break;
                }
                
            case 387:
                if(chebyshevDistance(path386, rc.getLocation()) <= 5) {
                    if(pathLength != 387) setCurrentState387();
                    break;
                }
                
            case 386:
                if(chebyshevDistance(path385, rc.getLocation()) <= 5) {
                    if(pathLength != 386) setCurrentState386();
                    break;
                }
                
            case 385:
                if(chebyshevDistance(path384, rc.getLocation()) <= 5) {
                    if(pathLength != 385) setCurrentState385();
                    break;
                }
                
            case 384:
                if(chebyshevDistance(path383, rc.getLocation()) <= 5) {
                    if(pathLength != 384) setCurrentState384();
                    break;
                }
                
            case 383:
                if(chebyshevDistance(path382, rc.getLocation()) <= 5) {
                    if(pathLength != 383) setCurrentState383();
                    break;
                }
                
            case 382:
                if(chebyshevDistance(path381, rc.getLocation()) <= 5) {
                    if(pathLength != 382) setCurrentState382();
                    break;
                }
                
            case 381:
                if(chebyshevDistance(path380, rc.getLocation()) <= 5) {
                    if(pathLength != 381) setCurrentState381();
                    break;
                }
                
            case 380:
                if(chebyshevDistance(path379, rc.getLocation()) <= 5) {
                    if(pathLength != 380) setCurrentState380();
                    break;
                }
                
            case 379:
                if(chebyshevDistance(path378, rc.getLocation()) <= 5) {
                    if(pathLength != 379) setCurrentState379();
                    break;
                }
                
            case 378:
                if(chebyshevDistance(path377, rc.getLocation()) <= 5) {
                    if(pathLength != 378) setCurrentState378();
                    break;
                }
                
            case 377:
                if(chebyshevDistance(path376, rc.getLocation()) <= 5) {
                    if(pathLength != 377) setCurrentState377();
                    break;
                }
                
            case 376:
                if(chebyshevDistance(path375, rc.getLocation()) <= 5) {
                    if(pathLength != 376) setCurrentState376();
                    break;
                }
                
            case 375:
                if(chebyshevDistance(path374, rc.getLocation()) <= 5) {
                    if(pathLength != 375) setCurrentState375();
                    break;
                }
                
            case 374:
                if(chebyshevDistance(path373, rc.getLocation()) <= 5) {
                    if(pathLength != 374) setCurrentState374();
                    break;
                }
                
            case 373:
                if(chebyshevDistance(path372, rc.getLocation()) <= 5) {
                    if(pathLength != 373) setCurrentState373();
                    break;
                }
                
            case 372:
                if(chebyshevDistance(path371, rc.getLocation()) <= 5) {
                    if(pathLength != 372) setCurrentState372();
                    break;
                }
                
            case 371:
                if(chebyshevDistance(path370, rc.getLocation()) <= 5) {
                    if(pathLength != 371) setCurrentState371();
                    break;
                }
                
            case 370:
                if(chebyshevDistance(path369, rc.getLocation()) <= 5) {
                    if(pathLength != 370) setCurrentState370();
                    break;
                }
                
            case 369:
                if(chebyshevDistance(path368, rc.getLocation()) <= 5) {
                    if(pathLength != 369) setCurrentState369();
                    break;
                }
                
            case 368:
                if(chebyshevDistance(path367, rc.getLocation()) <= 5) {
                    if(pathLength != 368) setCurrentState368();
                    break;
                }
                
            case 367:
                if(chebyshevDistance(path366, rc.getLocation()) <= 5) {
                    if(pathLength != 367) setCurrentState367();
                    break;
                }
                
            case 366:
                if(chebyshevDistance(path365, rc.getLocation()) <= 5) {
                    if(pathLength != 366) setCurrentState366();
                    break;
                }
                
            case 365:
                if(chebyshevDistance(path364, rc.getLocation()) <= 5) {
                    if(pathLength != 365) setCurrentState365();
                    break;
                }
                
            case 364:
                if(chebyshevDistance(path363, rc.getLocation()) <= 5) {
                    if(pathLength != 364) setCurrentState364();
                    break;
                }
                
            case 363:
                if(chebyshevDistance(path362, rc.getLocation()) <= 5) {
                    if(pathLength != 363) setCurrentState363();
                    break;
                }
                
            case 362:
                if(chebyshevDistance(path361, rc.getLocation()) <= 5) {
                    if(pathLength != 362) setCurrentState362();
                    break;
                }
                
            case 361:
                if(chebyshevDistance(path360, rc.getLocation()) <= 5) {
                    if(pathLength != 361) setCurrentState361();
                    break;
                }
                
            case 360:
                if(chebyshevDistance(path359, rc.getLocation()) <= 5) {
                    if(pathLength != 360) setCurrentState360();
                    break;
                }
                
            case 359:
                if(chebyshevDistance(path358, rc.getLocation()) <= 5) {
                    if(pathLength != 359) setCurrentState359();
                    break;
                }
                
            case 358:
                if(chebyshevDistance(path357, rc.getLocation()) <= 5) {
                    if(pathLength != 358) setCurrentState358();
                    break;
                }
                
            case 357:
                if(chebyshevDistance(path356, rc.getLocation()) <= 5) {
                    if(pathLength != 357) setCurrentState357();
                    break;
                }
                
            case 356:
                if(chebyshevDistance(path355, rc.getLocation()) <= 5) {
                    if(pathLength != 356) setCurrentState356();
                    break;
                }
                
            case 355:
                if(chebyshevDistance(path354, rc.getLocation()) <= 5) {
                    if(pathLength != 355) setCurrentState355();
                    break;
                }
                
            case 354:
                if(chebyshevDistance(path353, rc.getLocation()) <= 5) {
                    if(pathLength != 354) setCurrentState354();
                    break;
                }
                
            case 353:
                if(chebyshevDistance(path352, rc.getLocation()) <= 5) {
                    if(pathLength != 353) setCurrentState353();
                    break;
                }
                
            case 352:
                if(chebyshevDistance(path351, rc.getLocation()) <= 5) {
                    if(pathLength != 352) setCurrentState352();
                    break;
                }
                
            case 351:
                if(chebyshevDistance(path350, rc.getLocation()) <= 5) {
                    if(pathLength != 351) setCurrentState351();
                    break;
                }
                
            case 350:
                if(chebyshevDistance(path349, rc.getLocation()) <= 5) {
                    if(pathLength != 350) setCurrentState350();
                    break;
                }
                
            case 349:
                if(chebyshevDistance(path348, rc.getLocation()) <= 5) {
                    if(pathLength != 349) setCurrentState349();
                    break;
                }
                
            case 348:
                if(chebyshevDistance(path347, rc.getLocation()) <= 5) {
                    if(pathLength != 348) setCurrentState348();
                    break;
                }
                
            case 347:
                if(chebyshevDistance(path346, rc.getLocation()) <= 5) {
                    if(pathLength != 347) setCurrentState347();
                    break;
                }
                
            case 346:
                if(chebyshevDistance(path345, rc.getLocation()) <= 5) {
                    if(pathLength != 346) setCurrentState346();
                    break;
                }
                
            case 345:
                if(chebyshevDistance(path344, rc.getLocation()) <= 5) {
                    if(pathLength != 345) setCurrentState345();
                    break;
                }
                
            case 344:
                if(chebyshevDistance(path343, rc.getLocation()) <= 5) {
                    if(pathLength != 344) setCurrentState344();
                    break;
                }
                
            case 343:
                if(chebyshevDistance(path342, rc.getLocation()) <= 5) {
                    if(pathLength != 343) setCurrentState343();
                    break;
                }
                
            case 342:
                if(chebyshevDistance(path341, rc.getLocation()) <= 5) {
                    if(pathLength != 342) setCurrentState342();
                    break;
                }
                
            case 341:
                if(chebyshevDistance(path340, rc.getLocation()) <= 5) {
                    if(pathLength != 341) setCurrentState341();
                    break;
                }
                
            case 340:
                if(chebyshevDistance(path339, rc.getLocation()) <= 5) {
                    if(pathLength != 340) setCurrentState340();
                    break;
                }
                
            case 339:
                if(chebyshevDistance(path338, rc.getLocation()) <= 5) {
                    if(pathLength != 339) setCurrentState339();
                    break;
                }
                
            case 338:
                if(chebyshevDistance(path337, rc.getLocation()) <= 5) {
                    if(pathLength != 338) setCurrentState338();
                    break;
                }
                
            case 337:
                if(chebyshevDistance(path336, rc.getLocation()) <= 5) {
                    if(pathLength != 337) setCurrentState337();
                    break;
                }
                
            case 336:
                if(chebyshevDistance(path335, rc.getLocation()) <= 5) {
                    if(pathLength != 336) setCurrentState336();
                    break;
                }
                
            case 335:
                if(chebyshevDistance(path334, rc.getLocation()) <= 5) {
                    if(pathLength != 335) setCurrentState335();
                    break;
                }
                
            case 334:
                if(chebyshevDistance(path333, rc.getLocation()) <= 5) {
                    if(pathLength != 334) setCurrentState334();
                    break;
                }
                
            case 333:
                if(chebyshevDistance(path332, rc.getLocation()) <= 5) {
                    if(pathLength != 333) setCurrentState333();
                    break;
                }
                
            case 332:
                if(chebyshevDistance(path331, rc.getLocation()) <= 5) {
                    if(pathLength != 332) setCurrentState332();
                    break;
                }
                
            case 331:
                if(chebyshevDistance(path330, rc.getLocation()) <= 5) {
                    if(pathLength != 331) setCurrentState331();
                    break;
                }
                
            case 330:
                if(chebyshevDistance(path329, rc.getLocation()) <= 5) {
                    if(pathLength != 330) setCurrentState330();
                    break;
                }
                
            case 329:
                if(chebyshevDistance(path328, rc.getLocation()) <= 5) {
                    if(pathLength != 329) setCurrentState329();
                    break;
                }
                
            case 328:
                if(chebyshevDistance(path327, rc.getLocation()) <= 5) {
                    if(pathLength != 328) setCurrentState328();
                    break;
                }
                
            case 327:
                if(chebyshevDistance(path326, rc.getLocation()) <= 5) {
                    if(pathLength != 327) setCurrentState327();
                    break;
                }
                
            case 326:
                if(chebyshevDistance(path325, rc.getLocation()) <= 5) {
                    if(pathLength != 326) setCurrentState326();
                    break;
                }
                
            case 325:
                if(chebyshevDistance(path324, rc.getLocation()) <= 5) {
                    if(pathLength != 325) setCurrentState325();
                    break;
                }
                
            case 324:
                if(chebyshevDistance(path323, rc.getLocation()) <= 5) {
                    if(pathLength != 324) setCurrentState324();
                    break;
                }
                
            case 323:
                if(chebyshevDistance(path322, rc.getLocation()) <= 5) {
                    if(pathLength != 323) setCurrentState323();
                    break;
                }
                
            case 322:
                if(chebyshevDistance(path321, rc.getLocation()) <= 5) {
                    if(pathLength != 322) setCurrentState322();
                    break;
                }
                
            case 321:
                if(chebyshevDistance(path320, rc.getLocation()) <= 5) {
                    if(pathLength != 321) setCurrentState321();
                    break;
                }
                
            case 320:
                if(chebyshevDistance(path319, rc.getLocation()) <= 5) {
                    if(pathLength != 320) setCurrentState320();
                    break;
                }
                
            case 319:
                if(chebyshevDistance(path318, rc.getLocation()) <= 5) {
                    if(pathLength != 319) setCurrentState319();
                    break;
                }
                
            case 318:
                if(chebyshevDistance(path317, rc.getLocation()) <= 5) {
                    if(pathLength != 318) setCurrentState318();
                    break;
                }
                
            case 317:
                if(chebyshevDistance(path316, rc.getLocation()) <= 5) {
                    if(pathLength != 317) setCurrentState317();
                    break;
                }
                
            case 316:
                if(chebyshevDistance(path315, rc.getLocation()) <= 5) {
                    if(pathLength != 316) setCurrentState316();
                    break;
                }
                
            case 315:
                if(chebyshevDistance(path314, rc.getLocation()) <= 5) {
                    if(pathLength != 315) setCurrentState315();
                    break;
                }
                
            case 314:
                if(chebyshevDistance(path313, rc.getLocation()) <= 5) {
                    if(pathLength != 314) setCurrentState314();
                    break;
                }
                
            case 313:
                if(chebyshevDistance(path312, rc.getLocation()) <= 5) {
                    if(pathLength != 313) setCurrentState313();
                    break;
                }
                
            case 312:
                if(chebyshevDistance(path311, rc.getLocation()) <= 5) {
                    if(pathLength != 312) setCurrentState312();
                    break;
                }
                
            case 311:
                if(chebyshevDistance(path310, rc.getLocation()) <= 5) {
                    if(pathLength != 311) setCurrentState311();
                    break;
                }
                
            case 310:
                if(chebyshevDistance(path309, rc.getLocation()) <= 5) {
                    if(pathLength != 310) setCurrentState310();
                    break;
                }
                
            case 309:
                if(chebyshevDistance(path308, rc.getLocation()) <= 5) {
                    if(pathLength != 309) setCurrentState309();
                    break;
                }
                
            case 308:
                if(chebyshevDistance(path307, rc.getLocation()) <= 5) {
                    if(pathLength != 308) setCurrentState308();
                    break;
                }
                
            case 307:
                if(chebyshevDistance(path306, rc.getLocation()) <= 5) {
                    if(pathLength != 307) setCurrentState307();
                    break;
                }
                
            case 306:
                if(chebyshevDistance(path305, rc.getLocation()) <= 5) {
                    if(pathLength != 306) setCurrentState306();
                    break;
                }
                
            case 305:
                if(chebyshevDistance(path304, rc.getLocation()) <= 5) {
                    if(pathLength != 305) setCurrentState305();
                    break;
                }
                
            case 304:
                if(chebyshevDistance(path303, rc.getLocation()) <= 5) {
                    if(pathLength != 304) setCurrentState304();
                    break;
                }
                
            case 303:
                if(chebyshevDistance(path302, rc.getLocation()) <= 5) {
                    if(pathLength != 303) setCurrentState303();
                    break;
                }
                
            case 302:
                if(chebyshevDistance(path301, rc.getLocation()) <= 5) {
                    if(pathLength != 302) setCurrentState302();
                    break;
                }
                
            case 301:
                if(chebyshevDistance(path300, rc.getLocation()) <= 5) {
                    if(pathLength != 301) setCurrentState301();
                    break;
                }
                
            case 300:
                if(chebyshevDistance(path299, rc.getLocation()) <= 5) {
                    if(pathLength != 300) setCurrentState300();
                    break;
                }
                
            case 299:
                if(chebyshevDistance(path298, rc.getLocation()) <= 5) {
                    if(pathLength != 299) setCurrentState299();
                    break;
                }
                
            case 298:
                if(chebyshevDistance(path297, rc.getLocation()) <= 5) {
                    if(pathLength != 298) setCurrentState298();
                    break;
                }
                
            case 297:
                if(chebyshevDistance(path296, rc.getLocation()) <= 5) {
                    if(pathLength != 297) setCurrentState297();
                    break;
                }
                
            case 296:
                if(chebyshevDistance(path295, rc.getLocation()) <= 5) {
                    if(pathLength != 296) setCurrentState296();
                    break;
                }
                
            case 295:
                if(chebyshevDistance(path294, rc.getLocation()) <= 5) {
                    if(pathLength != 295) setCurrentState295();
                    break;
                }
                
            case 294:
                if(chebyshevDistance(path293, rc.getLocation()) <= 5) {
                    if(pathLength != 294) setCurrentState294();
                    break;
                }
                
            case 293:
                if(chebyshevDistance(path292, rc.getLocation()) <= 5) {
                    if(pathLength != 293) setCurrentState293();
                    break;
                }
                
            case 292:
                if(chebyshevDistance(path291, rc.getLocation()) <= 5) {
                    if(pathLength != 292) setCurrentState292();
                    break;
                }
                
            case 291:
                if(chebyshevDistance(path290, rc.getLocation()) <= 5) {
                    if(pathLength != 291) setCurrentState291();
                    break;
                }
                
            case 290:
                if(chebyshevDistance(path289, rc.getLocation()) <= 5) {
                    if(pathLength != 290) setCurrentState290();
                    break;
                }
                
            case 289:
                if(chebyshevDistance(path288, rc.getLocation()) <= 5) {
                    if(pathLength != 289) setCurrentState289();
                    break;
                }
                
            case 288:
                if(chebyshevDistance(path287, rc.getLocation()) <= 5) {
                    if(pathLength != 288) setCurrentState288();
                    break;
                }
                
            case 287:
                if(chebyshevDistance(path286, rc.getLocation()) <= 5) {
                    if(pathLength != 287) setCurrentState287();
                    break;
                }
                
            case 286:
                if(chebyshevDistance(path285, rc.getLocation()) <= 5) {
                    if(pathLength != 286) setCurrentState286();
                    break;
                }
                
            case 285:
                if(chebyshevDistance(path284, rc.getLocation()) <= 5) {
                    if(pathLength != 285) setCurrentState285();
                    break;
                }
                
            case 284:
                if(chebyshevDistance(path283, rc.getLocation()) <= 5) {
                    if(pathLength != 284) setCurrentState284();
                    break;
                }
                
            case 283:
                if(chebyshevDistance(path282, rc.getLocation()) <= 5) {
                    if(pathLength != 283) setCurrentState283();
                    break;
                }
                
            case 282:
                if(chebyshevDistance(path281, rc.getLocation()) <= 5) {
                    if(pathLength != 282) setCurrentState282();
                    break;
                }
                
            case 281:
                if(chebyshevDistance(path280, rc.getLocation()) <= 5) {
                    if(pathLength != 281) setCurrentState281();
                    break;
                }
                
            case 280:
                if(chebyshevDistance(path279, rc.getLocation()) <= 5) {
                    if(pathLength != 280) setCurrentState280();
                    break;
                }
                
            case 279:
                if(chebyshevDistance(path278, rc.getLocation()) <= 5) {
                    if(pathLength != 279) setCurrentState279();
                    break;
                }
                
            case 278:
                if(chebyshevDistance(path277, rc.getLocation()) <= 5) {
                    if(pathLength != 278) setCurrentState278();
                    break;
                }
                
            case 277:
                if(chebyshevDistance(path276, rc.getLocation()) <= 5) {
                    if(pathLength != 277) setCurrentState277();
                    break;
                }
                
            case 276:
                if(chebyshevDistance(path275, rc.getLocation()) <= 5) {
                    if(pathLength != 276) setCurrentState276();
                    break;
                }
                
            case 275:
                if(chebyshevDistance(path274, rc.getLocation()) <= 5) {
                    if(pathLength != 275) setCurrentState275();
                    break;
                }
                
            case 274:
                if(chebyshevDistance(path273, rc.getLocation()) <= 5) {
                    if(pathLength != 274) setCurrentState274();
                    break;
                }
                
            case 273:
                if(chebyshevDistance(path272, rc.getLocation()) <= 5) {
                    if(pathLength != 273) setCurrentState273();
                    break;
                }
                
            case 272:
                if(chebyshevDistance(path271, rc.getLocation()) <= 5) {
                    if(pathLength != 272) setCurrentState272();
                    break;
                }
                
            case 271:
                if(chebyshevDistance(path270, rc.getLocation()) <= 5) {
                    if(pathLength != 271) setCurrentState271();
                    break;
                }
                
            case 270:
                if(chebyshevDistance(path269, rc.getLocation()) <= 5) {
                    if(pathLength != 270) setCurrentState270();
                    break;
                }
                
            case 269:
                if(chebyshevDistance(path268, rc.getLocation()) <= 5) {
                    if(pathLength != 269) setCurrentState269();
                    break;
                }
                
            case 268:
                if(chebyshevDistance(path267, rc.getLocation()) <= 5) {
                    if(pathLength != 268) setCurrentState268();
                    break;
                }
                
            case 267:
                if(chebyshevDistance(path266, rc.getLocation()) <= 5) {
                    if(pathLength != 267) setCurrentState267();
                    break;
                }
                
            case 266:
                if(chebyshevDistance(path265, rc.getLocation()) <= 5) {
                    if(pathLength != 266) setCurrentState266();
                    break;
                }
                
            case 265:
                if(chebyshevDistance(path264, rc.getLocation()) <= 5) {
                    if(pathLength != 265) setCurrentState265();
                    break;
                }
                
            case 264:
                if(chebyshevDistance(path263, rc.getLocation()) <= 5) {
                    if(pathLength != 264) setCurrentState264();
                    break;
                }
                
            case 263:
                if(chebyshevDistance(path262, rc.getLocation()) <= 5) {
                    if(pathLength != 263) setCurrentState263();
                    break;
                }
                
            case 262:
                if(chebyshevDistance(path261, rc.getLocation()) <= 5) {
                    if(pathLength != 262) setCurrentState262();
                    break;
                }
                
            case 261:
                if(chebyshevDistance(path260, rc.getLocation()) <= 5) {
                    if(pathLength != 261) setCurrentState261();
                    break;
                }
                
            case 260:
                if(chebyshevDistance(path259, rc.getLocation()) <= 5) {
                    if(pathLength != 260) setCurrentState260();
                    break;
                }
                
            case 259:
                if(chebyshevDistance(path258, rc.getLocation()) <= 5) {
                    if(pathLength != 259) setCurrentState259();
                    break;
                }
                
            case 258:
                if(chebyshevDistance(path257, rc.getLocation()) <= 5) {
                    if(pathLength != 258) setCurrentState258();
                    break;
                }
                
            case 257:
                if(chebyshevDistance(path256, rc.getLocation()) <= 5) {
                    if(pathLength != 257) setCurrentState257();
                    break;
                }
                
            case 256:
                if(chebyshevDistance(path255, rc.getLocation()) <= 5) {
                    if(pathLength != 256) setCurrentState256();
                    break;
                }
                
            case 255:
                if(chebyshevDistance(path254, rc.getLocation()) <= 5) {
                    if(pathLength != 255) setCurrentState255();
                    break;
                }
                
            case 254:
                if(chebyshevDistance(path253, rc.getLocation()) <= 5) {
                    if(pathLength != 254) setCurrentState254();
                    break;
                }
                
            case 253:
                if(chebyshevDistance(path252, rc.getLocation()) <= 5) {
                    if(pathLength != 253) setCurrentState253();
                    break;
                }
                
            case 252:
                if(chebyshevDistance(path251, rc.getLocation()) <= 5) {
                    if(pathLength != 252) setCurrentState252();
                    break;
                }
                
            case 251:
                if(chebyshevDistance(path250, rc.getLocation()) <= 5) {
                    if(pathLength != 251) setCurrentState251();
                    break;
                }
                
            case 250:
                if(chebyshevDistance(path249, rc.getLocation()) <= 5) {
                    if(pathLength != 250) setCurrentState250();
                    break;
                }
                
            case 249:
                if(chebyshevDistance(path248, rc.getLocation()) <= 5) {
                    if(pathLength != 249) setCurrentState249();
                    break;
                }
                
            case 248:
                if(chebyshevDistance(path247, rc.getLocation()) <= 5) {
                    if(pathLength != 248) setCurrentState248();
                    break;
                }
                
            case 247:
                if(chebyshevDistance(path246, rc.getLocation()) <= 5) {
                    if(pathLength != 247) setCurrentState247();
                    break;
                }
                
            case 246:
                if(chebyshevDistance(path245, rc.getLocation()) <= 5) {
                    if(pathLength != 246) setCurrentState246();
                    break;
                }
                
            case 245:
                if(chebyshevDistance(path244, rc.getLocation()) <= 5) {
                    if(pathLength != 245) setCurrentState245();
                    break;
                }
                
            case 244:
                if(chebyshevDistance(path243, rc.getLocation()) <= 5) {
                    if(pathLength != 244) setCurrentState244();
                    break;
                }
                
            case 243:
                if(chebyshevDistance(path242, rc.getLocation()) <= 5) {
                    if(pathLength != 243) setCurrentState243();
                    break;
                }
                
            case 242:
                if(chebyshevDistance(path241, rc.getLocation()) <= 5) {
                    if(pathLength != 242) setCurrentState242();
                    break;
                }
                
            case 241:
                if(chebyshevDistance(path240, rc.getLocation()) <= 5) {
                    if(pathLength != 241) setCurrentState241();
                    break;
                }
                
            case 240:
                if(chebyshevDistance(path239, rc.getLocation()) <= 5) {
                    if(pathLength != 240) setCurrentState240();
                    break;
                }
                
            case 239:
                if(chebyshevDistance(path238, rc.getLocation()) <= 5) {
                    if(pathLength != 239) setCurrentState239();
                    break;
                }
                
            case 238:
                if(chebyshevDistance(path237, rc.getLocation()) <= 5) {
                    if(pathLength != 238) setCurrentState238();
                    break;
                }
                
            case 237:
                if(chebyshevDistance(path236, rc.getLocation()) <= 5) {
                    if(pathLength != 237) setCurrentState237();
                    break;
                }
                
            case 236:
                if(chebyshevDistance(path235, rc.getLocation()) <= 5) {
                    if(pathLength != 236) setCurrentState236();
                    break;
                }
                
            case 235:
                if(chebyshevDistance(path234, rc.getLocation()) <= 5) {
                    if(pathLength != 235) setCurrentState235();
                    break;
                }
                
            case 234:
                if(chebyshevDistance(path233, rc.getLocation()) <= 5) {
                    if(pathLength != 234) setCurrentState234();
                    break;
                }
                
            case 233:
                if(chebyshevDistance(path232, rc.getLocation()) <= 5) {
                    if(pathLength != 233) setCurrentState233();
                    break;
                }
                
            case 232:
                if(chebyshevDistance(path231, rc.getLocation()) <= 5) {
                    if(pathLength != 232) setCurrentState232();
                    break;
                }
                
            case 231:
                if(chebyshevDistance(path230, rc.getLocation()) <= 5) {
                    if(pathLength != 231) setCurrentState231();
                    break;
                }
                
            case 230:
                if(chebyshevDistance(path229, rc.getLocation()) <= 5) {
                    if(pathLength != 230) setCurrentState230();
                    break;
                }
                
            case 229:
                if(chebyshevDistance(path228, rc.getLocation()) <= 5) {
                    if(pathLength != 229) setCurrentState229();
                    break;
                }
                
            case 228:
                if(chebyshevDistance(path227, rc.getLocation()) <= 5) {
                    if(pathLength != 228) setCurrentState228();
                    break;
                }
                
            case 227:
                if(chebyshevDistance(path226, rc.getLocation()) <= 5) {
                    if(pathLength != 227) setCurrentState227();
                    break;
                }
                
            case 226:
                if(chebyshevDistance(path225, rc.getLocation()) <= 5) {
                    if(pathLength != 226) setCurrentState226();
                    break;
                }
                
            case 225:
                if(chebyshevDistance(path224, rc.getLocation()) <= 5) {
                    if(pathLength != 225) setCurrentState225();
                    break;
                }
                
            case 224:
                if(chebyshevDistance(path223, rc.getLocation()) <= 5) {
                    if(pathLength != 224) setCurrentState224();
                    break;
                }
                
            case 223:
                if(chebyshevDistance(path222, rc.getLocation()) <= 5) {
                    if(pathLength != 223) setCurrentState223();
                    break;
                }
                
            case 222:
                if(chebyshevDistance(path221, rc.getLocation()) <= 5) {
                    if(pathLength != 222) setCurrentState222();
                    break;
                }
                
            case 221:
                if(chebyshevDistance(path220, rc.getLocation()) <= 5) {
                    if(pathLength != 221) setCurrentState221();
                    break;
                }
                
            case 220:
                if(chebyshevDistance(path219, rc.getLocation()) <= 5) {
                    if(pathLength != 220) setCurrentState220();
                    break;
                }
                
            case 219:
                if(chebyshevDistance(path218, rc.getLocation()) <= 5) {
                    if(pathLength != 219) setCurrentState219();
                    break;
                }
                
            case 218:
                if(chebyshevDistance(path217, rc.getLocation()) <= 5) {
                    if(pathLength != 218) setCurrentState218();
                    break;
                }
                
            case 217:
                if(chebyshevDistance(path216, rc.getLocation()) <= 5) {
                    if(pathLength != 217) setCurrentState217();
                    break;
                }
                
            case 216:
                if(chebyshevDistance(path215, rc.getLocation()) <= 5) {
                    if(pathLength != 216) setCurrentState216();
                    break;
                }
                
            case 215:
                if(chebyshevDistance(path214, rc.getLocation()) <= 5) {
                    if(pathLength != 215) setCurrentState215();
                    break;
                }
                
            case 214:
                if(chebyshevDistance(path213, rc.getLocation()) <= 5) {
                    if(pathLength != 214) setCurrentState214();
                    break;
                }
                
            case 213:
                if(chebyshevDistance(path212, rc.getLocation()) <= 5) {
                    if(pathLength != 213) setCurrentState213();
                    break;
                }
                
            case 212:
                if(chebyshevDistance(path211, rc.getLocation()) <= 5) {
                    if(pathLength != 212) setCurrentState212();
                    break;
                }
                
            case 211:
                if(chebyshevDistance(path210, rc.getLocation()) <= 5) {
                    if(pathLength != 211) setCurrentState211();
                    break;
                }
                
            case 210:
                if(chebyshevDistance(path209, rc.getLocation()) <= 5) {
                    if(pathLength != 210) setCurrentState210();
                    break;
                }
                
            case 209:
                if(chebyshevDistance(path208, rc.getLocation()) <= 5) {
                    if(pathLength != 209) setCurrentState209();
                    break;
                }
                
            case 208:
                if(chebyshevDistance(path207, rc.getLocation()) <= 5) {
                    if(pathLength != 208) setCurrentState208();
                    break;
                }
                
            case 207:
                if(chebyshevDistance(path206, rc.getLocation()) <= 5) {
                    if(pathLength != 207) setCurrentState207();
                    break;
                }
                
            case 206:
                if(chebyshevDistance(path205, rc.getLocation()) <= 5) {
                    if(pathLength != 206) setCurrentState206();
                    break;
                }
                
            case 205:
                if(chebyshevDistance(path204, rc.getLocation()) <= 5) {
                    if(pathLength != 205) setCurrentState205();
                    break;
                }
                
            case 204:
                if(chebyshevDistance(path203, rc.getLocation()) <= 5) {
                    if(pathLength != 204) setCurrentState204();
                    break;
                }
                
            case 203:
                if(chebyshevDistance(path202, rc.getLocation()) <= 5) {
                    if(pathLength != 203) setCurrentState203();
                    break;
                }
                
            case 202:
                if(chebyshevDistance(path201, rc.getLocation()) <= 5) {
                    if(pathLength != 202) setCurrentState202();
                    break;
                }
                
            case 201:
                if(chebyshevDistance(path200, rc.getLocation()) <= 5) {
                    if(pathLength != 201) setCurrentState201();
                    break;
                }
                
            case 200:
                if(chebyshevDistance(path199, rc.getLocation()) <= 5) {
                    if(pathLength != 200) setCurrentState200();
                    break;
                }
                
            case 199:
                if(chebyshevDistance(path198, rc.getLocation()) <= 5) {
                    if(pathLength != 199) setCurrentState199();
                    break;
                }
                
            case 198:
                if(chebyshevDistance(path197, rc.getLocation()) <= 5) {
                    if(pathLength != 198) setCurrentState198();
                    break;
                }
                
            case 197:
                if(chebyshevDistance(path196, rc.getLocation()) <= 5) {
                    if(pathLength != 197) setCurrentState197();
                    break;
                }
                
            case 196:
                if(chebyshevDistance(path195, rc.getLocation()) <= 5) {
                    if(pathLength != 196) setCurrentState196();
                    break;
                }
                
            case 195:
                if(chebyshevDistance(path194, rc.getLocation()) <= 5) {
                    if(pathLength != 195) setCurrentState195();
                    break;
                }
                
            case 194:
                if(chebyshevDistance(path193, rc.getLocation()) <= 5) {
                    if(pathLength != 194) setCurrentState194();
                    break;
                }
                
            case 193:
                if(chebyshevDistance(path192, rc.getLocation()) <= 5) {
                    if(pathLength != 193) setCurrentState193();
                    break;
                }
                
            case 192:
                if(chebyshevDistance(path191, rc.getLocation()) <= 5) {
                    if(pathLength != 192) setCurrentState192();
                    break;
                }
                
            case 191:
                if(chebyshevDistance(path190, rc.getLocation()) <= 5) {
                    if(pathLength != 191) setCurrentState191();
                    break;
                }
                
            case 190:
                if(chebyshevDistance(path189, rc.getLocation()) <= 5) {
                    if(pathLength != 190) setCurrentState190();
                    break;
                }
                
            case 189:
                if(chebyshevDistance(path188, rc.getLocation()) <= 5) {
                    if(pathLength != 189) setCurrentState189();
                    break;
                }
                
            case 188:
                if(chebyshevDistance(path187, rc.getLocation()) <= 5) {
                    if(pathLength != 188) setCurrentState188();
                    break;
                }
                
            case 187:
                if(chebyshevDistance(path186, rc.getLocation()) <= 5) {
                    if(pathLength != 187) setCurrentState187();
                    break;
                }
                
            case 186:
                if(chebyshevDistance(path185, rc.getLocation()) <= 5) {
                    if(pathLength != 186) setCurrentState186();
                    break;
                }
                
            case 185:
                if(chebyshevDistance(path184, rc.getLocation()) <= 5) {
                    if(pathLength != 185) setCurrentState185();
                    break;
                }
                
            case 184:
                if(chebyshevDistance(path183, rc.getLocation()) <= 5) {
                    if(pathLength != 184) setCurrentState184();
                    break;
                }
                
            case 183:
                if(chebyshevDistance(path182, rc.getLocation()) <= 5) {
                    if(pathLength != 183) setCurrentState183();
                    break;
                }
                
            case 182:
                if(chebyshevDistance(path181, rc.getLocation()) <= 5) {
                    if(pathLength != 182) setCurrentState182();
                    break;
                }
                
            case 181:
                if(chebyshevDistance(path180, rc.getLocation()) <= 5) {
                    if(pathLength != 181) setCurrentState181();
                    break;
                }
                
            case 180:
                if(chebyshevDistance(path179, rc.getLocation()) <= 5) {
                    if(pathLength != 180) setCurrentState180();
                    break;
                }
                
            case 179:
                if(chebyshevDistance(path178, rc.getLocation()) <= 5) {
                    if(pathLength != 179) setCurrentState179();
                    break;
                }
                
            case 178:
                if(chebyshevDistance(path177, rc.getLocation()) <= 5) {
                    if(pathLength != 178) setCurrentState178();
                    break;
                }
                
            case 177:
                if(chebyshevDistance(path176, rc.getLocation()) <= 5) {
                    if(pathLength != 177) setCurrentState177();
                    break;
                }
                
            case 176:
                if(chebyshevDistance(path175, rc.getLocation()) <= 5) {
                    if(pathLength != 176) setCurrentState176();
                    break;
                }
                
            case 175:
                if(chebyshevDistance(path174, rc.getLocation()) <= 5) {
                    if(pathLength != 175) setCurrentState175();
                    break;
                }
                
            case 174:
                if(chebyshevDistance(path173, rc.getLocation()) <= 5) {
                    if(pathLength != 174) setCurrentState174();
                    break;
                }
                
            case 173:
                if(chebyshevDistance(path172, rc.getLocation()) <= 5) {
                    if(pathLength != 173) setCurrentState173();
                    break;
                }
                
            case 172:
                if(chebyshevDistance(path171, rc.getLocation()) <= 5) {
                    if(pathLength != 172) setCurrentState172();
                    break;
                }
                
            case 171:
                if(chebyshevDistance(path170, rc.getLocation()) <= 5) {
                    if(pathLength != 171) setCurrentState171();
                    break;
                }
                
            case 170:
                if(chebyshevDistance(path169, rc.getLocation()) <= 5) {
                    if(pathLength != 170) setCurrentState170();
                    break;
                }
                
            case 169:
                if(chebyshevDistance(path168, rc.getLocation()) <= 5) {
                    if(pathLength != 169) setCurrentState169();
                    break;
                }
                
            case 168:
                if(chebyshevDistance(path167, rc.getLocation()) <= 5) {
                    if(pathLength != 168) setCurrentState168();
                    break;
                }
                
            case 167:
                if(chebyshevDistance(path166, rc.getLocation()) <= 5) {
                    if(pathLength != 167) setCurrentState167();
                    break;
                }
                
            case 166:
                if(chebyshevDistance(path165, rc.getLocation()) <= 5) {
                    if(pathLength != 166) setCurrentState166();
                    break;
                }
                
            case 165:
                if(chebyshevDistance(path164, rc.getLocation()) <= 5) {
                    if(pathLength != 165) setCurrentState165();
                    break;
                }
                
            case 164:
                if(chebyshevDistance(path163, rc.getLocation()) <= 5) {
                    if(pathLength != 164) setCurrentState164();
                    break;
                }
                
            case 163:
                if(chebyshevDistance(path162, rc.getLocation()) <= 5) {
                    if(pathLength != 163) setCurrentState163();
                    break;
                }
                
            case 162:
                if(chebyshevDistance(path161, rc.getLocation()) <= 5) {
                    if(pathLength != 162) setCurrentState162();
                    break;
                }
                
            case 161:
                if(chebyshevDistance(path160, rc.getLocation()) <= 5) {
                    if(pathLength != 161) setCurrentState161();
                    break;
                }
                
            case 160:
                if(chebyshevDistance(path159, rc.getLocation()) <= 5) {
                    if(pathLength != 160) setCurrentState160();
                    break;
                }
                
            case 159:
                if(chebyshevDistance(path158, rc.getLocation()) <= 5) {
                    if(pathLength != 159) setCurrentState159();
                    break;
                }
                
            case 158:
                if(chebyshevDistance(path157, rc.getLocation()) <= 5) {
                    if(pathLength != 158) setCurrentState158();
                    break;
                }
                
            case 157:
                if(chebyshevDistance(path156, rc.getLocation()) <= 5) {
                    if(pathLength != 157) setCurrentState157();
                    break;
                }
                
            case 156:
                if(chebyshevDistance(path155, rc.getLocation()) <= 5) {
                    if(pathLength != 156) setCurrentState156();
                    break;
                }
                
            case 155:
                if(chebyshevDistance(path154, rc.getLocation()) <= 5) {
                    if(pathLength != 155) setCurrentState155();
                    break;
                }
                
            case 154:
                if(chebyshevDistance(path153, rc.getLocation()) <= 5) {
                    if(pathLength != 154) setCurrentState154();
                    break;
                }
                
            case 153:
                if(chebyshevDistance(path152, rc.getLocation()) <= 5) {
                    if(pathLength != 153) setCurrentState153();
                    break;
                }
                
            case 152:
                if(chebyshevDistance(path151, rc.getLocation()) <= 5) {
                    if(pathLength != 152) setCurrentState152();
                    break;
                }
                
            case 151:
                if(chebyshevDistance(path150, rc.getLocation()) <= 5) {
                    if(pathLength != 151) setCurrentState151();
                    break;
                }
                
            case 150:
                if(chebyshevDistance(path149, rc.getLocation()) <= 5) {
                    if(pathLength != 150) setCurrentState150();
                    break;
                }
                
            case 149:
                if(chebyshevDistance(path148, rc.getLocation()) <= 5) {
                    if(pathLength != 149) setCurrentState149();
                    break;
                }
                
            case 148:
                if(chebyshevDistance(path147, rc.getLocation()) <= 5) {
                    if(pathLength != 148) setCurrentState148();
                    break;
                }
                
            case 147:
                if(chebyshevDistance(path146, rc.getLocation()) <= 5) {
                    if(pathLength != 147) setCurrentState147();
                    break;
                }
                
            case 146:
                if(chebyshevDistance(path145, rc.getLocation()) <= 5) {
                    if(pathLength != 146) setCurrentState146();
                    break;
                }
                
            case 145:
                if(chebyshevDistance(path144, rc.getLocation()) <= 5) {
                    if(pathLength != 145) setCurrentState145();
                    break;
                }
                
            case 144:
                if(chebyshevDistance(path143, rc.getLocation()) <= 5) {
                    if(pathLength != 144) setCurrentState144();
                    break;
                }
                
            case 143:
                if(chebyshevDistance(path142, rc.getLocation()) <= 5) {
                    if(pathLength != 143) setCurrentState143();
                    break;
                }
                
            case 142:
                if(chebyshevDistance(path141, rc.getLocation()) <= 5) {
                    if(pathLength != 142) setCurrentState142();
                    break;
                }
                
            case 141:
                if(chebyshevDistance(path140, rc.getLocation()) <= 5) {
                    if(pathLength != 141) setCurrentState141();
                    break;
                }
                
            case 140:
                if(chebyshevDistance(path139, rc.getLocation()) <= 5) {
                    if(pathLength != 140) setCurrentState140();
                    break;
                }
                
            case 139:
                if(chebyshevDistance(path138, rc.getLocation()) <= 5) {
                    if(pathLength != 139) setCurrentState139();
                    break;
                }
                
            case 138:
                if(chebyshevDistance(path137, rc.getLocation()) <= 5) {
                    if(pathLength != 138) setCurrentState138();
                    break;
                }
                
            case 137:
                if(chebyshevDistance(path136, rc.getLocation()) <= 5) {
                    if(pathLength != 137) setCurrentState137();
                    break;
                }
                
            case 136:
                if(chebyshevDistance(path135, rc.getLocation()) <= 5) {
                    if(pathLength != 136) setCurrentState136();
                    break;
                }
                
            case 135:
                if(chebyshevDistance(path134, rc.getLocation()) <= 5) {
                    if(pathLength != 135) setCurrentState135();
                    break;
                }
                
            case 134:
                if(chebyshevDistance(path133, rc.getLocation()) <= 5) {
                    if(pathLength != 134) setCurrentState134();
                    break;
                }
                
            case 133:
                if(chebyshevDistance(path132, rc.getLocation()) <= 5) {
                    if(pathLength != 133) setCurrentState133();
                    break;
                }
                
            case 132:
                if(chebyshevDistance(path131, rc.getLocation()) <= 5) {
                    if(pathLength != 132) setCurrentState132();
                    break;
                }
                
            case 131:
                if(chebyshevDistance(path130, rc.getLocation()) <= 5) {
                    if(pathLength != 131) setCurrentState131();
                    break;
                }
                
            case 130:
                if(chebyshevDistance(path129, rc.getLocation()) <= 5) {
                    if(pathLength != 130) setCurrentState130();
                    break;
                }
                
            case 129:
                if(chebyshevDistance(path128, rc.getLocation()) <= 5) {
                    if(pathLength != 129) setCurrentState129();
                    break;
                }
                
            case 128:
                if(chebyshevDistance(path127, rc.getLocation()) <= 5) {
                    if(pathLength != 128) setCurrentState128();
                    break;
                }
                
            case 127:
                if(chebyshevDistance(path126, rc.getLocation()) <= 5) {
                    if(pathLength != 127) setCurrentState127();
                    break;
                }
                
            case 126:
                if(chebyshevDistance(path125, rc.getLocation()) <= 5) {
                    if(pathLength != 126) setCurrentState126();
                    break;
                }
                
            case 125:
                if(chebyshevDistance(path124, rc.getLocation()) <= 5) {
                    if(pathLength != 125) setCurrentState125();
                    break;
                }
                
            case 124:
                if(chebyshevDistance(path123, rc.getLocation()) <= 5) {
                    if(pathLength != 124) setCurrentState124();
                    break;
                }
                
            case 123:
                if(chebyshevDistance(path122, rc.getLocation()) <= 5) {
                    if(pathLength != 123) setCurrentState123();
                    break;
                }
                
            case 122:
                if(chebyshevDistance(path121, rc.getLocation()) <= 5) {
                    if(pathLength != 122) setCurrentState122();
                    break;
                }
                
            case 121:
                if(chebyshevDistance(path120, rc.getLocation()) <= 5) {
                    if(pathLength != 121) setCurrentState121();
                    break;
                }
                
            case 120:
                if(chebyshevDistance(path119, rc.getLocation()) <= 5) {
                    if(pathLength != 120) setCurrentState120();
                    break;
                }
                
            case 119:
                if(chebyshevDistance(path118, rc.getLocation()) <= 5) {
                    if(pathLength != 119) setCurrentState119();
                    break;
                }
                
            case 118:
                if(chebyshevDistance(path117, rc.getLocation()) <= 5) {
                    if(pathLength != 118) setCurrentState118();
                    break;
                }
                
            case 117:
                if(chebyshevDistance(path116, rc.getLocation()) <= 5) {
                    if(pathLength != 117) setCurrentState117();
                    break;
                }
                
            case 116:
                if(chebyshevDistance(path115, rc.getLocation()) <= 5) {
                    if(pathLength != 116) setCurrentState116();
                    break;
                }
                
            case 115:
                if(chebyshevDistance(path114, rc.getLocation()) <= 5) {
                    if(pathLength != 115) setCurrentState115();
                    break;
                }
                
            case 114:
                if(chebyshevDistance(path113, rc.getLocation()) <= 5) {
                    if(pathLength != 114) setCurrentState114();
                    break;
                }
                
            case 113:
                if(chebyshevDistance(path112, rc.getLocation()) <= 5) {
                    if(pathLength != 113) setCurrentState113();
                    break;
                }
                
            case 112:
                if(chebyshevDistance(path111, rc.getLocation()) <= 5) {
                    if(pathLength != 112) setCurrentState112();
                    break;
                }
                
            case 111:
                if(chebyshevDistance(path110, rc.getLocation()) <= 5) {
                    if(pathLength != 111) setCurrentState111();
                    break;
                }
                
            case 110:
                if(chebyshevDistance(path109, rc.getLocation()) <= 5) {
                    if(pathLength != 110) setCurrentState110();
                    break;
                }
                
            case 109:
                if(chebyshevDistance(path108, rc.getLocation()) <= 5) {
                    if(pathLength != 109) setCurrentState109();
                    break;
                }
                
            case 108:
                if(chebyshevDistance(path107, rc.getLocation()) <= 5) {
                    if(pathLength != 108) setCurrentState108();
                    break;
                }
                
            case 107:
                if(chebyshevDistance(path106, rc.getLocation()) <= 5) {
                    if(pathLength != 107) setCurrentState107();
                    break;
                }
                
            case 106:
                if(chebyshevDistance(path105, rc.getLocation()) <= 5) {
                    if(pathLength != 106) setCurrentState106();
                    break;
                }
                
            case 105:
                if(chebyshevDistance(path104, rc.getLocation()) <= 5) {
                    if(pathLength != 105) setCurrentState105();
                    break;
                }
                
            case 104:
                if(chebyshevDistance(path103, rc.getLocation()) <= 5) {
                    if(pathLength != 104) setCurrentState104();
                    break;
                }
                
            case 103:
                if(chebyshevDistance(path102, rc.getLocation()) <= 5) {
                    if(pathLength != 103) setCurrentState103();
                    break;
                }
                
            case 102:
                if(chebyshevDistance(path101, rc.getLocation()) <= 5) {
                    if(pathLength != 102) setCurrentState102();
                    break;
                }
                
            case 101:
                if(chebyshevDistance(path100, rc.getLocation()) <= 5) {
                    if(pathLength != 101) setCurrentState101();
                    break;
                }
                
            case 100:
                if(chebyshevDistance(path99, rc.getLocation()) <= 5) {
                    if(pathLength != 100) setCurrentState100();
                    break;
                }
                
            case 99:
                if(chebyshevDistance(path98, rc.getLocation()) <= 5) {
                    if(pathLength != 99) setCurrentState99();
                    break;
                }
                
            case 98:
                if(chebyshevDistance(path97, rc.getLocation()) <= 5) {
                    if(pathLength != 98) setCurrentState98();
                    break;
                }
                
            case 97:
                if(chebyshevDistance(path96, rc.getLocation()) <= 5) {
                    if(pathLength != 97) setCurrentState97();
                    break;
                }
                
            case 96:
                if(chebyshevDistance(path95, rc.getLocation()) <= 5) {
                    if(pathLength != 96) setCurrentState96();
                    break;
                }
                
            case 95:
                if(chebyshevDistance(path94, rc.getLocation()) <= 5) {
                    if(pathLength != 95) setCurrentState95();
                    break;
                }
                
            case 94:
                if(chebyshevDistance(path93, rc.getLocation()) <= 5) {
                    if(pathLength != 94) setCurrentState94();
                    break;
                }
                
            case 93:
                if(chebyshevDistance(path92, rc.getLocation()) <= 5) {
                    if(pathLength != 93) setCurrentState93();
                    break;
                }
                
            case 92:
                if(chebyshevDistance(path91, rc.getLocation()) <= 5) {
                    if(pathLength != 92) setCurrentState92();
                    break;
                }
                
            case 91:
                if(chebyshevDistance(path90, rc.getLocation()) <= 5) {
                    if(pathLength != 91) setCurrentState91();
                    break;
                }
                
            case 90:
                if(chebyshevDistance(path89, rc.getLocation()) <= 5) {
                    if(pathLength != 90) setCurrentState90();
                    break;
                }
                
            case 89:
                if(chebyshevDistance(path88, rc.getLocation()) <= 5) {
                    if(pathLength != 89) setCurrentState89();
                    break;
                }
                
            case 88:
                if(chebyshevDistance(path87, rc.getLocation()) <= 5) {
                    if(pathLength != 88) setCurrentState88();
                    break;
                }
                
            case 87:
                if(chebyshevDistance(path86, rc.getLocation()) <= 5) {
                    if(pathLength != 87) setCurrentState87();
                    break;
                }
                
            case 86:
                if(chebyshevDistance(path85, rc.getLocation()) <= 5) {
                    if(pathLength != 86) setCurrentState86();
                    break;
                }
                
            case 85:
                if(chebyshevDistance(path84, rc.getLocation()) <= 5) {
                    if(pathLength != 85) setCurrentState85();
                    break;
                }
                
            case 84:
                if(chebyshevDistance(path83, rc.getLocation()) <= 5) {
                    if(pathLength != 84) setCurrentState84();
                    break;
                }
                
            case 83:
                if(chebyshevDistance(path82, rc.getLocation()) <= 5) {
                    if(pathLength != 83) setCurrentState83();
                    break;
                }
                
            case 82:
                if(chebyshevDistance(path81, rc.getLocation()) <= 5) {
                    if(pathLength != 82) setCurrentState82();
                    break;
                }
                
            case 81:
                if(chebyshevDistance(path80, rc.getLocation()) <= 5) {
                    if(pathLength != 81) setCurrentState81();
                    break;
                }
                
            case 80:
                if(chebyshevDistance(path79, rc.getLocation()) <= 5) {
                    if(pathLength != 80) setCurrentState80();
                    break;
                }
                
            case 79:
                if(chebyshevDistance(path78, rc.getLocation()) <= 5) {
                    if(pathLength != 79) setCurrentState79();
                    break;
                }
                
            case 78:
                if(chebyshevDistance(path77, rc.getLocation()) <= 5) {
                    if(pathLength != 78) setCurrentState78();
                    break;
                }
                
            case 77:
                if(chebyshevDistance(path76, rc.getLocation()) <= 5) {
                    if(pathLength != 77) setCurrentState77();
                    break;
                }
                
            case 76:
                if(chebyshevDistance(path75, rc.getLocation()) <= 5) {
                    if(pathLength != 76) setCurrentState76();
                    break;
                }
                
            case 75:
                if(chebyshevDistance(path74, rc.getLocation()) <= 5) {
                    if(pathLength != 75) setCurrentState75();
                    break;
                }
                
            case 74:
                if(chebyshevDistance(path73, rc.getLocation()) <= 5) {
                    if(pathLength != 74) setCurrentState74();
                    break;
                }
                
            case 73:
                if(chebyshevDistance(path72, rc.getLocation()) <= 5) {
                    if(pathLength != 73) setCurrentState73();
                    break;
                }
                
            case 72:
                if(chebyshevDistance(path71, rc.getLocation()) <= 5) {
                    if(pathLength != 72) setCurrentState72();
                    break;
                }
                
            case 71:
                if(chebyshevDistance(path70, rc.getLocation()) <= 5) {
                    if(pathLength != 71) setCurrentState71();
                    break;
                }
                
            case 70:
                if(chebyshevDistance(path69, rc.getLocation()) <= 5) {
                    if(pathLength != 70) setCurrentState70();
                    break;
                }
                
            case 69:
                if(chebyshevDistance(path68, rc.getLocation()) <= 5) {
                    if(pathLength != 69) setCurrentState69();
                    break;
                }
                
            case 68:
                if(chebyshevDistance(path67, rc.getLocation()) <= 5) {
                    if(pathLength != 68) setCurrentState68();
                    break;
                }
                
            case 67:
                if(chebyshevDistance(path66, rc.getLocation()) <= 5) {
                    if(pathLength != 67) setCurrentState67();
                    break;
                }
                
            case 66:
                if(chebyshevDistance(path65, rc.getLocation()) <= 5) {
                    if(pathLength != 66) setCurrentState66();
                    break;
                }
                
            case 65:
                if(chebyshevDistance(path64, rc.getLocation()) <= 5) {
                    if(pathLength != 65) setCurrentState65();
                    break;
                }
                
            case 64:
                if(chebyshevDistance(path63, rc.getLocation()) <= 5) {
                    if(pathLength != 64) setCurrentState64();
                    break;
                }
                
            case 63:
                if(chebyshevDistance(path62, rc.getLocation()) <= 5) {
                    if(pathLength != 63) setCurrentState63();
                    break;
                }
                
            case 62:
                if(chebyshevDistance(path61, rc.getLocation()) <= 5) {
                    if(pathLength != 62) setCurrentState62();
                    break;
                }
                
            case 61:
                if(chebyshevDistance(path60, rc.getLocation()) <= 5) {
                    if(pathLength != 61) setCurrentState61();
                    break;
                }
                
            case 60:
                if(chebyshevDistance(path59, rc.getLocation()) <= 5) {
                    if(pathLength != 60) setCurrentState60();
                    break;
                }
                
            case 59:
                if(chebyshevDistance(path58, rc.getLocation()) <= 5) {
                    if(pathLength != 59) setCurrentState59();
                    break;
                }
                
            case 58:
                if(chebyshevDistance(path57, rc.getLocation()) <= 5) {
                    if(pathLength != 58) setCurrentState58();
                    break;
                }
                
            case 57:
                if(chebyshevDistance(path56, rc.getLocation()) <= 5) {
                    if(pathLength != 57) setCurrentState57();
                    break;
                }
                
            case 56:
                if(chebyshevDistance(path55, rc.getLocation()) <= 5) {
                    if(pathLength != 56) setCurrentState56();
                    break;
                }
                
            case 55:
                if(chebyshevDistance(path54, rc.getLocation()) <= 5) {
                    if(pathLength != 55) setCurrentState55();
                    break;
                }
                
            case 54:
                if(chebyshevDistance(path53, rc.getLocation()) <= 5) {
                    if(pathLength != 54) setCurrentState54();
                    break;
                }
                
            case 53:
                if(chebyshevDistance(path52, rc.getLocation()) <= 5) {
                    if(pathLength != 53) setCurrentState53();
                    break;
                }
                
            case 52:
                if(chebyshevDistance(path51, rc.getLocation()) <= 5) {
                    if(pathLength != 52) setCurrentState52();
                    break;
                }
                
            case 51:
                if(chebyshevDistance(path50, rc.getLocation()) <= 5) {
                    if(pathLength != 51) setCurrentState51();
                    break;
                }
                
            case 50:
                if(chebyshevDistance(path49, rc.getLocation()) <= 5) {
                    if(pathLength != 50) setCurrentState50();
                    break;
                }
                
            case 49:
                if(chebyshevDistance(path48, rc.getLocation()) <= 5) {
                    if(pathLength != 49) setCurrentState49();
                    break;
                }
                
            case 48:
                if(chebyshevDistance(path47, rc.getLocation()) <= 5) {
                    if(pathLength != 48) setCurrentState48();
                    break;
                }
                
            case 47:
                if(chebyshevDistance(path46, rc.getLocation()) <= 5) {
                    if(pathLength != 47) setCurrentState47();
                    break;
                }
                
            case 46:
                if(chebyshevDistance(path45, rc.getLocation()) <= 5) {
                    if(pathLength != 46) setCurrentState46();
                    break;
                }
                
            case 45:
                if(chebyshevDistance(path44, rc.getLocation()) <= 5) {
                    if(pathLength != 45) setCurrentState45();
                    break;
                }
                
            case 44:
                if(chebyshevDistance(path43, rc.getLocation()) <= 5) {
                    if(pathLength != 44) setCurrentState44();
                    break;
                }
                
            case 43:
                if(chebyshevDistance(path42, rc.getLocation()) <= 5) {
                    if(pathLength != 43) setCurrentState43();
                    break;
                }
                
            case 42:
                if(chebyshevDistance(path41, rc.getLocation()) <= 5) {
                    if(pathLength != 42) setCurrentState42();
                    break;
                }
                
            case 41:
                if(chebyshevDistance(path40, rc.getLocation()) <= 5) {
                    if(pathLength != 41) setCurrentState41();
                    break;
                }
                
            case 40:
                if(chebyshevDistance(path39, rc.getLocation()) <= 5) {
                    if(pathLength != 40) setCurrentState40();
                    break;
                }
                
            case 39:
                if(chebyshevDistance(path38, rc.getLocation()) <= 5) {
                    if(pathLength != 39) setCurrentState39();
                    break;
                }
                
            case 38:
                if(chebyshevDistance(path37, rc.getLocation()) <= 5) {
                    if(pathLength != 38) setCurrentState38();
                    break;
                }
                
            case 37:
                if(chebyshevDistance(path36, rc.getLocation()) <= 5) {
                    if(pathLength != 37) setCurrentState37();
                    break;
                }
                
            case 36:
                if(chebyshevDistance(path35, rc.getLocation()) <= 5) {
                    if(pathLength != 36) setCurrentState36();
                    break;
                }
                
            case 35:
                if(chebyshevDistance(path34, rc.getLocation()) <= 5) {
                    if(pathLength != 35) setCurrentState35();
                    break;
                }
                
            case 34:
                if(chebyshevDistance(path33, rc.getLocation()) <= 5) {
                    if(pathLength != 34) setCurrentState34();
                    break;
                }
                
            case 33:
                if(chebyshevDistance(path32, rc.getLocation()) <= 5) {
                    if(pathLength != 33) setCurrentState33();
                    break;
                }
                
            case 32:
                if(chebyshevDistance(path31, rc.getLocation()) <= 5) {
                    if(pathLength != 32) setCurrentState32();
                    break;
                }
                
            case 31:
                if(chebyshevDistance(path30, rc.getLocation()) <= 5) {
                    if(pathLength != 31) setCurrentState31();
                    break;
                }
                
            case 30:
                if(chebyshevDistance(path29, rc.getLocation()) <= 5) {
                    if(pathLength != 30) setCurrentState30();
                    break;
                }
                
            case 29:
                if(chebyshevDistance(path28, rc.getLocation()) <= 5) {
                    if(pathLength != 29) setCurrentState29();
                    break;
                }
                
            case 28:
                if(chebyshevDistance(path27, rc.getLocation()) <= 5) {
                    if(pathLength != 28) setCurrentState28();
                    break;
                }
                
            case 27:
                if(chebyshevDistance(path26, rc.getLocation()) <= 5) {
                    if(pathLength != 27) setCurrentState27();
                    break;
                }
                
            case 26:
                if(chebyshevDistance(path25, rc.getLocation()) <= 5) {
                    if(pathLength != 26) setCurrentState26();
                    break;
                }
                
            case 25:
                if(chebyshevDistance(path24, rc.getLocation()) <= 5) {
                    if(pathLength != 25) setCurrentState25();
                    break;
                }
                
            case 24:
                if(chebyshevDistance(path23, rc.getLocation()) <= 5) {
                    if(pathLength != 24) setCurrentState24();
                    break;
                }
                
            case 23:
                if(chebyshevDistance(path22, rc.getLocation()) <= 5) {
                    if(pathLength != 23) setCurrentState23();
                    break;
                }
                
            case 22:
                if(chebyshevDistance(path21, rc.getLocation()) <= 5) {
                    if(pathLength != 22) setCurrentState22();
                    break;
                }
                
            case 21:
                if(chebyshevDistance(path20, rc.getLocation()) <= 5) {
                    if(pathLength != 21) setCurrentState21();
                    break;
                }
                
            case 20:
                if(chebyshevDistance(path19, rc.getLocation()) <= 5) {
                    if(pathLength != 20) setCurrentState20();
                    break;
                }
                
            case 19:
                if(chebyshevDistance(path18, rc.getLocation()) <= 5) {
                    if(pathLength != 19) setCurrentState19();
                    break;
                }
                
            case 18:
                if(chebyshevDistance(path17, rc.getLocation()) <= 5) {
                    if(pathLength != 18) setCurrentState18();
                    break;
                }
                
            case 17:
                if(chebyshevDistance(path16, rc.getLocation()) <= 5) {
                    if(pathLength != 17) setCurrentState17();
                    break;
                }
                
            case 16:
                if(chebyshevDistance(path15, rc.getLocation()) <= 5) {
                    if(pathLength != 16) setCurrentState16();
                    break;
                }
                
            case 15:
                if(chebyshevDistance(path14, rc.getLocation()) <= 5) {
                    if(pathLength != 15) setCurrentState15();
                    break;
                }
                
            case 14:
                if(chebyshevDistance(path13, rc.getLocation()) <= 5) {
                    if(pathLength != 14) setCurrentState14();
                    break;
                }
                
            case 13:
                if(chebyshevDistance(path12, rc.getLocation()) <= 5) {
                    if(pathLength != 13) setCurrentState13();
                    break;
                }
                
            case 12:
                if(chebyshevDistance(path11, rc.getLocation()) <= 5) {
                    if(pathLength != 12) setCurrentState12();
                    break;
                }
                
            case 11:
                if(chebyshevDistance(path10, rc.getLocation()) <= 5) {
                    if(pathLength != 11) setCurrentState11();
                    break;
                }
                
            case 10:
                if(chebyshevDistance(path9, rc.getLocation()) <= 5) {
                    if(pathLength != 10) setCurrentState10();
                    break;
                }
                
            case 9:
                if(chebyshevDistance(path8, rc.getLocation()) <= 5) {
                    if(pathLength != 9) setCurrentState9();
                    break;
                }
                
            case 8:
                if(chebyshevDistance(path7, rc.getLocation()) <= 5) {
                    if(pathLength != 8) setCurrentState8();
                    break;
                }
                
            case 7:
                if(chebyshevDistance(path6, rc.getLocation()) <= 5) {
                    if(pathLength != 7) setCurrentState7();
                    break;
                }
                
            case 6:
                if(chebyshevDistance(path5, rc.getLocation()) <= 5) {
                    if(pathLength != 6) setCurrentState6();
                    break;
                }
                
            case 5:
                if(chebyshevDistance(path4, rc.getLocation()) <= 5) {
                    if(pathLength != 5) setCurrentState5();
                    break;
                }
                
            case 4:
                if(chebyshevDistance(path3, rc.getLocation()) <= 5) {
                    if(pathLength != 4) setCurrentState4();
                    break;
                }
                
            case 3:
                if(chebyshevDistance(path2, rc.getLocation()) <= 5) {
                    if(pathLength != 3) setCurrentState3();
                    break;
                }
                
            case 2:
                if(chebyshevDistance(path1, rc.getLocation()) <= 5) {
                    if(pathLength != 2) setCurrentState2();
                    break;
                }
                
            case 1:
                if(chebyshevDistance(path0, rc.getLocation()) <= 5) {
                    if(pathLength != 1) setCurrentState1();
                    break;
                }
                
            default:
                assert(pathLength <= 500);
                reset();
        }
    }

    void appendCurrentState() {
        switch (pathLength) {
            
            case 499 -> {
                
                if (!path498.equals(pos))
                    
                {
                    appendCurrentState499();
                }
            }
            
            case 498 -> {
                
                if (!path497.equals(pos))
                    
                {
                    appendCurrentState498();
                }
            }
            
            case 497 -> {
                
                if (!path496.equals(pos))
                    
                {
                    appendCurrentState497();
                }
            }
            
            case 496 -> {
                
                if (!path495.equals(pos))
                    
                {
                    appendCurrentState496();
                }
            }
            
            case 495 -> {
                
                if (!path494.equals(pos))
                    
                {
                    appendCurrentState495();
                }
            }
            
            case 494 -> {
                
                if (!path493.equals(pos))
                    
                {
                    appendCurrentState494();
                }
            }
            
            case 493 -> {
                
                if (!path492.equals(pos))
                    
                {
                    appendCurrentState493();
                }
            }
            
            case 492 -> {
                
                if (!path491.equals(pos))
                    
                {
                    appendCurrentState492();
                }
            }
            
            case 491 -> {
                
                if (!path490.equals(pos))
                    
                {
                    appendCurrentState491();
                }
            }
            
            case 490 -> {
                
                if (!path489.equals(pos))
                    
                {
                    appendCurrentState490();
                }
            }
            
            case 489 -> {
                
                if (!path488.equals(pos))
                    
                {
                    appendCurrentState489();
                }
            }
            
            case 488 -> {
                
                if (!path487.equals(pos))
                    
                {
                    appendCurrentState488();
                }
            }
            
            case 487 -> {
                
                if (!path486.equals(pos))
                    
                {
                    appendCurrentState487();
                }
            }
            
            case 486 -> {
                
                if (!path485.equals(pos))
                    
                {
                    appendCurrentState486();
                }
            }
            
            case 485 -> {
                
                if (!path484.equals(pos))
                    
                {
                    appendCurrentState485();
                }
            }
            
            case 484 -> {
                
                if (!path483.equals(pos))
                    
                {
                    appendCurrentState484();
                }
            }
            
            case 483 -> {
                
                if (!path482.equals(pos))
                    
                {
                    appendCurrentState483();
                }
            }
            
            case 482 -> {
                
                if (!path481.equals(pos))
                    
                {
                    appendCurrentState482();
                }
            }
            
            case 481 -> {
                
                if (!path480.equals(pos))
                    
                {
                    appendCurrentState481();
                }
            }
            
            case 480 -> {
                
                if (!path479.equals(pos))
                    
                {
                    appendCurrentState480();
                }
            }
            
            case 479 -> {
                
                if (!path478.equals(pos))
                    
                {
                    appendCurrentState479();
                }
            }
            
            case 478 -> {
                
                if (!path477.equals(pos))
                    
                {
                    appendCurrentState478();
                }
            }
            
            case 477 -> {
                
                if (!path476.equals(pos))
                    
                {
                    appendCurrentState477();
                }
            }
            
            case 476 -> {
                
                if (!path475.equals(pos))
                    
                {
                    appendCurrentState476();
                }
            }
            
            case 475 -> {
                
                if (!path474.equals(pos))
                    
                {
                    appendCurrentState475();
                }
            }
            
            case 474 -> {
                
                if (!path473.equals(pos))
                    
                {
                    appendCurrentState474();
                }
            }
            
            case 473 -> {
                
                if (!path472.equals(pos))
                    
                {
                    appendCurrentState473();
                }
            }
            
            case 472 -> {
                
                if (!path471.equals(pos))
                    
                {
                    appendCurrentState472();
                }
            }
            
            case 471 -> {
                
                if (!path470.equals(pos))
                    
                {
                    appendCurrentState471();
                }
            }
            
            case 470 -> {
                
                if (!path469.equals(pos))
                    
                {
                    appendCurrentState470();
                }
            }
            
            case 469 -> {
                
                if (!path468.equals(pos))
                    
                {
                    appendCurrentState469();
                }
            }
            
            case 468 -> {
                
                if (!path467.equals(pos))
                    
                {
                    appendCurrentState468();
                }
            }
            
            case 467 -> {
                
                if (!path466.equals(pos))
                    
                {
                    appendCurrentState467();
                }
            }
            
            case 466 -> {
                
                if (!path465.equals(pos))
                    
                {
                    appendCurrentState466();
                }
            }
            
            case 465 -> {
                
                if (!path464.equals(pos))
                    
                {
                    appendCurrentState465();
                }
            }
            
            case 464 -> {
                
                if (!path463.equals(pos))
                    
                {
                    appendCurrentState464();
                }
            }
            
            case 463 -> {
                
                if (!path462.equals(pos))
                    
                {
                    appendCurrentState463();
                }
            }
            
            case 462 -> {
                
                if (!path461.equals(pos))
                    
                {
                    appendCurrentState462();
                }
            }
            
            case 461 -> {
                
                if (!path460.equals(pos))
                    
                {
                    appendCurrentState461();
                }
            }
            
            case 460 -> {
                
                if (!path459.equals(pos))
                    
                {
                    appendCurrentState460();
                }
            }
            
            case 459 -> {
                
                if (!path458.equals(pos))
                    
                {
                    appendCurrentState459();
                }
            }
            
            case 458 -> {
                
                if (!path457.equals(pos))
                    
                {
                    appendCurrentState458();
                }
            }
            
            case 457 -> {
                
                if (!path456.equals(pos))
                    
                {
                    appendCurrentState457();
                }
            }
            
            case 456 -> {
                
                if (!path455.equals(pos))
                    
                {
                    appendCurrentState456();
                }
            }
            
            case 455 -> {
                
                if (!path454.equals(pos))
                    
                {
                    appendCurrentState455();
                }
            }
            
            case 454 -> {
                
                if (!path453.equals(pos))
                    
                {
                    appendCurrentState454();
                }
            }
            
            case 453 -> {
                
                if (!path452.equals(pos))
                    
                {
                    appendCurrentState453();
                }
            }
            
            case 452 -> {
                
                if (!path451.equals(pos))
                    
                {
                    appendCurrentState452();
                }
            }
            
            case 451 -> {
                
                if (!path450.equals(pos))
                    
                {
                    appendCurrentState451();
                }
            }
            
            case 450 -> {
                
                if (!path449.equals(pos))
                    
                {
                    appendCurrentState450();
                }
            }
            
            case 449 -> {
                
                if (!path448.equals(pos))
                    
                {
                    appendCurrentState449();
                }
            }
            
            case 448 -> {
                
                if (!path447.equals(pos))
                    
                {
                    appendCurrentState448();
                }
            }
            
            case 447 -> {
                
                if (!path446.equals(pos))
                    
                {
                    appendCurrentState447();
                }
            }
            
            case 446 -> {
                
                if (!path445.equals(pos))
                    
                {
                    appendCurrentState446();
                }
            }
            
            case 445 -> {
                
                if (!path444.equals(pos))
                    
                {
                    appendCurrentState445();
                }
            }
            
            case 444 -> {
                
                if (!path443.equals(pos))
                    
                {
                    appendCurrentState444();
                }
            }
            
            case 443 -> {
                
                if (!path442.equals(pos))
                    
                {
                    appendCurrentState443();
                }
            }
            
            case 442 -> {
                
                if (!path441.equals(pos))
                    
                {
                    appendCurrentState442();
                }
            }
            
            case 441 -> {
                
                if (!path440.equals(pos))
                    
                {
                    appendCurrentState441();
                }
            }
            
            case 440 -> {
                
                if (!path439.equals(pos))
                    
                {
                    appendCurrentState440();
                }
            }
            
            case 439 -> {
                
                if (!path438.equals(pos))
                    
                {
                    appendCurrentState439();
                }
            }
            
            case 438 -> {
                
                if (!path437.equals(pos))
                    
                {
                    appendCurrentState438();
                }
            }
            
            case 437 -> {
                
                if (!path436.equals(pos))
                    
                {
                    appendCurrentState437();
                }
            }
            
            case 436 -> {
                
                if (!path435.equals(pos))
                    
                {
                    appendCurrentState436();
                }
            }
            
            case 435 -> {
                
                if (!path434.equals(pos))
                    
                {
                    appendCurrentState435();
                }
            }
            
            case 434 -> {
                
                if (!path433.equals(pos))
                    
                {
                    appendCurrentState434();
                }
            }
            
            case 433 -> {
                
                if (!path432.equals(pos))
                    
                {
                    appendCurrentState433();
                }
            }
            
            case 432 -> {
                
                if (!path431.equals(pos))
                    
                {
                    appendCurrentState432();
                }
            }
            
            case 431 -> {
                
                if (!path430.equals(pos))
                    
                {
                    appendCurrentState431();
                }
            }
            
            case 430 -> {
                
                if (!path429.equals(pos))
                    
                {
                    appendCurrentState430();
                }
            }
            
            case 429 -> {
                
                if (!path428.equals(pos))
                    
                {
                    appendCurrentState429();
                }
            }
            
            case 428 -> {
                
                if (!path427.equals(pos))
                    
                {
                    appendCurrentState428();
                }
            }
            
            case 427 -> {
                
                if (!path426.equals(pos))
                    
                {
                    appendCurrentState427();
                }
            }
            
            case 426 -> {
                
                if (!path425.equals(pos))
                    
                {
                    appendCurrentState426();
                }
            }
            
            case 425 -> {
                
                if (!path424.equals(pos))
                    
                {
                    appendCurrentState425();
                }
            }
            
            case 424 -> {
                
                if (!path423.equals(pos))
                    
                {
                    appendCurrentState424();
                }
            }
            
            case 423 -> {
                
                if (!path422.equals(pos))
                    
                {
                    appendCurrentState423();
                }
            }
            
            case 422 -> {
                
                if (!path421.equals(pos))
                    
                {
                    appendCurrentState422();
                }
            }
            
            case 421 -> {
                
                if (!path420.equals(pos))
                    
                {
                    appendCurrentState421();
                }
            }
            
            case 420 -> {
                
                if (!path419.equals(pos))
                    
                {
                    appendCurrentState420();
                }
            }
            
            case 419 -> {
                
                if (!path418.equals(pos))
                    
                {
                    appendCurrentState419();
                }
            }
            
            case 418 -> {
                
                if (!path417.equals(pos))
                    
                {
                    appendCurrentState418();
                }
            }
            
            case 417 -> {
                
                if (!path416.equals(pos))
                    
                {
                    appendCurrentState417();
                }
            }
            
            case 416 -> {
                
                if (!path415.equals(pos))
                    
                {
                    appendCurrentState416();
                }
            }
            
            case 415 -> {
                
                if (!path414.equals(pos))
                    
                {
                    appendCurrentState415();
                }
            }
            
            case 414 -> {
                
                if (!path413.equals(pos))
                    
                {
                    appendCurrentState414();
                }
            }
            
            case 413 -> {
                
                if (!path412.equals(pos))
                    
                {
                    appendCurrentState413();
                }
            }
            
            case 412 -> {
                
                if (!path411.equals(pos))
                    
                {
                    appendCurrentState412();
                }
            }
            
            case 411 -> {
                
                if (!path410.equals(pos))
                    
                {
                    appendCurrentState411();
                }
            }
            
            case 410 -> {
                
                if (!path409.equals(pos))
                    
                {
                    appendCurrentState410();
                }
            }
            
            case 409 -> {
                
                if (!path408.equals(pos))
                    
                {
                    appendCurrentState409();
                }
            }
            
            case 408 -> {
                
                if (!path407.equals(pos))
                    
                {
                    appendCurrentState408();
                }
            }
            
            case 407 -> {
                
                if (!path406.equals(pos))
                    
                {
                    appendCurrentState407();
                }
            }
            
            case 406 -> {
                
                if (!path405.equals(pos))
                    
                {
                    appendCurrentState406();
                }
            }
            
            case 405 -> {
                
                if (!path404.equals(pos))
                    
                {
                    appendCurrentState405();
                }
            }
            
            case 404 -> {
                
                if (!path403.equals(pos))
                    
                {
                    appendCurrentState404();
                }
            }
            
            case 403 -> {
                
                if (!path402.equals(pos))
                    
                {
                    appendCurrentState403();
                }
            }
            
            case 402 -> {
                
                if (!path401.equals(pos))
                    
                {
                    appendCurrentState402();
                }
            }
            
            case 401 -> {
                
                if (!path400.equals(pos))
                    
                {
                    appendCurrentState401();
                }
            }
            
            case 400 -> {
                
                if (!path399.equals(pos))
                    
                {
                    appendCurrentState400();
                }
            }
            
            case 399 -> {
                
                if (!path398.equals(pos))
                    
                {
                    appendCurrentState399();
                }
            }
            
            case 398 -> {
                
                if (!path397.equals(pos))
                    
                {
                    appendCurrentState398();
                }
            }
            
            case 397 -> {
                
                if (!path396.equals(pos))
                    
                {
                    appendCurrentState397();
                }
            }
            
            case 396 -> {
                
                if (!path395.equals(pos))
                    
                {
                    appendCurrentState396();
                }
            }
            
            case 395 -> {
                
                if (!path394.equals(pos))
                    
                {
                    appendCurrentState395();
                }
            }
            
            case 394 -> {
                
                if (!path393.equals(pos))
                    
                {
                    appendCurrentState394();
                }
            }
            
            case 393 -> {
                
                if (!path392.equals(pos))
                    
                {
                    appendCurrentState393();
                }
            }
            
            case 392 -> {
                
                if (!path391.equals(pos))
                    
                {
                    appendCurrentState392();
                }
            }
            
            case 391 -> {
                
                if (!path390.equals(pos))
                    
                {
                    appendCurrentState391();
                }
            }
            
            case 390 -> {
                
                if (!path389.equals(pos))
                    
                {
                    appendCurrentState390();
                }
            }
            
            case 389 -> {
                
                if (!path388.equals(pos))
                    
                {
                    appendCurrentState389();
                }
            }
            
            case 388 -> {
                
                if (!path387.equals(pos))
                    
                {
                    appendCurrentState388();
                }
            }
            
            case 387 -> {
                
                if (!path386.equals(pos))
                    
                {
                    appendCurrentState387();
                }
            }
            
            case 386 -> {
                
                if (!path385.equals(pos))
                    
                {
                    appendCurrentState386();
                }
            }
            
            case 385 -> {
                
                if (!path384.equals(pos))
                    
                {
                    appendCurrentState385();
                }
            }
            
            case 384 -> {
                
                if (!path383.equals(pos))
                    
                {
                    appendCurrentState384();
                }
            }
            
            case 383 -> {
                
                if (!path382.equals(pos))
                    
                {
                    appendCurrentState383();
                }
            }
            
            case 382 -> {
                
                if (!path381.equals(pos))
                    
                {
                    appendCurrentState382();
                }
            }
            
            case 381 -> {
                
                if (!path380.equals(pos))
                    
                {
                    appendCurrentState381();
                }
            }
            
            case 380 -> {
                
                if (!path379.equals(pos))
                    
                {
                    appendCurrentState380();
                }
            }
            
            case 379 -> {
                
                if (!path378.equals(pos))
                    
                {
                    appendCurrentState379();
                }
            }
            
            case 378 -> {
                
                if (!path377.equals(pos))
                    
                {
                    appendCurrentState378();
                }
            }
            
            case 377 -> {
                
                if (!path376.equals(pos))
                    
                {
                    appendCurrentState377();
                }
            }
            
            case 376 -> {
                
                if (!path375.equals(pos))
                    
                {
                    appendCurrentState376();
                }
            }
            
            case 375 -> {
                
                if (!path374.equals(pos))
                    
                {
                    appendCurrentState375();
                }
            }
            
            case 374 -> {
                
                if (!path373.equals(pos))
                    
                {
                    appendCurrentState374();
                }
            }
            
            case 373 -> {
                
                if (!path372.equals(pos))
                    
                {
                    appendCurrentState373();
                }
            }
            
            case 372 -> {
                
                if (!path371.equals(pos))
                    
                {
                    appendCurrentState372();
                }
            }
            
            case 371 -> {
                
                if (!path370.equals(pos))
                    
                {
                    appendCurrentState371();
                }
            }
            
            case 370 -> {
                
                if (!path369.equals(pos))
                    
                {
                    appendCurrentState370();
                }
            }
            
            case 369 -> {
                
                if (!path368.equals(pos))
                    
                {
                    appendCurrentState369();
                }
            }
            
            case 368 -> {
                
                if (!path367.equals(pos))
                    
                {
                    appendCurrentState368();
                }
            }
            
            case 367 -> {
                
                if (!path366.equals(pos))
                    
                {
                    appendCurrentState367();
                }
            }
            
            case 366 -> {
                
                if (!path365.equals(pos))
                    
                {
                    appendCurrentState366();
                }
            }
            
            case 365 -> {
                
                if (!path364.equals(pos))
                    
                {
                    appendCurrentState365();
                }
            }
            
            case 364 -> {
                
                if (!path363.equals(pos))
                    
                {
                    appendCurrentState364();
                }
            }
            
            case 363 -> {
                
                if (!path362.equals(pos))
                    
                {
                    appendCurrentState363();
                }
            }
            
            case 362 -> {
                
                if (!path361.equals(pos))
                    
                {
                    appendCurrentState362();
                }
            }
            
            case 361 -> {
                
                if (!path360.equals(pos))
                    
                {
                    appendCurrentState361();
                }
            }
            
            case 360 -> {
                
                if (!path359.equals(pos))
                    
                {
                    appendCurrentState360();
                }
            }
            
            case 359 -> {
                
                if (!path358.equals(pos))
                    
                {
                    appendCurrentState359();
                }
            }
            
            case 358 -> {
                
                if (!path357.equals(pos))
                    
                {
                    appendCurrentState358();
                }
            }
            
            case 357 -> {
                
                if (!path356.equals(pos))
                    
                {
                    appendCurrentState357();
                }
            }
            
            case 356 -> {
                
                if (!path355.equals(pos))
                    
                {
                    appendCurrentState356();
                }
            }
            
            case 355 -> {
                
                if (!path354.equals(pos))
                    
                {
                    appendCurrentState355();
                }
            }
            
            case 354 -> {
                
                if (!path353.equals(pos))
                    
                {
                    appendCurrentState354();
                }
            }
            
            case 353 -> {
                
                if (!path352.equals(pos))
                    
                {
                    appendCurrentState353();
                }
            }
            
            case 352 -> {
                
                if (!path351.equals(pos))
                    
                {
                    appendCurrentState352();
                }
            }
            
            case 351 -> {
                
                if (!path350.equals(pos))
                    
                {
                    appendCurrentState351();
                }
            }
            
            case 350 -> {
                
                if (!path349.equals(pos))
                    
                {
                    appendCurrentState350();
                }
            }
            
            case 349 -> {
                
                if (!path348.equals(pos))
                    
                {
                    appendCurrentState349();
                }
            }
            
            case 348 -> {
                
                if (!path347.equals(pos))
                    
                {
                    appendCurrentState348();
                }
            }
            
            case 347 -> {
                
                if (!path346.equals(pos))
                    
                {
                    appendCurrentState347();
                }
            }
            
            case 346 -> {
                
                if (!path345.equals(pos))
                    
                {
                    appendCurrentState346();
                }
            }
            
            case 345 -> {
                
                if (!path344.equals(pos))
                    
                {
                    appendCurrentState345();
                }
            }
            
            case 344 -> {
                
                if (!path343.equals(pos))
                    
                {
                    appendCurrentState344();
                }
            }
            
            case 343 -> {
                
                if (!path342.equals(pos))
                    
                {
                    appendCurrentState343();
                }
            }
            
            case 342 -> {
                
                if (!path341.equals(pos))
                    
                {
                    appendCurrentState342();
                }
            }
            
            case 341 -> {
                
                if (!path340.equals(pos))
                    
                {
                    appendCurrentState341();
                }
            }
            
            case 340 -> {
                
                if (!path339.equals(pos))
                    
                {
                    appendCurrentState340();
                }
            }
            
            case 339 -> {
                
                if (!path338.equals(pos))
                    
                {
                    appendCurrentState339();
                }
            }
            
            case 338 -> {
                
                if (!path337.equals(pos))
                    
                {
                    appendCurrentState338();
                }
            }
            
            case 337 -> {
                
                if (!path336.equals(pos))
                    
                {
                    appendCurrentState337();
                }
            }
            
            case 336 -> {
                
                if (!path335.equals(pos))
                    
                {
                    appendCurrentState336();
                }
            }
            
            case 335 -> {
                
                if (!path334.equals(pos))
                    
                {
                    appendCurrentState335();
                }
            }
            
            case 334 -> {
                
                if (!path333.equals(pos))
                    
                {
                    appendCurrentState334();
                }
            }
            
            case 333 -> {
                
                if (!path332.equals(pos))
                    
                {
                    appendCurrentState333();
                }
            }
            
            case 332 -> {
                
                if (!path331.equals(pos))
                    
                {
                    appendCurrentState332();
                }
            }
            
            case 331 -> {
                
                if (!path330.equals(pos))
                    
                {
                    appendCurrentState331();
                }
            }
            
            case 330 -> {
                
                if (!path329.equals(pos))
                    
                {
                    appendCurrentState330();
                }
            }
            
            case 329 -> {
                
                if (!path328.equals(pos))
                    
                {
                    appendCurrentState329();
                }
            }
            
            case 328 -> {
                
                if (!path327.equals(pos))
                    
                {
                    appendCurrentState328();
                }
            }
            
            case 327 -> {
                
                if (!path326.equals(pos))
                    
                {
                    appendCurrentState327();
                }
            }
            
            case 326 -> {
                
                if (!path325.equals(pos))
                    
                {
                    appendCurrentState326();
                }
            }
            
            case 325 -> {
                
                if (!path324.equals(pos))
                    
                {
                    appendCurrentState325();
                }
            }
            
            case 324 -> {
                
                if (!path323.equals(pos))
                    
                {
                    appendCurrentState324();
                }
            }
            
            case 323 -> {
                
                if (!path322.equals(pos))
                    
                {
                    appendCurrentState323();
                }
            }
            
            case 322 -> {
                
                if (!path321.equals(pos))
                    
                {
                    appendCurrentState322();
                }
            }
            
            case 321 -> {
                
                if (!path320.equals(pos))
                    
                {
                    appendCurrentState321();
                }
            }
            
            case 320 -> {
                
                if (!path319.equals(pos))
                    
                {
                    appendCurrentState320();
                }
            }
            
            case 319 -> {
                
                if (!path318.equals(pos))
                    
                {
                    appendCurrentState319();
                }
            }
            
            case 318 -> {
                
                if (!path317.equals(pos))
                    
                {
                    appendCurrentState318();
                }
            }
            
            case 317 -> {
                
                if (!path316.equals(pos))
                    
                {
                    appendCurrentState317();
                }
            }
            
            case 316 -> {
                
                if (!path315.equals(pos))
                    
                {
                    appendCurrentState316();
                }
            }
            
            case 315 -> {
                
                if (!path314.equals(pos))
                    
                {
                    appendCurrentState315();
                }
            }
            
            case 314 -> {
                
                if (!path313.equals(pos))
                    
                {
                    appendCurrentState314();
                }
            }
            
            case 313 -> {
                
                if (!path312.equals(pos))
                    
                {
                    appendCurrentState313();
                }
            }
            
            case 312 -> {
                
                if (!path311.equals(pos))
                    
                {
                    appendCurrentState312();
                }
            }
            
            case 311 -> {
                
                if (!path310.equals(pos))
                    
                {
                    appendCurrentState311();
                }
            }
            
            case 310 -> {
                
                if (!path309.equals(pos))
                    
                {
                    appendCurrentState310();
                }
            }
            
            case 309 -> {
                
                if (!path308.equals(pos))
                    
                {
                    appendCurrentState309();
                }
            }
            
            case 308 -> {
                
                if (!path307.equals(pos))
                    
                {
                    appendCurrentState308();
                }
            }
            
            case 307 -> {
                
                if (!path306.equals(pos))
                    
                {
                    appendCurrentState307();
                }
            }
            
            case 306 -> {
                
                if (!path305.equals(pos))
                    
                {
                    appendCurrentState306();
                }
            }
            
            case 305 -> {
                
                if (!path304.equals(pos))
                    
                {
                    appendCurrentState305();
                }
            }
            
            case 304 -> {
                
                if (!path303.equals(pos))
                    
                {
                    appendCurrentState304();
                }
            }
            
            case 303 -> {
                
                if (!path302.equals(pos))
                    
                {
                    appendCurrentState303();
                }
            }
            
            case 302 -> {
                
                if (!path301.equals(pos))
                    
                {
                    appendCurrentState302();
                }
            }
            
            case 301 -> {
                
                if (!path300.equals(pos))
                    
                {
                    appendCurrentState301();
                }
            }
            
            case 300 -> {
                
                if (!path299.equals(pos))
                    
                {
                    appendCurrentState300();
                }
            }
            
            case 299 -> {
                
                if (!path298.equals(pos))
                    
                {
                    appendCurrentState299();
                }
            }
            
            case 298 -> {
                
                if (!path297.equals(pos))
                    
                {
                    appendCurrentState298();
                }
            }
            
            case 297 -> {
                
                if (!path296.equals(pos))
                    
                {
                    appendCurrentState297();
                }
            }
            
            case 296 -> {
                
                if (!path295.equals(pos))
                    
                {
                    appendCurrentState296();
                }
            }
            
            case 295 -> {
                
                if (!path294.equals(pos))
                    
                {
                    appendCurrentState295();
                }
            }
            
            case 294 -> {
                
                if (!path293.equals(pos))
                    
                {
                    appendCurrentState294();
                }
            }
            
            case 293 -> {
                
                if (!path292.equals(pos))
                    
                {
                    appendCurrentState293();
                }
            }
            
            case 292 -> {
                
                if (!path291.equals(pos))
                    
                {
                    appendCurrentState292();
                }
            }
            
            case 291 -> {
                
                if (!path290.equals(pos))
                    
                {
                    appendCurrentState291();
                }
            }
            
            case 290 -> {
                
                if (!path289.equals(pos))
                    
                {
                    appendCurrentState290();
                }
            }
            
            case 289 -> {
                
                if (!path288.equals(pos))
                    
                {
                    appendCurrentState289();
                }
            }
            
            case 288 -> {
                
                if (!path287.equals(pos))
                    
                {
                    appendCurrentState288();
                }
            }
            
            case 287 -> {
                
                if (!path286.equals(pos))
                    
                {
                    appendCurrentState287();
                }
            }
            
            case 286 -> {
                
                if (!path285.equals(pos))
                    
                {
                    appendCurrentState286();
                }
            }
            
            case 285 -> {
                
                if (!path284.equals(pos))
                    
                {
                    appendCurrentState285();
                }
            }
            
            case 284 -> {
                
                if (!path283.equals(pos))
                    
                {
                    appendCurrentState284();
                }
            }
            
            case 283 -> {
                
                if (!path282.equals(pos))
                    
                {
                    appendCurrentState283();
                }
            }
            
            case 282 -> {
                
                if (!path281.equals(pos))
                    
                {
                    appendCurrentState282();
                }
            }
            
            case 281 -> {
                
                if (!path280.equals(pos))
                    
                {
                    appendCurrentState281();
                }
            }
            
            case 280 -> {
                
                if (!path279.equals(pos))
                    
                {
                    appendCurrentState280();
                }
            }
            
            case 279 -> {
                
                if (!path278.equals(pos))
                    
                {
                    appendCurrentState279();
                }
            }
            
            case 278 -> {
                
                if (!path277.equals(pos))
                    
                {
                    appendCurrentState278();
                }
            }
            
            case 277 -> {
                
                if (!path276.equals(pos))
                    
                {
                    appendCurrentState277();
                }
            }
            
            case 276 -> {
                
                if (!path275.equals(pos))
                    
                {
                    appendCurrentState276();
                }
            }
            
            case 275 -> {
                
                if (!path274.equals(pos))
                    
                {
                    appendCurrentState275();
                }
            }
            
            case 274 -> {
                
                if (!path273.equals(pos))
                    
                {
                    appendCurrentState274();
                }
            }
            
            case 273 -> {
                
                if (!path272.equals(pos))
                    
                {
                    appendCurrentState273();
                }
            }
            
            case 272 -> {
                
                if (!path271.equals(pos))
                    
                {
                    appendCurrentState272();
                }
            }
            
            case 271 -> {
                
                if (!path270.equals(pos))
                    
                {
                    appendCurrentState271();
                }
            }
            
            case 270 -> {
                
                if (!path269.equals(pos))
                    
                {
                    appendCurrentState270();
                }
            }
            
            case 269 -> {
                
                if (!path268.equals(pos))
                    
                {
                    appendCurrentState269();
                }
            }
            
            case 268 -> {
                
                if (!path267.equals(pos))
                    
                {
                    appendCurrentState268();
                }
            }
            
            case 267 -> {
                
                if (!path266.equals(pos))
                    
                {
                    appendCurrentState267();
                }
            }
            
            case 266 -> {
                
                if (!path265.equals(pos))
                    
                {
                    appendCurrentState266();
                }
            }
            
            case 265 -> {
                
                if (!path264.equals(pos))
                    
                {
                    appendCurrentState265();
                }
            }
            
            case 264 -> {
                
                if (!path263.equals(pos))
                    
                {
                    appendCurrentState264();
                }
            }
            
            case 263 -> {
                
                if (!path262.equals(pos))
                    
                {
                    appendCurrentState263();
                }
            }
            
            case 262 -> {
                
                if (!path261.equals(pos))
                    
                {
                    appendCurrentState262();
                }
            }
            
            case 261 -> {
                
                if (!path260.equals(pos))
                    
                {
                    appendCurrentState261();
                }
            }
            
            case 260 -> {
                
                if (!path259.equals(pos))
                    
                {
                    appendCurrentState260();
                }
            }
            
            case 259 -> {
                
                if (!path258.equals(pos))
                    
                {
                    appendCurrentState259();
                }
            }
            
            case 258 -> {
                
                if (!path257.equals(pos))
                    
                {
                    appendCurrentState258();
                }
            }
            
            case 257 -> {
                
                if (!path256.equals(pos))
                    
                {
                    appendCurrentState257();
                }
            }
            
            case 256 -> {
                
                if (!path255.equals(pos))
                    
                {
                    appendCurrentState256();
                }
            }
            
            case 255 -> {
                
                if (!path254.equals(pos))
                    
                {
                    appendCurrentState255();
                }
            }
            
            case 254 -> {
                
                if (!path253.equals(pos))
                    
                {
                    appendCurrentState254();
                }
            }
            
            case 253 -> {
                
                if (!path252.equals(pos))
                    
                {
                    appendCurrentState253();
                }
            }
            
            case 252 -> {
                
                if (!path251.equals(pos))
                    
                {
                    appendCurrentState252();
                }
            }
            
            case 251 -> {
                
                if (!path250.equals(pos))
                    
                {
                    appendCurrentState251();
                }
            }
            
            case 250 -> {
                
                if (!path249.equals(pos))
                    
                {
                    appendCurrentState250();
                }
            }
            
            case 249 -> {
                
                if (!path248.equals(pos))
                    
                {
                    appendCurrentState249();
                }
            }
            
            case 248 -> {
                
                if (!path247.equals(pos))
                    
                {
                    appendCurrentState248();
                }
            }
            
            case 247 -> {
                
                if (!path246.equals(pos))
                    
                {
                    appendCurrentState247();
                }
            }
            
            case 246 -> {
                
                if (!path245.equals(pos))
                    
                {
                    appendCurrentState246();
                }
            }
            
            case 245 -> {
                
                if (!path244.equals(pos))
                    
                {
                    appendCurrentState245();
                }
            }
            
            case 244 -> {
                
                if (!path243.equals(pos))
                    
                {
                    appendCurrentState244();
                }
            }
            
            case 243 -> {
                
                if (!path242.equals(pos))
                    
                {
                    appendCurrentState243();
                }
            }
            
            case 242 -> {
                
                if (!path241.equals(pos))
                    
                {
                    appendCurrentState242();
                }
            }
            
            case 241 -> {
                
                if (!path240.equals(pos))
                    
                {
                    appendCurrentState241();
                }
            }
            
            case 240 -> {
                
                if (!path239.equals(pos))
                    
                {
                    appendCurrentState240();
                }
            }
            
            case 239 -> {
                
                if (!path238.equals(pos))
                    
                {
                    appendCurrentState239();
                }
            }
            
            case 238 -> {
                
                if (!path237.equals(pos))
                    
                {
                    appendCurrentState238();
                }
            }
            
            case 237 -> {
                
                if (!path236.equals(pos))
                    
                {
                    appendCurrentState237();
                }
            }
            
            case 236 -> {
                
                if (!path235.equals(pos))
                    
                {
                    appendCurrentState236();
                }
            }
            
            case 235 -> {
                
                if (!path234.equals(pos))
                    
                {
                    appendCurrentState235();
                }
            }
            
            case 234 -> {
                
                if (!path233.equals(pos))
                    
                {
                    appendCurrentState234();
                }
            }
            
            case 233 -> {
                
                if (!path232.equals(pos))
                    
                {
                    appendCurrentState233();
                }
            }
            
            case 232 -> {
                
                if (!path231.equals(pos))
                    
                {
                    appendCurrentState232();
                }
            }
            
            case 231 -> {
                
                if (!path230.equals(pos))
                    
                {
                    appendCurrentState231();
                }
            }
            
            case 230 -> {
                
                if (!path229.equals(pos))
                    
                {
                    appendCurrentState230();
                }
            }
            
            case 229 -> {
                
                if (!path228.equals(pos))
                    
                {
                    appendCurrentState229();
                }
            }
            
            case 228 -> {
                
                if (!path227.equals(pos))
                    
                {
                    appendCurrentState228();
                }
            }
            
            case 227 -> {
                
                if (!path226.equals(pos))
                    
                {
                    appendCurrentState227();
                }
            }
            
            case 226 -> {
                
                if (!path225.equals(pos))
                    
                {
                    appendCurrentState226();
                }
            }
            
            case 225 -> {
                
                if (!path224.equals(pos))
                    
                {
                    appendCurrentState225();
                }
            }
            
            case 224 -> {
                
                if (!path223.equals(pos))
                    
                {
                    appendCurrentState224();
                }
            }
            
            case 223 -> {
                
                if (!path222.equals(pos))
                    
                {
                    appendCurrentState223();
                }
            }
            
            case 222 -> {
                
                if (!path221.equals(pos))
                    
                {
                    appendCurrentState222();
                }
            }
            
            case 221 -> {
                
                if (!path220.equals(pos))
                    
                {
                    appendCurrentState221();
                }
            }
            
            case 220 -> {
                
                if (!path219.equals(pos))
                    
                {
                    appendCurrentState220();
                }
            }
            
            case 219 -> {
                
                if (!path218.equals(pos))
                    
                {
                    appendCurrentState219();
                }
            }
            
            case 218 -> {
                
                if (!path217.equals(pos))
                    
                {
                    appendCurrentState218();
                }
            }
            
            case 217 -> {
                
                if (!path216.equals(pos))
                    
                {
                    appendCurrentState217();
                }
            }
            
            case 216 -> {
                
                if (!path215.equals(pos))
                    
                {
                    appendCurrentState216();
                }
            }
            
            case 215 -> {
                
                if (!path214.equals(pos))
                    
                {
                    appendCurrentState215();
                }
            }
            
            case 214 -> {
                
                if (!path213.equals(pos))
                    
                {
                    appendCurrentState214();
                }
            }
            
            case 213 -> {
                
                if (!path212.equals(pos))
                    
                {
                    appendCurrentState213();
                }
            }
            
            case 212 -> {
                
                if (!path211.equals(pos))
                    
                {
                    appendCurrentState212();
                }
            }
            
            case 211 -> {
                
                if (!path210.equals(pos))
                    
                {
                    appendCurrentState211();
                }
            }
            
            case 210 -> {
                
                if (!path209.equals(pos))
                    
                {
                    appendCurrentState210();
                }
            }
            
            case 209 -> {
                
                if (!path208.equals(pos))
                    
                {
                    appendCurrentState209();
                }
            }
            
            case 208 -> {
                
                if (!path207.equals(pos))
                    
                {
                    appendCurrentState208();
                }
            }
            
            case 207 -> {
                
                if (!path206.equals(pos))
                    
                {
                    appendCurrentState207();
                }
            }
            
            case 206 -> {
                
                if (!path205.equals(pos))
                    
                {
                    appendCurrentState206();
                }
            }
            
            case 205 -> {
                
                if (!path204.equals(pos))
                    
                {
                    appendCurrentState205();
                }
            }
            
            case 204 -> {
                
                if (!path203.equals(pos))
                    
                {
                    appendCurrentState204();
                }
            }
            
            case 203 -> {
                
                if (!path202.equals(pos))
                    
                {
                    appendCurrentState203();
                }
            }
            
            case 202 -> {
                
                if (!path201.equals(pos))
                    
                {
                    appendCurrentState202();
                }
            }
            
            case 201 -> {
                
                if (!path200.equals(pos))
                    
                {
                    appendCurrentState201();
                }
            }
            
            case 200 -> {
                
                if (!path199.equals(pos))
                    
                {
                    appendCurrentState200();
                }
            }
            
            case 199 -> {
                
                if (!path198.equals(pos))
                    
                {
                    appendCurrentState199();
                }
            }
            
            case 198 -> {
                
                if (!path197.equals(pos))
                    
                {
                    appendCurrentState198();
                }
            }
            
            case 197 -> {
                
                if (!path196.equals(pos))
                    
                {
                    appendCurrentState197();
                }
            }
            
            case 196 -> {
                
                if (!path195.equals(pos))
                    
                {
                    appendCurrentState196();
                }
            }
            
            case 195 -> {
                
                if (!path194.equals(pos))
                    
                {
                    appendCurrentState195();
                }
            }
            
            case 194 -> {
                
                if (!path193.equals(pos))
                    
                {
                    appendCurrentState194();
                }
            }
            
            case 193 -> {
                
                if (!path192.equals(pos))
                    
                {
                    appendCurrentState193();
                }
            }
            
            case 192 -> {
                
                if (!path191.equals(pos))
                    
                {
                    appendCurrentState192();
                }
            }
            
            case 191 -> {
                
                if (!path190.equals(pos))
                    
                {
                    appendCurrentState191();
                }
            }
            
            case 190 -> {
                
                if (!path189.equals(pos))
                    
                {
                    appendCurrentState190();
                }
            }
            
            case 189 -> {
                
                if (!path188.equals(pos))
                    
                {
                    appendCurrentState189();
                }
            }
            
            case 188 -> {
                
                if (!path187.equals(pos))
                    
                {
                    appendCurrentState188();
                }
            }
            
            case 187 -> {
                
                if (!path186.equals(pos))
                    
                {
                    appendCurrentState187();
                }
            }
            
            case 186 -> {
                
                if (!path185.equals(pos))
                    
                {
                    appendCurrentState186();
                }
            }
            
            case 185 -> {
                
                if (!path184.equals(pos))
                    
                {
                    appendCurrentState185();
                }
            }
            
            case 184 -> {
                
                if (!path183.equals(pos))
                    
                {
                    appendCurrentState184();
                }
            }
            
            case 183 -> {
                
                if (!path182.equals(pos))
                    
                {
                    appendCurrentState183();
                }
            }
            
            case 182 -> {
                
                if (!path181.equals(pos))
                    
                {
                    appendCurrentState182();
                }
            }
            
            case 181 -> {
                
                if (!path180.equals(pos))
                    
                {
                    appendCurrentState181();
                }
            }
            
            case 180 -> {
                
                if (!path179.equals(pos))
                    
                {
                    appendCurrentState180();
                }
            }
            
            case 179 -> {
                
                if (!path178.equals(pos))
                    
                {
                    appendCurrentState179();
                }
            }
            
            case 178 -> {
                
                if (!path177.equals(pos))
                    
                {
                    appendCurrentState178();
                }
            }
            
            case 177 -> {
                
                if (!path176.equals(pos))
                    
                {
                    appendCurrentState177();
                }
            }
            
            case 176 -> {
                
                if (!path175.equals(pos))
                    
                {
                    appendCurrentState176();
                }
            }
            
            case 175 -> {
                
                if (!path174.equals(pos))
                    
                {
                    appendCurrentState175();
                }
            }
            
            case 174 -> {
                
                if (!path173.equals(pos))
                    
                {
                    appendCurrentState174();
                }
            }
            
            case 173 -> {
                
                if (!path172.equals(pos))
                    
                {
                    appendCurrentState173();
                }
            }
            
            case 172 -> {
                
                if (!path171.equals(pos))
                    
                {
                    appendCurrentState172();
                }
            }
            
            case 171 -> {
                
                if (!path170.equals(pos))
                    
                {
                    appendCurrentState171();
                }
            }
            
            case 170 -> {
                
                if (!path169.equals(pos))
                    
                {
                    appendCurrentState170();
                }
            }
            
            case 169 -> {
                
                if (!path168.equals(pos))
                    
                {
                    appendCurrentState169();
                }
            }
            
            case 168 -> {
                
                if (!path167.equals(pos))
                    
                {
                    appendCurrentState168();
                }
            }
            
            case 167 -> {
                
                if (!path166.equals(pos))
                    
                {
                    appendCurrentState167();
                }
            }
            
            case 166 -> {
                
                if (!path165.equals(pos))
                    
                {
                    appendCurrentState166();
                }
            }
            
            case 165 -> {
                
                if (!path164.equals(pos))
                    
                {
                    appendCurrentState165();
                }
            }
            
            case 164 -> {
                
                if (!path163.equals(pos))
                    
                {
                    appendCurrentState164();
                }
            }
            
            case 163 -> {
                
                if (!path162.equals(pos))
                    
                {
                    appendCurrentState163();
                }
            }
            
            case 162 -> {
                
                if (!path161.equals(pos))
                    
                {
                    appendCurrentState162();
                }
            }
            
            case 161 -> {
                
                if (!path160.equals(pos))
                    
                {
                    appendCurrentState161();
                }
            }
            
            case 160 -> {
                
                if (!path159.equals(pos))
                    
                {
                    appendCurrentState160();
                }
            }
            
            case 159 -> {
                
                if (!path158.equals(pos))
                    
                {
                    appendCurrentState159();
                }
            }
            
            case 158 -> {
                
                if (!path157.equals(pos))
                    
                {
                    appendCurrentState158();
                }
            }
            
            case 157 -> {
                
                if (!path156.equals(pos))
                    
                {
                    appendCurrentState157();
                }
            }
            
            case 156 -> {
                
                if (!path155.equals(pos))
                    
                {
                    appendCurrentState156();
                }
            }
            
            case 155 -> {
                
                if (!path154.equals(pos))
                    
                {
                    appendCurrentState155();
                }
            }
            
            case 154 -> {
                
                if (!path153.equals(pos))
                    
                {
                    appendCurrentState154();
                }
            }
            
            case 153 -> {
                
                if (!path152.equals(pos))
                    
                {
                    appendCurrentState153();
                }
            }
            
            case 152 -> {
                
                if (!path151.equals(pos))
                    
                {
                    appendCurrentState152();
                }
            }
            
            case 151 -> {
                
                if (!path150.equals(pos))
                    
                {
                    appendCurrentState151();
                }
            }
            
            case 150 -> {
                
                if (!path149.equals(pos))
                    
                {
                    appendCurrentState150();
                }
            }
            
            case 149 -> {
                
                if (!path148.equals(pos))
                    
                {
                    appendCurrentState149();
                }
            }
            
            case 148 -> {
                
                if (!path147.equals(pos))
                    
                {
                    appendCurrentState148();
                }
            }
            
            case 147 -> {
                
                if (!path146.equals(pos))
                    
                {
                    appendCurrentState147();
                }
            }
            
            case 146 -> {
                
                if (!path145.equals(pos))
                    
                {
                    appendCurrentState146();
                }
            }
            
            case 145 -> {
                
                if (!path144.equals(pos))
                    
                {
                    appendCurrentState145();
                }
            }
            
            case 144 -> {
                
                if (!path143.equals(pos))
                    
                {
                    appendCurrentState144();
                }
            }
            
            case 143 -> {
                
                if (!path142.equals(pos))
                    
                {
                    appendCurrentState143();
                }
            }
            
            case 142 -> {
                
                if (!path141.equals(pos))
                    
                {
                    appendCurrentState142();
                }
            }
            
            case 141 -> {
                
                if (!path140.equals(pos))
                    
                {
                    appendCurrentState141();
                }
            }
            
            case 140 -> {
                
                if (!path139.equals(pos))
                    
                {
                    appendCurrentState140();
                }
            }
            
            case 139 -> {
                
                if (!path138.equals(pos))
                    
                {
                    appendCurrentState139();
                }
            }
            
            case 138 -> {
                
                if (!path137.equals(pos))
                    
                {
                    appendCurrentState138();
                }
            }
            
            case 137 -> {
                
                if (!path136.equals(pos))
                    
                {
                    appendCurrentState137();
                }
            }
            
            case 136 -> {
                
                if (!path135.equals(pos))
                    
                {
                    appendCurrentState136();
                }
            }
            
            case 135 -> {
                
                if (!path134.equals(pos))
                    
                {
                    appendCurrentState135();
                }
            }
            
            case 134 -> {
                
                if (!path133.equals(pos))
                    
                {
                    appendCurrentState134();
                }
            }
            
            case 133 -> {
                
                if (!path132.equals(pos))
                    
                {
                    appendCurrentState133();
                }
            }
            
            case 132 -> {
                
                if (!path131.equals(pos))
                    
                {
                    appendCurrentState132();
                }
            }
            
            case 131 -> {
                
                if (!path130.equals(pos))
                    
                {
                    appendCurrentState131();
                }
            }
            
            case 130 -> {
                
                if (!path129.equals(pos))
                    
                {
                    appendCurrentState130();
                }
            }
            
            case 129 -> {
                
                if (!path128.equals(pos))
                    
                {
                    appendCurrentState129();
                }
            }
            
            case 128 -> {
                
                if (!path127.equals(pos))
                    
                {
                    appendCurrentState128();
                }
            }
            
            case 127 -> {
                
                if (!path126.equals(pos))
                    
                {
                    appendCurrentState127();
                }
            }
            
            case 126 -> {
                
                if (!path125.equals(pos))
                    
                {
                    appendCurrentState126();
                }
            }
            
            case 125 -> {
                
                if (!path124.equals(pos))
                    
                {
                    appendCurrentState125();
                }
            }
            
            case 124 -> {
                
                if (!path123.equals(pos))
                    
                {
                    appendCurrentState124();
                }
            }
            
            case 123 -> {
                
                if (!path122.equals(pos))
                    
                {
                    appendCurrentState123();
                }
            }
            
            case 122 -> {
                
                if (!path121.equals(pos))
                    
                {
                    appendCurrentState122();
                }
            }
            
            case 121 -> {
                
                if (!path120.equals(pos))
                    
                {
                    appendCurrentState121();
                }
            }
            
            case 120 -> {
                
                if (!path119.equals(pos))
                    
                {
                    appendCurrentState120();
                }
            }
            
            case 119 -> {
                
                if (!path118.equals(pos))
                    
                {
                    appendCurrentState119();
                }
            }
            
            case 118 -> {
                
                if (!path117.equals(pos))
                    
                {
                    appendCurrentState118();
                }
            }
            
            case 117 -> {
                
                if (!path116.equals(pos))
                    
                {
                    appendCurrentState117();
                }
            }
            
            case 116 -> {
                
                if (!path115.equals(pos))
                    
                {
                    appendCurrentState116();
                }
            }
            
            case 115 -> {
                
                if (!path114.equals(pos))
                    
                {
                    appendCurrentState115();
                }
            }
            
            case 114 -> {
                
                if (!path113.equals(pos))
                    
                {
                    appendCurrentState114();
                }
            }
            
            case 113 -> {
                
                if (!path112.equals(pos))
                    
                {
                    appendCurrentState113();
                }
            }
            
            case 112 -> {
                
                if (!path111.equals(pos))
                    
                {
                    appendCurrentState112();
                }
            }
            
            case 111 -> {
                
                if (!path110.equals(pos))
                    
                {
                    appendCurrentState111();
                }
            }
            
            case 110 -> {
                
                if (!path109.equals(pos))
                    
                {
                    appendCurrentState110();
                }
            }
            
            case 109 -> {
                
                if (!path108.equals(pos))
                    
                {
                    appendCurrentState109();
                }
            }
            
            case 108 -> {
                
                if (!path107.equals(pos))
                    
                {
                    appendCurrentState108();
                }
            }
            
            case 107 -> {
                
                if (!path106.equals(pos))
                    
                {
                    appendCurrentState107();
                }
            }
            
            case 106 -> {
                
                if (!path105.equals(pos))
                    
                {
                    appendCurrentState106();
                }
            }
            
            case 105 -> {
                
                if (!path104.equals(pos))
                    
                {
                    appendCurrentState105();
                }
            }
            
            case 104 -> {
                
                if (!path103.equals(pos))
                    
                {
                    appendCurrentState104();
                }
            }
            
            case 103 -> {
                
                if (!path102.equals(pos))
                    
                {
                    appendCurrentState103();
                }
            }
            
            case 102 -> {
                
                if (!path101.equals(pos))
                    
                {
                    appendCurrentState102();
                }
            }
            
            case 101 -> {
                
                if (!path100.equals(pos))
                    
                {
                    appendCurrentState101();
                }
            }
            
            case 100 -> {
                
                if (!path99.equals(pos))
                    
                {
                    appendCurrentState100();
                }
            }
            
            case 99 -> {
                
                if (!path98.equals(pos))
                    
                {
                    appendCurrentState99();
                }
            }
            
            case 98 -> {
                
                if (!path97.equals(pos))
                    
                {
                    appendCurrentState98();
                }
            }
            
            case 97 -> {
                
                if (!path96.equals(pos))
                    
                {
                    appendCurrentState97();
                }
            }
            
            case 96 -> {
                
                if (!path95.equals(pos))
                    
                {
                    appendCurrentState96();
                }
            }
            
            case 95 -> {
                
                if (!path94.equals(pos))
                    
                {
                    appendCurrentState95();
                }
            }
            
            case 94 -> {
                
                if (!path93.equals(pos))
                    
                {
                    appendCurrentState94();
                }
            }
            
            case 93 -> {
                
                if (!path92.equals(pos))
                    
                {
                    appendCurrentState93();
                }
            }
            
            case 92 -> {
                
                if (!path91.equals(pos))
                    
                {
                    appendCurrentState92();
                }
            }
            
            case 91 -> {
                
                if (!path90.equals(pos))
                    
                {
                    appendCurrentState91();
                }
            }
            
            case 90 -> {
                
                if (!path89.equals(pos))
                    
                {
                    appendCurrentState90();
                }
            }
            
            case 89 -> {
                
                if (!path88.equals(pos))
                    
                {
                    appendCurrentState89();
                }
            }
            
            case 88 -> {
                
                if (!path87.equals(pos))
                    
                {
                    appendCurrentState88();
                }
            }
            
            case 87 -> {
                
                if (!path86.equals(pos))
                    
                {
                    appendCurrentState87();
                }
            }
            
            case 86 -> {
                
                if (!path85.equals(pos))
                    
                {
                    appendCurrentState86();
                }
            }
            
            case 85 -> {
                
                if (!path84.equals(pos))
                    
                {
                    appendCurrentState85();
                }
            }
            
            case 84 -> {
                
                if (!path83.equals(pos))
                    
                {
                    appendCurrentState84();
                }
            }
            
            case 83 -> {
                
                if (!path82.equals(pos))
                    
                {
                    appendCurrentState83();
                }
            }
            
            case 82 -> {
                
                if (!path81.equals(pos))
                    
                {
                    appendCurrentState82();
                }
            }
            
            case 81 -> {
                
                if (!path80.equals(pos))
                    
                {
                    appendCurrentState81();
                }
            }
            
            case 80 -> {
                
                if (!path79.equals(pos))
                    
                {
                    appendCurrentState80();
                }
            }
            
            case 79 -> {
                
                if (!path78.equals(pos))
                    
                {
                    appendCurrentState79();
                }
            }
            
            case 78 -> {
                
                if (!path77.equals(pos))
                    
                {
                    appendCurrentState78();
                }
            }
            
            case 77 -> {
                
                if (!path76.equals(pos))
                    
                {
                    appendCurrentState77();
                }
            }
            
            case 76 -> {
                
                if (!path75.equals(pos))
                    
                {
                    appendCurrentState76();
                }
            }
            
            case 75 -> {
                
                if (!path74.equals(pos))
                    
                {
                    appendCurrentState75();
                }
            }
            
            case 74 -> {
                
                if (!path73.equals(pos))
                    
                {
                    appendCurrentState74();
                }
            }
            
            case 73 -> {
                
                if (!path72.equals(pos))
                    
                {
                    appendCurrentState73();
                }
            }
            
            case 72 -> {
                
                if (!path71.equals(pos))
                    
                {
                    appendCurrentState72();
                }
            }
            
            case 71 -> {
                
                if (!path70.equals(pos))
                    
                {
                    appendCurrentState71();
                }
            }
            
            case 70 -> {
                
                if (!path69.equals(pos))
                    
                {
                    appendCurrentState70();
                }
            }
            
            case 69 -> {
                
                if (!path68.equals(pos))
                    
                {
                    appendCurrentState69();
                }
            }
            
            case 68 -> {
                
                if (!path67.equals(pos))
                    
                {
                    appendCurrentState68();
                }
            }
            
            case 67 -> {
                
                if (!path66.equals(pos))
                    
                {
                    appendCurrentState67();
                }
            }
            
            case 66 -> {
                
                if (!path65.equals(pos))
                    
                {
                    appendCurrentState66();
                }
            }
            
            case 65 -> {
                
                if (!path64.equals(pos))
                    
                {
                    appendCurrentState65();
                }
            }
            
            case 64 -> {
                
                if (!path63.equals(pos))
                    
                {
                    appendCurrentState64();
                }
            }
            
            case 63 -> {
                
                if (!path62.equals(pos))
                    
                {
                    appendCurrentState63();
                }
            }
            
            case 62 -> {
                
                if (!path61.equals(pos))
                    
                {
                    appendCurrentState62();
                }
            }
            
            case 61 -> {
                
                if (!path60.equals(pos))
                    
                {
                    appendCurrentState61();
                }
            }
            
            case 60 -> {
                
                if (!path59.equals(pos))
                    
                {
                    appendCurrentState60();
                }
            }
            
            case 59 -> {
                
                if (!path58.equals(pos))
                    
                {
                    appendCurrentState59();
                }
            }
            
            case 58 -> {
                
                if (!path57.equals(pos))
                    
                {
                    appendCurrentState58();
                }
            }
            
            case 57 -> {
                
                if (!path56.equals(pos))
                    
                {
                    appendCurrentState57();
                }
            }
            
            case 56 -> {
                
                if (!path55.equals(pos))
                    
                {
                    appendCurrentState56();
                }
            }
            
            case 55 -> {
                
                if (!path54.equals(pos))
                    
                {
                    appendCurrentState55();
                }
            }
            
            case 54 -> {
                
                if (!path53.equals(pos))
                    
                {
                    appendCurrentState54();
                }
            }
            
            case 53 -> {
                
                if (!path52.equals(pos))
                    
                {
                    appendCurrentState53();
                }
            }
            
            case 52 -> {
                
                if (!path51.equals(pos))
                    
                {
                    appendCurrentState52();
                }
            }
            
            case 51 -> {
                
                if (!path50.equals(pos))
                    
                {
                    appendCurrentState51();
                }
            }
            
            case 50 -> {
                
                if (!path49.equals(pos))
                    
                {
                    appendCurrentState50();
                }
            }
            
            case 49 -> {
                
                if (!path48.equals(pos))
                    
                {
                    appendCurrentState49();
                }
            }
            
            case 48 -> {
                
                if (!path47.equals(pos))
                    
                {
                    appendCurrentState48();
                }
            }
            
            case 47 -> {
                
                if (!path46.equals(pos))
                    
                {
                    appendCurrentState47();
                }
            }
            
            case 46 -> {
                
                if (!path45.equals(pos))
                    
                {
                    appendCurrentState46();
                }
            }
            
            case 45 -> {
                
                if (!path44.equals(pos))
                    
                {
                    appendCurrentState45();
                }
            }
            
            case 44 -> {
                
                if (!path43.equals(pos))
                    
                {
                    appendCurrentState44();
                }
            }
            
            case 43 -> {
                
                if (!path42.equals(pos))
                    
                {
                    appendCurrentState43();
                }
            }
            
            case 42 -> {
                
                if (!path41.equals(pos))
                    
                {
                    appendCurrentState42();
                }
            }
            
            case 41 -> {
                
                if (!path40.equals(pos))
                    
                {
                    appendCurrentState41();
                }
            }
            
            case 40 -> {
                
                if (!path39.equals(pos))
                    
                {
                    appendCurrentState40();
                }
            }
            
            case 39 -> {
                
                if (!path38.equals(pos))
                    
                {
                    appendCurrentState39();
                }
            }
            
            case 38 -> {
                
                if (!path37.equals(pos))
                    
                {
                    appendCurrentState38();
                }
            }
            
            case 37 -> {
                
                if (!path36.equals(pos))
                    
                {
                    appendCurrentState37();
                }
            }
            
            case 36 -> {
                
                if (!path35.equals(pos))
                    
                {
                    appendCurrentState36();
                }
            }
            
            case 35 -> {
                
                if (!path34.equals(pos))
                    
                {
                    appendCurrentState35();
                }
            }
            
            case 34 -> {
                
                if (!path33.equals(pos))
                    
                {
                    appendCurrentState34();
                }
            }
            
            case 33 -> {
                
                if (!path32.equals(pos))
                    
                {
                    appendCurrentState33();
                }
            }
            
            case 32 -> {
                
                if (!path31.equals(pos))
                    
                {
                    appendCurrentState32();
                }
            }
            
            case 31 -> {
                
                if (!path30.equals(pos))
                    
                {
                    appendCurrentState31();
                }
            }
            
            case 30 -> {
                
                if (!path29.equals(pos))
                    
                {
                    appendCurrentState30();
                }
            }
            
            case 29 -> {
                
                if (!path28.equals(pos))
                    
                {
                    appendCurrentState29();
                }
            }
            
            case 28 -> {
                
                if (!path27.equals(pos))
                    
                {
                    appendCurrentState28();
                }
            }
            
            case 27 -> {
                
                if (!path26.equals(pos))
                    
                {
                    appendCurrentState27();
                }
            }
            
            case 26 -> {
                
                if (!path25.equals(pos))
                    
                {
                    appendCurrentState26();
                }
            }
            
            case 25 -> {
                
                if (!path24.equals(pos))
                    
                {
                    appendCurrentState25();
                }
            }
            
            case 24 -> {
                
                if (!path23.equals(pos))
                    
                {
                    appendCurrentState24();
                }
            }
            
            case 23 -> {
                
                if (!path22.equals(pos))
                    
                {
                    appendCurrentState23();
                }
            }
            
            case 22 -> {
                
                if (!path21.equals(pos))
                    
                {
                    appendCurrentState22();
                }
            }
            
            case 21 -> {
                
                if (!path20.equals(pos))
                    
                {
                    appendCurrentState21();
                }
            }
            
            case 20 -> {
                
                if (!path19.equals(pos))
                    
                {
                    appendCurrentState20();
                }
            }
            
            case 19 -> {
                
                if (!path18.equals(pos))
                    
                {
                    appendCurrentState19();
                }
            }
            
            case 18 -> {
                
                if (!path17.equals(pos))
                    
                {
                    appendCurrentState18();
                }
            }
            
            case 17 -> {
                
                if (!path16.equals(pos))
                    
                {
                    appendCurrentState17();
                }
            }
            
            case 16 -> {
                
                if (!path15.equals(pos))
                    
                {
                    appendCurrentState16();
                }
            }
            
            case 15 -> {
                
                if (!path14.equals(pos))
                    
                {
                    appendCurrentState15();
                }
            }
            
            case 14 -> {
                
                if (!path13.equals(pos))
                    
                {
                    appendCurrentState14();
                }
            }
            
            case 13 -> {
                
                if (!path12.equals(pos))
                    
                {
                    appendCurrentState13();
                }
            }
            
            case 12 -> {
                
                if (!path11.equals(pos))
                    
                {
                    appendCurrentState12();
                }
            }
            
            case 11 -> {
                
                if (!path10.equals(pos))
                    
                {
                    appendCurrentState11();
                }
            }
            
            case 10 -> {
                
                if (!path9.equals(pos))
                    
                {
                    appendCurrentState10();
                }
            }
            
            case 9 -> {
                
                if (!path8.equals(pos))
                    
                {
                    appendCurrentState9();
                }
            }
            
            case 8 -> {
                
                if (!path7.equals(pos))
                    
                {
                    appendCurrentState8();
                }
            }
            
            case 7 -> {
                
                if (!path6.equals(pos))
                    
                {
                    appendCurrentState7();
                }
            }
            
            case 6 -> {
                
                if (!path5.equals(pos))
                    
                {
                    appendCurrentState6();
                }
            }
            
            case 5 -> {
                
                if (!path4.equals(pos))
                    
                {
                    appendCurrentState5();
                }
            }
            
            case 4 -> {
                
                if (!path3.equals(pos))
                    
                {
                    appendCurrentState4();
                }
            }
            
            case 3 -> {
                
                if (!path2.equals(pos))
                    
                {
                    appendCurrentState3();
                }
            }
            
            case 2 -> {
                
                if (!path1.equals(pos))
                    
                {
                    appendCurrentState2();
                }
            }
            
            case 1 -> {
                
                if (!path0.equals(pos))
                    
                {
                    appendCurrentState1();
                }
            }
            
            case 0 -> {
                
                {
                    appendCurrentState0();
                }
            }
            
            default -> { assert(pathLength < 500); }
        }
    }

    // return the original location
    MapLocation advance(boolean canGuess) throws GameActionException {
        // System.out.println("old pos " + pos + " i am at " + rc.getLocation());
        if(!rc.getLocation().equals(lastLoc)) {
            lastLoc = rc.getLocation();
            setCurrentState();
        }

        if(pos.isAdjacentTo(target)) return pos;

        var dirToTarget = pos.directionTo(target);
        if(dirToTarget != bottomDir) {
            if(dirToTarget == Direction.CENTER) return pos;
            
            if(leftTurn) {
                
            if(dirToTarget == bottomDir.rotateRight()) {
                bottomDir = dirToTarget;
                stackSize++;
            } else {
                assert dirToTarget == bottomDir.rotateLeft() : "unexpected direction change from " + bottomDir + " to " + dirToTarget;
                bottomDir = dirToTarget;
                stackSize--;
                if(stackSize < 0) {
                    stackSize = 0;
                    topDir = bottomDir;
                }
            }
            
            } else {
                
            if(dirToTarget == bottomDir.rotateLeft()) {
                bottomDir = dirToTarget;
                stackSize++;
            } else {
                assert dirToTarget == bottomDir.rotateRight() : "unexpected direction change from " + bottomDir + " to " + dirToTarget;
                bottomDir = dirToTarget;
                stackSize--;
                if(stackSize < 0) {
                    stackSize = 0;
                    topDir = bottomDir;
                }
            }
            
            }
        }

        var oldPos = pos;

        MapLocation greedyLoc = pos.add(bottomDir);
        if(stackSize <= 1) {
            
            if(CellTracker.isPassable(greedyLoc)) {
                
            if(chebyshevDistance(rc.getLocation(), greedyLoc) <= 5) {
                indicate("going to greedyLoc");
                pos = greedyLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentState();
            }
            return oldPos;
            
            }
            var leftLoc = pos.add(bottomDir.rotateLeft());
            var rightLoc = pos.add(bottomDir.rotateRight());
            var leftPassable = CellTracker.isPassable(leftLoc);
            var rightPassable = CellTracker.isPassable(rightLoc);
            if(leftPassable && !rightPassable) {
                
            if(chebyshevDistance(rc.getLocation(), leftLoc) <= 5) {
                indicate("going to leftLoc");
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentState();
            }
            return oldPos;
            
            }
            if(!leftPassable && rightPassable) {
                
            if(chebyshevDistance(rc.getLocation(), rightLoc) <= 5) {
                indicate("going to rightLoc");
                pos = rightLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentState();
            }
            return oldPos;
            
            }
            if(leftPassable && rightPassable) {
                if(leftLoc.distanceSquaredTo(target) < rightLoc.distanceSquaredTo(target)) {
                    
            if(chebyshevDistance(rc.getLocation(), leftLoc) <= 5) {
                indicate("going to leftLoc");
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentState();
            }
            return oldPos;
            
                } else {
                    
            if(chebyshevDistance(rc.getLocation(), rightLoc) <= 5) {
                indicate("going to rightLoc");
                pos = rightLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentState();
            }
            return oldPos;
            
                }
            }
        }
        // System.out.println("bug" + pos + " " + bottomDir + " " + topDir + " " + stackSize);

        // System.out.println(pos + " " + target + " " + topDir + " " + bottomDir + " " + stackSize);

        // rc.setIndicatorString("" + bottomDir + " " + topDir + " " + stackSize + " " + leftTurn + " " + emptyStackTurns);

        // guess rotation direction (kinda inefficient but wtv)
        if(isMaster && !justGuessed && stackSize == 0 && CellTracker.isSeen(greedyLoc)) {
            assert !CellTracker.isPassable(greedyLoc);
            if(emptyStackTurns >= 1) {
                if(canGuess) {
                    indicate("guessing " + pos);
                    Profiler.begin();
                    // System.out.println("guessing " + pos + " " + emptyStackTurns);
                    var leftBug = new BugnavInstance(pos, true, target, false);
                    var rightBug = new BugnavInstance(pos, false, target, false);
                    
                    
                    {
                        MapLocation bugPos;
                        int shade = 255;
                        bugLoop: {
                            
                            bugPos = leftBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(leftBug.pos)) break bugLoop;
                            
                            bugPos = leftBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(leftBug.pos)) break bugLoop;
                            
                            bugPos = leftBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(leftBug.pos)) break bugLoop;
                            
                            bugPos = leftBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(leftBug.pos)) break bugLoop;
                            
                            bugPos = leftBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(leftBug.pos)) break bugLoop;
                            
                        }
                    }
                    
                    
                    {
                        MapLocation bugPos;
                        int shade = 255;
                        bugLoop: {
                            
                            bugPos = rightBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(rightBug.pos)) break bugLoop;
                            
                            bugPos = rightBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(rightBug.pos)) break bugLoop;
                            
                            bugPos = rightBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(rightBug.pos)) break bugLoop;
                            
                            bugPos = rightBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(rightBug.pos)) break bugLoop;
                            
                            bugPos = rightBug.advance(false);
                            // System.out.println(bugPos);
                            rc.setIndicatorDot(bugPos, Math.max(0, shade -= 30), 0, Math.max(0, shade));
                            if(bugPos.equals(rightBug.pos)) break bugLoop;
                            
                        }
                    }
                    
                    
                    // indicate("lookahead");
                    // indicate(leftBug.pos);
                    // indicate( Math.sqrt(leftBug.pos.distanceSquaredTo(target)) + leftBug.pathLength * 0.7 );
                    // indicate(rightBug.pos);
                    // indicate( Math.sqrt(rightBug.pos.distanceSquaredTo(target)) + rightBug.pathLength * 0.7 );
                    leftTurn =  Math.sqrt(leftBug.pos.distanceSquaredTo(target)) + leftBug.pathLength * 0.7  <  Math.sqrt(rightBug.pos.distanceSquaredTo(target)) + rightBug.pathLength * 0.7 ;
                    // indicate("leftTurn " + leftTurn);
                    setCurrentState();
                    Profiler.end("guess");
                    justGuessed = true;
                }
                return pos;
            } else if(emptyStackTurns < 0) {
                emptyStackTurns = 0;
            }
        }
        justGuessed = false;

        // Profiler.begin();
        

        if(leftTurn) {
            
        // this loop only exists to allow continue
        while(true) {
            var tryDir = topDir.rotateRight().rotateRight();
            
            
            if(stackSize + -2 >= 0)
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += -2;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            if(stackSize + -1 >= 0)
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += -1;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 1;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 2;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 3;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 4;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 5;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateLeft();
            
            assert false : "no valid move found from " + pos;
        }
        
        } else {
            
        // this loop only exists to allow continue
        while(true) {
            var tryDir = topDir.rotateLeft().rotateLeft();
            
            
            if(stackSize + -2 >= 0)
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += -2;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            if(stackSize + -1 >= 0)
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += -1;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 1;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 2;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 3;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 4;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            
            {
                var nextPos = pos.add(tryDir);
                if(!CellTracker.isSeen(nextPos) || chebyshevDistance(rc.getLocation(), nextPos) > 5) {
                    if(rc.onTheMap(nextPos)) rc.setIndicatorDot(nextPos, 0, 255, 255);
                    else if(isMaster) {
                        // avoid following the edge of the map
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        reset();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    }
                    // Profiler.end("bugnav");
                    return oldPos;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    
                    stackSize += 5;
                    
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentState();
                    // Profiler.end("bugnav");
                    return oldPos;
                }
            }
            tryDir = tryDir.rotateRight();
            
            assert false : "no valid move found from " + pos;
        }
        
        }
    }
}

public class Pathfinder {
    static BugnavInstance bug;

    public static void makeMove(Direction dir) throws GameActionException {
        rc.turn(dir);
        rc.moveForward();
        // TODO: optimise
        CellTracker.computeLocalPassable();
    }

    public static void makeMove(MapLocation target) throws GameActionException {
        assert rc.isMovementReady();
        // TODO: optimise
        CellTracker.computeLocalPassable();
        // TODO: better handling when target changes
        if(bug == null || !target.equals(bug.target)) {
            indicate("RETARGET");
            bug = new BugnavInstance(true, target);
        }
        MapLocation bugPos;
        int shade = 255;
        bugLoop: {
            
            bugPos = bug.advance( true );
            indicate(bugPos);
            rc.setIndicatorDot(bugPos, 0, Math.max(0, shade -= 30), 0);
            if(bugPos.equals(bug.pos)) break bugLoop;
            
            bugPos = bug.advance( false );
            indicate(bugPos);
            rc.setIndicatorDot(bugPos, 0, Math.max(0, shade -= 30), 0);
            if(bugPos.equals(bug.pos)) break bugLoop;
            
            bugPos = bug.advance( false );
            indicate(bugPos);
            rc.setIndicatorDot(bugPos, 0, Math.max(0, shade -= 30), 0);
            if(bugPos.equals(bug.pos)) break bugLoop;
            
            bugPos = bug.advance( false );
            indicate(bugPos);
            rc.setIndicatorDot(bugPos, 0, Math.max(0, shade -= 30), 0);
            if(bugPos.equals(bug.pos)) break bugLoop;
            
            bugPos = bug.advance( false );
            indicate(bugPos);
            rc.setIndicatorDot(bugPos, 0, Math.max(0, shade -= 30), 0);
            if(bugPos.equals(bug.pos)) break bugLoop;
            
        }
        rc.setIndicatorDot(bug.pos, 255, 0, 0);

        // no bfs needed
        var dir = rc.getLocation().directionTo(bugPos);
        // indicate(dir);
        if(rc.getLocation().add(dir).equals(bugPos)) {
            indicate("adjacent");
            makeMove(dir);
            return;
        }
        // TODO: what if location == bug.pos?

        // bfs
        var loc = rc.getLocation();
        // d{i}0 + d{i}1 is the mask of reachable within i steps
        long d00 = 0, d01 = 0;
        int idx = (bugPos.y - loc.y + 5) * 11 + (bugPos.x - loc.x + 5);
        if(idx < 61) {
            d00 |= 1l << idx;
        } else {
            d01 |= 1l << (idx - 61);
        }
        
        Direction bestMove = null;
        double bestScore = 1e18;
        
        long d10 = 0, d11 = 0;
        d10 = d00;
        d11 = d01;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d10, d11);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d10 |= (d10 & 0xfbff7feffdffbffl) << 1;
        d11 |= (d11 & 0x7feffdffbff7fefl) << 1;
        d10 |= (d10 & 0x1f7feffdffbff7fel) >> 1;
        d11 |= (d11 & 0xffdffbff7feffdel) >> 1;

        d11 |= ((d11 & 0x1ffffffffffffl) << 11) | (d10 >> 50);
        d10 |= (d10 & 0x3ffffffffffffl) << 11;
        d10 |= (d10 >> 11) | ((d11 & 0x7ffl) << 50);
        d11 |= d11 >> 11;
        d10 &= CellTracker.localPassable0;
        d11 &= CellTracker.localPassable1;
        if((d10 & 0x807000000000000l) != 0 || (d11 & 0xe01) != 0) {
            
            if(
               
               (d10 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d11 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d11 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d10 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d10 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d10 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d11 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d11 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 1);
                return;
            }
        }
        
        long d20 = 0, d21 = 0;
        d20 = d10;
        d21 = d11;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d20, d21);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d20 |= (d20 & 0xfbff7feffdffbffl) << 1;
        d21 |= (d21 & 0x7feffdffbff7fefl) << 1;
        d20 |= (d20 & 0x1f7feffdffbff7fel) >> 1;
        d21 |= (d21 & 0xffdffbff7feffdel) >> 1;

        d21 |= ((d21 & 0x1ffffffffffffl) << 11) | (d20 >> 50);
        d20 |= (d20 & 0x3ffffffffffffl) << 11;
        d20 |= (d20 >> 11) | ((d21 & 0x7ffl) << 50);
        d21 |= d21 >> 11;
        d20 &= CellTracker.localPassable0;
        d21 &= CellTracker.localPassable1;
        if((d20 & 0x807000000000000l) != 0 || (d21 & 0xe01) != 0) {
            
            if(
               
               (d20 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d21 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d21 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d20 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d20 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d20 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d21 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d21 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 2);
                return;
            }
        }
        
        long d30 = 0, d31 = 0;
        d30 = d20;
        d31 = d21;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d30, d31);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d30 |= (d30 & 0xfbff7feffdffbffl) << 1;
        d31 |= (d31 & 0x7feffdffbff7fefl) << 1;
        d30 |= (d30 & 0x1f7feffdffbff7fel) >> 1;
        d31 |= (d31 & 0xffdffbff7feffdel) >> 1;

        d31 |= ((d31 & 0x1ffffffffffffl) << 11) | (d30 >> 50);
        d30 |= (d30 & 0x3ffffffffffffl) << 11;
        d30 |= (d30 >> 11) | ((d31 & 0x7ffl) << 50);
        d31 |= d31 >> 11;
        d30 &= CellTracker.localPassable0;
        d31 &= CellTracker.localPassable1;
        if((d30 & 0x807000000000000l) != 0 || (d31 & 0xe01) != 0) {
            
            if(
               
               (d30 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d31 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d31 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d30 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d30 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d30 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d31 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d31 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 3);
                return;
            }
        }
        
        long d40 = 0, d41 = 0;
        d40 = d30;
        d41 = d31;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d40, d41);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d40 |= (d40 & 0xfbff7feffdffbffl) << 1;
        d41 |= (d41 & 0x7feffdffbff7fefl) << 1;
        d40 |= (d40 & 0x1f7feffdffbff7fel) >> 1;
        d41 |= (d41 & 0xffdffbff7feffdel) >> 1;

        d41 |= ((d41 & 0x1ffffffffffffl) << 11) | (d40 >> 50);
        d40 |= (d40 & 0x3ffffffffffffl) << 11;
        d40 |= (d40 >> 11) | ((d41 & 0x7ffl) << 50);
        d41 |= d41 >> 11;
        d40 &= CellTracker.localPassable0;
        d41 &= CellTracker.localPassable1;
        if((d40 & 0x807000000000000l) != 0 || (d41 & 0xe01) != 0) {
            
            if(
               
               (d40 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d41 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d41 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d40 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d40 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d40 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d41 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d41 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 4);
                return;
            }
        }
        
        long d50 = 0, d51 = 0;
        d50 = d40;
        d51 = d41;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d50, d51);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d50 |= (d50 & 0xfbff7feffdffbffl) << 1;
        d51 |= (d51 & 0x7feffdffbff7fefl) << 1;
        d50 |= (d50 & 0x1f7feffdffbff7fel) >> 1;
        d51 |= (d51 & 0xffdffbff7feffdel) >> 1;

        d51 |= ((d51 & 0x1ffffffffffffl) << 11) | (d50 >> 50);
        d50 |= (d50 & 0x3ffffffffffffl) << 11;
        d50 |= (d50 >> 11) | ((d51 & 0x7ffl) << 50);
        d51 |= d51 >> 11;
        d50 &= CellTracker.localPassable0;
        d51 &= CellTracker.localPassable1;
        if((d50 & 0x807000000000000l) != 0 || (d51 & 0xe01) != 0) {
            
            if(
               
               (d50 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d51 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d51 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d50 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d50 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d50 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d51 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d51 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 5);
                return;
            }
        }
        
        long d60 = 0, d61 = 0;
        d60 = d50;
        d61 = d51;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d60, d61);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d60 |= (d60 & 0xfbff7feffdffbffl) << 1;
        d61 |= (d61 & 0x7feffdffbff7fefl) << 1;
        d60 |= (d60 & 0x1f7feffdffbff7fel) >> 1;
        d61 |= (d61 & 0xffdffbff7feffdel) >> 1;

        d61 |= ((d61 & 0x1ffffffffffffl) << 11) | (d60 >> 50);
        d60 |= (d60 & 0x3ffffffffffffl) << 11;
        d60 |= (d60 >> 11) | ((d61 & 0x7ffl) << 50);
        d61 |= d61 >> 11;
        d60 &= CellTracker.localPassable0;
        d61 &= CellTracker.localPassable1;
        if((d60 & 0x807000000000000l) != 0 || (d61 & 0xe01) != 0) {
            
            if(
               
               (d60 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d61 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d61 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d60 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d60 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d60 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d61 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d61 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 6);
                return;
            }
        }
        
        long d70 = 0, d71 = 0;
        d70 = d60;
        d71 = d61;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d70, d71);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d70 |= (d70 & 0xfbff7feffdffbffl) << 1;
        d71 |= (d71 & 0x7feffdffbff7fefl) << 1;
        d70 |= (d70 & 0x1f7feffdffbff7fel) >> 1;
        d71 |= (d71 & 0xffdffbff7feffdel) >> 1;

        d71 |= ((d71 & 0x1ffffffffffffl) << 11) | (d70 >> 50);
        d70 |= (d70 & 0x3ffffffffffffl) << 11;
        d70 |= (d70 >> 11) | ((d71 & 0x7ffl) << 50);
        d71 |= d71 >> 11;
        d70 &= CellTracker.localPassable0;
        d71 &= CellTracker.localPassable1;
        if((d70 & 0x807000000000000l) != 0 || (d71 & 0xe01) != 0) {
            
            if(
               
               (d70 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d71 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d71 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d70 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d70 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d70 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d71 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d71 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 7);
                return;
            }
        }
        
        long d80 = 0, d81 = 0;
        d80 = d70;
        d81 = d71;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d80, d81);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d80 |= (d80 & 0xfbff7feffdffbffl) << 1;
        d81 |= (d81 & 0x7feffdffbff7fefl) << 1;
        d80 |= (d80 & 0x1f7feffdffbff7fel) >> 1;
        d81 |= (d81 & 0xffdffbff7feffdel) >> 1;

        d81 |= ((d81 & 0x1ffffffffffffl) << 11) | (d80 >> 50);
        d80 |= (d80 & 0x3ffffffffffffl) << 11;
        d80 |= (d80 >> 11) | ((d81 & 0x7ffl) << 50);
        d81 |= d81 >> 11;
        d80 &= CellTracker.localPassable0;
        d81 &= CellTracker.localPassable1;
        if((d80 & 0x807000000000000l) != 0 || (d81 & 0xe01) != 0) {
            
            if(
               
               (d80 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d81 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d81 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d80 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d80 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d80 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d81 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d81 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 8);
                return;
            }
        }
        
        long d90 = 0, d91 = 0;
        d90 = d80;
        d91 = d81;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d90, d91);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d90 |= (d90 & 0xfbff7feffdffbffl) << 1;
        d91 |= (d91 & 0x7feffdffbff7fefl) << 1;
        d90 |= (d90 & 0x1f7feffdffbff7fel) >> 1;
        d91 |= (d91 & 0xffdffbff7feffdel) >> 1;

        d91 |= ((d91 & 0x1ffffffffffffl) << 11) | (d90 >> 50);
        d90 |= (d90 & 0x3ffffffffffffl) << 11;
        d90 |= (d90 >> 11) | ((d91 & 0x7ffl) << 50);
        d91 |= d91 >> 11;
        d90 &= CellTracker.localPassable0;
        d91 &= CellTracker.localPassable1;
        if((d90 & 0x807000000000000l) != 0 || (d91 & 0xe01) != 0) {
            
            if(
               
               (d90 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d91 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d91 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d90 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d90 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d90 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d91 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d91 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 9);
                return;
            }
        }
        
        long d100 = 0, d101 = 0;
        d100 = d90;
        d101 = d91;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d100, d101);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d100 |= (d100 & 0xfbff7feffdffbffl) << 1;
        d101 |= (d101 & 0x7feffdffbff7fefl) << 1;
        d100 |= (d100 & 0x1f7feffdffbff7fel) >> 1;
        d101 |= (d101 & 0xffdffbff7feffdel) >> 1;

        d101 |= ((d101 & 0x1ffffffffffffl) << 11) | (d100 >> 50);
        d100 |= (d100 & 0x3ffffffffffffl) << 11;
        d100 |= (d100 >> 11) | ((d101 & 0x7ffl) << 50);
        d101 |= d101 >> 11;
        d100 &= CellTracker.localPassable0;
        d101 &= CellTracker.localPassable1;
        if((d100 & 0x807000000000000l) != 0 || (d101 & 0xe01) != 0) {
            
            if(
               
               (d100 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d101 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d101 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d100 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d100 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d100 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d101 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d101 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 10);
                return;
            }
        }
        
        long d110 = 0, d111 = 0;
        d110 = d100;
        d111 = d101;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d110, d111);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d110 |= (d110 & 0xfbff7feffdffbffl) << 1;
        d111 |= (d111 & 0x7feffdffbff7fefl) << 1;
        d110 |= (d110 & 0x1f7feffdffbff7fel) >> 1;
        d111 |= (d111 & 0xffdffbff7feffdel) >> 1;

        d111 |= ((d111 & 0x1ffffffffffffl) << 11) | (d110 >> 50);
        d110 |= (d110 & 0x3ffffffffffffl) << 11;
        d110 |= (d110 >> 11) | ((d111 & 0x7ffl) << 50);
        d111 |= d111 >> 11;
        d110 &= CellTracker.localPassable0;
        d111 &= CellTracker.localPassable1;
        if((d110 & 0x807000000000000l) != 0 || (d111 & 0xe01) != 0) {
            
            if(
               
               (d110 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d111 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d111 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d110 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d110 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d110 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d111 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d111 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 11);
                return;
            }
        }
        
        long d120 = 0, d121 = 0;
        d120 = d110;
        d121 = d111;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d120, d121);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d120 |= (d120 & 0xfbff7feffdffbffl) << 1;
        d121 |= (d121 & 0x7feffdffbff7fefl) << 1;
        d120 |= (d120 & 0x1f7feffdffbff7fel) >> 1;
        d121 |= (d121 & 0xffdffbff7feffdel) >> 1;

        d121 |= ((d121 & 0x1ffffffffffffl) << 11) | (d120 >> 50);
        d120 |= (d120 & 0x3ffffffffffffl) << 11;
        d120 |= (d120 >> 11) | ((d121 & 0x7ffl) << 50);
        d121 |= d121 >> 11;
        d120 &= CellTracker.localPassable0;
        d121 &= CellTracker.localPassable1;
        if((d120 & 0x807000000000000l) != 0 || (d121 & 0xe01) != 0) {
            
            if(
               
               (d120 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d121 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d121 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d120 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d120 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d120 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d121 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d121 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 12);
                return;
            }
        }
        
        long d130 = 0, d131 = 0;
        d130 = d120;
        d131 = d121;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d130, d131);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d130 |= (d130 & 0xfbff7feffdffbffl) << 1;
        d131 |= (d131 & 0x7feffdffbff7fefl) << 1;
        d130 |= (d130 & 0x1f7feffdffbff7fel) >> 1;
        d131 |= (d131 & 0xffdffbff7feffdel) >> 1;

        d131 |= ((d131 & 0x1ffffffffffffl) << 11) | (d130 >> 50);
        d130 |= (d130 & 0x3ffffffffffffl) << 11;
        d130 |= (d130 >> 11) | ((d131 & 0x7ffl) << 50);
        d131 |= d131 >> 11;
        d130 &= CellTracker.localPassable0;
        d131 &= CellTracker.localPassable1;
        if((d130 & 0x807000000000000l) != 0 || (d131 & 0xe01) != 0) {
            
            if(
               
               (d130 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d131 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d131 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d130 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d130 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d130 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d131 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d131 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 13);
                return;
            }
        }
        
        long d140 = 0, d141 = 0;
        d140 = d130;
        d141 = d131;

        // if(rc.getRoundNum() >= 303) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(CellTracker.localPassable0, CellTracker.localPassable1);
        //     Clock.yield();
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d140, d141);
        //     Clock.yield();
        // }

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d140 |= (d140 & 0xfbff7feffdffbffl) << 1;
        d141 |= (d141 & 0x7feffdffbff7fefl) << 1;
        d140 |= (d140 & 0x1f7feffdffbff7fel) >> 1;
        d141 |= (d141 & 0xffdffbff7feffdel) >> 1;

        d141 |= ((d141 & 0x1ffffffffffffl) << 11) | (d140 >> 50);
        d140 |= (d140 & 0x3ffffffffffffl) << 11;
        d140 |= (d140 >> 11) | ((d141 & 0x7ffl) << 50);
        d141 |= d141 >> 11;
        d140 &= CellTracker.localPassable0;
        d141 &= CellTracker.localPassable1;
        if((d140 & 0x807000000000000l) != 0 || (d141 & 0xe01) != 0) {
            
            if(
               
               (d140 & (1l << 49))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d141 & (1l << 0))
               
               != 0) {
                var nextLoc = loc.add(Direction.EAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.EAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d141 & (1l << 10))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTH);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTH;
                    bestScore = score;
                }
            }
            
            if(
               
               (d140 & (1l << 59))
               
               != 0) {
                var nextLoc = loc.add(Direction.WEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.WEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d140 & (1l << 48))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d140 & (1l << 50))
               
               != 0) {
                var nextLoc = loc.add(Direction.SOUTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.SOUTHEAST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d141 & (1l << 9))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHWEST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHWEST;
                    bestScore = score;
                }
            }
            
            if(
               
               (d141 & (1l << 11))
               
               != 0) {
                var nextLoc = loc.add(Direction.NORTHEAST);
                double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bugPos);
                if(score < bestScore) {
                    bestMove = Direction.NORTHEAST;
                    bestScore = score;
                }
            }
            
            if(bestMove != null) {
                makeMove(bestMove);
                // System.out.println("iterations: " + 14);
                return;
            }
        }
        
        assert false : "TODO: fallback when bfs can't find path";
    }
}