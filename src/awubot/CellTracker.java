package awubot;

import battlecode.common.*;

import awubot.util.Profiler;
import static awubot.util.Util.*;
import static awubot.Game.*;

public class CellTracker {
    
    static long seen0, passable0;
    
    static long seen1, passable1;
    
    static long seen2, passable2;
    
    static long seen3, passable3;
    
    static long seen4, passable4;
    
    static long seen5, passable5;
    
    static long seen6, passable6;
    
    static long seen7, passable7;
    
    static long seen8, passable8;
    
    static long seen9, passable9;
    
    static long seen10, passable10;
    
    static long seen11, passable11;
    
    static long seen12, passable12;
    
    static long seen13, passable13;
    
    static long seen14, passable14;
    
    static long seen15, passable15;
    
    static long seen16, passable16;
    
    static long seen17, passable17;
    
    static long seen18, passable18;
    
    static long seen19, passable19;
    
    static long seen20, passable20;
    
    static long seen21, passable21;
    
    static long seen22, passable22;
    
    static long seen23, passable23;
    
    static long seen24, passable24;
    
    static long seen25, passable25;
    
    static long seen26, passable26;
    
    static long seen27, passable27;
    
    static long seen28, passable28;
    
    static long seen29, passable29;
    
    static long seen30, passable30;
    
    static long seen31, passable31;
    
    static long seen32, passable32;
    
    static long seen33, passable33;
    
    static long seen34, passable34;
    
    static long seen35, passable35;
    
    static long seen36, passable36;
    
    static long seen37, passable37;
    
    static long seen38, passable38;
    
    static long seen39, passable39;
    
    static long seen40, passable40;
    
    static long seen41, passable41;
    
    static long seen42, passable42;
    
    static long seen43, passable43;
    
    static long seen44, passable44;
    
    static long seen45, passable45;
    
    static long seen46, passable46;
    
    static long seen47, passable47;
    
    static long seen48, passable48;
    
    static long seen49, passable49;
    
    static long seen50, passable50;
    
    static long seen51, passable51;
    
    static long seen52, passable52;
    
    static long seen53, passable53;
    
    static long seen54, passable54;
    
    static long seen55, passable55;
    
    static long seen56, passable56;
    
    static long seen57, passable57;
    
    static long seen58, passable58;
    
    static long seen59, passable59;
    

