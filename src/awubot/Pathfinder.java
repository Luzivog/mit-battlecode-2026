package awubot;

import battlecode.common.*;
import static awubot.util.Util.*;
import awubot.util.Rng;
import awubot.util.Profiler;
import static awubot.Game.*;

// IF TLE: lower these constants (especially MAX_BUG_ITERS)

class BugnavInstance {
    MapLocation target;

    // false for the instances spawned for lookahead
    boolean isMaster;
    // child instances for lookahead
    BugnavInstance leftBug, rightBug;
    // these are reset when we physically move
    boolean leftDone, rightDone;
    // detect movement with this
    MapLocation lastLoc;
    Direction lastDir;;
    // loop detection
    long[] guessedLeft, guessedRight;

    // extra "return variables" for lookahead instances
    boolean hitEdge = false, outOfSight = false, arrived = false;

    Direction bottomDir, topDir;

    // state (you can reconstruct the entire state from these variables + target/bottomDir)
    MapLocation pos;
    int stackSize;
    int emptyStackTurns;
    boolean leftTurn;

    public int nextPathIndex;
    public MapLocation path0;
    public MapLocation path1;
    public MapLocation path2;
    public MapLocation path3;
    public MapLocation path4;
    public MapLocation path5;
    public MapLocation path6;
    public MapLocation path7;
    public MapLocation path8;
    public MapLocation path9;
    public MapLocation path10;
    public MapLocation path11;
    public MapLocation path12;
    public MapLocation path13;
    public MapLocation path14;
    public MapLocation path15;
    public MapLocation path16;
    public MapLocation path17;
    public MapLocation path18;
    public MapLocation path19;
    public MapLocation path20;
    public MapLocation path21;
    public MapLocation path22;
    public MapLocation path23;
    public MapLocation path24;

    void clearPath() {
        pos = myLoc;
        nextPathIndex = 0;
        stackSize = 0;
        emptyStackTurns = 1000;
        bottomDir = topDir = pos.directionTo(target);
        leftBug = rightBug = null;
        path0 = new MapLocation(-1000, -1000);
        path1 = new MapLocation(-1000, -1000);
        path2 = new MapLocation(-1000, -1000);
        path3 = new MapLocation(-1000, -1000);
        path4 = new MapLocation(-1000, -1000);
        path5 = new MapLocation(-1000, -1000);
        path6 = new MapLocation(-1000, -1000);
        path7 = new MapLocation(-1000, -1000);
        path8 = new MapLocation(-1000, -1000);
        path9 = new MapLocation(-1000, -1000);
        path10 = new MapLocation(-1000, -1000);
        path11 = new MapLocation(-1000, -1000);
        path12 = new MapLocation(-1000, -1000);
        path13 = new MapLocation(-1000, -1000);
        path14 = new MapLocation(-1000, -1000);
        path15 = new MapLocation(-1000, -1000);
        path16 = new MapLocation(-1000, -1000);
        path17 = new MapLocation(-1000, -1000);
        path18 = new MapLocation(-1000, -1000);
        path19 = new MapLocation(-1000, -1000);
        path20 = new MapLocation(-1000, -1000);
        path21 = new MapLocation(-1000, -1000);
        path22 = new MapLocation(-1000, -1000);
        path23 = new MapLocation(-1000, -1000);
        path24 = new MapLocation(-1000, -1000);
    }

    BugnavInstance(boolean leftTurn, MapLocation target) {
        this.leftTurn = leftTurn;
        this.target = target;
        this.isMaster = true;
        guessedLeft = new long[mapHeight];
        guessedRight = new long[mapHeight];
        clearPath();
    }

    BugnavInstance(MapLocation pos, boolean leftTurn, MapLocation target, boolean isMaster) {
        this.leftTurn = leftTurn;
        this.target = target;
        this.isMaster = isMaster;
        clearPath();
        this.pos = pos;
        appendCurrentPosition();
    }

    void appendPosition(MapLocation loc) {
        switch (nextPathIndex) {
            case 0 -> {
                if (!path24.equals(loc)) {
                    path0 = loc;
                    nextPathIndex = 1;
                }
            }
            case 1 -> {
                if (!path0.equals(loc)) {
                    path1 = loc;
                    nextPathIndex = 2;
                }
            }
            case 2 -> {
                if (!path1.equals(loc)) {
                    path2 = loc;
                    nextPathIndex = 3;
                }
            }
            case 3 -> {
                if (!path2.equals(loc)) {
                    path3 = loc;
                    nextPathIndex = 4;
                }
            }
            case 4 -> {
                if (!path3.equals(loc)) {
                    path4 = loc;
                    nextPathIndex = 5;
                }
            }
            case 5 -> {
                if (!path4.equals(loc)) {
                    path5 = loc;
                    nextPathIndex = 6;
                }
            }
            case 6 -> {
                if (!path5.equals(loc)) {
                    path6 = loc;
                    nextPathIndex = 7;
                }
            }
            case 7 -> {
                if (!path6.equals(loc)) {
                    path7 = loc;
                    nextPathIndex = 8;
                }
            }
            case 8 -> {
                if (!path7.equals(loc)) {
                    path8 = loc;
                    nextPathIndex = 9;
                }
            }
            case 9 -> {
                if (!path8.equals(loc)) {
                    path9 = loc;
                    nextPathIndex = 10;
                }
            }
            case 10 -> {
                if (!path9.equals(loc)) {
                    path10 = loc;
                    nextPathIndex = 11;
                }
            }
            case 11 -> {
                if (!path10.equals(loc)) {
                    path11 = loc;
                    nextPathIndex = 12;
                }
            }
            case 12 -> {
                if (!path11.equals(loc)) {
                    path12 = loc;
                    nextPathIndex = 13;
                }
            }
            case 13 -> {
                if (!path12.equals(loc)) {
                    path13 = loc;
                    nextPathIndex = 14;
                }
            }
            case 14 -> {
                if (!path13.equals(loc)) {
                    path14 = loc;
                    nextPathIndex = 15;
                }
            }
            case 15 -> {
                if (!path14.equals(loc)) {
                    path15 = loc;
                    nextPathIndex = 16;
                }
            }
            case 16 -> {
                if (!path15.equals(loc)) {
                    path16 = loc;
                    nextPathIndex = 17;
                }
            }
            case 17 -> {
                if (!path16.equals(loc)) {
                    path17 = loc;
                    nextPathIndex = 18;
                }
            }
            case 18 -> {
                if (!path17.equals(loc)) {
                    path18 = loc;
                    nextPathIndex = 19;
                }
            }
            case 19 -> {
                if (!path18.equals(loc)) {
                    path19 = loc;
                    nextPathIndex = 20;
                }
            }
            case 20 -> {
                if (!path19.equals(loc)) {
                    path20 = loc;
                    nextPathIndex = 21;
                }
            }
            case 21 -> {
                if (!path20.equals(loc)) {
                    path21 = loc;
                    nextPathIndex = 22;
                }
            }
            case 22 -> {
                if (!path21.equals(loc)) {
                    path22 = loc;
                    nextPathIndex = 23;
                }
            }
            case 23 -> {
                if (!path22.equals(loc)) {
                    path23 = loc;
                    nextPathIndex = 24;
                }
            }
            case 24 -> {
                if (!path23.equals(loc)) {
                    path24 = loc;
                    nextPathIndex = 0;
                }
            }
            default -> {
                assert false;
            }
        }
    }

    void appendCurrentPosition() {
        appendPosition(pos);
    }

    MapLocation getRecentPos(int i) {
        switch ((nextPathIndex - i + 24) % 25) {
            case 0 -> {
                return path0;
            }
            case 1 -> {
                return path1;
            }
            case 2 -> {
                return path2;
            }
            case 3 -> {
                return path3;
            }
            case 4 -> {
                return path4;
            }
            case 5 -> {
                return path5;
            }
            case 6 -> {
                return path6;
            }
            case 7 -> {
                return path7;
            }
            case 8 -> {
                return path8;
            }
            case 9 -> {
                return path9;
            }
            case 10 -> {
                return path10;
            }
            case 11 -> {
                return path11;
            }
            case 12 -> {
                return path12;
            }
            case 13 -> {
                return path13;
            }
            case 14 -> {
                return path14;
            }
            case 15 -> {
                return path15;
            }
            case 16 -> {
                return path16;
            }
            case 17 -> {
                return path17;
            }
            case 18 -> {
                return path18;
            }
            case 19 -> {
                return path19;
            }
            case 20 -> {
                return path20;
            }
            case 21 -> {
                return path21;
            }
            case 22 -> {
                return path22;
            }
            case 23 -> {
                return path23;
            }
            case 24 -> {
                return path24;
            }
            default -> {
                assert false : "index out of range";
                return null;
            }
        }
    }

    boolean doLookahead() throws GameActionException {
        // indicate("lookahead!!");
        if(leftBug == null) {
            // initialise
            leftBug = new BugnavInstance(pos, true, target, false);
            rightBug = new BugnavInstance(pos, false, target, false);
            lastLoc = new MapLocation(-1, -1);
            lastDir = Direction.CENTER;
            return true;
        }
        rc.setIndicatorDot(leftBug.pos, 255, 255, 0);
        rc.setIndicatorDot(rightBug.pos, 255, 255, 0);
        // Profiler.begin();
        if(!myLoc.equals(lastLoc) || !myDir.equals(lastDir)) {
            // we moved, so we might be able to advance previously stuck stuff further
            lastLoc = myLoc;
            lastDir = myDir;
            leftDone = leftBug.hitEdge || leftBug.nextPathIndex == 24;
            rightDone = rightBug.hitEdge || leftBug.nextPathIndex == 24;
        }

        if(!(leftDone && rightDone)) {
            if(rightDone) {
                            // indicate("advance left");
            leftBug.outOfSight = false;
            leftDone = !leftBug.advance();
            // indicate(leftBug.pos);
            rc.setIndicatorDot(leftBug.pos, 255, 0, 255);

            } else if(leftDone) {
                            // indicate("advance right");
            rightBug.outOfSight = false;
            rightDone = !rightBug.advance();
            // indicate(rightBug.pos);
            rc.setIndicatorDot(rightBug.pos, 255, 0, 255);

            } else if(leftBug.nextPathIndex > rightBug.nextPathIndex) {
                            // indicate("advance right");
            rightBug.outOfSight = false;
            rightDone = !rightBug.advance();
            // indicate(rightBug.pos);
            rc.setIndicatorDot(rightBug.pos, 255, 0, 255);

            } else {
                            // indicate("advance left");
            leftBug.outOfSight = false;
            leftDone = !leftBug.advance();
            // indicate(leftBug.pos);
            rc.setIndicatorDot(leftBug.pos, 255, 0, 255);

            }
        }
        // Profiler.end("lookahead");

        boolean leftReady = leftBug.nextPathIndex >= 5 || leftBug.hitEdge || leftBug.outOfSight || leftBug.arrived;
        boolean rightReady = rightBug.nextPathIndex >= 5 || rightBug.hitEdge || rightBug.outOfSight || rightBug.arrived;
        indicate("ready " + leftReady + " " + rightReady);
        if(!myLoc.equals(pos) && (!leftReady || !rightReady)) return !(leftDone && rightDone);
        indicate("going ahead");

        var leftScore =  Math.sqrt(leftBug.pos.distanceSquaredTo(target)) + leftBug.nextPathIndex * 0.7 ;
        var rightScore =  Math.sqrt(rightBug.pos.distanceSquaredTo(target)) + rightBug.nextPathIndex * 0.7 ;
        indicate("lookahead" + pos);
        // indicate(leftBug.pos);
        indicate(leftScore);
        // indicate(rightBug.pos);
        indicate(rightScore);


        if(leftBug.arrived) {
                    var bug = leftBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
            if(bug.nextPathIndex == 13) break copyPath;
            appendPosition(bug.path13);
            if(bug.nextPathIndex == 14) break copyPath;
            appendPosition(bug.path14);
            if(bug.nextPathIndex == 15) break copyPath;
            appendPosition(bug.path15);
            if(bug.nextPathIndex == 16) break copyPath;
            appendPosition(bug.path16);
            if(bug.nextPathIndex == 17) break copyPath;
            appendPosition(bug.path17);
            if(bug.nextPathIndex == 18) break copyPath;
            appendPosition(bug.path18);
            if(bug.nextPathIndex == 19) break copyPath;
            appendPosition(bug.path19);
            if(bug.nextPathIndex == 20) break copyPath;
            appendPosition(bug.path20);
            if(bug.nextPathIndex == 21) break copyPath;
            appendPosition(bug.path21);
            if(bug.nextPathIndex == 22) break copyPath;
            appendPosition(bug.path22);
            if(bug.nextPathIndex == 23) break copyPath;
            appendPosition(bug.path23);
            if(bug.nextPathIndex == 24) break copyPath;
            appendPosition(bug.path24);
        }
        leftBug = null;
        rightBug = null;

        } else if(leftBug.arrived) {
                    var bug = rightBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
            if(bug.nextPathIndex == 13) break copyPath;
            appendPosition(bug.path13);
            if(bug.nextPathIndex == 14) break copyPath;
            appendPosition(bug.path14);
            if(bug.nextPathIndex == 15) break copyPath;
            appendPosition(bug.path15);
            if(bug.nextPathIndex == 16) break copyPath;
            appendPosition(bug.path16);
            if(bug.nextPathIndex == 17) break copyPath;
            appendPosition(bug.path17);
            if(bug.nextPathIndex == 18) break copyPath;
            appendPosition(bug.path18);
            if(bug.nextPathIndex == 19) break copyPath;
            appendPosition(bug.path19);
            if(bug.nextPathIndex == 20) break copyPath;
            appendPosition(bug.path20);
            if(bug.nextPathIndex == 21) break copyPath;
            appendPosition(bug.path21);
            if(bug.nextPathIndex == 22) break copyPath;
            appendPosition(bug.path22);
            if(bug.nextPathIndex == 23) break copyPath;
            appendPosition(bug.path23);
            if(bug.nextPathIndex == 24) break copyPath;
            appendPosition(bug.path24);
        }
        leftBug = null;
        rightBug = null;

        } else if(leftBug.hitEdge != rightBug.hitEdge) {
                    var bug = rightBug.hitEdge ? leftBug : rightBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
            if(bug.nextPathIndex == 13) break copyPath;
            appendPosition(bug.path13);
            if(bug.nextPathIndex == 14) break copyPath;
            appendPosition(bug.path14);
            if(bug.nextPathIndex == 15) break copyPath;
            appendPosition(bug.path15);
            if(bug.nextPathIndex == 16) break copyPath;
            appendPosition(bug.path16);
            if(bug.nextPathIndex == 17) break copyPath;
            appendPosition(bug.path17);
            if(bug.nextPathIndex == 18) break copyPath;
            appendPosition(bug.path18);
            if(bug.nextPathIndex == 19) break copyPath;
            appendPosition(bug.path19);
            if(bug.nextPathIndex == 20) break copyPath;
            appendPosition(bug.path20);
            if(bug.nextPathIndex == 21) break copyPath;
            appendPosition(bug.path21);
            if(bug.nextPathIndex == 22) break copyPath;
            appendPosition(bug.path22);
            if(bug.nextPathIndex == 23) break copyPath;
            appendPosition(bug.path23);
            if(bug.nextPathIndex == 24) break copyPath;
            appendPosition(bug.path24);
        }
        leftBug = null;
        rightBug = null;

        } else {
            var better = leftScore < rightScore ? leftBug : rightBug;
            var worse = leftScore < rightScore ? rightBug : leftBug;
            if(!better.outOfSight && worse.outOfSight) {
                indicate("clear winner");
                        var bug = better;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
            if(bug.nextPathIndex == 13) break copyPath;
            appendPosition(bug.path13);
            if(bug.nextPathIndex == 14) break copyPath;
            appendPosition(bug.path14);
            if(bug.nextPathIndex == 15) break copyPath;
            appendPosition(bug.path15);
            if(bug.nextPathIndex == 16) break copyPath;
            appendPosition(bug.path16);
            if(bug.nextPathIndex == 17) break copyPath;
            appendPosition(bug.path17);
            if(bug.nextPathIndex == 18) break copyPath;
            appendPosition(bug.path18);
            if(bug.nextPathIndex == 19) break copyPath;
            appendPosition(bug.path19);
            if(bug.nextPathIndex == 20) break copyPath;
            appendPosition(bug.path20);
            if(bug.nextPathIndex == 21) break copyPath;
            appendPosition(bug.path21);
            if(bug.nextPathIndex == 22) break copyPath;
            appendPosition(bug.path22);
            if(bug.nextPathIndex == 23) break copyPath;
            appendPosition(bug.path23);
            if(bug.nextPathIndex == 24) break copyPath;
            appendPosition(bug.path24);
        }
        leftBug = null;
        rightBug = null;

            } else if(!better.outOfSight || myLoc.isAdjacentTo(pos)) {
                indicate("unclear, guessing");
                leftScore = Math.pow(leftScore, 15);
                rightScore = Math.pow(rightScore, 15);
                var total = leftScore + rightScore;
                leftScore /= total;
                rightScore /= total;
                indicate(leftScore);
                indicate(rightScore);
                        var bug = Rng.mcg() < rightScore * 0x7fffffff ? leftBug : rightBug;
        if(bug.leftTurn) {
            guessedLeft[pos.y] |= 1l << pos.x;
        } else {
            guessedRight[pos.y] |= 1l << pos.x;
        }
        pos = bug.pos;
        stackSize = bug.stackSize;
        emptyStackTurns = bug.emptyStackTurns;
        leftTurn = bug.leftTurn;
        bottomDir = bug.bottomDir;
        topDir = bug.topDir;
        copyPath: {
            appendPosition(bug.path0);
            if(bug.nextPathIndex == 1) break copyPath;
            appendPosition(bug.path1);
            if(bug.nextPathIndex == 2) break copyPath;
            appendPosition(bug.path2);
            if(bug.nextPathIndex == 3) break copyPath;
            appendPosition(bug.path3);
            if(bug.nextPathIndex == 4) break copyPath;
            appendPosition(bug.path4);
            if(bug.nextPathIndex == 5) break copyPath;
            appendPosition(bug.path5);
            if(bug.nextPathIndex == 6) break copyPath;
            appendPosition(bug.path6);
            if(bug.nextPathIndex == 7) break copyPath;
            appendPosition(bug.path7);
            if(bug.nextPathIndex == 8) break copyPath;
            appendPosition(bug.path8);
            if(bug.nextPathIndex == 9) break copyPath;
            appendPosition(bug.path9);
            if(bug.nextPathIndex == 10) break copyPath;
            appendPosition(bug.path10);
            if(bug.nextPathIndex == 11) break copyPath;
            appendPosition(bug.path11);
            if(bug.nextPathIndex == 12) break copyPath;
            appendPosition(bug.path12);
            if(bug.nextPathIndex == 13) break copyPath;
            appendPosition(bug.path13);
            if(bug.nextPathIndex == 14) break copyPath;
            appendPosition(bug.path14);
            if(bug.nextPathIndex == 15) break copyPath;
            appendPosition(bug.path15);
            if(bug.nextPathIndex == 16) break copyPath;
            appendPosition(bug.path16);
            if(bug.nextPathIndex == 17) break copyPath;
            appendPosition(bug.path17);
            if(bug.nextPathIndex == 18) break copyPath;
            appendPosition(bug.path18);
            if(bug.nextPathIndex == 19) break copyPath;
            appendPosition(bug.path19);
            if(bug.nextPathIndex == 20) break copyPath;
            appendPosition(bug.path20);
            if(bug.nextPathIndex == 21) break copyPath;
            appendPosition(bug.path21);
            if(bug.nextPathIndex == 22) break copyPath;
            appendPosition(bug.path22);
            if(bug.nextPathIndex == 23) break copyPath;
            appendPosition(bug.path23);
            if(bug.nextPathIndex == 24) break copyPath;
            appendPosition(bug.path24);
        }
        leftBug = null;
        rightBug = null;

            }
        }
        return false;
    }

    // false = halt bug
    boolean advance() throws GameActionException {
        if(leftBug != null) {
            // we are currently in the middle of a lookahead
            return doLookahead();
        }
        // indicate("leftTurn " + leftTurn);

        if(pos.isAdjacentTo(target)) {
            pos = target;
            appendCurrentPosition();
            arrived = true;
            return false;
        }

        var dirToTarget = pos.directionTo(target);
        if(dirToTarget != bottomDir) {
            // indicate("changing bottomDir from " + bottomDir + " to " + dirToTarget);
            if(dirToTarget == Direction.CENTER) return false;
            if(stackSize == 0) {
                bottomDir = topDir = dirToTarget;
            } else {
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
        }
        // indicate("direction " + pos + " " + topDir + " " + bottomDir + " " + leftTurn);

        MapLocation greedyLoc = pos.add(bottomDir);
        if(stackSize <= 1) {
            var greedyPassable = CellTracker.isPassable(greedyLoc);
            var greedyGood = greedyPassable || !CellTracker.isSeen(greedyLoc);
            if(greedyGood) {
                            if(greedyPassable && chebyshevDistance(myLoc, greedyLoc) <= 5) {
                // indicate("greedyLoc " + greedyLoc);
                pos = greedyLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            return false;

            }
            var leftLoc = pos.add(bottomDir.rotateLeft());
            var rightLoc = pos.add(bottomDir.rotateRight());
            var leftPassable = CellTracker.isPassable(leftLoc);
            var leftGood = leftPassable || !CellTracker.isSeen(leftLoc);
            var rightPassable = CellTracker.isPassable(rightLoc);
            var rightGood = rightPassable || !CellTracker.isSeen(rightLoc);
            if(leftGood && !rightGood) {
                            if(leftPassable && chebyshevDistance(myLoc, leftLoc) <= 5) {
                // indicate("leftLoc " + leftLoc);
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            return false;

            }
            if(!leftGood && rightGood) {
                            if(rightPassable && chebyshevDistance(myLoc, rightLoc) <= 5) {
                // indicate("rightLoc " + rightLoc);
                pos = rightLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            return false;

            }
            if(leftGood && rightGood) {
                if(leftLoc.distanceSquaredTo(target) < rightLoc.distanceSquaredTo(target)) {
                                if(leftPassable && chebyshevDistance(myLoc, leftLoc) <= 5) {
                // indicate("leftLoc " + leftLoc);
                pos = leftLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            return false;

                } else {
                                if(rightPassable && chebyshevDistance(myLoc, rightLoc) <= 5) {
                // indicate("rightLoc " + rightLoc);
                pos = rightLoc;
                topDir = bottomDir;
                stackSize = 0;
                emptyStackTurns++;
                appendCurrentPosition();
                return true;
            }
            return false;

                }
            }
        }

        // guess rotation direction (kinda inefficient but wtv)
        if(isMaster && stackSize == 0 && CellTracker.isSeen(greedyLoc)) {
            assert !CellTracker.isPassable(greedyLoc);
            if(emptyStackTurns >= 2) {
                boolean pastLeft = ((guessedLeft[pos.y] >> pos.x) & 1l) != 0;
                boolean pastRight = ((guessedRight[pos.y] >> pos.x) & 1l) != 0;
                if(pastLeft && pastRight) {
                    indicate("idk which way");
                    // just pick randomly, we have no idea what's going on
                    // leftTurn = Rng.mcg() >> 30 == 0;
                    // keep going the same way, we'll get there eventually
                } else if(pastLeft) {
                    indicate("past left");
                    // try the other way
                    leftTurn = false;
                    guessedRight[pos.y] |= 1l << pos.x;
                } else if(pastRight) {
                    indicate("past right");
                    leftTurn = true;
                    guessedLeft[pos.y] |= 1l << pos.x;
                } else {
                    indicate("looking ahead");
                    return doLookahead();
                }
            } else if(emptyStackTurns < 0) {
                emptyStackTurns = 0;
            }
        }

        // Profiler.begin();

        if(leftTurn) {
                    // this loop only exists to allow continue
        while(true) {
            var tryDir = topDir.rotateRight().rotateRight();
            if(stackSize + -2 >= 0
               // only try -2 for diagonals
               && Math.abs(topDir.dx + topDir.dy) != 1
               )
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + -2 + " " + isMaster);
                    // indicate("bugnav " + -2 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + -2 + " " + isMaster);
                    // indicate("bugnav " + -2 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + -2 + " " + isMaster);
                    // indicate("bugnav " + -2 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            if(stackSize + -1 >= 0
               // only try -2 for diagonals
               )
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + -1 + " " + isMaster);
                    // indicate("bugnav " + -1 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + -1 + " " + isMaster);
                    // indicate("bugnav " + -1 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + -1 + " " + isMaster);
                    // indicate("bugnav " + -1 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 0 + " " + isMaster);
                    // indicate("bugnav " + 0 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 0 + " " + isMaster);
                    // indicate("bugnav " + 0 + " " + isMaster);
                    return false;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 0 + " " + isMaster);
                    // indicate("bugnav " + 0 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 1 + " " + isMaster);
                    // indicate("bugnav " + 1 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 1 + " " + isMaster);
                    // indicate("bugnav " + 1 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 1 + " " + isMaster);
                    // indicate("bugnav " + 1 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 2 + " " + isMaster);
                    // indicate("bugnav " + 2 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 2 + " " + isMaster);
                    // indicate("bugnav " + 2 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 2 + " " + isMaster);
                    // indicate("bugnav " + 2 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 3 + " " + isMaster);
                    // indicate("bugnav " + 3 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 3 + " " + isMaster);
                    // indicate("bugnav " + 3 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 3 + " " + isMaster);
                    // indicate("bugnav " + 3 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 4 + " " + isMaster);
                    // indicate("bugnav " + 4 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 4 + " " + isMaster);
                    // indicate("bugnav " + 4 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 4 + " " + isMaster);
                    // indicate("bugnav " + 4 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 5 + " " + isMaster);
                    // indicate("bugnav " + 5 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 5 + " " + isMaster);
                    // indicate("bugnav " + 5 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 5 + " " + isMaster);
                    // indicate("bugnav " + 5 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateLeft();
            assert false : "no valid move found from " + pos;
        }

        } else {
                    // this loop only exists to allow continue
        while(true) {
            var tryDir = topDir.rotateLeft().rotateLeft();
            if(stackSize + -2 >= 0
               // only try -2 for diagonals
               && Math.abs(topDir.dx + topDir.dy) != 1
               )
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + -2 + " " + isMaster);
                    // indicate("bugnav " + -2 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + -2 + " " + isMaster);
                    // indicate("bugnav " + -2 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + -2 + " " + isMaster);
                    // indicate("bugnav " + -2 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            if(stackSize + -1 >= 0
               // only try -2 for diagonals
               )
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + -1 + " " + isMaster);
                    // indicate("bugnav " + -1 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + -1 + " " + isMaster);
                    // indicate("bugnav " + -1 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + -1 + " " + isMaster);
                    // indicate("bugnav " + -1 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 0 + " " + isMaster);
                    // indicate("bugnav " + 0 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 0 + " " + isMaster);
                    // indicate("bugnav " + 0 + " " + isMaster);
                    return false;
                }
                if(CellTracker.isPassable(nextPos)) {
                    pos = nextPos;
                    topDir = tryDir;
                    if(stackSize == 0) {
                        emptyStackTurns++;
                    } else {
                        emptyStackTurns = 0;
                    }
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 0 + " " + isMaster);
                    // indicate("bugnav " + 0 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 1 + " " + isMaster);
                    // indicate("bugnav " + 1 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 1 + " " + isMaster);
                    // indicate("bugnav " + 1 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 1 + " " + isMaster);
                    // indicate("bugnav " + 1 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 2 + " " + isMaster);
                    // indicate("bugnav " + 2 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 2 + " " + isMaster);
                    // indicate("bugnav " + 2 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 2 + " " + isMaster);
                    // indicate("bugnav " + 2 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 3 + " " + isMaster);
                    // indicate("bugnav " + 3 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 3 + " " + isMaster);
                    // indicate("bugnav " + 3 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 3 + " " + isMaster);
                    // indicate("bugnav " + 3 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 4 + " " + isMaster);
                    // indicate("bugnav " + 4 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 4 + " " + isMaster);
                    // indicate("bugnav " + 4 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 4 + " " + isMaster);
                    // indicate("bugnav " + 4 + " " + isMaster);
                    return true;
                }
            }
            tryDir = tryDir.rotateRight();
            {
                // indicate("trying " + tryDir);
                var nextPos = pos.add(tryDir);
                if(!rc.onTheMap(nextPos)) {
                     if(isMaster) {
                        // avoid following the edge of the map (TODO: 1-thick borders)
                        indicate("bugnav hit edge of map");
                        leftTurn = !leftTurn;
                        clearPath();
                        // don't guess turn direction, we know that one direction is bad already
                        emptyStackTurns = -1000;
                        // start again
                        continue;
                    } else {
                        hitEdge = true;
                    }
                    // Profiler.end("bugnav " + 5 + " " + isMaster);
                    // indicate("bugnav " + 5 + " " + isMaster);
                    return false;
                }
                if(!CellTracker.isSeen(nextPos) || CellTracker.isPassable(nextPos) && chebyshevDistance(myLoc, nextPos) > 5) {
                    rc.setIndicatorDot(nextPos, 0, 255, 255);
                    outOfSight = true;
                    // Profiler.end("bugnav " + 5 + " " + isMaster);
                    // indicate("bugnav " + 5 + " " + isMaster);
                    return false;
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
                    appendCurrentPosition();
                    // Profiler.end("bugnav " + 5 + " " + isMaster);
                    // indicate("bugnav " + 5 + " " + isMaster);
                    return true;
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

    static int bfsPathLength = 0;
    static MapLocation bfsPath0;
    static MapLocation bfsPath1;
    static MapLocation bfsPath2;
    static MapLocation bfsPath3;
    static MapLocation bfsPath4;
    static MapLocation bfsPath5;
    static MapLocation bfsPath6;
    static MapLocation bfsPath7;
    static MapLocation bfsPath8;
    static MapLocation bfsPath9;
    static MapLocation bfsPath10;
    static MapLocation bfsPath11;
    static MapLocation bfsPath12;
    static MapLocation bfsPath13;
    static MapLocation bfsPath14;
    static MapLocation bfsPath15;
    static MapLocation bfsPath16;
    static MapLocation bfsPath17;
    static MapLocation bfsPath18;
    static MapLocation bfsPath19;
    static MapLocation bfsPath20;
    static MapLocation bfsPath21;
    static MapLocation bfsPath22;
    static MapLocation bfsPath23;
    static MapLocation bfsPath24;
    static void appendPosition(MapLocation loc) {
        switch(bfsPathLength) {
            case 0 -> {
                bfsPath0 = loc;
                bfsPathLength = 1;
            }
            case 1 -> {
                bfsPath1 = loc;
                bfsPathLength = 2;
            }
            case 2 -> {
                bfsPath2 = loc;
                bfsPathLength = 3;
            }
            case 3 -> {
                bfsPath3 = loc;
                bfsPathLength = 4;
            }
            case 4 -> {
                bfsPath4 = loc;
                bfsPathLength = 5;
            }
            case 5 -> {
                bfsPath5 = loc;
                bfsPathLength = 6;
            }
            case 6 -> {
                bfsPath6 = loc;
                bfsPathLength = 7;
            }
            case 7 -> {
                bfsPath7 = loc;
                bfsPathLength = 8;
            }
            case 8 -> {
                bfsPath8 = loc;
                bfsPathLength = 9;
            }
            case 9 -> {
                bfsPath9 = loc;
                bfsPathLength = 10;
            }
            case 10 -> {
                bfsPath10 = loc;
                bfsPathLength = 11;
            }
            case 11 -> {
                bfsPath11 = loc;
                bfsPathLength = 12;
            }
            case 12 -> {
                bfsPath12 = loc;
                bfsPathLength = 13;
            }
            case 13 -> {
                bfsPath13 = loc;
                bfsPathLength = 14;
            }
            case 14 -> {
                bfsPath14 = loc;
                bfsPathLength = 15;
            }
            case 15 -> {
                bfsPath15 = loc;
                bfsPathLength = 16;
            }
            case 16 -> {
                bfsPath16 = loc;
                bfsPathLength = 17;
            }
            case 17 -> {
                bfsPath17 = loc;
                bfsPathLength = 18;
            }
            case 18 -> {
                bfsPath18 = loc;
                bfsPathLength = 19;
            }
            case 19 -> {
                bfsPath19 = loc;
                bfsPathLength = 20;
            }
            case 20 -> {
                bfsPath20 = loc;
                bfsPathLength = 21;
            }
            case 21 -> {
                bfsPath21 = loc;
                bfsPathLength = 22;
            }
            case 22 -> {
                bfsPath22 = loc;
                bfsPathLength = 23;
            }
            case 23 -> {
                bfsPath23 = loc;
                bfsPathLength = 24;
            }
            case 24 -> {
                bfsPath24 = loc;
                bfsPathLength = 25;
            }
            default -> {
                assert false;
            }
        }
    }
    static MapLocation getRecentPos(int i) {
        switch(bfsPathLength - i - 1) {
            case 0 -> {
                return bfsPath0;
            }
            case 1 -> {
                return bfsPath1;
            }
            case 2 -> {
                return bfsPath2;
            }
            case 3 -> {
                return bfsPath3;
            }
            case 4 -> {
                return bfsPath4;
            }
            case 5 -> {
                return bfsPath5;
            }
            case 6 -> {
                return bfsPath6;
            }
            case 7 -> {
                return bfsPath7;
            }
            case 8 -> {
                return bfsPath8;
            }
            case 9 -> {
                return bfsPath9;
            }
            case 10 -> {
                return bfsPath10;
            }
            case 11 -> {
                return bfsPath11;
            }
            case 12 -> {
                return bfsPath12;
            }
            case 13 -> {
                return bfsPath13;
            }
            case 14 -> {
                return bfsPath14;
            }
            case 15 -> {
                return bfsPath15;
            }
            case 16 -> {
                return bfsPath16;
            }
            case 17 -> {
                return bfsPath17;
            }
            case 18 -> {
                return bfsPath18;
            }
            case 19 -> {
                return bfsPath19;
            }
            case 20 -> {
                return bfsPath20;
            }
            case 21 -> {
                return bfsPath21;
            }
            case 22 -> {
                return bfsPath22;
            }
            case 23 -> {
                return bfsPath23;
            }
            case 24 -> {
                return bfsPath24;
            }
            default -> {
                assert false : "index out of range";
                return null;
            }
        }
    }

    public static void moveForward() throws GameActionException {
        rawMove();
    }

    public static void makeMove(Direction dir) throws GameActionException {
        if(rc.canTurn()) turn(dir);
        var nextLoc = myLoc.add(dir);
        if(canSense(myLoc, nextLoc, myDir) && rc.senseMapInfo(nextLoc).isDirt()) {
            if(rc.canRemoveDirt(nextLoc)) rc.removeDirt(nextLoc);
            else return;
        }
        if(dir == myDir) {
            if(rc.canMoveForward()) moveForward();
        } else {
            // if we can't turn, then strafe
            if(rc.canMove(dir)) rawMove(dir);
            // assuming that moving is the last thing we are doing this turn, so no need to update CellTracker here
        }
    }

    static MapLocation lastBugPos, lastLoc, secondLastBugPos, secondLastLoc;
    static int reps = 0;

    public static void makeMove(MapLocation target) throws GameActionException {
        // TODO: better handling when target changes
        if(bug == null || !target.equals(bug.target)) {
            if(bug != null && target.isAdjacentTo(bug.target)) {
                indicate("target changed but adjacent, keep going");
                bug.target = target;
            } else {
                indicate("RETARGET");
                lastBugPos = lastLoc = secondLastBugPos = secondLastLoc = new MapLocation(-1, -1);
                bug = new BugnavInstance(true, target);
            }
        }
        int shade = 255;
        // Profiler.begin();
        bugLoop: {
            rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
            rc.setIndicatorDot(bug.pos, 0, Math.max(0, shade -= 30), 0);
            Profiler.begin();
            if(!bug.advance()) break bugLoop;
            Profiler.end("bug");
        }
        // Profiler.end("bug");

        if(!rc.isMovementReady()) return;

        if(bug.pos == secondLastBugPos && myLoc.equals(secondLastLoc)) {
            reps++;
            if(reps >= 5) {
                indicate("stuck, resetting bug");
                System.out.println("stuck, resetting bug");
                bug.clearPath();
                bug.advance();
                lastBugPos = lastLoc = new MapLocation(-1, -1);
                reps = 0;
            }
        } else {
            reps = 0;
        }
        if(!lastLoc.equals(myLoc) || !lastBugPos.equals(bug.pos)) {
            secondLastBugPos = lastBugPos;
            secondLastLoc = lastLoc;
            lastBugPos = bug.pos;
            lastLoc = myLoc;
        }
        rc.setIndicatorDot(bug.pos, 255, 0, 0);

        // no bfs needed
        {
            var dir = myLoc.directionTo(bug.pos);
            if(myLoc.add(dir).equals(bug.pos)) {
                if(dir != Direction.CENTER) {
                    indicate("adjacent");
                    makeMove(dir);
                }
                return;
            }
        }
        // TODO: what if location == bug.pos?

        // bfs
        Profiler.begin();
        CellTracker.computeLocalBfsMasks();
        var localPassableExceptAllies0 = CellTracker.localPassable0 & ~(1l << 60);
        var localPassableExceptAllies1 = CellTracker.localPassable1;
        // can't dig dirt if we don't have enough cheese or are a rat king
        if(rc.getAllCheese() < GameConstants.DIG_DIRT_CHEESE_COST || isRatKing) {
            localPassableExceptAllies0 &= ~CellTracker.localDirt0;
            localPassableExceptAllies1 &= ~CellTracker.localDirt1;
            // minkowski sum the walls for the rat king
            if(isRatKing) {
                localPassableExceptAllies0 |= (localPassableExceptAllies0 & 0xfbff7feffdffbffl) << 1;
                localPassableExceptAllies1 |= (localPassableExceptAllies1 & 0x7feffdffbff7fefl) << 1;
                localPassableExceptAllies0 |= (localPassableExceptAllies0 & 0x1f7feffdffbff7fel) >> 1;
                localPassableExceptAllies1 |= (localPassableExceptAllies1 & 0xffdffbff7feffdel) >> 1;

                localPassableExceptAllies1 |= ((localPassableExceptAllies1 & 0x1ffffffffffffl) << 11) | (localPassableExceptAllies0 >> 50);
                localPassableExceptAllies0 |= (localPassableExceptAllies0 & 0x3ffffffffffffl) << 11;
                localPassableExceptAllies0 |= (localPassableExceptAllies0 >> 11) | ((localPassableExceptAllies1 & 0x7ffl) << 50);
                localPassableExceptAllies1 |= localPassableExceptAllies1 >> 11;
            }
        }
        // IF TLE: unroll this
        for(int i = RobotTracker.allyCount - 1; i >= 0; i--) {
                    {
            var pos = RobotTracker.allies[i];
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    localPassableExceptAllies0 &= ~ (1l << idx);
                } else {
                    localPassableExceptAllies1 &= ~ (1l << (idx - 61));
                }
            }
        }

        }
        // d{i}0 + d{i}1 is the mask of reachable within i steps
        long d00 = 0, d01 = 0;
        long d10 = 0, d11 = 0;

                {
            var pos = bug.getRecentPos(0 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d00 |= (1l << idx);
                } else {
                    d01 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d00, d01);
        //     Clock.yield();
        // }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d00 |= (d00 & 0xfbff7feffdffbffl) << 1;
        d01 |= (d01 & 0x7feffdffbff7fefl) << 1;
        d00 |= (d00 & 0x1f7feffdffbff7fel) >> 1;
        d01 |= (d01 & 0xffdffbff7feffdel) >> 1;

        d01 |= ((d01 & 0x1ffffffffffffl) << 11) | (d00 >> 50);
        d00 |= (d00 & 0x3ffffffffffffl) << 11;
        d00 |= (d00 >> 11) | ((d01 & 0x7ffl) << 50);
        d01 |= d01 >> 11;
        d00 &= localPassableExceptAllies0;
        d01 &= localPassableExceptAllies1;
        long d20 = d00;
        long d21 = d01;
        d00 &= ~CellTracker.localDirt0;
        d01 &= ~CellTracker.localDirt1;
        d10 |= d00;
        d11 |= d01;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d10, d11);
        //     Clock.yield();
        // }
        if((d10 & 0x807000000000000l) != 0 || (d11 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d10 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d11 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d11 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d10 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d10 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d10 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d11 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d11 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 1);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d10 |= (d10 & 0xfbff7feffdffbffl) << 1;
        d11 |= (d11 & 0x7feffdffbff7fefl) << 1;
        d10 |= (d10 & 0x1f7feffdffbff7fel) >> 1;
        d11 |= (d11 & 0xffdffbff7feffdel) >> 1;

        d11 |= ((d11 & 0x1ffffffffffffl) << 11) | (d10 >> 50);
        d10 |= (d10 & 0x3ffffffffffffl) << 11;
        d10 |= (d10 >> 11) | ((d11 & 0x7ffl) << 50);
        d11 |= d11 >> 11;
        d10 &= localPassableExceptAllies0;
        d11 &= localPassableExceptAllies1;
        long d30 = d10;
        long d31 = d11;
        d10 &= ~CellTracker.localDirt0;
        d11 &= ~CellTracker.localDirt1;
        d20 |= d10;
        d21 |= d11;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(2 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d20 |= (1l << idx);
                } else {
                    d21 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d20, d21);
        //     Clock.yield();
        // }
        if((d20 & 0x807000000000000l) != 0 || (d21 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d20 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d21 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d21 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d20 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d20 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d20 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d21 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d21 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 2);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d20 |= (d20 & 0xfbff7feffdffbffl) << 1;
        d21 |= (d21 & 0x7feffdffbff7fefl) << 1;
        d20 |= (d20 & 0x1f7feffdffbff7fel) >> 1;
        d21 |= (d21 & 0xffdffbff7feffdel) >> 1;

        d21 |= ((d21 & 0x1ffffffffffffl) << 11) | (d20 >> 50);
        d20 |= (d20 & 0x3ffffffffffffl) << 11;
        d20 |= (d20 >> 11) | ((d21 & 0x7ffl) << 50);
        d21 |= d21 >> 11;
        d20 &= localPassableExceptAllies0;
        d21 &= localPassableExceptAllies1;
        long d40 = d20;
        long d41 = d21;
        d20 &= ~CellTracker.localDirt0;
        d21 &= ~CellTracker.localDirt1;
        d30 |= d20;
        d31 |= d21;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d30, d31);
        //     Clock.yield();
        // }
        if((d30 & 0x807000000000000l) != 0 || (d31 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d30 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d31 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d31 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d30 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d30 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d30 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d31 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d31 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 3);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d30 |= (d30 & 0xfbff7feffdffbffl) << 1;
        d31 |= (d31 & 0x7feffdffbff7fefl) << 1;
        d30 |= (d30 & 0x1f7feffdffbff7fel) >> 1;
        d31 |= (d31 & 0xffdffbff7feffdel) >> 1;

        d31 |= ((d31 & 0x1ffffffffffffl) << 11) | (d30 >> 50);
        d30 |= (d30 & 0x3ffffffffffffl) << 11;
        d30 |= (d30 >> 11) | ((d31 & 0x7ffl) << 50);
        d31 |= d31 >> 11;
        d30 &= localPassableExceptAllies0;
        d31 &= localPassableExceptAllies1;
        long d50 = d30;
        long d51 = d31;
        d30 &= ~CellTracker.localDirt0;
        d31 &= ~CellTracker.localDirt1;
        d40 |= d30;
        d41 |= d31;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(4 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d40 |= (1l << idx);
                } else {
                    d41 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d40, d41);
        //     Clock.yield();
        // }
        if((d40 & 0x807000000000000l) != 0 || (d41 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d40 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d41 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d41 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d40 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d40 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d40 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d41 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d41 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 4);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d40 |= (d40 & 0xfbff7feffdffbffl) << 1;
        d41 |= (d41 & 0x7feffdffbff7fefl) << 1;
        d40 |= (d40 & 0x1f7feffdffbff7fel) >> 1;
        d41 |= (d41 & 0xffdffbff7feffdel) >> 1;

        d41 |= ((d41 & 0x1ffffffffffffl) << 11) | (d40 >> 50);
        d40 |= (d40 & 0x3ffffffffffffl) << 11;
        d40 |= (d40 >> 11) | ((d41 & 0x7ffl) << 50);
        d41 |= d41 >> 11;
        d40 &= localPassableExceptAllies0;
        d41 &= localPassableExceptAllies1;
        long d60 = d40;
        long d61 = d41;
        d40 &= ~CellTracker.localDirt0;
        d41 &= ~CellTracker.localDirt1;
        d50 |= d40;
        d51 |= d41;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d50, d51);
        //     Clock.yield();
        // }
        if((d50 & 0x807000000000000l) != 0 || (d51 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d50 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d51 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d51 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d50 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d50 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d50 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d51 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d51 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 5);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d50 |= (d50 & 0xfbff7feffdffbffl) << 1;
        d51 |= (d51 & 0x7feffdffbff7fefl) << 1;
        d50 |= (d50 & 0x1f7feffdffbff7fel) >> 1;
        d51 |= (d51 & 0xffdffbff7feffdel) >> 1;

        d51 |= ((d51 & 0x1ffffffffffffl) << 11) | (d50 >> 50);
        d50 |= (d50 & 0x3ffffffffffffl) << 11;
        d50 |= (d50 >> 11) | ((d51 & 0x7ffl) << 50);
        d51 |= d51 >> 11;
        d50 &= localPassableExceptAllies0;
        d51 &= localPassableExceptAllies1;
        long d70 = d50;
        long d71 = d51;
        d50 &= ~CellTracker.localDirt0;
        d51 &= ~CellTracker.localDirt1;
        d60 |= d50;
        d61 |= d51;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(6 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d60 |= (1l << idx);
                } else {
                    d61 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d60, d61);
        //     Clock.yield();
        // }
        if((d60 & 0x807000000000000l) != 0 || (d61 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d60 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d61 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d61 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d60 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d60 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d60 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d61 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d61 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 6);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d60 |= (d60 & 0xfbff7feffdffbffl) << 1;
        d61 |= (d61 & 0x7feffdffbff7fefl) << 1;
        d60 |= (d60 & 0x1f7feffdffbff7fel) >> 1;
        d61 |= (d61 & 0xffdffbff7feffdel) >> 1;

        d61 |= ((d61 & 0x1ffffffffffffl) << 11) | (d60 >> 50);
        d60 |= (d60 & 0x3ffffffffffffl) << 11;
        d60 |= (d60 >> 11) | ((d61 & 0x7ffl) << 50);
        d61 |= d61 >> 11;
        d60 &= localPassableExceptAllies0;
        d61 &= localPassableExceptAllies1;
        long d80 = d60;
        long d81 = d61;
        d60 &= ~CellTracker.localDirt0;
        d61 &= ~CellTracker.localDirt1;
        d70 |= d60;
        d71 |= d61;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d70, d71);
        //     Clock.yield();
        // }
        if((d70 & 0x807000000000000l) != 0 || (d71 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d70 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d71 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d71 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d70 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d70 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d70 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d71 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d71 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 7);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d70 |= (d70 & 0xfbff7feffdffbffl) << 1;
        d71 |= (d71 & 0x7feffdffbff7fefl) << 1;
        d70 |= (d70 & 0x1f7feffdffbff7fel) >> 1;
        d71 |= (d71 & 0xffdffbff7feffdel) >> 1;

        d71 |= ((d71 & 0x1ffffffffffffl) << 11) | (d70 >> 50);
        d70 |= (d70 & 0x3ffffffffffffl) << 11;
        d70 |= (d70 >> 11) | ((d71 & 0x7ffl) << 50);
        d71 |= d71 >> 11;
        d70 &= localPassableExceptAllies0;
        d71 &= localPassableExceptAllies1;
        long d90 = d70;
        long d91 = d71;
        d70 &= ~CellTracker.localDirt0;
        d71 &= ~CellTracker.localDirt1;
        d80 |= d70;
        d81 |= d71;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(8 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d80 |= (1l << idx);
                } else {
                    d81 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d80, d81);
        //     Clock.yield();
        // }
        if((d80 & 0x807000000000000l) != 0 || (d81 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d80 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d81 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d81 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d80 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d80 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d80 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d81 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d81 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 8);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d80 |= (d80 & 0xfbff7feffdffbffl) << 1;
        d81 |= (d81 & 0x7feffdffbff7fefl) << 1;
        d80 |= (d80 & 0x1f7feffdffbff7fel) >> 1;
        d81 |= (d81 & 0xffdffbff7feffdel) >> 1;

        d81 |= ((d81 & 0x1ffffffffffffl) << 11) | (d80 >> 50);
        d80 |= (d80 & 0x3ffffffffffffl) << 11;
        d80 |= (d80 >> 11) | ((d81 & 0x7ffl) << 50);
        d81 |= d81 >> 11;
        d80 &= localPassableExceptAllies0;
        d81 &= localPassableExceptAllies1;
        long d100 = d80;
        long d101 = d81;
        d80 &= ~CellTracker.localDirt0;
        d81 &= ~CellTracker.localDirt1;
        d90 |= d80;
        d91 |= d81;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d90, d91);
        //     Clock.yield();
        // }
        if((d90 & 0x807000000000000l) != 0 || (d91 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d90 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d91 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d91 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d90 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d90 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d90 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d91 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d91 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 9);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d90 |= (d90 & 0xfbff7feffdffbffl) << 1;
        d91 |= (d91 & 0x7feffdffbff7fefl) << 1;
        d90 |= (d90 & 0x1f7feffdffbff7fel) >> 1;
        d91 |= (d91 & 0xffdffbff7feffdel) >> 1;

        d91 |= ((d91 & 0x1ffffffffffffl) << 11) | (d90 >> 50);
        d90 |= (d90 & 0x3ffffffffffffl) << 11;
        d90 |= (d90 >> 11) | ((d91 & 0x7ffl) << 50);
        d91 |= d91 >> 11;
        d90 &= localPassableExceptAllies0;
        d91 &= localPassableExceptAllies1;
        long d110 = d90;
        long d111 = d91;
        d90 &= ~CellTracker.localDirt0;
        d91 &= ~CellTracker.localDirt1;
        d100 |= d90;
        d101 |= d91;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(10 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d100 |= (1l << idx);
                } else {
                    d101 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d100, d101);
        //     Clock.yield();
        // }
        if((d100 & 0x807000000000000l) != 0 || (d101 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d100 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d101 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d101 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d100 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d100 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d100 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d101 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d101 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 10);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d100 |= (d100 & 0xfbff7feffdffbffl) << 1;
        d101 |= (d101 & 0x7feffdffbff7fefl) << 1;
        d100 |= (d100 & 0x1f7feffdffbff7fel) >> 1;
        d101 |= (d101 & 0xffdffbff7feffdel) >> 1;

        d101 |= ((d101 & 0x1ffffffffffffl) << 11) | (d100 >> 50);
        d100 |= (d100 & 0x3ffffffffffffl) << 11;
        d100 |= (d100 >> 11) | ((d101 & 0x7ffl) << 50);
        d101 |= d101 >> 11;
        d100 &= localPassableExceptAllies0;
        d101 &= localPassableExceptAllies1;
        long d120 = d100;
        long d121 = d101;
        d100 &= ~CellTracker.localDirt0;
        d101 &= ~CellTracker.localDirt1;
        d110 |= d100;
        d111 |= d101;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d110, d111);
        //     Clock.yield();
        // }
        if((d110 & 0x807000000000000l) != 0 || (d111 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d110 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d111 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d111 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d110 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d110 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d110 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d111 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d111 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 11);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d110 |= (d110 & 0xfbff7feffdffbffl) << 1;
        d111 |= (d111 & 0x7feffdffbff7fefl) << 1;
        d110 |= (d110 & 0x1f7feffdffbff7fel) >> 1;
        d111 |= (d111 & 0xffdffbff7feffdel) >> 1;

        d111 |= ((d111 & 0x1ffffffffffffl) << 11) | (d110 >> 50);
        d110 |= (d110 & 0x3ffffffffffffl) << 11;
        d110 |= (d110 >> 11) | ((d111 & 0x7ffl) << 50);
        d111 |= d111 >> 11;
        d110 &= localPassableExceptAllies0;
        d111 &= localPassableExceptAllies1;
        long d130 = d110;
        long d131 = d111;
        d110 &= ~CellTracker.localDirt0;
        d111 &= ~CellTracker.localDirt1;
        d120 |= d110;
        d121 |= d111;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(12 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d120 |= (1l << idx);
                } else {
                    d121 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d120, d121);
        //     Clock.yield();
        // }
        if((d120 & 0x807000000000000l) != 0 || (d121 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d120 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d121 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d121 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d120 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d120 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d120 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d121 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d121 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 12);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d120 |= (d120 & 0xfbff7feffdffbffl) << 1;
        d121 |= (d121 & 0x7feffdffbff7fefl) << 1;
        d120 |= (d120 & 0x1f7feffdffbff7fel) >> 1;
        d121 |= (d121 & 0xffdffbff7feffdel) >> 1;

        d121 |= ((d121 & 0x1ffffffffffffl) << 11) | (d120 >> 50);
        d120 |= (d120 & 0x3ffffffffffffl) << 11;
        d120 |= (d120 >> 11) | ((d121 & 0x7ffl) << 50);
        d121 |= d121 >> 11;
        d120 &= localPassableExceptAllies0;
        d121 &= localPassableExceptAllies1;
        long d140 = d120;
        long d141 = d121;
        d120 &= ~CellTracker.localDirt0;
        d121 &= ~CellTracker.localDirt1;
        d130 |= d120;
        d131 |= d121;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d130, d131);
        //     Clock.yield();
        // }
        if((d130 & 0x807000000000000l) != 0 || (d131 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d130 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d131 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d131 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d130 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d130 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d130 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d131 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d131 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 13);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d130 |= (d130 & 0xfbff7feffdffbffl) << 1;
        d131 |= (d131 & 0x7feffdffbff7fefl) << 1;
        d130 |= (d130 & 0x1f7feffdffbff7fel) >> 1;
        d131 |= (d131 & 0xffdffbff7feffdel) >> 1;

        d131 |= ((d131 & 0x1ffffffffffffl) << 11) | (d130 >> 50);
        d130 |= (d130 & 0x3ffffffffffffl) << 11;
        d130 |= (d130 >> 11) | ((d131 & 0x7ffl) << 50);
        d131 |= d131 >> 11;
        d130 &= localPassableExceptAllies0;
        d131 &= localPassableExceptAllies1;
        long d150 = d130;
        long d151 = d131;
        d130 &= ~CellTracker.localDirt0;
        d131 &= ~CellTracker.localDirt1;
        d140 |= d130;
        d141 |= d131;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(14 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d140 |= (1l << idx);
                } else {
                    d141 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d140, d141);
        //     Clock.yield();
        // }
        if((d140 & 0x807000000000000l) != 0 || (d141 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d140 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d141 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d141 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d140 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d140 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d140 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d141 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d141 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 14);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d140 |= (d140 & 0xfbff7feffdffbffl) << 1;
        d141 |= (d141 & 0x7feffdffbff7fefl) << 1;
        d140 |= (d140 & 0x1f7feffdffbff7fel) >> 1;
        d141 |= (d141 & 0xffdffbff7feffdel) >> 1;

        d141 |= ((d141 & 0x1ffffffffffffl) << 11) | (d140 >> 50);
        d140 |= (d140 & 0x3ffffffffffffl) << 11;
        d140 |= (d140 >> 11) | ((d141 & 0x7ffl) << 50);
        d141 |= d141 >> 11;
        d140 &= localPassableExceptAllies0;
        d141 &= localPassableExceptAllies1;
        long d160 = d140;
        long d161 = d141;
        d140 &= ~CellTracker.localDirt0;
        d141 &= ~CellTracker.localDirt1;
        d150 |= d140;
        d151 |= d141;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d150, d151);
        //     Clock.yield();
        // }
        if((d150 & 0x807000000000000l) != 0 || (d151 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d150 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d151 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d151 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d150 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d150 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d150 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d151 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d151 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 15);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d150 |= (d150 & 0xfbff7feffdffbffl) << 1;
        d151 |= (d151 & 0x7feffdffbff7fefl) << 1;
        d150 |= (d150 & 0x1f7feffdffbff7fel) >> 1;
        d151 |= (d151 & 0xffdffbff7feffdel) >> 1;

        d151 |= ((d151 & 0x1ffffffffffffl) << 11) | (d150 >> 50);
        d150 |= (d150 & 0x3ffffffffffffl) << 11;
        d150 |= (d150 >> 11) | ((d151 & 0x7ffl) << 50);
        d151 |= d151 >> 11;
        d150 &= localPassableExceptAllies0;
        d151 &= localPassableExceptAllies1;
        long d170 = d150;
        long d171 = d151;
        d150 &= ~CellTracker.localDirt0;
        d151 &= ~CellTracker.localDirt1;
        d160 |= d150;
        d161 |= d151;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(16 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d160 |= (1l << idx);
                } else {
                    d161 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d160, d161);
        //     Clock.yield();
        // }
        if((d160 & 0x807000000000000l) != 0 || (d161 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d160 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d161 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d161 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d160 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d160 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d160 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d161 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d161 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 16);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d160 |= (d160 & 0xfbff7feffdffbffl) << 1;
        d161 |= (d161 & 0x7feffdffbff7fefl) << 1;
        d160 |= (d160 & 0x1f7feffdffbff7fel) >> 1;
        d161 |= (d161 & 0xffdffbff7feffdel) >> 1;

        d161 |= ((d161 & 0x1ffffffffffffl) << 11) | (d160 >> 50);
        d160 |= (d160 & 0x3ffffffffffffl) << 11;
        d160 |= (d160 >> 11) | ((d161 & 0x7ffl) << 50);
        d161 |= d161 >> 11;
        d160 &= localPassableExceptAllies0;
        d161 &= localPassableExceptAllies1;
        long d180 = d160;
        long d181 = d161;
        d160 &= ~CellTracker.localDirt0;
        d161 &= ~CellTracker.localDirt1;
        d170 |= d160;
        d171 |= d161;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d170, d171);
        //     Clock.yield();
        // }
        if((d170 & 0x807000000000000l) != 0 || (d171 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d170 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d171 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d171 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d170 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d170 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d170 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d171 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d171 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 17);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d170 |= (d170 & 0xfbff7feffdffbffl) << 1;
        d171 |= (d171 & 0x7feffdffbff7fefl) << 1;
        d170 |= (d170 & 0x1f7feffdffbff7fel) >> 1;
        d171 |= (d171 & 0xffdffbff7feffdel) >> 1;

        d171 |= ((d171 & 0x1ffffffffffffl) << 11) | (d170 >> 50);
        d170 |= (d170 & 0x3ffffffffffffl) << 11;
        d170 |= (d170 >> 11) | ((d171 & 0x7ffl) << 50);
        d171 |= d171 >> 11;
        d170 &= localPassableExceptAllies0;
        d171 &= localPassableExceptAllies1;
        long d190 = d170;
        long d191 = d171;
        d170 &= ~CellTracker.localDirt0;
        d171 &= ~CellTracker.localDirt1;
        d180 |= d170;
        d181 |= d171;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(18 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d180 |= (1l << idx);
                } else {
                    d181 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d180, d181);
        //     Clock.yield();
        // }
        if((d180 & 0x807000000000000l) != 0 || (d181 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d180 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d181 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d181 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d180 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d180 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d180 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d181 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d181 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 18);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d180 |= (d180 & 0xfbff7feffdffbffl) << 1;
        d181 |= (d181 & 0x7feffdffbff7fefl) << 1;
        d180 |= (d180 & 0x1f7feffdffbff7fel) >> 1;
        d181 |= (d181 & 0xffdffbff7feffdel) >> 1;

        d181 |= ((d181 & 0x1ffffffffffffl) << 11) | (d180 >> 50);
        d180 |= (d180 & 0x3ffffffffffffl) << 11;
        d180 |= (d180 >> 11) | ((d181 & 0x7ffl) << 50);
        d181 |= d181 >> 11;
        d180 &= localPassableExceptAllies0;
        d181 &= localPassableExceptAllies1;
        long d200 = d180;
        long d201 = d181;
        d180 &= ~CellTracker.localDirt0;
        d181 &= ~CellTracker.localDirt1;
        d190 |= d180;
        d191 |= d181;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d190, d191);
        //     Clock.yield();
        // }
        if((d190 & 0x807000000000000l) != 0 || (d191 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d190 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d191 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d191 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d190 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d190 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d190 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d191 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d191 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 19);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d190 |= (d190 & 0xfbff7feffdffbffl) << 1;
        d191 |= (d191 & 0x7feffdffbff7fefl) << 1;
        d190 |= (d190 & 0x1f7feffdffbff7fel) >> 1;
        d191 |= (d191 & 0xffdffbff7feffdel) >> 1;

        d191 |= ((d191 & 0x1ffffffffffffl) << 11) | (d190 >> 50);
        d190 |= (d190 & 0x3ffffffffffffl) << 11;
        d190 |= (d190 >> 11) | ((d191 & 0x7ffl) << 50);
        d191 |= d191 >> 11;
        d190 &= localPassableExceptAllies0;
        d191 &= localPassableExceptAllies1;
        long d210 = d190;
        long d211 = d191;
        d190 &= ~CellTracker.localDirt0;
        d191 &= ~CellTracker.localDirt1;
        d200 |= d190;
        d201 |= d191;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(20 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d200 |= (1l << idx);
                } else {
                    d201 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d200, d201);
        //     Clock.yield();
        // }
        if((d200 & 0x807000000000000l) != 0 || (d201 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d200 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d201 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d201 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d200 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d200 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d200 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d201 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d201 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 20);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d200 |= (d200 & 0xfbff7feffdffbffl) << 1;
        d201 |= (d201 & 0x7feffdffbff7fefl) << 1;
        d200 |= (d200 & 0x1f7feffdffbff7fel) >> 1;
        d201 |= (d201 & 0xffdffbff7feffdel) >> 1;

        d201 |= ((d201 & 0x1ffffffffffffl) << 11) | (d200 >> 50);
        d200 |= (d200 & 0x3ffffffffffffl) << 11;
        d200 |= (d200 >> 11) | ((d201 & 0x7ffl) << 50);
        d201 |= d201 >> 11;
        d200 &= localPassableExceptAllies0;
        d201 &= localPassableExceptAllies1;
        long d220 = d200;
        long d221 = d201;
        d200 &= ~CellTracker.localDirt0;
        d201 &= ~CellTracker.localDirt1;
        d210 |= d200;
        d211 |= d201;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d210, d211);
        //     Clock.yield();
        // }
        if((d210 & 0x807000000000000l) != 0 || (d211 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d210 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d211 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d211 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d210 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d210 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d210 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d211 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d211 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 21);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d210 |= (d210 & 0xfbff7feffdffbffl) << 1;
        d211 |= (d211 & 0x7feffdffbff7fefl) << 1;
        d210 |= (d210 & 0x1f7feffdffbff7fel) >> 1;
        d211 |= (d211 & 0xffdffbff7feffdel) >> 1;

        d211 |= ((d211 & 0x1ffffffffffffl) << 11) | (d210 >> 50);
        d210 |= (d210 & 0x3ffffffffffffl) << 11;
        d210 |= (d210 >> 11) | ((d211 & 0x7ffl) << 50);
        d211 |= d211 >> 11;
        d210 &= localPassableExceptAllies0;
        d211 &= localPassableExceptAllies1;
        long d230 = d210;
        long d231 = d211;
        d210 &= ~CellTracker.localDirt0;
        d211 &= ~CellTracker.localDirt1;
        d220 |= d210;
        d221 |= d211;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(22 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d220 |= (1l << idx);
                } else {
                    d221 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d220, d221);
        //     Clock.yield();
        // }
        if((d220 & 0x807000000000000l) != 0 || (d221 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d220 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d221 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d221 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d220 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d220 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d220 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d221 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d221 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 22);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d220 |= (d220 & 0xfbff7feffdffbffl) << 1;
        d221 |= (d221 & 0x7feffdffbff7fefl) << 1;
        d220 |= (d220 & 0x1f7feffdffbff7fel) >> 1;
        d221 |= (d221 & 0xffdffbff7feffdel) >> 1;

        d221 |= ((d221 & 0x1ffffffffffffl) << 11) | (d220 >> 50);
        d220 |= (d220 & 0x3ffffffffffffl) << 11;
        d220 |= (d220 >> 11) | ((d221 & 0x7ffl) << 50);
        d221 |= d221 >> 11;
        d220 &= localPassableExceptAllies0;
        d221 &= localPassableExceptAllies1;
        long d240 = d220;
        long d241 = d221;
        d220 &= ~CellTracker.localDirt0;
        d221 &= ~CellTracker.localDirt1;
        d230 |= d220;
        d231 |= d221;
        // Profiler.end("bfs iteration");
        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d230, d231);
        //     Clock.yield();
        // }
        if((d230 & 0x807000000000000l) != 0 || (d231 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d230 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d231 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d231 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d230 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d230 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d230 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d231 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d231 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 23);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d230 |= (d230 & 0xfbff7feffdffbffl) << 1;
        d231 |= (d231 & 0x7feffdffbff7fefl) << 1;
        d230 |= (d230 & 0x1f7feffdffbff7fel) >> 1;
        d231 |= (d231 & 0xffdffbff7feffdel) >> 1;

        d231 |= ((d231 & 0x1ffffffffffffl) << 11) | (d230 >> 50);
        d230 |= (d230 & 0x3ffffffffffffl) << 11;
        d230 |= (d230 >> 11) | ((d231 & 0x7ffl) << 50);
        d231 |= d231 >> 11;
        d230 &= localPassableExceptAllies0;
        d231 &= localPassableExceptAllies1;
        long d250 = d230;
        long d251 = d231;
        d230 &= ~CellTracker.localDirt0;
        d231 &= ~CellTracker.localDirt1;
        d240 |= d230;
        d241 |= d231;
        // Profiler.end("bfs iteration");
                {
            var pos = bug.getRecentPos(24 / 2);
            int xi = pos.x - myLoc.x + 5;
            int yi = pos.y - myLoc.y + 5;
            if(xi >= 0 && xi < 11 && yi >= 0 && yi < 11)
            {
                int idx = yi * 11 + xi;
                if(idx < 61) {
                    d240 |= (1l << idx);
                } else {
                    d241 |= (1l << (idx - 61));
                }
            }
        }

        // if(rc.getRoundNum() >= 731 && rc.getID() == 10281) {
        //     indicatorString = "";
        //     CellTracker.printLocalMask(d240, d241);
        //     Clock.yield();
        // }
        if((d240 & 0x807000000000000l) != 0 || (d241 & 0xe01) != 0) {
            // Profiler.begin();
            // this is sloppy since we only care about tiles <= 2 chebyshev distance away
            long notWallAdjacent0 = CellTracker.localPassable0, notWallAdjacent1 = CellTracker.localPassable1;
            notWallAdjacent1 &= (notWallAdjacent1 << 1) | (notWallAdjacent0 >> 60);
            notWallAdjacent0 &= notWallAdjacent0 << 1;
            notWallAdjacent0 &= (notWallAdjacent0 >> 1) | ((notWallAdjacent1 & 1l) << 60);
            notWallAdjacent1 &= notWallAdjacent1 >> 1;
            notWallAdjacent1 &= (notWallAdjacent1 << 11) | (notWallAdjacent0 >> 50);
            notWallAdjacent0 &= notWallAdjacent0 << 11;
            notWallAdjacent0 &= (notWallAdjacent0 >> 11) | ((notWallAdjacent1 & 0x7ffl) << 50);
            notWallAdjacent1 &= notWallAdjacent1 >> 11;

            Direction bestMove = null;
            double bestScore = 1e18;
            if(
               (d240 & (1l << 49))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 49))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d241 & (1l << 0))
               != 0) {
                var nextLoc = myLoc.add(Direction.EAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.EAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 0))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.EAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.EAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d241 & (1l << 10))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTH);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTH) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 10))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTH == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTH;
                        bestScore = score;
                    }
                }
            }
            if(
               (d240 & (1l << 59))
               != 0) {
                var nextLoc = myLoc.add(Direction.WEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.WEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 59))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.WEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.WEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d240 & (1l << 48))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 48))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d240 & (1l << 50))
               != 0) {
                var nextLoc = myLoc.add(Direction.SOUTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.SOUTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent0 & (1l << 50))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.SOUTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.SOUTHEAST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d241 & (1l << 9))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHWEST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHWEST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 9))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHWEST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHWEST;
                        bestScore = score;
                    }
                }
            }
            if(
               (d241 & (1l << 11))
               != 0) {
                var nextLoc = myLoc.add(Direction.NORTHEAST);
                boolean isDirt = false;
                if(rc.canMove(Direction.NORTHEAST) || (isDirt = CellTracker.isDirt(nextLoc))) {
                    double score = nextLoc.distanceSquaredTo(target) + nextLoc.distanceSquaredTo(bug.pos);
                    // prefer staying away from walls, this helps when changing direction
                    if(
                       (notWallAdjacent1 & (1l << 11))
                       != 0
                       ) {
                        score /= 2;
                    }
                    // prefer definitely empty squares over possibly dirt
                    if(isDirt) {
                        score += 1000;
                    }
                    // if we can't turn, then prefer moving straight
                    // however, we still prefer strafing to moving forward into dirt
                    if(!rc.canTurn() && Direction.NORTHEAST == myDir) {
                        score -= 500;
                    }
                    if(score < bestScore) {
                        bestMove = Direction.NORTHEAST;
                        bestScore = score;
                    }
                }
            }
            // Profiler.end("bfs best move");
            if(bestMove != null) {
                Profiler.end("bfs");
                indicate("bfs iterations: " + 24);
                makeMove(bestMove);
                return;
            }
        }

        // Profiler.begin();

        // we don't need to worry about the "seam" here because it's at the cell we are standing on
        d240 |= (d240 & 0xfbff7feffdffbffl) << 1;
        d241 |= (d241 & 0x7feffdffbff7fefl) << 1;
        d240 |= (d240 & 0x1f7feffdffbff7fel) >> 1;
        d241 |= (d241 & 0xffdffbff7feffdel) >> 1;

        d241 |= ((d241 & 0x1ffffffffffffl) << 11) | (d240 >> 50);
        d240 |= (d240 & 0x3ffffffffffffl) << 11;
        d240 |= (d240 >> 11) | ((d241 & 0x7ffl) << 50);
        d241 |= d241 >> 11;
        d240 &= localPassableExceptAllies0;
        d241 &= localPassableExceptAllies1;
        long d260 = d240;
        long d261 = d241;
        d240 &= ~CellTracker.localDirt0;
        d241 &= ~CellTracker.localDirt1;
        d250 |= d240;
        d251 |= d241;
        // Profiler.end("bfs iteration");
        Profiler.end("bfs");

        System.out.println("fallback when bfs can't find path");
        indicate("fallback when bfs can't find path");

        // move in a random direction (to make way for any allies blocking us)
        var dirs = new Direction[8];
        int dirCount = 0;
        if(rc.canMove(Direction.NORTH)) dirs[dirCount++] = Direction.NORTH;
        if(rc.canMove(Direction.NORTHEAST)) dirs[dirCount++] = Direction.NORTHEAST;
        if(rc.canMove(Direction.EAST)) dirs[dirCount++] = Direction.EAST;
        if(rc.canMove(Direction.SOUTHEAST)) dirs[dirCount++] = Direction.SOUTHEAST;
        if(rc.canMove(Direction.SOUTH)) dirs[dirCount++] = Direction.SOUTH;
        if(rc.canMove(Direction.SOUTHWEST)) dirs[dirCount++] = Direction.SOUTHWEST;
        if(rc.canMove(Direction.WEST)) dirs[dirCount++] = Direction.WEST;
        if(rc.canMove(Direction.NORTHWEST)) dirs[dirCount++] = Direction.NORTHWEST;
        if(dirCount > 0) makeMove(dirs[Rng.mcg() % dirCount]);

        bug.clearPath();
    }
}