    public static void storeCell(MapInfo info) throws GameActionException {
        var loc = info.getMapLocation();
        // rc.setIndicatorDot(loc, 255, 255, 0);
        switch(loc.y) {
            
            case 0 -> {
                seen0 |= 1l << loc.x;
                if(info.isPassable()) passable0 |= 1l << loc.x;
            }
            
            case 1 -> {
                seen1 |= 1l << loc.x;
                if(info.isPassable()) passable1 |= 1l << loc.x;
            }
            
            case 2 -> {
                seen2 |= 1l << loc.x;
                if(info.isPassable()) passable2 |= 1l << loc.x;
            }
            
            case 3 -> {
                seen3 |= 1l << loc.x;
                if(info.isPassable()) passable3 |= 1l << loc.x;
            }
            
            case 4 -> {
                seen4 |= 1l << loc.x;
                if(info.isPassable()) passable4 |= 1l << loc.x;
            }
            
            case 5 -> {
                seen5 |= 1l << loc.x;
                if(info.isPassable()) passable5 |= 1l << loc.x;
            }
            
            case 6 -> {
                seen6 |= 1l << loc.x;
                if(info.isPassable()) passable6 |= 1l << loc.x;
            }
            
            case 7 -> {
                seen7 |= 1l << loc.x;
                if(info.isPassable()) passable7 |= 1l << loc.x;
            }
            
            case 8 -> {
                seen8 |= 1l << loc.x;
                if(info.isPassable()) passable8 |= 1l << loc.x;
            }
            
            case 9 -> {
                seen9 |= 1l << loc.x;
                if(info.isPassable()) passable9 |= 1l << loc.x;
            }
            
            case 10 -> {
                seen10 |= 1l << loc.x;
                if(info.isPassable()) passable10 |= 1l << loc.x;
            }
            
            case 11 -> {
                seen11 |= 1l << loc.x;
                if(info.isPassable()) passable11 |= 1l << loc.x;
            }
            
            case 12 -> {
                seen12 |= 1l << loc.x;
                if(info.isPassable()) passable12 |= 1l << loc.x;
            }
            
            case 13 -> {
                seen13 |= 1l << loc.x;
                if(info.isPassable()) passable13 |= 1l << loc.x;
            }
            
            case 14 -> {
                seen14 |= 1l << loc.x;
                if(info.isPassable()) passable14 |= 1l << loc.x;
            }
            
            case 15 -> {
                seen15 |= 1l << loc.x;
                if(info.isPassable()) passable15 |= 1l << loc.x;
            }
            
            case 16 -> {
                seen16 |= 1l << loc.x;
                if(info.isPassable()) passable16 |= 1l << loc.x;
            }
            
            case 17 -> {
                seen17 |= 1l << loc.x;
                if(info.isPassable()) passable17 |= 1l << loc.x;
            }
            
            case 18 -> {
                seen18 |= 1l << loc.x;
                if(info.isPassable()) passable18 |= 1l << loc.x;
            }
            
            case 19 -> {
                seen19 |= 1l << loc.x;
                if(info.isPassable()) passable19 |= 1l << loc.x;
            }
            
            case 20 -> {
                seen20 |= 1l << loc.x;
                if(info.isPassable()) passable20 |= 1l << loc.x;
            }
            
            case 21 -> {
                seen21 |= 1l << loc.x;
                if(info.isPassable()) passable21 |= 1l << loc.x;
            }
            
            case 22 -> {
                seen22 |= 1l << loc.x;
                if(info.isPassable()) passable22 |= 1l << loc.x;
            }
            
            case 23 -> {
                seen23 |= 1l << loc.x;
                if(info.isPassable()) passable23 |= 1l << loc.x;
            }
            
            case 24 -> {
                seen24 |= 1l << loc.x;
                if(info.isPassable()) passable24 |= 1l << loc.x;
            }
            
            case 25 -> {
                seen25 |= 1l << loc.x;
                if(info.isPassable()) passable25 |= 1l << loc.x;
            }
            
            case 26 -> {
                seen26 |= 1l << loc.x;
                if(info.isPassable()) passable26 |= 1l << loc.x;
            }
            
            case 27 -> {
                seen27 |= 1l << loc.x;
                if(info.isPassable()) passable27 |= 1l << loc.x;
            }
            
            case 28 -> {
                seen28 |= 1l << loc.x;
                if(info.isPassable()) passable28 |= 1l << loc.x;
            }
            
            case 29 -> {
                seen29 |= 1l << loc.x;
                if(info.isPassable()) passable29 |= 1l << loc.x;
            }
            
            case 30 -> {
                seen30 |= 1l << loc.x;
                if(info.isPassable()) passable30 |= 1l << loc.x;
            }
            
            case 31 -> {
                seen31 |= 1l << loc.x;
                if(info.isPassable()) passable31 |= 1l << loc.x;
            }
            
            case 32 -> {
                seen32 |= 1l << loc.x;
                if(info.isPassable()) passable32 |= 1l << loc.x;
            }
            
            case 33 -> {
                seen33 |= 1l << loc.x;
                if(info.isPassable()) passable33 |= 1l << loc.x;
            }
            
            case 34 -> {
                seen34 |= 1l << loc.x;
                if(info.isPassable()) passable34 |= 1l << loc.x;
            }
            
            case 35 -> {
                seen35 |= 1l << loc.x;
                if(info.isPassable()) passable35 |= 1l << loc.x;
            }
            
            case 36 -> {
                seen36 |= 1l << loc.x;
                if(info.isPassable()) passable36 |= 1l << loc.x;
            }
            
            case 37 -> {
                seen37 |= 1l << loc.x;
                if(info.isPassable()) passable37 |= 1l << loc.x;
            }
            
            case 38 -> {
                seen38 |= 1l << loc.x;
                if(info.isPassable()) passable38 |= 1l << loc.x;
            }
            
            case 39 -> {
                seen39 |= 1l << loc.x;
                if(info.isPassable()) passable39 |= 1l << loc.x;
            }
            
            case 40 -> {
                seen40 |= 1l << loc.x;
                if(info.isPassable()) passable40 |= 1l << loc.x;
            }
            
            case 41 -> {
                seen41 |= 1l << loc.x;
                if(info.isPassable()) passable41 |= 1l << loc.x;
            }
            
            case 42 -> {
                seen42 |= 1l << loc.x;
                if(info.isPassable()) passable42 |= 1l << loc.x;
            }
            
            case 43 -> {
                seen43 |= 1l << loc.x;
                if(info.isPassable()) passable43 |= 1l << loc.x;
            }
            
            case 44 -> {
                seen44 |= 1l << loc.x;
                if(info.isPassable()) passable44 |= 1l << loc.x;
            }
            
            case 45 -> {
                seen45 |= 1l << loc.x;
                if(info.isPassable()) passable45 |= 1l << loc.x;
            }
            
            case 46 -> {
                seen46 |= 1l << loc.x;
                if(info.isPassable()) passable46 |= 1l << loc.x;
            }
            
            case 47 -> {
                seen47 |= 1l << loc.x;
                if(info.isPassable()) passable47 |= 1l << loc.x;
            }
            
            case 48 -> {
                seen48 |= 1l << loc.x;
                if(info.isPassable()) passable48 |= 1l << loc.x;
            }
            
            case 49 -> {
                seen49 |= 1l << loc.x;
                if(info.isPassable()) passable49 |= 1l << loc.x;
            }
            
            case 50 -> {
                seen50 |= 1l << loc.x;
                if(info.isPassable()) passable50 |= 1l << loc.x;
            }
            
            case 51 -> {
                seen51 |= 1l << loc.x;
                if(info.isPassable()) passable51 |= 1l << loc.x;
            }
            
            case 52 -> {
                seen52 |= 1l << loc.x;
                if(info.isPassable()) passable52 |= 1l << loc.x;
            }
            
            case 53 -> {
                seen53 |= 1l << loc.x;
                if(info.isPassable()) passable53 |= 1l << loc.x;
            }
            
            case 54 -> {
                seen54 |= 1l << loc.x;
                if(info.isPassable()) passable54 |= 1l << loc.x;
            }
            
            case 55 -> {
                seen55 |= 1l << loc.x;
                if(info.isPassable()) passable55 |= 1l << loc.x;
            }
            
            case 56 -> {
                seen56 |= 1l << loc.x;
                if(info.isPassable()) passable56 |= 1l << loc.x;
            }
            
            case 57 -> {
                seen57 |= 1l << loc.x;
                if(info.isPassable()) passable57 |= 1l << loc.x;
            }
            
            case 58 -> {
                seen58 |= 1l << loc.x;
                if(info.isPassable()) passable58 |= 1l << loc.x;
            }
            
            case 59 -> {
                seen59 |= 1l << loc.x;
                if(info.isPassable()) passable59 |= 1l << loc.x;
            }
            
        }
    }

    
    public static boolean isSeen(MapLocation loc) {
        switch(loc.y) {
            
            case 0 -> {
                return (seen0 & (1l << loc.x)) != 0;
            }
            
            case 1 -> {
                return (seen1 & (1l << loc.x)) != 0;
            }
            
            case 2 -> {
                return (seen2 & (1l << loc.x)) != 0;
            }
            
            case 3 -> {
                return (seen3 & (1l << loc.x)) != 0;
            }
            
            case 4 -> {
                return (seen4 & (1l << loc.x)) != 0;
            }
            
            case 5 -> {
                return (seen5 & (1l << loc.x)) != 0;
            }
            
            case 6 -> {
                return (seen6 & (1l << loc.x)) != 0;
            }
            
            case 7 -> {
                return (seen7 & (1l << loc.x)) != 0;
            }
            
            case 8 -> {
                return (seen8 & (1l << loc.x)) != 0;
            }
            
            case 9 -> {
                return (seen9 & (1l << loc.x)) != 0;
            }
            
            case 10 -> {
                return (seen10 & (1l << loc.x)) != 0;
            }
            
            case 11 -> {
                return (seen11 & (1l << loc.x)) != 0;
            }
            
            case 12 -> {
                return (seen12 & (1l << loc.x)) != 0;
            }
            
            case 13 -> {
                return (seen13 & (1l << loc.x)) != 0;
            }
            
            case 14 -> {
                return (seen14 & (1l << loc.x)) != 0;
            }
            
            case 15 -> {
                return (seen15 & (1l << loc.x)) != 0;
            }
            
            case 16 -> {
                return (seen16 & (1l << loc.x)) != 0;
            }
            
            case 17 -> {
                return (seen17 & (1l << loc.x)) != 0;
            }
            
            case 18 -> {
                return (seen18 & (1l << loc.x)) != 0;
            }
            
            case 19 -> {
                return (seen19 & (1l << loc.x)) != 0;
            }
            
            case 20 -> {
                return (seen20 & (1l << loc.x)) != 0;
            }
            
            case 21 -> {
                return (seen21 & (1l << loc.x)) != 0;
            }
            
            case 22 -> {
                return (seen22 & (1l << loc.x)) != 0;
            }
            
            case 23 -> {
                return (seen23 & (1l << loc.x)) != 0;
            }
            
            case 24 -> {
                return (seen24 & (1l << loc.x)) != 0;
            }
            
            case 25 -> {
                return (seen25 & (1l << loc.x)) != 0;
            }
            
            case 26 -> {
                return (seen26 & (1l << loc.x)) != 0;
            }
            
            case 27 -> {
                return (seen27 & (1l << loc.x)) != 0;
            }
            
            case 28 -> {
                return (seen28 & (1l << loc.x)) != 0;
            }
            
            case 29 -> {
                return (seen29 & (1l << loc.x)) != 0;
            }
            
            case 30 -> {
                return (seen30 & (1l << loc.x)) != 0;
            }
            
            case 31 -> {
                return (seen31 & (1l << loc.x)) != 0;
            }
            
            case 32 -> {
                return (seen32 & (1l << loc.x)) != 0;
            }
            
            case 33 -> {
                return (seen33 & (1l << loc.x)) != 0;
            }
            
            case 34 -> {
                return (seen34 & (1l << loc.x)) != 0;
            }
            
            case 35 -> {
                return (seen35 & (1l << loc.x)) != 0;
            }
            
            case 36 -> {
                return (seen36 & (1l << loc.x)) != 0;
            }
            
            case 37 -> {
                return (seen37 & (1l << loc.x)) != 0;
            }
            
            case 38 -> {
                return (seen38 & (1l << loc.x)) != 0;
            }
            
            case 39 -> {
                return (seen39 & (1l << loc.x)) != 0;
            }
            
            case 40 -> {
                return (seen40 & (1l << loc.x)) != 0;
            }
            
            case 41 -> {
                return (seen41 & (1l << loc.x)) != 0;
            }
            
            case 42 -> {
                return (seen42 & (1l << loc.x)) != 0;
            }
            
            case 43 -> {
                return (seen43 & (1l << loc.x)) != 0;
            }
            
            case 44 -> {
                return (seen44 & (1l << loc.x)) != 0;
            }
            
            case 45 -> {
                return (seen45 & (1l << loc.x)) != 0;
            }
            
            case 46 -> {
                return (seen46 & (1l << loc.x)) != 0;
            }
            
            case 47 -> {
                return (seen47 & (1l << loc.x)) != 0;
            }
            
            case 48 -> {
                return (seen48 & (1l << loc.x)) != 0;
            }
            
            case 49 -> {
                return (seen49 & (1l << loc.x)) != 0;
            }
            
            case 50 -> {
                return (seen50 & (1l << loc.x)) != 0;
            }
            
            case 51 -> {
                return (seen51 & (1l << loc.x)) != 0;
            }
            
            case 52 -> {
                return (seen52 & (1l << loc.x)) != 0;
            }
            
            case 53 -> {
                return (seen53 & (1l << loc.x)) != 0;
            }
            
            case 54 -> {
                return (seen54 & (1l << loc.x)) != 0;
            }
            
            case 55 -> {
                return (seen55 & (1l << loc.x)) != 0;
            }
            
            case 56 -> {
                return (seen56 & (1l << loc.x)) != 0;
            }
            
            case 57 -> {
                return (seen57 & (1l << loc.x)) != 0;
            }
            
            case 58 -> {
                return (seen58 & (1l << loc.x)) != 0;
            }
            
            case 59 -> {
                return (seen59 & (1l << loc.x)) != 0;
            }
            
            default -> {
                return false;
            }
        }
    }
    
    public static boolean isPassable(MapLocation loc) {
        switch(loc.y) {
            
            case 0 -> {
                return (passable0 & (1l << loc.x)) != 0;
            }
            
            case 1 -> {
                return (passable1 & (1l << loc.x)) != 0;
            }
            
            case 2 -> {
                return (passable2 & (1l << loc.x)) != 0;
            }
            
            case 3 -> {
                return (passable3 & (1l << loc.x)) != 0;
            }
            
            case 4 -> {
                return (passable4 & (1l << loc.x)) != 0;
            }
            
            case 5 -> {
                return (passable5 & (1l << loc.x)) != 0;
            }
            
            case 6 -> {
                return (passable6 & (1l << loc.x)) != 0;
            }
            
            case 7 -> {
                return (passable7 & (1l << loc.x)) != 0;
            }
            
            case 8 -> {
                return (passable8 & (1l << loc.x)) != 0;
            }
            
            case 9 -> {
                return (passable9 & (1l << loc.x)) != 0;
            }
            
            case 10 -> {
                return (passable10 & (1l << loc.x)) != 0;
            }
            
            case 11 -> {
                return (passable11 & (1l << loc.x)) != 0;
            }
            
            case 12 -> {
                return (passable12 & (1l << loc.x)) != 0;
            }
            
            case 13 -> {
                return (passable13 & (1l << loc.x)) != 0;
            }
            
            case 14 -> {
                return (passable14 & (1l << loc.x)) != 0;
            }
            
            case 15 -> {
                return (passable15 & (1l << loc.x)) != 0;
            }
            
            case 16 -> {
                return (passable16 & (1l << loc.x)) != 0;
            }
            
            case 17 -> {
                return (passable17 & (1l << loc.x)) != 0;
            }
            
            case 18 -> {
                return (passable18 & (1l << loc.x)) != 0;
            }
            
            case 19 -> {
                return (passable19 & (1l << loc.x)) != 0;
            }
            
            case 20 -> {
                return (passable20 & (1l << loc.x)) != 0;
            }
            
            case 21 -> {
                return (passable21 & (1l << loc.x)) != 0;
            }
            
            case 22 -> {
                return (passable22 & (1l << loc.x)) != 0;
            }
            
            case 23 -> {
                return (passable23 & (1l << loc.x)) != 0;
            }
            
            case 24 -> {
                return (passable24 & (1l << loc.x)) != 0;
            }
            
            case 25 -> {
                return (passable25 & (1l << loc.x)) != 0;
            }
            
            case 26 -> {
                return (passable26 & (1l << loc.x)) != 0;
            }
            
            case 27 -> {
                return (passable27 & (1l << loc.x)) != 0;
            }
            
            case 28 -> {
                return (passable28 & (1l << loc.x)) != 0;
            }
            
            case 29 -> {
                return (passable29 & (1l << loc.x)) != 0;
            }
            
            case 30 -> {
                return (passable30 & (1l << loc.x)) != 0;
            }
            
            case 31 -> {
                return (passable31 & (1l << loc.x)) != 0;
            }
            
            case 32 -> {
                return (passable32 & (1l << loc.x)) != 0;
            }
            
            case 33 -> {
                return (passable33 & (1l << loc.x)) != 0;
            }
            
            case 34 -> {
                return (passable34 & (1l << loc.x)) != 0;
            }
            
            case 35 -> {
                return (passable35 & (1l << loc.x)) != 0;
            }
            
            case 36 -> {
                return (passable36 & (1l << loc.x)) != 0;
            }
            
            case 37 -> {
                return (passable37 & (1l << loc.x)) != 0;
            }
            
            case 38 -> {
                return (passable38 & (1l << loc.x)) != 0;
            }
            
            case 39 -> {
                return (passable39 & (1l << loc.x)) != 0;
            }
            
            case 40 -> {
                return (passable40 & (1l << loc.x)) != 0;
            }
            
            case 41 -> {
                return (passable41 & (1l << loc.x)) != 0;
            }
            
            case 42 -> {
                return (passable42 & (1l << loc.x)) != 0;
            }
            
            case 43 -> {
                return (passable43 & (1l << loc.x)) != 0;
            }
            
            case 44 -> {
                return (passable44 & (1l << loc.x)) != 0;
            }
            
            case 45 -> {
                return (passable45 & (1l << loc.x)) != 0;
            }
            
            case 46 -> {
                return (passable46 & (1l << loc.x)) != 0;
            }
            
            case 47 -> {
                return (passable47 & (1l << loc.x)) != 0;
            }
            
            case 48 -> {
                return (passable48 & (1l << loc.x)) != 0;
            }
            
            case 49 -> {
                return (passable49 & (1l << loc.x)) != 0;
            }
            
            case 50 -> {
                return (passable50 & (1l << loc.x)) != 0;
            }
            
            case 51 -> {
                return (passable51 & (1l << loc.x)) != 0;
            }
            
            case 52 -> {
                return (passable52 & (1l << loc.x)) != 0;
            }
            
            case 53 -> {
                return (passable53 & (1l << loc.x)) != 0;
            }
            
            case 54 -> {
                return (passable54 & (1l << loc.x)) != 0;
            }
            
            case 55 -> {
                return (passable55 & (1l << loc.x)) != 0;
            }
            
            case 56 -> {
                return (passable56 & (1l << loc.x)) != 0;
            }
            
            case 57 -> {
                return (passable57 & (1l << loc.x)) != 0;
            }
            
            case 58 -> {
                return (passable58 & (1l << loc.x)) != 0;
            }
            
            case 59 -> {
                return (passable59 & (1l << loc.x)) != 0;
            }
            
            default -> {
                return false;
            }
        }
    }
    

    public static void storeVisibleCells() throws GameActionException {
        Profiler.begin();
        var loc = rc.getLocation();
        switch(rc.getDirection()) {
            
            case Direction.NORTH -> {
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                       
                

                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                
                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                

                       
                

                
                
                
            }
            
            case Direction.NORTHEAST -> {
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 4 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
            }
            
            case Direction.EAST -> {
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 1 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 2 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 4 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
            }
            
            case Direction.SOUTHEAST -> {
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 1 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 2 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 3 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 4 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + 4 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
            }
            
            case Direction.SOUTH -> {
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + 1 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                {
                    int x = loc.x  + 2 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                {
                    int x = loc.x  + 3 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                

                     
                

                
                
                
            }
            
            case Direction.SOUTHWEST -> {
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -4 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
            }
            
            case Direction.WEST -> {
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -4 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -2 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + -1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -1 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                

                      
                

                
                
                
            }
            
            case Direction.NORTHWEST -> {
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -4 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -4 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -3 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -2 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x  + -1 , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 1 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 2 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 3 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                {
                    int x = loc.x , y = loc.y  + 4 ;
                    var l = new MapLocation(x, y);
                    if(rc.canSenseLocation(l)) {
                        CellTracker.storeCell(rc.senseMapInfo(l));
                    }
                }
                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                

                 
                

                
                
                
            }
            
            case Direction.CENTER -> {
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                

                

                
                
                
            }
            
        }
        Profiler.end("storeVisibleCells");
    }

    public static long localPassable0, localPassable1;
    static MapLocation localPassablePos = new MapLocation(-1, -1);
    public static void computeLocalPassable() throws GameActionException {
        if(rc.getLocation().equals(localPassablePos)) return;
        var loc = rc.getLocation();

        storeVisibleCells();

        localPassablePos = loc;
        localPassable0 = 0;
        localPassable1 = 0;
        switch(loc.y) {
            
            case 0 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                
                
                
                
                
                
                
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 1 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                
                
                
                
                
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 2 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                
                
                
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 3 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 4 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 5 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable0 >> (loc.x - 5)
                  : passable0 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 6 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable1 >> (loc.x - 5)
                  : passable1 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 7 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable2 >> (loc.x - 5)
                  : passable2 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 8 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable3 >> (loc.x - 5)
                  : passable3 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 9 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable4 >> (loc.x - 5)
                  : passable4 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 10 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable5 >> (loc.x - 5)
                  : passable5 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 11 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable6 >> (loc.x - 5)
                  : passable6 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 12 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable7 >> (loc.x - 5)
                  : passable7 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 13 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable8 >> (loc.x - 5)
                  : passable8 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 14 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable9 >> (loc.x - 5)
                  : passable9 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 15 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable10 >> (loc.x - 5)
                  : passable10 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 16 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable11 >> (loc.x - 5)
                  : passable11 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 17 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable12 >> (loc.x - 5)
                  : passable12 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 18 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable13 >> (loc.x - 5)
                  : passable13 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 19 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable14 >> (loc.x - 5)
                  : passable14 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 20 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable15 >> (loc.x - 5)
                  : passable15 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 21 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable16 >> (loc.x - 5)
                  : passable16 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 22 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable17 >> (loc.x - 5)
                  : passable17 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 23 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable18 >> (loc.x - 5)
                  : passable18 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 24 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable19 >> (loc.x - 5)
                  : passable19 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 25 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable20 >> (loc.x - 5)
                  : passable20 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 26 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable21 >> (loc.x - 5)
                  : passable21 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 27 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable22 >> (loc.x - 5)
                  : passable22 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 28 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable23 >> (loc.x - 5)
                  : passable23 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 29 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable24 >> (loc.x - 5)
                  : passable24 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 30 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable25 >> (loc.x - 5)
                  : passable25 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 31 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable26 >> (loc.x - 5)
                  : passable26 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 32 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable27 >> (loc.x - 5)
                  : passable27 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 33 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable28 >> (loc.x - 5)
                  : passable28 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 34 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable29 >> (loc.x - 5)
                  : passable29 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 35 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable30 >> (loc.x - 5)
                  : passable30 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 36 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable31 >> (loc.x - 5)
                  : passable31 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 37 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable32 >> (loc.x - 5)
                  : passable32 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 38 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable33 >> (loc.x - 5)
                  : passable33 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 39 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable34 >> (loc.x - 5)
                  : passable34 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 40 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable35 >> (loc.x - 5)
                  : passable35 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 41 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable36 >> (loc.x - 5)
                  : passable36 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 42 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable37 >> (loc.x - 5)
                  : passable37 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 43 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable38 >> (loc.x - 5)
                  : passable38 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 44 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable39 >> (loc.x - 5)
                  : passable39 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 45 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable40 >> (loc.x - 5)
                  : passable40 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 46 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable41 >> (loc.x - 5)
                  : passable41 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 47 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable42 >> (loc.x - 5)
                  : passable42 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 48 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable43 >> (loc.x - 5)
                  : passable43 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 49 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable44 >> (loc.x - 5)
                  : passable44 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 50 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable45 >> (loc.x - 5)
                  : passable45 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 51 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable46 >> (loc.x - 5)
                  : passable46 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 52 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable47 >> (loc.x - 5)
                  : passable47 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 53 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable48 >> (loc.x - 5)
                  : passable48 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 54 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable49 >> (loc.x - 5)
                  : passable49 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff)  << 49 
                ;
                
                
            }
            
            case 55 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable50 >> (loc.x - 5)
                  : passable50 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff)  << 38 
                ;
                
                
                
                
            }
            
            case 56 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable51 >> (loc.x - 5)
                  : passable51 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff)  << 27 
                ;
                
                
                
                
                
                
            }
            
            case 57 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable52 >> (loc.x - 5)
                  : passable52 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff)  << 16 
                ;
                
                
                
                
                
                
                
                
            }
            
            case 58 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable53 >> (loc.x - 5)
                  : passable53 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff)  << 5 
                ;
                
                
                
                
                
                
                
                
                
                
            }
            
            case 59 -> {
                // get the 11 relevant bits from row i + dy, shifted by off
                

                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable54 >> (loc.x - 5)
                  : passable54 << (5 - loc.x))
                 & 0x7ff) 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable55 >> (loc.x - 5)
                  : passable55 << (5 - loc.x))
                 & 0x7ff)  << 11 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable56 >> (loc.x - 5)
                  : passable56 << (5 - loc.x))
                 & 0x7ff)  << 22 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable57 >> (loc.x - 5)
                  : passable57 << (5 - loc.x))
                 & 0x7ff)  << 33 
                ;
                
                
                
                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable58 >> (loc.x - 5)
                  : passable58 << (5 - loc.x))
                 & 0x7ff)  << 44 
                ;
                
                

                localPassable0 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff)  << 55 
                 & 0x1f80000000000000l;
                localPassable1 |= 
                ((loc.x - 5 >= 0
                  ? passable59 >> (loc.x - 5)
                  : passable59 << (5 - loc.x))
                 & 0x7ff) 
                 >> 6;

                
                
                
                
                
                
                
                
                
                
                
            }
            
        }
    }

    public static void printLocalMask(long m1, long m2) {
        indicate("\n");
        for(int i = 10; i >= 0; i--) {
            long row;
            if(i < 5) row = (m1 >> (i * 11)) & 0x7ff;
            else if(i == 5) row = (m1 >> 55) | ((m2 & 0x3f) << 6);
            else row = (m2 >> ((i - 6) * 11 + 5)) & 0x7ff;
            String s = "";
            for(int j = 0; j < 11; j++) {
                s += ((row & (1 << j)) != 0 ? "X " : ". ");
            }
            indicate(s + "\n");
        }
    }
}